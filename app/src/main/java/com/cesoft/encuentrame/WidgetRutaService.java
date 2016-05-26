package com.cesoft.encuentrame;

import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;


////////////////////////////////////////////////////////////////////////////////////////////////////
// 
////////////////////////////////////////////////////////////////////////////////////////////////////
public class WidgetRutaService extends Service
{
	private static Handler _h = null;
	private static Runnable _r = null;
	private static final int _DELAY = 5*60*1000;
	private static Long _id = -1L;

	//______________________________________________________________________________________________
	@Override
	//public void onStart(Intent intent, int startId)
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
		if(_h == null)
		{
			_h = new Handler();
			_r = new Runnable()
			{
				@Override
				public void run()
				{
					System.err.println("-----RUN WIDGET-----");
					cambiarTextoWidget(intent);
					_h.postDelayed(_r, _DELAY);
				}
			};
			_h.postDelayed(_r, _DELAY);
		}
		cambiarTextoWidget(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	//______________________________________________________________________________________________
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	//______________________________________________________________________________________________
	private void cambiarTextoWidget(Intent intent)
	{
		try
		{
			String s="xxx";
			/*
			ArrayList<Objeto> lista;// = new ArrayList<>();
			//Iterator<Objeto> it =(Iterator<Objeto>)Objeto.findAll(Objeto.class);while(it.hasNext())lista.add(it.next());
			lista = (ArrayList<Objeto>)Objeto.findWithQuery(Objeto.class, "select * from Objeto where _padre is not null and _i_prioridad > 3 order by _i_prioridad desc");
			if(lista == null || lista.size() < 1)
				lista = (ArrayList<Objeto>)Objeto.findWithQuery(Objeto.class, "select * from Objeto where _padre is not null order by _i_prioridad desc");
			if(lista == null || lista.size() < 1)
				lista = (ArrayList<Objeto>)Objeto.findWithQuery(Objeto.class, "select * from Objeto order by _i_prioridad desc");

			if(lista != null && lista.size() > 0)
			{
				Objeto o = lista.get(0);
				if(lista.size() > 1)
				{
					for(int i=0; i < lista.size()-1; i++)
					{
						if(lista.get(i).getId().equals(_id))
						{
							o = lista.get(i+1);
							break;
						}
					}
				}
				_id = o.getId();
				s = o.getNombre();
			}

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
			int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
	//    ComponentName thisWidget = new ComponentName(getApplicationContext(), CesWidgetProvider.class);
	//    int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
			for(int widgetId : allWidgetIds)
			{
				RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_ruta);
				remoteViews.setTextViewText(R.id.txtTarea, s);

				Intent clickIntent = new Intent(this.getApplicationContext(), WidgetRuta.class);
				clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.txtTarea, pendingIntent);
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}*/
			stopSelf();
		}
		catch(Exception e)
		{
			System.err.println("WidgetRutaService:onStartCommand:e: "+e);
		}
	}
}
