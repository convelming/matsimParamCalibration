package utils;

import com.google.inject.Inject;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Author：Milu
 * 严重落后进度啦......
 * this is a baddddddd method to parse parameters in planCalcScoreConfigGroup
 *  note HashMap elements do not have sequential order when tranversing !!!!!!!!!!!!!!!
 * date：2024/1/18
 * project:matsimParamCalibration
 */
public class PlanCalcScoreParamDoubleArrayMapping {
    private double[] array;
    private LinkedHashMap<String, LinkedHashMap<String,Double>> actParamMap; private int iEndActIndex = 7;
    private LinkedHashMap<String, LinkedHashMap<String,Double>> modeParamMap;private int iEndModeIndex = 0;
    public List<String> arrayIndex;
    private PlanCalcScoreConfigGroup planCalcScoreConfigGroup;// TODO: 2024/3/7 check if this should be a normal parameter or injected
    private PlanCalcScoreConfigGroup.ScoringParameterSet scoringParameterSet;
    public PlanCalcScoreParamDoubleArrayMapping(PlanCalcScoreConfigGroup planCalcScoreConfigGroup) {

        actParamMap = new LinkedHashMap<>();
        modeParamMap = new LinkedHashMap<>();
        this.planCalcScoreConfigGroup =planCalcScoreConfigGroup;
        this.scoringParameterSet = planCalcScoreConfigGroup.getScoringParameters(null);
        // todo what if there are other subpopulations: possible solution is add subpopulation in planParamCalibrationConfigGroup,or by default null
        // // TODO: 2024/3/6 NNOOTT default set or map DOES NOT is not sequential!!!
        // for scoring parameters the first 7 parameters are fixed and for the time being no good solution to be automized...
        //    marginalUtilityOfWaiting_s[0];marginalUtilityOfLateArrival_s[1];marginalUtilityOfEarlyDeparture_s[2];
        //    marginalUtilityOfWaitingPt_s[3];marginalUtilityOfPerforming_s[4];utilityOfLineSwitch[5];marginalUtilityOfMoney[6];
        // add non default values to actParamMap, later will index to array accordingly, as some variables doesn't need to for various reasons undefined will be leave out
        scoringParameterSet.getActivityParamsPerType().forEach((actType,actParams)->{
//            <parameterset type="activityParams" >
//				<param name="activityType" value="taxi interaction" />
//				<param name="closingTime" value="undefined" />
//				<param name="earliestEndTime" value="undefined" />
//				<param name="latestStartTime" value="undefined" />
//				<param name="minimalDuration" value="undefined" />
//				<param name="openingTime" value="undefined" />
//				<param name="priority" value="1.0" />
//				<param name="scoringThisActivityAtAll" value="false" />
//				<param name="typicalDuration" value="undefined" />
//				<param name="typicalDurationScoreComputation" value="relative" />
//			</parameterset>
            LinkedHashMap<String,Double> tmpActParams = new LinkedHashMap<>();
            if (!actType.endsWith("interaction")){
                if(actParams.getOpeningTime().isDefined()){tmpActParams.put("openingTime",actParams.getOpeningTime().seconds());iEndActIndex++;}
                if(actParams.getClosingTime().isDefined()){tmpActParams.put("closingTime",actParams.getClosingTime().seconds());iEndActIndex++;}
                if(actParams.getEarliestEndTime().isDefined()){tmpActParams.put("earliestEndTime",actParams.getEarliestEndTime().seconds());iEndActIndex++;}
                if(actParams.getLatestStartTime().isDefined()){tmpActParams.put("latestStartTime",actParams.getLatestStartTime().seconds());iEndActIndex++;}
                if(actParams.getMinimalDuration().isDefined()){tmpActParams.put("minimalDuration",actParams.getMinimalDuration().seconds());iEndActIndex++;}
                if(actParams.getTypicalDuration().isDefined()){tmpActParams.put("typicalDuration",actParams.getTypicalDuration().seconds());iEndActIndex++;}
            }
            actParamMap.put(actType,tmpActParams);

        });

        // for mode parameterset
        //  <param name="constant" value="0.0" />
        //	<param name="dailyMonetaryConstant" value="0.0" />
        //	<param name="dailyUtilityConstant" value="0.0" />
        //	<param name="marginalUtilityOfDistance_util_m" value="0.0" />
        //	<param name="marginalUtilityOfTraveling_util_hr" value="-6.0" />
        //	<param name="mode" value="pt" />
        //	<param name="monetaryDistanceRate" value="0.0" />
        iEndModeIndex = iEndActIndex ;
        scoringParameterSet.getModes().forEach((mode, modeParams) -> {
            LinkedHashMap<String,Double> tmpModeParams = new LinkedHashMap<>();
            tmpModeParams.put("asc",modeParams.getConstant());
            tmpModeParams.put("dailyMonetaryConstant",modeParams.getDailyMonetaryConstant());
            tmpModeParams.put("monetaryDistanceRate",modeParams.getMonetaryDistanceRate());
            tmpModeParams.put("dailyUtilityConstant",modeParams.getDailyUtilityConstant());
            tmpModeParams.put("marginalUtilityOfDistance",modeParams.getMarginalUtilityOfDistance());
            tmpModeParams.put("marginalUtilityOfTraveling",modeParams.getMarginalUtilityOfTraveling());
            iEndModeIndex += 6;
            modeParamMap.put(mode,tmpModeParams);
        });
        initializeDoubleArray();

    }
    public double[] initializeDoubleArray(){
        this.array = new double[iEndModeIndex];arrayIndex = new ArrayList<>();
        //    marginalUtilityOfWaiting_s[0];marginalUtilityOfLateArrival_s[1];marginalUtilityOfEarlyDeparture_s[2];
//    marginalUtilityOfWaitingPt_s[3];marginalUtilityOfPerforming_s[4];utilityOfLineSwitch[5];marginalUtilityOfMoney[6];
        this.array[0] = scoringParameterSet.getEarlyDeparture_utils_hr();arrayIndex.add("earlyDeparture_utils_hr");
        this.array[1] = scoringParameterSet.getLateArrival_utils_hr();arrayIndex.add("lateArrival_utils_hr");
        this.array[2] = scoringParameterSet.getPerforming_utils_hr();arrayIndex.add("performing_utils_hr");
        this.array[3] = scoringParameterSet.getMarginalUtilityOfMoney();arrayIndex.add("marginalUtilityOfMoney");
        this.array[4] = scoringParameterSet.getMarginalUtlOfWaitingPt_utils_hr();arrayIndex.add("arginalUtlOfWaitingPt_utils_hr");
        this.array[5] = scoringParameterSet.getUtilityOfLineSwitch();arrayIndex.add("utilityOfLineSwitch");
        this.array[6] = scoringParameterSet.getMarginalUtlOfWaiting_utils_hr();arrayIndex.add("marginalUtlOfWaiting_utils_hr");
        int i[]= {7};
        actParamMap.forEach((actType,actSpecificValueMap)->
            actSpecificValueMap.forEach((actParam,value)->{this.array[i[0]] = value;i[0]++;arrayIndex.add(actType+"|"+actParam);})
        );
        modeParamMap.forEach((modeType,modeSpecificValueMap)->
                modeSpecificValueMap.forEach((modeParam,value)->{this.array[i[0]] = value;i[0]++;arrayIndex.add(modeType+"|"+modeParam);})
        );
        return this.array;
    }
    public double[][] getDefaultParameterBounds(){
        double bounds[][] = new double[iEndModeIndex][];
        for (int i = 0; i < 7; i++) {
            bounds[i] = new double[2];
            if (i==2||i==3){ // act performance
                bounds[i][0] = 0.0;bounds[i][1] = 1000.0;
            }else {
                bounds[i][0] = -1000.0;bounds[i][1] = -0.0;
            }
        }
        for (int i = 7; i < iEndActIndex; i++) {
            bounds[i] = new double[2];
            bounds[i][0] = 0.0;bounds[i][1] = 3600.0*24.0;
        }
        for (int i = iEndActIndex; i < iEndModeIndex; i++) {
            bounds[i] = new double[2];
            if (i%6 == iEndActIndex%6){
                // mode asc
                bounds[i][0] = -1000.0;bounds[i][1] = 1000.0;
            }else {
                bounds[i][0] = -1000.0;bounds[i][1] = -0.0;
            }
        }
        return bounds;
    }
    public double[] getDefaultParameterLowerBounds(){
        double bounds[] = new double[iEndModeIndex];
        for (int i = 0; i < 7; i++) {
            if (i==2||i==3){ // act performing
                bounds[i] = 0.0001;
            }else {
                bounds[i] = -1000.0;
            }
        }
        for (int i = 7; i < iEndActIndex; i++) {
            bounds[i] = Math.min(Math.max(0.00001,array[i]-3600.0),3600.0*24.0);
        }
        for (int i = iEndActIndex; i < iEndModeIndex; i++) {
            if(i%6 == iEndActIndex%6){
                // mode asc
                bounds[i]= -1000.0;
            }else {
                if (arrayIndex.get(i).contains("dailyMonetaryConstant")) {bounds[i] = -1000.0; }
                if (arrayIndex.get(i).contains("monetaryDistanceRate")) {bounds[i] = -1000.0; }
                if (arrayIndex.get(i).contains("dailyUtilityConstant")) {bounds[i] = -1000.0; }
                if (arrayIndex.get(i).contains("marginalUtilityOfDistance")) {bounds[i] = -1000.0; }
                if (arrayIndex.get(i).contains("marginalUtilityOfTraveling")) {bounds[i] = -1000.0; }
            }
        }
        return bounds;
    }
    public double[] getDefaultParameterUpperBounds(){
        double bounds[] = new double[iEndModeIndex];
        for (int i = 0; i < 7; i++) {
            if (i==2||i==3){ // act performing
                bounds[i] = 1000.0;
            }else {
                bounds[i] = -0.0;
            }
        }
        for (int i = 7; i < iEndActIndex; i++) {
            bounds[i]= Math.min(Math.max(0.,array[i]+3600.0),3600.0*24.0);// ideally this should be passed as a paramter from config file, the mutationRange params
        }
        for (int i = iEndActIndex; i < iEndModeIndex; i++) {
            if (i%6 == iEndActIndex%6){
                // mode asc
                bounds[i] = 1000.0;
            }else {
                if (arrayIndex.get(i).contains("dailyMonetaryConstant")) {bounds[i] = 1000.0; }
                if (arrayIndex.get(i).contains("monetaryDistanceRate")) {bounds[i] = -0.0; }
                if (arrayIndex.get(i).contains("dailyUtilityConstant")) {bounds[i] = 1000.0; }
                if (arrayIndex.get(i).contains("marginalUtilityOfDistance")) {bounds[i] = -0.0; }
                if (arrayIndex.get(i).contains("marginalUtilityOfTraveling")) {bounds[i] = -0.0; }
            }
        }
        return bounds;
    }
    public void clearDoubleArray(){
        for (int i = 0; i < array.length; i++) {
            array[0] = 0.0;
        }
    }
    public void updateParamsPerDoubleArray(double array[]){
        this.array = array;
        // update fixed parameters
        scoringParameterSet.setEarlyDeparture_utils_hr(array[0]);
        scoringParameterSet.setLateArrival_utils_hr(array[1]);
        scoringParameterSet.setPerforming_utils_hr(array[2]);
        scoringParameterSet.setMarginalUtilityOfMoney(array[3]);
        scoringParameterSet.setMarginalUtlOfWaitingPt_utils_hr(array[4]);
        scoringParameterSet.setUtilityOfLineSwitch(array[5]);
        scoringParameterSet.setMarginalUtlOfWaiting_utils_hr(array[6]);
        // update act related parameters
        int iTmp[] = {7};
        actParamMap.forEach((actType,actParams)->{
            actParams.forEach((param,value)->{
                if (param.equals("openingTime")) {scoringParameterSet.getActivityParams(actType).setOpeningTime(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("closingTime")) {scoringParameterSet.getActivityParams(actType).setClosingTime(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("earliestEndTime")) {scoringParameterSet.getActivityParams(actType).setEarliestEndTime(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("latestStartTime")) {scoringParameterSet.getActivityParams(actType).setLatestStartTime(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("minimalDuration")) {scoringParameterSet.getActivityParams(actType).setMinimalDuration(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("typicalDuration")) {scoringParameterSet.getActivityParams(actType).setTypicalDuration(array[iTmp[0]]);iTmp[0]++;}
            });
        });
        // update mode related parameters
        modeParamMap.forEach((mode,modeParams)->{
            modeParams.forEach((param,value)->{
                if (param.equals("asc")) {scoringParameterSet.getModes().get(mode).setConstant(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("dailyMonetaryConstant")) {scoringParameterSet.getModes().get(mode).setDailyMonetaryConstant(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("monetaryDistanceRate")) {scoringParameterSet.getModes().get(mode).setMarginalUtilityOfDistance(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("dailyUtilityConstant")) {scoringParameterSet.getModes().get(mode).setDailyUtilityConstant(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("marginalUtilityOfDistance")) {scoringParameterSet.getModes().get(mode).setMarginalUtilityOfDistance(array[iTmp[0]]);iTmp[0]++;}
                if (param.equals("marginalUtilityOfTraveling")) {scoringParameterSet.getModes().get(mode).setMarginalUtilityOfTraveling(array[iTmp[0]]);iTmp[0]++;}
            });
        });

    }


}
