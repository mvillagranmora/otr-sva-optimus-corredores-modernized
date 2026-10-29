/********************************************************
Definicion de dependencias de aplicacion principal
********************************************************/
angular.module('ngBcsComponents', [
									'oc.lazyLoad',
									'ui.bootstrap',
									'ui.utils',
									'ui.router',
									'ngAnimate',
									'pascalprecht.translate',       //Angular Translate
									'toaster',						// Notificaciones Emergentes
									'ngResource' ,
									'kendo.directives',
									'ui.select',
									'ngSanitize',
									'ModuloPlataforma',
									'ModuloCorredor',
									'ModuloTesoreria',
									'ModuloPortafolio',
									'ModuloCustodia',
									'ModuloOrdenes',
									'ModuloCuentaInversion',
									'ModuloSocioNegocio',
									'ModuloFacturacion',
									'ModuloNormativa',
									'ModuloHomologacion',
									'ModuloContabilidad',
									'ModuloMesa'
									]);
/**************************************************************
 * Generales de Comportamiento
 *************************************************************/
$(document).ready(function () {
	
});
/*******************************************************
Configuracion
********************************************************/
angular.module('ngBcsComponents').config(['$ocLazyLoadProvider', function($ocLazyLoadProvider) {
	$ocLazyLoadProvider.config({
		debug : false,
		events: false,
		modules: [ ]
	});
}]);

