#!/bin/bash

set -e

cd ../JavaLib
mvn clean install
cd -

cd ../WEKALib
mvn clean install
cd -

cd ../CDKLib
mvn clean install
cd -

mvn clean install

cp /home/martin/.m2/repository/org/kramerlab/cfpminer/CFPMiner/0.0.1-SNAPSHOT/CFPMiner-0.0.1-SNAPSHOT-jar-with-dependencies.jar cfpminer.jar



scp cfpminer.jar guetlein@mrbungle.zdv.uni-mainz.de:/home/guetlein/cfpminer/
ssh guetlein@mrbungle.zdv.uni-mainz.de 'scp cfpminer/cfpminer.jar mguetlei@mogon.zdv.uni-mainz.de:/home/mguetlei/cfpminer/'

#scp cfpminer.jar mguetlei@mogon.zdv.uni-mainz.de:/home/mguetlei/cfpminer/

#scp add_runner.sh guetlein@mrbungle.zdv.uni-mainz.de:/home/guetlein/cfpminer/; ssh guetlein@mrbungle.zdv.uni-mainz.de 'scp cfpminer/add_runner.sh mguetlei@mogon.zdv.uni-mainz.de:/home/mguetlei/cfpminer/'


##rsync cache guetlein@mrbungle.zdv.uni-mainz.de:/home/guetlein/dream -ruvc
##ssh guetlein@mrbungle.zdv.uni-mainz.de 'rsync dream/cache mguetlei@mogon.zdv.uni-mainz.de:dream -ruvc'

##rsync cache mguetlei@mogon.zdv.uni-mainz.de:dream -ruvc
