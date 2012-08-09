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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class LocationService extends Service implements LocationListener
{
	private LocationManager mgr;
	private static TextView output;
	//private static Button btnPanico;
	private String best;
	
	private String[] servidores;
	private String message;
	
	private int tiempoEnvioMensajePanico;
	private int tiempoEnvioMensajeNoPanico;
	private boolean IsenviarMensajeNoPanico;
	private int distanciaActualizacionPosicion;
	
	
	private boolean IsBtnPanicoPulsado;
	
	//Obtenemos la hora actual
	Calendar calendario;
	
	
	private static final String TAG = "Myservice";
	MediaPlayer player;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate() {
		
		//Obtenemos la hora actual
		calendario = new GregorianCalendar();
		
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		
		tiempoEnvioMensajePanico = 1000; //en miisegundos
		tiempoEnvioMensajeNoPanico = 5000; //en milisegundos
		distanciaActualizacionPosicion = 300; //en metros
		
		servidores = new String[3];
	  	servidores[0] = new String("5556");
		
		subscribeToLocationUpdates();
		
		player = MediaPlayer.create(this, R.raw.braincandy);
		player.setLooping(false); // Set looping
	}

	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		player.stop();
		mgr.removeUpdates(this);
	}
	
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		
		// Start updates (doc recommends delay >= 60000 ms)
		
		//se leen las preferencias del usuario
							  	
			  	servidores = new String[3];
			  	servidores[0] = new String("5556");
			  	
			//mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);
			  	
			Log.d(TAG, "onStart");
		player.start();
	}

	
	public void subscribeToLocationUpdates()
	{
       // this.mgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		Log.d(TAG, "Inscribiendo servicio de localizacion");

    	//log("Location providers:" );
    	dumpProviders();

    	Criteria criteria = new Criteria();
    	best = mgr.getBestProvider(criteria, true);
    	//log("\nBest provider is: " + best);
    	Log.d(TAG, "\nBest provider is: " + best);

    	mgr.requestLocationUpdates(best, tiempoEnvioMensajePanico, distanciaActualizacionPosicion, this);
    	Log.d(TAG, "se ejecuto: requestLocationUpdates");

    	Log.d(TAG, "\nLocations (starting with last known):" );
    	
    	Location location = mgr.getLastKnownLocation(best);
    	dumpLocation(location);
        
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
			//log("\n" + location.toString());
			
			/** send Message Geographics Position from Client**/
			//phone = "5554"; //2292423424";
			Log.d(TAG, "en dumplocation");
			message = "\nROCA: " + location.toString();// esta a punto de terminar programa de localizacion de personas. Esto es una prueba";
			message = createPosGeoMSN(location).toString();
			Log.d(TAG, "Listo mensaje para ser enviado: sendSMSMonitor con: " + servidores[0] + "->" + message.toString());
			sendSMSMonitor(servidores[0], message);
			
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
			tipoMensaje = "SOS";
			
//			if(IsBtnPanicoPulsado)
//			{
//				tipoMensaje = "SOS";
//			}
//			else
//			{
//				tipoMensaje = "OK";
//			}
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
		
//		//---sends an SMS message to another device---
//	    private void sendSMS(String phoneNumber, String msg)
//	    {        
//	    	// make sure the fields are not empty
//	        if (phoneNumber.length()>0 && msg.length()>0)
//	        {
//	        	// call the sms manager
//	            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, FollowMeActivity.class), 0);
//	                SmsManager sms = SmsManager.getDefault();
//	                // this is the function that does all the magic
//	                sms.sendTextMessage(phoneNumber, null, msg, pi, null);
//	                
//	               // log("\nMensaje Enviado a[ " + servidores[0] + " ]" );
//	        }
//	        else
//	        {
//	        	// display message if text fields are empty
//	            Toast.makeText(getBaseContext(),"All field are required",Toast.LENGTH_SHORT).show();
//	        	//log("\nPor alguna razon tu mensaje no se envio");
//	        }       
//	    }    

	    //---sends an SMS message to another device---
	    private void sendSMSMonitor(String phoneNumber, String message)
	    {  
	    	Log.d(TAG, "en sendSMSMonitor");
	    	
	        String SENT = "SMS_SENT";
	        String DELIVERED = "SMS_DELIVERED";
	 
	        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
	            new Intent(SENT), 0);
	 
	        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
	            new Intent(DELIVERED), 0);
	 
	        //---when the SMS has been sent---
	        registerReceiver(new BroadcastReceiver()
	        {
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Toast.makeText(getBaseContext(), "SMS sent", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                        Toast.makeText(getBaseContext(), "Generic failure", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
	                        Toast.makeText(getBaseContext(), "No service", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NULL_PDU:
	                        Toast.makeText(getBaseContext(), "Null PDU", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
	                        Toast.makeText(getBaseContext(), "Radio off", 
	                                Toast.LENGTH_SHORT).show();
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
	                        Toast.makeText(getBaseContext(), "SMS delivered", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case Activity.RESULT_CANCELED:
	                        Toast.makeText(getBaseContext(), "SMS not delivered", 
	                                Toast.LENGTH_SHORT).show();
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

	    
	    private void btnPanicoActivado()
	    {
	    	if(IsenviarMensajeNoPanico)
	    	{
	    		mgr.removeUpdates(this);
	    	}
	    	mgr = (LocationManager) getSystemService(LOCATION_SERVICE);


	    	Log.d("Location providers:", "proveedor" );
	    	dumpProviders();

	    	Criteria criteria = new Criteria();
	    	best = mgr.getBestProvider(criteria, true);
	    	//Log("\nBest provider is: " + best);

	    	mgr.requestLocationUpdates(best, tiempoEnvioMensajePanico, distanciaActualizacionPosicion, this);

	    	log("\nLocations (starting with last known):" );
	    	Location location = mgr.getLastKnownLocation(best);
	    	dumpLocation(location);

	    }
	    
	    private void envioMensajesPoscionNOPanico()
	    {
	    	mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

	    	log("Location providers:" );
	    	dumpProviders();

	    	Criteria criteria = new Criteria();
	    	best = mgr.getBestProvider(criteria, true);
	    	log("\nBest provider is: " + best);

	    	mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);

	    	log("\nLocations (starting with last known):" );
	    	Location location = mgr.getLastKnownLocation(best);
	    	dumpLocation(location);
	    }
	    
	    
	    private void btnPanicoDesactivado()
	    {
	    	mgr.removeUpdates(this);
	    	
	    	if(IsenviarMensajeNoPanico)
	    	{
	    		mgr = (LocationManager) getSystemService(LOCATION_SERVICE);


	        	log("Location providers:" );
	        	dumpProviders();

	        	Criteria criteria = new Criteria();
	        	best = mgr.getBestProvider(criteria, true);
	        	log("\nBest provider is: " + best);

	        	mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);

	        	log("\nLocations (starting with last known):" );
	        	Location location = mgr.getLastKnownLocation(best);
	        	dumpLocation(location);
	    	}
	    	
	    	

	    }
	    
	  //******************************************************************
		//		FUNCIONES DE ADMINISTRACION IMPRESION DATOS EN GUI
		//
		//******************************************************************
			
		
		// Define human readable names
		private static final String[] A = { "invalid" , "n/a" , "fine" , "coarse" };
		private static final String[] P = { "invalid" , "n/a" , "low" , "medium" ,	"high" };
		private static final String[] S = { "out of service" ,"temporarily unavailable" , "available" };
		/** Write a string to the output window */
		
		public static void log(String string)
		{
			output.append(string + "\n" );
		}
		
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
