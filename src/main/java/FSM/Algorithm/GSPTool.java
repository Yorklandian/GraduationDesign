package FSM.Algorithm;

import FSM.Filters.SequenceFilter;
import FSM.ItemSet;
import FSM.Sequence;
import FSM.tools.DataProcessTool;

import java.util.*;

/**
 * GSP频繁序列模式挖掘算法
 *
 */
public class GSPTool {
//    // 测试数据文件地址
//    private String filePath;
//    // 输出文件地址
//    private String outputFilePath;
    // 最小支持度阈值
    private int minSupportCount;
    // 时间最小间隔
    private double min_gap;
    // 时间最大间隔
    private double max_gap;
    // 原始数据序列
    private List<Sequence> originSequences;
    // GSP算法中产生的所有的频繁项集序列
    private List<Sequence> totalFrequencySeqs;
    // 序列项数字对时间的映射图容器 最外层：序列list，第二层：序列中的itemSet list，
    // 第三层：映射的是item，timestamp，既能存储时间，也存储item，map中每个entity为<item，itemTime>
    private ArrayList<ArrayList<HashMap<Integer, Double>>> itemNum2Time;

    //输入输出处理工具类
    private DataProcessTool dataProcessTool;

    public GSPTool(int minSupportCount, double min_gap, double max_gap,DataProcessTool dataTool) {

        this.minSupportCount = minSupportCount;
        this.min_gap = min_gap;
        this.max_gap = max_gap;
        this.dataProcessTool = dataTool;
        totalFrequencySeqs = new ArrayList<>();

    }

    /**
     * 清理，便于同文件下下次挖掘
     */
    public void clear(){
        this.originSequences.clear();
        this.totalFrequencySeqs.clear();
        this.itemNum2Time.clear();
    }


    /**
     * 从文件中读取数据,并生成对应的itemSet和Sequence,并将其加入到originalSequences中
     */
    //TODO: 未对时间进行处理，同时文件格式应进行更改
/*    private void readDataFile() {
        File file = new File(filePath);
        ArrayList<String[]> dataArray = new ArrayList<String[]>();

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

        HashMap<Integer, Sequence> mapSeq = new HashMap<>();
        Sequence seq;
        ItemSet itemSet;
        int tID;
        Integer time;
        String[] itemStr;
        for (String[] str : dataArray) {
            tID = Integer.parseInt(str[0]);
            itemStr = new String[Integer.parseInt(str[1])];
            time = Integer.parseInt(str[2]);
            System.arraycopy(str, 3, itemStr, 0, itemStr.length);
            itemSet = new ItemSet(itemStr,time);

            if (mapSeq.containsKey(tID)) {
                seq = mapSeq.get(tID);
            } else {
                seq = new Sequence(tID);
            }
            seq.getItemSetList().add(itemSet);
            mapSeq.put(tID, seq);
        }

        // 将序列图加入到序列List中
        originSequences = new ArrayList<>();
        for (Map.Entry entry : mapSeq.entrySet()) {
            originSequences.add((Sequence) entry.getValue());
        }
    }*/

