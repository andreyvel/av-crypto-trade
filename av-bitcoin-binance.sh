#export JAVA_HOME=/opt/jdk-20.0.1
#export PATH=$JAVA_HOME/bin:$PATH

java -cp ./av-bitcoin-binance/target/*:./av-bitcoin-binance/target/libs/* av.bitcoin.binance.AppMain
