package de.hojoe.kctapetool;

import java.io.IOException;

public class KcKassettenReader
{

  private WaveAnalyzer kcTape;
  private boolean blockAnfangGefunden = false;

  public KcKassettenReader(WaveAnalyzer kcTape)
  {
    this.kcTape = kcTape;
  }

  public KcDatei leseDatei() throws IOException
  {
    KcDatei kcDatei = new KcDatei();
    while( true )
    {
      try
      {
        if( !sucheBlockAnfang() )
        {
          System.out.println("Kein Block / Programmanfang gefunden");
          return null;
        }
        kcTape.leseTrennzeichen();
        int blocknummer = kcTape.leseDatenByte(false);
        byte[] bytes = new byte[128];
        for( int i = 0; i < 128; i++ )
        {
          int datenbyte = kcTape.leseDatenByte(false);
          bytes[i] = (byte)datenbyte;
        }
        int checksumme = kcTape.leseDatenByte(true);
        KcDateiBlock block = new KcDateiBlock(blocknummer, checksumme, bytes);
        kcDatei.add(block);
        if( block.getBlocknummer() == 1 )
        {
          System.out.println("Name: " + kcDatei.getDateiname());
        }
        System.out.printf("%02X", blocknummer);
        if( KcDateiBlock.berechneChecksumme(block.getDaten()) == block.getChecksumme() )
        {
          System.out.print("> ");
        }
        else
        {
          System.out.print("? ");
          System.out.println("\nChecksumme gelesen: " + Integer.toHexString(block.getChecksumme()) +
                                         " berechnet: " + Integer.toHexString((KcDateiBlock.berechneChecksumme(block.getDaten()))));
        }
        if( blocknummer == 255 )
        {
          System.out.println();
          break;
        }
      }
      catch( WaveAnalyzerException e )
      {
        System.out.println("Fehler beim Analysieren der Wave Daten: " + e.getMessage());
      }
    }
    return kcDatei;
  }

  private boolean sucheBlockAnfang() throws IOException
  {
    while( true )
    {
      int anzahlEinsSchwingungen = 0;
      if( !kcTape.sucheEinsSchwingung() )
      {
        return false;
      }
      long frame = kcTape.getFramePos();
      while( kcTape.leseEinsSchwingung() )
      {
        anzahlEinsSchwingungen++;
        if( !kcTape.isEinsSchwingung() )
        {
          break;
        }
      }
      if( anzahlEinsSchwingungen > 20 )
      {
        blockAnfangGefunden = true;
        frame = kcTape.getFramePos();
        // könnte ein Blockanfang sein?, dann muss ein Trennzeichen folgen
        if( kcTape.sucheTrennzeichenNachVorton() )
        {
//          System.out.printf("Blockanfang gefunden: bei Sekunde %d und %d Frames%n", frame / 44100, frame % 44100);
          return true;
        }
        System.out.printf("SYNC verloren bei Sekunde %d und %d Frames, Anzahl EINS Schwingungen: %d%n", frame / 44100, frame % 44100, anzahlEinsSchwingungen);
      }
    }
  }

  /**
   * Liefert <code>true</code> wenn ein
   */
  public boolean isAnfangGefunden()
  {
    return blockAnfangGefunden;
  }

}
