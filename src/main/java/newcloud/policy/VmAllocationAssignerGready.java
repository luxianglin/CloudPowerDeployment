package newcloud.policy;

import newcloud.GenExcel;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.*;


public class VmAllocationAssignerGready { // 贪心分配策略
    private GenExcel genExcel = null;
    private VmAllocationPolicy vmAllocationPolicy;

    public VmAllocationAssignerGready(VmAllocationPolicy vmAllocationPolicy, GenExcel genExcel) {
        this.vmAllocationPolicy = vmAllocationPolicy;
        this.genExcel = genExcel;
        this.genExcel.init();
    }

//    public Host getVmAllcaotionHost(List<PowerHost> hostList, Vm vm) {
//        Host targetHost = null;
//        List<Double> totalPowerList = new ArrayList<>();
//        for (PowerHost host : hostList) {
//            if (host.getVmList().size() == 0) {
//                targetHost = host;
//                return targetHost;
//            }
//        }
//        for (int i = 0; i < hostList.size(); i++) {
//            PowerHost host = hostList.get(i);
//            if (host.getVmList().size() > 0) {
//                double previousUtilizationOfCpu = (host.getTotalMips() - host.getAvailableMips()) / host.getTotalMips();
//                vmAllocationPolicy.allocateHostForVm(vm, host);
//                double utilizationOfCpu = (host.getTotalMips() - host.getAvailableMips()) / host.getTotalMips();
//                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
//                        previousUtilizationOfCpu,
//                        utilizationOfCpu,
//                        100);
//                totalPowerList.add(i, timeFrameHostEnergy);
//                vmAllocationPolicy.deallocateHostForVm(vm);
//            }
//        }
//        double minvalue = Collections.min(totalPowerList);
//        int index = totalPowerList.indexOf(minvalue);
//        targetHost = hostList.get(index);
//        return targetHost;
//    }

    public Host getVmAllcaotionHost(List<PowerHost> hostList, Vm vm) {
        Host targetHost = null;
        List<Double> totalPowerList = new ArrayList<>();
        for (int i = 0; i < hostList.size(); i++) {
            PowerHost host = hostList.get(i);
                double previousUtilizationOfCpu = (host.getTotalMips() - host.getAvailableMips()) / host.getTotalMips();
                vmAllocationPolicy.allocateHostForVm(vm, host);
                double utilizationOfCpu = (host.getTotalMips() - host.getAvailableMips()) / host.getTotalMips();
                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                        previousUtilizationOfCpu,
                        utilizationOfCpu,
                        100);
                totalPowerList.add(i, timeFrameHostEnergy);
                vmAllocationPolicy.deallocateHostForVm(vm);
            }
        double minvalue = Collections.min(totalPowerList);
        int index = totalPowerList.indexOf(minvalue);
        targetHost = hostList.get(index);
        return targetHost;
    }
}
