#! /bin/bash
ps -ef|grep osd|grep -v grep| awk {'print $2'}|xargs kill
