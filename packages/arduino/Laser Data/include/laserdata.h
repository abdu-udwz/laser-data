#ifndef LASER_H
#define LASER_H

#include "Arduino.h"
#include "prefs.h"
#include "ResponsiveAnalogRead.h"

void receiverMessage(String);
void receiverLoop();

void transmitterMessage(String);
void transmitterLoop();

extern int TRANSMITTER_PIN;
extern int RECEIVER_PIN;
extern int BIT_DELAY;

#endif
