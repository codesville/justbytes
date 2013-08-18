package com.justbytes.itechquiz.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.justbytes.itechquiz.Category;
import com.justbytes.itechquiz.R;

public class MainListArrayAdapter extends ArrayAdapter<String> {

	private final Context context;
	private int resourceId;
	private String[] categoryNames;

	public MainListArrayAdapter(Context context, int resource, String[] objects) {
		super(context, resource, objects);
		this.context = context;
		this.resourceId = resource;
		this.categoryNames = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(resourceId, parent, false);
		TextView txtVw = (TextView) rowView.findViewById(R.id.mainListText);
		ImageView imgVw = (ImageView) rowView.findViewById(R.id.mainListImage);
		txtVw.setText(categoryNames[position]);
		final Category cat = Category.valueOf(Category.DotNet.toString()
				.equals(categoryNames[position]) ? Category.DotNet.name()
				: categoryNames[position]);
		int imageId = -1;
		switch (cat) {
		case Java:
			imageId = R.drawable.javalogo;
			break;
		case DotNet:
			imageId = R.drawable.dotnetlogo;
			break;
		case Sql:
			imageId = R.drawable.sqllogo;
			break;
		case Unix:
			imageId = R.drawable.unixlogo;
			break;
		case Spring:
			imageId = R.drawable.spring;
			break;
		case Hibernate:
			imageId = R.drawable.hibernate;
			break;
		case SOA:
			imageId = R.drawable.soalogo;
			break;
		case XML:
			imageId = R.drawable.xmllogo;
			break;
		case JavaScript:
			imageId = R.drawable.jslogo;
			break;

		}
		imgVw.setImageResource(imageId);

		return rowView;

	}

}
