// volume.cpp : Defines the class behaviors for the application.

#include "stdafx.h"
#include "volume.h"
#include "volumeDlg.h"
#include "GSettings.h"
#include "check.h"
#include <math.h>
#include <conio.h>
#include <string>
#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CVolumeApp

BEGIN_MESSAGE_MAP(CVolumeApp, CWinApp)
    //{{AFX_MSG_MAP(CVolumeApp)
    // NOTE - the ClassWizard will add and remove mapping macros here.
    //    DO NOT EDIT what you see in these blocks of generated code!
    //}}AFX_MSG
    ON_COMMAND(ID_HELP, CWinApp::OnHelp)
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CVolumeApp construction

CVolumeApp::CVolumeApp()
{
    for(int i=0;i<RS_ALL;i++) rs[i].hdev=0;
}

CVolumeApp::~CVolumeApp()
{
    for(int i=0;i<RS_ALL;i++) if(rs[i].hdev) closeRS(rs[i].hdev);
}

/////////////////////////////////////////////////////////////////////////////
// The one and only CVolumeApp object

CVolumeApp theApp;

CEvent e_exit0,e_exit1,e_exit2,e_exit3;

/////////////////////////////////////////////////////////////////////////////
// CVolumeApp initialization

void CVolumeApp::closeRS(HANDLE h) {		
    if(h!=INVALID_HANDLE_VALUE) {
        SetCommMask(h,0);
        EscapeCommFunction(h,CLRDTR) ;
        PurgeComm(h,PURGE_TXABORT|PURGE_RXABORT|PURGE_TXCLEAR|PURGE_RXCLEAR);
        if(CloseHandle (h)==FALSE)
            AfxMessageBox("Error closing virtual Com port",MB_OK|MB_ICONWARNING);
    }
}

int  CVolumeApp::initRS(SCOM *scom,char *namefile) {
    // Получаем имя (с путём) файла из которого читается дистанция от LM
    char *buf = new char[MAX_PATH]; 
    GetCurrentDirectory(MAX_PATH,buf);
    TRACE( "Get LM points from file \"%s\\%s\"\n", buf, namefile );
    delete []buf;

    scom->hdev=INVALID_HANDLE_VALUE;
    if((scom->hdev=CreateFile(namefile,GENERIC_READ,FILE_SHARE_READ|FILE_SHARE_WRITE,NULL,OPEN_EXISTING,
        FILE_ATTRIBUTE_NORMAL|FILE_FLAG_OVERLAPPED,NULL))==INVALID_HANDLE_VALUE) {	
            std::string t = std::string("Error opening imitfile ComPort defined 'NameFileImitLM' = \"") + namefile + "\"";
            AfxMessageBox(t.c_str(),MB_OK|MB_ICONWARNING);
            return 0;
    }
    return 1;
}

int  CVolumeApp::initRS(SCOM *scom,int nport) {		  
    CString acp;
    acp.Format("COM%d",nport);
    scom->hdev=INVALID_HANDLE_VALUE;
    if((scom->hdev=CreateFile(acp,GENERIC_READ|GENERIC_WRITE,0,NULL,OPEN_EXISTING,
        FILE_ATTRIBUTE_NORMAL|FILE_FLAG_OVERLAPPED,NULL))==INVALID_HANDLE_VALUE) {	
            AfxMessageBox("Error opening virtual Com port",MB_OK|MB_ICONWARNING);
            return 0;
    }

    SetCommMask(scom->hdev,EV_RXCHAR);
    SetupComm(scom->hdev,4096,4096); 

    PurgeComm(scom->hdev,PURGE_TXABORT|PURGE_RXABORT|PURGE_TXCLEAR|PURGE_RXCLEAR);

    COMMTIMEOUTS CommTimeOuts;
    CommTimeOuts.ReadIntervalTimeout=0xFFFFFFFF;
    CommTimeOuts.ReadTotalTimeoutMultiplier=0;
    CommTimeOuts.ReadTotalTimeoutConstant=1000;
    CommTimeOuts.WriteTotalTimeoutMultiplier=0;
    CommTimeOuts.WriteTotalTimeoutConstant=1000;
    SetCommTimeouts(scom->hdev,&CommTimeOuts);		  

    DCB dcb;
    dcb.DCBlength=sizeof(DCB);
    GetCommState(scom->hdev,&dcb);

    dcb.BaudRate=CBR_57600;
    dcb.ByteSize=8;
    dcb.Parity=0;
    dcb.StopBits=ONESTOPBIT;

    //DTR/DSR
    dcb.fOutxDsrFlow=0; dcb.fDtrControl=DTR_CONTROL_ENABLE;
    //dcb.fOutxDsrFlow=1; dcb.fDtrControl=DTR_CONTROL_HANDSHAKE;

    //RTS/CTS
    dcb.fOutxCtsFlow=0; dcb.fRtsControl=RTS_CONTROL_ENABLE;
    //dcb.fOutxCtsFlow=1; dcb.fRtsControl=RTS_CONTROL_HANDSHAKE;

    //XON/XOFF
    //const char ASCII_XON=0x11,ASCII_XOFF=0x13; 
    dcb.fInX=dcb.fOutX=0;
    //dcb.fInX=dcb.fOutX=1;

    //dcb.fTXContinueOnXoff=TRUE;
    //dcb.XonChar=ASCII_XON ;
    //dcb.XoffChar = ASCII_XOFF ;
    //dcb.XonLim = 100 ;
    //dcb.XoffLim = 100 ;

    dcb.fBinary=TRUE;
    dcb.fParity=(dcb.Parity>0)?TRUE:FALSE;

    if(!SetCommState(scom->hdev,&dcb)) { 
        AfxMessageBox("Error configuring virtual Com port",MB_OK|MB_ICONWARNING);
        return 0;
    }
    EscapeCommFunction(scom->hdev,SETDTR);
    return 1;
}

