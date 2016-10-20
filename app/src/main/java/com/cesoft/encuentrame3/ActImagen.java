package com.cesoft.encuentrame3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;

import javax.microedition.khronos.opengles.GL10;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActImagen extends AppCompatActivity
{
	private static final String TAG = "CESoft:ActImagen:";
	public static final String PARAM_LUGAR = "lugar";
	public static final String PARAM_IMG_PATH = "img_path";

	private Lugar _l;
	private ImageView _iv;
	private String _imgURLnew;

	//private boolean _bSucio = false;//TODO: ?
	private enum ESTADO {SIN_IMG, OLD_IMG, NEW_IMG};
	private ESTADO _estado;


	// * Whether or not the system UI should be auto-hidden after
	// * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	private static final boolean AUTO_HIDE = true;

	// * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the system UI.
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	// * Some older devices needs a small delay between UI widget updates and a change of the status and navigation bar.
	private static final int UI_ANIMATION_DELAY = 300;
	private final Handler mHideHandler = new Handler();
	private View mContentView;
	private final Runnable mHidePart2Runnable = new Runnable()
	{
		@SuppressLint("InlinedApi")
		@Override
		public void run()
		{
			// Delayed removal of status and navigation bar
			// Note that some of these constants are new as of API 16 (Jelly Bean) and API 19 (KitKat).
			// It is safe to use them, as they are inlined at compile-time and do nothing on earlier devices.
			mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};
	private View mControlsView;
	private final Runnable mShowPart2Runnable = new Runnable()
	{
		@Override
		public void run()
		{
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null)
			{
				actionBar.show();
			}
			mControlsView.setVisibility(View.VISIBLE);
		}
	};
	private boolean mVisible;
	private final Runnable mHideRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			hide();
		}
	};
	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system UI.
	 * This is to prevent the jarring behavior of controls going away while interacting with activity UI.
	 */
	private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent)
		{
			if(AUTO_HIDE)
			{
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_imagen);
		setTitle(getString(R.string.imagen));

		mVisible = true;
		mControlsView = findViewById(R.id.fullscreen_content_controls);
		mContentView = findViewById(R.id.imagen);
		_iv = (ImageView)mContentView;

		// Set up the user interaction to manually show or hide the system UI.
		mContentView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				toggle();
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide() operations
		// to prevent the jarring behavior of controls going away while interacting with the UI.
		findViewById(R.id.tomar_foto).setOnTouchListener(mDelayHideTouchListener);
		Button b = (Button)findViewById(R.id.tomar_foto);
		b.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				dispatchTakePictureIntent();
			}
		});
		//----
		int[] maxTextureSize = new int[1];
		GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);

