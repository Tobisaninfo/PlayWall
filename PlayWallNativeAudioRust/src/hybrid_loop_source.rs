use rodio::{Decoder, Source};
use std::fs::File;
use std::io::BufReader;
use std::time::Duration;

pub struct HybridLoopSource<F, P>
where
    F: FnOnce() + Send + 'static,
    P: FnMut(f32) + Send + 'static,
{
    path: String,
    current_source: Box<dyn Source<Item=f32> + Send>,
    eof_callback: Option<F>,
    progress_callback: P,
    looping_flag_ptr: *const bool,
    samples_played: u64,
}

unsafe impl<F, P> Send for HybridLoopSource<F, P>
where
    F: FnOnce() + Send + 'static,
    P: FnMut(f32) + Send + 'static,
{}

impl<F, P> HybridLoopSource<F, P>
where
    F: FnOnce() + Send + 'static,
    P: FnMut(f32) + Send + 'static,
{
    pub fn new(path: String, looping_flag_ptr: *const bool, callback: F, progress_callback: P) -> Self {
        let file = File::open(&path).expect("Failed to open file");
        let reader = BufReader::new(file);
        let source = Decoder::new(reader).expect("Failed to create decoder");

        Self {
            path,
            current_source: Box::new(source),
            eof_callback: Some(callback),
            progress_callback,
            looping_flag_ptr,
            samples_played: 0,
        }
    }

    fn is_looping_enabled(&self) -> bool {
        unsafe { *self.looping_flag_ptr }
    }

    fn elapsed_seconds(&self) -> f32 {
        self.samples_played as f32
            / (self.current_source.sample_rate() as f32 * self.current_source.channels() as f32)
    }
}

impl<F, P> Iterator for HybridLoopSource<F, P>
where
    F: FnOnce() + Send + 'static,
    P: FnMut(f32) + Send + 'static,
{
    type Item = f32;

    fn next(&mut self) -> Option<Self::Item> {
        if let Some(sample) = self.current_source.next() {
            self.samples_played += 1;

            // Report progress every 10.000 samples (~200ms at 48kHz Stereo)
            if self.samples_played % 10000 == 0 {
                let elapsed = self.elapsed_seconds();
                (self.progress_callback)(elapsed);
            }

            return Some(sample);
        }
        self.samples_played = 0;

        // End of current decoder reached. Check looping flag.
        if self.is_looping_enabled() {
            let file = File::open(&self.path).ok()?;
            let reader = BufReader::new(file);
            if let Ok(source) = Decoder::new(reader) {
                self.current_source = Box::new(source);
                self.samples_played += 1;
                return self.current_source.next();
            }
        }

        // Run callback
        if let Some(cb) = self.eof_callback.take() {
            cb();
        }

        None
    }
}

impl<F, P> Source for HybridLoopSource<F, P>
where
    F: FnOnce() + Send + 'static,
    P: FnMut(f32) + Send + 'static,
{
    fn current_span_len(&self) -> Option<usize> {
        self.current_source.current_span_len()
    }

    fn channels(&self) -> u16 {
        self.current_source.channels()
    }

    fn sample_rate(&self) -> u32 {
        self.current_source.sample_rate()
    }

    fn total_duration(&self) -> Option<Duration> {
        None
    }
}
