DroneRemote
===========

## Description

DroneRemote is an Android application for piloting drones (multicopter or what you want) with Bluetooth SPP (Serial Port Profile) protocol on an Android device (SDK: 8+).

This project was developped with Android Studio (Intellij), but is also compatible with Eclipse.

Don't forget to import the project and not to open it.

I will be happy if you let me now if you used this project to make yours. :)

## Changelog

30.12.2013 : Release on GitHub

## Getting Started

The whole code is commented, but here are some informations :

The following frame (11 bytes) is sent via Bluetooth :
- Start = 240 
- Throttle = 0-100%
- Yaw = 0-100%
- Pitch = 0-100% 
- Roll = 0-100% 
- yAccelerometer = 60-110 degrees
- zAccelerometer = 60-110 degrees
- LED = 0-1 boolean
- REC = 0-1 boolean
- Checksum = 0-255
- End = 250

The following frame (6 bytes) is recieved via Bluetooth :
- Start = 240 
- PowerVolt = 0-255
- PowerPourcent = 0-255
- Temperatur = 0-255
- Checksum = 0-255
- End = 250

Checksum calculation:
```java
for (byte data : datas) checksum += data;
checksum = checksum & 0xff;
checksum = 0xff - checksum
```

## Reporting bugs, contributing & support

For issues, contributing or support, please send a mail to : contact@rinalducci.ch

## This project uses

- Google Bluetooth Sample (modified)
- RoundedImageView by Makeramen
- Joystick by MobileAnarchy (modified)
- Toggle button by KP Bird (modified)
- Icon by VisualPharm (modified - http://www.visualpharm.com)

## License

Copyright (C) 2013 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Authors

Marco Rinalducci (Swiss)
- https://github.com/MRinalducci
