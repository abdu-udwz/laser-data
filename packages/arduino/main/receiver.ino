#include "prefs.h"

boolean receiverOn = false; // the state of the receiver

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
      receiverOn = true;
    }
    else if (cmd.equals("standby"))
    {
      receiverOn = false;
    }
}

void receiverLoop(){
  long t = millis();
  int l = analogRead(RECEIVER_PIN);
  
  Serial.print("alp://cevnt/light_read/");
  Serial.print(t);
  Serial.print('*');
  Serial.print(l);
  Serial.print('&');
  Serial.print('\n');
  Serial.flush();
}
