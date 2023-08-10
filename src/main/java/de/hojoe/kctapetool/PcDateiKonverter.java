package de.hojoe.kctapetool;

import java.io.*;

/**
 * Bereitet die {@link KcDatei} für das Speichern in eine Datei auf dem PC auf.
 *
 * @author Holger Jödicke
 */
public interface PcDateiKonverter
{

  /**
   * Nimmt die Daten aus der {@link KcDatei} bereitet sie wenn nötig auf und schreibt sie in den
   * Output Stream.
   *
   * @param kcDatei die zu schreibende {@link KcDatei}.
   * @param os der {@link OutputStream} in den die aufbereiteten Daten geschrieben werden.
   */
  void write(OutputStream os, KcDatei kcDatei) throws IOException;

}
