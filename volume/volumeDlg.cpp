// volumeDlg.cpp : implementation file
//

#include "stdafx.h"
#include "volume.h"
#include "volumeDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog {
public:
  CAboutDlg();
  
  // Dialog Data
  //{{AFX_DATA(CAboutDlg)
  enum { IDD = IDD_ABOUTBOX };
  //}}AFX_DATA
  
  // ClassWizard generated virtual function overrides
  //{{AFX_VIRTUAL(CAboutDlg)
protected:
  virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
  //}}AFX_VIRTUAL
  
  // Implementation
protected:
  //{{AFX_MSG(CAboutDlg)
  //}}AFX_MSG
  DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)  {
  //{{AFX_DATA_INIT(CAboutDlg)
  //}}AFX_DATA_INIT
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)  {
  CDialog::DoDataExchange(pDX);
  //{{AFX_DATA_MAP(CAboutDlg)
  //}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
//{{AFX_MSG_MAP(CAboutDlg)
// No message handlers
//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CVolumeDlg dialog

CVolumeDlg::CVolumeDlg(ConstVolume *v,CalcVolume *c,CWnd* pParent /*=NULL*/)
: CDialog(CVolumeDlg::IDD, pParent) {
  //{{AFX_DATA_INIT(CVolumeDlg)
		// NOTE: the ClassWizard will add member initialization here
  //}}AFX_DATA_INIT
  // Note that LoadIcon does not require a subsequent DestroyIcon in Win32
  m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
  InitializeCriticalSection(&lock);
  cv=v;
  calc=c;
  rotation=false;
  m_glWnd=NULL;
}

void CVolumeDlg::DoDataExchange(CDataExchange* pDX) {
  CDialog::DoDataExchange(pDX);
  //{{AFX_DATA_MAP(CVolumeDlg)
  DDX_Control(pDX,IDC_NVE,e_nve);
  DDX_Control(pDX,IDC_ROTATION,e_rotation);
  DDX_Control(pDX,IDC_LASERPOINT,e_laserpoint);
  DDX_Control(pDX,IDC_LOGTOFILE,e_logtofile);
  DDX_Control(pDX,IDC_SCANNING,e_scanning);
  DDX_Control(pDX,IDC_BATT,e_batt);
  DDX_Control(pDX,IDC_NBATT,e_nbatt);
  DDX_Control(pDX,IDC_W0,e_w0);
  DDX_Control(pDX,IDC_W1,e_w1);
  DDX_Control(pDX,IDC_H,e_h);
  DDX_Control(pDX,IDC_L0,e_l0);
  DDX_Control(pDX,IDC_L1,e_l1);
  DDX_Control(pDX,IDC_NU,e_nu);
  DDX_Control(pDX,IDC_NL,e_nl);
  DDX_Control(pDX,IDC_NLEAVE,e_nleave);
  DDX_Control(pDX,IDC_NR0,e_nr0);
  DDX_Control(pDX,IDC_L,e_l);
  DDX_Control(pDX,IDC_R,e_r);
  DDX_Control(pDX,IDC_U,e_u);
  DDX_Control(pDX,IDC_VS,e_vs);
  DDX_Control(pDX,IDC_VP,e_vp);
  DDX_Control(pDX,IDC_V,e_v);
  DDX_Control(pDX,IDC_VE,e_ve);
  //}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CVolumeDlg, CDialog)
//{{AFX_MSG_MAP(CVolumeDlg)
ON_WM_SYSCOMMAND()
ON_WM_PAINT()
ON_WM_QUERYDRAGICON()
ON_BN_CLICKED(IDC_SCANNING, OnScanning)
ON_BN_CLICKED(IDC_LASERPOINT, OnLaserPoint)
ON_BN_CLICKED(IDC_ROTATION, OnRotation)
ON_BN_CLICKED(IDC_LOGTOFILE, OnLogtofile)
//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CVolumeDlg message handlers

BOOL CVolumeDlg::OnInitDialog() {
  CDialog::OnInitDialog();
  
  // Add "About..." menu item to system menu.
  
  // IDM_ABOUTBOX must be in the system command range.
  /*	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
  ASSERT(IDM_ABOUTBOX < 0xF000);
  
    CMenu* pSysMenu = GetSystemMenu(FALSE);
    if (pSysMenu != NULL)
    {
    CString strAboutMenu;
    strAboutMenu.LoadString(IDS_ABOUTBOX);
    if (!strAboutMenu.IsEmpty())
    {
    pSysMenu->AppendMenu(MF_SEPARATOR);
    pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
    }
    }
  */
  // Set the icon for this dialog.  The framework does this automatically
  //  when the application's main window is not a dialog
  SetIcon(m_hIcon, TRUE);			// Set big icon
  SetIcon(m_hIcon, FALSE);		// Set small icon
  
  if(cv->flag.render!=cv->RENDER_OFF) {
    WNDCLASS wndClass;
    
    wndClass.style = CS_OWNDC | CS_HREDRAW | CS_VREDRAW;
    wndClass.lpfnWndProc = AfxWndProc;
    wndClass.cbClsExtra = 0;
    wndClass.cbWndExtra = 0;
    wndClass.hInstance = AfxGetInstanceHandle();
    wndClass.hIcon = NULL;
    wndClass.hCursor = NULL;
    wndClass.hbrBackground = NULL;
    wndClass.lpszMenuName = NULL;
    wndClass.lpszClassName = "OpenGLClass";
    AfxRegisterClass(&wndClass);
    
    CRect glRect;
    CRect dlgRect;
    CRect clRect;
    GetWindowRect(&dlgRect);
    GetClientRect(&clRect);
    GetDlgItem(IDC_DIALOG_FRAME)->GetWindowRect(&glRect);    
    CRect newRect;
    newRect.top = glRect.top - dlgRect.top - (dlgRect.Height() - clRect.Height()) +18;
    newRect.bottom = glRect.bottom - dlgRect.top - (dlgRect.Height() - clRect.Height()) -3;
    newRect.left = glRect.left - dlgRect.left - (dlgRect.Width() - clRect.Width()) +6;
    newRect.right = glRect.right - dlgRect.left  - (dlgRect.Width() - clRect.Width()) +1;		
    
    m_glWnd=new CGLWindow(cv,calc,&rotation);
//    ::CreateWindow("OpenGLClass",NULL, WS_CHILD,
//      newRect.left,newRect.top,newRect.right-newRect.left+1,newRect.bottom-newRect.top+1,
//      this->m_hWnd,NULL,(HINSTANCE)GetWindowLong(this->m_hWnd,GWL_HINSTANCE),NULL);
    m_glWnd->Create("OpenGLClass",NULL, WS_CHILD | WS_VISIBLE, //WS_OVERLAPPEDWINDOW | WS_CLIPCHILDREN | WS_CLIPSIBLINGS | ,
      newRect,this,901,NULL);	 
//    ::SetWindowPos(m_glWnd->m_hWnd,HWND_NOTOPMOST,0,0,0,0,SWP_SHOWWINDOW);
    m_glWnd->Invalidate(FALSE);

    if(cv->flag.render!=cv->RENDER_3D) e_rotation.EnableWindow(FALSE);
  } else e_rotation.EnableWindow(FALSE);
		
  SetCursorPos(0,0);
  
  return TRUE;  // return TRUE  unless you set the focus to a control
}

void CVolumeDlg::OnSysCommand(UINT nID, LPARAM lParam) {
/*	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
{
CAboutDlg dlgAbout;
dlgAbout.DoModal();
}
  else*/
  {
  CDialog::OnSysCommand(nID, lParam);
  }
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CVolumeDlg::OnPaint() {
  if (IsIconic()) {
    CPaintDC dc(this); // device context for painting
    
    SendMessage(WM_ICONERASEBKGND, (WPARAM) dc.GetSafeHdc(), 0);
    
    // Center icon in client rectangle
    int cxIcon = GetSystemMetrics(SM_CXICON);
    int cyIcon = GetSystemMetrics(SM_CYICON);
    CRect rect;
    GetClientRect(&rect);
    int x = (rect.Width() - cxIcon + 1) / 2;
    int y = (rect.Height() - cyIcon + 1) / 2;
    
    // Draw the icon
    dc.DrawIcon(x, y, m_hIcon);
  } else {			
    CriticalData crdata_save;
    EnterCriticalSection(&lock);
 		 crdata_save=calc->crdata;
     LeaveCriticalSection(&lock);
     
     CString s;
     s.Format("%6.2f",cv->vol.Wleft); e_w0.SetWindowText(s);
     s.Format("%6.2f",cv->vol.Wright); e_w1.SetWindowText(s);
     s.Format("%6.2f",cv->vol.Height); e_h.SetWindowText(s);
     s.Format("%6.2f",cv->vol.Lstart); e_l0.SetWindowText(s);
     s.Format("%6.2f",cv->vol.Lend); e_l1.SetWindowText(s);
     s.Format("%6.2f",cv->vol.vs); e_vs.SetWindowText(s);
     
     s.Format("%d",crdata_save.count.nve); e_nve.SetWindowText(s);
     s.Format("%d",crdata_save.count.nu); e_nu.SetWindowText(s);
     s.Format("%d",crdata_save.count.nl); e_nl.SetWindowText(s);
     s.Format("%d",crdata_save.count.nbatt); e_nbatt.SetWindowText(s);
     s.Format("%d",crdata_save.count.nleave); e_nleave.SetWindowText(s);
     s.Format("%d",crdata_save.count.nbadve); e_nr0.SetWindowText(s);
     
     s.Format("%6.2f",crdata_save.lm.batt); e_batt.SetWindowText(s);
     
     s.Format("%6.2f",crdata_save.lm.l); e_l.SetWindowText(s);
     s.Format("%6.2f",crdata_save.slm.r); e_r.SetWindowText(s);
     s.Format("%6.2f",crdata_save.slm.u); e_u.SetWindowText(s);
     
     s.Format("%8.6f",crdata_save.vol.ve); e_ve.SetWindowText(s);
     
     if(cv->demo) {
       s.Format("DEMO");
       e_vp.SetWindowText(s);
       e_v.SetWindowText(s);
     } else {
       s.Format("%6.2f",crdata_save.vol.vp); e_vp.SetWindowText(s);
       s.Format("%6.2f",crdata_save.vol.v); e_v.SetWindowText(s);
     }
     CDialog::OnPaint();
  }
}

// The system calls this to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CVolumeDlg::OnQueryDragIcon() {
  return (HCURSOR) m_hIcon;
}

void CVolumeDlg::OnRotation() {
  rotation=!rotation;
}

void CVolumeDlg::OnLogtofile() {
  calc->LogToFile(!calc->IsLoggingToFile());
}

void CVolumeDlg::OnLaserPoint() {
  calc->ShowPointer(!calc->IsPointerVisible());
}

void CVolumeDlg::ShowError( const char* msg ){
  TRACE("ERROR: %s\n",msg);  
  if(cv->flag.render!=cv->RENDER_OFF){
   // m_glWnd->MessageBox(msg,"ERROR",MB_OK|MB_ICONERROR|MB_TOPMOST);
  } else {
  //  ::MessageBox(m_hWnd,msg,"ERROR",MB_OK|MB_ICONERROR);
  }
}

void CVolumeDlg::OnScanning() {
  TRACE("Scanning/Stop pressed..\n");
  BOOL bStart = calc->GetState() != SLMstate_scanning;

  calc->RecordPoints(bStart);  
  if(bStart){
    if(calc->getState() == calc->state_ready) {
      e_scanning.SetWindowText("Stop");
      e_laserpoint.EnableWindow(FALSE);
      calc->StartMotor(TRUE);
    } else {
      ShowError("Scanning: Cann't connect to SLM, try set times.slm_answer in config-file.");
    }
  } else { 
    e_scanning.SetWindowText("Scanning");
    e_laserpoint.EnableWindow(TRUE);
    calc->StartMotor(bStart);
  }
}

CVolumeDlg::~CVolumeDlg() {
  if(calc->GetState() == SLMstate_scanning) { 
    OnScanning();
    OnLaserPoint();
  }
  if(cv->flag.render!=cv->RENDER_OFF) if(m_glWnd) delete m_glWnd;
}

void CVolumeDlg::OnCancel() {
  CDialog::OnCancel();
}

