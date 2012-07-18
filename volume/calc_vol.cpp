#include "stdafx.h"
#include "calc_vol.h"
#include <math.h>

void CalcVolume::OnPointDetected(int nAngle, int nRange, int nSignal, DWORD dwTicks, int npacket)
{
	if(GetState() != SLMstate_scanning) return;

	static int rendercount=0;
	static int nRangeOld=0;

	CriticalData crdata_save;
	EnterCriticalSection(&lock);
 	 crdata_save=crdata;
	LeaveCriticalSection(&lock);

    crdata_save.count.nu=npacket;
	BOOL er=FALSE;
	if(nAngle==m_nLastAngle || nAngle < cv.vol.angmin || nAngle > cv.vol.angmax) er=!er;

	double dj;
	if(!er) {
		if(cv.flag.rbound) {
			if(nRange<cv.vol.rmin||nRange>cv.vol.rmax) nRange=nRangeOld;
			else nRangeOld=nRange;
		}

		int angprev=nAngle-deltaprev;
		if( nAngle > cv.vol.angmin && angprev >= cv.vol.angmin && m_nLastAngle != angprev) {
			crdata_save.count.nleave++;
			if(m_fv) fprintf(m_fv,"missing: angle=%d,old=%d\n",nAngle,m_nLastAngle);
			// Debug: выводим какие углы тер€ютс€
			TRACE("missig m_nLastAngle = %d != angprev = %d \n",m_nLastAngle,angprev);
		}

		if(nAngle < m_nLastAngle) {
			if(pnow!=rl[0]) { 
				pnow=rl[0]; 
				pold=rl[1]; 
			} 
			else { 
				pnow=rl[1]; 
				pold=rl[0]; 
			}
			if(cv.flag.render==cv.RENDER_3D&&cv.history.render) { 
				rendercount++; 
				if(rendercount>=cv.history.render) rendercount=0;
			} 
		}
		crdata_save.slm.u=(double)nAngle/100.;
		
		if(cv.flag.virt) 
			crdata_save.slm.r=Wall((double)nRange/100.,crdata_save.slm.u);
		else 
			crdata_save.slm.r=(double)nRange/100.;
		
		pnow[nAngle].r=crdata_save.slm.r;
		pnow[nAngle].s=nSignal;
		pnow[nAngle].t=dwTicks;
		
		pnow[nAngle].l=crdata_save.lm.l;//GetL(dwTicks);
		//pnow[nAngle].l=GetL(dwTicks);

		//if(m_fv) fprintf(m_fv,"%6.2f,%0d,%6.2f\n",crdata_save.slm.r,nAngle,crdata_save.lm.l);

		dj=((double)nAngle*0.01-90.)*SLM_PI/180.;
		pnow[nAngle].h=cv.vol.Height-pnow[nAngle].r*sin(dj);

		if( nAngle > cv.vol.angmin && angprev >= cv.vol.angmin) Calc(nAngle,&crdata_save);
		m_nLastAngle=nAngle;    
	}
    EnterCriticalSection(&lock);
	 if(!er) {
		if(cv.flag.render==cv.RENDER_3D&&cv.history.render) cr_prender[nAngle][rendercount]=pnow[nAngle]; 
		if(cv.flag.render==cv.RENDER_2D) {
			double Width = cv.vol.Wleft+cv.vol.Wright;
			double Length = cv.vol.Lstart-cv.vol.Lend;
			int y=(double)cv.render.y2d*(cv.vol.Wleft-pnow[nAngle].r*cos(dj))/Width;
			int x=(double)cv.render.x2d*(cv.vol.Lstart-pnow[nAngle].l)/Length;            			

			if(x<0){ x=0; } else if(x>cv.render.x2d-1) x=cv.render.x2d-1;
			if(y<0){ y=0; } else if(y>cv.render.y2d-1) y=cv.render.y2d-1;
			cr_index=x;

			if(cv.render.havarage){
				cr_points_count[y][x]++; // ƒобавл€етс€ нова€ точка
				cr_hcolor[y][x] = (cr_hcolor[y][x]*(cr_points_count[y][x]-1)+pnow[nAngle].h)/cr_points_count[y][x];
            } else cr_hcolor[y][x] = pnow[nAngle].h;  
		}
	 }
 	 crdata=crdata_save;
    LeaveCriticalSection(&lock);
	
}

CalcVolume::CalcVolume():CSLMSocket()
{
  InitializeCriticalSection(&lock);
	for(int i=0;i<SLM_MAX_ANGLE;i++) cr_prender[i]=0;
	cr_plm=NULL;
	cr_hcolor=NULL;
}

