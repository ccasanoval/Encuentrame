package com.cesoft.encuentrame3.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class AvisoArrayAdapter extends RecyclerView.Adapter<ItemViewHolder>
{
	private final Aviso[] avisos;
	private final IListaItemClick inter;

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
		Aviso a = avisos[position];
		int color;
		if(a.isActivo())
			color = android.R.color.holo_green_dark;
		else
			color = android.R.color.darker_gray;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			color = App.getInstance().getColor(color);
		else
			color = App.getInstance().getResources().getColor(color);

		holder.txtNombre.setTextColor(color);
		holder.bind(a);
	}

	@Override
	public int getItemCount() {
		return avisos.length;
	}
}
