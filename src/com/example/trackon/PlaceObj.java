package com.example.trackon;
/*
	 * Place object with name latitude and location added as part of output of Aynchronous
	 * fetch of Places of Interest
	 */

	 class PlaceObj{
		public String name;
		public Double lat;
		public Double lng;
		public PlaceObj(String _name, Double _lat,Double _lng){
			name = _name;
			lat = _lat;
			lng = _lng;
		}
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			if(o instanceof PlaceObj)
			 return (((PlaceObj) o).lat == this.lat && ((PlaceObj) o).lng == this.lng); 
			return false;
		}
	}
	
	
