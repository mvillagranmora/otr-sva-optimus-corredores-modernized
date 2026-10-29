var sesion = {};
const admin = BCS_ACCESOS["ADMIN"];

sesion.data = {};

sesion.volviendoAlLogin = false;
sesion.VolverLogin = function(msg) {
	if(!sesion.volviendoAlLogin){
		$("body").append("<div id='loading-overlay' class='overlay'><div class='k-loading-image'></div></div>");

		if(msg && msg!==""){
			alert(msg);
		}
		if (BCS_SESION.IdEntidadSSO != '') {
			window.location.href = "/BSCL/Plataforma/UI/SSOLogout?a="+BCS_SESION.IdEntidadSSO;
		}
		else {
			window.location.href = "/BSCL/Plataforma/UI/optimus?p=login";	
		}
		
		sesion.volviendoAlLogin = true;		
	}
};	
sesion.init = function() {	
	sesion.PKEY="pref"+'_'+BCS_SESION.IdEmpresa+'_'+BCS_SESION.IdUsuario;
	var get = (localStorage.getItem(sesion.PKEY)) ? "local" :"all";
	//sesion.loadPreferencias(get);
	sesion.restorePreferencias();
};
sesion.restorePreferencias = function() {
	sesion.data = BCS_PREFERENCIAS;
	try {
		var defaultGridKey = BCS_SESION.IdEmpresa + '::' + BCS_SESION.IdUsuario + '::DEFAULT-GRID';
		sesion.data[defaultGridKey] = BCS_PREFERENCIAS_GRILLA;
	}
	catch(e) {
		console.warn("BCS_PREFERENCIAS_GRILLA", e);
	}

	//data=JSON.parse(localStorage.getItem(sesion.PKEY));
	sesion.data = JSON.parse(JSON.stringify(sesion.data).replace(/\\"/g,"'").replace(/:"/g,":").replace(/",/g,",").replace(/"}/g,"}").replace(/'/g,"\""));
	//sesion.data = $.extend( {}, data, sesion.data );

	localStorage.setItem(sesion.PKEY, JSON.stringify(sesion.data));
}
sesion.savePreferencias = function(conf) {
	//TODO: guardar en base datos preferencias
	
	var datosPreferencias= {
			p_IdEmpresa : BCS_SESION.IdEmpresa,
			p_IdUsuario : BCS_SESION.IdUsuario,
			p_IdModulo: 'PLA',
			p_Configuracion: conf
    };
	$.ajax({
		method: "POST",
		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1701",
		data: JSON.stringify(datosPreferencias),
		contentType: 'application/json',
	    dataType: 'json'
	}).fail(function(err) {
	    console.log(err);
	 });
	
	//if (typeof sesion.PKEY === 'undefined') return;
}
sesion.loadPreferencias = function(key) {
	//TODO: carga de la base datos preferencias, merge db con defaults
	var data =null;
	if (typeof sesion.PKEY === 'undefined') return;
	if (key==='local') {
		data=JSON.parse(localStorage.getItem(sesion.PKEY));
		sesion.data = $.extend( {}, data, sesion.data );
	}else {
		sesion.restorePreferencias();
	}	
	
}
sesion.setPreferencias = function(key,val) {
	var nkey=BCS_SESION.IdEmpresa+'::'+BCS_SESION.IdUsuario+'::'+key;
	if (typeof sesion.PKEY === 'undefined' || sesion.data[nkey] === val) return;
	sesion.data[nkey]=val;
	localStorage.setItem(sesion.PKEY, JSON.stringify(sesion.data));
	sesion.savePreferencias([{"Llave" : nkey+"", "Valor": JSON.stringify(val)}]);
};
sesion.getPreferencias = function(key) {
	key=BCS_SESION.IdEmpresa+'::'+BCS_SESION.IdUsuario+'::'+key;
	if (typeof sesion.PKEY === 'undefined') return;
	var data = sesion.data[key];
	if (typeof data !== 'undefined') {
		return data;
	}
};	
sesion.getAccess = function(key)  {
	if (admin || typeof BCS_ACCESOS[key] === 'undefined' || BCS_ACCESOS[key].Existe==='S') 
		return true;
	else
		return false;
}


