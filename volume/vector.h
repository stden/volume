#ifndef __VECTOR_H__
#define __VECTOR_H__

#include <string.h>
#define M_PI 3.14159265358979323846
#define RADTOGR 180./M_PI
#define GRTORAD M_PI/180.
#define	NSPACE	3

union MatrixGL 
{ double ma[4][4];
  double m[16];
  MatrixGL(){memset(m,0,sizeof(double)*16);};
  MatrixGL init(double* matrx){memcpy(m,matrx,sizeof(double)*16); return *this;};
  MatrixGL(double* matrx) { init(matrx);};
  MatrixGL operator =(const MatrixGL& matr){memcpy(m,matr.m,sizeof(double)*16);return *this;};
};

MatrixGL operator ~(const MatrixGL& matr);

union VectorGL 
{ struct { double x,y,z; } s;
  double xyz[NSPACE];
  VectorGL(){s.x=0; s.y=0; s.z=0;};
  VectorGL init(double a,double b,double c) { s.x=a; s.y=b; s.z=c; return *this;};
  VectorGL(double a,double b,double c) { init(a,b,c); };
  VectorGL operator =(const VectorGL& vect){s.x=vect.s.x;s.y=vect.s.y;s.z=vect.s.z; return *this;};
};

VectorGL operator -(const VectorGL&,const VectorGL&);
VectorGL operator +(const VectorGL&,const VectorGL&);
VectorGL operator &(const VectorGL&,const VectorGL&);
VectorGL operator ^(const VectorGL&,const VectorGL&);
double operator |(const VectorGL&,const VectorGL&);
double V_len(const VectorGL&);
VectorGL operator *(double&,const VectorGL&);
VectorGL operator *(const MatrixGL& ,const VectorGL&);
VectorGL operator *(const VectorGL&, const MatrixGL&);

#endif
