#export JAVA_HOME=/opt/jdk-17.0.2
#export PATH=$JAVA_HOME/bin:$PATH

java -cp ./av-bitcoin-binance/target/*:./av-bitcoin-binance/target/libs/* av.bitcoin.binance.AppMain
