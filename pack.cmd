del imitators\java.prof
del imitators\bin\java.prof
del imitators\bin\lmpoints.txt
del volume_installer.*
cscript SVN_List.vbs
"%ProgramFiles%\WinRar.ENG\rar.exe" a -r -sfx volume_installer -x@exclude.txt -x@SVN_Exclude.txt