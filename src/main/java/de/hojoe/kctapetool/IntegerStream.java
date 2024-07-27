package de.hojoe.kctapetool;

import java.io.*;

/**
 * Ein Input Stream zum Lesen von Integer Werten und nicht nur Bytes.
 *
 * @author Holger Jödicke
 */
public interface IntegerStream extends Closeable
{

  /**
   * Liefert <code>true</code> wenn noch Daten zum Lesen vorhanden sind.
   */
  boolean available();

  /**
   * Liefert die Daten des 1. Kanals als vorzeichenbehafteten Integer.
   */
  int read() throws IOException;

  /**
   * Schließt den Stream.
   */
  @Override
  void close() throws IOException;

}