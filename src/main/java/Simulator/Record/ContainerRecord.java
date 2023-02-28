package Simulator.Record;

public class ContainerRecord {
    private int minute = 0;
    private int autoDieCount = 0;
    private int evictCount = 0;

    public void increaseAutoDie(){
        this.autoDieCount++;
    }

    public void increaseEvict(){
        this.evictCount++;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getAutoDieCount() {
        return autoDieCount;
    }

    public void setAutoDieCount(int autoDieCount) {
        this.autoDieCount = autoDieCount;
    }

    public int getEvictCount() {
        return evictCount;
    }

    public void setEvictCount(int evictCount) {
        this.evictCount = evictCount;
    }
}
