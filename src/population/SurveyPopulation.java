package population;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.population.PopulationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/4/29
 * project:matsimParamCalibration
 */
public class SurveyPopulation {
    public static void main(String[] args) {
        String popFile = "/Users/convel/Documents/gzpi/MATSimGZ/scenarioSimNets/surveyPop.xml";
        Population population = PopulationUtils.readPopulation(popFile);
        Set<String> actType = new HashSet<>();
        Set<String> modes = new HashSet<>();

        population.getPersons().forEach((personId, person) -> {
            person.getAttributes().putAttribute("subpopulation","survey");
            person.getPlans().forEach(plan -> {
                List<PlanElement> planElementList = plan.getPlanElements();
                planElementList.forEach(planElement -> {
                    if (planElement instanceof Leg){
                        Leg leg = (Leg)planElement;
                        if (leg.getMode().equals("taxi")){
                            leg.setMode("car");
                        }
                        modes.add(leg.getMode());
                    }else if(planElement instanceof Activity){
                        Activity act = (Activity) planElement;
                        actType.add(act.getType());
                    }
                });
            });
        });
        System.out.println();
        new PopulationWriter(population).write("/Users/convel/Documents/gzpi/MATSimGZ/scenarioSimNets/surveyPopWithSubpop.xml");
    }
}
