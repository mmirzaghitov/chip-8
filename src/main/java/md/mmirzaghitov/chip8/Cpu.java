package md.mmirzaghitov.chip8;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Cpu implements Runnable {

	private final int[] memory;
	private final Display display;
	private final Keyboard keyboard;

	private int pc = 0x200;
	private int sc = 0;
	private int ireg = 0;

	private int dt;
	private int st;

	private int[] registers = new int[16];
	private int[] stack = new int[16];

	private Random random = new Random();

	private Map<Integer, OPCodeProcessor> opcodeTable = new HashMap<>();

	private ReentrantLock lock = new ReentrantLock();

	Condition condition = lock.newCondition();

	{
		opcodeTable.put(0x0, this::_00NN);
		opcodeTable.put(0x1, this::_1NNN);
		opcodeTable.put(0x2, this::_2NNN);
		opcodeTable.put(0x3, this::_3NNN);
		opcodeTable.put(0x4, this::_4NNN);
		opcodeTable.put(0x5, this::_5NNN);
		opcodeTable.put(0x6, this::_6NNN);
		opcodeTable.put(0x7, this::_7NNN);
		opcodeTable.put(0x8, this::_8NNN);
		opcodeTable.put(0x9, this::_9NNN);
		opcodeTable.put(0xA, this::_ANNN);
		opcodeTable.put(0xB, this::_BNNN);
		opcodeTable.put(0xC, this::_CXNN);
		opcodeTable.put(0xD, this::_DXYN);
		opcodeTable.put(0xE, this::_EXNN);
		opcodeTable.put(0xF, this::_FXNN);

	}

	public Cpu(int[] memory, Display display, Keyboard keyboard) {
		this.memory = memory;
		this.display = display;
		this.keyboard = keyboard;
	}

	@Override
	public void run() {
		int opcode = memory[pc];
		processOpcode(opcode);

		if (dt > 0) {
			dt--;
		}

		if (st > 0) {
			st--;
		}
	}

	public void processOpcode(int opcode) {
		int prefix = (opcode & 0xF000) >> 12;
		OPCodeProcessor opCodeProcessor = opcodeTable.get(prefix);
		opCodeProcessor.process(opcode);
	}

	private void _00NN(int opcode) {
		int suffix = opcode & 0x000F;

		if (suffix == 0x0) {
			display.clearScreen();
		}
		pc += 2;
	}

	private void _1NNN(int opcode) {
		pc = opcode & 0x0FFF;
	}

	private void _2NNN(int opcode) {
		stack[++sc] = pc;
		pc = opcode & 0x0FFF;
	}

	private void _3NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		if (registers[x] == (opcode & 0x00FF)) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _4NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		if (registers[x] != (opcode & 0x00FF)) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _5NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int y = (opcode & 0x00F0) >> 4;
		if (registers[x] == registers[y]) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _6NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		registers[x] = opcode & 0x00FF;
		pc += 2;
	}

	private void _7NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		registers[x] += opcode & 0x00FF;
		pc += 2;
	}

	private void _8NNN(int opcode) {
		int suffix = opcode & 0x000F;

		if (suffix == 0x0) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[y];
			pc += 2;
		}

		if (suffix == 0x1) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[x] | registers[y];
			pc += 2;
		}

		if (suffix == 0x2) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[x] & registers[y];
			pc += 2;
		}

		if (suffix == 0x3) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[x] ^ registers[y];
			pc += 2;
		}

		if (suffix == 0x4) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			int res = registers[x] + registers[y];
			if (res > 256) {
				registers[0xF] = 1;
			}
			registers[x] = res & 0xFF;
			pc += 2;
		}

		if (suffix == 0x5) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			int res = registers[x] - registers[y];
			if (res < 0) {
				registers[0xF] = 0;
			} else {
				registers[0xF] = 1;
			}
			registers[x] = res & 0xFF;
			pc += 2;
		}

		if (suffix == 0x6) {
			int x = (opcode & 0x0F00) >> 8;
			registers[0xF] = registers[x] & 1;
			registers[x] = registers[x] >> 1;
			pc += 2;
		}

		if (suffix == 0x7) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			int res = registers[y] - registers[x];
			if (res < 0) {
				registers[0xF] = 0;
			} else {
				registers[0xF] = 1;
			}
			registers[x] = res & 0xFF;
			pc += 2;
		}

		if (suffix == 0xE) {
			int x = (opcode & 0x0F00) >> 8;
			registers[0xF] = registers[x] & 1;
			registers[x] = registers[x] >> 1;
			pc += 2;
		}
	}

	private void _9NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int y = (opcode & 0x00F0) >> 4;
		if (registers[x] != registers[y]) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _ANNN(int opcode) {
		int addr = opcode & 0x0FFF;
		ireg = memory[addr];
		pc += 2;
	}

	private void _BNNN(int opcode) {
		int addr = opcode & 0x0FFF;
		pc = addr + registers[0];
	}

	private void _CXNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int addr = opcode & 0x00FF;
		registers[x] = addr & random.nextInt(256);
	}

	private void _DXYN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int y = (opcode & 0x00F0) >> 4;
		int n = opcode & 0x000F;

		int lreg = ireg;
		int flip = 0;

		for (int i = 0; i < n; i++) {
			int val = memory[lreg + i];
			for (int j = 8; j > 0; j--) {
				flip |= display.setPixel(x + j, y + i, val >> j);
			}
		}

		display.render();
		registers[0xF] = flip;
		pc += 2;
	}

	private void _EXNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int suffix = opcode & 0xF;

		if (suffix == 0xE && keyboard.isKeyPressed(registers[x])) {
			pc += 2;
		}

		if (suffix == 0x1 && !keyboard.isKeyPressed(registers[x])) {
			pc += 2;
		}

		pc += 2;
	}

	private void _FXNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int suffix = opcode & 0xFF;

		if (suffix == 0x07) {
			registers[x] = dt;
		}

		if (suffix == 0x0A) {
			registers[x] = keyboard.waitUntilPressed();
		}

		if (suffix == 0x15) {
			dt = registers[x];
		}

		if (suffix == 0x18) {
			st = registers[x];
		}

		if (suffix == 0x1E) {
			ireg += registers[x];
		}

		if (suffix == 0x29) {
			ireg = 0; //TODO add fonts
		}

		if (suffix == 0x33) {
			memory[ireg] = registers[x] / 100;
			memory[ireg + 1] = (registers[x] % 100) / 10;
			memory[ireg + 2] = (registers[x] % 100) % 10;
		}

		if (suffix == 0x55) {
			for (int i = 0; i < registers.length; i++) {
				memory[ireg + i] = registers[i];
			}
		}

		if (suffix == 0x65) {
			for (int i = 0; i < registers.length; i++) {
				registers[i] = memory[ireg + i];
			}
		}
		pc += 2;
	}

	private interface OPCodeProcessor {

		void process(int opCode);
	}

}