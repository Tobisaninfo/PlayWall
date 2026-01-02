use rodio::Source;
use std::time::Duration;

pub struct EofCallbackSource<S, F>
where
    S: Source,
    F: FnOnce() + Send + 'static,
{
    inner: S,
    callback: Option<F>,
}

impl<S, F> EofCallbackSource<S, F>
where
    S: Source,
    F: FnOnce() + Send + 'static,
{
    pub fn new(source: S, callback: F) -> Self {
        Self {
            inner: source,
            callback: Some(callback),
        }
    }
}

impl<S, F> Iterator for EofCallbackSource<S, F>
where
    S: Source,
    F: FnOnce() + Send + 'static,
{
    type Item = S::Item;

    fn next(&mut self) -> Option<Self::Item> {
        match self.inner.next() {
            Some(sample) => Some(sample),
            None => {
                // EOF erreicht → Callback einmalig ausführen
                if let Some(cb) = self.callback.take() {
                    cb();
                }
                None
            }
        }
    }
}

impl<S, F> Source for EofCallbackSource<S, F>
where
    S: Source,
    F: FnOnce() + Send + 'static,
{
    fn current_span_len(&self) -> Option<usize> {
        self.inner.current_span_len()
    }

    fn channels(&self) -> u16 {
        self.inner.channels()
    }

    fn sample_rate(&self) -> u32 {
        self.inner.sample_rate()
    }

    fn total_duration(&self) -> Option<Duration> {
        self.inner.total_duration()
    }
}
