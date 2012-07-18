#if !defined(AFX_SLMSOCKET_H__37451165_7C0E_41F3_BD95_B15EF5D73EEC__INCLUDED_)
#define AFX_SLMSOCKET_H__37451165_7C0E_41F3_BD95_B15EF5D73EEC__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// SLMSocket.h : header file
//

//#include "Socket_MFC.h"
#include "Socket_Native.h"
#include "slmlib.h"

/////////////////////////////////////////////////////////////////////////////
// CSLMSocket command target

/////////////////////////////////////////////////////////////////////////////

extern FILE* g_fpOverview;

class CSLMSocket {
  BaseSocket<30,CSLMSocket> bs;
  // Operations
public:
  int m_nLastAngle;
  CSLMSocket();
  virtual ~CSLMSocket();
  
  // Initialisation
  BOOL Init(LPCTSTR szAddress);
  BOOL IsValid()				{	return m_bValid;		}	
  void SetScanSpeed(int n)	{	m_nScanSpeed = n;		}
  int GetScanSpeed()			{	return m_nScanSpeed;	}
  void SetAscii(BOOL b)		{	m_bAscii = b;			}
  BOOL GetAscii()				{	return m_bAscii;		}
  
  void DownloadParams();
  int GetTries()					{	return m_nTries;	}
  
  // Heartbeat
  void Tick(DWORD);
  
  // Status
  BOOL IsRejected()				{	return m_bRejected;					}
  BOOL IsScanning()				{	return m_bScanning;					}
  
  int GetCurrentAngle()			{	return m_nCurrAngle;				}
  
  SLMstate GetState()				{	return m_stateUser;					}
  
  // Commands and associated
  void ShowPointer(BOOL bVisible);
  BOOL IsPointerVisible()			{	return m_bPointerVisible;			}
  void UpdateCurrentAngle();
  
  // Logging
  void LogToFile(BOOL bLogToFile);
  BOOL IsLoggingToFile()	{	return m_fp != NULL;	}
  
  // 100th degree handling
  BOOL Get100()			{	return m_b100;			}
  void Set100(BOOL b)		{	m_b100 = b;				}
  BOOL In100Mode()		{	return m_b100 && (m_nScanSpeed == 1);	}
  
  void RecordPoints(BOOL b)	{	m_bRecordPoints = b;	}
  void ConnectToDevice();
  void CloseDown();
protected:
  virtual void StartMotor(BOOL bStart);	
  virtual void OnRejectedChange(BOOL bRejected) {}
  virtual void OnVersionDetect(LPCTSTR szVersion, LPCTSTR szVerDate) {}
  virtual void OnStateChange(SLMstate state) {}
  virtual void OnWrap()	{}
  virtual void OnPointDetected(int nAngle, int nRange, int nSignal, DWORD dwTicks, int npacket)=0;
  virtual void OnCurrentAngleUpdated(int nCurrentAngle) {}
  virtual void OnScanningStopped() {}
  
  void EnableFiring(BOOL bEnable);
  
  void Write(char chCommand, char* szExtra = NULL);
  void Write(char chCommand, int nParam);
    
  void ReadOK()		{	m_dwWriteTC = 0;	TRACE("ReadOK\n");	}
  
public:
  // Attributes
  enum slm_state {
    state_connecting = 0,
      state_id = 1,
      state_ver = 2,
      state_replieson = 3,
      state_format = 4,
      state_speed = 5,
      state_current_angle = 6,
      state_ready = 7,
      state_motor = 8,
      state_pointer = 9,
      state_firing = 10,
      state_load_eye_safety_timer = 11,
      state_set_format_as_tenths = 12,
  } private_state;
  
  void setState( slm_state _state ){ 
    private_state = _state;
    TRACE( "state = %s\n", m_szStates[private_state] );
  }
  slm_state getState(){
    return private_state;
  }	

protected:  
  SLMstate m_stateUser;
  
  char m_szVersion[32];
  char m_szVerDate[32];
  int m_nVersionMajor;
  int m_nVersionMinor;
  
  static char* m_szStates[];
  DWORD m_dwWriteTC;
  BOOL m_bScanning;
  BOOL m_bPointerVisible;
  BOOL m_bFiring;
  
  BOOL m_b100;
  
  int m_nLastAngleMin;
  int m_nLastAngleMax;
  int m_nCurrAngle;
  
  BOOL m_bRejected;
  int m_nTries;
  FILE* m_fp;
  FILE* m_fv;
  BOOL m_bOneShot;
  
  BOOL m_bRecordPoints;	// So you can disable recording when laser powering down
  DWORD m_dwStart;		// Time scan was started for distance offset
  
  // Overrides
  int m_nScanSpeed;
  BOOL m_bAscii;
  
  BOOL m_bValid;
public:
  void OnReceive();
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_SLMSOCKET_H__37451165_7C0E_41F3_BD95_B15EF5D73EEC__INCLUDED_)
