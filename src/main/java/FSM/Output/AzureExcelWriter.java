package FSM.Output;

import FSM.ItemSet;
import FSM.Sequence;
import FSM.tools.AzureApp;
import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

public class AzureExcelWriter {

    private String excelFilePath;

    private List<AzureApp> apps;

    public AzureExcelWriter(String excelFilePath, List<AzureApp> apps) {
        this.excelFilePath = excelFilePath;
        this.apps = apps;
    }

    public void writeToExcel(){
        String fileName = excelFilePath;

        EasyExcel.write(fileName).sheet("test").doWrite(generateDataList());
    }

    private List<List<String>> head(){
        List<List<String>> list = new ArrayList<>();
        List<String> appName = new ArrayList<>();
        appName.add("name");
        list.add(appName);
        return list;
    }

    /**
     * 生成所有data
     * @return 所有data的list
     */
    private List<List<Object>> generateDataList(){
        List<List<Object>> dataList = new ArrayList<>();
        for (AzureApp app: apps){
            List<Object> data = generateData(app);
            //无频繁序列的app不生成data
            if(data.size() == 0){
                continue;
            }
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 生成一个app对应的一行data
     * @param app 对应的Azure app
     * @return 表示一行数据的list
     */
    private List<Object> generateData(AzureApp app){
        List<Object> data = new ArrayList<>();

        //无频繁序列的app不生成data
        if(app.getFrequentSequenceList().size() == 0){
            return data;
        }

        data.add(app.getName());
        for (Sequence sequence: app.getFrequentSequenceList()){
            List<ItemSet> itemSets = sequence.getItemSetList();
            StringBuilder sb = new StringBuilder();
            for (ItemSet itemSet : itemSets) {
                sb.append("(");
                ItemSet set = itemSet;
                int size = set.getItems().size();
                for (int j = 0; j < size; j++) {
                    sb.append(set.getItems().get(j));
                    if (j < size - 1) {
                        sb.append(",");
                    }
                }
                sb.append(")");
            }
            String str = sb.toString();
            data.add(str);
            data.add(sequence.getSupport());
        }
        return data;
    }

    public String getExcelFilePath() {
        return excelFilePath;
    }

    public void setExcelFilePath(String excelFilePath) {
        this.excelFilePath = excelFilePath;
    }

    public List<AzureApp> getApps() {
        return apps;
    }

    public void setApps(List<AzureApp> apps) {
        this.apps = apps;
    }

}
