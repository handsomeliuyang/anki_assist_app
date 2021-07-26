# Anki助手

> 「Anki」是一款记忆软件，更确切的说它是一款智能安排我们复习知识点的工具。

Anki 对知识点的记忆上有非常大的帮助，基于遗忘曲线有针对性的适时重复，对于有一定自制力的成人来说，帮助非常的大，但对于缺乏自制力的孩子来说，使用体验上问题比较多：
1. 为了防止小孩偷懒式的随意点击，需要家长来操作软件，对家长的投入很大
2. 孩子需要长时间的使用手机/Pad等电子设备
3. 需要家长了解软件的使用，同时无法远程辅导

AnkiDroid是一个开源软件，同时提供了对外的Api，适配做扩展插件，选择基于自定一个插件：Anki助手来解决上述痛点。

解决上述痛点的方式：
1. 打印复习Anki卡片：把当前需要复习和学习的问题打印出来，让小孩摆脱电子设备独立完成复习
2. 家长检查：基于打印复习的结果，由家长把结果录入Anki软件，解决小孩偷懒式的随意点击
3. 加强记忆：对于完全错误或部分错误的卡片需要加强记忆

# 效果图

<img src="screenshot/打印复习原型图.drawio.svg" align="right" width="40%" height="100%"></img>

<img src="screenshot/家长检查原型图.drawio.svg" align="right" width="40%" height="100%"></img>

<img src="screenshot/加强记忆原型图.drawio.svg" align="right" width="40%" height="100%"></img>

# 依赖库
1. 实现展开Item效果：[Expandable RecyclerView](https://bignerdranch.github.io/expandable-recycler-view/)

# 源码参考库
1. 框架参考：[architecture-samples](https://github.com/android/architecture-samples)

# QA
1. 小米手机无法运行，运行崩溃 ？
    1. 原因：小米 无法申请自定义的 dangerous 级别的权限，而AnkiDroid的卡片数据权限是 dangerous 级别
    2. 解决方案：先关闭“MIUI优化”，再手动授予相应的权限

# AnkiDroid
1. 复习按钮：
   1. 新卡片：AGAIN, GOOD(10 Min), EASY
   2. 复习卡片：AGAIN, HARD, GOOD, EASY
   3. 学习中的卡片：
      1. 新卡来源：AGAIN, GOOD(1 d), EASY
      2. 复习卡片来源：AGAIN, GOOD