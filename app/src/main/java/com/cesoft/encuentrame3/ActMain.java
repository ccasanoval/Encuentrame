package com.cesoft.encuentrame3;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;


//https://guides.codepath.com/android/Handling-Scrolls-with-CoordinatorLayout
//https://developer.android.com/training/permissions/requesting.html
//MOCK LOCATIONS ON DEVICE : http://stackoverflow.com/questions/2531317/android-mock-location-on-device

//https://www.smashingmagazine.com/tag/android/

// *** Dagger
//http://www.vogella.com/tutorials/Dagger/article.html
//https://www.future-processing.pl/blog/dependency-injection-with-dagger-2/

// *** Rx
//https://gist.github.com/staltz/868e7e9bc2a7b8c1f754
//https://github.com/nmoskalenko/rxFirebase
//https://github.com/patloew/RxLocation
//
//http://sglora.com/android-tutorial-sobre-retrolambda/

// *** Charles, Fiddle
//https://jaanus.com/debugging-http-on-an-android-phone-or-tablet-with-charles-proxy-for-fun-and-profit/

// *** DEX
//I run the ./gradlew assembleRelease and use DEX decompiler tools https://github.com/skylot/jadx to decompile the APK. It’s like looking into code in Github...

// *** BATTERY
//https://developer.android.com/training/monitoring-device-state/doze-standby?hl=es-419
//https://developer.zebra.com/community/home/blog/2017/05/04/keeping-your-application-running-when-the-device-wants-to-sleep

//TODO: Conectar con un smart watch en la ruta y cada punto que guarde bio-metrics...?!   --->   https://github.com/patloew

//TODO: Avisar con TextToVoice y permitir no hacerlo mediante las opciones....
//TODO: Cambiar ListView por recyclerview
//TODO: AVISO: no molestar mas por hoy
//TODO: main window=> Number or routes, places and geofences...
//TODO: Egg?
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: Opcion que diga no preguntar por activar GPS ni BATTERY (en tablet que no tiene gps...)
//http://developer.android.com/intl/es/training/basics/supporting-devices/screens.html
// small, normal, large, xlarge   ///  low (ldpi), medium (mdpi), high (hdpi), extra high (xhdpi)

