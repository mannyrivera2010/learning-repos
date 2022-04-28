### Reading
https://chromatichq.com/blog/automated-servers-and-deployments-ansible-jenkins

### Setting up Jenkins for python project (ozp-backend)
Project name: build-backend-latest
Github Project: https://github.com/aml-development/ozp-backend/
Throttle Concurrent Builds: 
- Maximum Total Concurrent Builds = 1
- Maximum Concurrent Builds Per Node = 1
Restrict where this project can be run: ci-jenkins.amlng.di2e.net
Source Code Management: 
- Repositories: https://github.com/aml-development/ozp-backend.git
- Credentials: - none -
Branches to build: */master
Git executable: Default
Build Triggers: 
- Build when a change is pushed to GitHub
Build Environment:
- Delete workspace before build starts
- Abort the build if it's stuck
-- Time-out strategy: Absolute , Timeout minutes: 15
Build: 
Execute Shell:
````bash
# create clean python env
rm -rf ~/python_envs/ci-env/
mkdir ~/python_envs/ci-env/
pyvenv-3.4 ~/python_envs/ci-env/
source ~/python_envs/ci-env/bin/activate
# install prereqs (clean)
pip install --upgrade pip
# this is super sketchy, but without it, psycopg2 will fail to install (won't be
# able to find lib2to3 stuff)
# /opt/lib2to3 was copied from a clean Python-3.4.3 build (Lib directory)
cp -r /opt/lib2to3 ~/python_envs/ci-env/lib/python3.4/site-packages/
export PATH=/usr/local/pgsql/bin:$PATH
pip install -r requirements.txt --no-cache-dir -I
# make the release
python release.py --no-version
````

Post-build Actions: 
Archive the artifacts: backend*.tar.gz

Send Build artifacts over SSH:
ci-latest.domain.net
Transers:
Exec command: ````sudo /home/jenkins/ozp_deploy.sh ${JOB_NAME} ${BUILD_NUMBER}````

Delete Workspace when build is done.


### Setting up Jenkins for python project (ozp-backend) Release
Project name: build-backend-latest
Github Project: https://github.com/aml-development/ozp-backend/
Throttle Concurrent Builds: 
- Maximum Total Concurrent Builds = 1
- Maximum Concurrent Builds Per Node = 1
Restrict where this project can be run: ci-jenkins.amlng.di2e.net
Source Code Management: 
- Repositories: https://github.com/aml-development/ozp-backend.git
- Credentials: - none -
Branches to build: */tags/release/*
Git executable: Default
Build Triggers: 
- Build when a change is pushed to GitHub
Build Environment:
- Delete workspace before build starts
- Abort the build if it's stuck
-- Time-out strategy: Absolute , Timeout minutes: 15
Build: 
Execute Shell:
````bash
# create clean python env
rm -rf ~/python_envs/ci-env/
mkdir ~/python_envs/ci-env/
pyvenv-3.4 ~/python_envs/ci-env/
source ~/python_envs/ci-env/bin/activate
# install prereqs (clean)
pip install --upgrade pip
# this is super sketchy, but without it, psycopg2 will fail to install (won't be
# able to find lib2to3 stuff)
# /opt/lib2to3 was copied from a clean Python-3.4.3 build (Lib directory)
cp -r /opt/lib2to3 ~/python_envs/ci-env/lib/python3.4/site-packages/
export PATH=/usr/local/pgsql/bin:$PATH
pip install -r requirements.txt --no-cache-dir -I
# make the release
python release.py --no-version
````

Post-build Actions: 
Archive the artifacts: backend*.tar.gz

Send Build artifacts over SSH:
ci-latest.domain.net
Transers:
Exec command: ````sudo /home/jenkins/ozp_deploy.sh ${JOB_NAME} ${BUILD_NUMBER}````

Delete Workspace when build is done.


### Setting up Jenkins for python project (ozp-center)
Project name: build-center-latest
Github Project: https://github.com/aml-development/ozp-center/
Throttle Concurrent Builds: 
- Maximum Total Concurrent Builds = 1
- Maximum Concurrent Builds Per Node = 1
Restrict where this project can be run: ci-jenkins.amlng.di2e.net
Source Code Management: 
- Repositories: https://github.com/aml-development/ozp-center.git
- Credentials: - none -
Branches to build: */master
Git executable: Default
Build Triggers: 
- Build when a change is pushed to GitHub
Build Environment:
- Delete workspace before build starts
- Abort the build if it's stuck
-- Time-out strategy: Absolute , Timeout minutes: 15

Build: 
>Execute Shell:
````bash
#!/usr/bin/env bash
# use the develop branch of ozp-react-commons
# sed -i -e "s/ozp-react-commons#master/ozp-react-commons#develop/g" package.json
# sed -i -e "s/\/icons/\/icons#1fc7aee3a2812042c421baaab67abb2bd9606b0d/g" package.json
source /usr/local/node_versions/set_node_version.sh 5.3.0
echo "node version: "
node -v
npm install
npm run build
npm run test
npm run tarDistDate
````

Conditital step(single):
Run?: Text Finder
Regular expression: Cannot resolve module

Builder: Execute Shell
````
#!/usr/bin/env bash
echo "detected build error. Failing build"
exit 1
````

Post-build Actions: 
Archive the artifacts: center-*.tar.gz

Send Build artifacts over SSH:
ci-latest.domain.net
Transers:
Exec command: ````sudo /home/jenkins/ozp_deploy.sh ${JOB_NAME} ${BUILD_NUMBER}````

Delete Workspace when build is done.

### ozp-react commons
...
build:
````
#!/usr/bin/env bash
source /usr/local/node_versions/set_node_version.sh 5.3.0
echo "node version: "
node -v
npm install
npm run test
````

post-build actions:
Build other projects:
projects to build: build-center-latest,build-hud-latest
-Trigger only if build is stable

### ozp_deploy.sh
````bash
#! /bin/bash

python /home/jenkins/dev-tools/jenkins-ansible-deployer/jenkins_ansible_deployer.py $1 $2
````


https://github.com/aml-development/dev-tools/blob/master/jenkins-ansible-deployer/jenkins_ansible_deployer.py
