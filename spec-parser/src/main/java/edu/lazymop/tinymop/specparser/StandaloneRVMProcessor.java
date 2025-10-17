package edu.lazymop.tinymop.specparser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import com.runtimeverification.rvmonitor.java.rvj.RVMErrorChecker;
import com.runtimeverification.rvmonitor.java.rvj.logicclient.LogicRepositoryData;
import com.runtimeverification.rvmonitor.java.rvj.output.EnableSet;
import com.runtimeverification.rvmonitor.java.rvj.output.RVMOutputCode;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.RVMSpecFile;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.Formula;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.PropertyAndHandlers;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameterSet;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMonitorSpec;
import com.runtimeverification.rvmonitor.logicpluginshells.LogicPluginShellFactory;
import com.runtimeverification.rvmonitor.logicpluginshells.LogicPluginShellResult;
import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.PropertyType;
import com.runtimeverification.rvmonitor.logicrepository.plugins.LogicPlugin;
import com.runtimeverification.rvmonitor.logicrepository.plugins.ere.EREPlugin;
import com.runtimeverification.rvmonitor.logicrepository.plugins.fsm.FSMPlugin;
import com.runtimeverification.rvmonitor.logicrepository.plugins.ltl.LTLPlugin;
import com.runtimeverification.rvmonitor.util.RVMException;
import com.runtimeverification.rvmonitor.util.Tool;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerationUtil;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerator;
import edu.lazymop.util.Logger;
import edu.lazymop.util.Writer;

/**
 * RVMProcessor code from rv-monitor, except we don't need all the client-server architecture.
 */
public class StandaloneRVMProcessor {

    private static final Logger LOGGER = Logger.getGlobal();

    private boolean verbose;

    private String name;

    private String logic;

    private String transitionTable;

    private LogicRepositoryType monitorData;
    private RVMSpecFile rvmSpecFile;

    private List<EnableSet> enableSets;

    /**
     * Creates a processor for specs that is independent of the XML server architecture that JavaMOP hard-wires.
     *
     * @param name The name of the spec.
     * @param verbose Whether verbose mode is turned on.
     */
    public StandaloneRVMProcessor(String name, boolean verbose) {
        this.name = name;
        this.verbose = verbose;
        transitionTable = "";
        enableSets = new ArrayList<>();
    }

    /**
     * Print a data about intermediate spec processing.
     *
     * @param message A string describing the data.
     * @param data The data to be printed.
     */
    public void log(String message, Object data) {
        if (this.verbose) {
            LOGGER.log(Level.INFO, "\n== [open] " + message + " ==\n");
            LOGGER.log(Level.INFO, data.toString() + "\n");
            LOGGER.log(Level.INFO, "== [close] " + message + " ==\n");
        }
    }

    /**
     * The method that is called to process the RVMSpec internal file that the JavaMOP frontend produces.
     *
     * @param rvmSpecFile A RVMSpec internal file that the JavaMOP frontend produced.
     * @return A String representation of the result of processing rvmSpecFile.
     * @throws RVMException Thrown if something goes wrong within this processor.
     * @throws LogicException Thrown if something goes wrong when invoking the logic plugins.
     */
    public String process(RVMSpecFile rvmSpecFile) throws RVMException, LogicException {
        this.rvmSpecFile = rvmSpecFile;
        for (RVMonitorSpec rvmSpec : rvmSpecFile.getSpecs()) {
            enableSets.add(new EnableSet(rvmSpec.getEvents(), rvmSpec.getParameters()));
            for (PropertyAndHandlers prop : rvmSpec.getPropertiesAndHandlers()) {
                LogicRepositoryType logicOutput = process(rvmSpec, prop);
                monitorData = logicOutput;
                log("result from logic plugin(s)", logicOutput);
                // get the monitor from the logic shell
                LogicPluginShellResult logicShellOutput =
                        LogicPluginShellFactory.process(logicOutput, rvmSpec.getEventStr(), "java");
                prop.setLogicShellOutput(logicShellOutput);
                log("result from logic shell", logicShellOutput);
                String stateDeclaration = logicShellOutput.properties.getProperty("state declaration");
                transitionTable = stateDeclaration;
                log("result from logic shell(state declaration)", stateDeclaration);
                log("result from logic shell(spectype)", prop.getProperty().getType());
            }
        }

        // Check for errors in the spec *after* updating the props
        for (RVMonitorSpec rvmSpec : rvmSpecFile.getSpecs()) {
            RVMErrorChecker.verify(rvmSpec);
        }

        // Generate output code
        String result = (new RVMOutputCode(name, rvmSpecFile)).toString();

        List<PropertyAndHandlers> propertiesAndHandlers = rvmSpecFile.getSpecs().get(0).getPropertiesAndHandlers();
        if (propertiesAndHandlers.size() > 0) {
            logic = propertiesAndHandlers.get(0).getProperty().getType();
            MonitorGenerationUtil monitorGenerationUtil = new MonitorGenerationUtil(name, monitorData);
            if (verbose) {
                // write the enableSets to a file on disc
                if (enableSets.size() == 1) {
                    EnableSet enableSet = enableSets.get(0);
                    String actualEnableSets = monitorGenerationUtil.getEnableSets().split(System.lineSeparator())[1];
                    Map<String, RVMParameterSet> propertyEnables = enableSet.parseSets(actualEnableSets);
                    monitorGenerationUtil.setEnableSets(propertyEnables.toString());
                }
                Writer.persistEnableSets(name, monitorGenerationUtil.getEnableSets());
            }
            MonitorGenerator generator = new MonitorGenerator(monitorGenerationUtil, "/tmp/monitor.dot");
        } else {
            logic = "raw";
            MonitorGenerationUtil monitorGenerationUtil = new MonitorGenerationUtil(name, rvmSpecFile);
        }

        // Do indentation
        result = Tool.changeIndentation(result, "", "\t");

        return result;
    }

