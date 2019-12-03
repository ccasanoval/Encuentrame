package com.cesoft.encuentrame3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Lugar;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class LugarArrayAdapter extends RecyclerView.Adapter<ItemViewHolder>
{
	private final Lugar[] lugares;
	private IListaItemClick inter;

	public LugarArrayAdapter(Lugar[] lugares, IListaItemClick inter)
	{
		this.lugares = lugares;
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
		holder.bind(lugares[position]);
	}

	@Override
	public int getItemCount() {
		return lugares.length;
	}
}
