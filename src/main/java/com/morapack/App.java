package com.morapack;

import com.morapack.mpa.experiment.ExperimentRunner;
import com.morapack.mpa.experiment.KPI;

/**
 * CLI:
 * mvn -q -Dexec.args="24 30 10000 20" exec:java
 * args: <num_ordenes> <replicas> <tiempo_limite_ms> [poblacion]
 */
public class App {
    public static void main(String[] args) {
        int nOrders = args.length > 0 ? Integer.parseInt(args[0]) : 24;
        int reps    = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        long tlimMs = args.length > 2 ? Long.parseLong(args[2]) : 45000L;
        int pop     = args.length > 3 ? Integer.parseInt(args[3]) : 40;

        System.out.println("replica,runtime_ms,percent_delivered");
        for (int i = 0; i < reps; i++) {
            long seed = 12345L + i;
            KPI k = ExperimentRunner.runWeeklyMPA(nOrders, seed, tlimMs, pop);
            System.out.println(i + "," + k.toString());
        }
    }
}
