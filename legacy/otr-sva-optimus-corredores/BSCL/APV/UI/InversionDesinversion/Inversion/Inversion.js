(function() {
	angular.module('ModuloAPV').controller('InversionAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV,bcsServicioOrden,bcsServicioPlataforma,bcsConfiguracionAPV,bcsAlerta,OPCIONES_BCSALERTA)
	{
		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		
		var invAPV = this;
		var OrdenesRentaVariable = Array();
		var OrdenesFondosMutuos = Array();
		var cobrosModificadosFinal = [];
		var varCantidad = 0;		
		invAPV.datosForm = {};
		invAPV.fn = {};
		invAPV.datasource = {};
		invAPV.ConfiguracionCmbo = {};
		invAPV.aporteNormal = {};
		invAPV.consultas = {};
		invAPV.TipoOrdenHabilito = 'RV';
		var TipoAgrupadorCuentaInversion = 'PO';
		invAPV.HabilitoControlesBusqueda = true;
		invAPV.OrdenesRV = [];
		invAPV.datosForm.fechaInicial = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		var msgOrdenRV = new Array();
		var msgOrdenFM = new Array();
		var gridSaldoCuentaInversion = null;
		var mensajeResultadoOrdenes = new Array();
		var OrdenesRV = Array();
   	 	var OrdenesFM = Array();



		//******************************************************************************************************************************/
		//************************************************* Definicion de DataSources **************************************************/
		//******************************************************************************************************************************/
		var defOrdenes = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                sort 			: false,
				schema: {
					model: {
						fields: {
							Mercado			: { type: "string"},
							Instrumento		: { type: "string"},
							TipoPrecio		: { type: "string"},
							Precio			: { type: "number"},
							Liquidacion		: { type: "sysdate"},
							Moneda	    	: { type: "string"},
							Cantidad		: { type: "number"},	
							Monto			: { type: "number"},
							Regimen			: { type: "string"},
							IdInstrumento	: { type: "number"},
							SerieFondo		: { type: "string"},
							IdPortafolio	: { type: "number"},
							IdMoneda		: { type: "string"},
							NumeroCuota		: { type: "number"}
						}
					}
				}
			}	
		};	
		var defSaldoCuentaInversion = {
			type: "array",
			definition : {
				serverFiltering : false, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                sort 			: false,
				schema: {
					model: {
						fields: {
							NombrePortafolio	: { type: "string"},
							IdProductoAgrupador : { type: "number"},
							Producto			: { type: "string"},
							IdPortafolio		: { type: "number"},
							IdPlanTipoInversion : { type: "number"},
							NombrePlan			: { type: "string"},
							Regimen				: { type: "string"},
							SaldoDisponible		: { type: "number"},
							IdMoneda			: { type: "string"},
							MontoInvertir		: { type: "number"},
							IdEstado			: { type: "string"}
						}
					}
				}
			}	
		};
		
		var defConsultaPortafolioCliente = 	 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdPortafolio", dir: "ASC" },
                    pageSize        : 10,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarPortafolioMatrizAgrupador'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{													
													"IdSocioDeNegocio"				: {type: "number"},
													"IdPersona"						: {type: "number"},
													"IdCliente"						: {type: "number"},
													"IdPortafolio" 					: {type: "number"},
													"DocumentoIdentidad"			: {type: "string"},
													"IdMatrizAgrupadorAPV"			: {type: "number"},
													"NombreAgrupador"				: {type: "string"},
													"CodigoAgrupador"				: {type: "string"},
													"NombrePlanTipoInversion"		: {type: "string"},
													"NombreProductoAgrupador"		: {type: "string"},
													"RegimenTributario"				: {type: "string"},
													"IdEstadoAgrupador"				: {type: "string"},
													"CodigoPortafolio" 				: {type: "string"},
													"DescripcionPortafolio"			: {type: "string"},
													"Proposito"						: {type: "string"},
													"IdEstado" 						: {type: "string"},
													"Estado"						: {type: "string"},
													"IdEstadoAutorizacion"			: {type: "string"},
													"FechaIngreso"					: {type: "string"},
													"FechaCreacion"					: {type: "string"},
													"FechaModificacion"				: {type: "string"}
												}
											}
									  }
    			 }
		};	
		
		//******************************************************************************************************************************/
		//*************************************************** Definicion de grillas ****************************************************/
		//******************************************************************************************************************************/
		var dsOrdenes = new bcs.DataSource(defOrdenes);
		invAPV.datasource.Ordenes = {
				dataSource: dsOrdenes,
				columns: [
					{field: "Mercado"	 	,title: $translate.instant('UI_MERCADO')		, width: "80px", align: "rigth", sortable : true, editable: false },
					{field: "IdPortafolio"	,title: $translate.instant('UI_PORTAFOLIO')		, width: "80px", align: "left" , sortable : true, editable: false },
					{field: "IdInstrumento"	,title: $translate.instant('UI_INSTRUMENTO')	, width: "80px", align: "rigth", sortable : true, editable: false, hidden: true},
					{field: "Instrumento"	,title: $translate.instant('UI_INSTRUMENTO')	, width: "80px", align: "rigth", sortable : true, editable: false },
					{field: "SerieFondo"	,title: $translate.instant('UI_FONDO')	, width: "80px", align: "left" , sortable : true, editable: false},
					{field: "Regimen"	 	,title: $translate.instant('UI_REGIMEN')		, width: "80px", align: "rigth", sortable : true, editable: false },
					{field: "TipoPrecio" 	,title: $translate.instant('UI_TIPO_PRECIO')	, width: "80px", align: "rigth", sortable : true, editable: false },
				    {field: "Liquidacion"	,title: $translate.instant('UI_FECHA_LIQUIDACION')	, width: "80px", align: "rigth", sortable : true, editable: false },
				    {field: "IdMoneda"	 	,title: $translate.instant('UI_MONEDA_APORTE')	, width: "80px", align: "rigth", sortable : true, editable: false },
				    {field: "Moneda"	 	,title: $translate.instant('UI_MONEDA')			, width: "80px", align: "left" , sortable : true, editable: false, hidden: true },
				    {field: "Precio"	 	,title: $translate.instant('UI_PRECIO')			, width: "80px", align: "left" , sortable : true, editable: false },
				    {field: "Cantidad"	 	,title: $translate.instant('UI_CANTIDAD')		, width: "80px", align: "rigth", sortable : true, editable: false },
				    {field: "NumeroCuota"	,title: $translate.instant('UI_NUMERO_CUOTAS')	, width: "80px", align: "rigth", sortable : true, editable: false },
				    {field: "Monto"		 	,title: $translate.instant('UI_MONTO')			, width: "80px", align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(Monto,'ORD','Monto',IdMoneda,'','')).valor #</label>"   }
				    ],	
				sortable		: false,
				pageable		: true,
				autoBind		: true,
				height			: 140,
			    selectable 		: "row",
			    userSettings	: true,
				navigatable		: true
		};

		var dsSaldoCuentaInversion = new bcs.DataSource(defSaldoCuentaInversion);
		invAPV.datasource.SaldoCuentaInversion = {
				dataSource: dsSaldoCuentaInversion,
				columns: [
					{field: "NombrePortafolio"	,title: $translate.instant('UI_NOMBRE_PORTAFOLIO'), width: "80px", align: "rigth" , sortable : true, editable: false, hidden: true},
					{field: "IdEstado"			,title: $translate.instant('UI_ESTADO'), width: "80px", align: "rigth" , sortable : true, editable: false, hidden: true},
					{field: "Producto"	 		,title: $translate.instant('UI_PRODUCTO'), width: "120px", align: "left" , sortable : true, editable: false },
					{field: "IdPortafolio" 		,title: $translate.instant('UI_PORTAFOLIO'), width: "65px", align: "left" , sortable : true, editable: false },
					{field: "Regimen"	 		,title: $translate.instant('UI_REGIMEN'), width: "55px", align: "center" , sortable : true, editable: false },
					{field: "IdMoneda"	 		,title: $translate.instant('UI_MONEDA'), width: "55px", align: "center" , sortable : true, editable: false },
					{field: "SaldoDisponible"	,title: $translate.instant('UI_MONTO_PESOS'), width: "80px", align: "rigth" , sortable : true, editable: false	 ,template : " <label> #:  (util.bcs_formato(SaldoDisponible,'ORD','Monto',IdMoneda,'','')).valor #</label>" },
				    {field: "MontoInvertir"		,title: $translate.instant('UI_MONTO_INVERTIDO'), width: "80px", align: "rigth" , sortable : true, editable: false,template : " <label> #:  (util.bcs_formato(MontoInvertir,'ORD','Monto',IdMoneda,'','')).valor #</label>" },
				    ],	
				sortable		: false,
				pageable		: true,
				filterable		: true,
				autoBind		: true,
				height			: 257,
			    selectable 		: "multiple row",
			    userSettings	: true,
				navigatable		: true,
				/*
				rowCellColor 	: function(row) {					
					switch(row["IdEstado"].toUpperCase()) {
						case "ACT":
							//return {column: "Producto", color: "green"}
							return {column: "IdPortafolio", color: "green"};
							//{column: "Regimen", color: "green"}
							//{column: "IdMoneda", color: "green"}
							//{column: "SaldoDisponible", color: "green"};
							break;
						case "BLO":
							return {column: "IdPortafolio", color: "red"};
							break;
						case "PEN":
							return {column: "IdPortafolio", color: "yellow"};
							break;
					}
				},
				*/
				rowColorBackground : function(row) {
					console.log(row);
					switch(row["IdEstado"].toUpperCase()) {
						case "ACT":
							return "green";
							break;
						case "BLO":
							return "red";
							break;
						case "PEN":
							return "yellow";
							break;
					}
				},
				gridCreated    	: function(grid){
						gridSaldoCuentaInversion = grid;
				}
		};
		
		//******************************************************************************************************************************/
		//********************************************** Configuracion de ComboBox *****************************************************/
		//******************************************************************************************************************************/
		invAPV.ConfiguracionCmbo.SocioNegocio = {
			filtros: {
				IdEmpresa 		: {"field": "IdEmpresa"		,"operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente 		: {"field": "EsCliente"		,"operator": "eq", "value"   :  'UI_SI'},
				EstadoCliente 	: {"field": "EstadoCliente"	,"operator": "eq", "value"   :  'ACT'}
			},
		};
		
		invAPV.ConfiguracionCmbo.Portafolio = {
				filtros: {
					IdCliente : {"field" : "IdCliente", "operator" : "eq", "value" : 0},
					Proposito : {"field" : "Proposito", "operator" : "eq", "value" : "PROPOSITOAPV"}
				}	
		};
		
		invAPV.ConfiguracionCmbo.ProductoAgrupador =  {
			filtros: {
				IdPlanTipoInversion : {"field" : "IdPlanTipoInversion", "operator" : "eq", "value" : 0}
			}	
		};
		
		invAPV.ConfiguracionCmbo.RegimenTributario =  {
			filtros: {
				IdProductoAgrupador : {"field" : "IdProductoAgrupador", "operator" : "eq", "value" : 0}
			}	
		};

		invAPV.ConfiguracionCmbo.filtrosCuentaInversion = {
			filtros: {
				IdPortafolio	: {"field"    : "IdPortafolio"   ,"operator": "eq", "value" : -1}	
			}	
		};
		
		invAPV.ConfiguracionCmbo.TipoVigencia = {
			defecto : 'SH', //bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaSoloHoy,
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			}
		};
		invAPV.ConfiguracionCmbo.MonedaLiquidacionOrden = { 
			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
				IdMonedaInstrumento : { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
			}
		};

		invAPV.ConfiguracionCmbo.MecanismoCaptacion = {
			filtros: {
				IdPortafolio : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" }
			}
		};
		
		invAPV.ConfiguracionCmbo.Boveda = {
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
			}
		};

		invAPV.ConfiguracionCmbo.CondicionLiquidacion = { 
			filtros : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "COMACN" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
			}
		};
		
		invAPV.configuracionTipoPrecio = { 
				filtros : {
					IdTipoPrecioOrden : { "field"	: "IdTipoPrecioOrden", "operator": "eq", "value": "L" }
				}
		};		
		
		invAPV.configuracionCondicionLiquidacion = { 
				filtros : {
					Codigo : { "field"	: "Codigo", "operator": "eq", "value": "CN" }
				}
		};
		
		
		//******************************************************************************************************************************/
		//***************************************** Consulta Cuenta Inversión (Saldo Caja) *********************************************/
		//******************************************************************************************************************************/
		invAPV.consultas.buscarMovimientos = function() {					
			var _filter = Array();			
			dsSaldoCuentaInversion.removeAll();
			invAPV.datosForm.kngSaldoTotal = 0;
			invAPV.datosForm.kngSaldoTotalInvertido = 0;
			bcsComunes.ventanaOverlay(idVentana, true);			
			if(invAPV.datosForm.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion !== " TODOS")
			{
				_filter.push({ field: "IdPlanTipoInversion", operator: "eq", value: Number(invAPV.datosForm.NombrePlanAhorroSeleccionado.IdPlanTipoInversion) });
			}
			if(invAPV.datosForm.RegimenAgrupadorSeleccionado.Codigo !== " TODOS")
			{
				_filter.push({ field: "Regimen", operator: "eq", value: invAPV.datosForm.RegimenAgrupadorSeleccionado.Codigo });
			}
			if(invAPV.datosForm.ProductoAgrupadorSeleccionado.NombreProductoAgrupador !== " TODOS")
			{
				_filter.push({ field: "IdProductoAgrupador", operator: "eq", value: Number(invAPV.datosForm.ProductoAgrupadorSeleccionado.IdProductoAgrupador) });
			}
			
			TipoAgrupadorCuentaInversion = 'PL';
			if(invAPV.datosForm.NombrePlanAhorroSeleccionado !== undefined && invAPV.datosForm.NombrePlanAhorroSeleccionado !== false && invAPV.datosForm.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion !== " TODOS"){
				if(invAPV.datosForm.ProductoAgrupadorSeleccionado !== undefined && invAPV.datosForm.ProductoAgrupadorSeleccionado !== false && invAPV.datosForm.ProductoAgrupadorSeleccionado.NombreProductoAgrupador !== " TODOS"){
					if(invAPV.datosForm.RegimenAgrupadorSeleccionado !== undefined && invAPV.datosForm.RegimenAgrupadorSeleccionado !== false && invAPV.datosForm.RegimenAgrupadorSeleccionado.Codigo !== " TODOS"){
						TipoAgrupadorCuentaInversion = 'RE';
					}else{
						TipoAgrupadorCuentaInversion = 'PR';
					}
				}else{
					TipoAgrupadorCuentaInversion = 'PL';
				}
			}else
			{
				TipoAgrupadorCuentaInversion = 'PO';
			}
			
			//SALDOS
			if(invAPV.datosForm.SocioNegocioSeleccionado.IdCliente){
				var filtersSaldos = { 
					p_IdEmpresa 				: $scope.SesionIdEmpresa,				
					p_IdUsuario         		: $scope.SesionIdUsuario,
					p_IdModulo					: 'APV',
					p_UsaReglasNegocio			: 'N',
					p_IncluyeOtrosSaldos		: 'S',
					p_IdPais					: $scope.SesionIdPaisSistema,
					p_TipoAgrupador				: 'PO',
					p_RegimenTributario			: invAPV.datosForm.RegimenAgrupadorSeleccionado.Codigo !== " TODOS" ? invAPV.datosForm.RegimenAgrupadorSeleccionado.Codigo : null, 
					p_IdProductoAgrupador		: invAPV.datosForm.ProductoAgrupadorSeleccionado.NombreProductoAgrupador !== " TODOS" ? invAPV.datosForm.ProductoAgrupadorSeleccionado.IdProductoAgrupador : null,
					p_IdPlanTipoInversion		: invAPV.datosForm.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion !== " TODOS" ? invAPV.datosForm.NombrePlanAhorroSeleccionado.IdPlanTipoInversion : null,
					p_CuentaInversionAConsultar	: [{IdEjecutivo			: null,
													IdCliente			: invAPV.datosForm.SocioNegocioSeleccionado.IdCliente,
													IdPortafolio  	  	: null,
													IdMoneda      	  	: 'CLP'
												  }],
					p_FechaDesde				: $scope.SesionFechaSistema.slice(0,10) || '0001-01-01',
					p_FechaHasta				: $scope.SesionFechaSistema.slice(0,10) || '0001-01-01'
				};
				
				bcsServiciosAPV.ConsultarSaldoCtaInvCliente(filtersSaldos).$promise.then(function(respuesta){							
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
							var Saldos = Array();							
							var n = respuesta.P_OUT_SALDOPORFECHAMOVIMIENTO.length;
							var RespuestaSaldo = respuesta.P_OUT_SALDOPORFECHAMOVIMIENTO;
							var totalDisponible = 0;
							
							//for(var i=0; i<n ; i++){
							/*
								if(TipoAgrupadorCuentaInversion === "RE"){
									if(invAPV.datosForm.RegimenAgrupadorSeleccionado !== undefined && invAPV.datosForm.RegimenAgrupadorSeleccionado !== false){
										if(invAPV.datosForm.RegimenAgrupadorSeleccionado.Codigo === RespuestaSaldo[i].Regimen){
											Saldos.push(RespuestaSaldo[i]);
											totalDisponible = totalDisponible + RespuestaSaldo[i].SaldoDisponible;
										}
									}else{
										Saldos.push(RespuestaSaldo[i]);
									}
								}else if(TipoAgrupadorCuentaInversion === 'PL'){
									if(invAPV.datosForm.NombrePlanAhorroSeleccionado!== undefined && invAPV.datosForm.NombrePlanAhorroSeleccionado !== false){
										if(invAPV.datosForm.NombrePlanAhorroSeleccionado.IdPlanTipoInversion === RespuestaSaldo[i].IdPlanTipoInversion){
											Saldos.push(RespuestaSaldo[i]);
											totalDisponible = totalDisponible + RespuestaSaldo[i].SaldoDisponible;
										}
											
									}else{
										Saldos.push(RespuestaSaldo[i]);
									}
								}else if(TipoAgrupadorCuentaInversion === 'PR'){
									if(invAPV.datosForm.ProductoAgrupadorSeleccionado!== undefined && invAPV.datosForm.ProductoAgrupadorSeleccionado !== false){
										if(invAPV.datosForm.ProductoAgrupadorSeleccionado.IdProductoAgrupador === RespuestaSaldo[i].IdProductoAgrupador){
											Saldos.push(RespuestaSaldo[i]);
											totalDisponible = totalDisponible + RespuestaSaldo[i].SaldoDisponible;
										}
									}else{
										Saldos.push(RespuestaSaldo[i]);
									}
								}else{
									Saldos.push(RespuestaSaldo[i]);
									totalDisponible = totalDisponible + RespuestaSaldo[i].SaldoDisponible;
								}	
							*/
								//Saldos.push(RespuestaSaldo[i]);
								//totalDisponible = totalDisponible + RespuestaSaldo[i].SaldoDisponible;
							//}
							$.each(RespuestaSaldo,function(key,value){
								dsSaldoCuentaInversion.add({
									NombrePortafolio 	: value.NombrePortafolio,
									Producto			: value.Producto,
									IdPortafolio		: value.IdPortafolio,
									Regimen				: value.Regimen,
									IdMoneda			: value.IdMoneda,
									SaldoDisponible		: value.SaldoDisponible,
								    MontoInvertir		: 0,
								    IdEstado			: value.IdEstado
								});
								totalDisponible = totalDisponible + value.SaldoDisponible;
							});
							gridSaldoCuentaInversion.refresh();
							invAPV.datosForm.kngSaldoTotal = totalDisponible;
							invAPV.datosForm.kngSaldoTotalInvertido = Number(totalDisponible);
						}
						bcsComunes.ventanaOverlay(idVentana, false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);				
					}
				);
			}		
		};
		
		//******************************************************************************************************************************/
		//***************************************************** Ingreso Orden FFMM *****************************************************/
		//******************************************************************************************************************************/		
		invAPV.aporteNormal.ingresarAporteRescate = function(SolicitudAporteRescate) {
			//SolicitudAporteRescate
			bcsComunes.ventanaOverlay(idVentana,true);
			var datosIngresoAporteRescate = {
					 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					 p_IdModulo : 'APV',
					 p_SimularOperacion : 'N',
					 p_UsaReglasNegocio : 'S',
					 p_SolicitudesAporteRescate : SolicitudAporteRescate
			};
			bcsServiciosAPV.CrearSolicitudAporteRescateNormal(datosIngresoAporteRescate).$promise.then(function(respuesta){
		     	console.log(respuesta);
		     	if(respuesta.P_OUT_ERRORESCONTADOR == 0){
					if(respuesta.P_OUT_RESULTADO.length > 0){
						$.each(respuesta.P_OUT_RESULTADO,function(key,value){
							 var objCarga = {
								IdInterno 	: 2,
								tipo 		: 'INFORMACION',
								codigo 		: $translate.instant("UI_ORDEN_INGRESADA"),
								descripcion : "Operacion FFMM ingresada: " + value.IdSolicitudAporteRescate
							 };
							 mensajeResultadoOrdenes.push(objCarga);
						});
						invAPV.fn.Limpiar();
					}
					/*if(respuesta.P_OUT_ADVERTENCIASDETALLE.length > 0){
						$.each(respuesta.P_OUT_ADVERTENCIASDETALLE,function(key,value){
							 var objCarga = {
								IdInterno 	: value.IdInterno,
								tipo 		: 'ADVERTENCIA',
								codigo 		: $translate.instant(value.codigo),
								descripcion : $translate.instant(value.descripcion)
							 };
							 msgOrdenFM.push(objCarga);
						});
					}*/
		     	}
				if(respuesta.P_OUT_ERRORESCONTADOR > 0){
					$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
						 var objCarga = {
							IdInterno 	: 2,
							tipo 		: 'ERROR',
							codigo 		: value.codigo,
							descripcion : value.descripcion
						 };
						 mensajeResultadoOrdenes.push(objCarga);
					});
				}	

//				bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false,idVentana);
//	     		bcsComunes.ventanaOverlay(idVentana,false);
	     		
				if(mensajeResultadoOrdenes.length > 0){
					var IdVentanamodal = bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false, $scope.winID);		
					var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
					promesa.then(function(data){
			   			bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar($scope.IdVentana);
					});	
				}
	     		
	   		 },function(respuesta){
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   			bcsComunes.ventanaOverlay(idVentana,false);
	   		});
	     };
	     
	     //******************************************************************************************************************************/
		 //******************************************************** Funciones ***********************************************************/
		 //******************************************************************************************************************************/
	     invAPV.fn.seleccionTab = function(tipoTab){
	    	 if(tipoTab === 'RV'){
	    		 invAPV.TipoOrdenHabilito = 'RV';
	    	 }else{
	    		 invAPV.TipoOrdenHabilito = 'FM';
	    	 }
	     };	   
	     
	     invAPV.fn.SaldoCajaCustodia = function(){
	    	 //if(invAPV.TipoOrdenHabilito === 'RV'){
	    		 invAPV.datosForm.IdPortafolio = invAPV.datasource.SaldoCuentaInversionSeleccion[0].IdPortafolio;
	    		 invAPV.datosForm.IdMonedaLiquidacion = invAPV.datasource.SaldoCuentaInversionSeleccion[0].IdMoneda;
	    		 invAPV.ConfiguracionCmbo.filtrosCuentaInversion.filtros.IdPortafolio = invAPV.datosForm.IdPortafolio;
	    	 //}
	    	 //if(invAPV.TipoOrdenHabilito === 'FM'){
	    	//	 invAPV.datosForm.IdPortafolio = invAPV.datasource.SaldoCuentaInversionSeleccion[0].IdPortafolio;
	    	//	 invAPV.datosForm.IdMonedaLiquidacion = invAPV.datasource.SaldoCuentaInversionSeleccion[0].IdMoneda;
	    	 //}
	     };
	     
	     
	     invAPV.fn.calculamontoCuota = function(numeroCuotaSalida,montocalculo){
	    	 var datos = {};
			 datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_Series : [{
					IdInterno				: 0,
					Fecha					: util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
					IdSerieFondo			: invAPV.datosForm.InstrumentoSeleccionado.IdSerieFondo,
					CalculoPorMontoCuota	: 'M',
					Monto					: numeroCuotaSalida === 'G' ? montocalculo : invAPV.datosForm.kngMontoInicial,
					NumeroCuotas			: 0,
					IdMonedaFondo			: invAPV.datosForm.InstrumentoSeleccionado.IdMoneda,
					IdMonedaInversion		: invAPV.datosForm.IdMonedaAporte,
					TipoSolicitud			: 'APO',
					TipoFactorAjuste		: 'UF',
					IdEmpresa				: $scope.SesionIdEmpresa
				}]
			 };
			 
			 bcsServiciosAPV.CalcularMontoNumeroCuota(datos).$promise.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 if(numeroCuotaSalida === 'G'){
						 return respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 }else{
						 invAPV.datosForm.kngNumeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 invAPV.datosForm.kngMontoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }
				 }else{
					 invAPV.datosForm.kngNumeroCuotas = 0;
					 invAPV.datosForm.kngMontoInicial = 0;
				 }
	   		 },function(respuesta){
	   			invAPV.datosForm.kngNumeroCuotas = 0;
	   			invAPV.datosForm.kngMontoInicial = 0;
	   		});		 
	     };
	     			
		invAPV.fn.EliminaOrden = function(Orden){

			if(Orden.Mercado === 'RV'){
				var t = bcsAlerta.crear("PREGUNTA_SI_NO","¿Desea Eliminar la Orden de Renta Variable Seleccionada.?" ,idVentana);
				t .then(function(result)
			    {
					if (result === OPCIONES_BCSALERTA.NO){
						return;
					}else{
						var Indice = 0;
						for(var i=0; OrdenesRentaVariable.length >= i ; i++){
							if(OrdenesRentaVariable[i][0].IdPortafolio === Orden.IdPortafolio){
								Indice = i;
								break;
							}
						}
						
						//Elimino registro del Arreglo
						OrdenesRentaVariable.splice(Indice,1);
						//Actualizo Saldo Total Invertido (Saldo Disponible)
						invAPV.datosForm.kngSaldoTotalInvertido = invAPV.datosForm.kngSaldoTotalInvertido + Number(Orden.Precio * Orden.Cantidad);
						for(var x=0; dsSaldoCuentaInversion.data().length >= x ; x++){
							if(dsSaldoCuentaInversion.data()[x].IdPortafolio === Orden.IdPortafolio){
								dsSaldoCuentaInversion.data()[x].MontoInvertir = Number(dsSaldoCuentaInversion.data()[x].MontoInvertir) - Number(Orden.Precio * Orden.Cantidad);
								break;
							}
						};
						dsOrdenes.remove(Orden);
						invAPV.fn.HabilitoControlBusqueda();
						gridSaldoCuentaInversion.refresh();
					}
			    });
				
			}
	    	 
			if(Orden.Mercado === 'FM'){
				var t = bcsAlerta.crear("PREGUNTA_SI_NO","¿Desea Eliminar la Orden de Fondos Mutuos Seleccionada.?" ,idVentana);
				t .then(function(result)
			    {
					if (result === OPCIONES_BCSALERTA.NO){
						return;
					}else{
						/*
						var x = bcsAlerta.crear("PREGUNTA_SI_NO","¿Desea Eliminar las ordenes de Fondos Mutuos que se crearon a través de esta misma orden.?" ,idVentana);
						x .then(function(result)
					    {
					    
							if (result === OPCIONES_BCSALERTA.NO){
							*/
								//Otra Forma que tambien resulta para buscar un registro
								//$.grep(OrdenesFondosMutuos,function(e){ return e.IdPortafolio === Orden.IdPortafolio})
								//var OrdenEliminar = OrdenesFondosMutuos.filter(x => x.IdPortafolio === Orden.IdPortafolio);
								var Indice = 0;
								for(var i=0; OrdenesFondosMutuos.length >= i ; i++){
									if(OrdenesFondosMutuos[i].IdPortafolio === Orden.IdPortafolio){
										Indice = i;
										break;
									}
								}
								//Elimino registro del Arreglo
								OrdenesFondosMutuos.splice(Indice,1);
								//Actualizo Saldo Total Invertido (Saldo Disponible)
								invAPV.datosForm.kngSaldoTotalInvertido = invAPV.datosForm.kngSaldoTotalInvertido + Number(Orden.Monto);
								for(var x=0; dsSaldoCuentaInversion.data().length >= x ; x++){
									if(dsSaldoCuentaInversion.data()[x].IdPortafolio === Orden.IdPortafolio){
										dsSaldoCuentaInversion.data()[x].MontoInvertir = Number(dsSaldoCuentaInversion.data()[x].MontoInvertir) - Number(Orden.Monto);
										break;
									}
								};
								dsOrdenes.remove(Orden);
								invAPV.fn.HabilitoControlBusqueda();
								gridSaldoCuentaInversion.refresh();
							/*
							}else{
								//Elimino el Prorrateo de Ordenes -- Agregar
								
							}
							
						});	*/
					}
				});
			}			
	     };
	     
  	     invAPV.fn.ingresarOrdenRV = function(a) {

			 if(invAPV.datasource.SaldoCuentaInversionSeleccion === undefined){
				 bcsComunes.msg("warning","warning","Debe Seleccionar una Cuenta inversion con saldo.");
				 return;
			 }
    		 if(invAPV.datosForm.instrumento === undefined){
    			 bcsComunes.msg("warning","warning","Debe seleccionar un instrumento.");
    			 return;
    		 }
    		 if(invAPV.datosForm.precio === 0|| invAPV.datosForm.precio === undefined){
				 bcsComunes.msg("warning","warning","Debe ingresar precio.");
				 return;
			 }
    		 if( (invAPV.datosForm.cantidadInicial > 0 || Number(invAPV.datosForm.montoInicial) > 0) === false ){
				 bcsComunes.msg("warning","warning","Debe ingresar cantidad o monto.");
				 return;
			 }
			 if(invAPV.datosForm.bolsaSeleccionada.IdBolsa === undefined || invAPV.datosForm.bolsaSeleccionada.IdBolsa === ''){
				 bcsComunes.msg("warning","warning","Debe seleccionar bolsa.");
				 return;
			 }
			 if(invAPV.datosForm.condicionLiquidacion.IdCondicionDeLiquidacion === undefined || invAPV.datosForm.condicionLiquidacion.IdCondicionDeLiquidacion === ''){
				 bcsComunes.msg("warning","warning","Debe seleccionar Condicion de liquidacion.");
				 return;
			 }

			 if ((invAPV.datosForm.montoInicial !== undefined && invAPV.datosForm.montoInicial > 0?"S":"N")==="N")
				 varCantidad =  invAPV.datosForm.cantidadInicial
			 else
				 varCantidad =  invAPV.datosForm.montoInicial/Number(invAPV.datosForm.precio) 
				 
			 if(invAPV.datasource.SaldoCuentaInversionSeleccion[0].MontoInvertir === undefined)
				 invAPV.datasource.SaldoCuentaInversionSeleccion[0].MontoInvertir = 0;
   			 
			 var MontoDisponible = Number(invAPV.datasource.SaldoCuentaInversionSeleccion[0].SaldoDisponible) - Number(invAPV.datasource.SaldoCuentaInversionSeleccion[0].MontoInvertir);
			 //var MontoDisponible = Number(invAPV.datosForm.SaldoCuentaInversion) - Number(invAPV.datasource.SaldoCuentaInversionSeleccion[0].MontoInvertir);
			 
			 if(MontoDisponible > 0 && ((Number(invAPV.datosForm.precio) * varCantidad) <= MontoDisponible)){

				 var MontoCalculado = (Number(invAPV.datosForm.precio) * varCantidad); 
				 invAPV.datosForm.kngSaldoTotalInvertido = Number(invAPV.datosForm.kngSaldoTotalInvertido - MontoCalculado);
					 
				 invAPV.OrdenesRV = [{
					Cantidad : varCantidad,
					IdBoveda : invAPV.datosForm.bovedaSeleccionada.IdBoveda === 0 ? null:invAPV.datosForm.bovedaSeleccionada.IdBoveda,
					PrecioLimite : Number(invAPV.datosForm.precio),
					PrecioMercado : Number(invAPV.datosForm.instrumento.PrecioMercado),
					IdTipoPrecio : invAPV.datosForm.tipoPrecioSeleccionado.IdTipoPrecioOrden,
					IdPortafolio: invAPV.datasource.SaldoCuentaInversionSeleccion[0].IdPortafolio,
					IdEmisor: invAPV.datosForm.instrumento.IdEmisor,
					IdBolsa : invAPV.datosForm.bolsaSeleccionada.IdBolsa,
					EspecificaBolsa :invAPV.datosForm.bolsaSeleccionada.IdBolsa === 0? 'N':'S',
					IdMonedaLiquidacionOrden :invAPV.datosForm.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					IdMonedaLiquidacionCobros :invAPV.datosForm.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					TipoCambio : null, // invAPV.datosForm.tipoCambio || null,
					IdInstrumento : invAPV.datosForm.instrumento.IdInstrumento,
					EsPorMonto : invAPV.datosForm.montoInicial !== undefined && invAPV.datosForm.montoInicial > 0 ? "S" : "N",
					IdCondicionDeLiquidacion : invAPV.datosForm.condicionLiquidacion.IdCondicionDeLiquidacion
				 }];
			
				 OrdenesRentaVariable.push(invAPV.OrdenesRV);
				
				 dsOrdenes.add({
					Mercado			: 'RV',
					IdPortafolio	: invAPV.datasource.SaldoCuentaInversionSeleccion[0].IdPortafolio,
					IdInstrumento	: invAPV.datosForm.instrumento.IdInstrumento,
					Instrumento		: invAPV.datosForm.instrumento.Nemotecnico,
					SerieFondo		: '',
					Regimen			: invAPV.datasource.SaldoCuentaInversionSeleccion[0].Regimen,		
					TipoPrecio		: invAPV.datosForm.tipoPrecioSeleccionado.IdTipoPrecioOrden,
					Liquidacion		: util.date(invAPV.datosForm.fechaLiquidacion).toDate(),
					IdMoneda	    : invAPV.datosForm.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					Moneda	    	: invAPV.datosForm.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					Precio			: Number(invAPV.datosForm.precio),
					Cantidad		: varCantidad,	
					NumeroCuota		: 0,
					Monto			: MontoCalculado//invAPV.datosForm.montoInicial
				 });	
	
				 invAPV.datasource.SaldoCuentaInversionSeleccion[0].MontoInvertir = Number(invAPV.datasource.SaldoCuentaInversionSeleccion[0].MontoInvertir) + Number(MontoCalculado);
				 gridSaldoCuentaInversion.refresh();
				 invAPV.fn.limpiarDatosRV();
				 invAPV.fn.HabilitoControlBusqueda();
			 }else{
				 invAPV.datosForm.kngSaldoTotalInvertido = invAPV.datosForm.kngSaldoTotalInvertido + Number(MontoCalculado);
				 bcsComunes.msg("warning","warning","El monto ingresado es mayor al monto disponible.");
				 return;
			 } 
				
		};
		
	 	invAPV.fn.enviarOrdenesRV = function(Ordenes) {
			
			var datosIngresoOrden = {
					p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo : 'ORD',
					p_simularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_Ordenes : Ordenes,
					p_CobrosModificados : cobrosModificadosFinal
					
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
			promise.then(function(respuesta){
				/*
		     	$.each(respuesta.P_OUT_MENSAJES,function(key,value){
					 var objCarga = {
							IdInterno : value.id,
							tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
							codigo : value.codigo,
							descripcion : value.descripcion
					 };
					 msgOrdenRV.push(objCarga);
				});
				respuesta.P_OUT_RESULTADO = msgOrdenRV;
				if(respuesta.P_OUT_RESULTADO.length > 0){
					bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : msgOrdenRV } ,false,false,idVentana);							
				}*/
	   			bcsComunes.ventanaOverlay(idVentana,false);
		   		
				//se valido la orden
				if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
				{
					invAPV.datosForm.modo = 'RESUMEN';/*
					invAPV.datosResumen = {
							fechaOperacionOrden :	util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHoraSistema","CALL"),
							secuenciaOrden		:	respuesta.P_OUT_RESULTADO[0].IdOrden,
							tipoInstrumento 	: 	respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
							operacion 			:	respuesta.P_OUT_RESULTADO[0].AccionOrden,
							tipoInstrumento_operacion  : respuesta.P_OUT_RESULTADO[0].AccionOrden + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
							cantidad 			:	respuesta.P_OUT_RESULTADO[0].Cantidad,
							instrumento 		: 	respuesta.P_OUT_RESULTADO[0].Instrumento,
							precio 				: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden),
							esVentaCorta		: 	respuesta.P_OUT_RESULTADO[0].EsVentaCorta === 'S'?true:false,
							tipoPrecio 			: 	respuesta.P_OUT_RESULTADO[0].NombreTipoPrecio,
							precio_tipoPrecio	: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden) + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoPrecio,
							montoOperacion 		: 	respuesta.P_OUT_RESULTADO[0].MontoLiquidacionOperacion,
							totalCobros 		: 	Number(respuesta.P_OUT_RESULTADO[0].TotalCobros),
							montoTotal 			:	Number(respuesta.P_OUT_RESULTADO[0].MontoTotal),
							idMoneda 			:	respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,
							idMonedaOrden		:	respuesta.P_OUT_RESULTADO[0].IdMonedaOrden,
							documentoIdentidad	:	respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
							nombreEjecutivoPortafolio : respuesta.P_OUT_RESULTADO[0].NombreEjecutivoPortafolio
					};
					*/
					$.each(respuesta.P_OUT_RESULTADO,function(key,value){
						 var objCarga = {
								IdInterno 	: 1,
								tipo 		: 'INFORMACION',
								codigo 		: $translate.instant("UI_ORDEN_INGRESADA"),
								descripcion : "Operacion RV ingresada : " + value.IdOrden
						 };
						 mensajeResultadoOrdenes.push(objCarga);
					});
					
					
//					if(respuesta.P_OUT_RESULTADO.length > 0){
//						bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false,idVentana);							
//						invAPV.fn.Limpiar();
//					}
					
					if(OrdenesFM.length > 0)
						invAPV.aporteNormal.ingresarAporteRescate(OrdenesFM);
					else{
						//CAMBIO VENTANA RESULTADO
						if(mensajeResultadoOrdenes.length > 0){
							var IdVentanamodal = bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false, $scope.winID);		
							var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
							promesa.then(function(data){
								bcsComunes.ventanaOverlay(idVentana,false);
								bcsComunes.ventanaCerrar($scope.IdVentana);
								invAPV.fn.Limpiar();
							});	
						}
					}
					
					/*
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + respuesta.P_OUT_RESULTADO[0].IdOrden,
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
					*/
				}
				else
			    {
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_NO_SE_PUDO_INGRESAR_ORDEN"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
			    }
				
	   		},function(respuesta){
	   			bcsComunes.ventanaOverlay(idVentana,false);			
				bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_ORDEN"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
				
	   		});
	   	 };
	   	
	     
	     invAPV.fn.IngresaOrdenesFM = function(SaldoCuentaInversionSeleccionado){
	    		 
    		 if(invAPV.datosForm.InstrumentoSeleccionado === undefined || invAPV.datosForm.InstrumentoSeleccionado === false){
    			 bcsComunes.msg("warning","warning","Debe seleccionar un instrumento.");
    			 return;
    		 }
    		 if(invAPV.datosForm.kngSaldoTotal === 0 || invAPV.datosForm.kngSaldoTotal === ''){
				 bcsComunes.msg("warning","warning","Debe tener saldo en caja para ingresar una orden.");
				 return;
			 }
			 if(invAPV.datosForm.kngMontoInicial === 0 || invAPV.datosForm.kngMontoInicial === ''){
				 bcsComunes.msg("warning","warning","Debe ingresar Monto Inicial.");
				 return;
			 }
			 if(invAPV.datosForm.kngSaldoTotalInvertido < invAPV.datosForm.kngMontoInicial){
				 bcsComunes.msg("warning","warning","El Monto a Ingresar debe ser Menor al Saldo Disponible.");
				 return;
			 }
			 if(invAPV.datosForm.IdMonedaAporte === undefined || invAPV.datosForm.IdMonedaAporte === ''){
				 bcsComunes.msg("warning","warning","Debe tener moneda de aporte.");
				 return;
			 }
			 if(SaldoCuentaInversionSeleccionado === undefined || SaldoCuentaInversionSeleccionado === false){
				 bcsComunes.msg("warning","warning","Debe Seleccionar una o mas Cuentas de Inversión.");
				 return;
			 }
			 if(SaldoCuentaInversionSeleccionado.length > 0){
				 var MontoSaldoTotal = 0;
				 $.each(SaldoCuentaInversionSeleccionado,function(key,value){
					 MontoSaldoTotal = MontoSaldoTotal + Number(value.SaldoDisponible);
				 });
				 
    			 $.each(SaldoCuentaInversionSeleccionado,function(key,value){
    				 if(value.MontoInvertir === undefined)
    					 value.MontoInvertir = 0;
    				 
    				 var MontoDisponible = (Number(value.SaldoDisponible) - Number(value.MontoInvertir));

    				 if(MontoDisponible > 0){ //&& (invAPV.datosForm.kngSaldoTotalInvertido <= MontoDisponible)){
    					 var PorcentajeProrrateo = Number(MontoDisponible / MontoSaldoTotal);
    					 
    					 //var MontoCalculado = Number((MontoDisponible / invAPV.datosForm.kngSaldoTotal) * invAPV.datosForm.kngMontoInicial);   
    					 var MontoCalculado = Number(PorcentajeProrrateo * invAPV.datosForm.kngMontoInicial);
    					 MontoCalculado = MontoCalculado.toFixed(invAPV.datosForm.InstrumentoSeleccionado.decimalesCantidad);

    					 //var NumeroCuota = Number((MontoDisponible / invAPV.datosForm.kngSaldoTotal) * invAPV.datosForm.kngNumeroCuotas); 
    					 var NumeroCuota = Number(PorcentajeProrrateo * invAPV.datosForm.kngNumeroCuotas);
    					 NumeroCuota = NumeroCuota.toFixed(4);
    					 
    					 invAPV.datosForm.kngSaldoTotalInvertido = invAPV.datosForm.kngSaldoTotalInvertido - Number(MontoCalculado);
    					 
    					 if(invAPV.datosForm.kngSaldoTotalInvertido >= 0 && value.IdEstado === 'ACT' ){
    						 //Grilla que muestra lo que se va a generar como orden (solo visual)
    						 dsOrdenes.add({
				    			 	IdInstrumento	: invAPV.datosForm.InstrumentoSeleccionado.IdInstrumento,
				    			 	Instrumento		: invAPV.datosForm.InstrumentoSeleccionado.Nemotecnico,
				    				SerieFondo		: invAPV.datosForm.InstrumentoSeleccionado.IdSerieFondo,
				    				IdPortafolio	: value.IdPortafolio,
				    				IdMoneda		: invAPV.datosForm.IdMonedaAporte,
				    				Regimen			: value.Regimen,
				    				Precio			: invAPV.datosForm.InstrumentoSeleccionado.ValorFormato,
				    				Liquidacion		: $scope.SesionFechaSistema.slice(0,10),
				    				NumeroCuota		: NumeroCuota,	
				    				Monto			: MontoCalculado,
				    				Mercado			: 'FM'
    						 });	
    						 
    						 //Arreglo que se utiliza para generar Ordenes de Fondos Mutuos
    						 OrdenesFondosMutuos.push({
    							 	IdInstrumento	: invAPV.datosForm.InstrumentoSeleccionado.IdInstrumento,
				    			 	Instrumento		: invAPV.datosForm.InstrumentoSeleccionado.Nemotecnico,
				    				SerieFondo		: invAPV.datosForm.InstrumentoSeleccionado.IdSerieFondo,
				    				IdPortafolio	: value.IdPortafolio,
				    				IdMoneda		: invAPV.datosForm.IdMonedaAporte,
				    				Regimen			: '',
				    				NumeroCuota		: NumeroCuota,	
				    				Monto			: MontoCalculado
    						 });
    						 
    						 value.MontoInvertir = Number(value.MontoInvertir) + Number(MontoCalculado);
    						 
    					 }else{
    						 invAPV.datosForm.kngSaldoTotalInvertido = invAPV.datosForm.kngSaldoTotalInvertido + Number(MontoCalculado);
    					 }
    				 }else{
        				 bcsComunes.msg("warning","warning","El monto ingresado es mayor al monto disponible.");
        				 return;
    				 }
    				 
	    		 });
    			 gridSaldoCuentaInversion.refresh();
    			 invAPV.fn.limpiarDatosFM();
    			 invAPV.fn.HabilitoControlBusqueda();
    		 }		 
	    	 
	     };

	     invAPV.fn.Limpiar = function(TipoOrden){
    		 invAPV.fn.limpiarDatosFM();
    		 invAPV.fn.limpiarDatosRV();
    		 dsOrdenes.removeAll();
    		 dsSaldoCuentaInversion.removeAll();
    		 invAPV.fn.HabilitoControlBusqueda();
    		 invAPV.datosForm.kngSaldoTotal = 0;
    		 invAPV.datosForm.kngSaldoTotalInvertido = 0;
	     };
	     
	     invAPV.fn.limpiarDatosRV = function(){
    		 invAPV.datosForm.precio = 0;
    		 invAPV.datosForm.idTipoPrecio = 'M';
    		 invAPV.datosForm.setInstrumento  = '-1';
    		 invAPV.datosForm.monedaLiquidacionOrdenSeleccionada  = undefined;
    		 invAPV.datosForm.cantidadInicial     = 0;
    		 invAPV.datosForm.montoInicial = 0;
    		 invAPV.ConfiguracionCmbo.MecanismoCaptacion.idActual = undefined;
    		 
    		 invAPV.datosForm.TipoVigenciaIdActual = undefined;
    		 invAPV.datosForm.TipoVigenciaIdActualDefecto = undefined;
    		 invAPV.datosForm.BovedaIdActualDefecto = undefined;
    		 invAPV.datosForm.BovedaIdActual = undefined;
    		 invAPV.datosForm.bolsaSeleccionadaIdActual = undefined;
    		 invAPV.datosForm.bolsaSeleccionadaIdActualDefecto = undefined;
    		 
    		 invAPV.fn.DatosPordefecto();
	     };

	     invAPV.fn.limpiarDatosFM = function(){
    		 invAPV.datosForm.Instrumentonemotecnico = undefined;
			 invAPV.datosForm.InstrumentoidDefecto = undefined;
			 invAPV.datosForm.idAutorizadoDarOrdenes = undefined;
			 invAPV.datosForm.idAutorizadoDarOrdenesDefecto = undefined;
			 invAPV.datosForm.IdMonedaAporte = undefined;
			 invAPV.datosForm.IdMoneda = undefined;
			 invAPV.datosForm.IdMonedaAporte = '';
			 invAPV.datosForm.kngMontoInicial = 0;
    		 invAPV.datosForm.kngNumeroCuotas = 0;
	     };
	     
	     invAPV.fn.DatosPordefecto = function(){
	    	invAPV.datosForm.IdTipoOperacion = 'COMACN';	
			invAPV.datosForm.IdMedioPagoFMActual = 'CIN';
			invAPV.datosForm.IdMedioPagoFMActualDefecto = 'CIN';		
			invAPV.datosForm.IdMedioPagoRVActual = 'CIN';
			invAPV.datosForm.IdMedioPagoRVActualDefecto = 'CIN';	
			invAPV.datosForm.bolsaSeleccionadaIdActual = 'BCS';
			invAPV.datosForm.bolsaSeleccionadaIdActualDefecto = 'BCS';
			invAPV.datosForm.TipoVigenciaIdActual = 'SH';
			invAPV.datosForm.TipoVigenciaIdActualDefecto = 'SH';
			invAPV.datosForm.IdCondicionDeLiquidacion = 'T2';
			invAPV.datosForm.BovedaIdActual = 'DCV';
			invAPV.datosForm.BovedaIdActualDefecto = 'DCV';
	     };
	     
	     invAPV.fn.HabilitoControlBusqueda = function(){
	    	 if(dsOrdenes.data() === undefined && dsOrdenes.data() === false){
	    		 invAPV.HabilitoControlesBusqueda = true;
	    	 }else{
	    		 invAPV.HabilitoControlesBusqueda = false;
	    		 if(dsOrdenes.data().length === 0)
	    			 invAPV.HabilitoControlesBusqueda = true;
	    	 };
	     };	     
	     
	     //******************************************************************************************************************************/
		 //********************************************** Envia Solicitud de Ordenes ****************************************************/
		 //******************************************************************************************************************************/
	     invAPV.fn.enviarInversion = function(){
	    	 
	    	 if(OrdenesRentaVariable.length === 0 && OrdenesFondosMutuos.length === 0){
	    		 bcsComunes.msg("warning","warning","Falta agregar ordenes.");
				 return; 
	    	 }
	    	 
	    	 OrdenesRV = Array();
	    	 OrdenesFM = Array();
	    	 mensajeResultadoOrdenes = Array();
	    	 var IdInterno = 1;

	    	 // Ciclo de ordenes RV
	    	 $.each(OrdenesRentaVariable,function(key,value){
	    		 var Orden = {
							IdInterno :IdInterno,
						    FechaRecepcion : '0001-01-01 00:00:00',
							FechaVigencia  : '0001-01-01',
							FechaEmision : '0001-01-01',
							FechaOperacion : '0001-01-01', //se calcula en el server
							IdCondicionDeLiquidacion : value[0].IdCondicionDeLiquidacion,
							IdTipoOperacion : 'COMACN',
							IdTipoVigencia : 'SH',
							Glosa : '',
							Cantidad : value[0].Cantidad,
							MontoNeto : 0,
							ValorRescate : 0,
							IdBoveda 		: value[0].IdBoveda,
							IdFormaCustodia : 'CUS',
							IdPorcentajeVisibilidad :1,
							PrecioLimite : Number(value[0].PrecioLimite),
							PrecioMercado : Number(value[0].PrecioMercado),
							EsFacturaInmediata : 'N',
							IdTipoPrecio : value[0].IdTipoPrecio,
							IdPortafolio : value[0].IdPortafolio,
							IdTipoCanalIngreso : 'AGE', //$scope.SesionTipoCanalIngreso,
							IdTipoReajuste : '',
							IdEmisor: value[0].IdEmisor,
							IdBolsa : value[0].IdBolsa,
							EspecificaBolsa :value[0].EspecificaBolsa,
							IdMonedaLiquidacionOrden :value[0].IdMonedaLiquidacionOrden,
							IdMonedaLiquidacionCobros :value[0].IdMonedaLiquidacionCobros,
							TipoCambio : null, // invAPV.datosForm.tipoCambio || null,
							IdInstrumento : value[0].IdInstrumento,
							IdInstrumentoSubyacente : 0,
							IdCondicionDeLiquidacionPlazo : '',
							TasaVentaCorta : 0,
							PlazoVentaCorta : 0,
							EsVentaCorta : 'N',
							TasaSimultanea : 0,
							PlazoSimultanea : 0,
							PlazoIF : 0,
							TasaRFIF : 0,
							EsOD: 'N',
							EsFungible : 'N',
							MontoPlazo : 0,
							IdTipoLibroOrden : 'NORMAL',
							IdLibroOrdenes : null,
							IdCuentaCustodioPortafolio : null,
							EsPorMonto : value[0].EsPorMonto,
							IdCorredorCorresponsal : null, //invAPV.datosForm.idCorredorCorresponsal || null,
							IdPersonaAutorizadoDarOrdenes : null, //invAPV.datosForm.autorizadoDarOrdenes.IdPersona || null,
							IdTipoCanalDistribucion : 'MAN', //	invAPV.datosForm.idTipoCanalDistribucion || null,
							EsOrdenEspecial : 'N', //invAPV.datosForm.esOrdenEspecial === true?"S":"N",
							PagoCobroCuentaInversion :'N', //  invAPV.datosForm.pagoCobroCuentaInversion === true?"S":"N",
							IdPreferenciaPagoPortafolio : null, //invAPV.datosForm.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
							BaseTasa : 0,
							TipoInteres : "",
							IdMecanismoCaptacion : invAPV.datosForm.mecanismoDeCaptacion?invAPV.datosForm.mecanismoDeCaptacion.Codigo:'',
							MontoInteresContado : 0,
							MontoInteresPlazo : 0
						};
		 			OrdenesRV.push(Orden);
	
	    			var datos = {	
	    					p_IdEmpresa: $scope.SesionIdEmpresa,
	    					p_IdUsuario: $scope.SesionIdUsuario,
	    					p_IdModulo: 'APV',
	    										
	    					p_Ordenes : [],
	    					p_CobrosModificados : []
	    				};
	
	
					var cobro = {
						CantidadOrden   				: value[0].Cantidad || 0,
						DiasPlazo 						: 0,
						EsFungible  					: 'N',
						IdBolsa							: value[0].IdBolsa,
						IdCorredorCorresponsal			: null,
						IdInterno  						: IdInterno,
						IdInstrumento  					: value[0].IdInstrumento || 0,
						IdMonedaLiquidacionOrden        : value[0].IdMonedaLiquidacionOrden,
						IdPortafolio 					: value[0].IdPortafolio,
						IdTipoProducto  				: 'INAC',
						IdTipoOperacion					: invAPV.datosForm.IdTipoOperacion,
						IdTipoCanalIngreso				: $scope.SesionTipoCanalIngreso,
						FechaOperacion					: invAPV.datosForm.fechaInicial,
						MontoOrden						: 0,	//para compra simultanea
						PrecioOrden 					: Number(value[0].PrecioLimite),
						TasaSimultanea  				: 0,
						TasaRFIF  						: 0,
						TasaVentaCorta  				: 0,
						TipoCambio 						: null,
						ValorRescateOrden				: 0,
						
						MontoTotal						: invAPV.datosForm.montoInicial || 0,
						EsPorMonto 						: value.MontoTotal > 0 && !value.CantidadOrden? 'S' : 'N',
						TasaReporte						: 0,
						TasaReportado					: 0,
						PlazoReporte					: 0,
						BaseTasa						: 0,
						TipoInteres						: '',
					};
	
					datos.p_Ordenes.push(cobro);
					
	    			var cobrosModificados = [];
	    			$scope.calculando = true;
					var p = bcsServicioOrden.CalcularMontoYCobrosOrden(datos);
	
	    			p.$promise.then(function(result) {
	
						$.each(result.p_out_Cobros,function(key,value) {
							var cobroModificado ={
									IdInterno 			: value.IdInterno,
									IdCobro				: value.IdCobro,
									ValorCobroAplicado	: 0
							}
	
							cobrosModificadosFinal.push(cobroModificado);							
						});
	    		    	
	    			});
			   		IdInterno = IdInterno + 1;
			   });

	    	 OrdenesRentaVariable = Array();
	    	 
	    	 $.each(OrdenesFondosMutuos,function(key,value){
				   var Orden = { 
						   	IdInterno		      		: IdInterno,
							IdCliente 	          		: invAPV.datosForm.SocioNegocioSeleccionado.IdCliente,
							IdPortafolio          		: value.IdPortafolio,
							TipoSolicitud         		: 'APO',
							IdSerieFondo          		: value.SerieFondo,
							CalculoPorMontoCuota  		: 'M', 
							EsPorMontoLiquido			: 'N',
							Monto 				  		: value.Monto,
							NumeroCuotas 	  	  		: value.NumeroCuota,
							IdMoneda 			  		: value.IdMoneda,
							IdMedioDePago				: invAPV.datosForm.MedioPagoFMSeleccionado.IdMedioDePago,
							IdPreferenciaPagoPortafolio : 0,
							Visado						: 'N',
							SolicitudBatch				: 'N',
							OrigenOperacion				: 'M',
							FechaOperacion				: $scope.SesionFechaSistema.slice(0,10),
							IdRegimenTributario			: '',
							Glosa						: '',
							IndicadorMLT				: 'N',
							IdSerieFondoDestino			: '',
							IdEmisorDestino				: '',
							IdAutorizadoOrigen			: 0,
							Porcentaje                  : 0,
					    	FolioContrato               : 0
				   }
				   IdInterno = IdInterno + 1;
				   OrdenesFM.push(Orden);
    		 });
	    	 
			mensajeResultadoOrdenes = new Array();

	    	 if(OrdenesRV.length > 0)
		    		invAPV.fn.enviarOrdenesRV(OrdenesRV);
	    	 else if(OrdenesFM.length > 0)
	    		 invAPV.aporteNormal.ingresarAporteRescate(OrdenesFM);
	    	 else
	    		 return;
	    	 
	    	 OrdenesFondosMutuos = Array();
	     };

	     //******************************************************************************************************************************/
		 //********************************************************** Watch *************************************************************/
		 //******************************************************************************************************************************/
	     $scope.$watch('invAPV.datosForm.kngMontoInicial',function(newval,oldval){
			 if (newval !== oldval){
				 if(invAPV.datosForm.kngMontoInicial > 0)
					 invAPV.fn.calculamontoCuota('I');
			 }
		 });
	     
	     $scope.$watch('invAPV.datosForm.InstrumentoSeleccionado',function(newval,oldval){
	    	 if (newval && newval.IdFondo) {
				newval.ValorFormato = util.truncarNumero(newval.ValorCuota, newval.DecimalesValorCuota);
				invAPV.datosForm.IdMonedaAporte = newval.IdMoneda;
				invAPV.datosForm.IdMoneda = newval.IdMoneda;
			 }
			 else {
					invAPV.datosForm.kngNumeroCuotas = 0;
					invAPV.datosForm.kngMontoInicial = 0;
			}
		 });

		 $scope.$watchGroup(['invAPV.datosForm.tipoPrecioSeleccionado.IdTipoPrecioOrden','invAPV.datosForm.instrumento.PrecioMercado'],function(newval,oldval) {
			if (invAPV.datosForm.instrumento && invAPV.datosForm.tipoPrecioSeleccionado) {
				if (invAPV.datosForm.tipoPrecioSeleccionado.IdTipoPrecioOrden === bcsConfiguracionAPV.CONSTANTES.IdTipoPrecioMercado && invAPV.datosForm.instrumento.PrecioMercado) {
					invAPV.datosForm.precio = Number(invAPV.datosForm.instrumento.PrecioMercado);
				}
			}
		 });
		
		 $scope.$watch('invAPV.datosForm.operacionSeleccionada',function(newval,oldval) {
			if (!newval)
				return;
			invAPV.ConfiguracionCmbo.CondicionLiquidacion.filtros.IdTipoOperacion.value = invAPV.datosForm.operacionSeleccionada.IdTipoOperacion;
			invAPV.ConfiguracionCmboPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = invAPV.datosForm.operacionSeleccionada.IdTipoOperacion;
		 });

	     $scope.$watch('invAPV.datosForm.SocioNegocioSeleccionado',function(newval,oldval){
	    	if(newval && !oldval){
	    		invAPV.consultas.buscarMovimientos();
	    	}
	     });

	 	 $scope.$watch('invAPV.datosForm.instrumento.IdTipoInstrumento',function(newval,oldval) {
    		if (!newval) {
    			invAPV.ConfiguracionCmbo.MonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = undefined;
	 			invAPV.datosForm.idBolsaSeleccionada = undefined;
	 			//invAPV.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = undefined;
    		}
    		else {
    			invAPV.ConfiguracionCmbo.MonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = newval;
    			//invAPV.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = newval;
    		}
	 	 });

		 $scope.$watch('invAPV.datosForm.cantidadInicial',function(newval,oldval){
			if (newval && !oldval) {
				invAPV.datosForm.montoInicial = undefined;
			}

		 });
		 
		 $scope.$watch('invAPV.datosForm.montoInicial',function(newval,oldval){
		
			if (newval && !oldval) {
				invAPV.datosForm.cantidadInicial = undefined;
			}

		 });
		
		 $scope.$watchGroup(['invAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden'], function(newval,oldval){
			if (invAPV.datosForm.tipoVigenciaSeleccionada && invAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden){
				if (invAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden ===  bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaSoloHoy){
					invAPV.datosForm.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				}
				else if (invAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaPermanente) {
					invAPV.datosForm.fechaVigencia = util.dateDS("0001-01-01","FechaSistema","CALL");
				}
				else if (invAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaFechaFija) {
					invAPV.datosForm.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				}
				else if (invAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaSemanal) {
					var datos = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo : 'ORD',
						p_UsaReglasNegocio : 'S',
						p_Fecha : $scope.SesionFechaSistema,
						p_NumeroDias : 5,
						p_EsHabil : 'S',
						p_IdPais : $scope.SesionIdPaisSistema
					};
					bcsServicioPlataforma.FechaSiguiente(datos,function(result) {
						invAPV.datosForm.fechaVigencia = util.dateDS(result.P_OUT_FECHASIGUIENTE,"FechaSistema","CALL");
						}
					,function(result){
						console.log(result);
						}
					);
				}
				else {
					invAPV.datosForm.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				}
			}
			
		 }); 

		 $scope.$watch('$viewContentLoaded', function(){
			 invAPV.fn.DatosPordefecto();
		 });

	});
}());