package com.cesoft.encuentrame3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Ruta;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
public class RutaArrayAdapter extends RecyclerView.Adapter<ItemViewHolder>
{
	private final Ruta[] rutas;
	private final IListaItemClick inter;

	public RutaArrayAdapter(Ruta[] rutas, IListaItemClick inter)
	{
		this.rutas = rutas;
		this.inter = inter;
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista, parent, false);
		return new RutaViewHolder(view, inter);
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
		holder.bind(rutas[position]);
	}

	@Override
	public int getItemCount() {
		return rutas.length;
	}
}

/*
*
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
	}*/