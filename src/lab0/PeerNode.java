package lab0;

public class PeerNode {
	private String name;
	private String ip;
	private Long port;
	
	public PeerNode() {
		name = null;
		ip = null;
		port = null;
	}

	public PeerNode(String name, String ip, Long port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Long getPort() {
		return port;
	}

	public void setPort(Long port) {
		this.port = port;
	}
	
	
	
}
