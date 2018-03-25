import java.io.*;
import java.util.*;

public class Kernel extends Thread {
  // The number of virtual pages must be fixed at 63 due to dependencies in the GUI
  private static int numberOfVirtualPages = 63;

  private String logFilePath = null;
  private static final String lineSeparator = System.getProperty("line.separator");

  private String commandFilePath;
  private String configFilePath;

  private ControlPanel controlPanel;

  private ArrayList<Page> memList = new ArrayList<>();

  private ArrayList<Command> instructList = new ArrayList<>();

  private boolean doStdoutLog = false;
  private boolean doFileLog = false;

  public int commandsCounter;
  public int numberOfCommands;

  public long block = (int) Math.pow(2, 12);
  public static byte addressRadix = 10;

  public void init(String commandFilePath, String configFilePath) {
    this.commandFilePath = commandFilePath;
    this.configFilePath = configFilePath;

    File commandFile;

    String line;
    String tmp = null;
    String command = "";
    byte R = 0;
    byte M = 0;

    int i, j;
    long low, high;

    int id;
    int physical;
    int inMemTime;
    int lastTouchTime;
    double power;

    int physicalCount = 0;
    int mapCount = 0;

    long address;
    long addressLimit = (block * numberOfVirtualPages + 1) - 1;

    if (configFilePath != null) {
      commandFile = new File(configFilePath);
      try {
        DataInputStream in = new DataInputStream(new FileInputStream(commandFile));
        while ((line = in.readLine()) != null) {
          if (line.startsWith("numpages")) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
              tmp = st.nextToken();
              numberOfVirtualPages = Common.s2i(st.nextToken()) - 1;
              if (numberOfVirtualPages < 2 || numberOfVirtualPages > 63) {
                System.out.println("MemoryManagement: numpages out of bounds.");
                System.exit(-1);
              }
              addressLimit = (block * numberOfVirtualPages + 1) - 1;
            }
          }
        }
        in.close();
      } catch (IOException e) { /* Handle exceptions */ }

      for (i = 0; i <= numberOfVirtualPages; i++) {
        high = (block * (i + 1)) - 1;
        low = block * i;
        memList.add(new Page(i, -1, R, M, 0, 0, high, low));
      }

