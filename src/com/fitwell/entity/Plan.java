package com.fitwell.entity;

/**
 * Each plan is assigned a unique identifier and includes the start date,
 * duration, and current status (active, paused, completed, or cancelled) Plans
 * can be individual (personal) or collective (group-level)
 * 
 * A fitness plan consists of training classes, created and managed by the
 * fitness consultants
 * 
 * Personal plan - focuses on individual goals of a trainee and includes dietary
 * restrictions (as text), specified by a dietitian Group plans - involve
 * multiple trainees working together tracks key details of the group, including
 * the age range (e.g., 60-85), preferred class types (e.g., yoga, TRX, Pilates,
 * etc.), and general guidelines (as text).
 */
public class Plan {
	private int planId;
	private String type;
	private String status;

	public Plan(int planId, String type, String status) {
		this.planId = planId;
		this.type = type;
		this.status = status;
	}

	public int getPlanId() {
		return planId;
	}

	public String getType() {
		return type;
	}

	public String getStatus() {
		return status;
	}

}
