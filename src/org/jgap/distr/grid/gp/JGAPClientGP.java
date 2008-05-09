/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.distr.grid.gp;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;
import org.homedns.dade.jcgrid.client.*;
import org.homedns.dade.jcgrid.cmd.*;
import org.homedns.dade.jcgrid.message.*;
import org.jgap.*;
import org.jgap.distr.*;
import org.jgap.distr.grid.*;
import org.jgap.distr.grid.common.*;
import org.jgap.distr.grid.util.*;
import org.jgap.gp.*;
import org.jgap.gp.impl.*;
import org.jgap.util.*;

import com.thoughtworks.xstream.*;

/**
 * A client defines work for the grid and sends it to the JGAPServer.
 * Use this class as base class for your grid client implementations.
 *
 * @author Klaus Meffert
 * @since 3.2
 */
public class JGAPClientGP
    extends Thread {
  /**@todo in dateiname requester/worker kodieren*/
  /**@todo small, medium, large work requests*/
  /**@todo re-evaluate each result on behalf of another worker*/
  /**@todo versionsnummer in filename rein, mit der file erzeugt*/
  /**@todo remove old requests from online store auotmatically*/
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.12 $";

  public static final String MODULE_CS = "CS";

  public static final String CLIENT_DATABASE = "clientdb0.jgap";

  /**@todo das ist nicht module, sondern sender-receiver*/

  public static final String MODULE_SC = "SC";

  public static final String MODULE_SW = "SW";

  public static final String MODULE_WS = "WS";

  public static final String MODULE_ANY = "*";

  public static final String CONTEXT_WORK_REQUEST = "WREQ";

  public static final String CONTEXT_WORK_RESULT = "WRES";

  public static final String CONTEXT_ANY = "W*";

  public static final String CONTEXT_ID_EMPTY = "0";

  public static final String CONTEXT_ID_ANY = "*";

  public static final int TIMEOUT_SECONDS = 20;

  public static final int WAITTIME_SECONDS = 5;

//  private transient Logger log = Logger.getLogger(getClass());
  private static transient org.apache.log4j.Logger log
      = org.apache.log4j.Logger.getLogger(JGAPClientGP.class);

  protected GridNodeClientConfig m_gridconfig;

  protected JGAPRequestGP m_workReq;

  private IGridClientMediator m_gcmed;

  private IGridConfigurationGP m_gridConfig;

  /**
   * Is the client operating in a WAN or in a LAN?
   * TRUE:  WAN --> Do not use JCGrid architecture
   * FALSE: LAN --> Do use JCGrid architecture
   */
  private boolean m_WANMode;

  /**
   * Only received results or send requests beforehand?
   */
  private boolean m_receiveOnly;

  private boolean m_list;

  private String m_workDir;

  private String m_runID;

  private boolean m_endless;

  private ClientStatus m_objects;

  private PersistableObject m_persister;

  private int m_requestIdx;

  private boolean m_no_comm;

  private boolean m_no_evolution;

  public JGAPClientGP(GridNodeClientConfig a_gridconfig,
                      String a_clientClassName,
                      boolean a_WANMode,
                      boolean a_receiveOnly, boolean a_list, boolean a_no_comm,
                      boolean a_no_evolution, boolean a_endless)
      throws Exception {
    this(null, a_gridconfig, a_clientClassName, a_WANMode, a_receiveOnly,
         a_list, a_no_comm, a_no_evolution, a_endless);
    m_gcmed = new DummyGridClientMediator(m_gridconfig);
  }

  public JGAPClientGP(IGridClientMediator a_gcmed,
                      GridNodeClientConfig a_gridconfig,
                      String a_clientClassName, boolean a_WANMode,
                      boolean a_receiveOnly, boolean a_list, boolean a_no_comm,
                      boolean a_no_evolution, boolean a_endless)
      throws Exception {
    m_runID = getRunID();
    log.info("ID of this run: " + m_runID);
    if (a_clientClassName == null || a_clientClassName.length() < 1) {
      throw new IllegalArgumentException(
          "Please specify a class name of the configuration!");
    }
    m_WANMode = a_WANMode;
    m_receiveOnly = a_receiveOnly;
    if (m_receiveOnly) {
      log.info("Only receive results");
    }
    m_list = a_list;
    if (m_list) {
      log.info("List requests and results");
    }
    m_endless = a_endless;
    if (m_endless) {
      log.info("Endless run");
    }
    m_no_comm = a_no_comm;
    if (m_no_comm) {
      log.info("Don't send or receive anything");
    }
    m_no_evolution = a_no_evolution;
    if (m_no_evolution) {
      log.info("Don't execute genetic evolution");
    }
    m_gridconfig = a_gridconfig;
    Class client = Class.forName(a_clientClassName);
    m_gridConfig = (IGridConfigurationGP) client.getConstructor(new
        Class[] {}).newInstance(new Object[] {});
    m_gridConfig.initialize(m_gridconfig);
    if (m_gridConfig.getClientFeedback() == null) {
      m_gridConfig.setClientFeedback(new NullClientFeedbackGP());
    }
    // Setup work request.
    // -------------------
    JGAPRequestGP req = new JGAPRequestGP(m_gridconfig.getSessionName(),
        m_runID + "_" + m_requestIdx,
        0, m_gridConfig);
    m_requestIdx++;
    req.setWorkerReturnStrategy(m_gridConfig.getWorkerReturnStrategy());
    req.setGenotypeInitializer(m_gridConfig.getGenotypeInitializer());
    req.setEvolveStrategy(m_gridConfig.getWorkerEvolveStrategy());
    MasterInfo requester = new MasterInfo(true);
    req.setRequesterInfo(requester);
    req.setRequestDate(DateKit.now());
    // If evolution takes place on client only:
    // ----------------------------------------
//    req.setEvolveStrategy(null);
    //
    setWorkRequest(req);
    m_gcmed = a_gcmed;
    init();
  }

  private void init()
      throws Exception {
    String workDir = FileKit.getCurrentDir() + "/work/" + "storage";
    workDir = FileKit.getConformPath(workDir);
    // Try to load previous object information.
    // ----------------------------------------
    File f = new File(workDir, CLIENT_DATABASE);
    m_persister = new PersistableObject(f);
    m_objects = (ClientStatus) m_persister.load();
    if (m_objects == null) {
      m_objects = new ClientStatus();
      m_persister.setObject(m_objects);
    }
  }

  /**
   * @return the most possibly unique ID of a single program execution
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected String getRunID() {
    if (m_runID == null) {
      return "RJGrid" + DateKit.getNowAsString();
    }
    else {
      return m_runID;
    }
  }

  public void setWorkRequest(JGAPRequestGP a_request) {
    m_workReq = a_request;
  }

  /**
   * Called at start of run().
   * Override in sub classes if needed.
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void onBeginOfRunning()
      throws Exception {
  }

  /**
   * Called in run() before sending work requests.
   * Override in sub classes if needed.
   *
   * @param a_workRequests work requests pending to be sent
   *
   * @return true: do send work requests, false: don't send any work request
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected boolean beforeSendWorkRequests(JGAPRequestGP[] a_workRequests)
      throws Exception {
    return true;
  }

  /**
   * Called in run() before generating work requests for sending.
   * Override in sub classes if needed.
   *
   * @return true: do generate work requests, false: don't generate any work request
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected boolean beforeGenerateWorkRequests()
      throws Exception {
    return true;
  }

  /**
   * Called in run() after sending work requests successfully.
   * Override in sub classes if needed.
   *
   * @param a_workRequests the sent requests
   * @return true: process further, false: stop processing the rest
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected boolean afterSendWorkRequests(JGAPRequestGP[] a_workRequests)
      throws Exception {
    return true;
  }

  protected void errorOnSendWorkRequests(Throwable uex,
      JGAPRequestGP[] a_workRequests)
      throws Exception {
  }

  /**
   * Called in run() before one evolution step is executed.
   * Override in sub classes if needed.
   *
   * @param a_gcmed the GridClient mediator
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void beforeEvolve(IGridClientMediator a_gcmed)
      throws Exception {
  }

  /**
   * Called in run() after one evolution step is executed.
   * Override in sub classes if needed.
   *
   * @param a_gcmed the GridClient mediator
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void afterEvolve(IGridClientMediator a_gcmed)
      throws Exception {
  }

  /**
   * Called after stopping the client in run().
   * Override in sub classes if needed.
   *
   * @param a_t null if no error occured on stopping, otherwise exception object
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void afterStopped(Throwable a_t)
      throws Exception {
  }

  /**
   * Called in run() in case of any unhandled error.
   * Override in sub classes if needed.
   *
   * @param a_ex exception object expressing the error
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void onError(Exception a_ex) throws Exception {
  }

  /**
   * Called in run() on error when receiving work results.
   * Override in sub classes if needed.
   *
   * @param a_workRequests for which to receive results
   * @param a_ex Exception occured
   * @throws Exception rethrow an unhandled exception!
   *
   * @author Klaus Meffert
   * @since 3.3.4
   */
  protected void onErrorReceiveWorkResults(JGAPRequestGP[] a_workRequests,
      Exception a_ex)
      throws Exception {
  }

  /**
   * Threaded: Splits work, sends it to workers and receives computed solutions.
   *
   * @author Klaus Meffert
   * @since 3.01
   */
  public void run() {
    try {
      try {
        // Check for updates.
        // ------------------
        String libDir = "D:\\jgap\robocode\\rjgrid\\lib\\";
//        checkForUpdates("http://www.klaus-meffert.de/", libDir, m_workDir);
      } catch (Exception ex) {
        log.error("Check for updates failed", ex);
      }
      do {
        do {
          try {
            onBeginOfRunning();
            // Show stats about best results for current application.
            // ------------------------------------------------------
            showCurrentResults();
            // Do deferred deletion of results.
            // --------------------------------
            Iterator<String> it = m_objects.getResults().keySet().iterator();
            while (it.hasNext()) {
              String key = it.next();
              String value = (String) m_objects.getResults().get(key);
              if ("delete".equals(value)) {
                m_gcmed.removeMessage(key);
                m_objects.getResults().remove(key);
              }
            }
            try {
              try {
                if (m_list) {
                  // List existing requests and results with extended information.
                  // -------------------------------------------------------------
                  listRequests();
                  listResults();
                }
                if (!m_receiveOnly && !m_no_evolution) {
                  // Initialize evolution.
                  // ---------------------
                  IClientEvolveStrategyGP clientEvolver = m_gridConfig.
                      getClientEvolveStrategy();
                  if (clientEvolver != null) {
                    clientEvolver.initialize(m_gcmed, getConfiguration(),
                        m_gridConfig.getClientFeedback());
                  }
                }
                if (!m_no_evolution) {
                  // Do the evolution.
                  // -----------------
                  beforeEvolve(m_gcmed);
                  evolve(m_gcmed, m_receiveOnly);
                  afterEvolve(m_gcmed);
                }
              } catch (Exception ex) {
                log.error("Error", ex);
                throw ex;
              }
            } finally {
              Throwable t = null;
              try {
                try {
                  m_gcmed.stop();
                } catch (Throwable t1) {
                  t = t1;
                }
              } finally {
                afterStopped(t);
                break;
              }
            }
          } catch (Exception ex1) {
            try {
             onError(ex1);
          } catch (Exception ex) {
              log.fatal("Unpredicted error", ex);
              m_gridConfig.getClientFeedback().error(
                  "Error while doing the work",
                  ex);
              try {
//              m_gcmed.disconnect();
              } catch (Exception ex2) {
                log.warn("Precautios disconnect failed.", ex2);
              }
              sleep(10000);
            }
          }
        } while (true);
        if (!m_endless) {
          break;
        }
        else {
          log.info("Starting again after a short break...");
          sleep(15000);
        }
      } while (true);
    } catch (InterruptedException iex) {
      // Thread interrupted.
      // -------------------
      log.fatal("Thread was interrupted", iex);
      try {
        m_gcmed.disconnect();
      } catch (Exception ex) {
        log.warn("Disconnect after interruption failed", ex);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    log.info("Stopping client");
  }

  protected JGAPRequestGP[] sendWorkRequests(int a_evolutionIndex,
      IClientEvolveStrategyGP evolver, IRequestSplitStrategyGP splitter,
      IClientFeedbackGP feedback)
      throws Exception {
    JGAPRequestGP[] workRequests = null;
    if (beforeGenerateWorkRequests()) {
      log.info("Beginning evolution cycle " + a_evolutionIndex);
      try {
//      m_clientEvolveStrategy.beforeGenerateWorkResults();
        workRequests = evolver.generateWorkRequests(m_workReq, splitter, null);
        feedback.setProgressMaximum(0);
        feedback.setProgressMaximum(workRequests.length - 1);
        for (int i = 0; i < workRequests.length; i++) {
          presetPopulation(workRequests[i]);
        }
        if (beforeSendWorkRequests(workRequests)) {
          /**@todo merge previous results in req.getPopulation()*/
          if (!m_no_comm) {
            try {
              sendWorkRequests(workRequests);
              return workRequests;
            } catch (Exception ex) {
              throw new WorkRequestsSendException(ex, workRequests);
            }
          }
          else {
            return workRequests;
          }
        }
        else {
          return null;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        throw ex;
      }
    }
    else {
      return null;
    }
  }

  protected void sendWorkRequests(JGAPRequestGP[] a_workList)
      throws Exception {
    // Send work requests.
    // -------------------
    for (int i = 0; i < a_workList.length; i++) {
      JGAPRequestGP req = a_workList[i];
      GPPopulation pop = req.getPopulation();
      if (pop == null || pop.isFirstEmpty()) {
        log.debug("Population to send to worker is empty!");
      }
      m_gridConfig.getClientFeedback().sendingFragmentRequest(req);
      MessageContext context = new MessageContext(MODULE_CS,
          CONTEXT_WORK_REQUEST, CONTEXT_ID_EMPTY);
      m_gcmed.send(new GridMessageWorkRequest(req), context, null);
      if (isInterrupted()) {
        break;
      }
    }
  }

  protected void receiveWorkResults(JGAPRequestGP[] workList)
      throws Exception {
    log.info("Receiving work results...");
    IClientFeedbackGP feedback = m_gridConfig.getClientFeedback();
    // Receive work results.
    // ---------------------
    int size;
    if (workList == null) {
      size = -1;
    }
    else {
      size = workList.length;
    }
    if (m_WANMode && size < 1) {
      // First, get a list of all work results.
      // --------------------------------------
      MessageContext context = new MessageContext(MODULE_WS,
          /**@todo later: SC*/
          CONTEXT_WORK_RESULT, CONTEXT_ID_EMPTY);
      List requests = m_gcmed.listResults(context, null, null);
      // Then, iterate over them and receive one after another.
      // ------------------------------------------------------
      int i = 0;
      for (Object request : requests) {
        feedback.setProgressValue(i);
        i++;
        JGAPResultGP result = receiveWorkResult(request, feedback, true);
        if (result != null) {
          IGPProgram best = result.getPopulation().determineFittestProgram();
          log.info("Result received: " +
                   best.getFitnessValue());
          resultReceived(best);
          MasterInfo worker = result.getWorkerInfo();
          if (worker != null) {
            log.info(" Worker IP " + worker.m_IPAddress + ", host " +
                     worker.m_name);
          }
          // Store result to disk.
          // ---------------------
          String filename = "result_" + getRunID() + "_" + result.getID() +
              "_" +
              result.getSessionName() + "_" + result.getChunk();
          writeToFile(best, m_workDir, filename);
          // Now remove the result from the online store.
          // --------------------------------------------
          /**@todo do this here explicitely and not in receiveWorkResult*/
        }
      }
    }
    else {
      for (int i = 0; i < size; i++) {
        feedback.setProgressValue(i + workList.length);
        receiveWorkResult(workList, feedback);
        if (this.isInterrupted()) {
          break;
        }
      }
    }
  }

  private JGAPResultGP receiveWorkResult(Object a_result,
      IClientFeedbackGP feedback, boolean a_remove)
      throws Exception {
    MessageContext context = new MessageContext(MODULE_WS /**@todo later: SC*/,
        CONTEXT_WORK_RESULT, a_result);
    GridMessageWorkResult gmwr = (GridMessageWorkResult) m_gcmed.
        getGridMessage(context, null, TIMEOUT_SECONDS, WAITTIME_SECONDS,
                       a_remove);
    if (gmwr == null) {
      throw new WorkResultNotFoundException();
    }
    else {
      String s = " ";
      if (a_remove) {
        s += "and removed from WAN";
      }
      log.info("Work result received" + s);
    }
    JGAPResultGP workResult = (JGAPResultGP) gmwr.getWorkResult();
    m_gridConfig.getClientEvolveStrategy().resultReceived(workResult);
    int idx = workResult.getChunk();
    // Fire listener.
    // --------------
    feedback.receivedFragmentResult(null, workResult, idx);
    return workResult;
  }

  private JGAPResultGP receiveWorkResult(JGAPRequestGP[] workList,
      IClientFeedbackGP feedback)
      throws Exception {
    /**@todo make this asynchronous with fall-back and reconnect!*/
    MessageContext context = new MessageContext(MODULE_WS /**@todo later: SC*/,
        CONTEXT_WORK_RESULT, CONTEXT_ID_EMPTY);
    GridMessageWorkResult gmwr = (GridMessageWorkResult) m_gcmed.
        getGridMessage(context, null, TIMEOUT_SECONDS, WAITTIME_SECONDS, true);
    if (gmwr == null) {
      throw new NoWorkResultsFoundException();
    }
    else {
      log.info("Work result received!");
    }
    JGAPResultGP workResult = (JGAPResultGP) gmwr.getWorkResult();
    m_gridConfig.getClientEvolveStrategy().resultReceived(workResult);
    int idx = workResult.getChunk();
    // Fire listener.
    // --------------
    JGAPRequestGP req;
    if (workList == null || workList.length < 1) {
      req = null;
    }
    else {
      req = workList[idx];
    }
    feedback.receivedFragmentResult(req, workResult, idx);
    return workResult;
  }

  /**
   * If necessary: override to implement your evolution cycle individually.
   *
   * @param a_gcmed the GridClient mediator
   * @param a_receiveOnly false: Don't send any work requests, just receive
   * results from former evolutions
   *
   * @throws Exception
   */
  protected void evolve(IGridClientMediator a_gcmed, boolean a_receiveOnly)
      throws Exception {
    // Do the complete evolution cycle until end.
    // ------------------------------------------
    IClientFeedbackGP feedback = m_gridConfig.getClientFeedback();
    feedback.beginWork();
    IClientEvolveStrategyGP evolver = m_gridConfig.getClientEvolveStrategy();
    IRequestSplitStrategyGP splitter = m_gridConfig.getRequestSplitStrategy();
    int evolutionIndex = 0;
    do {
      JGAPRequestGP[] workRequests = null;
      boolean deferRequests = false;
      if (!a_receiveOnly) {
        try {
          // Care that not too much work requests are online, do a listing
          // from time to time. If enough requests already there, don't create
          // them any more.
          // -----------------------------------------------------------------
          long lastListing = m_objects.getLastListingRequestsMillis();
          long current = System.currentTimeMillis();
          if (current - lastListing > 60 * 60 * 1) { //60 Seconds * 60 Minutes * 1 Hour
            // Do a listing again after 60 minutes or more.
            // --------------------------------------------
            MessageContext context = new MessageContext(MODULE_CS,
                CONTEXT_WORK_REQUEST, CONTEXT_ID_EMPTY);
            List requests = a_gcmed.listRequests(context, null, null);
            m_objects.setLastListingRequestsMillis(current);
            m_persister.save();
            if (requests != null && requests.size() > 100) {
              deferRequests = true;
              log.info("Deferring creating and sending further requests"
                       + ", maximum reached ("
                       + requests.size() + " found).");
            }
            if (requests != null && requests.size() > 0) {
              // Remove requests from database that are not in list any more.
              // ------------------------------------------------------------
              Map foundKeys = new HashMap();
              Object first = requests.get(0);
              if (String.class.isAssignableFrom(first.getClass())) {
                // Requests of type String can be handled directly.
                // ------------------------------------------------
                for (Object key : requests) {
                  foundKeys.put(key, "");
                }
              }
              else {
                // Requests of type that sub classes have to handle.
                // -------------------------------------------------
                for (Object obj : requests) {
                  String key = getKeyFromObject(obj);
                  if (key != null) {
                    foundKeys.put(key, "");
                  }
                }
              }
              removeEntries(foundKeys, m_objects.getRequests());
            }
          }
          /**@todo do the same for results*/
          if (!deferRequests) {
            workRequests = sendWorkRequests(evolutionIndex, evolver, splitter,
                feedback);
          }
          else {
            // Defer creating and sending additional requests.
            // -----------------------------------------------
          }
        } catch (WorkRequestsSendException wex) {
          errorOnSendWorkRequests(wex.getCause(), wex.getWorkRequests());
//        } catch (UploadFailedException uex) {
//          errorOnSendWorkRequests(uex, null);
//          throw uex;
//        } catch (ListingFailedException lex) {
//          errorOnSendWorkRequests(lex, null);
//          throw lex;
        }
        if (!deferRequests && !afterSendWorkRequests(workRequests)) {
          break;
        }
      }
      if (this.isInterrupted()) {
        break;
      }
      if (!deferRequests && !a_receiveOnly) {
        evolver.afterWorkRequestsSent();
      }
      if (!m_no_comm) {
        try {
          receiveWorkResults(workRequests);
        } catch (Exception ex) {
          onErrorReceiveWorkResults(workRequests, ex);
        }
      }
      if (!a_receiveOnly && !m_no_evolution) {
        evolver.evolve();
        // Fire listener that one evolution cycle is complete.
        // ---------------------------------------------------
        feedback.completeFrame(evolutionIndex);
        evolutionIndex++;
        // Check if evolution is finished.
        // -------------------------------
        if (evolver.isEvolutionFinished(evolutionIndex)) {
          evolver.onFinished();
          break;
        }
      }
    } while (true);
    try {
      a_gcmed.disconnect();
    } catch (Exception ex) {
      log.error("Disconnecting from server failed!", ex);
    }
    m_gridConfig.getClientFeedback().endWork();
  }

  public void start() {
    try {
      m_gridConfig.validate();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    super.start();
  }

  public GPConfiguration getConfiguration() {
    return m_gridConfig.getConfiguration();
  }

  public IGridClientMediator getGridClientMediator() {
    return m_gcmed;
  }

  protected IGridConfigurationGP getGridConfigurationGP() {
    return m_gridConfig;
  }

  /**
   * Writes an object to a local file.
   *
   * @param a_obj the object to persist
   * @param a_dir directory to write the file to
   * @param a_filename name of the file to create
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  public void writeToFile(Object a_obj, String a_dir, String a_filename)
      throws Exception {
    XStream xstream = new XStream();
    File f = new File(a_dir, a_filename);
    FileOutputStream fos = new FileOutputStream(f);
    xstream.toXML(a_obj, fos);
    fos.close();
  }

  public void setWorkDirectory(String a_workDir)
      throws IOException {
    m_workDir = a_workDir;
    FileKit.createDirectory(m_workDir);
    log.info("Work dir: " + m_workDir);
  }

  public String getWorkdirectory() {
    return m_workDir;
  }

  protected void checkForUpdates(String a_URL, String a_libDir,
                                 String a_workDir)
      throws Exception {
    GridKit.updateModuleLibrary(a_URL, "rjgrid", a_libDir, a_workDir);
  }

  /**
   * Override in sub classes: list available requests
   */
  protected void listRequests() {
  }

  /**
   * Override in sub classes: list available results
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void listResults() {
  }

  /**
   * @return false: normal mode, true: do not communicate with the server
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  public boolean isNoCommunication() {
    return m_no_comm;
  }

  /**
   * Starts a client (first parameter: name of specific setup class).
   *
   * @param args String[]
   *
   * @author Klaus Meffert
   * @since 3.01
   */
  public static void main(String[] args) {
    try {
      // Setup logging.
      // --------------
      MainCmd.setUpLog4J("client", true);
      // Command line options.
      // ---------------------
      GridNodeClientConfig config = new GridNodeClientConfig();
      Options options = makeOptions();
      options.addOption("l", true, "LAN or WAN");
//      options.addOption("receive_only", false,
//                        "Only receive results, don't send requests");
//      options.addOption("list", false,
//                        "List requests and results");
//      options.addOption("no_comm", false,
//                        "Don't receive any results, don't send requests");
//      options.addOption("no_evolution", false,
//                        "Don't perform genetic evolution");
//      options.addOption("help", false,
//                        "Display all available options");
      CommandLine cmd = MainCmd.parseCommonOptions(options, config, args);
      printHelp(cmd, options);
      String networkMode = cmd.getOptionValue("l", "LAN");
      boolean inWAN;
      if (networkMode != null && networkMode.equals("LAN")) {
        inWAN = false;
      }
      else {
        inWAN = true;
      }
      if (!cmd.hasOption("config")) {
        System.out.println(
            "Please provide a name of the grid configuration class to use");
        System.out.println("An example class would be "
                           +
                           "examples.grid.fitnessDistributed.GridConfiguration");
        System.exit(1);
      }
//      if (args.length < 2) {
//        System.out.println(
//            "Please provide an application name of the grid (textual identifier");
//        System.exit(1);
//      }
      String clientClassName = cmd.getOptionValue("config");
      boolean receiveOnly = cmd.hasOption("receive_only");
      boolean list = cmd.hasOption("list");
      boolean noComm = cmd.hasOption("no_comm");
      boolean noEvolution = cmd.hasOption("no_evolution");
      boolean endless = cmd.hasOption("endless");
      // Setup and start client.
      // -----------------------
      JGAPClientGP client = new JGAPClientGP(config, clientClassName, inWAN,
          receiveOnly, list, noComm, noEvolution, endless);
      // Start the threaded process.
      // ---------------------------
      client.start();
      client.join();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  protected static void printHelp(CommandLine cmd, Options options) {
    if (cmd.hasOption("help")) {
      System.out.println("");
      System.out.println(" Command line options:");
      System.out.println(" ---------------------\n");
//      Option[] opts = options.
      for (Object opt0 : options.getOptions()) {
        Option opt = (Option) opt0;
        String s = opt.getOpt();
        s = StringKit.fill(s, 20, ' ');
        System.out.println(" " + s + " - " + opt.getDescription());
      }
      System.exit(0);
    }
  }

  protected static Options makeOptions() {
    Options options = new Options();
    options.addOption("no_comm", false,
                      "Don't receive any results, don't send requests");
    options.addOption("no_evolution", false,
                      "Don't perform genetic evolution");
    options.addOption("receive_only", false,
                      "Only receive results, don't send requests");
    options.addOption("endless", false, "Run endlessly");
    options.addOption("config", true, "Grid configuration's class name");
    options.addOption("list", false,
                      "List requests and results");
    options.addOption("help", false,
                      "Display all available options");
    return options;
  }

  protected void removeEntries(Map a_cachedKeys, Map a_foundKeys) {
    Iterator it = a_cachedKeys.keySet().iterator();
    while (it.hasNext()) {
      Object key = it.next();
      if (!a_foundKeys.containsKey(key)) {
        it.remove();
      }
    }
  }

  protected String getKeyFromObject(Object a_obj) {
    return null;
  }

  /**
   * New results has been received. Care that the best of them are stored
   * in case it is a top 3 result.
   *
   * @param a_pop the fittest results received for a work request
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void resultReceived(GPPopulation a_pop)
      throws Exception {
    for (IGPProgram prog : a_pop.getGPPrograms()) {
      if (prog != null) {
        resultReceived(prog);
      }
    }
  }

  /**
   * A new result has been received. Care that it is stored in case it is a top
   * 3 result.
   *
   * @param a_fittest the fittest result received for a work request
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void resultReceived(IGPProgram a_fittest)
      throws Exception {
    /**@todo jeden Worker einer von n (rein logischen) Gruppen zuteilen.
     * Pro logischer Gruppe top n Ergebnisse halten
     */
    try {
      Map<String, List> topAll = m_objects.getTopResults();
      String appid = m_gridConfig.getContext().getAppId();
      List<IGPProgram> topApp = topAll.get(appid);
      if (topApp == null) {
        topApp = new Vector();
        topAll.put(appid, topApp);
      }
      int fitter = 0;
      Iterator<IGPProgram> it = topApp.iterator();
      Object worstEntry = null;
      double worstFitness = -1;
      String norm = a_fittest.toStringNorm(0);
      while (it.hasNext()) {
        IGPProgram prog = (IGPProgram) it.next();
        // Don't allow identical results.
        // ------------------------------
        if (prog.toStringNorm(0).equals(norm)) {
          fitter = 100;
          break;
        }
        double fitness = prog.getFitnessValue();
        if (Math.abs(fitness - a_fittest.getFitnessValue()) < 0.001) {
          fitter = 100;
          break;
        }
        else if (fitness >= a_fittest.getFitnessValue()) {
          fitter++;
        }
        else {
          // Determine the worst entry for later replacement.
          // ------------------------------------------------
          if (worstEntry == null ||
              getConfiguration().getGPFitnessEvaluator().
              isFitter(worstFitness, fitness)) {
            worstEntry = prog;
            worstFitness = fitness;
          }
        }
      }
      if (fitter < 3) { /**@todo make configurable*/
        // Remove worst result yet and add new fit result.
        // -----------------------------------------------
        topApp.remove(worstEntry);
        topApp.add(a_fittest);
        log.info("Added fit program, fitness: " +
                 NumberKit.niceDecimalNumber(a_fittest.getFitnessValue(), 2));
      }
      else {
        log.info("Result not better than top results received");
      }
      m_persister.save();
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  /**
   * Presets initial population to be included for input to workers.
   *
   * @param a_workRequest the work request that is about to be sent.
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.3.3
   */
  protected void presetPopulation(JGAPRequestGP a_workRequest)
      throws Exception {
    /**@todo merge previously stored results with new requests!
     * sometimes preset them as input for worker, sometimes give worker an
     * empty population*/
    RandomGenerator randGen = getConfiguration().getRandomGenerator();
    double d = randGen.nextDouble();
    if (d > 0.2d) {
      Map<String, List> topAll = m_objects.getTopResults();
      String appid = m_gridConfig.getContext().getAppId();
      List<IGPProgram> topApp = topAll.get(appid);
      int added = 0;
      int index = 0;
      GPPopulation pop = a_workRequest.getPopulation();
      IGPProgram[] programs = pop.getGPPrograms();
      while (index < pop.getPopSize() && pop.getGPProgram(index) != null) {
        index++;
      }
      List toAdd = new Vector();
      for (IGPProgram prog : topApp) {
        toAdd.add(prog);
        added++;
        if (added >= 3 || randGen.nextDouble() > 0.7d) {
          break;
        }
      }
      // Now merge old and new programs to one pool.
      // -------------------------------------------
      int len = programs.length;
      if (len > 0) {
        len = 0;
        while (len < programs.length && programs[len] != null) {
          len++;
        }
      }
      IGPProgram[] programsNew = (IGPProgram[]) toAdd.toArray(new IGPProgram[] {});
      int size = len + toAdd.size();
      IGPProgram[] allPrograms = new IGPProgram[size];
      if (len > 0) {
        System.arraycopy(programs, 0, allPrograms, 0, len);
      }
      System.arraycopy(programsNew, 0, allPrograms, len, programsNew.length);
      pop.setGPPrograms(allPrograms);
      log.info("Population preset with " + added + " additional programs");
    }
  }

  protected void showCurrentResults()
      throws Exception {
    /**@todo impl*/
    String appid = m_gridConfig.getContext().getAppId();
    Map<String, List> topAll = m_objects.getTopResults();
    List<IGPProgram> topApp = topAll.get(appid);
    if (topApp != null && topApp.size() > 0) {
      log.info("Top evolved results yet:");
      log.info("------------------------");
      for (IGPProgram prog : topApp) {
        log.info("Fitness " +
                 NumberKit.niceDecimalNumber(prog.getFitnessValue(), 2));
      }
      log.info("");
    }
  }
}
