package lol.tgformat.module.impl.render;

import lol.tgformat.Client;
import lol.tgformat.api.event.Listener;
import lol.tgformat.events.render.Render2DEvent;
import lol.tgformat.module.ModuleType;
import lol.tgformat.module.values.ParentAttribute;
import lol.tgformat.module.values.impl.BooleanSetting;
import lol.tgformat.module.values.impl.ModeSetting;
import lol.tgformat.module.values.impl.MultipleBoolSetting;
import lol.tgformat.module.values.impl.NumberSetting;
import lol.tgformat.ui.clickgui.ModuleCollection;
import lol.tgformat.ui.drag.Dragging;
import lol.tgformat.ui.font.AbstractFontRenderer;
import lol.tgformat.ui.font.Pair;
import lol.tgformat.ui.utils.Animation;
import lol.tgformat.ui.utils.Direction;
import lol.tgformat.ui.utils.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StringUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import lol.tgformat.module.Module;
import net.netease.utils.ColorUtil;

import static lol.tgformat.ui.clickgui.Utils.*;

public class ArrayListMod extends Module {

    public final BooleanSetting importantModules = new BooleanSetting("Important", false);
    private final ModeSetting textShadow = new ModeSetting("Text Shadow", "Black", "Colored", "Black", "None");
    private final ModeSetting rectangle = new ModeSetting("Rectangle", "Top", "None", "Top", "Side", "Outline");
    private final BooleanSetting test = new BooleanSetting("test", true);
    private final MultipleBoolSetting fontSettings = new MultipleBoolSetting("Font Settings",
            new BooleanSetting("Bold", false),
            new BooleanSetting("Small Font", false), test);
    public final NumberSetting height = new NumberSetting("Height", 11, 20, 9, .5f);
    private final ModeSetting animation = new ModeSetting("Animation", "Scale in", "Move in", "Scale in");
    private final NumberSetting colorIndex = new NumberSetting("Color Seperation", 20, 100, 5, 1);
    private final NumberSetting colorSpeed = new NumberSetting("Color Speed", 15, 30, 2, 1);
    private final BooleanSetting background = new BooleanSetting("Background", true);
    private final BooleanSetting backgroundColor = new BooleanSetting("Background Color", false);
    private final NumberSetting backgroundAlpha = new NumberSetting("Background Alpha", .35, 1, 0, .01);
    private final Dragging arraylistDrag = Client.instance.createDrag(this,"testarraylist", 2, 1);

    public AbstractFontRenderer font = tenacityFont.size(20);
    public List<Module> modules;

    public ArrayListMod() {
        super("ArrayList", ModuleType.Render, "Displays your active modules");
        backgroundAlpha.addParent(background, ParentAttribute.BOOLEAN_CONDITION);
        backgroundColor.addParent(background, ParentAttribute.BOOLEAN_CONDITION);
        arraylistDrag.setWidth(50);
        arraylistDrag.setHeight(50);
    }

    public void getModulesAndSort() {
        if (modules == null || ModuleCollection.reloadModules) {
            modules = Client.instance.getModuleCollection().getModules();
        }
        modules.sort(Comparator.<Module>comparingDouble(m -> {
            String name = HUD.get(m.getName() + (m.hasMode() ? " " + m.getSuffix() : ""));
            return font.getStringWidth(applyText(name));
        }).reversed());
    }

    public String longest = "";

    Module lastModule;
    int lastCount;

