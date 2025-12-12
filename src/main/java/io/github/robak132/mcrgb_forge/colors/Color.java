package io.github.robak132.mcrgb_forge.colors;

public interface Color {

    RGB WHITE = new RGB(0xFF_FFFFFF);
    RGB BLACK = new RGB(0xFF_000000);
    RGB RED = new RGB(0xFF_FF0000);
    RGB GREEN = new RGB(0xFF_00FF00);
    RGB BLUE = new RGB(0xFF_0000FF);

    RGB WHITE_DYE = new RGB(0xFF_F9FFFE);
    RGB ORANGE_DYE = new RGB(0xFF_F9801D);
    RGB MAGENTA_DYE = new RGB(0xFF_C74EBD);
    RGB LIGHT_BLUE_DYE = new RGB(0xFF_3AB3DA);
    RGB YELLOW_DYE = new RGB(0xFF_FED83D);
    RGB LIME_DYE = new RGB(0xFF_80C71F);
    RGB PINK_DYE = new RGB(0xFF_F38BAA);
    RGB GRAY_DYE = new RGB(0xFF_474F52);
    RGB LIGHT_GRAY_DYE = new RGB(0xFF_9D9D97);
    RGB CYAN_DYE = new RGB(0xFF_169C9C);
    RGB PURPLE_DYE = new RGB(0xFF_8932B8);
    RGB BLUE_DYE = new RGB(0xFF_3C44AA);
    RGB BROWN_DYE = new RGB(0xFF_835432);
    RGB GREEN_DYE = new RGB(0xFF_5E7C16);
    RGB RED_DYE = new RGB(0xFF_B02E26);
    RGB BLACK_DYE = new RGB(0xFF_1D1D21);

    RGB[] DYE_RGB = {WHITE_DYE, ORANGE_DYE, MAGENTA_DYE, LIGHT_BLUE_DYE, YELLOW_DYE, LIME_DYE, PINK_DYE, GRAY_DYE, LIGHT_GRAY_DYE, CYAN_DYE, PURPLE_DYE,
            BLUE_DYE, BROWN_DYE, GREEN_DYE, RED_DYE, BLACK_DYE};

    default String toHexString() {
        RGB rgb = toRGB();
        return String.format("#%02X%02X%02X", rgb.red(), rgb.green(), rgb.blue());
    }

    default int argb() {
        RGB rgb = toRGB();
        return (rgb.alpha() << 24) | (rgb.red() << 16) | (rgb.green() << 8) | rgb.blue();
    }

    enum ColorModel {
        RGB, HSV, HSL, LAB
    }

    static Color create(ColorModel model, Number value1, Number value2, Number value3) {
        return create(model, 255, value1, value2, value3);
    }

    static Color create(ColorModel model, int alpha, Number value1, Number value2, Number value3) {
        return switch (model) {
            case RGB -> new RGB(alpha, value1.intValue(), value2.intValue(), value3.intValue());
            case HSV -> new HSV(alpha, value1.intValue(), value2.intValue(), value3.intValue());
            case HSL -> new HSL(alpha, value1.intValue(), value2.intValue(), value3.intValue());
            case LAB -> new LAB(alpha, value1.floatValue(), value2.floatValue(), value3.floatValue());
        };
    }

    Number[] values();
    RGB toRGB();
    default HSL toHSL() {
        return toRGB().toHSL();
    }
    default HSV toHSV() {
        return toRGB().toHSV();
    }
    default LAB toLAB() {
        return toRGB().toLAB();
    }
}
