package de.hojoe.kctapetool;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.AudioInputStream;

import org.apache.commons.io.IOUtils;

import de.hojoe.kctapetool.SchwingungKonfig.BitKonfig;

/**
 * Analysiert die Wave Daten anhand der Null Durchgänge bei jeder Schwingung.
 *
 * @author Holger Jödicke
 */
public class NullDurchgangWaveAnalyzer implements WaveAnalyzer
{
  private IntegerStream is;
  private AtomicLong framePos = new AtomicLong(0);
  private ArrayDeque<Integer> puffer = new ArrayDeque<>(100);
  private SchwingungKonfig bitKonfig = new SchwingungKonfig();

  public NullDurchgangWaveAnalyzer(AudioInputStream ais)
  {
    is = new AudioMonoIntegerStream(ais);
  }

  @Override
  public boolean sucheEinsSchwingung() throws IOException
  {
    if( !fillPuffer(bitKonfig.getEinsBit().getMaxLaenge()) )
    {
      return false;
    }
    while( pruefeSchwingungImPuffer(puffer.iterator(), bitKonfig.getEinsBit(), true, false) == null )
    {
      framePos.incrementAndGet();
      puffer.pollFirst();
      if( !fillPuffer(bitKonfig.getEinsBit().getMaxLaenge()) )
      {
        return false;
      }
    }
    if( !is.available() )
    {
      return false;
    }
    return true;
  }

  @Override
  public boolean leseEinsSchwingung()
  {
    KcTapeSchwingung einsSchwingung = pruefeSchwingungImPuffer(puffer.iterator(), bitKonfig.getEinsBit(), false, false);
    if( einsSchwingung == null )
    {
      return false;
    }
    framePos.addAndGet(einsSchwingung.laenge);
    for( int i = 0; i < einsSchwingung.laenge; i++ )
    {
      puffer.removeFirst();
    }
    return true;
  }

  @Override
  public boolean sucheTrennzeichenNachVorton() throws IOException
  {
    if( isTrennzeichen() )
    {
      return true;
    }
    if( !fillPuffer(bitKonfig.getEinsBit().getMaxLaenge() + bitKonfig.getTrennBit().getMaxLaenge()) )
    {
      return false;
    }
    while( puffer.size() > bitKonfig.getTrennBit().getMinLaenge() )
    {
      KcTapeSchwingung trennzeichen = pruefeSchwingungImPuffer(puffer.iterator(), bitKonfig.getTrennBit(), true, false);
      if( trennzeichen != null )
      {
        return true;
      }
      framePos.incrementAndGet();
      puffer.pollFirst();
    }
    return false;
  }

  @Override
  public boolean leseTrennzeichen()
  {
    KcTapeSchwingung schwingung = pruefeSchwingungImPuffer(puffer.iterator(), bitKonfig.getTrennBit(), false, false);
    if( schwingung == null )
    {
      return false;
    }
    framePos.addAndGet(schwingung.laenge);
    for( int i = 0; i < schwingung.laenge; i++ )
    {
      puffer.removeFirst();
    }
    return true;
  }

  @Override
  public boolean isEinsSchwingung() throws IOException
  {
    BitKonfig einsBit = bitKonfig.getEinsBit();
    if( !fillPuffer(einsBit.getMaxLaenge()) )
    {
      return false;
    }
    return pruefeSchwingungImPuffer(puffer.iterator(), einsBit, false, false) != null;
  }

  @Override
  public boolean isTrennzeichen() throws IOException
  {
    BitKonfig trennBit = bitKonfig.getTrennBit();
    if( !fillPuffer(trennBit.getMaxLaenge()) )
    {
      return false;
    }
    return pruefeSchwingungImPuffer(puffer.iterator(), trennBit, false, false) != null;
  }

  @Override
  public int leseDatenByte(boolean letztes) throws IOException, WaveAnalyzerException
  {
    if( !fillPuffer(bitKonfig.getEinsBit().getMaxLaenge() * 8 + bitKonfig.getTrennBit().getMaxLaenge()) && !letztes )
    {
      return -1;
    }
    List<Integer> list = new ArrayList<>(puffer);
    KcTapeSchwingung naechstesTrennzeichen = null;
    int von = bitKonfig.getNullBit().getLaenge() * 7;
    int bis = list.size() - bitKonfig.getTrennBit().getMinLaenge();
    int index;
    for( index = von; index < bis; index++ )
    {
      naechstesTrennzeichen = pruefeSchwingungImPuffer(list.listIterator(index), bitKonfig.getTrennBit(), true, letztes);
      if( naechstesTrennzeichen != null )
      {
        break;
      }
    }
    if( naechstesTrennzeichen == null )
    {
      throw new WaveAnalyzerException("Kein Trennzeichen erkannt, " + formatierePosition());
    }
    // puffer auf Stand bringen
    for( int i = 0; i < index + naechstesTrennzeichen.laenge; i++ )
    {
      puffer.removeFirst();
    }
    // liste auf Stand bringen:
    list = list.subList(0, index + 2);
    int data = extrahiereBits(list);
    // framePos zeigt hinter das nächste Trennzeichen
    framePos.addAndGet(index + naechstesTrennzeichen.laenge);
    return data;
  }

