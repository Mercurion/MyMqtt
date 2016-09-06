package com.myclose.mymqtt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

//import android.app.Activity;

public class ListaAdapter extends BaseAdapter {

	Context context;
	public List<String> values;
	LayoutInflater mInflater;
	DeleteInterface mDelegate;

	
	public ListaAdapter(Context context, List<String> notifications){
		this.context = context;
		values = notifications;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return values.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void upDateListItems(List<String> newValues){
		values  = newValues;
	}
	@Override
	public View getView(int position, View contentView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder mHolder;
		if(contentView == null){
			contentView = mInflater.inflate(R.layout.list_row, parent, false);
			mHolder = new ViewHolder();
			mHolder.alert_message = (TextView)contentView.findViewById(R.id.alert_message);
			mHolder.clearButton = (Button)contentView.findViewById(R.id.alert_button);
			contentView.setTag(mHolder);
		}else{
			mHolder = (ViewHolder)contentView.getTag();
		}
		final String item = getItem(position);
		mHolder.alert_message.setText(item);
		mHolder.clearButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDelegate.deleteItemList(item);
			}
		});
		
		return contentView;
	}
	public void setDelegate(DeleteInterface d){
		mDelegate = d;
	}
	
	static class ViewHolder{
		
		public TextView alert_message;
		public Button clearButton;
	}

}
