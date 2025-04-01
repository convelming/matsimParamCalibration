package population;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelAgentStuckScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.CharyparNagelMoneyScoring;
import utils.CalcPlanScore;

import java.util.List;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/6/7
 * project:matsimParamCalibration
 */
public class ComparePopSelectedPlans {
    public static void main(String[] args) {
        String popFile1 = "/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration/outputOnlySurvey052510w/output_plans.xml.gz";
        String popFile2 = "/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration/outputOnlySurvey052510w/output_plans.xml.gz";

        Population population = PopulationUtils.readPopulation(popFile1);

    }
//    public static calPlanScore(){
//        CharyparNagelLegScoring legScoring = new CharyparNagelLegScoring(subpopulationScoringParameters.getScoringParameters(person), scenario.getNetwork(), scenario.getConfig().transit().getTransitModes());
//        CharyparNagelActivityScoring activityScoring = new CharyparNagelActivityScoring(subpopulationScoringParameters.getScoringParameters(person));
//        CharyparNagelMoneyScoring moneyScoring = new CharyparNagelMoneyScoring(subpopulationScoringParameters.getScoringParameters(person));
//        CharyparNagelAgentStuckScoring agentStuckScoring = new CharyparNagelAgentStuckScoring(subpopulationScoringParameters.getScoringParameters(person));
//        Leg lastLeg = null;
//        List<PlanElement> elements = plan.getPlanElements();
//        for (int i = 0, len = elements.size(); i < len; i++) {
//            PlanElement element = elements.get(i);
//            if (element instanceof Leg) {
//                Leg leg = (Leg) element;
//                legScoring.handleLeg(leg);
//                lastLeg = leg;
//            } else if (element instanceof Activity) {
//                Activity activity = (Activity) element;
//                if (activity.getType().contains("interaction")) {
//                    continue;
//                }
//                if (activity.getStartTime().isUndefined()) {
//                    if (lastLeg != null) {
//                        activity.setStartTime(lastLeg.getDepartureTime().seconds() + lastLeg.getTravelTime().seconds());
//                    }
//                }
//                if (i == 0) {
//                    activityScoring.handleFirstActivity(activity);
//                } else if (i == len - 1) {
//                    activityScoring.handleLastActivity(activity);
//                } else {
//                    if (activity.getStartTime().isUndefined()) {
//                        System.out.println("activity start time is undefined!!!!!!!!!!!!!!!!!!!!");
//                    }
//                    activityScoring.handleActivity(activity);
//                }
//            }
//        }
////                plan.setScore(legScoring.getScore()+ moneyScoring.getScore()+activityScoring.getScore()+agentStuckScoring.getScore());
//        plan.setScore(legScoring.getScore()+activityScoring.getScore()+agentStuckScoring.getScore());
//    }
}
