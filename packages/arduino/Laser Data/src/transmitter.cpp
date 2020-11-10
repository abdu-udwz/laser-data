#include "laserdata.h"

String binaryBuffer[2] = {"", ""};

bool transmitterOn = false; // transmitter switch

unsigned int currentBitIndex = 0;
int currentBinaryIndex = -1;

unsigned long binaryStartTime = 0;  
unsigned long prevBinaryEndTime = 0;
unsigned long bitStartTime = 0;

String currentBinary = "";
unsigned long startTime = 0;
unsigned long endTime = 0;

bool sentNextRequest = false;

void proceedToNextBinary();
void requestBinaryString();
void finishTransmitting();
int nextBinaryIndex();
void checkBitState();

void transmitterMessage(String message)
{
  // message syntax: [cmd]*[val]&
    // cmd: transmit
    // val: binary string of 0s and 1s

    int cmdTerminator = message.indexOf('*');
    int valTerminator = message.indexOf('&');
    
    String cmd = message.substring(0, cmdTerminator);
    String val = message.substring(cmdTerminator + 1, valTerminator);
    
    if (cmd.equals("transmit"))
    {
      if (! transmitterOn)
      {
        transmitterOn = true;
      }

      binaryBuffer[nextBinaryIndex()] = val;
    }
    else if (cmd.equals("standby"))
    {
      transmitterOn = false;
      sentNextRequest = false;
      
      currentBinaryIndex = -1;
      currentBitIndex = 0;
      currentBinary = "";

      startTime = 0;
      endTime = 0;
      digitalWrite(TRANSMITTER_PIN, 0);

      Serial.print("alp://cevnt/info/reset/transmitter_standby*&\n");
      Serial.flush();
    }
}

void transmitterLoop()
{
    if (currentBinaryIndex == -1) // when first message is arrived
    {
        prevBinaryEndTime = millis();
        proceedToNextBinary();
        startTime = binaryStartTime;
    }

    if (currentBinary.equals("end"))
    {
      finishTransmitting();
      return;
    }

    checkBitState();
}

void checkBitState()
{ 
    unsigned long currentTime = millis();

    if (currentTime >= bitStartTime && 
        currentTime <= bitStartTime + BIT_DELAY -1)
    {
        // get the current bit char
        char currentBit = currentBinary.charAt(currentBitIndex);

        int state = currentBit == '1'? 1 : 0;
        digitalWrite(TRANSMITTER_PIN, state); 

        if (! sentNextRequest)
        {
          requestBinaryString();
          sentNextRequest = true;
        }
    }
    else if (currentTime >= bitStartTime + BIT_DELAY)
    {
        bitStartTime = bitStartTime + BIT_DELAY;
        currentBitIndex++;  
    }

    if (currentBitIndex == currentBinary.length())
    {
      // start time of a bit that's not real
      // the bit that comes after the last
      prevBinaryEndTime = bitStartTime;
      proceedToNextBinary();
    }
}

void proceedToNextBinary()
{
    currentBinaryIndex = nextBinaryIndex();
    currentBinary = binaryBuffer[currentBinaryIndex];
    
    currentBitIndex = 0;
    sentNextRequest = false;

    binaryStartTime = prevBinaryEndTime;
    bitStartTime = binaryStartTime;
}

void requestBinaryString()
{
    Serial.print("alp://cevnt/info/trans/");
    Serial.print("send_binary*");
    Serial.print("_bin_");
    Serial.print(currentBinary);
    Serial.print("&\n");

    // no need to flush it's expected that we got enought time..
    // Serial.flush();
}

void finishTransmitting()
{
    endTime = millis();
    transmitterOn = false;
    
    currentBinaryIndex = -1;

    digitalWrite(TRANSMITTER_PIN, 0);

    Serial.print("alp://cevnt/error/tran_finished/");
    Serial.print("start*");
    Serial.print(startTime);
    Serial.print("*end*");
    Serial.print(endTime);
    Serial.print("*diff*");
    Serial.print(endTime - startTime);
    Serial.print('&');
    Serial.print('\n');
    Serial.flush();
}

int nextBinaryIndex()
{
  if (currentBinaryIndex == -1)
    return 0;
  else if (currentBinaryIndex == 0)
    return 1;
  else
    return 0;  
}
