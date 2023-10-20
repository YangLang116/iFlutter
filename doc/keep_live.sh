#通过crontab对以下服务进行保活
#!/bin/bash
source /etc/profile
result=`netstat -tunpl | grep 4000 | wc -l`
if [ $result -ne 1 ];then
echo '重启Gitbook服务'
nohup /home/soft/node-v12.22.3-linux-x64/bin/gitbook serve --port 4000 &
fi
