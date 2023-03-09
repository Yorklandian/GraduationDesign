import numpy
import matplotlib.pyplot as plt
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import LSTM
from sklearn.preprocessing import MinMaxScaler
import  pandas as pd
import  os
from keras.models import Sequential

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
    vals = pd.DataFrame()
    for suffix in suffixes:
        one_day_data_path = os.path.join(res,"invocations"+suffix)
        df = pd.read_csv(one_day_data_path)
        df.index = df["HashFunction"]
        val = df.loc[name,"1":"1440"]
        vals = pd.concat([vals,val])
    return vals.values


def create_dataset(dataset, look_back):
#这里的look_back与timestep相同
    dataX, dataY = [], []
    for i in range(len(dataset)-look_back-1):
        a = dataset[i:(i+look_back)]
        dataX.append(a)
        dataY.append(dataset[i + look_back])
    return numpy.array(dataX),numpy.array(dataY)
#训练数据太少 look_back并不能过大


# dataframe = pd.read_csv('C:\\Users\\Administrator\\Desktop\\Cloud\\pythonScripts\\international-airline-passengers.csv',usecols=[1], engine='python', skipfooter=3)
# dataset = dataframe.values

dataset = readData(high_cost_function_names[0])
print(dataset)
# 将整型变为float
dataset = dataset.astype('float32')
#归一化 在下一步会讲解
scaler = MinMaxScaler(feature_range=(0, 1))
dataset = scaler.fit_transform(dataset)

train_size = int(len(dataset) * 0.65)
trainlist = dataset[:train_size]
testlist = dataset[train_size:]


look_back = 10
trainX,trainY  = create_dataset(trainlist,look_back)
testX,testY = create_dataset(testlist,look_back)

trainX = numpy.reshape(trainX, (trainX.shape[0], trainX.shape[1], 1))
testX = numpy.reshape(testX, (testX.shape[0], testX.shape[1] ,1 ))

# create and fit the LSTM network
model = Sequential()
model.add(LSTM(4, input_shape=(None,1)))
model.add(Dense(1))
model.compile(loss='mean_squared_error', optimizer='adam')
model.fit(trainX, trainY, epochs=20, batch_size=1, verbose=2)
model.save(os.path.join("DATA","Test" + ".h5"))

#model = load_model(os.path.join("DATA","Test" + ".h5"))
trainPredict = model.predict(trainX)
testPredict = model.predict(testX)

#反归一化
trainPredict = scaler.inverse_transform(trainPredict)
trainY = scaler.inverse_transform(trainY)
testPredict = scaler.inverse_transform(testPredict)
testY = scaler.inverse_transform(testY)

plt.plot(trainY)
plt.plot(trainPredict[1:])
plt.show()
plt.plot(testY)
plt.plot(testPredict[1:])
plt.show()