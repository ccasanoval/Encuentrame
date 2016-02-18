package com.cesoft.encuentrame;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cesoft.encuentrame.models.Lugar;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class LugarArrayAdapter extends ArrayAdapter<Lugar>
{
	private final Lugar[] _lugares;
	private CesIntLista _inter;

	public LugarArrayAdapter(Context context, Lugar[] lugares, CesIntLista inter)
	{
		super(context, -1, lugares);
		_lugares = lugares;
		_inter = inter;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// Check if an existing view is being reused, otherwise inflate the view
		if(convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.lista, parent, false);

//System.err.println("----------------------"+position+" : "+_lugares[position]);
		TextView txtNombre = (TextView)convertView.findViewById(R.id.txtNombre);
		txtNombre.setText(_lugares[position].getNombre());
		ImageButton btnEditar = (ImageButton)convertView.findViewById(R.id.btnEditar);
		ImageButton btnMapa = (ImageButton)convertView.findViewById(R.id.btnMapa);
		btnEditar.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemEdit(CesIntLista.tipoLista.LUGAR, _lugares[position]);
				}
			});
		btnMapa.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemMap(CesIntLista.tipoLista.LUGAR, _lugares[position]);
				}
			});

		return convertView;

		/*
		LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.lista, parent, false);

		//if(s.startsWith("iPhone"))imageView.setImageResource(R.drawable.no);
		return rowView;*/
	}

}
