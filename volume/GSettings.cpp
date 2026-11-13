// GSettings.cpp: implementation of the CGSettings class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "GSettings.h"
#include <math.h>

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CGSettings::CGSettings(LPCTSTR szName) :
	m_szName(NULL)
{
	SetName(szName);
}

CGSettings::~CGSettings()
{
	if (m_szName)
		delete [] m_szName;
}

////////////////////////////////////////////////////////////////////////////////
// Local settings
////////////////////////////////////////////////////////////////////////////////

LPTSTR CGSettings::GetString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szDefault)
{
	ASSERT(m_szName);
	char* szBuff = (char*)m_GAlloc.Alloc(GSETTINGS_MAX_STRING_LENGTH);
	GetPrivateProfileString(szSection, szName, szDefault, szBuff, GSETTINGS_MAX_STRING_LENGTH, m_szName);
	return szBuff;
}

void CGSettings::SetString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szValue)
{
	ASSERT(m_szName);
	WritePrivateProfileString(szSection, szName, szValue, m_szName);
}

int CGSettings::GetInt(LPCTSTR szSection, LPCTSTR szName, int nDefault)
{
	ASSERT(m_szName);
	char szBuff[GSETTINGS_MAX_INT_LENGTH];
	char szDefault[GSETTINGS_MAX_INT_LENGTH];
	sprintf(szDefault, "%d", nDefault);
	GetPrivateProfileString(szSection, szName, szDefault, szBuff, GSETTINGS_MAX_INT_LENGTH, m_szName);
	return atoi(szBuff);
}

void CGSettings::SetInt(LPCTSTR szSection, LPCTSTR szName, int nValue)
{
	ASSERT(m_szName);
	char szBuff[GSETTINGS_MAX_INT_LENGTH];
	sprintf(szBuff, "%d", nValue);
	WritePrivateProfileString(szSection, szName, szBuff, m_szName);
}

double CGSettings::GetDouble(LPCTSTR szSection, LPCTSTR szName, double dDefault)
{
	ASSERT(m_szName);
	char szBuff[GSETTINGS_MAX_INT_LENGTH];
	char szDefault[GSETTINGS_MAX_INT_LENGTH];
	sprintf(szDefault, "%0.9f", dDefault);
	GetPrivateProfileString(szSection, szName, szDefault, szBuff, GSETTINGS_MAX_INT_LENGTH, m_szName);
	return atof(szBuff);
}

void CGSettings::SetDouble(LPCTSTR szSection, LPCTSTR szName, double dValue)
{
	ASSERT(m_szName);
	char szBuff[GSETTINGS_MAX_INT_LENGTH];
	sprintf(szBuff, "%0.9g", dValue);
	WritePrivateProfileString(szSection, szName, szBuff, m_szName);
}

BOOL CGSettings::GetBool(LPCTSTR szSection, LPCTSTR szName, BOOL bDefault)
{
	ASSERT(m_szName);
	char szDefault[GSETTINGS_MAX_BOOL_LENGTH];
	szDefault[0] = bDefault ? '1' : '0';
	szDefault[1] = '\0';
	char szBuff[GSETTINGS_MAX_BOOL_LENGTH];
	GetPrivateProfileString(szSection, szName, szDefault, szBuff, GSETTINGS_MAX_BOOL_LENGTH, m_szName);

	return *szBuff == '1';
}

void CGSettings::SetBool(LPCTSTR szSection, LPCTSTR szName, BOOL bValue)
{
	ASSERT(m_szName);
	WritePrivateProfileString(szSection, szName, bValue ? "1" : "0", m_szName);
}

__int64 CGSettings::GetInt64(LPCTSTR szSection, LPCTSTR szName, __int64 n64Default)
{
	ASSERT(m_szName);
	char szBuff[GSETTINGS_MAX_INT_LENGTH];
	char szDefault[GSETTINGS_MAX_INT_LENGTH];
	sprintf(szDefault, "%I64d", n64Default);
	GetPrivateProfileString(szSection, szName, szDefault, szBuff, GSETTINGS_MAX_INT_LENGTH, m_szName);
	return _atoi64(szBuff);
}

