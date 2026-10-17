package com.createicbm.lib;

import com.createicbm.CreateICBM;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Utility class to handle working with Strings
 *
 * @author Darkguardsman
 *         based on Calclavia version from Resonant Engine but has been mostly rewritten so no longer marked as author
 */
public class LanguageUtility
{
    public static int toolTipLineLength = 30;

    /**
     * Grabs the localization for the string provided. Make sure the string
     * matches the exact key in a translation file.
     *
     * @param key - translation key, Example 'tile.sometile.name' or 'tile.modname:sometile.name'
     * @return translated key, or the same string provided if the key didn't match anything
     */
    public static String getLocal(String key)
    {
        //Check for empty or null keys
        if (key == null || key.isEmpty())
        {
            if (CreateICBM.runningAsDev)
            {
                CreateICBM.logger().error("LanguageUtility.getLocal(" + key + ") - invalid key", new RuntimeException());
            }
            return "error.key.empty";
        }

        //Get translation
        String translation = translate(key);
        if (translation == null || translation.isEmpty())
        {
            if (CreateICBM.runningAsDev)
            {
                CreateICBM.logger().error("LanguageUtility.getLocal(" + key + ") - no translation", new RuntimeException());
            }
            return key;
        }
        return translation;
    }

    /**
     * {@link Language} is loaded on both the dedicated server (en_us only) and the client (selected language).
     *
     * @param key to translate
     * @return translation or null if the key is unknown
     */
    private static String translate(String key)
    {
        final Language language = Language.getInstance();
        return language.has(key) ? language.getOrDefault(key) : null;
    }

    public static List<String> splitByLine(String string)
    {
        return splitByLine(string, toolTipLineLength);
    }

    public static List<String> splitByLine(String string, int charsPerLine) {
        final List<String> lines = new ArrayList();
        splitByLine(string, charsPerLine, lines::add);
        return lines;
    }

    public static void splitByLine(String string, int charsPerLine, Consumer<String> lines)
    {
        final String[] words = string.split(" ");
       //TODO predict size for faster runtime
        String line = "";
        int indent = 0;
        for (String word : words)
        {
            // Indent logic
            if(word.trim().startsWith("\\t") || word.trim().startsWith("\t")) {
                indent += 2;
            }
            else if(word.trim().startsWith("-\\t") || word.trim().startsWith("-\t")) {
                indent -= 2;
            }
            // Line break logic
            else if(word.contains("\\n") || word.contains("\n")) {
                if(word.equals("\\n") || word.equals("\n")) {
                    lines.accept(addSpacesLeft(line, indent));
                    line = "";
                }
                // invalid format but insert anyways TODO process by splitting first
                else {
                    line += word.replace("\\n", "").replace("\n", "").trim();
                }
            }
            // Continue building line
            else if (word.length() + line.length() <= charsPerLine)
            {
                line += word + " ";
            }
            else
            {
                // Add existing line
                lines.accept(addSpacesLeft(line, indent));

                // Start next line
                line = word + " ";
            }
        }

        // Add remaining
        lines.accept(addSpacesLeft(line.trim(), indent));
    }

    private static String addSpacesLeft(String line, int pad) {
        if(pad <= 0) {
            return line;
        }
        return String.format("%1$" + pad + "s", "") + line;
    }

    public static String posFormatted(Vec3 pos) {
        // Locale.ROOT so comma decimal locales still produce text GuiFormatHelpers#parseVec3d can read back
        return String.format(Locale.ROOT, "%.2f, %.2f, %.2f", pos.x, pos.y, pos.z);
    }

    public static String posFormatted(BlockPos pos) {
        return String.format(Locale.ROOT, "%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
    }

    public static void outputLines(Component textComponent, Consumer<String> lines) {
        final String formatted = buildToolTipString(textComponent);
        splitByLine(formatted, toolTipLineLength, lines);
    }

    public static String buildToolTipString(Component textComponent) {
        final StringBuilder stringbuilder = new StringBuilder();

        textComponent.visit((style, s) -> {
            if (!s.isEmpty())
            {
                stringbuilder.append(getFormattingCode(style));
                stringbuilder.append(s);
            }
            return java.util.Optional.empty();
        }, Style.EMPTY);

        return stringbuilder.toString();
    }

    /**
     * @param style to convert
     * @return legacy formatting codes
     */
    public static String getFormattingCode(Style style) {
        if (style == null || style.isEmpty()) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        final TextColor color = style.getColor();
        if (color != null) {
            final ChatFormatting formatting = ChatFormatting.getByName(color.serialize());
            if (formatting != null) {
                builder.append(formatting);
            }
        }
        if (style.isBold()) {
            builder.append(ChatFormatting.BOLD);
        }
        if (style.isItalic()) {
            builder.append(ChatFormatting.ITALIC);
        }
        if (style.isUnderlined()) {
            builder.append(ChatFormatting.UNDERLINE);
        }
        if (style.isObfuscated()) {
            builder.append(ChatFormatting.OBFUSCATED);
        }
        if (style.isStrikethrough()) {
            builder.append(ChatFormatting.STRIKETHROUGH);
        }
        return builder.toString();
    }
}
