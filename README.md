# 应用场景

长图编辑页添加文字功能，仿Instagram体验，如:

1. 支持文字背景(圆角+纯色，行之间的衔接处要有弧线);
2. 文字输入多行超出边界时，自动缩小字号大小；回删文字时自动还原字号大小;
3. 支持不限制层级的描边、投影等;

Instagram效果图:

![img](http://16xyz.com:8081/robot/adminblog/-/wikis/uploads/5ee05dfd9fc565c842420ac2e08727ef/image.png)

# 原理说明

## 系统控件绘制流程

```txt
View.draw() 
  ->View.drawBackground()
   ->TextView.onDraw()
    ->Layout.draw()
       ->Layout.drawBackground()
       ->LineBackgroundSpan.drawBackground() // 突破口:在绘制文字之前,可绘制任意内容作为文字背景
    ->Layout.drawText()
```

## 文字绘制规则

![image](https://user-images.githubusercontent.com/4396725/149644142-d503fe15-ebe7-4da1-bf96-ab0db24cd90b.png)

# 场景1:文字背景

## 获得文本边界
```kotlin
layout.getLineBounds(lineNumber, bound) // bound.height=bottom-top
```
## 衔接处圆弧效果

### 基准线
在行之间的衔接处，会出现以下2种情况:

- 无任何内边距(no paddings)

![image](https://user-images.githubusercontent.com/4396725/149644152-c9e9d13d-cafe-4041-9cbf-b6195833d2fe.png)

- 有内边距(如设置一个10dp的paddings)

![image](https://user-images.githubusercontent.com/4396725/149644158-38f7568a-968c-44f2-b00b-bee4e7a7c4f4.png)


因此基准线大致分成3种: 
1. 上长下短: lastLineBound.bottom
2. 上短下长: currentLineBound.top
3. 上下等长: lastLineBound.bottom

### 圆弧实现

![image](https://user-images.githubusercontent.com/4396725/149644161-b74c8d68-f439-4a30-8748-19b0c1459c4a.png)

先绘制黑色矩形，再使用`PorterDuff.Mode.CLEAR`方式绘制2个透明的圆，最终得到的是一个通用的衔接图。

> 衔接图: 用来填入衔接处,根据基准线判断是否需要旋转再填入

# 场景2:自适应字号

二分法查找合适字号。
设：默认设置的字号为最高值(high)，最低的字号(low)，View可见尺寸为availableViewBound.
算法伪代码如下:
```kotlin
    /**
     * 二分法快速调整字号.
     * 性能测试:1) 全新查找平均耗时18ms(StaticLayout无缓存情况创建需要7ms); 
     *         2) bestTextSizeCache有缓存情况平均耗时0ms
     */
    private fun findBestFontSize(): Float {
        firstLineBound.setEmpty()
        textBound.setEmpty()
        if (text.isNullOrEmpty()) {
            return maxTextSize
        }
        val preLayout = StaticLayout(
            text, paint, _maxTextWidth,
            Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, true
        )
        val lineCount = preLayout.lineCount
        if (lineCount > 0) {
            if (bestTextSizeCache.containsKey(lineCount)) {
                return bestTextSizeCache[lineCount]!!
            }
            preLayout.getLineBounds(0, firstLineBound)
        } else {
            return maxTextSize
        }
        val textPaint = TextPaint(paint)
        var bestFontSize = maxTextSize
        var low: Float = minTextSize
        var high: Float = maxTextSize
        var mid: Float
        while (low <= high) {
            textBound.setEmpty()
            mid = (low + high).div(2f)
            textPaint.textSize = mid
            val layout = StaticLayout(
                text, textPaint, _maxTextWidth,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, true
            )
            textBound.bottom = layout.height.toFloat()
            textBound.offsetTo(0f, 0f)
            // 预留1行高度
            textBound.bottom += firstLineBound.height()
            if (!availableViewBound.contains(textBound)) {
                high = mid - 1
                bestFontSize = high
            } else {
                bestFontSize = low
                low = mid + 1
            }
        }
        if (USE_CACHE) { //统一开关控制是否使用缓存机制
            bestTextSizeCache.put(lineCount, bestFontSize)
        }
        return bestFontSize
    }
// --------------------------------
// 应用到TextView
textview.textSize = findBestFontSize()
```

- 示例视频

https://user-images.githubusercontent.com/4396725/149644185-bd4bf4db-e3ba-4fee-aa78-d9dc7d392ac7.mp4

# 场景3:描边投影

给文本里每个文字添加自定义`ReplacementSpan`类，通过重写`draw`方法来实现描边跟投影效果。

- 描边: 通过修改`paint.style=Paint.Style.STROKE`方式，再绘制一次文字来实现。

![image](https://user-images.githubusercontent.com/4396725/149644200-c57f3c41-3740-4ecd-a5c8-60991d8c294c.png)

- 投影: 通过`canvas.translate(dx,dy)`（平移画布）方式，再绘制一次文字来实现。

![image](https://user-images.githubusercontent.com/4396725/149644205-2245f91c-bc27-4ae3-910c-b215d0db2501.png)

# 性能优化点

- 匹配字号
参考上述中的二分查找法

- 解决频繁gc bug

```kotlin
// 在输入过程中不断使用 SpannableString(text) 会出现频繁gc，造成主线程卡顿，输入过程不流畅
// 优化方案:使用 SpannableString.valueOf 取代，复用TextView控件中的SpannableString进行操作
val spannableStringBuilder = SpannableString.valueOf(text)
spannableStringBuilder.clearSpans()
```

- 文字测量对象的回收池

为了避免频繁创建对象引发gc，创建了对象回收池。当文字都被清空时，上次创建的对象进行回收处理。用户输入新文字需测量时，优先从回收池中获取旧对象，修改数据即可。
