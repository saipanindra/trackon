package com.example.trackon;

import com.google.android.gms.maps.GoogleMap;

 class AsynchInput{
	GoogleMap map;
	String placesURL;
	public AsynchInput(GoogleMap _map, String _placesURL){
		map = _map;
		placesURL = _placesURL;
	}
}
