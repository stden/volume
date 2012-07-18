// GLWindow.cpp : implementation file

#include "stdafx.h"
#include <math.h>
#include "GLWindow.h"
#include "calc_vol.h"
#include <fstream>
#include <iostream>
using namespace std;
#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

#if defined(APIENTRY)
#undef APIENTRY
#endif

BYTE* gltResourceBMPBits(UINT nResource, int *nWidth, int *nHeight)	{
	HINSTANCE hInstance;	// Instance Handle
	HANDLE hBitmap;			// Handle to bitmap resource
	BITMAPINFO bmInfo;

	// Find the bitmap resource
	hInstance = GetModuleHandle(NULL);
	hBitmap = LoadBitmap(hInstance,MAKEINTRESOURCE(nResource));

	if(hBitmap == NULL)
		return NULL;

	GetObject(hBitmap,sizeof(BITMAPINFO),&bmInfo);
	DeleteObject(hBitmap);

	hBitmap = ::LoadResource(hInstance,
		 ::FindResource(hInstance,MAKEINTRESOURCE(nResource), RT_BITMAP));

	if(hBitmap == NULL)
		return NULL;

	BYTE *pData = (BYTE *)LockResource(hBitmap);
	pData += sizeof(BITMAPINFO)-1;

	*nWidth = bmInfo.bmiHeader.biWidth; 
	*nHeight = bmInfo.bmiHeader.biHeight;

	return pData;
}

/////////////////////////////////////////////////////////////////////////////
// CGLWindow

CGLWindow::CGLWindow(ConstVolume *constvolume,CalcVolume *calcvolume,BOOL *p)
{
	GL_ListCnt = 0;
	m_hRC = NULL; m_hDC = NULL;
	cv=constvolume;
	calc=calcvolume;
	rotation=p;
    rcam=170.;//cv->vol.Wleft+cv->vol.Wright;
	an=0; drcam=1.; rmincam=1.; rmaxcam=100.;
	pr3d=false;
	rot_x_gr=60;
	rot_y_gr=-20;

  for(int j=MIN_ANGLE;j<=MAX_ANGLE;j++) { 
    double dj=((double)j*0.01-90.)*SLM_PI/180.;
    SinAngle[j-MIN_ANGLE] = sin(dj);
    CosAngle[j-MIN_ANGLE] = cos(dj);
  }
}

CGLWindow::~CGLWindow()
{ 
	if(cv->flag.render==cv->RENDER_2D) {
		DeleteDC(mdc);
		DeleteObject(mbmp);
		for(int i=0;i<cv->render.hcolor;i++) DeleteObject(brush[i]);
	}
}

BEGIN_MESSAGE_MAP(CGLWindow, CWnd)
	//{{AFX_MSG_MAP(CGLWindow)
	ON_WM_PAINT()
	ON_WM_CREATE()
	ON_WM_DESTROY()
	ON_WM_SIZE()
	ON_WM_MOUSEMOVE()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CGLWindow message handlers

inline void CGLWindow::BeginSetList() { 
//  TRACE("BeginSetList()");
  int id = GL_ListCnt;  
  listid[id]=glGenLists(1);
  glNewList(listid[id],GL_COMPILE);
}

inline void CGLWindow::EndSetList(){ 
//  TRACE("EndSetList()");
  glEndList();
  GL_ListCnt++;
}

void CGLWindow::printstring(void *font, char *string)
{
  int len,i;
  len=(int)strlen(string);
  for(i=0;i<len;i++) glutBitmapCharacter(font,string[i]);
}

