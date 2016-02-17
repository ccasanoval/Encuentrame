package com.cesoft.encuentrame;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cesoft.encuentrame.models.Lugar;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 12/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://www.vogella.com/tutorials/AndroidListView/article.html
public class LugarArrayAdapter extends ArrayAdapter<Lugar>
{
	//private final Context _context;
	private final Lugar[] _lugares;

	public LugarArrayAdapter(Context context, Lugar[] lugares)//TODO:ArrayList<Lugares>
	{
		super(context, -1, lugares);
		//_context = context;
		_lugares = lugares;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// Get the data item for this position
		Lugar user = _lugares[position];
		// Check if an existing view is being reused, otherwise inflate the view
		if(convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.lista, parent, false);

//System.err.println("----------------------"+position+" : "+_lugares[position]);
		TextView txtNombre = (TextView)convertView.findViewById(R.id.txtNombre);
		txtNombre.setText(_lugares[position].getNombre());
		ImageButton btnEditar = (ImageButton)convertView.findViewById(R.id.btnEditar);
		ImageButton btnMapa = (ImageButton)convertView.findViewById(R.id.btnMapa);
		btnEditar.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//TODO: Abrir pantalla edicion con punto
					/*Intent i = new Intent(LugarArrayAdapter.this.getContext(), ActLugar.class);
					i.putExtra("lugar", _lugares[position]);
					startActivityForResult(i, 69);//TODO: si es guardado, borrado => refresca la vista, si no nada*/
				}
			});
		btnMapa.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//TODO: Abrir pantalla mapa con punto
				}
			});

		return convertView;

		/*
		LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.lista, parent, false);

		//if(s.startsWith("iPhone"))imageView.setImageResource(R.drawable.no);
		return rowView;*/
	}


}
