// SLMSocket.cpp : implementation file

#include "stdafx.h"
#include <math.h>
#include "SLMSocket.h"
#include <FSTREAM>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CSLMSocket

FILE* g_fpOverview = NULL;

CSLMSocket::CSLMSocket() {
	m_bValid = FALSE;
	m_dwWriteTC = 0;
	m_bScanning = FALSE;
	m_bPointerVisible = FALSE;
	m_bFiring = FALSE;
	m_nScanSpeed = 1;	// 1 to 10. In v1.0, this maps to 1 = 18500, 10 = 50000
	m_bRejected = FALSE;
	m_nCurrAngle = -1;
	m_nTries = 0;
	m_nLastAngleMax = 0;
	m_fp = NULL;
	m_fv = NULL;
	g_fpOverview = NULL;
	m_bOneShot = FALSE;
	m_stateUser = (SLMstate)-1;
	m_b100 = FALSE;
	m_bRecordPoints = TRUE;
  m_nLastAngle = -1;
}

CSLMSocket::~CSLMSocket() {
	CloseDown();
	if (m_fp)
		fclose(m_fp);
	if (m_fv)
		fclose(m_fv);
	if (g_fpOverview)
		fclose(g_fpOverview);
}

void CSLMSocket::CloseDown() {
	if (m_bFiring)
		EnableFiring(FALSE);

	if (m_bPointerVisible)
		ShowPointer(FALSE);

	if (m_bScanning)
		StartMotor(FALSE);

  bs.Close();
}

// Do not edit the following lines, which are needed by ClassWizard.
#if 0
BEGIN_MESSAGE_MAP(CSLMSocket, CAsyncSocket)
	//{{AFX_MSG_MAP(CSLMSocket)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()
#endif	// 0

/////////////////////////////////////////////////////////////////////////////
// CSLMSocket member functions

BOOL CSLMSocket::Init(LPCTSTR szAddress) {
	CloseDown();

  if(!bs.Init(this,szAddress)) return FALSE;

	setState(state_connecting);

	m_bValid = TRUE;
	return TRUE;
}

void CSLMSocket::ConnectToDevice() {
	setState( state_replieson );

	Write(CMD_DO_REPLIES);    
	if (m_stateUser != SLMstate_searching)
	{
		m_stateUser = SLMstate_searching;
		OnStateChange(SLMstate_searching);
	}
	m_nTries++;
}

/////////////////////////////////////////////////////////////////////////////

void CSLMSocket::Write(char chCommand, char* szExtra) {
	char buff[16];
	char* szScan = buff;

	*(szScan++) = chCommand;

	if (szExtra) {
		while (*szExtra)
			*(szScan++) = *(szExtra++);
	}

	*(szScan++) = CMD_NEWLINE;
	*szScan = '\0';	// for debug

	bs.Send(buff);
  TRACE("AFTER SEND!!\n");
}

void CSLMSocket::Write(char chCommand, int nParam) {
	char buff[16];
	sprintf(buff, "%c%05u\x0a", chCommand, nParam);

	bs.Send(buff);
}

void CSLMSocket::LogToFile(BOOL bLogToFile) {
	if (!bLogToFile) {
		if (m_fp)
			fclose(m_fp);
		if (m_fv)
			fclose(m_fv);
		if (g_fpOverview)
			fclose(g_fpOverview);

		m_fp = NULL;
		m_fv = NULL;
		g_fpOverview = NULL;
	} else {
		m_fp = fopen("slm.txt", "w");
		//m_fv = fopen("calc.txt", "w"); //this must be enabled only lonely - without anyone log else
		g_fpOverview = fopen("slmoverview.txt", "w");
	}
}

char* CSLMSocket::m_szStates[] = {
	"state_connecting",
	"state_id",
	"state_ver",
	"state_replieson",
	"state_format",
	"state_speed",
	"state_current_angle",
	"state_ready",
	"state_start",
	"state_pointer",
	"state_firing",
	"state_load_eye_safety_timer",
	"state_set_format_as_tenths"
};

void CSLMSocket::DownloadParams() {
	setState(state_speed);
	if (!strcmp(m_szVersion, "v1.0"))
		Write(CMD_SPEED, 15000 + m_nScanSpeed * 3500);
	else
		Write(CMD_SET_SPEED, m_nScanSpeed);
}