void CGLWindow::DrawDebug(double l)
{
  char str[255];
  glPushMatrix();
  glDisable(GL_DEPTH_TEST);
  glMatrixMode(GL_PROJECTION);
  glPushMatrix();
  glLoadIdentity();
  gluOrtho2D(0,ww,hh,0);
  glMatrixMode(GL_MODELVIEW); 
  glLoadIdentity();
  glColor4f(0.3,0.3,0.3,.5);
  glRecti(ww-2,2,ww-100,150);
  glColor3f(1.,0,0);
  glRasterPos2i(ww-70,10);
  printstring(GLUT_BITMAP_TIMES_ROMAN_10,"Information");
  memset(str,0,255);
  sprintf(str,"%6.2f",l);
  glRasterPos2i(ww-90,30);
  printstring(GLUT_BITMAP_TIMES_ROMAN_10,str);

  glEnable(GL_DEPTH_TEST);
  glMatrixMode(GL_PROJECTION);
  glPopMatrix();
  glMatrixMode(GL_MODELVIEW); 
  glPopMatrix();
}

void CGLWindow::DrawAxis(double len,double mesh,double rad)
{ int i,j; //GLboolean lght,bld;
  glPushMatrix();
  glBegin(GL_LINES);
   glColor3f(1.,0,0);
   glVertex3d(-len,0,0); glVertex3d(len,0,0); 
   glColor3f(0,1.,0);
   glVertex3d(0,0,0); glVertex3d(0,len,0);  
   glColor3f(0,0,1.);
   glVertex3d(0,0,0); glVertex3d(0,0,len);  
  glEnd();
  if(rad&&mesh) {
    GLUquadricObj *quadObj = gluNewQuadric();
    gluQuadricDrawStyle(quadObj,GLU_FILL);
    gluQuadricNormals(quadObj,GLU_NONE);
    for(j=0;j<3;j++){
      switch(j) { 
	    case 0: glColor3f(1.,0,0); break;
        case 1: glColor3f(0,1.,0); break;
        case 2: glColor3f(0,0,1.); break;
	  }
      glPushMatrix();
      for(i=0;i<len/mesh;i++){
        glTranslated((j==0)*mesh,(j==1)*mesh,(j==2)*mesh);
        gluSphere(quadObj,rad,16,16);
	  }
      glPopMatrix();
	  if(!j) {
        glPushMatrix();
        for(i=0;i<len/mesh;i++){
          glTranslated(-mesh,0,0);
          gluSphere(quadObj,rad,16,16);
		}
        glPopMatrix();
	  }
	}
    gluDeleteQuadric(quadObj);
  }
  glPopMatrix();
}

void CGLWindow::Draw_LM(){
  EnterCriticalSection(&lock);
   double z = calc->crdata.lm.l;
  LeaveCriticalSection(&lock);
  double s = 0.1;
//   glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
  int i, t[4][2] = { { -1, -1 }, { -1, 1 }, { 1, 1 }, { 1, -1 } };
  glBegin(GL_QUADS);
   glColor4f(1.,1.,0,1.);
   for(i=0;i<4;i++) glVertex3d( t[i][0]*s, t[i][1]*s, z-s ); 
   for(i=0;i<4;i++) glVertex3d( t[i][0]*s, t[i][1]*s, z+s );    
   glColor4f(0.,1.,1.,1.);
   for(i=0;i<4;i++) glVertex3d( -s, t[i][0]*s, z+t[i][1]*s );    
   for(i=0;i<4;i++) glVertex3d( s, t[i][0]*s, z+t[i][1]*s );    
   glColor4f(1.,0.,1.,1.);
   for(i=0;i<4;i++) glVertex3d( t[i][0]*s, -s, z+t[i][1]*s );    
   for(i=0;i<4;i++) glVertex3d( t[i][0]*s, s, z+t[i][1]*s );    
  glEnd();
  //  glBlendFunc(GL_SRC_COLOR, GL_DST_COLOR);
}

