angular.module('ModuloOrdenes').controller('CuadraturaDiariaOperacionesCtrl',
		function($scope,$translate,$modal,$log,$resource,$timeout,
				bcsComunes,bcsAlerta,OPCIONES_BCSALERTA, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.data.resumenVencimiento = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.tab = {}; //workaround para el manejo de grillas dentro de tabs.
	$scope.tab.active = 0;
	
	//Visibilidad de Grillas
	$scope.verGrillaTRX		= true;
	$scope.verGrillaORDEN	= false;
	$scope.verGrillaASIGNACION	= false;
	$scope.verGrillaFACTURA	= false;
	$scope.verGrillaCAVALI	= false;
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.cuadratura = false;
	$scope.data.cierre = false;
	$scope.data.fechaCuadratura = $scope.SesionFechaSistema;
	$scope.kOptFechaCuadratura = {};
	$scope.kOptFechaCuadratura.max = $scope.data.fechaCuadratura;
	$scope.mostrarBotonCargaArchivoCavali = $scope.SesionLocale === 'CL' ? false : true;
		
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	//CONFIGURACION GRILLA DE RESUMEN
	$scope.confBasicaCuadraturas = {
		
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
						pageSize: 200, // Número de registros por página.
						schema : {
							model: {
								fields : {
									IdCuadraturaDiariaOperaciones			: {type: "string"},
									OperacionCuadraturaDiariaOperaciones	: {type: "string"},
									FechaEjecucion							: {type: "sysdate"},
									TotalOperaciones 						: {type: "number"},
									OperacionesConProblemas					: {type: "number"},
									TipoOperacion 							: {type: "string"},
									Resultado 								: {type: "string"}
								}
							}
						}
					}
		}),
		gridCreated: function(grid) {
			$scope.gridApiCuadraturas = grid;
		},
		columns: [
		          	{field : "OperacionCuadraturaDiariaOperaciones",	title : "UI_OPERACION", 				width:"300px", groupable: true},
		          	{field : "FechaEjecucion", 							title : 'UI_FECHA', 					width: "90px" , groupable: true},
					{field : "TotalOperaciones",						title : "UI_CANTIDAD_OPERACIONES",		width : "100px", groupable: true},
					{field : "OperacionesConProblemas",					title : "UI_DIFERENCIA_OPERACIONES",	width : "120px", groupable: true},
					{field : "TipoOperacion",							title : "UI_TIPO_OPERACION",			width : "0px", hidden:true},
					{field : "Resultado", 								title : "UI_RESULTADO",					width : "200px", groupable: true}
				],
		rowColorBackground : function(row) {
			//console.log(row);
			if(row.get("TotalOperaciones")==0 && row.get("OperacionesConProblemas")==0){
				return "yellow";
			}else if(row.get("OperacionesConProblemas")==0){
				return "green";
			}else{
				return "red";
			}
	  	},
		allowCopy			: { delimeter: "\t" },
		sortable: true,
		pageable: true,
		filterable: true,
		scrollable: true,
		height: 250,
		selectable 			: "multiple row",
		userSettings:true,
		navigatable: true,
  		autoBind : false
	};
	
	//CONFIGURACION GRILLA DE Transacciones con Asignaciones
	$scope.confBasicaTrxAsig = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										IdClasificacionTipoInstrumento	: {type: "string"},
										FechaOperacionTransaccion		: {type: "sysdatetime"},
										FechaVigencia					: {type: "sysdate"},
										FolioTransaccion				: {type: "string"},
										FechaVencimiento				: {type: "sysdate"},
										IdBolsa							: {type: "string"},
										AccionOrden						: {type: "string"},
										Nemotecnico						: {type: "string"},
										CodigoOperadorCorredor			: {type: "string"},
										CantidadTransaccion				: {type: "number"},
										CantidadAsignadaTransaccion		: {type: "number"},
										MontoTransaccion				: {type: "number"},
										MontoAsignadoTransaccion		: {type: "number"},
										IdMonedaTransaccion				: {type: "string"},
										EsFungible						: {type: "string"},
										Descripcion						: {type: "string"},
										IdTipoInstrumento				: {type: "string"},
										ValorRescateTransaccion			: {type: "number"},
										ValorRescateAsignadoTransaccion	: {type: "number"},
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.tab.gridApiTrxAsig = grid;
			},
		columns: [
		    {field: "Descripcion",						title: "UI_DESCRIPCION", 			width: "250px"},
		    {field: "IdTipoInstrumento",				title: "UI_TIPO_INSTRUMENTO", 		width: "150px"},
		    {field: "FechaOperacionTransaccion",		title: "UI_FECHA_OPERACION", 		width: "140px"},
	      	{field: "FolioTransaccion",					title: "UI_FOLIO_TRANSACCION", 		width: "100px"},
	      	{field: "IdBolsa",							title: "UI_BOLSA", 					width: "75px"},
	      	{field: "AccionOrden",						title: "UI_OPERACION", 				width: "100px"},
	      	{field: "Nemotecnico",						title: "UI_NEMO", 					width: "150px"},
	      	
	      	{field: "CodigoOperadorCorredor",			title: "UI_OPERADOR_CORREDOR", 		width: "100px"},
	      	{field: "CantidadTransaccion",				title: "UI_CANTIDAD", 				width: "100px",template : " <label> #:  (util.bcs_formato(CantidadTransaccion,'GEN','Cantidad',IdMonedaTransaccion,'','')).valor #</label>"},
	      	{field: "CantidadAsignadaTransaccion",		title: "UI_CANTIDAD_ASIGNADA", 		width: "100px",template : " <label> #:  (util.bcs_formato(CantidadAsignadaTransaccion,'GEN','Cantidad',IdMonedaTransaccion,'','')).valor #</label>"},
	      	{field: "MontoTransaccion",					title: "UI_MONTO", 					width: "100px",template : " <label> #:  (util.bcs_formato(MontoTransaccion,'GEN','Monto',IdMonedaTransaccion,'','')).valor #</label>"},
	      	{field: "MontoAsignadoTransaccion",			title: "UI_MONTO_ASIGNADO", 		width: "100px",template : " <label> #:  (util.bcs_formato(MontoAsignadoTransaccion,'GEN','Monto',IdMonedaTransaccion,'','')).valor #</label>"},
	      	{field: "IdMonedaTransaccion",				title: "UI_MONEDA", 				width: "75px"},
	      	{field: "EsFungible",						title: "UI_FUNGIBLE", 				width: "100px"},
	      	
	      	
	      	{field: "ValorRescateTransaccion",			title: "UI_VALOR_RESCATE", 			width: "100px",template : " <label> #:  (util.bcs_formato(ValorRescateTransaccion,'GEN','Monto',IdMonedaTransaccion,'','')).valor #</label>"},
	      	{field: "ValorRescateAsignadoTransaccion",	title: "UI_VALOR_RESCATE_ASIGNADO", width: "100px",template : " <label> #:  (util.bcs_formato(ValorRescateAsignadoTransaccion,'GEN','Monto',IdMonedaTransaccion,'','')).valor #</label>"}
		],
		sortable: true,
		pageable: true,
		filterable: true,
		scrollable: true,
		height: 250,
		selectable : " row",
		userSettings:true,
		navigatable: true,
		excel : {allPages : true},
		autoBind : false
	};
	
	$scope.confBasicaCavali = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										NumeroOperacion	    : {type: "number"},
										FechaOperacion		: {type: "sysdate"},
										FolioTransaccion	: {type: "number"},
										CantidadOperacion	: {type: "number"},
										MontoEfectivo		: {type: "number"},
										DescripcionNegocio	: {type: "string"},
										PrecioOperacion		: {type: "number"},
									}
								}
							}
						}
			}),
			
			gridCreated: function(grid) {
				$scope.tab.gridApiCavali = grid;
			},
		columns: [
		    {field: "NumeroOperacion",			   title: "UI_NUMERO_OPERACION",		width: "250px"},
		    {field: "FechaOperacion",			   title: "UI_FECHA_OPERACION", 		width: "150px"},
		    {field: "FolioTransaccion",		       title: "UI_FOLIO_TRANSACCION", 		width: "140px"},
		    {field: "CantidadOperacion",		   title: "UI_CANTIDAD",		 		width: "140px"},
		    {field: "MontoEfectivo",		       title: "UI_MONTO",			 		width: "140px"},
		    {field: "DescripcionNegocio",		   title: "UI_DESCRIPCION_NEGOCIO",		width: "250px"},
		    {field: "PrecioOperacion",		       title: "UI_PRECIO",			 		width: "140px"},
		],
		sortable: true,
		pageable: true,
		filterable: true,
		scrollable: true,
		height: 250,
		selectable : " row",
		userSettings:true,
		navigatable: true,
		excel : {allPages : true},
		autoBind : false
	};
	
	$scope.confBasicaOrdenes= {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										IdClasificacionTipoInstrumento	: {type: "string"},
										IdTipoInstrumento				: {type: "string"},
										IdOrden							: {type: "number"},
										FechaOperacionOrden				: {type: "sysdatetime"},
										FechaVigencia					: {type: "sysdate"},
										FechaVencimiento				: {type: "sysdate"},
										IdEstadoOrden					: {type: "string"},
										CantidadOrden					: {type: "number"},
										MontoOrden						: {type: "number"},
										IdMonedaOrden					: {type: "string"},
										Tasa							: {type: "number"},
										IdCliente						: {type: "number"},
										DocumentoIdentidad				: {type: "string"},
										NombreCompleto					: {type: "string"},
										InstrumentoUI					: {type: "string"},
										CantidadAsignadaOrden			: {type: "number"},
										MontoAsignadoOrden				: {type: "number"},
										Descripcion						: {type: "string"},
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.tab.gridApiOrdenes = grid;
			},
			columns: [
			    {field: "Descripcion",						title: "UI_DESCRIPCION", 		width: "200px"},
				{field: "IdTipoInstrumento",				title: "UI_TIPO_INSTRUMENTO",	width: "150px"},
				{field: "IdOrden",							title: "UI_ORDEN", 				width: "150px"},
				{field: "FechaOperacionOrden",				title: "UI_FECHA_ORDEN", 		width: "140px"},
				{field: "FechaVigencia",					title: "UI_FECHA_VIGENCIA", 	width: "140px"},
				{field: "FechaVencimiento",					title: "UI_FECHA_VENCIMIENTO", 	width: "140px"},
				{field: "InstrumentoUI",					title: "UI_NEMO", 				width: "150px"},
				{field: "CantidadOrden",					title: "UI_CANTIDAD", 			width: "150px",template : " <label> #:  (util.bcs_formato(CantidadOrden,'GEN','Cantidad',IdMonedaOrden,'','')).valor #</label>"},
				{field: "CantidadAsignadaOrden",			title: "UI_CANTIDAD_ASIGNADA", 	width: "150px",template : " <label> #:  (util.bcs_formato(CantidadAsignadaOrden,'GEN','Cantidad',IdMonedaOrden,'','')).valor #</label>"},
				{field: "MontoOrden",						title: "UI_MONTO",				width: "150px",template : " <label> #:  (util.bcs_formato(MontoOrden,'GEN','Monto',IdMonedaOrden,'','')).valor #</label>"},
				{field: "MontoAsignadoOrden",				title: "UI_MONTO_ASIGNADO",		width: "150px",template : " <label> #:  (util.bcs_formato(MontoAsignadoOrden,'GEN','Monto',IdMonedaOrden,'','')).valor #</label>"},
				{field: "Tasa",								title: "UI_TASA",				width: "150px",template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMonedaOrden,'','')).valor #</label>"},
				{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD",width: "150px"},
				{field: "NombreCompleto",					title: "UI_NOMBRE",				width: "150px"},
			],
			sortable: true,
      		filterable: true,
			pageable: true,
			selectable: "row",
			resizable: false,
      		autoBind: false,
      		scrollable: true,
			userSettings:true,
			navigatable: true,
			excel : {allPages : true},
			height: 250,
		};
	
	$scope.confBasicaAsignacion  = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										IdMovimientoComprobante			: {type: "number"},
										EstadoFacturacion				: {type: "string"},
										NombreEstadoFacturacion			: {type: "string"},
										IdAsignacion					: {type: "number"},
										FolioTransaccion				: {type: "string"},
										FechaAsignacion					: {type: "sysdatetime"},
										IdEstadoAsignacion				: {type: "string"},
										EstadoAsignacion				: {type: "string"},
										AccionOrden						: {type: "string"},
										NombreTipoOperacion				: {type: "string"},
										InstrumentoUI					: {type: "string"},
										IdBolsa							: {type: "string"},
										NombreBolsa						: {type: "string"},
										IdTipoInstrumento				: {type: "string"},
										NombreTipoInstrumento			: {type: "string"},
										IdClasificacionTipoInstrumento	: {type: "string"},
										IdCliente						: {type: "number"},
										DocumentoIdentidad				: {type: "string"},
										NombreCompleto					: {type: "string"},
										Descripcion						: {type: "string"}
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.tab.gridApiAsignacion = grid;
			},
			columns: [
			          
			    {field: "Descripcion",						title: "UI_DESCRIPCION", 				width: "200px"},
				{field: "IdAsignacion",						title: "UI_ASIGNACION",					width: "150px"},
				{field: "FolioTransaccion",					title: "UI_FOLIO_TRANSACCION", 			width: "150px"},
				{field: "FechaAsignacion",					title: "UI_FECHA_ASIGNACION" ,			width: "140px"},
				{field: "InstrumentoUI",					title: "UI_NEMO", 						width: "150px"},
				{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD", 		width: "150px"},
				{field: "NombreCompleto",					title: "UI_CLIENTE", 					width: "150px"},
				{field: "EstadoAsignacion",					title: "UI_ESTADO_ASIGNACION", 			width: "150px"},
				{field: "NombreEstadoFacturacion",			title: "UI_ESTADO_FACTURACION",			width: "150px"},
				{field: "IdMovimientoComprobante",			title: "UI_SECUENCIA_MOVIMIENTO",		width: "150px"},
				{field: "AccionOrden",						title: "UI_OPERACION",					width: "150px"},
				{field: "IdBolsa",							title: "UI_BOLSA", 						width: "150px"},
				{field: "NombreTipoInstrumento",			title: "UI_TIPO_INSTRUMENTO", 			width: "150px"}
				
			],
			sortable: true,
      		filterable: true,
			pageable: true,
			selectable: "row",
			resizable: false,
      		autoBind: false,
      		scrollable: true,
			userSettings:true,
			navigatable: true,
			excel : {allPages : true},
			height: 250,
		};
	
	$scope.confBasicaFacturacion  = {
		dataSource: new bcs.DataSource({
			type : "array", //Los tipos posibles son: odata, call, array y object
			definition : {
						pageSize: 200, // Número de registros por página.
						schema : {
							model: {
								fields : {
									EstadoFacturacion				: {type: "string"},
									NombreEstadoFacturacion			: {type: "string"},
									IdMovimientoComprobante			: {type: "number"},
									IdTipoComprobante				: {type: "string"},
									NombreTipoComprobante			: {type: "string"},
									IdConceptoMovimiento			: {type: "string"},
									NombreConceptoMovimiento		: {type: "string"},
									DocumentoIdentidad				: {type: "string"},
									TipoDocumentoIdentidad			: {type: "string"},
									NombreLargoCliente				: {type: "string"}
								}
							}
						}
					}
		}),
		gridCreated: function(grid) {
			$scope.tab.gridApiFacturacion = grid;
		},
		columns: [
		    {field: "NombreEstadoFacturacion",		title: "UI_ESTADO_FACTURACION",					width: "140px"},
			{field: "IdMovimientoComprobante",		title: "UI_MOVIMIENTO_COMPROBANTE",				width: "140px"},
			{field: "NombreTipoComprobante",		title: "UI_TIPO_COMPROBANTE" ,					width: "240px"},
			{field: "IdConceptoMovimiento",			title: "UI_CONCEPTO", 							width: "100px"},
			{field: "NombreConceptoMovimiento",		title: "UI_CONCEPTO", 							width: "250px"},
			{field: "DocumentoIdentidad",			title: "UI_DOCUMENTO_IDENTIDAD",	 			width: "130px"},
			{field: "TipoDocumentoIdentidad",		title: "UI_TIPO_DOCUMENTO_IDENTIDAD",			width: "150px"},
			{field: "NombreLargoCliente",			title: "UI_NOMBRE_CLIENTE",						width: "250px"}			
		],
		sortable: true,
  		filterable: true,
		pageable: true,
		selectable: "row",
		resizable: false,
  		autoBind: false,
  		scrollable: true,
		userSettings:true,
		navigatable: true,
		excel : {allPages : true},
		height: 250,
	};

	$scope.consultarResumen = function(){
		try{
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);

			$scope.limpiarPantalla();
			
			var urlConsultarResumen = "/BSCL/Plataforma/GWJAVA/PLA/call?t=186";
			var r = $resource("", {},
		    {
				'ConsultarResumen': { method: "POST", url: urlConsultarResumen}
			});
			
			var datos= {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'ORD',
				p_FechaInicio: util.dateToFormat($scope.data.fechaCuadratura || $scope.SesionFechaSistema ,"YYYY-MM-DD") 
			};
			
			var post = r.ConsultarResumen(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					if(result.P_OUT_RESUMEN){
						for(var i in result.P_OUT_RESUMEN){
							result.P_OUT_RESUMEN[i].OperacionCuadraturaDiariaOperaciones = $translate.instant(result.P_OUT_RESUMEN[i].OperacionCuadraturaDiariaOperaciones);
							result.P_OUT_RESUMEN[i].FechaEjecucion = $translate.instant(result.P_OUT_RESUMEN[i].FechaEjecucion);
							result.P_OUT_RESUMEN[i].Resultado = $translate.instant(result.P_OUT_RESUMEN[i].Resultado);
							$scope.data.operacionesConError += result.P_OUT_RESUMEN[i].Diferencias;
						}
						buildDetalleCuadratura($scope.gridApiCuadraturas, result.P_OUT_RESUMEN);
						
						//Cargar las diferencias en la cuadratura RV,RF,IF,IF Fungible, RV Simultanea
//						if(result.P_OUT_TRANSACCIONES){
//							buildDetalleCuadratura($scope.tab.gridApiTrxAsig, result.P_OUT_TRANSACCIONES);
//						}
//						//Cargar las ordenes que esten sobre asignadas o en estado ingresado
//						if(result.P_OUT_ORDENES){
//							buildDetalleCuadratura($scope.tab.gridApiOrdenes, result.P_OUT_ORDENES);
//						}
//						//Cargar las asignaciones enviadas a facturar, que no han sido facturadas
//						if(result.P_OUT_FACTURACION){
//							buildDetalleCuadratura($scope.tab.gridApiAsignacion, result.P_OUT_FACTURACION);
//						}
//						//Cargar palos de CAVALI con OPTIMUS
//						if(result.P_OUT_CUADRATURA_CAVALI){
//							buildDetalleCuadratura($scope.tab.gridApiCavali, result.P_OUT_CUADRATURA_CAVALI);
//						}

					}
					
					if(result.P_OUT_ORDENES_VENCER){
						$scope.data.resumenVencimiento = result.P_OUT_ORDENES_VENCER;
					}
					
					$scope.data.cuadratura = true;
					bcsComunes.ventanaOverlay(idVentana, false);
				}catch(e){
					bcsComunes.ventanaOverlay(idVentana, false);
				}
					
					
					
			}, function(result){//en caso de falla del servicio.
				bcsComunes.ventanaMsg({
				       conflictos:util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana, false);
			});
			
		}catch(e){
			console.log(e);
			bcsComunes.ventanaOverlay(idVentana, false);
		}
	};
	
	var buildDetalleCuadratura = function(gridapi, data){
		gridapi.dataSource.data(data, true);
	};
	
	var getFechaSistema = function(){//asegurar que la fecha del sistema exista.
		if($scope.SesionFechaSistema)
			return $scope.SesionFechaSistema + " 00:00:00.0";
		else
			return setTimeout(getFechaSistema(), 1000);	
	}
	
	$scope.limpiarPantalla = function()
	{	
//		$scope.gridApiCuadraturas.dataSource.removeAll();
		$scope.tab.gridApiTrxAsig.dataSource.removeAll();
		$scope.tab.gridApiOrdenes.dataSource.removeAll();
		$scope.tab.gridApiAsignacion.dataSource.removeAll();
		$scope.tab.gridApiCavali.dataSource.removeAll();
	};
	
	$scope.vencerOrdenes = function(){
		
		//Cerrar SAP Business One detendrá todos los procesos en ejecución y cerrará todas las ventanas abiertas. ¿Desea continuar?
		var resumen = $scope.gridApiCuadraturas.dataSource.data();
		
		var mensaje = $translate.instant("UI_CERRAR_DIA_SISTEMA");
		if(resumen.length==0){ //si esta vacio, es porque no han cuadrado el sistema
			mensaje =$translate.instant("UI_CERRAR_DIA_SISTEMA_SIN_CUADRAR");
		}
		
		mensaje = mensaje.replace("XXX", $scope.data.operacionesConError).replace("YYY", $scope.SesionFechaSistema);
		var data = {
			resumen: resumen,
			resumenVencimiento: $scope.data.resumenVencimiento,
			operacionesConError: $scope.data.operacionesConError,
			mensajeModal: mensaje,
			fechaVencimiento: getFechaSistema()
		};
		
		var id1 = bcsComunes.ventanaAbrir("VencimientoOrdenModal", data, false,true);                      
	    bcsComunes.ventanaPromesa(id1).then(function(data){ 
	    	if (data === true){
	    		$scope.data.cierre = true;
	    		$scope.data.cuadratura = false;
			}
		});		
	}
	
	$scope.oficializarOrdenes = function(){
		
		var resumen = $scope.gridApiCuadraturas.dataSource.data();
		
		var mensaje = $translate.instant("UI_OFICIALIZAR_ORDENES_DIARIAS");
		if(resumen.length==0){ //si esta vacio, es porque no han cuadrado el sistema
			mensaje =$translate.instant("UI_CERRAR_DIA_SISTEMA_SIN_CUADRAR");
		}
		
		var cantidadOrdenesOficializar = 0;
		$.each($scope.data.resumenVencimiento, function(key){
			cantidadOrdenesOficializar += $scope.data.resumenVencimiento[key].Total;
			 
		});
		
		
		mensaje = mensaje.replace("XXX", $scope.data.operacionesConError).replace("YYY", $scope.SesionFechaSistema);
		var data = {
			resumen: resumen,
			resumenOficializacion: [{Titulo: "UI_ORDENES_OFICIALIZAR", Total:cantidadOrdenesOficializar}],
			operacionesConError: $scope.data.operacionesConError,
			mensajeModal: mensaje,
			fechaVencimiento: getFechaSistema()
		};
				
		
		var id1 = bcsComunes.ventanaAbrir("OficializarOrdenModal", data, false,true);                      
	    bcsComunes.ventanaPromesa(id1).then(function(data){
	    	if(data==true){
	    	   	$scope.data.cierre = false;
		    	$scope.data.cuadratura = false;
	    	}
	    	bcsComunes.ventanaOverlay(idVentana, false);
		});	
	}
	
	$scope.gridSelecionada = "";
	$scope.select = function(grid)
	{
		$scope.gridSelecionada = grid;
		util.refreshGridSize(grid, 50);
	};
	
	$scope.exportarExcel = function(){
		console.log($scope.gridSelecionada);
		if($scope.gridSelecionada==="grid-resultado-TRX-ASIG"){
			$scope.tab.exportarExcel1();
		}else if($scope.gridSelecionada==="grid-resultado-ORDEN"){
			$scope.tab.exportarExcel2();
		}else if($scope.gridSelecionada==="grid-resultado-ASIGNACION"){
			$scope.tab.exportarExcel3();
		}else if($scope.gridSelecionada==="grid-resultado-CAVALI"){
			$scope.tab.exportarExcel4();
		}else if($scope.gridSelecionada==="grid-resultado-FACTURA"){
			$scope.tab.exportarExcel5();
		}
	};

	$scope.cargarArchivo = function() {
		var datosArchivo = {codigoArchivo: "CUADRATURA_CAVALI", titulo: "CARGA_CUADRATURA_CAVALI"};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
		bcsComunes.ventanaPromesa(id1).then(function(result){
			if(!result || !result.fileData){
				 bcsComunes.ventanaOverlay(idVentana,false);
				 return;
			}
			var contenidoArchivo = {p_contenido : result.fileData};
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'ORD',
					p_TipoPrograma : [{
						IdInterno : 0,
						IdTipoPrograma : "CARGAR_ARCHIVO_TRANS_ORD",
						SegundosDesfase : 0
					}],
					p_Parametro : [{
						IdInterno : 0,
						NombreParametro : 'IdEmpresa',
						Valor : $scope.SesionIdEmpresa
					},{
						IdInterno : 0,
						NombreParametro : 'IdUsuario',
						Valor : $scope.SesionIdUsuario
					},{
						IdInterno : 0,
						NombreParametro : 'IdModulo',
						Valor : 'ORD'
					},{
						IdInterno : 0,
						NombreParametro : 'TipoArchivo',
						Valor : "CUADRATURA_CAVALI"
					},{
						IdInterno : 0,
						NombreParametro : 'ContenidoArchivo',
						ValorBlob : btoa(JSON.stringify(contenidoArchivo))
					},{
						IdInterno : 0,
						NombreParametro : 'NombreArchivo',
						Valor : result.fileName
					}]
			}
			var urlInscribirProcesoBatch = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043";
			var r = $resource("", {},
		    {
				'InscribirProcesoBatch': { method: "POST", url: urlInscribirProcesoBatch}
			});
			
			r.InscribirProcesoBatch(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_CARGAR_ARCHIVO_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, idVentana);

			},
			function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			});
			
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			console.log(result);
		})
	}

	$scope.Cuadratura = function(){
		try{
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);

//			$scope.limpiarPantalla();
			
			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=187";
			var r = $resource("", {},
		    {
				'Cuadratura': { method: "POST", url: urlCuadratura}
			});
			
			var fechaSistema = getFechaSistema();
			var ProcesosAEjecutar = [];


			if($scope.seleccionCuadraturas == undefined ){
				var resumen = $scope.gridApiCuadraturas.dataSource.data();
				//Recopila datos de toda la grilla 
				$.each(resumen,function(key,value){  
					
					ProcesosAEjecutar.push(
						{     
							IdCuadraturaDiariaOperaciones	: resumen[key].IdCuadraturaDiariaOperaciones
						});
					});
					
				}else{
				//console.log($scope.seleccionCuadraturas.length);    		     
				//Recopila datos de movimientos seleccionados
				$.each($scope.seleccionCuadraturas,function(key,value){  
					
					ProcesosAEjecutar.push(
					{     
						IdCuadraturaDiariaOperaciones	: $scope.seleccionCuadraturas[key].IdCuadraturaDiariaOperaciones
					});
				});	
			}
			$scope.seleccionCuadraturas = undefined;
			console.log(ProcesosAEjecutar);
              
			var datos= {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'ORD',
				p_FechaInicio: util.dateToFormat($scope.data.fechaCuadratura || $scope.SesionFechaSistema ,"YYYY-MM-DD"),
				p_IdProceso: ProcesosAEjecutar,
				p_TipoLlamado: 'Proceso'
			};
			
			var post = r.Cuadratura(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					if(result.P_OUT_ERRORESCONTADOR===0){
						bcsComunes.ventanaMsg({
							   conflictos:util.parseServerResponse(result)
						},idVentana);

					};
					$scope.data.cuadratura = true;
					$scope.consultarResumen();
					bcsComunes.ventanaOverlay(idVentana, false);
				}catch(e){
					bcsComunes.ventanaOverlay(idVentana, false);
				}
					
					
					
			}, function(result){//en caso de falla del servicio.
				bcsComunes.ventanaMsg({
				       conflictos:util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana, false);
			});
			
		}catch(e){
			console.log(e);
			bcsComunes.ventanaOverlay(idVentana, false);
		}
	};
	
	$scope.ConsultaUltimoDetalle = function(vars){
		try{
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);
			
			$scope.limpiarPantalla();
			
			var ProcesosAEjecutar = [];

			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=187";
			var r = $resource("", {},
		    {
				'Cuadratura': { method: "POST", url: urlCuadratura}
			});
			
			//Recopila datos de movimientos seleccionados
			ProcesosAEjecutar.push(
			{     
				IdCuadraturaDiariaOperaciones	: $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones
		    });
			
			var fechaSistema = getFechaSistema();
			var datos= {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'ORD',
				p_FechaInicio: $scope.SesionFechaSistema,
				p_IdProceso: ProcesosAEjecutar,
				p_TipoLlamado: 'Consulta'
			};
			
			var post = r.Cuadratura(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					
					var tra = JSON.parse(result.P_OUT_DETALLE_CUADRATURA);
					
					if ($scope.seleccionCuadraturas[0].TipoOperacion == 'TRX_ASIG'){
						$scope.verGrillaTRX		= true;
						$scope.verGrillaORDEN	= false;
						$scope.verGrillaASIGNACION	= false;
						$scope.verGrillaFACTURA	= false;
						$scope.verGrillaCAVALI	= false;
						buildDetalleCuadratura($scope.tab.gridApiTrxAsig, tra);
					};
					if ($scope.seleccionCuadraturas[0].TipoOperacion == 'TRX_ASMV'){
						$scope.verGrillaTRX		= false;
						$scope.verGrillaORDEN	= false;
						$scope.verGrillaASIGNACION	= true;
						$scope.verGrillaFACTURA	= false;
						$scope.verGrillaCAVALI	= false;
						buildDetalleCuadratura($scope.tab.gridApiAsignacion, tra);
					};
					if ($scope.seleccionCuadraturas[0].TipoOperacion == 'TRX_MVFC'){
						$scope.verGrillaTRX		= false;
						$scope.verGrillaORDEN	= false;
						$scope.verGrillaASIGNACION	= false;
						$scope.verGrillaFACTURA	= true;
						$scope.verGrillaCAVALI	= false;
						buildDetalleCuadratura($scope.tab.gridApiFacturacion, tra);
					};
					if ($scope.seleccionCuadraturas[0].TipoOperacion == 'TRX_CAVA'){
						$scope.verGrillaTRX		= false;
						$scope.verGrillaORDEN	= false;
						$scope.verGrillaASIGNACION	= false;
						$scope.verGrillaFACTURA	= false;
						$scope.verGrillaCAVALI	= true;
						buildDetalleCuadratura($scope.tab.gridApiCavali, tra);
					};					
					
					
					
				
					
					$scope.data.cuadratura = true;
					bcsComunes.ventanaOverlay(idVentana, false);
				}catch(e){
					bcsComunes.ventanaOverlay(idVentana, false);
				}
					
					
					
			}, function(result){//en caso de falla del servicio.
				bcsComunes.ventanaMsg({
				       conflictos:util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana, false);
			});
			
		}catch(e){
			console.log(e);
			bcsComunes.ventanaOverlay(idVentana, false);
		}
	};
	
	$scope.Aprobar = function(){
		var errores = 0;
		$.each($scope.gridApiCuadraturas.dataSource.data(),function(key,value){
			if (value.OperacionesConProblemas !== 0 || value.FechaEjecucion != util.dateInicioDia($scope.SesionFechaSistema)) {
				errores++;
			}
		});
		var texto = 'APROBAR_CUADRATURA_PAUTA'
		if(errores > 0 || $scope.gridApiCuadraturas.dataSource.data().length == 0){
			texto = 'APROBAR_CUADRATURA_ERRORES_PAUTA'
		}
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant(texto));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
		    	bcsComunes.ventanaOverlay($scope.winID,true);
		    	var datos = {
		    		IdEmpresa : $scope.SesionIdEmpresa,
		    		CodigoModulo : 'ORD',
		    		CodigoProceso : 'CUADRATURA_ORDEN',
		    		FechaSistema : $scope.SesionFechaSistema,
		    		CodigoUsuario : $scope.SesionUsuario,
		    	}
		    	bcsServicioPlataforma.AgregarControlProceso(datos, function(result){
		    		if(result.P_OUT_ERRORESCONTADOR == 0){
		    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_EXITOSA"),conflictos:util.parseServerResponse(result)},idVentana);
		    		}else{
		    			bcsComunes.ventanaMsg({mensaje:result.P_OUT_MENSAJE,conflictos:util.parseServerResponse(result)},idVentana);
		    		}
		    	},function(result){
		    		console.log(result);
		    	});
			}
			
		});
	}
	
});