package com.cesoft.encuentrame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame.models.Lugar;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class LugarArrayAdapter extends ArrayAdapter<Lugar>
{
	private final Lugar[] _lugares;
	private IListaItemClick _inter;

	public LugarArrayAdapter(Context context, Lugar[] lugares, IListaItemClick inter)
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

		TextView txtNombre = (TextView)convertView.findViewById(R.id.txtNombre);
		txtNombre.setText(_lugares[position].getNombre());
		ImageButton btnEditar = (ImageButton)convertView.findViewById(R.id.btnEditar);
		ImageButton btnMapa = (ImageButton)convertView.findViewById(R.id.btnMapa);
		btnEditar.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemEdit(Util.LUGARES, _lugares[position]);
				}
			});
		btnMapa.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_inter.onItemMap(Util.LUGARES, _lugares[position]);
				}
			});

		TextView txtFecha = (TextView)convertView.findViewById(R.id.txtFecha);
		txtFecha.setText(Lugar.DATE_FORMAT2.format(_lugares[position].getFecha()));

		return convertView;
	}

}
