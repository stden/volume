Dim FSO
Set FSO = CreateObject("Scripting.FileSystemObject")

Sub ShowFolderList(folderspec)
  Dim curFolder, f1, fc
  Set curFolder = FSO.GetFolder(folderspec)
  Set fc = curFolder.SubFolders
  For Each f1 in fc
    if UCase(f1.name) = ".SVN" then
      Out.WriteLine(Replace(folderspec & "\",".\","") & f1.name)
    else
      ShowFolderList(folderspec & "\" & f1.name)
    end if
  Next
End Sub

Set Out = FSO.CreateTextFile("SVN_Exclude.txt")
if WScript.Arguments.Count = 1 then 
  ShowFolderList(WScript.Arguments(0))
else
  ShowFolderList(".")  ' Текущий каталог
end if
Out.Close()

