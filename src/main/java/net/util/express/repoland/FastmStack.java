package net.util.express.repoland;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class FastmStack<T> {

	private LinkedList<T> list = new LinkedList<T>();

	private int top = -1;

	public List<T> getAllStackObjects() {
		// List asos = new ArrayList();
		// asos.addAll(list);
		// return asos;
		return (List<T>) list.clone();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Object pop() {
		if (top == -1)
			return null;
		Object temp = list.getFirst();
		top -= 1;
		list.removeFirst();
		return temp;
	}

	public void push(T value) {
		top += 1;
		list.addFirst(value);
	}

	public void reserse() {
		Object[] os = list.toArray();
		while (!isEmpty()) {
			pop();
		}
		for (int index = 0; index < os.length; index++) {
			push((T) os[index]);
		}
	}

	public int size() {
		return list.size();
	}

	public T top() {
		if (top == -1)
			return null;
		return list.getFirst();
	}
}