void CSLMSocket::OnReceive() {

//  TRACE("OnReceive %ld\n",GetTickCount());  

	char buff[4097];
	SOCKADDR sa;
	int nLen = sizeof(sa);
	int nIndividualPacketLen = m_bAscii ? 17 : 8;
	if ((m_nVersionMajor >= 3) && m_bAscii)
		nIndividualPacketLen = 18;

	int nBytes = bs.ReceiveFrom(buff, 4096, &sa, &nLen);
	if ((nBytes > 0) && (buff[0] != '$'))
	{
        int i;
		for (i = 0; i < nBytes; i++)
		{
			if (buff[i] == CMD_NEWLINE)
			{
				buff[i] = '\0';
				if (i + 1 != nBytes)
					TRACE("Got message of %d but terminated it at %d\n", nBytes, i + 1);
				break;
			}
		}

		if (i == nBytes)
			buff[i] = '\0';

		if (i < 1024)
			TRACE("Reading %s (%s)\n", buff, m_szStates[getState()]);
		else
			TRACE("Got %d bytes\n", nBytes);

		slm_state stateOld = getState();

		switch (getState())	{
// Init stuff
          case state_replieson:
			if (!strcmp(buff, "REPLIES ON") || !strcmp(buff, "*REPLON"))	// no idea what mode we're in yet!
			{
				m_nTries = 0;
				ReadOK();

				if (m_bRejected)
				{
					m_bRejected = FALSE;
					OnRejectedChange(m_bRejected);
				}

				if (m_stateUser != SLMstate_initialising)
				{
					m_stateUser = SLMstate_initialising;
					OnStateChange(SLMstate_initialising);
				}

				setState(state_format);
				if (m_bAscii)
					Write(CMD_ASCII);
				else
					Write(CMD_BINARY);
			}
			break;

		case state_format:
			if (!strcmp(buff, m_bAscii ? "ASCII OUTPUT ENABLED" : "*BINARY"))
			{
				ReadOK();
				setState(state_id);
				Write(CMD_DEVICE_IDENTIFY);
			}
			break;

		case state_id:	// This should be the unit ID
			if (!strcmp(buff, m_bAscii ? "MDA072B" : "*MDA072"))
			{
				ReadOK();
				setState(state_ver);
				Write(CMD_VER_DATE);
			}
			break;

		case state_ver:
			if ((strlen(buff) < 32) && (sscanf(buff, "%s - %s", m_szVersion, m_szVerDate) == 2))
			{
				sscanf(m_szVersion, "v%d.%d", &m_nVersionMajor, &m_nVersionMinor);
				OnVersionDetect(m_szVersion, m_szVerDate);
				ReadOK();
				setState(state_current_angle);
				Write(CMD_CURRENT_ANGLE);
			}
			break;

		case state_current_angle:
			{
				int nMatches = m_bAscii ?
					sscanf(buff, "Angle- %d", &m_nCurrAngle) :
					sscanf(buff, "*A%d", &m_nCurrAngle);

				if (nMatches == 1)
				{
					if (m_nVersionMajor < 3)
						m_nCurrAngle *= 10;

					ReadOK();

					if (m_bOneShot)
					{
						m_bOneShot = FALSE;
						setState(state_ready);
						OnCurrentAngleUpdated(m_nCurrAngle);
						break;
					}

					DownloadParams();
				}
			}
			break;

		case state_speed:
			{
				int nDutyCycle;
				int nMatches = m_bAscii ?
					sscanf(buff, "Duty cycle set to %d", &nDutyCycle) :
					sscanf(buff, "*D%d", &nDutyCycle);

				if (nMatches == 1)
				{
					ReadOK();
					setState(state_ready);
					if (m_stateUser != SLMstate_idle)
					{
						m_stateUser = SLMstate_idle;
						OnStateChange(SLMstate_idle);
					}
				}
			}
			break;

// runtime stuff
		case state_load_eye_safety_timer:
			{
				int nES;
				if (sscanf(buff, m_bAscii ? "eye safety set to %d" : "*F%d", &nES) == 1)
				{
					ReadOK();
					setState(state_set_format_as_tenths);
					Write(CMD_CONFIG, In100Mode() ? "H" : "T");
				}
			}
			break;

		case state_set_format_as_tenths:
			if (In100Mode())
			{
				if (!strcmp(buff, m_bAscii ? "1/100th selected" : "*Hundr"))
				{
					ReadOK();
					setState(state_motor);
					Write(CMD_START_MOTOR);
				}
			}
			else
			{
				if (!strcmp(buff, m_bAscii ? "1/10th selected" : "*Tenth"))
				{
					ReadOK();
					setState(state_motor);
					Write(CMD_START_MOTOR);
				}
			}
			break;

		case state_motor:
			if (!strcmp(buff, m_bAscii ? "Scan started" : "*STARTD"))
			{
				ReadOK();
				m_bScanning = TRUE;
				EnableFiring(TRUE);
				if (m_stateUser != SLMstate_scanning)
				{
					m_stateUser = SLMstate_scanning;
					OnStateChange(SLMstate_scanning);
					m_dwStart = GetTickCount();
				}
			}
			else if (!strcmp(buff, m_bAscii ? "Scan stopped" : "*STOPED"))
			{
				ReadOK();
				m_bScanning = FALSE;
				EnableFiring(FALSE);
				if (m_stateUser != SLMstate_idle)
				{
					m_stateUser = SLMstate_idle;
					OnStateChange(SLMstate_idle);
				}
			}
			break;

		case state_pointer:
			if (!strcmp(buff, m_bAscii ? "Red Dot on" : "*DotOn "))
			{
				m_bPointerVisible = TRUE;
				ReadOK();
				setState(state_ready);
			}
			else if (!strcmp(buff, m_bAscii ? "Red Dot off" : "*DotOff"))
			{
				m_bPointerVisible = FALSE;
				ReadOK();
				setState(state_ready);
			}
			break;

		case state_firing:
			if (!strcmp(buff, m_bAscii ? "Laser on" : "*Lasron"))
			{
				m_bFiring = TRUE;
				ReadOK();
				setState(state_ready);
			}
			else if (!strcmp(buff, m_bAscii ? "Laser off" : "*Lasoff"))
			{
				m_bFiring = FALSE;
				ReadOK();
				OnScanningStopped();
				setState(state_current_angle);
				Write(CMD_CURRENT_ANGLE);
			}
			break;
		}
	}
	else if (((nBytes % nIndividualPacketLen) == 0) && (buff[0] == '$'))
	{
		// Parse the buffer;
		int nPackets = 0;
		char* sz = buff;
		int nRange = 0;
		int nSignal = 0;
		int nAngle;
		int nLastAngleForWrap = m_nLastAngleMax;

		int nDebugFirst = -1;
		int nDebugLast = -1;
		int nDebugCount = 0;
		int nDebugGap = 0;
		int nDebugZeroCount = 0;
		
		DWORD dwTicks = GetTickCount();

		static int npacket;
		npacket++;

		// Debug: отслеживанием потерю пакетов
		bool firstPoint = true;
		static int nSignalPrev = -1;

		while ((*sz == '$') && (nBytes > 0))
		{
			if (m_bAscii)
			{
				// Record = $rrrrr,ssss,aaaa<lf>	less than v3
				// record = $rrrrr,ssss,aaaaa<lf>	v3 and greater 18 chars
				nRange = atoi(sz + 1);
				nSignal = atoi(sz + 7);
				nAngle = atoi(sz + 12);
			}
			else
			{
        nRange = ((unsigned char)sz[1] << 8) + (unsigned char)sz[2];
				nSignal = ((unsigned char)sz[3] << 8) + (unsigned char)sz[4];
				nAngle = ((unsigned char)sz[5] << 8) + (unsigned char)sz[6];

				// Debug: отслеживанием потерю пакетов
				if(firstPoint){
          // TRACE("%ld\n",nSignal);
					if(nSignalPrev!=-1 && nSignal!=(nSignalPrev+1)){
						TRACE("nSignal = %d nSignalPrev = %d  = %d\n",nSignal,nSignalPrev,nSignal-nSignalPrev);
					}
					nSignalPrev = nSignal;
					firstPoint = false;
				}
			}

			if (nDebugFirst == -1)
				nDebugFirst = nAngle;
			else
			{
				int nGap = nAngle - nDebugLast;
				if (nGap > nDebugGap)
					nDebugGap = nGap;
			}

			nDebugLast = nAngle;
			nDebugCount++;

			if (nRange == SLM_MAX_RANGE)
				nRange = 0;	// 0 and 65535 are "no-hit"

			if (!nRange)
				nDebugZeroCount++;

			if (m_nVersionMajor < 3)
				nAngle *= 10;

#ifdef _DEBUG
			// Older units sometimes went backwards. Patch this in the debug
			if (nAngle < m_nLastAngleMax)
			{
				if (abs(nAngle - m_nLastAngleMax) <= 200)
					nAngle = m_nLastAngleMax;
			}
#endif

			if ((nAngle >= 0) && (nAngle < SLM_MAX_ANGLE))
			{
				if (m_fp)
					fprintf(m_fp, "%0d,%0d,%0d\n", nRange, nAngle, nSignal);

				if (nPackets == 0)
					m_nLastAngleMin = nAngle;

				if (nAngle < nLastAngleForWrap)
				{
					OnWrap();
					nLastAngleForWrap = nAngle;
				}

				if (m_bRecordPoints)
					OnPointDetected(nAngle, nRange, nSignal, dwTicks, npacket);

				nPackets++;
				m_nLastAngleMax = nAngle;		// Last angle in the message
			}

			sz += nIndividualPacketLen;
			nBytes -= nIndividualPacketLen;
		}

		if (g_fpOverview)
			fprintf(g_fpOverview, "Finished processing packet %05d to %05d. %05d points. Max gap = %05d. Zero count = %05d\n", nDebugFirst, nDebugLast, nDebugCount, nDebugGap, nDebugZeroCount);

//		TRACE("Got %d to %d - %d points - max gap = %d - zero count = %d\n", nDebugFirst, nDebugLast, nDebugCount, nDebugGap, nDebugZeroCount);
	}
	else if (nBytes < 0)
	{
		DWORD dwError = GetLastError();
		if (dwError == WSAECONNRESET)
		{
			m_bRejected = TRUE;
			OnRejectedChange(m_bRejected);
		}
		else
			TRACE("Got an Error %d\n", GetLastError());
	}
	else
	{
		TRACE("Unknown packet\n");
	}	
}

