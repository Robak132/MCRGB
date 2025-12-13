package io.github.robak132.mcrgb_forge.colors;

public final class OkLAB extends Color {

    private final float L, a, b;

    public OkLAB(int alpha, float L, float a, float b) {
        this.alpha = alpha;
        this.L = L;
        this.a = a;
        this.b = b;
    }

    public OkLAB(OkLAB okLab) {
        this.alpha = okLab.alpha;
        this.L = okLab.L;
        this.a = okLab.a;
        this.b = okLab.b;
    }

    // sRGB 0..255 -> linear 0..1
    static float sRGBToLinear(float c) {
        c /= 255f;
        if (c <= 0.04045f) {
            return c / 12.92f;
        }
        return (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }

    // linear 0..1 -> sRGB 0..255
    static int linearToSRGBInt(float c) {
        float v;
        if (c <= 0.0031308f) {
            v = 12.92f * c;
        } else {
            v = 1.055f * (float) Math.pow(c, 1.0 / 2.4) - 0.055f;
        }
        int iv = Math.round(v * 255f);
        if (iv < 0) {
            iv = 0;
        }
        if (iv > 255) {
            iv = 255;
        }
        return iv;
    }

    public double distanceWeighted(OkLAB other) {
        double dL = (this.L - other.L) * 0.6;   // weaken brightness
        double da = (this.a - other.a);
        double db = (this.b - other.b);

        return (da * da + db * db) * 1.6 + (dL * dL) * 0.5;
    }

    @Override
    public RGB toRGB() {
        // OKLab -> l',m',s'
        float l_i = L + 0.3963377774f * a + 0.2158037573f * b;
        float m_i = L - 0.1055613458f * a - 0.0638541728f * b;
        float s_i = L - 0.0894841775f * a - 1.2914855480f * b;

        // cube
        float l = l_i * l_i * l_i;
        float m = m_i * m_i * m_i;
        float s = s_i * s_i * s_i;

        // LMS -> linear RGB
        float lr = 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
        float lg = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
        float lb = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;

        // linear -> sRGB int
        int r_i = linearToSRGBInt(lr);
        int g_i = linearToSRGBInt(lg);
        int b_i = linearToSRGBInt(lb);
        return new RGB(alpha, r_i, g_i, b_i);
    }

    @Override
    public OkLAB toOkLAB() {
        return this;
    }

    public float lightness() {
        return L;
    }

    public float greenRedAxis() {
        return a;
    }

    public float blueYellowAxis() {
        return b;
    }

    @Override
    public Number ch0() {
        return L;
    }

    @Override
    public Number ch1() {
        return a;
    }

    @Override
    public Number ch2() {
        return b;
    }

}
