#export JAVA_HOME=/opt/jdk-20.0.1
#export PATH=$JAVA_HOME/bin:$PATH

java -cp ./av-bitcoin-emulator/target/*:./av-bitcoin-emulator/target/libs/* av.bitcoin.emulator.AppRandomQuote
