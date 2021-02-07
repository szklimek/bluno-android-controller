# Bluno Controller Android application
This repository holds source code of example Android application that allows to communicate with [Bluno](https://www.dfrobot.com/product-1044.html) using Bluetooth LE connection.
The application allows to:
* establish BLE connection with Bluno
* send short text message that in presented example is displayed on Bluno's LCD screen and later sent back to the app

The project can be used to get an overview of BLE on Android or as a base for more advanced projects. 

## Project overview
Project consists of:
* source code of Android application [app module](app)
* source code of Bluno controller: [bluno_sketch_ino](bluno/bluno_sketch.ino)

Following elements are necessary to run the project:
* any Android device with OS version 6< (Marshmallow) with BLE
* [Bluno](https://www.dfrobot.com/product-1044.html) with connected lcd screen compatible with Hitachi HD44780 driver (example of setting it up can be found in ["Hello World!" Arduino tutorial](https://www.arduino.cc/en/Tutorial/LibraryExamples/HelloWorld))

## Bluno Bluetooth documentation
Bluno GATT Table:

---------------
    Bluno specific service
    Service 0000dfb0-0000-1000-8000-00805f9b34fb
    Characteristics:
    |--0000dfb1-0000-1000-8000-00805f9b34fb (READABLE, WRITABLE, WRITABLE WITHOUT RESPONSE, NOTIFIABLE)
    |--0000dfb2-0000-1000-8000-00805f9b34fb (READABLE, WRITABLE, WRITABLE WITHOUT RESPONSE, NOTIFIABLE)
---------------
 
 
### Android application

