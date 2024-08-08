package de.hojoe.kctapetool;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Code zum lesen von TAP Dateien.
 *
 * @author Holger Jödicke
 */
public class TapReader
{

  /**
   * Header für das originale TAP Format.
   */
  public static byte[] TAP1_HEADER = "\u00c3KC-TAPE by AF. ".getBytes(StandardCharsets.ISO_8859_1);

  private int index;
  private byte[] allBytes;

  /**
   * Erzeugt einen neuen {@link TapReader} für die übergebenen Daten.
   */
  public TapReader(byte[] allBytes)
  {
    this.allBytes = allBytes;
  }

  /**
   * Liefert die {@link KcDatei}en aus den TAP Daten.
   */
  public List<KcDatei> loadTap1Datei()
  {
    ArrayList<KcDatei> list = new ArrayList<>();
    index = 0;
    while( index + TAP1_HEADER.length < allBytes.length &&
           Arrays.equals(allBytes, index, index + TAP1_HEADER.length, TAP1_HEADER, 0, TAP1_HEADER.length) )
    {
      index += 16;
      list.add(loadNext());
    }
    return list;
  }

  private KcDatei loadNext()
  {
    KcDatei kcDatei = new KcDatei();
    int blockNr = 0;
    while(blockNr != 0xff)
    {
      if(index + 1 + KcDateiBlock.BLOCK_SIZE > allBytes.length)
      {
        throw new RuntimeException("Datei zu kurz oder fehlende 'FF' Blocknummer");
      }
      blockNr = 0xff & allBytes[index];
      index++;
      byte[] blockData = new byte[KcDateiBlock.BLOCK_SIZE];
      System.arraycopy(allBytes, index, blockData, 0, KcDateiBlock.BLOCK_SIZE);
      index += KcDateiBlock.BLOCK_SIZE;
      kcDatei.add(new KcDateiBlock(blockNr, KcDateiBlock.berechneChecksumme(blockData), blockData));
    }
    return kcDatei;
  }

  /**
   * Prüft ob die übergebenen Bytes mit der {@link #TAP1_HEADER}  Kennung beginnen.
   */
  public static boolean isTap1Datei(byte[] allBytes)
  {
    if(allBytes.length < TAP1_HEADER.length)
    {
      return false;
    }
    return Arrays.equals(allBytes, 0, TAP1_HEADER.length, TAP1_HEADER, 0, TAP1_HEADER.length);
  }

}