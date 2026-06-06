use rodio::source::SeekError;
use rodio::{ChannelCount, SampleRate, Source};
use std::collections::VecDeque;
use std::f32::consts::PI;
use std::sync::atomic::{AtomicU32, Ordering};
use std::sync::Arc;
use std::time::Duration;

// Larger window → better quality (~46ms at 44100 Hz)
const WINDOW_FRAMES: usize = 2048;
const HOP_FRAMES: usize = 1024; // 50% overlap → Hann COLA condition → perfect reconstruction at speed=1.0
const SEARCH_FRAMES: usize = 256; // search range ±256 frames (~6ms at 44100 Hz)

pub struct WsolaSource<S: Source<Item=f32> + Send> {
    inner: S,
    channels: usize,
    speed: Arc<AtomicU32>,
    hann: Vec<f32>,
    input_buf: VecDeque<f32>,
    input_exhausted: bool,
    output_queue: VecDeque<f32>,
    accum: Vec<f32>,     // OLA accumulation buffer [WINDOW_FRAMES * channels]
    prev_hop: Vec<f32>,  // last emitted hop, used as cross-correlation template
    analysis_pos: f64,   // fractional frame position within input_buf
    exhausted: bool,
}

impl<S: Source<Item=f32> + Send> WsolaSource<S> {
    pub fn new(inner: S, speed: Arc<AtomicU32>) -> Self {
        let channels = inner.channels().get() as usize;
        let window_samples = WINDOW_FRAMES * channels;
        let hop_samples = HOP_FRAMES * channels;

        // Symmetric Hann window — zero at both ends, same coefficient repeated per channel
        let hann: Vec<f32> = (0..window_samples)
            .map(|i| {
                let frame = i / channels;
                0.5 * (1.0 - (2.0 * PI * frame as f32 / (WINDOW_FRAMES - 1) as f32).cos())
            })
            .collect();

        Self {
            inner,
            channels,
            speed,
            hann,
            input_buf: VecDeque::new(),
            input_exhausted: false,
            output_queue: VecDeque::new(),
            accum: vec![0.0f32; window_samples],
            prev_hop: vec![0.0f32; hop_samples],
            analysis_pos: 0.0,
            exhausted: false,
        }
    }

    fn speed_f32(&self) -> f32 {
        f32::from_bits(self.speed.load(Ordering::Relaxed))
    }

    fn ensure_input(&mut self, frames: usize) {
        let target = frames * self.channels;
        while !self.input_exhausted && self.input_buf.len() < target {
            match self.inner.next() {
                Some(s) => self.input_buf.push_back(s),
                None => self.input_exhausted = true,
            }
        }
    }

    fn find_best_match(&self, search_start: usize, search_end: usize) -> usize {
        if search_start >= search_end {
            return search_start;
        }
        let ch = self.channels;
        let template = &self.prev_hop;
        let template_len = template.len();

        let template_energy: f32 = template.iter().map(|&x| x * x).sum();
        // Silent template → no meaningful correlation, pick center of search range
        if template_energy < 1e-10 {
            return (search_start + search_end) / 2;
        }

        let mut best = search_start;
        let mut best_ncc = f32::NEG_INFINITY;

        for candidate in search_start..=search_end {
            let offset = candidate * ch;
            if offset + template_len > self.input_buf.len() {
                break;
            }
            // Compute cross-correlation and candidate energy in a single pass
            let mut corr = 0.0f32;
            let mut cand_energy = 0.0f32;
            for i in 0..template_len {
                let c = self.input_buf[offset + i];
                corr += template[i] * c;
                cand_energy += c * c;
            }
            // Normalized cross-correlation avoids bias toward high-energy regions
            let ncc = corr / (template_energy * cand_energy + 1e-10).sqrt();
            if ncc > best_ncc {
                best_ncc = ncc;
                best = candidate;
            }
        }
        best
    }

