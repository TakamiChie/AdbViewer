package jp.takamichie.desktop.AdbViewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ProcessExecuter {

	/**
	 * {@link ProcessExecuter#execute(LineCallback, String...)}メソッドで使用するコールバックオブジェクトです。
	 */
	public interface LineCallback {
		/**
		 * 標準出力行を取得する度にコールバックされます。
		 * @param line 行文字列
		 */
		void callback(String line);
	}

	private ProcessExecuter() {
	}

	/**
	 * コマンドライン引数を指定してアプリケーションを起動します。
	 * @param callback 標準出力にて行を取得する度に呼び出されるコールバックオブジェクト
	 * @param commands 起動するコマンドライン引数
	 * @return アプリケーションの標準出力の値
	 * @throws IOException 入出力エラー
	 */
	public static String execute(LineCallback callback, String... commands)
			throws IOException {
		Process proc = new ProcessBuilder(commands).redirectErrorStream(true)
				.start();
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				proc.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (callback != null)
					callback.callback(line);
				result.append(line);
				result.append("\n");
			}

		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
	}

	/**
	 * コマンドライン引数を指定してアプリケーションを起動します。
	 * @param commands 起動するコマンドライン引数
	 * @return アプリケーションの標準出力の値
	 * @throws IOException 入出力エラー
	 */
	public static String execute(String... commands) throws IOException{
		return execute(null, commands);
	}
}
