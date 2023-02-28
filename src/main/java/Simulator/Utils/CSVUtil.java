package Simulator.Utils;

import Simulator.ContainerScheduler;
import Simulator.Function;
import Simulator.Record.ContainerRecord;
import Simulator.Record.InvokeResultPerMinute;
import Simulator.Record.InvokeResultRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CSVUtil {
    private String dataPath;
    private String intermediateRecordPath;


    private Map<String,Function> nameToFunctionMap = new HashMap<>();
    private List<String> highCostFunctionNameList = new ArrayList<>();

    public CSVUtil(String dataPath, String intermediateRecordPath) {
        this.dataPath = dataPath;
        this.intermediateRecordPath = intermediateRecordPath;
    }


    public void ReadData(boolean generateMapOnly){
        int iCount = 0;
        int fCount = 0;
        try(BufferedReader br = new BufferedReader(new FileReader(this.dataPath));
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.intermediateRecordPath))){
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withAllowDuplicateHeaderNames().parse(br);
            CSVPrinter printer = new CSVPrinter(bw,CSVFormat.DEFAULT.withHeader("name","time"));

            //遍历所有record
            for (CSVRecord record :parser) {
                if(fCount >= 4000 ){
                    printer.close();
                    break;
                }
                String hashApp = record.get("HashApp");
                String hashFunc = record.get("HashFunction");
                int mem = (int) Float.parseFloat(record.get("divvied"));
                int warmStartTime = (int) Float.parseFloat(record.get("Average"));
                int coldStartTime = (int) Float.parseFloat(record.get("Maximum"));
                int invocationCount = (int) Float.parseFloat(record.get("Count"));

                //生成function
                Function function = new Function(mem,coldStartTime,warmStartTime,hashFunc,hashApp,invocationCount);
                if(!nameToFunctionMap.containsKey(hashFunc)){
                    nameToFunctionMap.put(hashFunc,function);
                }

                //存储高占用的function list
                if(FunctionJudge.isFunctionCostHigh(function)){
                    highCostFunctionNameList.add(hashFunc);
                }


                //是否生成调用list,若generateMapOnly为true,则不生成(由python脚本负责生成调用list)
                if(!generateMapOnly){
                    //生成此function的invoke list
                    for (int minute = 1; minute <= 1440 ; minute++) {
                        int startTime = (minute-1) * 60 * 1000;
                        int invokeCount = (int) Float.parseFloat(record.get( minute + "" ));
                        if(invokeCount == 0){
                            continue;
                        } else if(invokeCount == 1){
                            iCount++;
                            printer.printRecord(hashFunc,startTime);
                        } else {
                            int gap = (60*1000)/invokeCount;
                            for (int i = 0; i < invokeCount; i++) {
                                iCount++;
                                printer.printRecord(hashFunc,startTime + i*gap);
                                //invokeList.add(new FunctionInvoke(hashFunc,startTime+i*gap));
                            }
                        }
                    }
                }

                fCount++;
            }
            //将function names 按照sore从大到小排序
            highCostFunctionNameList.sort(((name1, name2) -> {
                Function func1 = nameToFunctionMap.get(name1);
                Function func2 = nameToFunctionMap.get(name2);
                int score1 = func1.getScore();
                int score2 = func2.getScore();
                return score2 - score1;

            }));
            System.out.println("high cost func num: " + highCostFunctionNameList.size());
            for (String name : highCostFunctionNameList) {
                System.out.println("high cost func name: " + name);
            }
            printer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendDataToSimulator(ContainerScheduler simulator){
        simulator.setNameToFunctionMap(this.getNameToFunctionMap());
        simulator.setHighCostFunctionNameList(this.highCostFunctionNameList);
    }


    public static void writeSimulationResults(String resPath, Map<String, InvokeResultRecord> resMap){
        int coldTotalTime = 0;
        int warmTotalTime = 0;
        int queueFullDropTotalTime = 0;
        int ttlDropTotalTime = 0;
        File file = new File(resPath);
        if(file.exists()){
            file.delete();
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(resPath))){
            CSVPrinter printer = new CSVPrinter(bw,CSVFormat.DEFAULT.withHeader("name","warm","cold","queue full drop","ttl drop"));
            for (String name :resMap.keySet()) {
                InvokeResultRecord invokeResultRecord = resMap.get(name);
                int warmCount = invokeResultRecord.getWarmStartTime();
                int coldCount = invokeResultRecord.getColdStartTime();
                int queueFullDropTime = invokeResultRecord.getQueueFullDropTime();
                int ttlDropTime = invokeResultRecord.getTtlDropTime();
                printer.printRecord(name,warmCount,coldCount,queueFullDropTime,ttlDropTime);

                warmTotalTime += warmCount;
                coldTotalTime += coldCount;
                queueFullDropTotalTime += queueFullDropTime;
                ttlDropTotalTime += ttlDropTime;
            }

            System.out.println("total warm/cold/queue full drop/ttl drop count: " + warmTotalTime + "/ " + coldTotalTime + "/ " + queueFullDropTotalTime + "/ " + ttlDropTotalTime);
            System.out.println("total invoke count: " + (warmTotalTime + coldTotalTime + queueFullDropTotalTime + ttlDropTotalTime));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeContainerRecords(String resPath, Map<Integer, ContainerRecord> resMap){
        File file = new File(resPath);
        if(file.exists()){
            file.delete();
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(resPath))){
            CSVPrinter printer = new CSVPrinter(bw,CSVFormat.DEFAULT.withHeader("minute","auto_die","evict"));
            for (int minute :resMap.keySet()) {
                ContainerRecord record = resMap.get(minute);
                int autoDie = record.getAutoDieCount();
                int evict = record.getEvictCount();
                printer.printRecord(minute,autoDie,evict);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeSimulationResultsPerMinute(String resPath, Map<String, InvokeResultPerMinute> resMap){
        File file = new File(resPath);
        if(file.exists()){
            file.delete();
        }
        List<String> headerList = new ArrayList<>();
        headerList.add("name");
        headerList.add("state");
        for (int i = 0; i < 1440; i++) {
            headerList.add(String.valueOf(i));
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(resPath))){
            CSVPrinter printer = new CSVPrinter(bw,CSVFormat.DEFAULT.withHeader(headerList.toArray(new String[0])));
            for (String name :resMap.keySet()) {
                InvokeResultPerMinute ir = resMap.get(name);
                List<String> record1 = new ArrayList<>();
                List<String> record2 = new ArrayList<>();
                List<String> record3 = new ArrayList<>();
                List<String> record4 = new ArrayList<>();
                record1.add(ir.getName());
                record1.add("Warm");
                record1.addAll(ir.getWarmList().stream().map(String::valueOf).collect(Collectors.toList()));
                printer.printRecord(record1);


                record2.add(ir.getName());
                record2.add("Cold");
                record2.addAll(ir.getColdList().stream().map(String::valueOf).collect(Collectors.toList()));
                printer.printRecord(record2);


                record3.add(ir.getName());
                record3.add("QueueFullDrop");
                record3.addAll(ir.getQueueFullList().stream().map(String::valueOf).collect(Collectors.toList()));
                printer.printRecord(record3);

                record4.add(ir.getName());
                record4.add("TTLDrop");
                record4.addAll(ir.getTTlList().stream().map(String::valueOf).collect(Collectors.toList()));
                printer.printRecord(record4);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public Map<String, Function> getNameToFunctionMap() {
        return nameToFunctionMap;
    }

    public void setNameToFunctionMap(Map<String, Function> nameToFunctionMap) {
        this.nameToFunctionMap = nameToFunctionMap;
    }

    public List<String> getHighCostFunctionNameList() {
        return highCostFunctionNameList;
    }

    public void setHighCostFunctionNameList(List<String> highCostFunctionNameList) {
        this.highCostFunctionNameList = highCostFunctionNameList;
    }
}
