package Simulator;

import Simulator.Utils.CSVUtil;
import Simulator.Enums.Policy;

public class test {


    public static void main(String[] args) {
        test();
        //simulateMultipleTimes();
    }

    public static void test(){

        String representativeFuncPath = "D:\\data\\representative\\functions.csv";
        String representativeInvokePath = "D:\\data\\representative\\invokes.csv";
        String repPredictionPath = "D:\\data\\representative\\prediction_results\\predictions.csv";
        String repInvokeResPath = "D:\\data\\representative\\results\\invokeRes.csv";
        String repContainerResPath = "D:\\data\\representative\\results\\container.csv";
        String repPerMinResPath = "D:\\data\\representative\\results\\perMinute.csv";
        String repMemPath = "D:\\data\\representative\\results\\mem.csv";


        String rareFuncPath = "D:\\data\\rare\\functions.csv";
        String rareInvokePath = "D:\\data\\rare\\invokes.csv";
        String rarePredictionPath = "D:\\data\\rare\\prediction_results\\predictions.csv";
        String rareInvokeResPath = "D:\\data\\rare\\results\\invokeRes.csv";
        String rareContainerResPath = "D:\\data\\rare\\results\\container.csv";
        String rarePerMinResPath = "D:\\data\\rare\\results\\perMinute.csv";
        String rareMemPath = "D:\\data\\rare\\results\\mem.csv";

        String freqFuncPath = "D:\\data\\frequent\\functions.csv";
        String freqInvokePath = "D:\\data\\frequent\\invokes.csv";
        String freqPredictionPath = "D:\\data\\frequent\\prediction_results\\predictions.csv";
        String freqInvokeResPath = "D:\\data\\frequent\\results\\invokeRes.csv";
        String freqContainerResPath = "D:\\data\\frequent\\results\\container.csv";
        String freqPerMinResPath = "D:\\data\\frequent\\results\\perMinute.csv";
        String freqMemPath = "D:\\data\\frequent\\results\\mem.csv";




        int memCapacity = 16 * 1024; //内存池空间 单位：Mb
        /*CSVUtil util = new CSVUtil(representativeFuncPath,predictionPath);
        ContainerScheduler scheduler = new ContainerScheduler(memCapacity, Policy.LRU,
                representativeInvokePath,repInvokeResPath,
                repContainerResPath,perMinResPath,
                memPath, (int) (memCapacity * 0.7));*/


        /*CSVUtil util = new CSVUtil(rareFuncPath,rarePredictionPath);
        ContainerScheduler scheduler = new ContainerScheduler(memCapacity, Policy.LRU,
                rareInvokePath,rareInvokeResPath,
                rareContainerResPath,rarePerMinResPath,
                rareMemPath, (int) (memCapacity * 0.7));*/

        CSVUtil util = new CSVUtil(freqFuncPath,freqPredictionPath);
        ContainerScheduler scheduler = new ContainerScheduler(memCapacity, Policy.DSMP,
                freqInvokePath,freqInvokeResPath,
                freqContainerResPath,freqPerMinResPath,
                freqMemPath, (int) (memCapacity * 0.7));


        util.readData(true);
        util.sendDataToSimulator(scheduler);
        boolean simpleAllocate = false;
        scheduler.doMainLoop(1440, simpleAllocate);


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

        //int[] ints = {4,8,16,24,32,34,36,38,40,42,44,46,48};
        int[] ints = {8};
        Policy[] policies = {Policy.LRU,/*Policy.SSMP*//*,Policy.DSMP*/};
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

                    CSVUtil util = new CSVUtil(representativeFuncPath,predictionPath);
                    util.readData(true);

                    ContainerScheduler scheduler = new ContainerScheduler(memCapacity, policy,representativeInvokePath,
                            preFixPath + repInvokeResPath + i + "G.csv",
                            preFixPath + repContainerResPath + i + "G.csv",
                            preFixPath +perMinResPath + i + "G.csv",
                            preFixPath + memPath + i + "G.csv",
                            (int) (memCapacity * 0.7));
                    util.sendDataToSimulator(scheduler);
                    boolean simpleAllocate = true;
                    scheduler.doMainLoop(1440, simpleAllocate);

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
