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
