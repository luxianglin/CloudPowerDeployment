package newcloud.policy;


import newcloud.GenExcel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static newcloud.Constants.NUMBER_OF_HOSTS;


public class VmAllocationAssignerLearningLamda { //强化学习分配策略
    private GenExcel genExcel = null;
    private double gamma;   //强化学习算法的γ值
    private double alpha;   //强化学习算法的α值
    private double epsilon; //强化学习算法的ε值
    private double lamda;//
    public static Map<String, Map<Integer, Double>> QList = new HashMap<String, Map<Integer, Double>>(); //Q值表
    public static Map<String, Map<Integer, Double>> EList = new HashMap<String, Map<Integer, Double>>(); //E值表

    public VmAllocationAssignerLearningLamda(double gamma, double alpha, double epsilon, double lamda, GenExcel genExcel) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.lamda = lamda;
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    public void initRowOfQList(String state_idx) { //初始化Q值表的行
        QList.put(state_idx, new HashMap<Integer, Double>());
        EList.put(state_idx, new HashMap<Integer, Double>());
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            QList.get(state_idx).put(i, 0.0); //赋初值为0
            EList.get(state_idx).put(i, 0.0);
        }
    }

    public int randomInt(int min, int max) { // random[min,max] 可取min,可取max
        if (min == max) {
            return min;
        }
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }

    public String createLastState_idx(String lastcpulist) {//生成更新前的状态行行号
        return lastcpulist;
    }

    public String createState_idx(String cpulist) { //生成当前状态行行号
        return cpulist;
    }

    public int createAction(String cpulist) { //生成选择虚拟机的动作，即获得想要的虚拟机号
        int current_action;        //生成的动作，即要选择的虚拟机
        int x = randomInt(0, 100); //生成随机数[0,100]
        String state_idx = createState_idx(cpulist); //根据各虚拟机等待队列当前状态状态生成的Q值表行号
        if (!QList.containsKey(state_idx)) { //若Q值表中不存在这一行，则初始化这一行
            initRowOfQList(state_idx);
        }


        //根据随机数x选择生成动作的方式
        if (((double) x / 100) < (1 - epsilon)) { //生成动作方式1：利用(exploit)
            int umax = 0;
            double tmp = -1; //中间值，用于寻找最大值，设置为负数肯定小于正数
            for (int i = 0; i < NUMBER_OF_HOSTS; i++) { //选择当前状态行中Q值最大的列号，即要选择的物理机号
                if (tmp < QList.get(state_idx).get(i)) {
                    tmp = QList.get(state_idx).get(i);
                    umax = i;
                }
            }
            if (tmp == -1) { //利用动作没有正常进行
                System.out.println("exploit没有正常进行。。！");
                System.exit(0);
            }
            current_action = umax;
        } else { //生成动作方式2：学习(explore)
            current_action = randomInt(0, NUMBER_OF_HOSTS - 1); //随机生成动作
        }
        return current_action;
    }

    public void updateQList(int action_idx, double reward, String lastcpulist, String cpulist) { //更新Q值表
        double finalreward = reward;
        String state_idx = createLastState_idx(lastcpulist); //没有将当前虚拟机分配到物理机队列时的状态行号
        String next_state_idx = createState_idx(cpulist);            //将当前虚拟机分配到物理机队列后的状态行号

        if (!QList.containsKey(next_state_idx)) { //若更新后的行不存在于Q值表中，则初始化它
            initRowOfQList(next_state_idx);
        }
        double QMaxNextState = -1.0;
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) { //获取更新后的状态行的最大值
            if (QMaxNextState < QList.get(next_state_idx).get(i)) {
                QMaxNextState = QList.get(next_state_idx).get(i);
            }
        }
        double delta = finalreward + gamma * QMaxNextState - QList.get(state_idx).get(action_idx);
        EList.get(state_idx).put(action_idx, EList.get(state_idx).get(action_idx) + 1);
        for (String key : EList.keySet()) {
            for (int j = 0; j < NUMBER_OF_HOSTS; j++) {
                QList.get(key).put(j, QList.get(key).get(j) + alpha * delta * EList.get(key).get(j));
                EList.get(key).put(j, EList.get(key).get(j) * gamma * lamda);
            }
        }

        //        double QValue = QList.get(state_idx).get(action_idx) //Q值表Q值更新的主要公式
//                + alpha * (finalreward + gamma * QMaxNextState - QList.get(state_idx).get(action_idx));


//        this.genExcel.fillData(QList, state_idx, action_idx, QValue);
    }


}
