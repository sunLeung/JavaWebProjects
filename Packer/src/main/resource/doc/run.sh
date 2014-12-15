rm nohup.out
nohup java -d64 -server -Xms4g -Xmx4g -XX:PermSize=128m -XX:MaxPermSize=256m -Dfile.encoding=UTF-8 -Xloggc:./gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Djava.ext.dirs=lib common.GameServer -port 30008
