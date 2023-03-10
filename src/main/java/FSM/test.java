package FSM;

import Simulator.MemoryBlock;
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
        String memPath = "D:\\data\\representative\\results\\mem.csv";
        String predictionPath = "D:\\data\\representative\\prediction_results\\predictions.csv";

        int memCapacity = 48 * 1024; //内存池空间 单位：Mb
        CSVUtil util = new CSVUtil(representativeFuncPath,representativeIntermediatePath);


        ContainerScheduler scheduler = new ContainerScheduler(memCapacity, Policy.DSMP,
                representativeInvokePath,repInvokeResPath,
                repContainerResPath,perMinResPath,
                memPath,predictionPath,
                (int) (memCapacity * 0.7));
        util.ReadData(true);
        util.sendDataToSimulator(scheduler);

        scheduler.doMainLoop(1440);


    }

    public static void simulateMultipleTimes(){
        String representativeFuncPath = "D:\\data\\representative\\functions.csv";
        String representativeInvokePath = "D:\\data\\representative\\invokes.csv";
        String representativeIntermediatePath = "D:\\data\\representative\\intermediate.csv";

        String dirPath = "D:\\data\\representative\\results\\";

        String repInvokeResPath = "\\invokeRes";
        String repContainerResPath = "\\container";
        String perMinResPath = "\\perMinute";
        String memPath = "\\mem";

        String predictionPath = "D:\\data\\representative\\prediction_results\\predictions.csv";

        int[] ints = {4,8,16,24,32,34,36,38,40,42,44,46,48};
        Policy[] policies = {Policy.LRU,Policy.SSMP,Policy.DSMP};
        int[] waitTimes = {10};
        for (int waitTime :waitTimes) {
            for (Policy policy :policies) {
                for (int i : ints) {
                    System.out.println("内存大小:" + i + "GB");
                    System.out.println("等待时间:" + waitTime + "ms");
                    System.out.println("采用策略:" + policy);
                    int memCapacity = i * 1024; //内存池空间 单位：Mb
                    MemoryBlock.messageTTL = waitTime;
                    String preFixPath = dirPath + waitTime + "ms\\" + policy.toString();

                    CSVUtil util = new CSVUtil(representativeFuncPath,representativeIntermediatePath);
                    util.ReadData(true);

                    ContainerScheduler scheduler = new ContainerScheduler(memCapacity, policy,representativeInvokePath,
                            preFixPath + repInvokeResPath + i + "G.csv",
                            preFixPath + repContainerResPath + i + "G.csv",
                            preFixPath +perMinResPath + i + "G.csv",
                            preFixPath + memPath + i + "G.csv",
                             predictionPath,
                            (int) (memCapacity * 0.7));
                    util.sendDataToSimulator(scheduler);

                    scheduler.doMainLoop(10);

                }
                System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
                System.out.println();
            }
            System.out.println("----------------------------------------");
            System.out.println();
            System.out.println();
        }


    }
}
