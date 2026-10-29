(function() {
	angular.module('ModuloFFM').controller('PanelEgresoCtrl',function($rootScope,$scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsConfiguracionFFM,bcsServicioFondosMutuos){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		pc1.cmbbx		= {};
		pc1.datasource  = {};
		pc1.CantidadTotalTraspaso=0;
		pc1.MontoTotalTraspaso=0;
		
		pc1.MontoTotalDestino=0;
		pc1.PorcentajeTotalDestino=0;
		pc1.formData= {};
		pc1.modoPantalla="";
		pc1.paramsDefecto = {};
		pc1.ConsultarTraspaso 					= bcsConfiguracionFFM.getDataSourceConsultarTraspaso();
		pc1.ConsultarTraspasoDetalleOrigen 		= bcsConfiguracionFFM.getDataSourceConsultarTraspasoDetalleOrigen();
		pc1.ConsultarTraspasoDetalleDestino		= bcsConfiguracionFFM.getDataSourceConsultarTraspasoDetalleDestino();
    
		pc1.cantidadDecimalesMontoTotal = util.bcs_formato(0,'FMM','Monto',$rootScope.SesionIdMonedaSistema,'','').cantidadDecimales;
		pc1.idMonedaMontoTotal = $rootScope.SesionIdMonedaSistema;
		
	    var ModoGrillaMasivo=false;
	 

	    pc1.ConsultarParametroFondoMutuo = bcsConfiguracionFFM.getDataSourceConsultarParametroFondoMutuo();
	
		/*********************************************
		 * Cargar configuraciones
		 ********************************************/
		//Busca valor configuracion grilla masiva o individual
	    
	    var fnConsultarConfiguracionGrillaMasiva = function(){
			 var filters= [ { field: "NombreTipoParametro",	operator: "eq", value: 'TRASPASO_EGR_GRILLA_MASIVA'},
		 					{ field: "IdEmpresa",			operator: "eq", value: $scope.$root.SesionIdEmpresa },
		 					{ field: "IdEstado",			operator: "eq", value: 'VIG' },
		 					];
	  
			 pc1.ConsultarParametroFondoMutuo.query({ filter: filters }).then(function(){
			 		if(pc1.ConsultarParametroFondoMutuo.ds._data.length > 0) {
			 			ModoGrillaMasivo=pc1.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro==='S'? true : false;
			 			
			 		}else{ModoGrillaMasivo=false;};
			 	});
		 };
		 fnConsultarConfiguracionGrillaMasiva();
	    
	    
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		pc1.cmbbx.oClientePortafolio ={
				tipo : "combobox-cliente-portafolio",
				configuracion :{},
				idActual : 0,
				seleccionado : false,
				filtros: {
					IdEmpresa 			 : {"field": "IdEmpresa"			,"operator": "eq", "value" : $scope.SesionIdEmpresa }
					,IdEstadoCliente 	 : {"field": "IdEstadoCliente"		,"operator": "eq", "value" : 'ACT'}
					,Proposito 			 : {"field": "Proposito"			,"operator": "eq", "value" : 'FFMM'}
				},
			};	
		
		pc1.cmbbx.EntidadDestino={
				idActual : 0,
				seleccionado : false,
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};	
		
		pc1.cmbbx.Motivo={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};

		pc1.cmbbx.TipoTraspaso={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};
		
		pc1.cmbbx.MonedaTraspaso = {
			filtros : {
				complejo: [],
				filters: [
					{
						 logic: "or",
						 filters: [
							{ field: "IdMoneda", operator: "eq", value: 'CLP' },
							{ field: "IdMoneda", operator: "eq", value: 'USD' }
						]
					}
				]	
			}
		};
		
		/*********************************************
		 * DEFINICION DATASOURCE 
		 ********************************************/
		
		var filters = new Array();
		var defSaldoCustodia = {
				type : "call",
				definition : {
					serverFiltering : false, // Filtrado de datos en el servidor.
	                serverPaging    : false, // Paginado de los datos en el servidor.
	                serverSorting   : false, // Ordenamiento de los datos en el servidor.
	                pageSize        : 6,
	                sort 			: false,
	                callTransaction : "3041",
		            callResultSet   : "P_OUT_TBL",
	                filter			:  [ {field: "CantidadCustodia"		, operator: "neq", value: Number('0')}
	                					,{field: "IdRegimenFondoMutuo"	, operator: "eq", value: '18Q'} // filtra la grilla de custodia solo para los 18Q.
	                					,{field: "FondoExtranjero"		, operator: "eq", value: 'N'}
	                					],
					schema: {
						model: {
							fields: {
								
								IdInstrumento					   		: { type: "number"},
			        			IdCliente						   		: { type: "number"},
			        			IdPortafolio					   		: { type: "number"},
			        			CodigoPortafolio				   		: { type: "string"},
			        			IdMoneda						   		: { type: "string"},
			        			Nemotecnico						   		: { type: "string"},
			        			ValorCuota						   		: { type: "number"},
			        			CantidadCustodia						: { type: "number"},
			        			INGMontoPrecioPromedioCompra			: { type: "number"},
			        			INGPrecioPromedioCompra					: { type: "number"},
			        			IntCantidadTraspaso				   		: { type: "number"},//calculado 
			        			IntMontoTraspaso					   	: { type: "number"},//calculado
			        			CantidadTransSolicitudesAporte			: { type: "number"},
			        			CantidadTransSolicitudesRescate			: { type: "number"},
			        			CantidadTransEgr						: { type: "number"},
			        			CantidadTransIng						: { type: "number"},
			        			IdDetalleTraspaso						: { type: "number"},
			        			IdRegimenFondoMutuo						: { type: "string"},
			        			FondoExtranjero							: { type: "string"}
							}
						}
					}
				}	
			};
		
		var dsSaldoCustodia = new bcs.DataSource(defSaldoCustodia);
		pc1.SaldoCustodia = {
				dataSource: dsSaldoCustodia,
				columns: [
						{field: "Nemotecnico",                   			title: $translate.instant('UI_INSTRUMENTO'),                   		width: "100px", align: "left"},
						{field: "CantidadCustodia",							title: $translate.instant('UI_CUSTODIA'),							width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadCustodia,'FMM','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "IdMoneda",							    	title: $translate.instant('UI_MONEDA'),			    				width: "75px",	align: "left"},
						{field: "IdRegimenFondoMutuo",                   	title: $translate.instant('UI_REGIMEN'),                   			width: "100px", align: "left"},
						{field: "ValorCuota",					            title: $translate.instant('UI_PRECIO'),							    width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(ValorCuota,'FMM','Precio',IdMoneda, '','')).valor #</label>"},
						{field: "INGMontoPrecioPromedioCompra",				title: $translate.instant('UI_MONTO'),								width: "100px", align: "left", template : " <label> #: (util.bcs_formato(INGMontoPrecioPromedioMonedaInstrumento,'FMM','Monto',IdMoneda, '','')).valor #</label>" },
						{field: "IntCantidadTraspaso",						title: $translate.instant('UI_CANTIDAD_TRASPASO'),					width: "100px", align: "right", editable: true},
						{field: "IntMontoTraspaso",							title: $translate.instant('UI_MONTO_TRASPASO'),						width: "100px", align: "right"},
						{field: "CantidadTransSolicitudesAporte",			title: $translate.instant('UI_APORTES_TRANSITO'),					width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransSolicitudesAporte,'CUS','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "CantidadTransSolicitudesRescate",			title: $translate.instant('UI_RESCATES_TRANSITO'),					width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransSolicitudesRescate,'CUS','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "CantidadTransEgr",              			title: $translate.instant('UI_EGRESOS_TRANSITO'),           		width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransEgr,'CUS','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "CantidadTransIng",              			title: $translate.instant('UI_INGRESO_TRANSITO'),           		width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransIng,'CUS','Cantidad',IdMoneda,'','').valor #</label>"}
						
						
					    ],	
				sortable		: false,
				pageable		: true,
				autoBind		: true,
				height			: 160,
				selectable		: "row",
			    userSettings	: false,
				navigatable		: true,
				save: function(e) 	{calculoMontoATraspasar(e.values, e.model);},
				gridCreated     : function(g){
					$scope.ApiMovimientos = g;
				},
				notEditableColumns : function(fila)
				{
					if (fila.CantidadCustodia == 0 || (pc1.CantidadTotalTraspaso > 0 && ModoGrillaMasivo==false && (fila.IntCantidadTraspaso ==null ||fila.IntCantidadTraspaso ==0) )) 
					{
						fila.IntCantidadTraspaso = 0;
						fila.IntMontoTraspaso = 0;
						return ['IntCantidadTraspaso'];
					}
				}
		};

		var defDestinos = {
				type: "odata",
				definition : {
					serverFiltering : false, // Filtrado de datos en el servidor.
	                serverPaging    : false, // Paginado de los datos en el servidor.
	                serverSorting   : false, // Ordenamiento de los datos en el servidor.
	                pageSize        : 6,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=900202",
							dataType: "json"
						}
					},
					filter: [
					   { field: "IdEstado ",  			operator: "eq", value: 'ACT'}
					],
	                sort 			: false,
					schema: {
						model: {
							fields: {
						    	RunFFMM          			:	{ type: "string"},
						    	NombreFFMM					:	{ type: "string"},
						    	IntMontoTraspaso			: 	{ type: "number"},//calculado 
			        			IntPorcentajeTraspaso		: 	{ type: "number"},//calculado
			        			IdMoneda					:	{ type: "string"},
			        			IdDetalleTraspaso			:	{ type: "number"}
			        		}
						}
					}
				}	
			};
			
		var dsDestinos = new bcs.DataSource(defDestinos);
		pc1.Solicitudes = {
				dataSource: dsDestinos,
				columns: [
					    {field: "RunFFMM",					title: $translate.instant('UI_RUN_FFMM'),					width: "100px", align: "left"}, //"RunFFMM"
					    {field: "NombreFFMM",				title: $translate.instant('UI_NOMBRE_FFMM'),				width: "100px", align: "left"}, //"NombreFFMM"
					    {field: "IntMontoTraspaso",			title: $translate.instant('UI_MONTO_TRASPASO'),				width: "100px", align: "right"},
						{field: "IntPorcentajeTraspaso",	title: $translate.instant('UI_PORCENTAJE_TRASPASO'),		width: "100px", align: "right", editable: true},
					    {field: "IdMoneda",					title: $translate.instant('UI_MONEDA'),						width: "100px", align: "right"},
						
						],	
				sortable		: true,
				pageable		: true,
				autoBind		: false,
				height			: 160,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				save: function(e) 	{calculoMontoDestino(e.values, e.model);},
				gridCreated     : function(g){
					pc1.ApiDestinos = g;
				},
				notEditableColumns : function(fila)
				{	},
				 dataBound: function(e) {
					    console.log("dataBound");
					  }
		};
		
		
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		var apiGridCustodiaFondosMutuos = pc1.SaldoCustodia.dataSource;
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		pc1.buscarCustodia = function ()
		{	
			
			if(!pc1.cmbbx.IdMonedaTraspaso){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_SELECCIONAR_MONEDA_PARA_BUSCAR_CUSTODIA"));
				return;
			}
			pc1.CantidadTotalTraspaso=0;
			pc1.MontoTotalTraspaso=0;
			apiGridCustodiaFondosMutuos.setCallParameters(pc1.preparaParametros());
			apiGridCustodiaFondosMutuos.read();
			let filters = [
				 {field: "CantidadCustodia"		, operator: "neq", value: Number('0')}
	            ,{field: "IdRegimenFondoMutuo"	, operator: "eq", value: '18Q'}
				,{field: "IdMoneda",	operator: "eq", value: pc1.cmbbx.IdMonedaTraspaso}
				,{field: "FondoExtranjero"		, operator: "eq", value: 'N'}
			];
			apiGridCustodiaFondosMutuos.filter(filters);
			
			//Se deja una fila oculta bloquedada por defecto
			pc1.SaldoCustodia.dataSource.add({Nemotecnico: "",
				CantidadCustodia 			:	Number('0'),
				IdMoneda 					:	"", 
				INGPrecioPromedioCompra  	:	Number('0'), 
				INGMontoPrecioPromedioCompra  :	Number('0'),
				IntCantidadTraspaso 		:   Number('0'),
				IntMontoTraspaso 			:	Number('0'),
				CantidadTransSolicitudesAporte 			:	Number('0'),
				CantidadTransSolicitudesRescate 		:	Number('0'),
				CantidadTransEgr 			:	Number('0'),
				CantidadTransIng 			:	Number('0')
			 });

		};
		
		pc1.buscarDestinos = function ()
		{
			pc1.Solicitudes.dataSource.removeAll();

			let filtros = Array(); 
			filtros.push({field: "RutSociedadAdmFFMM "			, operator: "eq", value: pc1.cmbbx.EntidadDestinoObj.RutSociedadAdmFFMM });
			filtros.push({field: "IdMoneda"			, operator: "contains", value: pc1.cmbbx.IdMonedaTraspaso });
			
			pc1.Solicitudes.dataSource.filter( filtros);
			pc1.Solicitudes.dataSource.sync();
		};
		
		pc1.preparaParametros = function()
		{
			parametrosConsulta = {};
			parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
			parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
			parametrosConsulta.p_IdModulo = "FMM";
			parametrosConsulta.p_AgrupaPorBoveda = "S";
			parametrosConsulta.p_IdCliente = 0;
			parametrosConsulta.p_IdPortafolio = 0;
			parametrosConsulta.p_IdProductoAgrupador = null;
			parametrosConsulta.p_IdInstrumento = 0;
			parametrosConsulta.p_FechaConsulta = $scope.SesionFechaSistema;
			parametrosConsulta.p_IdRegimenFondoMutuo = "18Q";
			
		
			parametrosConsulta.p_IdCliente = (pc1.cmbbx.oClientePortafolio.seleccionado) ? pc1.cmbbx.oClientePortafolio.seleccionado.IdCliente : 0;
			parametrosConsulta.p_IdPortafolio = (pc1.cmbbx.oClientePortafolio.seleccionado) ? pc1.cmbbx.oClientePortafolio.seleccionado.IdPortafolio : 0;
			
			return parametrosConsulta;
		};
	


		var calculoMontoATraspasar = function(values, model){
			bcsComunes.ventanaOverlay(pc1.idVentana,true);
			pc1.CantidadTotalTraspaso = 0;
			pc1.MontoTotalTraspaso = 0;
			
			var datosFondo = {};
			datosFondo.IdSerieFondo = model.IdSerieFondo;
			datosFondo.IdMoneda = model.IdMoneda;
			datosFondo.IdEmpresa = $scope.SesionIdEmpresa;
			
			bcsConfiguracionFFM.calcularMontoCuota(datosFondo,0,values.IntCantidadTraspaso,'RES',model.IdMoneda,'C',$scope.SesionFechaSistema)
			.then(function(respuesta){
				if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					if (pc1.SaldoCustodia.seleccion !== undefined){
						//CANTIDAD
						pc1.CantidadTotalTraspaso = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						pc1.SaldoCustodia.seleccion.IntCantidadTraspaso = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 	//MONTO
						pc1.MontoTotalTraspaso = respuesta.P_OUT_RESULTADO[0].Monto;
						pc1.SaldoCustodia.seleccion.IntMontoTraspaso = (util.bcs_formato(respuesta.P_OUT_RESULTADO[0].Monto,'FMM','Monto',model.IdMoneda, '','')).valor;
					} else{
						//CANTIDAD
						pc1.CantidadTotalTraspaso = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						//MONTO
						pc1.MontoTotalTraspaso = respuesta.P_OUT_RESULTADO[0].Monto;
					};	
				} else{
					//CANTIDAD
					pc1.CantidadTotalTraspaso = 0;
					//MONTO
					pc1.MontoTotalTraspaso = 0;
				}
				bcsComunes.ventanaOverlay(pc1.idVentana,false);
				
				var pas= pc1.SaldoCustodia.dataSource.page();
				
				pc1.SaldoCustodia.dataSource.fetchPage(pc1.SaldoCustodia.dataSource.totalPages());
				pc1.SaldoCustodia.dataSource.fetchPage(pas);
			},function(error){
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
			
			var pas= pc1.SaldoCustodia.dataSource.page();
			
			pc1.SaldoCustodia.dataSource.fetchPage(pc1.SaldoCustodia.dataSource.totalPages());
			pc1.SaldoCustodia.dataSource.fetchPage(pas);
		};
		
		var calculoMontoDestino = function(values, model){
			
					var Destino = pc1.Solicitudes.dataSource.data() || [];
					pc1.MontoTotalDestino=0;
					pc1.PorcentajeTotalDestino=0;
					$.each(Destino,function(key,value){
					   //MONTO
						pc1.PorcentajeTotalDestino += Number(value.IntPorcentajeTraspaso);
					   //PORCENTAJE
						if (Number(pc1.MontoTotalTraspaso) > 0)
						{
							pc1.Solicitudes.seleccion.IntMontoTraspaso = (Number(pc1.MontoTotalTraspaso) * Number(values.IntPorcentajeTraspaso))/Number(100);
							pc1.Solicitudes.seleccion.IntMontoTraspaso = (util.bcs_formato(pc1.Solicitudes.seleccion.IntMontoTraspaso,'FMM','Monto',pc1.idMonedaMontoTotal, '','')).valor;
						};
		
					});

				var pas= pc1.Solicitudes.dataSource.page();
				
				pc1.Solicitudes.dataSource.fetchPage(pc1.Solicitudes.dataSource.totalPages());
				pc1.Solicitudes.dataSource.fetchPage(pas);	
		};
		
		var actualizarPorcentaje = function(){
			var Destino = pc1.Solicitudes.dataSource.data() || [];
			
			$.each(Destino,function(key,value){
				
				value.IntPorcentajeTraspaso = value.IntPorcentajeTraspaso===null?0:value.IntPorcentajeTraspaso;
			
				if (Number(pc1.MontoTotalTraspaso) > 0 && value.IntPorcentajeTraspaso !== 0)
				{
					value.IntMontoTraspaso =      (Number(pc1.MontoTotalTraspaso) * Number(value.IntPorcentajeTraspaso))/Number(100);
					value.IntMontoTraspaso = (util.bcs_formato(value.IntMontoTraspaso,'FMM','Monto',pc1.idMonedaMontoTotal, '','')).valor;
				}else{
					value.IntMontoTraspaso =0;
				};
				
			});
			
		}
		
		pc1.ingresar = function ()
		{
			var Origen 	= pc1.SaldoCustodia.dataSource.data() 	|| [];
			var Destino = pc1.Solicitudes.dataSource.data() 	|| [];
			
    	 	if (pc1.cmbbx.oClientePortafolio.seleccionado==undefined || pc1.cmbbx.oClientePortafolio.seleccionado==0)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("SELECCIONAR_CLIENTE"));
    	 		return;
    	 	}
    	 	
			var tablaOrigen	= [];
			$.each(Origen,function(key,value){

				if($scope.solTra.params.IdTraspaso && pc1.modoPantalla=="EDICION"){
					if (Number(value.IntCantidadTraspaso) >0  || Number(value.IdDetalleTraspaso)>0){
						if(value.IdRegimenFondoMutuo=="18Q"){
							tablaOrigen.push({
									 "IdTraspaso" 			: $scope.solTra.params.IdTraspaso
									,"IdSerieFondo" 		: value.Nemotecnico
									,"IdRegimenTributario"	: value.IdRegimenFondoMutuo
									,"Cantidad" 			: Number(value.IntCantidadTraspaso)
									,"IdDetalleTraspaso" 	: Number(value.IdDetalleTraspaso)
									});
						}
					}
				}else{
					if (Number(value.IntCantidadTraspaso)>0){
					    if(value.IdRegimenFondoMutuo=="18Q"){
							tablaOrigen.push({
								 "IdInterno" 			: 1
								,"IdSerieFondo" 		: value.Nemotecnico
								,"IdRegimenTributario"	: value.IdRegimenFondoMutuo
								,"Cantidad" 			: Number(value.IntCantidadTraspaso)
								});
					    }
					}				
				};
			});
				 
			var PorcentajeTotal = 0;
			var tablaDestino    = [];
			$.each(Destino,function(key,value){

					if($scope.solTra.params.IdTraspaso && pc1.modoPantalla=="EDICION"){
						if (Number(value.IntPorcentajeTraspaso)>0 || Number(value.IdDetalleTraspaso)>0){
							if(value.IdRegimenFondoMutuo=="18Q"){
								tablaDestino.push({
									 	"IdTraspaso" :  $scope.solTra.params.IdTraspaso										 
										,"RunFFMM": value.RunFFMM
										,"NombreFFMM": value.NombreFFMM
										,"Porcentaje" : Number(value.IntPorcentajeTraspaso)
									});
							}
						};
					}else{
						if (Number(value.IntPorcentajeTraspaso)>0){
						tablaDestino.push({
						  	 "IdInterno" : 1
							,"RunFFMM": value.RunFFMM
							,"NombreFFMM": value.NombreFFMM
							,"Porcentaje" : Number(value.IntPorcentajeTraspaso)
							});
						};
					};
							
			});
			
			if (tablaOrigen.length ==0)
			{
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("SELECCIONAR_ORIGEN"));
				return;
			}
			if (tablaDestino.length ==0)
			{
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("SELECCIONAR_DESTINO"));
				return;
			}

			$.each(tablaDestino,function(key,value){
				PorcentajeTotal=Number(PorcentajeTotal) + Number(value.Porcentaje);
			});

			if (Number(PorcentajeTotal) != 100 || Number(PorcentajeTotal)==0)
			{
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("TOTAL_PORCENTAJE_INCORRECTO"));
				return;
			}
	   	 	
			 bcsComunes.ventanaOverlay(pc1.idVentana,true);
			 var datosIngreso = {
					 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					 p_IdModulo : 'FMM',
						 
					 p_Traspaso : [{
						    IdInterno		      		:	1,
							IdCliente 	          		:	(pc1.cmbbx.oClientePortafolio.seleccionado) ? pc1.cmbbx.oClientePortafolio.seleccionado.IdCliente : 0,
					    	IdPortafolio          		:	(pc1.cmbbx.oClientePortafolio.seleccionado) ? pc1.cmbbx.oClientePortafolio.seleccionado.IdPortafolio : 0,
					    	IdEntidadRecaudadora        :	pc1.cmbbx.EntidadDestinoObj.IdEntidadRecaudadora,
					   	   	IdMotivoTraspaso			: 	pc1.cmbbx.IdMotivo,
					    	IdTipoMovimientoTraspaso	:   'TRAEGR',
					    	IdTipoTraspaso				:	pc1.cmbbx.IdTipoTraspaso,
					    	Glosa						:	(pc1.formData.comentario)?pc1.formData.comentario:'',
					    	FechaHoraSuscripcion		: 	$scope.solTra.fechaInicioSol,
					    	IdDivisionPais				: 	($scope.solTra.idDivisionPaisComuna)?$scope.solTra.idDivisionPaisComuna:0,
					    	FolioTraspaso				: 	($scope.solTra.FolioTraspaso)?$scope.solTra.FolioTraspaso:0,
					    	Monto 				  		:	pc1.MontoTotalTraspaso
					 }],
					 
					 p_TraspasoDetalleOrigen : tablaOrigen,
					 
					 p_TraspasoDetalleDestino : tablaDestino
					 
			 }
				if($scope.solTra.params.IdTraspaso && pc1.modoPantalla=="EDICION"){
					datosIngreso.p_Traspaso[0].IdTraspaso=$scope.solTra.params.IdTraspaso;
					bcsServicioFondosMutuos.ModificarTraspaso(datosIngreso).$promise.then(function(respuesta){
				     	 console.log(respuesta);
				     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion modificada"},pc1.idVentana);
				     		bcsComunes.ventanaOverlay(pc1.idVentana,false);
				     		bcsComunes.ventanaCerrar(pc1.idVentana);
			     			
			     			
				     	 }else{
				     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICACION_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc1.idVentana);
				     		bcsComunes.ventanaOverlay(pc1.idVentana,false);
				     	 }
			   		 },function(respuesta){
			   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICACION_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc1.idVentana);
			   			bcsComunes.ventanaOverlay(pc1.idVentana,false);
			   		 });
					
					
				}else{
				     bcsServicioFondosMutuos.CrearTraspaso(datosIngreso).$promise.then(function(respuesta){
				     	 console.log(respuesta);
				     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada"},pc1.idVentana);
			     			bcsComunes.ventanaOverlay(pc1.idVentana,false);
			     			bcsComunes.ventanaCerrar(pc1.idVentana);
			     			
				     	 }else{
				     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc1.idVentana);
				     		bcsComunes.ventanaOverlay(pc1.idVentana,false);
				     	 }
			   		 },function(respuesta){
			   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc1.idVentana);
			   			bcsComunes.ventanaOverlay(pc1.idVentana,false);
			   		 });
					 
					 
				}	 
			 
			
		}
		
	    var fnConsultarTraspaso = function(){
			 var filters= [ { field: "IdTraspaso",	operator: "eq", value: $scope.solTra.params.IdTraspaso},
		 					{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa }
		 				  ];
	  
			 pc1.ConsultarTraspaso.query({ filter: filters }).then(function(){
		 		if(pc1.ConsultarTraspaso.ds._data.length > 0) {
		 			var Traspaso = pc1.ConsultarTraspaso.ds._data || [];
					$.each(Traspaso,function(key,value){
						pc1.cmbbx.IdEntidadDestino=value.IdEntidadRecaudadora;
						pc1.paramsDefecto.EntidadDestino = pc1.cmbbx.IdEntidadDestino;
						pc1.cmbbx.oClientePortafolio.idActual=value.IdPortafolio || -1;
						pc1.cmbbx.oClientePortafolio.idDefecto=value.IdPortafolio || -1;
						pc1.cmbbx.IdMotivo=value.IdMotivoTraspaso || -1;
						pc1.cmbbx.IdTipoTraspaso=value.IdTipoTraspaso || -1;
						pc1.formData.comentario=value.Glosa || '';
						$scope.solTra.fechaInicioSol=value.FechaHoraSuscripcion || '';
						$scope.solTra.FolioTraspaso=value.FolioTraspaso || 0;
						$scope.solTra.idDivisionPaisComunaPost=value.IdDivisionPais || 0;
						pc1.paramsDefecto.IdMonedaTraspaso=value.IdMonedaFondo || 'CLP';
						pc1.cmbbx.IdMonedaTraspaso = value.IdMonedaFondo;
					});
		 		}else{
					bcsComunes.ventanaOverlay(pc1.domVentana,false);
					bcsComunes.ventanaCerrar(pc1.domVentana);
					return false;
		 		};
			 });
		};
		 
		    var fnConsultarTraspasoDetalleOrigen = function(){
				 var filters= [ { field: "IdTraspaso",	operator: "eq", value: $scope.solTra.params.IdTraspaso},
			 					{ field: "IdEstado",	operator: "eq", value: 'ACT' }
			 				  ];
		  
				 pc1.ConsultarTraspasoDetalleOrigen.query({ filter: filters }).then(function(){
				 		if(!pc1.ConsultarTraspasoDetalleOrigen.ds._data.length > 0) {
							bcsComunes.ventanaOverlay(pc1.domVentana,false);
							bcsComunes.ventanaCerrar(pc1.domVentana);
							return false;
				 		};
						
				 	});
			 };
		    var fnConsultarTraspasoDetalleDestino = function(){
				 var filters= [ { field: "IdTraspaso",	operator: "eq", value: $scope.solTra.params.IdTraspaso},
			 					{ field: "IdEstado",	operator: "eq", value: 'ACT' }
			 				  ];
		  
				 pc1.ConsultarTraspasoDetalleDestino.query({ filter: filters }).then(function(){
				 		if(!pc1.ConsultarTraspasoDetalleDestino.ds._data.length > 0) {
							bcsComunes.ventanaOverlay(pc1.domVentana,false);
							bcsComunes.ventanaCerrar(pc1.domVentana);
							return false;
				 		};	
						fncompletarGrillaDestino();					
				 	});
			 };
			 
		var fncompletarGrillaOrigen = function(){
 			var TraspasoDetalleOrigen = pc1.ConsultarTraspasoDetalleOrigen.ds._data || [];
 			//var i=0;
 			pc1.MontoTotalTraspaso=0;
 			pc1.CantidadTotalTraspaso=0;
			$.each(pc1.ConsultarTraspasoDetalleOrigen.ds._data,function(key,value){
				var Cantidad=value.Cantidad;
				var IdSerieFondo=value.IdSerieFondo;
				var IdDetalleTraspaso=value.IdDetalleTraspaso;
				$.each(pc1.SaldoCustodia.dataSource.data(),function(key,value){
					if (IdSerieFondo==value.Nemotecnico){
	        			pc1.SaldoCustodia.dataSource.ds._data[key].IdDetalleTraspaso=Number(IdDetalleTraspaso);
						pc1.SaldoCustodia.dataSource.ds._data[key].IntCantidadTraspaso=Number(Cantidad);
						 //CANTIDAD					
					 	pc1.CantidadTotalTraspaso +=Number(pc1.SaldoCustodia.dataSource.ds._data[key].IntCantidadTraspaso);
						
					   //MONTO
					 	pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso = Number(pc1.SaldoCustodia.dataSource.ds._data[key].IntCantidadTraspaso) * Number(pc1.SaldoCustodia.dataSource.ds._data[key].INGPrecioPromedioCompra);
					 	
						pc1.MontoTotalTraspaso  += Number(pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso);
						pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso=(util.bcs_formato(pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso,'FMM','Monto',pc1.SaldoCustodia.dataSource.ds._data[key].IdMoneda, '','')).valor;
						return true;
					};
					
				});
			});
		}; 
		
		var fncompletarGrillaOrigenCantidad = function(TipoTraspaso){
			 pc1.CantidadTotalTraspaso =0;
			 pc1.MontoTotalTraspaso =0;
			$.each(pc1.SaldoCustodia.dataSource.data(),function(key,value){
				if (TipoTraspaso=='TOTAL'){
					
					pc1.SaldoCustodia.dataSource.ds._data[key].IntCantidadTraspaso =  Number(pc1.SaldoCustodia.dataSource.ds._data[key].CantidadCustodia);
					pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso = Number(pc1.SaldoCustodia.dataSource.ds._data[key].IntCantidadTraspaso) * Number(pc1.SaldoCustodia.dataSource.ds._data[key].INGPrecioPromedioCompra);
				 	pc1.MontoTotalTraspaso  += Number(pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso);
					pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso=(util.bcs_formato(pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso,'FMM','Monto',pc1.SaldoCustodia.dataSource.ds._data[key].IdMoneda, '','')).valor;
					pc1.CantidadTotalTraspaso +=Number(pc1.SaldoCustodia.dataSource.ds._data[key].IntCantidadTraspaso);
					
				}else{
					pc1.SaldoCustodia.dataSource.ds._data[key].IntCantidadTraspaso = "0";
					pc1.SaldoCustodia.dataSource.ds._data[key].IntMontoTraspaso = "0";
				};
				
		    });
			
			}; 
		
		var fncompletarGrillaDestino = function(){
			
			var TraspasoDetalleDestino = pc1.ConsultarTraspasoDetalleDestino.ds._data || [];
 				$.each(TraspasoDetalleDestino,function(key,value){
				var Porcentaje=value.Porcentaje;
				var RunFFMM=value.RunFFMM;
				var NombreFFMM= value.NombreFFMM;
				var IdDetalleTraspaso=value.IdDetalleTraspaso;
				
				pc1.Solicitudes.dataSource.ds._data[key].IdDetalleTraspaso=Number(IdDetalleTraspaso);
				pc1.Solicitudes.dataSource.ds._data[key].IntPorcentajeTraspaso=Porcentaje;
				
			});
		};
		
 
		pc1.buscarTraspaso = function ()
		{
			fnConsultarTraspaso();
		}
		
	 
		pc1.buscarTraspasoDetalleOrigen = function ()
		{
			fnConsultarTraspasoDetalleOrigen();
		}
		
		
		pc1.buscarTraspasoDetalleDestino = function ()
		{
			fnConsultarTraspasoDetalleDestino();
		}
		
		/*********************************************
		 * WATCH
		 ********************************************/
		$scope.$watch('pc1.cmbbx.EntidadDestinoObj',function(newval,oldval){
			if (newval){
				pc1.buscarDestinos();
			};	
		});	
		
		$scope.$watch('pc1.cmbbx.oClientePortafolio.seleccionado',function(newval,oldval){
			if (newval){
				pc1.buscarCustodia();
			};	
		});	
				
		$scope.$watch('pc1.MontoTotalTraspaso',function(newval,oldval){
			if (newval !=oldval){
				actualizarPorcentaje();
			};	
		});

		$scope.$watch('pc1.Solicitudes.dataSource.ds._data',function(newval,oldval){
			if (newval !=oldval && $scope.solTra.params.IdTraspaso && pc1.Solicitudes.dataSource.ds._data){
				fncompletarGrillaDestino();
			};	
		});
		
		$scope.$watchCollection('pc1.Solicitudes.dataSource.ds._data',function(newval,oldval){
			if (newval !=oldval && $scope.solTra.params.IdTraspaso && newval>0){
				fncompletarGrillaDestino();
			};	
		});
		
				
		$scope.$watch('pc1.SaldoCustodia.dataSource.ds._data',function(newval,oldval){
			if (newval !=oldval && $scope.solTra.params.IdTraspaso && pc1.SaldoCustodia.dataSource.ds._data){
				fncompletarGrillaOrigen();
			};	
		});
		
		$scope.$watch('pc1.cmbbx.IdTipoTraspaso',function(newval,oldval){
			if (newval !=oldval && pc1.cmbbx.IdTipoTraspaso){
				fncompletarGrillaOrigenCantidad	(pc1.cmbbx.IdTipoTraspaso);	
			};	
		});
		
		$scope.$watch('pc1.cmbbx.IdMonedaTraspaso',function(newval,oldval){
			if (newval !=oldval && pc1.cmbbx.IdMonedaTraspaso){
				pc1.idMonedaMontoTotal = pc1.cmbbx.IdMonedaTraspaso;
				pc1.cantidadDecimalesMontoTotal = util.bcs_formato(pc1.MontoTotalTraspaso,'FMM','Monto',pc1.cmbbx.IdMonedaTraspaso, '','').cantidadDecimales;
				if(pc1.cmbbx.oClientePortafolio.seleccionado){
					pc1.buscarCustodia();
				}
				if(pc1.cmbbx.EntidadDestinoObj){
					pc1.buscarDestinos();
				}
			}else {
				if(pc1.cmbbx.oClientePortafolio.seleccionado){
					apiGridCustodiaFondosMutuos.removeAll();
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_SELECCIONAR_MONEDA_PARA_BUSCAR_CUSTODIA"));					
				}
			}
		});
			 
		 
		/******************************************************
		 * GO
		 * ***************************************************/
		$scope.$watch('$viewContentLoaded', function(){
			   //INGMOD: Ingreso/modificacion
			if($scope.solTra.params.IdTraspaso){
				bcsComunes.ventanaTitulo(pc1.idVentana, $translate.instant("UI_EDICION_DE_TRASPASO"));
				pc1.modoPantalla="EDICION";
				$timeout(function() {
					var promesa = bcsComunes.ventanaPromesa(pc1.domVentana);
						$.when(promesa).then(function(){
							$.when(promesa).then(function(){
								pc1.buscarTraspaso();
								$.when(promesa).then(function(){
									pc1.buscarTraspasoDetalleOrigen();
								});
								
							});
							pc1.buscarTraspasoDetalleDestino();
						});
				},50);
			}else{
				bcsComunes.ventanaTitulo(pc1.idVentana, $translate.instant("UI_INGRESO_DE_TRASPASO"));
				pc1.modoPantalla="INGRESO";
			}
		});
		

	});
}());
