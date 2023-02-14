package FSM;


import FSM.GSP.GSPTool;
import FSM.Output.AzureExcelWriter;
import FSM.tools.AzureApp;
import FSM.tools.AzureTraceTool;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GSP序列模式分析算法
 */
public class Client {
    public static void main(String[] args){
        String filePath = "C:\\Users\\Administrator\\Desktop\\Cloud\\test.txt";

        String AzurefilePath = "C:\\Users\\Administrator\\Desktop\\Cloud\\AzureFunctionsInvocationTraceForTwoWeeksJan2021\\AzureFunctionsInvocationTraceForTwoWeeksJan2021.txt";

        String outputFilePath = "C:\\Users\\Administrator\\Desktop\\Cloud\\output.txt";

        ExecutorService es = Executors.newFixedThreadPool(10);
        //最小支持度阈值
        int minSupportCount = 10;
        //时间最小间隔
        double min_gap = 0.01;
        //施加最大间隔
        double max_gap = 1000.0;

        List<AzureApp> appList = new ArrayList<>();

        AtomicInteger longestFSlength = new AtomicInteger();

        AzureTraceTool dataTool = new AzureTraceTool(AzurefilePath,outputFilePath);
        dataTool.setTimeLimit(10000);

        GSPTool tool = new GSPTool(minSupportCount, min_gap, max_gap, dataTool);
        System.out.println(dataTool.getAppMap().keySet().size());
        int counter = 1;
        for (String name : dataTool.getAppMap().keySet()) {
            AzureApp app = dataTool.getAppMap().get(name);
            System.out.println("app id: " + name +"  App NO：: "+ counter);
            System.out.println("app itemSet list length: " + app.getItemSetList().size());
            System.out.println("app sequence list length: " + app.getSequenceList().size());
            System.out.println("app entity num:" + app.getEntity2IdMap().size());
            counter++;

            if(!app.isValid()){
                System.out.println("app fail to pass filter, no need to process");
                System.out.println("-------------------------");
                continue;
            }

            appList.add(app);
            //Too big 20000+ items???
            //341a2b2b3affc84b61502aa373df22557db55c7c304014b3ac84499247374f43 10000+,still jam
            // got it :prymaid!
            /*if(Objects.equals(name, "6e42a4e8ea1df408e059ba37d0e04255fd8385235b310265a57091132d760a65") || Objects.equals(name, "5fb02cfeb979fec16df02d1ece227157238477b19fc6e37554e2e2af6018d3ba")
                    || Objects.equals(name, "341a2b2b3affc84b61502aa373df22557db55c7c304014b3ac84499247374f43") || Objects.equals(name, "4b22cc3e1988174edd67d91fd7a82a9977c1719601065e5740d54074a6910170")
                    || Objects.equals(name, "70b9cea7ca266637479483f517194c402dfe99b5fc2357e6ebac5e715c9a34a2") || Objects.equals(name, "7958f89676bf3653f72cda8f38f6b3c0574dcae5169824a571b6996732dd1425")
                    || Objects.equals(name, "a594f92f84072b4cd031fe5283d1781a6e98f430696dec0a8e3b02eadb5fc0b8") || Objects.equals(name, "85479ef37b5dc75dd5aeca3bab499129b97a134dac5d740d2c68941de9d63031")
                    || Objects.equals(name, "06da275043bac5526d5c2252a4daa222bb062165977f111b693ed8d335917291")){
                System.out.println("skip!!!!!!!!!!!!!!!!");
                System.out.println("------------------------");
                continue;
            }

            tool.setOriginSequences(dataTool.getSequenceList(name));
            boolean res = tool.gspCalculate();

            List<Sequence> resSequenceList = new ArrayList<>();
            CollectionUtils.addAll(resSequenceList,tool.getTotalFrequencySeqs());
            Collections.copy(resSequenceList,tool.getTotalFrequencySeqs());
            app.setFrequentSequenceList(resSequenceList);

            if(tool.getTotalFrequencySeqs().size() > longestFSlength.get()){
                longestFSlength.set(tool.getTotalFrequencySeqs().size());
            }

            tool.clear();*/


            Future<Boolean> future = es.submit(() ->{
                tool.setOriginSequences(dataTool.getSequenceList(name));
                boolean res = tool.gspCalculate();
                List<Sequence> resSequenceList = new ArrayList<>();
                CollectionUtils.addAll(resSequenceList,tool.getTotalFrequencySeqs());
                Collections.copy(resSequenceList,tool.getTotalFrequencySeqs());
                app.setFrequentSequenceList(resSequenceList);

                if(tool.getTotalFrequencySeqs().size() > longestFSlength.get()){
                    longestFSlength.set(tool.getTotalFrequencySeqs().size());
                }

                tool.clear();
                return res;
            });
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.out.println("Too much time cost!");
            } finally {
                tool.clear();
            }

            System.out.println("-------------------------");
        }
        es.shutdown();
        System.out.println(longestFSlength);



        AzureExcelWriter writer = new AzureExcelWriter("C:\\Users\\Administrator\\Desktop\\Cloud\\output.xlsx",appList);
        writer.writeToExcel();

    }

    private void doFilter(){

    }
}