SCRIPTDIR=`dirname $0`
DIR=`cd $SCRIPTDIR/..; pwd`
export CLASSPATH=$DIR/lib/pollo.jar:$DIR/lib/endorsed/dom3-xercesImpl.jar:$DIR/lib/endorsed/dom3-xmlParserAPIs.jar:$DIR/lib/avalon-configuration.jar:$DIR/lib/log4j-core.jar:$DIR/lib/jaxen-core.jar:$DIR/lib/jaxen-dom.jar:$DIR/lib/saxpath.jar:$DIR/lib/msv-20031002.jar:$DIR/lib/xsdlib.jar:$DIR/lib/relaxngDatatype.jar:$DIR/lib/isorelax.jar:$DIR/lib/xpp3-1.1.3.4-RC3_min.jar:$DIR/lib/commons-lang-exception-2.0.jar:$DIR/conf:$DIR/build

java -Djava.endorsed.dirs=$DIR/lib/endorsed org.outerj.pollo.Pollo $@
