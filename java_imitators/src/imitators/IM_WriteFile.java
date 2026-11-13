/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IM_WriteFile implements IM.IListener {

  File of;
  FileWriter outputFile = null;

  public IM_WriteFile(String fileName) throws IOException, Exception {
    of = new File(fileName);
    of.delete();
    if (of.createNewFile())
      outputFile = new FileWriter(of);
    else
      throw new Exception("Can't create output file. Try to close Volume.exe and repeat.");
  }

  @Override
  public void close() {
    if (outputFile != null) try {
      outputFile.close();
      of.delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendDistance(String distance, IM im) throws IOException {
    outputFile.write(distance);
    outputFile.flush();
  }

}
