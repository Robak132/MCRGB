package io.github.robak132.mcrgb_forge.client.analysis;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple K-means using OKLab distance. Returns list of Sprite (mean + weight%).
 */
public final class ColorClustering {

    private ColorClustering() {}

    /**
     * Cluster pixels into up to k clusters.
     *
     * @param pixels      list of ColorVector (RGB 0..255)
     * @param k           desired number of clusters (e.g., 3)
     * @param maxIters    max iterations (e.g., 8)
     * @param sampleLimit maximum pixels to sample for performance (e.g., 4096)
     */
    public static List<SpriteColor> kMeansOkLab(List<ColorVector> pixels, int k, int maxIters, int sampleLimit) {
        List<ColorVector> sample = pixels;
        if (pixels.size() > sampleLimit) {
            // simple deterministic sampling (first sampleLimit) or random sampling
            sample = new ArrayList<>(sampleLimit);
            int step = Math.max(1, pixels.size() / sampleLimit);
            for (int i = 0; i < pixels.size() && sample.size() < sampleLimit; i += step) {
                sample.add(pixels.get(i));
            }
        }

        if (sample.isEmpty()) return List.of();

        final int n = sample.size();
        final int clusters = Math.min(k, n);

        // Precompute OKLab for every sample pixel
        float[][] lab = new float[n][];
        for (int i = 0; i < n; i++) {
            ColorVector cv = sample.get(i);
            lab[i] = OkLab.rgbToOkLab(cv.getR(), cv.getG(), cv.getB());
        }

        // initialize centers using k distinct random samples
        RandomSource rnd = RandomSource.create();
        List<float[]> centers = new ArrayList<>(clusters);
        boolean[] used = new boolean[n];
        for (int i = 0; i < clusters; i++) {
            int idx;
            do { idx = rnd.nextInt(n); } while (used[idx]);
            used[idx] = true;
            centers.add(lab[idx].clone());
        }

        int[] assignments = new int[n];
        boolean changed = true;

        for (int iter = 0; iter < maxIters && changed; iter++) {
            changed = false;

            // assignment step
            for (int i = 0; i < n; i++) {
                float bestDist = Float.MAX_VALUE;
                int best = 0;
                float[] p = lab[i];
                for (int c = 0; c < centers.size(); c++) {
                    float d = OkLab.distanceSq(p, centers.get(c));
                    if (d < bestDist) {
                        bestDist = d;
                        best = c;
                    }
                }
                if (assignments[i] != best) {
                    changed = true;
                    assignments[i] = best;
                }
            }

            // update step: compute new centers as mean of assigned OKLab coords
            int[] counts = new int[clusters];
            float[][] sums = new float[clusters][3];
            for (int i = 0; i < n; i++) {
                int c = assignments[i];
                counts[c]++;
                float[] p = lab[i];
                sums[c][0] += p[0];
                sums[c][1] += p[1];
                sums[c][2] += p[2];
            }
            for (int c = 0; c < clusters; c++) {
                if (counts[c] > 0) {
                    centers.get(c)[0] = sums[c][0] / counts[c];
                    centers.get(c)[1] = sums[c][1] / counts[c];
                    centers.get(c)[2] = sums[c][2] / counts[c];
                }
            }
        }

        // Build results: convert centers back to RGB and calculate weight (percent)
        List<SpriteColor> result = new ArrayList<>();
        int totalCount = pixels.size(); // use full pixels for weight, not sample size
        // compute counts over full pixel set by assigning full set to nearest center
        int[] fullCounts = new int[clusters];
        for (ColorVector cv : pixels) {
            float[] p = OkLab.rgbToOkLab(cv.getR(), cv.getG(), cv.getB());
            int best = 0;
            float bestD = Float.MAX_VALUE;
            for (int c = 0; c < clusters; c++) {
                float d = OkLab.distanceSq(p, centers.get(c));
                if (d < bestD) { bestD = d; best = c; }
            }
            fullCounts[best]++;
        }

        for (int c = 0; c < clusters; c++) {
            if (fullCounts[c] == 0) continue;
            float[] center = centers.get(c);
            int[] rgb = OkLab.okLabToRgb(center[0], center[1], center[2]);
            ColorVector mean = new ColorVector(rgb[0], rgb[1], rgb[2]);
            int weight = Math.round((fullCounts[c] * 100f) / totalCount);
            result.add(new SpriteColor(mean, weight));
        }

        return result;
    }
}
