#include <iostream>
#include <stdio.h>
#include "md5.h"
#include "check.h"

#define CharArray(name,length) 

DWORD getInfoHardDisk() {
    DWORD VolumeSerialNumber,MaximumComponentLength,FileSystemFlags;
    const int len=MAX_PATH;
    char VolumeNameBuffer[len],FileSystemNameBuffer[len];
    memset(VolumeNameBuffer,0,len);
    memset(FileSystemNameBuffer,0,len);
    GetVolumeInformation(NULL,VolumeNameBuffer,len,&VolumeSerialNumber,
        &MaximumComponentLength,&FileSystemFlags,FileSystemNameBuffer,len);
    return VolumeSerialNumber; 
}	

int openWriteFile(char *file_name,DWORD serial) {
    FILE *file=fopen(file_name,"w");
    if(!file) return -1; 	
    fprintf(file,"%ld\n",serial);
    fclose(file);
    return 0;
}

int checkKey (char* file_name, char* secretstr, bool getflag)
{
    DWORD serial; 
    FILE *file;

    const int lenmd=16;
    const int len=100;
    char strfile[len]; memset(strfile,0,len);
    char strinfo[len]; memset(strinfo,0,len);

    if(!getflag) {
        serial = getInfoHardDisk();
        file = fopen( file_name, "r" );
        if(!file) return openWriteFile(file_name,serial);
        fgets(strfile, len, file);
        fclose( file );
    } else {
        file = fopen( file_name, "r" );
        if(!file) {
            printf("'%s' not found\n",file_name);
            return -1;
        }
        fscanf(file,"%ld",&serial);
        printf("'%s': scanning key is %ld\n",file_name,serial);
        fclose(file); 
    } 

    ultoa(serial,strinfo,10);
    strcat(strinfo,secretstr);

    unsigned char digest[lenmd];
    md5((unsigned char*)strinfo,lenmd,digest);
    char str[lenmd*2+1]; memset(str,0,lenmd*2+1);
    for( int i = 0; i < lenmd; i++ )
        sprintf(str, "%s%02x", str, digest[i]);

    if(!getflag) {
        if (strcmp(str, strfile) !=0 ) return openWriteFile(file_name,serial);
    }
    else {
        FILE *file=fopen(file_name,"w");
        if(!file) return -1; 	
        fprintf(file,"%s",str);
        //fprintf(file,"\n");
        fclose(file);
    } 

    return 1;
}

