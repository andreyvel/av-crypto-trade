#export JAVA_HOME=/opt/jdk-17.0.2
#export PATH=$JAVA_HOME/bin:$PATH

java -cp ./av-bitcoin-emulator/target/*:./av-bitcoin-emulator/target/libs/* av.bitcoin.emulator.AppRandomQuote
