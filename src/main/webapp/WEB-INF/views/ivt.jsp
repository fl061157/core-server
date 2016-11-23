<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" pageEncoding="utf-8"%>


<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>${ivt.title}</title>
    <meta name="format-detection" content="telephone=no">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0">
    <link rel="shortcut icon" href="/static/img/ivt/favicon.ico" type="image/x-icon"/>
    <script type="application/javascript" src="/static/js/jquery.min.js"></script>

    <script type="application/javascript">
    //初始化位置
    $(document).ready(function(){
        $('.wrapper').css('height',document.body.scrollHeight+"px");
    })
    </script>
</head>
<body>

<div class="wrapper">
    <div class="upHalf">
        <div class="avatar">
            <img class="hat" src="/static/img/ivt/hat.png"><br>
            <!---头像 动态替换--->
            <img class="circle-img" src="${user.avatar_url}">
        </div>
        <div class="name">
            <!---昵称 动态替换--->
            <span>${user.nickname}</span>
        </div>
        <div class="invite">
            <span>${ivt.invite}</span>
        </div>
        <div class="account">
            <!---账号 动态替换--->
            <span>${ivt.account}${user.account}</span>
        </div>
    </div>
    <div class="downHalf">
        <div class="download-icon">
            <span> ${ivt.isDownload}<a href="http://a.app.qq.com/o/simple.jsp?pkgname=me.chatgame.mobilecg">${ivt.download}</a></span>
            <img src="/static/img/ivt/icon.png">
        </div>
        <div class="download">
            <a href="http://a.app.qq.com/o/simple.jsp?pkgname=me.chatgame.mobilecg">${ivt.download}</a>
        </div>
    </div>
</div>
<style type="text/css">
    body {
        margin: 0;
        padding: 0;
        font: 15px Microsoft YaHei Light;
        color: #ffffff;
    }
    a {
        text-decoration: none;
        color: #fff;
    }
    .wrapper {
        max-width: 500px;
        margin: 0 auto;
        text-align: center;
        background-color: #166198;
    }
    .circle-img{
        z-index: 22;
        width: 100px;
        height: 100px;
        position: relative;
        border-radius: 50%;
        overflow:hidden;
    }
    .wrapper .upHalf{
        background: url("/static/img/ivt/bg.png") bottom no-repeat;
        background-size: cover;
        height: 55%;
    }
    .upHalf .hat{
        width: 25px;
        margin-top: 6%;
    }
    .upHalf .name{
        font-size: 25px;
        opacity: 0.5;
        margin-top: 3%;
    }
    .upHalf .invite{
        font-size: 25px;
        font-weight: bold;
        margin-top: 3%;
    }
    .upHalf .account{
        font-size: 15px;
        opacity: 0.5;
        margin: 8% 0 1% 0;
    }
    .download-icon span{
        font-size: 15px;
        display: block;
        margin-top: 10%;
    }
    .download-icon img{
        width: 60px;
        margin-top: 3%;
    }
    .download a{
        display: inline-block;
        padding: 5px 12px;
        margin-bottom: 0;
        font-size: 20px;
        font-weight: 600;
        line-height: 1.92857143;
        text-align: center;
        white-space: nowrap;
        vertical-align: middle;
        -ms-touch-action: manipulation;
        touch-action: manipulation;
        cursor: pointer;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
        /*background-image: none;*/
        border: 1px solid transparent;
        border-radius: 10px;
        background-color: #FFFFFF;
        color: #1e5f89;
        width: 60%;
        max-width: 250px;
        margin: 8% 0 3% 0;
    }
</style>
</body>
</html>