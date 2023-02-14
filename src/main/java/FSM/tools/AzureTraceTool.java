package FSM.tools;

import FSM.ItemSet;
import FSM.Sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class AzureTraceTool implements DataProcessTool{
    private String traceFilePath;
    private String outPutFilePath;
    private File traceFile;
    private File outputFile;

    //时间限制，用于划分sequence
    private int timeLimit = 10;

    private Map<String,AzureApp> appMap = new HashMap<>();



    static Integer itemID = 1;
    static Integer transactionID = 1;

    public AzureTraceTool(String traceFilePath,String outPutFilePath){
        this.traceFilePath = traceFilePath;
        this.outPutFilePath = outPutFilePath;
        this.traceFile = new File(traceFilePath);
        this.outputFile = new File(outPutFilePath);

    }


    @Override
    public void readFileFromLog() {
        DecimalFormat df = new DecimalFormat("0.000");

        try(BufferedReader br = new BufferedReader(new FileReader(this.traceFile))){
            String content;
            double lastTime = -1;
            String lastEntityName = null;
            String lastAppID = null;
            ItemSet lastSet = null;
            while((content = br.readLine()) != null){
                String[] strings = content.split(",");
                if(Objects.equals(strings[0], "app")){
                    continue;
                }
                String appID = strings[0];
                String funcId = strings[1];
                String entity = appID + funcId;
                double endTime = Double.parseDouble(strings[2]);
                double duration = Double.parseDouble(strings[3]);
                //double invokeTime = endTime - duration;
                double invokeTime = Double.parseDouble(df.format(endTime - duration));
                if(!appMap.containsKey(appID)){
                    AzureApp app = new AzureApp(appID);
                    appMap.put(appID,app);
                }


                //先只处理一个app试试
                if(appMap.containsKey(appID)){
                    List<ItemSet> itemSetList = appMap.get(appID).getItemSetList();
                    Map<String,Integer> entity2IDMap = appMap.get(appID).getEntity2IdMap();
                    Map<Integer,String> id2EntityMap = appMap.get(appID).getId2EntityMap();

                    // 连续的相同数据（时间与entity都相同），视作一次调用，什么都不做
                    if(Objects.equals(entity, lastEntityName) && invokeTime == lastTime){
                        continue;
                    }

                    if(invokeTime == lastTime && Objects.equals(appID, lastAppID)){
                        if(entity2IDMap.containsKey(entity)){               //无需更新id
                            Integer id = entity2IDMap.get(entity);
                            lastSet.addItem(id);
                        } else {                                            //更新id
                            lastSet.addItem(itemID);

                            entity2IDMap.put(entity, itemID);
                            id2EntityMap.put(itemID,entity);
                            //id更新暂时使用单调递增
                            itemID++;
                        }
                    } else {
                        if(entity2IDMap.containsKey(entity)){               //无需更新map,id
                            Integer id = entity2IDMap.get(entity);
                            ItemSet itemSet = new ItemSet(id,invokeTime);
                            itemSetList.add(itemSet);
                            lastSet = itemSet;
                        } else {
                            ItemSet itemSet = new ItemSet(itemID, invokeTime); // 需要更新map,id
                            itemSetList.add(itemSet);

                            entity2IDMap.put(entity, itemID);
                            id2EntityMap.put(itemID,entity);
                            lastSet = itemSet;
                            itemID++;
                        }
                    }
                    lastEntityName = entity;
                    lastTime = invokeTime;
                    lastAppID = appID;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        readFileFromLog();
        for (String appID :appMap.keySet()) {
            AzureApp app = appMap.get(appID);
            generateSequence(appID);
            app.doFilter();
        }
        //输出下每个sequence中的ItemSet
        /*for(Sequence seq: sequenceList){
            System.out.println("----------------------------------");
            for(ItemSet set: seq.getItemSetList()){
                System.out.println("set:" + set.getItems().toString() +" " + set.getTime());
            }
        }*/
    }

    @Override
    public void outputToFile(List<Sequence> resultSequenceList) {

    }

    @Override
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public void generateSequences(List<ItemSet> itemSetList, List<Sequence> sequenceList) {
        double lastTime = -1.0;
        Sequence sequence = new Sequence(transactionID);

        for (int i = 0; i < itemSetList.size(); i++) {
            ItemSet itemSet = itemSetList.get(i);
            double time = itemSet.getTime();

            if(i == 0){
                sequence.addItemSet(itemSet);
                lastTime = time;
                continue;
            }

            //TODO: 根据实际情况设置更加灵活的事务划分机制
            if(time - lastTime > timeLimit){
                transactionID++;
                sequenceList.add(sequence);
                sequence = new Sequence(transactionID);
                sequence.addItemSet(itemSet);
            }
            else {
                sequence.addItemSet(itemSet);
            }
            lastTime = time;
        }
        sequenceList.add(sequence);
    }

    public void generateSequence(String appName){
        List<ItemSet> itemSetList = appMap.get(appName).getItemSetList();
        List<Sequence> sequenceList = appMap.get(appName).getSequenceList();
        generateSequences(itemSetList,sequenceList);
    }



    @Override
    public List<Sequence> getSequenceList() {
        //暂时先只返回第一个app
        return appMap.get("7b2c43a2bc30f6bb438074df88b603d2cb982d3e7961de05270735055950a568").getSequenceList();
    }

    @Override
    public List<Sequence> getSequenceList(String appID){
        return appMap.get(appID).getSequenceList();
    }


    public Map<String, AzureApp> getAppMap() {
        return appMap;
    }
}
