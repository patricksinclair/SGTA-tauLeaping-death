import org.apache.commons.math3.distribution.PoissonDistribution;

import javax.sound.midi.Soundbank;
import java.util.Random;
import java.util.stream.IntStream;

public class BioSystem {



    private int L, S, S_max;
    private double alpha, time_elapsed;

    private Microhabitat[] microhabitats;
    private int initial_pop = 100;
    private double tau = 0.01;

    private int n_tau_halves = 0;
    private Random rand = new Random();

    public BioSystem(int L, int S, double alpha){

        this.L = L;
        this.S = S;
        this.S_max = S;
        this.alpha = alpha;
        this.microhabitats = new Microhabitat[L];
        this.time_elapsed = 0.;

        for(int i = 0; i < L; i++){
            double c_i = Math.exp(alpha*(double)i) - 1.;
            microhabitats[i] = new Microhabitat(S, c_i);
        }
        microhabitats[0].fillWithWildType(initial_pop);
        microhabitats[0].setEdge_habitat();
        microhabitats[L-1].setEdge_habitat();
    }

    public double getTimeElapsed(){
        return time_elapsed;
    }

    public int getCurrentLivePopulation(){
        int runningTotal = 0;
        for(Microhabitat m : microhabitats) {
            runningTotal += m.getN_alive();
        }
        return runningTotal;
    }

    public double[] getLiveSpatialDistributionArray(){
        double[] mh_pops = new double[L];
        for(int i = 0; i < L; i++){
            mh_pops[i] = microhabitats[i].getN_alive();
        }
        return mh_pops;
    }

    public double[] getDeadSpatialDistributionArray(){
        double[] mh_pops = new double[L];
        for(int i = 0; i < L; i++){
            mh_pops[i] = microhabitats[i].getN_dead();
        }
        return mh_pops;
    }


    public double[] getGrowthRatesArray(){
        double[] mh_gRates = new double[L];
        for(int i = 0; i < L; i++){
            mh_gRates[i] = microhabitats[i].replication_or_death_rate();
        }
        return mh_gRates;
    }


    public void migrate(int N_migrants, int index_from, int index_to){
        microhabitats[index_from].removeNBacteria(N_migrants);
        microhabitats[index_to].addNBacteria(N_migrants);
    }


    private void performAction(){

        double tau_step = tau;
        int[] original_popsizes;
        int[] replication_allocations;
        int[] death_allocations;
        int[] migration_allocations; //no. of migrations out of each microhabitat

        whileloop:
        while(true){

            original_popsizes = new int[L];
            replication_allocations = new int[L];
            death_allocations = new int[L];
            migration_allocations = new int[L];

            for(int mh_index = 0; mh_index < L; mh_index++){

                int n_alive = microhabitats[mh_index].getN_alive();
                original_popsizes[mh_index] = n_alive;

                if(n_alive > 0){
                    double g_or_d_rate = microhabitats[mh_index].replication_or_death_rate();

                    ///REPLICATION ////////
                    if(g_or_d_rate > 0.){
                        PoissonDistribution poiss_replication = new PoissonDistribution(n_alive*g_or_d_rate*tau_step);
                        poiss_replication.reseedRandomGenerator(rand.nextLong());
                        replication_allocations[mh_index] = poiss_replication.sample();

                        //if there's more replications than nutrients then we try again
                        if(replication_allocations[mh_index] > microhabitats[mh_index].getS()){
                            n_tau_halves++;
                            tau_step/=2.;
                            continue whileloop;
                        }
                    ///DEATH ////////
                    }else if(g_or_d_rate < 0.){
                        PoissonDistribution poiss_death = new PoissonDistribution(n_alive*Math.abs(g_or_d_rate)*tau_step);
                        poiss_death.reseedRandomGenerator(rand.nextLong());
                        death_allocations[mh_index] = poiss_death.sample();

                        //if more deaths than live bacteria, we try again
                        if(death_allocations[mh_index] > n_alive){
                            n_tau_halves++;
                            tau_step/=2.;
                            continue whileloop;
                        }
                    }

                    //// MIGRATIONS /////
                    //only sample from remaining live population
                    if(n_alive - death_allocations[mh_index] > 0){
                        PoissonDistribution poiss_migration = new PoissonDistribution((n_alive - death_allocations[mh_index])*microhabitats[mh_index].migration_rate()*tau_step);
                        poiss_migration.reseedRandomGenerator(rand.nextLong());
                        migration_allocations[mh_index] = poiss_migration.sample();

                        //if no. of migrations > live population then try again
                        if(migration_allocations[mh_index] > (n_alive-death_allocations[mh_index])){
                            n_tau_halves++;
                            tau_step/=2.;
                            continue whileloop;
                        }
                    }
                }
            }
            break whileloop;
        }


        // now we carry out the assigned events
        for(int i = 0; i < L; i++){
            //births
            microhabitats[i].replicateNBacteria(replication_allocations[i]);
            //deaths
            microhabitats[i].killNBacteria(death_allocations[i]);
            //migrations
            if(migration_allocations[i] > 0){
                if(i==0){
                    microhabitats[i].removeNBacteria(migration_allocations[i]);
                    microhabitats[i+1].addNBacteria(migration_allocations[i]);
                }else if(i==(L-1)){
                    microhabitats[i].removeNBacteria(migration_allocations[i]);
                    microhabitats[i-1].addNBacteria(migration_allocations[i]);
                }else{
                    int left_migrants = rand.nextInt(migration_allocations[i]);
                    int right_migrants = migration_allocations[i] - left_migrants;
                    microhabitats[i].removeNBacteria(migration_allocations[i]);
                    microhabitats[i-1].addNBacteria(left_migrants);
                    microhabitats[i+1].addNBacteria(right_migrants);
                }
            }
        }

        time_elapsed+=tau_step;
    }




