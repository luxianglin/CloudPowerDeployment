package newcloud.NoTimeReliability;

import newcloud.GenExcel;
import newcloud.newHelper;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


import static newcloud.Constants.*;
import static newcloud.NoTimeReliability.VmAllocationAssigner.QList;


public class QScheduleTest {

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
    private static VmAllocationAssigner vmAllocationAssigner;
    private double LEARNING_GAMMA = 0.9; // 强化学习算法的γ值
    private double LEARNING_ALPHA = 0.8; // 强化学习算法的α值
    private double LEARNING_EPSILON = 0.5; // 强化学习算法的ε值
    private static double smallestdata = Double.MAX_VALUE;
    public List<Double> execute() throws Exception {

        vmAllocationAssigner = new VmAllocationAssigner(LEARNING_GAMMA, LEARNING_ALPHA, LEARNING_EPSILON, GenExcel.getInstance());
        for (int i = 0; i < Iteration; i++) {
            LEARNING_EPSILON = 1 / (i + 1);
            CloudSim.init(1, Calendar.getInstance(), false);
            broker = createBroker();
            int brokerId = broker.getId();
            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new QPowerAllocatePolicy(hostList);
            QPowerDatacenter datacenter = createDatacenter(
                    "Datacenter",
                    QPowerDatacenter.class,
                    hostList,
                    vmAllocationPolicy);

            datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            CloudSim.terminateSimulation(terminateTime);

            double lastClock = CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");
            CloudSim.stopSimulation();

        }
        for (String s : QList.keySet()) {
            System.out.println(s + ":" + QList.get(s));
        }
        System.out.println(QList.size());
        System.out.println(vmList.size());
//        GenExcel.getInstance().genExcel();
        for (int i = 0; i < QPowerDatacenter.allpower.size(); i++) {
            System.out.println(QPowerDatacenter.allpower.get(i));
            if (QPowerDatacenter.allpower.get(i) < smallestdata) {
                smallestdata = QPowerDatacenter.allpower.get(i);
            }
        }
        System.out.println("最小值：" + smallestdata);
        return QPowerDatacenter.allpower;
    }

    public static QPowerDatacenter createDatacenter(
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
        QPowerDatacenter datacenter = new QPowerDatacenter("test", characteristics, vmAllocationPolicy, new LinkedList<Storage>(), 300, vmAllocationAssigner);
        return datacenter;
    }

    public static DatacenterBroker createBroker() {
        QPowerDatacenterBroker broker = null;

        try {
            broker = new QPowerDatacenterBroker("Broker");
        } catch (Exception var2) {
            var2.printStackTrace();
            System.exit(0);
        }

        return broker;
    }

}


