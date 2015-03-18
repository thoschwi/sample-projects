#include "myutils.h"
#include "errorhandler.h"
#include "myprotocol.h"

char *working_dir = NULL;

/*Structure representing a client that is connected to the server.*/
struct client{

  int sd; //Socket file descriptor. -1 if not set
  char *addr; //IP address
  char *cur_dir; //Directory client is currently viewing
};

struct client *clients[MAX_CLIENTS]; //"Slots" for clients.

/*Mallocs a new client, sets its fields and puts it into clients.
 NOTE: Clients are free()d when they disconnect normally.*/
void make_client(int new_fd, char *addr, char *dir){
  struct client *c = (struct client*) malloc(8192);

  if(c == NULL){
    perror("malloc");
    exit(EXIT_FAILURE);
  }
      
  c->sd = new_fd;
  c->addr = addr;
  c->cur_dir = dir;
  clients[new_fd] = c;
}


/*Sends the name of the current directory to the client. Client counterpart
  is cur_dir(). Returns -1 if send() failed.*/
int serv_pwd(int sd, char *wd){

  uint16_t len = strlen(wd);
  
  send_header(sd, len, 2);

  if(send(sd, wd, len, 0) != -1){
    perror("send");
    return -1;
  }
  return 0;
}

/*Gathers all entries in the directory dir_name and sends it to the client.
  Imitates the shell cmd 'ls'. Returns the number of entries 
  (as found by get_dir_ents()), or -1 if send() failed. This function is 
  also called in serv_query() to list specific types of files depending 
  on the what was reqested by the client. See get_dir_ents() in my_utils.h
  for more details.*/
int serv_list_dir(int sd, int cmd, struct client *c){

  int i = 0;
  uint16_t len = 0;
  char *entries[64];//Arbitrary limit
  memset(entries, 0, sizeof(entries));

  i = get_dir_ents(entries, c->cur_dir, &len, cmd);
  printf("get_dir_ents found %d entries.\n", i);
  send_header(sd, len, cmd);
  
  char sendbuf[len];
  memset(sendbuf, 0, sizeof(sendbuf));
  make_sendbuf(entries, len, sendbuf);

  if(send(sd, sendbuf, len-1, 0) != -1){
    perror("send");
    return -1;
  }
  return i;
}

/*Called by serv_query() to process cmd 4 (file info).*/
int finfo(int sd, struct stat file_stat){

  char *reply;
  if(S_ISREG(file_stat.st_mode))
    reply = "File is a regular file!";
  else if(S_ISDIR(file_stat.st_mode))
    reply = "File is a directory!";
  else if(S_ISLNK(file_stat.st_mode))
    reply = "File is a link!";  
  else
    reply = "File must be some kind of special file!";

  send_header(sd, strlen(reply)+1, 0);
     
  if(send(sd, reply, strlen(reply)+1, 0) == -1){
    perror("send");
    return -1;
  }
  return 0;
}

/*Called by serv_query() to process cmd 3 (change dir). Changes
 the directory to whatever is selected by the client (only directory names
 are presented client_side). The client does not have permission to move
 above the directory in which the server started (working_dir).
 Returns -1 if send() should fail, 0 otherwise.*/
int change_dir(int sd, char *entry, char *full_path, struct client *c){

  char *reply = NULL;
  char buf[4096];
  char buf2[4096];

  if(strcmp(entry, "..") == 0){
    printf("change_dir: Client has requested to move up...\n");
    if(strcmp(clients[sd]->cur_dir, working_dir) == 0){
      reply = "Client does not have permission to move up!";
    } else {
      strcpy(buf2, c->cur_dir);
      int i = strlen(buf2);
      while(buf2[i] != '/'){//Remove last folder name in the path 
	buf2[i] = 0;
	i--;
      }
      buf2[i] = 0;//Remove slash also
      c->cur_dir = strcpy(buf, buf2);
      printf("Changed directory to %s (parent).\n", c->cur_dir);
      reply = "Changed to parent dir!";    
    }
  } else if(strcmp(entry, ".") != 0){
    c->cur_dir = strcpy(buf, full_path);
    printf("Changed directory to <%s>\n", c->cur_dir);
    reply = "Directory was changed!";
  } else
    reply = "'.' is the current dir; no change.";
  
  send_header(sd, strlen(reply), 0);
     
  if(send(sd, reply, strlen(reply), 0) == -1){
    perror("send");
    return -1;
  }
  return 0;  
}

