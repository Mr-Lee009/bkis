package vn.edu.bkis.thread_save.counter;

public class UnsaveCount {
  private int count = 0;

  public void increment() {
    // Đây không phải là thao tác nguyên tử (atomic)
    // Nó gồm 3 bước: Đọc count -> Cộng 1 -> Ghi lại count
    count++;
  }

  public int getCount() {
    return count;
  }
}
