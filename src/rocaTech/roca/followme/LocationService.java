package rocaTech.roca.followme;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;






import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
//import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;



public class LocationService extends Service implements LocationListener
{
	private LocationManager mgr;
	private String best;
	
	private String[] servidores;
	private String message;
	
	int tiempoEnvioMensajePanico;
	int tiempoEnvioMensajeNoPanico;
	boolean IsenviarMensajeNoPanico;
	int distanciaActualizacionPosicion;
	
	
	private static boolean IsBtnPanicoPulsado;
	private static boolean IsPrefUsuarioCambiadas;
	
	//Obtenemos la hora actual
	Calendar calendario;
	
	// BroadCastReceiver para obtener estados de la GUI
	private BroadcastReceiver mReceiver;
	
	
	private static final String TAG = "Myservice";
	//MediaPlayer player;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
		//Obtenemos la hora actual
		calendario = new GregorianCalendar();
		
		//Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate LocationService");
		
		tiempoEnvioMensajePanico = 1000; //en miisegundos
		tiempoEnvioMensajeNoPanico = 5000; //en milisegundos
		distanciaActualizacionPosicion = 300; //en metros
		
		servidores = new String[3];
	  	servidores[0] = new String("5556");
	  	
		get_PreferenciasUsuario();
		subscribeToLocationUpdates();
		
		//CODIGO DEL BORADCASTRECEIVER PARA OBTENER LOS CAMBIOS Y ESTADOS DE LA GUI
		 IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
		 
	        mReceiver = new BroadcastReceiver() {
	 
	            @Override
	            public void onReceive(Context context, Intent intent) {
	            	
	            	IsBtnPanicoPulsado = intent.getBooleanExtra("panico_pulsado", false);
	            	
	            	Log.d(TAG, "llego actualizacion de la GUI");
	            	if(IsBtnPanicoPulsado)
	            	{
	            		Log.d(TAG, "ejecutando envioMensajesPanicoActivado");
	            		envioMensajesPanicoActivado();
	            	}
	            	else
	            	{
	            		Log.d(TAG, "ejecutando PanicoDesactivado");
	            		PanicoDesactivado();
	            	}
	            	
	            	IsPrefUsuarioCambiadas = intent.getBooleanExtra("prefUsuario_cambio", false);
	            	if(IsPrefUsuarioCambiadas)
	            	{
	            		get_PreferenciasUsuario();
	            		Log.d(TAG, "se recuperaron las preferencias de usuario por cambio desde GUI");
	            	}
	               
	            }
	        };
	        //registering our receiver
	        this.registerReceiver(mReceiver, intentFilter);
		
		
		
		
//		player = MediaPlayer.create(this, R.raw.braincandy);
//		player.setLooping(true); // Set looping
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		//player.stop();
		mgr.removeUpdates(this);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		//Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
			
		//se leen las preferencias del usuario
		FollowMeActivity.log("Se inicio Servicio de Localizacion");
		get_PreferenciasUsuario();
		//player.start();
		
