package com.github.navnesen.streams.common;

import com.github.navnesen.async.AsyncResult;
import com.github.navnesen.util.Option;

public interface CancelFunction {
	AsyncResult<Void> run(Option<String> reason);
}
