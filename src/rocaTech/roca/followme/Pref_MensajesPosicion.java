package rocaTech.roca.followme;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Pref_MensajesPosicion extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefmensajes);
	}
}//fin de clase
