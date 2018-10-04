# PAWS Low Energy

This folder contains the project files for the PAWS LE smartphone application.

The PAWS LE system consists of this smartphone application and an embedded front-end headset consisting of four embedded microphones, a custom ASIC, and the Nordic nRF52 BLE chip.

The smartphone application detects, localizes, and alerts users of oncoming cars. Features used to localize cars are computed in the embedded front-end headset.

Parts of this code was originally adapted from the Android-nRF-UART application version 2.0.1 from Nordic Semiconductor for BLE communication

* Note: The PAWS project was at some point known as SEUS, which is why SEUS is used at some points rather than PAWS.

# Differences from PAWS
1. Angle estimation is done via AvPR, a regression-based method rather than an eight quadrant classifier
2. Distance estimation is also done via regressing over the maximal cepstral coefficients rather than a classifier

# Installing this application on your phone

There are two ways to install this application on your phone.

1. Install the apk in **app\build\outputs**
2. Install via Android Studio

Once the application is installed, you must move the **../SEUS** folder into the **Downloads** folder of your phone before running the application.

# Modifying and browsing the source code

Create a new Android Studio project with the files in this folder.

### Note
- Android 4.3 or later is required
- Android Studio supported