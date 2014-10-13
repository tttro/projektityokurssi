package fi.lbd.mobile.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import fi.lbd.mobile.ClientController;
import fi.lbd.mobile.ClientController.ViewType;

// Epic tutorial: http://www.vogella.com/tutorials/AndroidListView/article.html
public class ObjectListFragment extends ListFragment {
	private ClientController client;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//this.setRetainInstance(true);
		
		LatLng[] values = new LatLng[] { 
				new LatLng(61.442183227888464, 23.86426298304281), 
				new LatLng(63.442183227888464, 23.86426298304281), 
				new LatLng(60.442183227888464, 20.86426298304281), 
				new LatLng(58.442183227888464, 25.86426298304281)};
		
		ArrayAdapter<LatLng> adapter = new ArrayAdapter<LatLng>(getActivity(),
				android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof ClientController) {
			this.client = (ClientController)activity;
		} else {
			throw new ClassCastException("Activity doesn't implement ClientController interface");
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO: Passing selected map objects as arguments
		Bundle args = new Bundle();
		args.putDouble("latitude", ((LatLng)this.getListAdapter().getItem(position)).latitude);
		args.putDouble("longitude", ((LatLng)this.getListAdapter().getItem(position)).longitude);
		this.client.changeView(ViewType.Map, args);
	}
} 