// Inicio: Funciones para registrar log servicio ventana

sesion.getVentanaActiva = function() {
    var ventanas = $(".k-window");
    
    if (!ventanas.length) {
        return null;
    }
    
    var zIndexValues = ventanas.map(function() {
        return parseInt($(this).css('z-index'), 10) || 0;
    }).get();

    var maxZIndex = Math.max.apply(null, zIndexValues);
    var ventanaActiva = ventanas.eq(zIndexValues.indexOf(maxZIndex))[0];

    return ventanaActiva;
};



sesion.saveLogVentana = function(requestConfig) {
	
	var ventanas = [];
	$(".k-window").each(function() {
	    var ventanaId = this.id;
	    var ventanaTitle = $(this).find('.k-window-title').text();
	    var ventanaStyle = this.style.cssText;
	    
	    ventanas.push({
	        id: ventanaId,
	        title: ventanaTitle,
	        //zindex: ventanaStyle.match(/z-index:\s*(\d+);/) ? ventanaStyle.match(/z-index:\s*(\d+);/)[1] : null
	        zindex: ventanaStyle.substring(ventanaStyle.indexOf("z-index: ")+8).substring(0,ventanaStyle.substring(ventanaStyle.indexOf("z-index: ")+8).indexOf(";"))
	    });
	});

	if (!ventanas.length) 
        return ;

	var ventanaActiva = ventanas.reduce(function(prev, current) {
	    return (parseInt(prev.zindex, 10) > parseInt(current.zindex, 10)) ? prev : current;
	}, {});
	
    if (ventanaActiva && requestConfig.url && requestConfig.url.match(/t=(\d+)/)) {
        var matchResult = requestConfig.url.match(/t=(\d+)/);
        var matchValue = matchResult[1];

        if (matchValue !== "600017" && matchValue !== "700729" && matchValue !== "171721") {
            var datosVentana = {
                p_IdVentana: ventanaActiva.id.replace(/^Win_/, ''),
                p_TituloVentana: ventanaActiva.title,
                p_IdServicio: matchValue,
                p_IdUsuario: BCS_SESION.IdUsuario,
            };

            $.ajax({
                method: "POST",
                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=171721",
                data: JSON.stringify(datosVentana),
                contentType: 'application/json',
                dataType: 'json'
            }).fail(function(err) {
                console.log(err);
            });
        }
    }
}

// Fin: Funciones para registrar log servicio ventana

angular.module('ngBcsComponents').run(function($rootScope,bcsComunes,$q) {
	//Inicia sesion
	sesion.init();
	$rootScope.SesionIdEmpresa 					= BCS_SESION.IdEmpresa;
	$rootScope.SesionIdUsuario 					= BCS_SESION.IdUsuario;		
	$rootScope.SesionEmpresa					= BCS_SESION.NombreEmpresa;
	$rootScope.SesionFechaSistema 				= BCS_SESION.FechaSistema;
	$rootScope.SesionUsuario 					= BCS_SESION.NombreUsuario;
	$rootScope.SesionCorredoraIdSocioDeNegocio 	= BCS_SESION.IdSocioDeNegocio;
	$rootScope.SesionIdMonedaSistema 			= BCS_SESION.IdMonedaSistema;
	$rootScope.SesionIdPaisSistema 				= BCS_SESION.IdPaisSistema;
	$rootScope.SesionBolsaDefectoSistema 		= BCS_SESION.BolsaDefectoSistema;
	$rootScope.SesionIdEmpleado			 		= BCS_SESION.IdEmpleado;
	$rootScope.SesionIdPersonaCorredora			= BCS_SESION.IdPersonaCorredora;
	$rootScope.SesionIdCorredoraSistema			= BCS_SESION.IdCorredoraSistema;
	$rootScope.SesionIdEntidadSSO				= BCS_SESION.IdEntidadSSO;
	$rootScope.SesionSSORedirectURL				= BCS_SESION.SSORedirectURL;
	$rootScope.SesionLocale						= CONF_LOCALE;
	$rootScope.SesionTipoCanalIngreso	 		= 'AGE';
	$rootScope.SesionToken = function() {
		return util.ObtenerToken();
	};

	//Enlace directo al singleto de ventana	
	$rootScope.ventana=bcsComunes.ventana;

	//Solo para efectos de compatibilidad
//	$rootScope.SesionPromise = new Promise(function(resolve, reject) {       		
	$rootScope.SesionPromise = $q(function(resolve, reject) {	// Promise -> $q para compatibilidad con cef 3.16
      	window.setTimeout(function() {
      		console.warn('ADVERTENCIA! Variables de Sesion se obtienen directamente sin necesidad de promesas. Ej. $rootScope.SesionXXX' ,new Error().stack);
      		resolve();
      	},0);
	});
	
	//Valores por Defecto del Sistema
	var objObj = {};
	$.each(BCS_SESION.ValoresDefectoSistema,function(key,value){
		if(!objObj[value.IdValorDefectoSistema]) objObj[value.IdValorDefectoSistema] = {};
		objObj[value.IdValorDefectoSistema] = value.Valor;
	});
	$rootScope.SesionValoresDefectoSistema = objObj;

	window.DD_RUM && window.DD_RUM.setUserProperty('id', $rootScope.SesionIdUsuario);
	window.DD_RUM && window.DD_RUM.setUserProperty('name', $rootScope.SesionUsuario);
	window.DD_RUM && window.DD_RUM.setUserProperty('client', $rootScope.SesionEmpresa);
});
/***************************************************************
 *   INTERCEPTORES DE TOKEN - MUY IMPORTANTE!
 ***************************************************************/
