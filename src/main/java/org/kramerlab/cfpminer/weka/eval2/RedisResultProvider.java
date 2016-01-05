package org.kramerlab.cfpminer.weka.eval2;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.mg.wekalib.eval2.persistance.ResultProvider;

import com.lambdaworks.redis.RedisConnection;

public class RedisResultProvider implements ResultProvider
{
	RedisConnection<byte[], byte[]> con;

	public RedisResultProvider(RedisConnection<byte[], byte[]> con)
	{
		this.con = con;
		con.select(1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean contains(String key)
	{
		//return con.get(key.getBytes()) != null;
		return con.exists(key.toString().getBytes());
	}

	@Override
	public Serializable get(String key)
	{
		return SerializationUtils.deserialize(con.get(key.toString().getBytes()));
	}

	@Override
	public void set(String key, Serializable value)
	{
		if (con.get(key.toString().getBytes()) != null)
			throw new IllegalStateException("alreay computed!");
		con.set(key.toString().getBytes(), SerializationUtils.serialize(value));
	}

	@Override
	public void clear()
	{
		con.flushdb();
	}

}
