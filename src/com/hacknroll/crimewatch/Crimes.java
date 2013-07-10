package com.hacknroll.crimewatch;


public class Crimes {

	public String ID;
	public String Description;
	public String Calc_Date;
	public String Calc_Time;
	public String Address;
	public String City;
	public String Crime_Name;
	public String Crime_Type;
	public double Latitude;
	public double Longitude;
	
	public Crimes (String ID, String Description, String Calc_Date, String Calc_Time,
			String Address, String City, String Crime_Name, String Crime_Type,
			double Latitude, double Longitude){ 
		
		this.ID = ID;
		this.Description = Description;
		this.Calc_Date= Calc_Date;
		this.Calc_Time = Calc_Time;
		this.Address = Address;
		this.City = City;
		this.Crime_Name = Crime_Name;
		this.Crime_Type = Crime_Type;
		this.Latitude = Latitude;
		this.Longitude = Longitude;
	}
	
}