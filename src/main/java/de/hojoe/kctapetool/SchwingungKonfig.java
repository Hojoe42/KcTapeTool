package de.hojoe.kctapetool;

public class SchwingungKonfig
{

  // CD Samplerate
  private int ABTASTRATE = 44100;

  private static final int TRENN_FREQUENZ = 557;
  private static final int EINS_FREQUENZ = 1050;
  private static final int NULL_FREQUENZ = 1950;

  private BitKonfig einsBit = new BitKonfigImpl(EINS_FREQUENZ);
  private BitKonfig nullBit = new BitKonfigImpl(NULL_FREQUENZ);
  private BitKonfig trennBit = new BitKonfigImpl(TRENN_FREQUENZ);

  public BitKonfig getEinsBit()
  {
    return einsBit;
  }

  public BitKonfig getNullBit()
  {
    return nullBit;
  }

  public BitKonfig getTrennBit()
  {
    return trennBit;
  }

  interface BitKonfig
  {
    /** Liefert die Schwingungs-Frequenz dieses Bits */
    int getFrequenz();

    /** Anzahl der Frames bei einer Abtastrate von 44100 */
    int getLaenge();

    int getMaxLaenge();

    int getMinLaenge();

  }

  class BitKonfigImpl implements BitKonfig
  {

    private int frequenz;

    public BitKonfigImpl(int frequenz)
    {
      this.frequenz = frequenz;
    }

    @Override
    public int getFrequenz()
    {
      return frequenz;
    }

    @Override
    public int getLaenge()
    {
      return (int)Math.round(ABTASTRATE / (double)frequenz);
    }

    @Override
    public int getMaxLaenge()
    {
      return (int)Math.round(getLaenge() * 1.1);
    }

    @Override
    public int getMinLaenge()
    {
      return (int)Math.round(getLaenge() * 0.9);
    }

    @Override
    public String toString()
    {
      return "BitKonfigImpl [frequenz=" + frequenz +
        ", getLaenge()=" + getLaenge() +
        ", getMaxLaenge()=" + getMaxLaenge() +
        ", getMinLaenge()=" + getMinLaenge() +
        "]";
    }



  }

}
