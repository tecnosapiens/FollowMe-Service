package rocaTech.roca.followme;


import rocaTech.roca.followme.R.id;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.util.Calendar;
import java.util.GregorianCalendar;



public class FollowMeActivity extends Activity
{
	//variables de apoyo
	 private static final String TAG = "MyGUIService";
	
	
	// variables de la GUI
	private static TextView output;
	private static Button btnPanico;
	private static Button btnPararApli;
	private static Button btnOcultarApli;
	private boolean IsBtnPanicoPulsado;
	
	private static TextView textEdadEnvio;
	private static TextView textPosActual;
	private static ImageView imgProvPosGeo;
			
	//Obtenemos la hora actual
	Calendar calendario;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // se inicia el servicio de localizacion geografica y envio de mensajes de posicion
        iniciarServicioLocalizacion();
               
        // INSTRUCCIONES PARA RECUPERAR LAS PREFERENCIAS DEL USUARIO
        IsBtnPanicoPulsado = false;
        
		//Obtenemos la hora actual
		calendario = new GregorianCalendar();
					
	  	
	  	// FIN PREFERENCIAS DE USUARIO
        
        output = (TextView) findViewById(R.id.output);
    	output.setMovementMethod(new ScrollingMovementMethod());
    	
    	 textEdadEnvio = (TextView) findViewById(R.id.textEdadEnvio);
    	 textPosActual = (TextView) findViewById(R.id.textPosActual);
    	 imgProvPosGeo = (ImageView) findViewById(R.id.imageProvServPos);
    	 
    	 //set_proveedorPosGeo("noRecep");
    	
    	//log("Iniciando el Sistema de Posicion Geografica");
    
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
    				BtnPanicoPulsoadoBroadCast(IsBtnPanicoPulsado);
    				btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_stop));
    				
    				log("Boton Panico Activado");

    				Log.d(TAG, "onClick: boton panico pulsado");
    				
    			   
    			} 
    			else
    			{
    				
    				IsBtnPanicoPulsado = false;
    				BtnPanicoPulsoadoBroadCast(IsBtnPanicoPulsado);
    				btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_go));
    				
    				log("Boton Panico Desactivado");
    				
    				Log.d(TAG, "onClick: boton panico despulsado");


    			}


    		}
    	});
    	
    	btnPararApli = (Button) findViewById(id.BtnKillApli);
    	
    	btnPararApli.setOnClickListener(new View.OnClickListener() 
    	{
			
			public void onClick(View v) {
				
				// Instruccion para matar el los procesos de la aplicacion				
				PararCompletaAplicacion();
				
			}
		});
    	
    	
    	btnOcultarApli = (Button) findViewById(id.BtnOcultarApli);
    	
    	btnOcultarApli.setOnClickListener(new View.OnClickListener() 
    	{
			
			public void onClick(View v) {
				
				// Instruccion para matar el los procesos de la aplicacion	
				OcultarAplicacion();
				
				
			}
		});
    }
    
    @Override
	protected void onResume()
	{
		super.onResume();

		// Al reestablecer la visualizacion de la Aplicacion se recupera el estado del boton de panico
		// para con esto contiuar la ejecucion de la aplicacion en su ultimo estado
	    if (LocationService.get_EstadoPanico()) 
		{
	    	//((ToggleButton) btnPanico).setChecked(true);
			btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_stop));
			//log("Aplicacion reiniciada con Boton Panico Activado");
			BtnPanicoPulsoadoBroadCast(true);
		} else
		{
			//((ToggleButton) btnPanico).setChecked(false);
			btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_go));
			//log("Aplicacion reiniciada con Boton Panico Desactivado");
			BtnPanicoPulsoadoBroadCast(false);

		}
	    
	    
	    // se envia un mensaje al LocationService para que recupere las preferencias del Usuario
	    preferenciasUsuarioRecuperar(true);


	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
	}
	

	private void PararCompletaAplicacion()
	{
		//TODO: ANTES DE EJECUTAR EL PARO DE LA APLICACION SE DEBE PEDIR CONTRASEÑA
		stopServicioLocalizacion();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private void OcultarAplicacion()
	{
		//TODO: ANTES DE EJECUTAR EL OCULTAMIENTO........
		
		onBackPressed();
	}
	
	
		
    public void iniciarServicioLocalizacion()
    {
    	 
    	 startService(new Intent(this, LocationService.class));
    	 Log.d(TAG, "iniciarServicioLocalizacion: se va llamar a iniciar");
    }
    
    public void stopServicioLocalizacion()
    {
    	stopService(new Intent(this, LocationService.class));
    }
    
    public void BtnPanicoPulsoadoBroadCast(boolean valor)
    {
    	Intent i = new Intent("android.intent.action.MAIN").putExtra("panico_pulsado", valor);
    		Context context = this.getApplicationContext();
            context.sendBroadcast(i);
           log("Alarma Panico Activada");
    	
    }
    
    public void preferenciasUsuarioRecuperar(boolean valor)
    {
    	Intent i = new Intent("android.intent.action.MAIN").putExtra("prefUsuario_cambio", valor);
		Context context = this.getApplicationContext();
        context.sendBroadcast(i);
        log("Preferencias Usuario Recuperadas");
    }
    
    //******************************************************************
  	//		FUNCIONES DE ADMINISTRACION DE MENUS DE CONFIGURACION DE USUARIO
  	//
  	//******************************************************************
  		
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_principal, menu);
    return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) 
	    {
		    case R.id.MenuMensajes:
		       	startActivity(new Intent(this, Pref_MensajesPosicion.class));
		    return true;
		    case R.id.MenuServidores:
		    	startActivity(new Intent(this, Pref_ServidoresPosicion.class));
		    return true;
		    case R.id.MenuServicios:
		    	startActivity(new Intent(this, Pref_Aplicacion.class));
		    return true;
		    default:
		    return super.onOptionsItemSelected(item);
	    }
    }
    
   

 		public static void log(String string)
 		{
 			output.append(string + "\n" );
 			
 			final Layout layout = output.getLayout();
 	        if(layout != null)
 	        {
 	            int scrollDelta = layout.getLineBottom(output.getLineCount() - 1) - output.getScrollY() - output.getHeight();
 	            if(scrollDelta > 0)
 	            {
 	            	output.scrollBy(0, scrollDelta);
 	            }
 	        }
 		}
 		
 		
 		public static void set_timeLastMSN(String stringTiempo)
 		{
 			textEdadEnvio.setText(stringTiempo);
 		}
 		
 		public static void set_lastPosGeo(String lastLat, String lastLong)
 		{
 			
 			
 			textPosActual.setText(lastLat + "\n" + lastLong);
 		}
 		
 		public static void set_proveedorPosGeo(String prov)
 		{
 			
 			
 			if(prov.compareTo("gps") == 0)
 			{
 				
 			  imgProvPosGeo.setImageResource(R.drawable.gps);
 			}
 			else
 			{
 				
 				
 				if(prov.compareTo("passive") == 0)
 				{
 					
 					imgProvPosGeo.setImageResource(R.drawable.celular);
 				}
 				else
 				{
 					if(prov.compareTo("network") == 0)
 					{
 						
 						imgProvPosGeo.setImageResource(R.drawable.network);
 					}
 					else
 					{
 						imgProvPosGeo.setImageResource(R.drawable.no_recepcion);
 					}
 				}
 			}
 	
 		}
 		
 		
}// Fin Activity