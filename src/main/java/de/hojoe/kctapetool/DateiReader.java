package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;
import java.util.*;


/**
 * Code zum Einlesen Daten Dateien.
 *
 * @author Holger Jödicke
 */
public class DateiReader
{

  /**
   * Lädt eine Daten Datei und liefert eine oder mehrere {@link KcDatei} zurück. In Multi TAP Dateien können zum Beispiel mehrere Dateien enthalten sein.
   */
  public List<KcDatei> load(Path path)
  {
    byte[] allBytes;
    try
    {
      allBytes = Files.readAllBytes(path);
    }
    catch( IOException e )
    {
      throw new UncheckedIOException("Fehler beim Lesen der Daten von [" + path + "].", e);
    }
    if(TapReader.isTap1Datei(allBytes))
    {
      List<KcDatei> tap1Datei = new TapReader(allBytes).loadTap1Datei();
      return tap1Datei;
    }
    return Collections.singletonList(loadKccDatei(allBytes));
  }

  private KcDatei loadKccDatei(byte[] allBytes)
  {
    int index = 0;
    KcDatei kcDatei = new KcDatei();
    int blockNr = 1;
    while( index < allBytes.length )
    {
      byte[] blockData = new byte[KcDateiBlock.BLOCK_SIZE];
      int sizeToCopy = Math.min(blockData.length, allBytes.length - index);
      System.arraycopy(allBytes, index, blockData, 0, sizeToCopy);
      index += sizeToCopy;
      if( index >= allBytes.length )
      {
        // letzter Block
        blockNr = 0xff;
      }
      kcDatei.add(new KcDateiBlock(blockNr, KcDateiBlock.berechneChecksumme(blockData), blockData));
      blockNr++;
    }
    return kcDatei;
  }

}
