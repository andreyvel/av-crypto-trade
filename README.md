# av-bitcoin-trade-gui

Linux GUI client for bitcoin trading, supports Binance API

## Features
- Supports creating limit orders on the chart (drag red runner and press Spacebar)
- It is possible to create new order or to cancel it directly on the chart
- Displays filled orders on the chart
- ZMQ API for displaying custom elements on chart (points, lines)
- Simple API for integrating into any trade platform
- Supports binance-api by using binance-connector-java
- This software can be used to visualize auto trading.

## How to start

### Build project
- Configure java environment (install java jdk-17 or higher)
  - export JAVA_HOME=/opt/jdk-17.0.2
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

Example how advice points displayed on the chart
![Chart panel](docs/advice.png)

Report panel
![Report panel](docs/report.png)
