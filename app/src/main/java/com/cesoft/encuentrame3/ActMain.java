package com.cesoft.encuentrame3;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

//TODO: Conectar con un smart watch en la ruta y cada punto que guarde bio-metrics...?!   --->   https://github.com/patloew

//TODO: OPCIONES para habilitar o deshabilitar TextToVoice
//TODO: AVISO: no molestar mas por hoy
//TODO: main window=> Number or routes, places and geofences...
//TODO: Egg?
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: Opcion que diga no preguntar por activar GPS ni BATTERY (en tablet que no tiene gps...)
//http://developer.android.com/intl/es/training/basics/supporting-devices/screens.html
// small, normal, large, xlarge   ///  low (ldpi), medium (mdpi), high (hdpi), extra high (xhdpi)


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft
public class ActMain extends AppCompatActivity implements FrgMain.MainIterface
{
	private static final String TAG = ActMain.class.getSimpleName();

	private FrgMain[] frmMain = new FrgMain[3];
	private ViewPager viewPager;
	private Login login;
	private Util util;
	private Voice voice;
	private MenuItem vozMenuItem;

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		login = ((App)getApplication()).getGlobalComponent().login();
		if(!login.isLogged())gotoLogin();
		util = ((App)getApplication()).getGlobalComponent().util();
		voice = ((App)getApplication()).getGlobalComponent().voice();
		voice.setActivity(this);

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
		super.onNewIntent(intent);
		gotoPage(intent);
		showMensaje(intent);
	}


	//----------------------------------------------------------------------------------------------
	@Override
	public void onPause()
	{
		voice.stopListening();
		super.onPause();
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public void onDestroy()
	{
		viewPager = null;
		super.onDestroy();
	}
	//----------------------------------------------------------------------------------------------
	private boolean oncePideBateria = true;
	@Override
	public void onStart()
	{
		super.onStart();
		if(!login.isLogged())gotoLogin();
		if(oncePideBateria) {
			util.pideBateria(this);
			oncePideBateria = false;
		}
		util.pideGPS(this, 6969);
		EventBus.getDefault().register(this);
	}
	//----------------------------------------------------------------------------------------------
	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	//----------------------------------------------------------------------------------------------
	private void createViews()
	{
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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
		vozMenuItem = menu.findItem(R.id.action_voz);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will automatically handle clicks on the
		// Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id)
		{
			case R.id.action_config:
				startActivityForResult(new Intent(this, ActConfig.class), Constantes.CONFIG);
				return true;
			case R.id.action_mapa:
				Intent i = new Intent(this, ActMaps.class);
				i.putExtra(Util.TIPO, viewPager.getCurrentItem());
				startActivity(i);
				return true;
			case R.id.action_buscar:
				FrgMain frg = frmMain[viewPager.getCurrentItem()];// frg==null cuando se libero mem y luego se activó app...
				if(frg == null)new SectionsPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT).getItem(viewPager.getCurrentItem());
				buscar(frmMain[viewPager.getCurrentItem()]);
				return true;
			case R.id.action_privacy_policy:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cesweb-ef91a.firebaseapp.com"));
				startActivity(browserIntent);
				return true;
			case R.id.action_voz:
				voice.toggleStatus();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != RESULT_OK)return;
		if(requestCode == Constantes.CONFIG)gotoLogin();
		else super.onActivityResult(requestCode, resultCode, data);
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onVoiceEvent(Voice.VoiceEvent event)
	{
		if(vozMenuItem != null)
			vozMenuItem.setIcon(event.isListening() ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_white_24dp);
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onCommandEvent(Voice.CommandEvent event)
	{
		Log.e(TAG, "onCommandEvent--------------------------- "+event.getCommand()+" / "+event.getText());
		Toast.makeText(this, event.getText()+" ("+event.getCommand()+")", Toast.LENGTH_LONG).show();

		switch(event.getCommand()) {
			case R.string.voice_new_point:
				viewPager.setCurrentItem(Constantes.LUGARES);
				onLugar(true);
				voice.speak(event.getText());
				break;
			case R.string.voice_new_route:
				viewPager.setCurrentItem(Constantes.RUTAS);
				onRuta(true);
				voice.speak(event.getText());
				break;
			case R.string.voice_new_alert:
				viewPager.setCurrentItem(Constantes.AVISOS);
				onAviso(true);
				voice.speak(event.getText());
				break;

			case R.string.voice_cancel:
				break;
			case R.string.voice_save:
			case R.string.voice_start:
				break;

			case R.string.voice_name:
				break;
			case R.string.voice_description:
				break;
			case R.string.voice_radious:
				break;
			case R.string.voice_metres:
				break;
			case R.string.voice_kilometers:
				break;

			default:
				break;
		}
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
	public void onLugar(boolean isVoiceCommand)
	{
		Intent intent = new Intent(this, ActLugar.class);
		intent.putExtra(Voice.NAME, isVoiceCommand);
		startActivityForResult(intent, Constantes.LUGARES);
	}
	public void onAviso(boolean isVoiceCommand)
	{
		Intent intent = new Intent(this, ActAviso.class);
		intent.putExtra(Voice.NAME, isVoiceCommand);
		startActivityForResult(intent, Constantes.AVISOS);
	}
	public void onRuta(boolean isVoiceCommand)
	{
		Intent intent = new Intent(this, ActRuta.class);
		intent.putExtra(Voice.NAME, isVoiceCommand);
		startActivityForResult(intent, Constantes.RUTAS);
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

		SectionsPagerAdapter(@NonNull FragmentManager fm, int behavior) {
			super(fm, behavior);
		}

		@Override
		@NonNull
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
}