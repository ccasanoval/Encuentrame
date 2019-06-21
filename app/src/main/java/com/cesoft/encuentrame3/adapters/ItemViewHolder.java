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

import java.text.SimpleDateFormat;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/06/2019
public class ItemViewHolder extends RecyclerView.ViewHolder {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //private Objeto obj;
    private TextView txtNombre;
    private TextView txtFecha;
    private IListaItemClick inter;

    public ItemViewHolder(@NonNull View itemView, IListaItemClick inter) {
        super(itemView);
        this.inter = inter;
        txtNombre = itemView.findViewById(R.id.txtNombre);
        txtFecha = itemView.findViewById(R.id.txtFecha);
    }

    public void bind(Objeto obj) {
        //this.obj = obj;
        txtNombre.setText(obj.nombre);
        txtFecha.setText(simpleDateFormat.format(obj.fecha));

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