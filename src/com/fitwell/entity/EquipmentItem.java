package com.fitwell.entity;

public class EquipmentItem {
	
	//Fields: 
	protected int serialNumber; 
	protected int typeID; 
	protected int shelfNumber; 
	protected int x; 
	protected int y; 
	protected boolean needsReview; 
	protected boolean isFunctional;
	
	//Constructor
	public EquipmentItem(int serialNumber, int typeID, int shelfNumber, int x, int y, boolean needsReview,
			boolean isFunctional) {
		super();
		this.serialNumber = serialNumber;
		this.typeID = typeID;
		this.shelfNumber = shelfNumber;
		this.x = x;
		this.y = y;
		this.needsReview = needsReview;
		this.isFunctional = isFunctional;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public int getTypeID() {
		return typeID;
	}

	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}

	public int getShelfNumber() {
		return shelfNumber;
	}

	public void setShelfNumber(int shelfNumber) {
		this.shelfNumber = shelfNumber;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isNeedsReview() {
		return needsReview;
	}

	public void setNeedsReview(boolean needsReview) {
		this.needsReview = needsReview;
	}

	public boolean isFunctional() {
		return isFunctional;
	}

	public void setFunctional(boolean isFunctional) {
		this.isFunctional = isFunctional;
	}

	@Override
	public String toString() {
		return "EquipmentItem [serialNumber=" + serialNumber + ", typeID=" + typeID + ", shelfNumber=" + shelfNumber
				+ ", x=" + x + ", y=" + y + ", needsReview=" + needsReview + ", isFunctional=" + isFunctional + "]";
	} 
	
	
	
	
	
	
	

	
}