/*Sends a file chosen by the client over to his side. The contents
 are printed to the terminals on both sides. Returns -1 should any 
 system calls fail, 0 otherwise.*/
int send_file(int sd, char *path, struct stat file_stat){

  int bytes_left = 0;
  int bytes_sent = 0;
  int n = 0;
  char c = 0;
  FILE *f;
  
  send_header(sd, 5, 0);
     
  if(send(sd, "file", 5, 0) == -1){
    perror("send");
    return -1;
  }

  if((f = fopen(path, "r")) == NULL){
    perror("fopen");
    return -1;
  }
  
  bytes_left = file_stat.st_size;
  char send_buf[513];
 
  send_header(sd, bytes_left, 0);
  printf("File Size is %d\n", bytes_left);
  
  while(bytes_sent < bytes_left){
    int i = 0;
    memset(send_buf, 0, sizeof(send_buf));
    while(((c = fgetc(f)) != EOF)){
      send_buf[i] = c;
      i++;
      if(i == 512)
	break;
    }
    printf("%s", send_buf);
    if((n = send(sd, send_buf, 512, 0)) == -1){
      perror("send");
      break;
    }

    bytes_sent += n;
  }
  fclose(f);
  
  return 0;
}

int serv_query(int sd, struct client *c, int cmd){

  char *dir_name = c->cur_dir;
  uint16_t len = 0;
  uint16_t msg_buf[2] = {0,0};
  uint16_t msg = 0;
  uint16_t msg_len = 0;
  char *full_path = NULL;
  char *fname = NULL; //Filename of client's choice.
  char buf[4096];
  char *entries[64];
  struct stat file_stat;
  memset(entries, 0, sizeof(entries));
  memset(buf, 0, sizeof(buf));

  msg_len = recv_header(sd);
  if(recv(sd, msg_buf, msg_len, 0) == -1){
    perror("recv");
    return -1;
  }
    
  msg = ntohs(msg_buf[0]);
  get_dir_ents(entries, dir_name, &len, cmd);

  fname = entries[msg-1];
  strcpy(buf, dir_name);
  strcat(buf, "/");
  full_path = strcat(buf, fname);

  if(lstat(full_path, &file_stat) == -1){
    perror("lstat");
    return -1;
  }

  switch(cmd){
  case DIRS_ONLY:
    change_dir(sd, fname, full_path, c);
    break;
  case ALL_ENTS:
    finfo(sd, file_stat);
    break;
  case REG_ONLY:
    send_file(sd, full_path, file_stat);
    break;
  } 
  return 0;
}

