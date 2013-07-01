package com.example.trackon;



import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

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
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity {

	LocationManager locationManager = null;
	LocationListener locationListener  = null;
	Location existingLocation = null;
	//Button placesButton = null;
	ToggleButton placesToggleButton = null;
	ToggleButton visitedPlacesToggleButton = null;
	PlacesButtonClickListener placesClickListener = null;
	PlacesButtonOffClickListener placesOffClickListener = null;
	VisitedPlacesButtonClickListener visitedPlacesClickListener = null;
	VisitedPlacesButtonOffClickListener visitedPlacesOffClickListener = null;
	
	ArrayList<PlaceObj> placesList = new ArrayList<PlaceObj>();
	ArrayList<PlaceObj> placesVisited = new ArrayList<PlaceObj>();
	ArrayList<Marker> placesListMarkers = new ArrayList<Marker>();
	ArrayList<Marker> placesVisitedMarkers = new ArrayList<Marker>();
	
	final Context context = this;

	class GetPlaces extends AsyncTask<AsynchInput,Void,AsynchOutput>{

		@Override
		protected AsynchOutput doInBackground(AsynchInput... placesURL) {
			// TODO Auto-generated method stub
			StringBuilder placesBuilder = new StringBuilder();
			URI uri = null;
			URL myURL = null;
			GoogleMap myMap = null;
			for(AsynchInput inputObj : placesURL){
				myMap = inputObj.map;
				String nullFragment = null;
				try{
					myURL = new URL(inputObj.placesURL);
					uri = new URI(myURL.getProtocol(),myURL.getHost(),myURL.getPath(),myURL.getQuery(),nullFragment);
				}
				catch(MalformedURLException me){
					System.out.println("URL " + myURL + " is a malformed URL");
				}
				catch(URISyntaxException urie){
					System.out.println("URI " + uri + " is a malformed URL");
				}

				placesBuilder = PlacesFromURI.getPlacesFromURI(uri);




			}
			return new AsynchOutput(myMap, placesBuilder.toString());
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
				if(!resultString.equals("")){

					ArrayList<PlaceObj> places = PlacesFromJSON.getPlacesFromJSON(resultString);
					if(places.size()!=0){

						for(PlaceObj pObj : places){
							placesList.add(pObj);
						}
						showPlaceListFromCache(myMap);
					}
				}
			}
		}
	}


	class PlaceVisitChecker extends AsyncTask<AsynchInput,Void,AsynchOutput>{

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

		@Override
		protected void onPostExecute(AsynchOutput result) {
			// TODO Auto-generated method stub
			if(result!= null){
				super.onPostExecute(result);

				if(!result.placesOfInterest.equals(""))
				{
					ArrayList<PlaceObj> placesV = PlacesFromJSON.getPlacesFromJSON(result.placesOfInterest);
					try
					{

						
							Thread.sleep(2000);
							Location currentLoc  = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							//PlaceObj tempPObj = new PlaceObj("",currentLoc.getLatitude(),currentLoc.getLongitude());

							PlaceObj lastPlaceVisited = placesV.get(0);
							String proximatePlaceOfInterestJSON = getProximatePlaceOfInterest(result.map,currentLoc);
							ArrayList<PlaceObj> currentPOI = PlacesFromJSON.getPlacesFromJSON(proximatePlaceOfInterestJSON);
							PlaceObj lastProximatePlaceOfInterest = currentPOI.get(0);
							if(lastProximatePlaceOfInterest.id.equals(lastPlaceVisited.id) && !placesVisited.contains(lastProximatePlaceOfInterest))
							{
								
								Location placeLoc = new Location(LocationManager.GPS_PROVIDER);
								placeLoc.setLatitude(lastPlaceVisited.lat);
								placeLoc.setLongitude(lastPlaceVisited.lng);
								
								placesVisited.add(lastPlaceVisited);
								//showVisitedPlaceOnMap(result.map,placeLoc,lastProximatePlaceOfInterest.name);
							}
								
						

					}
					catch(InterruptedException i){
						i.printStackTrace();
					}
				}
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

	}
	public void showPlaceListFromCache(GoogleMap myMap)
	{
		for(PlaceObj pObj : placesList)
		{

			Location placeLoc = new Location(LocationManager.GPS_PROVIDER);
			placeLoc.setLatitude(pObj.lat);
			placeLoc.setLongitude(pObj.lng);
			showPlaceOfInterestOnMap(myMap,placeLoc,pObj.name);
		}
	}
	
	public void clearMarkers(ArrayList<Marker> markerList)
	{
		for(Marker m : markerList)
		{
			m.remove();
		}
		markerList.clear();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final GoogleMap myMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		//placesButton = (Button) findViewById(R.id.placesButton);
		placesToggleButton = (ToggleButton) findViewById(R.id.placesToggle);
		visitedPlacesToggleButton = (ToggleButton) findViewById(R.id.visitedPlacesToggle);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener(){

			@Override
			public void onLocationChanged(Location loc	) {
				long currentLocationTimeInMillis = loc.getTime();	
				if( (existingLocation != null && (currentLocationTimeInMillis - existingLocation.getTime()) > 2000 * 60) || existingLocation == null){

					myMap.clear();

					existingLocation = loc;
					showCurrentLocationOnMap(myMap,loc,"Your Location");
					checkForPlaceProximity(myMap, loc);
					if(placesToggleButton.isChecked())
						showInterestingPlacesNearby(myMap,loc);	
					zoomToCurrentLocation(myMap,loc);
					placesClickListener = new PlacesButtonClickListener(myMap,loc);
					placesOffClickListener = new PlacesButtonOffClickListener(myMap,loc);
					visitedPlacesClickListener = new VisitedPlacesButtonClickListener(myMap, loc);
					visitedPlacesOffClickListener = new VisitedPlacesButtonOffClickListener(myMap, loc);
					
					if(placesToggleButton.isChecked())
						placesToggleButton.setOnClickListener(placesOffClickListener);
					else
						placesToggleButton.setOnClickListener(placesClickListener);	
					if(visitedPlacesToggleButton.isChecked()){
						visitedPlacesToggleButton.setOnClickListener(visitedPlacesOffClickListener);
					
					}
					else
						visitedPlacesToggleButton.setOnClickListener(visitedPlacesOffClickListener);
					

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

	public void checkForPlaceProximity(GoogleMap myMap, Location loc)
	{
		String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
				"json?location="+loc.getLatitude()+","+loc.getLongitude()+
				"&radius=10&sensor=true" +
				"&types=food|bar|store|museum|art_gallery"+
				"&key=AIzaSyCCFZuvv8sdrcwIjCHQVoiNWCahSieEdbg";
		new PlaceVisitChecker().execute(new AsynchInput(myMap,placesSearchStr));
	}

	public String getProximatePlaceOfInterest(GoogleMap myMap, Location loc)
	{
		URL myURL = null;
		URI uri = null;
		StringBuilder placesBuilder = null;
		String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
				"json?location="+loc.getLatitude()+","+loc.getLongitude()+
				"&radius=10&sensor=true" +
				"&types=food|bar|store|museum|art_gallery"+
				"&key=AIzaSyCCFZuvv8sdrcwIjCHQVoiNWCahSieEdbg";

		try{
			myURL = new URL(placesSearchStr);
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
		return placesBuilder.toString();

	}

	/*
	 * To create the search URL and invoke the asynchronous task for getting places of interest.
	 */
	public void showInterestingPlacesNearby(GoogleMap myMap,Location loc){

		placesListMarkers.clear();
		String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
				"json?location="+loc.getLatitude()+","+loc.getLongitude()+
				"&radius=1000&sensor=true" +
				"&types=food|bar|store|museum|art_gallery"+
				"&key=AIzaSyCCFZuvv8sdrcwIjCHQVoiNWCahSieEdbg";
		new GetPlaces().execute(new AsynchInput(myMap,placesSearchStr));
	}

	//public void show
	public void clearInterestingPlacesNearby(GoogleMap myMap,Location loc){
		clearMarkers(placesListMarkers);
		showCurrentLocationOnMap(myMap,loc,"Your Location");
	}

	public void clearPlacesVisited(GoogleMap myMap, Location loc){
	  clearMarkers(placesVisitedMarkers);
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
	public void showCurrentLocationOnMap(GoogleMap myMap,Location loc,String label){

		//myMap.clear();
		LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
		//if(label.equals("Your Location")){

			myMap.addMarker(new MarkerOptions()
			.position(latlng)
			.title(label)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

		}
		
	 public void showVisitedPlaceOnMap(GoogleMap myMap,Location loc, String label){
		//else if(label.equals("POI")){
		 LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
		 placesVisitedMarkers.add(myMap.addMarker(new MarkerOptions()
			.position(latlng)
			.title(label)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))));

		}
	 public void showPlaceOfInterestOnMap(GoogleMap myMap, Location loc, String label)	 
	 {
		 LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
		 
		 placesListMarkers.add(myMap.addMarker(new MarkerOptions().position(latlng).title(label)));
		 
	 }
		//myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));


	

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	private class VisitedPlacesButtonClickListener implements OnClickListener{

		GoogleMap myMap;
		Location loc;
		VisitedPlacesButtonClickListener(GoogleMap _myMap, Location _loc){
			myMap = _myMap;
			loc = _loc;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			if(visitedPlacesToggleButton.isChecked())
			{
				visitedPlacesToggleButton.setOnClickListener(visitedPlacesOffClickListener);
			}
			else
			{
				visitedPlacesToggleButton.setOnClickListener(visitedPlacesClickListener);
			}
			for(PlaceObj placeVisited : placesVisited)
			{
				placesVisitedMarkers.clear();
				Location latlng = new Location(LocationManager.GPS_PROVIDER);
				latlng.setLatitude(placeVisited.lat);
				latlng.setLongitude(placeVisited.lng);
				showVisitedPlaceOnMap(myMap,latlng,placeVisited.name);
			}
			
		}
		
	}
	private class VisitedPlacesButtonOffClickListener implements OnClickListener{

		GoogleMap myMap;
		Location loc;
		VisitedPlacesButtonOffClickListener(GoogleMap _myMap, Location _loc){
			myMap = _myMap;
			loc = _loc;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			if(visitedPlacesToggleButton.isChecked())
			{
				visitedPlacesToggleButton.setOnClickListener(visitedPlacesOffClickListener);
			}
			else
			{
				visitedPlacesToggleButton.setOnClickListener(visitedPlacesClickListener);
			}
			clearPlacesVisited(myMap, loc);
			
		}
		
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
			Location currentLoc  = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			placesListMarkers.clear();
			if(!(currentLoc.getLatitude() == loc.getLatitude() && currentLoc.getLongitude() == loc.getLongitude()) || placesList.size() == 0 )
				showInterestingPlacesNearby(myMap,loc);
			else
			{
				
				showPlaceListFromCache(myMap);
			}
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

