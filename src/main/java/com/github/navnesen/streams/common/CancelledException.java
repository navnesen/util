package com.github.navnesen.streams.common;

import com.github.navnesen.util.Option;

public class CancelledException extends Exception {
	public CancelledException(Option<String> reason) {
		super(
			"Stream is cancelled%s".formatted(
				reason.isNone()
					? ""
					: ": %s".formatted(reason.unwrapUnchecked()
				)
			)
		);
	}
}
