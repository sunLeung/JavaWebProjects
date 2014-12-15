rm -rf /root/game/res/scripts
rm -rf /root/game/res/gds
rm /root/game/lib/game.jar
cp /root/GameLog/toser/game/game.jar /root/game/lib/game.jar
rm /root/game/res/res.zip
cp /root/GameLog/toser/game/res.zip /root/game/res/res.zip
cd /root/game/res
unzip -o res.zip
cd /root/game
./run.sh
