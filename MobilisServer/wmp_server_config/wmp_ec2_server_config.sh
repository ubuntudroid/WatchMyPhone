#usr/bin/sh
# This script creates backups of hibernate.xml and MobilisSettings.xml in src/META_INF and replaces them with the ec2 XMPP server configuration as saved in the location folder of these two scripts. It is normally run *before* creating a running jar for deployment on the Amazon EC2 server. Make sure you run local_server_config.sh *after* you've created the jar file.
# @sven 06.2011

mv ../src/META-INF/hibernate.xml ../src/META-INF/hibernate.xml.bak
mv ../src/META-INF/MobilisSettings.xml ../src/META-INF/MobilisSettings.xml.bak
cp hibernate.xml ../src/META-INF/
cp MobilisSettings.xml ../src/META-INF/
exit 0