    @Listener
    public void onRender2DEvent(Render2DEvent e) {
        font = getFont();
        getModulesAndSort();

        String longestModule = "";
        float longestWidth = 0;
        double yOffset = 0;
        ScaledResolution sr = new ScaledResolution(mc);
        int count = 0;
        for (Module module : modules) {
            if (importantModules.getConfigValue() && module.getCategory() == ModuleType.Render) continue;
            final Animation moduleAnimation = module.getAnimation();

            moduleAnimation.setDirection(module.isState() ? Direction.FORWARDS : Direction.BACKWARDS);

            if (!module.isState() && moduleAnimation.finished(Direction.BACKWARDS)) continue;


            String displayText = HUD.get(module.getName() + (module.hasMode() ? (" §7") + module.getSuffix() : ""));
            displayText = applyText(displayText);
            float textWidth = font.getStringWidth(displayText);

            if (textWidth > longestWidth) {
                longestModule = displayText;
                longestWidth = textWidth;
            }

            double xValue = sr.getScaledWidth() - (arraylistDrag.getX());


            boolean flip = xValue <= sr.getScaledWidth() / 2f;
            float x = (float) (flip ? xValue : sr.getScaledWidth() - (textWidth + arraylistDrag.getX()));


            float alphaAnimation = 1;

            float y = (float) (yOffset + arraylistDrag.getY());

            float heightVal = (float) (height.getValue() + 1);

            switch (animation.getMode()) {
                case "Move in":
                    if (flip) {
                        x -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getScaledWidth() - (arraylistDrag.getX() - textWidth)));
                    } else {
                        x += Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getX() + textWidth));
                    }
                    break;
                case "Scale in":
                    if (!moduleAnimation.isDone()) {
                        RenderUtil.scaleStart(x + font.getStringWidth(displayText) / 2f, y + heightVal / 2 - font.getHeight() / 2f, (float) moduleAnimation.getOutput().floatValue());
                    }
                    alphaAnimation = (float) moduleAnimation.getOutput().floatValue();
                    break;
            }


            int index = (int) (count * colorIndex.getValue());
            Pair<Color, Color> colors = HUD.getClientColors();

            Color textcolor = ColorUtil.interpolateColorsBackAndForth(colorSpeed.getValue().intValue(), index, colors.getFirst(), colors.getSecond(), false);

            if (background.getConfigValue()) {
                float offset = 5;
                Color color = backgroundColor.getConfigValue() ? textcolor : new Color(10, 10, 10);
                Gui.drawRect2(x - 2, y, font.getStringWidth(displayText) + offset, heightVal,
                        ColorUtil.applyOpacity(color, backgroundAlpha.getValue().floatValue() * alphaAnimation).getRGB());
            }

            float offset = 0;
            switch (rectangle.getMode()) {
                default:
                    break;
                case "Top":
                    if (count == 0) {
                        Gui.drawRect2(x - 2, y - 1, textWidth + 5 - offset, 1, textcolor.getRGB());
                    }
                    break;
                case "Side":
                    if (flip) {
                        Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                    } else {
                        Gui.drawRect2(x + textWidth + 2, y, 1, heightVal, textcolor.getRGB());
                    }
                    break;
                case "Outline":
                    if (count != 0) {
                        String modText = applyText(HUD.get(lastModule.getName() + (lastModule.hasMode() ? " " + lastModule.getSuffix() : "")));
                        float texWidth = font.getStringWidth(modText) - textWidth;
                        //Draws the difference of width rect and also the rect on the side of the text
                        if (flip) {
                            Gui.drawRect2(x + textWidth + 3, y, 1, heightVal, textcolor.getRGB());
                            Gui.drawRect2(x + textWidth + 3, y, texWidth + 1, 1, textcolor.getRGB());
                        } else {
                            Gui.drawRect2(x - (3 + texWidth), y, texWidth + 1, 1, textcolor.getRGB());
                            Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                        }
                        if (count == (lastCount - 1)) {
                            Gui.drawRect2(x - 3, y + heightVal, textWidth + 6, 1, textcolor.getRGB());
                        }
                    } else {
                        //Draws the rects for the first module in the count
                        if (flip) {
                            Gui.drawRect2(x + textWidth + 3, y, 1, heightVal, textcolor.getRGB());
                        } else {
                            Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                        }

                        //Top Bar rect
                        Gui.drawRect2(x - 3, y - 1, textWidth + 6, 1, textcolor.getRGB());
                    }
                    //sidebar
                    if (flip) {
                        Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                    } else {
                        Gui.drawRect2(x + textWidth + 2, y, 1, heightVal, textcolor.getRGB());
                    }


                    break;
            }


            float textYOffset = (float) 0;
            y += textYOffset;
            Color color = ColorUtil.applyOpacity(textcolor, alphaAnimation);
            switch (textShadow.getMode()) {
                case "None":
                    font.drawString(displayText, x, y + font.getMiddleOfBox(heightVal), color.getRGB());
                    break;
                case "Colored":
                    RenderUtil.resetColor();
                    font.drawString(StringUtils.stripColorCodes(displayText), x + 1, y + font.getMiddleOfBox(heightVal) + 1, ColorUtil.darker(color, .5f).getRGB());
                    RenderUtil.resetColor();
                    font.drawString(displayText, x, y + font.getMiddleOfBox(heightVal), color.getRGB());
                    break;
                case "Black":
                    RenderUtil.resetColor();
                    float f = .5f;
                    font.drawString(StringUtils.stripColorCodes(displayText), x + f, y + font.getMiddleOfBox(heightVal) + f,
                            ColorUtil.applyOpacity(Color.BLACK, alphaAnimation));
                    RenderUtil.resetColor();
                    font.drawString(displayText, x, y + font.getMiddleOfBox(heightVal), color.getRGB());
                    break;
            }


            //  font.drawString(displayText, x, (y - 3) + font.getMiddleOfBox(heightVal), color.getRGB());

            if (animation.is("Scale in") && !moduleAnimation.isDone()) {
                RenderUtil.scaleEnd();
            }

            lastModule = module;

            yOffset += moduleAnimation.getOutput().floatValue() * heightVal;
            count++;
        }
        lastCount = count;
        longest = longestModule;
    }

    private String applyText(String text) {
        return text;
    }


    private AbstractFontRenderer getFont() {
        boolean smallFont = fontSettings.getSetting("Small Font").getConfigValue();

        if (fontSettings.getSetting("Bold").getConfigValue()) {
            if (smallFont) {
                return tenacityBoldFont18;
            }
            return tenacityBoldFont20;
        }

        return smallFont ? tenacityFont18 : tenacityFont20;
    }

}