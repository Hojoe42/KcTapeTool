package de.hojoe.kctapetool;

import picocli.CommandLine;

/**
 * Main Klasse für das KC Tape Tool.
 *
 * @author Holger Jödicke
 */
public class KcTapeToolMain
{

  /**
   * Startaufruf des KC Tape Tools.
   */
  public static void main(String[] args)
  {
      int rc = new CommandLine(new KcTapeToolCommand()).execute(args);
      System.exit(rc);
  }

}
