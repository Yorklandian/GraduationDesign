package Simulator;

import Simulator.Enums.ContainerState;

import static Simulator.Enums.ContainerState.*;

public class Container {

    private int lastAccessTime;
    private int preWarmedTime;
    private int keepAliveStartTime;
    private ContainerState state = Cold;

    private final Function function;

    public Container(Function function){
        this.function = function;
    }

    public void update_initPreWarm(int time){
        this.state = Warm;
        this.keepAliveStartTime = time;
    }

    public void update_initRun(int time){
        this.state = Running;
        int executeTime = this.function.getColdRunTime();
        this.lastAccessTime = time;
        this.keepAliveStartTime = time + executeTime;
    }

    public void update_WarmToRunning(int time){
        this.state = Running;
        int executeTime = this.function.getWarmRunTime();
        //int memToOccupy = this.function.getMemSize();
        this.lastAccessTime = time;
        this.keepAliveStartTime = time + executeTime;
    }

    public void update_RunningToWarm(){
        this.state = Warm;
    }

    public int update_Terminate(){
        //TODO:可以保留一个对象池
        return this.getFunction().getMemSize();
    }

    public boolean shouldFuncRunFinish(int currentTime){
        return currentTime >= this.keepAliveStartTime;
    }


    public boolean isFree(){
        return this.state == Warm ;
    }

    public boolean isWarm(){
        return this.state == Warm; // || this.state == Cold
    }

    public boolean isRunning(){
        return this.state == Running;
    }

    public Function getFunction() {
        return function;
    }

    public int getLastAccess_t() {
        return lastAccessTime;
    }

    public void setLastAccess_t(int lastAccess_t) {
        this.lastAccessTime = lastAccess_t;
    }

    public int getPreWarmedTime() {
        return preWarmedTime;
    }

    public void setPreWarmedTime(int preWarmedTime) {
        this.preWarmedTime = preWarmedTime;
    }

    public int getKeepAliveStartTime() {
        return keepAliveStartTime;
    }

    public void setKeepAliveStartTime(int keepAliveStartTime) {
        this.keepAliveStartTime = keepAliveStartTime;
    }

    public ContainerState getState() {
        return state;
    }

    public void setState(ContainerState state) {
        this.state = state;
    }
}
