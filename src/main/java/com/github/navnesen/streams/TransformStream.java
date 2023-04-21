package com.github.navnesen.streams;

public class TransformStream<I, O> {

	public WritableStream<I> writable;
	public ReadableStream<O> readable;

}
