//////////////////////////////////////////////////////////////////////
// The superb CGAlloc - (C) GW/Abbeytek Ltd
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "galloc.h"

//////////////////////////////////////////////////////////////////////

#define GALLOC_DEFAULT_BLOCK_SIZE	1024

//////////////////////////////////////////////////////////////////////

void CGAlloc::CGBlock::EnsureBuffExists(int nLen)
{
	if (m_pBuff)
		return;

	if (nLen < GALLOC_DEFAULT_BLOCK_SIZE)
		nLen = GALLOC_DEFAULT_BLOCK_SIZE;

	m_pBuff = new BYTE[nLen];
	m_nLen = nLen;
}

LPTSTR CGAlloc::CGBlock::Alloc(LPCTSTR szText)
{
	int nLen = strlen(szText) + 1;	// for '\0'

	EnsureBuffExists(nLen);

	if (m_nIndex + nLen > m_nLen)
		return NULL;	// No room

	strcpy((char*)m_pBuff + m_nIndex, szText);
	m_nIndex += nLen;
	return (char*)m_pBuff + m_nIndex - nLen;
}

void* CGAlloc::CGBlock::Alloc(int nLen)
{
	EnsureBuffExists(nLen);

	if (m_nIndex + nLen > m_nLen)
		return NULL;	// No room

	m_nIndex += nLen;
	return m_pBuff + m_nIndex - nLen;
}

//////////////////////////////////////////////////////////////////////

CGAlloc::CGAlloc(void) :
	m_pBlocks(NULL)
{
}

CGAlloc::~CGAlloc(void)
{
	FreeAll();
}

LPTSTR CGAlloc::Alloc(LPCTSTR szText)
{
	if (!m_pBlocks)
		m_pBlocks = new CGBlock;

	LPTSTR szRet = m_pBlocks->Alloc(szText);
	if (!szRet)
	{
		CGBlock* pNew = new CGBlock;
		pNew->m_pNext = m_pBlocks;
		m_pBlocks = pNew;
		szRet = m_pBlocks->Alloc(szText);
	}

	return szRet;
}

void* CGAlloc::Alloc(int nLen)
{
	if (!m_pBlocks)
		m_pBlocks = new CGBlock;

	void* szRet = m_pBlocks->Alloc(nLen);
	if (!szRet)
	{
		CGBlock* pNew = new CGBlock;
		pNew->m_pNext = m_pBlocks;
		m_pBlocks = pNew;
		szRet = m_pBlocks->Alloc(nLen);
	}

	return szRet;
}

void CGAlloc::FreeAll()
{
	while (m_pBlocks)
	{
		CGBlock* pOld = m_pBlocks;
		m_pBlocks = m_pBlocks->m_pNext;
		delete pOld;
	}
}

//////////////////////////////////////////////////////////////////////
