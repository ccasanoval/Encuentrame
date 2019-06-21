package com.cesoft.encuentrame3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class RutaArrayAdapter extends RecyclerView.Adapter<ItemViewHolder>
{
	private final Ruta[] rutas;
	private IListaItemClick inter;

	@Inject	Util util;

	public RutaArrayAdapter(Context context, Ruta[] rutas, IListaItemClick inter)
	{
		this.rutas = rutas;
		this.inter = inter;
		util = App.getComponent(context).util();
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista, parent, false);
		return new ItemViewHolder(view, inter);
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
