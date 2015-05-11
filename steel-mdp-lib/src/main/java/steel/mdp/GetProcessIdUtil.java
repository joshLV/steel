package steel.mdp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * @project: Steel
 * @description: 获得进程号工具类
 
 */
public class GetProcessIdUtil {
	public static long getCurrentPid() {
		try {
			Vector<String> commands = new Vector<String>();
			commands.add("/bin/bash");
			commands.add("-c");
			commands.add("echo $PPID");
			ProcessBuilder pb = new ProcessBuilder(commands);

			Process pr = pb.start();
			pr.waitFor();
			if (pr.exitValue() == 0) {
				BufferedReader outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				return Long.valueOf(outReader.readLine().trim());
			} else {
				return -1L;
			}
		} catch (Exception e) {
			return -1L;
		}
	}
}
