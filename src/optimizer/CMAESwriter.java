package optimizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/4/8
 * project:matsimParamCalibration
 */
public class CMAESwriter {
    private static final Logger log = LogManager.getLogger(CMAESwriter.class);

    CMAES4PlanParamCalibration cmaes4PlanParamCalibration;

    public CMAESwriter(CMAES4PlanParamCalibration cmaes4PlanParamCalibration) {
        this.cmaes4PlanParamCalibration = cmaes4PlanParamCalibration;
    }

    public void append(String file, int matsimIter) throws Exception {
        if(new File(file).exists()){
            BufferedWriter bw = new BufferedWriter(new FileWriter(file,true) );
            bw.append(matsimIter+","+this.cmaes4PlanParamCalibration.cmaesOptimizer.getIterations()+","+this.cmaes4PlanParamCalibration.bestFitness+","+array2Str(this.cmaes4PlanParamCalibration.getBestResults())+"\n");
            bw.flush();
            bw.close();
        }else {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file) );
            bw.write("matsimIter,cmaesIter,bestFitness,"+array2Str(this.cmaes4PlanParamCalibration.planCalcScoreParamDoubleArrayMapping.arrayIndex)+"\n");
            bw.flush();
            bw.close();
        }
    }
    public void writeCMAESstatics(String file)throws Exception{
        BufferedWriter bw = new BufferedWriter(new FileWriter(file) );
        bw.write(array2Str(this.cmaes4PlanParamCalibration.planCalcScoreParamDoubleArrayMapping.arrayIndex));bw.newLine();
        bw.write(array2Str(this.cmaes4PlanParamCalibration.cmaesOptimizer.getStartPoint()));bw.newLine();
        bw.write(array2Str(this.cmaes4PlanParamCalibration.getBestResults()));bw.newLine();
        bw.write("iteration,sigma,fitness,"+array2StrWithPrefix(this.cmaes4PlanParamCalibration.planCalcScoreParamDoubleArrayMapping.arrayIndex,"mean_")+","
                +array2StrWithPrefix(this.cmaes4PlanParamCalibration.planCalcScoreParamDoubleArrayMapping.arrayIndex,"D_")+","+"\n");
        for (int i = 0; i < this.cmaes4PlanParamCalibration.cmaesOptimizer.getStatisticsSigmaHistory().size(); i++) {
            bw.write(i+","+this.cmaes4PlanParamCalibration.cmaesOptimizer.getStatisticsSigmaHistory().get(i)+"," +this.cmaes4PlanParamCalibration.cmaesOptimizer.getStatisticsFitnessHistory().get(i)+","+
                    array2Str(this.cmaes4PlanParamCalibration.cmaesOptimizer.getStatisticsMeanHistory().get(i).getRow(0))+","+
                    array2Str(this.cmaes4PlanParamCalibration.cmaesOptimizer.getStatisticsDHistory().get(i).getRow(0))+","+
                    "\n");
        }
        bw.flush();bw.close();
    }
    public static String array2Str(double[] array){
        String tmp = "";
        for (int i = 0; i < array.length; i++) {
            tmp+= String.format("%.2f",array[i])+",";
        }
        return tmp.substring(0,tmp.length()-1);
    }
    public static String array2Str(List<String> array){
        String tmp = "";
        for (int i = 0; i < array.size(); i++) {
            tmp+= array.get(i)+",";
        }
        return tmp.substring(0,tmp.length()-1);
    }
    public static String array2StrWithPrefix(List<String> array,String prefix){
        String tmp = "";
        for (int i = 0; i < array.size(); i++) {
            tmp+= prefix+array.get(i)+",";
        }
        return tmp.substring(0,tmp.length()-1);
    }
}
