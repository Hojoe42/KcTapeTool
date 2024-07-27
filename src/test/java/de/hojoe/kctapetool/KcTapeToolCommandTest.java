package de.hojoe.kctapetool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.nio.file.Paths;

import org.junit.jupiter.api.*;

import picocli.CommandLine;
import picocli.CommandLine.ExitCode;

class KcTapeToolCommandTest
{

  private StringWriter stringWriter = new StringWriter();

  private PrintWriter printWriter;

  @BeforeEach
  void beforeEach()
  {
    printWriter = new PrintWriter(stringWriter);
  }

  @AfterEach
  void AfterEach()
  {
    printWriter.close();
  }

  @Test
  void testHelpParameterKurz()
  {
    CommandLine cmd = new CommandLine(new TestKcTapeCommand()).setOut(printWriter);
    int exitCode = cmd.execute("-h");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(stringWriter.toString(), startsWith("Usage: KcTapeTool"));
  }

  @Test
  void testHelpParameterLang()
  {
    CommandLine cmd = new CommandLine(new TestKcTapeCommand()).setOut(printWriter);
    int exitCode = cmd.execute("--help");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(stringWriter.toString(), startsWith("Usage: KcTapeTool"));
  }

  @Test
  void testVersionParameterKurz()
  {
    CommandLine cmd = new CommandLine(new TestKcTapeCommand()).setOut(printWriter);
    int exitCode = cmd.execute("-V");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(stringWriter.toString(), matchesRegex("\\d\\.\\d\\.\\d\\R"));
  }

  @Test
  void testVersionParameterLang()
  {
    CommandLine cmd = new CommandLine(new TestKcTapeCommand()).setOut(printWriter);
    int exitCode = cmd.execute("--version");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(stringWriter.toString(), matchesRegex("\\d\\.\\d\\.\\d\\R"));
  }

  @Test
  void testDirectoryParameter()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("--directory", "c:\\tmp\\");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.getDirectory(), is(Paths.get("c:\\tmp\\")));
    assertThat(command.getSource(), is(nullValue()));
  }

  @Test
  void testListParameterLang()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("--list");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.list, is(true));
  }

  @Test
  void testListParameterKurz()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("-l");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.list, is(true));
  }

  @Test
  void testSourceParameterLang()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("--source", "/home/xyz/test.wav");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.source, is("/home/xyz/test.wav"));
    assertThat(command.getSource(), is("/home/xyz/test.wav"));
  }

  @Test
  void testSourceParameterKurz()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("-s", "/home/xyz/test.wav");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.source, is("/home/xyz/test.wav"));
    assertThat(command.getSource(), is("/home/xyz/test.wav"));
  }

  @Test
  void testDestinationParameterLang()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("--destination", "/home/xyz/test.wav");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.destination, is("/home/xyz/test.wav"));
    assertThat(command.getDestination(), is("/home/xyz/test.wav"));
  }

  @Test
  void testDestinationParameterKurz()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("-d", "/home/xyz/test.wav");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.destination, is("/home/xyz/test.wav"));
    assertThat(command.getDestination(), is("/home/xyz/test.wav"));
  }

  @Test
  void testWaitParameter()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter);
    int exitCode = cmd.execute("--wait", "30");
    assertThat(exitCode, is(ExitCode.OK));
    assertThat(command.timeout, is(30));
    assertThat(command.getTimeout(), is(30));
  }

  @Test
  void testUnbekannterParameter()
  {
    KcTapeToolCommand command = new TestKcTapeCommand();
    CommandLine cmd = new CommandLine(command).setOut(printWriter).setErr(printWriter);
    int exitCode = cmd.execute("--blablabla", "c:\\tmp\\");
    assertThat(exitCode, is(ExitCode.USAGE));
  }

  class TestKcTapeCommand extends KcTapeToolCommand
  {
    /** keine Logik ausführen, es geht nur darum ob das Parsen aller Parameter soweit funktioniert */
    @Override
    public Integer call() throws Exception
    {
      return ExitCode.OK;
    }

  }

}
