package Simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 内存块实体类，存有属于此内存块的容器池和消息队列
 */
public class MemoryBlock {
    /**
     * 内存块容量
     */
    private int capacity;
    /**
     * 已使用的内存
     */
    private int memUsed = 0;
    /**
     * 属于此空间的函数名，若是主内存块，则一直为null
     */
    private String functionNameForThisBlock = null;
    /**
     * 内存块中的最大消息队列长度
     */
    public static int maxQueueLength = 100000;// a queue can contain at most 100,000 messages
    /**
     * 消息过期时间
     */
    public static int messageTTL = 10; //the longest time a message can unit: ms


    /**
     * 属于此内存空间的容器池
     */
    private List<Container> containerPool = new ArrayList<>();
    /**
     * 属于此内存空间的消息队列
     */
    private Queue<FunctionInvoke> messageQueue = new LinkedList<>();

    public MemoryBlock(int capacity) {
        this.capacity = capacity;
    }

    public MemoryBlock(int capacity, String functionNameForThisBlock) {
        this.capacity = capacity;
        this.functionNameForThisBlock = functionNameForThisBlock;
    }

    /**
     * 增加内存占用量
     * @param increase 增加量
     */
    public void increaseMemUsed(int increase) {
        this.memUsed += increase;
        if(memUsed > capacity){
            memUsed = capacity;
        }
    }

    /**
     * 减少内存占用量
     * @param decrease 减少量
     */
    public void decreaseMemUsed(int decrease){
        this.memUsed -= decrease;
        if(memUsed < 0){
            memUsed = 0;
        }
    }

    /**
     * 消息入队
     * @param invoke 要入队的消息
     */
    public void offerMessage(FunctionInvoke invoke){
        this.messageQueue.offer(invoke);
    }

    /**
     * 查看队首消息
     * @return 队首的消息
     */
    public FunctionInvoke peekMessage(){
        return this.messageQueue.peek();
    }

    /**
     * 队首消息出队
     * @return 出队的消息
     */
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
