package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class LayerListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Layer>>{

	private MapChangeListener mapChangeListener;
	
	private ArrayList<Layer> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private final FragmentActivity activity;
	private final Context context;
	private final ArbiterProject arbiterProject;
	
	private static final Map<String, String> COLOR_MAP;
    static {
        Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("teal","#008080");
		aMap.put("maroon","#800000");
		aMap.put("green","#008000");
		aMap.put("purple","#800080");
		aMap.put("fuchsia","#FF00FF");
		aMap.put("lime","#00FF00");
		aMap.put("red","#FF0000");
		aMap.put("black","#000000");
		aMap.put("navy","#000080");
		aMap.put("aqua","#00FFFF");
		aMap.put("grey","#808080");
		aMap.put("olive","#808000");
		aMap.put("yellow","#FFFF00");
		aMap.put("silver","#C0C0C0");
		aMap.put("white","#FFFFFF");
		COLOR_MAP = Collections.unmodifiableMap(aMap);
    }
	
	public LayerListAdapter(FragmentActivity activity, int itemLayout){
		
		this.context = activity.getApplicationContext();
		this.inflater = LayoutInflater.from(this.context);
		this.items = new ArrayList<Layer>();
		this.itemLayout = itemLayout;
		this.activity = activity;
		this.arbiterProject = ArbiterProject.getArbiterProject();
		
		try {
			mapChangeListener = (MapChangeListener) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString() 
					+ " must implement MapChangeListener");
		}
	}
	
	public void setData(ArrayList<Layer> data){
		items = data;

		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final Layer listItem = getItem(position);
		
		if(listItem != null){
			if(listItem.getColor() != null) {
				View layerColorView = view.findViewById(R.id.layerColor);
				layerColorView.setBackgroundColor(Color.parseColor(COLOR_MAP.get(listItem.getColor())));
			}
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteLayer);
            ToggleButton layerVisibility = (ToggleButton) view.findViewById(R.id.layerVisibility);
            
            if(layerNameView != null){
            	layerNameView.setText(listItem.getLayerTitle());
            }
            
            if(serverNameView != null){
            	serverNameView.setText(listItem.getServerName());
            }
            
            if(deleteButton != null){
            	deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						deleteLayer(new Layer(listItem));
					}
            		
            	});
            }
            
            if(layerVisibility != null){
            	// Set the toggle to its appropriate position
            	layerVisibility.setChecked(listItem.isChecked());
                
            	layerVisibility.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						listItem.setChecked(!listItem.isChecked());
						
						updateLayerVisibility(listItem.getLayerId(), listItem.isChecked()); 
					}
				});
            }
		}
		
		return view;
	}
	
	private void updateLayerVisibility(final long layerId, final boolean visibility){
		final ContentValues values = new ContentValues();
		final String projectName = arbiterProject.getOpenProject(activity);
		
		values.put(LayersHelper.LAYER_VISIBILITY, visibility);
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				ProjectDatabaseHelper helper = ProjectDatabaseHelper.
						getHelper(context, ProjectStructure
								.getProjectPath(projectName), false);
				
				LayersHelper.getLayersHelper().updateAttributeValues(helper.getWritableDatabase(), context, layerId, values, new Runnable(){
					@Override
					public void run(){
						mapChangeListener.getMapChangeHelper()
							.onLayerVisibilityChanged(layerId);
					}
				});
			}
		});
	}
	
	private void deleteLayer(final Layer layer){
		final String projectName = arbiterProject.getOpenProject(activity);
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				
				String path = ProjectStructure.getProjectPath(projectName);
				
				ProjectDatabaseHelper projectHelper = ProjectDatabaseHelper.
						getHelper(context, path, false);
				
				FeatureDatabaseHelper featureHelper = 
						FeatureDatabaseHelper.getHelper(context, path, false);
				
				LayersHelper.getLayersHelper().delete(
					projectHelper.getWritableDatabase(), 
					featureHelper.getWritableDatabase(), 
					context, 
					layer
				);
					
				mapChangeListener.getMapChangeHelper()
					.onLayerDeleted(layer.getLayerId());
			}
		});
	}
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.size();
	}

	@Override
	public Layer getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ArrayList<Layer> getLayers(){
		return items;
	}
}
