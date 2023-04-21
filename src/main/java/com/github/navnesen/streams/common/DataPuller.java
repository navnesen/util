package com.github.navnesen.streams.common;

import com.github.navnesen.async.AsyncResult;

public interface DataPuller<T> {
	AsyncResult<T> pull();
}
