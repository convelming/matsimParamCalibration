package population;

import org.apache.commons.math3.analysis.function.Pow;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.*;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.api.core.v01.population.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/10
 * project:matsimParamCalibration
 */
public class ReadPopulation {

    public static void main(String[] args) {
        Population population = PopulationUtils.readPopulation("/Users/convel/Documents/gzpi/MATSimGZ/scenarioSimNets/surveyPopWithSubpop.xml");
        Set<String> modes = new HashSet<>();
        Population population1 = PopulationUtils.readPopulation("/Users/convel/Desktop/cen4ZonePop.xml");
        Population scaledPop = ScalePopulation.scalePopulation(population1,0.5);
        population.getPersons().forEach((personId, person) -> {
            scaledPop.addPerson(person);
        });
        System.out.println(scaledPop.getPersons().size());

        String originNetwork = "/Users/convel/Documents/gzpi/MATSimGZ/networkPt2405/gz240509h6SimpPath4526.xml";
        Network network = NetworkUtils.readNetwork(originNetwork);
        scaledPop.getPersons().forEach((personId, person) -> {
            List<? extends PlanElement> tmpPlanElements = person.getPlans().get(0).getPlanElements();
            for (int i = 0; i < tmpPlanElements.size(); i++) {
                if (tmpPlanElements.get(i) instanceof Activity){
                    Activity activity = (Activity) tmpPlanElements.get(i);
                    activity.setLinkId(NetworkUtils.getNearestLink(network,activity.getCoord()).getId());
                }
                if (tmpPlanElements.get(i) instanceof Leg){
                    Leg leg = (Leg) tmpPlanElements.get(i);
                    if (leg.getMode().equals("bus")||leg.getMode().equals("subway")){
                        leg.setMode("pt");
                    }
                    modes.add(leg.getMode());
                }
            }
        });

        new PopulationWriter(scaledPop).write("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration/cen4Zon50perWithSurPop.xml");
//        new PopulationWriter(population1).write("/Users/convel/Documents/gzpi/MATSimGZ/scenarioSimNets/hpPop100w1perWithSurPop.xml");
        //        System.out.println(population.getPersons().size());
//        Population surveyPop = PopulationUtils.readPopulation("linkCount230427/pop/surveyPop.xml");
//        surveyPop.getPersons().forEach((personId, person) -> {
//            person.getAttributes().putAttribute("surveyPeople",true);
//            population.addPerson(person);
//        });
//        Network network = NetworkUtils.readNetwork("linkCount230427/gz230427_fullPath_4526_h9_withSubwayPtMapped.xml");
//        Set<Id<Person>> personActsNoLinks =new HashSet<>();
//        // try to match each person's start link and end link, to avoid subway or mid of highway
//        population.getPersons().forEach((personId, person) -> {
//            List<PlanElement> planElementList = person.getSelectedPlan().getPlanElements();
//            for (int i = 0; i < planElementList.size(); i++) {
//                if(planElementList.get(i) instanceof Activity){
//                    Activity act = (Activity)planElementList.get(i);
//                    Id<Link> tmpLinkId = MatchAct2Link4Population.getLinkId4Act(network,act.getCoord());
//                    if (tmpLinkId!=null){
//                        act.setLinkId(tmpLinkId);
//                    }else{
//                        personActsNoLinks.add(personId);
//                        nullLinkCount++;
//                        return;
//                    }
//                }
//            }
//        });
//
//        System.out.println(surveyPop.getPersons().size());
//        System.out.println(population.getPersons().size());
//        personActsNoLinks.forEach(personId -> population.getPersons().remove(personId));
//        System.out.println(population.getPersons().size()+"-*--*-*-*-*-*-*-*--*-*-*-*-*-*--*-*-*--*");
//        new PopulationWriter(population).write("linkCount230427/pop/mergedPop5k.xml");

    }
    public static int nullLinkCount = 0;

}
