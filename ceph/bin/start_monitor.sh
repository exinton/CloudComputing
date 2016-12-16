#!/bin/bash

cd ../../CephMapMonitors
java -cp "../ceph/lib/*" cephMonitor.cephMonitor > ../ceph/log/monitor.out 2>&1 &

