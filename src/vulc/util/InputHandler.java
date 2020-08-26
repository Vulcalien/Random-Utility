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
	private final List<Key> keyboardKeys = new ArrayList<Key>();
	private final List<Key> mouseKeys = new ArrayList<Key>();

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
	}

	private void receiveInput(KeyAction action, KeyType type, int code) {
		List<Key> keys = getList(type);
		for(Key key : keys) {
			if(key.code == code) {
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
		}
	}

	private List<Key> getList(KeyType type) {
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
		xMouse = e.getX();
		yMouse = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
		xMouse = e.getX();
		yMouse = e.getY();
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
			this.type = type;
			this.code = code;

			keys.add(this);
			getList(type).add(this);
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

		public void setKeyBinding(KeyType newType, int newCode) {
			if(this.type != null) getList(this.type).remove(this);
			init(newType, newCode);
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
