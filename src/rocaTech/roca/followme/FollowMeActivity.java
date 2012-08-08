package rocaTech.roca.followme;



import rocaTech.roca.followme.R.id;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class FollowMeActivity extends Activity
{
	//variables de apoyo
	 private static final String TAG = "ServicesDemo";
	
	
	// variables de la GUI
	private static TextView output;
	private static Button btnPanico;
	private static Button btnPararApli;
	private boolean IsBtnPanicoPulsado;
	
	
	// variables de Menu
	private String[] servidores;
	private int tiempoEnvioMensajePanico;
	private int tiempoEnvioMensajeNoPanico;
	private boolean IsenviarMensajeNoPanico;
	private int distanciaActualizacionPosicion;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        output = (TextView) findViewById(R.id.output);
    	output.setMovementMethod(new ScrollingMovementMethod());
    	
    	// Control del Boton (Toggle) de Panico
    	btnPanico = (ToggleButton) findViewById(R.id.BtnPanico);
    	    	
    	btnPanico.setOnClickListener(new View.OnClickListener()
    	{

    		public void onClick(View v)
    		{
    			//Hacer rutina de gestion de posicion geografica por boton panico activado
    			if (((ToggleButton) btnPanico).isChecked()) 
    			{
    				IsBtnPanicoPulsado = true;
    				//btnPanicoActivado();  //Funcion que realiza la accion al activar el boton panico
    				 Log.d(TAG, "onClick: starting srvice");
    			     iniciarServicioLocalizacion();
    			     btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_stop));

    			} else
    			{
    				IsBtnPanicoPulsado = false;
    				//btnPanicoDesactivado(); //Funcion que realiza la accion al desactivar el boton panico
    				Log.d(TAG, "onClick: stopping srvice");
    			    stopServicioLocalizacion();
    				btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_go));


    			}


    		}
    	});
    	
    	btnPararApli = (Button) findViewById(id.BtnKillApli);
    	
    	btnPararApli.setOnClickListener(new View.OnClickListener() 
    	{
			
			public void onClick(View v) {
				
				// TODO Auto-generated method stub
				
				onStop();
				
			}
		});
    	
    }
    
    @Override
	protected void onResume()
	{
		super.onResume();
		
//		//se leen las preferencias del usuario
//		SharedPreferences sharedPrefs =	PreferenceManager.getDefaultSharedPreferences(FollowMeActivity.this);
//		
//		tiempoEnvioMensajePanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensajes_panico", "10000"));
//		tiempoEnvioMensajeNoPanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensaje_sin_panico", "300000"));
//	  	IsenviarMensajeNoPanico = sharedPrefs.getBoolean("enviar_mensajes_sin_panico", false);
//	  	distanciaActualizacionPosicion = 100;
//	  	
//	  	servidores = new String[3];
//	  	servidores[0] = new String(sharedPrefs.getString("servidor1", "-1"));
//	  	
//		
//		// Start updates (doc recommends delay >= 60000 ms)
//		if(IsenviarMensajeNoPanico)
//		{
//			mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);
//		}
		
		
		
//		//Recuperacion de las preferencias de usuarios para envio de mensajes de posicion
//		// En panico = Usuario a activado la alarma de SOS
//		// No panico = la aplicacion puede enviar mensaje de posicion periodicamente. El tiempo de envio dependera 
//		// de las preferencias de envio proporcionadas por el usuario
//		
//		SharedPreferences sharedPrefs =	PreferenceManager.getDefaultSharedPreferences(FollowMeActivity.this);
//	  	  
//    	StringBuilder builder = new StringBuilder();
//	  	 
//	  	  builder.append("\n enviar_mensajes_sin_panico (usuario): " + sharedPrefs.getBoolean("enviar_mensajes_sin_panico", false));
//	  	  builder.append("\n tiempo_envio_mensaje_sin_panico (usuario): " + sharedPrefs.getString("tiempo_envio_mensaje_sin_panico", "600000"));
//	  	  builder.append("\n tiempo_envio_mensajes_panico (usuario): " + sharedPrefs.getString("tiempo_envio_mensajes_panico", "60000"));
//	  	  builder.append("\n servidor1 (usuario): " + sharedPrefs.getString("servidor1", "-1"));
//	  	  
//	  	 Log.i("Preferencias", builder.toString() );   	
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		stopServicioLocalizacion();
		// Stop updates to save power while app paused
//		if(!IsBtnPanicoPulsado)
//		{
//			mgr.removeUpdates(this);
//		}
	}
	
	@Override
	protected void onStop()
	{
		log("se para aplicacion");
		super.onStop();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	// Define human readable names
		private static final String[] A = { "invalid" , "n/a" , "fine" , "coarse" };
		private static final String[] P = { "invalid" , "n/a" , "low" , "medium" ,	"high" };
		private static final String[] S = { "out of service" ,"temporarily unavailable" , "available" };
		/** Write a string to the output window */
		
		public static void log(String string)
		{
			output.append(string + "\n" );
		}
		
		
    public void iniciarServicioLocalizacion()
    {
    	Log.d(TAG, "iniciarServicioLocalizacion: se va llamar a iniciar");
    	 startService(new Intent(this, LocationService.class));
    }
    
    public void stopServicioLocalizacion()
    {
    	stopService(new Intent(this, LocationService.class));
    }
    
}// Fin Activity