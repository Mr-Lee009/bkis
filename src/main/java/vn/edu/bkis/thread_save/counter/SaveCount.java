package vn.edu.bkis.thread_save.counter;

public class SaveCount {
  private int count = 0;

  public synchronized void increment() {
    // Phương thức này được đồng bộ hóa (synchronized)
    // Nên chỉ có một thread có thể thực hiện increment tại một thời điểm
    count++;
  }

  public synchronized int getCount() {
    return count;
  }
}
