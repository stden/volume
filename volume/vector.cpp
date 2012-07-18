#include "stdafx.h"
#include <math.h>
#include "vector.h"

MatrixGL operator ~(const MatrixGL& matr) {
  MatrixGL mt;
  int i,j;
  for(i=0;i<4;i++)
    for(j=0;j<4;j++)
        mt.m[i*4+j]=matr.m[i+4*j]; 
  return mt;
}

VectorGL operator -(const VectorGL &a,const VectorGL &b){
  int i; VectorGL r;
  for(i=0;i<NSPACE;i++) r.xyz[i]=a.xyz[i]-b.xyz[i];
  return r;
}

VectorGL operator +(const VectorGL &a,const VectorGL &b){
  int i; VectorGL r;
  for(i=0;i<NSPACE;i++) r.xyz[i]=a.xyz[i]+b.xyz[i];
  return r;
}

double V_len2(const VectorGL &a,const VectorGL &b){
  int i; double r=0;
  for(i=0;i<NSPACE;i++) r+=a.xyz[i]*b.xyz[i];
  return r;
}

VectorGL operator &(const VectorGL &a,const VectorGL &b){
  int i; double t,t1; VectorGL r;
  t=V_len2(b,b); t1=V_len2(a,b);
  for(i=0;i<NSPACE;i++) r.xyz[i]=b.xyz[i]*t1/t;
  return r;
}

VectorGL operator ^(const VectorGL &a,const VectorGL &b){
  VectorGL r;
  r.s.x=a.s.y*b.s.z-a.s.z*b.s.y;
  r.s.y=a.s.z*b.s.x-a.s.x*b.s.z;
  r.s.z=a.s.x*b.s.y-a.s.y*b.s.x;
  return r;
}

double operator |(const VectorGL &a,const VectorGL &b){
  return acos((a.s.x*b.s.x+a.s.y*b.s.y+a.s.z*b.s.z)/(V_len(a)*V_len(b)))*RADTOGR;
}

double V_len(const VectorGL &v){
  double r;
  r=V_len2(v,v);
  return sqrt(fabs(r));
}

VectorGL operator *(double &d,const VectorGL &v){
  VectorGL r; int i;
  for(i=0;i<NSPACE;i++) r.xyz[i]=d*v.xyz[i];
  return r;
}

VectorGL operator *(const MatrixGL &matr,const VectorGL &v){
  int i,j; VectorGL r; double d;
  for(i=0;i<NSPACE;i++){
    d=0;
    for(j=0;j<NSPACE;j++) d+=matr.m[i*4+j]*v.xyz[j];
    r.xyz[i]=d;
  }
  return r;
}

VectorGL operator *(const VectorGL &v,const MatrixGL &matr){
  int i,j; VectorGL r; double d;
  for(i=0;i<NSPACE;i++){
    d=0;
    for(j=0;j<NSPACE;j++) d+=matr.m[j*4+i]*v.xyz[j];
    r.xyz[i]=d;
  }
  return r;
}

