package com.example.trackon;



import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity {

	LocationManager locationManager = null;
	LocationListener locationListener  = null;
	Location existingLocation = null;

private class AsynchInput{
	GoogleMap map;
	String placesURL;
	public AsynchInput(GoogleMap _map, String _placesURL){
		map = _map;
		placesURL = _placesURL;
	}
}

private class AsynchOutput{
	GoogleMap map;
	String placesOfInterest;
	public AsynchOutput(GoogleMap _map, String _placesOfInterest){
		map = _map;
		placesOfInterest = _placesOfInterest;
	}
}


private class PlaceObj{
	public String name;
	public Double lat;
	public Double lng;
	public PlaceObj(String _name, Double _lat,Double _lng){
		name = _name;
		lat = _lat;
		lng = _lng;
	}
}

private class GetPlaces extends AsyncTask<AsynchInput,Void,AsynchOutput>{
	
protected ArrayList<PlaceObj> getPlacesFromJSON(String placesJSON){
		
		ArrayList<PlaceObj> pList = new ArrayList<PlaceObj>();
		try{
		JSONObject jsonObj = new JSONObject(placesJSON);
		JSONArray jsonResultArray = jsonObj.getJSONArray("results");
		for(int i = 0 ; i < jsonResultArray.length() ; i++){
			
			JSONObject resultObj = jsonResultArray.getJSONObject(i);
			JSONObject geometryObj = resultObj.getJSONObject("geometry");
			JSONObject locationObj = geometryObj.getJSONObject("location");
			Double locationLat = locationObj.getDouble("lat");
			Double locationLng = locationObj.getDouble("lng");
			String name = resultObj.getString("name");
			//String geometryStr = jsonObject.getString("geometry");
			//String name = jsonObject.getString("name");
			//Double locationLat = Double.parseDouble(geometryStr.split("{")[1].split("{")[1].split(":")[1].split(",")[0].trim());
			//Double locationLng = Double.parseDouble(geometryStr.split("{")[1].split("{")[1].split(":")[1].split(",")[1].split(":")[1].trim());
			PlaceObj pObj = new PlaceObj(name,locationLat,locationLng);
			 
			pList.add(pObj);
			
		}
		
		return pList;
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;

}



	@Override
	protected AsynchOutput doInBackground(AsynchInput... placesURL) {
		// TODO Auto-generated method stub
		StringBuilder placesBuilder = new StringBuilder();
		URI uri = null;
		URL myURL = null;
		
		
		for(AsynchInput inputObj : placesURL){
			HttpClient placesClient  = new DefaultHttpClient();
			try{
				 myURL = new URL(inputObj.placesURL);
				 String nullFragment = null;
				 uri = new URI(myURL.getProtocol(),myURL.getHost(),myURL.getPath(),myURL.getQuery(),nullFragment);
				}
				catch(MalformedURLException me){
					System.out.println("URL " + myURL + " is a malformed URL");
				}
				catch(URISyntaxException urie){
					System.out.println("URI " + uri + " is a malformed URL");
				}
			try{
				HttpGet placesGet = new HttpGet(uri);
				HttpResponse placesResponse = placesClient.execute(placesGet);
				if(placesResponse != null){
				StatusLine placeSearchStatus = placesResponse.getStatusLine();
				if (placeSearchStatus != null && placeSearchStatus.getStatusCode() == 200) {
					HttpEntity placesEntity = placesResponse.getEntity();
					if(placesEntity != null){
						InputStream placesContent = placesEntity.getContent();
						InputStreamReader placesInput = new InputStreamReader(placesContent);
						BufferedReader placesReader = new BufferedReader(placesInput);
						String lineIn;
						while ((lineIn = placesReader.readLine()) != null) {
					    placesBuilder.append(lineIn);
						}
					}
					return new AsynchOutput(inputObj.map, placesBuilder.toString());
					}
			 }
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	

	@Override
	protected void onPostExecute(AsynchOutput result) {
		// TODO Auto-generated method stub
		if(result != null){
		super.onPostExecute(result);
		GoogleMap myMap = result.map;
		String resultString = result.placesOfInterest;
		if(resultString != ""){
		 ArrayList<PlaceObj> places = getPlacesFromJSON(resultString);
		 if(places.size()!=0){
			 for(PlaceObj pObj : places){
				 Location loc = new Location(LocationManager.GPS_PROVIDER);
				 loc.setLatitude(pObj.lat);
				 loc.setLongitude(pObj.lng);
				 showLocationOnMap(myMap,loc,pObj.name);
			 }
		  }
		 }
		}
	}
}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final GoogleMap myMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener(){

		@Override
		public void onLocationChanged(Location loc) {
			long currentLocationTimeInMillis = loc.getTime();	
			if((existingLocation != null && (currentLocationTimeInMillis - existingLocation.getTime()) > 2000) || existingLocation == null){
				
				myMap.clear();
				existingLocation = loc;
				showLocationOnMap(myMap,loc,"Your Location");
				showInterestingPlacesNearby(myMap,loc);
			
			}
		}
		@Override
		public void onProviderDisabled(String provider) {
		}
		@Override
		public void onProviderEnabled(String provider) {
		}
		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
		}

	};

	}
	
	protected void onResume(){
		super.onResume();
		if(locationManager!=null)
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	protected void onPause(){
		super.onPause();
		if(locationManager!=null)
		locationManager.removeUpdates(locationListener);
	}
	
	public void showInterestingPlacesNearby(GoogleMap myMap,Location loc){
		
		String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
			    "json?location="+loc.getLatitude()+","+loc.getLongitude()+
			    "&radius=1000&sensor=true" +
			    "&types=food|bar|store|museum|art_gallery"+
			    "&key=AIzaSyBGdrFlD3TJ_r2_4UjZxHAYB8iZHEr77VI";
		
		new GetPlaces().execute(new AsynchInput(myMap,placesSearchStr));
			
		}
		
	public void zoomToCurrentLocation(GoogleMap myMap,Location loc){
		LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
		myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
	}
	
	public void showLocationOnMap(GoogleMap myMap,Location loc,String label){

		//myMap.clear();
		if(label.equals("Your Location"))
		 zoomToCurrentLocation(myMap,loc);
		LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
		myMap.addMarker(new MarkerOptions().position(latlng).title(label));
		//myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));

			
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

 