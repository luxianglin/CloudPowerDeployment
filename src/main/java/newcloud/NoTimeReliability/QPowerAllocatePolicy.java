package newcloud.NoTimeReliability;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import java.util.List;
import java.util.Map;

public class QPowerAllocatePolicy extends PowerVmAllocationPolicyAbstract {
    /**
     * Instantiates a new PowerVmAllocationPolicyAbstract.
     *
     * @param list the list
     */
    public QPowerAllocatePolicy(List<? extends Host> list) {
        super(list);
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null;
    }

    @Override
    public PowerHost findHostForVm(Vm vm) {
        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (host.isSuitableForVm(vm)) {
                return host;
            }
        }
        return null;
    }
}
