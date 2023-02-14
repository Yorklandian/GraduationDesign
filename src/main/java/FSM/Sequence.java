package FSM;

import java.util.ArrayList;
import java.util.List;

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

    public void setItemSetList(ArrayList<ItemSet> itemSetList) {
        this.itemSetList = itemSetList;
    }

    public Integer getSupport() {
        return support;
    }

    public void setSupport(Integer support) {
        this.support = support;
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

    public boolean getIsSubSequence() {
        return isSubSequence;
    }

    public void setIsSubSequence(boolean subSequence) {
        isSubSequence = subSequence;
    }

}