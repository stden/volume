Dim FSO
Set FSO = CreateObject("Scripting.FileSystemObject")

Sub ShowFolderList(folderspec)
  Dim curFolder, f1, fc
  Set curFolder = FSO.GetFolder(folderspec)
  Set fc = curFolder.SubFolders
  For Each f1 in fc
    WScript.Echo "Каталог: " & folderspec & "\" & f1.name 
    if UCase(f1.name) = ".SVN" then
      WScript.Echo "Удаляю каталог SVN: " & folderspec & "\" & f1.name 
      FSO.DeleteFolder(folderspec & "\" & f1.name) 
    else
      ShowFolderList(folderspec & "\" & f1.name)
    end if
  Next
End Sub

if WScript.Arguments.Count = 1 then 
  ShowFolderList(WScript.Arguments(0))
else
  ShowFolderList(".")  ' Текущий каталог
end if

