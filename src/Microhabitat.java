public class Microhabitat {

    private int N_alive, N_dead;
    private double c;
    int S, S_max;
    boolean edge_habitat = false;

    private double b = 0.1, K_prime = 33.; //migration rate and monod constant

    public Microhabitat(int S, double c){
        this.S = S;
        this.S_max = S;
        this.c = c;
        this.N_alive = 0;
        this.N_dead = 0;
    }

    public int getS(){return S;}
    public double getC(){return c;}
    public int getN_alive(){return N_alive;}
    public int getN_dead(){return N_dead;}
    public int getN_tot(){return N_alive+N_dead;}
    public double getB(){return b;}

    public void setEdge_habitat(){
        edge_habitat = true;
    }

    public double migration_rate(){
        return edge_habitat ? 0.5*b : b;
    }


    public double beta(){

        double mu = S/(K_prime+S);
        double mu_max = S_max/(K_prime+S_max);
        return 1. + 9.*mu/mu_max;
    }

    public double phi_c(){
        return 1. - (6.*(c/beta())*(c/beta()))/(5. + (c/beta())*(c/beta()));
    }


    public double replication_or_death_rate(){

        double phi_c = phi_c();
        return (phi_c >= 0) ? phi_c*(S/(K_prime + S)) : phi_c;
    }

    public void fillWithWildType(int N){N_alive+=N;}

    public void addNBacteria(int N){N_alive+=N;}
    public void removeNBacteria(int N){N_alive-=N;}

    public void replicateNBacteria(int N){
        N_alive += N;
        S -= N;
    }
    public void killNBacteria(int N){
        N_alive -= N;
        N_dead += N;
    }

}
