package de.hojoe.kctapetool.analyzer;

/**
 * Definiert das Aussehen eines einzelnen Bits im Strom.
 *
 *
 * @author Holger Jödicke
 */
public interface BitKonfig
{
  /** Liefert die Schwingungs-Frequenz dieses Bits */
  int getFrequenz();

  /** Anzahl der Frames bei einer Abtastrate von 44100 */
  default int getLaenge()
  {
    return Math.round(SchwingungKonfig.CD_ABTASTRATE / (float)getFrequenz());
  }

  default int getMaxLaenge()
  {
    return (int)Math.round(getLaenge() * 1.2);
  }

  default int getMinLaenge()
  {
    return (int)Math.round(getLaenge() * 0.8);
  }


}
