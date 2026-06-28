package com.fitwell.entity;

/**
 * browse classes assigned to their (personal or
 * group) plans and register for classes, based on availability, up to 24 hours
 * in advance
 */
public class Trainee {
	public final int traineeId;
	public final String firstName;
	public final String lastName;
	public final int age;
	public final String phoneNumber;
	public final String email;

	public Trainee(int traineeId, String firstName, String lastName, int age, String phoneNumber, String email) {
		super();
		this.traineeId = traineeId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.phoneNumber = phoneNumber;
		this.email = email;
	}

	public int getTraineeId() {
		return traineeId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public int getAge() {
		return age;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getEmail() {
		return email;
	}

}
