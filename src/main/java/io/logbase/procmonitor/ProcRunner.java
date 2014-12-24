package io.logbase.procmonitor;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcRunner extends Thread {
  static Logger log = Logger.getLogger(
    ProcRunner.class.getName());
  public final String cmd;
  private final Monitor m;

  public ProcRunner(String cmd, Monitor m) {
    this.cmd = cmd;
    this.m = m;
  }

  @Override
  public void run() {
    Process process = null;

    try {
      process = Runtime.getRuntime().exec(cmd);
      log.error("started the proc");
    } catch (IOException e) {
      log.fatal(e);
      return;
    }

    InputStream stdout = process.getInputStream();

    BufferedReader br =
      new BufferedReader(new InputStreamReader(stdout));
    String line;
    try {
      while ((line = br.readLine()) != null) {
        m.handle(line);
      }
    } catch (IOException e) {
      log.fatal(e);
      return;
    }
  }
}
