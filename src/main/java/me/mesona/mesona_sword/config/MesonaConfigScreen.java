package me.mesona.mesona_sword.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

public class MesonaConfigScreen extends Screen {

    private final Screen parent;

    public MesonaConfigScreen(Screen parent) {
        super(Component.translatable("config.mesona_sword.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.addRenderableWidget(Button.builder(
                getStarfieldButtonText(),
                button -> {
                    MesonaConfig.STARFIELD_ENABLED.set(!MesonaConfig.STARFIELD_ENABLED.get());
                    MesonaConfig.SPEC.save();
                    button.setMessage(getStarfieldButtonText());
                })
                .pos(centerX - 100, this.height / 2 - 20)
                .size(200, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> this.onClose())
                .pos(centerX - 100, this.height / 2 + 20)
                .size(200, 20)
                .build());
    }

    private Component getStarfieldButtonText() {
        boolean enabled = MesonaConfig.STARFIELD_ENABLED.get();
        return Component.translatable("config.mesona_sword.starfield")
                .append(": ")
                .append(Component.translatable(enabled ? "config.mesona_sword.on" : "config.mesona_sword.off"));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    public static IConfigScreenFactory createFactory() {
        return (modContainer, screen) -> new MesonaConfigScreen(screen);
    }
}