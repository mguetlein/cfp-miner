package org.kramerlab.cfpminer.weka.eval2;

import org.mg.javalib.util.ThreadUtil;
import org.mg.wekalib.eval2.persistance.Blocker;

import com.lambdaworks.redis.RedisConnection;

public class RedisBlocker implements Blocker
{
	RedisConnection<String, String> con;

	public RedisBlocker(RedisConnection<String, String> con)
	{
		this.con = con;
		con.select(2);
	}

	@Override
	public boolean isBlockedByThread(String key, String threadId)
	{
		return threadId.equals(con.get(key.toString()));
	}

	@Override
	public boolean block(String key, String threadId)
	{
		if (con.get(key.toString()) != null)
			return false;
		con.set(key.toString(), threadId);
		ThreadUtil.sleep(1000);
		return (con.get(key.toString()).equals(threadId));
	}

	@Override
	public void unblock(String key)
	{
		con.del(key.toString());
	}

	@Override
	public void clear()
	{
		con.flushdb();
	}

}
