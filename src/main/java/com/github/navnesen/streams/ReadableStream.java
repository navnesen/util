package com.github.navnesen.streams;

import com.github.navnesen.async.AsyncResult;
import com.github.navnesen.async.DeferredAsyncResult;
import com.github.navnesen.async.common.AsyncAction;
import com.github.navnesen.streams.common.CancelFunction;
import com.github.navnesen.streams.common.CancelledException;
import com.github.navnesen.streams.common.DataPuller;
import com.github.navnesen.sync.Mutex;
import com.github.navnesen.util.Option;
import com.github.navnesen.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ReadableStream<T> {

	// region cancellation

	protected Mutex<Boolean> _isCancelled = Mutex.of(false);
	protected Mutex<Option<String>> _cancelReason = Mutex.of(Option.none());
	protected Option<CancelFunction> _cancelFunction = Option.none();

	protected void assertNotCancelled() throws CancelledException {
		try (var isCancelled = this._isCancelled.lock()) {
			if (isCancelled.get()) {
				try (var cancelReason = this._cancelReason.lock()) {
					throw new CancelledException(cancelReason.get());
				}
			}
		}
	}

	protected AsyncResult<Void> internalCancel(Option<String> reason) {
		return new AsyncResult<>(() -> {
			try (var isCancelled = this._isCancelled.lock()) {
				if (isCancelled.get()) {
					try (var cancelReason = this._cancelReason.lock()) {
						throw new CancelledException(cancelReason.get());
					}
				}
				isCancelled.set(true);
				try (var cancelReason = this._cancelReason.lock()) {
					cancelReason.set(reason);
				}
				try (var currentReader = this._currentReader.lock()) {
					try (var readerStream = currentReader.get().expect("no current reader")._stream.lock()) {
						readerStream.set(Option.none());
					}
				}
				try (var getReaderWaiters = this._getReaderWaiters.lock()) {
					for (var async : getReaderWaiters.get()) {
						async.error(new CancelledException(reason));
					}
				}
				{
					var opt = this._cancelFunction;
					if (opt.isSome()) {
						opt.unwrapUnchecked().run(reason).unwrap();
					}
				}
			}
			return null;
		});
	}

	// endregion

	// region reader request queue

	protected Mutex<Option<Reader<T>>> _currentReader = Mutex.of(Option.none());
	protected Mutex<List<DeferredAsyncResult<Reader<T>>>> _getReaderWaiters = Mutex.of(new ArrayList<>());

	protected void releaseFrom(Reader<T> reader) {
		try (var _currentReader = this._currentReader.lock()) {
			// verify current reader
			// noinspection resource
			if (_currentReader.get().expect("no active reader") != reader) {
				throw new RuntimeException("current reader is not the same as the provided reader");
			}

			try (var getReaderWaiters = this._getReaderWaiters.lock()) {
				if (getReaderWaiters.get().size() > 0) {
					var newReader = new Reader<>(this);
					_currentReader.set(Option.some(newReader));
					getReaderWaiters.get().remove(0).okay(newReader);
				} else {
					_currentReader.set(Option.none());
				}
			}
		}
	}

	// endregion

	// region data buffer

	protected Mutex<List<T>> _buffer = Mutex.of(new ArrayList<>());
	protected Mutex<List<DeferredAsyncResult<T>>> _readWaiters = Mutex.of(new ArrayList<>());

	protected Mutex<Option<DataPuller<T>>> _pullFunction = Mutex.of(Option.none());

	protected void internalGiveValueToWaiterOrWriteToBuffer(T value) {

	}

	protected void internalFailFirstOrCurrentWaiterDueToPullError(Throwable ex) {

	}

	protected AsyncResult<T> internalGetOrWaitForData() {
		return new AsyncResult<>(() -> {
			try (var buffer = this._buffer.lock()) {
				if (buffer.get().size() > 0) {
					return buffer.get().remove(0);
				}
			}
			var waiter = new DeferredAsyncResult<T>();
			try (var readWaiters = this._readWaiters.lock()) {
				readWaiters.get().add(waiter);
			}
			try (var _puller = this._pullFunction.lock()) {
				var puller = _puller.get();
				if (puller.isSome()) {
					puller.unwrapUnchecked().pull()
						.inspect(this::internalGiveValueToWaiterOrWriteToBuffer)
						.inspectErr(this::internalFailFirstOrCurrentWaiterDueToPullError);
				}
				return waiter.unwrap();
			}
		});
	}

	// endregion

	// region user methods
	public AsyncResult<Void> cancel() {
		return this.cancel(Option.none());
	}

	public AsyncResult<Void> cancel(String reason) {
		return this.cancel(Option.some(reason));
	}

	public AsyncResult<Void> cancel(Option<String> reason) {
		return this.getReader().andThen(reader -> reader.cancel(reason));
	}

	public AsyncResult<Reader<T>> getReader() {
		return new AsyncResult<>(() -> {
			this.assertNotCancelled();
			try (var currentReader = this._currentReader.lock()) {
				if (currentReader.get().isNone()) {
					var reader = new Reader<T>(this);
					currentReader.set(Option.some(reader));
					return reader;
				} else {
					try (var readers = this._getReaderWaiters.lock()) {
						var waiter = new DeferredAsyncResult<Reader<T>>();
						readers.get().add(waiter);
						return waiter.unwrap();
					}
				}
			}
		});
	}

	public <U> ReadableStream<U> pipeThrough(TransformStream<T, U> transformer) {
		new AsyncResult<>((AsyncAction<Void>) () -> {
			return null;
		}).orElse(ex -> transformer.readable.cancel(ex.getMessage()));
		return transformer.readable;
	}

	public AsyncResult<Void> pipeTo(WritableStream<T> writable) {
		// TODO implement this.
		return new AsyncResult<>(() -> null);
	}

	public Tuple<ReadableStream<T>, ReadableStream<T>> tee() {
		// TODO implement this correctly by creating a new buffered readable.
		return Tuple.of(this, this);
	}
	// endregion
}
