package de.hojoe.kctapetool.analyzer;

public class BitKonfigImpl implements BitKonfig
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

}