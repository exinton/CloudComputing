#!/bin/bash
port=$1
  echo $port

cd ../../OSDServer
java -cp "../ceph/lib/*" newOSD.OsdServer $port  2>&1 | tee ../ceph/log/osd.out  &

