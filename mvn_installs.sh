# Processing 4.0.1
mvn install:install-file -Dfile=lib/lxstudio-0.4.2-SNAPSHOT-jar-with-dependencies.jar -DgroupId=heronarts -DartifactId=lxstudio -Dversion=0.4.2-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=classpath/core-4.0.1.jar -DgroupId=org.processing -DartifactId=core -Dversion=4.0.1 -Dpackaging=jar
mvn install:install-file -Dfile=classpath/jogl-all-4.0.1.jar -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dversion=4.0.1 -Dpackaging=jar
mvn install:install-file -Dfile=classpath/gluegen-rt-4.0.1.jar -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt-main -Dversion=4.0.1 -Dpackaging=jar
