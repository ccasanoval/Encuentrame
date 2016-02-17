package com.cesoft.encuentrame;

import android.content.Context;
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
	private final Context _context;
	private final Ruta[] _rutas;

	public RutaArrayAdapter(Context context, Ruta[] rutas)
	{
		super(context, -1, rutas);
		_context = context;
		_rutas = rutas;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.lista, parent, false);
		TextView txtNombre = (TextView)rowView.findViewById(R.id.txtNombre);
		txtNombre.setText(_rutas[position].getNombre()+" ("+_rutas[position].getPuntos().size()+")");//TODO:Enhance
		ImageButton btnEditar = (ImageButton)rowView.findViewById(R.id.btnEditar);
		ImageButton btnMapa = (ImageButton)rowView.findViewById(R.id.btnMapa);
		btnEditar.setOnClickListener(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//TODO: Abrir pantalla edicion con ruta
				}
			});
		btnMapa.setOnClickListener(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//TODO: Abrir pantalla mapa con ruta
				}
			});
		//if(s.startsWith("iPhone"))imageView.setImageResource(R.drawable.no);
		return rowView;
	}


}
