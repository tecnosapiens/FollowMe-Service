package rocaTech.roca.followme;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Pref_Aplicacion extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefaplicacion);
	}
	
}//fin de clase