// #define TRACE_IM

namespace IM_Data {
    bool found_EOF = false;
    long GetRS_calls = 0;  
#ifdef TRACE_IM
    long receiveCnt = 0;  
    long receiveTotalCnt = 0;  
    long startTime = GetCurrentTime();
    long lastTraceTime = GetCurrentTime();
#endif
    void onReceive( double distance ){ 
#ifdef TRACE_IM
        receiveCnt++; receiveTotalCnt++;
        long curTime = GetCurrentTime();
        if(curTime - lastTraceTime > 2000){
            double average_speed = receiveCnt*1000.0 / (curTime-lastTraceTime);
            TRACE("Receive LM #%ld = %lf speed = %lf call GetRS speed = %lf\n", 
                receiveTotalCnt, distance, average_speed, GetRS_calls*1000.0 / (curTime-lastTraceTime) );
            lastTraceTime = curTime;
            receiveCnt = 0; GetRS_calls = 0;
        }
#endif
    }
}

int CVolumeApp::GetRS(RS_TYPE type,double *z) {
    IM_Data::GetRS_calls++;
    const int lenstr=30;
    static char mbyte[RS_ALL][lenstr];
    const int nread=1;
    //for java imitator lm genereted 0x0a only
    //key 'enter' is 0xa for unix OS but 0xa and 0xd for windows OS
    char chend=0x0d;
    if(cv.flag.imitmode) chend=0x0a;		
    DWORD charsRead,dwErrorFlags;
    COMSTAT ComStat;
    static int count[RS_ALL];
    char *ptrstr;
    if(rs[type].hdev!=INVALID_HANDLE_VALUE) {
        ClearCommError(rs[type].hdev,&dwErrorFlags,&ComStat);
        if(ComStat.cbInQue>0) {
            if(!ReadFile(rs[type].hdev,&mbyte[type][count[type]],nread,&charsRead,&rs[type].olRead)) {
                switch (GetLastError()) { 
                    case ERROR_HANDLE_EOF: 
                        IM_Data::found_EOF = true;
                        //AfxMessageBox("Error reading Com port: end of file",MB_OK|MB_ICONWARNING);
                        //e_exit2.SetEvent();
                        //return -1;
                        return 0;
                    case ERROR_IO_PENDING: 
                        /*if(!GetOverlappedResult(rs[RS_LM].hdev,&rs[RS_LM].olRead,&charsRead,TRUE)) {
                        AfxMessageBox("Error reading Com port: GetOverlappedResult",MB_OK|MB_ICONWARNING);
                        e_exit2.SetEvent();
                        return -1;
                        }*/
                        return 0;
                    default:
                        AfxMessageBox("Error reading Com port",MB_OK|MB_ICONWARNING);
                        e_exit2.SetEvent();
                        return -1;
                }  
            }
            if(cv.flag.imitmode) rs[RS_LM].olRead.Offset++;
            if(mbyte[type][count[type]]==chend){
                count[type]=0;
            } else { 
                count[type]++; 
                if(count[type]==lenstr) count[type]=0; 
            }
            if(!count[type]) {
                if(type==RS_BATT){ ptrstr=strchr(mbyte[type],'=')+1; } else ptrstr=mbyte[type];
                *z=atof(ptrstr);
                IM_Data::onReceive(*z);
                memset(mbyte[type],0,lenstr);
                return 1;
            }
        }
    }
    return 0;
}

