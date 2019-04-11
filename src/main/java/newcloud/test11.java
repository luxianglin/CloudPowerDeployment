package newcloud;

import newcloud.ExceuteData.*;
import newcloud.NoTimeReliability.QScheduleTest;

import java.util.List;

import static newcloud.Constants.Iteration;

public class test11 {
    public static void main(String[] args) throws Exception {
        double total = 0;
//        LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
//        learningScheduleTest.setLEARNING_GAMMA(0.6);
//        List<Double> learningPowerList = learningScheduleTest.execute();
//        for (int i = 90; i < learningPowerList.size(); i++) {
//            total += learningPowerList.get(i);
//        }
//        System.out.println(total / 10);

//        FairScheduleTest fairScheduleTest = new FairScheduleTest();
////        List<Double> fairPowerList = fairScheduleTest.execute();
////        for (int i = 0; i < fairPowerList.size(); i++) {
////            total += fairPowerList.get(i);
////        }
////        System.out.println(total / Iteration);

//        GreedyScheduleTest greedyScheduleTest = new GreedyScheduleTest();
//        List<Double> greedyPowerList = greedyScheduleTest.execute();
//        for (int i = 0; i < greedyPowerList.size(); i++) {
//            total += greedyPowerList.get(i);
//        }
//        System.out.println(total / Iteration);


//        RandomScheduleTest randomScheduleTest = new RandomScheduleTest();
//        List<Double> randomPowerList = randomScheduleTest.execute();
//        for (int i = 0; i < randomPowerList.size(); i++) {
//            total += randomPowerList.get(i);
//        }
//        System.out.println(total / Iteration);

        LearningLamdaScheduleTest learningLamdaScheduleTest = new LearningLamdaScheduleTest();
        List<Double> lamdaPowerList = learningLamdaScheduleTest.execute();
        List<Integer> result = learningLamdaScheduleTest.getNumByType();
        System.out.println(result);
//        for (int i = 0; i < lamdaPowerList.size(); i++) {
//            total += lamdaPowerList.get(i);
//        }
//        System.out.println(total / Iteration);

//        SarsaScheduleTest sarsaScheduleTest = new SarsaScheduleTest();
//        List<Double> sarsaPowerList = sarsaScheduleTest.execute();
//        for (int i = 0; i < sarsaPowerList.size(); i++) {
//            total += sarsaPowerList.get(i);
//        }
//        System.out.println(total / Iteration);


//        SarsaLamdaScheduleTest sarsaLamdaScheduleTest = new SarsaLamdaScheduleTest();
//        List<Double> sarsaLamdaPowerList = sarsaLamdaScheduleTest.execute();
//        for (int i = 0; i < sarsaLamdaPowerList.size(); i++) {
//            total += sarsaLamdaPowerList.get(i);
//        }
//        System.out.println(total / Iteration);

//        LearningAndInitScheduleTest sarsaLamdaScheduleTest = new LearningAndInitScheduleTest();
//        List<Double> sarsaLamdaPowerList = sarsaLamdaScheduleTest.execute();
//        for (int i = 0; i < sarsaLamdaPowerList.size(); i++) {
//            total += sarsaLamdaPowerList.get(i);
//        }
//        System.out.println(total / Iteration);
    }
}
