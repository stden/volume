#ifndef __CALC_VOL_H__
#define __CALC_VOL_H__
#include "SLMSocket.h"

struct PointDevice
{
    double r;
    int s;
    double l;
	double h;
    DWORD t;
    PointDevice() { r=0; l=0; s=0; t=0;}
};

struct PointLM
{
    double l;
    DWORD t;
};

struct ConstVolume
{
	struct { 
		double Lstart,Lend,Height,Wleft,Wright,vs; 
		int model,angmin,angmax,rmin,rmax; 
	} vol;
	struct SLM { 
		double step; 
		int freq; 
		#define maxipstr 16
		char ip[maxipstr]; 
		SLM() { memset(ip,0,maxipstr); }
	} slm;
	struct LM{ 
		int nComDist,nComBatt;
		#define maxnamefilestr 100
		char NameFileImitLM[maxnamefilestr]; 
		LM() { memset(NameFileImitLM,0,maxnamefilestr); }
	} lm;
	struct { int render,lm; } history;
	struct { DWORD render,result,batt,slm_answer,slm_control; } times;
	struct { 
		BOOL imitmode,virt,lm,negative,rbound; 
		int render; 
	} flag;
	struct { 
		int x2d,y2d,hcolor; 
		BOOL buf2d,havarage,part; 
	} render;
	enum {RENDER_OFF,RENDER_2D,RENDER_3D};
	BOOL demo;
};

struct CriticalCount
{
	int nl,nu,nleave,nbadve,nve,nbatt;
	CriticalCount() { nl=nu=nleave=nbadve=nve=nbatt=0; }
};

struct CriticalData 
{
	struct { double batt,l; } lm;
	struct { double u,r; } slm;
	struct { double vp,ve,v; } vol;
	CriticalCount count;
	CriticalData() { memset(&slm,0,sizeof(slm)); }
};

class CalcVolume : public CSLMSocket
{
    CRITICAL_SECTION lock;
	ConstVolume cv;
	double sin_step,cos_step,ulb,urb;
	int deltaprev;
	PointDevice rl[2][SLM_MAX_ANGLE];
	PointDevice *pnow,*pold;

    virtual void OnPointDetected(int nAngle, int nRange, int nSignal, DWORD dwTicks, int npacket);
    double Wall(double,double);
    void Calc(int u,CriticalData*);
    double GetL(DWORD t);
	BOOL InitSLM();
	BOOL InitRender();
  public:
	CriticalData crdata;
	PointLM *cr_plm;
	PointDevice *cr_prender[SLM_MAX_ANGLE];
	double **cr_hcolor;
	int **cr_points_count;
	int cr_index;
    CalcVolume();
	~CalcVolume();
	BOOL InitCalc(ConstVolume);
  void StartMotor(BOOL bStart);	
};
#endif