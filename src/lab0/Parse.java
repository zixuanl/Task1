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
	private Integer local_index = 0;
	private Integer index = -1;
	private String log_ip = "0.0.0.0";
	
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
				
				if (n.getName().equals("alice"))
					log_ip = n.getIp();
				
				index++;
			} else {
				local = new PeerNode(conf.get("name").toString(), conf
						.get("ip").toString(), Long.parseLong(conf.get("port")
						.toString()));
				if (local.getName().equals("alice"))
					log_ip = local.getIp();
				index++;
				local_index = index;
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


	public String getLogip() {
		return log_ip;
	}

	public ArrayList<PeerNode> getPeerNodes() {
		return nodes;
	}


	public Integer getLocalIndex() {
		return local_index;
	}

	public PeerNode getLocalNode() {
		return local;
	}
}
