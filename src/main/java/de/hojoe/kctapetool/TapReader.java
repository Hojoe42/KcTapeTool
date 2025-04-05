package de.hojoe.kctapetool;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Code zum lesen von TAP Dateien.
 *
 * <ul>
 *   <li>Am Anfang kommen 16 Byte mit folgendem Inhalt: C3 4B 43 2D 54 41 50 45 20 62 79 20 41 46 2E 20</li>
 *   <li>Danach folgen 129 Byte Blöcke</li>
 *   <li>Jeder Block enthält am Anfang 1 Byte mit der Blocknummer, danach folgen 128 Bytes mit den Nutzdaten.</li>
 *   <li>Die Blocknummer kann 0- oder 1-indiziert sein.</li>
 *   <li>Der letzte Block hat normalerweise die Blocknummer FFh</li>
 * </ul>
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

  /**
   * Liest die nächste Datei aus dem (Multi-) TAP Container.
   */
  private KcDatei loadNext()
  {
    KcDatei kcDatei = new KcDatei();
    int blockNr = 1;
    int geleseneBlockNr = 0;
    while(geleseneBlockNr != 0xff)
    {
      if(index + 1 + KcDateiBlock.BLOCK_SIZE > allBytes.length)
      {
        throw new RuntimeException("Datei zu kurz oder fehlende 'FF' Blocknummer");
      }
      geleseneBlockNr = 0xff & allBytes[index++];
      byte[] blockData = new byte[KcDateiBlock.BLOCK_SIZE];
      System.arraycopy(allBytes, index, blockData, 0, KcDateiBlock.BLOCK_SIZE);
      index += KcDateiBlock.BLOCK_SIZE;
      kcDatei.add(new KcDateiBlock(blockNr++, KcDateiBlock.berechneChecksumme(blockData), blockData));
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