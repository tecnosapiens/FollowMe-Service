package rocaTech.roca.followme;


import rocaTech.roca.followme.R.id;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
	private boolean IsBtnPanicoPulsado;
			
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
    				
    			   
    			} else
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
    }
    
    @Override
	protected void onResume()
	{
		super.onResume();

		// Al reestablecer la visualizacion de la Aplicacion se recupera el estado del boton de panico
		// para con esto contiuar la ejecucion de la aplicacion en su ultimo estado
	    if (LocationService.get_EstadoPanico()) 
		{
	    	((ToggleButton) btnPanico).setChecked(true);
			btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_stop));
			log("Aplicacion reiniciada con Boton Panico Activado");

		} else
		{
			((ToggleButton) btnPanico).setChecked(false);
			btnPanico.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_button_go));
			log("Aplicacion reiniciada con Boton Panico Desactivado");

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
           log("Se activa alarma de Panico");
    	
    }
    
    public void preferenciasUsuarioRecuperar(boolean valor)
    {
    	Intent i = new Intent("android.intent.action.MAIN").putExtra("prefUsuario_cambio", valor);
		Context context = this.getApplicationContext();
        context.sendBroadcast(i);
        log("Se recuperan preferencias Usuario");
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
		   // menu de opcion 3 pulsada
		    return true;
		    default:
		    return super.onOptionsItemSelected(item);
	    }
    }
    
   

 		public static void log(String string)
 		{
 			output.append(string + "\n" );
 		}
 		
    
}// Fin Activity