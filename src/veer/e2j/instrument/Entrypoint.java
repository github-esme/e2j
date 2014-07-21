package veer.e2j.instrument;

import veer.e2j.collect.ClassCollector;
import veer.e2j.collect.Constraint;

import java.lang.instrument.Instrumentation;

public class Entrypoint {

  public static void premain(String options, Instrumentation instr) {
    String output = (options == null || options.isEmpty())
            ? "e2j-dump.jar" : options;

    ClassCollector collector = new ClassCollector(output, new Constraint() {

      private ClassLoader target;

      public boolean accept(ClassLoader loader, String name) {
        if (target == null) {
          String loader_name = loader.getClass().getName();
          if (loader_name.equals("com.regexlab.j2e.Jar2ExeClassLoader")) {
            target = loader;
          }
        }
        return loader == target;
      }
    });
    collector.prepare();
    collector.attach(instr);
  }
}
