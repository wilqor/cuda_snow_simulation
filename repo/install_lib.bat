:: steps to install:
:: -copy appropriate files to repo directory
:: -setup the version variable
:: -setup the proper mvn path
:: -run the script
:: -directories along with pom files will be created

SET version=0.7.0
::version=0.6.5
::version=0.6.0

:: install java jars
mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcublas -Dversion=%version% -Dfile=jcublas-%version%.jar -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcuda -Dversion=%version% -Dfile=jcuda-%version%.jar -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcufft -Dversion=%version% -Dfile=jcufft-%version%.jar -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcurand -Dversion=%version% -Dfile=jcurand-%version%.jar -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda -DartifactId=jcusparse -Dversion=%version% -Dfile=jcusparse-%version%.jar -Durl=file://.

:: install windows-x86_64 native libs
mvn deploy:deploy-file -DgroupId=jcuda.windows -DartifactId=JCublas -Dversion=%version% -Dclassifier=windows-x86_64 -Dfile=JCublas-windows-x86_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.windows -DartifactId=JCublas2 -Dversion=%version% -Dclassifier=windows-x86_64 -Dfile=JCublas2-windows-x86_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.windows -DartifactId=JCudaDriver -Dversion=%version% -Dclassifier=windows-x86_64 -Dfile=JCudaDriver-windows-x86_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.windows -DartifactId=JCudaRuntime -Dversion=%version% -Dclassifier=windows-x86_64 -Dfile=JCudaRuntime-windows-x86_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.windows -DartifactId=JCufft -Dversion=%version% -Dclassifier=windows-x86_64 -Dfile=JCufft-windows-x86_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.windows -DartifactId=JCurand -Dversion=%version% -Dclassifier=windows-x86_64 -Dfile=JCurand-windows-x86_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.windows -DartifactId=JCusparse -Dversion=%version% -Dclassifier=windows-x86_64 -Dfile=JCusparse-windows-x86_64.dll -Durl=file://.

:: install linux-x86_64 native libs
mvn deploy:deploy-file -DgroupId=jcuda.linux -DartifactId=libJCublas -Dversion=%version% -Dclassifier=linux-x86_64 -Dfile=libJCublas-linux-x86_64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.linux -DartifactId=libJCublas2 -Dversion=%version% -Dclassifier=linux-x86_64 -Dfile=libJCublas2-linux-x86_64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.linux -DartifactId=libJCudaDriver -Dversion=%version% -Dclassifier=linux-x86_64 -Dfile=libJCudaDriver-linux-x86_64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.linux -DartifactId=libJCudaRuntime -Dversion=%version% -Dclassifier=linux-x86_64  -Dfile=libJCudaRuntime-linux-x86_64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.linux -DartifactId=libJCufft -Dversion=%version% -Dclassifier=linux-x86_64 -Dfile=libJCufft-linux-x86_64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.linux -DartifactId=libJCurand -Dversion=%version% -Dclassifier=linux-x86_64 -Dfile=libJCurand-linux-x86_64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=jcuda.linux -DartifactId=libJCusparse -Dversion=%version% -Dclassifier=linux-x86_64 -Dfile=libJCusparse-linux-x86_64.so -Durl=file://.

SET slick_version=1.0
:: install slick jars
mvn deploy:deploy-file -DgroupId=slick -DartifactId=lwjgl -Dversion=%slick_version% -Dfile=lwjgl.jar -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick -DartifactId=lwjgl_util -Dversion=%slick_version% -Dfile=lwjgl_util.jar -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick -DartifactId=jinput -Dversion=%slick_version% -Dfile=jinput.jar -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick -DartifactId=slick -Dversion=%slick_version% -Dfile=slick.jar -Durl=file://.

:: install slick windows native libs
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=jinput-dx8 -Dversion=%slick_version% -Dfile=jinput-dx8.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=jinput-dx8_64 -Dversion=%slick_version% -Dfile=jinput-dx8_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=jinput-raw -Dversion=%slick_version% -Dfile=jinput-raw.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=jinput-raw_64 -Dversion=%slick_version% -Dfile=jinput-raw_64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=lwjgl -Dversion=%slick_version% -Dfile=lwjgl.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=lwjgl64 -Dversion=%slick_version% -Dfile=lwjgl64.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=OpenAL32 -Dversion=%slick_version% -Dfile=OpenAL32.dll -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.windows -DartifactId=OpenAL64 -Dversion=%slick_version% -Dfile=OpenAL64.dll -Durl=file://.

:: install slick linux native libs
mvn deploy:deploy-file -DgroupId=slick.linux -DartifactId=libjinput-linux -Dversion=%slick_version% -Dfile=libjinput-linux.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.linux -DartifactId=libjinput-linux64 -Dversion=%slick_version% -Dfile=libjinput-linux64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.linux -DartifactId=liblwjgl -Dversion=%slick_version% -Dfile=liblwjgl.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.linux -DartifactId=liblwjgl64 -Dversion=%slick_version% -Dfile=liblwjgl64.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.linux -DartifactId=libopenal -Dversion=%slick_version% -Dfile=libopenal.so -Durl=file://.
mvn deploy:deploy-file -DgroupId=slick.linux -DartifactId=libopenal64 -Dversion=%slick_version% -Dfile=libopenal64.so -Durl=file://.