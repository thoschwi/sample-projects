#include "myutils.h"
#include "errorhandler.h"
#include "myprotocol.h"


/*Tokenizes the receive buffer and puts the each token into tokens.
 Expects that the sendbuffer string delimits each token with a slash ('/').
 Returns the number of tokens that was made. 
 ONLY FOR USE IN FUNCTIONS THAT RECV() DIRECTORY CONTENTS.
*/
int tok_recvbuf(char **tokens, char *recvbuf){

  int i = 0;
  char *token  = strtok(recvbuf, "/");
  while(token != NULL){
    tokens[i] = token;
    token = strtok(NULL, "/");   
    i++;
  }
  tokens[i] = NULL;
  printf("num_entries is: %i\n", i);
  return i;
}

/*Client-side logic for cmd 1. Server-side counterpart is serv_list_dir().
  Queries, receives and lists the entries vertically in the terminal 
  (limit is 64 entries) in the current directory of the server. 
  Returns the number of entries in the directory.*/
int list_dir(char cmd, int sd, char **entries){

  int num_entries = 0;
  int len = 0;
  int bytes_recvd = 0;
  
  send_cmd(cmd, sd);
  len = recv_header(sd);

  char recvbuf[len];
  memset(recvbuf, 0, sizeof(recvbuf));
  memset(entries, 0, sizeof(entries)); 

  if((bytes_recvd = recv(sd, recvbuf, len, 0)) == -1)
    perror("recv");

  num_entries = tok_recvbuf(entries, recvbuf);
  if (num_entries > 64){
    printf("NOTE: There were more entries in the dir, but not enough room to show them all.\n");
    num_entries = 64;
  }
  int i;
  printf("Contents of current dir:\n");
  for(i = 0; i < num_entries; i++)
    printf("\t[%d] %s\n", i+1, entries[i]);
  
  return num_entries;
}

int recv_file(int sd){

  int bytes_left;
  int n;

  bytes_left = recv_header(sd);
  char recvbuf[513];
  memset(recvbuf, 0, sizeof(recvbuf));
 
  while(bytes_left > 0){
    if((n = recv(sd, recvbuf, 512, 0)) == -1){
	perror("recv");
    return -1;
    }else{
      printf("%s", recvbuf);
      bytes_left -= n;      
    }
  }
  
  return 0;
}

/*Requests the server for the given command, 3 (change dir), 
  4 (file info) or 5 (cat), and prompts the user to select
  from the available options presented. */
void query(char cmd, int sd){

  char *entries[64];
  uint16_t num_entries = list_dir(cmd, sd, entries);
  int bytes_recvd = 0;
  uint16_t len = 0;
  uint16_t msg[2] = {0,0};
  char r_cmd = 0; //The option the server willl reply with
  char input[3];
  
  memset(input, 0, sizeof(input));
  memset(entries, 0, sizeof(entries));

  int c = 0;
  while(1){ //Cmd loop. Accepts up to 2 chars.  
    do{
      printf("\n*** Enter a number (q to exit this menu) > ");
      if(! fgets(input, 4, stdin)){
	close(sd);
	exit(0);
      }
    } while(is_overflowed(input));
          
    c = (input[0] != 'q') ? atoi(input) : 113;
    if(c > 0 && c <= num_entries){
      switch(cmd){
      case '4':
	r_cmd = 'F';
	break;
      case '3':
	r_cmd = 'C';
	break;
      case '5':
	r_cmd = 'T';
	break;
      }
    
      msg[0] = htons(c);
      send_cmd(r_cmd, sd); //Tell server to run serv_query().
      send_header(sd, sizeof(msg), 0);
      if(send(sd, msg, sizeof(msg), 0) == -1){
	perror("send");
	continue;
      }
           
      len = recv_header(sd);
      char recvbuf[len];
      memset(recvbuf, 0, sizeof(recvbuf));
      memset(recvbuf, 0, sizeof(recvbuf));
      if((bytes_recvd = recv(sd, recvbuf, len, 0)) == -1){
	perror("recv");
	continue;
      }
      printf("\n\tServer replied: %s\n", recvbuf);
      if(strcmp(recvbuf, "file") == 0){
	recv_file(sd);
      }
      return;
    }
    else
      if(c == 113)
	break;
      else
	printf("Invalid input!");
  }
}

/*Client-side logic for cmd 2. Server-side counterpart is serv_pwd().
 Queries, receives and prints to terminal the name of the 
 current directory of the server.*/
void cur_dir(char cmd, int sd){

  uint16_t len = 0;
  int bytes_recvd = 0;

  send_cmd(cmd, sd);
  len = recv_header(sd);
  
  char recvbuf[len];
  memset(recvbuf, 0, sizeof(recvbuf));

  if((bytes_recvd = recv(sd, recvbuf, len, 0)) == -1)
    perror("recv");

  recvbuf[len] = '\0';
  printf("\n\tCurrent directory:\n%s\n", recvbuf);
  
}

/*''''''''CLIENT MAIN''''''''*/
int main(int argc, char **argv){

  if(argc < 3)
    client_usage();

  const char *host = argv[1];
  const char *port = argv[2];

  int sockfd; //File descriptor returned from socket()
  int status; //Return val of getaddrinfo(). For debugging.
  //Structs filled by getaddrinfo()
  struct addrinfo hints;
  struct addrinfo *results;
  char input[MAX_USER_INPUT+1];
  char addr[INET6_ADDRSTRLEN];
  char *entries[64];

  memset(&hints, 0, sizeof(hints));

  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_STREAM;

  if((status = getaddrinfo(host, port, &hints, &results)) != 0){
    fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(status));
    exit(EXIT_FAILURE);  
  }

  print_ips(results);

  //Attempt to connect to each address in results.
  struct addrinfo *tmp;
  for(tmp = results; tmp != NULL; tmp = tmp->ai_next){
    if((sockfd = socket(tmp->ai_family, tmp->ai_socktype,
			tmp->ai_protocol)) == -1){
      perror("socket");
      continue;
    }

    if((connect(sockfd, results->ai_addr, results->ai_addrlen)) == -1){
      close(sockfd);
      perror("connect");
      continue;
    }    
    break;
  }

  if(tmp == NULL){
    fprintf(stderr, "Failed to connect!\n");
    exit(EXIT_FAILURE);
  }

  inet_ntop(tmp->ai_family, get_addr((struct sockaddr *)tmp->ai_addr),
	    addr, sizeof(addr));
  freeaddrinfo(results);
  
  char c = 0;
  while(c != 'q'){ //Main menu loop. WILL ONLY ACCEPT SINGLE CHARS!    
    do{
      printf("\ncmd (h for help) > ");
      if(! fgets(input, MAX_USER_INPUT+2, stdin)){
	close(sockfd);
	exit(0);
      }
    } while(is_overflowed(input));
    
    c = input[0];
    switch(c){
    case '1':
      list_dir(c, sockfd, entries);
      break;
    case '2':
      cur_dir(c, sockfd);
      break;
    case '3':     
      query(c, sockfd);//TO DO: Server
      break;
    case '4':
      query(c, sockfd);
      break;
    case '5':    
      query(c, sockfd); //TO DO: Server
      break;
    case 'h':
      main_help(addr);
      break;
    case 'q':
      send_cmd(c, sockfd);
      break;
    default:
      printf("Invalid input!");
    }    
  } 
  close(sockfd);
  return 0;
}

