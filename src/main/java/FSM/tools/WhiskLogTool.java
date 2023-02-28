package FSM.tools;

import FSM.ItemSet;
import FSM.Sequence;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WhiskLogTool extends DataProcessTool {
    //日志文件路径
    private String logFilePath;
    //输出文件路径
    private String outputFilePath;
    //日志文件
    private File logFile;
    //输出文件
    private File outputFile;
    //时间限制，暂时使用时间限制作为序列划分的依据
    private int timeLimit = 10;

    //保存 数字-Action名称 的映射
    private Map<Integer,String> id2EntityMap = new HashMap<>();
    //保存 Action名称-数字 的映射
    private Map<String,Integer> entity2IDMap = new HashMap<>();
    //保存所有的itemSets
    private List<ItemSet> itemSetList = new ArrayList<>();
    //保存所有的Sequences
    private List<Sequence> sequenceList = new ArrayList<>();

    static Integer itemID = 1;
    static Integer transactionID = 1;

    public WhiskLogTool(String logFilePath, String outputFilePath) {
        this.logFilePath = logFilePath;
        this.outputFilePath = outputFilePath;
        this.logFile = new File(logFilePath);
        this.outputFile = new File(outputFilePath);
        this.start();
    }

    /**
     * 读取日志文件并从中生成事务序列
     */
    public void start(){
        readFileFromLog();
        generateSequences(this.itemSetList, this.sequenceList);
        //输出下每个sequence中的ItemSet
        for(Sequence seq: sequenceList){
            System.out.println("----------------------------------");
            for(ItemSet set: seq.getItemSetList()){
                System.out.println("set:" + set.getItems().toString() +" " + set.getTime());
            }
        }
    }

    /**
     * 读取文件，生成itemList列表以及id-entity映射
     */
    public void readFileFromLog(){
        try(BufferedReader br = new BufferedReader(new FileReader(logFile))){
            String str;
            String lastEntityName = null;
            ItemSet lastSet = null;
            long lastTime = -1;

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while ((str = br.readLine()) != null){
                String[] strings = str.split("\\s+");
                String dateStr = strings[0];
                String timeStr = strings[1];
                Date date = dateFormatter.parse(dateStr + " " + timeStr);
                //int timeInSec = Integer.parseInt(strings[0]);
                long timeInSec =  (date.getTime()/1000);
                String entity = strings[strings.length-1];

                // 连续的相同数据（时间与entity都相同），视作一次调用，什么都不做
                if(Objects.equals(entity, lastEntityName) && timeInSec == lastTime){
                    continue;
                }

                //时间相同的两次不同调用，视作处在同一itemSet中,不生成新的itemSet
                if(timeInSec == lastTime ){
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
                        ItemSet itemSet = new ItemSet(id, (double) timeInSec);
                        itemSetList.add(itemSet);
                        lastSet = itemSet;
                    } else {
                        ItemSet itemSet = new ItemSet(itemID, (double) timeInSec); // 需要更新map,id
                        itemSetList.add(itemSet);

                        entity2IDMap.put(entity, itemID);
                        id2EntityMap.put(itemID,entity);
                        lastSet = itemSet;
                        itemID++;
                    }
                }
                lastEntityName = entity;
                lastTime = timeInSec;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 由itemSet列表生成Sequences,目前默认找到两个时间相差10s以上则认为是两个事务
     */
    public void generateSequences(List<ItemSet> itemSetList, List<Sequence> sequenceList){
        double lastTime = -1;
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

    public void outputToFile(List<Sequence> resultSequenceList){
        try(BufferedWriter br = new BufferedWriter(new FileWriter(this.outputFile))){
            for(Sequence seq: resultSequenceList){
                br.write("<");
                for(ItemSet itemSet: seq.getItemSetList()){
                    br.write("(");
                    for(int num: itemSet.getItems()){
                        br.write(id2EntityMap.get(num) + ", ");
                    }
                    br.write(")");
                }
                br.write(">  " + "support: " + seq.getSupport());
                br.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public Map<Integer, String> getId2EntityMap() {
        return id2EntityMap;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setId2EntityMap(Map<Integer, String> id2EntityMap) {
        this.id2EntityMap = id2EntityMap;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public List<Sequence> getSequenceList() {
        return sequenceList;
    }

    @Override
    public List<Sequence> getSequenceList(String appID) {
        return null;
    }

    public void setSequenceList(List<Sequence> sequenceList) {
        this.sequenceList = sequenceList;
    }

}
