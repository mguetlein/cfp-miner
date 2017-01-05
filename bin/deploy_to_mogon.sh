#!/bin/bash

#VERSION="0.0.1-SNAPSHOT"
VERSION="1.2.0-DEEP"

set -e

cd ../JavaLib
mvn clean install
cd -

cd ../weka-lib
mvn clean install
cd -

cd ../cdk-lib
mvn clean install
cd -

mvn clean install

cp /home/martin/.m2/repository/org/kramerlab/cfpminer/cfp-miner/$VERSION/cfp-miner-$VERSION-jar-with-dependencies.jar cfpminer.jar



#scp cfpminer.jar guetlein@mrbungle.zdv.uni-mainz.de:/home/guetlein/cfpminer/
#ssh guetlein@mrbungle.zdv.uni-mainz.de 'scp cfpminer/cfpminer.jar mguetlei@mogon.zdv.uni-mainz.de:/home/mguetlei/cfpminer/'

scp cfpminer.jar mguetlei@mogon.zdv.uni-mainz.de:/home/mguetlei/cfpminer/

#scp add_runner.sh guetlein@mrbungle.zdv.uni-mainz.de:/home/guetlein/cfpminer/; ssh guetlein@mrbungle.zdv.uni-mainz.de 'scp cfpminer/add_runner.sh mguetlei@mogon.zdv.uni-mainz.de:/home/mguetlei/cfpminer/'


##rsync cache guetlein@mrbungle.zdv.uni-mainz.de:/home/guetlein/dream -ruvc
##ssh guetlein@mrbungle.zdv.uni-mainz.de 'rsync dream/cache mguetlei@mogon.zdv.uni-mainz.de:dream -ruvc'

##rsync cache mguetlei@mogon.zdv.uni-mainz.de:dream -ruvc
