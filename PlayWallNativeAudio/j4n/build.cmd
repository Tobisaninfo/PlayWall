@echo off
if not exist target mkdir target
if not exist target\classes mkdir target\classes


echo compile classes
javac -nowarn -d target\classes -sourcepath jvm -cp "c:\users\tobias\ideaprojects\playwalldesktop\playwallplugins\playwallpluginnativeaudio\j4n\jni4net.j-0.8.8.0.jar"; "jvm\nativeaudio\NativeAudio.java" 
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeAudio.j4n.jar 
jar cvf NativeAudio.j4n.jar  -C target\classes "nativeaudio\NativeAudio.class"  > nul 
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeAudio.j4n.dll 
csc /nologo /warn:0 /t:library /out:NativeAudio.j4n.dll /recurse:clr\*.cs  /reference:"C:\Users\tobias\IdeaProjects\PlayWallDesktop\PlayWallPlugins\PlayWallPluginNativeAudio\j4n\NativeAudio.dll" /reference:"C:\Users\tobias\IdeaProjects\PlayWallDesktop\PlayWallPlugins\PlayWallPluginNativeAudio\j4n\jni4net.n-0.8.8.0.dll"
IF %ERRORLEVEL% NEQ 0 goto end


:end
