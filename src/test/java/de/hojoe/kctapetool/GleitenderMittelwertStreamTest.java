package de.hojoe.kctapetool;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.Test;

class GleitenderMittelwertStreamTest
{

  @Test
  void testRead() throws IOException
  {
    IntegerTestStream integerTestStream = new IntegerTestStream(Arrays.asList(1,2,3,4,5,6,7));
    try(GleitenderMittelwertStream gmStream = new GleitenderMittelwertStream(integerTestStream, 5))
    {
      assertEquals(3, gmStream.read());
      assertEquals(4, gmStream.read());
      assertEquals(5, gmStream.read());
      assertEquals(6, gmStream.read());
      assertEquals(6, gmStream.read());
      assertEquals(7, gmStream.read());
      assertEquals(7, gmStream.read());
      assertFalse(gmStream.available());
    }
  }


  class IntegerTestStream implements IntegerStream
  {
    private Deque<Integer> werte;

    public IntegerTestStream(Collection<Integer> werte)
    {
      Objects.requireNonNull(werte);
      this.werte = new ArrayDeque<>(werte);
    }

    @Override
    public boolean available()
    {
      return !werte.isEmpty();
    }

    @Override
    public int read() throws IOException
    {
      return werte.removeFirst();
    }

    @Override
    public void close() throws IOException
    {
      // nix
    }
  }
}
