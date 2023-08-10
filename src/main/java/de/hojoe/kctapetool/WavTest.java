package de.hojoe.kctapetool;

import javax.sound.sampled.*;

public class WavTest
{

  private static void printSoundInputs()
  {
    try
    {
      System.out.println("Line In Audio Eingänge:");
      for( Line.Info info : AudioSystem.getSourceLineInfo(Port.Info.LINE_IN) )
      {
        System.out.println("  " + info);
      }
      System.out.println("Mikrofon Audio Eingänge:");
      for( Line.Info info : AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE) )
      {
        System.out.println("  " + info);
      }
      System.out.println("Mixer Infos:");
      for(Mixer.Info  mixerInfo : AudioSystem.getMixerInfo())
      {
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        System.out.println("  " + mixerInfo + "  " + mixer);
        for( Line.Info targetLineInfo : mixer.getTargetLineInfo() )
        {
          System.out.println("    Target Line: " + targetLineInfo);
          Line line = AudioSystem.getLine(targetLineInfo);
          System.out.println("    Line: " + line);
          if(line instanceof TargetDataLine)
          {
            TargetDataLine tdl = (TargetDataLine)line;
            DataLine.Info lineInfo = (DataLine.Info)tdl.getLineInfo();
            for( AudioFormat audioFormat : lineInfo.getFormats() )
            {
              System.out.println("      " + audioFormat);
            }
          }
          else if(line instanceof Port)
          {
            Port port = (Port)line;
            Port.Info lineInfo = (Port.Info)port.getLineInfo();
            System.out.println("    " + lineInfo.getName() + ", is Source: " + lineInfo.isSource());
          }
          else
          {
            System.out.println("    !!! ???" + line);
          }
        }
      }
    }
    catch( LineUnavailableException e )
    {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) throws Exception
  {
    printSoundInputs();
  }

}