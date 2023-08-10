package de.hojoe.kctapetool;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "write", description = "Schreibt Dateien zum KC !!!nicht implementiert!!!")
public class KcWriter implements Callable<Integer>
{

  public KcWriter()
  {
    super();
  }

  @Override
  public Integer call() throws Exception
  {
    return 0;
  }

}
