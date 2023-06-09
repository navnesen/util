package com.github.navnesen.async;


import com.github.navnesen.util.Dirty;
import com.github.navnesen.util.Result;

public class DeferredAsyncResult<T> extends AsyncResult<T> {
	public DeferredAsyncResult() {
		super();
	}

	public void okay(T value) {
		try {
			this.complete(Result.ok(value));
		} catch (Exception e) {
			Dirty.raise(e);
		}
	}

	public void error(Throwable exception) {
		try {
			this.complete(Result.err(exception));
		} catch (Exception e) {
			Dirty.raise(e);
		}
	}
}
