package rocaTech.roca.followme;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Pref_ServidoresPosicion extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefservidores);
		
	}
	
	
//	@Override
//    public boolean onCreateOptionsMenu(Menu menu)
//	{
//        menu.add(Menu.NONE, 0, 0, "Configuracion Servidores");
//        return super.onCreateOptionsMenu(menu);
//    }
 
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        switch (item.getItemId())
//        {
//            case 0:
//            //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//       	  	 
//            	SharedPreferences sharedPrefs = getSharedPreferences("pref_servidores",Context.MODE_PRIVATE );
//      	  	  
//            	StringBuilder builder = new StringBuilder();
//      	  	 
//      	  	  builder.append("\n" + sharedPrefs.getBoolean("enviar_mensajes_sin_panico", false));
//      	  	  builder.append("\n" + sharedPrefs.getString("intervalo_envio_mensaje_noPanico", "-1"));
//      	  	  builder.append("\n" + sharedPrefs.getBoolean("perform_updates_panic", false));
//      	  	  builder.append("\n" + sharedPrefs.getString("updates_interval_panic", "-1"));
//      	  	  
//      	  	Toast.makeText(getBaseContext(), builder.toString(),Toast.LENGTH_LONG).show();
////            	Intent i = new Intent(this, MenuAdmonMensajes.class);
////            	startActivity(i);
////                //startActivity(new Intent(this, MenuAdmonMensajes.class));
////            	String msg_for_me = i.getBundleExtra(name).getStringExtra("mensaje_recibido");
//                
//                return true;
//        }
//        return false;
//    }
    
}//fin de clase