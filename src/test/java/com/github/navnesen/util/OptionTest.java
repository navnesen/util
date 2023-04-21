package com.github.navnesen.util;

import com.github.navnesen.async.AsyncResult;

class OptionTest {
	public Option<String> currentlySignedInUser() {
		return Option.some("john.doe");
	}

	public Result<User, Throwable> fetchUserByUsername(String username) {
		return Result.err(new UserNotFoundException());
	}

	public AsyncResult<String> fetchUserFullName(User user) {
		return AsyncResult.ok("John Doe");
	}

	public void example() {
		this.currentlySignedInUser()
			.okOr((Throwable) new RuntimeException("Visitor is not signed in"))
			.andThen(this::fetchUserByUsername)
			.map(this::fetchUserFullName)
			.unwrap()
	}
}

class User {

}

class UserNotFoundException extends Exception {
	public UserNotFoundException() {
		super("User not found!");
	}
}