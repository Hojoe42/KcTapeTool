package de.hojoe.kctapetool;

import java.nio.file.Path;

import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * Main Klasse für das KC Tape Tool.
 *
 * @author Holger Jödicke
 */
@Command(name = "KcTapeTool",
         subcommands ={  HelpCommand.class, KcReader.class, KcWriter.class},
         synopsisSubcommandLabel = "(help | read | write)",
         version = "0.1.0")
public class KcTapeTool implements Runnable
{
  /** Returncode für alles in Ordnung. */
  static final Integer RC_OK = 0;

  @Option(names = {"-V", "--version"},
          versionHelp = true,
          description = "Zeigt die Versioninfo an und beendet das Programm")
  boolean versionInfoRequested;

  @Option(names = {"--help"},
          usageHelp = true,
          description = "Zeigt diese Hilfe und beendet das Programm")
  boolean usageHelpRequested;

  @Option(names = {"-d", "--directory"},
          description = "Arbeitsverzeichnis in das Dateien geschrieben oder von dem Dateien gelesen werden. Wenn nicht angegeben, wird das aktuelle Verzeichnis verwendet",
          paramLabel = "<ein Verzeichnis>")
  private Path directory;

  @Override
  public void run()
  {
    CommandLine.usage(this, System.out);
  }

  /**
   * Startaufruf des KC Tape Tools.
   */
  public static void main(String[] args)
  {
//    args = new String[] { "-V" };
//    args = new String[] { "--help" };
//    args = new String[] { "read", "-l" };
//    args = new String[] { "write", "-l" };
//    args = new String[] { "write" };
//    args = new String[] { "write", "d:/tmp/kc/CAOS_E.KCC" };
//    args = new String[] { "read" };
//    args = new String[] { "read", "-i", "Eingang" };
//    args = new String[] { "read", "-i", "Eingang (Realtek(R) Audio)" };
//    args = new String[] { "read", "-f", "CopyStream1.wav" };
//    args = new String[] { "read", "-f", "HANOI.wav" };
//    args = new String[] { "read", "-f", "tatum.wav" };
//    args = new String[] { "read", "-f", "tmp_test.wav" };
//    args = new String[] { "help", "read" };
//    args = new String[] { "read", "-f", "d:/tmp/kc/KC-Kabel-Test-Links.wav" };
//    args = new String[] { "read", "-f", "d:/tmp/kc/basic_fff.wav" };
//    args = new String[] { "read", "-f", "d:/tmp/kc/caos.wav" };
    try
    {
      int rc = new CommandLine(new KcTapeTool()).execute(args);
      System.exit(rc);
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }

}