void CGLWindow::DrawBound(double L0,double L1,double W0,double W1,double H)
{	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	glColor4f(1.,0,0,.3);
	glBegin(GL_QUADS);
     //glVertex3d(-W0,0,L0); glVertex3d(W1,0,L0); glVertex3d(W1,H,L0); glVertex3d(-W0,H,L0);
     glVertex3d(-W0,0,L1); glVertex3d(W1,0,L1); glVertex3d(W1,H,L1); glVertex3d(-W0,H,L1);
	glEnd();
	glColor4f(0,1.,0,.3);
	glBegin(GL_QUADS);
	 //glVertex3d(-W0,0,L0); glVertex3d(-W0,0,L1); glVertex3d(-W0,H,L1); glVertex3d(-W0,H,L0);
	 glVertex3d(W1,0,L0); glVertex3d(W1,0,L1); glVertex3d(W1,H,L1); glVertex3d(W1,H,L0); 	 
	glEnd();
	glColor4f(0,0,1.,.3);
	glBegin(GL_QUADS);
	 glVertex3d(-W0,H,L0); glVertex3d(-W0,H,L1); glVertex3d(W1,H,L1); glVertex3d(W1,H,L0);
    glEnd();
    glBlendFunc(GL_SRC_COLOR, GL_DST_COLOR);
}

void CGLWindow::DrawSurface() {
  if(!cv->history.render) return;

  // if(GL_ListCnt < 90) BeginSetList();
  // EnterCriticalSection(&lock);
   double VolumeHeight = cv->vol.Height;
   glPointSize(1.);
   glBegin(GL_POINTS);
   glColor4f(1.0,0,0,1.);
   for(int j=MIN_ANGLE;j<=MAX_ANGLE;j++) { 
  	 PointDevice *cr = calc->cr_prender[j];
     for(int i=0;i<cv->history.render;i++) {
	     if (cr[i].r==0) continue;
	     double hcolor = cr[i].h / VolumeHeight;
       glColor4f(hcolor,hcolor,hcolor,1.);
       glVertex3d(cr[i].r*CosAngle[j-MIN_ANGLE],cr[i].r*SinAngle[j-MIN_ANGLE],cr[i].l);
     }
   }
   glEnd();
   glPointSize(1.); 
//  LeaveCriticalSection(&lock);
//  if(GL_ListCnt < 90) EndSetList();
}

MatrixGL CGLWindow::RotateCam(VectorGL &coord,VectorGL &dir,VectorGL &top)
{ 
  MatrixGL mt;
    coord.s.z=rcam;
    top.s.z=rcam; 
  glPushMatrix();
    glLoadIdentity(); 

    glRotated(rot_y_gr,1.,0,0);
    glRotated(rot_x_gr,0,1.,0);
    rot_x_gr=rot_y_gr=0;
	glMultMatrixd(mrot.m);
    glGetDoublev(GL_MODELVIEW_MATRIX,mrot.m);
  glPopMatrix();
  mt=~mrot;
  coord=coord*mt;
  top=top*mt;
  dir=dir*mt;
  return mt;
}

MatrixGL CGLWindow::RotateCam(double angle,VectorGL rot,VectorGL &coord,VectorGL&,VectorGL &top) { 
  MatrixGL mt;
  glPushMatrix();
    glLoadIdentity();
    glRotated(angle,rot.s.x,rot.s.y,rot.s.z);
    glGetDoublev(GL_MODELVIEW_MATRIX,mt.m);
    coord=coord*mt;
    top=top*mt;
  glPopMatrix();
  return mt;
}

