package population;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jgrapht.alg.util.Triple;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.*;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.pt.router.TransitRouterConfig;
import org.matsim.pt.router.TransitRouterImpl;
import org.matsim.pt.transitSchedule.api.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.*;
import java.time.format.DateTimeFormatter;
/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/7/5
 * project:matsimParamCalibration
 */
public class ParseNanshaCardData {
    public static void main(String[] args) throws Exception {
        String cardFile = "/Users/convel/Documents/gzpi/基础研发/公交决策平台/nanshaCard/CARD202306.csv";
        BufferedReader br = new BufferedReader(new FileReader(cardFile));
        String line = br.readLine();
        Map<String, List<String>> ptUsers = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss");
        // Parse the date and time string
//        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
        Map<String, Set<String>> dailyUsers = new HashMap<>(); // active users daily
        Map<String, Integer> dailyCounts = new HashMap<>(); // daily passenger counts
        while ((line = br.readLine()) != null) {
            String tmp[] = line.split(",", -1);
            String id = tmp[0];
//            System.out.println(line);
            String dateStr = tmp[5].split(" ")[0];

            if (ptUsers.containsKey(id)) {
                ptUsers.get(id).add(tmp[3] + "," + tmp[5] + "," + tmp[7] + "," + tmp[8] + "," + tmp[11]);
            } else {
                List<String> tmpStr = new ArrayList<>();
                tmpStr.add(tmp[3] + "," + tmp[5] + "," + tmp[7] + "," + tmp[8] + "," + tmp[11]);
                ptUsers.put(id, tmpStr);
            }

            if (dailyUsers.containsKey(dateStr)) {
                dailyUsers.get(dateStr).add(id);
            } else {
                Set<String> tmpUsers = new HashSet<>();
                tmpUsers.add(id);
                dailyUsers.put(dateStr, tmpUsers);
            }
            if (dailyCounts.containsKey(dateStr)) {
                dailyCounts.put(dateStr, dailyCounts.get(dateStr) + 1);
            } else {
                dailyCounts.put(dateStr, 1);
            }
        }
        for (int i = 1; i < 31; i++) {
            String tmpDataStr = "2023/6/" + i;
            System.out.println(tmpDataStr + "," + dailyUsers.get(tmpDataStr).size() + "," + dailyCounts.get(tmpDataStr));
        }
        System.out.println(ptUsers.size());

        // read transit schedule
        Config config = ConfigUtils.createConfig();
        config.transit().setTransitScheduleFile("/Users/convel/Documents/gzpi/MATSimGZ/networkPt2405/mappedTransitSchedule240528h9KeepPath3857RouteIdCleaned.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        TransitSchedule ts = scenario.getTransitSchedule();
        TransitRouterConfig trc = new TransitRouterConfig(config);
        TransitRouterImpl trr = new TransitRouterImpl(trc, ts);
        System.out.println(ts.getTransitLines().size());
        // get a data and mapping the trips according to its historical trips
        for (int i = 1; i < 2; i++) {
            String tmpDataStr = "2023/6/" + i;
            Set<String> dayIDs = dailyUsers.get(tmpDataStr);
            for (String personId : dayIDs) {
                // get historical trips
                List<String> tmpTripHis = ptUsers.get(personId);
                List<BoardingStop> monthlyBoardingHist = getAvgBoardingStops(tmpTripHis);
                monthlyBoardingHist.forEach(boardingStop -> mappingBusStop(ts,boardingStop));
                List<String> tripsToday = getDayTrips(tmpTripHis, tmpDataStr);
                List<BoardingStop> tripsTodayAsStops = getAvgBoardingStops(tripsToday);
                tripsTodayAsStops.forEach(boardingStop -> {
                    mappingBusStop(ts, boardingStop);
                    boardingStop.getPossibleAlightStops(ts, monthlyBoardingHist);
                });

                List<List<Triple<MappedLineRouteStop,MappedLineRouteStop,Double>>> tripChainsToday = generatePermutations(tripsTodayAsStops);
                // merge transfer trips
                // filter impossible ods
                // score all possible pt ods and select the on with the highest scores
                System.out.println("test possible trip chains: ");
                List<Triple<MappedLineRouteStop, MappedLineRouteStop, Double>> mostPossStopChains = evaluateAndGetMostPossibleStopChains(tripChainsToday,ts);
                if (mostPossStopChains!=null) {
                    mostPossStopChains.forEach(t -> System.out.print(t.getFirst().mappedLineId + "." + t.getFirst().mappedRouteId + "." + t.getFirst().mappedStopId
                            + ">>" + t.getSecond().mappedLineId + "." + t.getSecond().mappedRouteId + "." + t.getSecond().mappedStopId + " at:" + t.getThird() + ">>>>"));
                }else {
                    System.out.println( "____________________");
                }
                System.out.println("++++++++++++++++++++++++++++++++++++");
            }
        }
    }
    public static List<Triple<MappedLineRouteStop, MappedLineRouteStop, Double>> evaluateAndGetMostPossibleStopChains(List<List<Triple<MappedLineRouteStop, MappedLineRouteStop, Double>>> possibleStopChains, TransitSchedule ts){
        if (possibleStopChains.size() == 1) {return possibleStopChains.get(0);}
        else if (possibleStopChains.size() < 1) {return null;}
        else {
            double bestScore = - Double.MAX_VALUE;
            List<Triple<MappedLineRouteStop, MappedLineRouteStop, Double>> bestChain = new ArrayList<>();
            for (int i = 0; i < possibleStopChains.size(); i++) {
                double tmpScore = 0.0;
                for (int j = 0; j < possibleStopChains.get(i).size(); j++) {
                    // test if the arrival time is later than next stop chain's boarding time
                    Triple<MappedLineRouteStop, MappedLineRouteStop, Double> stopChain = possibleStopChains.get(i).get(j);
                    if (j<possibleStopChains.size() - 2) {
                        Triple<MappedLineRouteStop, MappedLineRouteStop, Double> nextStopChain = possibleStopChains.get(i).get(j);
                        // calculate alight time
                        double estAlightTime = calAlightTime(stopChain,ts);
                        if (estAlightTime < nextStopChain.getThird()) {
                            tmpScore+=2;
                            if (nextStopChain.getThird()-estAlightTime<10*60.0){ //this should be transfer
                                tmpScore+=10;
                            }
                        }else {
                            tmpScore-=100;
                        }
                    }
                }
                if (tmpScore > bestScore) {
                    bestScore = tmpScore;
                    bestChain = possibleStopChains.get(i);
                }
            }
            return bestChain;
        }
    }
    public static double calAlightTime(Triple<MappedLineRouteStop, MappedLineRouteStop, Double> possibleStopChains, TransitSchedule ts){
        double boardTime = possibleStopChains.getThird();
        TransitStopFacility boardingStop = ts.getFacilities().get(possibleStopChains.getFirst().mappedStopId);
        TransitStopFacility alightStop = ts.getFacilities().get(possibleStopChains.getSecond().mappedStopId);
        double distance = CoordUtils.calcEuclideanDistance(boardingStop.getCoord(),alightStop.getCoord())*1.4;
        return boardTime+distance/25.0;
    }

    public static List<String> getDayTrips(List<String> tripHis,String day){
        List<String> dayTrips = new ArrayList<>();
        tripHis.forEach(trip->{
            String tripData = trip.split(",")[1].split(" ")[0];
            if (tripData.equals(day)){
                dayTrips.add(trip);
            }
        });
        return dayTrips;
    }
    public static List<BoardingStop> getAvgBoardingStops(List<String> tripStrs){
        Map<String, List<Double>> tmpStops = new HashMap<>();
        for (int i = 0; i < tripStrs.size(); i++) {
            String tmp[] = tripStrs.get(i).split(",",-1);
            if (tmp[4].equals("")) {
                continue;
            }
            String lineStopId = tmp[0]+","+tmp[4];
            double seconds = LocalTime.parse(tmp[1].split(" ")[1]).toSecondOfDay();
            if(tmpStops.containsKey(lineStopId)){
                tmpStops.get(lineStopId).add(seconds);
            }else{
                List<Double> tmpSeds = new ArrayList<>();tmpSeds.add(seconds);
                tmpStops.put(lineStopId,tmpSeds);
            }
        }
        List<BoardingStop> boardingStops = new ArrayList<>();
        tmpStops.forEach((s, doubleList) -> {
            double avgTime = doubleList.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
            boardingStops.add(new BoardingStop(s,avgTime,doubleList.size()));
        });
        Collections.sort(boardingStops,new Comparator<BoardingStop>() {
            @Override
            public int compare(BoardingStop p1, BoardingStop p2) {
                return Double.compare(p1.seconds, p2.seconds);
            }
        });
        return boardingStops;
    }
    public static void mappingBusStop(TransitSchedule transitSchedule, BoardingStop boardingStop) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int shortestDistance = Integer.MAX_VALUE;
        // the distance needed to evaluated and calculated by line and stop name together
        for (TransitLine transitLine:transitSchedule.getTransitLines().values()){
            for(TransitRoute transitRoute:transitLine.getRoutes().values()){
                for (int i = 0; i < transitRoute.getStops().size(); i++) {
                    String tmpMappingStr = transitLine.getName()+">>"+transitRoute.getStops().get(i).getStopFacility().getName();
                    int distance = levenshtein.apply(boardingStop.lineName+">>"+boardingStop.stopName, tmpMappingStr);
                    if (getNumInStr(boardingStop.lineName)!=getNumInStr(transitLine.getName())){
                        distance+=10;
                    }
//                    System.out.println(boardingStop.lineName+">>"+boardingStop.stopName+"||"+tmpMappingStr+"||levDis:"+distance);
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        MappedLineRouteStop mappedLineRouteStop = new MappedLineRouteStop(transitLine.getId(),transitRoute.getId(),transitRoute.getStops().get(i).getStopFacility().getId());
                        boardingStop.mappedLineRouteStop = new HashSet<>();boardingStop.mappedLineRouteStop.add(mappedLineRouteStop);
                    }else if(distance == shortestDistance) { // for reversed transit route
                        boardingStop.mappedLineRouteStop.add(new MappedLineRouteStop(transitLine.getId(),transitRoute.getId(),transitRoute.getStops().get(i).getStopFacility().getId()));
                    }
                }
            }
        }
        // mapped result:
//        System.out.print(boardingStop.lineName+","+boardingStop.stopName+ " is mapped to:");
//        boardingStop.mappedLineRouteStop.forEach(mappedLineRouteStop -> {
//            System.out.println(transitSchedule.getTransitLines().get(mappedLineRouteStop.mappedLineId).getName()+","+
//                    transitSchedule.getTransitLines().get(mappedLineRouteStop.mappedLineId).getRoutes().get(mappedLineRouteStop.mappedRouteId)+","+
//                    transitSchedule.getFacilities().get(mappedLineRouteStop.mappedStopId).getName());
//        });
    }

    static class BusStop{
        String lineName;
        String stopName;
        double seconds; // boarding or alighting the bus
        public BusStop(String lineNameStopName, double seconds) {
            this.lineName = lineNameStopName.split(",")[0];
            this.stopName = lineNameStopName.split(",")[1];
            this.seconds = seconds;
        }
        public BusStop(String lineName,String StopName, double seconds) {
            this.lineName = lineName;
            this.stopName = StopName;
            this.seconds = seconds;
        }
    }

    static class BoardingStop extends BusStop{
        public int numRecorded;
        public double levnDis;
        public Set<MappedLineRouteStop> mappedLineRouteStop;
        public BoardingStop(String lineNameStopName, double seconds,int numRecorded) {
            super(lineNameStopName,seconds);
            this.numRecorded = numRecorded;
        }
        Map<MappedLineRouteStop,List<MappedLineRouteStop>> possibleAlightStops = new HashMap<>();
        List<Triple<MappedLineRouteStop, MappedLineRouteStop, Double >> possibleBoardAlightStopCombos = new ArrayList<>();
        public void getPossibleAlightStops(TransitSchedule ts, List<BoardingStop> boardingHistory) {
            for (MappedLineRouteStop mappedLineRouteStop : mappedLineRouteStop) {
                for (int i = 0; i < boardingHistory.size(); i++) {
                    BoardingStop tmpBoardingStop = boardingHistory.get(i);
                    for (MappedLineRouteStop alightStop : tmpBoardingStop.mappedLineRouteStop) {
                        List<TransitRouteStop> stopList = ts.getTransitLines().get(mappedLineRouteStop.mappedLineId).getRoutes().get(mappedLineRouteStop.mappedRouteId).getStops();
                        int boardingIndex = getTransitRouteStopListIndex(stopList, mappedLineRouteStop.mappedStopId);
                        int alightIndex = getTransitRouteStopListIndex(stopList, alightStop.mappedStopId);
                        if (mappedLineRouteStop.mappedRouteId.equals(alightStop.mappedRouteId) && boardingIndex < alightIndex) {
                            // alight stop must be after boarding stop
                            if (!possibleAlightStops.containsKey(mappedLineRouteStop)) {
                                List<MappedLineRouteStop> tmpStops = new ArrayList<>();
                                tmpStops.add(alightStop);
                                possibleAlightStops.put(mappedLineRouteStop, tmpStops);
                            } else {
                                possibleAlightStops.get(mappedLineRouteStop).add(alightStop);
                            }
                        }
                    }
                }
                if (!possibleAlightStops.containsKey(mappedLineRouteStop)) {
                    List<TransitRouteStop> stopList = ts.getTransitLines().get(mappedLineRouteStop.mappedLineId).getRoutes().get(mappedLineRouteStop.mappedRouteId).getStops();
                    int boardingIndex = getTransitRouteStopListIndex(stopList, mappedLineRouteStop.mappedStopId);
                    if (boardingIndex == stopList.size() - 1) {
                        System.out.println("Boarding stop should not be the last stop of the bus route!!!!");
                        continue;
                    }
                    TransitRouteStop tmpAlightTransitRouteStop = stopList.get(Math.min(boardingIndex + 1 + new Random().nextInt(stopList.size() - boardingIndex - 1), stopList.size() - 1));
                    MappedLineRouteStop tmpAlightStop = new MappedLineRouteStop(mappedLineRouteStop.mappedLineId, mappedLineRouteStop.mappedRouteId, tmpAlightTransitRouteStop.getStopFacility().getId());
                    possibleBoardAlightStopCombos.add(new Triple(mappedLineRouteStop, tmpAlightStop, this.seconds));
                }
            }

            for (Map.Entry possibleAlightStop : possibleAlightStops.entrySet()) {
                //todo   if there is no alight stop mapped
                MappedLineRouteStop aboardStop = (MappedLineRouteStop) possibleAlightStop.getKey();
                List<MappedLineRouteStop> tmpAlightStops = (List<MappedLineRouteStop>) possibleAlightStop.getValue();
                tmpAlightStops.forEach(tmpLineRouteStop -> possibleBoardAlightStopCombos.add(new Triple(aboardStop, tmpLineRouteStop, this.seconds)));
            }
        }
    }

    public static List<List<Triple<MappedLineRouteStop,MappedLineRouteStop,Double>>> generatePermutations(List<BoardingStop> aList) {
        List<List<Triple<MappedLineRouteStop,MappedLineRouteStop,Double>>> result = new ArrayList<>();
        generatePermutationsRecursive(aList, 0, new ArrayList<>(), result);
        return result;
    }

    private static  void generatePermutationsRecursive(List<BoardingStop> aList, int index,
                                                          List<Triple<MappedLineRouteStop,MappedLineRouteStop,Double>> currentPermutation, List<List<Triple<MappedLineRouteStop,MappedLineRouteStop,Double>>> result) {
        if (index == aList.size()) {
            result.add(new ArrayList<>(currentPermutation));
            return;
        }

        for (Triple<MappedLineRouteStop,MappedLineRouteStop,Double> b : aList.get(index).possibleBoardAlightStopCombos) {
            currentPermutation.add(b);
            generatePermutationsRecursive(aList, index + 1, currentPermutation, result);
            currentPermutation.remove(currentPermutation.size() - 1); // backtrack
        }
    }

    public static int getTransitRouteStopListIndex(List<TransitRouteStop> stopList,Id<TransitStopFacility> stopFacilityId){
        for (int i = 0; i < stopList.size(); i++) {
            if (stopList.get(i).getStopFacility().getId().equals(stopFacilityId)){
                return i;
            }
        }
        return -1;
    }
    static class MappedLineRouteStop {
        Id<TransitLine> mappedLineId;
        Id<TransitRoute> mappedRouteId;
        Id<TransitStopFacility> mappedStopId;

        public MappedLineRouteStop(Id<TransitLine> mappedLineId, Id<TransitRoute> mappedRouteID, Id<TransitStopFacility> mappedStopIds) {
            this.mappedLineId = mappedLineId;
            this.mappedRouteId = mappedRouteID;
            this.mappedStopId = mappedStopIds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MappedLineRouteStop)) return false;
            MappedLineRouteStop that = (MappedLineRouteStop) o;
            return Objects.equals(mappedLineId, that.mappedLineId) && Objects.equals(mappedRouteId, that.mappedRouteId) && Objects.equals(mappedStopId, that.mappedStopId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mappedLineId, mappedRouteId, mappedStopId);
        }
    }