angular.module('ngBcsComponents').config(['$translateProvider', function($translateProvider) {	
	sesion.init();
	var verdicc=sesion.getPreferencias("VerDiccionario"); 
	if (verdicc!==true) $translateProvider.translations('bcs',BCS_DICCIONARIO);
	$translateProvider.preferredLanguage('bcs');
}]);
angular.module('ngBcsComponents').config(['$logProvider', function($logProvider) {
	$logProvider.debugEnabled(true);
}]);
angular.module('ngBcsComponents').config(['$stateProvider','$urlRouterProvider', function($stateProvider,$urlRouterProvider) {
	$urlRouterProvider.otherwise('/home');
	//parametros para ventanas
	var vurl='/access/:keywin';
	var vpar={ keywin: { squash: true, value: null } };   
	for (var i=0;i<10;i++) {
		vurl+='/:param'+i+'';
		vpar['param'+i] = { squash: true, value: null };
	}	
	$stateProvider.state('access', {
		url: vurl, 
		params: vpar ,
		template: '<div ng-controller="AccessCtrl" ></div>',
		controller :  function($scope,$rootScope,$stateParams,bcsComunes) {
							$rootScope.$watch('PaginaCargada',function (nval,oval) {
								if (nval==true) {
									var pobjs={};
									var index=0;
									for (key in  BCS_VENTANA[$stateParams.keywin].PARAMS) {
										if ($stateParams['param'+index] != undefined) {
											pobjs[key]=$stateParams['param'+index]; 
										}
										index++;
									}									
									bcsComunes.log().debug('BcsComponents: AccessCtrl ',pobjs);
									bcsComunes.ventanaAbrir($stateParams.keywin,pobjs);										
								}
							});
		}
	});
}]);
/*******************************************************
Ejecucion
********************************************************/
angular.module('ngBcsComponents').run(function($rootScope,$log,$state) {
	$log.debug('BcsComponents: Run');
	//safeApply
    $rootScope.safeApply = function(fn) {
        var phase = $rootScope.$$phase;
        if (phase === '$apply' || phase === '$digest') {
            if (fn && (typeof(fn) === 'function')) {
                fn();
            }
        } else {
            this.$apply(fn);
        }
    };
    //Inicio de Ejecucion
    $rootScope.PaginaCargada=false;
	//state info
    $rootScope.$state = $state;
    
});
/*******************************************************
Controlador General
********************************************************/
angular.module('ngBcsComponents').controller('AppCtrl', function( 	$scope ,
																	$rootScope,
																	$ocLazyLoad,
																	$resource,
																	bcsServicioPlataforma,
																	bcsComunes,
																	$http,
																	$log,
																	$compile,
																	$interval,
																	$state,
																	$window,
																	$timeout,
																	$translate
																) {
	$log.debug('BcsComponents: Controller');
	$rootScope.InjectbcsComunes=bcsComunes;
	
	//Modo de navegacion y comportamiento de ventanas
	$rootScope.ModoNavegacion=util.ObtenerModo();
	$scope.overlay = bcsComunes.overlay;

	$log.debug('  Modo Navegacion:',$rootScope.ModoNavegacion);
	
	//ambiente
	$rootScope.Ambiente = BCS_AMBIENTE;
//CELCONDOR-417	
	$rootScope.urlWikiOpt = BCS_CONFIGURACION.PLATAFORMA["URLWIKIOPT"];
//CELCONDOR-417
	
	if ($rootScope.Ambiente==='DES')       $rootScope.UI_AMBIENTE='UI_AMBIENTE_DESARROLLO';
	else if ($rootScope.Ambiente==='QA')   $rootScope.UI_AMBIENTE='UI_AMBIENTE_QA';
	else if ($rootScope.Ambiente==='UAT')  $rootScope.UI_AMBIENTE='UI_AMBIENTE_CERTIFICACION';
	else if ($rootScope.Ambiente==='PRO')  $rootScope.UI_AMBIENTE='UI_AMBIENTE_PRODUCCION';	
	else 								   $rootScope.UI_AMBIENTE='UI_AMBIENTE_SIN_DEFINIR';
	$log.debug('  Tipo Ambiente:',$rootScope.Ambiente);
	
	//para borrar
	if ($rootScope.Ambiente!=='PRO') {
		$scope.$watch('overlay',function(nval,oval) {
			if (nval.encendido===true) {
				alert("DESARROLLADOR: NO USAR overlay.encendido . USAR bcsComunes.ventanaOverlay y uso correcto de ventanas.");
			}
		},true);		
	}

	//menues
	for(var key in BCS_VENTANA)   BCS_VENTANA[key].KEY=key;
	if ($( '#Menu' ).length > 0)  {
		$log.debug('  Cargando Menu de la izquierda');
		$scope.MenuLeft=BCS_USER_MENU;
	}

	$scope.AbrirVentana= function(state,prop,unique) {
		bcsComunes.ventanaAbrir(state,prop,unique);
	};

	$scope.RestauraVentanaId= function(winid) {
		bcsComunes.ventanaRestaura(winid);
		
	};
	$scope.RestauraVentanaIdC= function(winid) {
		bcsComunes.ventanaRestauraC(winid);
		
	};
	$scope.cerrarVentanaC= function(winid) {
		bcsComunes.ventanaCerrar(winid);
		
	};
	$scope.closeAllWindows= function(ventanaManager){
		if(ventanaManager){
			if(ventanaManager.length>0){
				for(let i=0; i< ventanaManager.length; i++){
					bcsComunes.ventanaCerrar(ventanaManager[i].id);
				  }
			}
		}
	}

	$scope._fechasistema = BCS_TIMESTAMP_LOCAL; //timestamp con fecha y hora de la carga del index.
	
	//Obtener fecha del sistema cada 15 mins
	$scope.segundos=0;
	$scope.fnActualiza = function() {
		var datos = {};
		$scope.segundos=0;
		$scope._fechasistema=undefined;		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
    	bcsServicioPlataforma.Fechas(datos,
    		function(result) { //OK
    			$scope.segundos=0;
    			$scope._fechasistema = result.P_OUT_FECHAHORASISTEMALOCAL;
    			$rootScope.SesionFechaSistema = result.P_OUT_FECHASISTEMA;
    		},
    		function(result) {//ERR
    			bcsComunes.log().error("No se pudo obtener la fecha de sistema");
    		});
    };
    $scope.timer = $interval($scope.fnActualiza, 15*60*1000);

	//Suma cada 1 segundos
    $scope.timer = $interval(function(){
    	$scope.segundos++;
		if ($scope._fechasistema !== undefined) {
			$rootScope.SesionFechaSistema =  $scope._fechasistema.substring(0, 10); //Idem a BD yyyy-mm-dd 
			$rootScope.SesionHoraSistema = util.date($scope._fechasistema).add(util.duration({'seconds' : $scope.segundos})).format("HH:mm:ss");
			$scope.InfoFecha  = util.date($scope._fechasistema).format("L");
			$scope.InfoHora   = util.date($scope._fechasistema).add(util.duration({"seconds": $scope.segundos})).format("HH:mm");			
		}else {
			if ($scope.segundos>5) $scope.fnActualiza();
		}
    }, 1000);

    $scope.estadoModulo = function() {
    	var datosEstadoModulo = {};
		datosEstadoModulo.p_IdEmpresa = $scope.SesionIdEmpresa;
		datosEstadoModulo.p_IdUsuario = $scope.SesionIdUsuario;
		datosEstadoModulo.p_IdModulo = '';
		var errorEstadoModulo = function(respuesta){	
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_NO_SE_OBTUVO_CIERRE_MODULO"));
			return;
		};
		var successEstadoModulo = function(respuesta){
			var listadoModulos = respuesta.p_out_ListaModulos;
			if (listadoModulos !== undefined) {
				for (var i = 0; i < listadoModulos.length; i++) {
					BCS_ESTADO_MODULO[listadoModulos[i].NombreModulo] = listadoModulos[i].IdEstadoModulo;
				}
			}
		};
		bcsServicioPlataforma.ConsultarEstadoModulo(datosEstadoModulo,successEstadoModulo,errorEstadoModulo);
    };

    $scope.timer = $interval($scope.estadoModulo, 15000);

    //termino de carga pagina
    Pace.on('done', function() {
    	$timeout(function() {
    		window.document.title = "Sistema Gestion Corredora OPTIMUS SAP";
    		$rootScope.VersionSistema=BCS_VERSION;
    		$rootScope.PaginaCargada=true;
    		$('.dropdown.keep-open').on({
    		    "shown.bs.dropdown": function() { 
	    		    this.closable = false; 
    		    },
    		    "click":             function() { this.closable = true; },
    		    "hide.bs.dropdown":  function() { 
    		    	if ($scope.menuMinimizado) return true; else return this.closable; 
    		    }
    		});
    		$("#Pagina").show();
    	},500);
    });

	var mylog=bcsComunes.log();
	
	//Cierre de Sesion
	$scope.cerrarSesion = function(){
		
		var urlCerrarSesion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=182";
		var r = $resource("", {},
	    {
			'CerrarSesion': { method: "POST", url: urlCerrarSesion}
		});
		
		var datosSesion= {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_Token:sessionStorage.Token
		};
		
		var post = r.CerrarSesion(datosSesion);
		post.$promise.then(function(result){
			if(result.P_OUT_ESTADO!=null && result.P_OUT_ESTADO==='ok+++'){
				bcsComunes.msg("INFO",'INFO',$translate.instant("LOGOFF"));
				sesion.VolverLogin("");
			}else{
				for(var i in result.P_OUT_ERRORESDETALLE){
					bcsComunes.msg("error",'Error',$translate.instant(result.P_OUT_ERRORESDETALLE[i].Codigo));	
				}
				sesion.VolverLogin("Su Sesión ha expirado");
			}
		});
	};
	//funciones para cerrar sesion por inactividad del usuario
	(function(){
		var t;
		window.onload = resetTimer;
		// DOM Events
		document.onmousemove = resetTimer;
		document.onkeypress = resetTimer;

		function logout() {
			$scope.cerrarSesion();
			sesion.VolverLogin("Su Sesión ha expirado");
		}

		function resetTimer() {
			clearTimeout(t);
			t = setTimeout(logout, ($scope.SesionValoresDefectoSistema.TiempoInactividadLogoff || 15) * 60000);
		}
	}());

	
	//Cambio de password
	$scope.cambiarPassword = function(){
		bcsComunes.ventanaAbrir("CambiarClaveUsuario",{},false,true);
	};
	
	$scope.asuntosPendientes = function(){
		bcsComunes.ventanaAbrir("ConsultarAsuntosPendientes",{},false,false);
	};

	$scope.showDropdown = false; //para ocultar menu de mensajes
	$scope.showHTMLContent = false; //ocultar menu mensajes
	$scope.mostrarIcono = true; // ocultar icono de busqueda de left-navbar


	/*Para realizar los cambios de posiocion de el main menu*/


	$scope.toggleMenu = function() {
		$scope.isMenuVisible = !$scope.isMenuVisible;


	
			if ($scope.isMenuVisible) {
				var navbar = angular.element('#navbar');
				navbar.css({
					'margin-left': '-180px'
				});
	
			
	
				var icon = angular.element('nav.sidebar .navbar-nav .dropdown-toggle .glyphicon');
				icon.attr('style', 'float: right !important;')
	
				var a = icon.parent()
				var menu = a.parent()
				menu.removeClass('open');
	
	
	
			} else {
				var navbar = angular.element('#navbar');
				navbar.css({
					'margin-left': '0px'
				});
	
				var icon = angular.element('nav.sidebar .navbar-nav .dropdown-toggle .glyphicon');
				icon.attr('style', 'float: left !important;')
				
				
	
			}
		
		

		
	};
    // visualizacion de los botones

	$scope.mostrarBotones = false

	$scope.hasExceededWidth = function() {
		
		var spanCar = document.getElementById('spanElement');
		if(spanCar.scrollWidth > spanCar.clientWidth){
			$scope.mostrarBotones = true
		}
		else{
			$scope.mostrarBotones = false
		}
	  };

	  var hasExceededWidthExecuted = false;

		$scope.$watch(function() {
		return angular.element(document.getElementById('spanElement')).text();
		}, function(newVal, oldVal) {
		if (!hasExceededWidthExecuted && newVal !== oldVal) {
			hasExceededWidthExecuted = true;
			$scope.hasExceededWidth();
			hasExceededWidthExecuted = false;
		}
		});

	  
	

	

	

	
	// control del carrosel

	$scope.scrollLeft = function() {
		var spanElement = document.getElementById('spanElement');

		var scrollAmount = 100;
		var newScrollLeft = spanElement.scrollLeft - scrollAmount;
		var minScrollLeft = 0;

		if (newScrollLeft <= minScrollLeft) {
			newScrollLeft = minScrollLeft;
		  }
	
		  spanElement.scrollTo({
			left: newScrollLeft,
			behavior: 'smooth'
		  });
	  };

	  $scope.scrollRight = function() {
		var spanElement = document.getElementById('spanElement');
		var scrollAmount = 100;
		var newScrollLeft = spanElement.scrollLeft + scrollAmount;
		var maxScrollLeft = spanElement.scrollWidth - spanElement.clientWidth;

		if (newScrollLeft >= maxScrollLeft) {
			newScrollLeft = maxScrollLeft;
		  }
	
		  spanElement.scrollTo({
			left: newScrollLeft,
			behavior: 'smooth'
		  });
	  };
	

	  $scope.$on('$viewContentLoaded', function () {
		$timeout(function () {
			angular.element('body').addClass('bg-ready');
	
		});
	});


	/*Ocultar mensaje de bienvenida*/
	
	$scope.hiddenWelcome = function(){
		var welcome = angular.element('#Welcome2');
				welcome.css({
					'display': 'none'
				});
	}

	$scope.displayWelcome = function(){
		var welcome = angular.element('#Welcome2');
				welcome.css({
					'display': 'block'
				});
	}

	$scope.rightSidebar = false;
	$scope.rightProgress = false;
	$rootScope.progreso = '';
	
	/*Para el despligue del menu de mensajes*/ 

	$scope.loadHTMLContent = function() {
		
		if($scope.rightSidebar){
			$scope.rightSidebar = false
		}
		if($scope.rightProgress){
			$scope.rightProgress = false;
		}
		$scope.showHTMLContent = !$scope.showHTMLContent;
	  };

	  $scope.loadHTMLContentInstrumentos = function() {

		if($scope.showHTMLContent){
			$scope.showHTMLContent=false;
		}
		if($scope.rightProgress){
			$scope.rightProgress = false;
		}
		$scope.rightSidebar = !$scope.rightSidebar;
	  };
	  $scope.loadHTMLContentProgreso = function() {

		if($scope.showHTMLContent){
			$scope.showHTMLContent=false;
		}
	
		$scope.rightProgress = !$scope.rightProgress;
	  };

	$scope.zPersonas= function(){
		if($scope.rightSidebar || $scope.showHTMLContent){
			$scope.showHTMLContent = false
			$scope.rightSidebar= false
		}
		if($scope.rightProgress){
			$scope.rightProgress = false;
		}
	}

	$scope.toggleDropdown = function() {
		$scope.showDropdown = !$scope.showDropdown;
	  };

	  $scope.preventClose = function($event) {
		$event.stopPropagation();}


	// Para mostrar mensajes en menu-noti
	$scope.datars = bcsComunes.logMensajes.data;


	//icono left bar

	$scope.leftBarOpen= function(){
		if($scope.isMenuVisible){
			$scope.toggleMenu();
		}
	}
	
	//Busqueda de ventanas
	var lista = [];
	
	$.each($scope.MenuLeft,function(key,val) {
		$.each(val.SUBMENU,function(key2,val2) { 
			if (val2!= undefined && val2.SEPARATE !== true  && val2.WIN !==undefined && val2.WIN.ENABLE === true && (val2.QA === undefined || (val2.QA===true && $rootScope.Ambiente !== 'PRO'))) {
				var t= { 
					title: $translate.instant(val2.WIN.TITLE),
					code: val2.WIN.KEY,
					internal : $translate.instant(val2.WIN.TITLE).replace(/á/g, 'a').replace(/é/g, 'e').replace(/í/g, 'i').replace(/ó/g, 'o').replace(/ú/g, 'u'),
					modulo: $translate.instant(val.TITULO),
					img: val.WIN.IMAGE
				}
				lista.push(t);
				/* var menu = lista.find(e => e.internal === t.internal);
				//Si un menu esta repetido debe aparecer solo una vez en el buscador
				if (menu == undefined || menu == null)
				{
					lista.push(t);
				} */
				
			}
		})
});

		$scope.d = {};
		$scope.d.buscarVentanaOptions = {
			dataSource: {
				data: lista
			},
			filter: "contains",
			select: function(f) {
				bcsComunes.ventanaAbrir(this.dataItem(f.item).code);
				$scope.d.buscarVentanaWidget.value("");
				$scope.d.buscarVentanaWidget.close();
				f.preventDefault();
			},
			dataTextField : "internal",
			template: '<span class="itemBuscador" style="white-space: nowrap;"><span class="bcs-background-search"><i class="#: img # imgModuloSearch" ></i><span>#: modulo #</span></span><span class="ventanaSearch"> \ #: internal #</span></span>',
			open: function(e) {
				e.sender.list.width("auto");
			}
		};

	
	//Mensajes de websocket que llegan desde el sistema
	if (Notification && Notification.permission !== "granted") {
		console.warn("Notification:  Solicitando permisos");
		Notification.requestPermission();
	}
		
    $scope.msgWebsocket = function(message) {
    	//---:MENSAJE TIPO AVISO:---
    	if (message.Evento=='AVISO') {
        	bcsComunes.msg("info","info",message.DATA);
        	if (Notification && Notification.permission === "granted") {
	        	var notification = new Notification('AVISO', {
	        	      icon: '/BSCL/Plataforma/UI/images/aviso.jpg',
	        	      body: message.DATA,
	        	});        
        	}
    	}
    	else if(message.Evento == 'SOLICITUD_PUNTAS') {    		
    		var usuarioPermiteDistribucion = "";
    	    
	    	var tablaDatosUsuario = [];	
			var ObjUsuario = { "IdUsuario" : $scope.SesionIdUsuario };
			tablaDatosUsuario.push(ObjUsuario);

			var parametros = {};
			parametros.p_IdEmpresa		= $scope.SesionIdEmpresa;
			parametros.p_in_Usuarios	= tablaDatosUsuario;
			
			bcsServicioPlataforma.Servicios().ConsultarConfiguracionUsuario(parametros,function(respuesta){   			

	       		$.each(respuesta.P_OUT_CONFIGURACION,function(key,value){
	       			
					if (value.Codigo === "PERMITEINTERB") usuarioPermiteInterbancario = value.Valor;		
					
				}); 
	       		
	       		if (usuarioPermiteInterbancario === 'S'){	       			
					bcsComunes.ventanaMsg({mensaje: message.DATA});					
	            	if (Notification && Notification.permission === "granted") {
	    	        	var notification = new Notification('SOLICITUD_PUNTAS', {
	    	        	      icon: '/BSCL/Plataforma/UI/images/aviso.jpg',
	    	        	      body: message.DATA,
	    	        	});        
	            	}
	       		}

			});
    	}
    	
    };	
	
	//Notificaciones
	var dataSourceNotificacion = new bcs.DataSource({		
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "IdNotificacion", dir: "desc" },
			pageSize : 10,
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1381",
					dataType: "json"
				}
			},
			schema : 
			{
				model: {
					fields : {
						
						
						 IdEmpresa						: {type: "number"}
						,IdNotificacion 				: {type: "number"}
						,IdConfiguracionEvento			: {type: "number"}
						,FechaCreacion 					: {type: "date"}
						,FormaDespachoSrv 				: {type: "string"}
						,TiempoDespacho					: {type: "string"}
						,FechaDespachoOrginal			: {type: "sysdatetime"}
						,FechaDespachoRecordatorio		: {type: "sysdatetime"}
						,Enviado						: {type: "string"}
						,FechaEnviado					: {type: "sysdatetime"}
						,IdUsuarioEnviado				: {type: "number"}
						,ReEnviar						: {type: "string"}
						,ReEnviado						: {type: "string"}
						,FechaReEnviado					: {type: "sysdatetime"}
						,IdUsuarioReEnviado				: {type: "number"}
						,ConfirmacionDespacho			: {type: "string"}
						,TokenConfirmacion				: {type: "string"}
						,Confirmado						: {type: "string"}
						,FechaConfirmado				: {type: "date"}
						,AccionConfirmado				: {type: "string"}
						,IdUsuarioConfimado				: {type: "number"}
						,FechaVencimiento				: {type: "date"}
						,FechaRecordatorio				: {type: "date"}
						,Observacion					: {type: "string"}
						,Descartado						: {type: "string"}
						,IdUsuarioDescarte				: {type: "number"}
						,FechaDescarte					: {type: "sysdatetime"}
						,Enviar							: {type: "string"}
						,DireccionEvento	 			: {type: "string"}
						,Leida							: {type: "string"}
						,IdUsuario						: {type: "number"}
						,DescripcionConfiguracion		: {type: "string"}
						,IdUsuarioRecibe				: {type: "number"}
						
					}
				} 
			},
			filter :
				[ {field : "Leida",operator: "eq", value : 'N'},
		         			{field : "EnviadoSrv",operator:"eq",value: 'S'},
		         			{field : "FormaDespachoSrv ", operator:"neq", value : 'C'},
		         			{field : "IdUsuarioRecibe",operator:"eq",value: $scope.SesionIdUsuario}
						  ]
				    
		}
		
	});
	
	$rootScope.$on("CallParentMethod",function(){
		$scope.parentmethod();
	});
	
	$scope.parentmethod = function(){
		dataSourceNotificacion.fetch().then(function(){
			var datos = dataSourceNotificacion.data();
			$scope.notificaciones =datos;			
			$scope.notificacion = $.extend({},datos[0]);
			$scope.notificacion.largo = dataSourceNotificacion.total();
			$scope.notificacion.FechaDespachoOriginal = util.formatoSecondate($scope.notificacion.FechaDespachoOrginal) ;
		});
	};
	
	$scope.verMisNotificaciones = function(){		
		var id1 = bcsComunes.ventanaAbrir("MisNotificaciones",false,true);
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ });
	}; 
	
	//leer notificaciones automaticamente
	$scope.updateTimeNotificaciones = (BCS_CONFIGURACION.PLATAFORMA["EVT.TIEMPO_PULL_NOTIFICACIONES"]!==undefined) ? BCS_CONFIGURACION["EVT.TIEMPO_PULL_NOTIFICACIONES"] : 600000;
	
	$scope.leerDsNotificacionesRecibidas = function(){	
 		if ($rootScope.NotificacionesEnLinea==true) {
 			
			dataSourceNotificacion.fetch().then(function(){		
				$scope.notificacion = {};
				var count = 0;
	 	 		var data =   dataSourceNotificacion.data();
		 	 	$scope.notificacion = $.extend({},data[0]);
		 	 	$scope.notificacion.largo = dataSourceNotificacion.total();
		 	 	$scope.notificacion.FechaDespachoOriginal = util.formatoSecondate($scope.notificacion.FechaDespachoOrginal) ;
		 	 	$scope.tags = [];
		 	 	$.each(data,function(key,codigos){
  		 	 		var obj = {};
  		 	 		if(count<10){
	  		 	 		obj['FechaDespachoOriginal']  = util.formatoSecondate(codigos.FechaDespachoOrginal) ;
	  		 	 		obj['DescripcionConfiguracion'] =  codigos.DescripcionConfiguracion;
	  		 	 		$scope.tags.push(obj);
  		 	 		}
  		 	 		count++;
  		 	 	});
		 	 	 	 	
			});
		}
		$timeout(function(){$scope.leerDsNotificacionesRecibidas()},$scope.updateTimeNotificaciones);		
	};
	$timeout(function(){$scope.leerDsNotificacionesRecibidas()},$scope.updateTimeNotificaciones);	
	
	$scope.cargarRangoFechasDefecto = function(){
		var ConfiguracionDS = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
					dataType: "json"
			    }
			},
			schema: {
	        	model:
	    		{
	        		fields : {
	        			IdEmpresa: {type: "string"},
	        			CodigoParametro: {type: "string"},
	        			Descripcion: {type: "string"},
	        			Valor: {type: "number"},
					}
	    		}
	    	}
		});
	 	
	 	var filters = new Array();
		filters.push({field: "CodigoParametro", operator:"eq", value: "PLA.RangoDiasDefault"});
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
				
	 	ConfiguracionDS.query({filter:filters}).then(function(){
	 		sesion.setPreferencias("FiltroRangoDias", ConfiguracionDS.view()[0].Valor);
	 	});
	};

	$timeout(function(){$scope.cargarRangoFechasDefecto()},100);
	
	$scope.verificarVigenciaClave = function(){
		
		//si vengo por SSO, no cambio clave nunca
		if ($rootScope.SesionIdEntidadSSO !== '') return;
		var usuarioDS = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=384",
					dataType: "json",
					data:{
						$select: "\"CodigoUsuario\", \"DebeCambiarClave\", \"UltimoCambioClave\""
					}
			    }
			},
			schema: {
	        	model:
	    		{
	        		fields : {
	        			CodigoUsuario: {type: "string"},
	        			DebeCambiarClave: {type: "string"},
	        			UltimoCambioClave: {type: "date"}
					}
	    		}
	    	}
		});
	 	
	 	var filters = new Array();
		filters.push({field: "CodigoUsuario", operator:"eq", value: $scope.SesionUsuario});
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
		
		usuarioDS.query({filter:filters}).then(function(){
	 		if(usuarioDS.view().length>0){
	 			if(usuarioDS.view()[0].DebeCambiarClave==="S"){
					var datos = {};
					datos.mensaje = $translate.instant("UI_DEBE_CAMBIAR_CLAVE_OBLIGACION");
					bcsComunes.ventanaAbrir("CambiarClaveUsuario",datos,false ,true, false, true);	
	 			}
	 		}
	 	});
	};
	$timeout(function(){$scope.verificarVigenciaClave()},100);

	//validate browser version/agent && screen resolution
	(function(){		
		const regex = /(Chrome)\/(\d+)/g;
		const str = navigator.userAgent;
		let m;

		var msg = {
			P_OUT_ADVERTENCIASDETALLE : [],
			P_OUT_ERRORESDETALLE : [],
			P_OUT_ERRORESCONTADOR : 0,
			P_OUT_DEALLESCONTADOR : 0
		};

		while((m = regex.exec(str)) !== null){
			if(m[2] && m[2]<57){
				msg.P_OUT_ADVERTENCIASDETALLE.push({codigo:"UI_NAVEGADOR", Descripcion:"Se recomienda actualizar la versión de su navegador Google Chrome"});
				msg.P_OUT_DEALLESCONTADOR++;
			}
		}
		if((m = regex.exec(str))===null){
			msg.P_OUT_ADVERTENCIASDETALLE.push({codigo:"UI_NAVEGADOR", Descripcion:"Se recomienda el uso del navegador Google Chrome"});
			msg.P_OUT_DEALLESCONTADOR++;
		}
		
		if(window.screen.availWidth < 1024 || window.screen.availHeight < 768){
			msg.P_OUT_ADVERTENCIASDETALLE.push({codigo:"UI_NAVEGADOR", Descripcion:"Se recomienda configurar la resolucion de pantalla a 1280x720"});
			msg.P_OUT_DEALLESCONTADOR++;
		}

		if(msg.P_OUT_DEALLESCONTADOR>0){
			bcsComunes.ventanaMsg({
				mensaje : "UI_ADVERTENCIA",
				conflictos : util.parseServerResponse(msg)
			},bcsComunes.ventanaWinId($scope))
		}
	})();

});

