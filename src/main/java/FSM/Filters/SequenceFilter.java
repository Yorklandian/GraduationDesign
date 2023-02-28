package FSM.Filters;

import FSM.ItemSet;
import FSM.Sequence;

import java.util.*;

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

    /**
     * 删除不平衡的sequence 长度相比item类别比例太大 例如<1,1,1,1,1,1,2,2,2,1,1,1,1,1,1,2,2>
     * @param sequenceList
     */
    public static void filterUnbalanceSequences(List<Sequence> sequenceList){
        Iterator<Sequence> iterator = sequenceList.iterator();
        while (iterator.hasNext()){
            boolean flag = false;
            Sequence sequence = iterator.next();
            if(sequence.getItemNums() > 0){
                if((double)sequence.getItemSetList().size()/(double)sequence.getItemNums() >= 5){ //比值可改变
                    flag = true;
                }
            }

            if(flag){
                iterator.remove();
            }
        }

    }

    /**
     * 将连续多个重复的itemSet合并为一个，默认留下最后一个
     * @param limit 连续重复的限制，重复次数大于limit则进行合并
     */
    public static void combineMultiReplicateItemSets(int limit,Sequence sequence){
        int count = 1;
        Set<Integer> indexToRemove = new HashSet<>();
        ItemSet tempItemSet = new ItemSet(-1,0);
        for (int i = 0; i < sequence.getItemSetList().size(); i++) {
            ItemSet itemSet = sequence.getItemSetList().get(i);
            if(itemSet.compareItems(tempItemSet)){
                count++;
                if(count > limit){
                    //找到重复的最后一个index
                    int lastindex = i;
                    for (int j = i+1; j < sequence.getItemSetList().size() ; j++) {
                        if(sequence.getItemSetList().get(j).compareItems(itemSet)){
                            count++;
                            lastindex = j;
                        }
                        else {
                            break;
                        }
                    }

                    for (int j = lastindex-1; j > lastindex-count; j--) {
                        indexToRemove.add(j);
                    }
                    itemSet = sequence.getItemSetList().get(lastindex);
                }
            } else {
                count = 1;
            }
            tempItemSet = itemSet;
        }
        if(indexToRemove.size() ==0){
            return;
        }

/*        Iterator<ItemSet> iterator = sequence.getItemSetList().iterator();
        int index = 0;
        while (iterator.hasNext()){
            if(indexToRemove.contains(index)){
                iterator.remove();
            }
            index++;
        }*/
        List<ItemSet> newList = new ArrayList<>();
        for (int i = 0; i < sequence.getItemSetList().size(); i++) {
            if(!indexToRemove.contains(i)){
                newList.add(sequence.getItemSetList().get(i));
            }
        }
        sequence.setItemSetList(newList);

    }

    /**
     * 执行所有的filter
     * @param sequenceList
     */
    public static void doAllFilter(List<Sequence> sequenceList){
        filterReplicateSequence(sequenceList);
        filterUnbalanceSequences(sequenceList);
        filterOneItemSequences(sequenceList);
        filterToMaxFreqPatterns(sequenceList);
        sequenceList.forEach(sequence -> combineMultiReplicateItemSets(3,sequence));
    }

}
