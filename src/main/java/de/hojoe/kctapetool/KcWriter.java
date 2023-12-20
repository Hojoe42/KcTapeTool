package de.hojoe.kctapetool;

import static java.lang.Math.min;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;

import picocli.CommandLine.*;

/**
 * Implementation des 'write' Kommandos.
 *
 * <p>
 * Beispielaufrufe:
 * <pre>
   write -l
   write -o Primärer
   write Beispiel.KCC
 * <pre>
 *
 * @author Holger Jödicke
 */
@Command(name = "write", description = "Schreibt Dateien zum KC !!!nicht implementiert!!!")
public class KcWriter implements Callable<Integer>
{

  private static final int MAX_FILE_SIZE = 64 * 1024;

  private static final String OUTPUT_DESTINATION_DESC = "Name der Input Source, kann abgekürzt werden, solange es eindeutig ist. Siehe --list-sources. " +
    "Wenn nichts angegeben ist, dann wird die erste gefundene Quelle verwendet. Wird ignoriert wenn aus einer Wave Datei gelesen wird.";

@Option(names = {"-l", "--list-destinations"}, description = "Liste aller Sound Ausgänge ausgeben und beenden des Programms")
private boolean listDestinations;

@Option(names = {"-f", "--file"},
        description = "schreibt die WAV Daten in die angegebene Datei.",
        paramLabel = "<WAV-Datei>")
private Path wavFile;

@Option(names = {"-o", "--output-destination"},
        description = OUTPUT_DESTINATION_DESC,
        paramLabel = "<Output Ziel>", defaultValue = "")
private String outputDestinationName;

@Parameters(paramLabel = "KC-FILE", arity = "0", description = "Die KC Datei welche zum KC übertragen oder als WAV Datei gespeichert werden soll.")
private Path kcFile;

  @Override
  public Integer call() throws Exception
  {
    if( listDestinations )
    {
      listOutputGeraete();
      return KcTapeTool.RC_OK;
    }
    if(kcFile == null)
    {
      System.out.println("keine KC-Datei angeben");
      return -1;
    }
    if(Files.size(kcFile) > MAX_FILE_SIZE)
    {
      System.out.println("Die Datei [" + kcFile.toRealPath() + "] ist gößer als 64 KiB");
      return -1;
    }
    KcDatei kcDatei = load(kcFile);
    KcAudioInputStream kcAudioInputStream = new KcAudioInputStream(kcDatei);
    AudioInputStream ais = new AudioInputStream(kcAudioInputStream, kcAudioInputStream.getAudioFormat(), AudioSystem.NOT_SPECIFIED);
    AudioSystem.write(ais, Type.WAVE, new File("test.wav"));
    return 0;
  }

  private KcDatei load(Path path) throws IOException
  {
    byte[] allBytes = Files.readAllBytes(path);
    int index = 0;
    KcDatei kcDatei = new KcDatei();
    int blockNr = 1;
    while( index < allBytes.length )
    {
      byte[] blockData = new byte[KcDateiBlock.BLOCK_SIZE];
      int sizeToCopy = min(blockData.length, allBytes.length - index);
      System.arraycopy(allBytes, index, blockData, 0, sizeToCopy);
      index += sizeToCopy;
      if(index >= allBytes.length)
      {
        // letzter Block
        blockNr = 0xff;
      }
      kcDatei.add(new KcDateiBlock(blockNr, KcDateiBlock.berechneChecksumme(blockData), blockData));
      blockNr++;

    }
    return kcDatei;
  }

  private void listOutputGeraete()
  {
    System.out.println("Verfügbare Sound Ausgabe Geräte (* default):");
    Mixer defaultMixer = AudioGeraete.getDefaultAusgabeMixer();
    for( Mixer mixer : AudioGeraete.getAlleAusgabeGeraeteNamen() )
    {
      String prefix = Objects.equals(defaultMixer, mixer) ? "  (*) " : "      ";
      System.out.println(prefix + mixer.getMixerInfo().getName());
    }
  }

}
