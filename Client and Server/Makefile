CC=gcc
CFLAGS=-Wall -Wextra -g

.PHONY: clean all

all: server_3 client_2

server_2: server_3.c
	$(CC) $(CFLAGS) -o server_3 server_3.c

client_2: client_2.c
	$(CC) $(CFLAGS) -o client_2 client_2.c

clean: 
	rm -f server_3
	rm -f client_2
