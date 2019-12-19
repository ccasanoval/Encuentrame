package com.cesoft.encuentrame3.util

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsLocationCallback @Inject constructor() : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        val locationList = locationResult.locations
        if(locationList.isNotEmpty()) {
            val location = locationList[locationList.size - 1]
            EventBus.getDefault().post(location)
        }
    }
}