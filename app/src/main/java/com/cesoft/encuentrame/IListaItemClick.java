package com.cesoft.encuentrame;

import com.cesoft.encuentrame.models.Objeto;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
public interface IListaItemClick
{
	//public static enum tipoLista {LUGAR, RUTA, AVISO};
	public void onItemEdit(int tipo, Objeto obj);
	public void onItemMap(int tipo, Objeto obj);
	public void onRefreshListaRutas();
}
