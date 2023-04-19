package com.github.navnesen.async.common;

import com.github.navnesen.util.Result;

public interface AwaitableResult<T> {
	Result<T, Throwable> await();
}