/*'''''''SERVER MAIN'''''''*/
int main(int argc, char **argv){

  if(argc < 2)
    server_usage();

  const char *port = argv[1];
  char w_dir_buf[4096];

  int request_sd; //Listen to this
  int new_fd; //Connect to this
  int status;
  int activate = 1;
  socklen_t sin_size;
  struct addrinfo hints;
  struct addrinfo *results;
  struct sockaddr_storage connector;
  char addr[INET6_ADDRSTRLEN];

  fd_set master;
  fd_set read_fds;
  int max_fd;

  FD_ZERO(&master);
  FD_ZERO(&read_fds);
  
  memset(&hints, 0, sizeof(hints));
  memset(w_dir_buf, 0, sizeof(w_dir_buf));
  getcwd(w_dir_buf, 1024);
  working_dir = w_dir_buf;
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_STREAM;
  hints.ai_flags = AI_PASSIVE;
   
  if((status = getaddrinfo(NULL, port, &hints, &results)) != 0){
    fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(status));
    exit(EXIT_FAILURE);
  }

  struct addrinfo *tmp;
  for(tmp = results; tmp != NULL; tmp = tmp->ai_next){
    if((request_sd = socket(tmp->ai_family, tmp->ai_socktype,
			    tmp->ai_protocol)) == -1){
      perror("socket");
      continue;
    }
      
    if(setsockopt(request_sd, SOL_SOCKET,
		  SO_REUSEADDR, &activate, sizeof(int)) == -1)
      server_error("setsockopt", results);
           
    if(bind(request_sd, tmp->ai_addr, tmp->ai_addrlen) == -1){
      close(request_sd);
      perror("bind");
      continue;
    }
    break;
  }

  if(tmp == NULL){
    fprintf(stderr, "Failed to bind!\n");
    exit(EXIT_FAILURE);
  }
  /*'''''WE ARE STILL IN MAIN()'''''*/
  freeaddrinfo(results);

  printf("Listening for connectors...\n");
  if(listen(request_sd, SOMAXCONN) == -1){ 
    perror("listen");
    exit(EXIT_FAILURE);
  }
    
  FD_SET(request_sd, &master);
  max_fd = request_sd;
  printf("request_sd is: %d\n", request_sd);
  while(1){ //MAIN LOOP
    read_fds = master;
    printf("select: Awaiting inputs...\n");
    if(select(max_fd+1, &read_fds, NULL, NULL, NULL) == -1){
      perror("select");
      exit(EXIT_FAILURE);
    }

    int i;
    for(i = 0; i <= max_fd; i++){
      if(FD_ISSET(i, &read_fds)){//New connection!
	if(i == request_sd){ //Handle new connections.
	  sin_size = sizeof(connector);
	  new_fd = accept(request_sd,
			  (struct sockaddr *)&connector, &sin_size);
	  if(new_fd == -1){
	    perror("accept");	  
	  }else{
	    FD_SET(new_fd, &master);
	    if(new_fd > max_fd)
	      max_fd = new_fd;

	    printf("new_fd is: %d\n", new_fd);  
	    inet_ntop(connector.ss_family,
		      get_addr((struct sockaddr *)&connector),
		      addr, sizeof(addr));
	    make_client(new_fd, addr, w_dir_buf);
	    printf("***Client info***\nAddress:\t%s\nDirectory:\t%s\n",
		   clients[new_fd]->addr, clients[new_fd]->cur_dir);	
	  }
	}else{ //Handle requests from a client.

	  char buf[MAX_USER_INPUT+1];
	  memset(buf, 0, sizeof(buf));;
	  char cmd = 0;
      
	  printf("\nReceived input from client [%d]...\n", i);
	  recv_cmd(i, buf);      
	  cmd = buf[0];
      
	  switch(cmd){
	  case '1':
	    printf("MAIN: ls was queried!\n");
	    printf("Listing dir for client [%d] (%s)",
		   i, clients[i]->cur_dir);
	    serv_list_dir(i, ALL_ENTS, clients[i]);
	    break;
	  case '2':
	    printf("MAIN: pwd was queried!\n");
	    serv_pwd(i, clients[i]->cur_dir);
	    break;
	  case '3':
	    printf("MAIN: cd was queried!\n");
	    serv_list_dir(i, DIRS_ONLY, clients[i]);
	    break;
	  case 'C'://Occurs if a choice is made in client-side cmd 3...
	    serv_query(i, clients[i], DIRS_ONLY);
	    break;
	  case '4':
	    printf("MAIN: file info was queried!\n");
	    serv_list_dir(i, ALL_ENTS, clients[i]);
	    break;
	  case 'F'://...cmd 4...
	    serv_query(i, clients[i], ALL_ENTS); 
	    break;
	  case '5':
	    printf("MAIN: cat was queried!\n");
	    serv_list_dir(i, REG_ONLY, clients[i]);
	    break;
	  case 'T'://...and cmd 5.
	    serv_query(i, clients[i], REG_ONLY);
	    break;
	  case 'q':
	    printf("Client [%d] has disconnected.\n", i);
	    close(i);
	    free(clients[i]);
	    FD_CLR(i, &master);
	    break;
	  }
	}
      }//End handling of requests.       
    }//End iterating fds.
  }//End MAIN LOOP
  close(request_sd);
  return 0;
}//END MAIN, AT LAST!

