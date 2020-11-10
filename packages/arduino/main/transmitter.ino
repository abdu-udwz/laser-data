#include "prefs.h"

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
      transmitBinary(val);
    }
}

void transmitBinary(String value)
{ 
//  Serial.print("alp://cevnt/info/");
//  Serial.print("st_");
//  Serial.print('*');
//  Serial.print(millis());
//  Serial.print('&');
//  Serial.print('\n');
//  Serial.flush();
  int valueLength = value.length();
  for (short i = 0; i < valueLength; ++i)
  {
    char current = value[i];
    byte state = (current == '1')? HIGH : LOW;

    digitalWrite(TRANSMITTER_PIN, state);     
    delay(BIT_DELAY);
  }
//  Serial.print("alp://cevnt/info/et_");
//  Serial.print('*');
//  Serial.print(millis());
//  Serial.print('&');
//  Serial.print('\n');
//  Serial.flush();
}
