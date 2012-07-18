// volumeDlg.h : header file
//

#if !defined(AFX_VOLUMEDLG_H__2A3CB21A_3B22_4E95_AACF_D13743E34AE7__INCLUDED_)
#define AFX_VOLUMEDLG_H__2A3CB21A_3B22_4E95_AACF_D13743E34AE7__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
#include "GLWindow.h"
/////////////////////////////////////////////////////////////////////////////

class CVolumeDlg : public CDialog
{
    CRITICAL_SECTION lock;
	ConstVolume *cv;
	CalcVolume *calc;
	BOOL rotation;
// Construction
public:
	CVolumeDlg(ConstVolume*,CalcVolume*,CWnd* pParent = NULL);	// standard constructor
	~CVolumeDlg();
  void ShowError( const char* msg );
// Dialog Data
	//{{AFX_DATA(CVolumeDlg)
	enum { IDD = IDD_VOLUME_DIALOG };
		// NOTE: the ClassWizard will add data members here
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CVolumeDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
  CGLWindow *m_glWnd;
	HICON m_hIcon;
	CStatic e_w0,e_w1,e_h,e_l0,e_l1;
	CStatic e_nu,e_nl,e_erhostl,e_erhostu,e_erstartl,e_erstartu,e_erksl,e_erksu,e_l,e_r,e_u;
	CStatic e_vs,e_vp,e_v,e_ernzl,e_ernzu,e_nleave,e_nr0,e_ve;
    CStatic e_batt,e_nbatt,e_nve;
    CButton e_scanning,e_laserpoint,e_rotation,e_logtofile;
	// Generated message map functions
	//{{AFX_MSG(CVolumeDlg)
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnScanning();
    afx_msg void OnLaserPoint();
    afx_msg void OnRotation();
	afx_msg void OnLogtofile();
	virtual void OnCancel();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_VOLUMEDLG_H__2A3CB21A_3B22_4E95_AACF_D13743E34AE7__INCLUDED_)
