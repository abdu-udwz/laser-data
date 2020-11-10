#include <Arduino.h>

const long BAUD_RATE = 500000;
const byte TRANSMITTER_PIN = 2;
const byte RECEIVER_PIN = A0;

void setup() {
  pinMode(TRANSMITTER_PIN, OUTPUT);
  pinMode(RECEIVER_PIN, INPUT);
  Serial.begin(BAUD_RATE);
}

void loop() 
{
  Serial.println(analogRead(RECEIVER_PIN));
  delay(10);
}
