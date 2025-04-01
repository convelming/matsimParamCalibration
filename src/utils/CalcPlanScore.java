package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.scoring.functions.*;

import java.util.List;
import java.util.Map;

/**
 * Author：Milu
 * This class extracts plan elements(activitys and legs etc) to double array as variables, and traces back to
 * date：2024/2/28
 * project: matsimParamCalibration
 */
public class CalcPlanScore {
    private static final Logger log = LogManager.getLogger( CalcPlanScore.class ) ;

//    private Map<Id<Person>, List<Plan>> fixedPersonPlans;
//    private List<String> activityList;
//    private List<String> modeList;
//    private PlanCalcScoreConfigGroup.ScoringParameterSet scoringParameterSet;// = planCalcScoreConfigGroup.getScoringParameters(null);
//
//    public double[] plan2DoubleArrayVariables(Plan plan){
//        double variableArray[] = new double[7 + scoringParameters.utilParams.size()*7+scoringParameters.modeParams.size()*6];
//
//        return variableArray;
//    }
//    public double[] scoringParameterSet2DoubleArray(PlanCalcScoreConfigGroup.ScoringParameterSet scoringParameterSet){
//        //todo check if act has interactive act
//        int actSize = scoringParameterSet.getActivityParamsPerType().size();
//        scoringParameterSet.getActivityParamsPerType();
//
//        int modeSize = scoringParameterSet.getModes().size();
//        double array = new double[scoringParameterSet.getActivityParamsPerType()]
//    }
//    public void resetCalParam(double [] estParams){
//
//    }
//
//    public void updatePlanCalcScoreConfigGroupParams(PlanCalcScoreConfigGroup planCalcScoreConfigGroup){
//        planCalcScoreConfigGroup.getScoringParameters("null").getModes();
//        planCalcScoreConfigGroup.getScoringParameters("null").getActivityParamsPerType();
//    }
    public static void calAltPlanScores(Map<Person,List<Plan>> planMap,Scenario scenario){

        SubpopulationScoringParameters subpopulationScoringParameters = new SubpopulationScoringParameters(scenario);
        planMap.forEach((person, planList) -> {

            planList.forEach(plan -> {
                CharyparNagelLegScoring legScoring = new CharyparNagelLegScoring(subpopulationScoringParameters.getScoringParameters(person), scenario.getNetwork(), scenario.getConfig().transit().getTransitModes());
                CharyparNagelActivityScoring activityScoring = new CharyparNagelActivityScoring(subpopulationScoringParameters.getScoringParameters(person));
                CharyparNagelMoneyScoring moneyScoring = new CharyparNagelMoneyScoring(subpopulationScoringParameters.getScoringParameters(person));
                CharyparNagelAgentStuckScoring agentStuckScoring = new CharyparNagelAgentStuckScoring(subpopulationScoringParameters.getScoringParameters(person));
                Leg lastLeg = null;
                List<PlanElement> elements = plan.getPlanElements();
                for (int i = 0, len = elements.size(); i < len; i++) {
                    PlanElement element = elements.get(i);
                    if (element instanceof Leg ) {
                        Leg leg = (Leg)element;
                        legScoring.handleLeg(leg);
                        lastLeg = leg;
                    } else if (element instanceof Activity ) {
                        Activity activity = (Activity)element;
                        if (activity.getType().contains("interaction")) {
                            continue;
                        }
                        if (activity.getStartTime().isUndefined()) {
                            if (lastLeg != null) {
                                activity.setStartTime(lastLeg.getDepartureTime().seconds() + lastLeg.getTravelTime().seconds());
                            }
                        }
                        if (i == 0) {
                            activityScoring.handleFirstActivity(activity);
                        } else if (i == len - 1) {
                            activityScoring.handleLastActivity(activity);
                        } else {
                            if(activity.getStartTime().isUndefined()){
                                log.warn("activity is undefined!!!!!!!!!!!!!!!!!!!!");
                            }
                            activityScoring.handleActivity(activity);
                        }
                    }
                }
                plan.setScore(legScoring.getScore()+activityScoring.getScore()+agentStuckScoring.getScore()+ moneyScoring.getScore());
            });
        });
    }
}
