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
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * A simple {@link Fragment} subclass.
 */
public class InstructionsFragment extends Fragment {

    public InstructionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.readme)));
        StringBuilder datax = new StringBuilder("");
        try {
            String readString = bufferedReader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        String instructions = datax.toString();

        //final String instructions = getString(R.string.how_to_use);
        final WebView webView = new WebView(getActivity());

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(false);

        webView.loadData(instructions, "text/html; charset=utf-8", "UTF-8");

        //webView.loadUrl("file:///android_res/raw/readme.html");
        return webView;
    }

}
