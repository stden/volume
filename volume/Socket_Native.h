#ifndef WINSOCKETS_
#define WINSOCKETS_

#include <assert.h>
#include <winsock.h>

// Инициализация и закрытие Winsock
class WinsockInit {
public:
  WSADATA wsd;
  int iStatus;
  WinsockInit(WORD WinsockVersion = 0x0101) {
    iStatus = WSAStartup(WinsockVersion, &wsd);
  };
  ~WinsockInit() {
    WSACleanup();
  };
};

static const WinsockInit wsInit;

// InetAddr
class InetAddr : public sockaddr_in {
public:
  InetAddr(WORD wPort = 0) {
    memset(this, 0, sizeof(sockaddr_in));
    sin_family = AF_INET;
    sin_addr.s_addr = htonl(INADDR_ANY);
    sin_port = htons((u_short)wPort);
  }
  InetAddr(LPCTSTR lpszAddress, WORD wPort = 0) {
    Resolve(lpszAddress, wPort);
  }
  InetAddr& operator =(LPCTSTR lpszAddress) {
    Resolve(lpszAddress);
    return *this;
  }
protected:
  void Resolve(LPCTSTR lpszAddress, WORD wPort = 0) {
    memset(this, 0, sizeof(sockaddr_in));
    sin_family = AF_INET;
    sin_addr.s_addr = inet_addr((LPTSTR)lpszAddress);
    if (sin_addr.s_addr == INADDR_NONE && strcmp((LPTSTR)lpszAddress,
        "255.255.255.255")!=0) {
      HOSTENT* lphost = gethostbyname((LPTSTR)lpszAddress);
      if (lphost)
        sin_addr.s_addr = ((IN_ADDR*)lphost->h_addr)->s_addr;
      else
        sin_addr.s_addr = INADDR_ANY;
    }
    sin_port = htons((u_short)wPort);
  }
};

// Socket
template <int SOCKET_TYPE>
class Socket {
public:
  Socket() :
    sock(INVALID_SOCKET), bOwnSocket(false) {
  }
  Socket(SOCKET s) :
    sock(s), bOwnSocket(false) {
  }
  Socket(const Socket& s) :
    sock(s), bOwnSocket(false) {
  }
  virtual ~Socket() {
    if (bOwnSocket && sock != INVALID_SOCKET)
      Close();
  }
  bool Create() {
    assert(sock == INVALID_SOCKET);
    sock = socket(AF_INET, SOCKET_TYPE, 0);
    return (bOwnSocket = (sock != INVALID_SOCKET));
  }
  void Close() {
    if(sock==INVALID_SOCKET) return; // !!!
    assert(sock != INVALID_SOCKET);
    shutdown(sock, 2);
    closesocket(sock);
    sock = INVALID_SOCKET;
  }
  bool Bind(const InetAddr& addr) {
    return bind(sock, (const sockaddr*)&addr, sizeof(sockaddr))
        != SOCKET_ERROR;
  }
  bool Connect(const InetAddr& addr) {
    return connect(sock, (const sockaddr*)&addr, sizeof(sockaddr))
        != SOCKET_ERROR;
  }
  bool Listen() {
    return listen(sock, 5) != SOCKET_ERROR;
  }
  Socket Accept(InetAddr& addr) {
    int len = sizeof(sockaddr);
    return Socket(accept(sock, (sockaddr*)&addr, &len));
  }
  int Send(const char* buf, int cbBuf) {
    return send(sock, buf, cbBuf, 0);
  }
  int Send(const char* fmt, ...) {
    va_list marker;
    va_start(marker, fmt);
    char szBuf[1024*4];
    vsprintf(szBuf, fmt, marker);
    va_end(marker);
    return Send(szBuf, strlen(szBuf));
  }
  int Receive(char* buf, int cbBuf) {
    return recv(sock, buf, cbBuf, 0);
  }
  bool SetOpt(int opt, const char* pBuf, int cbBuf) {
    return setsockopt(sock, SOL_SOCKET, opt, pBuf, cbBuf) != SOCKET_ERROR;
  }
  bool GetOpt(int opt, char* pBuf, int& cbBuf) {
    return getsockopt(sock, SOL_SOCKET, opt, pBuf, &cbBuf) != SOCKET_ERROR;
  }
  operator SOCKET&() const {
    return (SOCKET&)sock;
  }
  operator bool() const {
    return sock != INVALID_SOCKET;
  }

protected:
  SOCKET sock;

private:
  bool bOwnSocket;
};

#define bufMaxLen 4096

extern CEvent e_exit1,e_exit2,e_exit3;

template <int Port, class Client>
class BaseSocket {
  HANDLE hThread;
  Socket<SOCK_DGRAM> socket;
  Client* client;  
  long sendCnt;
public:
  BOOL Init(Client* _client, LPCTSTR szAddress){
    client = _client;
    if (!socket.Create())
      throw "Failed to create socket!";
    InetAddr addr(szAddress, Port);
    if (!socket.Connect(addr)) {
      socket.Close();
      throw "Failed to connect to host!";
    }    
    // start receiving messages from host
    hThread = CreateThread(NULL, 0, ListenProc, this, 0, NULL);
    return TRUE;
  }
  char buf[bufMaxLen];
  int bufLen;
  void DoReceive() {
    while(TRUE){
    	DWORD WaitReturn=WaitForSingleObject(e_exit1.m_hObject,1);
	    if(WaitReturn==WAIT_OBJECT_0) {
        TRACE("e_exit3.SetEvent()\n");
		    e_exit3.SetEvent();
		    break;
      }    
      bufLen = socket.Receive(buf,bufMaxLen);
      if(bufLen!=-1) 
        client->OnReceive();
    }
  }
  static DWORD WINAPI ListenProc(LPVOID pparam) {
    BaseSocket* pThis = (BaseSocket*)pparam;    
    pThis->DoReceive();
    CloseHandle(pThis->hThread);
    pThis->hThread = NULL;
    return 0;
  }  
  void Close(){
    socket.Close();
  }
  DWORD m_dwWriteTC;
  void Send( char *buff ){
    sendCnt++;
    m_dwWriteTC = ::GetTickCount();
    TRACE("Send #%ld time=%ld \"%s\"\n", sendCnt, m_dwWriteTC, buff);
    socket.Send(buff,strlen(buff));
  }
	int ReceiveFrom(char* lpBuf, int nBufLen, SOCKADDR* lpSockAddr, int* lpSockAddrLen ){
    if(bufLen>0){
      memcpy(lpBuf,buf,bufLen);
    }
    return bufLen;
  }
};

#endif /*WINSOCKETS_*/
