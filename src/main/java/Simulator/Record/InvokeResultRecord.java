package Simulator.Record;

public class InvokeResultRecord {
    private int warmStartTime = 0;
    private int coldStartTime = 0;
    private int queueFullDropTime = 0;
    private int ttlDropTime = 0;


    public void increaseWarmStartTime(){
        this.warmStartTime++;
    }
    public void increaseColdStartTime(){
        this.coldStartTime++;
    }
    public void increaseQueueDropTime(){
        this.queueFullDropTime++;
    }
    public void increaseTTLDropTime(){
        this.ttlDropTime++;
    }

    public int getWarmStartTime() {
        return warmStartTime;
    }

    public void setWarmStartTime(int warmStartTime) {
        this.warmStartTime = warmStartTime;
    }

    public int getColdStartTime() {
        return coldStartTime;
    }

    public void setColdStartTime(int coldStartTime) {
        this.coldStartTime = coldStartTime;
    }

    public int getQueueFullDropTime() {
        return queueFullDropTime;
    }

    public void setQueueFullDropTime(int queueFullDropTime) {
        this.queueFullDropTime = queueFullDropTime;
    }

    public int getTtlDropTime() {
        return ttlDropTime;
    }

    public void setTtlDropTime(int ttlDropTime) {
        this.ttlDropTime = ttlDropTime;
    }
}
