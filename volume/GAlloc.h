#pragma once

// GAlloc.h

class CGAlloc {
 public:
  CGAlloc(void);
  ~CGAlloc(void);

  LPTSTR Alloc(LPCTSTR szText);
  void* Alloc(int nLen);
  void FreeAll(void);

 protected:
  class CGBlock {
   public:
	CGBlock() : m_nIndex(0), m_pNext(NULL), m_pBuff(NULL) {}
	~CGBlock()	{	if (m_pBuff)	delete [] m_pBuff;	}

	LPTSTR Alloc(LPCTSTR szText);
	void* Alloc(int nLen);
		
	CGBlock* m_pNext;

   protected:
  	void EnsureBuffExists(int nLen);
	int m_nIndex;
	int m_nLen;
	BYTE* m_pBuff;
  } * m_pBlocks;
};