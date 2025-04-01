package facilities;

import org.apache.commons.math3.analysis.function.Pow;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.*;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/3/25
 * project:matsimParamCalibration
 */
public class BuildingFacilities2Act {
    public static void main(String[] args) throws Exception{
        ActivityFacilities activityFacilities = parseFacilityFile("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration0327/gz_facilities.xml");
        BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration0327/testAct.csv"));
        bw.write("geom;type;floor\n");
//        Network network = NetworkUtils.readNetwork("/Users/convel/Documents/gzpi/MATSimGZ/parameterCalibration/gzInpoly240126.xml");
        Population population = PopulationUtils.readPopulation("/Users/convel/Documents/gzpi/MATSimGZ/scenario240201/popHpWithSurvey240124.xml");
        population.getPersons().values().parallelStream().forEach(person -> {
            person.getPlans().forEach(plan -> {
                List<PlanElement> planElements = plan.getPlanElements();
                for (int i = 0; i < planElements.size(); i++) {
                    Set<String> actTypes = new HashSet<>();
                    if(planElements.get(i) instanceof Activity && !((Activity) planElements.get(i)).getType().contains("interaction")){
                        Activity tmpAct = (Activity) planElements.get(i);
                        if (!actTypes.contains(tmpAct.getType())){
//                            System.out.println(tmpAct.getCoord().getX()+","+tmpAct.getCoord().getY()+","+tmpAct.getType());
                            ActivityFacility activityFacility = getNearestFacilityInRange(tmpAct.getCoord(),tmpAct.getType(),activityFacilities);

                            List<Polygon> actPolygon = parsePolygonCoordinates(activityFacility.getAttributes().getAttribute("coordinates").toString(),10.0);
                            tmpAct.setCoord(new Coord(genRndPoint(actPolygon,10.0)));
                            int tmpFloor = new Random().nextInt((int)activityFacility.getAttributes().getAttribute("floor"))+1;
                            tmpAct.getAttributes().putAttribute("floor",tmpFloor);
//                            System.out.println("point( "+tmpAct.getCoord().getX()+" "+tmpAct.getCoord().getY()+"); "+tmpAct.getType()+";"+tmpFloor);
                            try {
                                bw.write("point( "+tmpAct.getCoord().getX()+" "+tmpAct.getCoord().getY()+"); "+tmpAct.getType()+";"+tmpFloor+"\n");
                                bw.flush();

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            tmpAct.setFacilityId(activityFacility.getId());
                            actTypes.add(tmpAct.getType());
                        }
                    }
                }
            });
        });
        bw.flush();bw.close();
//        System.out.println("facilities: "+activityFacilities.getFacilities().size()+", network: "+ network.getLinks().size()+" links, population: "+population.getPersons().size());

//        population.getPersons().forEach();
////        System.out.println(activityFacilities.getFacilities().size());
//        String test = "[\n" +
//                "        [\n" +
//                "            [35.0, 10.0],\n" +
//                "            [45.0, 45.0],\n" +
//                "            [15.0, 40.0],\n" +
//                "            [10.0, 20.0],\n" +
//                "            [35.0, 10.0]\n" +
//                "        ],\n" +
//                "        [\n" +
//                "            [20.0, 30.0],\n" +
//                "            [35.0, 35.0],\n" +
//                "            [30.0, 20.0],\n" +
//                "            [20.0, 30.0]\n" +
//                "        ],[[16.4,38.7],[16.4,31.8],[34.2,36.4],[36.4,42.3],[16.4,38.7]],[[15.6,27.3],[12.7,20.9],[25.1,15.8],[21.2,24.3],[15.6,27.3]]" +
//                "    ]";
//        test = test.replace("\n","").replace(" ","");
//        System.out.println(test);
//        System.out.println("polygon(("+test.replace("]],[[","),(").replace(","," ").replace("] [",",").replace(") (","),(").replace("[[[","").replace("]]]","")+"))");
//        List<Polygon> test1 = parsePolygonCoordinates(test,10);
//        for (int i = 0; i < 100; i++) {
//            double centroid[] = genRndPoint(test1,10);
//            System.out.println("point("+centroid[0]+" "+centroid[1]+");"+i);
//        }
        new PopulationWriter(population).write("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration0327/popHpWithSurvey240327.xml");
        System.out.println();
    }
    public static ActivityFacility getNearestFacilityInRange(Coord coord, String actType, ActivityFacilities activityFacilities){
        AtomicReference<Double> distance = new AtomicReference<>(Double.MAX_VALUE);
        ActivityFacility[] nearestFacility = new ActivityFacility[1];
        activityFacilities.getFacilities().forEach((activityFacilityId, activityFacility) -> {
            double tmpDis = CoordUtils.calcEuclideanDistance(coord,activityFacility.getCoord());
            if (tmpDis<= distance.get()){
                if (actType.equals("pickup")||actType.equals("errand")){
                    nearestFacility[0] = activityFacility;
                    distance.set(tmpDis);
                }else if (activityFacility.getActivityOptions().containsKey(actType)){
                    nearestFacility[0] = activityFacility;
                    distance.set(tmpDis);
                }
            }
        });
        return nearestFacility[0];
    }
    public static ActivityFacilities parseFacilityFile(String file){
//        MatsimFacilitiesReader matsimFacilitiesReader = new MatsimFacilitiesReader(scenario);
//        matsimFacilitiesReader.readFile("");
        Scenario scenario = ScenarioUtils.createMutableScenario( ConfigUtils.createConfig() );
        MatsimFacilitiesReader mr = new MatsimFacilitiesReader(scenario);
        mr.readFile(file);
        return scenario.getActivityFacilities();
    }
    public void writeActFacilityFile(){
//        ActivityFacility actFacility = scenario.getActivityFacilities().getFactory().createActivityFacility(Id.create("",ActivityFacility.class),new Coord(0.0,0.0));
//        actFacility.getAttributes().putAttribute("floor",8);
//        actFacility.getAttributes().putAttribute("height",30.0);
//        actFacility.getAttributes().putAttribute("coordinates" , "[[x,y],[x,y]]");
//        ActivityOption activityOptionWork = new ActivityOptionImpl("work"); activityOptionWork.addOpeningTime(new OpeningTimeImpl(7.0*3600,22.0*3600.0));
//        ActivityOption activityOptionOther = new ActivityOptionImpl("Other");activityOptionOther.addOpeningTime(new OpeningTimeImpl(9.0*3600,17.0*3600.0));
//        actFacility.addActivityOption(activityOptionWork);actFacility.addActivityOption(activityOptionOther);
//        ActivityFacilities facilities = new ActivityFacilitiesImpl();
//        facilities.addActivityFacility(actFacility);
//        FacilitiesWriter facilitiesWriter = new FacilitiesWriter(facilities);
//
//        facilitiesWriter.write("/users/convel/desktop/testFacilities.xml");
    }
    public static List<Polygon> parsePolygonCoordinates(String coordinatesStr, double scale) {
        List<Polygon> polygons = new ArrayList<>();

        String tmpPolygons[] = coordinatesStr.replace(" ","").split("\\]\\],\\[\\[");
        for (int i = 0; i < tmpPolygons.length; i++) {
            String tmpPointsStr []= tmpPolygons[i].split("\\],\\[");
            int[] xs = new int[tmpPointsStr.length];
            int[] ys = new int[tmpPointsStr.length];
            for (int j = 0; j < tmpPointsStr.length; j++) {
                String[] pointStr = tmpPointsStr[j].replace("[","").replace("]","").split(",");
                if (Double.parseDouble(pointStr[0])*scale>=Integer.MAX_VALUE){
                    System.out.println("ERROR after scaling, coordinate x too large!!!! for EPSG:4536 , scale shall not larger than 10.");
                }else {
                    xs[j] = (int)(Double.parseDouble(pointStr[0])*scale);
                }
                if (Double.parseDouble(pointStr[1])*scale>=Integer.MAX_VALUE){
                    System.out.println("ERROR after scaling, coordinate y too large!!!!");
                }else {
                    ys[j] = (int)(Double.parseDouble(pointStr[1])*scale);
                }
            }
            Polygon polygon = new Polygon(xs, ys, xs.length);
            polygons.add(polygon);

        }
        return polygons;
    }
    public static double[] genRndPoint(List<Polygon> polygons, double scale){
        Random random = new Random();
        Polygon outerPoly = polygons.get(0);
        List<Polygon> holes = polygons.subList(1,polygons.size());
        int x = outerPoly.getBounds().x+ random.nextInt(outerPoly.getBounds().width);
        int y = outerPoly.getBounds().y+ random.nextInt(outerPoly.getBounds().height);
        boolean isInPolygon = false;
        while(!isInPolygon){
            if(outerPoly.contains(x,y)) {
                isInPolygon = true;
                for (int i = 0; i < holes.size(); i++) {
                    if (holes.get(i).contains(x,y)){
                        isInPolygon = false;
                        break;
                    }
                }
            }else {
                isInPolygon = false;
            }
            if (!isInPolygon){
                x = outerPoly.getBounds().x+ random.nextInt(outerPoly.getBounds().width);
                y = outerPoly.getBounds().y+ random.nextInt(outerPoly.getBounds().height);
            }

        }
        return new double[]{(double)x/scale,(double)y/scale};
    }
    public static double[] calCentroid(Polygon polygon,double scale) {

        double area = 0.0;
        double centroidX = 0.0;
        double centroidY = 0.0;

        // Get the vertices of the Polygon
        int[] xPoints = polygon.xpoints;
        int[] yPoints = polygon.ypoints;

        // Iterate over the vertices of the Polygon
        for (int i = 0; i < polygon.npoints - 1; i++) {
            double partialArea = xPoints[i] * yPoints[i + 1] - xPoints[i + 1] * yPoints[i];
            area += partialArea;
            centroidX += (xPoints[i] + xPoints[i + 1]) * partialArea;
            centroidY += (yPoints[i] + yPoints[i + 1]) * partialArea;
        }

        // Compute the final area and centroid coordinates
        area /= 2.0;
        centroidX /= (6 * area);
        centroidY /= (6 * area);

        return new double[]{centroidX/scale, centroidY/scale};

    }

}
