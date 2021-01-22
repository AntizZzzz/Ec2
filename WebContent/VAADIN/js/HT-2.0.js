/**
 * 
 * 呼叫中心方法对象
 * 1.0
 * 
 * 提供方法
 * HT.call(username,exten,destNum,callbackFn)		//呼叫号码
 * HT.signIn(username,exten,callbackFn)		//签到
 * HT.signOut(username,callbackFn)			//签退
 * 
 */

var HT = {
	path : "http://192.168.1.160:8081/ec2/http/common/",//请求地址
	threadTime : 1000,		//访问请求频率1s
	connectStatus:false,	//连接状态
	showWindowStatus:false	//是否显示弹窗
};

/**
 * 签到
 * (String)username		用户名
 * (String)exten		分机
 * (function)callbackFn	回调函数
 * 
 */
HT.signIn = function(username,exten,callbackFn){
	var _self = this;
	if(username && exten){
		$.ajax({
			url :  _self.path + "loginBind",	 //地址
			data:{username:username,exten:exten},//参数
			type : "GET", 						 //方式
			cache:false,						 //不缓存 
			dataType : "jsonp", 				 //JSONP
			jsonpCallback:"callback",			 //JSONP回调函数名称
	 		timeout: 1000,						 //超时
			success : function(data) {
				if(data["code"] == 0){
					_self._timerStart(username);
				}
				callbackFn(data);
			},
			error:function(){
				callbackFn({code:-2,message:'签到请求错误'});
				_self._timerStart(username);
			}
		});
	}else{
		callbackFn({code:-1,message:'参数错误'});
	}
};

/**
 * 签退
 * (String)username		用户名
 * (function)callbackFn	回调函数
 * 
 */
HT.signOut = function(username,callbackFn){
	var _self = this;
	if(username){
		$.ajax({
			url :  _self.path + "logoutUnbind",	//地址
			data:{username:username},			//参数
			type : "GET", 						//方式
			cache:false,						//不缓存 
			dataType : "jsonp", 				//JSONP
			jsonpCallback:"callback",			//JSONP回调函数名称
	 		timeout: 60000,						//超时
			success : function(data) {
				if(data["code"] == 0){
					_self._timerStop();
				}
				callbackFn(data);
			},
			error:function(){
				callbackFn({code:-2,message:'签退请求错误'});
			}
		});
	}else{
		callbackFn({code:-1,message:'参数错误'});
	}
};

//开始连接
HT._timerStart = function(username){
	var _self = this;
	if(!_self.connectStatus){
		HT.run(username);
	}
};

HT.run = function(username){
	var _self = this;
	_self.connectStatus = true;
	_self.showWindowStatus = true;
	$.ajax({
		url :  _self.path + "timeInfo",	//地址
		data:{username:username},			//参数
		type : "GET", 						//方式
		async:false,
		cache:false,						//不缓存 
		dataType : "jsonp", 				//JSONP
		jsonpCallback:"callback",			//JSONP回调函数名称
 		timeout: 10000,						//超时
		success : function(data,textStatus, jqXHR) {
			if(data['results']['phoneNumber'] && _self.showWindowStatus){
				_self._showPhoneInbound(data);
			}
			HT.run(username);
			jqXHR = null;
		},
		complete: function (jqXHR, textStatus) { jqXHR = null;},
		error : function(jqXHR, textStatus, errorThrown) {
			HT.run(username);
			jqXHR = null;
		}
	});
};

//停止不显示弹窗
HT._timerStop = function(){
	var _self = this;
	_self.showWindowStatus = false;
};

//回调弹屏方法
HT._showPhoneInbound= function(data){
	phoneInbound(data);
};