    /**
     * 生成1频繁项集
     *
     * @return 1频繁项集
     */
    private List<Sequence> generateOneFrequencyItem() {
        // count为支持度计数supportCount
        int count = 0;

        Sequence tempSeq;
        ItemSet tempItemSet;

        //遍历所有item， 将其存放在map中，对应键值对为<item,supportCount>
        HashMap<Integer, Integer> itemNumMap = new HashMap<>();
        List<Sequence> seqList = new ArrayList<>();

        for (Sequence seq : originSequences) {
            for (ItemSet itemSet : seq.getItemSetList()) {
                for (int num : itemSet.getItems()) {
                    // 如果没有此种类型项，则进行添加操作
                    if (!itemNumMap.containsKey(num)) {
                        itemNumMap.put(num, 1);
                    }
                }
            }
        }

        boolean isContain = false;
        int number = 0;
        for (Map.Entry entry : itemNumMap.entrySet()) {
            count = 0;
            number = (int) entry.getKey();
            for (Sequence seq : originSequences) {
                isContain = false;

                for (ItemSet itemSet : seq.getItemSetList()) {
                    for (int num : itemSet.getItems()) {
                        if (num == number) {
                            isContain = true;
                            break;
                        }
                    }

                    if(isContain){
                        //count++;
                        break;
                    }
                }
                //支持度属于用户粒度，意思是单个sequence中出现多次也只需加1
                if(isContain){
                    count++;
                }
            }

            itemNumMap.put(number, count);
        }


        for (Map.Entry entry : itemNumMap.entrySet()) {
            count = (int) entry.getValue();
            if (count >= minSupportCount) {
                tempSeq = new Sequence();
                tempItemSet = new ItemSet(new int[] { (int) entry.getKey() });

                tempSeq.setSupport(count);

                tempSeq.getItemSetList().add(tempItemSet);
                seqList.add(tempSeq);
            }

        }
        // 将序列升序排列
        Collections.sort(seqList);
        // 将频繁1项集加入总频繁项集列表中
        totalFrequencySeqs.addAll(seqList);

        //输出频繁一项集
//        for(int i = 0; i< totalFrequencySeqs.size();i++){
//            Integer num = seqList.get(i).getFirstItemSetNum();
//            System.out.println(num);
//        }

        return seqList;
    }

    /**
     * 通过1频繁项集连接产生2频繁项集
     *
     * @param oneSeqList
     *            1频繁项集序列
     * @return 2频繁项集序列
     */
    private List<Sequence> generateTwoFrequencyItem(List<Sequence> oneSeqList) {
        Sequence tempSeq;
        ArrayList<Sequence> resultSeq = new ArrayList<>();
        ItemSet tempItemSet;
        int num1;
        int num2;

        // 假如将<a>,<b>2个1频繁项集做连接组合，可以分为<a a>，<a b>，<b a>,<b b>4个序列模式
        // 注意此时的每个序列中包含2个独立项集
        for (int i = 0; i < oneSeqList.size(); i++) {
            num1 = oneSeqList.get(i).getFirstItemSetNum();
            for (int j = 0; j < oneSeqList.size(); j++) {
                num2 = oneSeqList.get(j).getFirstItemSetNum();

                tempSeq = new Sequence();
                tempItemSet = new ItemSet(new int[] { num1 });
                tempSeq.getItemSetList().add(tempItemSet);
                tempItemSet = new ItemSet(new int[] { num2 });
                tempSeq.getItemSetList().add(tempItemSet);
                int support = countSupport(tempSeq);
                if (support >= minSupportCount) {
                    tempSeq.setSupport(support);
                    resultSeq.add(tempSeq);
                }
            }
        }

        // 上面连接还有1种情况是每个序列中只包含有一个项集的情况，此时a,b的划分则是<(a,a)> <(a,b)> <(b,b)>,其中<(a,a)> <(b,b)>不应出现
        for (int i = 0; i < oneSeqList.size(); i++) {
            num1 = oneSeqList.get(i).getFirstItemSetNum();
            for (int j = i; j < oneSeqList.size(); j++) {
                num2 = oneSeqList.get(j).getFirstItemSetNum();
                //不允许类似<(a,a)>或<(b,b)>的itemSet
                if(num1 == num2){
                    continue;
                }
                tempSeq = new Sequence();
                tempItemSet = new ItemSet(new int[] { num1, num2 });
                tempSeq.getItemSetList().add(tempItemSet);
                int support = countSupport(tempSeq);
                if (support >= minSupportCount) {
                    tempSeq.setSupport(support);
                    resultSeq.add(tempSeq);
                }

            }
        }
        // 同样将2频繁项集加入到总频繁项集中
        totalFrequencySeqs.addAll(resultSeq);

        return resultSeq;
    }

