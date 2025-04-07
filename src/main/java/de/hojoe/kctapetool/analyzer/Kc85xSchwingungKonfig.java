package de.hojoe.kctapetool.analyzer;

public class Kc85xSchwingungKonfig implements SchwingungKonfig
{

  private static final int TRENN_FREQUENZ = 557;
  private static final int EINS_FREQUENZ = 1050;
  private static final int NULL_FREQUENZ = 1950;

  private BitKonfig einsBit = new BitKonfigImpl(EINS_FREQUENZ);
  private BitKonfig nullBit = new BitKonfigImpl(NULL_FREQUENZ);
  private BitKonfig trennBit = new BitKonfigImpl(TRENN_FREQUENZ);

  @Override
  public BitKonfig getEinsBit()
  {
    return einsBit;
  }

  @Override
  public BitKonfig getNullBit()
  {
    return nullBit;
  }

  @Override
  public BitKonfig getTrennBit()
  {
    return trennBit;
  }

}
