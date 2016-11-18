#!/bin/bash

export CEPH_HOME=/home/tongxin/Dropbox/UTDallas/cloudcomputing/ceph/ceph
cd $CEPH_HOME
java -cp "lib/*" osd.FileReadWriteServer  > $CEPH_HOME/log/osd.out 2>&1 &

