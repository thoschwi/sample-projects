#include <stdlib.h>

void client_usage(){
  printf("USAGE: <hostname> <port number>\n");
  exit(EXIT_FAILURE);
}

/*Handles errors from socket, connect etc in the client. Note that this function does the exact same thing as server_error(); I made two just for clarity.*/
void client_error(char *msg, struct addrinfo *ai){
  perror(msg);
  freeaddrinfo(ai);
  exit(EXIT_FAILURE);
}

void server_error(char *msg, struct addrinfo *ai){
  perror(msg);
  freeaddrinfo(ai);
  exit(EXIT_FAILURE);
}


void server_usage(){
  printf("USAGE: <port number>\n");
  exit(EXIT_FAILURE);
}

/*Closes connection and free()s a malloc()ed pointer before exit()ing. */
void clean_crash(int sd, void *p){

  close(sd);
  free(p);
  exit(EXIT_FAILURE);
}