//    static void evaluateBusTripChains(List<BoardingStop> boardingTrips2BeMapped, List<BoardingStop> monthlyStops, TransitSchedule ts,TransitRouterImpl tr){
//        // get candidate bus trip chains
//        List<List<List<?extends PlanElement>>> candidatesBusTripChains = getCandidatesBusTripChains(boardingTrips2BeMapped,monthlyStops,ts,tr);
//        // evaluate and get the best trip chains
//        List<BusTripChainsWithScore> busTripChainsWithScoreList = new ArrayList<>();
//        for (int i = 0; i < candidatesBusTripChains.size(); i++) {
//            busTripChainsWithScoreList.add(scoreBusTripChains(candidatesBusTripChains.get(i), monthlyStops));
//        }
//    }

    public static List<? extends  PlanElement> assignRandomAlightStop(TransitSchedule ts,  TransitRouterImpl tr,MappedLineRouteStop mappedLineRouteStop, double depTime){
        int iCurrentStop = -1;
        List<TransitRouteStop> stopList = ts.getTransitLines().get(mappedLineRouteStop.mappedLineId).getRoutes().get(mappedLineRouteStop.mappedRouteId).getStops();
        for (int i = 0; i < stopList.size(); i++) {
            if (stopList.get(i).getStopFacility().getId().equals(mappedLineRouteStop.mappedStopId)){
                iCurrentStop = i;break;
            }
        }
        if (iCurrentStop==-1){
            System.out.println("no stop is found..."); return  null;
        } else if (iCurrentStop==stopList.size()-1) {
            System.out.println("current stop is the last stop, loop route is not yet solved...");return null;
        }else{
            TransitRouteStop alightStop = stopList.get(iCurrentStop+1+new Random().nextInt(stopList.size()-iCurrentStop-1));
            RoutingRequestImpl routingRequest = new RoutingRequestImpl(ts.getFacilities().get(mappedLineRouteStop.mappedStopId),
                    alightStop.getStopFacility(),depTime);
            List<? extends PlanElement> planElements = tr.calcRoute(routingRequest);
            return planElements;
        }
    }
    public static List<Plan> candidateLegs2Plans(List<List<List<? extends PlanElement>>> candidateLegs){
        Population population  = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationFactory pf = population.getFactory();
        List<Plan> planList = new ArrayList<>();
        for (int i = 0; i < candidateLegs.size(); i++) {
            Plan tmpPlan = pf.createPlan();
            // get the first leg and its position
            for (int j = 0; j < candidateLegs.get(i).size(); j++) {
                Leg firstLeg = (Leg)candidateLegs.get(i).get(j).get(0);
                TransitRoute transitRoute = (TransitRoute) firstLeg.getRoute();
                // TODO: 2024/7/8 get the act coord or facility id
                Activity act = pf.createActivityFromCoord("dummy",transitRoute.getStops().get(0).getStopFacility().getCoord());
                act.setEndTime(firstLeg.getDepartureTime().seconds());
                tmpPlan.addActivity(act);
                Leg lastLeg = null;
                for (int k = 0; k < candidateLegs.get(i).get(j).size(); k++) {
                    if(candidateLegs.get(i).get(j).get(k) instanceof Leg){
                        tmpPlan.addLeg((Leg)candidateLegs.get(i).get(j).get(k));
                        if (k==candidateLegs.get(i).get(j).size()-1)lastLeg = (Leg)candidateLegs.get(i).get(j).get(k);
                    }else if(candidateLegs.get(i).get(j).get(k) instanceof Activity) {
                        tmpPlan.addActivity((Activity)candidateLegs.get(i).get(j).get(k));
                    }
                }
                // TODO: 2024/7/8 get the act coord or facility id
                Activity endAct= pf.createActivityFromCoord("dummy",((TransitRoute)lastLeg.getRoute()).getStops().get(((TransitRoute)lastLeg.getRoute()).getStops().size()-1).getStopFacility().getCoord());
                tmpPlan.addActivity(endAct);
            }
            planList.add(tmpPlan);
        }
        return planList;
    }

    /**
     * this method finds out if the two boarding records is transfer for the same trip,
     * it is believed that it is the same trip if the leg list's trvale time is within 10min buffer as the baoding time difference
     * @param thisStopFacility
     * @param nextStopFacility
     * @param ts
     * @param trr
     * @param thisStopBoardTime
     * @param nextStopBoardTime
     * @return
     */
    public static List<? extends PlanElement> isTransferBtwStops(TransitStopFacility thisStopFacility, TransitStopFacility nextStopFacility, TransitSchedule ts, TransitRouterImpl trr,double thisStopBoardTime,double nextStopBoardTime){

        RoutingRequestImpl routingRequest= new RoutingRequestImpl(thisStopFacility,nextStopFacility,thisStopBoardTime-61.0); // make sure the trip maker could enter the boarding bus
        List<? extends PlanElement> tmpLegs = trr.calcRoute(routingRequest);
                // TODO: 2024/7/8 check distance between stops as well?
       if(travelTimeInBuffer(tmpLegs,nextStopBoardTime-thisStopBoardTime,600.0)) {
           return tmpLegs;
       }
        return new ArrayList<>();
    }

    public static boolean travelTimeInBuffer(List<? extends PlanElement> planElements,double timeDiff, double buffer){
        // cal travel time
        double sumTravelTime = 0.0;
        for (int i = 0; i < planElements.size(); i++) {
            if(planElements.get(i) instanceof Leg){
                sumTravelTime+=((Leg) planElements.get(i)).getTravelTime().seconds();
            }
        }
        if (Math.abs(sumTravelTime-timeDiff)<=buffer){
            return true;
        }
        return false;
    }
    public static BusTripChainsWithScore scoreBusTripChains(List<List<?extends PlanElement>> busTripChains, List<BoardingStop> monthlyStops){
        BusTripChainsWithScore busTripChainsWithScore = new BusTripChainsWithScore(busTripChains);
        // there are different situation to be scored for....
        // each List<?extends PlanElement> is a route estimated with the beginning of boardingInfo and  alight
        // check  consistency of the departure and arrival time, if not consistent -100
        if (true){  //time inconsistent
            busTripChainsWithScore.score -= 100.0;
        }
//         if there is transfer   need to check +10???
        // if the alight stop is in monthlyStops +10*frequency
        for (int i = 0; i < monthlyStops.size(); i++) {

            if(true){
                busTripChainsWithScore.score += monthlyStops.get(i).numRecorded*1; //there is
                // if the upstream downstream is correct  +10

            }
        }
        // TODO: 2024/7/10  decide it is upstream or downstream flows
        // 使用 Comparator lambda
//        busTripChainsWithScores.sort((p1, p2) -> Double.compare(p2.score, p1.score));
        return busTripChainsWithScore;
    }
    public static class BusTripChainsWithScore{
        List<List<?extends PlanElement>> busTripChains;
        double score = -1;

        public BusTripChainsWithScore(List<List<? extends PlanElement>> busTripChains) {
            this.busTripChains = busTripChains;
        }
    }
    public static int getNumInStr(String input) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return -1; // 或者你可以选择抛出异常或返回其他值以表示未找到数字
    }

}
