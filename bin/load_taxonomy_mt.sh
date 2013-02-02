#!/bin/bash
# Usage: ./load_taxonomy.sh
# All parameters are supplied via hardcoded variables in script
#
TGNI_HOME=/prod/web/data/tgni
#PROJECT_ROOT=/Users/sujit/Projects/tgni
PROJECT_ROOT=/home/jboss/HEAD/tgni
LIB_DIR=$PROJECT_ROOT/lib
LOG4J_CONFIG_PATH=file:$PROJECT_ROOT/src/main/resources/log4j-script.properties

# parameters to the MTTaxonomyLoader class
GRAPH_DIR=$TGNI_HOME/data/graphdb
INDEX_DIR=$TGNI_HOME/conf/database-mysql.properties
STOPWORDS_FILE=$TGNI_HOME/conf/stopwords.txt
TAXONOMY_MAPPING_AE_FILE=$TGNI_HOME/conf/descriptors/TaxonomyMappingAE.xml
ORA_DB_PROPS=$TGNI_HOME/conf/database-oracle.properties
# end parameters to the MTTaxonomyLoader class

CLASSPATH=$PROJECT_ROOT/target/classes
for i in `/bin/ls $LIB_DIR/*jar`; do
  CLASSPATH=$CLASSPATH:$i
done
# two step - first load concepts then relationships
echo "Deleting old data from $TGNI_HOME/data..."
rm -rf $TGNI_HOME/data && mkdir $TGNI_HOME/data

echo "Loading concepts..."
java -Xmx14336m -Dlog4j.configuration=$LOG4J_CONFIG_PATH -cp $CLASSPATH com.healthline.tgni.loader.ConceptLoadManager $GRAPH_DIR $INDEX_DIR $STOPWORDS_FILE $TAXONOMY_MAPPING_AE_FILE $ORA_DB_PROPS

# TODO: build index oid_nid(oid)

echo "Loading relationships..."
java -Xmx14336m -Dlog4j.configuration=$LOG4J_CONFIG_PATH -cp $CLASSPATH com.healthline.tgni.loader.RelationshipLoadManager $GRAPH_DIR $INDEX_DIR $ORA_DB_PROPS
