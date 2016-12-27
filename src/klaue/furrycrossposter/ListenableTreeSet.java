package klaue.furrycrossposter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ListenableTreeSet<T> extends TreeSet<T> {
	private static final long serialVersionUID = -3486305602551487237L;
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	

	public void addChangeListener(ChangeListener c) {
		if (!this.listeners.contains(c)) this.listeners.add(c);
	}
	
	public void removeChangeListener(ChangeListener c) {
		this.listeners.remove(c);
	}
	
	private void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener listener : listeners) {
			listener.stateChanged(e);
		}
	}
	
	@Override
	public boolean add(T e) {
		boolean b = super.add(e);
		if (b) fireChangeEvent();
		return b;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean b = super.addAll(c);
		if (b) fireChangeEvent();
		return b;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean b = super.remove(o);
		if (b) fireChangeEvent();
		return b;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b = super.removeAll(c);
		if (b) fireChangeEvent();
		return b;
	}
	
	@Override
	public void clear() {
		super.clear();
		fireChangeEvent();
	}
}
