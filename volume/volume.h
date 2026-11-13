// volume.h : main header file for the VOLUME application
//

#if !defined(AFX_VOLUME_H__94AEE3FB_F3C2_46F0_A164_BF742A2B4AE9__INCLUDED_)
#define AFX_VOLUME_H__94AEE3FB_F3C2_46F0_A164_BF742A2B4AE9__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols

#include "calc_vol.h"
#include "slmlib.h"

/////////////////////////////////////////////////////////////////////////////
// CVolumeApp:
// See volume.cpp for the implementation of this class
//

enum RS_TYPE {RS_LM,RS_BATT,RS_ALL};

struct SCOM { 
	  HANDLE		 hdev;
      OVERLAPPED	 olRead;
	  OVERLAPPED	 olWrite;
	  SCOM() { 
		  hdev=INVALID_HANDLE_VALUE; 
		  memset(&olRead,0,sizeof(OVERLAPPED)); 
		  memset(&olWrite,0,sizeof(OVERLAPPED));
	  }
};

class CVolumeApp : public CWinApp
{
    
	static SCOM rs[RS_ALL];

	static ConstVolume cv;
	static CalcVolume calc;
		
	CWinThread *dthread;
	static UINT DeviceThread(LPVOID pParams);

	void closeRS(HANDLE h);
	int  initRS(SCOM *scom,char *namefile);
	int  initRS(SCOM *scom,int nport);
	
	void LoadIniFile(char *name);
	BOOL InitLM();
	static int GetRS(RS_TYPE,double*);
	static void PutRS(RS_TYPE);
public:
	CVolumeApp();
	~CVolumeApp();
        
// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CVolumeApp)
	public:
	virtual BOOL InitInstance();
	//}}AFX_VIRTUAL

// Implementation

	//{{AFX_MSG(CVolumeApp)
		// NOTE - the ClassWizard will add and remove member functions here.
		//    DO NOT EDIT what you see in these blocks of generated code !
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};


/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_VOLUME_H__94AEE3FB_F3C2_46F0_A164_BF742A2B4AE9__INCLUDED_)
