use rodio::{Decoder, Source};
use std::fs::File;
use std::io::BufReader;
use std::time::Duration;

pub struct HybridLoopSource<F>
where
    F: FnOnce() + Send + 'static,
{
    path: String,
    current_source: Box<dyn Source<Item=f32> + Send>,
    callback: Option<F>,
    looping_flag_ptr: *const bool,
}

unsafe impl<F> Send for HybridLoopSource<F> where F: FnOnce() + Send + 'static {}

impl<F> HybridLoopSource<F>
where
    F: FnOnce() + Send + 'static,
{
    pub fn new(path: String, looping_flag_ptr: *const bool, callback: F) -> Self {
        let file = File::open(&path).expect("Failed to open file");
        let reader = BufReader::new(file);
        let source = Decoder::new(reader).expect("Failed to create decoder");

        Self {
            path,
            current_source: Box::new(source),
            callback: Some(callback),
            looping_flag_ptr,
        }
    }

    fn is_looping_enabled(&self) -> bool {
        unsafe { *self.looping_flag_ptr }
    }
}

impl<F> Iterator for HybridLoopSource<F>
where
    F: FnOnce() + Send + 'static,
{
    type Item = f32;

    fn next(&mut self) -> Option<Self::Item> {
        if let Some(sample) = self.current_source.next() {
            return Some(sample);
        }

        // End of current decoder reached. Check looping flag.
        if self.is_looping_enabled() {
            let file = File::open(&self.path).ok()?;
            let reader = BufReader::new(file);
            if let Ok(source) = Decoder::new(reader) {
                self.current_source = Box::new(source);
                return self.current_source.next();
            }
        }

        // Run callback
        if let Some(cb) = self.callback.take() {
            cb();
        }

        None
    }
}

impl<F> Source for HybridLoopSource<F>
where
    F: FnOnce() + Send + 'static,
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
