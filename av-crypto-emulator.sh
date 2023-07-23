#export JAVA_HOME=/opt/jdk-17.0.2
#export PATH=$JAVA_HOME/bin:$PATH

java -cp ./av-crypto-emulator/target/*:./av-crypto-emulator/target/libs/* av.crypto.emulator.AppRandomQuote
