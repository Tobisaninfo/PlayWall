@echo off
if not exist target mkdir target
if not exist target\classes mkdir target\classes


echo compile classes
javac -nowarn -d target\classes -sourcepath jvm -cp "g:\programmieren\playwall\playwallnativeaudio\j4n\jni4net.j-0.8.9.0.jar"; @sources.txt
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeAudio.j4n.jar 
jar cvf NativeAudio.j4n.jar  @classes.txt > nul 
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeAudio.j4n.dll 
csc /nologo /warn:0 /t:library /out:NativeAudio.j4n.dll /recurse:clr\*.cs  /reference:"G:\Programmieren\PlayWall\PlayWallNativeAudio\j4n\NativeAudio.dll" /reference:"G:\Programmieren\PlayWall\PlayWallNativeAudio\j4n\jni4net.n-0.8.9.0.dll"
IF %ERRORLEVEL% NEQ 0 goto end


:end