/////////////////////////////////////////////////////////////////////////////

void CSLMSocket::StartMotor(BOOL bStart)
{
	TRACE( "StartMotor %s\n",m_szStates[getState()] );

	m_nCurrAngle = -1;
	if (bStart && (m_nVersionMajor >= 3))
	{
		setState(state_load_eye_safety_timer);
		Write(CMD_CONFIG, "E1543");
	}
	else
	{
		setState(state_motor);
		Write(bStart ? CMD_START_MOTOR : CMD_STOP_MOTOR);
	}
}

/////////////////////////////////////////////////////////////////////////////

void CSLMSocket::Tick(DWORD dt)
{
//  TRACE("Tick\n");
	if (getState() == state_connecting)
		ConnectToDevice();
	else if (m_dwWriteTC)
	{
		if (GetTickCount() - m_dwWriteTC > dt)
		{
			TRACE("Timeout %d - %d > %d\n", GetTickCount(), m_dwWriteTC, dt);
			if (getState() == state_ver)
			{
				// no reply to a ver probably means a really old SLM.
				strcpy(m_szVersion, "v1.0");
				strcpy(m_szVerDate, "-");
				sscanf(m_szVersion, "v%d.%d", &m_nVersionMajor, &m_nVersionMinor);
				OnVersionDetect(m_szVersion, m_szVerDate);
				ReadOK();
				setState(state_current_angle);
				Write(CMD_CURRENT_ANGLE);
			}
			else
			{
				setState(state_connecting);
				m_bValid = TRUE;
			}
		}
	}
}

/////////////////////////////////////////////////////////////////////////////

void CSLMSocket::ShowPointer(BOOL bVisible) {
	setState(state_pointer);
	Write(bVisible ? CMD_POINTER_ON : CMD_POINTER_OFF);
}

void CSLMSocket::EnableFiring(BOOL bEnable) {
	setState(state_firing);
	Write(bEnable ? CMD_ENABLE_FIRING : CMD_DISABLE_FIRING);
}

void CSLMSocket::UpdateCurrentAngle() {
	setState(state_current_angle);
	Write(CMD_CURRENT_ANGLE);
	m_bOneShot = TRUE;
}
