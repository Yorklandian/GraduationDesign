import os
import pandas as pd
import numpy as np
import scipy as sp
import matplotlib.pyplot as plt
from sklearn.preprocessing import MinMaxScaler
from keras.layers import LSTM
from keras.layers import Dense
from keras.models import Sequential
from sklearn.metrics import mean_squared_error
import numpy as np
import math
high_cost_function_names = ["f1de419dc75ea0f629deaf936e0b65934cbf2bc444ffd7b3116e3a19dd108f11",
                            "5608f70ad5c4f89e83b01f37bdabd7b89f79338b34776128d68676d83cd3de15",
                            "6dc157fa79f808bd7cf577af09efaee2dad035d658e549d4219e705a181d8917",
                            "3a7e7d0856fa781c7b04c5c45fb622a8eb794a0f4b84b64807c111fa8e423d22",
                            "f4dc04b1dd73316e1b916468f43a0327467320152c4d1be823279fcf2b1621cc"]

suffixes = [".d01.csv",".d02.csv",".d03.csv",".d04.csv",".d05.csv",".d06.csv",
            ".d07.csv",".d08.csv",".d09.csv",".d10.csv",".d11.csv",".d12.csv"]

res = "D:\\data\\representative\\invocation_percentiles"

#数据构建部分
#从csv中读取数据
def readData(name:str):
    vals = []
    for suffix in suffixes:
        one_day_data_path = os.path.join(res,"invocations"+suffix)
        df = pd.read_csv(one_day_data_path)
        df.index = df["HashFunction"]
        val = df.loc[name,"1":"1440"]
        val = val.values.tolist()
        vals = np.append(vals,val)
    return vals

# 进行差分转换，将数据转换为其自身与前一个数据之差
def difference(data_set: list,interval=1):
    diff=list()
    for i in range(interval,len(data_set)):
        value=data_set[i]-data_set[i-interval]
        if value > 250 or value < -250:
            value = diff[len(diff)-1]
        diff.append(value)
    return diff


#将时间序列形式的数据转换为监督学习集的形式，例如：[[10],[11],[12],[13],[14]]转换为[[0,10],[10,11],[11,12],[12,13],[13,14]]，即把前一个数作为输入，后一个数作为对应输出
def timeseries_to_supervised(data:list,lag=1):
    df=pd.DataFrame(data)
    columns=[df.shift(1)]
    columns.append(df)
    df=pd.concat(columns,axis=1)
    df.fillna(0,inplace=True)
    return df

#将训练集和测试集中的数据都缩放到[-1,1]之间，可以加快收敛。
def scale(train):
    # 创建一个缩放器，将数据集中的数据缩放到[-1,1]的取值范围中
    scaler=MinMaxScaler(feature_range=(-1,1))
    # 使用数据来训练缩放器
    scaler=scaler.fit(train)
    # 使用缩放器来将训练集和测试集进行缩放
    train_scaled=scaler.transform(train)
    return scaler,train_scaled

#模型训练部分

def fit_lstm(train,batch_size,nb_epoch,neurons):
    # 将数据对中的x和y分开
    X,y=train[:,0:-1],train[:,-1]
    # 将2D数据拼接成3D数据，形状为[N*1*1]
    X=X.reshape(X.shape[0],1,X.shape[1])

    model=Sequential()
    model.add(LSTM(neurons,batch_input_shape=(batch_size,X.shape[1],X.shape[2]),stateful=True))
    model.add(Dense(1))
    model.compile(loss='mean_squared_error',optimizer='adam')
    for i in range(nb_epoch):
        # shuffle是不混淆数据顺序
        his=model.fit(X,y,batch_size=batch_size,verbose=1,shuffle=False)
        # 每训练完一次就重置一次网络状态，网络状态与网络权重不同
        model.reset_states()
    return model

#将一条数据的输入和输出列分开，并且将输入进行变换，传入到预测函数中进行单步预测
def forecast_lstm(model,batch_size,X):
    # 将形状为[1:]的，包含一个元素的一维数组X，转换形状为[1,1,1]的3D张量
    X=X.reshape(1,1,len(X))
    # 输出形状为1行一列的二维数组yhat
    yhat=model.predict(X,batch_size=batch_size)
    # 将yhat中的结果返回
    return yhat[0,0]

#得到预测值后对其进行逆缩放和逆差分，将其还原到原来的取值范围内
# 对预测的数据进行逆差分转换
def invert_difference(history,yhat,interval=1):
    return yhat+history[-interval]

# 将预测值进行逆缩放，使用之前训练好的缩放器，x为一维数组，y为实数
def invert_scale(scaler,X,y):
    # 将X,y转换为一个list列表
    new_row=[x for x in X]+[y]
    # 将列表转换为数组
    array=np.array(new_row)
    # 将数组重构成一个形状为[1,2]的二维数组->[[10,12]]
    array=array.reshape(1,len(array))
    # 逆缩放输入的形状为[1,2]，输出形状也是如此
    invert=scaler.inverse_transform(array)
    # 只需要返回y值即可
    return invert[0,-1]

invokes = readData(high_cost_function_names[0])
minutes = [i for i in range(1440 * 12 - 1)]
diff_value = difference(invokes)

# plt.plot(minutes,diff_value)
# plt.show()
supervised=timeseries_to_supervised(diff_value,1)
supervised_value=supervised.values


train=supervised_value

scaler,train_scaled=scale(train)

lstm_model=fit_lstm(train_scaled,1,1,4)

predictions=list()

val = train[len(train)-1,0:-1]
input = np.array([val])
for i in range(50):
    print("input:",input)
    yhat=forecast_lstm(lstm_model,1,input)
    y=invert_scale(scaler,input,yhat)
    print("output:",y)
    
    input = np.array([int(y)])
    # 对预测的y值进行逆差分
    #yhat=invert_difference(invokes,yhat,len(test_scaled)+1-i)
    # 存储正在预测的y值
    predictions.append(int(y))

plt.plot(predictions)
plt.show()
# print(predictions)
# predictions = sp.signal.savgol_filter(predictions,59,3)
# # 计算方差
# rmse=mean_squared_error(invokes[:testNum],predictions)
# print("Test RMSE:",rmse)
# plt.plot(invokes[-testNum:])
# plt.plot(predictions)
# plt.legend(['true','pred'])
# plt.show()


