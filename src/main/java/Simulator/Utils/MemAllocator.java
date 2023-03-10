package Simulator.Utils;

import Simulator.MemoryBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemAllocator {
    //主内存块
    private MemoryBlock mainMemBlock;
    private int memCapacity;
    //独立内存块
    private Map<String,MemoryBlock> seperatedMemBlocksMap = new HashMap<>();
    private int maxSepMemBlockCapacity;

    private Map<String, List<Integer>> predictionData = new HashMap<>();


    private void dynamicInit(){

    }

    private void dynamicScale(int minute){

    }

}
