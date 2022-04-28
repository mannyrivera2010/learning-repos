#!/bin/bash
#Functions
function InstallSoftware{
	sudo apt-get install -y git
	sudo apt-get install -y ssh
	sudo apt-get install -y python-pip
	sudo apt-get install -y git-cola
	sudo apt-get install -y geany
	sudo apt-get install -y libpq-dev 
	sudo apt-get install -y python-dev
	sudo apt-get install -y postgresql
	sudo apt-get install -y postgresql-contrib
}

function InstallPipSoftware{
	sudo pip install virtualenv		
}
#Setup Development Enviroment
echo -e "\n\n================== Starting to Setup Development Environment  ===================\n\n"
InstallSoftware
InstallPipSoftware
# END SCRIPT
