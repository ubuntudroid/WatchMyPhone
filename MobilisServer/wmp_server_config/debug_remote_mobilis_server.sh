#!/bin/bash
# This script runs MobilisServer.jar in remote debug mode. Connect to localhost:8998 on a tunneled connection to debug the session remotely.
# Make sure, that this script is located in the same folder as MobilisServer.jar!
java -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y -jar MobilisServer.jar
