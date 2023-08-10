package de.hojoe.kctapetool;

import java.io.*;
import java.util.List;

/**
 * Ein Konverter für Basic Programme.
 *
 * @author Holger Jödicke
 */
public class BasicConverter implements PcDateiKonverter
{
  /**
   * Länge des Haeders.
   * <p>
   * Am Anfang einer Basic Datei von einer Kassette kommen 3 Bytes die den Typ definieren (0xd3) und
   * 8 Bytes Dateiname (ohne Endung). Ab dem 12. Byte kommt das eigentliche Basic Programm.
   * <p>
   * Beim Speichern auf dem PC wird dieser Header entfernt.
   */
  private static final int HEADER_LAENGE = 11;

  @Override
  public void write(OutputStream os, KcDatei kcDatei) throws IOException
  {
    List<KcDateiBlock> bloecke = kcDatei.getBloecke();
    byte[] ersterBlock = bloecke.get(0).getDaten();
    os.write(ersterBlock, HEADER_LAENGE, ersterBlock.length - HEADER_LAENGE);
    for( int i = 1; i < bloecke.size(); i++ )
    {
      os.write(bloecke.get(i).getDaten());
    }
    // am Ende noch mit Nullen auffüllen
    for( int i = 0; i < HEADER_LAENGE; i++ )
    {
      os.write(0);
    }
  }

}
