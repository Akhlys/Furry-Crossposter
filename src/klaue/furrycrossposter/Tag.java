package klaue.furrycrossposter;

import java.io.Serializable;

public class Tag implements Comparable<Tag>, Serializable {
	private static final long serialVersionUID = 2964624768254713596L;

	public enum Type {GENERAL, ARTIST, COPYRIGHT, CHARACTER, SPECIES};
	private Type type = null;
	private String name = null;
	private int count = 0;
	private int id = 0;

	/**
	 * 
	 * @param name
	 * @param type
	 * @param count
	 * @param id
	 */
	public Tag(String name, Type type, int count, int id) {
		this.type = type;
		this.name = name;
		this.count = count;
		this.id = id;
	}
	
	/**
	 * 
	 * @param name
	 * @param type
	 */
	public Tag(String name, Type type) {
		this(name, type, 0, 0);
	}
	
	/**
	 * used with e621
	 * @param name the tag
	 * @param type 0-1, 3-5
	 */
	public Tag(String name, int type, int count, int id) {
		this(name, Type.GENERAL, count, id);
		switch(type) {
			case 0: this.setType(Type.GENERAL); break;
			case 1: this.setType(Type.ARTIST); break;
			case 3: this.setType(Type.COPYRIGHT); break;
			case 4: this.setType(Type.CHARACTER); break;
			case 5: this.setType(Type.SPECIES); break;
			default: this.setType(Type.GENERAL);
		}
	}
	
	/**
	 * used with e621
	 * @param name the tag
	 * @param type 0-1, 3-5
	 */
	public Tag(String name, int type) {
		this(name, type, 0, 0);
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public int compareTo(Tag arg0) {
		return this.name.compareTo(arg0.name);
	}
	
	@Override
	public String toString() {
		return "Tag " + this.name + ": Type " + this.type + ", count " + this.count + ", id " + this.id;
	}
}
