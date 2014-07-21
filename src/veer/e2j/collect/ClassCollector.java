package veer.e2j.collect;

import veer.e2j.instrument.Filter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class ClassCollector {

  private final Filter visitor;
  private final BlockingQueue<ClassDefinition> source;
  private final String output;

  public ClassCollector(String output, Constraint... constraints) {
    this(output, Arrays.asList(constraints));
  }

  public ClassCollector(String output, Collection<Constraint> constraints) {
    this.output = output;
    source = new LinkedBlockingQueue<ClassDefinition>();
    visitor = new Filter(constraints, source);
  }

  public void prepare() {
    Runtime.getRuntime().addShutdownHook(new CollectionTerminator());
  }

  public void attach(Instrumentation instr) {
    instr.addTransformer(visitor);
  }

  public void finish() {
    List<ClassDefinition> definitions = new ArrayList<ClassDefinition>(source.size());
    source.drainTo(definitions);
    try{
		OutputStream stream = new FileOutputStream(output);
		JarOutputStream sink = new JarOutputStream(stream);
      for (ClassDefinition definition : definitions) {
        emit(sink, definition);
      }
      sink.finish();
      sink.close();
    } catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  }

  private void emit(JarOutputStream sink, ClassDefinition definition) throws IOException {
    sink.putNextEntry(new JarEntry(definition.name() + ".class"));
    sink.write(definition.data());
    sink.closeEntry();
  }

  private final class CollectionTerminator extends Thread {

    private final ClassCollector collector = ClassCollector.this;

    public void run() {
      collector.finish();
    }
  }
}