//TODO:---------------------------------------------------------------------------------------------
// Guardar actividad de cada punto, desactivar servicios si esta parado, etc
// https://developers.google.com/location-context/activity-recognition/
// https://github.com/googlesamples/android-play-location


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity implements FrgMain.MainIterface
{
	private static final String TAG = ActMain.class.getSimpleName();

	private FrgMain[] _aFrg = new FrgMain[3];
	private ViewPager _viewPager;
	private Login _login;
	private Util _util;

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		_login = ((App)getApplication()).getGlobalComponent().login();
		_util = ((App)getApplication()).getGlobalComponent().util();
		if(!_login.isLogged())gotoLogin();

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setSubtitle(Login.getCurrentUserName());

		createViews();
		gotoPage(getIntent());
		showMensaje(getIntent());
	}
	@Override
	protected void onNewIntent(Intent intent)
	{
		gotoPage(intent);
		showMensaje(intent);
	}
	//----------------------------------------------------------------------------------------------
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_viewPager = null;
	}
	//----------------------------------------------------------------------------------------------
	private static boolean oncePideBateria = true;
	@Override
	public void onStart()
	{
		super.onStart();
		if(!_login.isLogged())gotoLogin();
		if(oncePideBateria) {
			_util.pideBateria(this);
			oncePideBateria = false;
		}
		_util.pideGPS(this, 6969);
	}
	//----------------------------------------------------------------------------------------------
	@Override
	public void onStop() {
		super.onStop();
	}

	//----------------------------------------------------------------------------------------------
	private void createViews()
	{
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		_viewPager = findViewById(R.id.container);
		_viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(_viewPager);
		tabLayout.setSelectedTabIndicatorHeight(10);//TODO
	}
	private void gotoPage(Intent intent)
	{
		int nPagina = intent.getIntExtra(Constantes.WIN_TAB, Constantes.NADA);
		if(nPagina == Constantes.LUGARES || nPagina == Constantes.RUTAS || nPagina == Constantes.AVISOS)
			_viewPager.setCurrentItem(nPagina);
	}
	private void showMensaje(Intent intent)
	{
		String sMensaje = intent.getStringExtra(Constantes.MENSAJE);
		if(sMensaje != null && !sMensaje.isEmpty())
			Toast.makeText(this, sMensaje, Toast.LENGTH_LONG).show();
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_act_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will automatically handle clicks on the
		// Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
		Intent i;
		int id = item.getItemId();
		switch(id)
		{
			case R.id.action_config:
				startActivityForResult(new Intent(this, ActConfig.class), Constantes.CONFIG);
				return true;
			case R.id.action_mapa:
				i = new Intent(this, ActMaps.class);
				i.putExtra(Util.TIPO, _viewPager.getCurrentItem());
				startActivity(i);
				return true;
			case R.id.action_buscar:
				FrgMain frg = _aFrg[_viewPager.getCurrentItem()];// frg==null cuando se libero mem y luego se activó app...
				if(frg == null)new SectionsPagerAdapter(getSupportFragmentManager()).getItem(_viewPager.getCurrentItem());
				buscar(_aFrg[_viewPager.getCurrentItem()]);
				return true;
			case R.id.action_privacy_policy:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cesweb-ef91a.firebaseapp.com"));
				startActivity(browserIntent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != RESULT_OK)return;
		if(requestCode == Constantes.CONFIG)gotoLogin();
		else super.onActivityResult(requestCode, resultCode, data);
	}

	//----------------------------------------------------------------------------------------------
	/// FrgMain : MainIterface
	public void gotoLogin()
	{
		_login.logout();
		Intent intent = new Intent(getBaseContext(), ActLogin.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	//---
	public void onLugar()
	{
		startActivityForResult(new Intent(this, ActLugar.class), Constantes.LUGARES);
	}
	public void onAviso()
	{
		startActivityForResult(new Intent(this, ActAviso.class), Constantes.AVISOS);
	}
	public void onRuta()
	{
		startActivityForResult(new Intent(this, ActRuta.class), Constantes.RUTAS);
	}
	//---
	public void goLugar(Objeto obj)
	{
		Intent i = new Intent(this, ActLugar.class);
		i.putExtra(Objeto.NOMBRE, obj);
		startActivityForResult(i, Constantes.LUGARES);
	}
	public void goAviso(Objeto obj)
	{
		Intent i = new Intent(this, ActAviso.class);
		i.putExtra(Objeto.NOMBRE, obj);
		startActivityForResult(i, Constantes.AVISOS);
	}
	public void goRuta(Objeto obj)
	{
		try
		{
			Intent i = new Intent(this, ActRuta.class);
			i.putExtra(Objeto.NOMBRE, obj);
			startActivityForResult(i, Constantes.RUTAS);
		}
		catch(Exception e){Log.e(TAG, "------------------goRuta:onItemEdit:e:-----------------------", e);}
	}
	//---
	public void goLugarMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Objeto.NOMBRE, obj);
		i.putExtra(Util.TIPO, Constantes.LUGARES);
		startActivityForResult(i, Constantes.LUGARES);
	}
	public void goAvisoMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Objeto.NOMBRE, obj);
		i.putExtra(Util.TIPO, Constantes.AVISOS);
		startActivityForResult(i, Constantes.AVISOS);
	}
	public void goRutaMap(Objeto obj)
	{
		try
		{
			Intent i = new Intent(this, ActMaps.class);
			i.putExtra(Objeto.NOMBRE, obj);
			i.putExtra(Util.TIPO, Constantes.RUTAS);
			startActivityForResult(i, Constantes.RUTAS);
		}
		catch(Exception e){Log.e(TAG, "goRutaMap:RUTAS:e:-------------------------------------------", e);}
	}
	//---
	public void buscar(final FrgMain frg)
	{
		try
		{
			if( ! frg.isAdded())
			{
				Log.e(TAG, "buscar: *************** frg is not ADDED *****************");
				getSupportFragmentManager().beginTransaction().add(frg, String.valueOf(frg._sectionNumber)).commit();
				Util.exeDelayed(100, () -> buscar(frg));
				return;
			}

			Filtro fil = frg.getFiltro();
			Intent i = new Intent(/*frg.getContext()*/this, ActBuscar.class);//_main.getApplicationContext
			i.putExtra(Filtro.FILTRO, fil);
			frg.startActivityForResult(i, Constantes.BUSCAR);
		}
		catch(Exception e){Log.e(TAG, "buscar:e:----------------------------------------------------", e);}
	}
	//---
	public int getCurrentItem()
	{
		if(_viewPager == null)return Constantes.NADA;
		return _viewPager.getCurrentItem();
	}



	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////
	// FRAGMEN PAGER ADAPTER
	////////////////////////////////////////////////////////////////////////////////////////////////
	private class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			_aFrg[position] = FrgMain.newInstance(position);
			return _aFrg[position];
		}
		@Override
		public int getCount()
		{
			return 3;
		}
		@Override
		public CharSequence getPageTitle(int position)
		{
			switch(position)
			{
			case Constantes.LUGARES:return getString(R.string.lugares);
			case Constantes.RUTAS:	return getString(R.string.rutas);
			case Constantes.AVISOS:	return getString(R.string.avisos);
			}
			return null;
		}
	}


	//----------------------------------------------------------------------------------------------
	/*@SuppressLint("BatteryLife")
	private void pideBateria() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
			if(pm != null) {
				if(pm.isIgnoringBatteryOptimizations(getPackageName())) {
					Log.e(TAG, "pideBateria:isIgnoringBatteryOptimizations: ----*******************************************************************----------------AAA:"+getPackageName());
				}
				else {

					// Need this in manifest: <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
					// But by including this Google may delete the app from the store...
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
					intent.setData(Uri.parse("package:"+getApplicationContext().getPackageName()));
					startActivity(intent);

					// CHECK:
					//  turn off the screen and:
					//adb shell dumpsys battery unplug
					//adb shell dumpsys deviceidle step --> Till status =  IDLE_PENDING, SENSING, (LOCATING), IDLE_MAINTENANCE, IDLE

					Log.e(TAG, "pideBateria:isIgnoringBatteryOptimizations: ------**********************************************--------------BBB"+getPackageName());
				}
			}
		}
	}
	private void pideGPS() {
		//if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ! ha.canAccessLocation())activarGPS(true);
		int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
		if(permissionCheck == PackageManager.PERMISSION_DENIED)
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 6969);
	}*/
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		try {
			Log.e(TAG, "onRequestPermissionsResult------------------- requestCode = "
					+ requestCode + " : " + permissions[0] + " = " + grantResults[0]);
		}catch(Exception ignore){}
	}
}