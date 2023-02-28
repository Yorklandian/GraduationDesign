import pandas as pd

dataPath = "E:\\data\\representative\\results\\invokeRes.csv"

df = pd.read_csv(dataPath)

warm_sum = df["warm"].sum()
cold_sum = df["cold"].sum()
drop_sum = df["drop"].sum()

print(warm_sum)
print(cold_sum)
print(drop_sum)