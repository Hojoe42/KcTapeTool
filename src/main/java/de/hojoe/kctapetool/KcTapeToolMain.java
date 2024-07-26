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
//    args = new String[] { "--help" };
//    args = new String[] { "-V" };
//    args = new String[] { "--version" };
//    args = new String[] { "-s", "Eingang" };
//    args = new String[] { "-s", "test.wav" };
//    args = new String[] { "-l" };
//    args = new String[] { "--list" };
//    args = new String[] { "-s", "Eingang", "--wait", "60", "-d", "-o", "kcc" };
      int rc = new CommandLine(new KcTapeToolCommand()).execute(args);
      System.exit(rc);
  }

}
