package rpgclasses.utils;

import aphorea.utils.AphColors;

import java.awt.*;

public class RPGColors extends AphColors {
    public static class HEX {
        public static String iron = "#626871";
        public static String gold = "#EAB676";
        public static String lighting = "#FAFBA5";
        public static String dirt = "#5A3E2B";
    }

    public static Color dirt = new Color(90, 62, 43);

    public static int getColorInt(String color) {
        String processedColor = color;
        if (color.startsWith("#")) {
            processedColor = color.substring(1);
        }
        return Integer.parseInt(processedColor, 16);
    }

    public static Color getColor(String color) {
        return new Color(getColorInt(color));
    }
}
