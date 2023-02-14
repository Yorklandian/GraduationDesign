package FSM.PreFixSpan;


import FSM.ItemSet;
import FSM.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PreFixSpan 频繁序列模式挖掘算法 TODO：时间约束
 */
public class PreFixSpanTool {
    // 最小支持度阈值
    private int minSupportCount;
    // 时间最小间隔
    private double min_gap;
    // 时间最大间隔
    private double max_gap;
    // 原始数据序列
    private List<Sequence> originSequences;
    // 算法中产生的所有的频繁项集序列
    private List<Sequence> totalFrequencySeqs;

    /**
     * 获取所有不同的item(即integer类型的id)
     * @return
     */
    private List<Integer> getAllItems(){
        List<Integer> res = new ArrayList<>();
        for (Sequence seq :originSequences) {
            for (ItemSet itemset :seq.getItemSetList()) {
                for (Integer intger :itemset.getItems()) {
                    if(!res.contains(intger)){
                        res.add(intger);
                    }
                }
            }
        }
        Collections.sort(res);
        return res;
    }

    private void deleteInfrequentItems(List<Integer> infrequentItems){
        if(infrequentItems.size() == 0){

        }
    }


}
