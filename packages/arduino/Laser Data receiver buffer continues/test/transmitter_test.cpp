#include "Arduino.h"
#include "testprefs.h"

const int sampleSize = 20;

const String binaryStrings[sampleSize] = {
                                  "11011000",
                                  "10110001",
                                  "00101010",
                                  "11011000",
                                  "10110011",
                                  "11011001",
                                  "10000101",
                                  "11011000",
                                  "10110001",
                                  "11011000",
                                  "10101101",
                                  "11011000",
                                  "10101000",
                                  "11011000",
                                  "10100111",
                                  "11011000",
                                  "10110001",
                                  "00101010",
                                  "11011000",
                                  "10110011",
                                  };


long binaryStartTime = 0;   // the start time of currently binary
                            // buffer being transmitted

bool transmitting = false;
String currentBinary = "";
String nextBinary = "";

unsigned int currentBitIndex = 0;
unsigned int currentBinaryIndex = 0;

void transmitterLoop();

long startTime = 0;
long endTime = 0;

void test_transmitter()
{
    if (currentBinaryIndex == sampleSize)
    {
        endTime = millis();
        currentBinaryIndex = 0;
        currentBinary = binaryStrings[currentBinaryIndex];
        nextBinary = binaryStrings[currentBinaryIndex + 1]; 
        transmitting = false;
        binaryStartTime = 0;

        Serial.print("======== start: ");
        Serial.print(startTime);
        Serial.print(" ======= end: ");
        Serial.print(endTime);
        Serial.print(" ======= diff: ");
        Serial.print(endTime - startTime);
        Serial.print('\n');
        Serial.flush();
    }

    if (transmitting){      

      if (binaryStartTime == 0){
        binaryStartTime = millis();
        startTime = binaryStartTime;
      }
           
      if (currentBitIndex == currentBinary.length()){
        
        currentBitIndex = 0;        
        currentBinary = nextBinary;
        
        currentBinaryIndex++;
        nextBinary = binaryStrings[currentBinaryIndex];         
        binaryStartTime = millis();
      }

      transmitterLoop();
    }
    else{
      delay(TEST_DELAY);
      transmitting = true;
    }
}

void transmitterLoop()
{
    long currentTime = millis();
    long nextBitTime = binaryStartTime + (currentBitIndex * BIT_DELAY) + BIT_DELAY;

    if (currentTime >= nextBitTime && currentTime <= nextBitTime + 1)
    {
        // Serial.print("alp://cevnt/info/trans_loop_");
        // Serial.print(currentTime);
        // Serial.print("__");
        // Serial.print(nextBitTime);
        // Serial.print('\n');
        // Serial.flush();

        char currentBit = currentBinary[currentBitIndex];
        boolean state = currentBit == '1'? HIGH : LOW;
        digitalWrite(TRANSMITTER_PIN, state);  
        currentBitIndex++;
    }
}