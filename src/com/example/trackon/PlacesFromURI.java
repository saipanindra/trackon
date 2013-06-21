package com.example.trackon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class PlacesFromURI {

	public static StringBuilder getPlacesFromURI(URI myURI){
		StringBuilder placesBuilder = new StringBuilder();
			HttpClient placesClient  = new DefaultHttpClient();
		try{
			HttpGet placesGet = new HttpGet(myURI);
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
					
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return placesBuilder;
	}
}
