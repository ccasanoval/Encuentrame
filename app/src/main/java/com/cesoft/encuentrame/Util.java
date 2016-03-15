package com.cesoft.encuentrame;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/03/2016.
public class Util
{
	protected  static Location _locLast;

	//______________________________________________________________________________________________
	public static void setLocation(Location loc){_locLast=loc;}
	public static Location getLocation(Context c)
	{
		Location location1=null, location2=null;
		try
		{
			LocationManager locationManager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
			if(locationManager == null)return _locLast;
			boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if(isNetworkEnabled)
				location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(isGPSEnabled)
				location2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(location1==null && location2==null)return _locLast;
			if(_locLast == null)_locLast = location1!=null?location1:location2;
			if(location1.getTime() > _locLast.getTime())
				_locLast = location1;
			else if(location2.getTime() > _locLast.getTime())
				_locLast = location2;
		}
		catch(SecurityException se)
		{
			se.printStackTrace();
		}
		return _locLast;
    }
}
