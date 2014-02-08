package edu.cmu.ds.messagepasser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.yaml.snakeyaml.Yaml;

import edu.cmu.ds.messagepasser.model.Node;
import edu.cmu.ds.messagepasser.model.Rule;

public class ConfigParser {

	private Node loggerNode;
	private ArrayList<Node> peerNodes = new ArrayList<Node>();
	private ArrayList<Rule> sendRules, receiveRules;
	private Node localNode;
	private Integer localNodeIndex = 0;

	public ConfigParser(String configurationFileName, String localName) throws FileNotFoundException {

		InputStream input = new FileInputStream(new File(configurationFileName));
		Yaml yaml = new Yaml();
		Map<String, Object> configMap = (Map<String, Object>) yaml.load(input);

		setUpNodes(configMap, localName);
		setUpLogger(configMap);
		sendRules = parseRules(configMap, "sendRules");
		receiveRules = parseRules(configMap, "receiveRules");
		
	}
	
	/**
	 * Read configuration map loaded from file, and set up local and peer nodes
	 * @param configMap
	 * @param localName
	 */
	private void setUpNodes(Map<String, Object> configMap, String localName) {
		List<Object> confs = (List<Object>) configMap.get("configuration");
		int nodeIndex = 0;
		for (Object c: confs) {
			Map<String, Object> conf = (Map<String, Object>) c;
			if (!conf.get("name").equals(localName)) {
				Node n = new Node((String) conf.get("name"), (String) conf.get("ip"),
						new Integer((String) conf.get("port")));
				peerNodes.add(n);
			} else {
				localNode = new Node((String) conf.get("name"), (String) conf.get("ip"),
						new Integer((String) conf.get("port")));
				localNodeIndex = nodeIndex;
			}
			nodeIndex++;
		}
	}
	
	/**
	 * Read configuration map loaded from file, and set up logger
	 * @param configMap
	 */
	private void setUpLogger(Map<String, Object> configMap) {
		List<Object> confs = (List<Object>) configMap.get("logger");
		if (confs != null && !confs.isEmpty()) {
			Map<String, Object> conf = (Map<String, Object>) confs.get(0);
			loggerNode = new Node("", (String) conf.get("ip"), new Integer((String) conf.get("port")));
		} else {
			throw new RuntimeException("Cannot find config for logger");
		}
	}

	/**
	 * Universal rule parser. Read configuration map loaded from file.
	 * @param configMap
	 * @param ruleType sendRules or receiveRules
	 * @return List of rules depending on type
	 * @throws FileNotFoundException
	 */
	private ArrayList<Rule> parseRules(Map<String, Object> configMap, String ruleType)
			throws FileNotFoundException {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		List<Object> listRules = (List<Object>) configMap.get(ruleType);
		rules = new ArrayList<Rule>();
		for (Object s : listRules) {
			Map<String, Object> listRule = (Map<String, Object>) s;
			String src = null;
			String dest = null;
			String kind = null;
			Integer seqNum = null;
			Boolean dup = null;
			if (listRule.get("src") != null)
				src = listRule.get("src").toString();
			if (listRule.get("dest") != null)
				dest = listRule.get("dest").toString();
			if (listRule.get("kind") != null)
				kind = listRule.get("kind").toString();
			if (listRule.get("seqNum") != null)
				seqNum = Integer.parseInt(listRule.get("seqNum").toString());
			if (listRule.get("duplicate") != null)
				dup = Boolean.parseBoolean(listRule.get("duplicate").toString());
			Rule r = new Rule(listRule.get("action").toString(), src, dest, kind, seqNum, dup);
			rules.add(r);
		}
		return rules;
	}

	public String getLoggerIp() {
		return loggerNode.getIp();
	}
	
	public int getLoggerPort() {
		return loggerNode.getPort();
	}

	public ArrayList<Node> getPeerNodes() {
		return peerNodes;
	}

	public int getLocalNodeIndex() {
		return localNodeIndex;
	}

	public Node getLocalNode() {
		return localNode;
	}
	
	public ArrayList<Rule> getSendRules() {
		return sendRules;
	}
	
	public ArrayList<Rule> getReceiveRules() {
		return receiveRules;
	}
}
