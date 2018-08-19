package net.util.express;

import net.util.express.repoland.RepolandComputeITFImp;

public class ExpressUtils {
	public static boolean judge(String express) {
		ComputeITF computeITF = new RepolandComputeITFImp();
		return computeITF.judge(express);
	}
}
