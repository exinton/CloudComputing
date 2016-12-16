#!/bin/bash

cd ../../OSDServer
java -cp "../ceph/lib/*" newOSD.OsdServer > ../ceph/log/osd.out 2>&1 &

