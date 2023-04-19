package com.github.navnesen.async.common;

import com.github.navnesen.util.Result;

public interface AsyncActionResult<T> {
	Result<T, Throwable> run() throws Throwable;
}
