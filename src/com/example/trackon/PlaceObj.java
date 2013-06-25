package com.example.trackon;
/*
	 * Place object with name latitude and location added as part of output of Aynchronous
	 * fetch of Places of Interest
	 */

	 class PlaceObj{
		public String name;
		public Double lat;
		public Double lng;
		public String id;
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return Integer.parseInt(lat.toString()) ^ Integer.parseInt(lng.toString());
			//return super.hashCode();
		}
		public PlaceObj(String _name, Double _lat,Double _lng,String _id){
			name = _name;
			lat = _lat;
			lng = _lng;
			id = _id;
		}
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			if(o instanceof PlaceObj)
			 return (((PlaceObj) o).id == this.id); 
			return false;
		}
	}
	
	