CalcVolume::~CalcVolume()
{
	if(cv.flag.render==cv.RENDER_3D&&cv.history.render) 
		for(int i=0;i<SLM_MAX_ANGLE;i++) 
			if(cr_prender[i]) 
				delete [] cr_prender[i];
	if(cv.flag.render==cv.RENDER_2D&&cr_hcolor) {
		for(int j=0;j<cv.render.y2d;j++){
			if(cr_hcolor[j]) delete [] cr_hcolor[j];
			if(cr_points_count[j]) delete [] cr_points_count[j];
		}
		delete [] cr_hcolor;
		delete [] cr_points_count;
	}
	if(cr_plm) delete [] cr_plm;
}

BOOL CalcVolume::InitSLM() {
	if(!Init(cv.slm.ip)) { 
		AfxMessageBox("Error connecting with SLM IP address"); 
		return FALSE;
	}
      
	SetScanSpeed(cv.slm.freq);
	SetAscii(FALSE);
	Set100(cv.slm.freq==1&&cv.slm.step<0.05);
	return TRUE;
}

BOOL CalcVolume::InitRender()
{
	if(cv.flag.render==cv.RENDER_3D&&cv.history.render) { 
        for(int i=0;i<SLM_MAX_ANGLE;i++) {
            cr_prender[i]=new PointDevice[cv.history.render];
            if(cr_prender[i]==NULL) { 
                AfxMessageBox("Error alloc memory for 3d rendering"); 
                return FALSE;
            }
		}
	}  
	if(cv.flag.render==cv.RENDER_2D) {
		cr_hcolor=new double*[cv.render.y2d];
		cr_points_count=new int*[cv.render.y2d];
		for(int j=0;j<cv.render.y2d;j++) {
			cr_hcolor[j]=new double[cv.render.x2d];
			cr_points_count[j]=new int[cv.render.x2d];
            if(cr_hcolor[j]==NULL || cr_points_count[j]==NULL) { 
                AfxMessageBox("Error alloc memory for 2d rendering"); 
                return FALSE;
            }
		}
	}
	return TRUE;
}

BOOL CalcVolume::InitCalc(ConstVolume constvolume)
{
	cv=constvolume;
	cr_plm=new PointLM[cv.history.lm];
	if(cr_plm==NULL) { 
		AfxMessageBox("Error alloc memory for lm history"); 
        return FALSE;
    }
	if(!InitSLM()) return FALSE;
	if(!InitRender()) return FALSE;

	double urad=cv.slm.step*SLM_PI/180.;
    sin_step=sin(urad); cos_step=cos(urad);
    ulb=atan(cv.vol.Height/cv.vol.Wleft)*180./SLM_PI+90.;
    urb=270.-atan(cv.vol.Height/cv.vol.Wright)*180./SLM_PI;        
    crdata.lm.l=cv.vol.Lstart;
    for(int i=0;i<cv.history.lm;i++) { cr_plm[i].l=cv.vol.Lstart; cr_plm[i].t=0; }
	crdata.vol.vp=sin(double(cv.vol.angmin-9000)/100.*SLM_PI/180.)*pow(cv.vol.Wleft,2)*(cv.vol.Lstart-cv.vol.Lend);
	crdata.vol.v=cv.vol.vs-crdata.vol.vp;
	pnow=rl[0];
	pold=rl[1];
	deltaprev=(cv.slm.step<.05) ? 1 : 10;
	return TRUE;
}

void CalcVolume::StartMotor(BOOL bStart) {
    if(bStart) {
	EnterCriticalSection(&lock);
     crdata.vol.vp=sin(double(cv.vol.angmin-9000)/100.*SLM_PI/180.)*pow(cv.vol.Wleft,2)*(cv.vol.Lstart-cv.vol.Lend);
	 crdata.vol.v=cv.vol.vs-crdata.vol.vp; 
	 crdata.count.nve=0;
	 
	 for(int i=0;i<SLM_MAX_ANGLE;i++) {
		rl[1][i].l=rl[0][i].l=cv.vol.Lstart; 
		rl[1][i].r=rl[0][i].r=0;
	 }
	 if(cv.flag.render==cv.RENDER_3D&&cv.history.render)  
      for(int i=0;i<SLM_MAX_ANGLE;i++)
	  		memset(cr_prender[i],0,cv.history.render*sizeof(PointDevice)); 
	 if(cv.flag.render==cv.RENDER_2D)
		 for(int y=0;y<cv.render.y2d;y++){
			memset(cr_hcolor[y],0,cv.render.x2d*sizeof(double));
			memset(cr_points_count[y],0,cv.render.x2d*sizeof(int));
		 }
    LeaveCriticalSection(&lock);
	}
	CSLMSocket::StartMotor(bStart);
}

