package rpgclasses.utils;

import necesse.gfx.GameResources;
import org.lwjgl.opengl.GL11;

public class RPGRenderer {
    public enum Alignment {
        START,
        CENTER,
        END
    }

    public static void drawArc(int centerX, int centerY, int innerRadius, int thickness, float start, float length, int segments, Alignment alignment, float red, float green, float blue, float alpha) {
        if (thickness < 1) thickness = 1;
        if (length < 1) length = 1;
        if (length > 360) length = 360;
        if (segments < 1) segments = 1;

        int outerRadius = innerRadius + thickness;

        float startAngleDeg = start;
        if (alignment == Alignment.START) {
            // Nothing
        } else if (alignment == Alignment.CENTER) {
            startAngleDeg -= length / 2f;
        } else if (alignment == Alignment.END) {
            startAngleDeg -= length;
        }

        GameResources.empty.bindTexture();
        GL11.glLoadIdentity();
        GL11.glColor4f(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        for (int i = 0; i <= segments; i++) {
            double t = (double) i / (double) segments;
            double angleRad = Math.toRadians(startAngleDeg + length * t);

            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);

            double xOuter = centerX + outerRadius * cos;
            double yOuter = centerY + outerRadius * sin;

            double xInner = centerX + innerRadius * cos;
            double yInner = centerY + innerRadius * sin;

            GL11.glVertex2d(xOuter, yOuter);
            GL11.glVertex2d(xInner, yInner);
        }

        GL11.glEnd();
    }


}
