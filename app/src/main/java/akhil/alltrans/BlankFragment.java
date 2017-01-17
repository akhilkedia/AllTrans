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


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {


    public BlankFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        String instructions = "HOW TO USE THE APPLICATION\n" +
                "\n" +
                "1. In \"Global Settings\" tab, tap \"Microsoft Azure Translate Subscription Key\", input the key from below.\n" +
                "2. Choose the \"Translate from Language\" and \"Translate to Language\".\n" +
                "2. In the \"Apps to Translate\" tab, find the app you want to translate, click the checkbox next to it.\n" +
                "\n" +
                "HOW TO SIGN UP FOR MICROSOFT AZURE TRANSLATE\n" +
                "\n" +
                "1. Sign up for a Microsoft Azure account. \n" +
                "If you don’t already have an Azure account, sign up for a Microsoft Azure account at http://azure.com. \n" +
                "\n" +
                "2. After you have an account, sign into the Azure portal at http://portal.azure.com.\n" +
                "\n" +
                "3. Add a Microsoft Translator API subscription to your Azure account. Choose a pay-as-you-go subscription. Will require a credit card, but don't worry, nothing will be charged.\n" +
                "Select the + New option.\n" +
                "Select Intelligence from the list of services.\n" +
                "Select Cognitive Services APIs.\n" +
                "Select the API Type option.\n" +
                "Select Text Translation\n" +
                " In the Pricing Tier section, select the Free Tier (F0)\n" +
                "Fill out the rest of the form, and press the Create button.\n" +
                "\n" +
                "4. Retrieve your authentication key.\n" +
                "Go to All Resources and select the Microsoft Translator API you subscribed to.\n" +
                "Go to the Keys option and copy your subscription key to access the service.\n" +
                "\n" +
                "TROUBLESHOOTING\n" +
                "\n" +
                "1. If the app get stuck on loading the first time after you start translating, click on the app in \"Apps to translate\" tab, and in \"Other Settings\" fill in \"Delay Before Translating\" to 2000.\n" +
                "2. If the app used to translate fine, but stopped loading, click on the app in \"Apps to translate\" tab, and click on \"Click to Clear Translation Cache\".\n" +
                "3. If the app is not being translated, click on the app in \"Apps to translate\" tab, and enable \"Aggresive Mode\". Also check your Microsoft Subscription Key is correct.\n";
        textView.setText(instructions);
        return textView;
    }

}
