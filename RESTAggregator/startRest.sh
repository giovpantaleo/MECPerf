#!/bin/bash
##pip install /home/ubuntu/RESTAggregator-1.0.0-py2-none-any.whl 
##nohup waitress-serve --host="" --port=5001 --call "RESTAggregator:create_app" &

# R2LAB
pip install /home/MECPerf/MECPerf-master/RESTAggregator/dist/RESTAggregator-1.0.0-py2-none-any.whl 
nohup waitress-serve --host="192.168.3.11" --port=5001 --call "RESTAggregator:create_app" &
##nohup waitress-serve --host="127.0.0.1" --port=5001 --call "RESTAggregator:create_app" &

