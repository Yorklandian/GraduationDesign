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

        String representativeFuncPath = "E:\\data\\representative\\functions.csv";
        String representativeInvokePath = "E:\\data\\representative\\invokes.csv";
        String representativeIntermediatePath = "E:\\data\\representative\\intermediate.csv";
        String repInvokeResPath = "E:\\data\\representative\\results\\invokeRes.csv";
        String repContainerResPath = "E:\\data\\representative\\results\\container.csv";
        String perMinResPath = "E:\\data\\representative\\results\\perMinute.csv";

        int memCapacity = 64 * 1024; //内存池空间 单位：Mb
        CSVUtil util = new CSVUtil(representativeFuncPath,representativeIntermediatePath);


        ContainerScheduler scheduler = new ContainerScheduler(memCapacity, Policy.DSMP,representativeInvokePath,repInvokeResPath,repContainerResPath,perMinResPath, (int) (memCapacity * 0.7));
        util.ReadData(true);
        util.sendDataToSimulator(scheduler);

        scheduler.doMainLoop(1440);


    }

    public static void simulateMultipleTimes(){
        String representativeFuncPath = "E:\\data\\representative\\functions.csv";
        String representativeInvokePath = "E:\\data\\representative\\invokes.csv";
        String representativeIntermediatePath = "E:\\data\\representative\\intermediate.csv";



        String repInvokeResPath = "E:\\data\\representative\\results\\invokeRes";
        String repContainerResPath = "E:\\data\\representative\\results\\container";
        String perMinResPath = "E:\\data\\representative\\results\\perMinute";

        CSVUtil util = new CSVUtil(representativeFuncPath,representativeIntermediatePath);
        util.ReadData(true);

        int[] ints = {8,16,24,32,48};
        for (int i : ints) {
            System.out.println("内存大小:" + i + "GB");
            int memCapacity = i * 1024; //内存池空间 单位：Mb

            ContainerScheduler scheduler = new ContainerScheduler(memCapacity, Policy.DSMP,representativeInvokePath,
                    repInvokeResPath + i + "G.csv",repContainerResPath + i + "G.csv",perMinResPath + i + "G.csv",
                    (int) (memCapacity * 0.7));
            util.sendDataToSimulator(scheduler);

            scheduler.doMainLoop(1440);

        }
    }
}
