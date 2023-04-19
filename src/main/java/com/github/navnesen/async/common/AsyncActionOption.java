package com.github.navnesen.async.common;

import com.github.navnesen.util.Option;

public interface AsyncActionOption<T> {
	Option<T> run() throws Throwable;
}
