package newcloud.NoTimeReliability;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;

import java.util.Iterator;

import static newcloud.Constants.CREATE_VM_ACK;


public class QPowerDatacenterBroker extends PowerDatacenterBroker {
    /**
     * Instantiates a new power datacenter broker.
     *
     * @param name the name
     * @throws Exception the exception
     */
    public QPowerDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case CREATE_VM_ACK:
                processVmCreate(ev);
                break;
            default:
                Log.printLine(getName() + ": unknown event type");
                break;
        }

    }


    @Override
    protected void createVmsInDatacenter(int datacenterId) {
        // send as much vms as possible for this datacenter before trying the next one
        int requestedVms = 0;
        String datacenterName = CloudSim.getEntityName(datacenterId);
        for (Vm vm : getVmList()) {
            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
                        + " in " + datacenterName);
                sendNow(datacenterId, CREATE_VM_ACK, vm);
                requestedVms++;
            }
        }

        getDatacenterRequestedIdsList().add(datacenterId);

        setVmsRequested(requestedVms);
        setVmsAcks(0);
    }

    @Override
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ((int[]) ev.getData());
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];
        if (result == 1) {
            this.getVmsToDatacentersMap().put(vmId, datacenterId);
            this.getVmsCreatedList().add(VmList.getById(this.getVmList(), vmId));
            Log.printLine(CloudSim.clock() + ": " + this.getName() + ": VM #" + vmId + " has been created in Datacenter #" + datacenterId + ", Host #" + VmList.getById(this.getVmsCreatedList(), vmId).getHost().getId());
        } else {
            Log.printLine(CloudSim.clock() + ": " + this.getName() + ": Creation of VM #" + vmId + " failed in Datacenter #" + datacenterId);
        }

        this.incrementVmsAcks();
        if (this.getVmsCreatedList().size() == this.getVmList().size() - this.getVmsDestroyed()) {
            this.submitCloudlets();
        } else if (this.getVmsRequested() == this.getVmsAcks()) {
            Iterator i$ = this.getDatacenterIdsList().iterator();

            while (i$.hasNext()) {
                int nextDatacenterId = (Integer) i$.next();
                if (!this.getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
                    this.createVmsInDatacenter(nextDatacenterId);
                    return;
                }
            }

            if (this.getVmsCreatedList().size() > 0) {
                this.submitCloudlets();
            } else {
                Log.printLine(CloudSim.clock() + ": " + this.getName() + ": none of the required VMs could be created. Aborting");
                this.finishExecution();
            }
        }

    }
}
