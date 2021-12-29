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

import android.app.Notification;
import android.app.Notification.MessagingStyle.Message;
import android.app.Person;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

import static akhil.alltrans.SetTextHookHandler.isNotWhiteSpace;


public class NotificationHookHandler extends XC_MethodReplacement implements OriginalCallable {

    public void callOriginalMethod(CharSequence translatedString, Object userData) {
        NotificationHookUserData notificationHookUserData = (NotificationHookUserData) userData;
        MethodHookParam methodHookParam = notificationHookUserData.methodHookParam;
        String originalString = notificationHookUserData.originalString;
        Method myMethod = (Method) methodHookParam.method;
        myMethod.setAccessible(true);
        Object[] myArgs = methodHookParam.args;
        Notification notification = (Notification) methodHookParam.args[methodHookParam.args.length - 1];

        if (translatedString != null) {
            changeText(notification, originalString, translatedString.toString());
        }
        try {
            utils.debugLog("In Thread " + Thread.currentThread().getId() + " Invoking original function " + methodHookParam.method.getName());
            XposedBridge.invokeOriginalMethod(myMethod, methodHookParam.thisObject, myArgs);
        } catch (Throwable e) {
            Log.e("AllTrans", "AllTrans: Got error in invoking method as : " + Log.getStackTraceString(e));
        }
    }

    public static Parcelable[] getMessagesFromBundleArray2(Parcelable[] bundles, String originalString, String translatedString) {
        if (bundles == null) {
            return null;
        }
        for (int i = 0; i < bundles.length; i++) {
            Bundle bundle = (Bundle) bundles[i];
            if (bundle == null) {
                continue;
            }
            if (bundle.containsKey("text") &&
                    bundle.getCharSequence("text") != null &&
                    bundle.getCharSequence("text").toString().equals(originalString)) {
                bundle.putCharSequence("text", translatedString);
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                Person senderPerson = bundle.getParcelable("sender_person");
                if (senderPerson != null && senderPerson.getName() == originalString) {
                    Person newPerson = senderPerson.toBuilder().setName(translatedString).build();
                    bundle.putParcelable("sender_person", newPerson);
                }
            }
            CharSequence senderName = bundle.getCharSequence("sender");
            if (senderName != null && senderName.equals(originalString)) {
                bundle.putCharSequence("sender", translatedString);
            }
            bundles[i] = bundle;
        }
        return bundles;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private CharSequence[] getAllText(Notification notification) {
        ArrayList<CharSequence> allNotificationText = new ArrayList<>();
        if (notification.extras == null) {
            return allNotificationText.toArray(new CharSequence[0]);
        }
        utils.debugLog("In Thread " + Thread.currentThread().getId() + " and it has extras " + notification.extras);

//        First simple Charsequences
        String[] charseqs = {
                Notification.EXTRA_TITLE,
                Notification.EXTRA_TITLE_BIG,
                Notification.EXTRA_TEXT,
                Notification.EXTRA_SUB_TEXT,
                Notification.EXTRA_INFO_TEXT,
                Notification.EXTRA_SUMMARY_TEXT};
        utils.debugLog(Arrays.toString(charseqs) + "");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            charseqs = Arrays.copyOf(charseqs, charseqs.length + 1);
            charseqs[charseqs.length - 1] = Notification.EXTRA_BIG_TEXT;
            utils.debugLog(Arrays.toString(charseqs) + "");
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            charseqs = Arrays.copyOf(charseqs, charseqs.length + 2);
            charseqs[charseqs.length - 2] = Notification.EXTRA_SELF_DISPLAY_NAME;
            charseqs[charseqs.length - 1] = Notification.EXTRA_CONVERSATION_TITLE;
            utils.debugLog(Arrays.toString(charseqs) + "");
        }
        for (String key : charseqs) {
            if (notification.extras.containsKey(key) && notification.extras.getCharSequence(key) != null) {
                utils.debugLog("Got string " + key + " as " + notification.extras.getCharSequence(key));
                allNotificationText.add(notification.extras.getCharSequence(key));
            }
        }

//        Then Charsequence Arrays
        String[] charSeqArr = {Notification.EXTRA_TEXT_LINES, Notification.EXTRA_PEOPLE};
        for (String key : charSeqArr) {
            if (notification.extras.containsKey(key) && notification.extras.getCharSequenceArray(key) != null) {
                allNotificationText.addAll(Arrays.asList(Objects.requireNonNull(notification.extras.getCharSequenceArray(key))));
            }
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//        Then Person style
            if (notification.extras.containsKey(Notification.EXTRA_MESSAGING_PERSON)) {
                Person person = notification.extras.getParcelable(Notification.EXTRA_MESSAGING_PERSON);
                if (person != null) {
                    allNotificationText.add(person.getName());
                }
            }
            if (notification.extras.containsKey(Notification.EXTRA_PEOPLE_LIST)) {
                ArrayList<Person> people = notification.extras.getParcelableArrayList(Notification.EXTRA_PEOPLE_LIST);
                if (people != null) {
                    for (Person person : people) {
                        allNotificationText.add(person.getName());
                    }
                }
            }
        }

//        Then MessagingStyle Arrays
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            String[] messageArr = {Notification.EXTRA_MESSAGES};
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                messageArr = new String[]{Notification.EXTRA_MESSAGES, Notification.EXTRA_HISTORIC_MESSAGES};
            }
            for (String key : messageArr) {
                if (notification.extras.containsKey(key) && notification.extras.getParcelableArray(key) != null) {
                    Parcelable[] histMessages = notification.extras.getParcelableArray(key);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        List<Message> messages = Message.getMessagesFromBundleArray(histMessages);
                        for (Message message : messages) {
                            if (message == null) {
                                continue;
                            }
                            if (message.getText() != null) {
                                allNotificationText.add(message.getText());
                            }
                            if (message.getSenderPerson() != null) {
                                allNotificationText.add(message.getSenderPerson().getName());
                            }
                        }
                    }
                }
            }
        }
