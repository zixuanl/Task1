package lab0;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class Parse {

	private ArrayList<PeerNode> nodes = new ArrayList<PeerNode>();
	private ArrayList<Rule> rules;
	// private ArrayList<Rule> rRules = new ArrayList<Rule>();
	private PeerNode local;

	public void parseConf(String configuration_filename, String local_name)
			throws FileNotFoundException {

		InputStream input = new FileInputStream(
				new File(configuration_filename));
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>) yaml.load(input);

		// parse configuration
		List<Object> confs = (List<Object>) map.get("configuration");
		for (Object c : confs) {
			Map<String, Object> conf = (Map<String, Object>) c;
			if (!conf.get("name").equals(local_name)) {
				PeerNode n = new PeerNode(conf.get("name").toString(), conf
						.get("ip").toString(), Long.parseLong(conf.get("port")
						.toString()));
				nodes.add(n);
			} else {
				local = new PeerNode(conf.get("name").toString(), conf
						.get("ip").toString(), Long.parseLong(conf.get("port")
						.toString()));
			}
		}
	}

	public ArrayList<Rule> parseRules(String configuration_filename, String type)
			throws FileNotFoundException {

		InputStream input = new FileInputStream(
				new File(configuration_filename));
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>) yaml.load(input);

		// parse rules
		List<Object> listRules = (List<Object>) map.get(type);
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
				dup = Boolean
						.parseBoolean(listRule.get("duplicate").toString());
			Rule r = new Rule(listRule.get("action").toString(), src, dest,
					kind, seqNum, dup);
			rules.add(r);
		}
		return rules;
	}

	// public void parseReceiveRule(String configuration_filename)
	// throws FileNotFoundException {
	//
	// InputStream input = new FileInputStream(
	// new File(configuration_filename));
	// Yaml yaml = new Yaml();
	// Map<String, Object> map = (Map<String, Object>) yaml.load(input);
	//
	// // parse receive rules
	// List<Object> recRules = (List<Object>) map.get("receiveRules");
	// for (Object rec : recRules) {
	// Map<String, Object> recRule = (Map<String, Object>) rec;
	// String src = null;
	// String dest = null;
	// String kind = null;
	// Integer seqNum = null;
	// if (recRule.get("src") != null)
	// src = recRule.get("src").toString();
	// if (recRule.get("dest") != null)
	// dest = recRule.get("dest").toString();
	// if (recRule.get("kind") != null)
	// kind = recRule.get("kind").toString();
	// if (recRule.get("seqNum") != null)
	// seqNum = Integer.parseInt(recRule.get("seqNum").toString());
	// Rule r = new Rule(recRule.get("action").toString(), src, dest,
	// kind, seqNum);
	// rRules.add(r);
	// }
	// }

	public ArrayList<PeerNode> getPeerNodes() {
		return nodes;
	}

	// public ArrayList<Rule> getSendRules() {
	// return sRules;
	// }

	// public ArrayList<Rule> getReceiveRules() {
	// return rRules;
	// }

	public PeerNode getLocalNode() {
		return local;
	}
}
