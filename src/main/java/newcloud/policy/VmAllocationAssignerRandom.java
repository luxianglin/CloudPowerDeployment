package newcloud.policy;

import newcloud.GenExcel;
import newcloud.NewPowerAllocatePolicy;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class VmAllocationAssignerRandom { // 随机分配策略
    private GenExcel genExcel = null;


    public VmAllocationAssignerRandom( GenExcel genExcel) {
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    public Host getVmAllcaotionHost(List<Host> hostList, Vm vm) {
        int index = randomInt(0, hostList.size() - 1);
        Host targetHost = hostList.get(index);
        return targetHost;
    }

    private int randomInt(int min, int max) { // random[min,max] 可取min,可取max
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }
}
