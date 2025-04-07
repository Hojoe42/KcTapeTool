package de.hojoe.kctapetool.analyzer;

/**
 * Fehler beim Analysieren einer KC Wave Datei / -Stream.
 *
 *
 * @author Holger Jödicke
 */
public class WaveAnalyzerException extends Exception
{

  /**
   * Erzeugt die Exception mit der übergebenen Meldung.
   */
  public WaveAnalyzerException(String message)
  {
    super(message);
  }

}
