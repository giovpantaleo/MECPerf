#!/bin/bash
   
PROJECT_ROOT_PATH=$(pwd)
COMMON_PATH=$PROJECT_ROOT_PATH/common/src/main/java/
OBSERVER_PATH=$PROJECT_ROOT_PATH/observer/src/main/java/
REMOTESERVER_PATH=$PROJECT_ROOT_PATH/remoteserver/src/main/java/
AGGREGATOR_PATH=$PROJECT_ROOT_PATH/aggregator/src/main/java/
APPCOMMANDLINE_PATH=$PROJECT_ROOT_PATH/app/src/main/java/

echo "start compiling..."


#-bootclasspath /home/jdk/jre/lib/rt.jar
javac -target 11 -source 11 -cp $COMMON_PATH $COMMON_PATH/it/unipi/dii/common/Measure.java 
javac -target 11 -source 11 -cp $COMMON_PATH $COMMON_PATH/it/unipi/dii/common/Measurements.java
javac -target 11 -source 11 -cp $COMMON_PATH $COMMON_PATH/it/unipi/dii/common/ControlMessages.java
echo "      Commons compiled"
javac -target 11 -source 11 -cp $COMMON_PATH $APPCOMMANDLINE_PATH/it/unipi/dii/mecperfng/MainUtils.java
echo "      MainUtils compiled"

javac -target 11 -source 11 -cp $COMMON_PATH -cp /home/MECPerf/MECPerf-master/json-simple-1.1.1.jar $AGGREGATOR_PATH/it/unipi/dii/aggregator/Aggregator.java
echo "      Aggregator compiled"
javac -target 11 -source 11 -cp $COMMON_PATH  $OBSERVER_PATH/it/unipi/dii/observer/Observer.java
#javac --add-modules=jdk.incubator.httpclient-cp $COMMON_PATH  $OBSERVER_PATH/it/unipi/dii/observer/Observer.java
echo "      Observer compiled"
javac -target 11 -source 11 -cp $COMMON_PATH $REMOTESERVER_PATH/it/unipi/dii/remoteserver/Server.java
echo "      RemoteServer compiled"

javac -target 11 -source 11 -cp $COMMON_PATH:$APPCOMMANDLINE_PATH -Xlint:unchecked  $APPCOMMANDLINE_PATH/it/unipi/dii/mecperfng/commandlineapp/CommandLineApp.java
echo "      App for linux compiled"

       
       
          
echo "creating jars..."
# c: reate jar
# v: verbose
# f: specify output jar name

cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/common/ControlMessages.class it/unipi/dii/common/ControlMessages.java
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/common/ControlMessages\$Messages.class
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java 
cd $APPCOMMANDLINE_PATH
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/mecperfng/commandlineapp/CommandLineApp.class it/unipi/dii/mecperfng/commandlineapp/CommandLineApp.java 
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/mecperfng/MainUtils.class it/unipi/dii/mecperfng/MainUtils.java
echo "      CommandLineApp.jar created"

jar umf MANIFEST.MF $PROJECT_ROOT_PATH/CommandLineApp.jar
echo "      CommandLineApp manifest file updated"



cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java 
jar uf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/common/ControlMessages.class it/unipi/dii/common/ControlMessages.java
jar uf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/common/ControlMessages\$Messages.class
cd $OBSERVER_PATH
jar uf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/observer/Observer.class it/unipi/dii/observer/Observer.java 
echo "      Observer.jar created"
jar umf MANIFEST.MF $PROJECT_ROOT_PATH/Observer.jar
#jar uf $PROJECT_ROOT_PATH/Observer.jar MANIFEST.MF 
echo "      Observer manifest file updated"


cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java
jar uf $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/common/ControlMessages.class it/unipi/dii/common/ControlMessages.java
jar uf $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/common/ControlMessages\$Messages.class
cd $REMOTESERVER_PATH
jar uf  $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/remoteserver/Server.class it/unipi/dii/remoteserver/Server.java
echo "      Server.jar done"
jar umf MANIFEST.MF $PROJECT_ROOT_PATH/Server.jar
echo "      Server manifest file updated"


cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java 
jar uf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/common/MeasureResult.class it/unipi/dii/common/MeasureResult.java 
cd $AGGREGATOR_PATH
jar uf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/aggregator/Aggregator.class it/unipi/dii/aggregator/Aggregator.java
jar uf $PROJECT_ROOT_PATH/Aggregator.jar /home/MECPerf/MECPerf-master/json-simple-1.1.1.jar

echo "      Aggregator.jar done"
jar umf MANIFEST.MF $PROJECT_ROOT_PATH/Aggregator.jar
echo "      Aggregator manifest file updated"
          
