#!/bin/bash
##java -cp /usr/share/java/mysql.jar:/home/MECPerf-masterObserver/Aggregator.jar it.unipi.dii.aggregator.Aggregator --database-ip "" --database-name "" --database-user "" --database-password "" --aggregator-port 6766

# Database and aggregator on node 11 
##java -cp /usr/share/java/mysql.jar:/home/MECPerf/MECPerf-master/Aggregator.jar it.unipi.dii.aggregator.Aggregator --database-ip "192.168.3.11" --database-name "MECPerf" --database-user "MECPerf" --database-password "psw" --aggregator-port 6766
# Database 127.0.0.1
java -cp /usr/share/java/mysql.jar:/home/MECPerf/MECPerf-master/Aggregator.jar it.unipi.dii.aggregator.Aggregator --database-ip "127.0.0.1" --database-name "MECPerf" --database-user "MECPerf" --database-password "psw" --aggregator-port 6766
# Database and aggregator on node 8
## java -cp /usr/share/java/mysql.jar:/home/MECPerf/MECPerf-master/Aggregator.jar it.unipi.dii.aggregator.Aggregator --database-ip "192.168.3.8" --database-name "MECPerf" --database-user "MECPerf" --database-password "psw" --aggregator-port 6766

