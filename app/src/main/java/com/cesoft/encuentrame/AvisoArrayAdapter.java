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
	private final Aviso[] _avisos;
	private IListaItemClick _inter;

	public AvisoArrayAdapter(Context context, Aviso[] avisos, IListaItemClick inter)
	{
		super(context, -1, avisos);
		_avisos = avisos;
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
		txtNombre.setText(_avisos[position].getNombre());
		ImageButton btnEditar = (ImageButton)convertView.findViewById(R.id.btnEditar);
		ImageButton btnMapa = (ImageButton)convertView.findViewById(R.id.btnMapa);
		btnEditar.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemEdit(Util.AVISOS, _avisos[position]);
				}
			});
		btnMapa.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemMap(Util.AVISOS, _avisos[position]);
				}
			});

		TextView txtFecha = (TextView)convertView.findViewById(R.id.txtFecha);
		txtFecha.setText(Aviso.DATE_FORMAT2.format(_avisos[position].getFecha()));

		return convertView;
	}
}
