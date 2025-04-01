package population;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.mobsim.qsim.ActivityEngineWithWakeup;
import org.matsim.core.network.NetworkUtils;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/15
 * project:matsimParamCalibration
 */
public class NetworkReverseCreater {
    public static void main(String[] args) {
        Network network = NetworkUtils.readNetwork("/Users/convel/IdeaProjects/matsimParamCalibration/testInput/network.xml");
        network.getLinks().forEach((linkId, link) -> {
            Id<Link> tmpReverseLinkId = Id.createLinkId(link.getToNode().getId().toString()+"-"+link.getFromNode().getId().toString());
            if(!network.getLinks().containsKey(tmpReverseLinkId)){
                NetworkUtils.createAndAddLink(network,tmpReverseLinkId,link.getToNode(),link.getFromNode(),link.getLength(),link.getFreespeed(),link.getCapacity(),link.getNumberOfLanes());
            }
        });
        new NetworkWriter(network).write("/Users/convel/IdeaProjects/matsimParamCalibration/testInput/networkWithReversedLinks.xml");
    }
}
