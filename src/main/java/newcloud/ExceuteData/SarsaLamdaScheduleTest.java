package newcloud.ExceuteData;


import newcloud.*;
import newcloud.datacenter.PowerDatacenterSarsa;
import newcloud.datacenter.PowerDatacenterSarsa_lamda;
import newcloud.policy.VmAllocationAssignerSarsa;
import newcloud.policy.VmAllocationAssignerSarsa_lamda;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static newcloud.Constants.*;
import static newcloud.policy.VmAllocationAssignerSarsa_lamda.EList;
import static newcloud.policy.VmAllocationAssignerSarsa_lamda.QList;

public class SarsaLamdaScheduleTest {

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
    private static VmAllocationAssignerSarsa_lamda vmAllocationAssignerSarsa_lamda;
    private static double smallestdata = Double.MAX_VALUE;
    double LEARNING_GAMMA = 0.9; // 强化学习算法的γ值
    double LEARNING_ALPHA = 0.8; // 强化学习算法的α值
    double LEARNING_EPSILON = 0.2; // 强化学习算法的ε值
    double LAMDA = 1;

    public double getLEARNING_GAMMA() {
        return LEARNING_GAMMA;
    }

    public void setLEARNING_GAMMA(double LEARNING_GAMMA) {
        this.LEARNING_GAMMA = LEARNING_GAMMA;
    }

    public double getLEARNING_ALPHA() {
        return LEARNING_ALPHA;
    }

    public void setLEARNING_ALPHA(double LEARNING_ALPHA) {
        this.LEARNING_ALPHA = LEARNING_ALPHA;
    }

    public double getLEARNING_EPSILON() {
        return LEARNING_EPSILON;
    }

    public void setLEARNING_EPSILON(double LEARNING_EPSILON) {
        this.LEARNING_EPSILON = LEARNING_EPSILON;
    }

    public List<Double> execute() throws Exception {


        for (int i = 0; i < Iteration; i++) {
            LEARNING_EPSILON = changeEpsilon(LEARNING_EPSILON, i);
            vmAllocationAssignerSarsa_lamda = new VmAllocationAssignerSarsa_lamda(LEARNING_GAMMA, LEARNING_ALPHA, LEARNING_EPSILON, LAMDA, GenExcel.getInstance());
//            CloudSim.initialize();
            CloudSim.init(1, Calendar.getInstance(), false);

            broker = createBroker();
            brokerId = broker.getId();
            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(Constants.NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new NewPowerAllocatePolicy(hostList);
            PowerDatacenterSarsa_lamda datacenter = createDatacenter(
                    "Datacenter",
                    PowerDatacenterSarsa_lamda.class,
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
        for (String e : EList.keySet()) {
            System.out.println(e + ":" + EList.get(e));
        }
//        GenExcel.getInstance().genExcel();
        for (int i = 0; i < PowerDatacenterSarsa_lamda.allpower.size(); i++) {
            System.out.println(PowerDatacenterSarsa_lamda.allpower.get(i));
            if (PowerDatacenterSarsa_lamda.allpower.get(i) < smallestdata) {
                smallestdata = PowerDatacenterSarsa_lamda.allpower.get(i);
            }
        }
        System.out.println("最小值：" + smallestdata);
        return PowerDatacenterSarsa_lamda.allpower;
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

    public PowerDatacenterSarsa_lamda createDatacenter(
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
        PowerDatacenterSarsa_lamda datacenter = new PowerDatacenterSarsa_lamda("test", characteristics, vmAllocationPolicy, new LinkedList<Storage>(), 300, vmAllocationAssignerSarsa_lamda);
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


