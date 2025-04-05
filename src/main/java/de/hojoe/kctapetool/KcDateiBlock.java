package de.hojoe.kctapetool;

import java.util.Arrays;

/* package */ class KcDateiBlock
{
  /** Größe eines Blocks auf der Kassette. */
  public static final int BLOCK_SIZE = 128;

  /** gelesene Blocknummer, 1 bis, letzter Block FF */
  private int blocknummer;
  /** die gelesene Checksumme */
  private int checksumme;
  private byte[] daten;

  /**
   * Erzeugt einen neuen {@link KcDateiBlock}.
   *
   * @param blocknummer die Blocknummer, 1-indiziert
   * @param checksumme die Checksumme der Nutzdaten
   * @param blockDaten die eigentlichen Nutzdaten
   * @see #berechneChecksumme(byte[])
   */
  public KcDateiBlock(int blocknummer, int checksumme, byte[] blockDaten)
  {
    this.blocknummer = blocknummer;
    this.checksumme = checksumme;
    if( blockDaten.length != BLOCK_SIZE )
    {
      throw new IllegalArgumentException("Die Block Daten müssen genau "+ BLOCK_SIZE +" Bytes lang sein: " + blockDaten.length);
    }
    daten = blockDaten;
  }

  /**
   * Liefert die Blocknummer 1-indiziert.
   */
  public int getBlocknummer()
  {
    return blocknummer;
  }

  /**
   * Liefert die Checksummer. Sie liegt im Bereich von 0-255.
   */
  public int getChecksumme()
  {
    return 0xff & checksumme;
  }

  public byte[] getDaten()
  {
    return daten;
  }

  public static int berechneChecksumme(byte[] daten)
  {
    int summe = 0;
    for( byte element : daten )
    {
      summe += element;
    }
    return 0xff & summe;
  }

  @Override
  public String toString()
  {
    return "KcDateiBlock [blocknummer=" + blocknummer + ", checksumme=" + Integer.toHexString(checksumme) + "H, daten=" + Arrays.toString(daten) + "]";
  }



}