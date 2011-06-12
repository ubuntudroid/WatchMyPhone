#!/bin/bash
# This scripts overrides the current Mobilis Server configuration as defined in /src/META-INF/MobilisSettings.xml and hibernate.xml by the default local configuration saved in the appropriate bak files by the a accompanied wmp_server_config.sh script. This script is normally run *after* the wmp_server_config.sh script and after a runnable jar file has been created for deployment on the Amazon EC2 server. It returns the Server project back to normal development state.
# @sven 06.2011

rm ../src/META-INF/hibernate.xml
rm ../src/META-INF/MobilisSettings.xml
mv ../src/META-INF/hibernate.xml.bak ../src/META-INF/hibernate.xml
mv ../src/META-INF/MobilisSettings.xml.bak ../src/META-INF/MobilisSettings.xml
exit 0
