package rocaTech.roca.followme;

import java.util.Calendar;
import java.util.GregorianCalendar;

import java.util.List;






//import android.app.Activity;
//import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
//import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
//import android.widget.Toast;
//import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;

import rocaTech.roca.followme.Mensaje_data;

public class LocationService extends Service implements LocationListener
{
	private LocationManager mgr;
	private String best;
	
	private String aliasAplicacion;
	private String[] servidores;
	public String message;
	
	int tiempoEnvioMensajePanico;
	int tiempoEnvioMensajeNoPanico;
	boolean IsenviarMensajeNoPanico;
	int distanciaActualizacionPosicion;
	
	
	private static boolean IsBtnPanicoPulsado;
	private static boolean IsPrefUsuarioCambiadas;
	
	//Obtenemos la hora actual
	Calendar calendario;
	

	private static String cadenaLatitud;
	private static String cadenaLongitud;
	private static String cadenaTiempo;
	
	// BroadCastReceiver para obtener estados de la GUI
	private BroadcastReceiver mReceiver;
	
	
	private static final String TAG = "Myservice";
	//MediaPlayer player;
	
	boolean enConteo;
	Handler mHandler;
	int tiempoTranscurrido; //conteo de tiempo en segundos
	
	
	/****************************************************************************
		 *     VARIABLES DE CONECCION POR SOCKET
		 */
		