    public static void expGrad_popAndgRateDistbs(double input_alpha){
        long startTime = System.currentTimeMillis();
        int L = 500, S = 500;
        int nSamples = 40, nReps = 32;

        double duration = 1000.;

        String directory_name = "results";
        String filename_alive = "SGTA-death-alpha="+String.format("%.6f", input_alpha)+"-alive-distb-RESEEDED";
        String filename_dead = "SGTA-death-alpha="+String.format("%.6f", input_alpha)+"-dead-distb-RESEEDED";
        String filename_gRate = "SGTA-death-alpha="+String.format("%.6f", input_alpha)+"-gRate-distb-RESEEDED";

        Databox[] databoxes = new Databox[nReps];

        IntStream.range(0, nReps).parallel().forEach(i ->
                databoxes[i] = expGrad_popAndgRateDistbs_subroutine(i, L, S, input_alpha, duration, nSamples));

        Databox avg_results = Databox.avgDataboxArray(databoxes);

        Toolbox.writeDistbsToFile(directory_name, filename_alive, avg_results.t_vals, avg_results.alive_distb);
        Toolbox.writeDistbsToFile(directory_name, filename_dead, avg_results.t_vals, avg_results.dead_distb);
        Toolbox.writeDistbsToFile(directory_name, filename_gRate, avg_results.t_vals, avg_results.gRate_distb);

        long finishTime = System.currentTimeMillis();
        String diff = Toolbox.millisToShortDHMS(finishTime - startTime);
        System.out.println("results written to file");
        System.out.println("Time taken: "+diff);
    }


    private static Databox expGrad_popAndgRateDistbs_subroutine(int i, int L, int S, double alpha, double duration, int nSamples){

        boolean alreadyRecorded = false;
        double[] t_vals = new double[nSamples+1];
        double[][] alive_distbs = new double[nSamples+1][];
        double[][] dead_distbs = new double[nSamples+1][];
        double[][] gRate_distbs = new double[nSamples+1][];
        int sampleCounter = 0;
        double interval = duration/(double)nSamples;

        BioSystem bs = new BioSystem(L, S, alpha);

        while(bs.time_elapsed <= duration+0.2*interval){

            if(bs.time_elapsed%interval < 0.01 && !alreadyRecorded){
                System.out.println("rep: "+i+"\tt: "+bs.time_elapsed+"\ttau halves: "+bs.n_tau_halves);
                t_vals[sampleCounter] = bs.time_elapsed;
                alive_distbs[sampleCounter] = bs.getLiveSpatialDistributionArray();
                dead_distbs[sampleCounter] = bs.getDeadSpatialDistributionArray();
                gRate_distbs[sampleCounter] = bs.getGrowthRatesArray();

                alreadyRecorded = true;
                sampleCounter++;
            }
            if(bs.time_elapsed%interval > 0.1) alreadyRecorded = false;

            bs.performAction();
        }


        return new Databox(t_vals, alive_distbs, dead_distbs, gRate_distbs);
    }


















}
