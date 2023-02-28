package FSM.Algorithm;


import FSM.Filters.SequenceFilter;
import FSM.ItemSet;
import FSM.Sequence;
import FSM.tools.DataProcessTool;

import java.io.*;
import java.util.*;

/**
 * PreFixSpan 频繁序列模式挖掘算法 TODO：时间约束
 */
public class PreFixSpanTool {

    //测试文件地址
    private String filePath;

    // 最小支持度阈值
    private int minSupportCount;
    // 时间最小间隔
    private double min_gap;
    // 时间最大间隔
    private double max_gap;
    // 原始数据序列
    private List<Sequence> originSequences = new ArrayList<>();
    // 算法中产生的所有的频繁项集序列
    private List<Sequence> totalFrequencySeqs = new ArrayList<>();
    //保存搜友频繁的单item
    private List<Integer> singleItems = new ArrayList<>();

    DataProcessTool dataProcessTool;

    public PreFixSpanTool(String filePath, int minSupport) {
        this.filePath = filePath;
        this.minSupportCount = minSupport;
        readDataFile();
    }

    public PreFixSpanTool(int minSupportCount,DataProcessTool dataProcessTool){
        this.minSupportCount = minSupportCount;
        this.dataProcessTool = dataProcessTool;

    }
    /**
     * 进行计算
     */
    public boolean preFixSpanCalculate(){
        Sequence seq;
        Sequence tempSeq;
        List<Sequence> tempSeqList;
        ItemSet itemSet;
        removeInitSequencesItem();

        for (Integer s : singleItems) {
            // 从最开始的a,b,d开始递归往下寻找频繁序列模式
            seq = new Sequence();
            itemSet = new ItemSet(s,0);
            seq.getItemSetList().add(itemSet);

            if (isLargerThanMinSupport(s, originSequences)) {
                //生成后缀数据库
                tempSeqList = new ArrayList<>();
                for (Sequence s2 : originSequences) {
                    // 判断单一项是否包含于在序列中，包含才进行提取操作
                    if (s2.itemIsContained(s)) {
                        tempSeq = s2.extractItem(s);
                        if(!tempSeq.isEmpty()){
                            tempSeqList.add(tempSeq);
                        }
                    }
                }

                totalFrequencySeqs.add(seq);
                recursiveSearchSequences(seq, tempSeqList);
            }
        }
        this.doFilter();
        printTotalFreSeqs();
        return true;
    }

    public void clear(){
        this.originSequences.clear();
        this.totalFrequencySeqs.clear();
        this.singleItems.clear();
    }

    /**
     * 删除初始序列中不满足支持度计数的item
     */
    private void removeInitSequencesItem() {
        int count = 0;
        //key: item    value: count
        Map<Integer, Integer> itemMap = new HashMap<>();
        singleItems = new ArrayList<>();

        for (Sequence seq : originSequences) {
            for (ItemSet itemSet : seq.getItemSetList()) {
                for (Integer s : itemSet.getItems()) {
                    if (!itemMap.containsKey(s)) {
                        itemMap.put(s, 1);
                    }
                }
            }
        }

        Integer key;
        for (Map.Entry entry : itemMap.entrySet()) {
            count = 0;
            key = (Integer) entry.getKey();
            for (Sequence seq : originSequences) {
                if (seq.itemIsContained(key)) {
                    count++;
                }
            }

            itemMap.put(key, count);

        }

        for (Map.Entry entry : itemMap.entrySet()) {
            key = (Integer) entry.getKey();
            count = (int) entry.getValue();

            if (count < minSupportCount) {
                // 如果支持度阈值小于所得的最小支持度阈值，则删除该项
                for (Sequence seq : originSequences) {
                    seq.deleteSingleItem(key);
                }
            } else {
                singleItems.add(key);
            }
        }

        Collections.sort(singleItems);
    }

