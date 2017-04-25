package com.cesoft.encuentrame3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.util.Constantes;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
class LugarArrayAdapter extends ArrayAdapter<Lugar>
{
	private final Lugar[] _lugares;
	private IListaItemClick _inter;

	LugarArrayAdapter(Context context, Lugar[] lugares, IListaItemClick inter)
	{
		super(context, -1, lugares);
		_lugares = lugares;
		_inter = inter;
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

		holder.txtNombre.setText(_lugares[position].getNombre());
		holder.txtFecha.setText(Lugar.DATE_FORMAT2.format(_lugares[position].getFecha()));
		holder.btnEditar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_inter.onItemEdit(Constantes.LUGARES, _lugares[position]);
			}
		});
		holder.btnMapa.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_inter.onItemMap(Constantes.LUGARES, _lugares[position]);
			}
		});

		return convertView;
	}

}
