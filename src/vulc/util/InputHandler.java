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

	public static enum KeyAction {
		PRESS, RELEASE
	}

	public static final int UNBIND = -1;
	public static final int KEYBOARD = 0;
	public static final int MOUSE = 1;

	private final List<KeyReference> keys = new ArrayList<KeyReference>();

	@SuppressWarnings("rawtypes")
	private final HashMap[] keyGroups = {
	    new HashMap<Integer, KeyReference>(), // KEYBOARD
	    new HashMap<Integer, KeyReference>()  // MOUSE
	};

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
		for(KeyReference key : keys) {
			key.tick();
		}
		xMouse = xMouseToTick;
		yMouse = yMouseToTick;
	}

	private void receiveInput(KeyAction action, int type, int code) {
		KeyReference key = getGroup(type).get(code);
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

	@SuppressWarnings("unchecked")
	private HashMap<Integer, KeyReference> getGroup(int type) {
		return keyGroups[type];
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		receiveInput(KeyAction.PRESS, KEYBOARD, e.getKeyCode());
	}

	public void keyReleased(KeyEvent e) {
		receiveInput(KeyAction.RELEASE, KEYBOARD, e.getKeyCode());
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		receiveInput(KeyAction.PRESS, MOUSE, e.getButton());
	}

	public void mouseReleased(MouseEvent e) {
		receiveInput(KeyAction.RELEASE, MOUSE, e.getButton());
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
		for(KeyReference key : keys) {
			if(!key.toTickWasReleased) {
				key.toTickReleaseCount++;
				key.toTickWasReleased = true;
			}
		}
	}

	private class KeyReference {

		private final int type;
		private final int code;

		private int links = 0;

		private int toTickPressCount = 0;
		private int toTickReleaseCount = 0;

		private boolean toTickWasReleased = true;

		private int pressCount;
		private int releaseCount;

		private boolean shouldStayDown = false;
		private boolean isKeyDown = false;

		private KeyReference(int type, int code) {
			this.type = type;
			this.code = code;

			keys.add(this);
			getGroup(type).put(code, this);
		}

		private void unbind() {
			links--;
			if(links == 0) {
				keys.remove(this);
				getGroup(type).remove(code);
			}
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

	}

	public class Key {

		private KeyReference reference;

		public Key() {
		}

		public Key(int type, int code) {
			setKeyBinding(type, code);
		}

		public void setKeyBinding(int type, int code) {
			unbind();

			reference = getGroup(type).get(code);
			if(reference == null) {
				reference = new KeyReference(type, code);
				getGroup(type).put(code, reference);
			}
			reference.links++;
		}

		public void unbind() {
			if(reference != null) {
				reference.unbind();
				reference = null;
			}
		}

		public int getType() {
			if(reference != null) return reference.type;
			else return -1;
		}

		public int getCode() {
			if(reference != null) return reference.code;
			else return -1;
		}

		public boolean down() {
			if(reference != null) return reference.isKeyDown;
			else return false;
		}

		public boolean pressed() {
			if(reference != null) return reference.pressCount != 0;
			else return false;
		}

		public boolean released() {
			if(reference != null) return reference.releaseCount != 0;
			else return false;
		}

		public int pressCount() {
			if(reference != null) return reference.pressCount;
			else return 0;
		}

		public int releaseCount() {
			if(reference != null) return reference.releaseCount;
			else return 0;
		}

	}

}
