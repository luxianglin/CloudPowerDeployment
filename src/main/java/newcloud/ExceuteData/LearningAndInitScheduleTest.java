package newcloud.ExceuteData;

import newcloud.*;
import newcloud.datacenter.PowerDatacenterLearningAndInit;
import newcloud.policy.VmAllocationAssignerLearningAndInit;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static newcloud.Constants.*;
import static newcloud.policy.VmAllocationAssignerLearningAndInit.QList;

public class LearningAndInitScheduleTest {

    /**
     * The cloudlet list.
     */
    private static List<Cloudlet> cloudletList;

    /**
     * The vmlist.
     */
    public static List<Vm> vmList;

    private static List<PowerHost> hostList;
    private static DatacenterBroker broker;
    public static int brokerId;
    private static VmAllocationAssignerLearningAndInit vmAllocationAssignerLearningAndInit;
    private static double smallestdata = Double.MAX_VALUE;

    public List<Double> execute() throws Exception {
        double LEARNING_GAMMA = 0.9; // 强化学习算法的γ值
        double LEARNING_ALPHA = 0.8; // 强化学习算法的α值
        double LEARNING_EPSILON = 0.5; // 强化学习算法的ε值

        for (int i = 0; i < Iteration; i++) {
            LEARNING_EPSILON = 1 / (i * 0.6 + 1);
            vmAllocationAssignerLearningAndInit = new VmAllocationAssignerLearningAndInit(LEARNING_GAMMA, LEARNING_ALPHA, LEARNING_EPSILON, GenExcel.getInstance());
            CloudSim.init(1, Calendar.getInstance(), false);

            broker = createBroker();
            brokerId = broker.getId();
            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(Constants.NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new NewPowerAllocatePolicy(hostList);
            PowerDatacenterLearningAndInit datacenter = createDatacenter(
                    "Datacenter",
                    PowerDatacenterLearningAndInit.class,
                    hostList,
                    vmAllocationPolicy);

            datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            CloudSim.terminateSimulation(terminateTime);

            double lastClock = CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
//            printCloudletList(newList);
            CloudSim.stopSimulation();
            System.out.println(i + "----------------------------------");
        }
        for (String s : QList.keySet()) {
            System.out.println(s + ":" + QList.get(s));
        }
        System.out.println(QList.size());
        System.out.println(vmList.size());
//        GenExcel.getInstance().genExcel();
        for (int i = 0; i < PowerDatacenterLearningAndInit.allpower.size(); i++) {
            System.out.println(PowerDatacenterLearningAndInit.allpower.get(i));
            if (PowerDatacenterLearningAndInit.allpower.get(i) < smallestdata) {
                smallestdata = PowerDatacenterLearningAndInit.allpower.get(i);
            }
        }
        System.out.println("最小值：" + smallestdata);
        return PowerDatacenterLearningAndInit.allpower;
    }

    public List<Integer> getNumByType() {
        List<Integer> resultNum = new ArrayList<>();
        List<PowerHost> highCPUHost = new ArrayList<>();
        List<PowerHost> lowCPUHost = new ArrayList<>();
        List<PowerHost> zeroCPUHost = new ArrayList<>();
        int[] highCPUHostType = new int[]{0, 0, 0};
        int[] lowCPUHostType = new int[]{0, 0, 0};
        int[] zeroCPUHostType = new int[]{0, 0, 0};
        for (PowerHost host : hostList) {
            double ss = host.getAvailableMips();
            double t = host.getTotalMips();
            double t2 = host.getUtilizationMips();
            double usedCpu = 1 - (double) Math.round((host.getAvailableMips() / host.getTotalMips()) * 100) / 100;
            if (usedCpu >= 0.8) {
                highCPUHost.add(host);
            } else if (usedCpu < 0.8 && usedCpu > 0.2) {
                lowCPUHost.add(host);
            } else {
                zeroCPUHost.add(host);
            }
        }
        for (PowerHost host : highCPUHost) {
            for (int i = 0; i < HOST_MIPS.length; i++) {
                if (host.getTotalMips() == HOST_MIPS[i]) {
                    highCPUHostType[i] += 1;
                }
            }
        }
        for (PowerHost host : lowCPUHost) {
            for (int i = 0; i < HOST_MIPS.length; i++) {
                if (host.getTotalMips() == HOST_MIPS[i]) {
                    lowCPUHostType[i] += 1;
                }
            }
        }
        for (PowerHost host : zeroCPUHost) {
            for (int i = 0; i < HOST_MIPS.length; i++) {
                if (host.getTotalMips() == HOST_MIPS[i]) {
                    zeroCPUHostType[i] += 1;
                }
            }
        }
        resultNum.add(highCPUHost.size());
        resultNum.add(highCPUHostType[0]);
        resultNum.add(highCPUHostType[1]);
        resultNum.add(highCPUHostType[2]);
        resultNum.add(lowCPUHost.size());
        resultNum.add(lowCPUHostType[0]);
        resultNum.add(lowCPUHostType[1]);
        resultNum.add(lowCPUHostType[2]);
        resultNum.add(zeroCPUHost.size());
        resultNum.add(zeroCPUHostType[0]);
        resultNum.add(zeroCPUHostType[1]);
        resultNum.add(zeroCPUHostType[2]);
        return resultNum;
    }

    public double changeEpsilon(double LEARNING_EPSILON, int i) {
        DecimalFormat df = new DecimalFormat("0.00");
        double temp = 1 - Double.valueOf(df.format((float) i / (float) Iteration));
        if (temp > 0.9 && temp <= 1) {
            LEARNING_EPSILON = 1.0;
        } else if (temp > 0.8 && temp <= 0.9) {
            LEARNING_EPSILON = 0.0;
        } else if (temp > 0.7 && temp <= 0.8) {
            LEARNING_EPSILON = 0.4;
        } else if (temp > 0.6 && temp <= 0.7) {
            LEARNING_EPSILON = 0.0;
        } else if (temp > 0.5 && temp <= 0.6) {
            LEARNING_EPSILON = 0.3;
        } else if (temp > 0.4 && temp <= 0.5) {
            LEARNING_EPSILON = 0.0;
        } else if (temp > 0.3 && temp <= 0.4) {
            LEARNING_EPSILON = 0.2;
        } else if (temp > 0.2 && temp <= 0.3) {
            LEARNING_EPSILON = 0.0;
        } else if (temp > 0.1 && temp <= 0.2) {
            LEARNING_EPSILON = 0.0;
        } else if (temp >= 0 && temp <= 0.1) {
            LEARNING_EPSILON = 0.0;
        }
        return LEARNING_EPSILON;
    }

    public PowerDatacenterLearningAndInit createDatacenter(
            String name,
            Class<? extends Datacenter> datacenterClass,
            List<PowerHost> hostList,
            VmAllocationPolicy vmAllocationPolicy) throws Exception {
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch,
                os,
                vmm,
                hostList,
                time_zone,
                cost,
                costPerMem,
                costPerStorage,
                costPerBw);
        PowerDatacenterLearningAndInit datacenter = new PowerDatacenterLearningAndInit("test", characteristics, vmAllocationPolicy, new LinkedList<Storage>(), 300, vmAllocationAssignerLearningAndInit);
        return datacenter;
    }

    public DatacenterBroker createBroker() {
        NewPowerDatacenterBroker broker = null;

        try {
            broker = new NewPowerDatacenterBroker("Broker");
        } catch (Exception var2) {
            var2.printStackTrace();
            System.exit(0);
        }

        return broker;
    }

}