    /**
     * 根据上次的频繁集连接产生新的侯选集
     *
     * @param seqList
     *            上次产生的候选集
     * @return 新的候选集
     */
    private List<Sequence> generateCandidateItem(
            List<Sequence> seqList) {
        Sequence tempSeq;
        ArrayList<Integer> tempNumArray;
        ArrayList<Sequence> resultSeq = new ArrayList<>();
        // 序列数字项列表
        ArrayList<ArrayList<Integer>> seqNums = new ArrayList<>();

        for (int i = 0; i < seqList.size(); i++) {
            tempNumArray = new ArrayList<>();
            tempSeq = seqList.get(i);
            for (ItemSet itemSet : tempSeq.getItemSetList()) {
                tempNumArray.addAll(itemSet.copyItems());
            }
            seqNums.add(tempNumArray);
        }

        ArrayList<Integer> array1;
        ArrayList<Integer> array2;
        // 序列i,j的拷贝
        Sequence seqi = null;
        Sequence seqj = null;
        // 判断是否能够连接，默认能连接
        boolean canConnect = true;
        // 进行连接运算，包括自己与自己连接
        for (int i = 0; i < seqNums.size(); i++) {
            for (int j = 0; j < seqNums.size(); j++) {
                array1 = (ArrayList<Integer>) seqNums.get(i).clone();
                array2 = (ArrayList<Integer>) seqNums.get(j).clone();

                // 将第一个数字组去掉第一个，第二个数字组去掉最后一个，如果剩下的部分相等，则可以连接
                array1.remove(0);
                array2.remove(array2.size() - 1);

                canConnect = true;
                for (int k = 0; k < array1.size(); k++) {
                    if (array1.get(k) != array2.get(k)) {
                        canConnect = false;
                        break;
                    }
                }

                if (canConnect) {
                    seqi = seqList.get(i).copySequence();
                    seqj = seqList.get(j).copySequence();

                    int lastItemNum = seqj.getLastItemSetNum();
                    if (seqj.isLastItemSetSingleNum()) {
                        // 如果j序列的最后项集为单一值，则最后一个数字以独立项集加入i序列
                        ItemSet itemSet = new ItemSet(new int[] { lastItemNum });
                        seqi.getItemSetList().add(itemSet);
                    } else {
                        // 如果j序列的最后项集为非单一值，则最后一个数字加入i序列最后一个项集中
                        ItemSet itemSet = seqi.getLastItemSet();
                        itemSet.getItems().add(lastItemNum);
                    }

                    // 判断是否超过最小支持度阈值
                    int support = countSupport(seqi);
                    if (isChildSeqContained(seqi)
                            && support >= minSupportCount) {
                        seqi.setSupport(support);
                        resultSeq.add(seqi);
                    }
                }
            }
        }

        totalFrequencySeqs.addAll(resultSeq);
        return resultSeq;
    }

    /**
     * 判断此序列的所有子序列是否也是频繁序列
     *
     * @param seq
     *            待比较序列
     * @return boolean
     */
    private boolean isChildSeqContained(Sequence seq) {
        boolean isContained = false;
        List<Sequence> childSeqs;

        childSeqs = seq.createChildSeqs();
        for (Sequence tempSeq : childSeqs) {
            isContained = false;

            for (Sequence frequencySeq : totalFrequencySeqs) {
                if (tempSeq.compareSequenceItems(frequencySeq)) {
                    isContained = true;
                    break;
                }
            }

            if (!isContained) {
                break;
            }
        }

        return isContained;
    }

