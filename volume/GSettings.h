// GSettings.h: interface for the CGSettings class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GSETTINGS_H__A66C50E5_2799_4F01_810C_36648AE1B89B__INCLUDED_)
#define AFX_GSETTINGS_H__A66C50E5_2799_4F01_810C_36648AE1B89B__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "GAlloc.h"

#define GSETTINGS_MAX_STRINGS_LENGTH		32767	// win95/98 doesn't like this any bigger
#define GSETTINGS_MAX_STRING_LENGTH			256
#define GSETTINGS_MAX_INT_LENGTH			32
#define GSETTINGS_MAX_BOOL_LENGTH			2

class CGSettings  
{
public:
	CGSettings(LPCTSTR szName = NULL);
	virtual ~CGSettings();

public:
	void SetName(LPCTSTR szName);

	// Private Settings
	LPTSTR GetString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szDefault = "");
	void SetString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szValue);

	void AddInstance(LPCTSTR szTask, LPCTSTR szInstance);

	char** GetStrings(LPCTSTR szSection, LPCTSTR szName);
	void AddString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szString);
	void RemoveString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szString);

	int GetInt(LPCTSTR szSection, LPCTSTR szName, int nDefault = 0);
	void SetInt(LPCTSTR szSection, LPCTSTR szName, int nValue);

	double GetDouble(LPCTSTR szSection, LPCTSTR szName, double dDefault = 0.0);
	void SetDouble(LPCTSTR szSection, LPCTSTR szName, double dValue);

	BOOL GetBool(LPCTSTR szSection, LPCTSTR szName, BOOL bDefault);
	void SetBool(LPCTSTR szSection, LPCTSTR szName, BOOL bValue);

	__int64 GetInt64(LPCTSTR szSection, LPCTSTR szName, __int64 n64Default = 0);
	void SetInt64(LPCTSTR szSection, LPCTSTR szName, __int64 n64Value);

	char* GetSectionNames();
	char** GetStringsSectionNames();
	LPCTSTR GetFilePath()	{	return m_szName;	}
	void DeleteAll()		{	DeleteFile(m_szName);	}

protected:
	char** CGSettings::GetStringsLocal(char* szString, char chDelim);

protected:
	CGAlloc m_GAlloc;
	char* m_szName;
};

#endif // !defined(AFX_GSETTINGS_H__A66C50E5_2799_4F01_810C_36648AE1B89B__INCLUDED_)
