package Simulator;

public class Function {


    private int memSize;
    private int coldRunTime;
    private int warmRunTime;
    private int invocationCount;
    private int score;
    private String appName;
    private String name;


    public Function(int memSize, int coldRunTime, int warmRunTime, String name, String appName, int invocationCount) {
        this.memSize = memSize;
        this.coldRunTime = coldRunTime;
        this.warmRunTime = warmRunTime;
        this.name = name;
        this.appName = appName;
        this.invocationCount = invocationCount;
        this.score = invocationCount * memSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMemSize() {
        return memSize;
    }

    public void setMemSize(int memSize) {
        this.memSize = memSize;
    }

    public int getColdRunTime() {
        return coldRunTime;
    }

    public void setColdRunTime(int coldRunTime) {
        this.coldRunTime = coldRunTime;
    }

    public int getWarmRunTime() {
        return warmRunTime;
    }

    public void setWarmRunTime(int warmRunTime) {
        this.warmRunTime = warmRunTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getInvocationCount() {
        return invocationCount;
    }

    public void setInvocationCount(int invocationCount) {
        this.invocationCount = invocationCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
