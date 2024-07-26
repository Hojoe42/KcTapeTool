package de.hojoe.kctapetool.charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.*;

/**
 * Provider für den CAOS Zeichensatz.
 *
 * @author Holger Jödicke
 */
public class KcCaosCharsetProvider extends CharsetProvider
{
  /** KC 85/3-8 Standard Caos Zeichensatz */
  public static final Charset CAOS_CHARSET = new KcCaosCharset();

  private static final List<Charset> CHARSET_LIST = Collections.singletonList(CAOS_CHARSET);

  @Override
  public Iterator<Charset> charsets()
  {
    return CHARSET_LIST.iterator();
  }

  @Override
  public Charset charsetForName(String charsetName)
  {
    if( KcCaosCharset.NAME.equals(charsetName) )
    {
      return CAOS_CHARSET;
    }
    return null;
  }

  /**
   * Für einfache Tests ...
   */
  public static void main(String[] args) throws Exception
  {
    byte[] b = {0x7b, 0x7c, 0x7d, 0x7e}; // äöüß im CAOS
    System.out.println(new String(b, CAOS_CHARSET));
    System.out.println(new String(b, KcCaosCharset.NAME));
    System.out.println(new String(b, "KC-CAOS"));
  }
}