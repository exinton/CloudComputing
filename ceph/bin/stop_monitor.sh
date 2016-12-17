#! /bin/bash
ps -ef|grep monitor|grep -v grep| awk {'print $2'}|xargs kill