		Socket miCliente;
		private boolean connected = false;
		ObjectOutputStream oos;
		ObjectInputStream ois;
		Mensaje_data mdata;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate()
	{
		mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		//Obtenemos la hora actual
		calendario = new GregorianCalendar();
		
		enConteo = false;
		mHandler = new Handler();
		
		//Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate LocationService");
		
		aliasAplicacion = "alias1";
		tiempoEnvioMensajePanico = 1000; //en miisegundos
		tiempoEnvioMensajeNoPanico = 5000; //en milisegundos
		distanciaActualizacionPosicion = 300; //en metros
		
		servidores = new String[3];
	  	servidores[0] = new String("5556");
	  	message = "-1";
	  	
		get_PreferenciasUsuario();
		subscribeToLocationUpdates();
		
		//CODIGO DEL BORADCASTRECEIVER PARA OBTENER LOS CAMBIOS Y ESTADOS DE LA GUI
		 IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
		 
	        mReceiver = new BroadcastReceiver() {
	 
	            @Override
	            public void onReceive(Context context, Intent intent) {
	            	
	            	
	            	//FollowMeActivity.log("Valor Panico Pulsado antes:" + Boolean.toString(IsBtnPanicoPulsado));
	            	IsBtnPanicoPulsado = intent.getBooleanExtra("panico_pulsado", IsBtnPanicoPulsado);
	            	//FollowMeActivity.log("Valor Panico Pulsado despues:" + Boolean.toString(IsBtnPanicoPulsado));
	            	
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
		
		if(mHandler != null)
	    {
	    	   mHandler.removeCallbacks(mMuestraMensaje);
	    }
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		//Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
			
		//se leen las preferencias del usuario
		FollowMeActivity.log("Inicio Servicio Localizacion");
		get_PreferenciasUsuario();
		iniciarHiloConteoTiempo();
		//mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
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
		FollowMeActivity.log("Posicion Geografica Actualizada");
		dumpLocation(location);
	}
	
	public void onProviderDisabled(String provider)
	{
		Log.d(TAG, "Provider disabled: " + provider);
		comunicarProveedorPos("no");
	}
	
	public void onProviderEnabled(String provider) 
	{
		Log.d(TAG, "Provider enabled: " + provider);
		comunicarProveedorPos(provider);
	}
	
	public void onStatusChanged(String provider, int status,Bundle extras)
	{
		Log.d(TAG, "Provider status changed: " + provider);
		comunicarProveedorPos(provider);
		
	}
	
	/** Describe the given location, which might be null */
	private void dumpLocation(Location location)
	{
		if (location == null)
		{
			Log.d(TAG, "en dumplocation: Location[unknown]" );
			message = "-1";
		}
		else
		{
	
			
			/** send Message Geographics Position from Client**/
			//phone = "5554"; //229242";
			Log.d(TAG, "en dumplocation");
			//message = "\nROCA: " + location.toString();// esta a punto de terminar programa de localizacion de personas. Esto es una prueba";
			message = createPosGeoMSN(location).toString();
			Log.d(TAG, "Listo mensaje para ser enviado: sendSMSMonitor con: " + servidores[0] + "->" + message.toString());
					
		
//			sendSMSMonitor(servidores[0], message);
//			FollowMeActivity.log("Envio Mensaje Posicion:" + servidores[0]);
		
			
			
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
			
			//Esta funcion convierte el valor de Latitud y Longitud en String con formato
			CoordenadasDecimalToString(location.getLatitude(), location.getLongitude());
			
			Log.d(TAG, "en location.getProvider()");
			
			Time now = new Time();
	    	now.setToNow();
	    	
	    	Log.d(TAG, "en Time: now.setToNow()");
	    	
			StringBuilder builder = new StringBuilder();
			builder.append("$" + aliasAplicacion)
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
			
			comunicarPosGeo();
			
			return builder;
		}
		
		 public static void comunicarPosGeo()
		 {
		    	FollowMeActivity.set_lastPosGeo(cadenaLatitud, cadenaLongitud);
		 }
		 
		 public static void comunicarTiempoUltimoMSN(int timeLastMSN)
		 {
			 cadenaTiempo = String.format("%00d", timeLastMSN);
		    FollowMeActivity.set_timeLastMSN(cadenaTiempo);
		 }
		 
		 public static void comunicarProveedorPos(String provider)
		 {
			 
		    FollowMeActivity.set_proveedorPosGeo(provider);
		 }

	    //---sends an SMS message to another device---
	    private void sendSMSMonitor(String phoneNumber, String message)
	    {  
	    	Log.d(TAG, "en sendSMSMonitor");
	    	
//	        String SENT = "SMS_SENT";
//	        String DELIVERED = "SMS_DELIVERED";
//	 
//	        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
//	 
//	        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
	 
//	        //---when the SMS has been sent---
//	        registerReceiver(new BroadcastReceiver()
//	        {
//	            @Override
//	            public void onReceive(Context arg0, Intent arg1) {
//	                switch (getResultCode())
//	                {
//	                    case Activity.RESULT_OK:
//	                        //Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
//	                    	FollowMeActivity.log("SMS enviado");
//	                        break;
//	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//	                        //Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
//	                    	FollowMeActivity.log("Falla Generica");
//	                        break;
//	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
//	                        //Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
//	                        FollowMeActivity.log("No hay servicio");
//	                        break;
//	                    case SmsManager.RESULT_ERROR_NULL_PDU:
//	                        //Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
//	                        FollowMeActivity.log("Null PDU");
//	                        break;
//	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//	                        //Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
//	                    	FollowMeActivity.log("Radio Apagado");
//	                        break;
//	                }
//	            }
//	        }, new IntentFilter(SENT));
	 
//	        //---when the SMS has been delivered---
//	        registerReceiver(new BroadcastReceiver(){
//	            @Override
//	            public void onReceive(Context arg0, Intent arg1) {
//	                switch (getResultCode())
//	                {
//	                    case Activity.RESULT_OK:
//	                        //Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
//	                        FollowMeActivity.log("SMS Entregado");
//	                        break;
//	                    case Activity.RESULT_CANCELED:
//	                        //Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
//	                        FollowMeActivity.log("SMS No entregado");
//	                        break;                        
//	                }
//	            }
//	        }, new IntentFilter(DELIVERED));        
	 
	        SmsManager sms = SmsManager.getDefault();
	        //sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI); 
	        sms.sendTextMessage(phoneNumber, null, message, null, null); 
	        Log.d(TAG, "en sendSMSMonitor: se envio mensaje por SMS");
	        
	        //******* PRUEBAS ENVIO DE MENSAJE POR SOCKET ********
	        Log.d(TAG, "CONECTANDO SOCKET");
	       connected = Connect();
	       if(connected)
	       {
	    	   Snd_txt_Msg(message);
	    	   Log.d(TAG, "en sendSMSMonitor: se envio mensaje por Socket");
	    	   Disconnect();
	    	   connected = false;
	       }
	       else
	       {
	    	   Log.d(TAG, "dESCONECTANDO SOCKET");
	    	   FollowMeActivity.log(" ERROR: Desconect Socket");
	       }
	       //Disconnect();
	       Log.d(TAG, "dESCONECTANDO SOCKET");
	        //FollowMeActivity.log("SMS enviado en funcion de envio de mensaje");
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
	    	//mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

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
	    	
	    	//FollowMeActivity.log("Envio Mensaje con Panico Activado");
	    	dumpLocation(location);
	    	
	    	iniciarHiloConteoTiempo();
	    	

	    }
	    
	    private void envioMensajesPoscionNOPanico()
	    {
	    	//mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

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
	    	
	    	//FollowMeActivity.log("Envio Mensaje con Panico Desactivado");
	    	
	    	dumpLocation(location);
	    	iniciarHiloConteoTiempo();
	    }
	    
	    
	    private void PanicoDesactivado()
	    {
	    	mgr.removeUpdates(this);
	    	
	    	if(IsenviarMensajeNoPanico)
	    	{
	    		Log.d(TAG, "Se inicia el envio de mensajes NO Panico");
	    		//mgr = (LocationManager) getSystemService(LOCATION_SERVICE);


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
	        	
	        	//FollowMeActivity.log("Envio Mensaje con Panico Desactivado");
	        	
	        	dumpLocation(location);
	        	iniciarHiloConteoTiempo();
	    	}
	    	else
	    	{
	    		FollowMeActivity.log("NO CONFIGURADO ENVIO DE MENSAJES");
	    		
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
		  	
		  	aliasAplicacion = sharedPrefs.getString("alias_apliacion", "alias1");
		  	
		  	Log.d(TAG, "Preferencias Usuario: " + Integer.toString(tiempoEnvioMensajeNoPanico) + "->" + 
		  							Integer.toString(tiempoEnvioMensajePanico) + "->" +
		  							Boolean.toString(IsenviarMensajeNoPanico) + "->" +
		  							servidores[0]+ "->" +
		  							aliasAplicacion);
		  	
		  	//FollowMeActivity.log("Alias Apliacion: " + aliasAplicacion);
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
		
		
		// HILO PARA EL CONTEO DEL TIEMPO MONOTONICO
		
		private void iniciarHiloConteoTiempo()
		{
			if(mHandler != null)
	    	{
	    	      Log.i("FolowMe", "timer canceled");
	    	      mHandler.removeCallbacks(mMuestraMensaje);
	    	      Log.i("FolowMe", "se borro la pila del handler");
	    	      
	    	      tiempoTranscurrido = 0;
	    	          	      
	    	      mHandler.postDelayed(mMuestraMensaje, 1000);
	    	      Log.i("FollowMe", "dentro del hilo task");
	    	      
	    	}
		}
		
		private Runnable mMuestraMensaje = new Runnable()
	    {
	    	
	        public void run()
	        {
	        	tiempoTranscurrido = tiempoTranscurrido + 1000;
	        	
	        	
	        	if(IsBtnPanicoPulsado)
	        	{
//	        		FollowMeActivity.log("Tiempo en Panico: " + Integer.toString(tiempoEnvioMensajePanico)
//	        												  + " -> "
//	        												  + Integer.toString(tiempoTranscurrido));
	        		if(tiempoTranscurrido == tiempoEnvioMensajePanico)
	        		{
	        			// enviar el mensaje de posicion por panico
	        			//TODO: SE DEBE IMPLEMENTAR EL CODIGO PARA EVIAR LOS MENSAJES A TODOS LOS SERVIDORES
	        			if(message != "-1")
	        			{
	        				sendSMSMonitor(servidores[0], message);
	        			//FollowMeActivity.log("Envio Mensaje Posicion PANICO:" + servidores[0]);
	        			}
	        			else
	        			{
	        				FollowMeActivity.log("NO SE ENVIO MENSAJE");
	        			}
	        			//FollowMeActivity.log("Aqui Enviar SMS Panico: " + Integer.toString(tiempoTranscurrido));
	        			tiempoTranscurrido = 0;
	        		}
	        	}
	        	else
	        	{
//        			FollowMeActivity.log("Tiempo en No Panico: " + Integer.toString(tiempoEnvioMensajeNoPanico)
//																 + " -> "
//									        					 + Integer.toString(tiempoTranscurrido));
        			if(tiempoTranscurrido == tiempoEnvioMensajeNoPanico)
        			{
        				// enviar el mensaje de posicion por panico
	        			//TODO: SE DEBE IMPLEMENTAR EL CODIGO PARA EVIAR LOS MENSAJES A TODOS LOS SERVIDORES
        				if(message != "-1")
	        			{
        					sendSMSMonitor(servidores[0], message);
        					FollowMeActivity.log("Envio Mensaje Posicion NO PANICO:" + servidores[0]);
	        			}
        				else
        				{
        					FollowMeActivity.log("NO SE ENVIO MENSAJE");
        				}
        				
        				tiempoTranscurrido = 0;
        			}
	        	}
				
				FollowMeActivity.set_timeLastMSN(Integer.toString(tiempoTranscurrido/1000));
	    		Log.i("FollowYou", "Se repite ejecucion del HILO");
		        mHandler.removeCallbacks(mMuestraMensaje);
	           mHandler.postDelayed(mMuestraMensaje, 1000);
	        } // fin run
	      };// fin runnable mMuestraMensaje
	      
	      
	      
	      /**
	 		* Funcion para convertir la Latitud/Longitud de formato numerico tipo double a formato texto tipo String
	 		*
	 		* Esta función recibe las variables de coordenadas  latitud/longitud con formato numerico tipo double  (grado y decima de grado) y las convierte 
	 		* a variable texto tipo String (grado-minuto + decima de minuto-direccion)
	 		*
	 		* @param latitud		valor de la latitud en formato de grado y decimas de grado ejemplo 19.23454, que se va a transformar
	 		* @param longitud		valor de la longitud en formato de grado y decimas de grado ejemplo -96.23454, que se va a transformar
	 		* 
	 		* @param &cadenaLatitud		variable String para almacenar el valor de la Latitud convertida con formato 19° 25.43' N
	 		* @param &cadenaLongitud	variable String para almacenar el valor de la Longitud convertida con formato 19° 25.43' N
	 		*/
	 		private static void CoordenadasDecimalToString(double latitud, double longitud)
	 		{
	 			String tempEW;
	 			String tempNS;


	 			double minutos;
	 			double grados;
	 			double entero, dec;	

	 			if (longitud<0)
	 			{tempEW="W"; longitud = Math.abs(longitud);}
	 			else
	 			{tempEW="E";}
	 			if (latitud<0)
	 			{tempNS="S"; latitud = Math.abs(latitud);}
	 			else
	 			{tempNS="N";}
	 			cadenaLatitud="";
	 			cadenaLongitud="";
	 			if(latitud< 90 || latitud > -90 ||longitud<180 || longitud > -180 )
	 			{
	 				dec = latitud - (entero = Math.floor(latitud));;
	 				minutos = dec * 60;
	 				grados = entero;
	 				if(grados < 0) grados = grados * (-1);
	 				if(minutos < 0) minutos = minutos * (-1);


	 				//cadenaLatitud = System::Convert::ToString(grados) + " °  " + System::Convert::ToString(minutos) + "'  " + tempNS;
	 				//cadenaLatitud = Double.toString(grados) + " °  " + Double.toString(minutos) + "'  " + tempNS;
	 				cadenaLatitud = String.format("%2.0f", grados) + " °  " + String.format("%2.2f", minutos) + "'  " + tempNS;

	 				dec = longitud - (entero = Math.floor(longitud));
	 				minutos = dec * 60;
	 				grados = entero;
	 				if(grados < 0) grados = grados * (-1);
	 				if(minutos < 0) minutos = minutos * (-1);
	 				//cadenaLongitud = System::Convert::ToString(grados) + " °  " + System::Convert::ToString(minutos) + "'  "+ tempEW;
	 				//cadenaLongitud = Double.toString(grados) + " °  " + Double.toString(minutos) + "'  " + tempEW;
	 				cadenaLongitud = String.format("%3.0f", grados) + " °  " + String.format("%2.2f", minutos) + "'  " + tempEW;
	 			}

	 		}
	    
	      
	 		/****************************************************************************
	 		 *     FUNCIONES DE CONECCION POR SOCKET
	 		 */
	 		
	 		
	 		
	 		//Conectamos
	 		public boolean Connect() 
	 		{
	 			Log.d("TAG","CONECCION DEL SOCKECT");
	 			//Obtengo datos ingresados en campos
	 			String IP = "192.168.110.1";
	 			int PORT = 5555;

	 			try {//creamos sockets con los valores anteriores
	 				miCliente = new Socket(IP, PORT);
	 				//si nos conectamos
	 				if (miCliente.isConnected() == true) {
	 					return true;
	 				} else {
	 					return false;
	 				}
	 			} catch (Exception e) {
	 				//Si hubo algun error mostrmos error
	 				
	 				FollowMeActivity.log("ERROR CONECT SOCKET");
	 				Log.d("TAG","Error connect() = " + e);
	 				Log.e("Error connect()", "" + e);
	 				return false;
	 			}
	 		}

	 		//Metodo de desconexion
	 		public void Disconnect()
	 		{
	 			try {
	 				//Prepramos mensaje de desconexion
	 				Mensaje_data msgact = new Mensaje_data();
	 				msgact.texto = "";
	 				msgact.Action = -1;
	 				msgact.last_msg = true;
	 				//avisamos al server que cierre el canal
	 				boolean val_acc = Snd_Msg(msgact);

	 				if (!val_acc) {//hubo un error
	 					FollowMeActivity.log(" ERROR: Desconect Socket");
	 					
	 					Log.e("Disconnect() -> ", "!ERROR!");

	 				} else {//ok nos desconectamos
	 					FollowMeActivity.log("Sockect Desconectado");
	 					//camibmos led a rojo
	 					
	 					Log.e("Disconnect() -> ", "!ok!");
	 					//cerramos socket	
	 					miCliente.close();
	 				}
	 			} catch (IOException e) {
	 				// TODO Auto-generated catch block
	 				e.printStackTrace();
	 			}

	 			if (!miCliente.isConnected())
	 			{
	 				FollowMeActivity.log("Socket deconectado");
	 			}
	 				//Change_leds(false);
	 		}

	 		//Enviamos mensaje de accion segun el boton q presionamos
	 		public void Snd_Action(int bt) 
	 		{
	 			Mensaje_data msgact = new Mensaje_data();
	 			//no hay texto
	 			msgact.texto = "";
	 			//seteo en el valor action el numero de accion
	 			msgact.Action = bt;
	 			//no es el ultimo msg
	 			msgact.last_msg = false;
	 			//mando msg
	 			boolean val_acc = Snd_Msg(msgact);
	 			//error al enviar
	 			if (!val_acc) {
	 				FollowMeActivity.log(" ERROR: Desconect Socket");
	 				
	 				Log.e("Snd_Action() -> ", "!ERROR!");

	 			}

	 			if (!miCliente.isConnected())
	 			{
	 				FollowMeActivity.log("Socket deconectado");
	 			}
	 		}

	 		//Envio mensaje de texto 
	 		public void Snd_txt_Msg(String txt) 
	 		{

	 			Mensaje_data mensaje = new Mensaje_data();
	 			//seteo en texto el parametro  recibido por txt
	 			mensaje.texto = txt;
	 			//action -1 no es mensaje de accion
	 			mensaje.Action = -1;
	 			//no es el ultimo msg
	 			mensaje.last_msg = false;
	 			//mando msg
	 			boolean val_acc = Snd_Msg(mensaje);
	 			//error al enviar
	 			if (!val_acc) {
	 				FollowMeActivity.log(" ERROR: Desconect Socket");
	 				Log.e("Snd_txt_Msg() -> ", "!ERROR!");
	 			}
	 			if (!miCliente.isConnected())
	 			{
	 				FollowMeActivity.log("Socket deconectado");
	 			}
	 		}
	 		
	 		/*Metodo para enviar mensaje por socket
	 		 *recibe como parmetro un objeto Mensaje_data
	 		 *retorna boolean segun si se pudo establecer o no la conexion
	 		 */
	 		public boolean Snd_Msg(Mensaje_data msg) 
	 		{

	 			try {
	 				//Accedo a flujo de salida
	 				oos = new ObjectOutputStream(miCliente.getOutputStream());
	 				//creo objeto mensaje
	 				Mensaje_data mensaje = new Mensaje_data();

	 				if (miCliente.isConnected())// si la conexion continua
	 				{
	 					//lo asocio al mensaje recibido
	 					mensaje = msg;
	 					//Envio mensaje por flujo
	 					oos.writeObject(mensaje);
	 					//envio ok
	 					return true;

	 				} else {//en caso de que no halla conexion al enviar el msg
	 					FollowMeActivity.log(" ERROR: Desconect Socket");
	 					return false;
	 				}

	 			} catch (IOException e) {// hubo algun error
	 				Log.e("Snd_Msg() ERROR -> ", "" + e);

	 				return false;
	 			}
	 		}
	 	
}// Fin clase LocationService