    /**
     * 递归搜索满足条件的序列模式
     * @param beforeSeq 前缀序列
     * @param afterSeqList 后缀序列数组，即前缀投影
     */
    private void recursiveSearchSequences(Sequence beforeSeq,List<Sequence> afterSeqList) {
        ItemSet tempItemSet;
        Sequence tempSeq;
        Sequence tempSeq2;
        List<Sequence> tempSeqList;

        for (int item : singleItems) {
            // 分成2种形式递归，以<a>为起始项，第一种直接加入独立项集遍历<a,a>,<a,b> <a,c>..
            if (isLargerThanMinSupport(item, afterSeqList)) {
                tempSeq = beforeSeq.copySequence();
                tempItemSet = new ItemSet(item,0);
                tempSeq.getItemSetList().add(tempItemSet);

                totalFrequencySeqs.add(tempSeq);

                //生成后缀数据库
                tempSeqList = new ArrayList<>();
                for (Sequence seq : afterSeqList) {
                    if (seq.itemIsContained(item)) {
                        tempSeq2 = seq.extractItem(item);
                        if(!tempSeq2.isEmpty()){
                            tempSeqList.add(tempSeq2);
                        }
                    }
                }

                recursiveSearchSequences(tempSeq, tempSeqList);
            }

            // 第二种递归为以元素的身份加入最后的项集内以a为例<(aa)>,<(ab)>,<(ac)>...
            // a在这里可以理解为一个前缀序列，里面可能是单个元素或者已经是多元素的项集
            tempSeq = beforeSeq.copySequence();
            int size = tempSeq.getItemSetList().size();
            tempItemSet = tempSeq.getItemSetList().get(size - 1);
            tempItemSet.getItems().add(item);

            if (isLargerThanMinSupport(tempItemSet, afterSeqList)) {
                tempSeqList = new ArrayList<>();
                for (Sequence seq : afterSeqList) {
                    if (seq.componentItemIsContained(tempItemSet)) {
                        tempSeq2 = seq.extractComponentItem(tempItemSet.getItems());
                        if(!tempSeq2.isEmpty()){
                            tempSeqList.add(tempSeq2);
                        }
                    }
                }
                totalFrequencySeqs.add(tempSeq);

                recursiveSearchSequences(tempSeq, tempSeqList);
            }
        }
    }


    /**
     * 判断单一item是否满足最小支持度
     * @param item 待判断的item
     * @param sequenceList 所有sequence
     * @return boolean
     */
    private boolean isLargerThanMinSupport(int item, List<Sequence> sequenceList){
        boolean isLarger = false;
        int count = 0;
        if(sequenceList == null){
            return false;
        }
        for (Sequence seq: sequenceList) {
            if(seq.itemIsContained(item)){
                count++;
            }
        }
        if(count >= minSupportCount){
            isLarger = true;
        }
        return isLarger;
    }

    /**
     * 判断传入的组合项集如(1,2)在所有序列中是否满足最小支持度
     * @param itemSet 待判断项集
     * @param sequenceList 所有sequence
     * @return boolean
     */
    private boolean isLargerThanMinSupport(ItemSet itemSet, List<Sequence> sequenceList){
        boolean isLarger = false;
        int count = 0;
        if(sequenceList == null){
            return false;
        }
        for (Sequence seq: sequenceList) {
            if(seq.componentItemIsContained(itemSet)){
                count++;
            }
        }

        if (count >= minSupportCount){
            isLarger = true;
        }
        return isLarger;
    }

    /**
     * 过滤结果
     */
    public void doFilter(){
//        SequenceFilter.filterToMaxFreqPatterns(this.getTotalFrequencySeqs());
//        SequenceFilter.filterOneItemSequences(this.getTotalFrequencySeqs());
//        SequenceFilter.filterReplicateSequence(this.getTotalFrequencySeqs());
//        SequenceFilter.filterUnbalanceSequences(this.getTotalFrequencySeqs());
        SequenceFilter.doAllFilter(this.getTotalFrequencySeqs());

    }

    private void clearEmptySequences(List<Sequence> sequenceList){
        sequenceList.removeIf(Sequence::isEmpty);
    }

    //tests
    private void readDataFile() {
        File file = new File(filePath);
        List<String[]> dataArray = new ArrayList<>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            String[] tempArray;
            while ((str = in.readLine()) != null) {
                tempArray = str.split(" ");
                dataArray.add(tempArray);
            }
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }

