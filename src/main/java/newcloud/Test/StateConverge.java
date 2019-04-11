package newcloud.Test;


import TwoDrawPlot.TwoPlotter;
import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import newcloud.ExceuteData.LearningAndNoConvergeScheduleTest;
import newcloud.ExceuteData.LearningScheduleTest;

import java.util.List;

import static newcloud.Constants.Iteration;

public class StateConverge {
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y1 = null; // 存放y值的数组
        MWNumericArray y2 = null; // 存放y值的数组


        TwoPlotter thePlot = null; // plotter类的实例（在MatLab编译时，新建的类）
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

            LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
            List<Double> learningPowerList = learningScheduleTest.execute();
            for (int i = 1; i <= learningPowerList.size(); i++) {
                x.set(i, i);
                y1.set(i, learningPowerList.get(i - 1));
            }

            LearningAndNoConvergeScheduleTest learningAndNoConvergeScheduleTest = new LearningAndNoConvergeScheduleTest();
            List<Double> lamdaPowerList = learningAndNoConvergeScheduleTest.execute();
            for (int i = 1; i <= lamdaPowerList.size(); i++) {
                x.set(i, i);
                y2.set(i, lamdaPowerList.get(i - 1));
            }

            // 初始化plotter的对象
            thePlot = new TwoPlotter();

            // 作图
            thePlot.drawplot(x, y1, "状态聚合", x, y2, "非状态聚合","迭代次数","能耗","状态聚合对收敛结果的影响");
            thePlot.waitForFigures();
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        } finally {
            // 释放本地资源
            MWArray.disposeArray(x);
            MWArray.disposeArray(y1);
            MWArray.disposeArray(y2);
            if (thePlot != null)
                thePlot.dispose();
        }
    }
}
