package de.hojoe.kctapetool;

import java.io.IOException;
import java.nio.*;

import javax.sound.sampled.*;
import javax.sound.sampled.Control.Type;

public class TestTargetDataLine implements TargetDataLine
{

  private AudioFormat audioFormat;
  private IntegerStream source;
  private long framePosition = 0;
  private boolean open = false;


  public TestTargetDataLine(AudioFormat audioFormat, IntegerStream source)
  {
    this.audioFormat = audioFormat;
    this.source = source;
  }

  @Override
  public void drain()
  {
    // @todo Auto-generated method stub

  }

  @Override
  public void flush()
  {
    // @todo Auto-generated method stub
  }

  @Override
  public void start()
  {
    // @todo Auto-generated method stub
  }

  @Override
  public void stop()
  {
    // @todo Auto-generated method stub

  }

  @Override
  public boolean isRunning()
  {
    // @todo Auto-generated method stub
    return false;
  }

  @Override
  public boolean isActive()
  {
    // @todo Auto-generated method stub
    return false;
  }

  @Override
  public AudioFormat getFormat()
  {
    return audioFormat;
  }

  @Override
  public int getBufferSize()
  {
    return audioFormat.getFrameSize();
  }

  @Override
  public int available()
  {
    return source.available() ? audioFormat.getChannels() * audioFormat.getFrameSize() : 0;
  }

  @Override
  public int getFramePosition()
  {
    return (int)framePosition;
  }

  @Override
  public long getLongFramePosition()
  {
    return framePosition;
  }

  @Override
  public long getMicrosecondPosition()
  {
    return (long)(framePosition / audioFormat.getFrameRate());
  }

  @Override
  public float getLevel()
  {
    return 1.0f;
  }

  @Override
  public javax.sound.sampled.Line.Info getLineInfo()
  {
    return new Line.Info(getClass());
  }

  @Override
  public void open() throws LineUnavailableException
  {
    open = true;
  }

  @Override
  public void open(AudioFormat format, int bufferSize) throws LineUnavailableException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void open(AudioFormat format) throws LineUnavailableException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close()
  {
    open = false;
    try
    {
      source.close();
    }
    catch( IOException e )
    {
      throw new RuntimeException("Fehler beim close()", e);
    }
  }

  @Override
  public boolean isOpen()
  {
    return open;
  }

  @Override
  public Control[] getControls()
  {
    return new Control[] {};
  }

  @Override
  public boolean isControlSupported(Type control)
  {
    return false;
  }

  @Override
  public Control getControl(Type control)
  {
    return null;
  }

  @Override
  public void addLineListener(LineListener listener)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeLineListener(LineListener listener)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read(byte[] b, int off, int len)
  {
    ShortBuffer shortBuffer = ByteBuffer.wrap(b, off, len).asShortBuffer();
    int count = 0;
    while(source.available() && shortBuffer.position() < shortBuffer.capacity())
    {
      try
      {
        shortBuffer.put((short)source.read());
        count++;
      }
      catch( IOException e )
      {
        throw new RuntimeException(e);
      }
    }
    return count * audioFormat.getFrameSize();
  }
}