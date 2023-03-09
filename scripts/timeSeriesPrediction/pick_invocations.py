import os
import pandas as pd

datapath = "C:\\Users\\Administrator\\Desktop\\Cloud\\Azure2019Data\\Azure2019\\"
invocation_prefix = "invocations_per_function_md.anon"
suffixes = [".d01.csv",".d02.csv",".d03.csv",".d04.csv",".d05.csv",".d06.csv",
            ".d07.csv",".d08.csv",".d09.csv",".d10.csv",".d11.csv",".d12.csv"]
high_cost_function_names = ["f1de419dc75ea0f629deaf936e0b65934cbf2bc444ffd7b3116e3a19dd108f11",
                            "5608f70ad5c4f89e83b01f37bdabd7b89f79338b34776128d68676d83cd3de15",
                            "6dc157fa79f808bd7cf577af09efaee2dad035d658e549d4219e705a181d8917",
                            "3a7e7d0856fa781c7b04c5c45fb622a8eb794a0f4b84b64807c111fa8e423d22",
                            "f4dc04b1dd73316e1b916468f43a0327467320152c4d1be823279fcf2b1621cc"]

res = "D:\\data\\representative\\invocation_percentiles"

def get_one_day_data(path:str):
    invocations = pd.read_csv(path)
    invocations = invocations.dropna()
    invocations.index = invocations["HashFunction"]
    functions = []
    for name in high_cost_function_names:
        if name in invocations.index.values:
            functions.append(name)
    print(day_suffix, ': ', len(functions))
    all_func_data = invocations.loc[functions]
    # for func_name in high_cost_function_names:
    #     func_data = invocations.loc[func_name]
    #     all_func_data = pd.concat([all_func_data,func_data])
    return all_func_data


for day_suffix in suffixes:
    file_path = os.path.join(datapath, invocation_prefix+day_suffix)
    res_path = os.path.join(res,"invocations"+day_suffix)
    data_for_a_day = get_one_day_data(file_path)
    data_for_a_day.to_csv(res_path)


#day1.to_csv("test.csv")