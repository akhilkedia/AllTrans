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
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
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
                "This application requires you to sign up for a key from Microsoft or Yandex Translate.\n" +
                "Yandex Translate supports more languages, has a much easier signup process, and requires NO CREDIT CARD.\n" +
                "Microsoft Translate gives better results, but requires a credit card to sign up. (But don't worry, nothing will be charged.)\n" +
                "\n" +
                "1. In \"Global Settings\" tab, enable \"Use Yandex instead of Microsoft\" if you want to use Yandex, else disable to use Microsoft.\n" +
                "1. In \"Global Settings\" tab, tap \"Enter Microsoft/Yandex Subscription Key\", input the corresponding key (see Instructions from below).\n" +
                "2. Choose the \"Translate from Language\" and \"Translate to Language\".\n" +
                "3. In the \"Apps to Translate\" tab, find the app you want to translate, click the checkbox next to it.\n" +
                "\n" +
                "\n" +
                "HOW TO GET YANDEX TRANSLATE SUBSCRIPTION KEY\n" +
                "\n" +
                "1. Go to Yandex Website - https://tech.yandex.com/keys/get/?service=trnsl\n" +
                "2. If you don't already have an account, click \"register\" and sign up for a new account.\n" +
                "3. After signing up, agree to the User Agreement and click \"Get API Key\".\n" +
                "4. You have successfully received your key! It's something like \"trnsl.1.1.201701......\"\n" +
                "\n" +
                "HOW TO GET MICROSOFT AZURE TRANSLATE SUBSCRIPTION KEY\n" +
                "\n" +
                "1. Sign up for a Microsoft Azure account at http://azure.com\n" +
                "\n" +
                "2. After you have an account, sign into the Azure portal at http://portal.azure.com\n" +
                "\n" +
                "3. Add a Microsoft Translator API subscription to your Azure account.\n" +
                "Choose a pay-as-you-go subscription. Will require a credit card, but don't worry, nothing will be charged.\n" +
                "Select the + New option.\n" +
                "Select Intelligence from the list of services.\n" +
                "Select Cognitive Services APIs.\n" +
                "Select the API Type option.\n" +
                "Select Text Translation\n" +
                "In the Pricing Tier section, select the Free Tier (F0)\n" +
                "Fill out the rest of the form, and press the Create button.\n" +
                "\n" +
                "4. Retrieve your authentication key.\n" +
                "Go to All Resources and select the Microsoft Translator API you subscribed to.\n" +
                "Go to the Keys option and copy your subscription key to access the service.\n" +
                "The subscription key is something like \"321dcba....\"\n" +
                "\n" +
                "TROUBLESHOOTING\n" +
                "\n" +
                "1. If the app get stuck on loading the first time after you start translating, click on the app in \"Apps to translate\" tab, and in \"Other Settings\" fill in \"Delay Before Translating\" to 2000.\n" +
                "2. If the app used to translate fine, but stopped loading, click on the app in \"Apps to translate\" tab, and click on \"Click to Clear Translation Cache\".\n" +
                "3. If the app is not being translated, click on the app in \"Apps to translate\" tab, and enable \"Aggressive Mode\". Also check your Microsoft Subscription Key is correct.\n";
        textView.setText(instructions);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        return textView;
    }

}
