DroneRemote
===========

# Description

DroneRemote is an application for piloting a drone with Bluetooth SPP (Serial Port Profile) protocol on an Android device (8+)

## Changelog

30.12.2013 : Release on Github

## Getting Started

The following frame (11 bytes) is sent via Bluetooth :
Start = 240 
Throttle = 0-100%
Yaw = 0-100%
Pitch = 0-100% 
Roll = 0-100% 
yAccelerometer = 60-110 degrees
zAccelerometer = 60-110 degrees
LED = 0-1 boolean
REC = 0-1 boolean
Checksum = 0-255
End = 250

The following frame (6 bytes) is recieved via Bluetooth :
Start = 240 
PowerVolt = 0-255
PowerPourcent = 0-255
Temperatur = 0-255
Checksum = 0-255
End = 250

Checksum calculation:
for (byte data : datas) checksum += data;
checksum = checksum & 0xff;
checksum = 0xff - checksum

## Reporting bugs & contributing & support

For support, contributing or issues send a mail to : contact@rinalducci.ch

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
