package vn.edu.bkis.thread_save.counter;

public class mainDemoCounter {
  public static void main(String[] args) {
    test2();
  }

  public static void test1() {
    // Tạo một instance của UnsaveCount, SaveCount và AtomicCounter
    UnsaveCount unsaveCount = new UnsaveCount();
    SaveCount saveCount = new SaveCount();
    AtomicCounter atomicCounter = new AtomicCounter();

    // Tạo 1000 thread để tăng giá trị của mỗi counter
    Thread[] threads = new Thread[1000];
    for (int i = 0; i < 1000; i++) {
      threads[i] = new Thread(() -> {
        unsaveCount.increment();
        saveCount.increment();
        atomicCounter.increment();
      });
    }

    // Bắt đầu tất cả các thread
    for (Thread thread : threads) {
      thread.start();
    }

    // Chờ tất cả các thread hoàn thành
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // In ra kết quả của mỗi counter
    System.out.println("UnsaveCount: " + unsaveCount.getCount());
    System.out.println("SaveCount: " + saveCount.getCount());
    System.out.println("AtomicCounter: " + atomicCounter.getCount());

  }

  public static void test2() {
    // Tạo một instance của UnsaveCount, SaveCount và AtomicCounter
    UnsaveCount unsaveCount = new UnsaveCount();
    SaveCount saveCount = new SaveCount();
    AtomicCounter atomicCounter = new AtomicCounter();

    // Tạo 1000 thread để tăng giá trị của mỗi counter
    Thread[] threads = new Thread[1000];
    for (int i = 0; i < 1000; i++) {
      threads[i] = new Thread(() -> {
        unsaveCount.increment();
        saveCount.increment();
        atomicCounter.increment();
      });
    }

    // Bắt đầu tất cả các thread
    for (Thread thread : threads) {
      thread.start();
    }

    // Chờ tất cả các thread hoàn thành
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // In ra kết quả của mỗi counter
    System.out.println("UnsaveCount: " + unsaveCount.getCount());
    System.out.println("SaveCount: " + saveCount.getCount());
    System.out.println("AtomicCounter: " + atomicCounter.getCount());

  }
}
