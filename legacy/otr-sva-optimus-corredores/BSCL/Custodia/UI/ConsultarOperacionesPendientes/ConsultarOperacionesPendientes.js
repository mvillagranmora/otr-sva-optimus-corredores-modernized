angular.module('ModuloCustodia').controller('ConsultarOperacionesPendientesCtrl', 
		function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosCustodia,OPCIONES_BCSALERTA,bcsAlerta)				
{	
	$scope.filtros = {};
	$scope.data ={};	
		
		
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.fechaActual		= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaConsulta	= $scope.fechaActual;
	
	$scope.oClientePortafolio={
	tipo : "combobox-cliente-portafolio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionEstadoMovimiento={
		tipo : "combobox-estado-custodia",
		configuracion : {},
		filtros : {
			NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : 'CUSTODIA.OPERACIONES' }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	
 var dsOperacionesPendientes = new bcs.DataSource({
	type : "odata",
	definition : {
		serverFiltering: true,
		serverPaging: true,
		serverSorting: true,
		sort: { field: "FolioOperacion", dir: "desc" },
		pageSize: 100,
		transport: {
			read : {
				url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=602",
				dataType : "json"
			}
		},	                               
		schema: {
			model: {
				fields : {                                                                 
					IdInterno			  			: { type: "number"},                                           
					FolioOperacion			  		: { type: "number"},
					IdRegistro			  			: { type: "number"},
					Producto						: { type: "string"},
					IdTipo							: { type: "string"},
					IdTipoProducto					: { type: "string"},
					Producto						: { type: "string"},
					NombreOperacion					: { type: "string"},
					IdTipoInstrumento				: { type: "string"},
					Nemo                      		: { type: "string"},
					IdClasificacionTipoInstrumento  : { type: "string"},
					IdCondicionLiquidacion			: { type: "string"},
					IdTipoOperacion                 : { type: "string"},
					TipoOperacion					: { type: "string"},
					Accion            				: { type: "string"},
					OperadorDirecto              	: { type: "string"},
					FechaIngresoRegistro            : { type: "sysdate"},
					FechaVencimiento                : { type: "sysdate"},
					DocumentoIdentidad				: { type: "string"},
					IdPortafolio               		: { type: "number"},
					CodigoPortafolio          		: { type: "string"},
					Nombre							: { type: "string"},	
					FolioComprobante            	: { type: "number"},
					FolioComprobanteReferencia      : { type: "number"},
					FolioComprobanteRelacionado     : { type: "number"},
					IdOrden                 		: { type: "number"},
					Cantidad                  		: { type: "number"},
					Precio                    		: { type: "number"},
					Tasa                      		: { type: "number"},
					Monto                     		: { type: "number"},
					MontoComprom             		: { type: "number"},
					IdEstado                    	: { type: "string"},
					DescEstado                    	: { type: "string"},
					IdAgente                    	: { type: "string"},
					IdMoneda                    	: { type: "string"},
					IdTipoGarantia           		: { type: "string"},
					DescripcionTipoGarantia         : { type: "string"},
					FondoAfp                 		: { type: "string"},
					IdTipoComprobante             	: { type: "string"},
					CodigoTipoProducto              : { type: "string"},
					IdBolsa                   		: { type: "string"},
					DocumentoIdentidadEntidad       : { type: "string"},
					IndDcv                			: { type: "string"},
					FolioTransaccion               	: { type: "number"},
					DescEstadoCclv          		: { type: "string"},
					UsoGarantia          			: { type: "string"},
					TopoOpePendiente              	: { type: "string"},
					BlockEnvio            			: { type: "string"},
					IdConcepto             			: { type: "string"},
					IdEmisor                    	: { type: "string"},
					EsFungible          			: { type: "string"},
					NemoSVS                  		: { type: "string"},
					IdTipoReajuste                 	: { type: "string"},
					Categoria                 		: { type: "string"},      
					CodCorr                 		: { type: "string"},
					IdGestion                 		: { type: "number"},
				}
			}
		}
	}});
 
 
 	$scope.fnColorBk = function(row) {
     var color = "";
		
		if ( row.IdEstado == "R"){
			color = "red";
			return color;
		}
		
		if ( row.IdEstado == "C" || row.IdEstado == "M"){
			color = "green";
			return color;
		}
		
		if ( row.IdEstado == "N"){
			color = "yellow";
			return color;
		}

		
		};
 
 	$scope.columnasOperacionesPendientes = function() {
		return  [
				 {field: "Producto"        					,title: 'UI_PRODUCTO',									width: "180px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "DescEstado"               		,title: 'UI_ESTADO', 									width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				 
				,{field: "NombreOperacion"      			,title: 'UI_OPER_PENDIENTE', 							width: "190px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdClasificacionTipoInstrumento"   ,title: 'UI_MERCADO', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdTipoInstrumento"    			,title: 'UI_TIPO_INSTRUMENTO',							width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Nemo"                 			,title: 'UI_NEMO_ABR', 									width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdCondicionLiquidacion"           ,title: 'UI_CONDICION_LIQUIDACION_ABR',					width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdTipoOperacion"            		,title: 'UI_TIPO_OPERACION',							width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "OperadorDirecto"         			,title: 'UI_OPER_DIRECTO', 								width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "DescEstadoCclv"     				,title: 'UI_DESCRIPCION_CCLV', 							width: "550px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FolioOperacion"					,title: 'UI_SECUENCIA', 		format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FechaIngresoRegistro"             ,title: 'UI_FECHA_REGISTRO',							template : " <label> #:  util.formatoDate(FechaIngresoRegistro) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
				,{field: "FechaVencimiento"             	,title: 'UI_FECHA_VENCIMIENTO', 						template : " <label> #:  util.formatoDate(FechaVencimiento) #</label>" ,			 	width: "100px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
				,{field: "DocumentoIdentidad"           	,title: 'UI_RUT_CLIENTE', 									width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Nombre"               			,title: 'UI_NOMBRE_CLIENTE',							width: "250px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "CodigoPortafolio"        			,title: 'UI_CODIGO_PORTAFOLIO',							width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				,{field: "FolioComprobante"       			,title: 'UI_FOLIO_COMPROBANTE', format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FolioComprobanteReferencia"      	,title: 'UI_FOLIO_COMP_ASOC', 	format: "{0:n0}",		width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FolioComprobanteRelacionado"      ,title: 'UI_FOLIO_COMP_CONT', 	format: "{0:n0}",		width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdOrden"            				,title: 'UI_SECUENCIA_ORDEN', 	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Cantidad"             			,title: 'UI_CANTIDAD', 			format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Precio"               			,title: 'UI_PRECIO', 									width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true , template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>"}
				,{field: "Tasa"                 			,title: 'UI_TASA', 										width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true , template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMoneda,IdTipoInstrumento,'')).valor #</label>"}
				,{field: "Monto"                			,title: 'UI_MONTO', 			format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "MontoComprom"        				,title: 'UI_MONTO_COMPROBANTE',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdMoneda"               			,title: 'UI_MONEDA', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdAgente"               			,title: 'UI_AGENTE', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdBolsa"              			,title: 'UI_ENTIDAD', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				,{field: "IdTipoGarantia"        			,title: 'UI_TIPO_GARANTIA', 							width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "DescripcionTipoGarantia"  		,title: 'UI_DESC_TIPO_GARANTIA', 						width: "300px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FondoAfp"            				,title: 'UI_FONDO_AFP', 								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "NemoSVS"             				,title: 'UI_NEMO_SVS', 									width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}		         
		];
	};
	
	
	$scope.confGrillaOperacionesPendientes = {
		dataSource			: dsOperacionesPendientes,
		columns				: $scope.columnasOperacionesPendientes(),	
		rowColorBackground 	: $scope.fnColorBk,
		sortable	: true,
		pageable	: { pageSize : 25 },   //pagina localmente
		filterable 	: true,
		selectable 	: "multiple row",	//"row", 
		reorderable	: false,
		userSettings : true,
		groupable	: true,
		height		: 526,
		allowCopy	: true,
		resizable	: true,
	    autoBind	: false,
	    gridCreated : function(grid) {
	    	grillaOperacionesPendientes = grid;
	    },
	    scrollable	: true
	};
	
   	//********* fin configuracion de objetos *********//
	
/*************************************************************************
* BUSCAR
**************************************************************************/


	$scope.buscarOperacionesPendientes = function(){
		
		if ($scope.kngFechaConsulta === undefined || $scope.kngFechaConsulta === "" || $scope.kngFechaConsulta === null){
			bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_INGRESAR_FECHA'));
			return;
		} else {
			
			var filtros = new Array();
			var filtroOperaciones = new Array();
			var filtroVencimientos = new Array();
			var fechaConsultaInicio = util.dateInicioDia($scope.kngFechaConsulta);
			var fechaConsultaTermino = util.dateTerminoDia($scope.kngFechaConsulta);			

			// inicializa los filtros
			filtroOperaciones.push({ field: "IdTipo", operator: "neq", value: "VENC" });
			filtroOperaciones.push({ field: "FechaIngresoRegistro", operator: "gte", value: fechaConsultaInicio });
			filtroOperaciones.push({ field: "FechaIngresoRegistro", operator: "lte", value: fechaConsultaTermino });			
			
			filtroVencimientos.push({ field: "IdTipo", operator: "eq", value: "VENC" });
			filtroVencimientos.push({ field: "FechaVencimiento", operator: "gte", value: fechaConsultaInicio });
			filtroVencimientos.push({ field: "FechaVencimiento", operator: "lte", value: fechaConsultaTermino });			
		
			if ($scope.oClientePortafolio.seleccionado.IdCliente > 0){
		    	filtroOperaciones.push({ field: "IdCliente", operator: "eq", value: $scope.oClientePortafolio.seleccionado.IdCliente});
		    	filtroVencimientos.push({ field: "IdCliente", operator: "eq", value: $scope.oClientePortafolio.seleccionado.IdCliente});
		    };
		    
			if ($scope.oClientePortafolio.seleccionado !== false && $scope.oClientePortafolio.seleccionado.CodigoPortafolio !== undefined && $scope.oClientePortafolio.seleccionado.CodigoPortafolio !== "" && $scope.oClientePortafolio.seleccionado.CodigoPortafolio !== null ){
				filtroOperaciones.push({ field: "CodigoPortafolio", operator: "eq", value: $scope.oClientePortafolio.seleccionado.CodigoPortafolio});
				filtroVencimientos.push({ field: "CodigoPortafolio", operator: "eq", value: $scope.oClientePortafolio.seleccionado.CodigoPortafolio});
			};
	    	
	    	if ($scope.filtro.estadoObj.IdEstado && $scope.filtro.estadoObj.IdEstado != '0'){
				filtroOperaciones.push({ field: "IdEstadoCustodia", operator: "eq", value: $scope.filtro.estadoObj.IdEstado});
				filtroVencimientos.push({ field: "IdEstadoCustodia", operator: "eq", value: $scope.filtro.estadoObj.IdEstado});
			};
		    
			filtros = {
				logic: "or",
				filters: [
					{
						logic: "and",
						filters: filtroOperaciones
					},
					{
						logic: "and",
						filters: filtroVencimientos
					},
				]
			}

		    dsOperacionesPendientes.filter(filtros);
		}
	};
	
/*************************************************************************
* ENVIAR GARANTÍAS
***************************************************************************/

	$scope.btnEnviarGarantias = function(){
		if ($scope.seleccionOperacionPendiente != undefined){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ENVIAR_GARANTIA"))
			t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) {
				bcsComunes.ventanaOverlay($scope.idVentana,true);

				var OperacionPendienteIns = [];
				//Recorremos las filas seleccionadas
				$.each($scope.seleccionOperacionPendiente,function(key,value){
					if ($scope.seleccionOperacionPendiente[key].IdGestion == 0 
						|| ( $scope.seleccionOperacionPendiente[key].IdGestion > 0 && $scope.seleccionOperacionPendiente[key].IdEstado == 'R'  && $scope.seleccionOperacionPendiente[key].EstadoCclv === "CC37"))	
					{
							OperacionPendienteIns.push(
							{
								FolioOperacion					: $scope.seleccionOperacionPendiente[key].IdRegistro || 0,
								IdTipo							: $scope.seleccionOperacionPendiente[key].IdTipo,							
								FechaIngresoRegistro			: util.dateSPdate($scope.seleccionOperacionPendiente[key].FechaIngresoRegistro)	||'1900-01-01',
								IdTipoOperacion					: $scope.seleccionOperacionPendiente[key].IdTipoOperacion || '',
								TipoOperacion					: $scope.seleccionOperacionPendiente[key].TipoOpePendiente || '',
								NombreOperacion					: $scope.seleccionOperacionPendiente[key].NombreOperacion || '',
								IdTipoInstrumento				: $scope.seleccionOperacionPendiente[key].IdTipoInstrumento || '',
								Nemo							: $scope.seleccionOperacionPendiente[key].Nemo || '',
								IdClasificacionTipoInstrumento	: $scope.seleccionOperacionPendiente[key].IdClasificacionTipoInstrumento || '',
								IdCondicionLiquidacion			: $scope.seleccionOperacionPendiente[key].IdCondicionLiquidacion	|| '',
								TipoOperacion					: $scope.seleccionOperacionPendiente[key].TipoOperacion || '',
								IdOperacion						: $scope.seleccionOperacionPendiente[key].IdOperacion|| '',
								OpDirecto						: $scope.data.switchpOperador  ? 'S' : 'N',
								FechaMovimientoComprobante		: '1900-01-01',//$scope.seleccionOperacionPendiente.,							// REVISAR
								FechaVencimiento				: util.dateSPdate($scope.seleccionOperacionPendiente[key].FechaVencimiento)	||'1900-01-01',	
								DocumentoIdentidad				: $scope.seleccionOperacionPendiente[key].DocumentoIdentidad || '',
								IdPortafolio					: $scope.seleccionOperacionPendiente[key].IdPortafolio || 0,
								FolioComprobante				: $scope.seleccionOperacionPendiente[key].FolioComprobante,
								FolioComprobanteReferencia 		: $scope.seleccionOperacionPendiente[key].FolioComprobanteReferencia,
								IdOrden							: $scope.seleccionOperacionPendiente[key].IdOrden,
								Cantidad						: $scope.seleccionOperacionPendiente[key].Cantidad || 0,
								Precio							: $scope.seleccionOperacionPendiente[key].Precio	|| 0,
								Tasa							: $scope.seleccionOperacionPendiente[key].Tasa || 0,
								Monto							: $scope.seleccionOperacionPendiente[key].Monto || 0,
								MontoMovimientoComprobante		: $scope.seleccionOperacionPendiente[key].MontoComprom || 0,				// REVISAR
								IdEstado						: 'P',//$scope.seleccionOperacionPendiente[key].IdEstado,
								IdAgente						: $scope.seleccionOperacionPendiente[key].IdAgente,
								IdMoneda						: $scope.seleccionOperacionPendiente[key].IdMoneda,
								IdTipoGarantia					: $scope.seleccionOperacionPendiente[key].IdTipoGarantia,				// REVISAR
								FondoAfp						: $scope.seleccionOperacionPendiente[key].FondoAfp,
								IdTipoComprobante				: $scope.seleccionOperacionPendiente[key].IdTipoComprobante,
								IdTipoProducto					: $scope.seleccionOperacionPendiente[key].IdTipoProducto,					// REVISAR
								Producto						: $scope.seleccionOperacionPendiente[key].Producto,					// REVISAR
								IdBolsa							: $scope.seleccionOperacionPendiente[key].IdBolsa,
								DocumentoIdentidadEntidad		: $scope.seleccionOperacionPendiente[key].DocumentoIdentidadEntidad || '',	// REVISAR
								IndDcv							: $scope.seleccionOperacionPendiente[key].IndDcv || '',
								FolioTransaccion				: $scope.seleccionOperacionPendiente[key].FolioTransaccion,
								EstadoCclvDesc					: $scope.seleccionOperacionPendiente[key].DescEstadoCclv || '',
								FechaEjecucion					: $scope.SesionFechaSistema,
								NemoSVS							: $scope.seleccionOperacionPendiente[key].NemoSVS	|| '',
								Categoria						: $scope.seleccionOperacionPendiente[key].Categoria || '',
							});
					}
				});
			  
				var DatosEnviar = {};
				
				DatosEnviar.p_IdEmpresa	=	$scope.SesionIdEmpresa || 0	;
				DatosEnviar.p_IdUsuario	=	$scope.SesionIdUsuario || 0	;
				DatosEnviar.p_IdModulo	=	'CUS'	;			  	  
				DatosEnviar.P_ENVIAROPERACIONESPENDIENTES = OperacionPendienteIns;
				
				
				bcsServiciosCustodia.CrearEnvioOperacionesPendientes(DatosEnviar).$promise.then(
					function(respuesta)
					{
						$scope.gridApi.dataSource.query().then(function(result){
							var v_dsOperacionesPendientes = dsOperacionesPendientes.data()|| [];
							var listDatos = {};
							var OperacionPendiente = [];

							//Recorremos las filas seleccionadas
							$.each(v_dsOperacionesPendientes,function(key,value)
							{
								if (v_dsOperacionesPendientes[key].IdGestion > 0 && v_dsOperacionesPendientes[key].IdEstado == 'P')	
								{
									var varObjOperacionesPendientes = {};
									varObjOperacionesPendientes= 
									{
										p_IdTipo				: v_dsOperacionesPendientes[key].IdTipo,
										p_TipoOperacion         : v_dsOperacionesPendientes[key].TipoOperacion 	|| '',
										p_FolioOperacion        : v_dsOperacionesPendientes[key].IdRegistro 	|| 0,
										p_FolioTransaccion      : v_dsOperacionesPendientes[key].FolioTransaccion 	|| 0,
										p_DocumentoIdentidad    : v_dsOperacionesPendientes[key].DocumentoIdentidad|| '',
										p_Cantidad              : v_dsOperacionesPendientes[key].Cantidad || 0,
										p_FechaIngresoRegistro  : util.dateSPdate(v_dsOperacionesPendientes[key].FechaIngresoRegistro)	||'1900-01-01',
										p_IndDcv                : v_dsOperacionesPendientes[key].IndDcv || '',
										p_Categoria             : v_dsOperacionesPendientes[key].Categoria || '',
										p_NemoSVS               : v_dsOperacionesPendientes[key].Nemo || '',
										p_IdTipoGarantia        : v_dsOperacionesPendientes[key].IdTipoGarantia || '',
										p_IdBolsa               : v_dsOperacionesPendientes[key].IdBolsa,
										p_FondoAfp              : '',		
										p_IdInterno			  	: v_dsOperacionesPendientes[key].IdInterno || 0,		
										p_Producto				: v_dsOperacionesPendientes[key].Producto || '',
										p_Nombre				: v_dsOperacionesPendientes[key].Nombre || '',
										p_IdGestion				: v_dsOperacionesPendientes[key].IdGestion,		
									}
									OperacionPendiente.push(varObjOperacionesPendientes)
								}
							});
							
							listDatos.p_IdEmpresa      = $scope.SesionIdEmpresa; 
							listDatos.p_IdUsuario      = $scope.SesionIdUsuario;
							listDatos.p_IdModulo       = 'CUS';
							listDatos.p_CodidoCorredor = v_dsOperacionesPendientes[0].CodCorr;//61; // TODO
							listDatos.p_OperacionesPendientes =  OperacionPendiente;

							if (listDatos.p_OperacionesPendientes.length == 0) {
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								bcsComunes.msg("warning","warning",$translate.instant("UI_REGISTROS_NO_ENVIADOS") + ' : ' + $translate.instant("UI_ADVERTENCIA"));
								return;
							};

							bcsServiciosCustodia.EnviarOperacionesPendientes(listDatos).$promise.then(
								function(respuesta){	  		
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.idVentana);
									
									bcsComunes.ventanaOverlay($scope.idVentana,false);
									$scope.gridApi.dataSource.read();
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										mensaje:'UI_ERROR_TIMEOUT',
										conflictos:util.parseServerResponse(result)
									},$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
								}
							);
						});
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:'UI_ERROR_INTERNO_SISTEMA',
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					}
				);
			}
			else 
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		});
		return;  
		}
	};
/*************************************************************************
* COMPLETADO
***************************************************************************/

		$scope.btnCompletado = function(){
				if ($scope.seleccionOperacionPendiente != undefined){
						var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ENVIAR_GARANTIA"))//+ " Tipo Evento Capital: " +  $scope.bEC.seleccionEventoCapital.TipoEvento + " Nemo: " + $scope.bEC.seleccionEventoCapital.Nemo + " Secuencia: " + $scope.bEC.seleccionEventoCapital.Secuencia)
							t.result.then(function(result){
								if (result===OPCIONES_BCSALERTA.SI) {
									bcsComunes.ventanaOverlay($scope.idVentana,true);

									var OperacionPendienteIns = [];					
									//Recorremos las filas seleccionadas
							  	    $.each($scope.seleccionOperacionPendiente,function(key,value){
							  	    	if ($scope.seleccionOperacionPendiente[key].IdInterno == 0 )
							  	    	{
								  	    		OperacionPendienteIns.push(
								  	    		{				  			
										  			FolioOperacion					: $scope.seleccionOperacionPendiente[key].FolioOperacion || 0,
										  			IdTipo							: $scope.seleccionOperacionPendiente[key].IdTipo,							
										  			FechaIngresoRegistro			: util.dateSPdate($scope.seleccionOperacionPendiente[key].FechaIngresoRegistro)	||'1900-01-01',
										  			IdTipoOperacion					: $scope.seleccionOperacionPendiente[key].IdTipoOperacion || '',
										  			TipoOperacion					: $scope.seleccionOperacionPendiente[key].TipoOpePendiente || '',
										  			NombreOperacion					: $scope.seleccionOperacionPendiente[key].NombreOperacion || '',
										  			IdTipoInstrumento				: $scope.seleccionOperacionPendiente[key].IdTipoInstrumento || '',
										  			Nemo							: $scope.seleccionOperacionPendiente[key].Nemo || '',
										  			IdClasificacionTipoInstrumento	: $scope.seleccionOperacionPendiente[key].IdClasificacionTipoInstrumento || '',
										  			IdCondicionLiquidacion			: $scope.seleccionOperacionPendiente[key].IdCondicionLiquidacion	|| '',
										  			TipoOperacion					: $scope.seleccionOperacionPendiente[key].TipoOperacion || '',
										  			IdOperacion						: $scope.seleccionOperacionPendiente[key].IdOperacion|| '',
										  			OpDirecto						: $scope.data.switchpOperador  ? 'S' : 'N',
										  			FechaMovimientoComprobante		: '1900-01-01',//$scope.seleccionOperacionPendiente.,							// REVISAR
										  			FechaVencimiento				: util.dateSPdate($scope.seleccionOperacionPendiente[key].FechaVencimiento)	||'1900-01-01',	
										  			DocumentoIdentidad				: $scope.seleccionOperacionPendiente[key].DocumentoIdentidad || '',
										  			IdPortafolio					: $scope.seleccionOperacionPendiente[key].IdPortafolio || 0,
										  			FolioComprobante				: $scope.seleccionOperacionPendiente[key].FolioComprobante,
										  			FolioComprobanteReferencia 		: $scope.seleccionOperacionPendiente[key].FolioComprobanteReferencia,
										  			IdOrden							: $scope.seleccionOperacionPendiente[key].IdOrden,
										  			Cantidad						: $scope.seleccionOperacionPendiente[key].Cantidad || 0,
										  			Precio							: $scope.seleccionOperacionPendiente[key].Precio	|| 0,
										  			Tasa							: $scope.seleccionOperacionPendiente[key].Tasa || 0,
										  			Monto							: $scope.seleccionOperacionPendiente[key].Monto || 0,
										  			MontoMovimientoComprobante		: $scope.seleccionOperacionPendiente[key].MontoComprom || 0,				// REVISAR
										  			IdEstado						: 'M',//$scope.seleccionOperacionPendiente[key].IdEstado,
										  			IdAgente						: $scope.seleccionOperacionPendiente[key].IdAgente,
										  			IdMoneda						: $scope.seleccionOperacionPendiente[key].IdMoneda,
										  			IdTipoGarantia					: $scope.seleccionOperacionPendiente[key].IdTipoGarantia,				// REVISAR
										  			FondoAfp						: $scope.seleccionOperacionPendiente[key].FondoAfp,
										  			IdTipoComprobante				: $scope.seleccionOperacionPendiente[key].IdTipoComprobante,
										  			IdTipoProducto					: $scope.seleccionOperacionPendiente[key].IdTipoProducto,					// REVISAR
										  			Producto						: $scope.seleccionOperacionPendiente[key].Producto,					// REVISAR
										  			IdBolsa							: $scope.seleccionOperacionPendiente[key].IdBolsa,
										  			DocumentoIdentidadEntidad		: $scope.seleccionOperacionPendiente[key].DocumentoIdentidadEntidad || '',	// REVISAR
										  			IndDcv							: $scope.seleccionOperacionPendiente[key].IndDcv || '',
										  			FolioTransaccion				: $scope.seleccionOperacionPendiente[key].FolioTransaccion,
										  			EstadoCclvDesc					: $scope.seleccionOperacionPendiente[key].DescEstadoCclv || '',
										  			FechaEjecucion					: $scope.SesionFechaSistema,
										  			NemoSVS							: $scope.seleccionOperacionPendiente[key].NemoSVS	|| '',
										  			Categoria						: $scope.seleccionOperacionPendiente[key].Categoria || '',
								  	    		}
								  	    		);
							  	    	}
							  	    	}
							  	    );
							  	  
							  	  var DatosEnviar = {};
							  	  
							  	  DatosEnviar.p_IdEmpresa	=	$scope.SesionIdEmpresa || 0	;
							  	  DatosEnviar.p_IdUsuario	=	$scope.SesionIdUsuario || 0	;
							  	  DatosEnviar.p_IdModulo	=	'CUS'	;			  	  
							  	  DatosEnviar.P_ENVIAROPERACIONESPENDIENTES = OperacionPendienteIns;
							  	  
							  	  
							  	  bcsServiciosCustodia.CrearEnvioOperacionesPendientes(DatosEnviar,
															function(respuesta){	  		
																bcsComunes.ventanaMsg({
																	conflictos:util.parseServerResponse(respuesta)
																},$scope.idVentana);
																
																bcsComunes.ventanaOverlay($scope.idVentana,false);
																$scope.gridApi.dataSource.read();
															},
															function(respuesta){
																bcsComunes.ventanaMsg({
																	conflictos:util.parseServerResponse(respuesta)
																},$scope.idVentana);
																bcsComunes.ventanaOverlay($scope.idVentana,false);
															});											
											
									}
								else 
									bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
								});
								return;  

					}
				};
//**
		
		
$scope.btnValidarGarantias = function(){
	if ($scope.seleccionOperacionPendiente != undefined){
		var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ENVIAR_GARANTIA"))//+ " Tipo Evento Capital: " +  $scope.bEC.seleccionEventoCapital.TipoEvento + " Nemo: " + $scope.bEC.seleccionEventoCapital.Nemo + " Secuencia: " + $scope.bEC.seleccionEventoCapital.Secuencia)
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					var datos = {};
					bcsComunes.ventanaOverlay($scope.idVentana,true);

					
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';

					datos.p_IdEventoCapital					= Number($scope.seleccionEventoCapital.IdEventoCapital) || 0;			                   		
					datos.p_IdTipoEventoCapital				= $scope.seleccionEventoCapital.IdTipoEventoCapital || '';
					datos.p_IdEventoCapitalCliente			= 0;
					
					bcsServiciosEventoCapital.EnviarOperacionesPendientes(datos,
							function(respuesta){	  		
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								$scope.gridApi.dataSource.read();
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
							});
					
				}
				else 
					bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

/*					bcsServiciosEventoCapital.NotificarEventoCapital(datos,
							function(respuesta){	  		
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								$scope.gridApi.dataSource.read();
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
							});
					
				}
				else 
					bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   */

	}
};		

/*************************************************************************
* LIMPIAR
***************************************************************************/

	$scope.limpiarFiltros = function() {
		
		$scope.filtros.IdPortafolio = false;
		$scope.kngFechaConsulta		 = util.dateInicioDia($scope.SesionFechaSistema);
	};
});


