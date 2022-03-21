/*
A buffer-based implementation of the receiver handling code. It is an attempt to
improve perf
 */
#include "laserdata.h"
#include <string.h>
#include <stdlib.h>

#define READING_BUFF_SIZE 10

bool receiverOn = false; // the state of the receiver

unsigned int **readingBuffer = new unsigned int *[READING_BUFF_SIZE];
// "send" in the context of this file
// refers to the act of writing to the serial port
bool sendReadings = false;
int readingIndex = -1;
int sendingIndex = -1;

void proceedToNextReading();
int nextSendingIndex();

void initReceiver()
{
  for (int i = 0; i < READING_BUFF_SIZE; i++)
  {
    readingBuffer[i] = new unsigned int[2]; // Initialize all elements to zero.
  }
}

void receiverMessage(String message)
{
  // message syntax: [cmd]*[val]&
  // cmd:
  //     standby
  //     receive

  // val: ...
  int cmdTerminator = message.indexOf('*');
  int valTerminator = message.indexOf('&');

  String cmd = message.substring(0, cmdTerminator);
  String val = message.substring(cmdTerminator + 1, valTerminator);

  if (cmd.equals("receive"))
  {
    Serial.print("alp://cevnt/info/rece/receiver_on*&\n");
    Serial.flush();
    receiverOn = true;
    proceedToNextReading();
  }
  else if (cmd.equals("standby"))
  {
    readingIndex = -1;
    sendingIndex = -1;
    sendReadings = false;
    receiverOn = false;

    Serial.print("alp://cevnt/info/rece/receiver_standby*&\n");
    Serial.flush();
    initReceiver();
  }
}

void receiverLoop()
{
  // don't store reading if sending hasn't finished yet
  if (readingIndex != sendingIndex)
  {
    if (readingIndex == READING_BUFF_SIZE - 1 && !sendReadings)
      sendReadings = true;

    unsigned long t = millis();
    int l = analogRead(RECEIVER_PIN);

    readingBuffer[readingIndex][0] = t;
    readingBuffer[readingIndex][1] = l;
    proceedToNextReading();
  }

  if (sendReadings)
  {
    // proce/ed to next sending index
    sendingIndex = nextSendingIndex();
    Serial.print("alp://cevnt/light_reading/");
    Serial.print(readingBuffer[sendingIndex][0]);
    Serial.print('*');
    Serial.print(readingBuffer[sendingIndex][1]);
    Serial.print('&');
    Serial.print('\n');
    Serial.flush();
  }
}

void proceedToNextReading()
{
  readingIndex++;
  if (readingIndex == READING_BUFF_SIZE)
  {
    readingIndex = 0;
  }
}

int nextSendingIndex()
{
  int nextIndex = ++sendingIndex;
  if (nextIndex == READING_BUFF_SIZE)
    nextIndex = 0;
  return nextIndex;
}