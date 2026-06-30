package com.fitwell.entity;

import java.time.LocalDateTime;

/**
 * browse classes assigned to their (personal or
 * group) plans and register for classes, based on availability, up to 24 hours
 * in advance
 */
public class Trainee {
	private final int traineeId;
	private final String firstName;
	private final String lastName;
	private final LocalDateTime birthDate;
	private final String phoneNumber;
	private final String email;
	private final int updateMethod;
	private final boolean isActive;

	public Trainee(int traineeId, String firstName, String lastName, LocalDateTime birthDate, String phoneNumber,
			String email) {
		this(traineeId, firstName, lastName, birthDate, phoneNumber, email, 1, true);
	}

	public Trainee(int traineeId, String firstName, String lastName, LocalDateTime birthDate, String phoneNumber,
			String email,
			int updateMethod, boolean isActive) {
		this.traineeId = traineeId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.updateMethod = updateMethod;
		this.isActive = isActive;
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

	public LocalDateTime getBirthDate() {
		return birthDate;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public int getUpdateMethod() {
		return updateMethod;
	}

	public boolean isActive() {
		return isActive;
	}

}
