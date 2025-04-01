package optimizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import utils.CalcPlanScore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/11/25
 * project:matsimParamCalibration
 */
public class ModelEvaluations {
    private static final Logger log = LogManager.getLogger(ModelEvaluations.class);
    public Scenario scenario; public int variableDimension;
    public Map<Person, List<Plan>> trainingMap;
    public Map<Person, List<Plan>> testingMap;
    public double[] trainEval,testEval; //[accuracy,logLost,aic,bic,rhoSquared,adjRhoSquared]
    public double trainingPercentage;

    public ModelEvaluations(Scenario scenario, int variableDimension, Map<Person, List<Plan>> map, double trainingPercentage) {
        this.scenario = scenario;
        this.variableDimension = variableDimension;
        splitData2TrainingTesting(map,trainingPercentage);this.trainingPercentage = trainingPercentage;
    }

    /***
     * This method uses
     * @param originalMap
     * @param percentage
     */

    public void splitData2TrainingTesting(Map<Person, List<Plan>> originalMap, double percentage) {
        if (percentage < 0 || percentage > 1) {
            throw new IllegalArgumentException("Percentage must be between 0 and 1.");
        }
        // Convert the map entries to a list
        List<Map.Entry<Person, List<Plan>>> entryList = new ArrayList<>(originalMap.entrySet());
        int totalSize = entryList.size();
        if (percentage>=1.0) {
            this.trainingMap = originalMap; this.testingMap=null;
            return;
        }else{
            // Calculate the number of elements to put in map1 based on the given percentage
            int validatingSize = (int) Math.round(totalSize *(1-percentage) );
            // Shuffle the entries to ensure randomness
            Collections.shuffle(entryList);
            // Create the two maps
            this.trainingMap = new HashMap<>();
            this.testingMap = new HashMap<>();
            // Split the shuffled list into two parts
            for (int i = 0; i < validatingSize; i++) {
                Map.Entry<Person, List<Plan>> entry = entryList.get(i);
                testingMap.put(entry.getKey(), entry.getValue());
            }
            for (int i = validatingSize; i < totalSize; i++) {
                Map.Entry<Person, List<Plan>> entry = entryList.get(i);
                trainingMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
    public double[] getFitnessStatistics(Map<Person, List<Plan>> originalMap, Scenario scenario, int variableDimension){
        CalcPlanScore.calAltPlanScores(originalMap,scenario);
        // 1. accuracy
        //Accuracy=正确预测的样本数/总样本数

        // 2.log-loss Log Loss = -1/n * sum(i=1:n,sum(k=1:n,y_ik*log(p_ik)))
        // n sample size, k class size
        // y_ik indicators for class k, 1 if yes else 0
        // p_ik probability
        double t[] = new double[]{0.0,0.0,0.0,0.0};//tp[0],tn[1],fp[2],fn[3]
        double logL[] = new double[]{0.0,0.0};
        originalMap.forEach((person, plans) -> {
            double tmpBesScore = -Double.MAX_VALUE;
            double sumScore = 0.0; int bestScoreIndex = 0;
            for (int i = 0; i < plans.size(); i++) {
                sumScore += Math.exp(Math.tanh(plans.get(i).getScore()));
                if (tmpBesScore<Math.exp(Math.tanh(plans.get(i).getScore()))) {
                    tmpBesScore = Math.exp(Math.tanh(plans.get(i).getScore()));
                    bestScoreIndex = i;
                }
            }
            if (bestScoreIndex==0) t[0]++;//TP 实际是类别k，且预测为类别k
//            if (bestScoreIndex==0) t[1]=0;// TN实际不是类别k，且预测也不是类别k ,because always be the first one so this doesn't hold
            if (bestScoreIndex!=0) t[2]++;// FP实际不是类别k，但被错误预测为类别k  same as tn
            if (bestScoreIndex!=0) t[3]++;// FN实际是类别k，但被错误预测为其他类别k
            double tmpProb = tmpBesScore/sumScore;
            logL[0] += Math.log(tmpProb);
            logL[1] += Math.log(1.0/5.0);
        });
        double accuracy = t[0] / originalMap.size();//Accuracy=正确预测的样本数/总样本数
        log.info("===================model fit:===================");
        log.info("Accuracy: " + accuracy);
        double logLost = -1.0/originalMap.size()*logL[0];
        log.info("Loglost: " + logLost);
        // 3. TP（真正类）、TN（真负类）、FP（假正类） 和 FN（假负类）
        // TP (True Positive): 实际是类别k，且预测为类别k 0
        // TN (True Negative): 实际不是类别k，且预测也不是类别k 1
        // FP (False Positive): 实际不是类别k，但被错误预测为类别k 2
        // FN (False Negative): 实际是类别k，但被错误预测为其他类别k 3
        //  precision_k = TP_k/(TP_k+FP_k)
        //  recall_k = TP_k/(TP_k+FN_k)
        //  F1_score_k = 2 x (precision_k x recall_k)/(precision_k + recall_k)
        double precision = t[0]/(t[0]+t[2]);
        double recall = t[0]/(t[0]+t[3]);
        double f1_score = 2*(precision*recall)/(precision+recall);
        log.info("precision: " + precision);
        log.info("recall: " + recall);
        log.info("F1 score: " + f1_score);
        // 4. 贝叶斯信息准则 (BIC) 和 赤池信息准则 (AIC)
        // AIC = 2k−2logL
        // BIC=klogN−2logL
        // k: 模型参数数量
        // N: 样本数
        // logL: 模型对数似然
        double k = variableDimension;
        double aic = 2*k-2*MaximumLogLikelihoodFunction.cal(originalMap);
        double bic = k*Math.log(originalMap.size())-2*MaximumLogLikelihoodFunction.cal(originalMap);
        log.info("AIC: " + String.format("%.2f", aic)+", BIC: " + String.format("%.2f", bic));
        // 5. Adjusted Rho Squared = 1 - (logL_full-k)/(logL_null)
        double rhoSquared = 1-(logL[0]/logL[1]);
        double adjRhoSquared = 1-(logL[0]-k)/logL[1];
        log.info("RhoSquared: " + String.format("%.4f",rhoSquared)+", and adjRhoSquared: " + String.format("%.4f",adjRhoSquared));
        log.info("================================================");
        return new double[]{accuracy,logLost,aic,bic,rhoSquared,adjRhoSquared};//[accuracy,logLost,aic,bic,rhoSquared,adjRhoSquared]
    }
    public void evaluate(){
        this.trainEval = getFitnessStatistics(this.trainingMap,scenario,variableDimension);
        if (this.trainingPercentage>=1.0){this.testEval = new double[6];}else{
        this.testEval = getFitnessStatistics(this.testingMap,scenario,variableDimension);}
    }
    public void append(String file, int matsimIter, String optType) throws IOException {
//        public double accuracy;
//        public double loglost;
//        public double aic,bic,rhoSquared, adjRhoSquared;
        if(new File(file).exists()){
            BufferedWriter bw = new BufferedWriter(new FileWriter(file,true) );
            bw.append(matsimIter+","+optType+","+array2String(trainEval)+","+array2String(testEval)+"\n");
            bw.flush();
            bw.close();
        }else {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file) );
            bw.write("matsimIter,optType,trainSample,trainAccuracy,trainLogLost,trainAic,trainBic,trainRhoSquared,trainAdjRhoSquared," +
                    ",testSample,testAccuracy,testLogLost,testAic,testBic,testRhoSquared,testAdjRhoSquared\n");
            bw.flush();
            bw.close();
        }
    }
    public String array2String(double[] array){
        String tmp ="";
        for (int i = 0; i < array.length; i++) {
            tmp+=array[i]+",";
        }
        return tmp.substring(0,tmp.length()-1);
    }

}
