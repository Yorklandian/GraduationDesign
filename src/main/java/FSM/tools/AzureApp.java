package FSM.tools;

import FSM.Filters.SequenceFilter;
import FSM.ItemSet;
import FSM.Sequence;

import java.util.*;

public class AzureApp {
    private String name;

    //标记此app是否通过过滤条件，默认为true，若不满足则无需对其进行处理
    private boolean valid = true;

    private List<ItemSet> itemSetList = new ArrayList<>();
    private List<Sequence> sequenceList = new ArrayList<>();
    private Map<String, Integer> entity2IdMap = new HashMap<>();
    private Map<Integer, String> id2EntityMap = new HashMap<>();

    private List<Sequence> frequentSequenceList = new ArrayList<>();

    public AzureApp(String name) {
        this.name = name;
    }

    /**
     * 初始sequence过滤器，将原始sequencelist中不满足条件的sequence过滤掉
     * @return
     */
    public boolean doFilter(){
        SequenceFilter.filterReplicateSequence(this.getSequenceList());
        //只有一个item（function）的app，不做挖掘
        if(this.getEntity2IdMap().size() <= 1){
            this.valid = false;
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public List<ItemSet> getItemSetList() {
        return itemSetList;
    }

    public void setItemSetList(List<ItemSet> itemSetList) {
        this.itemSetList = itemSetList;
    }

    public List<Sequence> getSequenceList() {
        return sequenceList;
    }

    public void setSequenceList(List<Sequence> sequenceList) {
        this.sequenceList = sequenceList;
    }

    public Map<String, Integer> getEntity2IdMap() {
        return entity2IdMap;
    }

    public void setEntity2IdMap(Map<String, Integer> entity2IdMap) {
        this.entity2IdMap = entity2IdMap;
    }

    public Map<Integer, String> getId2EntityMap() {
        return id2EntityMap;
    }

    public void setId2EntityMap(Map<Integer, String> id2EntityMap) {
        this.id2EntityMap = id2EntityMap;
    }

    public List<Sequence> getFrequentSequenceList() {
        return frequentSequenceList;
    }

    public void setFrequentSequenceList(List<Sequence> frequentSequenceList) {
        this.frequentSequenceList = frequentSequenceList;
    }
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

}
