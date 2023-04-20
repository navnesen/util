package com.github.navnesen.util;

import com.github.navnesen.util.common.InspectAction;
import com.github.navnesen.util.common.TypeAction;
import com.github.navnesen.util.common.TypeActionMap;

public class Option<T> {
	public static <T> Option<T> some(T value) {
		return new Option<>(true, value);
	}

	public static <T> Option<T> none() {
		return new Option<>(false, null);
	}

	protected boolean hasValue;
	protected T value;

	protected Option(boolean hasValue, T value) {
		this.hasValue = hasValue;
		this.value = value;
	}

	public boolean isSome() {
		return this.hasValue;
	}

	public boolean isSomeAnd(TypeActionMap<T, Boolean> action) {
		if (!this.hasValue) return false;
		return action.run(this.value);
	}

	public boolean isNone() {
		return !this.hasValue;
	}

	public T expect(String message) {
		if (!this.hasValue) throw new RuntimeException(message);
		return this.value;
	}

	public T unwrap() {
		return this.expect("could not unwrap value");
	}

	public T unwrapOr(T defaultValue) {
		if (!this.hasValue) return defaultValue;
		return this.value;
	}

	public T unwrapOrElse(TypeAction<T> action) {
		if (!this.hasValue) return action.run();
		return this.value;
	}

	public T unwrapUnchecked() {
		return this.value;
	}

	public <U> Option<U> map(TypeActionMap<T, U> action) {
		if (!this.hasValue) return Option.none();
		return Option.some(action.run(this.value));
	}

	public Option<T> inspect(InspectAction<T> action) {
		if (this.hasValue) action.run(this.value);
		return this;
	}

	public <U> Option<U> mapOr(TypeActionMap<T, U> action, U defaultValue) {
		if (!this.hasValue) return Option.some(defaultValue);
		return Option.some(action.run(this.value));
	}

	public <U> Option<U> mapOrElse(TypeActionMap<T, U> action, TypeAction<U> defaultValueFn) {
		if (!this.hasValue) return Option.some(defaultValueFn.run());
		return Option.some(action.run(this.value));
	}

	public <E> Result<T, E> okOr(E err) {
		if (!this.hasValue) return Result.err(err);
		return Result.ok(this.value);
	}

	public <E> Result<T, E> okOrElse(TypeAction<E> action) {
		if (!this.hasValue) return Result.err(action.run());
		return Result.ok(this.value);
	}

	public <U> Option<U> and(Option<U> opt) {
		if (!this.hasValue) return Option.none();
		return opt;
	}

	public <U> Option<U> andThen(TypeActionMap<T, Option<U>> action) {
		if (!this.hasValue) return Option.none();
		return action.run(this.value);
	}

	public Option<T> filter(TypeActionMap<T, Boolean> predicate) {
		if (this.isSome() && predicate.run(this.value)) {
			return Option.some(this.value);
		}
		return Option.none();
	}

	public Option<T> or(Option<T> opt) {
		if (this.hasValue) return Option.some(this.value);
		return opt;
	}

	public Option<T> orElse(TypeAction<Option<T>> action) {
		if (this.hasValue) return Option.some(this.value);
		return action.run();
	}

	public Option<T> xor(Option<T> opt) {
		if (this.isSome() && opt.isNone()) return Option.some(this.value);
		if (this.isNone() && opt.isSome()) return Option.some(opt.value);
		return Option.none();
	}

	public Option<T> insert(T value) {
		this.hasValue = true;
		this.value = value;
		return this;
	}

	public T getOrInsert(T value) {
		if (this.hasValue) return this.value;
		this.insert(value);
		return this.value;
	}

	public T getOrInsertWith(TypeAction<T> action) {
		if (this.hasValue) return this.value;
		this.insert(action.run());
		return this.value;
	}

	public Option<T> take() {
		var newOption = new Option<>(this.hasValue, this.value);
		this.hasValue = false;
		this.value = null;
		return newOption;
	}

	public <U> boolean contains(U value) {
		return this.hasValue && this.value == value;
	}
}