    /**
     * 候选集判断支持度的值
     *
     * @param seq
     *            待判断序列 例如<1,(2,3),4 >
     * @return 待判断序列的支持度
     */
    private int countSupport(Sequence seq) {
        int count = 0;
        int matchNum = 0;
        Sequence tempSeq;
        ItemSet tempItemSet;
        HashMap<Integer, Double> timeMap;
        List<ItemSet> itemSetList;

        // numArray 保存待判断序列中所有itemSet <[1],[2,3],[4] >
        List<List<Integer>> numArray = new ArrayList<>();

        // 每项集对应的时间链表 用于保存所有成功匹配的时间链表
        ArrayList<ArrayList<Double>> timeArray;

        for (ItemSet itemSet : seq.getItemSetList()) {
            numArray.add(itemSet.getItems());
        }

        // 遍历所有原始序列
        for (int i = 0; i < originSequences.size(); i++) {
            // 每个原始序列使用一条timeArray, 对待判断序列中每个成功匹配的itemSet保留一条int时间数组
            timeArray = new ArrayList<>();
            // 遍历待判断序列中所有itemSet
            for (int s = 0; s < numArray.size(); s++) {
                //依次获取待遍历sequence的每个itemSet，作为待比较itemSet 记为childNum
                List<Integer> childNum = numArray.get(s);
                //localTime 数组存在于每个待遍历序列的每个itemSet中
                ArrayList<Double> localTime = new ArrayList<>();

                // 获取第i个原始 sequence，获取其所有itemSet
                tempSeq = originSequences.get(i);
                itemSetList = tempSeq.getItemSetList();

                // 遍历第i个原始 sequence 的所有itemSet
                for (int j = 0; j < itemSetList.size(); j++) {
                    tempItemSet = itemSetList.get(j);
                    matchNum = 0;
                    Double t = 0.0;
                    // 开始进行比较
                    // 对于每个itemSet，与待检查的itemSet进行比较,只有当项集长度 *匹配* 时才匹配 TODO 此处似乎有误，长度匹配不应是长度相等，已改为长度大于等于
                    if (tempItemSet.getItems().size() >= childNum.size()) {

                        // 第i个原始序列中第j个itemSet的timeMap，由此可获取时间
                        timeMap = itemNum2Time.get(i).get(j);
                        // 获取待比较itemSet中所有item
                        for (Integer item : childNum) {
                            // 依靠map逐个比较item
                            if (timeMap.containsKey(item)) {
                                matchNum++;
                                t = timeMap.get(item);
                            }
                        }

                        // 如果完全匹配，则记录时间
                        if (matchNum == childNum.size()) {
                            localTime.add(t);
                        }
                    }

                }

                if (localTime.size() > 0) {
                    timeArray.add(localTime);
                }
            }

            // timeArray存放 待判断序列在当前原始序列中 项集匹配成功 的所有时间链表
            // 判断时间是否满足时间最大最小约束，如果满足，则此条事务包含候选事务
            if (timeArray.size() == numArray.size() && judgeTimeInGap(timeArray)) {
                // 每条sequence中count最多 +1
                count++;
            }
        }
        /*for (ItemSet itemSet: seq.getItemSetList()) {
            for (int item: itemSet.getItems()) {
                System.out.print(item);
                System.out.print(", ");
            }
            System.out.print(";");
        }
        System.out.print(":");
        System.out.println(count);*/

        return count;
    }

    /**
     * 判断事务是否满足时间约束
     *
     * @param timeArray
     *            时间数组，每行代表各项集的在事务中的发生时间链表
     * @return boolean
     */
    private boolean judgeTimeInGap(ArrayList<ArrayList<Double>> timeArray) {
        if(timeArray.size() == 1){
            return true;
        }
        boolean result = false;
        Double preTime = 0.0;
        ArrayList<Double> firstTimes = timeArray.get(0);
        timeArray.remove(0);

        for (int i = 0; i < firstTimes.size(); i++) {
            preTime = firstTimes.get(i);

            if (dfsJudgeTime(preTime, timeArray)) {
                result = true;
                //TODO: 如果要改变支持度计数方式，要使judge返回judge成功的次数
                break;
            }
        }

        return result;
    }

