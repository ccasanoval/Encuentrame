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
	/*public class LugarViewHolder extends RecyclerView.ViewHolder
	{
		private TextView txtNombre;
		private TextView txtFecha;
		private ImageButton btnEditar;
		private ImageButton btnMapa;

		public LugarViewHolder(@NonNull View itemView) {
			super(itemView);
			txtNombre = itemView.findViewById(R.id.txtNombre);
			txtFecha = itemView.findViewById(R.id.txtFecha);
			btnEditar = itemView.findViewById(R.id.btnEditar);
			btnMapa = itemView.findViewById(R.id.btnMapa);

			btnEditar.setOnClickListener(v -> onEditarClick());
			btnMapa.setOnClickListener(v -> onMapaClick());
		}

		public void bind(Lugar lugar) {
			txtNombre.setText(lugar.nombre);
			txtFecha.setText(lugar.fecha.toString());
		}

		private void onEditarClick() {}
		private void onMapaClick() {}
	}*/

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


	/*@Override
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
	}*/

}
