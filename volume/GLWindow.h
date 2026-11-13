#if !defined(AFX_GLWINDOW_H__048D92C6_6DF2_11D3_8A92_00A0CC2492EC__INCLUDED_)
#define AFX_GLWINDOW_H__048D92C6_6DF2_11D3_8A92_00A0CC2492EC__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// GLWindow.h : header file
//

#include "vector.h"
#include "calc_vol.h"

/////////////////////////////////////////////////////////////////////////////
// CGLWindow window

#define MIN_ANGLE 9000
#define MAX_ANGLE 27000

class CGLWindow : public CWnd
{
    double SinAngle[MAX_ANGLE-MIN_ANGLE+1],CosAngle[MAX_ANGLE-MIN_ANGLE+1];
    CRITICAL_SECTION lock;
	ConstVolume *cv;
	CalcVolume *calc;
	int win_width,win_height;
	double rect_width,rect_height;
	double vol_width,vol_height;
	BOOL *rotation;
	HDC mdc;
	HBITMAP mbmp;
	HBRUSH *brush; 
		
// Construction
public:
	CGLWindow(ConstVolume*,CalcVolume*,BOOL*);

// Attributes
public:

// Operations
public:

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CGLWindow)
	//}}AFX_VIRTUAL

// Implementation
public:
	int m_nPixelFormat;
	HGLRC m_hRC;
	HDC m_hDC;
	virtual ~CGLWindow();
	int oldx,oldy; 
	bool pr3d;
	// Generated message map functions
protected:
//    enum DRAWLISTGL {DL_AXIS = 0,DL_BOUND = 1};
    int ww,hh;
    double an,rcam;
    double drcam,rmincam,rmaxcam;
    int rot_x_gr, rot_y_gr;
    MatrixGL mrot;
    int listid[100];
	int GL_ListCnt;
    void InitGL(int w,int h);
    void BeginSetList();
    void EndSetList();
    void printstring(void *font, char *string);
    void DrawDebug(double);
    void DrawSurface();
	void Draw_LM();
    void DrawAxis(double len,double mesh,double rad);
    void DrawBound(double L0,double L1,double W0,double W1,double H);
    MatrixGL RotateCam(VectorGL &coord,VectorGL &dir,VectorGL &top);
    MatrixGL RotateCam(double angle,VectorGL rot,VectorGL &coord,VectorGL&,VectorGL &top);
	int GetHColor(double h);
	GLuint m_Sides[6];
	//{{AFX_MSG(CGLWindow)
	afx_msg void OnPaint();
	afx_msg int OnCreate(LPCREATESTRUCT lpCreateStruct);
	afx_msg void OnDestroy();
	afx_msg void OnSize(UINT nType, int cx, int cy);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_GLWINDOW_H__048D92C6_6DF2_11D3_8A92_00A0CC2492EC__INCLUDED_)
