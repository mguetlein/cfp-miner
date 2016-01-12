#!/bin/bash

module load Java/jdk1.8.0_25

#for f in $(cat endpoints.txt); do 
#    for i in 1024 2048 4096 8192; do 
#	sleep 0.33
#	bsub -q short -W 300 -n 1 -app Reserve10G java -jar -Xmx5G #cfpminer.jar -d $f -s $i -v
#    done
#done

for n in {1..10}; do
    for f in CPDBAS_SingleCellCall AMES CPDBAS_Mouse CPDBAS_MultiCellCall; do
	sleep 0.33
	bsub -q long -W 7200 -n 1 -app Reserve10G java -jar -Xmx5G cfpminer.jar -d $f -v
    done
done

