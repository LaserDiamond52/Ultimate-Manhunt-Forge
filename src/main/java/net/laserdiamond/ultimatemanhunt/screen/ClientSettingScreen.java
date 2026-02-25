package net.laserdiamond.ultimatemanhunt.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.client.ClientSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientSettingScreen extends Screen {

    private static final ResourceLocation GUI_TEXTURE = UltimateManhunt.fromUMPath("textures/gui/client_settings.png");
    private static final int WINDOW_WIDTH = 240;
    private static final int WINDOW_HEIGHT = 192;
    private static final int MAX_PAGES = 3;

    // Need to render the following:

    // 9 Sliders:
    // - Start, Pause, Resume, End Game Volume
    // - Heart Beat Volume
    // - Heart Flatline Volume
    // - Prowler Volume
    // - Hunter Vignette Intensity
    // - Grace Period Vignette Intensity
    //
    // Sliders will be vertical, so make sure they are LOCKED TO THE Y AXIS

    // 6 Buttons
    // - Mute button for:
    //   - Heart Beat
    //   - Heart Flatline
    //   - Prowler
    // - Enable/Disable button for:
    //   - Hunter Vignette
    //   - Grace Period Vignette
    //   - 3D Player Tracker

    // Multiple Pages should be used!

    private int currentPage;

    public ClientSettingScreen()
    {
        super(Component.literal("Settings"));
        this.currentPage = 1;
    }

    public int getCurrentPage()
    {
        return currentPage;
    }

    public void incrementPage()
    {
        this.currentPage = Math.min(this.currentPage + 1, MAX_PAGES);
    }

    public void decrementPage()
    {
        this.currentPage = Math.max(1, this.currentPage - 1);
    }

    public boolean isMaxPage()
    {
        return this.currentPage >= MAX_PAGES;
    }

    public boolean isMinPage()
    {
        return this.currentPage <= 1;
    }

    @Override
    protected void init()
    {
        super.init();

        int startX = ((this.width - WINDOW_WIDTH) / 2) + 50;
        int startY = ((this.height - WINDOW_HEIGHT * 7 / 3) / 2) + 150;

        this.addRenderableWidget(new PageButtonWidget(startX + 75, startY + 145, IconInfo.nextPage(0, 0), this::incrementPage, this::isMaxPage, List.of(Component.literal(ChatFormatting.GREEN + "Next Page"))));
        this.addRenderableWidget(new PageButtonWidget(startX + 45, startY + 145, IconInfo.previousPage(0, 0), this::decrementPage, this::isMinPage, List.of(Component.literal(ChatFormatting.GREEN + "Previous Page"))));

        this.addRenderableWidget(new SettingsSliderWidget(startX, startY, SettingsOptions.START_GAME, ClientSettings.START_GAME_VOLUME::setValue, 1));
        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 40, SettingsOptions.PAUSE_GAME, ClientSettings.PAUSE_GAME_VOLUME::setValue, 1));
        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 80, SettingsOptions.RESUME_GAME, ClientSettings.RESUME_GAME_VOLUME::setValue, 1));
        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 120, SettingsOptions.END_GAME, ClientSettings.END_GAME_VOLUME::setValue, 1));

        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 1, SettingsOptions.START_GAME, ClientSettings.START_GAME_VOLUME::isEnabled, ClientSettings.START_GAME_VOLUME::setEnabled, 1));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 41, SettingsOptions.PAUSE_GAME, ClientSettings.PAUSE_GAME_VOLUME::isEnabled, ClientSettings.PAUSE_GAME_VOLUME::setEnabled, 1));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 81, SettingsOptions.RESUME_GAME, ClientSettings.RESUME_GAME_VOLUME::isEnabled, ClientSettings.RESUME_GAME_VOLUME::setEnabled, 1));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 121, SettingsOptions.END_GAME, ClientSettings.END_GAME_VOLUME::isEnabled, ClientSettings.END_GAME_VOLUME::setEnabled, 1));

        this.addRenderableWidget(new SettingsSliderWidget(startX, startY, SettingsOptions.HUNTERS_RELEASED, ClientSettings.HUNTERS_RELEASED_VOLUME::setValue, 2));
        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 40, SettingsOptions.HEART_BEAT, ClientSettings.HEART_BEAT_VOLUME::setValue, 2));
        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 80, SettingsOptions.HEART_FLATLINE, ClientSettings.HEART_FLATLINE_VOLUME::setValue, 2));
        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 120, SettingsOptions.PROWLER, ClientSettings.PROWLER_VOLUME::setValue, 2));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 1, SettingsOptions.HUNTERS_RELEASED, ClientSettings.HUNTERS_RELEASED_VOLUME::isEnabled, ClientSettings.HUNTERS_RELEASED_VOLUME::setEnabled, 2));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 41, SettingsOptions.HEART_BEAT, ClientSettings.HEART_BEAT_VOLUME::isEnabled, ClientSettings.HEART_BEAT_VOLUME::setEnabled, 2));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 81, SettingsOptions.HEART_FLATLINE, ClientSettings.HEART_FLATLINE_VOLUME::isEnabled, ClientSettings.HEART_FLATLINE_VOLUME::setEnabled, 2));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 121, SettingsOptions.PROWLER, ClientSettings.PROWLER_VOLUME::isEnabled, ClientSettings.PROWLER_VOLUME::setEnabled, 2));

        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 40, SettingsOptions.HUNTER_VIGNETTE, ClientSettings.HUNTER_VIGNETTE::setValue, 3));
        this.addRenderableWidget(new SettingsSliderWidget(startX, startY + 80, SettingsOptions.GRACE_PERIOD_VIGNETTE, ClientSettings.GRACE_PERIOD_VIGNETTE::setValue, 3));

        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 41, SettingsOptions.HUNTER_VIGNETTE, ClientSettings.HUNTER_VIGNETTE::isEnabled, ClientSettings.HUNTER_VIGNETTE::setEnabled, 3));
        this.addRenderableWidget(new SettingsButtonWidget(startX - 30, startY + 81, SettingsOptions.GRACE_PERIOD_VIGNETTE, ClientSettings.GRACE_PERIOD_VIGNETTE::isEnabled, ClientSettings.GRACE_PERIOD_VIGNETTE::setEnabled, 3));
        this.addRenderableWidget(new SettingsButtonWidget(startX + 60, startY + 111, SettingsOptions.PLAYER_TRACKER, ClientSettings.PLAYER_TRACKER::isEnabled, ClientSettings.PLAYER_TRACKER::setEnabled, 3));

    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        int screenX = (this.width - WINDOW_WIDTH) / 2;
        int screenY = (this.height - WINDOW_HEIGHT) / 2;

        pGuiGraphics.blit(GUI_TEXTURE, screenX, screenY, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        String suffix = switch (this.currentPage)
        {
            case 1 -> "- Volume (1/2)";
            case 2 -> "- Volume (2/2)";
            case 3 -> "- Visuals (1/1)";
            default -> "";
        };
        String title = this.title.getString() + " " + suffix;
        int x = screenX + 130;
        pGuiGraphics.drawString(this.font, title, x - this.font.width(title) / 2, screenY + 7, ChatFormatting.DARK_GRAY.getColor(), false);

        this.renderables.forEach(renderable -> renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick));
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private class SettingsSliderWidget extends AbstractWidget implements PageRenderable
    {
        private final SettingsOptions settingsOptions;
        private int sliderX;
        private final int maxBarX;
        private final FloatConsumer consumer;
        private final int page;

        public SettingsSliderWidget(int x, int y, SettingsOptions settingsOptions, FloatConsumer consumer, int page)
        {
            super(x, y, 170, 20, Component.empty());
            this.settingsOptions = settingsOptions;
            this.sliderX = (int) (x + (162 * this.settingsOptions.getController().getValue()));
            this.maxBarX = this.getX() + 162;
            this.consumer = consumer;
            this.page = page;
        }

        @Override
        protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
        {
            this.active = this.shouldRender();
            if (!this.active)
            {
                return; // Not on correct page to render. Cancel
            }
            float floatFactor = this.settingsOptions.getController().getValue();

            pGuiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), 0, WINDOW_HEIGHT, this.width, this.height);
            pGuiGraphics.blit(GUI_TEXTURE, this.sliderX, this.getY(), 0, this.isHoveredOrFocused() ? 232 : 212, 8, this.height);
            DecimalFormat df = new DecimalFormat("0");
            Minecraft minecraft = Minecraft.getInstance();
            int textColor = this.isHoveredOrFocused() ? this.settingsOptions.selectedColor.getColor() : ChatFormatting.WHITE.getColor();
            pGuiGraphics.drawCenteredString(minecraft.font, Component.literal(this.settingsOptions.text + ": " + df.format(floatFactor * 100)), this.getX() + 88, this.getY() + 6, textColor);

            if (this.isHovered())
            {
                pGuiGraphics.renderTooltip(minecraft.font, this.settingsOptions.getBarTooltip(floatFactor), Optional.empty(), pMouseX, pMouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
        {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }

        @Override
        protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY)
        {
//            if (this.currentPage.get() != this.page)
//            {
//                return; // Not on correct page to render. Cancel
//            }
            int x = (int) Mth.clamp(pMouseX, this.getX(), this.maxBarX);
            this.sliderX = x;

            float newValue = ((x - this.maxBarX) / 162f) + 1f;
            this.consumer.accept(newValue);
        }

        @Override
        public int getPage()
        {
            return page;
        }

        @Override
        public Supplier<Integer> getCurrentPage()
        {
            return ClientSettingScreen.this::getCurrentPage;
        }
    }

    private class SettingsButtonWidget extends AbstractWidget implements PageRenderable
    {

        private final SettingsOptions settingsOptions;
        private final BooleanSupplier booleanSupplier;
        private final BooleanConsumer booleanConsumer;
        private final int page;

        private SettingsButtonWidget(int pX, int pY, SettingsOptions settingsOptions, BooleanSupplier boolGetter, BooleanConsumer boolSetter, int page)
        {
            super(pX, pY, 20, 18, Component.empty());
            this.settingsOptions = settingsOptions;
            this.booleanSupplier = boolGetter;
            this.booleanConsumer = boolSetter;
            this.page = page;
        }

        @Override
        protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
        {
            this.active = this.shouldRender();
            if (!active)
            {
                return;
            }
            boolean flag = this.isHoveredOrFocused();
            int u = 8;
            int v = flag ? 230 : 212;
            pGuiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), u, v, this.width, this.height);

            boolean flag2 = this.booleanSupplier.getAsBoolean();
            IconInfo iconInfo = this.settingsOptions.getIconInfo();
            pGuiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), iconInfo.getU(!flag2), iconInfo.getV(!flag2), iconInfo.uWidth, iconInfo.vHeight);

            if (this.isHovered)
            {
                Minecraft minecraft = Minecraft.getInstance();
                pGuiGraphics.renderTooltip(minecraft.font, this.settingsOptions.getButtonTooltipButton(flag2), Optional.empty(), pMouseX, pMouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
        {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }

        @Override
        public void onClick(double pMouseX, double pMouseY)
        {
            boolean currentValue = this.booleanSupplier.getAsBoolean();
            this.booleanConsumer.accept(!currentValue);
        }

        @Override
        public int getPage()
        {
            return page;
        }

        @Override
        public Supplier<Integer> getCurrentPage()
        {
            return ClientSettingScreen.this::getCurrentPage;
        }
    }

    private static class PageButtonWidget extends AbstractWidget
    {
        private final IconInfo iconInfo;
        private final PageChanger pageChanger;
        private final Supplier<Boolean> cannotSwitchPage;
        private final List<Component> hoverTooltip;

        public PageButtonWidget(int pX, int pY, IconInfo iconInfo, PageChanger pageChanger, Supplier<Boolean> cannotSwitchPage, List<Component> hoverTooltip)
        {
            super(pX, pY, 20, 18, Component.empty());
            this.pageChanger = pageChanger;
            this.cannotSwitchPage = cannotSwitchPage;
            this.iconInfo = iconInfo;
            this.hoverTooltip = hoverTooltip;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
        {
            int buttonU = 8;
            int buttonV = 212;
            boolean cannotSwitchPage = this.cannotSwitchPage.get();
            if (cannotSwitchPage)
            {
                guiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), buttonU, 212, this.width, this.height);
                guiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), this.iconInfo.oppositeU, this.iconInfo.oppositeV, this.width, this.height);
                return;
            }
            boolean isHovered = this.isHovered();
            guiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), buttonU, buttonV, this.width, this.height);
            guiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), this.iconInfo.getU(!isHovered), this.iconInfo.getV(!isHovered), this.width, this.height);

            if (isHovered)
            {
                Minecraft minecraft = Minecraft.getInstance();
                guiGraphics.renderTooltip(minecraft.font, this.hoverTooltip, Optional.empty(), mouseX, mouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
        {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public void onClick(double pMouseX, double pMouseY)
        {
            this.pageChanger.onPageChange(); // Change based on the current page
        }

        @FunctionalInterface
        public interface PageChanger
        {
            void onPageChange();
        }
    }

    private interface PageRenderable
    {
        int getPage();

        Supplier<Integer> getCurrentPage();

        default boolean shouldRender()
        {
            return this.getCurrentPage().get() == this.getPage();
        }
    }

    private record IconInfo(int u, int v, int uWidth, int vHeight, int oppositeU, int oppositeV, int oppositeUWidth, int oppositeVHeight, int xOffset, int yOffset)
    {
        public static IconInfo volumeIconInfo(int xOffset, int yOffset)
        {
            return new IconInfo(28, 212, 20, 18,28, 230, 20, 18, xOffset, yOffset);
        }

        public static IconInfo visibilityIconInfo(int xOffset, int yOffset)
        {
            return new IconInfo(48, 212, 20, 18, 48, 230, 20, 18, xOffset, yOffset);
        }

        public static IconInfo trackerIconInfo(int xOffset, int yOffset)
        {
            return new IconInfo(68, 212, 20, 18, 68, 230, 20, 18, xOffset, yOffset);
        }

        public int getU(boolean isOpposite)
        {
            return isOpposite ? this.oppositeU : this.u;
        }

        public int getV(boolean isOpposite)
        {
            return isOpposite ? this.oppositeV : this.v;
        }

        public static IconInfo nextPage(int xOffset, int yOffset)
        {
            return new IconInfo(108, 212, 20, 18, 88, 212, 20, 18, xOffset, yOffset);
        }

        public static IconInfo previousPage(int xOffset, int yOffset)
        {
            return new IconInfo(108, 230, 20, 18, 88, 230, 20, 18, xOffset, yOffset);
        }
    }

    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0");

    private enum SettingsOptions
    {
        START_GAME ("Start Game Volume", ChatFormatting.AQUA, IconInfo.volumeIconInfo(0, 0), ClientSettings.START_GAME_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.AQUA + "Start Game Volume"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the sound that indicates"),
                        Component.literal(ChatFormatting.GRAY + "a Manhunt game has started."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        PAUSE_GAME ("Pause Game Volume", ChatFormatting.AQUA, IconInfo.volumeIconInfo(0, 0), ClientSettings.PAUSE_GAME_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.AQUA + "Pause Game Volume"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the sound that indicates"),
                        Component.literal(ChatFormatting.GRAY + "a Manhunt game has been paused."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        RESUME_GAME ("Resume Game Volume", ChatFormatting.AQUA, IconInfo.volumeIconInfo(0, 0), ClientSettings.RESUME_GAME_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.AQUA + "Resume Game Volume"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the sound that indicates"),
                        Component.literal(ChatFormatting.GRAY + "a Manhunt game has been resumed."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        END_GAME ("End Game Volume", ChatFormatting.AQUA, IconInfo.volumeIconInfo(0, 0), ClientSettings.END_GAME_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.AQUA + "End Game Volume"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the sound that indicates"),
                        Component.literal(ChatFormatting.GRAY + "a Manhunt game has ended."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        HUNTERS_RELEASED ("Hunters Released Volume", ChatFormatting.RED, IconInfo.volumeIconInfo(0, 0), ClientSettings.HUNTERS_RELEASED_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.RED + "Hunters Released Volume"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the sound that indicates"),
                        Component.literal(ChatFormatting.GRAY + "the hunters have been released."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        HEART_BEAT ("Heart Beat Volume", ChatFormatting.RED, IconInfo.volumeIconInfo(0, 0), ClientSettings.HEART_BEAT_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.RED + "Heart Beating Volume"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the heart beating that plays"),
                        Component.literal(ChatFormatting.GRAY + "when a hunter is close."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        HEART_FLATLINE ("Heart Flatline Volume", ChatFormatting.RED, IconInfo.volumeIconInfo(0, 0), ClientSettings.HEART_FLATLINE_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.RED + "Heart Flatline Volume"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the heart flatlining sound"),
                        Component.literal(ChatFormatting.GRAY + "that plays when you die near a hunter while the"),
                        Component.literal(ChatFormatting.GRAY + "heart beating sound is playing."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        PROWLER ("Hunter Detection Music", ChatFormatting.LIGHT_PURPLE, IconInfo.volumeIconInfo(0, 0), ClientSettings.PROWLER_VOLUME,
                (value) -> List.of(
                        Component.literal(ChatFormatting.LIGHT_PURPLE + "Hunter Detection Music"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the volume of the hunter detection music that"),
                        Component.literal(ChatFormatting.GRAY + "plays when a hunter is close."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Volume: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::volumeButtonTooltip),
        HUNTER_VIGNETTE ("Hunter Detection Vignette", ChatFormatting.RED, IconInfo.visibilityIconInfo(0, 0), ClientSettings.HUNTER_VIGNETTE,
                (value) -> List.of(
                        Component.literal(ChatFormatting.RED + "Hunter Detection Vignette"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the intensity of the red vignette that appears"),
                        Component.literal(ChatFormatting.GRAY + "when a hunter is near."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Intensity: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::enableButtonTooltip),
        GRACE_PERIOD_VIGNETTE ("Grace Period Vignette", ChatFormatting.BLUE, IconInfo.visibilityIconInfo(0, 0), ClientSettings.GRACE_PERIOD_VIGNETTE,
                (value) -> List.of(
                        Component.literal(ChatFormatting.BLUE + "Grace Period Vignette"),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Controls the intensity of the blue vignette that appears"),
                        Component.literal(ChatFormatting.GRAY + "when on grace period after being killed by a hunter."),
                        Component.empty(),
                        Component.literal(ChatFormatting.GRAY + "Current Intensity: " + ChatFormatting.YELLOW + FLOAT_FORMAT.format(value)),
                        Component.empty()
                ), SettingsOptions::enableButtonTooltip),
        PLAYER_TRACKER ("Render Player Tracker", ChatFormatting.GOLD, IconInfo.trackerIconInfo(0, 0), ClientSettings.PLAYER_TRACKER,
                (value) -> List.of(), SettingsOptions::trackerEnableTooltip);

        public static List<Component> volumeButtonTooltip(boolean enabled)
        {
            String s = enabled ? ChatFormatting.RED + "Click to Mute" : ChatFormatting.GREEN + "Click to Unmute";
            return List.of(
                    Component.literal(s)
            );
        }

        public static List<Component> enableButtonTooltip(boolean enabled)
        {
            String s = enabled ? ChatFormatting.RED + "Click to Disable" : ChatFormatting.GREEN + "Click to Enable";
            return List.of(
                    Component.literal(s)
            );
        }

        public static List<Component> trackerEnableTooltip(boolean enabled)
        {
            String s = enabled ? ChatFormatting.RED + "Click to Disable" : ChatFormatting.GREEN + "Click to Enable";
            return List.of(
                    Component.literal(ChatFormatting.GOLD + "3D Player Tracker"),
                    Component.empty(),
                    Component.literal(ChatFormatting.GRAY + "Toggles the 3D Player Tracker"),
                    Component.empty(),
                    Component.literal(s)
            );
        }
        public final String text;
        public final ChatFormatting selectedColor;
        private final IconInfo iconInfo;
        private final ClientSettings.FloatBooleanController controller;
        private final Function<Float, List<Component>> barTooltip;
        private final Function<Boolean, List<Component>> buttonTooltipButton;

        SettingsOptions(String text, ChatFormatting selectedColor, IconInfo iconInfo, ClientSettings.FloatBooleanController controller, Function<Float, List<Component>> barTooltip, Function<Boolean, List<Component>> buttonTooltipButton)
        {
            this.text = text;
            this.selectedColor = selectedColor;
            this.iconInfo = iconInfo;
            this.controller = controller;
            this.barTooltip = barTooltip;
            this.buttonTooltipButton = buttonTooltipButton;
        }

        public IconInfo getIconInfo()
        {
            return this.iconInfo;
        }

        public ClientSettings.FloatBooleanController getController()
        {
            return this.controller;
        }

        public List<Component> getBarTooltip(float value)
        {
            return this.barTooltip.apply(value * 100);
        }

        public List<Component> getButtonTooltipButton(boolean enabled)
        {
            return this.buttonTooltipButton.apply(enabled);
        }
    }
}
