# anki_assist_app

# QA
1. 小米手机无法运行，运行崩溃 ？
    1. 原因：小米 无法申请自定义的 dangerous 级别的权限，而AnkiDroid的卡片数据权限是 dangerous 级别
    2. 解决方案：先关闭“MIUI优化”，再手动授予相应的权限
2. 