void CGLWindow::OnPaint() {    
  static int last_index = 0;
     
  if(cv->flag.render==cv->RENDER_OFF) return;
  static DWORD t0=GetTickCount();
  DWORD t=GetTickCount();
  if(!pr3d&&((t-t0)<cv->times.render||m_hRC==NULL)) return;
  pr3d=false;

  t0=t;

  if(cv->flag.render==cv->RENDER_2D) {
	CPaintDC dc(this);

	EnterCriticalSection(&lock);
    int index=calc->cr_index;
	LeaveCriticalSection(&lock);

	RECT mr;
	for(int j=0;j<cv->render.y2d;j++) {
		mr.top=j*rect_height;
		mr.bottom=(j+1)*rect_height;
		int left, right; // Левая и правая граница участка, который будем перерисовывать
		if(!cv->render.part){ 
			left = 0;
			right = cv->render.x2d-1;
		} else {
			left = max( last_index-1, 0 );
			right = min( index, cv->render.x2d-1 ); 
			last_index = index;
		}		
    	 for(int i=left;i<=right;i++) {
			EnterCriticalSection(&lock);
			 double h=calc->cr_hcolor[j][i];
			LeaveCriticalSection(&lock);
			mr.left=i*rect_width;
			mr.right=(i+1)*rect_width;
			if(!cv->render.buf2d) {
				FillRect(dc,&mr,brush[GetHColor(h)]);
			} else 
				FillRect(mdc,&mr,brush[GetHColor(h)]);
		 }
	}
	if(cv->render.buf2d) BitBlt(dc,0,0,win_width,win_height,mdc,0,0,SRCCOPY);
  }

  if(cv->flag.render==cv->RENDER_3D) { 
    long tt = GetCurrentTime();

    double l;
    EnterCriticalSection(&lock);
	  l=calc->crdata.lm.l;
    LeaveCriticalSection(&lock);

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
  
    glPushAttrib(GL_ENABLE_BIT);
    //VectorGL eye(-rcam/4.,-2.*rcam,rcam/2.+l+cv->vol.Height/8.);
    EnterCriticalSection(&lock);
	   double y = cv->vol.Height / 2.0;
	   double z = calc->crdata.lm.l;
    LeaveCriticalSection(&lock);
    //VectorGL eye(0,y,z-rcam);
    //VectorGL eyedir(0,y,z); // Смотрим на LM/SLM
    //VectorGL eyetop(0,y-1.,z);
    VectorGL eye(0,0,-rcam);
    VectorGL eyedir(0,0,0); // Смотрим на LM/SLM
    VectorGL eyetop(0,-1.,0);
    if(*rotation) {
	  VectorGL rt(0,1.,0);
	  RotateCam(an,rt,eye,eyedir,eyetop);
	  an+=1.; if(an>360.) an-=360.;
	}else RotateCam(eye,eyedir,eyetop);

    gluLookAt( eye.s.x, eye.s.y, eye.s.z,
		       eyedir.s.x, eyedir.s.y, eyedir.s.z,
			   eyetop.s.x, eyetop.s.y, eyetop.s.z );
	//TRACE("GL(1) = %ld\n",GetCurrentTime()-tt);
    DrawSurface();
	//TRACE("GL(2) = %ld\n",GetCurrentTime()-tt);
	for(int i=0;i<GL_ListCnt;i++)
      glCallList(listid[i]);
	Draw_LM();
	//TRACE("GL(3) = %ld\n",GetCurrentTime()-tt);
    // DrawDebug(l);
	//TRACE("GL(4) = %ld\n",GetCurrentTime()-tt);
    SwapBuffers(m_hDC);
	//TRACE("GL(5) = %ld\n",GetCurrentTime()-tt);
  }
}

