#include "laserdata.h"

// #include "testprefs.h"

int TRANSMITTER_PIN = 0;
int RECEIVER_PIN = 0 ;
int BIT_DELAY = 0;

bool boardInitialized = false;

extern bool receiverOn;
extern bool transmitterOn;

String inputString = "";         // a string to hold incoming data
bool stringComplete = false;  // whether the string is complete

void setupMessage(String);
extern void initReceiver();

void setup() {
  
  Serial.begin(BAUD_RATE);
  initReceiver();
}

void loop() 
{  
  while (Serial.available() && !stringComplete) {
    char inChar = (char)Serial.read();

    inputString += inChar;
    if (inChar == '\n') {
      stringComplete = true;
    }
  }

  if (stringComplete) 
  {
    // Serial.print(inputString);
    // Serial.flush();

    if(inputString.startsWith("alp://"))
    { // OK a message I know
    
      bool msgRecognized = true;
      
      if (inputString.substring(6,10) == "cust"){
        
        // apl://cust/[type]/[cmd]
          // type : setp, tran, rece, devp
                          
        String type = inputString.substring(11, 15);
        String message = inputString.substring(16);
        
        if (type == "setp")
        {
          setupMessage(message);
        }
        else if(type == "tran") 
        {
          if (boardInitialized){
            transmitterMessage(message);            
          }
          else{
            Serial.print("alp://cevnt/error/not_initialized/transmitter*&");
            Serial.print('\n');
            Serial.flush();
          }
        }
        else if (type == "rece")
        {  
          if (boardInitialized){       
            receiverMessage(message);  
          }
          else
            Serial.print("alp://cevnt/error/not_initialized/receiver*&");
            Serial.print('\n');
            Serial.flush();
        }
        
      } else {
        msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
      }
    }
     // clear the string:
    inputString = "";
    stringComplete = false;   
  }

  if (transmitterOn && boardInitialized)
  {
    transmitterLoop();
  }

  if (receiverOn && boardInitialized)
  {
      receiverLoop();
  }

}

void setupMessage(String message){
    //message syntax: [cmd]*[val]&
      // cmd: trans_pin,
      //      recev_pin,
      //      bit_delay,
      //      initialize
      // val: int value
      
    int cmdTerminator = message.indexOf('*');
    int valTerminator = message.indexOf('&');
    
    String cmd = message.substring(0, cmdTerminator);
    String val = message.substring(cmdTerminator + 1, valTerminator);

    if (cmd.equals("trans_pin"))
    {
      TRANSMITTER_PIN = val.toInt();
      pinMode(TRANSMITTER_PIN, OUTPUT);
      
      Serial.print("alp://cevnt/");
      Serial.print("info/setup/");
      Serial.print("transmitter_pin*");
      Serial.print(TRANSMITTER_PIN);
      Serial.print('&');
      Serial.print('\n');
      Serial.flush();
    }
    else if (cmd.equals("recev_pin"))
    {
      byte pinNum = val.toInt();
      switch(pinNum)
      {
        case 0:
          RECEIVER_PIN = A0;
          break;
        case 1:
          RECEIVER_PIN = A1;
          break;
        case 2:
          RECEIVER_PIN = A2;
          break;
        case 3:
          RECEIVER_PIN = A3;
          break;
        case 4:
          RECEIVER_PIN = A4;
          break;
        case 5:
          RECEIVER_PIN = A5;
          break;
        default:
          RECEIVER_PIN = A0;
          break;
      }

      pinMode(RECEIVER_PIN, INPUT);

      Serial.print("alp://cevnt/");
      Serial.print("info/setup/");
      Serial.print("receiver_pin*");
      Serial.print(RECEIVER_PIN);
      Serial.print('&');
      Serial.print('\n');
      Serial.flush();
      
    }
    else if (cmd.equals("bit_delay"))
    {
      BIT_DELAY = val.toInt();
      Serial.print("alp://cevnt/info/setup/");
      Serial.print("bit_delay*");
      Serial.print(BIT_DELAY);
      Serial.print('&');
      Serial.print('\n');
      Serial.flush();
    }
    
    else if (cmd.equals("initialize"))
    {
      if (val.equals("true"))
      {
        boardInitialized = true;  
        Serial.print("alp://cevnt/info/setup/initialized*&\n");
      }
      else
      {
        boardInitialized = false;
        Serial.print("alp://cevnt/info/reset/deinitialized*&\n");
      }
      
      Serial.flush();
    }
}

/*
  SerialEvent occurs whenever a new data comes in the
 hardware serial RX.  This routine is run between each
 time loop() runs, so using delay inside loop can delay
 response.  Multiple bytes of data may be available.
 This is general code you can reuse.
 */
void serialEvent() {
}