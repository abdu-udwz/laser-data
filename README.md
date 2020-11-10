# laser-data

A small communication device software 

Implementing of point-to-point digital communication using laser beam. 
A *transeiver* built using Arudino Uno R3 is supposed to function as *network card* for a computer using USB connection between the Arudino board and a computer 


all project source code lives within `packagjes` folder

#### Content of `packages` folder
- **/arduino** \
    the arudino device firmware source code. simply consits of two "units" a **transmitter** and **receiver** to control the laser-emitting bin on the board, and the analog pin reading the laser signal.
- **/serial-terminal** \
   A java app which represents a layer above the hardware, it controls the devices over USB serial connection.
- **/messenger** \
   A web application and its server built to utilize the devices as communication meduim. the server doesn't directly control the device it *dirctes* the java app. the java app then handles the rest sending commands to the hardware