int CGLWindow::OnCreate(LPCREATESTRUCT lpCreateStruct) {
	if (CWnd::OnCreate(lpCreateStruct) == -1) return -1;	

	m_hDC = ::GetDC(this->m_hWnd); // Get the device context
	
	static	PIXELFORMATDESCRIPTOR pfd=
	{
		sizeof(PIXELFORMATDESCRIPTOR),	1,
		PFD_DRAW_TO_WINDOW |
		PFD_SUPPORT_OPENGL |
		PFD_DOUBLEBUFFER,
		PFD_TYPE_RGBA,
		32,	0, 0, 0, 0, 0, 0, 0, 0,	0, 0, 0, 0, 0, 32, 0, 0, PFD_MAIN_PLANE,
		0, 0, 0, 0
	};    
    
    m_nPixelFormat=ChoosePixelFormat(m_hDC,&pfd);	    
	SetPixelFormat(m_hDC, m_nPixelFormat, NULL);

	// Create the rendering context and make it current
	m_hRC = wglCreateContext(m_hDC);

	if(m_hRC != NULL)
		wglMakeCurrent(m_hDC, m_hRC);

	// Setup the context
	if(m_hRC != NULL) {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glGenTextures(6, m_Sides);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glFrontFace(GL_CCW);
	}
    CRect r;
    GetWindowRect(&r);
    InitGL(r.Width(),r.Height());
    glLoadIdentity();   
     glGetDoublev(GL_MODELVIEW_MATRIX,mrot.m);
	// AXIS
    BeginSetList();
     DrawAxis(100.,1.,0.03);
    EndSetList();	
	// BOUND
    BeginSetList();
	 DrawBound(cv->vol.Lstart,cv->vol.Lend,cv->vol.Wright,cv->vol.Wleft,cv->vol.Height);
	EndSetList();

    InitializeCriticalSection(&lock);
	if(cv->flag.render==cv->RENDER_2D) {
		RECT r;
		GetWindowRect(&r);
		win_width=r.right-r.left; win_height=r.bottom-r.top;
		if(cv->render.x2d>win_width) cv->render.x2d=win_width;
		if(cv->render.y2d>win_height) cv->render.y2d=win_height;
		rect_width=win_width/(double)cv->render.x2d; rect_height=win_height/(double)cv->render.y2d; // + 0.3;
		vol_width=(cv->vol.Lend-cv->vol.Lstart)/(double)cv->render.x2d; 
		vol_height=(cv->vol.Wleft+cv->vol.Wright)/(double)cv->render.y2d;
		CPaintDC dc(this);
		mdc=CreateCompatibleDC(dc);
		mbmp=CreateCompatibleBitmap(dc,win_width,win_height);
        SelectObject(mdc,mbmp);
		brush=new HBRUSH[cv->render.hcolor];
		for(int i=0;i<cv->render.hcolor;i++) 
			brush[i]=CreateSolidBrush(i+(i<<8)+(i<<16));
	}
	return 0;
}

void CGLWindow::OnDestroy() {
	CWnd::OnDestroy();

	if(m_hRC){	
		glDeleteTextures(6, m_Sides);

		wglMakeCurrent(m_hDC, NULL);
		wglDeleteContext(m_hRC);
		m_hRC = NULL;
	}

	::ReleaseDC(this->m_hWnd, m_hDC);
	m_hDC = NULL;
}

void CGLWindow::InitGL(int w,int h) {
  glDisable(GL_CULL_FACE);
  //glShadeModel(GL_SMOOTH);
  glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
  glClearDepth(1.0f);
  glEnable(GL_DEPTH_TEST);
  glDepthFunc(GL_LEQUAL);

  glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
  
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();

  gluPerspective(60./(GLfloat)w*(GLfloat)h,(GLfloat)w/(GLfloat)h,0.001f,1000.0f);
  glMatrixMode(GL_MODELVIEW);
  glEnable(GL_BLEND);
  glBlendFunc(GL_SRC_COLOR, GL_DST_COLOR);
  //glEnable (GL_NORMALIZE);
  //glEnable(GL_COLOR_MATERIAL);
  ww=w;
  hh=h;
}

void CGLWindow::OnSize(UINT nType, int w, int h) {
	CWnd::OnSize(nType, w, h);
	InitGL(w,h);
}

int CGLWindow::GetHColor(double h) {
	int color=(int)((double)(cv->render.hcolor-1)*h/cv->vol.Height);
	if(color<0){ 
		color=0; 
	} else 
		if(color>cv->render.hcolor-1) color=cv->render.hcolor-1;
	return color;
}

void CGLWindow::OnMouseMove(UINT nFlags, CPoint point) {	
	if ((nFlags == MK_RBUTTON) && (point.y != oldy)) {
		if (point.y > oldy)	{	
			rcam-=drcam; 
			if(rcam>rmaxcam) rcam=rmaxcam;
		} else {	
			rcam+=drcam; 
			if(rcam<rmincam) rcam=rmincam;
		}
		pr3d=true;
		SendMessage(WM_PAINT);
	}
	if ((nFlags == MK_LBUTTON) && (point.x!=oldx || point.y!=oldy)) {
		if(point.x!=oldx) rot_x_gr=1.0*(oldx-point.x);
		if(point.y!=oldy) rot_y_gr=1.0*(oldy-point.y);
		pr3d=true;
		SendMessage(WM_PAINT);
	}
	oldx = point.x; oldy = point.y; 
}


