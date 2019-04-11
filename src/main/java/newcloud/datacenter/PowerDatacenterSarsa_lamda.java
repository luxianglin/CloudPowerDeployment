/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package newcloud.datacenter;



import newcloud.NewPowerAllocatePolicy;
import newcloud.policy.VmAllocationAssignerSarsa_lamda;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static newcloud.Constants.*;
import static newcloud.ExceuteData.SarsaLamdaScheduleTest.brokerId;


/**
 * PowerDatacenter is a class that enables simulation of power-aware data centers.
 * <p>
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 *
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenterSarsa_lamda extends PowerDatacenter {

    /**
     * The datacenter consumed power.
     */
    private double power;

    /**
     * Indicates if migrations are disabled or not.
     */
    private boolean disableMigrations;

    /**
     * The last time submitted cloudlets were processed.
     */
    private double cloudletSubmitted;

    /**
     * The VM migration count.
     */
    private int migrationCount;

    private double currentTime;

    private VmAllocationAssignerSarsa_lamda vmAllocationAssignerSarsa_lamda;

    private Host targetHost;

    private String currentcpu;
    private String historycpu;


    public static List<List<Double>> everyhosthistorypower = new ArrayList<>();

    public static List<Double> allpower = new ArrayList<>();

    /**
     * Instantiates a new PowerDatacenter.
     *
     * @param name               the datacenter name
     * @param characteristics    the datacenter characteristics
     * @param schedulingInterval the scheduling interval
     * @param vmAllocationPolicy the vm provisioner
     * @param storageList        the storage list
     * @throws Exception the exception
     */
    public PowerDatacenterSarsa_lamda(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval, VmAllocationAssignerSarsa_lamda vmAllocationAssignerSarsa_lamda) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

        setPower(0.0);
        setDisableMigrations(false);
        setCloudletSubmitted(-1);
        setMigrationCount(0);
        this.vmAllocationAssignerSarsa_lamda = vmAllocationAssignerSarsa_lamda;
        resetEnvironment();
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case CREATE_VM_ACK:
                processVmCreate(ev, true);
                break;
            default:
                if (ev == null) {
                    Log.printConcatLine(getName(), ".processOtherEvent(): Error - an event is null in Datacenter.");
                }
                break;
        }
    }

    public void resetEnvironment() {
        currentcpu = "";
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            currentcpu += "0";
        }
        historycpu = currentcpu;
//        for (Host host : getHostList()) {
//            host.vmDestroyAll();
//        }
    }

    public void getReward() {
        double totalreward = 0;
        double reward = 0;
        List<Double> cpulist = new ArrayList<>();
        List<Double> historyList = new ArrayList<>();

        for (PowerHost host : this.<PowerHost>getHostList()) {
            double utilizationOfCpu = host.getUtilizationOfCpu();
            double historyUtilizationOfCpu = host.getPreviousUtilizationMips() / host.getTotalMips();
            cpulist.add(utilizationOfCpu);
            historyList.add(historyUtilizationOfCpu);
        }

        if (everyhosthistorypower.size() >= 2) {
            List<Double> everyhostcurrentpower = everyhosthistorypower.get(0);
            List<Double> everyhostlastpower = everyhosthistorypower.get(1);
            for (int i = 0; i < getHostList().size(); i++) {
                if (everyhostcurrentpower.get(i) != 0) {
                    reward = Math.pow(everyhostlastpower.get(i) / everyhostcurrentpower.get(i), 10000);
                } else {
                    reward = 0;
                }
                totalreward += reward;

            }
        }
        double ss=totalreward;
        currentcpu = convertCPUUtilization(cpulist);
        historycpu = convertCPUUtilization(historyList);
        vmAllocationAssignerSarsa_lamda.createLastState_idx(historycpu);
        vmAllocationAssignerSarsa_lamda.createState_idx(currentcpu);
        vmAllocationAssignerSarsa_lamda.updateQList(targetHost.getId(), totalreward, historycpu, currentcpu);
    }


    public String convertCPUUtilization(List<Double> cpuutils) {
        String convertresult = "";
        String convertList = "";
        for (Double cpuutil : cpuutils) {
            cpuutil = cpuutil * 100;
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

    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
        Vm vm = (Vm) ev.getData();
        int hostid = vmAllocationAssignerSarsa_lamda.createAction(currentcpu);
        targetHost = getHostList().get(hostid);
        boolean result = getVmAllocationPolicy().allocateHostForVm(vm, targetHost);
        if (!result) {
            NewPowerAllocatePolicy newPowerAllocatePolicy = (NewPowerAllocatePolicy) getVmAllocationPolicy();
            targetHost = newPowerAllocatePolicy.findHostForVm(vm);
            result = newPowerAllocatePolicy.allocateHostForVm(vm);
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

            vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
                    .getAllocatedMipsForVm(vm));
        }

    }

    @Override
    protected void updateCloudletProcessing() {
//        if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
//            CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
//            schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
//            return;
//        }
        currentTime = CloudSim.clock();

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

            setLastProcessTime(currentTime);
        }
    }

    /**
     * Update cloudet processing without scheduling future events.
     *
     * @return the double
     * @todo There is an inconsistence in the return value of this
     * method with return value of similar methods
     * such as {@link #updateCloudetProcessingWithoutSchedulingFutureEventsForce()},
     * that returns {@link Double#MAX_VALUE} by default.
     * The current method returns 0 by default.
     * @see #updateCloudetProcessingWithoutSchedulingFutureEventsForce()
     */
    protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
        if (CloudSim.clock() > getLastProcessTime()) {
            return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
        }
        return 0;
    }

    /**
     * Update cloudet processing without scheduling future events.
     *
     * @return expected time of completion of the next cloudlet in all VMs of all hosts or
     * {@link Double#MAX_VALUE} if there is no future events expected in this host
     */
    protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
        double currentTime = CloudSim.clock();
        double minTime = Double.MAX_VALUE;
        double timeDiff = currentTime - getLastProcessTime();
        double timeFrameDatacenterEnergy = 0.0;
        List<Double> everyhostpower = new ArrayList<>();

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
                //将每一个物理机的能耗写入列表中
                everyhostpower.add(timeFrameHostEnergy);
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

        everyhosthistorypower.add(0, everyhostpower);

        setPower(getPower() + timeFrameDatacenterEnergy);
        checkCloudletCompletion();
        /** Remove completed VMs **/
