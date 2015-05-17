#!/bin/bash

# steps to install:
# -copy appropriate files to repo directory
# -setup the version variable
# -setup the proper mvn path
# -run the script
# -directories along with pom files will be created

version=0.7.0
#version=0.6.5
#version=0.6.0

maven_path=~/Downloads/apache-maven-3.3.3/bin/

# install java jars
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcublas -Dversion=$version -Dfile=jcublas-$version.jar -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcuda -Dversion=$version -Dfile=jcuda-$version.jar -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcufft -Dversion=$version -Dfile=jcufft-$version.jar -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcurand -Dversion=$version -Dfile=jcurand-$version.jar -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcusparse -Dversion=$version -Dfile=jcusparse-$version.jar -Durl=file://.

# install linux-x86_64 native libs
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=libJCublas -Dversion=$version -Dclassifier=linux-x86_64 -Dfile=libJCublas-linux-x86_64.so -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=libJCublas2 -Dversion=$version -Dclassifier=linux-x86_64 -Dfile=libJCublas2-linux-x86_64.so -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=libJCudaDriver -Dversion=$version -Dclassifier=linux-x86_64 -Dfile=libJCudaDriver-linux-x86_64.so -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=libJCudaRuntime -Dversion=$version -Dclassifier=linux-x86_64  -Dfile=libJCudaRuntime-linux-x86_64.so -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=libJCufft -Dversion=$version -Dclassifier=linux-x86_64 -Dfile=libJCufft-linux-x86_64.so -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=libJCurand -Dversion=$version -Dclassifier=linux-x86_64 -Dfile=libJCurand-linux-x86_64.so -Durl=file://.
$maven_path/mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=libJCusparse -Dversion=$version -Dclassifier=linux-x86_64 -Dfile=libJCusparse-linux-x86_64.so -Durl=file://.