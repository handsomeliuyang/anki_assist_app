# anki_assist_app

# 依赖库
1. 实现展开Item效果：[Expandable RecyclerView](https://bignerdranch.github.io/expandable-recycler-view/)

# 源码参考库
1. 框架参考：[architecture-samples](https://github.com/android/architecture-samples)
2. 

# QA
1. 小米手机无法运行，运行崩溃 ？
    1. 原因：小米 无法申请自定义的 dangerous 级别的权限，而AnkiDroid的卡片数据权限是 dangerous 级别
    2. 解决方案：先关闭“MIUI优化”，再手动授予相应的权限
2. 

# AnkiDroid
1. 复习按钮：
   1. 新卡片：AGAIN, GOOD(10 Min), EASY
   2. 复习卡片：AGAIN, HARD, GOOD, EASY
   3. 学习中的卡片：
      1. 新卡来源：AGAIN, GOOD(1 d), EASY
      2. 复习卡片来源：AGAIN, GOOD