//        Not translating Message.setData(), and Person.getURI() as requires URI retrieval
//        Not translating Actions as that will mean translating RemoteInputs which will mean
        return allNotificationText.toArray(new CharSequence[0]);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void changeText(Notification notification, String originalString, String translatedString) {
        utils.debugLog("In Thread " + Thread.currentThread().getId() + " and it has extras " + notification.extras);

//        First simple Charsequences
        String[] charseqs = {
                Notification.EXTRA_TITLE,
                Notification.EXTRA_TITLE_BIG,
                Notification.EXTRA_TEXT,
                Notification.EXTRA_SUB_TEXT,
                Notification.EXTRA_INFO_TEXT,
                Notification.EXTRA_SUMMARY_TEXT};
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            charseqs = Arrays.copyOf(charseqs, charseqs.length + 1);
            charseqs[charseqs.length - 1] = Notification.EXTRA_BIG_TEXT;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            charseqs = Arrays.copyOf(charseqs, charseqs.length + 2);
            charseqs[charseqs.length - 2] = Notification.EXTRA_SELF_DISPLAY_NAME;
            charseqs[charseqs.length - 1] = Notification.EXTRA_CONVERSATION_TITLE;
        }
        for (String key : charseqs) {
            if (notification.extras.containsKey(key) && notification.extras.getCharSequence(key) != null) {
                if (Objects.equals(notification.extras.getCharSequence(key).toString(), originalString)) {
                    notification.extras.putCharSequence(key, translatedString);
                }
            }
        }

