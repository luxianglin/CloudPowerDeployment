package newcloud.Test;

import FourDrawPlot.Plotter;
import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import newcloud.ExceuteData.GreedyScheduleTest;
import newcloud.ExceuteData.LearningAndInitScheduleTest;
import newcloud.ExceuteData.LearningLamdaScheduleTest;
import newcloud.ExceuteData.LearningScheduleTest;

import java.util.List;

import static newcloud.Constants.Iteration;

public class CPUUtilizationCompare {
    public static void main(String[] args) throws Exception {

        LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
        List<Double> learningPowerList = learningScheduleTest.execute();
        List<Integer> result1 = learningScheduleTest.getNumByType();

        LearningLamdaScheduleTest learningLamdaScheduleTest = new LearningLamdaScheduleTest();
        List<Double> lamdaPowerList = learningLamdaScheduleTest.execute();
        List<Integer> result2 = learningLamdaScheduleTest.getNumByType();

        GreedyScheduleTest greedyScheduleTest = new GreedyScheduleTest();
        List<Double> greedyPowerList = greedyScheduleTest.execute();
        List<Integer> result3 = greedyScheduleTest.getNumByType();



        System.out.println(result1);
        System.out.println(result2);
        System.out.println(result3);
    }
}
