#!/bin/bash
#java -jar  Server.jar --remote-cmd-port 6789 --remote-tcp-port 6788 --remote-udp-port 6787 
gradle -q :remoteserver:run --args="--remote-cmd-port 6789 --remote-tcp-port 6788 --remote-udp-port 6787"