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

    /**
     * Ejecuta una corrida semanal con N órdenes y tiempo máximo de cómputo.
     */
    public static KPI runWeeklyMPA(int orders, long seed, long timeLimitMs) {
        WeeklyScenario scn = WeeklyScenario.demoNetwork();
        List<PackageOrder> ords = scn.generateOrders(orders, seed);

        // NOTA: Para igualdad con otros algoritmos, usa el mismo timeLimitMs.
        MPAPlanner mpa = new MPAPlanner(scn.graph, 40, timeLimitMs, seed);
        MPAPlanner.Result res = mpa.solveWeekly(ords);
        Solution best = res.best;

        return new KPI(res.runtimeMs, best.percentDelivered);
    }
}
