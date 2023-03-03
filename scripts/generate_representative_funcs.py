import os
import pandas as pd
from multiprocessing import Pool
from math import ceil
pd.set_option('display.max_rows',1000)

func_path = "D:\\data\\representative\\functions.csv"
invoke_path = "D:\\data\\representative\\invokes.csv"
buckets = [str(i) for i in range(1, 1441)]

datapath = "C:\\Users\\Administrator\\Desktop\\Cloud\\Azure2019Data\\Azure2019\\"
durations = "function_durations_percentiles.anon.d01.csv"
invocations = "invocations_per_function_md.anon.d01.csv"
mem_fnames = "app_memory_percentiles.anon.d01.csv"

quantiles = [0.0, 0.25, 0.5, 0.75, 1.0]

#从4个quantile中一共取出num_funcs个函数
def gen_trace(df: pd.DataFrame, num_funcs: int):
    per_qant_func_num = num_funcs // 4
    sums = df["InvocationSum"]
    qts = sums.quantile(quantiles)

    chooseRes = pd.DataFrame()
    for i in range(4):
        low = qts.iloc[i]
        high = qts.iloc[i+1]
        choose_from = df[sums.between(low, high)]
        #从每个quantile中随机采样 per_quant_func_num个函数
        chosen = choose_from.sample(per_qant_func_num)
        chooseRes = pd.concat([chooseRes,chosen])
    chooseRes.to_csv(func_path)
    generate_invoke_df(chooseRes)


#生成函数调用data frame
def generate_invoke_df(chooseRes: pd.DataFrame):
    trace = list()

    for row_index, row in chooseRes.iterrows():     
        func_name = row["HashFunction"]
        #遍历1440分钟 minute：第几分钟， invokeCount：此分钟调用次数
        for minute, invokeCount in enumerate(row[buckets]):
            start = minute * 60 * 1000
            if invokeCount == 0:
                continue
            elif invokeCount == 1:
                trace.append([func_name,start])
            else:
                gap = int(60000/invokeCount)
                for i in range(invokeCount):
                    trace.append([func_name,start + i * gap]) #调用一次则为此分钟初始时调用，多次则此分钟均分
    df = pd.DataFrame(columns=["name","time"],data=trace)
    df = df.sort_values(by="time",ascending=True)
    df.to_csv(invoke_path)
    


def gen_traces(func_nums):
    global durations
    global invocations
    global memory

    def divive_by_func_num(row):
        return ceil(row["AverageAllocatedMb"] / group_by_app[row["HashApp"]])

    #处理 duration
    file = os.path.join(datapath, durations)
    durations = pd.read_csv(file)
    durations.index = durations["HashFunction"]
    durations = durations.drop_duplicates("HashFunction")

    group_by_app = durations.groupby("HashApp").size()

    #处理invocation
    file = os.path.join(datapath, invocations)
    invocations = pd.read_csv(file)
    invocations = invocations.dropna()
    invocations.index = invocations["HashFunction"]
    sums = invocations.sum(axis=1)
    invocations["InvocationSum"] = sums

    invocations = invocations[sums > 1] # action must be invoked at least twice
    invocations = invocations.drop_duplicates("HashFunction")


    joined = invocations.join(durations, how="inner", lsuffix='', rsuffix='_durs')

    #处理memory
    file = os.path.join(datapath, mem_fnames.format(1))
    memory = pd.read_csv(file)
    memory = memory.drop_duplicates("HashApp")
    memory.index = memory["HashApp"]

    new_mem = memory.apply(divive_by_func_num, axis=1, raw=False, result_type='expand')
    memory["divvied"] = new_mem

    joined = joined.join(memory, how="inner", on="HashApp", lsuffix='', rsuffix='_mems')
    joined = joined.T.drop_duplicates().T.reindex() #去除重复的列

    gen_trace(joined,func_nums)

# if os.path.exists(func_path):
#     os.remove(func_path)

# if os.path.exists(invoke_path):
#     os.remove(invoke_path)        
# gen_traces(400)

df = pd.read_csv(func_path)
df["InvocationSum"] = df.iloc[:,5:1445].sum(axis=1)
#print(df["Count"])
#print(df["InvocationSum"])
# generate_invoke_df(df)

