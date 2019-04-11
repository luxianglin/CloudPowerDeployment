package newcloud;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import java.util.List;
import java.util.Map;

public class NewPowerAllocatePolicy extends PowerVmAllocationPolicyAbstract {
    /**
     * Instantiates a new PowerVmAllocationPolicyAbstract.
     *
     * @param list the list
     */
    public NewPowerAllocatePolicy(List<? extends Host> list) {
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

    public void deallocateHostForVm(Vm vm) {
        Host host = (Host)this.getVmTable().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }
}