    /**
     * 深度优先遍历时间，判断是否有符合条件的时间间隔
     *
     * @param preTime
     * @param timeArray
     * @return boolean
     */
    private boolean dfsJudgeTime(Double preTime, ArrayList<ArrayList<Double>> timeArray) {
        boolean result = false;
        ArrayList<ArrayList<Double>> timeArrayClone = (ArrayList<ArrayList<Double>>) timeArray.clone();
        ArrayList<Double> firstItemsetTime = timeArrayClone.get(0);

        for (int i = 0; i < firstItemsetTime.size(); i++) {
            if (firstItemsetTime.get(i) - preTime >= min_gap
                    && firstItemsetTime.get(i) - preTime <= max_gap) {
                // 如果此2项间隔时间满足时间约束，则继续往下递归
                preTime = firstItemsetTime.get(i);
                timeArrayClone.remove(0);

                if (timeArrayClone.size() == 0) {
                    return true;
                } else {
                    result = dfsJudgeTime(preTime, timeArrayClone);
                    if (result) {
                        return true;
                    }
                }
            }
        }

        return result;
    }

    /**
     * 初始化序列项到时间的序列图，为了后面的时间约束计算
     */
    private void initItemNumToTimeMap() {
        Sequence seq;
        itemNum2Time = new ArrayList<>();
        HashMap<Integer, Double> tempMap;
        ArrayList<HashMap<Integer, Double>> tempMapList;

        // 遍历所有原始sequence
        for (int i = 0; i < originSequences.size(); i++) {
            seq = originSequences.get(i);
            tempMapList = new ArrayList<>();

            //遍历原始sequence中的所有itemSet
            for (int j = 0; j < seq.getItemSetList().size(); j++) {
                ItemSet itemSet = seq.getItemSetList().get(j);
                tempMap = new HashMap<>();
                // 遍历itemSet中的所有item
                for (int itemNum : itemSet.getItems()) {
                    // 存储时间，默认的item时间是其所在的itemSet在序列中的index+1，并以map形式存储，此处应作更改
                    tempMap.put(itemNum, itemSet.getTime());
                }

                tempMapList.add(tempMap);
            }

            itemNum2Time.add(tempMapList);
        }
    }

    /**
     * 进行GSP算法计算
     */
    public boolean gspCalculate() {
        List<Sequence> oneSeq;
        List<Sequence> twoSeq;
        List<Sequence> candidateSeq;

        initItemNumToTimeMap();
        oneSeq = generateOneFrequencyItem();
        twoSeq = generateTwoFrequencyItem(oneSeq);
        candidateSeq = twoSeq;

        // 不断连接生产候选集，直到没有产生出侯选集
        do {
            candidateSeq = generateCandidateItem(candidateSeq);

        } while (candidateSeq.size() != 0);

        this.doFilter();
        outputSeqence(this.totalFrequencySeqs);
        dataProcessTool.outputToFile(this.totalFrequencySeqs);
        return true;
    }

    /**
     * 过滤结果,移除 全重复的，单个的，非最大的，不平衡的结果
     */
    public void doFilter(){
//        SequenceFilter.filterToMaxFreqPatterns(this.getTotalFrequencySeqs());
//        SequenceFilter.filterOneItemSequences(this.getTotalFrequencySeqs());
//        SequenceFilter.filterReplicateSequence(this.getTotalFrequencySeqs());
//        SequenceFilter.filterUnbalanceSequences(this.getTotalFrequencySeqs());
        SequenceFilter.doAllFilter(this.getTotalFrequencySeqs());
    }

    /**
     * 输出序列列表信息
     *
     * @param outputSeqList
     *            待输出序列列表
     */
    private void outputSeqence(List<Sequence> outputSeqList) {
        for (Sequence seq : outputSeqList) {
            System.out.print("<");
            for (ItemSet itemSet : seq.getItemSetList()) {
                System.out.print("(");
                for (int num : itemSet.getItems()) {
                    System.out.print(num + ",");
                }
                System.out.print("), ");
            }
            System.out.println(">" + "support: " + seq.getSupport());
        }
    }



    public List<Sequence> getTotalFrequencySeqs() {
        return totalFrequencySeqs;
    }
    public void setOriginSequences(List<Sequence> originSequences) {
        this.originSequences = originSequences;
    }

    public DataProcessTool getDataProcessTool() {
        return dataProcessTool;
    }

    public void setDataProcessTool(DataProcessTool dataProcessTool) {
        this.dataProcessTool = dataProcessTool;
    }
}
