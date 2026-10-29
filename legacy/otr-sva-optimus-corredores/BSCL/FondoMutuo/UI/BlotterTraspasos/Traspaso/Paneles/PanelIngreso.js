(function() {
	angular.module('ModuloFFM').controller('PanelIngresoCtrl',function($rootScope,$scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsConfiguracionFFM,bcsServicioFondosMutuos
			,bcsAlerta, OPCIONES_BCSALERTA,$http){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc2 = this;
		pc2.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc2.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		pc2.cmbbx		= {};
		pc2.datasource  = {};
		pc2.apiGridDestinoFondosMutuos= {};
		pc2.CantidadTotalTraspaso=0;
		pc2.MontoTotalTraspaso=0;
		pc2.CantidadDecimalesMontoTraspaso = 0;
		pc2.DestinosEliminados = [];
		pc2.IdMonedaFondo = $rootScope.SesionIdMonedaSistema;
		
		pc2.MontoTotalDestino=0;
		pc2.PorcentajeTotalDestino=0;
		pc2.formData= {};
		pc2.modoPantalla="";
		pc2.PorcentajeMoneyMarket=100;
		pc2.ConfiguracionFM = {};
		pc2.ConsultarTraspaso 					= bcsConfiguracionFFM.getDataSourceConsultarTraspaso();
		pc2.ConsultarTraspasoDetalleOrigen 		= bcsConfiguracionFFM.getDataSourceConsultarTraspasoDetalleOrigen();
		pc2.ConsultarTraspasoDetalleDestino		= bcsConfiguracionFFM.getDataSourceConsultarTraspasoDetalleDestino();
		
		pc2.formData = {};
	    pc2.EditarCabecera='S';
	    
	 	
	 
	    if ($scope.solTra.params.Modo){pc2.Modo=$scope.solTra.params.Modo;};
	    
	    pc2.ConsultarParametroFondoMutuo = bcsConfiguracionFFM.getDataSourceConsultarParametroFondoMutuo();
	
		/*********************************************
		 * Cargar configuraciones
		 ********************************************/
		bcsComunes.ventanaAutoHeight(pc2.idVentana)
	     
		//Busca valor fondo Money Market por defecto para traspasos
	    var fnConsultarConfiguracionFondoMoney = function(){
			 var filters= [ { field: "NombreTipoParametro",	operator: "eq", value: 'TRASPASO_FONDO_MONEY_MARKET'},
		 					{ field: "IdEmpresa",			operator: "eq", value: $scope.$root.SesionIdEmpresa },
		 					{ field: "IdEstado",			operator: "eq", value: 'VIG' } ];
	  
			 pc2.ConsultarParametroFondoMutuo.query({ filter: filters }).then(function(){
			 		if(pc2.ConsultarParametroFondoMutuo.ds._data.length > 0) {
			 			pc2.FondoMoneyMarket=pc2.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro? pc2.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro : '';
			 			
			 		}else{
			 			pc2.FondoMoneyMarket='';
			 			pc2.PorcentajeMoneyMarket=0;
			 			};
			 	});
		 };
		 
		 fnConsultarConfiguracionFondoMoney();
		//Busca valor Edicion de Cabecera en el Detalle
		 
	    var fnConsultarConfiguracionFondoMoney = function(){
			 var filters= [ { field: "NombreTipoParametro",	operator: "eq", value: 'TRASPASO_INGRESO_EDITAR_CABECERA'},
		 					{ field: "IdEmpresa",			operator: "eq", value: $scope.$root.SesionIdEmpresa },
		 					{ field: "IdEstado",			operator: "eq", value: 'VIG' }
		 					];
	  
			 pc2.ConsultarParametroFondoMutuo.query({ filter: filters }).then(function(){
			 		if(pc2.ConsultarParametroFondoMutuo.ds._data.length > 0) {
			 			pc2.EditarCabecera=pc2.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro? pc2.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro : 'S';
			 			
			 		}else{
			 			pc2.EditarCabecera='S';
			 			};
			 	});
		 };
		 fnConsultarConfiguracionFondoMoney();

		 
		 
		 /*********************************************
		 * COMBOBOX
		 ********************************************/
		pc2.cmbbx.oClientePortafolio ={
				tipo : "combobox-cliente-portafolio",
				configuracion :{},
				idActual : 0,
				seleccionado : false,
				filtros: {
					 IdEmpresa 		 : {"field": "IdEmpresa"		,"operator": "eq", "value"   :  $scope.SesionIdEmpresa }
					,IdEstadoCliente : {"field": "IdEstadoCliente"	,"operator": "eq", "value"   :  'ACT'}
					,Proposito 		 : {"field": "Proposito"		,"operator": "eq", "value"   :  'FFMM'}
					,IdPortafolio	 : {"field": "IdPortafolio"		,"operator": "eq", "value"   :  undefined}					
				},
			};	
		
		pc2.cmbbx.EntidadOrigen={
				filtros : {
					IdEstado 			: { field: "IdEstado"			  , operator: "eq", value: 'ACT' }
				},
			};	
		
		pc2.cmbbx.Motivo={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};

		pc2.cmbbx.TipoTraspaso={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};
		
		pc2.formData.FiltrosBusquedaFondo = {
					IdCategoriaPerfilFondoMutuo : { "field"	: "IdCategoriaPerfilFondoMutuo"	,"operator": "eq", "value"   :  undefined},
					IdFondo 					: { "field" : "IdFondo"						,"operator": "eq", "value"   : undefined},
					IdCliente 					: { "field"	: "IdCliente"					,"operator": "eq", "value"   :  undefined},
					IdPortafolio 				: { "field"	: "IdPortafolio"				,"operator": "eq", "value"   :  undefined},
					IdEstado 					: { field	: "IdEstado"					, operator:  "eq", value	 : 'ACT' },
					BloqueoAporte 				: { field	: "BloqueoAporte"				, operator:  "eq", value	 : 'N' },
					BloqueoRescate 				: { field	: "BloqueoRescate"				, operator:  "eq", value	 : 'N' },
					IdMoneda					: { field	: "IdMoneda"					, operator:  "eq", value	 : $rootScope.SesionIdMonedaSistema },
					EsFondoNacional 			: { "field"   : "EsFondoNacional", "operator": "eq", "value": 'S'},
			};
			
		pc2.filtroMonedaUSD = { "field"	: "IdMoneda"					, "operator":  "eq", "value"	 : 'USD' };
		pc2.filtroMonedaCLP = { "field"	: "IdMoneda"					, "operator":  "eq", "value"	 : $rootScope.SesionIdMonedaSistema };
		
		pc2.formData.FiltrosComboFondo = {
			complejo: [pc2.filtroMonedaUSD, pc2.filtroMonedaCLP],
			logic: "and",
		  	filters: [
				{ "field"   : "IdEstado", "operator": "eq",  "value" :'ACT' },
				{ "field"   : "EsFondoNacional", "operator": "eq", "value": 'S'},
				{
				    logic: "or",
				    filters: [pc2.filtroMonedaUSD, pc2.filtroMonedaCLP]
				}
			]
		};
				 
		pc2.cmbbx.filtrosRegimen = {
				IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
				IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
				IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
				IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
		}
		
		pc2.cmbbx.filtrosRegimenDestino = {
				IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
				IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
				IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
				IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
		}
		
		/*********************************************
		 * DEFINICION DATASOURCE 
		 ********************************************/
		var defDestinos = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 6,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300016",
							
							dataType: "json"
						}
					},
					filter: [
					   { field : "IdEstado"		        , operator: "eq", value: 'ACT' }
					],
	                sort 			: false,
					schema: {
						model: {
							fields: {
						    	IdSerieFondo          		:	{ type: "string"},
						    	NombreSerieFondo			:	{ type: "string"},
						    	IntMontoTraspaso			: 	{ type: "number"},//calculado 
						    	Porcentaje					: 	{ type: "number"},//calculado
			        			IdDetalleTraspaso			:	{ type: "number"},
			        			IdRegimenFondoMutuo			:	{ type: "string"}
							}
						}
					}
				}	
			};
		
		var dsDestinos = new bcs.DataSource(defDestinos);
		pc2.Solicitudes = {
				dataSource: dsDestinos,
				columns: [
					    {field: "IdSerieFondo",				title: $translate.instant('UI_INSTRUMENTO'),					width: "100px", align: "left"},
					    {field: "NombreSerieFondo",			title: $translate.instant('UI_NOMBRE'),							width: "100px", align: "left"},
					    {field: "IdRegimenFondoMutuo",		title: $translate.instant('UI_REGIMEN'),						width: "100px", align: "left"},
					    {field: "IntMontoTraspaso",			title: $translate.instant('UI_MONTO_TRASPASO'),					width: "100px", align: "right", editable: false},
						{field: "Porcentaje",				title: $translate.instant('UI_PORCENTAJE_TRASPASO'),			width: "100px", align: "right", editable: true}
					    ],	
				sortable		: true,
				pageable		: true,
				height			: 160,
				selectable		: "row",
			    userSettings	: false,
				navigatable		: true,
				save: function(e) 	{calculoMontoDestino(e.values, e.model);},
				gridCreated     : function(g){
					pc2.ApiDestinos = g;
					
					
				},
				notEditableColumns : function(fila)
				{
					//if (fila.IdSerieFondo == '' || (pc2.PorcentajeTotalDestino > 0 && ModoGrillaMasivo==false && (fila.Porcentaje ==null ||fila.Porcentaje ==0) )) 
					//{
					//	fila.IntMontoTraspaso = 0;
					//	fila.Porcentaje = 0;
					//	return ['Porcentaje'];
					//}
				},
				 dataBound: function(e) {
					    console.log("dataBound");
					  }
		};
		
		
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/

		pc2.buscarDestinos = function ()
		{
			
			pc2.Solicitudes.dataSource.removeAll();

			pc2.MontoTotalDestino=0;
			pc2.PorcentajeTotalDestino=0;
			pc2.Solicitudes.dataSource.add({IdSerieFondo: "",
				NombreSerieFondo 			:	"",
				IntMontoTraspaso 			:	Number('0'),
				Porcentaje 					:	Number('0')

			 });
			
			var filters = [];
			filters
					.push(//{field: "IdEmpresa"	, operator: "eq", value: $scope.SesionIdEmpresa},
						  { field: "IdEstado"			, operator: "eq", value: 'ACT' }
						 ,{ field: "IdTraspaso"			, operator: "eq", value: $scope.solTra.params.IdTraspaso }
					);
		
			pc2.Solicitudes.dataSource.filter(filters);
			pc2.Solicitudes.dataSource.read();
			
		};
		
		pc2.preparaParametros = function()
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
			
		
			parametrosConsulta.p_IdCliente = (pc2.cmbbx.oClientePortafolio.seleccionado) ? pc2.cmbbx.oClientePortafolio.seleccionado.IdCliente : 0;
			parametrosConsulta.p_IdPortafolio = (pc2.cmbbx.oClientePortafolio.seleccionado) ? pc2.cmbbx.oClientePortafolio.seleccionado.IdPortafolio : 0;
			
			return parametrosConsulta;
		};
	
		
		var calculoMontoDestino = function(values, model){

			var Destino = pc2.Solicitudes.dataSource.data() || [];
			pc2.MontoTotalDestino=0;
			pc2.PorcentajeTotalDestino=0;
			$.each(Destino,function(key,value){
				//MONTO
				pc2.PorcentajeTotalDestino += Number(value.Porcentaje);
				//PORCENTAJE
				if (Number(pc2.MontoTotalTraspaso) > 0)
				{
					pc2.Solicitudes.seleccion.IntMontoTraspaso = (Number(pc2.MontoTotalTraspaso) * Number(values.Porcentaje))/Number(100);
				};

			});	
		};
		var actualizarPorcentaje = function(){
			var Destino = pc2.Solicitudes.dataSource.data() || [];
			$.each(Destino,function(key,value){

				if (Number(pc2.MontoTotalTraspaso) > 0)
				{
					value.IntMontoTraspaso =      (Number(pc2.MontoTotalTraspaso) * Number(value.Porcentaje))/Number(100);

				}else{value.IntMontoTraspaso =0;};

			});

		}		
		
		pc2.quitar = function ()
		{
			pc2.Solicitudes.seleccion.Porcentaje  =0;
			
			if(pc2.Solicitudes.seleccion.IdDetalleTraspaso > 0){
				pc2.Solicitudes.seleccion.IdEstado = "INA";
				pc2.DestinosEliminados.push(pc2.Solicitudes.seleccion);
			}
			dsDestinos.ds._data.remove(pc2.Solicitudes.seleccion);
			dsDestinos.ds._view.remove(pc2.Solicitudes.seleccion);
			
			pc2.apiGridDestinoFondosMutuos.refresh();
			 if (dsDestinos.ds._view.length ==0){
				 pc2.moneyMarket=true;
			 }else{pc2.moneyMarket=false;
			 };

		};
		pc2.agregar = function ()
		{
			var cont=0;
			
			$.each(dsDestinos.ds._view,function(key,value){
				if (value.IdSerieFondo == pc2.FondoMutuoDestino.IdSerieFondo){
					cont++;	
				};
			});
			
			if (cont== 0){
				
					dsDestinos.add({IdSerieFondo: pc2.FondoMutuoDestino.IdSerieFondo,
						CodigoSerie: pc2.FondoMutuoDestino.IdSerieFondoNombre,
						DocumentoIdentidadEmisor: '',
						Estado: '',
						FechaIngreso: '',
						FechaIngresoRegistro: '',
						FechaModificacion: '',
						FechaModificacionRegistro: '',
						IdDetalleTraspaso: 0,
						IdEstado: 'ACT',
						IdSerieFondo: pc2.FondoMutuoDestino.IdSerieFondo,
						IdTraspaso:  $scope.solTra.params.IdTraspaso,
						IntMontoTraspaso: Number('0'),
						NombreCortoEmisor: '',
						NombreSerieFondo: pc2.FondoMutuoDestino.IdSerieFondoNombre,
						Porcentaje:  Number('0'),
						IdRegimenFondoMutuo: pc2.cmbbx.IdRegimenFondoMutuoDestino
					
				  });
				  
					pc2.moneyMarket=false;
					 
			}else{		
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INSTRUMENTO_EXISTE"));
    	 		return;
			};
			
			
			
		
		};
		
		
		
		pc2.ingresar = function ()
		{
			
			var Destino = pc2.Solicitudes.dataSource.data() 	|| [];
			
    	 	if (pc2.cmbbx.oClientePortafolio.seleccionado==undefined || pc2.cmbbx.oClientePortafolio.seleccionado==0)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_CLIENTE"));
    	 		return;
    	 	}
    	 	

    	 	if (pc2.cmbbx.IdRegimenFondoMutuo==undefined || pc2.cmbbx.IdRegimenFondoMutuo !='18Q')
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_REGIMEN_DE_INSTRUMENTO_NO_18Q"));
    	 		return;
    	 	}

    	 	
			if (!pc2.nemotecnico)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_INSTRUMENTO"));
    	 		return;
    	 	}
    	 	
			if (pc2.FolioMLT==null || pc2.FolioMLT==undefined)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_INGRESAR_FOLIO_MLT"));
    	 		return;
    	 	}
			
			if (pc2.FolioCSC==null || pc2.FolioCSC==undefined)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_INGRESAR_FOLIO_CSC"));
    	 		return;
    	 	}
			
			if (pc2.cmbbx.IdEmpleadoEjecutivoObj=undefined || pc2.cmbbx.IdEmpleadoEjecutivo==0)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_EJECUTIVO"));
    	 		return;
    	 	}
					
			var tablaOrigen	= [];
			if($scope.solTra.params.IdTraspaso && pc2.modoPantalla=="EDICION"){
				tablaOrigen.push({
					 "IdTraspaso" :  $scope.solTra.params.IdTraspaso
					,"IdSerieFondo" : pc2.nemotecnico
					,"Cantidad" : 0
					,"IdRegimenTributario":(pc2.cmbbx.IdRegimenFondoMutuo) ? pc2.cmbbx.IdRegimenFondoMutuo : ''
					,"IdDetalleTraspaso" :Number(pc2.IdDetalleTraspaso)	
					});
				
			}else{
				tablaOrigen.push({
				  	 "IdInterno" : 1
					,"IdSerieFondo" : pc2.nemotecnico
					,"Cantidad" : 0
					,"IdRegimenTributario":(pc2.cmbbx.IdRegimenFondoMutuo) ? pc2.cmbbx.IdRegimenFondoMutuo : ''
					});
			};
    	 			
			var PorcentajeTotal=0;
			var tablaDestino	= [];
			$.each(Destino,function(key,value){

				if($scope.solTra.params.IdTraspaso && pc2.modoPantalla=="EDICION"){
					if ((Number(value.Porcentaje)>0 || Number(value.IdDetalleTraspaso)>0) && $scope.solTra.params.IdTraspaso == Number(value.IdTraspaso) && value.IdEstado=='ACT' ){
					tablaDestino.push({
						 "IdTraspaso" :  $scope.solTra.params.IdTraspaso
						,"IdSerieFondo" : value.IdSerieFondo
						,"IdRegimenFondoMutuo" : value.IdRegimenFondoMutuo
						,"Porcentaje" : Number(value.Porcentaje)
						,"IdDetalleTraspaso" :Number(value.IdDetalleTraspaso)
						}); 
					};
				};
				
			});
			
			if (tablaOrigen.length ==0)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_ORIGEN"));
    	 		return;
    	 	}
			
	   		if (dsDestinos.ds._view.length ==0 && pc2.Modo == "DETALLE" &&  pc2.moneyMarket==false)
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_DESTINO") +  " " + $translate.instant("UI_Y") + " " + $translate.instant("UI_PORCENTAJE_VALIDO"));
    	 		return;
    	 	}


	   		PorcentajeTotal=0;
	   		//if (pc2.moneyMarket==false){
	   			$.each(tablaDestino,function(key,value){
	   				
	   					PorcentajeTotal=Number(PorcentajeTotal)+ Number(value.Porcentaje);	
	   				
	    			
	    		});	
	   		//}
    		
    		    		
    		if ((Number(PorcentajeTotal) != 100 && pc2.moneyMarket==false ) && pc2.Modo == "DETALLE"  ) 
    	 	{
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_TOTAL_PORCENTAJE_INCORRECTO"));
    	 		return;
    	 	};
    	 	if (pc2.Modo == "DETALLE"){pc2.IdEstado='ING';};
			
    	 	if(pc2.DestinosEliminados.length > 0){
				$.each(pc2.DestinosEliminados,function(key,value){
					tablaDestino.push({
						 "IdTraspaso" :  $scope.solTra.params.IdTraspaso
						,"IdSerieFondo" : value.IdSerieFondo
						,"Porcentaje" : value.Porcentaje
						,"IdDetalleTraspaso" :value.IdDetalleTraspaso
					});
				});
			}
			
    		//incluye moneyMarket x defecto cuando no eexiste un destino
	   		if (pc2.moneyMarket==true && Number(PorcentajeTotal)==0 && pc2.Modo == "DETALLE")
   			{
	   				if (pc2.FondoMoneyMarket=='') 
	   					{
	   						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CONFIGURAR_MONEY_MARKET_O_DESTINO_VALIDO"));
	   						return;
	   					}else{
	   						
	   						
	   						var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_DESTINO_MONEYMARKET" ) + ", ¿"  + $translate.instant("UI_DESEA_CONTINUAR" ) +"?",pc2.idVentana);
	   						t .then(function(result)
	   						      {		
	   								 if (result===OPCIONES_BCSALERTA.SI)
	   							       {	
		   			   						tablaDestino.push({
		   			   						 "IdTraspaso" :  $scope.solTra.params.IdTraspaso
		   			   						,"IdSerieFondo" : pc2.FondoMoneyMarket
		   			   						,"Porcentaje" : Number(pc2.PorcentajeMoneyMarket)
		   			   						,"IdDetalleTraspaso" :0
		   			   						});
		   			   					fnGuardarCambios(tablaOrigen,tablaDestino);
	   									 
	   							       }else{return;}
	   								 
	   								 ;
	   							});
	   						
	   					}
	   			
   			}else{
   				fnGuardarCambios(tablaOrigen,tablaDestino);
   				
   			};
    	
		}
		var fnGuardarCambios = function(tablaOrigen,tablaDestino){
			 bcsComunes.ventanaOverlay(pc2.idVentana,true);
			 var datosIngreso = {
					 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					 p_IdModulo : 'FMM',
						 
					 p_Traspaso : [{
						    IdInterno		      		:	1,
							IdCliente 	          		:	(pc2.cmbbx.oClientePortafolio.seleccionado) ? pc2.cmbbx.oClientePortafolio.seleccionado.IdCliente : 0,
					    	IdPortafolio          		:	(pc2.cmbbx.oClientePortafolio.seleccionado) ? pc2.cmbbx.oClientePortafolio.seleccionado.IdPortafolio : 0,
					    	IdEntidadRecaudadora        :	pc2.cmbbx.EntidadOrigenObj.IdEntidadRecaudadora,
					   	   	IdMotivoTraspaso			: 	pc2.cmbbx.IdMotivo,
					    	IdTipoMovimientoTraspaso	:   'TRAING',
					    	IdTipoTraspaso				:	pc2.cmbbx.IdTipoTraspaso,
					    	Glosa						:	(pc2.formData.comentario)?pc2.formData.comentario:'',
					    	FechaHoraSuscripcion		: 	$scope.solTra.fechaInicioSol,
					    	IdDivisionPais				: 	($scope.solTra.idDivisionPaisComuna)?$scope.solTra.idDivisionPaisComuna:0,
					    	FolioTraspaso				: 	($scope.solTra.FolioTraspaso)?$scope.solTra.FolioTraspaso:0,
					    	Monto 				  		:	Number(pc2.MontoTotalTraspaso),
					    	FolioMLT					: 	(pc2.FolioMLT)?pc2.FolioMLT:0,
					    	FolioCSC					: 	(pc2.FolioCSC)?pc2.FolioCSC:0,
					    	IdEmpleadoEjecutivo			: 	(pc2.cmbbx.IdEmpleadoEjecutivo)?pc2.cmbbx.IdEmpleadoEjecutivo:0,
					    	IdEstado					: 	pc2.IdEstado
					    	
					 }],
					 
					 p_TraspasoDetalleOrigen : tablaOrigen,
					 
					 p_TraspasoDetalleDestino : tablaDestino 
					 
			 }
				if($scope.solTra.params.IdTraspaso && pc2.modoPantalla=="EDICION"){
					datosIngreso.p_Traspaso[0].IdTraspaso=$scope.solTra.params.IdTraspaso;
					bcsServicioFondosMutuos.ModificarTraspaso(datosIngreso).$promise.then(function(respuesta){
				     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion modificada"},pc2.idVentana);
				     		bcsComunes.ventanaOverlay(pc2.idVentana,false);
				     		bcsComunes.ventanaCerrar(pc2.idVentana);
			     			
			     			
				     	 }else{
				     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICACION_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc2.idVentana);
				     		bcsComunes.ventanaOverlay(pc2.idVentana,false);
				     	 }
			   		 },function(respuesta){
			   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICACION_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc2.idVentana);
			   			bcsComunes.ventanaOverlay(pc2.idVentana,false);
			   		 });
					
					
				}else{
				     bcsServicioFondosMutuos.CrearTraspaso(datosIngreso).$promise.then(function(respuesta){
				     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada"},pc2.idVentana);
			     			bcsComunes.ventanaOverlay(pc2.idVentana,false);
			     			bcsComunes.ventanaCerrar(pc2.idVentana);
			     			
				     	 }else{
				     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc2.idVentana);
				     		bcsComunes.ventanaOverlay(pc2.idVentana,false);
				     	 }
			   		 },function(respuesta){
			   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pc2.idVentana);
			   			bcsComunes.ventanaOverlay(pc2.idVentana,false);
			   		 });
					 
					 
				}	 
		}
		
	    var fnConsultarTraspaso = function(){
			 var filters= [ { field: "IdTraspaso",	operator: "eq", value: $scope.solTra.params.IdTraspaso},
		 					{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa }
		 				  ];
	  
			 pc2.ConsultarTraspaso.query({ filter: filters }).then(function(){
			 		if(pc2.ConsultarTraspaso.ds._data.length > 0) {
			 			var Traspaso = pc2.ConsultarTraspaso.ds._data || [];
						$.each(Traspaso,function(key,value){
								pc2.cmbbx.IdEntidadOrigen=value.CodigoEntidadRecaudadora || -1;
								pc2.cmbbx.IdPortafolio=value.IdPortafolio || -1;
								pc2.cmbbx.oClientePortafolio.idActual=value.IdPortafolio || -1;
								pc2.cmbbx.oClientePortafolio.idDefecto=value.IdPortafolio || -1;
								
								pc2.cmbbx.IdMotivo=value.IdMotivoTraspaso || -1;
								pc2.cmbbx.IdTipoTraspaso=value.IdTipoTraspaso || -1;
								pc2.formData.comentario=value.Glosa || '';
								$scope.solTra.fechaInicioSol=value.FechaHoraSuscripcion || '';
								$scope.solTra.FolioTraspaso=value.FolioTraspaso || 0;
								$scope.solTra.idDivisionPaisComunaPost=value.IdDivisionPais || 0;
								pc2.FolioCSC=value.FolioCSC	|| 0;
								pc2.FolioMLT=value.FolioMLT	|| 0;
								pc2.MontoTotalTraspaso=value.Monto	|| 0;
								pc2.cmbbx.IdEmpleadoEjecutivo=value.IdEmpleadoEjecutivo	|| 0;
								
								pc2.IdEstado=value.IdEstado || '';
								
								pc2.cmbbx.NombreCortoEmisor=value.NombreCortoEmisor || '';
								pc2.cmbbx.DocumentoIdentidadEmisor=value.DocumentoIdentidadEntidadRecaudadora || '';
								pc2.MotivoTraspaso=value.MotivoTraspaso || '';
								pc2.TipoTraspaso=value.TipoTraspaso || '';
								pc2.NombreCompletoEjecutivo=value.NombreCompletoEjecutivo || '';
								pc2.NombrePortafolio=value.DocumentoIdentidad + ' ' + value.NombreCompleto + '  (' + value.CodigoPortafolio + ')'
								pc2.cmbbx.oClientePortafolio.filtros.IdPortafolio.value = pc2.cmbbx.IdPortafolio;
								
								pc2.cmbbx.IdPortafolioPostFiltro = value.IdPortafolio || -1;
								pc2.IdMonedaFondo = value.IdMonedaFondo;
								pc2.CantidadDecimalesMontoTraspaso = util.bcs_formato(pc2.MontoTotalTraspaso,'FMM','Monto',pc2.IdMonedaFondo, '','').cantidadDecimales;
								pc2.formData.FiltrosComboFondoDestino = {
									IdMoneda:	{ field	: "IdMoneda"						, operator	: "eq",  value : pc2.IdMonedaFondo},
									IdEstado:	{ field   : "IdEstado", operator: "eq",  value :'ACT' },
									EsFondoNacional: { field   : "EsFondoNacional", operator: "eq", value: 'S'}
								};
								
						});
			 			
			 		}else{
						bcsComunes.ventanaOverlay(pc2.domVentana,false);
						bcsComunes.ventanaCerrar(pc2.domVentana);
						return false;
			 		};
			 	});
		 };
		 
		    var fnConsultarTraspasoDetalleOrigen = function(){
				 var filters= [ { field: "IdTraspaso"			, operator: "eq", value: $scope.solTra.params.IdTraspaso},
			 					{ field: "IdEstado"				, operator: "eq", value: 'ACT'}
			 				  ];
		  
				 pc2.ConsultarTraspasoDetalleOrigen.query({ filter: filters }).then(function(){
				 		if(pc2.ConsultarTraspasoDetalleOrigen.ds._data.length > 0) {
				 			
				 			var TraspasoDetalleOrigen = pc2.ConsultarTraspasoDetalleOrigen.ds._data || [];
							$.each(TraspasoDetalleOrigen,function(key,value){
								pc2.IdSerieFondoDefecto=value.IdSerieFondo;
					 			pc2.cmbbx.IdRegimenFondoMutuo=value.IdRegimenTributario|| '';
					 			pc2.IdDetalleTraspaso=value.IdDetalleTraspaso|| 0;

								pc2.Regimen=value.RegimenTributario || '';
								if (pc2.Modo == "DETALLE") {
					    			pc2.nemotecnico=value.IdSerieFondo || '';
									pc2.IdMonedaFondo = value.IdMonedaFondo;
					    	 	};
								
					    	 	
							});
				 			
				 		}else{
							bcsComunes.ventanaOverlay(pc2.domVentana,false);
							bcsComunes.ventanaCerrar(pc2.domVentana);
							return false;
				 		};
				 	});
			 };
		    let fnConsultarTraspasoDetalleDestino = function(){
				 let filters= [ { field: "IdTraspaso"			, operator: "eq", value: $scope.solTra.params.IdTraspaso},
			 					{ field: "IdEstado"				, operator: "eq", value: 'ACT'}
			 				  ];
		  
				 pc2.ConsultarTraspasoDetalleDestino.query({ filter: filters }).then(function(){
				 		if(!pc2.ConsultarTraspasoDetalleDestino.ds._data.length > 0) {
				 			pc2.moneyMarket=true;
							bcsComunes.ventanaOverlay(pc2.domVentana,false);
							bcsComunes.ventanaCerrar(pc2.domVentana);
							return false;
				 		}else{
				 			pc2.moneyMarket=false;
				 			let detalleDestinos = pc2.ConsultarTraspasoDetalleDestino.ds._data;
				 			$.each(detalleDestinos,function(key,value){
					 			dsDestinos.add({IdSerieFondo: value.IdSerieFondo,
									CodigoSerie: value.CodigoSerie,
									DocumentoIdentidadEmisor: '',
									Estado: '',
									FechaIngreso: '',
									FechaIngresoRegistro: '',
									FechaModificacion: '',
									FechaModificacionRegistro: '',
									IdDetalleTraspaso: value.IdDetalleTraspaso || 0,
									IdEstado: 'ACT',
									IdSerieFondo: value.IdSerieFondo,
									IdTraspaso:  value.IdTraspaso,
									IntMontoTraspaso: value.Monto,
									NombreCortoEmisor: '',
									NombreSerieFondo: value.NombreSerieFondo,
									Porcentaje:  value.Porcentaje,
									IdRegimenFondoMutuo: value.IdRegimenFondoMutuo
							  });
							  actualizarPorcentaje();
							});
				 		};
				 	});
			 };
	
		var fncompletarGrillaDestino = function(){
			
			var TraspasoDetalleDestino = pc2.ConsultarTraspasoDetalleDestino.ds._data || [];
 				$.each(TraspasoDetalleDestino,function(key,value){
				var Porcentaje=value.Porcentaje;
				var IdSerieFondo=value.IdSerieFondo;
				var IdDetalleTraspaso=value.IdDetalleTraspaso;
				var i=0;
				$.each(pc2.Solicitudes.dataSource.data(),function(key,value){
					if (IdSerieFondo==value.IdSerieFondo){
						pc2.Solicitudes.dataSource.ds._data[i].IdDetalleTraspaso=Number(IdDetalleTraspaso);
						pc2.Solicitudes.dataSource.ds._data[i].Porcentaje=Porcentaje;
						return false;
					};
					i++;
				});
				
			});
 				actualizarPorcentaje();
		};
		
 
		pc2.buscarTraspaso = function ()
		{
			fnConsultarTraspaso();
		}
		
	 
		pc2.buscarTraspasoDetalleOrigen = function ()
		{
			fnConsultarTraspasoDetalleOrigen();
		}
		
		
		pc2.buscarTraspasoDetalleDestino = function ()
		{
			fnConsultarTraspasoDetalleDestino();
		}
		
		/*********************************************
		 * WATCH
		 ********************************************/

		$scope.$watch('pc2.MontoTotalTraspaso',function(newval,oldval){
			if (newval !=oldval){
				actualizarPorcentaje();
			};	
		});

		$scope.$watch('pc2.Solicitudes.dataSource.ds._data',function(newval,oldval){
			if (newval !=oldval && $scope.solTra.params.IdTraspaso && pc2.Solicitudes.dataSource.ds._data){
				fncompletarGrillaDestino();
			};	
		});
		
		$scope.$watch('pc2.FondoMutuo',function(newval,oldval){
				if (newval && newval.IdFondo) {
					pc2.cmbbx.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
					pc2.IdMonedaFondo = newval.IdMoneda;
					pc2.CantidadDecimalesMontoTraspaso = util.bcs_formato(pc2.MontoTotalTraspaso,'FMM','Monto',pc2.IdMonedaFondo, '','').cantidadDecimales;
				}else {
					pc2.IdMonedaFondo = $rootScope.SesionIdMonedaSistema;
					pc2.CantidadDecimalesMontoTraspaso = 0;
				};
			
		});
		
		$scope.$watch('pc2.FondoMutuoDestino',function(newval,oldval){
			 if (newval && newval.IdFondo) {
				 pc2.cmbbx.filtrosRegimenDestino.IdSerieFondo.value = newval.IdSerieFondo;
			 };
		});
	
		 
		/******************************************************
		 * GO
		 * ***************************************************/
		$scope.$watch('$viewContentLoaded', function(){
			   //INGMOD: Ingreso/modificacion
			if($scope.solTra.params.IdTraspaso){
				bcsComunes.ventanaTitulo(pc2.idVentana, $translate.instant("UI_EDICION_DE_TRASPASO"));
				pc2.modoPantalla="EDICION";
				$timeout(function() {
					var promesa = bcsComunes.ventanaPromesa(pc2.domVentana);
						$.when(promesa).then(function(){
							$.when(promesa).then(function(){
								pc2.buscarTraspaso();
								$.when(promesa).then(function(){
									pc2.buscarTraspasoDetalleDestino()		
								});
								
							});
							pc2.buscarTraspasoDetalleOrigen();
						});
					
				},50);
			}else{
				bcsComunes.ventanaTitulo(pc2.idVentana, $translate.instant("UI_INGRESO_DE_TRASPASO"));
				pc2.modoPantalla="INGRESO";
			}
		});
		

	});
}());
