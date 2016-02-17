package com.cesoft.encuentrame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame.models.Aviso;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class AvisoArrayAdapter extends ArrayAdapter<Aviso>
{
	private final Context _context;
	private final Aviso[] _avisos;

	public AvisoArrayAdapter(Context context, Aviso[] avisos)
	{
		super(context, -1, avisos);
		_context = context;
		_avisos = avisos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.lista, parent, false);
		TextView txtNombre = (TextView)rowView.findViewById(R.id.txtNombre);
		txtNombre.setText(_avisos[position].getNombre());
		ImageButton btnEditar = (ImageButton)rowView.findViewById(R.id.btnEditar);
		ImageButton btnMapa = (ImageButton)rowView.findViewById(R.id.btnMapa);
		btnEditar.setOnClickListener(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//TODO: Abrir pantalla edicion con aviso
				}
			});
		btnMapa.setOnClickListener(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//TODO: Abrir pantalla mapa con aviso
				}
			});
		//if(s.startsWith("iPhone"))imageView.setImageResource(R.drawable.no);
		return rowView;
	}


}
