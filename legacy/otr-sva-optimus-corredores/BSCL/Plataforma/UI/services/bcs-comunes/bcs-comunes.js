angular.module('ModuloPlataforma').factory('bcsComunes',function($translate,bcsServicioPlataforma,toaster,$log,$ocLazyLoad,$rootScope,$timeout,$interval,$compile,$http,$interpolate,$q) {

	//private
	var conflictos = { errores :[],advertencias:[]};
	var parametros = {};

	//public
	var obj = {};

	//conportamiento cache
	obj.cache = true;
	obj.ventanaAutoHeight = function(ventanaid){
        var ventana = $("[ventanaid=" + ventanaid +"]").closest(".k-window");
        ventana.css('height', 'auto');
    };
	//Listado de Conflicto
	obj.getConflictos = function()
	{
		return conflictos;
	};

	obj.setConflictos = function(e)
	{
		conflictos = e;
	};

	obj.overlay = {
		encendido: false
	};
	
	obj.logCount = 0;
	obj.logMensajes = {
		data : []
	};

	obj.portafoliosSeleccionados = {};
	obj.limpiarPortafolios = function() {
		for (var p in obj.portafoliosSeleccionados)
			if (obj.portafoliosSeleccionados.hasOwnProperty(p)) {
				delete obj.portafoliosSeleccionados[p];
			}
	};
	obj.setClientePortafolio = function(key,data) {
		if (key)
		{
			obj.portafoliosSeleccionados[key] = data;
		}
	};

	obj.configurarGrid = function(scope,elem,options,kendoWidget,kNgModel,onDoubleClick,toolbarConfig)
	{
		scope.$on("kendoWidgetCreated", function (event, widget) {
			if (widget.$angular_scope === scope)
				scope.kendoWidget = widget;
		});

		scope.saveAsExcel = function()
		{
			scope.kendoWidget.saveAsExcel();
		}


		var toolbarObj = [];

		if (scope.toolbarConfig !== undefined)
		{
			if (scope.toolbarConfig.title !== undefined && scope.toolbarConfig.title.length > 0)
				toolbarObj.push({template : kendo.template("<label style='padding-right:10px' translate>" + scope.toolbarConfig.title +"</label>")});
			if (scope.toolbarConfig.buttons !== undefined)
			{
				$.each(scope.toolbarConfig.buttons,function(key,value){
					if (value.name === "excel")
						toolbarObj.push({template : kendo.template("<kendo-button ng-click='saveAsExcel()' translate>UI_EXPORTAR_EXCEL</kendo-button>")});
					else
					{
						toolbarObj.push({template : kendo.template("<kendo-button translate ng-click='" + value.functionName + "()'>" + value.name + "</kendo-button>")});
						scope[value.functionName] = value.functionCall;
					}
				});
			}
		}

		var defaultOptions = {
			sortable: true,
            reorderable: true,
            filterable: true,
			groupable: true,
			pageable: true,
			selectable: "multiple, row",
			columnMenu: false,
			reorderable: true,
			resizable: true,
            autoBind: false,
            scrollable: true,
            height:600,
            toolbar: toolbarObj,
		};

		elem.on("dblclick", function (event) {
			var tt;
			if (event !== undefined)
			{
				if ($(event.target).prop('nodeName') == "TD")
					tt = $(event.target);
				if ($(event.target).parent().prop('nodeName') == "TD")
					tt = $(event.target).parent();
			}

			if (tt)
			{
				scope.onDoubleClick();
			}
        });

		scope.handleChange = function(event,data,dataItem,columns)
    	{
			scope.kNgModel = ($.isArray(data))? _.uniq(data): data;
    	};

    	scope.kOptions = scope.kOptions || {};
		scope.kOptions = $.extend( {}, defaultOptions, scope.kOptions );

	};

	obj.VerificaRut = function (rut) {
		if (rut.toString().trim() != '' && rut.toString().indexOf('-') > 0) {
	        var caracteres = new Array();
	        var serie = new Array(2, 3, 4, 5, 6, 7);
	        var dig = rut.toString().substr(rut.toString().length - 1, 1);
	        rut = rut.toString().substr(0, rut.toString().length - 2);

	        for (var i = 0; i < rut.length; i++) {
	            caracteres[i] = parseInt(rut.charAt((rut.length - (i + 1))));
	        }

	        var sumatoria = 0;
	        var k = 0;
	        var resto = 0;

	        for (var j = 0; j < caracteres.length; j++) {
	            if (k == 6) {
	                k = 0;
	            }
	            sumatoria += parseInt(caracteres[j]) * parseInt(serie[k]);
	            k++;
	        }

	        resto = sumatoria % 11;
	        dv = 11 - resto;

	        if (dv == 10) {
	            dv = "K";
	        }
	        else if (dv == 11) {
	            dv = 0;
	        }

	        if (dv.toString().trim().toUpperCase() == dig.toString().trim().toUpperCase())
	            return true;
	        else
	            return false;
	    }
	    else {
	        return false;
	    }
	}
	obj.calcularMontoPlazo = function(monto, plazo, tasa){
		return Number(monto * ( 1 + (tasa/100) * (plazo /30) ));
	};
	obj.calcularMontoaPlazo = function(monto, plazo, tasa, base){
		return Number(monto * ( 1 + (tasa/100) * (plazo /base) ));
	};
	obj.calcularMontoPlazoInteresCompuesto = function(monto, plazo, tasa, baseTasa){
		var tasa = new Big(tasa).div(new Big(100));
		var periodos = new Big(plazo).div(new Big(baseTasa));
		var factor = Math.pow(Number(tasa.plus(new Big(1).valueOf())),Number(periodos.valueOf()));
		return Number(new Big(factor).times(new Big(monto)));
	};
	obj.calcularMontoInicial = function(montoPlazo, plazo, tasa){
		return Number(montoPlazo / ( 1 + (tasa/100) * (plazo /30) ));
	};

	//Mensajes Emergentes
	obj.msg = function (tipo,titulo,msg) {
		var msgt = $translate.instant(msg);
		var titulot = $translate.instant(titulo).toUpperCase(); 
		
		obj.logMensajes.data.push({pos: ++obj.logCount,fecha: new Date(),tipo:tipo,titulo:titulot,msg:msgt});
		if (titulo==null||titulo.length==0) $rootScope.Msg=msg;
		else $rootScope.Msg=titulot+":"+msgt;		
		if (tipo=="error") 	{
			$rootScope.MsgTipo="ERROR";
			toaster.error(titulot, msgt);
		}
		else if (tipo=="warning") 	{
			$rootScope.MsgTipo="WARN";
			toaster.warning(titulot, msgt);
		}
		else {
			$rootScope.MsgTipo="INFO";
			toaster.info(titulot, msgt);
		}
		$rootScope.MsgTipoUltimo=$rootScope.MsgTipo;
		$timeout(function () {
			$rootScope.MsgTipo="";
			$rootScope.Msg="";
		},6000);
		
	};

	//Log
	obj.log = function() {return $log;}

	//Objetos de Ventanas
	obj._ventanaId = 0;

	//Ventanas abiertas
	$rootScope.VentanaManager = [];	
	obj.ventanasAbiertas = function () {
		$rootScope.VentanaManager = [];	
		$('div.k-window').each(function(index,object) {
			var ele=$(object).find('[ventanaid]').first();
			var formulario = ele.data("kendoWindow");
			var vid= parseInt($(ele).attr('ventanaid'));
			var vobj={};
			vobj.id = vid;
			vobj.titulo = formulario.title();			
			$rootScope.VentanaManager.push(vobj);
		 });
	};	

	//Nueva Ventana
	obj.ventanaAbrir = function(winkey,params,unique,modal,idmodal, required,options) {
		if (!BCS_VENTANA[winkey]) {
			obj.msg("error","error",$translate.instant("UI_VENTANA_NO_DEFINIDA"));
			return;
		}
		if(sesion.getAccess(winkey)) {
			//validacion si esta afecto a cierre de modulo
			var afectoCierreModulo = BCS_VENTANA[winkey].AFECTOCIERRE;
			var idModuloAsociado = BCS_VENTANA[winkey].MODULO;
			if (afectoCierreModulo !== undefined && idModuloAsociado !== undefined && afectoCierreModulo === true) {
				if (BCS_ESTADO_MODULO[idModuloAsociado] === "CER") {
					obj.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
					return;
				} else {
					return _ventanaAbrir(winkey,params,unique,modal,idmodal, required,options);
				}
			} else {
				return _ventanaAbrir(winkey,params,unique,modal,idmodal, required,options);
			}
		} else {
			obj.msg("error",'Error',$translate.instant("UI_NO_TIENE_ACCESO"));
		}

	};

	


	var _ventanaAbrir = function(winkey,params,unique,modal,idmodal, required,options) {
		if (unique == undefined) unique=true;
		if (modal == undefined) modal=false;
		if (idmodal == undefined) idmodal=false;		
		if (BCS_VENTANA[winkey] == undefined) throw "Ventana '"+ winkey + "' no existe definida";
		
		let datos = {
			p_IdEmpresa : $rootScope.SesionIdEmpresa,
			p_IdUsuario : $rootScope.SesionIdUsuario,
			p_IdTipoCanalIngreso 	: 'OPT',
			p_Adicional1 	: BCS_VENTANA[winkey].VIEW,
			p_IdVentana : winkey,
			p_Ventana 	: BCS_VENTANA[winkey].TITLE
		};
		bcsServicioPlataforma.Servicios().RegistrarUsoVentanaOptimus(datos).$promise.then( function(result) {
			if (result.P_OUT_ERRORESCONTADOR > 0) {
				console.log("ERROR AL REGISTRAR USO VENTANA.")
			}
		},function(result){
			console.log("ERROR AL REGISTRAR USO VENTANA.");
		});

		var options = options || {};
		var ventanas = $("#Formularios");
		var formkey = "#FORM_"+winkey;
		//Si se encuentra creada se recupera
		if ((unique == true && $(formkey).length>0) || (unique == false && $(formkey).length > 0 && params != undefined && params.modulo != undefined)) {
			var formulario = $(formkey).data("kendoWindow");
			var ventana = formulario.element.closest(".k-window");	
			ventana.css("display", "block");
			formulario.restore();
			obj.ventanaFrente(formulario.ventanaid);
			return formulario.ventanaid;
		}
		//Se abre una nueva ventana
		var ventanaid = ++obj._ventanaId;
		ventanas.append('<div id="FORM_'+winkey+'" ventanaid="'+ventanaid+'" modal="'+modal+'" winkey="' + winkey + '"></div>');
		var ventanaobj = $("[ventanaid='"+ventanaid+"']");
		var nscope=$rootScope.$new(false);
		nscope.ventanaid=ventanaid;
		
		var form_resize =true;
		var sizeX   =BCS_VENTANA[winkey].WIDTH;
		var sizeY   =BCS_VENTANA[winkey].HEIGHT;
		
		var sizeXmin=BCS_VENTANA[winkey].WIDTH*0.5;
		var sizeYmin=BCS_VENTANA[winkey].HEIGHT*0.5;
		
		
		
		//Calcula la posicion de la ventana
		var posX=100;
		var posY=100;		
		if (modal===false && idmodal===false) {			
			//posicion izquierda-arriba a diagonal derecha-abajo
			var widthx=$('#Menu').find('nav').first().width();
			var height=$('#top-nav').height();
			//--
			if ($rootScope.VentanaManager.length==0) {
				obj.windowPosX=0;
				obj.windowPosY=0;
			}			
			//--
			if ($rootScope.menuMinimizado===true) widthx=widthx/2;			
			if (obj.windowPosX==undefined) obj.windowPosX=widthx;
			if (obj.windowPosX <= widthx || obj.windowPosX >= 400) obj.windowPosX=widthx;				
			obj.windowPosX+=200;
			//--
			if (obj.windowPosY==undefined) obj.windowPosY=height;
			if (obj.windowPosY <= height || obj.windowPosY >= 200) obj.windowPosY=height;							
			obj.windowPosY+=30;
			//--
			posX=obj.windowPosX;
			posY=obj.windowPosY;		
		}else if  (modal===true) {
			//modal al escritorio
			posX=( $(window).width() - parseInt(sizeX) )/2 +  $('#Menu').find('nav').first().width()/2 ;
			if (posX<$('#Menu').find('nav').first().width()) posX=$('#Menu').find('nav').first().width()+10;
			//--
			posY=($(window).height() - parseInt(sizeY) )/2 +  $('#top-nav').height()/2 ; 
			if (posY< ($('#top-nav').height()+5)) posY=$('#top-nav').height()+10;					
		}else {
			//modal al padre
			var winpadre=$("[ventanaid='"+idmodal+"']");
			posX=winpadre.offset().left + winpadre.width()/2 - parseInt(sizeX)/2;
			posY=winpadre.offset().top + winpadre.height()/2 - parseInt(sizeY)/2;
			//POR DEFECTO SE GENERA EN EL CENTRO
			if (params != undefined && params.childPositionX != undefined){
				switch (params.childPositionX) {
					case "RIGHT":
						posX=winpadre.offset().left + winpadre.width();
						break;
				}
			}
			if (params != undefined && params.childPositionY != undefined){
				switch (params.childPositionY) {
					case "TOP":
						posY=winpadre.offset().top;
						break;
					case "BOTTOM":
						posY=winpadre.offset().top + winpadre.height() - parseInt(sizeY);
						break;
				}
			}
			var height=$('#top-nav').height();
			if (posY<=height) posY=height+10;
		}
		
		var form_actions = [];
		if (BCS_VENTANA[winkey].PRINTABLE === true) {
			form_actions.push("PrintPlaceholder");
		}

		if(required==undefined || required==false){
			//si required = true, se ocultan los botones de cerrar y minimizar. Solo se puede cerrar la ventana con el metodo bcsComunes.ventanaCerrar
			form_actions.push("Minimize");
			form_actions.push("Close");
		}
		if ($rootScope.ModoNavegacion !== 'browser') {
			form_actions = [];
			posX=0; posY=0;	
			sizeXmin=sizeX;
			sizeYmin=sizeY;		
			resize=false;
		}
		var wtitle= BCS_VENTANA[winkey].TITLE;
		var formulario = ventanaobj.kendoWindow({
			position: {top:  parseInt(posY)+"px", left: parseInt(posX)+"px" },
			width:  sizeX,
			//minWidth: sizeXmin,
			height: sizeY,
			//minHeight: sizeYmin,
		    title: options.title !== undefined? options.title : $translate.instant( wtitle ),
			actions: form_actions,
			modal: modal,
		  	visible: false,
		  	resizable: true,
		  	animation: false,
			close: function (e) {
				//Si estoy cargando no puedo cerrar
				if (obj.ventanaIsOverlay(ventanaid)) { 
					e.preventDefault(); 
					return;
				}	
				var formulario = ventanaobj.data("kendoWindow");					
				var vscope  = ventanaobj.find("[ng-controller]").first().scope();
				if (idmodal!==false) {
					obj.ventanaOverlay(idmodal,false);
				}
				//resolvemos la promesa DESPUES de apagar la cortina modal, para que no entre en conflicto
				$('html,body').css('cursor','wait');
				$timeout(function() {
					var xdetka=new Date();
					formulario.optimusloading=true;
					formulario.dfd.resolve(formulario.data);
					formulario.options.modal=true; //truco para no hacer render
					ventanaobj.parent().hide();		
					ventanaobj.find("[data-" + kendo.ns + "role]").addBack().each(function(){
						var data = $(this).data();					            
						for (var key in data) {		
							if (key.indexOf("kendo") === 0) {	
								if (data[key].popup !== undefined) {
									data[key].popup.options.modal=true;
								}
								if (data[key].dateView !== undefined) {
									data[key].dateView.popup.options.modal=true;
								}
								if (data[key].timeView !== undefined) {
									data[key].timeView.popup.options.modal=true;
								}					                	
							}
						}
					});
					formulario.destroy();
					vscope.$destroy(); 
					nscope.$destroy();
					$('html,body').css('cursor','default');
					//administrador ventanas & foco de padre
					$timeout(function() { 
						if (idmodal!==false) {
							var winpadre=$("[ventanaid='"+idmodal+"']");
							winpadre.focus();
						}
						obj.ventanasAbiertas(); 
					},5);	
				},5);	
				
			},
			dragend: function(e) {
				if ($rootScope.ModoNavegacion !== 'browser') return;
				if (this.wrapper.offset().top  < $('#top-nav').height()) {
					this.setOptions({
						position: {top:  parseInt(posY)+"px", left: parseInt(this.wrapper.offset().left)+"px" }
					});				
				}			    
			},			
			resize: function(e) {
				if ($rootScope.ModoNavegacion !== 'browser') return;
				if (this.wrapper.is('.k-window-maximized')) {
					this.restore();
				}
  			}
		}).data("kendoWindow");
        //Parametros de ventana		
		formulario.optimusloading=true;
		formulario.open();
		formulario.maximize();
		formulario.ventanaid=ventanaid;
		formulario.params=$.extend( {}, BCS_VENTANA[winkey].PARAMS, params );
		formulario.dfd = new $.Deferred();
		formulario.data = {};		
		ventanaobj.parent().removeClass( "k-widget" );
		ventanaobj.parent().attr('id', "Win_"+winkey); //agrega id a las ventanas
		ventanaAUX= $(".k-icon.k-i-window-minimize")
		ventanaAUX.parent().click(function(){ 
			var ventanaActual = $(this).closest('.k-window');
  			ventanaActual.css('display', 'none');
		});
		if (modal==false) {
			ventanaobj.parent().css( "z-index",1);
			obj.ventanaFrente(ventanaid);
			ventanaobj.parent().on( "focusin",function () {  obj.ventanaFrente(ventanaid); });
		}
        //Activa/desactiva watchers dinamicamente
        var wrapper = formulario.element; 
        wrapper.closest(".k-window").find(".k-i-printplaceholder").addClass("no-print sapUiIcon print pull-left");
        wrapper.closest(".k-window").find(".print")
        	.removeClass("k-icon k-i-printplaceholder")
        	.css({'font-size':'16px','color': '#4d4d4d'})
        	.click(function(e){
        		obj.ventanaImprimir(wrapper.attr("ventanaid"));
        	})
        wrapper.blur(function(e) {
            if (winkey!="MonitorTools" && (!(BCS_VENTANA[winkey] && BCS_VENTANA[winkey].REFRESH === true))) {
            	obj.toggleAllWatchers(wrapper, true);
            } 
        });
        wrapper.on("focusin", function(e) {
             if (winkey!="MonitorTools" && (!(BCS_VENTANA[winkey] && BCS_VENTANA[winkey].REFRESH === true))) {            	
             	obj.toggleAllWatchers(wrapper, false);
             }             
        });	
		$timeout(function() {
			formulario.optimusloading=false;	
		},50);                        
		//cambia cache segun ambiente
		var mview=BCS_VENTANA[winkey].VIEW;
		var mdeps=BCS_VENTANA[winkey].DEPENDENCIES;	    
    	if (BCS_AMBIENTE === 'DES') mview+='?_dc='+util.date(new Date()).format("YYYYMMDD");
    	else mview+='?_dc='+util.date(new Date()).format("YYYYMMDDHHmmss");	    	
        for  (var j=0;j<mdeps.length;j++) {
        	if (BCS_AMBIENTE === 'DES') mdeps[j].cache=true;
        	else mdeps[j].cache=false;
        }	 
        //ventana modal referente al padre
		if (idmodal!==false) {
			obj.ventanaOverlay(idmodal,true);	
			obj.ventanaOverlayOnClick(idmodal,ventanaid);		
		}
        //carga los componentes   		
		$ocLazyLoad.load( mdeps ).then(function() {
			ventanaobj.append("<div id='loading-overlay' class='overlay'><div class='k-loading-image'></div></div>");
			obj.ventanaOverlay(ventanaid,false);
			try {
				ventanaobj.append($compile('<div ng-include="\''+mview+ '\'" ></div>')(nscope));
			}catch(er) {
				console.error("NO SE PUEDE CREAR VENTANA!!",er);
			}
		});		
		//administrador ventanas 
		obj.ventanasAbiertas();
		
		$timeout(function() {
			if(!!wrapper && !!wrapper.parent()){
				//wrapper.parent().addClass("k-state-focused");
				//var zIndex = wrapper.parent().css("z-index");
				//wrapper.parent().css("z-index", zIndex + 1);
				formulario.toFront();
				wrapper.focus();
			}
		},500);

		return ventanaid;
	}


	obj.ventanaAutoOverlay = function(ventanaid,selector,timeout) {
		obj.ventanaOverlay(ventanaid,true);
		if (timeout===undefined) timeout=1000;
		var stop;
		//check
		stop = $interval(function() {
			if (Pace.running!==true) {
				var checkEle=true;
				if (selector!==undefined) {
					var ventanaobj = $("[ventanaid='"+ventanaid+"']");
					var ele=ventanaobj.find(selector);
					if (ele===undefined) checkEle=false;					
				} 
				if (checkEle==true) {
					obj.ventanaOverlay(ventanaid,false);
					$interval.cancel(stop);
					stop = undefined;
				}
			}		            
       }, 250);
		//timeout
		$timeout(function() {
			if (stop!==undefined) {
				obj.ventanaOverlay(ventanaid,false);
				$interval.cancel(stop);	
				stop = undefined;
			}
		},timeout);						
	}
	obj.ventanaIsOverlay = function(ventanaid) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			return ventanaobj.find("#loading-overlay").is(":visible"); 
		}catch(e) {
			console.log(e);
			return false;
		}
	};	
	obj.ventanaOverlayOnClick = function(ventanaid,ventanaidfront) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			ventanaobj.find("#loading-overlay").click(function(){
				obj.ventanaFrente(ventanaidfront);
			});
			return ventanaid;
		}catch(e) {
			console.log(e);
			return false;
		}
	};			
	obj.ventanaOverlay = function(ventanaid,isEncendido) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var heightOverlay = ventanaobj.find("#loading-overlay").height();
			if (isEncendido===true) {
				ventanaobj.find("#loading-overlay").show();
			    var heightDatos = $(ventanaobj.data("kendoWindow").wrapper.find('[ng-include]')[0].firstChild).height() +
				parseInt($(ventanaobj.data("kendoWindow").wrapper.find('.k-window-content')).css('padding-top')) +
				parseInt($(ventanaobj.data("kendoWindow").wrapper.find('.k-window-content')).css('padding-bottom')) ;
					
				if (heightOverlay > heightDatos) {
					ventanaobj.find("#loading-overlay").height(heightOverlay);
				}
				else
					ventanaobj.find("#loading-overlay").height(heightDatos);				
			}
			else ventanaobj.find("#loading-overlay").hide();
			return ventanaid;
		}catch(e) {
			console.log(e);
			return false;
		}
	};
	obj.ventanaWinId = function(scope) {
		try {
			return scope.$parent.$parent.ventanaid;
		}catch(e) {
			return false;
		}
	};	
	obj.ventanaFrente =  function(ventanaid) {
		var ventanaobj = $("[ventanaid='"+ventanaid+"']");
		var maxz=0,vid=0,minz=9999;
		$("[ventanaid]").each( function(index) {
			if ($(this).attr('modal') == 'false') {
			 	var v=parseInt($(this).parent().css("z-index"));
			 	if (v>maxz) {maxz=v;vid=parseInt($(this).attr('ventanaid'));}
			 	if (v<minz) {minz=v;}
			}
		});
		ventanaobj.parent().css( "z-index",maxz+1);
		if (maxz>8000) {
			$("[ventanaid]").each( function(index) {
				if ($(this).attr('modal') == 'false') {
					var v=parseInt($(this).parent().css("z-index"))-1000;
					if (v<0) v=0;
					$(this).parent().css("z-index",v);
				}
			});
		}
	};
	obj.ventanaRestaura = function (ventanaid) {
		var ventanaobj = $("[ventanaid='"+ventanaid+"']");
		var formulario = ventanaobj.data("kendoWindow");
		var ventana = formulario.element.closest(".k-window");	
		ventana.css("display", "block");	
		formulario.restore();				
		obj.ventanaFrente(ventanaid);			
	};	
	obj.ventanaCerrar = function(ventanaid) {
		try {
			obj.ventanaOverlay(ventanaid,false);//se asegura de apagar overlay de ventana.
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var formulario = ventanaobj.data("kendoWindow");
			formulario.close();
			return ventanaid;
		}catch(e) {
			console.log(e);
			return false;
		}
	};
	obj.ventanaParams = function(ventanaid) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var formulario = ventanaobj.data("kendoWindow");
			return formulario.params;
		}catch(e) {
			return false;
		}
	};
	obj.ventanaParamsEdit = function(winkey, ventanaid, newData) {
		try {
			$("[ventanaid='"+ventanaid+"']").data("kendoWindow").params = $.extend( {}, BCS_VENTANA[winkey].PARAMS, newData );
			return true;
		}catch(e) {
			return false;
		}
	};
	obj.ventanaTitulo = function(ventanaid,titulo) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var formulario = ventanaobj.data("kendoWindow");
			formulario.title(titulo);
			obj.ventanasAbiertas();
			return true;
		}catch(e) {
			return false;
		}
	};
	
	obj.getAlto = function(ventanaid) {
		var ventanaobj = $("[ventanaid='"+ventanaid+"']");
		var formulario = ventanaobj.data("kendoWindow");
		return formulario.wrapper.height();
	}

	obj.ventanaTamanoAutomatico = function(ventanaid,deltasize) {
		if (!deltasize) deltasize = 10;
		$timeout(function() {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			if (ventanaobj) {
				var formulario = ventanaobj.data("kendoWindow");
				var size =  $(formulario.wrapper.find('[ng-include]')[0].firstChild).height() + 
					parseInt($(formulario.wrapper.find('.k-window-content')).css('padding-top')) +
					parseInt($(formulario.wrapper.find('.k-window-content')).css('padding-bottom')) 
					//formulario.wrapper.find('.k-window-titlebar').outerHeight();			
				obj.ventanaCambiarTamano(ventanaid,0,size+deltasize);
			}
		},10);


	}

	obj.getAncho = function(ventanaid) {
		var ventanaobj = $("[ventanaid='"+ventanaid+"']");
		var formulario = ventanaobj.data("kendoWindow");
		return formulario.wrapper.width();
	}

	obj.ventanaCambiarTamano = function(ventanaid,ancho,alto) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var formulario = ventanaobj.data("kendoWindow");
			if (Number(ancho) > 0) formulario.wrapper.css({width: ancho});
			if (Number(alto) > 0) formulario.wrapper.height(alto);
			
			return true;
		}catch(e) {
			return false;
		}
	};
	
	obj.ventanaGetTitulo = function(ventanaid) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var formulario = ventanaobj.data("kendoWindow");
			return formulario.title();
		}catch(e) {
			console.log(e);
		}
	};

	obj.ventanaCambiaCss = function(ventanaid,propiedad,valor) {
		var ventanaobj = $("[ventanaid='"+ventanaid+"']");
		var formulario = ventanaobj.data("kendoWindow");
		$(formulario.wrapper.find('.k-window-content')).css(propiedad,valor);
	};

	obj.ventanaData = function(ventanaid,data) {
		try {
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var formulario = ventanaobj.data("kendoWindow");
			formulario.data=data;
			return true;
		}catch(e) {
			return false;
		}
	};
	obj.ventanaPromesa= function(ventanaid) {
		try {
			if (ventanaid === false || ventanaid === undefined) {
				var dummyPromise = $q.defer();
				dummyPromise.resolve();
				return dummyPromise.promise;
			}
			var ventanaobj = $("[ventanaid='"+ventanaid+"']");
			var formulario = ventanaobj.data("kendoWindow");
			return formulario.dfd.promise();
		}catch(e) {
			return false;
		}
	};
	obj.configurarVentana = function(p1,p2,p3,p4) {
		console.warn('ADVERTENCIA! configurarVentana Ya no se usa! Eliminar del código.' ,new Error().stack);
	};
	
	/*obj.ventanaExternaImprimir = function(url,contexto,atributos,scope) {
		$http({
			  method: 'GET',
			  url: url
			}).then(function successCallback(response) {
				var scope = $rootScope.$new();
				var compilado = $compile(response.data)(scope);
				scope.orden = {
					DocumentoIdentidad : 'asdfasdfa'
				}
				myWindow = window.open(undefined,undefined,'menubar=no,location=yes,resizable=no,scrollbars=yes,status=yes');
				//$(myWindow.document.head).append('<script src=\"http://localhost:8085/BSCL/Plataforma/UI/libs/K2015_3_1111/bootstrap.min.js\"></script><link href=\"http://localhost:8085/BSCL/Plataforma/UI/libs/K2015_3_1111/bootstrap.min.css\" rel=\"stylesheet\" />');


				//$(myWindow.document.body).append(compilado);
				$(myWindow.document).find("html").html(compilado);
				//myWindow.document.write(compilado);
				

			});
	};*/

	obj.ventanaAutoHeight = function(ventanaid){
		var ventana = $("[ventanaid=" + ventanaid +"]").closest(".k-window");
		ventana.css('height', 'auto');
	};
	
	obj.ventanaImprimir = function(ventanaid) {
		var ventana = $("[ventanaid=" + ventanaid +"]").closest(".k-window");
		var body = $("body");
		body.addClass("hideChildren");
		ventana.addClass("printWindow");
		ventanaImprimiendo = ventanaid;
		window.print();
		
	};
	var ventanaImprimiendo = 0;
	var beforePrint = function() {};

	var afterPrint = function() {
		var ventana = $("[ventanaid=" + ventanaImprimiendo +"]").closest(".k-window");
		var body = $("body");
		body.removeClass("hideChildren");
		ventana.removeClass("printWindow");
	};

	(function() {
		if (window.matchMedia) {
			var mediaQueryList = window.matchMedia('print');
			mediaQueryList.addListener(function(mql) {
				if (mql.matches) {
					beforePrint();
				} else {
					afterPrint();
				}
			});
		}

		window.onbeforeprint = beforePrint;
		window.onafterprint = afterPrint;

	}());
	
	obj.ventanaMsg = function (msg, idPadre) {
		if((!msg.conflictos || (msg.conflictos.erroresContador === 0 && msg.conflictos.advertenciasContador === 0 && msg.conflictos.autorizacionesContador === 0)) && $rootScope.MinimizarMensajeExito === true){
			obj.msg("info",'',msg.mensaje);
			return false;
		}else{
			return _ventanaAbrir("VentanaMensajeResultado",msg,false,false,idPadre);;
		}
	};
	obj.ventanaMsgErr = function (msg, idPadre) {
		var respuestaSimulada={
			mensaje: msg,
			conflictos: {
			    erroresFatales: [],
			    errores: [],
			    advertencias: [],
			    autorizaciones: [],
			    informaciones: [],
			    erroresFatalesContador: 0,
			    erroresContador: 1,
			    autorizacionesContador: 0,
			    advertenciasContador: 0,
			    informacionesContador: 0
			}
		};
		return _ventanaAbrir("VentanaMensajeResultado",respuestaSimulada,false,false,idPadre);
	};
	obj.getWatchers =  function(element) {
		var watchers = [];
		var ang=angular.element(element||document.documentElement);
        angular.forEach(['$scope', '$isolateScope'], function (scopeProperty) { 
            if (ang.data() && ang.data().hasOwnProperty(scopeProperty)) {
                angular.forEach(ang.data()[scopeProperty].$$watchers, function (watcher) {
                    watchers.push(watcher);
                });
            }
        });
        angular.forEach(ang.children(), function (childElement) {
			var subwatchers=obj.getWatchers(angular.element(childElement))||[];
			watchers=watchers.concat(subwatchers);
        });
		var watchersWithoutDuplicates = [];
		angular.forEach(watchers, function(item) {
			if(watchersWithoutDuplicates.indexOf(item) < 0) {
				 watchersWithoutDuplicates.push(item);
			}
		});
		return watchersWithoutDuplicates;
    };		
    obj.toggleAllWatchers = function(element, pause) {
    	//return;
		var ang=angular.element(element||document.documentElement);
		/*if (ang.data() && ang.data().kendoWindow) {
			if (ang.data().kendoWindow.optimusloading===true) {
				console.log("DONT toggleAllWatchers title:",ang.data().kendoWindow.title()," pause:",pause);
				return;
			}else {
				console.log("DO toggleAllWatchers title:",ang.data().kendoWindow.title()," pause:",pause);
			}
		}*/				 
        angular.forEach(['$scope', '$isolateScope'], function (scopeProperty) { 
            if (ang.data() && ang.data().hasOwnProperty(scopeProperty)) {
				var obj=ang.data()[scopeProperty];
				if (obj.$$watchers !== null) {
					if (pause) {
						if (obj.$$isPause !== true) {
							obj.$$isPause = true;
							obj._bk_$$watchers = [];
							angular.forEach(obj.$$watchers, function (watcher) {
								obj._bk_$$watchers.push($.extend(true, {}, watcher));
							});
							obj.$$watchers = [];
						}
					} else {
						if (obj.$$isPause !== false) {
							if (obj.hasOwnProperty('_bk_$$watchers')) {
								obj.$$isPause = false;
								angular.forEach(obj._bk_$$watchers, function (watcher) {
									 obj.$$watchers.push($.extend(true, {}, watcher));
								});
							}
						}
					}
				}
            }
        });
        angular.forEach(ang.children(), function (childElement) {
        	obj.toggleAllWatchers(angular.element(childElement), pause);        	
        });
    };
    
    
    obj.getDefaultMap = function(winkey,tipoCombo) {
    	var map = {};
    	
    	map.IngresarOrdenMain = {
    			'combobox-condicion-liquidacion' : {
    				campo : "IdTipoOperacion",
    				//extranjeros
    				COMMIL : 'T3',
    				VENMIL : 'T3',
    				COMACX : 'T3',
    				VENACX : 'T3',
    				COMCFX : 'T3',
    				VENCFX : 'T3',
    				COMETX : 'T3',
    				VENETX : 'T3',
    				
    				//nacionales
    				COMACN : 'T2',
    				VENACN : 'T2',
    				COMCFI : 'T2',
    				VENCFI : 'T2',
    				COMETN : 'T2',
    				VENETN : 'T2'
    		},
    		'combobox-tipo-instrumento-moneda-liquidacion' : {
				campo : "IdMonedaInstrumento",
				USD : 'USD',
				CLP : 'CLP',
				PEN : 'USD',
    		}
    	};
    	
    	if ($rootScope.SesionLocale === 'PE') {
    		map.IngresarOrdenMain['combobox-tipo-instrumento-moneda-liquidacion'].PEN = "PEN";
    		map.IngresarOrdenMain['combobox-tipo-instrumento-moneda-liquidacion'].CLP = "USD";
    	}
    	
    	map.CrearTransaccion = {
        		'combobox-condicion-liquidacion' : {
        				campo : "IdTipoOperacion",
        				COMMIL : 'T3',
        				VENMIL : 'T3'
        		}
    	};
    	
    	map.BlotterOrdenes = {
        		'combobox-condicion-liquidacion' : {
        				campo : "IdTipoOperacion",
        				COMMIL : 'T3',
        				VENMIL : 'T3'
        		}
    	};
    	
    	if (map[winkey] && map[winkey][tipoCombo])
    		return map[winkey][tipoCombo]
    	else 
    		return undefined;
    	
    	
    }
    
    obj.ventanaRestauraC = function (ventanaid) {
		var ventanaobj = $("[ventanaid='"+ventanaid+"']");
		var formulario = ventanaobj.data("kendoWindow");
		var ventana = formulario.element.closest(".k-window");
		if(ventana.css('display') === "block"){
			obj.ventanaFrente(ventanaid);	
		}
		else{
			ventana.css("display", "block");	
			formulario.restore();				
			obj.ventanaFrente(ventanaid);
		}					
	};
	obj.obtenerIdVentanaGrid = function(gridId){ // desde el bcs-grid, esta es la funcion para bcsgrid.js
    	var ventana = $("#" + gridId +"").closest(".k-window");
		ventana.css("min-height", "200px");
		return ventana;
	};
	obj.validarAcceso = function(idHtml){
		var elementId = document.getElementById(idHtml);			
		if(sesion.getAccess($(elementId).attr("id")) === false){
			obj.msg("error",'Error',$translate.instant("UI_NO_TIENE_ACCESO"));
			return false;
		}else{
			return true;
		}

	};
	obj.tootipGlosa = function(idGlosa, array){
		let label = document.querySelector('#'+idGlosa);
		
		if (!label){
			return false;
		}
		let tooltip = document.getElementById("tooltip" + idGlosa)
		
		if (!tooltip){
			tooltip = document.createElement('div');
			tooltip.id = "tooltip" + idGlosa
			tooltip.className = 'tooltipGlosa';
			document.body.appendChild(tooltip);
			
			for(i=0; i < array.length; i++){
				let text = document.createElement('label');
				text.textContent = array[i].Estado
				text.className = 'col-xs-6 ' +  array[i].Color
				document.querySelector('#'+ tooltip.id).appendChild(text);
			}			
		}		
		
		label.addEventListener('mouseenter', () => {
			let labelRect = label.getBoundingClientRect();
			tooltip.style.top = (labelRect.top - tooltip.offsetHeight - 5) + 'px';
			tooltip.style.left = (labelRect.left + (labelRect.width / 1) - (tooltip.offsetWidth / 1)) + 'px';
			tooltip.style.display = 'block';
		});

		label.addEventListener('mouseleave', () => {
			tooltip.style.display = 'none';
		});
	};	
	return obj;
});

