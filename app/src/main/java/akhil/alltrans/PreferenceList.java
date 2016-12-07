package akhil.alltrans;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Created by akhil on 5/12/16.
 */

public class PreferenceList {
    public static String ClientID;
    public static String ClientSecret;
    public static String TranslateFromLanguage;
    public static String TranslateToLanguage;

    public static boolean SetText;
    public static boolean SetHint;
    public static boolean LoadURL;
    public static boolean DrawText;

    public static boolean Caching;
    public static int Delay;

    public static void getPref(XSharedPreferences gPref, XSharedPreferences lPref) {
        ClientID = gPref.getString("ClientID", "");
        ClientSecret = gPref.getString("ClientSecret", "");

        if (lPref.contains("OverRide")) {
            if (lPref.getBoolean("OverRide", false))
                gPref = lPref;
        }

        TranslateFromLanguage = gPref.getString("TranslateFromLanguage", "");
        TranslateToLanguage = gPref.getString("TranslateToLanguage", "");

        SetText = gPref.getBoolean("SetText", false);
        SetHint = gPref.getBoolean("SetHint", false);
        LoadURL = gPref.getBoolean("LoadURL", false);
        DrawText = gPref.getBoolean("DrawText", false);

        Caching = gPref.getBoolean("Cache", false);
        Delay = Integer.parseInt(gPref.getString("Delay", "0"));

    }
}
