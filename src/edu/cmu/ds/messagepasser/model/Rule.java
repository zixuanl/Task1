package edu.cmu.ds.messagepasser.model;

public class Rule {
	private String action;
	private String source;
	private String destination;
	private String kind;
	private Integer sequenceNumber;
	private Boolean isDuplicate;

	public Rule() {
		action = null;
		source = null;
		destination = null;
		kind = null;
		sequenceNumber = null;
		isDuplicate = null;
	}

	public Rule(Rule r) {
		action = r.action;
		source = r.source;
		destination = r.destination;
		kind = r.kind;
		sequenceNumber = r.sequenceNumber;
		isDuplicate = r.isDuplicate;
	}

	public Rule(String action, String source, String destination, String kind,
			Integer sequenceNumber, Boolean isDuplicate) {
		this.action = action;
		this.destination = destination;
		this.source = source;
		this.kind = kind;
		this.sequenceNumber = sequenceNumber;
		this.isDuplicate = isDuplicate;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Boolean getIsDuplicate() {
		return isDuplicate;
	}

	public void setIsDuplicate(Boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	public boolean matches(Message message) {
		// If the target message is null: Not a match
		if (message == null)
			return false;
		// If source is set, and it doesn't match message's: Not a match
		if (source != null && !source.equals(message.getSource()))
			return false;
		// If destination is set, and it doesn't match message's: Not a match
		if (destination != null && !destination.equals(message.getDestination()))
			return false;
		// If kind is set, and it doesn't match message's: Not a match
		if (kind != null && !kind.equals(message.getKind()))
			return false;
		// If sequenceNumber is set, and it doesn't match message's: Not a match
		if (sequenceNumber != null && !sequenceNumber.equals(message.getSequenceNumber()))
			return false;
		// If isDuplicate is set, and it doesn't match message's: Not a match
		if (isDuplicate != null && !isDuplicate.equals(message.getIsDuplicate()))
			return false;
		
		return true;
	}
}
