package com.example.trackon;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
public class VisitedPlaceDialog extends DialogFragment{

	Context context = getActivity();

	public static VisitedPlaceDialog newinstance(String name, ArrayList<String> dates)
	{
		VisitedPlaceDialog  vDialog = new VisitedPlaceDialog();
		Bundle args = new Bundle();
		args.putString("name", name);
		args.putStringArrayList("times", dates);
		
		vDialog.setArguments(args);
		return vDialog;
		
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);	
	View dialogView = inflater.inflate(R.layout.place_visited_layout, null);
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle(R.string.paces_visited_dialog_title);
	builder.setView(dialogView);
	builder.setNegativeButton(R.string.places_visited_close_button, new OnClickListener(){
    
		@Override
		public void onClick(DialogInterface dialog, int arg1) {
			// TODO Auto-generated method stub
			dialog.dismiss();
		}
	});
	return builder.create();
		
	}

	
}
