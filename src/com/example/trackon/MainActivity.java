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
	Button placesButton = null;
	ToggleButton placesToggleButton = null;
	PlacesButtonClickListener placesClickListener = null;
	PlacesButtonOffClickListener placesOffClickListener = null;


	/*
	 * Inner Class , Input for Asynchronous fetch of Places of interest
	 */
	private class AsynchInput{
		GoogleMap map;
		String placesURL;
		public AsynchInput(GoogleMap _map, String _placesURL){
			map = _map;
			placesURL = _placesURL;
		}
	}

	/*
	 * Inner Class,Output from Asynchronous fetch of Places of Interest
	 */
	private class AsynchOutput{
		GoogleMap map;
		String placesOfInterest;
		public AsynchOutput(GoogleMap _map, String _placesOfInterest){
			map = _map;
			placesOfInterest = _placesOfInterest;
		}
	}
	/*
	 * Place object with name latitude and location added as part of output of Aynchronous
	 * fetch of Places of Interest
	 */

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
	/*
	 * Task to Fetch the Places of Interest Aynchronously
	 */
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

		/*
		 * 
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 * To get the Places of Interest
		 */

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
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 * When the application first loads
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final GoogleMap myMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		placesButton = (Button) findViewById(R.id.placesButton);
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
					placesButton.setOnClickListener(placesClickListener);
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

