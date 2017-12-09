# ![AllTrans](https://raw.githubusercontent.com/akhilkedia/AllTrans/master/app/src/main/res/mipmap-hdpi/ic_launcher.png)AllTrans - Completely Translate Apps

Like Chrome translation of webpages, but for Android apps.

[![LicenseGPLv3](https://img.shields.io/badge/License-GPL%20v3-green.svg)](http://www.gnu.org/licenses/gpl-3.0) [![Donate](https://img.shields.io/badge/Donate-PayPal-blue.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UY6TVJXST724J)

## Table of Contents

 * [What AllTrans does](#what-alltrans-does)
 * [ScreenShots and Videos](#screenshots-and-videos)
   + [Below are some screenshots of AllTrans translating apps from Korean to English](#below-are-some-screenshots-of-alltrans-translating-apps-from-korean-to-english)
   + [Below are some of the screenshots of AllTrans app](#below-are-some-of-the-screenshots-of-alltrans-app)
   + [Videos](#videos)
 * [How to install AllTrans](#how-to-install-alltrans)
 * [How to use AllTrans](#how-to-use-alltrans)
 * [How to get Yandex Translate Key](#how-to-get-yandex-translate-key)
 * [How to get Microsoft Azure Translate Key](#how-to-get-microsoft-azure-translate-key)
 * [TroubleShooting](#troubleshooting)
   + [No app is being translated in any app](#no-app-is-being-translated-in-any-app)
   + [Translated app is stuck in opening screen](#translated-app-is-stuck-in-opening-screen)
   + [The Translated app is Force Close](#the-translated-app-is-force-close)
   + [A particular app is not being translated](#a-particular-app-is-not-being-translated)
   + [A game app is not being translated](#a-game-app-is-not-being-translated)
   + [If you still have problems like Force Close or parts of an app not being translated](#if-you-still-have-problems-like-force-close-or-parts-of-an-app-not-being-translated)
 * [Donations](#donations)
 * [License](#license)
 
## What AllTrans does

It replaces **all text in an app** in a language from one language to another at runtime.

Say for example an app is in German. A user selects the app name, and the required language conversion (say German to English).

Then whenever the user uses the required app, all the text, **ANYWHERE** in the app, are replaced by their English equivalents.

This is something similar to the way Google Translate works in Chrome.

**Note** - Due to a technical limitation, this won't work with many games. Nothing can be done about this.

## ScreenShots and Videos

### Below are some screenshots of AllTrans translating apps from Korean to English

![](https://raw.githubusercontent.com/akhilkedia/AllTrans/master/screenshots/Joint1S.png)

![](https://raw.githubusercontent.com/akhilkedia/AllTrans/master/screenshots/Joint2S.png)

![](https://raw.githubusercontent.com/akhilkedia/AllTrans/master/screenshots/Joint3S.png)

### Below are some of the screenshots of AllTrans app

![](https://raw.githubusercontent.com/akhilkedia/AllTrans/master/screenshots/Screen1S.png)

![](https://raw.githubusercontent.com/akhilkedia/AllTrans/master/screenshots/Screen2S.png)

![](https://raw.githubusercontent.com/akhilkedia/AllTrans/master/screenshots/Screen3S.png)

### Videos

A video (in English) showing how to use AllTrans by Gadget Hacks on Youtube [https://www.youtube.com/watch?v=sKDtkmISi6k](https://www.youtube.com/watch?v=sKDtkmISi6k)

[![Alt text](https://img.youtube.com/vi/sKDtkmISi6k/0.jpg)](https://www.youtube.com/watch?v=sKDtkmISi6k)

## How to install AllTrans

1. This application requires android version 4.0.1 or later (Android IceCreamSandwich or later - so far JellyBean, Kitkat, Lollipop and MarshMallow).
Until official Xposed for Android 7.0 Nougat is released, Nougat will not be released. 
2. Make sure your android phone is [rooted](https://en.wikipedia.org/wiki/Rooting_(Android_OS)).
If you don't know what rooting is, this app won't work for you.
3. Make sure you have [Xposed Framework](https://forum.xda-developers.com/showthread.php?t=3034811) installed and running.
4. Build this project in Android studio and install the produced AllTrans apk.
You can also get the latest release of the APK from the [Xposed Module Repository](http://repo.xposed.info/module/akhil.alltrans)
5. Reboot your phone.

## How to use AllTrans

This application **requires** you to sign up for **free** a key from Microsoft or Yandex Translate. See instructions below on how to get the keys.

Yandex Translate supports more languages, has a much easier signup process, and requires **NO CREDIT CARD**.

Microsoft Translate gives **better translations**, but requires a credit card to sign up. (But don't worry, nothing will be charged.)

1. Make sure `AllTrans` app is enabled in `Xposed Installer -> Modules`.
1. Launch `AllTrans` app. In `"Global Settings"` tab, enable `"Use Yandex instead of Microsoft"` if you want to use Yandex. Disable to use Microsoft.
2. Choose the `"Translate from Language"` and `"Translate to Language"`.
3. In the `"Apps to Translate"` tab, find the app you want to translate, click the checkbox next to it. If the checkbox is not visible, try rotating your phone sideways into LandScape mode.
4. Close and restart the app you want translated - it should be translated!

## How to get Yandex Translate Key

1. Go to [Yandex Translate API Website](https://tech.yandex.com/keys/get/?service=trnsl)
2. If you don't already have an account, click `"register"` and sign up for a new account.
3. After signing up, agree to the User Agreement and click `"Get API Key"`.
4. You have successfully received your key!

The subscription key is something like `"trnsl.1.1.201701......"`

## How to get Microsoft Azure Translate Key

1. Sign up for a Microsoft Azure account.
If you don't already have an Azure account, sign up for a [Microsoft Azure account](http://azure.com).
2. After you have an account, sign into the [Azure Portal](http://portal.azure.com).
3. Add a `"Microsoft Translator API Subscription"` to your Azure account.
  1. Choose a `"pay-as-you-go"` subscription. Will require a credit card, but don't worry, nothing will be charged.
  2. Select the `"+ New"` option.
  3. Select `"Intelligence"` from the list of services.
  4. Select `"Cognitive Services APIs"`.
  5. Select the `"API Type"` option.
  6. Select `"Text Translation"`.
  7. In the `"Pricing Tier"` section, select the `"Free Tier (F0)"`.
  8. Fill out the rest of the form, and press the `"Create"` button.
4. Retrieve your `"Authentication Key"`.
  1. Go to `"All Resources"` and select the Microsoft Translator API you subscribed to.
  2. Go to the `"Keys"` option and copy your subscription key to access the service.
  
The subscription key is something like `"321dcba...."`

## TroubleShooting

### No app is being translated in any app

* If no app is being translated, check is `"AllTrans"` is enabled in `Xposed Installer -> Modules`.
* Also check if your `"Microsoft/Yandex Subscription Key"` is correct.

### Translated app is stuck in opening screen

* Click on the app's name in `"Apps to translate"` tab, and click on `"Click to Clear Translation Cache"`.
* If this is the first time you started translating this app, click on the app's name in `"Apps to Translate"` tab, and in `"Other Settings"` fill in `"Delay Before Replcaing With Translated Text"` to `2000`.

### The Translated app is "Force Close"

* Click on the app's name in `"Apps to translate"` tab, and click on `"Click to Clear Translation Cache"`.
* Click on the app's name in `"Apps to Translate"` tab, and in `"Other Settings"` fill in `"Delay Before Replcaing With Translated Text"` to `2000`.
* Click on the app's name in `"Apps to translate"` tab, and in `"Other Settings"` fill in `"Delay Before Starting to Translate WebViews"` to `2000`.

### A particular app is not being translated

* Click on the app's name in `"Apps to translate"` tab, and in `"Other Settings"` fill in `"Delay Before Starting to Translate WebViews"` to `2000`.
* If some parts of the app are still not being translated, click on the app's name in `"Apps to translate"` tab, and enable `"Aggressive Mode"`.

### A game app is not being translated

* Due to technical limitations, `"AllTrans"` will not work with many games. Nothing can be done about this.

### If you still have problems like "Force Close" or parts of an app not being translated

Contact me for support. When you do, I will need atleast the following information - 

1. The name of the app, along with a **link to download the app** from some APK store like `Google Play Store`, `CoolApk`, etc.
2. Your phone's Android version.
3. A `"logcat"` of you trying to open the app which gives you problems. You can use apps freely available on the Play Store for this, such as [This one](https://play.google.com/store/apps/details?id=com.nolanlawson.logcat)

Once you have the above, contact me on the  [XDA support thread](https://forum.xda-developers.com/xposed/modules/xposed-alltrans-completely-translate-t3539878).
You can also report issues on the  [Project's `Github`](https://github.com/akhilkedia/AllTrans).

## Donations
If you like this project, buy me a cup of coffee! :) 

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UY6TVJXST724J)

## License

This program is AllTrans
Copyright (C) 2017  Akhil Kedia

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
