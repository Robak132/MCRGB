package io.github.robak132.mcrgb_forge.client.analysis;

import io.github.robak132.mcrgb_forge.colors.OkLAB;
import io.github.robak132.mcrgb_forge.colors.RGB;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

/**
 * Simple K-means using OKLab distance. Returns list of Sprite (mean + weight%).
 */
public final class ColorClustering {

    private ColorClustering() {
    }

    /**
     * Cluster pixels into up to k clusters.
     *
     * @param pixels      list of ColorVector (RGB 0..255)
     * @param k           desired number of clusters (e.g., 3)
     * @param maxIters    max iterations (e.g., 8)
     * @param sampleLimit maximum pixels to sample for performance (e.g., 4096)
     */
    public static List<SpriteColor> kMeansOkLab(List<RGB> pixels, int k, int maxIters, int sampleLimit) {
        List<RGB> sample = pixels;
        if (pixels.size() > sampleLimit) {
            sample = new ArrayList<>(sampleLimit);
            int step = Math.max(1, pixels.size() / sampleLimit);
            for (int i = 0; i < pixels.size() && sample.size() < sampleLimit; i += step) {
                sample.add(pixels.get(i));
            }
        }
        if (sample.isEmpty()) {
            return List.of();
        }

        final int n = sample.size();
        final int clusters = Math.min(k, n);

        OkLAB[] okLab = new OkLAB[n];
        for (int i = 0; i < n; i++) {
            RGB cv = sample.get(i);
            okLab[i] = cv.toOkLAB();
        }

        RandomSource rnd = RandomSource.create();
        List<OkLAB> centers = new ArrayList<>(clusters);
        boolean[] used = new boolean[n];
        for (int i = 0; i < clusters; i++) {
            int idx;
            do {
                idx = rnd.nextInt(n);
            } while (used[idx]);
            used[idx] = true;
            centers.add(new OkLAB(okLab[idx]));
        }

        int[] assignments = new int[n];
        boolean changed = true;

        for (int iter = 0; iter < maxIters && changed; iter++) {
            changed = false;

            // assignment step
            for (int i = 0; i < n; i++) {
                double bestDist = Double.MAX_VALUE;
                int best = 0;
                OkLAB p = okLab[i];
                for (int c = 0; c < centers.size(); c++) {
                    double d = p.distanceWeighted(centers.get(c));
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
                OkLAB p = okLab[i];
                counts[c]++;

                sums[c][0] += p.lightness();
                sums[c][1] += p.greenRedAxis();
                sums[c][2] += p.blueYellowAxis();
            }
            for (int c = 0; c < clusters; c++) {
                if (counts[c] > 0) {
                    centers.set(c, new OkLAB(255, sums[c][0] / counts[c], sums[c][1] / counts[c], sums[c][2] / counts[c]));
                }
            }
        }

        List<SpriteColor> result = new ArrayList<>();
        float[] fullCounts = new float[clusters];
        for (RGB cv : pixels) {
            OkLAB p = cv.toOkLAB();
            int best = 0;
            double bestD = Float.MAX_VALUE;

            for (int c = 0; c < clusters; c++) {
                double d = p.distanceWeighted(centers.get(c));
                if (d < bestD) {
                    bestD = d;
                    best = c;
                }
            }

            float alphaWeight = 1f + (cv.alpha() / 255f) * 4f;
            fullCounts[best] += alphaWeight;
        }

        float totalWeight = 0f;
        for (float v : fullCounts) totalWeight += v;

        for (int c = 0; c < clusters; c++) {
            if (fullCounts[c] == 0) {
                continue;
            }
            OkLAB center = centers.get(c);
            RGB mean = center.toRGB();
            int weight = Math.round((fullCounts[c] / totalWeight) * 100f);
            result.add(new SpriteColor(mean, weight));
        }

        result.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        return result;
    }
}