//        for (PowerHost host : this.<PowerHost>getHostList()) {
//            for (Vm vm : host.getCompletedVms()) {
//                getVmAllocationPolicy().deallocateHostForVm(vm);
//                getVmList().remove(vm);
//                Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
//            }
//        }

        Log.printLine();
        if (currentTime > outputTime) {
            allpower.add(getPower());
        }
        setLastProcessTime(currentTime);
        return minTime;
    }

    @Override
    protected void processVmMigrate(SimEvent ev, boolean ack) {
        updateCloudetProcessingWithoutSchedulingFutureEvents();
        super.processVmMigrate(ev, ack);
        SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
        if (event == null || event.eventTime() > CloudSim.clock()) {
            updateCloudetProcessingWithoutSchedulingFutureEventsForce();
        }
    }

    @Override
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
        updateCloudletProcessing();

        try {
            // gets the Cloudlet object
            Cloudlet cl = (Cloudlet) ev.getData();

            // checks whether this Cloudlet has finished or not
            if (cl.isFinished()) {
                String name = CloudSim.getEntityName(cl.getUserId());
                Log.printConcatLine(getName(), ": Warning - Cloudlet #", cl.getCloudletId(), " owned by ", name,
                        " is already completed/finished.");
                Log.printLine("Therefore, it is not being executed again");
                Log.printLine();

                // NOTE: If a Cloudlet has finished, then it won't be processed.
                // So, if ack is required, this method sends back a result.
                // If ack is not required, this method don't send back a result.
                // Hence, this might cause CloudSim to be hanged since waiting
                // for this Cloudlet back.
                if (ack) {
                    int[] data = new int[3];
                    data[0] = getId();
                    data[1] = cl.getCloudletId();
                    data[2] = CloudSimTags.FALSE;

                    // unique tag = operation tag
                    int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                    sendNow(cl.getUserId(), tag, data);
                }

                sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

                return;
            }

            // process this Cloudlet to this CloudResource
            cl.setResourceParameter(
                    getId(), getCharacteristics().getCostPerSecond(),
                    getCharacteristics().getCostPerBw());

            int userId = cl.getUserId();
            int vmId = cl.getVmId();

            // time to transfer the files
            double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());

            Host host = getVmAllocationPolicy().getHost(vmId, userId);
            Vm vm = host.getVm(vmId, userId);
            CloudletScheduler scheduler = vm.getCloudletScheduler();
            double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);

            // if this cloudlet is in the exec queue
            if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
                estimatedFinishTime += fileTransferTime;
                System.out.println("estimatedFinishTime:" + estimatedFinishTime);
                send(getId(), currentTime, CloudSimTags.VM_DATACENTER_EVENT);
            }

            if (ack) {
                int[] data = new int[3];
                data[0] = getId();
                data[1] = cl.getCloudletId();
                data[2] = CloudSimTags.TRUE;

                // unique tag = operation tag
                int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                sendNow(cl.getUserId(), tag, data);
            }
        } catch (ClassCastException c) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
            c.printStackTrace();
        } catch (Exception e) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
            e.printStackTrace();
        }

        checkCloudletCompletion();
        setCloudletSubmitted(CloudSim.clock());
        getReward();
        send(brokerId, 0, CLOUDSIM_RESTART);

    }

    /**
     * Gets the power.
     *
     * @return the power
     */
    public double getPower() {
        return power;
    }

    /**
     * Sets the power.
     *
     * @param power the new power
     */
    protected void setPower(double power) {
        this.power = power;
    }

    /**
     * Checks if PowerDatacenter is in migration.
     *
     * @return true, if PowerDatacenter is in migration; false otherwise
     */
    protected boolean isInMigration() {
        boolean result = false;
        for (Vm vm : getVmList()) {
            if (vm.isInMigration()) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Checks if migrations are disabled.
     *
     * @return true, if  migrations are disable; false otherwise
     */
    public boolean isDisableMigrations() {
        return disableMigrations;
    }

    /**
     * Disable or enable migrations.
     *
     * @param disableMigrations true to disable migrations; false to enable
     */
    public void setDisableMigrations(boolean disableMigrations) {
        this.disableMigrations = disableMigrations;
    }

    /**
     * Checks if is cloudlet submited.
     *
     * @return true, if is cloudlet submited
     */
    protected double getCloudletSubmitted() {
        return cloudletSubmitted;
    }

    /**
     * Sets the cloudlet submitted.
     *
     * @param cloudletSubmitted the new cloudlet submited
     */
    protected void setCloudletSubmitted(double cloudletSubmitted) {
        this.cloudletSubmitted = cloudletSubmitted;
    }

    /**
     * Gets the migration count.
     *
     * @return the migration count
     */
    public int getMigrationCount() {
        return migrationCount;
    }

    /**
     * Sets the migration count.
     *
     * @param migrationCount the new migration count
     */
    protected void setMigrationCount(int migrationCount) {
        this.migrationCount = migrationCount;
    }

    /**
     * Increment migration count.
     */
    protected void incrementMigrationCount() {
        setMigrationCount(getMigrationCount() + 1);
    }

}
