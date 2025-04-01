package population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;

import java.util.*;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/18
 * project:matsimParamCalibration
 */
public class MatchAct2Link4Population {

    public static void main(String[] args) {
        Population population = PopulationUtils.readPopulation("/Users/convel/Documents/gzpi/MATSimGZ/networkCalibration/linkCount240123/population230406.xml");

        Network network = NetworkUtils.readNetwork("/Users/convel/Documents/gzpi/MATSimGZ/networkCalibration/gz_idRemap_subway_240123.xml");
        Set<Id<Person>> personActsNoLinks =new HashSet<>();
        // try to match each person's start link and end link, to avoid subway or mid of highway
        population.getPersons().forEach((personId, person) -> {
            List<PlanElement> planElementList = person.getSelectedPlan().getPlanElements();
            for (int i = 0; i < planElementList.size(); i++) {
                if(planElementList.get(i) instanceof Activity){
                    Activity act = (Activity)planElementList.get(i);
                    Id<Link> tmpLinkId = MatchAct2Link4Population.getLinkId4Act(network,act.getCoord());
                    if (tmpLinkId!=null){
                        act.setLinkId(tmpLinkId);
                    }else{
                        personActsNoLinks.add(personId);
                        return;
                    }
                }
            }
        });

        System.out.println(population.getPersons().size());
        personActsNoLinks.forEach(personId -> population.getPersons().remove(personId));
        System.out.println(population.getPersons().size()+"-*--*-*-*-*-*-*-*--*-*-*-*-*-*--*-*-*--*");
        new PopulationWriter(population).write("/Users/convel/Documents/gzpi/MATSimGZ/networkCalibration/linkCount240123/population230406WithLinkAct240123.xml");
    }

    public static Id<Link> getLinkId4Act(Network network, Coord coord){
        ArrayList<Node> nodes = (ArrayList<Node>) NetworkUtils.getNearestNodes(network,coord,1000);

        List<Link> linkList = new ArrayList<>();
        nodes.forEach(node ->
                linkList.addAll(node.getOutLinks().values())
        );
        List<Id<Link>> filteredLinkIds = new ArrayList<>();
        String regex = "\\d+\\.\\d+_\\d+\\.\\d+>>\\d+\\.\\d+_\\d+\\.\\d+";

        linkList.forEach(link -> {
            if (link.getAllowedModes().contains("car")||link.getAllowedModes().contains("pt")){
                if (!(link.getAttributes().getAttribute("type").equals("motorway")||
                        link.getAttributes().getAttribute("type").equals("motorway_link")||
                        link.getAttributes().getAttribute("type").equals("trunk")||
                        link.getAttributes().getAttribute("type").equals("trunk_link")
                )){
                    filteredLinkIds.add(link.getId());
                    if(link.getId().toString().matches(regex)){

                    }
                }
            }
        });
        if (filteredLinkIds.size()<1){
            return null;
        }
        return filteredLinkIds.get(new Random().nextInt(filteredLinkIds.size()));
    }

}
