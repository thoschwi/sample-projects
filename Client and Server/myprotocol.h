
#define CMD_LS 1
#define CMD_PWD 2
#define CMD_CD 3
#define CMD_FINFO 4
#define CMD_CAT 5

/*Makes and returns a continuous string of all the dir entries in entries.*/
char *make_sendbuf(char **entries, uint16_t len, char *sendbuf){

  char tmp[len+1];
  char buf[len+1];
  memset(sendbuf, 0, sizeof(sendbuf));
  memset(tmp, 0, sizeof(tmp));
  
  while(*entries){
    strcpy(tmp, *entries);
    strcpy(buf, tmp);
    strcat(buf, "/");
    strcat(sendbuf, buf);
    entries++;
  }
  return sendbuf;
}

/*Sends the char that was input by the user. Only for use when sending
 cmds from the menu loops (client-side).*/
void send_cmd(char cmd, int sd){

  int bytes_sent = 0;
  char buf[MAX_USER_INPUT+1];
  memset(buf, 0, sizeof(buf));

  buf[0] = cmd;
  buf[MAX_USER_INPUT] = '\0';
  
  if((bytes_sent = send(sd, buf, 2, 0)) == -1)
    perror("send");

}

/*Server-side counterpart of send_cmd.*/
void recv_cmd(int sd, char *buf){

  int bytes_recvd = 0;
  memset(buf, 0, sizeof(buf));
  
  if((bytes_recvd = recv(sd, buf, 3, 0)) == -1){
    perror("recv");
    exit(EXIT_FAILURE);
  }
}

/*Sends a "packet" containing info (in a uint16_t array) about 
  the data that is about to be sent to the remote connection. To be called 
  always before sending text data of arbitrary lengths.
  The other side must always respond with a recv_header() in 
  its corresponding function. Returns 0 when each side 
  has successfully shaken hands (sent and received the info), 
  -1 otherwise (the numbers sent do not check out 
  with the numbers received).*/
int send_header(int sd, int len, int cmd){

  uint16_t header[3];
  uint16_t intbuf[3];
  uint16_t chksum = 0;
  int bytes_recvd = 0;
  int bytes_sent = 0;

  header[0] = htons(len); //length of data set about to be sent
  header[1] = htons(cmd); //the specific cmd server is responding to
  header[2] = 0; //termination

  chksum = sizeof(header);
  
  if((bytes_sent = send(sd, header, chksum, 0)) == -1){
    perror("send");
    return -1;
  }
     
  if((bytes_recvd = recv(sd, intbuf, chksum+1, 0)) == -1){
    perror("recv");
    return -1;
  }
     
  if(bytes_recvd-1 == chksum)
    return 0;
  else
    return -1;    
}

/*Receives the "header" info sent by send_header() and sends back an
 acknowledgement of the info. Returns the number of bytes that is about
 to be sent by the calling side.*/
uint16_t recv_header(int sd){

  uint16_t header[3] = {0, 0, 0};
  int bytes_recvd = 0;
  int bytes_sent = 0;

  if((bytes_recvd = recv(sd, header, sizeof(header)+1, 0)) == -1)
    perror("recv");

  header[0] = ntohs(header[0]);
  header[1] = ntohs(header[1]);
  header[2] = ntohs(header[2]);

  switch(header[1]){
  case CMD_LS:
    printf("Server has responded to cmd ls\n");
    break;
  case CMD_PWD:
    printf("Server has responded to cmd pwd\n");
    break;
  case CMD_CD:
    printf("Server has responded to cmd cd\n");
    break;
  case CMD_FINFO:
    printf("Server has responded to cmd file info\n");
    break;
  case CMD_CAT:
    printf("Server has responded to cmd cat\n");
    break;
  default: //Other values can be passed to switch off printing
    break;
  }

  if((bytes_sent = send(sd, header, sizeof(header), 0)) == -1)
    perror("send");

  printf("Remote connection is about to send %d bytes!\n", header[0]);
 
  return header[0];
}
  
