package de.hojoe.kctapetool;

import java.util.*;

import org.apache.commons.io.FilenameUtils;

import de.hojoe.kctapetool.charset.KcCaosCharsetProvider;

/**
 * Repräsentiert eine Datei mit ihren einzelnen Daten-Blöcken.
 *
 * @author Holger Jödicke
 */
public class KcDatei
{
  private static final byte BASIC_TYPE = (byte)0xd3;

  private ArrayList<KcDateiBlock> bloecke = new ArrayList<>();

  /**
   * Fügt einen Block zur Datei hinzu. Es kann immer nur der nächste Block (lückenlos) angefügt
   * werden. Wird ein Block mit bereits existierender Nummer eingefügt, dann wird der alte Block nur
   * überschrieben, wenn die Checksumme falsch war. War die Checksumme korrekt, dann wird der neue
   * Block ignoriert.
   */
  public void add(KcDateiBlock block)
  {
    int index = block.getBlocknummer() - 1;
    if( bloecke.size() == index || block.getBlocknummer() == 255 )
    {
      bloecke.add(block);
    }
    else
      if( bloecke.size() > index )
      {
        KcDateiBlock alt = bloecke.get(index);
        if( KcDateiBlock.berechneChecksumme(alt.getDaten()) != alt.getChecksumme() )
        {
          bloecke.set(index, block);
        }
      }
  }

  /**
   * Liefert den Dateiname und die Endung.
   * <p>
   * Nicht immer ist direkt in den Daten eine Dateiendung abgelegt, dann wird versucht eine passende
   * Endung zu finden. Bei Basic Dateien wird als Endung '.SSS' angenommen, ansonsten '.KCC'.
   */
  public String getFullDateiName()
  {
    String dateiname = getDateiname();
    if( dateiname.isEmpty() )
    {
      dateiname = "kcDatei";
    }
    if( FilenameUtils.getExtension(dateiname).isEmpty() )
    {
      if( isBasicDateiType() )
      {
        dateiname += ".SSS";
      }
      else
      {
        dateiname += ".KCC";
      }
    }
    return dateiname;
  }

  /**
   * Liefert den Dateiname aus dem 1. Block.
   *
   * @return der Dateiname, kann auch leer sein.
   */
  public String getDateiname()
  {
    if( bloecke.isEmpty() )
    {
      return "";
    }
    byte[] daten = bloecke.get(0).getDaten();
    if( isBasicDateiType() )
    {
      return getBasicDateiName(daten).trim();
    }
    return getCaosDateiName(daten).trim();
  }

  /**
   * Liefert <code>true</code>, wenn es sich um eine Basic Datei handelt.
   */
  public boolean isBasicDateiType()
  {
    if( bloecke.isEmpty() )
    {
      return false;
    }
    byte[] daten = bloecke.get(0).getDaten();
    return daten[0] == BASIC_TYPE && daten[1] == BASIC_TYPE && daten[2] == BASIC_TYPE;
  }

  /**
   * Liefert eine Liste aller vorhandenen {@link KcDateiBlock}e.
   */
  public List<KcDateiBlock> getBloecke()
  {
    return Collections.unmodifiableList(bloecke);
  }

  private String getCaosDateiName(byte[] daten)
  {
    return new String(daten, 0,12, KcCaosCharsetProvider.CAOS_CHARSET);
  }

  private String getBasicDateiName(byte[] daten)
  {
    return new String(daten, 3,11, KcCaosCharsetProvider.CAOS_CHARSET);
  }

  @Override
  public String toString()
  {
    return getFullDateiName() + ", Blöcke: " + bloecke.size();
  }

}
