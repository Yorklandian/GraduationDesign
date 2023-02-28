package FSM;

import Simulator.Utils.CSVUtil;
import Simulator.ContainerScheduler;
import Simulator.Enums.Policy;

public class test {


    public static void main(String[] args) {
        //test();
        simulateMultipleTimes();
    }

    public static void test(){

        String representativeFuncPath = "D:\\data\\representative\\functions.csv";
        String representativeInvokePath = "D:\\data\\representative\\invokes.csv";
        String representativeIntermediatePath = "D:\\data\\representative\\intermediate.csv";
        String repInvokeResPath = "D:\\data\\representative\\results\\invokeRes.csv";
        String repContainerResPath = "D:\\data\\representative\\results\\container.csv";
        String perMinResPath = "D:\\data\\representative\\results\\perMinute.csv";

        int memCapacity = 32 * 1024; //内存池空间 单位：Mb
        CSVUtil util = new CSVUtil(representativeFuncPath,representativeIntermediatePath);


        ContainerScheduler scheduler = new ContainerScheduler(memCapacity, Policy.DSMP,representativeInvokePath,repInvokeResPath,repContainerResPath,perMinResPath, (int) (memCapacity * 0.7));
        util.ReadData(true);
        util.sendDataToSimulator(scheduler);

        scheduler.doMainLoop(60);


    }

    public static void simulateMultipleTimes(){
        String representativeFuncPath = "D:\\data\\representative\\functions.csv";
        String representativeInvokePath = "D:\\data\\representative\\invokes.csv";
        String representativeIntermediatePath = "D:\\data\\representative\\intermediate.csv";

        String dirPath = "D:\\data\\representative\\results\\";

        String repInvokeResPath = "\\invokeRes";
        String repContainerResPath = "\\container";
        String perMinResPath = "\\perMinute";

        CSVUtil util = new CSVUtil(representativeFuncPath,representativeIntermediatePath);
        util.ReadData(true);

        int[] ints = {8};
        Policy[] policies = {Policy.LRU,Policy.SSMP,Policy.DSMP};
        for (Policy policy :policies) {
            for (int i : ints) {
                System.out.println("内存大小:" + i + "GB");
                int memCapacity = i * 1024; //内存池空间 单位：Mb

                ContainerScheduler scheduler = new ContainerScheduler(memCapacity, policy,representativeInvokePath,
                        dirPath + policy.toString() + repInvokeResPath + i + "G.csv",
                        dirPath + policy.toString() + repContainerResPath + i + "G.csv",
                        dirPath + policy.toString() +perMinResPath + i + "G.csv",
                        (int) (memCapacity * 0.7));
                util.sendDataToSimulator(scheduler);

                scheduler.doMainLoop(60);

            }
        }

    }
}
