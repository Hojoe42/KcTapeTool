package de.hojoe.kctapetool;

import java.io.*;

/**
 * Ein Konverter für CAOS / KCC Programme.
 *
 * @author Holger Jödicke
 */
public class CaosKonverter implements PcDateiKonverter
{

  @Override
  public void write(OutputStream os, KcDatei kcDatei) throws IOException
  {
    for( KcDateiBlock block : kcDatei.getBloecke() )
    {
      os.write(block.getDaten());
    }
  }

}
