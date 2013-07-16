package com.example.trackon;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class PlacesFromJSON {

 public static ArrayList<PlaceObj> getPlacesFromJSON(String placesJSON)
 {
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
				String id = resultObj.getString("id");
				Date d = new Date(System.currentTimeMillis());
			
				PlaceObj pObj = new PlaceObj(name,locationLat,locationLng,id,d);//,d);
             	
				pList.add(pObj);

			}



		}
		catch(Exception e){
			e.printStackTrace();
		}

		return pList;
 }
	
}