void CVolumeApp::PutRS(RS_TYPE type)
{
    const int lenstr=2;
    const char mbatt[lenstr]={'y',0x0d};
    DWORD charsRead;
    if(rs[type].hdev!=INVALID_HANDLE_VALUE)
        WriteFile(rs[type].hdev,mbatt,lenstr,&charsRead,&rs[type].olWrite); 
}

UINT CVolumeApp::DeviceThread(LPVOID pParams)
{
    CRITICAL_SECTION lock;
    InitializeCriticalSection(&lock);
    CriticalCount crcount,crcount_save;
    DWORD tresult,tnow,tslm_control,tbatt;
    tresult=GetTickCount();
    tbatt=tslm_control=tresult;

    for(int i=0;i<RS_ALL;i++) {
        PurgeComm(rs[i].hdev,PURGE_TXABORT|PURGE_RXABORT|PURGE_TXCLEAR|PURGE_RXCLEAR);
        EscapeCommFunction(rs[i].hdev,CLRRTS);
        EscapeCommFunction(rs[i].hdev,SETRTS);
        rs[i].olRead.hEvent=CreateEvent(NULL,TRUE,FALSE,NULL);
    }

    double lold=0; 
    int lmcount=0;
    const double rmin=(double)cv.vol.rmin/100.;
    const double rmax=(double)cv.vol.rmax/100.;

    while(1)
    { 
        DWORD WaitReturn=WaitForSingleObject(e_exit0.m_hObject,1);
        if(WaitReturn==WAIT_OBJECT_0) {
            TRACE("e_exit2.SetEvent()\n");
            e_exit2.SetEvent();
            return 0; // !!!
            // break;
        }    

        double s,z[RS_ALL];	int j,ret;
        IM_Data::found_EOF = false;
        while(!IM_Data::found_EOF){
            for(int i=0;i<RS_ALL;i++) {
                ret=GetRS((RS_TYPE)i,&z[i]);
                switch(ret) {
            case 0: break;
            case 1: 
                if(i==RS_LM) {
                    if(cv.flag.rbound) {
                        if(z[i]<rmin||z[i]>rmax) z[i]=lold;
                        else lold=z[i];
                    }
                    EnterCriticalSection(&lock);
                    calc.cr_plm[lmcount].l=z[i];
                    calc.cr_plm[lmcount].t=GetTickCount();
                    calc.crdata.count.nl++;

                    if(!cv.flag.lm) calc.crdata.lm.l=z[RS_LM];
                    else {
                        s=0;
                        for(j=0;i<cv.history.lm;i++) s+=calc.cr_plm[i].l;
                        calc.crdata.lm.l=s/(double)cv.history.lm;
                    }
                    LeaveCriticalSection(&lock);
                    lmcount++; 
                    if(lmcount>=cv.history.lm) lmcount=0;
                }
                else {
                    EnterCriticalSection(&lock);
                    calc.crdata.lm.batt=z[i];
                    calc.crdata.count.nbatt++;
                    LeaveCriticalSection(&lock);
                }
                break;
            default: return 1;
                } 
            }
        }

        tnow=GetTickCount();
        if((tnow-tslm_control)>cv.times.slm_control) { 
            calc.Tick(cv.times.slm_answer); 
            tslm_control=tnow; 
        }
        if((tnow-tresult)>cv.times.result) {
            EnterCriticalSection(&lock);
            crcount_save=calc.crdata.count;
            LeaveCriticalSection(&lock);
            if(crcount.nu!=crcount_save.nu||crcount.nl!=crcount_save.nl||crcount.nbatt!=crcount_save.nbatt) {
                if(theApp.m_pMainWnd) theApp.m_pMainWnd->Invalidate(FALSE); 
                crcount=crcount_save;
            }
            tresult=tnow;
        }
        if((tnow-tbatt)>cv.times.batt) {
            PutRS(RS_BATT);
            tbatt=tnow;
        }
    } 
    return 0;
}

