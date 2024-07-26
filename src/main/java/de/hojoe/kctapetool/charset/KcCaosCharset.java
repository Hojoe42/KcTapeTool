package de.hojoe.kctapetool.charset;

// Vorlage: Written by Reinier Zwitserloot
// https://stackoverflow.com/questions/76936549/charset-not-present-in-used-jdk/76936879#76936879

import java.nio.*;
import java.nio.charset.*;
import java.util.*;

/**
 * Zeichensatz für den originalen Zeichensatz im KC 85/3-8 (Vermutlich auch 85/2).
 * <p>
 * Verwendung zum Beispiel:
 * <pre>
      System.out.println(new String(bytes, KcCaosCharset.NAME));
      System.out.println(new String(bytes, KcCaosCharsetProvider.CAOS_CHARSET));
 * </pre>
 *
 * @author Holger Jödicke
 */
public class KcCaosCharset extends Charset
{
  /** Öffentlicher Name des Zeichensatzes */
  public static final String NAME = "KC-CAOS";

  private static final char[] CAOS_B2C =
  {
    /* @formatter:off
     https://www.compart.com/de/unicode/
     Abweichungen:
     5B -> 2588   █
     5C -> 007C   |
     5D -> 00AC   ¬
     60 -> 00A9   ©
     7B -> 00E4   ä
     7C -> 00F6   ö
     7D -> 00FC   ü
     7E -> 00DF   ß
     7F -> 2610   ☐
    */
    '\u0000', '\u0001', '\u0002', '\u0004', '\u0004', '\u0005', '\u0006', '\u0007', '\u0008', '\u0009', '\r',     '\u000B', '\u000C', '\n',     '\u000E', '\u000F',
    '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D', '\u001E', '\u001F',
    '\u0020', '\u0021', '\u0022', '\u0023', '\u0024', '\u0025', '\u0026', '\'',     '\u0028', '\u0029', '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
    '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B', '\u003C', '\u003D', '\u003E', '\u003F',
    '\u0040', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047', '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D', '\u004E', '\u004F',
    '\u0050', '\u0051', '\u0052', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059', '\u005A', '\u2588', '\u007C', '\u00AC', '\u005E', '\u005F',
    '\u00A9', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B', '\u006C', '\u006D', '\u006E', '\u006F',
    '\u0070', '\u0071', '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077', '\u0078', '\u0079', '\u007A', '\u00E4', '\u00F6', '\u00FC', '\u00DF', '\u2610',
    '\u0000', '\u0001', '\u0002', '\u0004', '\u0004', '\u0005', '\u0006', '\u0007', '\u0008', '\u0009', '\r',     '\u000B', '\u000C', '\n',     '\u000E', '\u000F',
    '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D', '\u001E', '\u001F',
    '\u0020', '\u0021', '\u0022', '\u0023', '\u0024', '\u0025', '\u0026', '\'',     '\u0028', '\u0029', '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
    '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B', '\u003C', '\u003D', '\u003E', '\u003F',
    '\u0040', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047', '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D', '\u004E', '\u004F',
    '\u0050', '\u0051', '\u0052', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059', '\u005A', '\u2588', '\u007C', '\u00AC', '\u005E', '\u005F',
    '\u00A9', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B', '\u006C', '\u006D', '\u006E', '\u006F',
    '\u0070', '\u0071', '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077', '\u0078', '\u0079', '\u007A', '\u00E4', '\u00F6', '\u00FC', '\u00DF', '\u2610',
    // @formatter:on
  };

  private static final Map<Character, Byte> LOOKUP;
  static
  {
    Map<Character, Byte> map = new HashMap<>();
    for( int i = 0; i < CAOS_B2C.length; i++ )
    {
      map.put(CAOS_B2C[i], (byte)i);
    }
    LOOKUP = Collections.unmodifiableMap(map);
  }

  /** Wird üblicher Weise durch den {@link KcCaosCharsetProvider} erzeugt.  */
  KcCaosCharset()
  {
    super(NAME, null);
  }

  @Override
  public boolean contains(Charset cs)
  {
    return cs.name().equals(NAME);
  }

  @Override
  public CharsetDecoder newDecoder()
  {
    return new CharsetDecoderExtension(this, 1F, 1F);
  }

  @Override
  public CharsetEncoder newEncoder()
  {
    return new CharsetEncoderExtension(this, 1F, 1F);
  }

  private static final class CharsetEncoderExtension extends CharsetEncoder
  {
    private CharsetEncoderExtension(Charset cs, float averageBytesPerChar, float maxBytesPerChar)
    {
      super(cs, averageBytesPerChar, maxBytesPerChar);
    }

    @Override
    protected CoderResult encodeLoop(CharBuffer from, ByteBuffer to)
    {
      while( from.hasRemaining() )
      {
        if( !to.hasRemaining() )
        {
          return CoderResult.OVERFLOW;
        }
        char d = from.get();
        Byte v = LOOKUP.get(d);
        if( v == null )
        {
          // 'un'consume the character we consumed
          from.position(from.position() - 1);
          return CoderResult.unmappableForLength(1);
        }
        to.put(v.byteValue());
      }

      return CoderResult.UNDERFLOW;
    }
  }

  private final static class CharsetDecoderExtension extends CharsetDecoder
  {
    private CharsetDecoderExtension(Charset cs, float averageCharsPerByte, float maxCharsPerByte)
    {
      super(cs, averageCharsPerByte, maxCharsPerByte);
    }

    @Override
    protected CoderResult decodeLoop(ByteBuffer from, CharBuffer to)
    {
      while( from.hasRemaining() )
      {
        if( !to.hasRemaining() )
        {
          return CoderResult.OVERFLOW;
        }
        byte c = from.get();
        char d = CAOS_B2C[c & 0xFF];
        to.put(d);
      }

      return CoderResult.UNDERFLOW;
    }
  }

}