package de.hojoe.kctapetool;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.jar.*;

import picocli.CommandLine.*;

/**
 * PicoCLI Parameter für das {@link KcTapeTool}.
 *
 * @author Holger Jödicke
 */
@Command(name = "KcTapeTool", versionProvider = KcTapeToolCommand.ManifestVersionProvider.class)
public class KcTapeToolCommand implements Callable<Integer>
{
  @Option(names = { "-V", "--version" }, versionHelp = true, description = "Zeigt die Versioninfo an und beendet das Programm")
  private boolean versionInfoRequested;

  @Option(names = { "-h", "--help" }, usageHelp = true, description = "Zeigt diese Hilfe und beendet das Programm")
  private boolean usageHelpRequested;

  @Option(names = { "-l", "--list" }, description = "Gibt die Sound Eingabe- und Ausgabekanäle aus.")
  private boolean list;

  @Option(names = { "--directory" },
    description = "Arbeitsverzeichnis in das Dateien geschrieben oder von dem Dateien gelesen werden. Wenn nicht angegeben, wird das aktuelle Verzeichnis verwendet",
    paramLabel = "<Verzeichnis>")
  private Path directory;

  @Option(names = { "-s", "--source"},
    description = "Die Quelle, daraus wird gelesen. Kann eine Datei oder ein Sound Eingang sein.",
    paramLabel = "<Datei | Soundeingang>")
  private String source;

  @Option(names = { "-d", "--destination" },
    description = "Das Ausgabe Ziel zu dem geschrieben wird. Kann eine Datei oder ein Sound Ausgang sein. Kann entfallen, wenn sich der Dateiname aus den gelesenen Daten ergibt",
    paramLabel = "<Datei | Soundausgang>")
  private String destination;

  @Option(names = { "--wait" }, defaultValue = "60", description = "Definiert die Dauer der Wartezeit beim Lesen von einem Soundeingang in Sekunden.")
  private int timeout;

  @Option(names = {"-p", "--playback"}, description = "Parallele Wiedergabe der Sound Daten zum mithören-")
  private boolean playback;

  @Option(names = { "--playback-device" },
    description = "Der Name des Wiedergabe Gerätes zum parallelem mithören der Sound Daten.",
    paramLabel = "<Soundausgang>")
  private String playbackDevice;

  @Override
  public Integer call() throws Exception
  {
    KcTapeTool kcTapeTool = new KcTapeTool(this);
    return kcTapeTool.execute();
  }

  /**
   * @return the versionInfoRequested
   */
  public boolean isVersionInfoRequested()
  {
    return versionInfoRequested;
  }

  /**
   * @param versionInfoRequested the versionInfoRequested to set
   */
  public void setVersionInfoRequested(boolean versionInfoRequested)
  {
    this.versionInfoRequested = versionInfoRequested;
  }

  /**
   * @return the usageHelpRequested
   */
  public boolean isUsageHelpRequested()
  {
    return usageHelpRequested;
  }

  /**
   * @param usageHelpRequested the usageHelpRequested to set
   */
  public void setUsageHelpRequested(boolean usageHelpRequested)
  {
    this.usageHelpRequested = usageHelpRequested;
  }

  /**
   * @return the list
   */
  public boolean isList()
  {
    return list;
  }

  /**
   * @param list the list to set
   */
  public void setList(boolean list)
  {
    this.list = list;
  }

  /**
   * @return the directory
   */
  public Path getDirectory()
  {
    return directory;
  }

  /**
   * @param directory the directory to set
   */
  public void setDirectory(Path directory)
  {
    this.directory = directory;
  }

  /**
   * @return the source
   */
  public String getSource()
  {
    return source;
  }

  /**
   * @param source the source to set
   */
  public void setSource(String source)
  {
    this.source = source;
  }

  /**
   * @return the destination
   */
  public String getDestination()
  {
    return destination;
  }

  /**
   * @param destination the destination to set
   */
  public void setDestination(String destination)
  {
    this.destination = destination;
  }

  /**
   * @return the timeout
   */
  public int getTimeout()
  {
    return timeout;
  }

  /**
   * @param timeout the timeout to set
   */
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  /**
   * @return the playback
   */
  public boolean isPlayback()
  {
    return playback;
  }

  /**
   * @param playback the playback to set
   */
  public void setPlayback(boolean playback)
  {
    this.playback = playback;
  }

  /**
   * @return the playbackDevice
   */
  public String getPlaybackDevice()
  {
    return playbackDevice;
  }

  /**
   * @param playbackDevice the playbackDevice to set
   */
  public void setPlaybackDevice(String playbackDevice)
  {
    this.playbackDevice = playbackDevice;
  }

  /**
   * Fügt den übergebenen {@link Path} an ein eventuell vorhandenes default Directory an. Wurde kein directory angegeben, dann wird der übergebenen
   * {@link Path} unverändert zurück gegeben.
   */
  public Path appendToDirectory(Path subPath)
  {
    if(directory != null)
    {
      return directory.resolve(subPath);
    }
    return subPath;
  }

  /**
   * Idee aus der picocli Doku übernommen: https://github.com/remkop/picocli/blob/main/picocli-examples/src/main/java/picocli/examples/VersionProviderDemo2.java
   */
  static class ManifestVersionProvider implements IVersionProvider
  {
    private static final String APP_NAME = "KC Tape Tool";
    private static final String ATT_IMPLEMENTATION_TITLE = "Implementation-Title";
    private static final String ATT_IMPLEMENTATION_VERSION = "Implementation-Version";

    @Override
    public String[] getVersion() throws Exception
    {
      Manifest manifest = loadManifest();
      if( manifest == null )
      {
        return new String[] {"unbekannt"};
      }
      Attributes attr = manifest.getMainAttributes();
      return new String[] { attr.getValue(ATT_IMPLEMENTATION_VERSION) };
    }

    private Manifest loadManifest() throws IOException
    {
      Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
      while( resources.hasMoreElements() )
      {
        URL url = resources.nextElement();
        try (InputStream is = url.openStream())
        {
          Manifest manifest = new Manifest(is);
          Attributes attributes = manifest.getMainAttributes();
          if( APP_NAME.equals(get(attributes, ATT_IMPLEMENTATION_TITLE)) )
          {
            return manifest;
          }
        }
      }
      return null;
    }

    private static Object get(Attributes attributes, String key)
    {
      return attributes.get(new Attributes.Name(key));
    }
  }
}
