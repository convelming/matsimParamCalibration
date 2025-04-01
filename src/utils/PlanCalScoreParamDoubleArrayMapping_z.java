package utils;

import org.apache.commons.lang3.RandomUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.utils.misc.OptionalTime;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * Author：Zengren
 */
public class PlanCalScoreParamDoubleArrayMapping_z {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Config config = ConfigUtils.loadConfig("./testInput/config.xml");
        config.planCalcScore().setLocked(); // 模拟 matsim.run lock

        ConfigUtils.writeConfig(config, "./output/org-config.xml");
        unlock(config.planCalcScore());
        PlanCalcScoreConfigGroup.ScoringParameterSet scoringParameters = config.planCalcScore().getScoringParameters(null);
        ParamArray params = new ParamArray(scoringParameters);
        System.out.println(params.all.toString());
        double[] random = new double[params.used.length];
        for (int i = 0; i < random.length; i++) {
            random[i] = RandomUtils.nextDouble(1, 10);
        }
        // 模拟修改后再塞回 config
        params.used = random;
        params.updateConfig(scoringParameters);

        ConfigUtils.writeConfig(config, "./output/random-config.xml");
        System.out.println("done .");

    }

    public static class ParamArray {

        public List<String> all = new ArrayList<>();
        public List<String> keys = new ArrayList<>();
        public Map<Integer, Integer> allToUsed = new HashMap<>();
        public double[] used;

        public double[] updatedUsed;

        public Pattern isNumber = Pattern.compile("-?[0-9]\\d*.\\d*|0\\.\\d*[0-9]\\d*");
        public Pattern isTime = Pattern.compile("\\d{1,2}(-|/|.)\\d{1,2}\\1\\d{1,2}");

        public ParamArray(PlanCalcScoreConfigGroup.ScoringParameterSet config) {
            config.getParams().forEach((k, v) -> {
                keys.add(k);
                all.add(v);
            });
            config.getActivityParams().forEach(activity -> {
                if (activity.getActivityType().endsWith("interaction")) {
                    return;
                }
                keys.addAll(activity.getParams().keySet());
                all.addAll(activity.getParams().values());
            });
            config.getModes().forEach((k, v) -> {
                keys.addAll(v.getParams().keySet());
                all.addAll(v.getParams().values());
            });
            allToUsed();
        }

        public void allToUsed() {
            int cur = 0;
            for (int i = 0, len = all.size(); i < len; i++) {
                String val = all.get(i);
                String key = keys.get(i);

                if (key.equals("priority")) { //
                    continue;
                }

                if (isNumber.matcher(val).matches()) {
                    allToUsed.put(i, cur);
                    cur++;
                }
                if (isTime.matcher(val).matches()) {
                    allToUsed.put(i, cur);
                    cur++;
                }
            }
            used = new double[allToUsed.size()];
            allToUsed.forEach((allIndex, usedIndex) -> {
                String val = all.get(allIndex);
                double d = 0.;

                if (isNumber.matcher(val).matches()) {
                    d = Double.parseDouble(val);
                }

                if (isTime.matcher(val).matches()) {
                    String[] tmp = val.split(":");
                    d += (Double.parseDouble(tmp[0]) * 3600);
                    d += (Double.parseDouble(tmp[1]) * 60);
                    d += Double.parseDouble(tmp[2]);
                }

                used[usedIndex] = d;
            });
        }

        public double[] getUsed() {
            return this.used;
        }

        public void updateConfig(PlanCalcScoreConfigGroup.ScoringParameterSet config) {
            int[] allIndex = {0};
            config.getParams().forEach((k, y) -> {
                Integer usedIndex = this.allToUsed.get(allIndex[0]);
                if (usedIndex != null) {
                    Double val = this.used[usedIndex];
                    Class<PlanCalcScoreConfigGroup.ScoringParameterSet> zlass = PlanCalcScoreConfigGroup.ScoringParameterSet.class;
                    try {
                        Field field = zlass.getDeclaredField(this.keys.get(allIndex[0]));
                        field.setAccessible(true);
                        field.set(config, val);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                allIndex[0]++;
            });
            config.getActivityParams().forEach(activity -> {
                activity.getParams().forEach((k, v) -> {
                    Integer usedIndex = this.allToUsed.get(allIndex[0]);
                    if (usedIndex != null) {
                        Double val = this.used[usedIndex];
                        Class<PlanCalcScoreConfigGroup.ActivityParams> zlass = PlanCalcScoreConfigGroup.ActivityParams.class;
                        try {
                            Field field = zlass.getDeclaredField(this.keys.get(allIndex[0]));
                            field.setAccessible(true);
                            if (field.getType() == OptionalTime.class) {
                                field.set(activity, OptionalTime.defined(val));
                            } else {
                                field.set(activity, val);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    allIndex[0]++;
                });
            });
            config.getModes().forEach((k, mode) -> {
                unlock(mode);
                mode.getParams().forEach((a, b) -> {
                    Integer usedIndex = this.allToUsed.get(allIndex[0]);
                    if (usedIndex != null) {
                        Double val = this.used[usedIndex];
                        Class<PlanCalcScoreConfigGroup.ModeParams> zlass = PlanCalcScoreConfigGroup.ModeParams.class;
                        try {
                            // 拿到属性对应 set方法
                            Field field = zlass.getSuperclass().getDeclaredField("stringGetters");
                            field.setAccessible(true);
                            Map<String, Method> map = (Map<String, Method>) field.get(mode);
                            String setMethodName = map.get(keys.get(allIndex[0])).getName().replace("get", "set");
                            Method method = zlass.getMethod(setMethodName, double.class);
                            method.invoke(mode, val);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    allIndex[0]++;
                });
            });
        }



        public String key2SetMethodName(int index) {
            char[] chars = keys.get(index).toCharArray();
            if (chars[0] >= 97 && chars[0] <= 122) {
                chars[0] -= 32;
            }
            return "set" + new String(chars);
        }

    }

    private static void unlock(ConfigGroup config){
        Class<?> zlass = config.getClass();
        while (zlass != null && !zlass.getName().endsWith(".ConfigGroup")) {
            zlass = zlass.getSuperclass();
        }

        if(zlass == null){
//                log.error(config.getClass() + " is not configGroup . ");
            return;
        }

        try {
            Field locked = zlass.getDeclaredField("locked");
            locked.setAccessible(true);
            locked.set(config, false);
//                log.info(config.getClass() + " locked set false success .");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
