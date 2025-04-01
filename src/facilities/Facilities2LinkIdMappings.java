package facilities;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.facilities.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/4/1
 * project:matsimParamCalibration
 */
public class Facilities2LinkIdMappings {
    public static void main(String[] args) throws Exception{
        ActivityFacilities activityFacilities = BuildingFacilities2Act.parseFacilityFile("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration0327/gz_facilities.xml");
        Network network = NetworkUtils.readNetwork("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration0327/gzInpoly240126.xml");
        BufferedWriter bw = new BufferedWriter( new FileWriter("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration0327/facilityCentroidsWithLinkId.csv"));
        bw.write("facilityId;linkid;x;y\n");
        activityFacilities.getFacilities().forEach((activityFacilityId, activityFacility) -> {
            Link nearestLink = NetworkUtils.getNearestRightEntryLink(network,activityFacility.getCoord());
            try {
                bw.write(activityFacilityId.toString()+";"+nearestLink.getId().toString()+";"+activityFacility.getCoord().getX()+";"+activityFacility.getCoord().getY()+"\n");
                bw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ((ActivityFacilityImpl)activityFacility).setLinkId(nearestLink.getId());
        });
        FacilitiesWriter facilitiesWriter = new FacilitiesWriter(activityFacilities);
        bw.close();
//
        facilitiesWriter.write("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration0327/gz_facilitiesWithLinkId240401.xml");
        System.out.println("done!");
    }
}