        minSupportCount = 2;
        originSequences = new ArrayList<>();
        totalFrequencySeqs = new ArrayList<>();
        Sequence tempSeq;
        ItemSet tempItemSet;
//        for (String[] str : dataArray) {
//            tempSeq = new Sequence();
//            for (String s : str) {
//                System.out.println(Integer.parseInt(s));
//                tempItemSet = new ItemSet(Integer.parseInt(s),0);
//                tempSeq.getItemSetList().add(tempItemSet);
//            }
//            originSequences.add(tempSeq);
//        }
        for (String[] strs: dataArray) {
            tempSeq = new Sequence();
            for (String s : strs) {
                if(s.contains(",")){
                    tempItemSet = new ItemSet(s.split(","));
                } else {
                    tempItemSet = new ItemSet(Integer.parseInt(s),0);
                }
                tempSeq.addItemSet(tempItemSet);
            }
            originSequences.add(tempSeq);
        }

        System.out.println("原始序列数据：");
        outputSeqence(originSequences);
        System.out.println("*********************************");
    }

    private void outputSeqence(List<Sequence> seqList) {
        for (Sequence seq : seqList) {
            System.out.print("<");
            for (ItemSet itemSet : seq.getItemSetList()) {
                if (itemSet.getItems().size() > 1) {
                    System.out.print("(");
                }

                for (Integer s : itemSet.getItems()) {
                    System.out.print(s + " ");
                }

                if (itemSet.getItems().size() > 1) {
                    System.out.print(")");
                }
            }
            System.out.println(">");
        }
    }

    /**
     * 按模式类别输出频繁序列模式
     */
    private void printTotalFreSeqs() {
        outputSeqence(this.totalFrequencySeqs);
        /*System.out.println("序列模式挖掘结果：");
        System.out.println(totalFrequencySeqs.size());

        List<Sequence> seqList;
        HashMap<Integer, List<Sequence>> seqMap = new HashMap<>();
        for (Integer s : singleItems) {
            seqList = new ArrayList<>();
            for (Sequence seq : totalFrequencySeqs) {
                if (seq.getItemSetList().get(0).getItems().get(0).equals(s)) {
                    seqList.add(seq);
                }
            }
            seqMap.put(s, seqList);
        }

        int count = 0;
        for (Integer s : singleItems) {
            count = 0;
            System.out.println();
            System.out.println();

            seqList =  seqMap.get(s);
            for (Sequence tempSeq : seqList) {
                count++;
                System.out.print("<");
                for (ItemSet itemSet : tempSeq.getItemSetList()) {
                    if (itemSet.getItems().size() > 1) {
                        System.out.print("(");
                    }

                    for (Integer str : itemSet.getItems()) {
                        System.out.print(str + " ");
                    }

                    if (itemSet.getItems().size() > 1) {
                        System.out.print(")");
                    }
                }
                System.out.print(">, ");

                // 每5个序列换一行
                if (count == 5) {
                    count = 0;
                    System.out.println();
                }
            }

        }*/
    }

    public int getMinSupportCount() {
        return minSupportCount;
    }

    public void setMinSupportCount(int minSupportCount) {
        this.minSupportCount = minSupportCount;
    }

    public double getMin_gap() {
        return min_gap;
    }

    public void setMin_gap(double min_gap) {
        this.min_gap = min_gap;
    }

    public double getMax_gap() {
        return max_gap;
    }

    public void setMax_gap(double max_gap) {
        this.max_gap = max_gap;
    }

    public List<Sequence> getOriginSequences() {
        return originSequences;
    }

    public void setOriginSequences(List<Sequence> originSequences) {
        this.originSequences = originSequences;
    }

    public List<Sequence> getTotalFrequencySeqs() {
        return totalFrequencySeqs;
    }

    public void setTotalFrequencySeqs(List<Sequence> totalFrequencySeqs) {
        this.totalFrequencySeqs = totalFrequencySeqs;
    }

}
