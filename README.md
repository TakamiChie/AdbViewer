AdbViewer
=========

現在のADB接続状態を監視、コマンドの送信を行うツールです。

できること
---------
こんなことが出来ます。

* PCに接続されているデバイスの一覧表示
* ワイヤレスADBでの端末接続
* デバイスIDのコピー
* ファイルを選択してAPKファイルのインストール
* シェルプロンプトを開く
* Logcatを開く

そのうちやること
---------
やろうとしてます
* 簡単なスクリプトで任意のコマンドをデバイスに送る
* Android4.0以上の非root端末のワイヤレスADB有効化(adb tcpip)
* デバイスのクリップボードになんか送る
* デバイスのスクリーンショットをPCのクリップボードにコピー
* デバイスがWi-Fi圏内に入ったときに、自動的にワイヤレスADB接続
* ADBデバイスの状態が変化した時に音を鳴らす
* ADBにパスが通っていなくても実行できるようにする
* 関連のアプリ(SDKマネージャなど)を起動する

条件
--------
ADBにパスが通っている(コマンドプロンプトでいきなり「adb」と打っても実行可能)ことが条件です。
そのうちなんとかします。