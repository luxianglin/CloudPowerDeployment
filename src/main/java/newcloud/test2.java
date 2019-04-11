package newcloud;

import newcloud.ExceuteData.LearningScheduleTest;

import java.util.ArrayList;
import java.util.List;

import static newcloud.Constants.Iteration;

public class test2 {
    public static void main(String[] args) throws Exception {
        double total = 0;
        LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
//        learningScheduleTest.setLEARNING_GAMMA(0);
//        learningScheduleTest.setLEARNING_EPSILON(0);
        learningScheduleTest.setLEARNING_GAMMA(0);
        List<Double> learningPowerList = learningScheduleTest.execute();
        for (int i = 0; i < learningPowerList.size(); i++) {
            total += learningPowerList.get(i);
        }
        System.out.println(total / Iteration);

    }
}
