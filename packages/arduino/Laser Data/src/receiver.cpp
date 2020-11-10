#include "laserdata.h"
#include  "Arduino.h"
bool receiverOn = false; // the state of the receiver

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
      delay(1000);
      receiverOn = true;
    }
    else if (cmd.equals("standby"))
    {
      receiverOn = false;
      Serial.print("alp://cevnt/info/rece/receiver_standby*&\n");
      Serial.flush();
    }
}

void receiverLoop(){
  unsigned long t = millis();
  int l = analogRead(RECEIVER_PIN);
  // char tStr[20];
  // sprintf (tStr, "%lu", t);
  // char lStr[6];
  // sprintf (lStr, "%u", l);

  Serial.print("alp://cevnt/light_reading/");
  Serial.print(t);
  Serial.print('*');
  Serial.print(l);
  Serial.print('&');
  Serial.print('\n');
  // Serial.flush();
}
