/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx;

import java.awt.event.*;
import java.awt.*;

/**
 * Class which provides several methods for multithreading and AWT
 */
public class EventQueueHelper {

  private static EventQueue eventQueue;

  /**
   * Event type used for dispatching runnable objects for
   * invokeLater() and invokeAndWait().
   */
  private static class RunnableEvent extends AWTEvent {

    /*package*/ static final int EVENT_ID = AWTEvent.RESERVED_ID_MAX + 1000;
    /*package*/ static final Component target = new RunnableTarget();

    /*package*/ Runnable runnable;

    /**
     * Constructor
     */
    RunnableEvent(Runnable pRunnable) {
      super(target, EVENT_ID);
        runnable = pRunnable;
    }

    // EOC
  }

  /**
   * Invisible target for RunnableEvents
   */
  private static class RunnableTarget extends Component {

    /**
     * Constructor
     */
    RunnableTarget() {
      super();
      enableEvents(RunnableEvent.EVENT_ID);
    }

    /**
     * Handle incoming events
     */
    protected void processEvent(AWTEvent event) {
      if (event instanceof RunnableEvent) {
        RunnableEvent revent = (RunnableEvent)event;
        revent.runnable.run();
      }
    }

    // EOC
  }

  /**
   * Initializer
   */
  private static EventQueue getEventQueue() {

    if (eventQueue!=null) {
      return eventQueue;
    }

    try {
      eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    } catch (Exception e) {
      System.out.println("[Debug]Couldn't locate SystemEventQueue");
    }

    return eventQueue;
  }

  /**
   * Let a Runnable object run on the system event queue
   */
  public static void invokeLater(Runnable runnable) {

    EventQueue systemEventQueue = getEventQueue();

    RunnableEvent event = new RunnableEvent(runnable);

    systemEventQueue.postEvent(event);

   }
}

