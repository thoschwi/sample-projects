#include <ctype.h>
#include <dirent.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>

#define MAX_USER_INPUT 1 //The max input length of the main menu prompt.
#define MAX_CLIENTS 100 //Arbitrary max number of simultaneous connections.

//Symbolic constants for get_dir_ents():
#define ALL_ENTS 1 //All entries will be stored.
#define DIRS_ONLY 3 //Only directories will be stored.
#define REG_ONLY 5 //Only regular files will be stored.


void main_help(char *addr){
  char *list[7] = {"[1] List content of current directory (ls)",
		   "[2] Print name of current directory (pwd)",
		   "[3] Change current directory (cd)",
		   "[4] Get file info",
		   "[5] Display file (cat)",
		   "[h] This menu",
		   "[q] Quit"};
  printf("*** You are connected to %s.\n*** Please press a key:\n", addr);
  
  int i;
  for(i = 0; i < 7; i++){
    printf("\t%s\n", list[i]);
  }
}

/*Processes a string of input from the command line. It checks if it's
 longer than what is allowed by the current menu and consumes any
 overflow if it is. 
 Returns 1 if the string was found to be too long, 0 otherwise.*/
int is_overflowed(char *buf){

  int too_long = 0;
  char c = 0;

  if(buf[strlen(buf)-1] != '\n'){ //No newline; input must be too long.
    printf("Input is too long!.\n");
    while((c = getchar()) != '\n' && c != EOF) //Consume overflow.
      ;
    too_long = 1;
  }
   
  buf[strlen(buf)-1] = '\0'; //Remove newline for subsequent calls.
  return too_long;
}


/*Iterates through the entries in the directory given by dir_name, puts
  them in entries and returns the total number of entries found. 
  This function is used by serv_list_dir() to prepare the contents of 
  the client's current directory before sending it to him.
  len will contain the total number of bytes + 1 delimiting byte
  for each entry. 
  The param cmd specifies what kind of entries should be stored 
  (see #defines above). This is to ensure that the client is presented
  only with what he requested, and thus cannot select an invalid option.*/
int get_dir_ents(char **entries, char dir_name[], uint16_t *len, int cmd){

  int i = 0; //Number of entries found.
  DIR *dir = NULL;
  char *full_path = NULL;
  char buf[512];
  struct dirent *ent = NULL;
  struct stat file_stat;
  memset(buf, 0, sizeof(buf));

  printf("get_dir_ents: Attempting to open dir <%s>\n", dir_name);
  if((dir = opendir(dir_name)) != NULL){     
    while((ent = readdir(dir)) != NULL){
      char *fname = ent->d_name;
      strcpy(buf, dir_name);
      strcat(buf, "/");
      full_path = strcat(buf, fname);

      if(lstat(full_path, &file_stat) == -1){
	perror("lstat");
	return -1;
      }

      switch(cmd){
      case ALL_ENTS: //For cmds 1 and 4.
	entries[i] = fname;
	*len += (strlen(fname)+1);//the extra byte is for delimiting
	i++;
	break;
      case DIRS_ONLY: //For cmd 3.
	if(S_ISDIR(file_stat.st_mode)){
	  entries[i] = fname;
	  *len += (strlen(fname)+1); 
	  i++;
	}
	break;
      case REG_ONLY: //For cmd 5.
	if(S_ISREG(file_stat.st_mode)){
	  entries[i] = fname;
	  *len += (strlen(fname)+1);
	  i++;
	}
	break;
      }    
    }
  } else {
    perror("opendir");
    exit(EXIT_FAILURE);
  }
  entries[i] = NULL;
  closedir(dir);
  return i;
}

/*Takes a pointer to a struct addrinfo and prints to terminal 
  all ip-addresses that it contains. Used for debugging.*/
void print_ips(struct addrinfo *res){

  void *addr;
  char *ipver;
  struct addrinfo *tmp;
  char ipstr[INET6_ADDRSTRLEN];

  for(tmp = res; tmp != NULL; tmp = tmp->ai_next){

    if(tmp->ai_family == AF_INET){ //IPv4
      struct sockaddr_in *ipv4 = (struct sockaddr_in *) tmp->ai_addr;
      addr = &(ipv4->sin_addr);
      ipver = "IPv4";
    } else { //IPv6
      struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *)tmp->ai_addr;
      addr = &(ipv6->sin6_addr);
      ipver = "IPv6";
    }

    inet_ntop(tmp->ai_family, addr, ipstr, sizeof(ipstr));
    printf(" %s: %s\n", ipver, ipstr);
  }
}

/*Returns pointer to the address of a host; IPv4 or IPv6.*/
void *get_addr(struct sockaddr *s)
{
  if (s->sa_family == AF_INET) {
    return &(((struct sockaddr_in*)s)->sin_addr);
  }
  return &(((struct sockaddr_in6*)s)->sin6_addr);
}

