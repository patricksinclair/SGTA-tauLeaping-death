import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Toolbox {


    public static void writeDistbsToFile(String directoryName, String filename, double[] t_vals, double[][] distb_vals){

        File directory = new File(directoryName);
        if(!directory.exists()) directory.mkdirs();

        File file = new File(directoryName+"/"+filename+".txt");

        try{

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            int string_length = 15;


            // this writes a header with time and indexes
            String header_time = "#time,";
            String header_ouput = String.format("%-"+string_length+"s", header_time);
            for(int m = 0; m < distb_vals[0].length-1; m++){
                String mh_index = "mh_index = "+m+",";
                header_ouput += String.format("%-"+string_length+"s", mh_index);
            }
            String mh_index = "mh_index = "+(distb_vals[0].length-1);
            header_ouput += String.format("%-"+string_length+"s", mh_index);

            bw.write(header_ouput);
            bw.newLine();



            for(int t = 0; t < t_vals.length; t++) {

                String output = "";
                String t_val = String.format("%.4E", t_vals[t]) + ",";
                output += String.format("%-" + string_length + "s", t_val);

                for(int m = 0; m < distb_vals[0].length - 1; m++) {
                    String distb_val = String.format("%.4E", distb_vals[t][m]) + ",";
                    output += String.format("%-" + string_length + "s", distb_val);
                }
                String distb_val = String.format("%.4E", distb_vals[t][distb_vals[0].length - 1]);
                output += String.format("%-" + string_length + "s", distb_val);

                bw.write(output);
                bw.newLine();
            }




            bw.close();

        }catch (IOException e){}





    }






    public static String millisToShortDHMS(long duration) {
        String res = "";
        long days  = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        if (days == 0) {
            res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        else {
            res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return res;
    }
}