//Controlador para el sidebar derecho.
angular.module('ngBcsComponents').controller('SideBarCtrl',
		function($scope,$translate,$modal,$log,$resource,$rootScope,
				bcsComunes,bcsAlerta, bcsServicioPlataforma,$timeout){
	
	var getCheckPreferencia = function(key){
		return (sesion.getPreferencias(key)!=undefined)?sesion.getPreferencias(key) : false;
	};
	//configuracion recordar cliente automatico
	$scope.recordarClienteAutomatico = getCheckPreferencia("recordarClienteAutomatico");	
	$scope.$watch('recordarClienteAutomatico',function(nval,oval) {
		sesion.setPreferencias("recordarClienteAutomatico", nval);
		if(nval==true){
			//si se activa recordar un cliente automaticamente, deshabilitar el cliente recordado por defecto.
			$scope.recordarCliente = false;
			sesion.setPreferencias("recordarCliente", false);
		}
	});
	
	//configuracion mostrar mansajeria.
	$scope.mostrarMensaje = getCheckPreferencia("showBcsMensaje");	
	$scope.$watch('mostrarMensaje',function(nval,oval) {
		sesion.setPreferencias("showBcsMensaje", nval);
	});
	
	//checkbox para habilitar al cliente recordado
	$scope.recordarCliente = getCheckPreferencia("recordarCliente");
	$scope.$watch('recordarCliente',function(nval,oval) {
		sesion.setPreferencias("recordarCliente", nval);
		if(nval==true){
			//si se activa recordar un cliente por defecto, deshabilitar el cliente recordado automaticamente.
			$scope.recordarClienteAutomatico = false;
			sesion.setPreferencias("recordarClienteAutomatico", false);
		}
	});
	
	//configuracion Ocultar Barra Inferior 
	$scope.OcultarBarraInferior = getCheckPreferencia("OcultarBarraInferior");
	$rootScope.OcultarBarraInferior = $scope.OcultarBarraInferior;
	$scope.$watch('OcultarBarraInferior',function(nval,oval) {
		sesion.setPreferencias("OcultarBarraInferior", nval);
		$rootScope.OcultarBarraInferior = nval;		
	});
	
	//configuracion ver diccionario
	$scope.VerDiccionario=false;
	if ($rootScope.Ambiente=="DES"||$rootScope.Ambiente=="QA") {
		$scope.VerDiccionario = getCheckPreferencia("VerDiccionario");
		$rootScope.VerDiccionario = $scope.VerDiccionario;
		$scope.$watch('VerDiccionario',function(nval,oval) {
			sesion.setPreferencias("VerDiccionario", nval);
			$rootScope.VerDiccionario = nval;
			 bcsComunes.msg("info","info","Para aplicar la opción de diccionario. Se requiere recargar la página.");
		});	
	}
	
	//configuracion cambiar tamaño ventana
	$scope.permiteCambioTamanoVentana = getCheckPreferencia("permiteCambioTamanoVentana");
	$rootScope.permiteCambioTamanoVentana = $scope.permiteCambioTamanoVentana;
	$scope.$watch('permiteCambioTamanoVentana',function(nval,oval) {
		sesion.setPreferencias("permiteCambioTamanoVentana", nval);
		$rootScope.permiteCambioTamanoVentana = nval;	 	
	});

	
	//configuracion Minimizar Mensaje de exito (popups)
	$scope.MinimizarMensajeExito = getCheckPreferencia("MinimizarMensajeExito");
	$rootScope.MinimizarMensajeExito = $scope.MinimizarMensajeExito;
	$scope.$watch('MinimizarMensajeExito',function(nval,oval) {
		sesion.setPreferencias("MinimizarMensajeExito", nval);
		$rootScope.MinimizarMensajeExito = nval;	 	
	});	
	
	//configuracion notificaciones en linea (popups)
	$rootScope.NotificacionesEnLinea=false;	
	$scope.NotificacionesEnLinea = getCheckPreferencia("NotificacionesEnLinea");
	$rootScope.NotificacionesEnLinea = $scope.NotificacionesEnLinea;
	$scope.$watch('NotificacionesEnLinea',function(nval,oval) {
		sesion.setPreferencias("NotificacionesEnLinea", nval);
		$rootScope.NotificacionesEnLinea = nval;	 	
	});
	
	//configuracion mostrar/ocultar menu
	$scope.menuMinimizado = getCheckPreferencia("menuMinimizado");
	$rootScope.menuMinimizado = $scope.menuMinimizado;
	$scope.$watch('menuMinimizado',function(nval,oval) {
		sesion.setPreferencias("menuMinimizado", nval);	
		$rootScope.menuMinimizado = $scope.menuMinimizado;
		$scope.minimizaMenu();
	});
	$scope.minimizaMenu = function () {
		if ($scope.menuMinimizado===true) {
			if ($("#wrapper").hasClass("toggle")) 
				$("#wrapper").removeClass("toggle")
		}else {
			if (!$("#wrapper").hasClass("toggle")) 
				$("#wrapper").addClass("toggle")			
		}		
	};
	$scope.minimizaMenu();
	
	//configuracion recordar cliente
	$scope.nombreCompleto = (sesion.getPreferencias("nombreClienteRecordado"))?sesion.getPreferencias("nombreClienteRecordado"):$translate.instant("UI_SELECCIONE_CLIENTE");
	$scope.busquedaClienteAvanzado = function(){
		
		var filtros = new Array();
		filtros.push({field:"EstadoCliente", operator:"eq", value:"ACT"});
		filtros.push({field:"EsCliente", operator:"eq", value:"UI_SI"});
		
		var id1 = bcsComunes.ventanaAbrir("busquedaSocioNegocio", {filters: filtros} );
		var promesa = bcsComunes.ventanaPromesa(id1);
		
		promesa.then(function(data){
			sesion.setPreferencias("portafolioRecordado", 0);
			if(data!=undefined && data.IdSocioDeNegocio != undefined){
				$scope.nombreCompleto =  data.NombreCompleto;
				sesion.setPreferencias("SocioNegocioRecordado", data.IdSocioDeNegocio);
				sesion.setPreferencias("IdPersonaRecordado", data.IdPersona);
				sesion.setPreferencias("IdClienteRecordado", parseInt(data.IdCliente));
				sesion.setPreferencias("nombreClienteRecordado", data.NombreCompleto);
				
				//cargar los portafolios del cliente
				$scope.por.filtros.IdCliente.value =  parseInt(data.IdCliente);
				$scope.por.filtros.read = true;
			}else{
				if(data==undefined){
					$scope.nombreCompleto = $translate.instant("UI_SELECCIONE_CLIENTE");
					sesion.setPreferencias("nombreClienteRecordado", "");
					sesion.setPreferencias("SocioNegocioRecordado", 0);
					sesion.setPreferencias("IdClienteRecordado", false);
					$scope.por.filtros.IdCliente.value = 0;
				}
			}
		});
	};
	
	//configuracion recordar portafolio
	$scope.por={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   : 0}
		},
	};
	
	$scope.$watch('portafolioRecordado',function(nval,oval) {
		if(nval!=undefined)
			sesion.setPreferencias("portafolioRecordado", nval); 	
	});
	
	if(sesion.getPreferencias("IdClienteRecordado")){
		$scope.por.filtros.IdCliente.value =  sesion.getPreferencias("IdClienteRecordado");
		$scope.por.filtros.read = true;
	}
	
	//Evento para los menus desplieguen los submenus con el click
	$timeout(function() {
		$('li.dropdown.keep-open a').on('click', function (event) {
		    $(this).parent().toggleClass('open');
		});
	 },100);

	
});

