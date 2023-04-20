package com.github.navnesen.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A mutual exclusion primitive useful for protecting shared data.
 *
 * @param <T>
 */
public class Mutex<T> {
	public static <T> Mutex<T> of(T value) {
		return new Mutex<>(value);
	}

	protected T value;
	protected final AtomicReference<List<Locked<T>>> waiters = new AtomicReference<>(new ArrayList<>());
	protected final AtomicReference<Boolean> locked = new AtomicReference<>(false);

	public Mutex(T value) {
		this.value = value;
	}

	public Locked<T> lock() {
		@SuppressWarnings("resource") final Locked<T> lock = new Locked<>();

		synchronized (this) {
			this.locked.updateAndGet(locked -> {
				final AtomicBoolean newLockState = new AtomicBoolean(locked);
				this.waiters.updateAndGet(waiters -> {
					if (!locked) {
						newLockState.set(true);
						lock.mutex = this;
					} else {
						waiters.add(lock);
					}
					return waiters;
				});
				return newLockState.get();
			});

		}
		return lock.waitForLock();
	}
}
