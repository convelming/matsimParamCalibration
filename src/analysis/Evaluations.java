package analysis;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.population.PopulationUtils;

import java.util.*;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2025/4/2
 * project:matsimParamCalibration
 */
public class Evaluations {
    private Set<String> betweenActs = new HashSet<>(); // required to be indexed and elementwise unique
    private Map<String,Map<String, Integer>> modeCounts = new HashMap<>(); // Map<mode,Map<btwActs, count>>
    private Map<String,List<Double>> travelTimes = new HashMap<>();// key is act->act, value is list of travel times


    /**
     *
     | Mode         | home->work    | work->home    | edu->home     | other->home  | work->other   | xxx->xxx      |
     |--------------|---------------|---------------|---------------|--------------|---------------|---------------|
     | **Car**      | -   int       | -             | -             | -            | -             | -             |
     | **PT**       | -             | -             | -             | -            | -             | -             |
     | **Walk**     | -             | -             | -             | -            | -             | -             |
     | **travel time** |list<double>| -             | -             | -            | -             | -             |

     * @param population should be result population from output
     */
    public void calModalSplitsBetweenActs(Population population){
        population.getPersons().forEach((personId, person) -> {
            person.getPlans().forEach(plan -> {
                List<Activity> activityList = new ArrayList<>();
                List<Leg> legList = new ArrayList<>();
                for (int i = 0; i < plan.getPlanElements().size(); i++) {
                    PlanElement planElement = plan.getPlanElements().get(i);
                    if (planElement instanceof Activity && !((Activity)planElement).getType().contains("interaction")) activityList.add((Activity) planElement);
                    if (planElement instanceof Leg && !((Activity) plan.getPlanElements().get(i-1)).getType().contains("interaction")) legList.add((Leg) planElement);
                }
                for (int i = 0; i < activityList.size()-1; i++) {

                    String tmpBtwActs = activityList.get(i).getType()+"->"+activityList.get(i+1).getType();
                    betweenActs.add(tmpBtwActs);
                    // update travel time information
                    if (travelTimes.containsKey(tmpBtwActs)){
                        travelTimes.get(tmpBtwActs).add(legList.get(i).getTravelTime().seconds());
                    }else{
                        List<Double> tmpTravelTime = new ArrayList<>();
                        tmpTravelTime.add(legList.get(i).getTravelTime().seconds());
                        travelTimes.put(tmpBtwActs,tmpTravelTime);
                    }
                    if(modeCounts.containsKey(legList.get(i).getMode())){
                        if (modeCounts.get(legList.get(i).getMode()).containsKey(tmpBtwActs)){
                            modeCounts.get(legList.get(i).getMode()).put(tmpBtwActs, modeCounts.get(legList.get(i).getMode()).get(tmpBtwActs)+1);
                        }else {
                            modeCounts.get(legList.get(i).getMode()).put(tmpBtwActs, 1);
                        }
                    }else{
                        Map<String,Integer> tmpModeCount = new HashMap<>();
                        tmpModeCount.put(tmpBtwActs, 1);
                        modeCounts.put(legList.get(i).getMode(),tmpModeCount);
                    }
                }
            });
        });
    }

    public static void main(String[] args) {
        Population population = PopulationUtils.readPopulation("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration/cen4Zon1_output/output_plans.xml");
        Evaluations evaluations = new Evaluations();
        evaluations.calModalSplitsBetweenActs(population);
        System.out.println(evaluations.betweenActs.size());
    }
}
