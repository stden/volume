package data;

import java.io.Serializable;

public class StoreParams implements Serializable {

  //public double width = 0;
  public double left = 0;
  public double right = 0;
  //public double length = 0;
  public double start = 0;
  public double stop = 0;
  public double height = 0;
  /** volume of empty store */
  public double volume = 0;
  public double ElVolume = 0;
  public double FreeVolume = 0;

  /** Creates a new instance of StoreParams */
  public StoreParams(double left, double right, double start, double stop, double height) {
    if ((left > 0) && (right > 0) && (start > 0) && (stop > 0) && (start > stop) && (height > 0)) {
      this.left = left;
      this.right = right;
      this.start = start;
      this.stop = stop;
      this.height = height;
      volume = (left + right) * (start - stop) * height;
    } else {
      throw new IllegalArgumentException();
    }
  }
  /*public StoreParams(double length, double width, double height){        
      if( (length>0) && (width>0) && (height>0) ){
          this.length = length;
          this.width = width;
          this.height = height;
          volume = length*height*width;            
      } else {       
          throw new IllegalArgumentException();
      }
  }*/

  /*public double getVolume(){
       return volume;
   }*/

  /*public double getLength(){
      return (start - stop);
  }*/

  /* public double getWidth(){
       return (left+right);
   }*/

  /* public double getHeight(){
       return height;
   }*/

}
