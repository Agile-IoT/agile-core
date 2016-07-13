#!/bin/sh

CURRDIR=`pwd`
DEPS=${1:-$CURRDIR/deps}
BUILD=$DEPS/build/dbus-java

mkdir -p $BUILD

DBUSJAVA=2.7
LMLIB=0.8

wget http://www.matthew.ath.cx/projects/java/libmatthew-java-$LMLIB.tar.gz
wget https://dbus.freedesktop.org/releases/dbus-java/dbus-java-2.7.tar.gz

tar -xzf libmatthew-java-$LMLIB.tar.gz
rm libmatthew-java-$LMLIB.tar.gz

tar -xzf dbus-java-$DBUSJAVA.tar.gz
rm dbus-java-$DBUSJAVA.tar.gz

mv dbus-java-$DBUSJAVA $BUILD
mv libmatthew-java-$LMLIB $BUILD

cd $BUILD/libmatthew-java-$LMLIB

make >> /dev/null
PREFIX=$BUILD make install >> /dev/null

cp ./*.jar $DEPS
cp ./*.so $DEPS
cp ./libunix-java.so $DEPS/unix-java.so

cd $BUILD/dbus-java-$DBUSJAVA

PREFIX=$BUILD JAVAUNIXLIBDIR=$BUILD/lib/jni JAVAUNIXJARDIR=$BUILD/share/java make bin >> /dev/null

cp ./*.jar $DEPS

rm -rf $BUILD

cd $DEPS

mvn install:install-file -Dfile=$DEPS/dbus-java-bin-2.7.jar \
                         -DgroupId=org.freedesktop.dbus \
                         -DartifactId=dbus-java \
                         -Dversion=2.7 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS


mvn install:install-file -Dfile=$DEPS/libdbus-java-2.7.jar \
                         -DgroupId=org.freedesktop.dbus \
                         -DartifactId=libdbus-java \
                         -Dversion=2.7 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/unix-0.5.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=unix \
                         -Dversion=0.5 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/debug-enable-1.1.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=debug-enable \
                         -Dversion=1.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/debug-disable-1.1.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=debug-disable \
                         -Dversion=1.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/cgi-0.6.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=cgi \
                         -Dversion=0.6 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/hexdump-0.2.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=hexdump \
                         -Dversion=0.2 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/io-0.1.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=io \
                         -Dversion=0.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS
