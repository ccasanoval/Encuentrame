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


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity implements FrgMain.MainIterface
{
	private static final String TAG = ActMain.class.getSimpleName();

	private FrgMain[] frmMain = new FrgMain[3];
	private ViewPager viewPager;
	private Login login;
	private Util util;

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		login = ((App)getApplication()).getGlobalComponent().login();
		util = ((App)getApplication()).getGlobalComponent().util();
		if(!login.isLogged())gotoLogin();

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
		viewPager = null;
	}
	//----------------------------------------------------------------------------------------------
	private boolean oncePideBateria = true;
	@Override
	public void onStart()
	{
		super.onStart();
		if(!login.isLogged())gotoLogin();
		if(oncePideBateria) {
			util.pideBateria();
			oncePideBateria = false;
		}
		util.pideGPS(this, 6969);
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
		viewPager = findViewById(R.id.container);
		viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
	}
	private void gotoPage(Intent intent)
	{
		int nPagina = intent.getIntExtra(Constantes.WIN_TAB, Constantes.NADA);
		if(nPagina == Constantes.LUGARES || nPagina == Constantes.RUTAS || nPagina == Constantes.AVISOS)
			viewPager.setCurrentItem(nPagina);
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
				i.putExtra(Util.TIPO, viewPager.getCurrentItem());
				startActivity(i);
				return true;
			case R.id.action_buscar:
				FrgMain frg = frmMain[viewPager.getCurrentItem()];// frg==null cuando se libero mem y luego se activó app...
				if(frg == null)new SectionsPagerAdapter(getSupportFragmentManager()).getItem(viewPager.getCurrentItem());
				buscar(frmMain[viewPager.getCurrentItem()]);
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
		login.logout();
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
				getSupportFragmentManager().beginTransaction().add(frg, String.valueOf(frg.getSectionNumber())).commit();
				util.exeDelayed(100, () -> buscar(frg));
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
		if(viewPager == null)return Constantes.NADA;
		return viewPager.getCurrentItem();
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
			frmMain[position] = FrgMain.newInstance(position);
			return frmMain[position];
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
			default:break;
			}
			return null;
		}
	}

	/*@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		try {
			Log.e(TAG, "onRequestPermissionsResult------------------- requestCode = "
					+ requestCode + " : " + permissions[0] + " = " + grantResults[0]);
		}catch(Exception ignore){}
	}*/
}