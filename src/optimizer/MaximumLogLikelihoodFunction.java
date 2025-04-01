package optimizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;

import java.util.List;
import java.util.Map;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/18
 * project:matsimParamCalibration
 */
public class MaximumLogLikelihoodFunction {
    private static final Logger log = LogManager.getLogger(MaximumLogLikelihoodFunction.class);

    /**
     *
     * @param fixedPersonPlans  by default, the first plan for each person is the original selected plan
     *                          each score is process first with math.atan() to limit it to -1/2*pi ~ 1/2*pi
     *                          otherwise if there are large negative scores, the exp(-largeValue)~=0 and leads to problems
     * @return
     */
    public static double cal(Map<Person, List<Plan>> fixedPersonPlans) {
        double sumMLE = 0.0;
        for (Map.Entry<Person, List<Plan>>  plans:fixedPersonPlans.entrySet()) {
            double selectedExpPlanScore = Math.exp(Math.atan(plans.getValue().get(0).getScore()));
            double sumExpScore = 0.0;
            for (int i = 0; i < plans.getValue().size(); i++) {
                sumExpScore += Math.exp(Math.atan(plans.getValue().get(i).getScore()));
            }
            double prob = selectedExpPlanScore/sumExpScore;
            sumMLE += Math.log(prob);
        }
//        log.info(sumMLE);
        return sumMLE;
    }
    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

//
//    @Override
//    public double value(double[] parameters) {
//        this.parameters = parameters;
//        return 0;
//    }


}
