package newcloud.ExceuteData;

import newcloud.*;
import newcloud.datacenter.PowerDatacenterGready;
import newcloud.policy.VmAllocationAssignerGready;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static newcloud.Constants.*;
import static newcloud.Constants.HOST_MIPS;


public class GreedyScheduleTest {

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
    private static VmAllocationAssignerGready vmAllocationAssignerGready;
    private static double smallestdata = Double.MAX_VALUE;

    public List<Double> execute() throws Exception {
        for (int i = 0; i < Iteration; i++) {
            CloudSim.init(1, Calendar.getInstance(), false);
            broker = createBroker();
            brokerId = broker.getId();
            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(Constants.NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new NewPowerAllocatePolicy(hostList);

            vmAllocationAssignerGready = new VmAllocationAssignerGready(vmAllocationPolicy, GenExcel.getInstance());


            PowerDatacenterGready datacenter = createDatacenter(
                    "Datacenter",
                    PowerDatacenterGready.class,
                    hostList,
                    vmAllocationPolicy);

            datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            CloudSim.terminateSimulation(terminateTime);
            double lastClock = CloudSim.startSimulation();
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();
            System.out.println(i + "----------------------------------");
        }

        System.out.println(vmList.size());
//        GenExcel.getInstance().genExcel();
        for (PowerHost host : hostList) {
            System.out.println(host.getId() + ":" + host.getVmList().size() + ":" + host.getAvailableMips());
        }
        for (int i = 0; i < PowerDatacenterGready.allpower.size(); i++) {
            System.out.println(PowerDatacenterGready.allpower.get(i));
            if (PowerDatacenterGready.allpower.get(i) < smallestdata) {
                smallestdata = PowerDatacenterGready.allpower.get(i);
            }
        }
        System.out.println("最小值：" + smallestdata);
        return PowerDatacenterGready.allpower;
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

    public PowerDatacenterGready createDatacenter(
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
        PowerDatacenterGready datacenter = new PowerDatacenterGready("test", characteristics, vmAllocationPolicy, new LinkedList<Storage>(), 300, vmAllocationAssignerGready);
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


