package Simulator;

public class FunctionInvoke {
    private String functionName;
    private int invokeTime;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public int getInvokeTime() {
        return invokeTime;
    }

    public void setInvokeTime(int invokeTime) {
        this.invokeTime = invokeTime;
    }

    public FunctionInvoke(String functionName, int invokeTime) {
        this.functionName = functionName;
        this.invokeTime = invokeTime;
    }
}