      try {
        DataInputStream in = new DataInputStream(new FileInputStream(commandFile));
        while ((line = in.readLine()) != null)

        {
          if (line.startsWith("memset")) {
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            while (st.hasMoreTokens()) {
              id = Common.s2i(st.nextToken());
              tmp = st.nextToken();

              if (tmp.startsWith("x")) {
                physical = -1;
              } else {
                physical = Common.s2i(tmp);
              }

              if ((0 > id || id > numberOfVirtualPages) || (-1 > physical || physical > ((numberOfVirtualPages - 1) / 2))) {
                System.out.println("MemoryManagement: Invalid page value in " + configFilePath);
                System.exit(-1);
              }

              R = Common.s2b(st.nextToken());

              if (R < 0 || R > 1) {
                System.out.println("MemoryManagement: Invalid R value in " + configFilePath);
                System.exit(-1);
              }

              M = Common.s2b(st.nextToken());

              if (M < 0 || M > 1) {
                System.out.println("MemoryManagement: Invalid M value in " + configFilePath);
                System.exit(-1);
              }

              inMemTime = Common.s2i(st.nextToken());

              if (inMemTime < 0) {
                System.out.println("MemoryManagement: Invalid inMemTime in " + configFilePath);
                System.exit(-1);
              }

              lastTouchTime = Common.s2i(st.nextToken());

              if (lastTouchTime < 0) {
                System.out.println("MemoryManagement: Invalid lastTouchTime in " + configFilePath);
                System.exit(-1);
              }

              Page page = memList.get(id);
              page.physical = physical;
              page.R = R;
              page.M = M;
              page.inMemTime = inMemTime;
              page.lastTouchTime = lastTouchTime;
            }
          }
          if (line.startsWith("enable_logging")) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
              if (st.nextToken().startsWith("true")) {
                doStdoutLog = true;
              }
            }
          }
          if (line.startsWith("log_file")) {
            StringTokenizer st = new StringTokenizer(line);

            while (st.hasMoreTokens()) {
              tmp = st.nextToken();
            }

            if (tmp.startsWith("log_file")) {
              doFileLog = false;
              logFilePath = "tracefile";
            } else {
              doFileLog = true;
              doStdoutLog = false;
              logFilePath = tmp;
            }
          }
          if (line.startsWith("pagesize")) {
            StringTokenizer st = new StringTokenizer(line);

            while (st.hasMoreTokens()) {
              st.nextToken();
              tmp = st.nextToken();

              if (tmp.startsWith("power")) {
                power = Double.parseDouble(st.nextToken());
                block = (int) Math.pow(2, power);
              } else {
                block = Long.parseLong(tmp, 10);
              }

              addressLimit = (block * numberOfVirtualPages + 1) - 1;
            }

            if (block < 64 || block > Math.pow(2, 26)) {
              System.out.println("MemoryManagement: pageSize is out of bounds");
              System.exit(-1);
            }

            for (i = 0; i <= numberOfVirtualPages; i++) {
              Page page = memList.get(i);
              page.high = (block * (i + 1)) - 1;
              page.low = block * i;
            }
          }
          if (line.startsWith("addressRadix")) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
              st.nextToken();
              tmp = st.nextToken();
              addressRadix = Byte.parseByte(tmp);
              if (addressRadix < 0 || addressRadix > 20) {
                System.out.println("MemoryManagement: addressRadix out of bounds.");
                System.exit(-1);
              }
            }
          }
        }
        in.close();
      } catch (IOException e) { /* Handle exceptions */ }
    }
    commandFile = new File(commandFilePath);
    try {
      DataInputStream in = new DataInputStream(new FileInputStream(commandFile));
      while ((line = in.readLine()) != null) {
        if (line.startsWith("READ") || line.startsWith("WRITE")) {
          if (line.startsWith("READ")) {
            command = "READ";
          }
          if (line.startsWith("WRITE")) {
            command = "WRITE";
          }
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          tmp = st.nextToken();
          if (tmp.startsWith("random")) {
            instructList.add(new Command(command, Common.randomLong(addressLimit)));
          } else {
            if (tmp.startsWith("bin")) {
              address = Long.parseLong(st.nextToken(), 2);
            } else if (tmp.startsWith("oct")) {
              address = Long.parseLong(st.nextToken(), 8);
            } else if (tmp.startsWith("hex")) {
              address = Long.parseLong(st.nextToken(), 16);
            } else {
              address = Long.parseLong(tmp);
            }
            if (0 > address || address > addressLimit) {
              System.out.println("MemoryManagement: " + address + ", Address out of range in " + commandFilePath);
              System.exit(-1);
            }
            instructList.add(new Command(command, address));
          }
        }
      }
      in.close();
    } catch (IOException e) { /* Handle exceptions */ }
    numberOfCommands = instructList.size();
    if (numberOfCommands < 1) {
      System.out.println("MemoryManagement: no instructions present for execution.");
      System.exit(-1);
    }
    if (doFileLog) {
      File trace = new File(logFilePath);
      trace.delete();
    }
    commandsCounter = 0;
    for (i = 0; i < numberOfVirtualPages; i++) {
      Page page = memList.get(i);
      if (page.physical != -1) {
        mapCount++;
      }
      for (j = 0; j < numberOfVirtualPages; j++) {
        Page tmp_page = memList.get(j);
        if (tmp_page.physical == page.physical && page.physical >= 0) {
          physicalCount++;
        }
      }
      if (physicalCount > 1) {
        System.out.println("MemoryManagement: Duplicate physical page's in " + configFilePath);
        System.exit(-1);
      }
      physicalCount = 0;
    }
    if (mapCount < (numberOfVirtualPages + 1) / 2) {
      for (i = 0; i < numberOfVirtualPages; i++) {
        Page page = memList.get(i);
        if (page.physical == -1 && mapCount < (numberOfVirtualPages + 1) / 2) {
          page.physical = i;
          mapCount++;
        }
      }
    }
    for (i = 0; i < numberOfVirtualPages; i++) {
      Page page = memList.get(i);
      if (page.physical == -1) {
        controlPanel.removePhysicalPage(i);
      } else {
        controlPanel.addPhysicalPage(i, page.physical);
      }
    }
    for (i = 0; i < instructList.size(); i++) {
      high = block * numberOfVirtualPages;
      Command instruct = instructList.get(i);
      if (instruct.address < 0 || instruct.address > high) {
        System.out.println("MemoryManagement: Command (" + instruct.text + " " + instruct.address + ") out of bounds.");
        System.exit(-1);
      }
    }
  }

  public void setControlPanel(ControlPanel newControlPanel) {
    controlPanel = newControlPanel;
  }

  // TODO: This function is part of future huge refactor of GUI
  // TODO: It called 64 times
  public void getPage(int pageNum) {
    Page page = memList.get(pageNum);
    controlPanel.paintPage(page);
  }

  private void printToLogFile(String message) {
    String line;
    String temp = "";

    File trace = new File(logFilePath);
    if (trace.exists()) {
      try {
        DataInputStream in = new DataInputStream(new FileInputStream(logFilePath));
        while ((line = in.readLine()) != null) {
          temp = temp + line + lineSeparator;
        }
        in.close();
      } catch (IOException e) {
        /* Do nothing */
      }
    }
    try {
      PrintStream out = new PrintStream(new FileOutputStream(logFilePath));
      out.print(temp);
      out.print(message);
      out.close();
    } catch (IOException e) {
      /* Do nothing */
    }
  }

  // Execute all commands step by step with delay in 2 seconds from commandFile
  public void run() {
    step();
    while (commandsCounter != numberOfCommands) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        /* Do nothing */
      }
      step();
    }
  }

  public void step() {
    int i;

    Command command = instructList.get(commandsCounter);

    controlPanel.instructionValueLabel.setText(command.text);
    controlPanel.addressValueLabel.setText(Long.toString(command.address, addressRadix));
    getPage(Virtual2Physical.pageNum(command.address, numberOfVirtualPages, block));

    if (controlPanel.pageFaultValueLabel.getText().equals("YES")) {
      controlPanel.pageFaultValueLabel.setText("NO");
    }

    if (command.text.startsWith("READ")) {
      Page page = memList.get(Virtual2Physical.pageNum(command.address, numberOfVirtualPages, block));
      if (page.physical == -1) {
        if (doFileLog) {
          printToLogFile("READ " + Long.toString(command.address, addressRadix) + " ... page fault");
        }
        if (doStdoutLog) {
          System.out.println("READ " + Long.toString(command.address, addressRadix) + " ... page fault");
        }
        PageFault.replacePage(memList, Virtual2Physical.pageNum(command.address, numberOfVirtualPages, block), controlPanel);
        controlPanel.pageFaultValueLabel.setText("YES");
      } else {
        page.R = 1;
        page.lastTouchTime = 0;
        if (doFileLog) {
          printToLogFile("READ " + Long.toString(command.address, addressRadix) + " ... okay");
        }
        if (doStdoutLog) {
          System.out.println("READ " + Long.toString(command.address, addressRadix) + " ... okay");
        }
      }
    }
    if (command.text.startsWith("WRITE")) {
      Page page = memList.get(Virtual2Physical.pageNum(command.address, numberOfVirtualPages, block));
      if (page.physical == -1) {
        if (doFileLog) {
          printToLogFile("WRITE " + Long.toString(command.address, addressRadix) + " ... page fault");
        }
        if (doStdoutLog) {
          System.out.println("WRITE " + Long.toString(command.address, addressRadix) + " ... page fault");
        }
        PageFault.replacePage(memList, Virtual2Physical.pageNum(command.address, numberOfVirtualPages, block), controlPanel);
        controlPanel.pageFaultValueLabel.setText("YES");
      } else {
        page.M = 1;
        page.lastTouchTime = 0;
        if (doFileLog) {
          printToLogFile("WRITE " + Long.toString(command.address, addressRadix) + " ... okay");
        }
        if (doStdoutLog) {
          System.out.println("WRITE " + Long.toString(command.address, addressRadix) + " ... okay");
        }
      }
    }
    for (i = 0; i < numberOfVirtualPages; i++) {
      Page page = memList.get(i);
      if (page.R == 1 && page.lastTouchTime == 10) {
        page.R = 0;
      }
      if (page.physical != -1) {
        page.inMemTime = page.inMemTime + 10;
        page.lastTouchTime = page.lastTouchTime + 10;
      }
    }
    commandsCounter++;
    controlPanel.timeValueLabel.setText(Integer.toString(commandsCounter * 10) + " (ns)");
  }

  public void reset() {
    memList.clear();
    instructList.clear();

    controlPanel.statusValueLabel.setText("STOP");
    controlPanel.timeValueLabel.setText("0");
    controlPanel.instructionValueLabel.setText("NONE");
    controlPanel.addressValueLabel.setText("NULL");
    controlPanel.pageFaultValueLabel.setText("NO");
    controlPanel.virtualPageValueLabel.setText("x");
    controlPanel.physicalPageValueLabel.setText("0");
    controlPanel.RValueLabel.setText("0");
    controlPanel.MValueLabel.setText("0");
    controlPanel.inMemTimeValueLabel.setText("0");
    controlPanel.lastTouchTimeValueLabel.setText("0");
    controlPanel.lowValueLabel.setText("0");
    controlPanel.highValueLabel.setText("0");

    init(commandFilePath, configFilePath);
  }
}
