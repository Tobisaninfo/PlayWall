use rodio::{Decoder, Source};
use std::fs::File;
use std::io::BufReader;

pub struct LoopingSource {
    path: String,
    current_source: Box<dyn Source<Item=f32> + Send>,
}

impl LoopingSource {
    pub fn new(path: String) -> Self {
        let file = File::open(&path).expect("Failed to open file for looping");
        let reader = BufReader::new(file);
        let source = Decoder::new(reader).expect("Failed to create decoder for looping");
        Self {
            path,
            current_source: Box::new(source),
        }
    }
}

impl Iterator for LoopingSource {
    type Item = f32;

    fn next(&mut self) -> Option<Self::Item> {
        if let Some(sample) = self.current_source.next() {
            return Some(sample);
        }

        let file = File::open(&self.path).ok()?;
        let reader = BufReader::new(file);
        if let Ok(source) = Decoder::new(reader) {
            self.current_source = Box::new(source);
            self.current_source.next()
        } else {
            None
        }
    }
}

impl Source for LoopingSource {
    fn current_span_len(&self) -> Option<usize> {
        self.current_source.current_span_len()
    }

    fn channels(&self) -> u16 {
        self.current_source.channels()
    }

    fn sample_rate(&self) -> u32 {
        self.current_source.sample_rate()
    }
    fn total_duration(&self) -> Option<std::time::Duration> {
        None
    }
}
