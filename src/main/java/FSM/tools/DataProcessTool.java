package FSM.tools;

import FSM.ItemSet;
import FSM.Sequence;

import java.util.List;

public interface DataProcessTool {
    public void readFileFromLog();
    public void start();
    public void outputToFile(List<Sequence> resultSequenceList);
    public void setTimeLimit(int timeLimit);
    public void generateSequences(List<ItemSet> itemSetList, List<Sequence> sequenceList);
    public List<Sequence> getSequenceList();
    public List<Sequence> getSequenceList(String appID);
}
