package FSM;

import java.util.*;

/**
 * 序列，每个序列内部包含多组ItemSet项集
 *
 */
public class Sequence implements Comparable<Sequence>, Cloneable {
    // 序列所属事务ID
    private int trsanctionID;
    // 序列中的项集列表
    private List<ItemSet> itemSetList;
    // 支持度 初始的序列支持度默认为0
    private Integer support = 0;

    // 是否是子序列，在最终结果中移除
    private boolean isSubSequence = false;


    public Sequence(int trsanctionID) {
        this.trsanctionID = trsanctionID;
        this.itemSetList = new ArrayList<>();
    }

    public Sequence() {
        this.itemSetList = new ArrayList<>();
    }


    /**
     * 取出序列中第一个项集的第一个元素
     *
     * @return 序列中第一个项集中第一个元素
     */
    public Integer getFirstItemSetNum() {
        return this.getItemSetList().get(0).getItems().get(0);
    }

    /**
     * 获取序列中最后一个项集
     *
     * @return 序列中最后一个项集
     */
    public ItemSet getLastItemSet() {
        return getItemSetList().get(getItemSetList().size() - 1);
    }

    /**
     * 获取序列中最后一个项集的最后一个一个元素
     *
     * @return 获取序列中最后一个项集的最后一个一个元素
     */
    public Integer getLastItemSetNum() {
        ItemSet lastItemSet = getItemSetList().get(getItemSetList().size() - 1);
        int lastItemNum = lastItemSet.getItems().get(
                lastItemSet.getItems().size() - 1);

        return lastItemNum;
    }

    /**
     * 判断序列中最后一个项集是否为单一的值
     *
     * @return
     */
    public boolean isLastItemSetSingleNum() {
        ItemSet lastItemSet = getItemSetList().get(getItemSetList().size() - 1);
        int size = lastItemSet.getItems().size();

        return size == 1;
    }