		Log.d(TAG, "onStart");
	}

	
	public void subscribeToLocationUpdates()
	{
       
		Log.d(TAG, "suscribeToLocationUpdates");
		if(IsenviarMensajeNoPanico && !IsBtnPanicoPulsado)
		{
			Log.d(TAG, "listo para envioMensajesPoscionNOPanico");
			envioMensajesPoscionNOPanico();
			Log.d(TAG, "se ejecuto envioMensajesPoscionNOPanico");
		}
		else
		{
			if(IsBtnPanicoPulsado)
			{
				envioMensajesPanicoActivado();
			}
			else
			{
				PanicoDesactivado();
			}
			
		}
		

    }

	
	
	
	//******************************************************************
	//		FUNCIONES DE ADMINISTRACION DE POSICION GEOGRAFICA
	//
	//******************************************************************
	
	
	/*
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	public void onLocationChanged(Location location)
	{
		dumpLocation(location);
	}
	
	public void onProviderDisabled(String provider)
	{
		Log.d(TAG, "Provider disabled: " + provider);
	}
	
	public void onProviderEnabled(String provider) 
	{
		Log.d(TAG, "Provider enabled: " + provider);
	}
	
	public void onStatusChanged(String provider, int status,Bundle extras)
	{
		Log.d(TAG, "Provider status changed: " + provider);
		
	}
	
	/** Describe the given location, which might be null */
	private void dumpLocation(Location location)
	{
		if (location == null)
		{
			Log.d(TAG, "en dumplocation: Location[unknown]" );
		}
		else
		{
	
			
			/** send Message Geographics Position from Client**/
			//phone = "5554"; //229242";
			Log.d(TAG, "en dumplocation");
			message = "\nROCA: " + location.toString();// esta a punto de terminar programa de localizacion de personas. Esto es una prueba";
			message = createPosGeoMSN(location).toString();
			
					
			Log.d(TAG, "Listo mensaje para ser enviado: sendSMSMonitor con: " + servidores[0] + "->" + message.toString());
			sendSMSMonitor(servidores[0], message);
			FollowMeActivity.log("Envio Mensaje Posicion:" + servidores[0]);
		
			
			
			//TODO: hay que revisar la forma de como enviar a mas de un servidor
//			for(int i= 0; i<servidores.length; i++)
//			{
//				if(servidores[i] != "-1")
//				{
//					Log.d(TAG, "Envio de mensaje por Servidor: " + servidores[i]);
//					sendSMSMonitor(servidores[i], message);
//				}
//				else
//				{
//					//TODO: aqui se debe enviar un mensaje al usuario donde le indique que no hay servidores inscritos
//					Toast.makeText(getBaseContext(), "No hay servidor inscrito", Toast.LENGTH_SHORT).show();
//				}
//				
//			}
			
		}
	}
	
	
	//******************************************************************
		//		FUNCIONES DE ADMINISTRACION DE ENVIO MENSAJES POSICION
		//
		//******************************************************************

		
		/** Create a Position Geographic Message from Client**/
		private StringBuilder createPosGeoMSN(Location location)
		{
			
			String hora = calendario.getTime().toLocaleString();
			String time = Long.toString(location.getTime());
			String tipoMensaje =  "";
						
			if(IsBtnPanicoPulsado)
			{
				tipoMensaje = "SOS";
			}
			else
			{
				tipoMensaje = "OK";
			}
			Log.d(TAG, "en createPosGeoMSN");
			Log.d(TAG, "tiempoMensajes: " + hora + " -----> " + time);
			
			String latitude = Double.toString(location.getLatitude());
			String longitude = Double.toString(location.getLongitude());
			String provider = location.getProvider();
			
			Log.d(TAG, "en location.getProvider()");
			
			Time now = new Time();
	    	now.setToNow();
	    	
	    	Log.d(TAG, "en Time: now.setToNow()");
	    	
			StringBuilder builder = new StringBuilder();
			builder.append("$+id")
			.append(",")
			.append(tipoMensaje)
			.append(",")
			.append(now.format("%H%M%S"))
			.append(",")
			.append(now.format("%d%m%Y"))
			.append(",")
			.append(latitude)
			.append(",")
			.append(longitude)
			.append(",")
			.append(provider);
			//builder.append("LocationProvider[" )
			
			Log.d(TAG, "en createPosGeoMSN: creada: " + latitude + " - " + longitude);
			
			return builder;
		}
		


	    //---sends an SMS message to another device---
	    private void sendSMSMonitor(String phoneNumber, String message)
	    {  
	    	Log.d(TAG, "en sendSMSMonitor");
	    	
	        String SENT = "SMS_SENT";
	        String DELIVERED = "SMS_DELIVERED";
	 
	        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
	 
	        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
	 
	        //---when the SMS has been sent---
	        registerReceiver(new BroadcastReceiver()
	        {
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
	                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NULL_PDU:
	                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
	                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
	                        break;
	                }
	            }
	        }, new IntentFilter(SENT));
	 
	        //---when the SMS has been delivered---
	        registerReceiver(new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
	                        break;
	                    case Activity.RESULT_CANCELED:
	                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
	                        break;                        
	                }
	            }
	        }, new IntentFilter(DELIVERED));        
	 
	        SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI); 
	        Log.d(TAG, "en sendSMSMonitor: se envio mensaje");
	    }
	    
	    
	  
	    
	    //******************************************************************
	    //		FUNCIONES DE ADMINISTRACION DE CONTROLES EN GUI
	    //
	    //******************************************************************

	    
	    private void envioMensajesPanicoActivado()
	    {
	    	if(IsenviarMensajeNoPanico)
	    	{
	    		mgr.removeUpdates(this);
	    		Log.d(TAG, "Se remueven los updates de No Panico");
	    	}
	    	mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

	    	Log.d(TAG, "Inscribiendo servicio de localizacion Panico Activado");
	    	Log.d(TAG, "Location providers: proveedor" );
	    	dumpProviders();

	    	Criteria criteria = new Criteria();
	    	best = mgr.getBestProvider(criteria, true);
	    	//Log("\nBest provider is: " + best);

	    	mgr.requestLocationUpdates(best, tiempoEnvioMensajePanico, distanciaActualizacionPosicion, this);
	    	Log.d(TAG, "Request update en PanicoDesactivado: " + best + ":" +
					Integer.toString(tiempoEnvioMensajePanico) + ":" +
					Integer.toString(distanciaActualizacionPosicion));
	    	
	    	Log.d(TAG, "\nLocations (starting with last known):" );
	    	Location location = mgr.getLastKnownLocation(best);
	    	
	    	FollowMeActivity.log("Envio Mensaje con Panico Activado");
	    	dumpLocation(location);

	    }
	    
	    private void envioMensajesPoscionNOPanico()
	    {
	    	mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

	    	Log.d(TAG, "Location providers:" );
	    	dumpProviders();

	    	Criteria criteria = new Criteria();
	    	best = mgr.getBestProvider(criteria, true);
	    	Log.d(TAG, "\nBest provider is: " + best);

	    	mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);
	    	Log.d(TAG, "Request update en PanicoDesactivado: " + best + ":" +
					Integer.toString(tiempoEnvioMensajeNoPanico) + ":" +
					Integer.toString(distanciaActualizacionPosicion));
	    	
	    	Location location = mgr.getLastKnownLocation(best);
	    	Log.d(TAG, "\nLocations (starting with last known):" );
	    	
	    	FollowMeActivity.log("Envio Mensaje con Panico Desactivado");
	    	
	    	dumpLocation(location);
	    }
	    
	    
	    private void PanicoDesactivado()
	    {
	    	mgr.removeUpdates(this);
	    	
	    	if(IsenviarMensajeNoPanico)
	    	{
	    		Log.d(TAG, "Se inicia el envio de mensajes NO Panico");
	    		mgr = (LocationManager) getSystemService(LOCATION_SERVICE);


	        	//log("Location providers:" );
	        	dumpProviders();

	        	Criteria criteria = new Criteria();
	        	best = mgr.getBestProvider(criteria, true);
	        	//log("\nBest provider is: " + best);

	        	mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);
	        	Log.d(TAG, "Request update en PanicoDesactivado: " + best + ":" +
	        														Integer.toString(tiempoEnvioMensajeNoPanico) + ":" +
	        														Integer.toString(distanciaActualizacionPosicion));

	        	//log("\nLocations (starting with last known):" );
	        	Location location = mgr.getLastKnownLocation(best);
	        	
	        	FollowMeActivity.log("Envio Mensaje con Panico Desactivado");
	        	
	        	dumpLocation(location);
	    	}
	    	else
	    	{
	    		FollowMeActivity.log("NO HAY ENVIO DE MENSAJES");
	    	}
	    	
	    	

	    }
	    
  
	   
	    
	    public void get_PreferenciasUsuario()
	    {
	    	Log.d(TAG, "Recuperando las preferencias de Usuario");
	    	SharedPreferences sharedPrefs =	PreferenceManager.getDefaultSharedPreferences(LocationService.this);
			
			tiempoEnvioMensajePanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensajes_panico", "10000"));
			tiempoEnvioMensajeNoPanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensaje_sin_panico", "300000"));
		  	IsenviarMensajeNoPanico = sharedPrefs.getBoolean("enviar_mensajes_sin_panico", false);
		  	distanciaActualizacionPosicion = 100;
		  	
		  	servidores = new String[3];
		  	servidores[0] = new String(sharedPrefs.getString("servidor1", "-1"));
		  	
		  	Log.d(TAG, "Preferencias Usuario: " + Integer.toString(tiempoEnvioMensajeNoPanico) + "->" + 
		  							Integer.toString(tiempoEnvioMensajePanico) + "->" +
		  							Boolean.toString(IsenviarMensajeNoPanico) + "->" +
		  							servidores[0]);
	    }

	    public static boolean get_EstadoPanico()
	    {
	    	return IsBtnPanicoPulsado;
	    }
	    
	  //******************************************************************
		//		FUNCIONES DE ADMINISTRACION IMPRESION DATOS EN GUI
		//
		//******************************************************************
			
		
		// Define human readable names
		private static final String[] A = { "invalid" , "n/a" , "fine" , "coarse" };
		private static final String[] P = { "invalid" , "n/a" , "low" , "medium" ,	"high" };
		//private static final String[] S = { "out of service" ,"temporarily unavailable" , "available" };
