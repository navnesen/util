package com.github.navnesen.util;

public class Tuple<A, B> {

	public static <A, B> Tuple<A, B> of(A a, B b) {
		return new Tuple<>(a, b);
	}

	public final A a;
	public final B b;

	protected Tuple(A a, B b) {
		this.a = a;
		this.b = b;
	}
}
