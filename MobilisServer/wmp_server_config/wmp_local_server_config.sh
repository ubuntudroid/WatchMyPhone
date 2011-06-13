#usr/bin/bash
# This scripts overrides the current Mobilis Server configuration as defined in /src/META-INF/MobilisSettings.xml and hibernate.xml by the default local configuration saved in the appropriate bak files by the a accompanied wmp_server_config.sh script. This script is normally run *after* the wmp_server_config.sh script and after a runnable jar file has been created for deployment on the Amazon EC2 server. It returns the Server project back to normal development state.
# To make sure, that the current XMLs are not destroyed without any backups in place, we check for the existence of the bak files *before* deleting the files.
# @sven 06.2011
if [ ! -f ../src/META-INF/hibernate.xml.bak ]; then
	echo "Couldn't find hibernate.xml.bak (normally this means that local config is already in place)- aborting...";
	exit 1;
fi
if [ ! -f ../src/META-INF/MobilisSettings.xml.bak ]; then
	echo "Couldn't find MobilisSettings.xml.bak (normally this means that local config is already in place) - aborting...";
	exit 1;
fi
rm ../src/META-INF/hibernate.xml
rm ../src/META-INF/MobilisSettings.xml
mv ../src/META-INF/hibernate.xml.bak ../src/META-INF/hibernate.xml
mv ../src/META-INF/MobilisSettings.xml.bak ../src/META-INF/MobilisSettings.xml
exit 0
