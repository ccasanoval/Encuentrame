package com.cesoft.encuentrame3.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Util;

import java.util.Locale;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class RutaArrayAdapter extends ArrayAdapter<Ruta>
{
	private final Ruta[] rutas;
	private IListaItemClick inter;

	@Inject	Util util;

	public RutaArrayAdapter(Context context, Ruta[] rutas, IListaItemClick inter)
	{
		super(context, -1, rutas);
		this.rutas = rutas;
		this.inter = inter;
		util = App.getComponent(getContext()).util();
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
			holder.txtNombre = convertView.findViewById(R.id.txtNombre);
			holder.txtFecha = convertView.findViewById(R.id.txtFecha);
			holder.btnEditar = convertView.findViewById(R.id.btnEditar);
			holder.btnMapa = convertView.findViewById(R.id.btnMapa);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.txtNombre.setText(String.format(Locale.ENGLISH, "%s (%d)", rutas[position].getNombre(), rutas[position].getPuntosCount()));
		holder.txtFecha.setText(Ruta.DATE_FORMAT2.format(rutas[position].getFecha()));
		holder.btnEditar.setOnClickListener(v -> inter.onItemEdit(Constantes.RUTAS, rutas[position]));
		holder.btnMapa.setOnClickListener(v -> inter.onItemMap(Constantes.RUTAS, rutas[position]));
		// Si la ruta se está grabando, resaltar
		if(rutas[position].getId() != null && rutas[position].getId().equals(util.getTrackingRoute()))
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
