# README #

This repository contains the code and necessary files to build and run the **Pedestrian Audio Wearable System (PAWS)** smartphone applications.

PAWS is a low-power connected system for improving pedestrian safety in current and future smart cities. PAWS uses microphones embedded into a headset combined with low-power feature extraction, signal processing, and machine learning, for detecting, localizing, and providing alerts of oncoming vehicles to pedestrians in noisy environments.

The contents of this repository are summarized below.

* **PAWS**: Contains the source code and necessary files for the Pedestrian Audio Wearable System (PAWS) smartphone application.
	* Application: Contains the Android project for building and loading the PAWS smartphone application.
	* SEUS: This folder contains the PAWS car detection and localization classifier models. To run the PAWS smartphone application, this folder must be placed into the *Downloads* folder of the smartphone.
* **PAWS_LE**: Contains the source code and necessary files for the Pedestrian Audio Wearable System Low-Energy (PAWS LE) smartphone application. PAWS LE is the low-energy variant of PAWS and has finer granularity car localization, achieved by using a technique called *Angle via Polygonal Regression (AvPR)*.
	* Application: Contains the Android project for building and loading the PAWS LE smartphone application.
	* SEUS: This folder contains the PAWS LE car detection and localization classifier models. To run the PAWS LE smartphone application, this folder must be placed into the *Downloads* folder of the smartphone.
* **PAWS_Features**: Contains the Android project for the test application that displays the raw features obtained from the PAWS/PAWS_LE headset.
* **PAWS_Wireless_Latency**: Contains the Android project for the test application that measures the wireless latency between the PAWS/PAWS_LE headset using a button connected to the smartphone's audio jack.

# Reproducing the PAWS system

To reproduce the PAWS system, you must first install the PAWS smartphone application; details on how to do this can be found in the **PAWS/Application/** folder.

Next, you must reproduce the PAWS front-end PCB. Details on how to do this can be found in the PAWS-FrontEnd repository.

# Reproducing the PAWS LE System

To reproduce the PAWS LE system, you must first install the PAWS LE smartphone application; details on how to do this can be found in the **PAWS_LE/Application/** folder.

Next, you must reproduce the PAWS LE front-end PCB. Details on how to do this can be found in the PAWS-FrontEnd repository.

# More About the PAWS Project
To learn more about PAWS, please visit our [project page](http://icsl.ee.columbia.edu/projects/seus), or contact us at: [stephen.xia@columbia.edu](stephen.xia@columbia.edu). 	 
	
This repository is part of the **Pedestrian Audio Wearable System (PAWS)** project of the **Intelligent and Connected Systems Lab (ICSL)**, Columbia University.
For more information about our latest projects, please visit our [group website](http://icsl.ee.columbia.edu).