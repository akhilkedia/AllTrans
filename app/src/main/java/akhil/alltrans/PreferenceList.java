/*
 * Copyright 2017 Akhil Kedia
 * This file is part of AllTrans.
 *
 * AllTrans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AllTrans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllTrans. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package akhil.alltrans;

import de.robv.android.xposed.XSharedPreferences;

class PreferenceList {
    public static String SubscriptionKey;
    public static String TranslateFromLanguage;
    public static String TranslateToLanguage;
    public static boolean EnableYandex;

    public static boolean SetText;
    public static boolean SetHint;
    public static boolean LoadURL;
    public static boolean DrawText;

    public static boolean Caching;
    public static long CachingTime;
    public static int Delay;
    public static int DelayWebView;
    public static boolean Scroll;

    public static void getPref(XSharedPreferences gPref, XSharedPreferences lPref, String packageName) {
        SubscriptionKey = gPref.getString("SubscriptionKey", "");
        EnableYandex = gPref.getBoolean("EnableYandex", false);
        //TODO: why is DelayWebView being read from gPref and not lPref?!
        DelayWebView = Integer.parseInt(gPref.getString("DelayWebView", "500"));
        //boolean anon = gPref.getBoolean("Anon", true);
        //boolean debug = gPref.getBoolean("Debug", false);
        //boolean localEnabled = gPref.getBoolean(packageName, false);

        CachingTime = lPref.getLong("ClearCacheTime", 0L);

        if (lPref.contains("OverRide")) {
            if (lPref.getBoolean("OverRide", false))
                gPref = lPref;
        }

        TranslateFromLanguage = gPref.getString("TranslateFromLanguage", "");
        TranslateToLanguage = gPref.getString("TranslateToLanguage", "");

        SetText = gPref.getBoolean("SetText", true);
        SetHint = gPref.getBoolean("SetHint", true);
        LoadURL = gPref.getBoolean("LoadURL", true);
        DrawText = gPref.getBoolean("DrawText", false);

        Caching = gPref.getBoolean("Cache", true);
        Delay = Integer.parseInt(gPref.getString("Delay", "0"));
        Scroll = gPref.getBoolean("Scroll", true);
    }
}
