# web_socket
使用webSocket搭建后台消息推送服务，消息确认，消息日志，内网下app消息推送

#svg
顺便有个svg文件动态缩放功能，用到了jquery.svg.pan.zoom.js

具体操作左键双击可以放大，滚轮也可以操作
~~~
参考 https://github.com/jiunong/jquery-svg-pan-zoom
~~~

访问地址：localhost:port(9999)/i/{svgfilename}

~~~
    /**WebResourceConfig.java 配置svg文件位置*/
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/svg/**").addResourceLocations("file:F:/data/svg/");
    }
~~~

#Others
D3 操作svg 编写点击事件
