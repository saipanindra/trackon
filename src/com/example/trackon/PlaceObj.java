package com.example.trackon;

import java.util.ArrayList;
import java.util.Date;
/*
	 * Place object with name latitude and location added as part of output of Aynchronous
	 * fetch of Places of Interest
	 */

	 class PlaceObj{
		public String name;
		public Double lat;
		public Double lng;
		public String id;
		public Double expense;
		public String notes;
		public ArrayList<String> visitedTimes;
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return Integer.parseInt(lat.toString()) ^ Integer.parseInt(lng.toString());
			//return super.hashCode();
		}
		public PlaceObj(String _name, Double _lat,Double _lng,String _id){//, Date _dt){
			name = _name;
			lat = _lat;
			lng = _lng;
			id = _id;
			expense = 0.0;
			notes = "";
			//visitedTimes.add(_dt.toString());
		}
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			if(o instanceof PlaceObj)
			 return (((PlaceObj) o).id.equals(this.id)); 
			return false;
		}
	}
	
	
