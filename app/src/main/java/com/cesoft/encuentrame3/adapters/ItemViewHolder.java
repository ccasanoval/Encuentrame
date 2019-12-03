package com.cesoft.encuentrame3.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/06/2019
public class ItemViewHolder extends RecyclerView.ViewHolder {

    protected TextView txtNombre;
    private TextView txtFecha;
    private IListaItemClick inter;

    ItemViewHolder(@NonNull View itemView, IListaItemClick inter) {
        super(itemView);
        this.inter = inter;
        txtNombre = itemView.findViewById(R.id.txtNombre);
        txtFecha = itemView.findViewById(R.id.txtFecha);
    }

    public void bind(Objeto obj) {
        txtNombre.setText(obj.nombre);
        txtFecha.setText(Objeto.DATE_FORMAT.format(obj.fechaLong));
        final int type;
        if(obj instanceof Lugar) type = Constantes.LUGARES;
        else if(obj instanceof Ruta) type = Constantes.RUTAS;
        else if(obj instanceof Aviso) type = Constantes.AVISOS;
        else type = Constantes.LUGARES;
        ImageButton btnEditar = itemView.findViewById(R.id.btnEditar);
        ImageButton btnMapa = itemView.findViewById(R.id.btnMapa);
        btnEditar.setOnClickListener(v -> inter.onItemEdit(type, obj));
        btnMapa.setOnClickListener(v -> inter.onItemMap(type, obj));
    }
}