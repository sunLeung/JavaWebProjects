#!/bin/bash
export JAVA_HOME=/home/liangyx/installed/jdk1.6.0_37
export JRE_HOME=${JAVA_HOME}/jre
export ANT_HOME=/home/liangyx/installed/apache-ant-1.9.4
export PATH=${JAVA_HOME}/bin:${ANT_HOME}/bin:/usr/bin/:/bin/
workdir=/home/liangyx/projects/zg_v2_kr2
scriptProject=army_data_2.0_kr
gameProject=ArmyGameServer_KR_v2
configdir=korea_v3
builddir=$1
java -version
ant -version
echo packer dir:${workdir}
echo ----------------start packer------------------------
svn cleanup ${workdir}/${scriptProject}
svn update ${workdir}/${scriptProject}
svn cleanup ${workdir}/${configdir}
svn update ${workdir}/${configdir}
svn cleanup ${workdir}/${gameProject}
svn update ${workdir}/${gameProject}/lib
svn update ${workdir}/${gameProject}/package
svn update ${workdir}/${gameProject}/src
svn update ${workdir}/${gameProject}/res/config
echo -------------svn update completed--------------------
/bin/\cp ${workdir}/${configdir}/*.xls ${workdir}/${gameProject}/res/gds
/bin/\cp ${workdir}/${configdir}/illegalword.txt ${workdir}/${gameProject}/res/gds/illegalword
/bin/\cp ${workdir}/${configdir}/illegalextra.txt ${workdir}/${gameProject}/res/gds/illegalword
/bin/\cp ${workdir}/${configdir}/code_zh_CN.properties ${workdir}/${gameProject}/res/lang
/bin/\cp ${workdir}/${configdir}/map/*.* ${workdir}/${gameProject}/res/gds/maps
/bin/\cp ${workdir}/${configdir}/ms2d/*.* ${workdir}/${gameProject}/res/gds/maps
/bin/\cp ${workdir}/${configdir}/*.txt ${workdir}/${gameProject}/res/gds
/bin/\cp ${workdir}/${configdir}/*.json ${workdir}/${gameProject}/res/gds
echo --------------copy file completed---------------------
/bin/\rm -rf ${workdir}/${gameProject}/bin
ant -buildfile ${workdir}/${gameProject}/build.xml
echo ------------------build completed---------------------
rm -rf ${builddir}
mkdir -p ${builddir}/res
/bin/\cp -rf ${workdir}/${gameProject}/res/config ${builddir}/res/config
/bin/\cp -rf ${workdir}/${gameProject}/res/gds ${builddir}/res/gds
/bin/\cp -rf ${workdir}/${gameProject}/res/lang ${builddir}/res/lang
/bin/\cp -rf ${workdir}/${scriptProject}/res/scripts ${builddir}/res/scripts
/bin/\cp -f ${workdir}/${gameProject}/game.jar ${builddir}/game.jar
find ${builddir} -type d -iname ".svn" -exec /bin/rm -rf {} \;
cd ${builddir}/res
zip -r ${builddir}/res.zip ./*
echo ------------------------zip completed--------------------
