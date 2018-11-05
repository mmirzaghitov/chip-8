package md.mmirzaghitov.chip8;

import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.D;
import static javafx.scene.input.KeyCode.DIGIT1;
import static javafx.scene.input.KeyCode.DIGIT2;
import static javafx.scene.input.KeyCode.DIGIT3;
import static javafx.scene.input.KeyCode.DIGIT4;
import static javafx.scene.input.KeyCode.E;
import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCode.Q;
import static javafx.scene.input.KeyCode.R;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.V;
import static javafx.scene.input.KeyCode.W;
import static javafx.scene.input.KeyCode.X;
import static javafx.scene.input.KeyCode.Z;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Keyboard {

	private KeyCode[] keys = new KeyCode[]{DIGIT1, DIGIT2, DIGIT3, DIGIT4, Q, W, E, R, A, S, D, F, Z, X, C, V};

	private ReentrantLock lock = new ReentrantLock();
	private volatile Condition keyPressedCondition = lock.newCondition();
	private volatile int keyPressed = -1;

	private Map<KeyCode, AtomicBoolean> keyPressedMap = new LinkedHashMap<>();

	{
		keyPressedMap.put(DIGIT1, new AtomicBoolean());
		keyPressedMap.put(DIGIT2, new AtomicBoolean());
		keyPressedMap.put(DIGIT3, new AtomicBoolean());
		keyPressedMap.put(DIGIT4, new AtomicBoolean());
		keyPressedMap.put(Q, new AtomicBoolean());
		keyPressedMap.put(W, new AtomicBoolean());
		keyPressedMap.put(E, new AtomicBoolean());
		keyPressedMap.put(R, new AtomicBoolean());
		keyPressedMap.put(A, new AtomicBoolean());
		keyPressedMap.put(S, new AtomicBoolean());
		keyPressedMap.put(D, new AtomicBoolean());
		keyPressedMap.put(F, new AtomicBoolean());
		keyPressedMap.put(Z, new AtomicBoolean());
		keyPressedMap.put(X, new AtomicBoolean());
		keyPressedMap.put(C, new AtomicBoolean());
		keyPressedMap.put(V, new AtomicBoolean());

	}

	public void keyPressed(KeyEvent keyEvent) {
		lock.lock();
		try {
			for (int i = 0; i < keys.length; i++) {
				if (keys[i] == keyEvent.getCode()) {
					keyPressed = i;
				}
			}
			keyPressedCondition.signalAll();
			keyPressedMap.get(keyEvent.getCode()).set(true);
		} finally {
			lock.unlock();
		}
	}

	public void keyReleased(KeyEvent keyEvent) {
		keyPressedMap.get(keyEvent.getCode()).set(false);
		keyPressed = -1;
	}

	public boolean isKeyPressed(int key) {
		return keyPressedMap.get(keys[key]).get();
	}

	public int waitUntilPressed() {
		try {
			lock.lock();
			while (keyPressed < 0) {
				keyPressedCondition.await();
			}
			return keyPressed;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

}