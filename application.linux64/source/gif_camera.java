import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import gifAnimation.*; 
import controlP5.*; 
import processing.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class gif_camera extends PApplet {

 //GIF ENCODER LIBRARY
 //GUI LIBRARY
 //VIDEO LIBRARY

Capture cam;
GifMaker gifExport;
ControlP5 cp5;
PFont font;
Gif playbackGif;
boolean isRecording, isWaitingToRecord, tryAgain, gifCreated;
String fName,lName;
float gifSpeed, gifFrameRate;
int gifNumFrames;
float addFrameInterval; //how many ms we must wait betweern adding frames
long timer, startWaitTime; //timers
float gifSpeedMap, gifTotalTime, gifMaxTime;
int waitTime;
int gifFrameCount;


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//           S E T U P             
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public void setup() {
  size(1100,490);
  cp5 = new ControlP5(this);
  font = createFont("arial",20);
  
  //__________________________________GUI stuff
  cp5.addTextfield("text_fName")
  .setPosition(380,70)
  .setSize(200,40)
  .setFont(font)
  .setLabelVisible(false)
  .setColor(color(250));
  cp5.addTextfield("text_lName")
  .setPosition(595,70)
  .setSize(200,40)
  .setFont(font)
  .setLabelVisible(false)
  .setColor(color(250));
  cp5.addButton("button_setName")
  .setCaptionLabel("         USE THIS NAME") //where is the centering option?
  .setPosition(810,70)
  .setSize(100,40);
  cp5.addButton("button_saveThisGif")
  .setCaptionLabel(" ----> SAVE THIS GIF <---- ") 
  .setPosition(380,360)
  .setVisible(false)
  .setSize(110,60);
  cp5.addSlider("gifSpeed")
  .setCaptionLabel("set the GIF playback speed") 
  .setPosition(380,195)
  .setSize(80,20)
  .setRange(0,10)
  .setValue(8);
  cp5.addSlider("gifFrameRate")
  .setCaptionLabel("set the GIF frame rate") 
  .setPosition(600,195)
  .setSize(80,20)
  .setRange(5,20)
  .setValue(8);
  cp5.addSlider("gifNumFrames")
  .setCaptionLabel("total GIF frames") 
  .setPosition(805,195)
  .setSize(80,20)
  .setRange(10,40)
  .setValue(20);
  
  String[] cameras = Capture.list();
  for (int i = 0; i < cameras.length; i++) {
    println(cameras[i]);
  }
  cam = new Capture(this,240,180); //EDIT THIS
  cam.start();  
  
  restart();
  gifCreated = false;
  tryAgain = false;
  lName="";
  fName="";
  waitTime = 3000;
  
}
public void restart(){
  isRecording = false;
  isWaitingToRecord = false;
  gifFrameCount = 0;
}


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  I N T E R F A C E  F U N C T I O N S            
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//____________________________ set name button was clicked
public void button_setName(){
  println("'USE THIS NAME' was clicked");
  fName = cp5.get(Textfield.class,"text_fName").getText();
  lName = cp5.get(Textfield.class,"text_lName").getText();
  println(fName+" "+lName);
}
//____________________________ save gif button clicked
public void button_saveThisGif(){
  println("'SAVE THIS GIF' was clicked");
  tryAgain = false;
  fName="";
  lName="";
  cp5.get(Textfield.class,"text_fName").setValue("");
  cp5.get(Textfield.class,"text_lName").setValue("");
  cp5.get(Button.class,"button_saveThisGif").setVisible(false);
  gifCreated = false;
  restart();
}
//____________________________ gif setting sliders were adjusted
public void gifSpeed() {
  gifSpeed = cp5.get(Slider.class,"gifSpeed").getValue();
  gifSpeedMap = map(gifSpeed,0,10,80,20);
}
//____________________________ gif setting sliders were adjusted
public void gifNumFrames() {
  gifNumFrames = PApplet.parseInt(cp5.get(Slider.class,"gifNumFrames").getValue());
}
//____________________________ gif setting sliders were adjusted
public void gifFrameRate() {
  gifFrameRate = cp5.get(Slider.class,"gifFrameRate").getValue();
  addFrameInterval = map(gifFrameRate,5,20,120,33);
}




//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//           D R A W             
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

public void draw() {

  background(0);
  
  //------------------------ first name, last name
  fill(150);
  textFont(font,22);
  text("First Name",380,60);
  text("Last Name",595,60);

  //------------------------ gif settings
  fill(150);
  textFont(font,22);
  text("GIF Settings",380,180);
  
  //------------------------ record camera
  textFont(font,22);
  //text("CAMERA",0,200);
  if (cam.available() == true) {
    cam.read();
  }
  //cam.filter(GRAY); //black and white camera
  image(cam,0,0);
  
  //------------------------ your gif
  textFont(font,22);
  //text("YOUR GIF",0,480);
  noFill();
  stroke(150);
  strokeWeight(2);
  rect(0,250,240,180);
  if(gifCreated==true){
    image(playbackGif,0,250);
  }
  
  //------------------------ reocord stuff
  textFont(font,26);
  fill(250);
  if((fName.length()==0 || lName.length()==0) && (tryAgain == false)){ //start screen___________________
      fill(0,220,0);
      text("Enter your name to start",380,280);  
  }else{
    
      if(tryAgain==true){ //try again___________________
          fill(0,200,0);
          text("Hit space to try again if you like, or save below",380,280);         
      }
      else if(isWaitingToRecord==true){ //waiting to record___________________
      
        if(millis()-startWaitTime < waitTime){ //waiting...___________________
          fill(250);
          gifCreated = false;
          text("Start recording in..."+ PApplet.parseInt( 4.0f - ((millis()-startWaitTime)/1000.0f) ),380,280);      
        }else{ //creating gif___________________
          gifExport = new GifMaker(this, "gifs/"+fName+"_"+lName+".gif");
          gifExport.setSize(240,180); //EDIT THIS
          gifExport.setRepeat(0);
          //gifExport.setQuality(10); //this quality doesnt make any sense...?
          isWaitingToRecord = false;
          isRecording = true; 
        }
        
      }
      else if(isRecording==true){ //recording___________________
        
        fill(255,0,0);
        text("REC",380,280);
        noFill();      
        stroke(255,0,0);
        strokeWeight(1);          
        rect(480,255,300,25);
        fill(255,0,0);
        rect(480,255,(gifFrameCount/PApplet.parseFloat(gifNumFrames))*300,25);
          
        if(millis()-timer > addFrameInterval){ //add frames___________________
          timer = millis(); //update the timer
          gifExport.setDelay((int)gifSpeedMap);
          gifExport.addFrame();    
          gifTotalTime += gifSpeedMap;
          gifFrameCount++;
          println("...added frame "+ gifFrameCount+"/"+gifNumFrames);

          if(gifFrameCount > gifNumFrames){ //gif is done___________________
            gifExport.finish();
            println("exporting gif: "+fName+"_"+lName+".gif");
            delay(500);
            playbackGif = new Gif(this, "gifs/"+fName+"_"+lName+".gif");
            playbackGif.loop();
            gifCreated = true;
            cp5.get(Button.class,"button_saveThisGif").setVisible(true);
            tryAgain = true;
            restart();
          }
        }
      }
      else{ //hit space to record___________________
        fill(0,200,0);
        text("Hit 'Space' To Record You File, '"+fName+"_"+lName+".gif'",380,280);  
      }
  }

}




//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//     K E Y  P R E S S E D            
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public void keyPressed() {
  if (key==' ' && fName.length()>0 && lName.length()>0 && isRecording!=true) {
    isWaitingToRecord = true;
    startWaitTime = millis();
    cp5.get(Button.class,"button_saveThisGif").setVisible(false);
    tryAgain = false;
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "gif_camera" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
