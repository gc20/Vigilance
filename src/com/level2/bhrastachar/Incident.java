package com.level2.bhrastachar;

public class Incident {

	private String Title;
	private String Message;
	//private int Category; // 1-> Education 2-> Traffic 3-> Public Utilities
	private String Location;
	private double Latitude;
	private double Longitude;
	private long ReportTime;
	private String Department;
	private int Bribe;
	private String Offender;
	private String Email;
	
	public Incident (String Title, String Message, String Location, Double Latitude, Double Longitude, long ReportTime, String Department, int Bribe, String Offender, String Email){
		this.Title = Title;
		this.Message = Message;
		//this.Category = Category;
		this.Location = Location;
		this.Latitude = Latitude;
		this.Longitude = Longitude;
		this.ReportTime = ReportTime;
		this.Department = Department;
		this.Bribe = Bribe;
		this.Offender = Offender;
		this.Email = Email;
	}
	
	public String getTitle() {
        return Title;
    }
	
	public String getMessage() {
        return Message;
    }
	
	/*public int getCategory() {
		return Category;
	}*/
	
    public String getLocation() {
    	return Location;
    }
	
    public Double getLatitude() {
    	return Latitude;
    }
	
    public Double getLongitude() {
    	return Longitude;
    }
	
    public long getReportTime() {
    	return ReportTime;
	}

    public String getDepartment() {
    	return Department;
    }

    public int getBribe() {
    	return Bribe;
    }

    public String getOffender() {
    	return Offender;
    }

    public String getEmail() {
    	return Email;
    }

}
