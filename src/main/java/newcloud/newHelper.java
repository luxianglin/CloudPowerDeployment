package newcloud;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.MathUtil;

public class newHelper {
    public newHelper() {
    }

    public static List<Vm> createVmList(int brokerId, int vmsNumber) {
        List<Vm> vms = new ArrayList();

        for(int i = 0; i < vmsNumber; ++i) {
            int vmType = i / (int)Math.ceil((double)vmsNumber / 5.0D);
            vms.add(new PowerVm(i, brokerId, (double)Constants.VM_MIPS[vmType], Constants.VM_PES[vmType], Constants.VM_RAM[vmType], 100000L, 2500L, 1, "Xen", new CloudletSchedulerDynamicWorkload((double)Constants.VM_MIPS[vmType], Constants.VM_PES[vmType]), 300.0D));
        }

        return vms;
    }

    public static List<PowerHost> createHostList(int hostsNumber) {
        List<PowerHost> hostList = new ArrayList();

        for(int i = 0; i < hostsNumber; ++i) {
            int hostType = i % 3;
            List<Pe> peList = new ArrayList();

            for(int j = 0; j < Constants.HOST_PES[hostType]; ++j) {
                peList.add(new Pe(j, new PeProvisionerSimple((double)Constants.HOST_MIPS[hostType])));
            }

            hostList.add(new PowerHostUtilizationHistory(i, new RamProvisionerSimple(Constants.HOST_RAM[hostType]), new BwProvisionerSimple(10000000000L), 1000000L, peList, new VmSchedulerTimeSharedOverSubscription(peList), Constants.HOST_POWER[hostType]));
        }

        return hostList;
    }

    public static DatacenterBroker createBroker() {
        PowerDatacenterBroker broker = null;

        try {
            broker = new PowerDatacenterBroker("Broker");
        } catch (Exception var2) {
            var2.printStackTrace();
            System.exit(0);
        }

        return broker;
    }

    public static Datacenter createDatacenter(String name, Class<? extends Datacenter> datacenterClass, List<PowerHost> hostList, VmAllocationPolicy vmAllocationPolicy) throws Exception {
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0D;
        double cost = 3.0D;
        double costPerMem = 0.05D;
        double costPerStorage = 0.001D;
        double costPerBw = 0.0D;
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        Datacenter datacenter = null;

        try {
            datacenter = (Datacenter)datacenterClass.getConstructor(String.class, DatacenterCharacteristics.class, VmAllocationPolicy.class, List.class, Double.TYPE).newInstance(name, characteristics, vmAllocationPolicy, new LinkedList(), 300.0D);
        } catch (Exception var20) {
            var20.printStackTrace();
            System.exit(0);
        }

        return datacenter;
    }

    public static List<Double> getTimesBeforeHostShutdown(List<Host> hosts) {
        List<Double> timeBeforeShutdown = new LinkedList();
        Iterator var2 = hosts.iterator();

        while(var2.hasNext()) {
            Host host = (Host)var2.next();
            boolean previousIsActive = true;
            double lastTimeSwitchedOn = 0.0D;

            HostStateHistoryEntry entry;
            for(Iterator var7 = ((HostDynamicWorkload)host).getStateHistory().iterator(); var7.hasNext(); previousIsActive = entry.isActive()) {
                entry = (HostStateHistoryEntry)var7.next();
                if (previousIsActive && !entry.isActive()) {
                    timeBeforeShutdown.add(entry.getTime() - lastTimeSwitchedOn);
                }

                if (!previousIsActive && entry.isActive()) {
                    lastTimeSwitchedOn = entry.getTime();
                }
            }
        }

        return timeBeforeShutdown;
    }

