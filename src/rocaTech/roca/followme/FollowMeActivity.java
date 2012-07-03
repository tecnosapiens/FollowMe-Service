package rocaTech.roca.followme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
    	
    }
    
   
    public void iniciarServicioLocalizacion()
    {
    	 startService(new Intent(this, LocationService.class));
    }
    
    public void stopServicioLocalizacion()
    {
    	stopService(new Intent(this, LocationService.class));
    }
    
}// Fin Activity