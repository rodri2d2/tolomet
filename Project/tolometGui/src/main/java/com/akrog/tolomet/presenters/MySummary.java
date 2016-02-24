package com.akrog.tolomet.presenters;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import com.akrog.tolomet.Manager;
import com.akrog.tolomet.ModelActivity;
import com.akrog.tolomet.R;
import com.akrog.tolomet.view.Axis;

public class MySummary implements Presenter, Axis.ChangeListener {
	private ModelActivity activity;
	private Manager model;
	private TextView summary;
	private Long stamp = null;
	
	@Override
	public void initialize(ModelActivity activity, Bundle bundle) {
		this.activity = activity;
		model = activity.getModel();
		summary = (TextView)activity.findViewById(R.id.textView1);
	}
	
	@Override
	public void save(Bundle bundle) {		
	}
	
	@Override
	public void updateView() {
		if( model.getCurrentStation().isEmpty() )
    		summary.setText(activity.getString(R.string.NoData));
    	else
    		summary.setText(model.getSummary(stamp, activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE));
	}

	@Override
	public void onNewLimit(Number value) {
		stamp = value.longValue();
		if( model != null )
			updateView();
	}
}
