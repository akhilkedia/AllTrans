package akhil.alltrans;

import de.robv.android.xposed.XSharedPreferences;

class PreferenceList {
    public static String SubscriptionKey;
    public static String TranslateFromLanguage;
    public static String TranslateToLanguage;

    public static boolean SetText;
    public static boolean SetHint;
    public static boolean LoadURL;
    public static boolean DrawText;

    public static boolean Caching;
    public static int Delay;

    public static void getPref(XSharedPreferences gPref, XSharedPreferences lPref) {
        SubscriptionKey = gPref.getString("SubscriptionKey", "");

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
