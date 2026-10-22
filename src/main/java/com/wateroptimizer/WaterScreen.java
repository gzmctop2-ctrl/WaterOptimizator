package com.wateroptimizer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class WaterScreen extends Screen {
    private static final int BIND_W = 40;
    private static final int KEY_W = 60;
    private static final int H = 20;
    private static final int MARGIN = 10;

    private static final int NONE = 0, ACTION = 1, MENU = 2;
    private int waiting = NONE;

    private ButtonWidget actionKeyButton;
    private ButtonWidget menuKeyButton;

    public WaterScreen() {
        super(Text.literal("WaterOptimizer"));
    }

    @Override
    protected void init() {
        // Left: action bind (function key, default K)
        int lx = MARGIN;
        int y = MARGIN;
        addDrawableChild(ButtonWidget.builder(Text.literal("Bind"), b -> { })
                .dimensions(lx, y, BIND_W, H).build());
        actionKeyButton = addDrawableChild(ButtonWidget.builder(keyText(Config.actionKey), b -> {
            waiting = ACTION;
            refresh();
        }).dimensions(lx + BIND_W, y, KEY_W, H).build());

        // Left: speed slider under it
        addDrawableChild(new SpeedSlider(lx, y + H + 6, BIND_W + KEY_W, H));

        // Top-right: menu bind (key that opens this HUD, default Right Shift)
        int rx = width - MARGIN - BIND_W - KEY_W;
        addDrawableChild(ButtonWidget.builder(Text.literal("Bind"), b -> { })
                .dimensions(rx, y, BIND_W, H).build());
        menuKeyButton = addDrawableChild(ButtonWidget.builder(keyText(Config.menuKey), b -> {
            waiting = MENU;
            refresh();
        }).dimensions(rx + BIND_W, y, KEY_W, H).build());
    }

    private Text keyText(int code) {
        return InputUtil.Type.KEYSYM.createFromCode(code).getLocalizedText();
    }

    private void refresh() {
        actionKeyButton.setMessage(waiting == ACTION ? Text.literal("> <") : keyText(Config.actionKey));
        menuKeyButton.setMessage(waiting == MENU ? Text.literal("> <") : keyText(Config.menuKey));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waiting != NONE) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                if (waiting == ACTION) Config.actionKey = keyCode;
                else Config.menuKey = keyCode;
                Config.save();
            }
            waiting = NONE;
            refresh();
            return true;
        }
        if (keyCode == Config.menuKey) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // No dark/blur background: light HUD over the game.
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        Config.save();
    }

    private static class SpeedSlider extends SliderWidget {
        SpeedSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Text.empty(), Config.speed);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Speed: " + Math.round(value * 100) + "%"));
        }

        @Override
        protected void applyValue() {
            Config.speed = (float) value;
        }
    }
}
