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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

class PreferenceList {
    public static boolean Enabled;
    public static boolean LocalEnabled;
    public static boolean Debug;

    public static String SubscriptionKey;
    public static String TranslateFromLanguage;
    public static String TranslateToLanguage;
    public static String TranslatorProvider;

    public static boolean SetText;
    public static boolean SetHint;
    public static boolean LoadURL;
    public static boolean DrawText;
    public static boolean Notif;

    public static boolean Caching;
    public static long CachingTime;
    public static int Delay;
    public static int DelayWebView;
    public static boolean Scroll;

    public static Object getValue(Map<String, Object> pref, String key, Object defValue) {
        return pref.containsKey(key) ? pref.get(key) : defValue;
    }

    public static void getPref(String globalPref, String localPref, String packageName) {
        Map<String, Object> gPref = new Gson().fromJson(globalPref, new TypeToken<Map<String, Object>>() {
        }.getType());
        Map<String, Object> lPref = new Gson().fromJson(localPref, new TypeToken<Map<String, Object>>() {
        }.getType());

        Enabled = (boolean) getValue(gPref, "Enabled", false);
        LocalEnabled = (boolean) getValue(gPref, packageName, false);
        Debug = (boolean) getValue(gPref, "Debug", false);

        SubscriptionKey = (String) getValue(gPref, "SubscriptionKey", "");
        TranslatorProvider = (String) getValue(gPref, "TranslatorProvider", "g");

        CachingTime = Long.parseLong((String) getValue(lPref, "ClearCacheTime", "0"));

        if ((boolean) getValue(lPref, "OverRide", false)) {
            gPref = lPref;
        }

        TranslateFromLanguage = (String) getValue(gPref, "TranslateFromLanguage", "");
        TranslateToLanguage = (String) getValue(gPref, "TranslateToLanguage", "");

        SetText = (boolean) getValue(gPref, "SetText", true);
        SetHint = (boolean) getValue(gPref, "SetHint", true);
        LoadURL = (boolean) getValue(gPref, "LoadURL", true);
        DrawText = (boolean) getValue(gPref, "DrawText", false);
        Notif = (boolean) getValue(gPref, "Notif", false);

        Caching = (boolean) getValue(gPref, "Cache", true);
        Delay = Integer.parseInt((String) getValue(gPref, "Delay", "0"));
        Scroll = (boolean) getValue(gPref, "Scroll", false);
        DelayWebView = Integer.parseInt((String) getValue(gPref, "DelayWebView", "500"));
    }
}