Log.e(TAG, "zzzzzz-------- max: "+maxTextureSize[0]);
Log.e(TAG, "zzzzzz-------- max: "+Util.getMaxTextureSize());
		//------------------------------------------------------------------------------------------
		try
		{
			_imgURLnew = getIntent().getStringExtra(PARAM_IMG_PATH);
			_l = getIntent().getParcelableExtra(PARAM_LUGAR);
Log.e(TAG, "00000-------- : "+_imgURLnew+" "+_l.getId());
			if(_imgURLnew != null)
			{
				File file = new File(_imgURLnew);
Log.e(TAG, "oncreate-------- INI: "+_imgURLnew+" :: "+file.exists());
				if(file.exists())
				{
					try{
					//Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
					//_iv.setImageBitmap(myBitmap);
					_iv.setImageURI(Uri.fromFile(new File(_imgURLnew)));
					refreshMenu(ESTADO.NEW_IMG);
					}
					catch(OutOfMemoryError e)
					{
						//TODO: por que las imagenes no se ven en todos los dispositivos, HACER COMPATIBLES!!!!!!!!
						//TODO: guardar imagen reducida a 1Mb
						Log.e(TAG, "onCreate:BitmapFactory.decodeFile:e"+e,e);
					}
				}
				else
					_imgURLnew = null;
			}
			if(_imgURLnew == null)
			{
				if(_l != null && _l.getId() != null)
				{
					_l.downloadImg(_iv, this, new Objeto.ObjetoListener<String>()
					{
						@Override
						public void onData(String[] aData)
						{
							Log.e(TAG, "onCreate:tiene imagen y se establecio-------------- OK: ");
							refreshMenu(ESTADO.OLD_IMG);
						}
						@Override
						public void onError(String err)
						{
							Log.e(TAG, "onCreate: no tiene imagen o error--------------: "+err);
							refreshMenu(ESTADO.SIN_IMG);
							dispatchTakePictureIntent();
						}
					});
				}
				else if(_l == null || _l.getId() == null)
				{
					Log.e(TAG, "onCreate: no existe url ni Lugar --------------: ");
					refreshMenu(ESTADO.SIN_IMG);
					dispatchTakePictureIntent();
				}
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, String.format("onCreate:e:%s",e),e);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been created, to briefly hint to the user that UI controls are available.
		delayedHide(100);
	}

	private void toggle()
	{
		if(mVisible)	hide();
		else			show();
	}

	private void hide()
	{
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)actionBar.hide();
		mControlsView.setVisibility(View.GONE);
		mVisible = false;
		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	@SuppressLint("InlinedApi")
	private void show()
	{
		// Show the system bar
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;
		// Schedule a runnable to display UI elements after a delay
		mHideHandler.removeCallbacks(mHidePart2Runnable);
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	// Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
	private void delayedHide(int delayMillis)
	{
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}



	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	private Menu _menu = null;
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_img, menu);
		_menu = menu;
		refreshMenu(_estado);
		return true;
	}
	private void refreshMenu(ESTADO v)
	{
		_estado = v;
		if(_menu == null)return;
		if(_estado == ESTADO.SIN_IMG)
		{
			_menu.findItem(R.id.menu_eliminar).setVisible(false);
			_menu.findItem(R.id.menu_guardar).setVisible(false);
			show();
		}
		else if(_estado == ESTADO.NEW_IMG)
		{
			_menu.findItem(R.id.menu_eliminar).setVisible(true);
			_menu.findItem(R.id.menu_guardar).setVisible(true);
		}
		else if(_estado == ESTADO.OLD_IMG)
		{
			_menu.findItem(R.id.menu_eliminar).setVisible(true);
			_menu.findItem(R.id.menu_guardar).setVisible(false);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			eliminar();
		return super.onOptionsItemSelected(item);
	}
	/// MENU

	//______________________________________________________________________________________________
	private final static String _imgPath = getImgPath();
	private static String getImgPath()
	{
		/*String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
		//managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
		if(cursor == null)return null;
		cursor.moveToLast();
		int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		String s = cursor.getString(column_index_data);
		cursor.close();*/
		//return s;

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CESoft");
		if( ! mediaStorageDir.exists())
		{
			if( ! mediaStorageDir.mkdirs())
				return (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Encuentrame.jpg")).getPath();
		}
		return (new File(mediaStorageDir, "CESoftEncuentrame.jpg")).getPath();
		//return Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "CESoftEncuentrame.jpg")).getPath();
		//return Uri.fromFile(new File(getFilesDir(), "CESoftEncuentrame.jpg")).getPath(); DOESNT WORK
	}

	//______________________________________________________________________________________________
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private void dispatchTakePictureIntent()
	{
		if( ! getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			Toast.makeText(this, getString(R.string.sin_camara), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
Log.e(TAG, "dispatchTakePictureIntent222---------------------");
		i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(_imgPath)));
		if(i.resolveActivity(getPackageManager()) != null)
			startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
		{
			refreshMenu(ESTADO.NEW_IMG);
			show();

			//int max = Util.getMaxTextureSize();Log.e(TAG, "AAA-----------------------------------------"+max);
			_imgURLnew = _imgPath;
			try
			{
				_iv.setImageURI(Uri.fromFile(new File(_imgURLnew)));
			}
			catch(Exception e)
			{
				Log.e(TAG, String.format("onActivityResult:e:%s",e), e);
			}
		}
		else
		{
			finish();
			Log.e(TAG, "onActivityResult:---------else finish");
		}
	}


	//______________________________________________________________________________________________
	public static final int IMAGE_CAPTURE = 6969;
	private void guardar()
	{
		Intent i = new Intent();
		i.putExtra(PARAM_IMG_PATH, _imgURLnew);
		setResult(RESULT_OK, i);
		finish();
	}
	private void eliminar()
	{
		switch(_estado)
		{
		case SIN_IMG:
			break;
		case NEW_IMG:
			_imgURLnew = null;
			//refreshMenu(ESTADO.SIN_IMG);
			guardar();
			break;
		case OLD_IMG:
			_l.delImg();
			//refreshMenu(ESTADO.SIN_IMG);
			finish();
			break;
		}
	}
}


/*public String getImgTaken()
	{
		String[] projection = new String[]{
			MediaStore.Images.ImageColumns._ID,
			MediaStore.Images.ImageColumns.DATA,
			MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
			MediaStore.Images.ImageColumns.DATE_TAKEN,
			MediaStore.Images.ImageColumns.MIME_TYPE
		};
		final Cursor cursor = getContentResolver()
			.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

		String s = null;
		if(cursor == null)return null;
		if(cursor.moveToFirst()) s = cursor.getString(1);
		cursor.close();
		return s;
	}
*/
	/*public static final String CAMERA_IMAGE_BUCKET_NAME = android.os.Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
	public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
	//Matches code in MediaProvider.computeBucketValues. Should be a common function.
	public static String getBucketId(String path){return String.valueOf(path.toLowerCase().hashCode());}
	public String getLastCameraImage()
	{
		final String[] projection = { MediaStore.Images.Media.DATA };
		final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
		final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
		final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
		//ArrayList<String> result = new ArrayList<String>(cursor.getCount());
		if(cursor == null)return null;
		String s=null;
		if(cursor.moveToFirst())
		{
			final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			do
			{
				s = cursor.getString(dataColumn);
			} while (cursor.moveToNext());
		}
		//cursor.moveToLast();
		cursor.close();
		return s;
	}*/
	/*public Uri getImageUri(Context inContext, Bitmap inImage)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		return Uri.parse(path);
	}
	public String getRealPathFromURI(Uri uri)
	{
		String s = null;
		Cursor cursor = null;
		try
		{
			cursor = getContentResolver().query(uri, null, null, null, null);
			if(cursor == null)return null;
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			s = cursor.getString(idx);
		}
		catch(Exception e){Log.e(TAG, "getRealPathFromURI:e:", e);}
		finally{if(cursor != null)cursor.close();}
		return s;
	}*/