package de.hojoe.kctapetool;

public class SamplePuffer
{
  private int sample;
  private int currentBit = 0;
  private int highestBit;

  public SamplePuffer(int sampleSizeInBits)
  {
    highestBit = 1 << (sampleSizeInBits-1);
  }

  /**
   * F�gt ein Byte hinzu.
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
      // das höchst Bit ist gesetzt, es muss eine negative Zahl sein -> alle oberen Bits setzen
      return sample | (highestBit *-1);
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