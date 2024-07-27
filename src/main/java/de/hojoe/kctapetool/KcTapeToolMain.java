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
//    args = new String[] { "-s", "test1.wav", "--directory", "d:/tmp/kc" };
//    args = new String[] { "-l" };
//    args = new String[] { "--list" };
//    args = new String[] { "-s", "Eingang", "--wait", "30", "-d", "d:/tmp/kc/out.bla"};
    args = new String[] { "-s", "d:/tmp/kc/CAOS_E.KCC"};
      int rc = new CommandLine(new KcTapeToolCommand()).execute(args);
      System.exit(rc);
  }

}
