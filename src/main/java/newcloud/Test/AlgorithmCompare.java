package newcloud.Test;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import FourDrawPlot.*;
import newcloud.ExceuteData.*;

import java.util.ArrayList;
import java.util.List;

import static newcloud.Constants.Iteration;


/**
 * Created by root on 8/25/17.
 */
public class AlgorithmCompare {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y1 = null; // 存放y值的数组
        MWNumericArray y2 = null; // 存放y值的数组
        MWNumericArray y3 = null; // 存放y值的数组
        MWNumericArray y4 = null; // 存放y值的数组

        Plotter thePlot = null; // plotter类的实例（在MatLab编译时，新建的类）
        int n = Iteration; // 作图点数
        int num = 1;
        try {
            // 分配x、y的值
            int[] dims = {1, n};
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y1 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y2 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y3 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y4 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);

            LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
            List<Double> learningPowerList = learningScheduleTest.execute();
            for (int i = 1; i <= learningPowerList.size(); i++) {
                x.set(i, i);
                y1.set(i, learningPowerList.get(i - 1));
            }

            LearningLamdaScheduleTest learningLamdaScheduleTest = new LearningLamdaScheduleTest();
            List<Double> lamdaPowerList = learningLamdaScheduleTest.execute();
            for (int i = 1; i <= lamdaPowerList.size(); i++) {
                x.set(i, i);
                y2.set(i, lamdaPowerList.get(i - 1));
            }

            GreedyScheduleTest greedyScheduleTest = new GreedyScheduleTest();
            List<Double> greedyPowerList = greedyScheduleTest.execute();
            for (int i = 1; i <= greedyPowerList.size(); i++) {
                x.set(i, i);
                y3.set(i, greedyPowerList.get(i - 1));
            }

            LearningAndInitScheduleTest fairScheduleTest = new LearningAndInitScheduleTest();
            List<Double> fairPowerList = fairScheduleTest.execute();
            for (int i = 1; i <= fairPowerList.size(); i++) {
                x.set(i, i);
                y4.set(i, fairPowerList.get(i - 1));
            }

            System.out.println(getAverageResult(learningPowerList, learningPowerList.size() / 10));
            System.out.println(getAverageResult(lamdaPowerList, lamdaPowerList.size() / 10));
            System.out.println(getAverageResult(greedyPowerList, greedyPowerList.size() / 10));
            System.out.println(getAverageResult(fairPowerList, fairPowerList.size() / 10));

//            RandomScheduleTest randomScheduleTest = new RandomScheduleTest();
//            List<Double> randomPowerList = randomScheduleTest.execute();
//            for (int i = 1; i <= randomPowerList.size(); i++) {
//                x.set(i, i);
//                y4.set(i, randomPowerList.get(i - 1));
//            }

//            LearningAndInitScheduleTest learningAndInitScheduleTest = new LearningAndInitScheduleTest();
//            List<Double> learningAndInitPowerList = learningAndInitScheduleTest.execute();
//            for (int i = 1; i <= learningAndInitPowerList.size(); i++) {
//                x.set(i, i);
//                y4.set(i, learningAndInitPowerList.get(i - 1));
//            }
            // 初始化plotter的对象
            thePlot = new Plotter();

            // 作图
            thePlot.drawplot(x, y1, "Q-Learning", y2, "Q-Learning(Lamda)", y3, "Greedy", y4, "PSO", "迭代次数", "能耗", "各类算法随迭代次数的能耗变化");
            thePlot.waitForFigures();
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        } finally {
            // 释放本地资源
            MWArray.disposeArray(x);
            MWArray.disposeArray(y1);
            MWArray.disposeArray(y2);
            MWArray.disposeArray(y3);
            MWArray.disposeArray(y4);
            if (thePlot != null)
                thePlot.dispose();
        }

    }

    public static List<Double> getAverageResult(List<Double> datas, int step) {
        List<Double> temp = new ArrayList<>();
        int num = datas.size() / step;
        for (int i = 0; i < num; i++) {
            double total = 0;
            for (int j = 0; j < step; j++) {
                total += datas.get(0);
                datas.remove(0);
            }
            temp.add(total / step);
        }
        return temp;
    }
}
