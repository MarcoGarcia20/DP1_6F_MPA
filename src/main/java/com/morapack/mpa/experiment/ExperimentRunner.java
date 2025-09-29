package com.morapack.mpa.experiment;

import com.morapack.mpa.domain.PackageOrder;
import com.morapack.mpa.planner.MPAPlanner;
import com.morapack.mpa.planner.Solution;
import com.morapack.mpa.scenario.WeeklyScenario;

import java.util.List;

/**
 * Lanza corridas repetidas del escenario semanal con MPA
 * y exporta métricas para el análisis estadístico.
 */
public class ExperimentRunner {

    /** Ejecuta una corrida semanal con N órdenes y tiempo máximo de cómputo. */
    public static KPI runWeeklyMPA(int orders, long seed, long timeLimitMs, int population) {
        WeeklyScenario scn = WeeklyScenario.demoNetwork();
        List<PackageOrder> ords = scn.generateOrders(orders, seed);

        // Early stop: corta si no mejora por 30% del tiempo límite
        long noImproveMs = Math.max(2000L, (long) (timeLimitMs * 0.30));

        MPAPlanner mpa = new MPAPlanner(scn.graph, population, timeLimitMs, seed, /*maxIter*/ Integer.MAX_VALUE, noImproveMs);
        MPAPlanner.Result res = mpa.solveWeekly(ords);
        Solution best = res.best;

        return new KPI(res.runtimeMs, best.percentDelivered);
    }

    // Compatibilidad hacia atrás (por si llamas sin población)
    public static KPI runWeeklyMPA(int orders, long seed, long timeLimitMs) {
        return runWeeklyMPA(orders, seed, timeLimitMs, 40);
    }   
}
