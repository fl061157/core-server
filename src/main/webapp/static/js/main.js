$(function(){
	$("img.twodc-small").hover(function(){
		$("#twodc").show();
	},function(){
		$("#twodc").hide();
	});

	init();

	$("#download-button, a.ios, a.android").click(function(){
		alert("内测中，敬请期待！");
	})   

	$("#download-button1").click(function(){
		alert("内测中，敬请期待！");
		//alert(1);
		//var href = $("#download-button").attr("href");
		//var ua  = navigator.userAgent;
		// if (/(MicroMessenge)r/i.test(ua)) {
		// //alert(navigator.userAgent);
		// 	alert("如遇无法下载的情况，请点击右上角，选择在浏览器中打开后下载。");
		// }
	})    
})

function init(){
    //var href = $("#button").attr("href");
    var ua  = navigator.userAgent;
    if(/(MicroMessenge)r/i.test(ua)){
        $("#download-button").attr("href","javascript:;")
        $("#download-button").attr("id","download-button1");
        $("#download").html("<img src='/static/images/mobile/download-bg.png' alt='download' id='download-button1' />")
    } else if (/(Android)/i.test(ua)) {
        //alert(navigator.userAgent);
        $("#download-button").attr("href","javascript:;");
        //alert(href);
    } else if(/(iPhone|iPad|iPod|iOS)/i.test(ua)){
        //alert(navigator.userAgent);
        $("#download-button").attr("href","javascript:;")
        //alert(href);
    }
}