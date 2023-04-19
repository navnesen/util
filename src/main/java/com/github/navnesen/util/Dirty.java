package com.github.navnesen.util;

public class Dirty {
	@SuppressWarnings("unchecked")
	public static <E extends Throwable> void raise(Throwable e) throws E {
		throw (E) e;
	}
}
