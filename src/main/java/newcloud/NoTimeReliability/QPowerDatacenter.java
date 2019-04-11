package newcloud.NoTimeReliability;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static newcloud.Constants.*;


public class QPowerDatacenter extends PowerDatacenter {


    private String cpulist;
    private boolean result = false;
    private double totalpower = 0;
    VmAllocationAssigner vmAllocationAssigner;
    private List<Vm> allocatedVmList = new ArrayList<>();
    List<String> cpuHistoryList = new ArrayList<>();
    private String lastcpulist;
    private int hostid = 0;
    private double lastprocesstime = 0;
    private VmAllocationPolicy vmAllocationPolicy;
    private Map<Integer, String[]> cpuhistory = new HashMap<>();
    public static List<Double> allpower = new ArrayList<>();

    /**
     * Instantiates a new datacenter.
     *
     * @param name               the name
     * @param characteristics    the res config
     * @param vmAllocationPolicy the vm provisioner
     * @param storageList        the storage list
     * @param schedulingInterval the scheduling interval
     * @throws Exception the exception
     */
    public QPowerDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval, VmAllocationAssigner vmAllocationAssigner) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.vmAllocationPolicy = vmAllocationPolicy;
        this.vmAllocationAssigner = vmAllocationAssigner;
        resetEnvironment();
    }

    public void resetEnvironment() {
        cpulist = "";
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            cpulist += "0";
        }
        lastcpulist = cpulist;
        for (Host host : getHostList()) {
            host.vmDestroyAll();
        }

    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case CREATE_VM:
                processVmCreate(ev, false);
                break;
            case CREATE_VM_ACK:
                processVmCreate(ev, true);
                break;
            default:
                Log.printLine(getName() + ": unknown event type");
                break;
        }
    }


    public void getReward() {
        if (result == true) {
            double currentTime = CloudSim.clock();
            double timeDiff = currentTime - lastprocesstime;
            lastprocesstime = currentTime;
            double timeFrameDatacenterEnergy = 0.0;
            if (timeDiff > 0) {
                for (PowerHost powerhost : this.<PowerHost>getHostList()) {
                    double previousUtilizationOfCpu = powerhost.getPreviousUtilizationOfCpu();
                    double utilizationOfCpu = powerhost.getUtilizationOfCpu();
                    if (previousUtilizationOfCpu != 0) {
                        double timeFrameHostEnergy = powerhost.getEnergyLinearInterpolation(
                                previousUtilizationOfCpu,
                                utilizationOfCpu,
                                timeDiff);
                        timeFrameDatacenterEnergy += timeFrameHostEnergy;
                        double reward = timeFrameHostEnergy;
                        String[] savecpu = cpuhistory.get(powerhost.getId());
                        lastcpulist = savecpu[0];
                        cpulist = savecpu[1];
                        vmAllocationAssigner.createLastState_idx(lastcpulist);
                        vmAllocationAssigner.createState_idx(cpulist);
                        vmAllocationAssigner.updateQList(powerhost.getId(), reward, lastcpulist, cpulist);
                    }
                }
            }
            totalpower = timeFrameDatacenterEnergy;


            cpuHistoryList.add(cpulist);
        }
    }


    @Override
    protected void updateCloudletProcessing() {
        if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
            CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
            schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            return;
        }
        double currentTime = CloudSim.clock();

        // if some time passed since last processing
        if (currentTime > getLastProcessTime()) {
            System.out.print(currentTime + " ");

            double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

            if (!isDisableMigrations()) {
                List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
                        getVmList());

                if (migrationMap != null) {
                    for (Map<String, Object> migrate : migrationMap) {
                        Vm vm = (Vm) migrate.get("vm");
                        PowerHost targetHost = (PowerHost) migrate.get("host");
                        PowerHost oldHost = (PowerHost) vm.getHost();

                        if (oldHost == null) {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d to Host #%d is started",
                                    currentTime,
                                    vm.getId(),
                                    targetHost.getId());
                        } else {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
                                    currentTime,
                                    vm.getId(),
                                    oldHost.getId(),
                                    targetHost.getId());
                        }

                        targetHost.addMigratingInVm(vm);
                        incrementMigrationCount();

                        /** VM migration delay = RAM / bandwidth **/
                        // we use BW / 2 to model BW available for migration purposes, the other
                        // half of BW is for VM communication
                        // around 16 seconds for 1024 MB using 1 Gbit/s network
                        send(
                                getId(),
                                vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
                                CloudSimTags.VM_MIGRATE,
                                migrate);
                    }
                }
            }

            // schedules an event to the next time
            if (minTime != Double.MAX_VALUE) {
                CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
                send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            }
            getReward();
            setLastProcessTime(currentTime);

        }
    }

    @Override
    public void processVmCreate(SimEvent ev, boolean ack) {
        Vm vm = (Vm) ev.getData();
        hostid = vmAllocationAssigner.createAction(cpulist);
        Host host = getHostList().get(hostid);
        result = getVmAllocationPolicy().allocateHostForVm(vm, host);
        lastcpulist = cpulist;
        cpulist = convertCPUUtilization(getHostList());
        allocatedVmList.add(vm);
        if (result != true) {
            double reward = 1000000000;
            vmAllocationAssigner.updateQList(hostid, reward, lastcpulist, cpulist);
            QPowerAllocatePolicy ss = (QPowerAllocatePolicy) vmAllocationPolicy;
            PowerHost findhost = ss.findHostForVm(vm);
            result = getVmAllocationPolicy().allocateHostForVm(vm, findhost);
            lastcpulist = cpulist;
            cpulist = convertCPUUtilization(getHostList());
            String[] cpus = new String[2];
            cpus[0] = lastcpulist;
            cpus[1] = cpulist;
            cpuhistory.put(findhost.getId(), cpus);
        } else {
            String[] cpus = new String[2];
            cpus[0] = lastcpulist;
            cpus[1] = cpulist;
            cpuhistory.put(hostid, cpus);
        }

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = vm.getId();

            if (result) {
                data[2] = CloudSimTags.TRUE;
            } else {
                data[2] = CloudSimTags.FALSE;
            }
            send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CREATE_VM_ACK, data);
        }

        if (result) {
            getVmList().add(vm);

            if (vm.isBeingInstantiated()) {
                vm.setBeingInstantiated(false);
            }
            vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
        }
    }

    /**
     * Update cloudet processing without scheduling future events.
     *
     * @return expected time of completion of the next cloudlet in all VMs of all hosts or
     * {@link Double#MAX_VALUE} if there is no future events expected in this host
     */
    @Override
    protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
        double currentTime = CloudSim.clock();
        double minTime = Double.MAX_VALUE;
        double timeDiff = currentTime - getLastProcessTime();
        double timeFrameDatacenterEnergy = 0.0;

        Log.printLine("\n\n--------------------------------------------------------------\n\n");
        Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);

        for (PowerHost host : this.<PowerHost>getHostList()) {
            Log.printLine();

            double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
            if (time < minTime) {
                minTime = time;
            }

            Log.formatLine(
                    "%.2f: [Host #%d] utilization is %.2f%%",
                    currentTime,
                    host.getId(),
                    host.getUtilizationOfCpu() * 100);
        }

        if (timeDiff > 0) {
            Log.formatLine(
                    "\nEnergy consumption for the last time frame from %.2f to %.2f:",
                    getLastProcessTime(),
                    currentTime);

            for (PowerHost host : this.<PowerHost>getHostList()) {
                double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
                double utilizationOfCpu = host.getUtilizationOfCpu();
                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                        previousUtilizationOfCpu,
                        utilizationOfCpu,
                        timeDiff);
                timeFrameDatacenterEnergy += timeFrameHostEnergy;

                Log.printLine();
                Log.formatLine(
                        "%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
                        currentTime,
                        host.getId(),
                        getLastProcessTime(),
                        previousUtilizationOfCpu * 100,
                        utilizationOfCpu * 100);
                Log.formatLine(
                        "%.2f: [Host #%d] energy is %.2f W*sec",
                        currentTime,
                        host.getId(),
                        timeFrameHostEnergy);
            }

            Log.formatLine(
                    "\n%.2f: Data center's energy is %.2f W*sec\n",
                    currentTime,
                    timeFrameDatacenterEnergy);
        }

        setPower(getPower() + timeFrameDatacenterEnergy);

        checkCloudletCompletion();

        /** Remove completed VMs **/
        for (PowerHost host : this.<PowerHost>getHostList()) {
            for (Vm vm : host.getCompletedVms()) {
                getVmAllocationPolicy().deallocateHostForVm(vm);
                getVmList().remove(vm);
                Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
            }
        }

        Log.printLine();
        if (currentTime > outputTime) {
            allpower.add(getPower());
        }
        setLastProcessTime(currentTime);
        return minTime;
    }

    @Override
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
        super.processCloudletSubmit(ev, ack);
        setCloudletSubmitted(CloudSim.clock());
    }

    public String convertCPUUtilization(List<Host> hosts) {
        String convertresult = "";
        String convertList = "";
        for (Host host : hosts) {
            double cpuutil = getUtilizationOfCpuMips(host) * 100;
            if (cpuutil >= 0 && cpuutil < 10) {
                convertresult = "0";
            } else if (cpuutil >= 10 && cpuutil < 20) {
                convertresult = "1";
            } else if (cpuutil >= 20 && cpuutil < 30) {
                convertresult = "2";
            } else if (cpuutil >= 30 && cpuutil < 40) {
                convertresult = "3";
            } else if (cpuutil >= 40 && cpuutil < 50) {
                convertresult = "4";
            } else if (cpuutil >= 50 && cpuutil < 60) {
                convertresult = "5";
            } else if (cpuutil >= 60 && cpuutil < 70) {
                convertresult = "6";
            } else if (cpuutil >= 70 && cpuutil < 80) {
                convertresult = "7";
            } else if (cpuutil >= 80 && cpuutil < 90) {
                convertresult = "8";
            } else if (cpuutil >= 90 && cpuutil <= 100) {
                convertresult = "9";
            } else {
                System.out.println("CPU利用率转化出现问题");
                break;
            }
            convertList += convertresult;
        }
        return convertList;
    }

    public double getUtilizationOfCpuMips(Host host) {
        double hostUtilizationMips = 0;
        for (Vm vm2 : host.getVmList()) {
            // calculate additional potential CPU usage of a migrating in VM
            hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
        }
        double s = hostUtilizationMips;
        double ss = host.getTotalMips();
        return hostUtilizationMips / host.getTotalMips();
    }

}
