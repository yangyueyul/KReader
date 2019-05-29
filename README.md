# 掌读
* Android阅读器
* 项目修改自FBreaderJ：https://github.com/geometer/FBReaderJ
* 当前版本为旧版本，*新版开源计划筹备中*
* 新版[下载地址](https://www.pgyer.com/Airv)


# 项目展示
![k 1](https://github.com/ydcx/KooReader/blob/master/k1.png)<br/>

![k 2](https://github.com/ydcx/KooReader/blob/master/k2.png)<br/>

# 代码框架
## Klibrary
### Core
* 抽象类或final类 很多抽象方法让子类实现，核心，自定义文件类型，字体类型，图片类型

### Text
* 文字处理 断字 类型(文字,超链,图片) char数组缓存 游标ZLTextview TextPage

### Ui
* 图片加载 颜色 CSS样式 自定义View 动画

## Kooreader
#### Bookmodel
* 包含书籍 目录 字体 缓存 TextModel

#### Formats
* 各种格式的解析

#### Kooreader
* 颜色，翻页边距设置 KooView 手势处理

#### Library
* 本地书籍查看相关树

#### KooTree
* 自定义树结构

## Android
#### MainActivity
* 主界面 阅读设置 本地文件选择

#### AIDL Service
* 通过远程服务对数据库的操作

#### Sqlite
* 数据库 book.db config.db

#### Preferences
* 设置保存

#### util
* 设备信息 横竖屏 Dialog显示

# 翻页效果
![f 1](https://github.com/ydcx/KooReader/blob/master/f1.jpg?raw=true)<br/>

# More
* 更多解析见[Epub书籍快速分章加载](https://www.jianshu.com/p/fd03a43fbf63)
