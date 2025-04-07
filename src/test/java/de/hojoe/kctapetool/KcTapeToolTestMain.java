package de.hojoe.kctapetool;

import picocli.CommandLine;

/**
 * Klasse zum manuellem Test verschiedener Kommandozeilen Aufrufe
 *
 * @author Holger Jödicke
 */
public class KcTapeToolTestMain
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
//    args = new String[] { "-s", "Eingang", "--wait", "30", "-p", "--playback-device", "Kopf" };
//    args = new String[] { "-s", "test1.wav", "--directory", "d:/tmp/kc" };
//    args = new String[] { "-l" };
//    args = new String[] { "--list" };
//    args = new String[] { "-s", "Eingang", "--wait", "30", "-d", "d:/tmp/kc/out.bla"};
//    args = new String[] { "-s", "d:/tmp/kc/CAOS_E.KCC"};
//    args = new String[] { "-s", "d:/tmp/kc/CAOS_E.KCC", "-d", "Prim", "-p", "--playback-device", "Kopf"};
//    args = new String[] { "--source", "Kassette1A.wav", "--directory", "d:/tmp/kc/kassetten/"};
//    args = new String[] { "-s", "d:/tmp/kc/tap/AEGYPTEN_XXX.853"};
    args = new String[] { "-s", "3D-OTHEL.TAP", "-d", "out.wav", "--directory", "d:/tmp/kc/tap"}; // 3D-OTHEL.TAP enthält eine Datei
//    args = new String[] { "-s", "3D-OTHEL.TAP", "-d", "out.kcc", "--directory", "d:/tmp/kc/tap"}; // 3D-OTHEL.TAP enthält eine Datei
//    args = new String[] { "-s", "S-KROETE.855", "-d", "KROETE.kcc", "--directory", "d:/tmp/kc/tap"};  // enthält 3 Dateien
//    args = new String[] { "-s", "d:/tmp/kc/tap/DEEPSPACE.TAP", };  // Issue/1
    int rc = new CommandLine(new KcTapeToolCommand()).execute(args);
    System.exit(rc);
  }

}
