#!/bin/bash
#java -jar Observer.jar --aggregator-ip "192.168.122.3" --aggregator-port 6766 --remote-ip  "192.168.122.4"  --remote-cmd-port 6789 --remote-tcp-port 6788 --remote-udp-port 6787 --observer-cmd-port 6792 --observer-tcp-port 6791 --observer-udp-port 6790

#R2LAB correct
# Aggregator node 11
#java -jar Observer.jar --aggregator-ip "192.168.3.11" --aggregator-port 6766 --remote-ip  "192.168.3.11"  --remote-cmd-port 6789 --remote-tcp-port 6788 --remote-udp-port 6787 --observer-cmd-port 6792 --observer-tcp-port 6791 --observer-udp-port 6790

# Aggregator node 8
##java -jar Observer.jar --aggregator-ip "192.168.3.8" --aggregator-port 6766 --remote-ip  "192.168.3.11"  --remote-cmd-port 6789 --remote-tcp-port 6788 --remote-udp-port 6787 --observer-cmd-port 6792 --observer-tcp-port 6791 --observer-udp-port 6790

# Aggregator node 17, rms 26
java -jar Observer.jar --aggregator-ip "192.168.3.17" --aggregator-port 6766 --remote-ip  "192.168.3.26"  --remote-cmd-port 6789 --remote-tcp-port 6788 --remote-udp-port 6787 --observer-cmd-port 6792 --observer-tcp-port 6791 --observer-udp-port 6790
