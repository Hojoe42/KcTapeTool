package de.hojoe.kctapetool.tools;

import javax.sound.sampled.*;

/**
 * Liest die Soundgeräte aus und gibt die erreichbaren Informationen auf die Konsole aus.
 *
 * @author Holger Jödicke
 */
public class AnalyzeSoundDevices
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
      System.out.println("CD Player Eingänge:");
      for( Line.Info info : AudioSystem.getSourceLineInfo(Port.Info.COMPACT_DISC) )
      {
        System.out.println("  " + info);
      }
      System.out.println("Line Out Ausgänge:");
      for( Line.Info info : AudioSystem.getTargetLineInfo(Port.Info.LINE_OUT) )
      {
        System.out.println("  " + info);
      }
      System.out.println("Kopfhörer:");
      for( Line.Info info : AudioSystem.getTargetLineInfo(Port.Info.HEADPHONE) )
      {
        System.out.println("  " + info);
      }

      System.out.println();
      System.out.println("Mixer Infos:");
      for(Mixer.Info  mixerInfo : AudioSystem.getMixerInfo())
      {
        try( Mixer mixer = AudioSystem.getMixer(mixerInfo) )
        {
          printMixerInfos(mixerInfo, mixer);
        }
      }
    }
    catch( LineUnavailableException e )
    {
      e.printStackTrace();
    }
  }

  private static void printMixerInfos(Mixer.Info mixerInfo, Mixer mixer) throws LineUnavailableException
  {
    System.out.println("  " + mixerInfo + "  " + mixer);
    for( Line.Info targetLineInfo : mixer.getTargetLineInfo() )
    {
      System.out.println("    Target Line: " + targetLineInfo);
      try( Line line = AudioSystem.getLine(targetLineInfo); )
      {
        printLineInfos(line);
      }
    }
    for( Line.Info sourceLineInfo : mixer.getSourceLineInfo() )
    {
      System.out.println("    Source Line: " + sourceLineInfo);
      try( Line line = AudioSystem.getLine(sourceLineInfo) )
      {
        System.out.println("    Line: " + line);
        System.out.println("    !!! ???" + line);
      }
    }
  }

  private static void printLineInfos(Line line)
  {
    System.out.println("    Line: " + line);
    if( line instanceof TargetDataLine )
    {
      TargetDataLine tdl = (TargetDataLine)line;
      DataLine.Info lineInfo = (DataLine.Info)tdl.getLineInfo();
      for( AudioFormat audioFormat : lineInfo.getFormats() )
      {
        System.out.println("      " + audioFormat);
      }
    }
    else
    {
      if( line instanceof Port )
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

  /**
   * Startet die Soundgeräteanalyse.
   */
  public static void main(String[] args) throws Exception
  {
    printSoundInputs();
  }

}