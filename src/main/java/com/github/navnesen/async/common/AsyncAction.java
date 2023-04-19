package com.github.navnesen.async.common;

public interface AsyncAction<T> {
	T run() throws Throwable;
}
