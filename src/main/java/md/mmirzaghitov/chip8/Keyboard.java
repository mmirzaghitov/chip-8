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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Keyboard {

	private KeyCode[] keys = new KeyCode[]{DIGIT1, DIGIT2, DIGIT3, DIGIT4, Q, W, E, R, A, S, D, F, Z, X, C, V};
	private volatile boolean[] keysPressed = new boolean[16];

	private ReentrantLock lock = new ReentrantLock();
	private volatile Condition keyPressedCondition = lock.newCondition();
	private volatile int keyPressed = -1;

	public void keyPressed(KeyEvent keyEvent) {
		lock.lock();
		try {
			for (int i = 0; i < keys.length; i++) {
				if (keys[i] == keyEvent.getCode()) {
					keyPressed = i;
				}
			}

			if (keyPressed > 0) {
				keyPressedCondition.signalAll();
				keysPressed[keyPressed] = true;
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean isKeyPressed(int key) {
		if (key > keys.length) {
			return false;
		}
		boolean b = keysPressed[key];
		keysPressed[key] = false;
		return b;
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