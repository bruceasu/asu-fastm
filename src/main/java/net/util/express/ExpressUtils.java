package net.util.express;

import net.util.express.repoland.RepolandCompute;

public class ExpressUtils {
	public static boolean judge(String express) {
		RepolandCompute computeITF = new RepolandCompute();
		return computeITF.judge(express);
	}
}
