package com.github.navnesen.sync;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A container for the value protected by a mutex.
 *
 * @param <T>
 */
public class Locked<T> implements AutoCloseable {
	protected Mutex<T> mutex;

	protected Locked<T> waitForLock() {
		synchronized (this) {
			while (this.mutex == null) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		return this;
	}

	/**
	 * Verify that read/write operations on the lock is still possible.
	 */
	protected void assertNotReleased() {
		if (this.mutex == null) {
			throw new RuntimeException("Mutex lock is released!");
		}
	}

	/**
	 * Get the current value in the mutex.
	 */
	public T get() {
		this.assertNotReleased();
		return this.mutex.value;
	}

	/**
	 * Put a new value into the mutex.
	 */
	public void set(T value) {
		this.assertNotReleased();
		this.mutex.value = value;
	}

	/**
	 * Releases the mutex to the next waiter.
	 */
	public void release() {
		if (this.mutex == null) {
			return;
		}
		Mutex<T> mutex = this.mutex;
		this.mutex = null;
		synchronized (mutex.waiters) {
			final AtomicReference<Locked<T>> nextLockRef = new AtomicReference<>(null);
			mutex.waiters.updateAndGet(waiters -> {
				if (waiters.size() > 0) {
					nextLockRef.set(waiters.remove(0));
				} else {
					mutex.locked.set(false);
				}
				return waiters;
			});
			final Locked<T> nextLock = nextLockRef.get();
			if (nextLock != null) {
				// noinspection SynchronizationOnLocalVariableOrMethodParameter
				synchronized (nextLock) {
					nextLock.mutex = mutex;
					nextLock.notifyAll();
				}
			}
		}
	}

	/**
	 * Alias to {@link Locked#release()}.
	 */
	@Override
	public void close() {
		this.release();
	}
}
