import java.util.stream.IntStream;

public class Databox {

    double[] t_vals;
    double[][] alive_distb, dead_distb;
    double[][] gRate_distb;

    public Databox(double[] t_vals, double[][] alive_distb, double[][] dead_distb, double[][] gRate_distb){
        this.t_vals = t_vals;
        this.alive_distb = alive_distb;
        this.dead_distb = dead_distb;
        this.gRate_distb = gRate_distb;
    }

    public double[][] getAlive_distb(){
        return alive_distb;
    }
    public double[][] getDead_distb(){
        return dead_distb;
    }
    public double[][] getgRate_distb(){
        return gRate_distb;
    }

    public void add(Databox db){
        //this adds the distbs of two Databoxes elementwise
        //used for averaging
        for(int t = 0; t < alive_distb.length; t++){

            t_vals[t] += db.t_vals[t];

            for(int l = 0; l < alive_distb[0].length; l++){
                alive_distb[t][l] += db.alive_distb[t][l];
                dead_distb[t][l] += db.dead_distb[t][l];
                gRate_distb[t][l] += db.gRate_distb[t][l];
            }
        }
    }

    public void divideBy(double divisor){
        //divides all elements of all distbs by the value of divisor
        //used for averaging
        for(int t = 0; t < alive_distb.length; t++){

            t_vals[t] /= divisor;

            for(int l = 0; l < alive_distb[0].length; l++){
                alive_distb[t][l] /= divisor;
                dead_distb[t][l] /= divisor;
                gRate_distb[t][l] /= divisor;
            }
        }
    }


    public static Databox avgDataboxArray(Databox[] databoxes){

        for(int i = 1; i < databoxes.length; i++){
            databoxes[0].add(databoxes[i]);
        }

        databoxes[0].divideBy(databoxes.length);

        return new Databox(databoxes[0].t_vals, databoxes[0].alive_distb, databoxes[0].dead_distb, databoxes[0].gRate_distb);
    }
}
