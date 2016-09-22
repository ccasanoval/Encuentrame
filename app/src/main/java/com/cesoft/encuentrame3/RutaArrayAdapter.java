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

	RutaArrayAdapter(Context context, Ruta[] rutas, IListaItemClick inter)
	{
		super(context, -1, rutas);
		_rutas = rutas;
		_inter = inter;
	}

	@Override
	public @NonNull View getView(final int position, View convertView, @NonNull ViewGroup parent)
	{
		if(convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.lista, parent, false);
			if(_rutas[position] == null)
			{
				System.err.println("RutaArrayAdapter:getView: _rutas["+position+"]=null");
				return convertView;
			}
			String sIdRuta = Util.getTrackingRoute(getContext());
			TextView txtNombre = (TextView)convertView.findViewById(R.id.txtNombre);
			txtNombre.setText(String.format(Locale.ENGLISH, "%s (%d)", _rutas[position].getNombre(), _rutas[position].getPuntosCount()));
			ImageButton btnEditar = (ImageButton)convertView.findViewById(R.id.btnEditar);
			ImageButton btnMapa = (ImageButton)convertView.findViewById(R.id.btnMapa);
			btnEditar.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						_inter.onItemEdit(Util.RUTAS, _rutas[position]);
					}
				});
			btnMapa.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						_inter.onItemMap(Util.RUTAS, _rutas[position]);
					}
				});

			TextView txtFecha = (TextView)convertView.findViewById(R.id.txtFecha);
			txtFecha.setText(Ruta.DATE_FORMAT2.format(_rutas[position].getFecha()));

			// Si la ruta se está grabando, resaltar
			//String sIdRuta = Util.getTrackingRoute();
			if(sIdRuta.equals(_rutas[position].getId()))
			{
	//System.err.println("----------------RUTA ACTIVA:"+sIdRuta+" ::: "+_rutas[position]+"..."+position+"....."+convertView);
				txtNombre.setTextColor(Color.RED);
				convertView.setBackgroundColor(Color.YELLOW);
			}
			else
			{
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
					txtNombre.setTextColor(convertView.getResources().getColor(R.color.colorItem, convertView.getContext().getTheme()));
				else
					//noinspection deprecation
					txtNombre.setTextColor(convertView.getResources().getColor(R.color.colorItem));
				convertView.setBackgroundColor(Color.WHITE);
			}
		}
		return convertView;
	}
}