//Interceptor (ANGULAR )HTTP / RESOURCE
angular.module('ngBcsComponents').config(['$httpProvider',function($httpProvider) {
        $httpProvider.interceptors.push('HTTPInterceptor');
    }])
    .factory('HTTPInterceptor',['$q',function($q) {
        return {
            responseError: function(response){
                //console.log('HTTP - INTERCEPTA ERROR',response);
            	if (response.status == 401)  { sesion.VolverLogin("Su sesión ha sido caducada por el sistema."); }                
                return $q.reject(response);
            },
            request: function(config){
                //console.log('HTTP - INTERCEPTA REQUEST',config);
                config.headers['X-SESSION-TOKEN'] = util.ObtenerToken();   		
				sesion.saveLogVentana(config);
                             
                return config;
            }            
        };
    }]);
//Interceptores (KENDO) jquery
$(document).ready(function() {
    $.ajaxPrefilter(function( options, originalOptions, jqXHR ) {
        //console.log("JQUERY - INTERCEPTA REQUEST",jqXHR,options,originalOptions); 
        jqXHR.setRequestHeader('X-SESSION-TOKEN', util.ObtenerToken());
		sesion.saveLogVentana(originalOptions);        
        
        return true;
    });
    $(document).ajaxComplete(function (event,jqXHR,ajaxOptions) {
    	 //console.log("JQUERY - INTERCEPTA RESPONSE",event,jqXHR,ajaxOptions);
    	if (jqXHR.status==401) { sesion.VolverLogin("Su sesión ha sido caducada por el sistema."); }       
    	if (jqXHR.status==0)   { sesion.VolverLogin("Error grave de comunicación con el servidor."); }   
    });   
});  
					
/***************************************************************
 *   INTERCEPTORES DE ERRORES
 ***************************************************************/
angular.module('ngBcsComponents').config(function($provide) {
    $provide.decorator("$exceptionHandler", function($delegate) {
		return function(exception, cause) {
			$delegate(exception, cause);
			try {
				var angRoot=angular.element(document.documentElement).data().$scope;		
				var params={ };
				params.mensaje = exception.message;
				params.conflictos = {};
				var errorObj={};
				var URL = window.location.protocol + "//" + window.location.host;
				errorObj.Descripcion=exception.stack.replace(new RegExp(URL, 'g'), "");
				params.conflictos.errores = [ errorObj ];			
				angRoot.InjectbcsComunes.msg('error','ERROR',params.mensaje); 
				angRoot.InjectbcsComunes.ventanaAbrir("VentanaMensajeResultado",params,false,false);
			}catch(err) {};
		};
	});
});
