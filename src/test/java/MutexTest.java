import com.github.navnesen.sync.Mutex;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MutexTest {

	@Test
	public void testMutex() throws InterruptedException {
		Mutex<Integer> mutex = new Mutex<>(0);

		new Thread(() -> {
			try (var lock = mutex.lock()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					System.out.println("interrupted!");
					throw new RuntimeException(e);
				}
			}
		}).start();

		Thread.sleep(100);

		final AtomicReference<Long> _lockRequested = new AtomicReference<>(null);
		final AtomicReference<Long> _lockReceived = new AtomicReference<>(null);

		var th = new Thread(() -> {
			_lockRequested.set(new java.util.Date().getTime());
			try (var lock = mutex.lock()) {
				// assert lock.mutex != null;
				_lockReceived.set(new java.util.Date().getTime());
				assert lock.get() == 0;
				lock.set(1);
				assert lock.get() == 1;
				assert lock.get() != -1;
				assertThrows(RuntimeException.class, () -> {
					lock.release();
					lock.set(-1);
				});
			}
		});

		th.start();
		th.join();
		System.out.println(_lockReceived.get() - _lockRequested.get());
		assert _lockReceived.get() - _lockRequested.get() > 200;
	}
}
