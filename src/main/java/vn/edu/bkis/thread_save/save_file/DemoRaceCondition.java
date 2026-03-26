package vn.edu.bkis.thread_save.save_file;


import java.io.FileWriter;
import java.io.IOException;

public class DemoRaceCondition {

  private static final Object lock = new Object();

  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      int threadId = i;
      new Thread(() -> writeFile(threadId)).start();
    }
  }

  private static void writeFile(int threadId) {
    synchronized (lock) {
      try (FileWriter writer = new FileWriter("test.txt", true)) {
        for (int i = 0; i < 1000; i++) {
          writer.write("Thread " + threadId + " - line " + i + "\n");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
