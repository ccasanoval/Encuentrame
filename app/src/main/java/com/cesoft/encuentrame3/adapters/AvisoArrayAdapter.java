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

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.util.Constantes;

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

		holder.txtNombre.setText(_avisos[position].getNombre());
		if(_avisos[position].getFecha()!=null)holder.txtFecha.setText(Aviso.DATE_FORMAT2.format(_avisos[position].getFecha()));
		if(!_avisos[position].isActivo())
		{
			holder.txtNombre.setTextColor(Color.GRAY);
		}
		else
		{
			//holder.txtNombre.setTextColor(Color.GREEN);
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
				holder.txtNombre.setTextColor(convertView.getResources().getColor(R.color.colorItem, convertView.getContext().getTheme()));
			else
				//noinspection deprecation
				holder.txtNombre.setTextColor(convertView.getResources().getColor(R.color.colorItem));
		}
		holder.btnEditar.setOnClickListener(v -> _inter.onItemEdit(Constantes.AVISOS, _avisos[position]));
		holder.btnMapa.setOnClickListener(v -> _inter.onItemMap(Constantes.AVISOS, _avisos[position]));

		return convertView;
	}
}