//Controlador para barra de progreso
angular.module('ngBcsComponents').controller('ProgressCtrl',
	function($scope,$rootScope,bcsComunes, bcsServicioPlataforma,$timeout){

				$scope.IdUsuario = $scope.SesionIdUsuario;
				
				
				$rootScope.progress = 0;
				$scope.detalleProgreso = '';
				$scope.estadoProgreso = '';
				$scope.jsonObject = {};
				$scope.detener = false;
				$scope.procesosPendientes = [];
				$scope.eliminarProceso = function(IdTrabajo) {
					var DetalleProgreso = {
						IdTrabajo : IdTrabajo,
						IdUsuario : $scope.IdUsuario
					}
					const datos = {
						p_Accion: "FINALIZAR",
						p_IdUsuario: $scope.IdUsuario,
						p_IdEmpresa: $scope.SesionIdEmpresa,
						p_DetalleProgreso : DetalleProgreso
					};

					bcsServicioPlataforma.Servicios().BarraProgresoServicio(datos,
						function success(result) {
							$scope.detener = false;
							consultarProgreso();
						},
						function error(err) {
							console.error('Error al finalizar progreso:', err);
						}
					);

				};

				$scope.consultarResultadoPrograma = function(IdTrabajo){
					var id12 = bcsComunes.ventanaAbrir(
						"MantenedorPrograma",{IdPrograma:IdTrabajo},false,false
					)
					bcsComunes.ventanaFrente(id12);
				}
				$rootScope.abrirConsultaProgresos =function(){
					if($scope.rightProgress){
						if($scope.detener){
							$scope.detener = false;
							consultarProgreso();
							myInterval = setInterval(everyTime, 4000);
						}
						
						
					}
					else{
						$scope.loadHTMLContentProgreso();
						$scope.detener = false;
						consultarProgreso();
						
					}
					
				}
				function consultarProgreso() {
					const datos = {
						p_Accion: "CONSULTAR",
						p_IdUsuario: $scope.IdUsuario,
						p_IdEmpresa: $scope.SesionIdEmpresa
					};
					
					
					if(!$scope.detener){
						bcsServicioPlataforma.Servicios().BarraProgresoServicio(datos,
							function success(result) {
								
								if(result.P_OUT_PROGRESOS.length > 0){
										
										$.each( result.P_OUT_PROGRESOS,function(key,value) {
											if (value.PorcentajeProgreso < 100 && !$scope.procesosPendientes.includes(value.IdTrabajo)) {
												$scope.procesosPendientes.push(value.IdTrabajo);
											}
											if(value.PorcentajeProgreso == 100 && $scope.procesosPendientes.includes(value.IdTrabajo)){
												const index = $scope.procesosPendientes.indexOf(value.IdTrabajo);
												if (index > -1) { 
													$scope.procesosPendientes.splice(index, 1);
												  }
											}
											
											$scope.proceso.PorcentajeProgreso = value.PorcentajeProgreso;
											$scope.proceso.Servicio = value.Servicio + "("+value.IdTrabajo+")";
											$scope.proceso.Detalle = value.JsonData;
											
											$scope.procesos = result.P_OUT_PROGRESOS;
											$scope.proceso = {};
											
											
										});
										if($scope.procesosPendientes.length == 0){
											$scope.detener = true;
											$scope.procesosPendientes = [];
										}
										else{
											$scope.detener = false;
										}
										
									}
									else{
										$scope.detener = true;
										$scope.procesos = [];
										$scope.proceso = {};

									}
									
							},
							function error(err) {
								console.error('Error al consultar progreso:', err);
								$scope.detener = true;
							}
						);
					}
					
				}
				var myInterval;
				function everyTime() {
					consultarProgreso();
					if($scope.detener){
						clearInterval(myInterval);
					}
				} 

				$scope.$watch('rightProgress',function (nval,oval) {
					if (nval==true) {
						$scope.detener = false;
						consultarProgreso();
						myInterval = setInterval(everyTime, 4000);							
					}else{
						$scope.detener=true;
						clearInterval(myInterval);
						$scope.procesos = [];
						$scope.proceso = {};
					}
				});

				
				
			



});
