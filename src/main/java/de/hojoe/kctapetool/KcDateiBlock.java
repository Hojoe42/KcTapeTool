package de.hojoe.kctapetool;

/* package */ class KcDateiBlock
{
  /** gelesene Blocknummer, 1 bis, letzter Block FF */
  private int blocknummer;
  /** die gelesene Checksumme */
  private int checksumme;
  private byte[] daten;

  public KcDateiBlock(int blocknummer, int checksumme, byte[] blockDaten)
  {
    this.blocknummer = blocknummer;
    this.checksumme = checksumme;
    if( blockDaten.length != 128 )
    {
      throw new IllegalArgumentException("Die Block Daten müssen genau 128 Byte lang sein: " + blockDaten.length);
    }
    daten = blockDaten;
  }

  public int getBlocknummer()
  {
    return blocknummer;
  }

  public int getChecksumme()
  {
    return 0xff & checksumme;
  }

  public byte[] getDaten()
  {
    return daten;
  }

  public int berechneChecksumme()
  {
    int summe = 0;
    for( byte element : daten )
    {
      summe += element;
    }
    return 0xff & summe;
  }

}