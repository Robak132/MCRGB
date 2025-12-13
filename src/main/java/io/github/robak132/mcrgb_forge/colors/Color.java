package io.github.robak132.mcrgb_forge.colors;

public abstract class Color {
    protected int alpha;

    public static Color create(ColorModel model, Number value1, Number value2, Number value3) {
        return create(model, 255, value1, value2, value3);
    }

    public static Color create(ColorModel model, int alpha, Number value1, Number value2, Number value3) {
        return switch (model) {
            case RGB -> new RGB(alpha, value1.intValue(), value2.intValue(), value3.intValue());
            case HSV -> new HSV(alpha, value1.intValue(), value2.intValue(), value3.intValue());
            case HSL -> new HSL(alpha, value1.intValue(), value2.intValue(), value3.intValue());
            case LAB -> new OkLAB(alpha, value1.floatValue(), value2.floatValue(), value3.floatValue());
        };
    }

    public String toHexString() {
        RGB rgb = toRGB();
        return String.format("#%02X%02X%02X", rgb.red(), rgb.green(), rgb.blue());
    }

    public int argb() {
        RGB rgb = toRGB();
        return (rgb.alpha() << 24) | (rgb.red() << 16) | (rgb.green() << 8) | rgb.blue();
    }

    public Color toModel(ColorModel model) {
        switch (model) {
            case RGB -> {
                return toRGB();
            }
            case HSV -> {
                return toHSV();
            }
            case HSL -> {
                return toHSL();
            }
            case LAB -> {
                return toOkLAB();
            }
            default -> throw new IllegalArgumentException("Unknown color model: " + model);
        }
    }

    public abstract RGB toRGB();

    public HSL toHSL() {
        return toRGB().toHSL();
    }

    public HSV toHSV() {
        return toRGB().toHSV();
    }

    public OkLAB toOkLAB() {
        return toRGB().toOkLAB();
    }

    public int alpha() {
        return alpha;
    }

    public abstract Number ch0();
    public abstract Number ch1();
    public abstract Number ch2();

    public enum ColorModel {
        RGB, HSV, HSL, LAB
    }
}
