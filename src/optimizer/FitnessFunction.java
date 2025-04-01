package optimizer;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import utils.CalcPlanScore;
import utils.PlanCalcScoreParamDoubleArrayMapping;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/3/22
 * project:matsimParamCalibration
 */
public class FitnessFunction implements MultivariateFunction {
    private static final Logger log = LogManager.getLogger(FitnessFunction.class);

    Scenario scenario;
    Map<Person, List<Plan>> planList;

    public FitnessFunction(Scenario scenario, Map<Person, List<Plan>> planList) {
        this.scenario = scenario;
        this.planList = planList;
    }


    @Override
    public double value(double[] point) {
        PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping = new PlanCalcScoreParamDoubleArrayMapping(scenario.getConfig().planCalcScore());
        planCalcScoreParamDoubleArrayMapping.updateParamsPerDoubleArray(point);
//        double[] lb = planCalcScoreParamDoubleArrayMapping.getDefaultParameterLowerBounds();
//        double[] ub = planCalcScoreParamDoubleArrayMapping.getDefaultParameterUpperBounds();
//        for (int i = 0; i < point.length; i++) {
//            System.out.println(planCalcScoreParamDoubleArrayMapping.arrayIndex.get(i)+" lowerbound "+lb[i]+",value: "+point[i]+", upperBound: "+ub[i]);
//        };
        CalcPlanScore.calAltPlanScores(planList,scenario);
        double mle = MaximumLogLikelihoodFunction.cal(planList);
//        log.info(mle);
        return mle;
    }


}
