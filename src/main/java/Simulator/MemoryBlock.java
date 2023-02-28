package Simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MemoryBlock {
    int capacity;
    int memUsed = 0;
    String functionNameForThisBlock = null;

    public static int maxQueueLength = 100000;// a queue can contain at most 100,000 messages
    public static int messageTTL = 100; //the longest time a message can unit: ms

    private List<Container> containerPool = new ArrayList<>();
    private Queue<FunctionInvoke> messageQueue = new LinkedList<>();

    public MemoryBlock(int capacity) {
        this.capacity = capacity;
    }

    public MemoryBlock(int capacity, String functionNameForThisBlock) {
        this.capacity = capacity;
        this.functionNameForThisBlock = functionNameForThisBlock;
    }

    public void increaseMemUsed(int increase) {
        this.memUsed += increase;
        if(memUsed > capacity){
            memUsed = capacity;
        }
    }

    public void decreaseMemUsed(int decrease){
        this.memUsed -= decrease;
        if(memUsed < 0){
            memUsed = 0;
        }
    }

    public void offerMessage(FunctionInvoke invoke){
        this.messageQueue.offer(invoke);
    }

    public FunctionInvoke peekMessage(){
        return this.messageQueue.peek();
    }

    public FunctionInvoke pollMessage(){
        return this.messageQueue.poll();
    }

    public int getMessageQueueLength(){
        return this.messageQueue.size();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getMemUsed() {
        return memUsed;
    }

    public void setMemUsed(int memUsed) {
        this.memUsed = memUsed;
    }

    public List<Container> getContainerPool() {
        return containerPool;
    }

    public void setContainerPool(List<Container> containerPool) {
        this.containerPool = containerPool;
    }
}
