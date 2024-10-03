package de.hojoe.kctapetool;

import java.io.IOException;

/**
 * Schnittstelle zum Analysieren der Schwingungen um die Logischen Einsen und Nullen zu finden.
 *
 *
 * @author Holger Jödicke
 */
public interface WaveAnalyzer extends AutoCloseable
{

  /**
   * Sucht eine EINS Schwingung. Die Frameposition steht am Anfang dieser gefundenen EINS.
   *
   * @return <code>true</code>, wenn eine EINS Schwingung gefunden wurde, <code>false</code> beim
   *         Datei- /Streamende.
   */
  boolean sucheEinsSchwingung() throws IOException;

  /**
   * Liest die aktuelle EINS Schwingung.
   *
   * @return die Nummer des folgenden Frames oder -1 wenn es keine Daten mehr gibt.
   */
  boolean leseEinsSchwingung();

  /**
   * Sucht das Trennzeichen nach dem Vorton. Die Synchronisation mit den 1 Bits des Vortons kann um
   * eine halbe Periode versetzt sein, dann kommt nicht direkt das Trennzeichen, sondern noch eine
   * halbe EINS Schwingung vor dem Trennzeichen.
   *
   * @return <code>true</code> wenn ein Trennzeichen gefunden wurde
   */
  boolean sucheTrennzeichenNachVorton() throws IOException;

  /**
   * Liest das aktuelle Trennzeichen.
   *
   * @return true wenn das Trennzeichen gelesen wurde, <code>false</code>, fall es kein Trennzeichen
   *         an der aktuellen Position gibt.
   */
  boolean leseTrennzeichen();

  /**
   * Prüft ob an der aktuellen Position eine EINS Schwingung vorliegt. Die Position wird nicht verändert.
   */
  boolean isEinsSchwingung() throws IOException;

  /**
   * Prüft ob an der aktuellen Position eine Trennzeichen vorliegt. Die Position wird nicht verändert.
   */
  boolean isTrennzeichen() throws IOException;

  /**
   * Liest das nächste Datenbyte ein.
   *
   * @param letztes es handelt sich um das letzte Byte, die Checksumme, eine Blocks
   */
  int leseDatenByte(boolean letztes) throws IOException, WaveAnalyzerException;

  /**
   * Liefert die Nummer des nächsten Frames. Wurde noch nichts gelesen, dann wird 0 zurück geliefert.
   */
  long getFramePos();

  @Override
  void close() throws IOException;

}