void CGSettings::SetInt64(LPCTSTR szSection, LPCTSTR szName, __int64 n64Value)
{
	ASSERT(m_szName);
	char szBuff[GSETTINGS_MAX_INT_LENGTH];
	sprintf(szBuff, "%I64d", n64Value);
	WritePrivateProfileString(szSection, szName, szBuff, m_szName);
}

char** CGSettings::GetStrings(LPCTSTR szSection, LPCTSTR szName)
{
	return GetStringsLocal(GetString(szSection, szName, ""), ',');
}

char** CGSettings::GetStringsLocal(char* szString, char chDelim)
{
	if (!szString)
	{
		char** ppStrings = (char**)m_GAlloc.Alloc(sizeof(char*));
//		m_strings = new char*[1];
		ppStrings[0] = NULL;
		return ppStrings;
	}

	char* szScan = szString;

	int nString = 0;
	while (*szScan)
	{
		nString++;
		while (*szScan && (*szScan != chDelim))
			szScan++;

		if (*szScan == chDelim)
			szScan++;
	}

	char** ppStrings = (char**)m_GAlloc.Alloc((nString + 1) * sizeof(char*));
//	m_strings = new char*[nString + 1];	// space for last NULL

	szScan = szString;
	nString = 0;
	while (*szScan)
	{
		ppStrings[nString++] = szScan;
		while (*szScan && (*szScan != chDelim))
			szScan++;

		if (*szScan == chDelim)
			*(szScan++) = '\0';
	}

	ppStrings[nString] = NULL;
	return ppStrings;
}

void CGSettings::AddString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szString)
{
	char buff[GSETTINGS_MAX_STRING_LENGTH];
	buff[0] = '\0';

	for (char** szScan = GetStrings(szSection, szName); *szScan; szScan++)
	{
		if (!strcmp(*szScan, szString))
			return;	// Already present

		if (buff[0])
			strcat(buff, ",");

		strcat(buff, *szScan);
	}

	// Not found - Add it on the end.
	if (buff[0])
		strcat(buff, ",");
	strcat(buff, szString);

	SetString(szSection, szName, buff);
}

void CGSettings::RemoveString(LPCTSTR szSection, LPCTSTR szName, LPCTSTR szString)
{
	char buff[GSETTINGS_MAX_STRING_LENGTH];
	buff[0] = '\0';

	for (char** szScan = GetStrings(szSection, szName); *szScan; szScan++)
	{
		if (!strcmp(*szScan, szString))
			continue;	// skip this one - we are removing it

		if (buff[0])
			strcat(buff, ",");

		strcat(buff, *szScan);
	}

	SetString(szSection, szName, buff);
}

void CGSettings::AddInstance(LPCTSTR szTask, LPCTSTR szInstance)
{
	AddString(szTask, "instances", szInstance);
}

char* CGSettings::GetSectionNames()
{
	ASSERT(m_szName);
	char* szBuff = (char*)m_GAlloc.Alloc(GSETTINGS_MAX_STRINGS_LENGTH);
	if (GetPrivateProfileSectionNames(szBuff, GSETTINGS_MAX_STRINGS_LENGTH, m_szName))
		return szBuff;

	return NULL;
}

char** CGSettings::GetStringsSectionNames()
{
	return GetStringsLocal(GetSectionNames(), '\0');
}

void CGSettings::SetName(LPCTSTR szName)
{
	if (m_szName)
		delete [] m_szName;

	if (szName)
	{
		if (strchr(szName, '\\'))	// it's a pathname. Just copy it
		{
			m_szName = new char[strlen(szName) + 1];
			strcpy(m_szName, szName);
		}
		else
		{
			m_szName = new char[strlen(szName) + 3];
			sprintf(m_szName, ".\\%s", szName);
		}
	}
	else
		m_szName = NULL;
}
