package FSM.Filters;

import FSM.ItemSet;
import FSM.Sequence;

import java.util.Iterator;
import java.util.List;

public class SequenceFilter {
    /**
     * 过滤掉item全部重复的sequence
     * @param sequenceList
     */
    public static void filterReplicateSequence(List<Sequence> sequenceList){
        Iterator<Sequence> iterator = sequenceList.iterator();
        //删除所有ItemSet都相同的Sequence，消除金字塔
        while (iterator.hasNext()){
            List<ItemSet> itemSetList = iterator.next().getItemSetList();
            ItemSet firstItemSet = itemSetList.get(0);
            boolean flag = true;
            int index = 0;
            for (ItemSet itemSet :itemSetList) {
                if(index ==0){
                    index++;
                    continue;
                }

                if(!firstItemSet.compareItems(itemSet)){
                    flag = false;
                }
                index++;
            }
            if(flag){
                iterator.remove();
            }
        }
    }

    /**
     * 过滤掉单个item的sequence
     * @param sequenceList
     */
    public static void filterOneItemSequences(List<Sequence> sequenceList){
        sequenceList.removeIf(sequence -> sequence.getItemSetList().size() == 1);
    }


    /**
     * 过滤至最大的sequenceList
     * @param sequenceList
     */
    public static void filterToMaxFreqPatterns(List<Sequence> sequenceList){
        for (Sequence seq : sequenceList) {
            if(seq.hasParentSequence(sequenceList)){
                seq.setIsSubSequence(true);
            }
        }

        sequenceList.removeIf(sequence -> sequence.getIsSubSequence());
    }


}