    fn step(&mut self) {
        let speed = self.speed_f32().clamp(0.25, 4.0);
        let ch = self.channels;
        let window_samples = WINDOW_FRAMES * ch;
        let hop_samples = HOP_FRAMES * ch;

        // Fast pass-through for speed ≈ 1.0 (avoids windowing artifacts)
        if (speed - 1.0).abs() < 0.005 {
            self.ensure_input(HOP_FRAMES);
            for _ in 0..hop_samples {
                match self.input_buf.pop_front() {
                    Some(s) => self.output_queue.push_back(s),
                    None => {
                        self.exhausted = true;
                        return;
                    }
                }
            }
            return;
        }

        // analysis_hop = synthesis_hop * speed:
        //   speed=1.3 → consume 1.3× more input frames per output step → 1.3× faster playback
        //   speed=0.8 → consume 0.8× fewer input frames per output step → 0.8× slower playback
        let analysis_hop = HOP_FRAMES as f64 * speed as f64;
        let pos_int = self.analysis_pos as usize;

        self.ensure_input(pos_int + WINDOW_FRAMES + SEARCH_FRAMES + HOP_FRAMES);

        let avail_frames = self.input_buf.len() / ch;
        if avail_frames < WINDOW_FRAMES {
            if self.input_exhausted {
                // Flush OLA tail
                for i in 0..hop_samples {
                    self.output_queue.push_back(self.accum[i]);
                }
            }
            self.exhausted = true;
            return;
        }

        let search_start = pos_int.saturating_sub(SEARCH_FRAMES);
        let search_end = (pos_int + SEARCH_FRAMES).min(avail_frames - WINDOW_FRAMES);
        let best = self.find_best_match(search_start, search_end);
        let best_offset = best * ch;

        // Hann-window the chosen input frame and overlap-add into accum
        for i in 0..window_samples {
            let s = if best_offset + i < self.input_buf.len() {
                self.input_buf[best_offset + i]
            } else {
                0.0
            };
            self.accum[i] += self.hann[i] * s;
        }

        // Emit the first hop from accum
        for &s in &self.accum[..hop_samples] {
            self.output_queue.push_back(s);
        }

        // Save the raw input tail (last HOP_FRAMES of the selected window) as template.
        // Next search compares this tail against candidate window starts → phase continuity.
        let tail_start = (best + WINDOW_FRAMES - HOP_FRAMES) * ch;
        for i in 0..hop_samples {
            self.prev_hop[i] = if tail_start + i < self.input_buf.len() {
                self.input_buf[tail_start + i]
            } else {
                0.0
            };
        }

        // Shift accum left by hop_samples, zero the newly exposed tail
        self.accum.copy_within(hop_samples..window_samples, 0);
        self.accum[window_samples - hop_samples..].fill(0.0);

        self.analysis_pos += analysis_hop;

        // Drain input frames we can no longer need for seek-back or search
        let keep_from = (self.analysis_pos as usize).saturating_sub(SEARCH_FRAMES + WINDOW_FRAMES);
        if keep_from > 0 {
            let drain = keep_from * ch;
            if drain <= self.input_buf.len() {
                self.input_buf.drain(..drain);
                self.analysis_pos -= keep_from as f64;
            }
        }
    }
}

impl<S: Source<Item=f32> + Send> Iterator for WsolaSource<S> {
    type Item = f32;

    fn next(&mut self) -> Option<f32> {
        while !self.exhausted && self.output_queue.is_empty() {
            self.step();
        }
        self.output_queue.pop_front()
    }
}

impl<S: Source<Item=f32> + Send> Source for WsolaSource<S> {
    fn current_span_len(&self) -> Option<usize> {
        None
    }
    fn channels(&self) -> ChannelCount {
        self.inner.channels()
    }
    fn sample_rate(&self) -> SampleRate {
        self.inner.sample_rate()
    }
    fn total_duration(&self) -> Option<Duration> {
        None
    }

    fn try_seek(&mut self, pos: Duration) -> Result<(), SeekError> {
        self.inner.try_seek(pos)?;
        // Reset all WSOLA state — analysis restarts from the seeked position
        self.input_buf.clear();
        self.output_queue.clear();
        self.accum.fill(0.0);
        self.prev_hop.fill(0.0);
        self.analysis_pos = 0.0;
        self.input_exhausted = false;
        self.exhausted = false;
        Ok(())
    }
}
