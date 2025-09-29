package com.morapack.mpa.planner;

import com.morapack.mpa.domain.PackageOrder;
import com.morapack.mpa.graph.TEGraph;

import java.util.List;
import java.util.Random;

/**
 * Marine Predators Algorithm (MPA) simplificado
 * - Representación: vector continuo de prioridades por paquete (0..1)
 * - Fases:
 *    F1 Explorar (Brownian)     [0, 1/3)
 *    F2 Transición (mix)        [1/3, 2/3)
 *    F3 Explotar (Lévy)         [2/3, 1]
 * - Decodificador: convierte prioridades -> orden de ruteo -> Solución
 */
public class MPAPlanner {

    public static class Result {
        public final Solution best;
        public final long runtimeMs;
        public Result(Solution best, long runtimeMs) { this.best = best; this.runtimeMs = runtimeMs; }
    }

    private final TEGraph graph;
    private final Decoder decoder;
    private final int population;
    private final long timeLimitMs;
    private final int maxIterations;
    private final long noImproveMillis;
    private final Random rnd;

    // Parámetros de movimiento
    private static final double BROW_SIGMA_F1 = 0.10;  // exploración
    private static final double BROW_SIGMA_F2 = 0.05;  // transición
    private static final double LEVY_SCALE_F2 = 0.02;  // transición
    private static final double LEVY_SCALE_F3 = 0.01;  // explotación
    private static final double LEVY_BETA = 1.5;

    // Precalculo de sigma para Lévy (estable durante toda la corrida)
    private final double levySigma;

    public MPAPlanner(TEGraph graph, int population, long timeLimitMs, long seed) {
        this(graph, population, timeLimitMs, seed, Integer.MAX_VALUE, Math.max(2000L, (long)(timeLimitMs*0.30)));
    }

    public MPAPlanner(TEGraph graph, int population, long timeLimitMs, long seed, int maxIterations, long noImproveMillis) {
        this.graph = graph;
        this.decoder = new Decoder(graph);
        this.population = Math.max(4, population);
        this.timeLimitMs = Math.max(1000L, timeLimitMs);
        this.maxIterations = maxIterations;
        this.noImproveMillis = noImproveMillis;
        this.rnd = new Random(seed);
        this.levySigma = computeLevySigma(LEVY_BETA);
    }

    public Result solveWeekly(List<PackageOrder> orders) {
        final long t0 = System.currentTimeMillis();

        // Inicialización
        final int n = orders.size();
        double[][] X = new double[population][n];
        for (int p=0; p<population; p++) for (int i=0; i<n; i++) X[p][i] = rnd.nextDouble();

        Solution[] fit = new Solution[population];
        for (int p=0; p<population; p++) fit[p] = decoder.decode(X[p], orders);

        int eliteIdx = 0;
        for (int p=1; p<population; p++) if (fit[p].fitness > fit[eliteIdx].fitness) eliteIdx = p;
        Solution elite = fit[eliteIdx];

        long lastImprove = System.currentTimeMillis();
        int iter = 0;

        while (System.currentTimeMillis() - t0 < timeLimitMs && iter < maxIterations) {
            iter++;
            double progress = (System.currentTimeMillis() - t0) / (double) timeLimitMs;

            for (int p=0; p<population; p++) {
                double[] cand = X[p].clone();

                // Fases del MPA
                if (progress < 1.0/3.0) {
                    brownianMove(cand, BROW_SIGMA_F1);
                } else if (progress < 2.0/3.0) {
                    brownianMove(cand, BROW_SIGMA_F2);
                    levyJump(cand, LEVY_SCALE_F2);
                } else {
                    // Intensificación alrededor del elite
                    for (int i=0; i<n; i++) cand[i] = 0.5*cand[i] + 0.5*X[eliteIdx][i];
                    levyJump(cand, LEVY_SCALE_F3);
                }
                clamp01(cand);

                Solution s = decoder.decode(cand, orders);
                if (s.fitness > fit[p].fitness) {
                    X[p] = cand;
                    fit[p] = s;
                    if (s.fitness > elite.fitness) {
                        elite = s;
                        eliteIdx = p;
                        lastImprove = System.currentTimeMillis();
                    }
                }
            }

            // Early stop: sin mejora por una ventana de tiempo
            if (System.currentTimeMillis() - lastImprove > noImproveMillis) break;
        }

        long runtime = System.currentTimeMillis() - t0;
        return new Result(elite, runtime);
    }

    // Movimiento Browniano ~ N(0, sigma^2)
    private void brownianMove(double[] v, double sigma) {
        for (int i=0; i<v.length; i++) v[i] += sigma * rnd.nextGaussian();
    }

    // Salto Lévy con sigma precomputada (menos costo por elemento)
    private void levyJump(double[] v, double scale) {
        for (int i=0; i<v.length; i++) {
            double u = rnd.nextGaussian() * scale;
            double vg = rnd.nextGaussian();
            double step = (u / Math.pow(Math.abs(vg), 1.0/LEVY_BETA)) * levySigma;
            v[i] += step;
        }
    }

    private void clamp01(double[] v) {
        for (int i=0; i<v.length; i++) {
            if (v[i] < 0) v[i] = 0;
            else if (v[i] > 1) v[i] = 1;
        }
    }

    // Sigma de Lévy (estable) para beta=1.5
    private double computeLevySigma(double beta) {
        // Fórmula estándar (Mantegna) con función gamma
        double num = gamma(1+beta) * Math.sin(Math.PI*beta/2.0);
        double den = gamma((1+beta)/2.0) * beta * Math.pow(2.0, (beta-1.0)/2.0);
        return Math.pow(num/den, 1.0/beta);
    }

    // Aproximación de Gamma (Lanczos)
    private double gamma(double z) {
        double[] p = { 676.5203681218851, -1259.1392167224028, 771.32342877765313,
                -176.61502916214059, 12.507343278686905, -0.13857109526572012,
                9.9843695780195716e-6, 1.5056327351493116e-7 };
        int g = 7;
        if (z < 0.5) return Math.PI / (Math.sin(Math.PI*z) * gamma(1-z));
        z -= 1;
        double x = 0.99999999999980993;
        for (int i=0; i<p.length; i++) x += p[i] / (z + i + 1);
        double t = z + g + 0.5;
        return Math.sqrt(2*Math.PI) * Math.pow(t, z+0.5) * Math.exp(-t) * x;
    }
}