    public static List<Double> getTimesBeforeVmMigration(List<Vm> vms) {
        List<Double> timeBeforeVmMigration = new LinkedList();
        Iterator var2 = vms.iterator();

        while(var2.hasNext()) {
            Vm vm = (Vm)var2.next();
            boolean previousIsInMigration = false;
            double lastTimeMigrationFinished = 0.0D;

            VmStateHistoryEntry entry;
            for(Iterator var7 = vm.getStateHistory().iterator(); var7.hasNext(); previousIsInMigration = entry.isInMigration()) {
                entry = (VmStateHistoryEntry)var7.next();
                if (previousIsInMigration && !entry.isInMigration()) {
                    timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
                }

                if (!previousIsInMigration && entry.isInMigration()) {
                    lastTimeMigrationFinished = entry.getTime();
                }
            }
        }

        return timeBeforeVmMigration;
    }

    public static void printResults(PowerDatacenter datacenter, List<Vm> vms, double lastClock, String experimentName, boolean outputInCsv, String outputFolder) {
        Log.enable();
        List<Host> hosts = datacenter.getHostList();
        int numberOfHosts = hosts.size();
        int numberOfVms = vms.size();
        double energy = datacenter.getPower() / 3600000.0D;
        int numberOfMigrations = datacenter.getMigrationCount();
        Map<String, Double> slaMetrics = getSlaMetrics(vms);
        double slaOverall = (Double)slaMetrics.get("overall");
        double slaAverage = (Double)slaMetrics.get("average");
        double slaDegradationDueToMigration = (Double)slaMetrics.get("underallocated_migration");
        double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);
        double sla = slaTimePerActiveHost * slaDegradationDueToMigration;
        List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);
        int numberOfHostShutdowns = timeBeforeHostShutdown.size();
        double meanTimeBeforeHostShutdown = 0.0D / 0.0;
        double stDevTimeBeforeHostShutdown = 0.0D / 0.0;
        if (!timeBeforeHostShutdown.isEmpty()) {
            meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
            stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
        }

        List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
        double meanTimeBeforeVmMigration = 0.0D / 0.0;
        double stDevTimeBeforeVmMigration = 0.0D / 0.0;
        if (!timeBeforeVmMigration.isEmpty()) {
            meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
            stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
        }

        if (outputInCsv) {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            File folder1 = new File(outputFolder + "/stats");
            if (!folder1.exists()) {
                folder1.mkdir();
            }

            File folder2 = new File(outputFolder + "/time_before_host_shutdown");
            if (!folder2.exists()) {
                folder2.mkdir();
            }

            File folder3 = new File(outputFolder + "/time_before_vm_migration");
            if (!folder3.exists()) {
                folder3.mkdir();
            }

            File folder4 = new File(outputFolder + "/metrics");
            if (!folder4.exists()) {
                folder4.mkdir();
            }

            StringBuilder data = new StringBuilder();
            String delimeter = ",";
            data.append(experimentName + delimeter);
            data.append(parseExperimentName(experimentName));
            data.append(String.format("%d", numberOfHosts) + delimeter);
            data.append(String.format("%d", numberOfVms) + delimeter);
            data.append(String.format("%.2f", lastClock) + delimeter);
            data.append(String.format("%.5f", energy) + delimeter);
            data.append(String.format("%d", numberOfMigrations) + delimeter);
            data.append(String.format("%.10f", sla) + delimeter);
            data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
            data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);
            data.append(String.format("%.10f", slaOverall) + delimeter);
            data.append(String.format("%.10f", slaAverage) + delimeter);
            data.append(String.format("%d", numberOfHostShutdowns) + delimeter);
            data.append(String.format("%.2f", meanTimeBeforeHostShutdown) + delimeter);
            data.append(String.format("%.2f", stDevTimeBeforeHostShutdown) + delimeter);
            data.append(String.format("%.2f", meanTimeBeforeVmMigration) + delimeter);
            data.append(String.format("%.2f", stDevTimeBeforeVmMigration) + delimeter);
            if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
                PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract)datacenter.getVmAllocationPolicy();
                double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
                double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
                double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
                double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
                double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
                double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
                double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryTotal());
                double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryTotal());
                data.append(String.format("%.5f", executionTimeVmSelectionMean) + delimeter);
                data.append(String.format("%.5f", executionTimeVmSelectionStDev) + delimeter);
                data.append(String.format("%.5f", executionTimeHostSelectionMean) + delimeter);
                data.append(String.format("%.5f", executionTimeHostSelectionStDev) + delimeter);
                data.append(String.format("%.5f", executionTimeVmReallocationMean) + delimeter);
                data.append(String.format("%.5f", executionTimeVmReallocationStDev) + delimeter);
                data.append(String.format("%.5f", executionTimeTotalMean) + delimeter);
                data.append(String.format("%.5f", executionTimeTotalStDev) + delimeter);
                writeMetricHistory(hosts, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName + "_metric");
            }

            data.append("\n");
            writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
            writeDataColumn(timeBeforeHostShutdown, outputFolder + "/time_before_host_shutdown/" + experimentName + "_time_before_host_shutdown.csv");
            writeDataColumn(timeBeforeVmMigration, outputFolder + "/time_before_vm_migration/" + experimentName + "_time_before_vm_migration.csv");
        } else {
            Log.setDisabled(false);
            Log.printLine();
            Log.printLine(String.format("Experiment name: " + experimentName));
            Log.printLine(String.format("Number of hosts: " + numberOfHosts));
            Log.printLine(String.format("Number of VMs: " + numberOfVms));
            Log.printLine(String.format("Total simulation time: %.2f sec", lastClock));
            Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
            Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
            Log.printLine(String.format("SLA: %.5f%%", sla * 100.0D));
            Log.printLine(String.format("SLA perf degradation due to migration: %.2f%%", slaDegradationDueToMigration * 100.0D));
            Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100.0D));
            Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100.0D));
            Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100.0D));
            Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
            Log.printLine(String.format("Mean time before a host shutdown: %.2f sec", meanTimeBeforeHostShutdown));
            Log.printLine(String.format("StDev time before a host shutdown: %.2f sec", stDevTimeBeforeHostShutdown));
            Log.printLine(String.format("Mean time before a VM migration: %.2f sec", meanTimeBeforeVmMigration));
            Log.printLine(String.format("StDev time before a VM migration: %.2f sec", stDevTimeBeforeVmMigration));
            if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
                PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract)datacenter.getVmAllocationPolicy();
                double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
                double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
                double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
                double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
                double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
                double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
                double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryTotal());
                double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryTotal());
                Log.printLine(String.format("Execution time - VM selection mean: %.5f sec", executionTimeVmSelectionMean));
                Log.printLine(String.format("Execution time - VM selection stDev: %.5f sec", executionTimeVmSelectionStDev));
                Log.printLine(String.format("Execution time - host selection mean: %.5f sec", executionTimeHostSelectionMean));
                Log.printLine(String.format("Execution time - host selection stDev: %.5f sec", executionTimeHostSelectionStDev));
                Log.printLine(String.format("Execution time - VM reallocation mean: %.5f sec", executionTimeVmReallocationMean));
                Log.printLine(String.format("Execution time - VM reallocation stDev: %.5f sec", executionTimeVmReallocationStDev));
                Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
                Log.printLine(String.format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
            }

            Log.printLine();
        }

        Log.setDisabled(true);
    }

    public static String parseExperimentName(String name) {
        Scanner scanner = new Scanner(name);
        StringBuilder csvName = new StringBuilder();
        scanner.useDelimiter("_");

        for(int i = 0; i < 4; ++i) {
            if (scanner.hasNext()) {
                csvName.append(scanner.next() + ",");
            } else {
                csvName.append(",");
            }
        }

        scanner.close();
        return csvName.toString();
    }

    protected static double getSlaTimePerActiveHost(List<Host> hosts) {
        double slaViolationTimePerHost = 0.0D;
        double totalTime = 0.0D;
        Iterator var5 = hosts.iterator();

        while(var5.hasNext()) {
            Host _host = (Host)var5.next();
            HostDynamicWorkload host = (HostDynamicWorkload)_host;
            double previousTime = -1.0D;
            double previousAllocated = 0.0D;
            double previousRequested = 0.0D;
            boolean previousIsActive = true;

            HostStateHistoryEntry entry;
            for(Iterator var15 = host.getStateHistory().iterator(); var15.hasNext(); previousIsActive = entry.isActive()) {
                entry = (HostStateHistoryEntry)var15.next();
                if (previousTime != -1.0D && previousIsActive) {
                    double timeDiff = entry.getTime() - previousTime;
                    totalTime += timeDiff;
                    if (previousAllocated < previousRequested) {
                        slaViolationTimePerHost += timeDiff;
                    }
                }

                previousAllocated = entry.getAllocatedMips();
                previousRequested = entry.getRequestedMips();
                previousTime = entry.getTime();
            }
        }

        return slaViolationTimePerHost / totalTime;
    }

    protected static double getSlaTimePerHost(List<Host> hosts) {
        double slaViolationTimePerHost = 0.0D;
        double totalTime = 0.0D;
        Iterator var5 = hosts.iterator();

        while(var5.hasNext()) {
            Host _host = (Host)var5.next();
            HostDynamicWorkload host = (HostDynamicWorkload)_host;
            double previousTime = -1.0D;
            double previousAllocated = 0.0D;
            double previousRequested = 0.0D;

            HostStateHistoryEntry entry;
            for(Iterator var14 = host.getStateHistory().iterator(); var14.hasNext(); previousTime = entry.getTime()) {
                entry = (HostStateHistoryEntry)var14.next();
                if (previousTime != -1.0D) {
                    double timeDiff = entry.getTime() - previousTime;
                    totalTime += timeDiff;
                    if (previousAllocated < previousRequested) {
                        slaViolationTimePerHost += timeDiff;
                    }
                }

                previousAllocated = entry.getAllocatedMips();
                previousRequested = entry.getRequestedMips();
            }
        }

        return slaViolationTimePerHost / totalTime;
    }

    protected static Map<String, Double> getSlaMetrics(List<Vm> vms) {
        Map<String, Double> metrics = new HashMap();
        List<Double> slaViolation = new LinkedList();
        double totalAllocated = 0.0D;
        double totalRequested = 0.0D;
        double totalUnderAllocatedDueToMigration = 0.0D;

        double vmUnderAllocatedDueToMigration;
        for(Iterator var9 = vms.iterator(); var9.hasNext(); totalUnderAllocatedDueToMigration += vmUnderAllocatedDueToMigration) {
            Vm vm = (Vm)var9.next();
            double vmTotalAllocated = 0.0D;
            double vmTotalRequested = 0.0D;
            vmUnderAllocatedDueToMigration = 0.0D;
            double previousTime = -1.0D;
            double previousAllocated = 0.0D;
            double previousRequested = 0.0D;
            boolean previousIsInMigration = false;

            VmStateHistoryEntry entry;
            for(Iterator var24 = vm.getStateHistory().iterator(); var24.hasNext(); previousIsInMigration = entry.isInMigration()) {
                entry = (VmStateHistoryEntry)var24.next();
                if (previousTime != -1.0D) {
                    double timeDiff = entry.getTime() - previousTime;
                    vmTotalAllocated += previousAllocated * timeDiff;
                    vmTotalRequested += previousRequested * timeDiff;
                    if (previousAllocated < previousRequested) {
                        slaViolation.add((previousRequested - previousAllocated) / previousRequested);
                        if (previousIsInMigration) {
                            vmUnderAllocatedDueToMigration += (previousRequested - previousAllocated) * timeDiff;
                        }
                    }
                }

                previousAllocated = entry.getAllocatedMips();
                previousRequested = entry.getRequestedMips();
                previousTime = entry.getTime();
            }

            totalAllocated += vmTotalAllocated;
            totalRequested += vmTotalRequested;
        }

        metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
        if (slaViolation.isEmpty()) {
            metrics.put("average", 0.0D);
        } else {
            metrics.put("average", MathUtil.mean(slaViolation));
        }

        metrics.put("underallocated_migration", totalUnderAllocatedDueToMigration / totalRequested);
        return metrics;
    }

    public static void writeDataColumn(List<? extends Number> data, String outputPath) {
        File file = new File(outputPath);

        try {
            file.createNewFile();
        } catch (IOException var6) {
            var6.printStackTrace();
            System.exit(0);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            Iterator var4 = data.iterator();

            while(var4.hasNext()) {
                Number value = (Number)var4.next();
                writer.write(value.toString() + "\n");
            }

            writer.close();
        } catch (IOException var7) {
            var7.printStackTrace();
            System.exit(0);
        }

    }

    public static void writeDataRow(String data, String outputPath) {
        File file = new File(outputPath);

        try {
            file.createNewFile();
        } catch (IOException var5) {
            var5.printStackTrace();
            System.exit(0);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(data);
            writer.close();
        } catch (IOException var4) {
            var4.printStackTrace();
            System.exit(0);
        }

    }

    public static void writeMetricHistory(List<? extends Host> hosts, PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy, String outputPath) {
        for(int j = 0; j < 10; ++j) {
            Host host = (Host)hosts.get(j);
            if (vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
                File file = new File(outputPath + "_" + host.getId() + ".csv");

                try {
                    file.createNewFile();
                } catch (IOException var11) {
                    var11.printStackTrace();
                    System.exit(0);
                }

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    List<Double> timeData = (List)vmAllocationPolicy.getTimeHistory().get(host.getId());
                    List<Double> utilizationData = (List)vmAllocationPolicy.getUtilizationHistory().get(host.getId());
                    List<Double> metricData = (List)vmAllocationPolicy.getMetricHistory().get(host.getId());

                    for(int i = 0; i < timeData.size(); ++i) {
                        writer.write(String.format("%.2f,%.2f,%.2f\n", timeData.get(i), utilizationData.get(i), metricData.get(i)));
                    }

                    writer.close();
                } catch (IOException var12) {
                    var12.printStackTrace();
                    System.exit(0);
                }
            }
        }

    }

    public static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        String indent = "\t";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Resource ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
        DecimalFormat dft = new DecimalFormat("###.##");

        for(int i = 0; i < size; ++i) {
            Cloudlet cloudlet = (Cloudlet)list.get(i);
            Log.print(indent + cloudlet.getCloudletId());
            if (cloudlet.getCloudletStatus() == 4) {
                Log.printLine(indent + "SUCCESS" + indent + indent + cloudlet.getResourceId() + indent + cloudlet.getVmId() + indent + dft.format(cloudlet.getActualCPUTime()) + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }

    public static void printMetricHistory(List<? extends Host> hosts, PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy) {
        for(int i = 0; i < 10; ++i) {
            Host host = (Host)hosts.get(i);
            Log.printLine("Host #" + host.getId());
            Log.printLine("Time:");
            if (vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
                Iterator var4 = ((List)vmAllocationPolicy.getTimeHistory().get(host.getId())).iterator();

                Double metric;
                while(var4.hasNext()) {
                    metric = (Double)var4.next();
                    Log.format("%.2f, ", new Object[]{metric});
                }

                Log.printLine();
                var4 = ((List)vmAllocationPolicy.getUtilizationHistory().get(host.getId())).iterator();

                while(var4.hasNext()) {
                    metric = (Double)var4.next();
                    Log.format("%.2f, ", new Object[]{metric});
                }

                Log.printLine();
                var4 = ((List)vmAllocationPolicy.getMetricHistory().get(host.getId())).iterator();

                while(var4.hasNext()) {
                    metric = (Double)var4.next();
                    Log.format("%.2f, ", new Object[]{metric});
                }

                Log.printLine();
            }
        }

    }
}
