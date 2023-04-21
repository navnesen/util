package com.github.navnesen.streams;

import com.github.navnesen.async.AsyncResult;
import com.github.navnesen.sync.Mutex;
import com.github.navnesen.util.Dirty;
import com.github.navnesen.util.Option;

import java.util.Objects;

public class Reader<T> implements AutoCloseable {
	private final String STREAM_ACCESS_ERR = "does not have access to stream";

	protected final Mutex<Option<ReadableStream<T>>> _stream;

	protected Reader(ReadableStream<T> stream) {
		this._stream = new Mutex<>(Option.some(stream));
	}

	public void releaseLock() {
		try (var stream = this._stream.lock()) {
			stream.get().expect(STREAM_ACCESS_ERR)
				.releaseFrom(this);
		}
	}

	public AsyncResult<Void> cancel() {
		return this.cancel(Option.none());
	}

	public AsyncResult<Void> cancel(String reason) {
		return this.cancel(Option.some(reason));
	}

	public AsyncResult<Void> cancel(Option<String> reason) {
		return new AsyncResult<>(() -> {
			try (var stream = this._stream.lock()) {
				return stream.get()
					.expect(STREAM_ACCESS_ERR)
					.internalCancel(reason)
					.await();
			}
		});
	}

	public AsyncResult<T> read() {
		return new AsyncResult<>(() -> {
			try (var stream = this._stream.lock()) {
				return stream.get()
					.expect(STREAM_ACCESS_ERR)
					.internalGetOrWaitForData()
					.await();
			}
		});
	}

	/**
	 * Alias to {@link Reader#releaseLock()}.
	 */
	@Override
	public void close() {
		try {
			this.releaseLock();
		} catch (RuntimeException ex) {
			// ignore cancellation errors
			if (!Objects.equals(ex.getMessage(), STREAM_ACCESS_ERR)) {
				Dirty.raise(ex);
			}
		}
	}

}
