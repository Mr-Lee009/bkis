package vn.edu.bkis.thread_save.save_file;

import java.io.FileWriter;
import java.io.IOException;

public class mainTestSaveFile_SaveThread {

  public static void main(String[] args) {
    // kich ban save file su dung thread save
    writeFileUnSaveThread();
  }

  private static void writeFileUnSaveThread() {
    for (int i = 1; i <= 100; i++) {
      int threadId = i;
      new Thread(() -> {
        writeFileWithUnSaveThread(threadId);
      }).start();
    }

    // Cho phép tất cả các thread hoàn thành công việc trước khi kết thúc chương trình
    try {
        Thread.sleep(5000); // Đợi 5 giây để tất cả các thread hoàn thành
        } catch (InterruptedException e) {
        e.printStackTrace();
    }
  }

  private static void writeFileWithUnSaveThread(int threadId) {
    try (FileWriter writer = new FileWriter("test.txt", true)) {
      for (int i = 0; i < 100; i++) {
        // log
        System.out.printf("Thread %d - line %d\n", threadId, i);
        writer.write("Thread " + threadId + " - line " + i + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }



}
