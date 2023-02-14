package FSM;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 序列中的子项集
 *
 */
public class ItemSet {
    /**
     * Item 类型默认使用integer，在日志读取阶段默认将String类型的Entity映射到integer
     * 这样能显著降低内存消耗，提升排序等处理的效率
     *
     * 项集中保存的是数字项数组
     */
    private List<Integer> items = new ArrayList<>();

    /**
     * 每个itemSet有其对应的时间，默认以秒为单位，用于进行时间约束计算。
     */
    private double time = 0.0f;

    public ItemSet(Integer item, double time){
        this.items.add(item);
        this.time = time;
    }

    public ItemSet(String[] itemStr, double time) {
        for (String s : itemStr) {
            items.add(Integer.parseInt(s));
        }
        this.time = time;
    }

    public ItemSet(List<Integer> items, double time){
        this.items.addAll(items);
        this.time = time;
    }


    public ItemSet(int[] itemNum, double time) {
        for (int num : itemNum) {
            items.add(num);
        }
        this.time = time;
    }

    public ItemSet(String[] itemStr) {
        for (String s : itemStr) {
            items.add(Integer.parseInt(s));
        }
    }

    public ItemSet(int[] itemNum) {
        for (int num : itemNum) {
            items.add(num);
        }
    }

    public ItemSet(List<Integer> itemNum) {
        this.items = itemNum;
    }


    public List<Integer> getItems() {
        return items;
    }

    public void setItems(ArrayList<Integer> items) {
        this.items = items;
    }

    public double getTime() {
        return time;
    }

    public void setTime(Float time) {
        this.time = time;
    }

    public void addItem(Integer item){
        this.items.add(item);
    }

    /**
     * 判断2个项集是否相等，只比较项集中的对象，不比较时间
     *
     * @param itemSet
     *            比较对象
     * @return boolean
     */
    public boolean compareItems(ItemSet itemSet) {
        boolean result = true;

        if (this.items.size() != itemSet.items.size()) {
            return false;
        }

        for (int i = 0; i < itemSet.items.size(); i++) {
            if (!Objects.equals(this.items.get(i), itemSet.items.get(i))) {
                // 只要有值不相等，直接算作不相等
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * 拷贝项集中同样的数据一份
     *
     * @return
     */
    public List<Integer> copyItems() {

        return new ArrayList<>(this.items);
    }
}