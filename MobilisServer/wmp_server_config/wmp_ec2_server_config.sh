#usr/bin/sh
# This script creates backups of hibernate.xml and MobilisSettings.xml in src/META_INF and replaces them with the ec2 XMPP server configuration as saved in the location folder of these two scripts. It is normally used to create the config files to connect to our Amazon EC2 XMPP server/MySQL database. Make sure you run local_server_config.sh if you want to use the local server instead.
# For deploying of MobilisServer on the Amazon EC2 server use the local server configuration.
# @sven 06.2011

mv ../src/META-INF/hibernate.xml ../src/META-INF/hibernate.xml.bak
mv ../src/META-INF/MobilisSettings.xml ../src/META-INF/MobilisSettings.xml.bak
cp hibernate.xml ../src/META-INF/
cp MobilisSettings.xml ../src/META-INF/
exit 0

