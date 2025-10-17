package edu.lazymop.tinymop.specparser.monitoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.runtimeverification.rvmonitor.java.rvj.parser.ast.RVMSpecFile;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;
import edu.lazymop.util.Logger;

public class MonitorGenerationUtil {

    protected static final Logger LOGGER = Logger.getGlobal();

    protected String specName;

    protected String category;

    protected List<String> eventNames;

    protected Set<String> defaultStates;
    protected String startState;
    protected Map<String, List<String>> aliasedStates;

    protected Set<String> states;

    protected Map<String, Map<String, String>> transitions;

    protected String formula;

    private String enableSets;

    /**
     * Create a Monitor object, which hold data that will be used for Monitor code generation for non-raw specs.
     *
     * @param name        The name of the spec that is being processed.
     * @param monitorData The object that holds all the output of RVMonitor.
     */
    public MonitorGenerationUtil(String name, LogicRepositoryType monitorData) {
        this.specName = name;
        this.eventNames = obtainEventNames(monitorData.getEvents());
        this.formula = monitorData.getProperty().getFormula();
        this.category = monitorData.getCategories().trim();// assuming only one category as most specs do today
        this.states = getStateNames(formula);
        this.startState = fetchStartState(monitorData.getMessage());
        this.aliasedStates = fetchAliasedStates(monitorData.getMessage());
        //TODO: log a message when the number of default states is more than 1
        this.defaultStates = obtainDefaultStates(formula, states);
        this.transitions = getTransitionsPerState(formula, states);
        this.enableSets = monitorData.getEnableSets();

        if (this.defaultStates.isEmpty() && !this.states.contains(this.startState)) {
            LOGGER.log(Level.WARNING, "Cannot find start state");
            String potentialStartState = "";
            for (String state : this.states) {
                if (state.contains(this.startState)) {
                    if (!potentialStartState.isEmpty()) {
                        throw new RuntimeException("Multiple potential start states");
                    }
                    potentialStartState = state;
                }
            }

            this.startState = potentialStartState;
        } else if (this.defaultStates.size() > 1) {
            throw new RuntimeException("Multiple default states");
        }
    }

    /**
     * Create a Monitor object, which hold data that will be used for Monitor code generation for raw specs.
     *
     * @param name        The name of the spec that is being processed.
     * @param rvmSpecFile The internal specFile object that RVMonitor uses.
     */
    public MonitorGenerationUtil(String name, RVMSpecFile rvmSpecFile) {
        this.specName = name;
        this.eventNames = obtainEventNames(rvmSpecFile);
        LOGGER.log(Level.INFO, "Event Names: " + this.eventNames + "\n");
    }

    private Map<String, List<String>> fetchAliasedStates(List<String> message) {
        Map<String, List<String>> aliasMap = new HashMap<>();
        for (String msg: message) {
            if (msg.startsWith("START ALIASES:")) {
                Pattern pattern = Pattern.compile("\\{(.*?)\\}");
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find()) {
                    String match = matcher.group(1);
                    String[] split = match.split("=");
                    if (split.length == 2) {
                        String aliased = split[0].trim();
                        String aliases = split[1].trim();
                        Pattern pattern2 = Pattern.compile("\\[(.*?)\\]");
                        Matcher matcher2 = pattern2.matcher(aliases);
                        if (matcher2.find()) {
                            List<String> aliasesList = Arrays.asList(matcher2.group(1).split(","));
                            aliasMap.put(aliased, aliasesList);
                        }
                    }
                }
            }
        }
        return aliasMap;
    }

    private String fetchStartState(List<String> message) {
        String state = "";
        for (String m : message) {
            if (m.startsWith("START STATE:")) {
                state = m.split(":")[1].trim();
            }
        }
        return state;
    }

    private List<String> obtainEventNames(RVMSpecFile rvmSpecFile) {
        List<String> events = new ArrayList<>();
        for (EventDefinition eventDef : rvmSpecFile.getSpecs().get(0).getEvents()) {
            events.add(eventDef.getId());
        }
        return events;
    }

    private List<String> obtainEventNames(String events) {
        List<String> names = new ArrayList<>();
        for (String name : events.split(" ")) {
            names.add(name.trim());
        }
        return names;
    }

    private Map<String, Map<String, String>> getTransitionsPerState(String formula, Set<String> states) {
        Map<String, Map<String, String>> transitions = new HashMap<>();
        for (String state : states) {
            Pattern pattern = Pattern.compile(state + "\\[(?s)(.*?)\\]", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(formula);
            while (matcher.find()) {
                String stateTransitions = matcher.group();
                Map<String, String> transitionsAtState = new HashMap<>();
                for (String split : stateTransitions.split("\n")) {
                    if (split.contains("->")) {
                        String[] transition = split.split("->");
                        transitionsAtState.put(transition[0].trim(), transition[1].trim());
                    }
                }
                transitions.put(state, transitionsAtState);
            }
        }
        return transitions;
    }

    private Set<String> obtainDefaultStates(String formula, Set<String> states) {
        Set<String> defaults = new HashSet<>();
        for (String state : states) {
            Pattern pattern = Pattern.compile("default " + state);
            Matcher matcher = pattern.matcher(formula);
            while (matcher.find()) {
                defaults.add(matcher.group().trim().split(" ")[1]);
            }
        }
        return defaults;
    }

    private Set<String> getStateNames(String formula) {
        Set<String> states = new HashSet<>();
        Pattern pattern = Pattern.compile("(.*?\\[)");
        Matcher matcher = pattern.matcher(formula);
        while (matcher.find()) {
            states.add(matcher.group(1).trim().replaceAll("\\[", ""));
        }
        return states;
    }

    public String getSpecName() {
        return specName;
    }

    public String getFilename() {
        return specName.replace("_", "") + "Monitor.java";
    }

    public Set<String> getDefaultStates() {
        return defaultStates;
    }

    public List<String> getEventNames() {
        return eventNames;
    }

    public Set<String> getStates() {
        return states;
    }

    public String getCategory() {
        return category;
    }

    public Map<String, Map<String, String>> getTransitions() {
        return transitions;
    }

    public String getStartState() {
        return startState;
    }

    public Map<String, List<String>> getAliasedStates() {
        return aliasedStates;
    }

    public String getEnableSets() {
        return enableSets;
    }

    public void setEnableSets(String enableSets) {
        this.enableSets = enableSets;
    }
}
