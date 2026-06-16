package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Keybinds {

	public static final List<Binding> BINDINGS = new ArrayList<>();

	public enum State {
		DOWN,
		UP,
		CLICK
	}

	public static class Binding {
		public final KeyMapping mapping;
		public final Consumer<State> handler;

		public boolean isDown = false;

		public Binding(KeyMapping mapping, Consumer<State> handler) {
			this.mapping = mapping;
			this.handler = handler;
		}

		public void down() {
			if (isDown) return;
			isDown = true;

			handler.accept(State.DOWN);
		}
		public void up() {
			if (!isDown) return;
			isDown = false;

			handler.accept(State.UP);
		}
		public void click() {
			handler.accept(State.CLICK);
		}
	}

	private static void handleHandheld(State state) {
		if (state == State.DOWN) {
			CommonSimpleRadio.info("down");
		} else if (state == State.UP) {
			CommonSimpleRadio.info("up");
		}
	}

	// -------- Bindings -------- \\

	public static Binding HANDHELD = bind(new KeyMapping(
			"key.simpleradio.use_handheld",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_G,
			"key.categories.simpleradio.simpleradio"
	), Keybinds::handleHandheld);

	private static Binding bind(KeyMapping mapping, Consumer<State> handler) {
		Binding binding = new Binding(mapping, handler);
		BINDINGS.add(binding);
		return binding;
	}

	public static void processBinding(Binding binding) {
		KeyMapping mapping = binding.mapping;

		while (mapping.consumeClick()) binding.click();
		if (mapping.isDown()) {
			binding.down();
		} else {
			binding.up();
		}
	}
}
