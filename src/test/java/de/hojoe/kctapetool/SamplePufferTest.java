package de.hojoe.kctapetool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SamplePufferTest
{

  @Test
  void test8Bit()
  {
    SamplePuffer puffer = new SamplePuffer(8);
    puffer.reset();

    puffer.add((byte)0);
    assertEquals(0, puffer.getSample());
    puffer.reset();

    puffer.add((byte)64);
    assertEquals(64, puffer.getSample());
    puffer.reset();

    puffer.add(Byte.MAX_VALUE);
    assertEquals(127, puffer.getSample());
    puffer.reset();

    puffer.add((byte)-64);
    assertEquals(-64, puffer.getSample());
    puffer.reset();

    puffer.add(Byte.MIN_VALUE);
    assertEquals(-128, puffer.getSample());
    puffer.reset();
  }

  @Test
  void test12Bit()
  {
    SamplePuffer puffer = new SamplePuffer(12);
    puffer.reset();

    puffer.add((byte)0);
    puffer.add((byte)0);
    assertEquals(0, puffer.getSample());
    puffer.reset();

    puffer.add((byte)0xff);
    puffer.add((byte)0x07);
    assertEquals(2047, puffer.getSample());
    puffer.reset();

    puffer.add((byte)0);
    puffer.add((byte)1);
    assertEquals(256, puffer.getSample());
    puffer.reset();

    puffer.add((byte)0x00);
    puffer.add((byte)0x08);
    assertEquals(-2048, puffer.getSample());
    puffer.reset();

    puffer.add((byte)0xff);
    puffer.add((byte)0x0f);
    assertEquals(-1, puffer.getSample());
    puffer.reset();

    puffer.add((byte)-10);
    puffer.add((byte)0x0f);
    assertEquals(-10, puffer.getSample());
    puffer.reset();
  }

  @Test
  void test16Bit()
  {
    SamplePuffer puffer = new SamplePuffer(16);
    puffer.reset();

    puffer.add((byte)0);
    puffer.add((byte)0);
    assertEquals(0, puffer.getSample());
    puffer.reset();

    puffer.add((byte)0xff);
    puffer.add((byte)0x7f);
    assertEquals(Short.MAX_VALUE, puffer.getSample());
    puffer.reset();

    puffer.add((byte)0);
    puffer.add((byte)1);
    assertEquals(256, puffer.getSample());
    puffer.reset();

    puffer.add((byte)0xff);
    puffer.add((byte)0xff);
    assertEquals(-1, puffer.getSample());
    puffer.reset();

    puffer.add((byte)-10);
    puffer.add((byte)0xff);
    assertEquals(-10, puffer.getSample());
    puffer.reset();
  }

  @Test
  void test16BitFull()
  {
    SamplePuffer puffer = new SamplePuffer(16);
    for( int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++ )
    {
      puffer.reset();
      puffer.add((byte)(i & 0xff));
      puffer.add((byte)((i & 0xffff)>>>8));
      assertEquals(i, puffer.getSample());
    }
  }


}
