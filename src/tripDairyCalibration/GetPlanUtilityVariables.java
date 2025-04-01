package tripDairyCalibration;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.population.PopulationUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2023/11/7
 * project:geotools-29.2
 */
public class GetPlanUtilityVariables {
    public static double prio = 1.0;
//    public static Config config = ConfigUtils.loadConfig("/Users/convel/Documents/gzpi/问卷调查/论文/surveyCalibration/input1106/config.xml");
    public static Config config = ConfigUtils.loadConfig("/Users/convel/Documents/gzpi/问卷调查/论文/surveyCalibration/scenario1109/config.xml");
    public static void main(String[] args) throws IOException {
        Population surveyPop = PopulationUtils.readPopulation("/Users/convel/Documents/gzpi/问卷调查/论文/surveyCalibration/scenario1110/output/ITERS/it.0/0.plans.xml.gz");
        Population allPop = PopulationUtils.readPopulation("/Users/convel/Documents/gzpi/问卷调查/论文/surveyCalibration/scenario1110/output1110V3/output_plans.xml.gz");
        BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/convel/Documents/gzpi/问卷调查/论文/surveyCalibration/scenario1110/matsimData4BiogemeV3.csv"));

        System.out.println("actDuration, waitTime, lateDiff, earlyDiff, durDiff, travelTimeCar, travelDisCar, deltaMCar, travelTimePt, travelDisPt, deltaMPt, transfersPt, travelTimeWalk, travelDisWalk");
        Map<Id<Person>,List<Double>> dataMap = new HashMap<>();
        Map<Id<Person>,double[]> selectedPlans = new HashMap<>();

        surveyPop.getPersons().forEach((personId, person) -> {
            if (personId.toString().contains("survey_")) {
                Plan selectedPlan = person.getSelectedPlan();
                List<Double> tmpList = createListFromDoubleArray(getPlanVariables(selectedPlan));
                tmpList.add(1.);
                dataMap.put(personId, tmpList);
                selectedPlans.put(personId, getPlanVariables(selectedPlan));
            }
        });

        allPop.getPersons().forEach((personId, person) -> {
            if (dataMap.containsKey(personId)){
                System.out.println(personId);

                person.getPlans().forEach(plan -> {
                    double tmpVariables[] = getPlanVariables(plan);
                    if(compareDoubleArray(selectedPlans.get(personId),tmpVariables)){
    //                    System.out.println("========================================================================");
                        addDoubleArray2ListWithAvailability(dataMap.get(personId),createEmptyArray(tmpVariables.length),0.0);
                    }else{
                        addDoubleArray2ListWithAvailability(dataMap.get(personId),tmpVariables,1.0);
                    }
                });
            }
        });
        bw.write("option,actDuration,waitTime,lateDiff,earlyDiff,durDiff,travelTimeCar,travelDisCar,deltaMCar,ascCar,travelTimePt,travelDisPt,deltaMPt,transfersPt,ascPt,travelTimeWalk,travelDisWalk,ascWalk,av," +
                "actDuration1,waitTime1,lateDiff1,earlyDiff1,durDiff1,travelTimeCar1,travelDisCar1,deltaMCar1,ascCar1,travelTimePt1,travelDisPt1,deltaMPt1,transfersPt1,ascPt1,travelTimeWalk1,travelDisWalk1,ascWalk1,av1," +
                "actDuration2,waitTime2,lateDiff2,earlyDiff2,durDiff2,travelTimeCar2,travelDisCar2,deltaMCar2,ascCar2,travelTimePt2,travelDisPt2,deltaMPt2,transfersPt2,ascPt2,travelTimeWalk2,travelDisWalk2,ascWalk2,av2," +
                "actDuration3,waitTime3,lateDiff3,earlyDiff3,durDiff3,travelTimeCar3,travelDisCar3,deltaMCar3,ascCar3,travelTimePt3,travelDisPt3,deltaMPt3,transfersPt3,ascPt3,travelTimeWalk3,travelDisWalk3,ascWalk3,av3," +
                "actDuration4,waitTime4,lateDiff4,earlyDiff4,durDiff4,travelTimeCar4,travelDisCar4,deltaMCar4,ascCar4,travelTimePt4,travelDisPt4,deltaMPt4,transfersPt4,ascPt4,travelTimeWalk4,travelDisWalk4,ascWalk4,av4," +
                "actDuration5,waitTime5,lateDiff5,earlyDiff5,durDiff5,travelTimeCar5,travelDisCar5,deltaMCar5,ascCar5,travelTimePt5,travelDisPt5,deltaMPt5,transfersPt5,ascPt5,travelTimeWalk5,travelDisWalk5,ascWalk5,av5\n");
        dataMap.values().forEach(value->{
            try {
                bw.write("1,"+doubleArray2Str(value,",")+"\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
//        setupPlanCalGroupParameters("/Users/convel/Documents/gzpi/问卷调查/论文/surveyCalibration/input1106/planCalcScoreConfigGroup.xml");
        System.out.println("done!");
    }
    public static void setupPlanCalGroupParameters(String outputConfig){
        PlanCalcScoreConfigGroup planCalcScoreConfigGroup = config.planCalcScore();

        String actType = "home";
        // for home act
        planCalcScoreConfigGroup.getActivityParams(actType).setEarliestEndTime(4.*3600.); // for the first activity
        planCalcScoreConfigGroup.getActivityParams(actType).setLatestStartTime(11.*3600.); // only for the first activity home based tours
        planCalcScoreConfigGroup.getActivityParams(actType).setMinimalDuration(24600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setTypicalDuration(53600.36);
        // for work activity
        actType = "work";
        planCalcScoreConfigGroup.getActivityParams(actType).setOpeningTime(7.0*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setClosingTime(23.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setEarliestEndTime(10.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setLatestStartTime(17.0*3600.+50.0*60.0);
        planCalcScoreConfigGroup.getActivityParams(actType).setMinimalDuration(6.0*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setTypicalDuration(9.0*3600.+16.0*60.0);
        // for education activity
        actType = "school";
        planCalcScoreConfigGroup.getActivityParams(actType).setOpeningTime(6.5*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setClosingTime(20.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setEarliestEndTime(11.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setLatestStartTime(12.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setMinimalDuration(6.0*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setTypicalDuration(8.0*3600.+43.0*60.0);
        // for leisure activity
        actType = "leisure";
        planCalcScoreConfigGroup.getActivityParams(actType).setEarliestEndTime(10.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setLatestStartTime(22.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setMinimalDuration(0.5*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setTypicalDuration(3.5*3600.);
        // for pickup/dropoff activity
        actType = "pickup/dropoff";
        planCalcScoreConfigGroup.getActivityParams(actType).setEarliestEndTime(7.5*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setLatestStartTime(18.25*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setMinimalDuration(2./60.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setTypicalDuration(10./60.*3600.);
        // for other activity
        actType = "other";
        planCalcScoreConfigGroup.getActivityParams(actType).setEarliestEndTime(6.*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setLatestStartTime(17.25*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setMinimalDuration(25*60.);
        planCalcScoreConfigGroup.getActivityParams(actType).setTypicalDuration(2.0*3600.+25.*60.0);
        // for other activity
        actType = "errand";
        planCalcScoreConfigGroup.getActivityParams(actType).setEarliestEndTime(7.5*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setLatestStartTime(16.0*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setMinimalDuration(0.75*3600.);
        planCalcScoreConfigGroup.getActivityParams(actType).setTypicalDuration(4.0*3600.+5.0*60.0);
        ConfigUtils.writeConfig(config,outputConfig);
    }

    /**
     * activity variables includes for all: duration,lateDiff,earlyDiff,wait(0),durDiff
     * leg(trip) variables include for each mode(pt,walk,car):travelTime,travelDis,deltaM,transfers
     * total variables:duration,startTime,endTime,shortDur,travelTimeCar,travelDisCar,deltaMCar,
     *     travelTimePt,travelDisPt,deltaMPt,transfersPt,travelTimeWalk,travelDisWalk,deltaMWalk,transfersWalk
     *
     */
    public static double[] getPlanVariables(Plan plan){
        PlanCalcScoreConfigGroup planCalcScoreConfigGroup = config.planCalcScore();
//        System.out.println("actDuration, waitTime, lateDiff, earlyDiff, durDiff, travelTimeCar, travelDisCar, deltaMCar, ascCar,travelTimePt, travelDisPt, deltaMPt, transfersPt, ascPt,travelTimeWalk, travelDisWalk,ascWalk");
        List<? extends PlanElement> planElements = plan.getPlanElements();
        double actDuration = 0.,waitTime =0.0, lateDiff = 0.,earlyDiff = 0.,durDiff = 0.;
        double travelTimeCar = 0.,travelDisCar = 0.,deltaMCar = 0.,ascCar = 0.0,
                travelTimePt = 0.,travelDisPt = 0.,deltaMPt = 0.,transfersPt = 0.,ascPt = 0.,
                travelTimeWalk = 0.,travelDisWalk = 0.,ascWalk = 0.;
        // activity has endTime, leg has depTime and travelTime
        double timeStamp = 0.0;
        for (int i = 0; i < planElements.size(); i++) {

            if (planElements.get(i) instanceof Activity){
                Activity tmpAct = (Activity)planElements.get(i);
                PlanCalcScoreConfigGroup.ActivityParams tmpParams = planCalcScoreConfigGroup.getActivityParams(tmpAct.getType());
                if (tmpAct.getType().contains("interaction"))continue;

                double tmpDur = Math.max(0, (i==(planElements.size()-1))?(24.*3600.-timeStamp):(tmpAct.getEndTime().seconds()-timeStamp));
                actDuration += Math.max(0,(Math.log(tmpDur/tmpParams.getTypicalDuration().seconds())+1/(prio!=0?prio:1.)));
                waitTime += ((tmpParams.getOpeningTime().isUndefined()||i==0)?0.:(timeStamp<tmpParams.getOpeningTime().seconds()?(tmpParams.getOpeningTime().seconds()-timeStamp):0.));
                lateDiff += ((tmpParams.getLatestStartTime().isUndefined()||i==planElements.size()-1)?0.:
                               (timeStamp>tmpParams.getLatestStartTime().seconds()?(timeStamp-tmpParams.getLatestStartTime().seconds()):0.));
                earlyDiff += ((tmpParams.getEarliestEndTime().isUndefined()||i==planElements.size()-1)?0.:
                        (tmpParams.getEarliestEndTime().seconds()>tmpAct.getEndTime().seconds()?(tmpParams.getEarliestEndTime().seconds()-tmpAct.getEndTime().seconds()):0.));
                timeStamp = (i==planElements.size()-1)?3600.*24.:tmpAct.getEndTime().seconds();
            }else if(planElements.get(i) instanceof Leg){
                Leg tmpLeg = (Leg)planElements.get(i);
                timeStamp = tmpLeg.getDepartureTime().seconds();
                if (tmpLeg.getMode().equals("car")){
                    travelTimeCar += tmpLeg.getTravelTime().seconds();
                    travelDisCar += tmpLeg.getRoute().getDistance();
                    deltaMCar += travelDisCar*1.1/1000.0;
                    ascCar++;
                }else if (tmpLeg.getMode().equals("pt")){
                    travelDisPt = tmpLeg.getRoute().getDistance();
                    travelTimePt += tmpLeg.getTravelTime().seconds();
                    deltaMPt += 2.0;
                    transfersPt +=1;
                    ascPt++;
                }else if (tmpLeg.getMode().equals("walk")){
                    travelDisWalk += tmpLeg.getRoute().getDistance();
                    travelTimeWalk += tmpLeg.getTravelTime().seconds();
                    ascWalk++;
                }
                timeStamp += tmpLeg.getTravelTime().seconds();
            }
        }
        travelDisPt = Math.max(0.,travelDisPt-1);
        //header:
        // actDuration, waitTime, lateDiff,earlyDiff, durDiff, travelTimeCar, travelDisCar, deltaMCar, travelTimePt, travelDisPt, deltaMPt, transfersPt, travelTimeWalk, travelDisWalk
        double[] planVariables = {actDuration, waitTime, lateDiff, earlyDiff, durDiff, travelTimeCar, travelDisCar,
                deltaMCar, ascCar,travelTimePt, travelDisPt, deltaMPt, transfersPt, ascPt, travelTimeWalk, travelDisWalk, ascWalk};
//        System.out.println(actDuration+", "+waitTime+","+lateDiff+","+earlyDiff+", "+durDiff+", "+travelTimeCar+", "+travelDisCar+
//                ", "+deltaMCar+", "+travelTimePt+", "+travelDisPt+", "+deltaMPt+", "+transfersPt+", :"+travelTimeWalk+", "+ travelDisWalk);
        return planVariables;
    }
    public static List<Double> createListFromDoubleArray(double[] doubles){
        List<Double> doubleList =  new ArrayList<>();
        for (int i = 0; i < doubles.length; i++) {
            doubleList.add(doubles[i]);
        }
        return doubleList;
    }
    public static void addDoubleArray2List(List<Double> doubleList, double[] doubles){
        for (int i = 0; i < doubles.length; i++) {
            doubleList.add(doubles[i]);
        }
    }
    public static void addDoubleArray2ListWithAvailability(List<Double> doubleList, double[] doubles,double availability){
        for (int i = 0; i < doubles.length; i++) {
            doubleList.add(doubles[i]);
        }
        doubleList.add(availability);
    }
    public static double[] createEmptyArray(int length){
        double array[] =new double[length];
        for (int i = 0; i < length; i++) {
            array[0] = 0.0;
        }
        return array;
    }

    public static boolean compareDoubleArray(double[] arr1,double[] arr2){
        if (arr1.length!=arr2.length){
            return false;
        }else{
            boolean ifEqual = true;
            for (int i = 0; i < arr1.length; i++) {
                if(arr1[i]!=arr2[i]){
                    return false;
                }
            }
            return ifEqual;
        }

    }
    public static String doubleArray2Str(List<Double> array,String splitter){
        String str = "";
        for (int i = 0; i < array.size(); i++) {
            str += array.get(i)+splitter;
        }
        return str.substring(0,str.length()-1);
    }
}
