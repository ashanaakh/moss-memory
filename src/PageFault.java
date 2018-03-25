import java.util.ArrayList;

/**
 * Least Recently Used (LRU) Page Replacement Algorithm
 * <p>
 * This PageFault file is an example of the FIFO Page Replacement
 * <p>
 * Algorithm as described in the Memory Management section.
 */

public class PageFault {

  /**
   * The page replacement algorithm for the memory management simulator.
   * This method gets called whenever a page needs to be replaced.
   * <p>
   * The page replacement algorithm included with the simulator is
   * FIFO (first-in first-out).  A while or for loop should be used
   * to search through the current memory contents for a candidate
   * replacement page.  In the case of FIFO the while loop is used
   * to find the proper page while making sure that virtualPageNum is
   * not exceeded.
   * <pre>
   *   Page page = mem.get(oldestPage)
   * </pre>
   * This line brings the contents of the Page at oldestPage (a
   * specified integer) from the mem vector into the page object.
   * Next recall the contents of the target page, replacePageNum.
   * Set the physical memory address of the page to be added equal
   * to the page to be removed.
   * <pre>
   *   controlPanel.removePhysicalPage(oldestPage)
   * </pre>
   * Once a page is removed from memory it must also be reflected
   * graphically.  This line does so by removing the physical page
   * at the oldestPage value.  The page which will be added into
   * memory must also be displayed through the addPhysicalPage
   * function call.  One must also remember to reset the values of
   * the page which has just been removed from memory.
   *
   * @param memory         is the vector which contains the contents of the pages
   *                       in memory being simulated.  mem should be searched to find the
   *                       proper page to remove, and modified to reflect any changes.
   * @param pageToLoadIndex is the requested page which caused the
   *                       page fault.
   * @param controlPanel   represents the graphical element of the
   *                       simulator, and allows one to modify the current display.
   */

  public static void replacePage(ArrayList<Page> memory, int pageToLoadIndex, ControlPanel controlPanel) {

    Page pageToLoad = memory.get(pageToLoadIndex);

    // By default first page in memory will be least recently used
    Page leastRecentlyUsed = memory.get(0);

    pageToLoad.R = 1;
    // TODO: Fix pageToLoad.M

    // Chose Page with min usedAtStep field
    for (Page p : memory) {
      if (p.lastTouchTime > leastRecentlyUsed.lastTouchTime) {
        leastRecentlyUsed = p;
      }
    }

    int leastRecentlyUsedIndex = memory.indexOf(leastRecentlyUsed);
    controlPanel.removePhysicalPage(leastRecentlyUsedIndex);

    pageToLoad.physical = leastRecentlyUsed.physical;

    leastRecentlyUsed.clear();

    controlPanel.addPhysicalPage(pageToLoadIndex, pageToLoad.physical);
  }
}
