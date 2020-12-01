package net.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author suk
 */
public class CopyOnWriteMap<K, V> implements Map<K, V>
{
	transient final ReentrantLock	lock		= new ReentrantLock();

	protected volatile Map<K, V>	mapToRead	= getNewMap();

	@Override
	public synchronized void clear()
	{
		this.mapToRead = getNewMap();
	}

	@Override
	public boolean containsKey(final Object key)
	{
		return this.mapToRead.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value)
	{
		return this.mapToRead.containsValue(value);
	}

	protected Map<K, V> copy()
	{
		Map<K, V> newMap = getNewMap();
		newMap.putAll(this.mapToRead);
		return newMap;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet()
	{
		return this.mapToRead.entrySet();
	}

	@Override
	public V get(final Object key)
	{
		return this.mapToRead.get(key);
	}

	protected Map<K, V> getNewMap()
	{
		return new HashMap<K, V>();
	}

	@Override
	public boolean isEmpty()
	{
		return this.mapToRead.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return this.mapToRead.keySet();
	}

	@Override
	public synchronized V put(final K key, final V value)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			Map<K, V> map = copy();
			V o = map.put(key, value);
			this.mapToRead = map;
			return o;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public synchronized void putAll(final Map<? extends K, ? extends V> t)
	{
		Map<K, V> map = copy();
		map.putAll(t);
		this.mapToRead = map;
	}

	@Override
	public synchronized V remove(final Object key)
	{
		Map<K, V> map = copy();
		V o = map.remove(key);
		this.mapToRead = map;
		return o;
	}

	@Override
	public int size()
	{
		return this.mapToRead.size();
	}

	@Override
	public Collection<V> values()
	{
		return this.mapToRead.values();
	}
}