    @Override
    public int compareTo(Sequence o) {
        // TODO Auto-generated method stub
        return this.getFirstItemSetNum().compareTo(o.getFirstItemSetNum());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    /**
     * 拷贝一份一模一样的序列，既拷贝项集，又拷贝支持度。但是注意，拷贝的itemSet中不含时间
     */
    public Sequence copySequence(){
        Sequence copySeq = new Sequence();
        for(ItemSet itemSet: this.itemSetList){
            copySeq.getItemSetList().add(new ItemSet(itemSet.copyItems()));
        }
        copySeq.setSupport(this.getSupport());

        return copySeq;
    }

    /**
     * 比较2个序列是否相等，需要判断内部的每个项集是否完全一致，不比较支持度
     *
     * @param seq
     *            比较的序列对象
     * @return boolean
     */
    public boolean compareSequenceItems(Sequence seq) {
        boolean result = true;
//        if(!Objects.equals(seq.getSupport(), this.getSupport())){
//            return false;
//        }
        List<ItemSet> itemSetList2 = seq.getItemSetList();
        ItemSet tempItemSet1;
        ItemSet tempItemSet2;


        if (itemSetList2.size() != this.itemSetList.size()) {
            return false;
        }
        for (int i = 0; i < itemSetList2.size(); i++) {
            tempItemSet1 = this.itemSetList.get(i);
            tempItemSet2 = itemSetList2.get(i);

            if (!tempItemSet1.compareItems(tempItemSet2)) {
                // 只要不相等，直接退出函数
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * 判断 sequence 是否是调用者的子序列
     * @param sequence 待判断的子序列
     * @return boolean
     */
    private boolean isParentSequenceOf(Sequence sequence){
        boolean result = false;
        List<ItemSet> itemSetList1 = this.getItemSetList();
        List<ItemSet> itemSetList2 = sequence.getItemSetList();
        if(itemSetList1.size() < itemSetList2.size()){
            return false;
        }

        ItemSet itemSet1;
        ItemSet itemSet2;

        int matchNum = 0;
        int lastIndex = 0;
        for (int i = 0; i < itemSetList2.size(); i++) {
            itemSet1 = itemSetList2.get(i);
            for (int j = 0; j < itemSetList1.size(); j++) {
                itemSet2 = itemSetList1.get(j);
                if(itemSet1.compareItems(itemSet2) && lastIndex <= j){
                    matchNum++;
                    lastIndex = j;
                    break;
                }
            }
        }
        if(matchNum == itemSetList2.size()){
            result = true;
        }

        return result;
    }

    /**
     * 判断sequenceList中是否有sequence的parent
     * @param sequenceList 总list
     */
    public boolean hasParentSequence(List<Sequence> sequenceList){
        boolean res = false;
        for (Sequence seq: sequenceList) {
            if(seq.compareSequenceItems(this)){
                continue;
            }
            if(seq.isParentSequenceOf(this)){
                //this.setIsSubSequence(true);
                res = true;
                break;
            }
        }
        return res;
    }

    /**
     * 生成此序列的所有子序列
     *
     * @return 所有子序列
     */
    public List<Sequence> createChildSeqs() {
        List<Sequence> childSeqs = new ArrayList<>();
        List<Integer> tempItems;
        Sequence tempSeq = null;
        ItemSet tempItemSet;

        for (int i = 0; i < this.itemSetList.size(); i++) {
            tempItemSet = itemSetList.get(i);
            if (tempItemSet.getItems().size() == 1) {
                tempSeq = this.copySequence();

                // 如果只有项集中只有1个元素，则直接移除
                tempSeq.itemSetList.remove(i);
                childSeqs.add(tempSeq);
            } else {
                tempItems = tempItemSet.getItems();
                for (int j = 0; j < tempItems.size(); j++) {
                    tempSeq = this.copySequence();

                    // 在拷贝的序列中移除一个数字
                    tempSeq.getItemSetList().get(i).getItems().remove(j);
                    childSeqs.add(tempSeq);
                }
            }
        }

        return childSeqs;
    }

    /**
     * 判断单一item是否包含于此序列
     * @param i 待判断项
     * @return boolean
     */
    public boolean itemIsContained(int i){
        boolean isContained = false;
        for (ItemSet itemSet : this.getItemSetList()) {
            isContained = false;
            for (Integer intger :itemSet.getItems()) {
                if(itemSet.getItems().contains(-1)){
                    continue;
                }
                if(i == intger){
                    isContained = true;
                    break;
                }
            }
            if(isContained){
                break;
            }
        }
        return isContained;
    }

    /**
     * 判断组合项集 如（1，2） 是否存在于序列中
     * @param itemSet 待判断的itemSet
     * @return boolean
     */
    public boolean componentItemIsContained(ItemSet itemSet){
        boolean isContained = false;
        List<Integer> tempItems;
        int lastItem = itemSet.getLastValue();
        for (ItemSet set : this.itemSetList) {
            tempItems = set.getItems();
            // 分2种情况查找，第一种从_X中找出x等于项集最后的元素，因为_前缀已经为原本的元素
            if (tempItems.size() > 1
                    && tempItems.get(0) == -1
                    && tempItems.get(1).equals(lastItem)) {
                isContained = true;
                //break;
            } else if (tempItems.size()> 0 && tempItems.get(0) != -1) {
                // 从没有_前缀的项集开始寻找，第二种为从后面的后缀中找出直接找出连续字符为ab为同一项集的项集
                if (arrayContains(tempItems, itemSet.getItems())) {
                    isContained = true;
                    //break;
                }
            }

            if (isContained) {
                break;
            }
        }

        return isContained;
    }

    /**
     * 从序列中删除单个项
     * @param item 要删除的项
     */
    public void deleteSingleItem(int item){
        List<Integer> tempItems;
        List<Integer> deleteItems;
        for (ItemSet itemSet : this.getItemSetList()) {
            tempItems = itemSet.getItems();
            deleteItems = new ArrayList<>();

            for (Integer tempItem : tempItems) {
                if (tempItem == item) {
                    deleteItems.add(tempItem);
                }
            }

            tempItems.removeAll(deleteItems);
        }
        clearEmptyItemSets();

    }

    /**
     * 获取提取项integer之后的sequence
     * @param item 被提取的item
     * @return 提取项integer之后的sequence
     */
    public Sequence extractItem(int item) {
        Sequence extractSeq = this.copySequence();
        ItemSet itemSet;
        List<Integer> items;
        List<ItemSet> deleteItemSets = new ArrayList<>();
        List<Integer> tempItems = new ArrayList<>();

        for (int k = 0; k < extractSeq.itemSetList.size(); k++) {
            itemSet = extractSeq.itemSetList.get(k);
            items = itemSet.getItems();
            if (items.size() == 1 && items.get(0).equals(item)) {
                //如果找到的是单项，则完全移除，跳出循环
                extractSeq.itemSetList.remove(k);
                break;
            } else if (items.size() > 1 && items.get(0) != -1) {
                //在后续的多元素项中判断是否包含此元素
                if (items.contains(item)) {
                    //如果包含把s后面的元素加入到临时字符数组中
                    int index = items.indexOf(item);
                    for (int j = index; j < items.size(); j++) {
                        tempItems.add(items.get(j));
                    }
                    //将第一位的s变成下标符"_",即变为-1
                    tempItems.set(0, -1);
                    if (tempItems.size() == 1) {
                        // 如果此匹配为在最末端，同样移除
                        deleteItemSets.add(itemSet);
                    } else {
                        //将变化后的项集替换原来的
                        extractSeq.itemSetList.set(k, new ItemSet(tempItems));
                    }
                    break;

                } else {
                    deleteItemSets.add(itemSet);
                }
            } else {
                // 不符合以上2项条件的统统移除
                deleteItemSets.add(itemSet);
            }
        }
        extractSeq.itemSetList.removeAll(deleteItemSets);
        extractSeq.clearEmptyItemSets();

        return extractSeq;
    }

    /**
     * 获取提取组合项之后的序列
     * @param array 要提取的组合项
     * @return 提取后的sequence
     */
    public Sequence extractComponentItem(List<Integer> array) {
        // 找到目标项，是否立刻停止
        //boolean stopExtract = false;
        //深拷贝
        Sequence seq = this.copySequence();
        // 最后一个item
        int lastItem = array.get(array.size() - 1);

        List<Integer> tempItems;
        List<ItemSet> deleteItems = new ArrayList<>();

        for (int i = 0; i < seq.itemSetList.size(); i++) {
//            if (stopExtract) {
//                break;
//            }

            tempItems = seq.itemSetList.get(i).getItems();
            // 分2种情况查找，第一种从_X中找出x等于项集最后的元素，因为_前缀已经为原本的元素
            if (tempItems.size() > 1 && tempItems.get(0) == -1 && tempItems.get(1).equals(lastItem)) {
                if (tempItems.size() == 2) {
                    seq.itemSetList.remove(i);
                } else {
                    // 把1号位置变为下标符"_"，即 -1，往后移1个字符的位置
                    tempItems.set(1, -1);
                    // 移除第一个的"_"下划符
                    tempItems.remove(0);
                }
                //stopExtract = true;
                break;
            } else if (tempItems.size() > 0 &&tempItems.get(0) != -1) {
                // 从没有_前缀的项集开始寻找，第二种为从后面的后缀中找出直接找出连续字符为ab为同一项集的项集
                if (arrayContains(tempItems, array)) {
                    // 从左往右找出第一个给定字符的位置，把后面的部分截取出来
                    int index = tempItems.indexOf(lastItem);
                    List<Integer> array2 = new ArrayList<>();

                    for (int j = index; j < tempItems.size(); j++) {
                        array2.add(tempItems.get(j));
                    }
                    array2.set(0, -1);

                    if (array2.size() == 1) {
                        //如果此项在末尾的位置，则移除该项，否则进行替换
                        deleteItems.add(seq.itemSetList.get(i));
                    } else {
                        seq.itemSetList.set(i, new ItemSet(array2));
                    }
                    //stopExtract = true;
                    break;
                } else {
                    deleteItems.add(seq.itemSetList.get(i));
                }
            } else {
                // 这种情况是处理_X中X不等于最后一个元素的情况
                deleteItems.add(seq.itemSetList.get(i));
            }
        }

        seq.itemSetList.removeAll(deleteItems);
        seq.clearEmptyItemSets();

        return seq;
    }


    /**
     * 获取序列中最后一个项集的最后1个元素
     * @return 最后一个元素
     */
    public Integer getLastItemSetValue() {
        int size = this.getItemSetList().size();
        ItemSet itemSet = this.getItemSetList().get(size - 1);
        size = itemSet.getItems().size();

        return itemSet.getItems().get(size - 1);
    }


    /**
     * 判断list2是否是list1的子序列
     * @param list1 父list
     * @param list2 子list
     * @return boolean
     */
    private boolean arrayContains(List<Integer> list1, List<Integer> list2){
        boolean isContained = false;

        for (int i = 0; i < list1.size() - list2.size() + 1; i++) {
            isContained = true;

            for (int j = 0, k = i; j < list2.size(); j++, k++) {
                if (!list1.get(k).equals(list2.get(j))) {
                    isContained = false;
                    break;
                }
            }

            if (isContained) {
                break;
            }
        }

        return isContained;
    }

    /**
     * 判断sequence是否为空
     * @return boolean
     */
    public boolean isEmpty(){
        if(this.getItemSetList().size() == 0){
            return true;
        }

        boolean flag = true;
        for (ItemSet itemSet: this.getItemSetList()) {
            if(!itemSet.isEmpty()){
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * 清理sequence中的空itemSet
     */
    public void clearEmptyItemSets(){
        this.getItemSetList().removeIf(ItemSet::isEmpty);
    }

    /**
     * 获取sequence中item种类的个数
     * @return
     */
    public int getItemNums(){
        Set<Integer> items = new HashSet<>();
        for (ItemSet itemSet: this.getItemSetList()) {
            for (Integer intger :itemSet.getItems()) {
                if(!items.contains(intger)){
                    items.add(intger);
                }
            }
        }
        return items.size();
    }


    public boolean getIsSubSequence() {
        return isSubSequence;
    }

    public void setIsSubSequence(boolean subSequence) {
        isSubSequence = subSequence;
    }

    public void addItemSet(ItemSet itemSet){
        this.itemSetList.add(itemSet);
    }

    public int getTrsanctionID() {
        return trsanctionID;
    }

    public void setTrsanctionID(int trsanctionID) {
        this.trsanctionID = trsanctionID;
    }

    public List<ItemSet> getItemSetList() {
        return itemSetList;
    }

    public void setItemSetList(List<ItemSet> itemSetList) {
        this.itemSetList = itemSetList;
    }

    public Integer getSupport() {
        return support;
    }

    public void setSupport(Integer support) {
        this.support = support;
    }


}