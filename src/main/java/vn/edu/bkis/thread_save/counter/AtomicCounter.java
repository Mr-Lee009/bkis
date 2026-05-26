package vn.edu.bkis.thread_save.counter;

import vn.edu.bkis.constan.ConstantCommon;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter {
  private AtomicInteger count = new AtomicInteger(ConstantCommon.ZERO_NUMBER);

  public void increment() {
    // Phương thức incrementAndGet() là một thao tác nguyên tử (atomic)
    // Nó sẽ tự động đảm bảo rằng việc tăng giá trị count là an toàn khi có nhiều thread truy cập
    count.incrementAndGet();
  }

  public int getCount() {
    return count.get();
  }
}
