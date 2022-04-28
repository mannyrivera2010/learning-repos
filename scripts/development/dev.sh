#!/bin/bash

#Setup Development Enviroment

#Installs the Following
#	>Java JDK
#	>Eclipse JEE Juno x86_64 or x86
#	>Geany
#	>Python-Dev
#	>Django
#	>Sphinx
#	>Apache Webserver
#	>PHP5
#	>phpMyAdmin
#	>MySql
#	>FileZilla

echo -e "\n\n================== Starting! ===================\n\n"

echo -e "\n**** DOWNLOADING Java JDK and Eclipse ****"
###########################################################################
#http://installit.googlecode.com/hg/install.eclipse.sh

# install Java JDK
wget http://installit.googlecode.com/hg/install.java-jdk.sh -O - | bash -

if [ "$(uname -m)" == "x86_64" ]; then
  # 64 bit
  URL=http://mirror.netcologne.de/eclipse/technology/epp/downloads/release/juno/SR2/eclipse-jee-juno-SR2-linux-gtk-x86_64.tar.gz
else
  # 32 bit
  URL=http://mirror.netcologne.de/eclipse/technology/epp/downloads/release/juno/SR2/eclipse-jee-juno-SR2-linux-gtk.tar.gz
fi

# download new eclipse release
wget ${URL} -P /tmp

# backup old release
[ -d /usr/lib/eclipse/ ] && sudo mv /usr/lib/eclipse/ /usr/lib/eclipse.$(date -I)

# install new release
sudo tar xzf /tmp/eclipse-jee-*-linux-gtk*.tar.gz -C /usr/lib/

# set owner and permissions
sudo chown -R root:root /usr/lib/eclipse
sudo chmod -R +r /usr/lib/eclipse

# add executable to path
cat <<EOF> /tmp/eclipse
#!/bin/sh
export ECLIPSE_HOME="/usr/lib/eclipse"

\${ECLIPSE_HOME}/eclipse $*
EOF

sudo mv /tmp/eclipse /usr/bin/eclipse
sudo chmod 755 /usr/bin/eclipse

# create starter
cat <<EOF> /tmp/eclipse.desktop
[Desktop Entry]
Encoding=UTF-8
Name=Eclipse
Comment=Eclipse IDE
Exec=eclipse
Icon=/usr/lib/eclipse/icon.xpm
Terminal=false
Type=Application
Categories=GNOME;Application;Development;
StartupNotify=true
EOF

sudo mv /tmp/eclipse.desktop /usr/share/applications/eclipse.desktop
sudo chmod +r /usr/share/applications/eclipse.desktop

# configure memory
sudo sed -i 's|-Xmx384m|-Xmx1024m|g' /usr/lib/eclipse/eclipse.ini

#http://installit.googlecode.com/hg/install.eclipse.sh
###########################################################################


###############2##########################
echo -e "\n**** INSTALLING Geany... ****"
sudo apt-get -y install geany

echo -e "\n**** INSTALLING python... ****"
sudo apt-get -y install python

echo -e "\n**** INSTALLING python-dev... ****"
sudo apt-get -y install python-dev

echo -e "\n**** INSTALLING python-setuptools... ****"
sudo apt-get -y install python-setuptools

echo -e "\n**** INSTALLING Django[1.6] + Sphinx"
sudo easy_install Django #1.6
sudo apt-get -y install python-sphinx
echo -e "\n"

echo -e "\n**** INSTALLING Apache web server + mod_ssl + mod_wsgi... ****"
sudo apt-get -y install apache2 libapache2-mod-gnutls libapache2-mod-wsgi
sudo a2enmod ssl        # Enable mod_ssl
sudo a2enmod wsgi       # Enable mod_wsgi
sudo a2enmod rewrite    # Enable mod_rewrite
sudo a2enmod proxy      # Enable mod_proxy
echo -e "\n"

echo -e "\n**** INSTALLING Apache web server + mod_ssl + mod_wsgi... ****"
sudo apt-get -y install php5 php5-mysql
echo -e "\n"

echo -e "\n**** INSTALLING Sqlite ****"
apt-get install sqlite3 python-sqlite
echo -e "\n"


echo -e "\n**** INSTALLING phpMyAdmin ****"
###########################################################################
#http://installit.googlecode.com/hg/install.phpMyAdmin.sh


URL=http://netcologne.dl.sourceforge.net/project/phpmyadmin/phpMyAdmin/3.5.3/phpMyAdmin-3.5.3-english.tar.bz2

# set variables
FILE=${URL##*/}

# set target
if [ ! -z $1 ]; then
  TARGET=$1
else
  TARGET=/var/www
fi

# set owner
if [ ! -z $2 ]; then
  WWW_USER=$2
else
  WWW_USER=www-data
fi

# download if not already exists
[ ! -f /tmp/${FILE} ] && wget -nv ${URL} -O /tmp/${FILE}

# backup old phpmyadmin installation
[ -d ${TARGET}/phpMyAdmin/ ] && mv ${TARGET}/phpMyAdmin/ ${TARGET}/phpMyAdmin.backup.$(date "+%F_%T")

# extract
tar -xjf /tmp/${FILE} -C ${TARGET}

# rename directory to simply "phpMyAdmin"
mv ${TARGET}/phpMyAdmin-*/ ${TARGET}/phpMyAdmin/

# change owner
chown ${WWW_USER}:${WWW_USER} -R ${TARGET}/phpMyAdmin/

#http://installit.googlecode.com/hg/install.phpMyAdmin.sh
###########################################################################
echo -e "\n"

echo -e "\n**** INSTALLING MySQL / MySQLdb... ****"
sudo apt-get -y install libdb 4.8
sudo DEBIAN_FRONTEND=noninteractive \
    apt-get -y install mysql-server mysql-common libmysqlclient18 python-mysqldb
echo -e "\n"

echo -e "\n**** INSTALLING FileZilla... ****"
sudo apt-get -y install filezilla
echo -e "\n"

echo -e "================== FINISHED! ===================\n\n"
