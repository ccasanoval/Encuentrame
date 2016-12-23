package com.cesoft.encuentrame3;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Ruta;

import java.util.Locale;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
class RutaArrayAdapter extends ArrayAdapter<Ruta>
{
	private final Ruta[] _rutas;
	private IListaItemClick _inter;
	private Util _util;

	RutaArrayAdapter(Context context, Ruta[] rutas, IListaItemClick inter, Util util)
	{
		super(context, -1, rutas);
		_rutas = rutas;
		_inter = inter;
		_util = util;
	}


	private class ViewHolder
	{
		private TextView txtNombre;
		private TextView txtFecha;
		private ImageButton btnEditar;
		private ImageButton btnMapa;
	}
	@Override
	public @NonNull View getView(final int position, View convertView, @NonNull ViewGroup parent)
	{
		final ViewHolder holder;
		if(convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.lista, parent, false);
			holder = new ViewHolder();
			holder.txtNombre = (TextView)convertView.findViewById(R.id.txtNombre);
			holder.txtFecha = (TextView)convertView.findViewById(R.id.txtFecha);
			holder.btnEditar = (ImageButton)convertView.findViewById(R.id.btnEditar);
			holder.btnMapa = (ImageButton)convertView.findViewById(R.id.btnMapa);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.txtNombre.setText(String.format(Locale.ENGLISH, "%s (%d)", _rutas[position].getNombre(), _rutas[position].getPuntosCount()));
		holder.txtFecha.setText(Ruta.DATE_FORMAT2.format(_rutas[position].getFecha()));
		holder.btnEditar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_inter.onItemEdit(Util.RUTAS, _rutas[position]);
			}
		});
		holder.btnMapa.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_inter.onItemMap(Util.RUTAS, _rutas[position]);
			}
		});
		// Si la ruta se está grabando, resaltar
		if(_rutas[position].getId().equals(_util.getTrackingRoute()))
		{
			holder.txtNombre.setTextColor(Color.RED);
			convertView.setBackgroundColor(Color.YELLOW);
		}
		else
		{
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
				holder.txtNombre.setTextColor(convertView.getResources().getColor(R.color.colorItem, convertView.getContext().getTheme()));
			else
				//noinspection deprecation
				holder.txtNombre.setTextColor(convertView.getResources().getColor(R.color.colorItem));
			convertView.setBackgroundColor(Color.WHITE);
		}

		return convertView;
	}
}
