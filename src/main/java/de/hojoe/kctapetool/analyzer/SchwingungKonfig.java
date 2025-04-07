package de.hojoe.kctapetool.analyzer;

/**
 * Stellt die Konfiguration für die verschiedenen Symbole (Eins, Null, Trennzeichen) zur Verfügung.
 *
 *
 * @author Holger Jödicke
 */
public interface SchwingungKonfig
{

  /** CD Samplerate */
  public static int CD_ABTASTRATE = 44100;


  /**
   * Liefert die {@link BitKonfig} für das Eins Symbol.
   */
  public BitKonfig getEinsBit();

  /**
   * Liefert die {@link BitKonfig} für das Null Symbol.
   */
  public BitKonfig getNullBit();

  /**
   * Liefert die {@link BitKonfig} für das Trennzeichen.
   */
  public BitKonfig getTrennBit();

}