//        Then Charsequence Arrays
        String[] charSeqArr = {Notification.EXTRA_TEXT_LINES, Notification.EXTRA_PEOPLE};
        for (String key : charSeqArr) {
            if (notification.extras.containsKey(key) && notification.extras.getCharSequenceArray(key) != null) {
                CharSequence[] textList = notification.extras.getCharSequenceArray(key);
                if (textList == null) {
                    continue;
                }
                ArrayList<CharSequence> newTextList = new ArrayList<>();
                for (CharSequence charSequence : textList) {
                    if (Objects.equals(charSequence.toString(), originalString)) {
                        newTextList.add(translatedString);
                    } else {
                        newTextList.add(charSequence);
                    }
                }
                notification.extras.putCharSequenceArray(key, newTextList.toArray(new CharSequence[0]));
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            //        Then Person style
            if (notification.extras.containsKey(Notification.EXTRA_MESSAGING_PERSON)) {
                Person person = notification.extras.getParcelable(Notification.EXTRA_MESSAGING_PERSON);
                if (person != null) {
                    if (person.getName() == originalString) {
                        Person newPerson = person.toBuilder().setName(translatedString).build();
                        notification.extras.putParcelable(Notification.EXTRA_MESSAGING_PERSON, newPerson);
                    }
                }
            }
            if (notification.extras.containsKey(Notification.EXTRA_PEOPLE_LIST)) {
                ArrayList<Person> people = notification.extras.getParcelableArrayList(Notification.EXTRA_PEOPLE_LIST);
                ArrayList<Person> newPeople = new ArrayList<>();
                if (people != null) {
                    for (Person person : people) {
                        if (person.getName() == originalString) {
                            Person newPerson = person.toBuilder().setName(translatedString).build();
                            newPeople.add(newPerson);
                        } else {
                            newPeople.add(person);
                        }
                    }
                    notification.extras.putParcelableArrayList(Notification.EXTRA_PEOPLE_LIST, newPeople);
                }
            }
        }

//        Then MessagingStyle Arrays
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            String[] messageArr = {Notification.EXTRA_MESSAGES};
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                messageArr = new String[]{Notification.EXTRA_MESSAGES, Notification.EXTRA_HISTORIC_MESSAGES};
            }
            for (String key : messageArr) {
                if (notification.extras.containsKey(key) && notification.extras.getParcelableArray(key) != null) {
                    Parcelable[] histMessages = notification.extras.getParcelableArray(key);
                    Parcelable[] newmessages = getMessagesFromBundleArray2(histMessages, originalString, translatedString);
                    notification.extras.putParcelableArray(key, newmessages);
                }
            }
        }
//        Not translating Message.setData(), and Person.getURI() as requires URI retrieval
//        Not translating Actions as that will mean translating RemoteInputs which will mean
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
        utils.debugLog("Notification : in notificationhook ");
        Notification notification = (Notification) methodHookParam.args[methodHookParam.args.length - 1];
        Object[] userDataOut = new Object[3];
        userDataOut[0] = methodHookParam;
        userDataOut[1] = "";
        callOriginalMethod("", userDataOut);

        CharSequence[] allNotificationTexts = getAllText(notification);

        for (CharSequence text : allNotificationTexts) {
            if (text == null || !isNotWhiteSpace(text.toString())) {
                continue;
            }
            String stringArgs = text.toString();
            utils.debugLog("In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);
            NotificationHookUserData userData = new NotificationHookUserData(methodHookParam, text.toString());

            alltrans.cacheAccess.acquireUninterruptibly();
            if (PreferenceList.Caching && alltrans.cache.containsKey(stringArgs)) {
                String translatedString = alltrans.cache.get(stringArgs);
                utils.debugLog("In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                alltrans.cacheAccess.release();
                callOriginalMethod(translatedString, userData);
                continue;
            } else {
                alltrans.cacheAccess.release();
            }

            GetTranslate getTranslate = new GetTranslate();
            getTranslate.stringToBeTrans = stringArgs;
            getTranslate.originalCallable = this;
            getTranslate.userData = userData;
            getTranslate.canCallOriginal = true;
            GetTranslateToken getTranslateToken = new GetTranslateToken();
            getTranslateToken.getTranslate = getTranslate;
            getTranslateToken.doAll();
        }
        return null;
    }

}

class NotificationHookUserData {
    public final XC_MethodHook.MethodHookParam methodHookParam;
    public final String originalString;

    NotificationHookUserData(XC_MethodHook.MethodHookParam methodHookParam, String originalString) {
        this.methodHookParam = methodHookParam;
        this.originalString = originalString;
    }
}
