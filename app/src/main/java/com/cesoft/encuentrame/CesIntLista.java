package com.cesoft.encuentrame;

import com.cesoft.encuentrame.models.Objeto;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
public interface CesIntLista
{
	//public static enum tipoLista {LUGAR, RUTA, AVISO};
	public void onItemEdit(tipoLista tipo, Objeto obj);
	public void onItemMap(tipoLista tipo, Objeto obj);

	public enum tipoLista
	{
		LUGAR(1), RUTA(2), AVISO(3);
		private int value;
		private tipoLista(int value){this.value = value;}
		public int getValue(){return value;}
	}
}
