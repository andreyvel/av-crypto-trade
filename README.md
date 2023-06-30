# av-bitcoin-trade-gui

Java trade GUI client for bitcoin trade

## Features
- Support creating limit orders on the chart (drag red runner and press Spacebar)
- Support display and canceling limit orders on the chart
- Displays fired order on the chart
- ZMQ API for displaying custom elements on chart
- Simple API for integrating any trade platform
- Supports binance-connector-java for binance clients
- This software can be used to visualize trading bot orders.

## How to start

### Build project
- Configure java environment (install java jdk-17 or higher)
  - export JAVA_HOME=/opt/jdk-20.0.1
  - export PATH=$JAVA_HOME/bin:$PATH
- Run ./mvn-build.sh for creating jar files from sources

### Start quote proxy server
- Configure av-bitcoin-emulator.yaml and run ./av-bitcoin-emulator.sh<br>
  emulator generate random bitcoin quotes, it used for local GUI client testing
- or
- Configure av-bitcoin-binance.yaml and run ./av-bitcoin-binance.sh
  this app used binance-connector-java for data exchange

### Start GUI client
- Check proxy server REST API http://localhost:8089
- Configure av-bitcoin-trade-gui.yaml and run ./av-bitcoin-trade-gui.sh


Chart panel
![Chart panel](docs/chart.png)

Report panel
![Report panel](docs/report.png)
