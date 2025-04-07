package de.hojoe.kctapetool;

import java.io.*;
import java.util.*;

import javax.sound.sampled.AudioFormat;

import de.hojoe.kctapetool.analyzer.*;

public class KcAudioInputStream extends InputStream
{
  private final static float sampleRate = 44100;
  private final static int sampleSizeInBits = 16;
  private final static int channels = 1;
  private final static boolean signed = true;
  private final static boolean bigEndian = false;
  private final static Integer minSample = (int)Short.MIN_VALUE;
  private final static Integer maxSample = (int)Short.MAX_VALUE;

  private final SchwingungKonfig bitKonfig = new Kc85xSchwingungKonfig();
  private ArrayList<Integer> daten = new ArrayList<>();
  private int index = 0;

  public KcAudioInputStream(List<KcDatei> kcDateien)
  {
    // @todo extra Thread der einen Puffer füllt, damit nicht alle Daten vorgeneriert werden müssen
    for( KcDatei kcDatei : kcDateien )
    {
      generate(kcDatei);
    }
  }

  @Override
  public int read() throws IOException
  {
    if(index >= daten.size())
    {
      return -1;
    }
    return daten.get(index++);
  }

  private void generate(KcDatei kcDatei)
  {
    // 1 Sekunde Stille
    for( int i = 0; i < sampleRate; i++ )
    {
      writeSample(0);
    }
    // Programmstart: 4000 Einsbit Schwingungen
    for( int i = 0; i < 4000; i++ )
    {
      writeBit(bitKonfig.getEinsBit());
    }
    for( KcDateiBlock kcDateiBlock : kcDatei.getBloecke() )
    {
      writeBlock(kcDateiBlock);
    }
    for( int i = 0; i < 3; i++ )
    {
      writeBit(bitKonfig.getEinsBit());
    }
    // etwas Stille hinten dran
    for( int i = 0; i < sampleRate; i++ )
    {
      writeSample(0);
    }
  }

  private void writeBlock(KcDateiBlock kcDateiBlock)
  {
    // Vorblock
    for( int i = 0; i < 160; i++ )
    {
      writeBit(bitKonfig.getEinsBit());
    }
    // Trennbit / Trennzeichen
    writeBit(bitKonfig.getTrennBit());
    // Blocknummer
    writeByte(kcDateiBlock.getBlocknummer());
    // Daten
    for( byte b : kcDateiBlock.getDaten() )
    {
      writeByte(b);
    }
    // Checksumme
    writeByte(kcDateiBlock.getChecksumme());
  }

  private void writeByte(int aByte)
  {
    for( int i = 0; i < 8; i++ )
    {
      if((aByte & 0x01) == 0)
      {
        writeBit(bitKonfig.getNullBit());
      }
      else
      {
        writeBit(bitKonfig.getEinsBit());
      }
      aByte = aByte >> 1;
    }
    // Trennbit / Trennzeichen
    writeBit(bitKonfig.getTrennBit());
  }

  private void writeBit(BitKonfig bit)
  {
    int laenge = bit.getLaenge();
    int haelfte = laenge/2;
    for( int i = 0; i < haelfte; i++ )
    {
      writeSample(maxSample);
    }
    for( int i = 0; i < laenge - haelfte -1; i++ )
    {
      writeSample(minSample);
    }
    writeSample(0);
  }

  private void writeSample(int sample)
  {
    // 16 Bit Werte, little endian
    daten.add((sample & 0xff));
    daten.add((sample & 0xffff) >> 8);
  }

  public AudioFormat getAudioFormat()
  {
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }

}