    /**
     * Produces data intermediate RVMonitorSpec and the property being processed.
     *
     * @param rvmSpec Internal data that is produced as part of our own processing.
     * @param prop The property (representing a JavaMOP spec).
     * @return An internal data representation (LogicRepositoryType).
     * @throws RVMException Thrown if something goes wrong within this processor.
     * @throws LogicException Thrown if something goes wrong when invoking the logic plugins.
     */
    public static LogicRepositoryType process(RVMonitorSpec rvmSpec, PropertyAndHandlers prop)
            throws RVMException, LogicException {
        if (rvmSpec == null || prop == null) {
            throw new RVMException("No annotation specified");
        }

        LogicRepositoryType logicInputXML = getLogicRepositoryType(rvmSpec, prop);

        LogicRepositoryData logicInputData = new LogicRepositoryData(logicInputXML);
        String logicName = null;
        if (logicInputXML.getProperty() != null) {
            logicName = logicInputXML.getProperty().getLogic().toLowerCase();
        }
        LogicRepositoryType logicOutputXML =  process(logicName, logicInputData);
        return logicOutputXML;
    }


    /**
     * More processing of internal data, this time to resolve the spec formalism to the monitor formalism.
     *
     * @param logicName The spec formalism
     * @param logicInputData Initial data about the spec that is known from the spec alone.
     * @return An internal data representation (LogicRepositoryType).
     * @throws RVMException Thrown if something goes wrong within this processor.
     * @throws LogicException Thrown if something goes wrong when invoking the logic plugins.
     */
    public static LogicRepositoryType process(String logicName, LogicRepositoryData logicInputData)
            throws RVMException, LogicException {
        ByteArrayOutputStream logicOutputOutputStream = null;
        LogicRepositoryData logicOutputData = null;
        LogicRepositoryType logicOutputXML = null;

        if (logicName == null || logicName.length() == 0) {
            throw new RVMException("no logic names");
        }

        if (logicOutputOutputStream == null) {
            LogicPlugin plugin = getPlugin(logicName);
            logicOutputOutputStream = plugin.process(logicInputData.getInputStream());
            logicOutputData = new LogicRepositoryData(logicOutputOutputStream);
            logicOutputXML = logicOutputData.getXML();
        }

        if (logicOutputOutputStream != null) {
            if (logicOutputXML.getMessage().contains("done")) {
                return logicOutputXML;
            } else {
                if (logicOutputXML.getProperty() == null) {
                    throw new LogicException("Wrong Logic Plugin Result from " + logicName + " Logic Plugin");
                }
                String logic = logicOutputXML.getProperty().getLogic();
                return process(logic, logicOutputData);
            }
        }

        return logicOutputXML;
    }

    private static LogicRepositoryType getLogicRepositoryType(RVMonitorSpec rvmSpec, PropertyAndHandlers prop) {
        StringBuilder categories = new StringBuilder();
        for (String key : prop.getHandlers().keySet()) {
            categories.append(" ").append(key);
        }
        categories = new StringBuilder(categories.toString().trim());

        String formula = ((Formula) prop.getProperty()).getFormula().trim();
        PropertyType logicProperty = new PropertyType();
        logicProperty.setFormula(formula);
        logicProperty.setLogic(prop.getProperty().getType());

        LogicRepositoryType logicInputXML = new LogicRepositoryType();
        logicInputXML.setClient("RVMonitor");
        logicInputXML.setEvents(rvmSpec.getEventStr());
        logicInputXML.setCategories(categories.toString());
        logicInputXML.setProperty(logicProperty);
        return logicInputXML;
    }

    private static LogicPlugin getPlugin(String logicName) throws RVMException {
        LogicPlugin plugin;
        switch (logicName) {
            case "ltl":
                plugin = new LTLPlugin();
                break;
            case "ere":
                plugin = new EREPlugin();
                break;
            case "fsm":
                plugin = new FSMPlugin();
                break;
            default:
                throw new RVMException("No logic plugin found for: " + logicName);
        }
        return plugin;
    }

    public String getName() {
        return name;
    }

    public String getLogic() {
        return logic;
    }

    public String getTransitionTable() {
        return transitionTable;
    }

    public LogicRepositoryType getMonitorData() {
        return monitorData;
    }

    public RVMSpecFile getRvmSpecFile() {
        return rvmSpecFile;
    }

    public List<EnableSet> getEnableSets() {
        return enableSets;
    }
}
