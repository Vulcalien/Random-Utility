/*******************************************************************************
 * Copyright 2019-2020 Vulcalien
 * This code or part of it is licensed under MIT License by Vulcalien
 ******************************************************************************/
package vulc.util;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InputHandler implements KeyListener,
                          MouseListener,
                          MouseMotionListener,
                          FocusListener {

	public static enum KeyType {
		KEYBOARD, MOUSE
	}

	public static enum KeyAction {
		PRESS, RELEASE
	}

	private final List<Key> keys = new ArrayList<Key>();
	private final HashMap<Integer, Key> keyboardKeys = new HashMap<Integer, Key>();
	private final HashMap<Integer, Key> mouseKeys = new HashMap<Integer, Key>();

	private int xMouseToTick = -1, yMouseToTick = -1;
	public int xMouse = -1, yMouse = -1;

	public void init(Component component) {
		component.setFocusTraversalKeysEnabled(false);

		component.addKeyListener(this);
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		component.addFocusListener(this);
	}

	public void tick() {
		for(int i = 0; i < keys.size(); i++) {
			keys.get(i).tick();
		}
		xMouse = xMouseToTick;
		yMouse = yMouseToTick;
	}

	private void receiveInput(KeyAction action, KeyType type, int code) {
		Key key = getGroup(type).get(code);
		if(key == null) return;

		if(action == KeyAction.PRESS) {
			if(key.toTickWasReleased) {
				key.toTickPressCount++;
				key.toTickWasReleased = false;
			}
		} else if(action == KeyAction.RELEASE) {
			key.toTickReleaseCount++;
			key.toTickWasReleased = true;
		}
	}

	private HashMap<Integer, Key> getGroup(KeyType type) {
		switch(type) {
			case KEYBOARD:
				return keyboardKeys;

			case MOUSE:
				return mouseKeys;

			default:
				return null;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		receiveInput(KeyAction.PRESS, KeyType.KEYBOARD, e.getKeyCode());
	}

	public void keyReleased(KeyEvent e) {
		receiveInput(KeyAction.RELEASE, KeyType.KEYBOARD, e.getKeyCode());
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		receiveInput(KeyAction.PRESS, KeyType.MOUSE, e.getButton());
	}

	public void mouseReleased(MouseEvent e) {
		receiveInput(KeyAction.RELEASE, KeyType.MOUSE, e.getButton());
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		xMouseToTick = e.getX();
		yMouseToTick = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
		xMouseToTick = e.getX();
		yMouseToTick = e.getY();
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		for(Key key : keys) {
			if(!key.toTickWasReleased) {
				key.toTickReleaseCount++;
				key.toTickWasReleased = true;
			}
		}
	}

	public class Key {

		private KeyType type;
		private int code;

		private int toTickPressCount = 0;
		private int toTickReleaseCount = 0;

		private boolean toTickWasReleased = true;

		private int pressCount;
		private int releaseCount;

		private boolean shouldStayDown = false;
		private boolean isKeyDown = false;

		public Key() {
		}

		public Key(KeyType type, int code) {
			init(type, code);
		}

		private void init(KeyType type, int code) {
			if(this.type == null) keys.add(this);
			getGroup(type).put(code, this);

			this.type = type;
			this.code = code;
		}

		private void tick() {
			pressCount = toTickPressCount;
			releaseCount = toTickReleaseCount;

			toTickPressCount = 0;
			toTickReleaseCount = 0;

			if(pressCount != 0) shouldStayDown = true;
			isKeyDown = shouldStayDown;
			if(releaseCount != 0) shouldStayDown = false;
		}

		public KeyType getType() {
			return type;
		}

		public int getCode() {
			return code;
		}

		public void setKeyBinding(KeyType newType, int newCode) {
			if(type != null) getGroup(type).remove(code);
			init(newType, newCode);
		}

		public void unbind() {
			if(type == null) return;

			getGroup(type).remove(code);
			keys.remove(this);
			type = null;
		}

		public boolean isKeyDown() {
			return isKeyDown;
		}

		public boolean isPressed() {
			return pressCount != 0;
		}

		public boolean isReleased() {
			return releaseCount != 0;
		}

		public int pressCount() {
			return pressCount;
		}

		public int releaseCount() {
			return releaseCount;
		}

	}

}
