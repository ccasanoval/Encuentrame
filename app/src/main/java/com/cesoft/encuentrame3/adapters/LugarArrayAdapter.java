package com.cesoft.encuentrame3.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.util.Constantes;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class LugarArrayAdapter extends ArrayAdapter<Lugar>
{
	private final Lugar[] lugares;
	private IListaItemClick inter;

	public LugarArrayAdapter(Context context, Lugar[] lugares, IListaItemClick inter)
	{
		super(context, -1, lugares);
		this.lugares = lugares;
		this.inter = inter;
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

		holder.txtNombre.setText(lugares[position].getNombre());
		holder.txtFecha.setText(Lugar.DATE_FORMAT2.format(lugares[position].getFecha()));
		holder.btnEditar.setOnClickListener(v -> inter.onItemEdit(Constantes.LUGARES, lugares[position]));
		holder.btnMapa.setOnClickListener(v -> inter.onItemMap(Constantes.LUGARES, lugares[position]));

		return convertView;
	}

}