//		/** Write a string to the output window */
//		
//		public static void log(String string)
//		{
//			output.append(string + "\n" );
//		}
		
		/** Write information from all location providers */
		private void dumpProviders() 
		{
			List<String> providers = mgr.getAllProviders();
			Log.d("TAG", "Obteniendo Proveedores localizacion");
			for (String provider : providers) 
			{
				dumpProvider(provider);
				Log.d("TAG", "Proveedor: " + provider);
			}
		}
		
		/** Write information from a single location provider */
		private void dumpProvider(String provider)
		{
			LocationProvider info = mgr.getProvider(provider);
			Log.d("TAG", "Obteniendo datos del proveedor desde la variable: mgr");
			
			StringBuilder builder = new StringBuilder();
			builder.append("LocationProvider[" )
			.append("name=" )
			.append(info.getName())
			.append(",enabled=" )
			.append(mgr.isProviderEnabled(provider))
			.append(",getAccuracy=" )
			.append(A[info.getAccuracy() + 1])
			.append(",getPowerRequirement=" )
			.append(P[info.getPowerRequirement() + 1])
			.append(",hasMonetaryCost=" )
			.append(info.hasMonetaryCost())
			.append(",requiresCell=" )
			.append(info.requiresCell())
			.append(",requiresNetwork=" )
			.append(info.requiresNetwork())
			.append(",requiresSatellite=" )
			.append(info.requiresSatellite())
			.append(",supportsAltitude=" )
			.append(info.supportsAltitude())
			.append(",supportsBearing=" )
			.append(info.supportsBearing())
			.append(",supportsSpeed=" )
			.append(info.supportsSpeed())
			.append("]" );
			//log(builder.toString());
			Log.d("TAG", "Se genero un Builder con datos del proveedor");
		}
}// Fin clase LocationService
