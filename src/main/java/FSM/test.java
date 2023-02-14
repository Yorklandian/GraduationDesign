package FSM;

import FSM.tools.AzureTraceTool;

public class test {
    /*public static void main(String[] args) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = "2023-02-10 16:09:00";
        Date date = dateFormatter.parse(dateString);
        long timeInSec = date.getTime()/1000;
        System.out.println(timeInSec);
        double time2Float = (double) timeInSec;
        System.out.println(time2Float);
    }*/

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Administrator\\Desktop\\Cloud\\AzureFunctionsInvocationTraceForTwoWeeksJan2021\\AzureFunctionsInvocationTraceForTwoWeeksJan2021.txt";
        String outputFilePath = "C:\\Users\\Administrator\\Desktop\\Cloud\\output.txt";
        //最小支持度阈值
        int minSupportCount = 2;
        //时间最小间隔
        double min_gap = 0.1;
        //施加最大间隔
        double max_gap = 5;

        AzureTraceTool tool = new AzureTraceTool(filePath,outputFilePath);
        tool.setTimeLimit(10);
        tool.start();
    }
}
