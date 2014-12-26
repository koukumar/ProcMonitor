package io.logbase.procmonitor;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class Monitor implements Handler, Runnable {
  static Logger log = Logger.getLogger(
    Monitor.class.getName());
  private final AtomicLong counter = new AtomicLong();
  private final int timeInterval;
  private final String cmd;
  private final String format;
  private final Gson gson = new Gson();

  public Monitor(int timeInterval, String cmd, String format){
    this.timeInterval = timeInterval;
    this.cmd = cmd;
    this.format = format;
  }

  public static void main(String args[]) throws IOException {

    if(args.length!=1){
      log.fatal("Required args: config_file.json");
      return;
    }

    Properties prop = new Properties();
    String propFileName = args[0];
    InputStream inputStream = new FileInputStream(propFileName);
    prop.load(inputStream);
    String cmd = prop.getProperty("cmd");
    int timeInterval = Integer.parseInt(prop.getProperty("timeInterval"));
    String format = prop.getProperty("format");
    Monitor m = new Monitor(timeInterval, cmd, format);
    m.run();
  }

  @Override
  public void handle(String msg) {
    counter.incrementAndGet();
    try{
      gson.fromJson(msg,  Map.class);
      if(gson != null) {
        System.out.println(msg);
      }
    }catch(Exception e){
      log.error("Error for msg: " + msg);
      log.error(e);
    }
  }

  @Override
  public void run() {
    ProcRunner proc = new ProcRunner(cmd, this);

    proc.start();
    while(true){
      sleep();
      if(counter.get()==0){
        log.error("Killing the proc");
        proc.stop();
        proc = new ProcRunner(cmd, this);
        proc.start();
        sleep();
      }else{
        double qps = Math.round(counter.get()*100.0/(timeInterval/1000))/100.0;
        log.error("Time " + (new Date()) + " Qps: " + qps );
      }
      counter.set(0);
    }
  }

  public void sleep(){
    try {
      Thread.sleep(timeInterval);
    } catch (InterruptedException e) {
      log.fatal(e);
      System.exit(1);
    }
  }
}
