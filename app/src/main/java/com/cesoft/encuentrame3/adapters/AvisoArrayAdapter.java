package com.cesoft.encuentrame3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class AvisoArrayAdapter extends RecyclerView.Adapter<ItemViewHolder>
{
	private final Aviso[] avisos;
	private IListaItemClick inter;

	public AvisoArrayAdapter(Aviso[] avisos, IListaItemClick inter)
	{
		this.avisos = avisos;
		this.inter = inter;
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista, parent, false);
		return new ItemViewHolder(view, inter);
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
		holder.bind(avisos[position]);
	}

	@Override
	public int getItemCount() {
		return avisos.length;
	}
}
