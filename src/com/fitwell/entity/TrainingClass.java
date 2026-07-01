package com.fitwell.entity;

import java.time.LocalDateTime;

public class TrainingClass {
    private int classId;
    private String name;
	private int classType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int maxParticipants;
    private int seatsLeft;

	public TrainingClass(int classId, String name, LocalDateTime startDateTime, LocalDateTime endDateTime,
			int classType, int maxParticipants) {

		this.classId = classId;
		this.name = name;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.maxParticipants = maxParticipants;
		this.classType = classType;
	}
	public TrainingClass(int classId, String name, LocalDateTime startDateTime, LocalDateTime endDateTime,
			int classType, int maxParticipants, int seatsLeft) {

		this.classId = classId;
		this.name = name;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.maxParticipants = maxParticipants;
		this.classType = classType;
		this.seatsLeft = seatsLeft;
	}
	public int getClassId() {
		return classId;
	}
	public void setClassId(int classId) {
		this.classId = classId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(LocalDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}
	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(LocalDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}
	public int getMaxParticipants() {
		return maxParticipants;
	}
	public void setMaxParticipants(int maxParticipants) {
		this.maxParticipants = maxParticipants;
	}
	public int getClassType() {
		return classType;
	}
	public void setClassType(int classType) {
		this.classType = classType;
	}
	public int getSeatsLeft() {
		return seatsLeft;
	}
	public void setSeatsLeft(int seatsLeft) {
		this.seatsLeft = seatsLeft;
	}
    
}
