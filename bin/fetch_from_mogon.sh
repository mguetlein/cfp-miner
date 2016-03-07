#!/bin/bash

#rm -rf jobs/store
#mkdir jobs/store
#for d in $(cat data/endpoints.txt); do
#    mkdir jobs/store/$d
#done

#d="AMES"

host="mguetlei@mogon.zdv.uni-mainz.de:~/cfpminer"
#host="guetlein@mrbungle.zdv.uni-mainz.de:~/cfpminer"

for d in $(cat data/endpoints.txt); do
    rsync -ruv $host/jobs/store/$d/CV* jobs/store/$d
    rsync -ruv $host/jobs/store/$d/Val* jobs/store/$d
done
