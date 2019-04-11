package newcloud.Test;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import FourDrawPlot.Plotter;
import newcloud.ExceuteData.GreedyScheduleTest;
import newcloud.ExceuteData.LearningAndInitScheduleTest;
import newcloud.ExceuteData.LearningLamdaScheduleTest;
import newcloud.ExceuteData.LearningScheduleTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static newcloud.Constants.Iteration;
import static newcloud.Constants.inputFolder;

public class TaskCompare {
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y1 = null; // 存放y值的数组
        MWNumericArray y2 = null; // 存放y值的数组
        MWNumericArray y3 = null; // 存放y值的数组
        MWNumericArray y4 = null; // 存放y值的数组

        Plotter thePlot = null; // plotter类的实例（在MatLab编译时，新建的类）
        String ss = "G:\\IdeaProjects\\PowerDeployment\\src\\main\\resources\\datas\\";
        String[] folders = new String[]{"50", "100", "150", "200", "250", "300"};
        List<Double> a1 = new ArrayList<>();
        List<Double> a2 = new ArrayList<>();
        List<Double> a3 = new ArrayList<>();
        List<Double> a4 = new ArrayList<>();
        int n = folders.length; // 作图点数
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

            for (int i = 1; i <= folders.length; i++) {
                String file = folders[i - 1];
                inputFolder = ss + file;
                LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
                List<Double> learningPowerList = learningScheduleTest.execute();

                LearningLamdaScheduleTest learningLamdaScheduleTest = new LearningLamdaScheduleTest();
                List<Double> lamdaPowerList = learningLamdaScheduleTest.execute();

                GreedyScheduleTest greedyScheduleTest = new GreedyScheduleTest();
                List<Double> greedyPowerList = greedyScheduleTest.execute();


                LearningAndInitScheduleTest psoScheduleTest = new LearningAndInitScheduleTest();
                List<Double> psoPowerList = psoScheduleTest.execute();


                x.set(i, folders[i - 1]);
                y1.set(i, getLast(learningPowerList));
                y2.set(i, getLast(lamdaPowerList));
                y3.set(i, getLast(greedyPowerList));
                y4.set(i, getLast(psoPowerList));


                a1.add(getLast(learningPowerList));
                a2.add(getLast(lamdaPowerList));
                a3.add(getLast(greedyPowerList));
                a4.add(getLast(psoPowerList));
            }
            System.out.println(a1);
            System.out.println(a2);
            System.out.println(a3);
            System.out.println(a4);

            // 初始化plotter的对象
            thePlot = new Plotter();

            // 作图
            thePlot.drawplot(x, y1, "Q-Learning", y2, "Q-Learning(Lamda)", y3, "Greedy", y4, "PSO", "虚拟机数量", "能耗", "各类算法随虚拟机数量的能耗变化");
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

    public static double getMin(List<Double> datas) {
        return Collections.min(datas);
    }

    public static double getLast(List<Double> datas) {
        return datas.get(datas.size() - 1);
    }
}
