package jp.takamichie.desktop.AdbViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalExclusionType;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main implements ActionListener {

	/**
	 * IDごとに処理を行う際に呼び出されるコールバックリスナ
	 */
	interface IDCallback {
		/**
		 * デバイスIDを発見する度に呼び出されるコールバックメソッドです。
		 *
		 * @param id
		 *            ID
		 */
		void callback(String id);
	}

	private static final String ACTION_ENABLEDAUTOCHECK = "autocheck";
	private static final String ACTION_ENABLETOPMOST = "topmost";
	private static final String ACTION_EXITAPP = "exitapp";
	private static final String ACTION_IDCOPY = "idcopy";
	private static final String ACTION_INSTALLAPK = "installapk";
	private static final String ACTION_SHOWSHELLPROMPT = "shellprompt";
	private static final String ACTION_SHOWLOGCAT = "logcat";
	private static final String ACTION_ABOUT = "about";
	private static final long AUTOUPDATE_TIMER = 5000;
	private static final String ACTION_CONNECTDEVICE = "connectdevice";
	private static final String PREFKEY_RECENTIPS = "recentIP";
	private static final String PROPFILE_PATH = "AdbViewer.properties";
	private static final Object APPLICATION_VER = "1.0";
	private static final Object SUPPORTED_ADB_VER = "1.0.29";
	private JFrame mAdbviewerFrame;
	private ScheduledExecutorService mScheduler;
	private JList<String> mDisplayDevices;
	private boolean isAutoUpdate;
	private JLabel mDisplayStatus;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.mAdbviewerFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
		Toolkit kit = Toolkit.getDefaultToolkit();
		URL url = mAdbviewerFrame.getClass().getResource("./res/ic_app.png");
		mAdbviewerFrame.setIconImage(url == null ? kit.getImage("./res/ic_app.png") : kit.createImage(url));
		// 5秒ごとに実行
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		mScheduler.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				if (isAutoUpdate) {
					updateDeviceList();
				}
			}
		}, 0, AUTOUPDATE_TIMER, TimeUnit.MILLISECONDS);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		mAdbviewerFrame = new JFrame();
		mAdbviewerFrame.setTitle("AdbViewer");
		mAdbviewerFrame.setType(Type.POPUP);
		mAdbviewerFrame
				.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		mAdbviewerFrame.setBounds(100, 100, 288, 161);
		mAdbviewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mDisplayDevices = new JList<String>();
		mDisplayDevices.setBorder(new BevelBorder(BevelBorder.LOWERED, null,
				null, null, null));
		mDisplayDevices.setBackground(Color.BLACK);
		mDisplayDevices.setForeground(Color.WHITE);
		mAdbviewerFrame.getContentPane().add(mDisplayDevices,
				BorderLayout.CENTER);

		JToolBar toolBar = new JToolBar();
		mAdbviewerFrame.getContentPane().add(toolBar, BorderLayout.NORTH);

		mDisplayStatus = new JLabel("");
		mAdbviewerFrame.getContentPane()
				.add(mDisplayStatus, BorderLayout.SOUTH);

		JMenuBar menuBar = new JMenuBar();
		mAdbviewerFrame.setJMenuBar(menuBar);

		JMenu menuFiles = new JMenu("ファイル(F)");
		menuFiles.setMnemonic('F');
		menuBar.add(menuFiles);

		JCheckBoxMenuItem menuitemEnabledAutoCheck = new JCheckBoxMenuItem(
				"自動更新(A)");
		menuitemEnabledAutoCheck.setActionCommand(ACTION_ENABLEDAUTOCHECK);
		menuitemEnabledAutoCheck.setMnemonic('A');
		menuitemEnabledAutoCheck.addActionListener(this);
		menuitemEnabledAutoCheck.setSelected(true);
		isAutoUpdate = menuitemEnabledAutoCheck.isSelected();
		menuFiles.add(menuitemEnabledAutoCheck);

		JCheckBoxMenuItem menuitemEnableTopmost = new JCheckBoxMenuItem(
				"常に手前表示(T)");
		menuitemEnableTopmost.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_T, 0));
		menuitemEnableTopmost.setMnemonic('T');
		menuitemEnableTopmost.setActionCommand(ACTION_ENABLETOPMOST);
		menuitemEnableTopmost.addActionListener(this);
		menuFiles.add(menuitemEnableTopmost);

		JMenuItem menuitemConnectDevice = new JMenuItem("LAN内端末の接続(C)");
		menuitemConnectDevice.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_C, 0));
		menuitemConnectDevice.setMnemonic('C');
		menuitemConnectDevice.setActionCommand(ACTION_CONNECTDEVICE);
		menuitemConnectDevice.addActionListener(this);
		menuFiles.add(menuitemConnectDevice);

		menuFiles.addSeparator();

		JMenuItem menuitemExitApp = new JMenuItem("終了(X)");
		menuitemExitApp.setMnemonic('X');
		menuitemExitApp.setActionCommand(ACTION_EXITAPP);
		menuitemExitApp.addActionListener(this);
		menuFiles.add(menuitemExitApp);

		JMenu menuDevices = new JMenu("デバイス(D)");
		menuDevices.setMnemonic('D');
		menuBar.add(menuDevices);

		JMenuItem menuitemIDCopy = new JMenuItem("IDをコピー(C)");
		menuitemIDCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				InputEvent.CTRL_MASK));
		menuitemIDCopy.setMnemonic('C');
		menuitemIDCopy.setActionCommand(ACTION_IDCOPY);
		menuitemIDCopy.addActionListener(this);
		menuDevices.add(menuitemIDCopy);

		JMenuItem menuitemInstallApk = new JMenuItem("APKインストール");
		menuitemInstallApk.setMnemonic('A');
		menuitemInstallApk.setActionCommand(ACTION_INSTALLAPK);
		menuitemInstallApk.addActionListener(this);
		menuDevices.add(menuitemInstallApk);

		JMenuItem menuitemShowShellPrompt = new JMenuItem("シェルログイン(S)");
		menuitemShowShellPrompt.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F2, 0));
		menuitemShowShellPrompt.setMnemonic('S');
		menuitemShowShellPrompt.setActionCommand(ACTION_SHOWSHELLPROMPT);
		menuitemShowShellPrompt.addActionListener(this);
		menuDevices.add(menuitemShowShellPrompt);

		JMenuItem menuitemShowLogcat = new JMenuItem("Logcat表示");
		menuitemShowLogcat.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F3, 0));
		menuitemShowLogcat.setMnemonic('L');
		menuitemShowLogcat.setActionCommand(ACTION_SHOWLOGCAT);
		menuitemShowLogcat.addActionListener(this);
		menuDevices.add(menuitemShowLogcat);

		JMenu menuAbout = new JMenu("情報(A)");
		menuAbout.setMnemonic('A');
		menuBar.add(menuAbout);

		JMenuItem menuitemAboutApp = new JMenuItem("AdbViewerについて");
		menuitemAboutApp.setMnemonic('A');
		menuitemAboutApp.setActionCommand(ACTION_ABOUT);
		menuitemAboutApp.addActionListener(this);
		menuAbout.add(menuitemAboutApp);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			switch (e.getActionCommand()) {
			case ACTION_ENABLEDAUTOCHECK:
				// do nothing
				operate_enableAutoCheck((JCheckBoxMenuItem) e.getSource());
				break;
			case ACTION_ENABLETOPMOST:
				operate_enableTopMost((JCheckBoxMenuItem) e.getSource());
				break;
			case ACTION_CONNECTDEVICE:
				operate_connectdevice();
				break;
			case ACTION_EXITAPP:
				System.exit(0);
				break;
			case ACTION_IDCOPY:
				operate_idcopy();
				break;
			case ACTION_INSTALLAPK:
				operate_installapk();
				break;
			case ACTION_SHOWSHELLPROMPT:
				operate_showShellPrompt();
				break;
			case ACTION_SHOWLOGCAT:
				operate_showLogcat();
				break;
			case ACTION_ABOUT:
				operate_showabout();
				break;
			default:
				break;
			}
		} catch (IOException ex) {
			showStandardErrorDialog(ex);
			ex.printStackTrace();
		}
	}

	/** イベントハンドラ **/

	/**
	 * 自動更新メニューのイベントハンドラです。
	 *
	 * @param source
	 *            メニューアイテム
	 */
	private void operate_enableAutoCheck(JCheckBoxMenuItem source) {
		isAutoUpdate = source.isSelected();
	}

	/**
	 * 常に手前表示メニューのイベントハンドラです。
	 *
	 * @param source
	 *            メニューアイテム
	 */
	private void operate_enableTopMost(JCheckBoxMenuItem source) {
		mAdbviewerFrame.setAlwaysOnTop(source.isSelected());
	}

	private void operate_connectdevice() throws IOException {
		Properties prop = new Properties();
		try (InputStream stream = new FileInputStream(PROPFILE_PATH)) {
			prop.loadFromXML(stream);
		} catch (IOException e) {
			// ignore this
		}
		String ips = prop.getProperty(PREFKEY_RECENTIPS, "");
		ArrayList<String> recents = new ArrayList<>(Arrays.asList(ips
				.split(",")));
		recents.remove("");
		JPanel panel = new JPanel();
		JComboBox<String> editor_ipaddr = new JComboBox<>(
				recents.toArray(new String[recents.size()]));
		editor_ipaddr.setEditable(true);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel("接続する端末のIPアドレスを指定してください"));
		panel.add(editor_ipaddr);
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				// superを呼ばない
			}
		};

		JDialog dialog = pane.createDialog(mAdbviewerFrame, "選択");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		if (new Integer(JOptionPane.OK_OPTION).equals(pane.getValue())) {
			String item = editor_ipaddr.getSelectedItem().toString();
			String ret = ProcessExecuter.execute("adb", "connect", item);
			if (ret.contains("connected")) {
				// 履歴の更新
				recents.remove(item);
				recents.add(item);
				StringBuilder prefips = new StringBuilder();
				for (String s : recents) {
					prefips.append(s);
					prefips.append(",");
				}
				prop.setProperty(PREFKEY_RECENTIPS,
						prefips.substring(0, prefips.length() - 1));
				try (OutputStream out = new FileOutputStream(PROPFILE_PATH)) {
					prop.storeToXML(out, null);
					out.flush();
				}
				updateDeviceList();
				mDisplayStatus.setText("デバイスに接続しました");
			} else {
				mDisplayStatus.setText("デバイスに接続できませんでした");
				showStandardErrorDialog(ret);
			}
		}
	}

	/**
	 * IDをコピーメニューのイベントハンドラです
	 */
	private void operate_idcopy() {
		final StringBuilder builder = new StringBuilder();
		execSelectedDevices(new IDCallback() {
			@Override
			public void callback(String id) {
				builder.append(id);
			}
		});
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		clip.setContents(new StringSelection(builder.toString()), null);
		mDisplayStatus.setText("クリップボードにIDをコピーしました");
	}

	/**
	 * APKインストールメニューのイベントハンドラです
	 *
	 * @throws IOException
	 *             入出力エラー
	 */
	private void operate_installapk() throws IOException {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("APKファイル", "apk"));
		if (chooser.showOpenDialog(mAdbviewerFrame) == JFileChooser.APPROVE_OPTION) {
			mDisplayStatus.setText("APKをインストールしています");
			mDisplayStatus.invalidate();
			mAdbviewerFrame.validate();
			execSelectedDevices(new IDCallback() {

				@Override
				public void callback(String id) {
					String ret;
					try {
						ret = ProcessExecuter.execute("adb", "-s", id,
								"install", "-r", chooser.getSelectedFile()
										.getAbsolutePath());
						if (ret.contains("Success")) {
							mDisplayStatus.setText("APKのインストールに成功しました");
						} else {
							JOptionPane.showMessageDialog(mAdbviewerFrame, ret,
									"エラー", JOptionPane.ERROR_MESSAGE);
						}
					} catch (IOException e) {
						showStandardErrorDialog(e);
						e.printStackTrace();
					}
				}

			});
		}
	}

	/**
	 * シェルログインメニューのイベントハンドラです。
	 */
	private void operate_showShellPrompt() {
		execSelectedDevices(new IDCallback() {

			@Override
			public void callback(String id) {
				try {
					new ProcessBuilder("cmd", "/c", "start", "adb", "-s", id,
							"shell").start();
				} catch (IOException e) {
					showStandardErrorDialog(e);
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Logcatを表示メニューのイベントハンドラです。
	 */
	private void operate_showLogcat() {
		execSelectedDevices(new IDCallback() {
			@Override
			public void callback(String id) {
				try {
					new ProcessBuilder("cmd", "/c", "start", "adb", "-s", id,
							"logcat").start();
				} catch (IOException e) {
					showStandardErrorDialog(e);
					e.printStackTrace();
				}
			}
		});
	}

	private void operate_showabout() throws IOException {
		String[] version = ProcessExecuter.execute("adb", "version").split(" ");
		String ver = version[version.length - 1];
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Application Version:%s\n",
				APPLICATION_VER));
		builder.append(String.format("ADB Version:%s\n", ver));
		builder.append(String
				.format("このアプリケーションはADB Version:%s にて動作確認済です。\nADBバージョンが古い場合はandroidコマンドよりアップデートを行ってください",
						SUPPORTED_ADB_VER));

		JOptionPane.showMessageDialog(mAdbviewerFrame, builder.toString(),
				String.format("%sのバージョン情報", mAdbviewerFrame.getTitle()),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/** その他メソッド **/

	/**
	 * 現在リストで選択している全てのデバイスに対して任意の処理を実行します。
	 *
	 * @param idCallback
	 *            処理を実行する{@link IDCallback}リスナ
	 */
	private void execSelectedDevices(IDCallback idCallback) {
		for (String s : mDisplayDevices.getSelectedValuesList()) {
			String[] dataline = s.split("\t");
			if (idCallback != null)
				idCallback.callback(dataline[0]);
		}
	}

	/**
	 * デバイスリストを更新します。
	 */
	private void updateDeviceList() {
		final ArrayList<String> devices = new ArrayList<String>();
		try {
			int[] selection = mDisplayDevices.getSelectedIndices();
			ProcessExecuter.execute(new ProcessExecuter.LineCallback() {
				@Override
				public void callback(String line) {
					String[] dataline = line.split("\t");
					if (dataline.length > 1 && dataline[1].equals("device")) {
						StringBuilder display = new StringBuilder();
						display.append(dataline[0]);
						try {
							display.append("\t");
							display.append("(");
							display.append(ProcessExecuter.execute("adb", "-s",
									dataline[0], "shell", "getprop",
									"ro.product.model"));
							display.append(")");
						} catch (IOException e) {
							// ignore this
						}
						devices.add(display.toString());
					}
				}
			}, "adb", "devices");
			mDisplayDevices.removeAll();
			mDisplayDevices.setListData(devices.toArray(new String[devices
					.size()]));
			if (selection.length == 0 && devices.size() > 0) {
				mDisplayDevices.setSelectedIndex(0);
			} else {
				mDisplayDevices.setSelectedIndices(selection);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * エラーダイアログを表示します。
	 *
	 * @param e
	 *            例外オブジェクト
	 */
	private void showStandardErrorDialog(Exception e) {
		showStandardErrorDialog(e.getMessage());
	}

	/**
	 * エラーダイアログを表示します。
	 *
	 * @param e
	 *            エラーを説明した文字列
	 */
	private void showStandardErrorDialog(String error) {
		JOptionPane.showMessageDialog(mAdbviewerFrame, error, "エラー",
				JOptionPane.ERROR_MESSAGE);
	}
}
