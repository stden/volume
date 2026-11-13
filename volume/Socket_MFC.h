template <int Port, class Client>
class BaseSocket : public CAsyncSocket{
    Client* client;
    long sendCnt;
  public:
    BOOL Init(Client* _client, LPCTSTR szAddress){
    	AfxSocketInit();
      client = _client;
      sendCnt = 0;
      if (!Create(0, SOCK_DGRAM, FD_READ))
        return FALSE;      
      if (!CAsyncSocket::Connect(szAddress, Port))
        return FALSE;
      return TRUE;      
    }
    DWORD m_dwWriteTC;
    void Send( char *buff ){
      sendCnt++;
      m_dwWriteTC = ::GetTickCount();
      TRACE("Send #%ld time=%ld \"%s\"\n", sendCnt, m_dwWriteTC, buff);
      CAsyncSocket::Send(buff, strlen(buff));
    }
    virtual void OnReceive(int nErrorCode) {
      client->OnReceive();
      CAsyncSocket::OnReceive(nErrorCode);
    }
};