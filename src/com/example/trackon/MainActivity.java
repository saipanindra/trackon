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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity {

	LocationManager locationManager = null;
	LocationListener locationListener  = null;
	Location existingLocation = null;
	//Button placesButton = null;
	ToggleButton placesToggleButton = null;
	PlacesButtonClickListener placesClickListener = null;
	PlacesButtonOffClickListener placesOffClickListener = null;
    ArrayList<PlaceObj> placesList = new ArrayList<PlaceObj>();
    ArrayList<PlaceObj> placesVisited = new ArrayList<PlaceObj>();
	
    class GetPlaces extends AsyncTask<AsynchInput,Void,AsynchOutput>{

    	@Override
    	protected AsynchOutput doInBackground(AsynchInput... placesURL) {
    		// TODO Auto-generated method stub
    		StringBuilder placesBuilder = new StringBuilder();
    		URI uri = null;
    		URL myURL = null;


    		for(AsynchInput inputObj : placesURL){
    			
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
    		
    			placesBuilder = PlacesFromURI.getPlacesFromURI(uri);
    			return new AsynchOutput(inputObj.map, placesBuilder.toString());
    	
    	 }
    		return null;
    	}
    	/*
    	 * (non-Javadoc)
    	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
    	 * Process(Adding markers on map) after getting output from the Asynchronous task
    	 */

    	@Override
    	protected void onPostExecute(AsynchOutput result) {
    		// TODO Auto-generated method stub
    		if(result != null){
    			super.onPostExecute(result);
    			GoogleMap myMap = result.map;
    			String resultString = result.placesOfInterest;
    			if(resultString != ""){
    				ArrayList<PlaceObj> places = PlacesFromJSON.getPlacesFromJSON(resultString);
    				if(places.size()!=0){
    					for(PlaceObj pObj : places){
    					    placesList.add(pObj);
    					}
    					for(PlaceObj pObj : placesList)
    					{
    						Location placeLoc = new Location(LocationManager.GPS_PROVIDER);
    						placeLoc.setLatitude(pObj.lat);
    						placeLoc.setLongitude(pObj.lng);
    						showLocationOnMap(myMap,placeLoc,pObj.name);
    					}
    				}
    			}
    		}
    	}
    }
    
    
    class PlaceVisitChecker extends AsyncTask<AsynchInput,Void,AsynchOutput>{

		@Override
		protected AsynchOutput doInBackground(AsynchInput... arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void onPostExecute(AsynchOutput result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
    	
    }

    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final GoogleMap myMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		//placesButton = (Button) findViewById(R.id.placesButton);
		placesToggleButton = (ToggleButton) findViewById(R.id.placesToggle);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener(){

			@Override
			public void onLocationChanged(Location loc	) {
				long currentLocationTimeInMillis = loc.getTime();	
				if((existingLocation != null && (currentLocationTimeInMillis - existingLocation.getTime()) > 2000 * 60) || existingLocation == null){

					
					myMap.clear();
					existingLocation = loc;
					showLocationOnMap(myMap,loc,"Your Location");
				    if(placesToggleButton.isChecked())
						showInterestingPlacesNearby(myMap,loc);	
					zoomToCurrentLocation(myMap,loc);
					placesClickListener = new PlacesButtonClickListener(myMap,loc);
					placesOffClickListener = new PlacesButtonOffClickListener(myMap,loc);
					//placesButton.setOnClickListener(placesClickListener);
					if(placesToggleButton.isChecked())
						placesToggleButton.setOnClickListener(placesOffClickListener);
					else
						placesToggleButton.setOnClickListener(placesClickListener);	
					//showInterestingPlacesNearby(myMap,loc);

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
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 * When the application resumes from a pause
	 */

	protected void onResume(){
		super.onResume();
		if(locationManager!=null)
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 * When application pauses.
	 */
	protected void onPause(){
		super.onPause();
		if(locationManager!=null)
			locationManager.removeUpdates(locationListener);
	}

	/*
	 * To create the search URL and invoke the asynchronous task for getting places of interest.
	 */
	public void showInterestingPlacesNearby(GoogleMap myMap,Location loc){

		String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
				"json?location="+loc.getLatitude()+","+loc.getLongitude()+
				"&radius=1000&sensor=true" +
				"&types=food|bar|store|museum|art_gallery"+
				"&key=AIzaSyBGdrFlD3TJ_r2_4UjZxHAYB8iZHEr77VI";

		new GetPlaces().execute(new AsynchInput(myMap,placesSearchStr));
		

	}
	
	//public void show
	public void clearInterestingPlacesNearby(GoogleMap myMap,Location loc){

		myMap.clear();
		showLocationOnMap(myMap,loc,"Your Location");

	}

	/*
	 * Zoom into location of interest.
	 */
	public void zoomToCurrentLocation(GoogleMap myMap,Location loc){
		LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
		myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 30));
	}
	/*
	 * Mark the location of interest on map
	 */
	public void showLocationOnMap(GoogleMap myMap,Location loc,String label){

		//myMap.clear();
		LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
		if(label.equals("Your Location")){

			myMap.addMarker(new MarkerOptions()
			.position(latlng)
			.title(label)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

		}
		else
			myMap.addMarker(new MarkerOptions().position(latlng).title(label));
		//myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));


	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class PlacesButtonClickListener implements OnClickListener{

		GoogleMap myMap;
		Location loc;
		PlacesButtonClickListener(GoogleMap _myMap, Location _loc){
			myMap = _myMap;
			loc = _loc;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showInterestingPlacesNearby(myMap,loc);
			if(placesToggleButton.isChecked())
			 placesToggleButton.setOnClickListener(placesOffClickListener);
			else
				placesToggleButton.setOnClickListener(placesClickListener);	
		}

	}

	private class PlacesButtonOffClickListener implements OnClickListener{

		GoogleMap myMap;
		Location loc;
		PlacesButtonOffClickListener(GoogleMap _myMap, Location _loc){
			myMap = _myMap;
			loc = _loc;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			clearInterestingPlacesNearby(myMap,loc);
			if(placesToggleButton.isChecked())
				 placesToggleButton.setOnClickListener(placesOffClickListener);
				else
					placesToggleButton.setOnClickListener(placesClickListener);	
		}

	}



}

