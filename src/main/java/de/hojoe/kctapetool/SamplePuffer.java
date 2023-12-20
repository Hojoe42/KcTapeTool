package de.hojoe.kctapetool;

/**
 * Sammelt die Daten / Bytes für ein Audio Sample bis maximal 32 Bit auf.
 *
 *
 * @author Holger Jödicke
 */
public class SamplePuffer
{
  private int sample;
  private int currentBit = 0;
  private int highestBit;

  /**
   * Erzeugt den Puffer mit einer bestimmten Anzahl von Bits pro Sample.
   *
   * @param sampleSizeInBits Anzahl der relevanten Bits
   */
  public SamplePuffer(int sampleSizeInBits)
  {
    highestBit = 1 << (sampleSizeInBits-1);
  }

  /**
   * Fügt ein Byte hinzu.
   */
  public void add(byte b)
  {
    sample |= (b & 0xff) << currentBit;
    currentBit += 8;
  }

  /**
   * Liefert das Sample.
   */
  public int getSample()
  {
    if((sample & highestBit) == highestBit)
    {
      // das höchst Bit im Sample ist gesetzt, es muss eine negative Zahl sein -> alle weiteren oberen Bits setzen
      return sample | (highestBit * -1);
    }
    return sample;
  }

  /**
   * Leert den Puffer.
   */
  public void reset()
  {
    sample = 0;
    currentBit = 0;
  }

}