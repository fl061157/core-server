<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <link rel="stylesheet" type="text/css" href="/static/css/wrapper.css" />
    <title></title>
</head>
<body>

<div class="head_div"></div>
<div class="top_div" >

    <div class="top_1" >
        <div >
        <img src="${user.avatar_url}" class="img-circle">
        </div>
    </div>
    <div class="top_2">
        <span>${user.nickname}</span><br>
        <span>ID:${user.account}</span>
    </div>

</div>
<div class="middle_div">
    <img src="/qr/barcode/${user.id}">
</div>

<div class="bottom_div" >
    <input type="hidden" value="${user.language}" id="lang">

    <button type="button" class="btn btn-info" onClick="downloadApp();">
        <img src="/static/images/logo_1.png" style="width:30px;float: left">&nbsp;
        <span>Download ChatGame</span>
    </button>
    <p>
        <a href="cgtp://name_card?uid=${user.id}" style="color:#1d9de7">Open In ChatGame</a>
    </p>
</div>

<!--微信下载提醒-->
<div class="x-landing-wrapper">
    <div class="x-landing-overlay"></div>
    <div class="wx-tips">
        <h2>点击这里下载哦！</h2>
        <p class="p1">1、点击右上角菜单。</p>
        <p class="p2">2、选择【浏览器】打开，再下载。</p>
        <div class="close">关闭</div>
    </div>
</div>

<style type="text/css">

    html,body{
    width: 100%;
    height: 100%;
    margin: 0px;
    padding: 0px;
    font-family: "微软雅黑", Helvetica, "Helvetica Neue", "segoe UI Light", "Kozuka Gothic Pro";
    }
    .head_div{
    width: 100%;
    height: 3%;
    min-height: 10px;
    }

    .top_div{
    width: 100%;
    /*height: 20%;*/
    }

    .top_1{
    width: 100%;
    /*height: 100%;*/
    /*float: left;*/
    text-align:center;
    vertical-align:middle;
    }
    .top_1 img{
    vertical-align:text-top;
    }
    .top_2{
    vertical-align: middle;
    text-align: center;
    }
    .top_2 span{
    font-size: 13px;
    color: #8a8a8a;
    }
    .middle_div{
    width:100%;
    height:40%;
    text-align: center;
    vertical-align: middle;
    }
    .bottom_div{
    width: 100%;
    height:30%;
    text-align: center;
    }
    .img-circle {
    z-index: 22;
    width: 60px;
    height: 60px;
    position: relative;
    /*margin-right: 15px;*/
    border-radius: 50%;
    overflow:hidden;
    border: 2px solid;
    border-color: #ffffff;
    }
    .btn {
    display: inline-block;
    padding: 14px 12px;
    margin-bottom: 0;
    font-size: 16px;
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
    background-image: none;
    border: 1px solid transparent;
    border-radius: 10px
    }
    .btn span{
    margin: 22px auto;
    }
    .btn-info {
    color: #fff;
    background-color: #1d9de7;
    border-color: #1d9de7
    }
</style>
<style type="text/css" href=""></style>
<script type="application/javascript" src="/static/js/jquery.min.js"></script>
<script type="application/javascript">

    $(function() {
        var lang = $('#lang').val();
        if("zh" != lang){
            $('.x-landing-wrapper h2').html('Install Now！');
            $('.x-landing-wrapper .p1').html('1、Click to open the menu on upper right corner.');
            $('.x-landing-wrapper .p2').html('2、Choose [ Browser ] to open and download it directly. ');
            $('.x-landing-wrapper .close').html('close');
        }
    })

    function downloadApp(){
        <%--var is_weixin = navigator.userAgent.match(/(MicroMessenger)/i) != null;--%>
		<%--var is_ios = navigator.userAgent.toLowerCase().match(/(iPhone|iPad|iPod|iOS)/i) != null;--%>
		<%--var is_android= navigator.userAgent.match(/(Android)/i) != null;--%>

        <%--if(is_weixin) {--%>
            <%--$('.x-landing-wrapper').show();--%>
            <%--return;--%>
        <%--}else--%>
		<%--if(is_ios){--%>
			<%--location.href = "https://itunes.apple.com/cn/app/chatgame/id904202178?mt=8";--%>
		<%--}else{--%>
			<%--location.href = "http://www.chatgame.me/static_file/ChatGame.apk";--%>
		<%--}--%>
        location.href = "http://a.app.qq.com/o/simple.jsp?pkgname=me.chatgame.mobilecg";
	}


    $('.x-landing-wrapper .close').on('click', function () {
        $('.x-landing-wrapper').hide();
    })
</script>
</body>
</html>

