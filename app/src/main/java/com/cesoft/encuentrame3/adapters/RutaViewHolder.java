package com.cesoft.encuentrame3.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import androidx.annotation.NonNull;
import javax.inject.Inject;
import java.util.Locale;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Util;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/06/2019
public class RutaViewHolder extends ItemViewHolder {

    @Inject Util util;

    public RutaViewHolder(@NonNull View itemView, IListaItemClick inter) {
        super(itemView, inter);
        Context appContext = itemView.getContext().getApplicationContext();
        App.getComponent(appContext).inject(this);
    }

    @Override
    public void bind(Objeto obj) {
        super.bind(obj);
        Ruta ruta = (Ruta)obj;
        txtNombre.setText(String.format(Locale.ENGLISH, "%s (%d)", ruta.getNombre(), ruta.getPuntosCount()));
        //txtFecha.setText(Objeto.DATE_FORMAT2.format(ruta.getFecha()));
        // Si la ruta se está grabando, resaltar
        if(ruta.getId() != null && ruta.getId().equals(util.getTrackingRoute()))
        {
            txtNombre.setTextColor(Color.RED);
            itemView.setBackgroundColor(Color.YELLOW);
        }
        else
        {
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                txtNombre.setTextColor(itemView.getResources().getColor(R.color.colorItem, itemView.getContext().getTheme()));
            else
                txtNombre.setTextColor(itemView.getResources().getColor(R.color.colorItem));
            itemView.setBackgroundColor(Color.WHITE);
        }

    }
}