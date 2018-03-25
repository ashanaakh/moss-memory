// The main MemoryManagement program, created by Alexander Reeder, 2000 Nov 19

import java.io.File;

public class MemoryManagement {

  public static void checkFile(String path) {
    File file = new File(path);

    boolean isOk = file.exists() && file.canRead();

    if (!isOk) {
      System.out.println(path + " is unreachable!");
      System.exit(-1);
    }
  }

  public static void main(String[] args) {
    ControlPanel controlPanel;
    Kernel kernel;

    String commandFilePath = "commands";
    String propsFilePath = "memory.conf";

    if (args.length >= 1) {
      commandFilePath = args[0];
    }

    if (args.length >= 2) {
      propsFilePath = args[1];
    }

    checkFile(commandFilePath);
    checkFile(propsFilePath);

    kernel = new Kernel();
    controlPanel = new ControlPanel("Memory Management");

    controlPanel.init(kernel, commandFilePath, propsFilePath);
  }
}