  private int extrahiereBits(List<Integer> list)
  {
    int listIndex = 0;
    int b = 0;
    for( int i = 0; i < 8; i++ )
    {
      KcTapeSchwingung nullSchwingung = pruefeSchwingungImPuffer(list.listIterator(listIndex), bitKonfig.getNullBit(), false, false);
      if( nullSchwingung != null )
      {
        listIndex += nullSchwingung.laenge;
        continue;
      }
      KcTapeSchwingung einsSchwingung = pruefeSchwingungImPuffer(list.listIterator(listIndex), bitKonfig.getEinsBit(), false, false);
      if( einsSchwingung != null )
      {
        b |= 1 << i;
        listIndex += einsSchwingung.laenge;
        continue;
      }
      String pos = formatierePosition();
      throw new RuntimeException("mehr Intelligenz benötigt, Position: " + pos + ", Bit: " + i);
    }
    return b;
  }

  /**
   * @param bit nach welchem Bit gesucht wird
   * @param nullDurchgang1 soll der nullDurchgang1 gesucht werden oder ist der Puffer bereits am Anfang einer Schwingung.
   * @param letztes das letzte Trennzeichen eine Blocks
   */
  private KcTapeSchwingung pruefeSchwingungImPuffer(Iterator<Integer> iter, BitKonfig bit, boolean nullDurchgang1, boolean letztes)
  {
    int first = iter.next();
    int second = iter.next();
    // Nulldurchgang 1
    if( nullDurchgang1 && first < 0 == second < 0 )
    {
      return null;
    }
    int index = 1;
    int vorgaenger = second;
    int nullDurchgang2 = -1;
    int nullDurchgang3 = -1;
    // Karenz bis wieder mit der Suche nach einem Durchgang begonnen wird
    int minIndexDurchgang = 5; // ca. 1/4 von 22 (kürzeste Schwingung)
    while( iter.hasNext() )
    {
      int aktuell = iter.next();
      index++;
      if( nullDurchgang2 == -1 )
      {
        if( index > minIndexDurchgang && vorgaenger < 0 != aktuell < 0 )
        {
          nullDurchgang2 = index;
        }
      }
      else if( nullDurchgang2 != -1 && index > nullDurchgang2 + minIndexDurchgang && vorgaenger < 0 != aktuell < 0 )
      {
        nullDurchgang3 = index;
        break;
      }
      vorgaenger = aktuell;
      if( index >= bit.getMaxLaenge() )
      {
        break;
      }
    }
//    int null2Min = (int)Math.round(bit.getLaenge() / 2.0 * 0.7);
//    int null2Max = (int)Math.round(bit.getLaenge() / 2.0 * 1.3);
    if( nullDurchgang2 != -1 && nullDurchgang3 != -1 && nullDurchgang3 >= bit.getMinLaenge() )
    {
      return new KcTapeSchwingung(index);
    }

    // Das letzte Trennzeichen ist manchmal deutlich länger und verwaschen, dann ist nur der Anfang
    // relevant
    if( letztes && nullDurchgang2 != -1 && nullDurchgang3 >= bit.getMinLaenge())
    {
      return new KcTapeSchwingung(index);
    }
    return null;
  }

  @Override
  public long getFramePos()
  {
    return framePos.get();
  }

  /**
   * Füllt den Puffer bis zur angegebenen Größe.
   *
   * @return <code>true</code>, wenn der Puffer aus dem Stream gefüllt werden konnte, ansonsten
   *         <code>false</code>
   */
  private boolean fillPuffer(int size) throws IOException
  {
    while( puffer.size() < size )
    {
      if( !is.available() )
      {
        return false;
      }
      int i = is.read();
//      System.out.print(Integer.toHexString(i) + " ");
      puffer.add(i);
    }
//    System.out.println();
    return true;
  }

  @Override
  public void close() throws IOException
  {
    IOUtils.closeQuietly(is);
  }

  private String formatierePosition()
  {
    long fp = framePos.get();
    return String.format("Position: Sekunde %d und Frame %d", fp / 44100, fp % 44100);
  }

  @Override
  public String toString()
  {
    return formatierePosition() + ", Puffer:\n" + puffer;
  }

  private static class KcTapeSchwingung
  {
    private int laenge;

    public KcTapeSchwingung(int laenge)
    {
      this.laenge = laenge;
    }

  }

}
