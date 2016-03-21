package com.cesoft.encuentrame;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame.models.Ruta;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class RutaArrayAdapter extends ArrayAdapter<Ruta>
{
	private final Ruta[] _rutas;
	private CesIntLista _inter;

	public RutaArrayAdapter(Context context, Ruta[] rutas, CesIntLista inter)
	{
		super(context, -1, rutas);
		_rutas = rutas;
		_inter = inter;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.lista, parent, false);

		TextView txtNombre = (TextView)convertView.findViewById(R.id.txtNombre);
		txtNombre.setText(String.format("%s (%d)", _rutas[position].getNombre(), _rutas[position].getPuntos().size()));
		ImageButton btnEditar = (ImageButton)convertView.findViewById(R.id.btnEditar);
		ImageButton btnMapa = (ImageButton)convertView.findViewById(R.id.btnMapa);
		btnEditar.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemEdit(CesIntLista.tipoLista.RUTA, _rutas[position]);
				}
			});
		btnMapa.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemMap(CesIntLista.tipoLista.RUTA, _rutas[position]);
				}
			});

		// Si la ruta se está grabando, resaltar
		String sIdRuta = Util.getTrackingRoute();
		if(sIdRuta.equals(_rutas[position].getObjectId()))
		{
			txtNombre.setTextColor(Color.RED);
			convertView.setBackgroundColor(Color.YELLOW);
		}

		return convertView;
	}
}
