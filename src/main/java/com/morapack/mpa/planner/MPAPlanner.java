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
    private final Random rnd;

    public MPAPlanner(TEGraph graph, int population, long timeLimitMs, long seed) {
        this.graph = graph;
        this.decoder = new Decoder(graph);
        this.population = population;
        this.timeLimitMs = timeLimitMs;
        this.rnd = new Random(seed);
    }

    public Result solveWeekly(List<PackageOrder> orders) {
        long t0 = System.currentTimeMillis();

        // Inicialización: población de vectores [0,1]
        int n = orders.size();
        double[][] X = new double[population][n];
        for (int p=0; p<population; p++) for (int i=0; i<n; i++) X[p][i] = rnd.nextDouble();

        Solution[] fitness = new Solution[population];
        for (int p=0; p<population; p++) fitness[p] = decoder.decode(X[p], orders);

        Solution elite = fitness[0];
        int eliteIdx = 0;
        for (int p=1; p<population; p++) if (fitness[p].fitness > elite.fitness) { elite = fitness[p]; eliteIdx = p; }

        int iter = 0;
        while (System.currentTimeMillis() - t0 < timeLimitMs) {
            iter++;
            double progress = (System.currentTimeMillis() - t0) / (double) timeLimitMs;

            // Para cada depredador, generar movimiento según fase
            for (int p=0; p<population; p++) {
                double[] candidate = X[p].clone();

                if (progress < 1.0/3.0) {                // Fase 1: Brownian (explorar)
                    brownianMove(candidate, 0.10);
                } else if (progress < 2.0/3.0) {         // Fase 2: mix
                    brownianMove(candidate, 0.05);
                    levyJump(candidate, 0.02);
                } else {                                  // Fase 3: Lévy (explotar alrededor de elite)
                    // mover hacia elite con perturbación Lévy
                    for (int i=0; i<n; i++) {
                        candidate[i] = 0.5*candidate[i] + 0.5*X[eliteIdx][i];
                    }
                    levyJump(candidate, 0.01);
                }

                clamp01(candidate);

                Solution s = decoder.decode(candidate, orders);
                if (s.fitness > fitness[p].fitness) {
                    X[p] = candidate;
                    fitness[p] = s;
                    if (s.fitness > elite.fitness) {
                        elite = s;
                        eliteIdx = p;
                    }
                }
            }
        }

        long runtime = System.currentTimeMillis() - t0;
        return new Result(elite, runtime);
    }

    // Movimiento Browniano ~ N(0, sigma^2)
    private void brownianMove(double[] v, double sigma) {
        for (int i=0; i<v.length; i++) {
            v[i] += sigma * rnd.nextGaussian();
        }
    }

    // Salto Lévy (simplificado): heavy-tail con alfa~1.5 aprox.
    private void levyJump(double[] v, double scale) {
        for (int i=0; i<v.length; i++) {
            double u = rnd.nextGaussian() * scale;
            double vgauss = rnd.nextGaussian();
            double beta = 1.5;
            double sigma = Math.pow( (gamma(1+beta)*Math.sin(Math.PI*beta/2)) / (gamma((1+beta)/2)*beta*Math.pow(2, (beta-1)/2)), 1/beta );
            double step = u / Math.pow(Math.abs(vgauss), 1/beta) * sigma;
            v[i] += step;
        }
    }

    private void clamp01(double[] v) {
        for (int i=0; i<v.length; i++) {
            if (v[i] < 0) v[i] = 0;
            else if (v[i] > 1) v[i] = 1;
        }
    }

    // Aproximación de Gamma (Lanczos reducido)
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
