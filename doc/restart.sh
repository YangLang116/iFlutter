netstat -tunpl | grep 4000 | awk '{print $7}' | cut -f 1 -d "/" | xargs -t kill -9
nohup /home/soft/node-v12.22.3-linux-x64/bin/gitbook serve --port 4000
