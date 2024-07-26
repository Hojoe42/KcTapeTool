package de.hojoe.kctapetool;

import java.util.*;

import javax.sound.sampled.*;
import javax.sound.sampled.Line.Info;

/**
 * Ein paar Hilfsmethoden beim Umgang mit Audio Geräten.
 *
 * @author Holger Jödicke
 */
public class AudioGeraete
{

  /** {@link Info} Objekt für Aufnahme Geräte. */
  private static final Line.Info inputLineInfo = new Line.Info(TargetDataLine.class);

  /** {@link Info} Objekt für Abspiel Geräte. */
  private static final Line.Info outputLineInfo = new Line.Info(SourceDataLine.class);

  private AudioGeraete()
  {
  }

  /**
   * Liefert die Namen aller Sound Input Geräte / Mixer. Mit diesem Namen kann sich der passende Mixer mittels
   * <code>AudioSystem.getMixer(mixerInfo)</code> geholt werden.
   */
  public static List<Mixer> getAlleInputGeraeteNamen()
  {
    ArrayList<Mixer> alleInputMixer = new ArrayList<>();
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      @SuppressWarnings("resource")
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(inputLineInfo) )
      {
        alleInputMixer.add(mixer);
      }
    }
    return alleInputMixer;
  }

  /**
   * Liefert den Mixer der mit dem angegebenen Namen beginnt oder übereinstimmt.
   */
  public static Mixer getMixer(String inputSourceName)
  {
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      if( mixerInfo.getName().startsWith(inputSourceName) )
      {
        return AudioSystem.getMixer(mixerInfo);
      }
    }
    return null;
  }

  /**
   * Liefert die Namen aller Sound Output Geräte / Mixer. Mit diesem Namen kann sich der passende Mixer mittels
   * <code>AudioSystem.getMixer(mixerInfo)</code> geholt werden.
   */
  public static List<Mixer> getAlleAusgabeGeraeteNamen()
  {
    ArrayList<Mixer> alleOutputMixer = new ArrayList<>();
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      @SuppressWarnings("resource")
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(outputLineInfo) )
      {
        alleOutputMixer.add(mixer);
      }
    }
    return alleOutputMixer;
  }

  public static Mixer getDefaultInputMixer()
  {
    Mixer defaultMixer = AudioSystem.getMixer(null);
    if( defaultMixer != null && defaultMixer.isLineSupported(inputLineInfo) )
    {
      // wird normalerweise nicht funktionieren, da der Default Mixer meist die Standardausgabe (und nicht Eingabe) ist
      return defaultMixer;
    }
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(inputLineInfo) )
      {
        return mixer;
      }
    }
    return null;
  }

  public static Mixer getDefaultAusgabeMixer()
  {
    Mixer defaultMixer = AudioSystem.getMixer(null);
    if( defaultMixer != null && defaultMixer.isLineSupported(outputLineInfo) )
    {
      return defaultMixer;
    }
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(outputLineInfo) )
      {
        return mixer;
      }
    }
    return null;
  }

}