void CVolumeApp::LoadIniFile(char *name) {
    TRACE("Load ini-file \"%s\"\n",name);
    CGSettings gs(name);
    strcpy(cv.slm.ip,gs.GetString("slm","ip","127.0.0.1"));
    cv.slm.step=gs.GetDouble("slm","step",0.1);
    cv.slm.freq=gs.GetInt("slm","freq",10);

    cv.lm.nComDist=gs.GetInt("lm","nComDist",0);
    cv.lm.nComBatt=gs.GetInt("lm","nComBatt",0);
    strcpy(cv.lm.NameFileImitLM,gs.GetString("lm","NameFileImitLM",""));

    cv.vol.Lstart=gs.GetDouble("volume","Lstart",0.0);
    cv.vol.Lend=gs.GetDouble("volume","Lend",0.0);
    cv.vol.Height=gs.GetDouble("volume","Height",0.0);
    cv.vol.Wleft=gs.GetDouble("volume","Wleft",0.0);
    cv.vol.Wright=gs.GetDouble("volume","Wright",0.0);
    cv.vol.model=gs.GetInt("volume","model",0);
    cv.vol.angmin=gs.GetInt("volume","angmin",9000);
    cv.vol.angmax=gs.GetInt("volume","angmax",27000);
    if(cv.vol.angmax<=cv.vol.angmin||cv.vol.angmin<9000||cv.vol.angmax>27000) {
        cv.vol.angmin=9000;
        cv.vol.angmax=27000;
    }
    cv.vol.rmin=gs.GetInt("volume","rmin",10);
    cv.vol.rmax=gs.GetInt("volume","rmax",15000);
    if(cv.vol.rmax<=cv.vol.rmin) {
        cv.vol.rmin=10;
        cv.vol.rmax=15000;
    }

    cv.history.render=gs.GetInt("history","render",10);
    cv.history.lm=gs.GetInt("history","lm",50);

    cv.times.render=(DWORD)(1000.*gs.GetDouble("times","render",1.0));
    cv.times.result=(DWORD)(1000.*gs.GetDouble("times","result",1.0));
    cv.times.batt=(DWORD)(1000.*gs.GetDouble("times","batt",5.0));
    cv.times.slm_answer=(DWORD)(1000.*gs.GetDouble("times","slm_answer",3.0));
    cv.times.slm_control=(DWORD)(1000.*gs.GetDouble("times","slm_control",0.25));

    cv.flag.imitmode=gs.GetBool("flags","imitmode",0);
    cv.flag.virt=gs.GetBool("flags","virt",TRUE);
    cv.flag.lm=gs.GetBool("flags","lm",TRUE);
    cv.flag.negative=gs.GetBool("flags","negative",TRUE);
    cv.flag.rbound=gs.GetBool("flags","rbound",TRUE);
    cv.flag.render=gs.GetInt("flags","render",cv.RENDER_OFF);

    cv.render.x2d=gs.GetInt("render","x2d",0);
    cv.render.y2d=gs.GetInt("render","y2d",0);
    cv.render.buf2d=gs.GetBool("render","buf2d",TRUE);
    cv.render.havarage=gs.GetBool("render","havarage",TRUE);
    cv.render.part=gs.GetBool("render","part",FALSE);

    cv.render.hcolor=0xFF;
    cv.vol.vs=cv.vol.Height*(cv.vol.Wleft+cv.vol.Wright)*fabs(cv.vol.Lstart-cv.vol.Lend);
}

BOOL CVolumeApp::InitLM() {
    if(!cv.flag.imitmode) {
        if(!initRS(&rs[RS_LM],cv.lm.nComDist)) return FALSE;
        if(!initRS(&rs[RS_BATT],cv.lm.nComBatt)) return FALSE;
    } else if(!initRS(&rs[RS_LM],cv.lm.NameFileImitLM)) return FALSE;
    return TRUE;
}

BOOL CVolumeApp::InitInstance() {

    char secretstr[]={'f','2','l','i','n','e','.','.','\0'};
    char filename[]="volume.key";
    switch(checkKey(filename,secretstr)) {
        case 0:
            AfxMessageBox("Volumetric calculation program works in a demo mode\n"
                "Send your 'volume.key' to the developer program for reception of other efficient key");
            cv.demo=1;
            break;
        case 1:
            cv.demo=0;
            break;
        default:
            CString mes;
            mes.Format("Not open file '%s'",filename);
            AfxMessageBox(mes);
            return FALSE;
    }

    if (!AfxSocketInit()) {
        AfxMessageBox(IDP_SOCKETS_INIT_FAILED);
        return FALSE;
    }

    AfxEnableControlContainer();

#ifdef _AFXDLL
    Enable3dControls();	
#else
    Enable3dControlsStatic();
#endif

    LoadStdProfileSettings(0);

    LoadIniFile("volume.ini");

    if(!InitLM()) return 1;
    if(!calc.InitCalc(cv)) return 1;

    dthread=AfxBeginThread ((AFX_THREADPROC)DeviceThread,0);
    dthread->SetThreadPriority(THREAD_PRIORITY_LOWEST);

    CVolumeDlg dlg(&cv,&calc);
    m_pMainWnd = &dlg;
    dlg.DoModal();

    e_exit0.SetEvent();
    e_exit1.SetEvent();
    HANDLE han[2];
    han[0]=e_exit2.m_hObject;
    han[1]=e_exit3.m_hObject;
    DWORD WaitReturn=WaitForMultipleObjects(2,han,true,INFINITE);

    return FALSE;
}

SCOM CVolumeApp::rs[RS_ALL];
CalcVolume CVolumeApp::calc;
ConstVolume CVolumeApp::cv;
