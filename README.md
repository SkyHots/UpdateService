# UpdateService
1.这个demo主要是通过订阅后端推送的rabbitmq消息来进行更新的

2.收到更新推送=》通过推送的版本号判断是否要更新=》需要更新通过推送的url去下载apk
=》下载完成之后执行静默安装

3.这个demo的具体业务可以不看，直接看utils包下面的静默安装工具类SilentInstallUtils