void CalcVolume::Calc(int u,CriticalData *data) {   
	if(m_fv) {
		fprintf(m_fv,"u=%d r:now:%6.2f(%6.2f)prev:%6.2f(%6.2f)l:now:%6.2f(%6.2f)prev:%6.2f(%6.2f)\n",u,
							pnow[u].r,pnow[u-deltaprev].r,pold[u].r,pold[u-deltaprev].r,
							pnow[u].l,pnow[u-deltaprev].l,pold[u].l,pold[u-deltaprev].l);
	}
    if(pnow[u-deltaprev].l>cv.vol.Lstart||pnow[u].l>cv.vol.Lstart||
       pold[u-deltaprev].l>cv.vol.Lstart||pold[u].l>cv.vol.Lstart)
    {
		//data->vol.vp=0;
        //data->vol.v=cv.vol.vs;
		if(m_fv) fprintf(m_fv,"error lstart: now:%6.2f(%6.2f)prev:%6.2f(%6.2f)\n",
							pnow[u].l,pnow[u-deltaprev].l,pold[u].l,pold[u-deltaprev].l); 
        return;
    }
    if(pnow[u-deltaprev].l<cv.vol.Lend||pnow[u].l<cv.vol.Lend||
       pold[u-deltaprev].l<cv.vol.Lend||pold[u].l<cv.vol.Lend) 
	{
		if(m_fv) fprintf(m_fv,"error lend: now:%6.2f(%6.2f)prev:%6.2f(%6.2f)\n",
							pnow[u].l,pnow[u-deltaprev].l,pold[u].l,pold[u-deltaprev].l); 		
		return;

	}

	double S1,S2,S;
	if(cv.vol.model) {
		double R1=0.5*(pnow[u-deltaprev].r+pold[u-deltaprev].r);
		double R2=0.5*(pnow[u].r+pold[u].r);
		double Rmin,Rmax;
	
		if(R1>R2) { Rmax=R1; Rmin=R2;} else { Rmax=R2; Rmin=R1;}
	
		S1=0.5*Rmin*Rmin*sin_step*cos_step;
		S2=0.5*Rmin*sin_step*(Rmax-Rmin*cos_step);
		S=S1+fabs(S2);
	}
	else {
	    S1=pold[u-deltaprev].r*pold[u].r*sin_step/2.;
		S2=pnow[u-deltaprev].r*pold[u].r*sin_step/2.;
		S=(S1+S2)/2.;
	}

    double Lt=0.5*(pnow[u-deltaprev].l+pnow[u].l);
	double Lt0=0.5*(pold[u-deltaprev].l+pold[u].l);
    if(cv.flag.negative||Lt0>Lt) data->vol.ve=(Lt0-Lt)*S;

	if(!pnow[u-deltaprev].r||!pold[u-deltaprev].r||!pnow[u].r||!pold[u].r) {
		data->count.nbadve++;
		if(m_fv) fprintf(m_fv,"error r: now:%6.2f(%6.2f)prev:%6.2f(%6.2f) badve=%d\n",
				pnow[u].r,pnow[u-deltaprev].r,pold[u].r,pold[u-deltaprev].r,data->count.nbadve);
	}
	//pold[u-deltaprev].r=0;
	
	data->count.nve++;
	data->vol.vp+=data->vol.ve;
	data->vol.v-=data->vol.ve;

	if(m_fv) {
		fprintf(m_fv,"ok: ve=%f,vp=%f\n",u,data->vol.ve,data->vol.vp);
	}
}

long abs( DWORD X ){
  return abs((long)X);
}

double CalcVolume::GetL(DWORD t)
{
    int k=0;
	double ret;
	EnterCriticalSection(&lock);
     long s=abs(cr_plm[0].t-t);
     for(int i=1;i<cv.history.lm;i++) if(abs(cr_plm[i].t-t)<s) { s=abs(cr_plm[i].t-t); k=i; }
	 ret=cr_plm[k].l;
	LeaveCriticalSection(&lock);
    return ret;
}

double CalcVolume::Wall(double r,double u) {
    double rwall;
    if(u<ulb) {
        if(fabs(u-90.)<2.) rwall=cv.vol.Wleft;
        else rwall=cv.vol.Wleft/cos((u-90.)*SLM_PI/180.);
    } else if(u>urb) {
        if(fabs(270.-u)<2.) rwall=cv.vol.Wright;
        else rwall=cv.vol.Wright/cos((270.-u)*SLM_PI/180.);
    } else {
        if(fabs(u-180.)<2.) rwall=cv.vol.Height;
        else rwall=cv.vol.Height/cos(fabs(u-180.)*SLM_PI/180.);
    }
    return r>rwall ? rwall : r;
}
