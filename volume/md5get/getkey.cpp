// getkey.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <stdlib.h>
#include "check.h"

int main()
{
	char secretstr[]={'f','2','l','i','n','e','.','.','\0'};
	char filename[]="volume.key";
	if(checkKey(filename,secretstr,true)<=0) printf("error checkKey()\n");
	else printf("'%s' generated\n",filename);
	return 0;
}


