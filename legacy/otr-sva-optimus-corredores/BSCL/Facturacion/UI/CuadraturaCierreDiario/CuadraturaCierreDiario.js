angular.module('ModuloFacturacion').controller('CuadraturaDiariaFACCtrl',
		function($scope,$translate,$modal,$log,$resource,$timeout,
				bcsComunes,bcsAlerta,OPCIONES_BCSALERTA, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.data.resumenVencimiento = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
		
	//Seteo de variables de sistema
	$scope.HabilitarBotonCuadratura = false;
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.cuadratura = false;
	$scope.data.cierre = false;
	var fechaSistema = $scope.SesionFechaSistema;
	$scope.kOptFechaCuadratura = {};
	$scope.data.fechaCuadratura = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
	$scope.kOptFechaCuadratura.max = fechaSistema;
	$scope.tiposCuadraturaOtrosConceptos = ["CUAMOVFACCOM", "CUAMOVFACVEN"];
	$scope.tiposCuadraturaTransacciones = ['CUAMOVFACANCACN','CUAMOVFACANCRF','CUAMOVFACANCSPT','CUAMOVFACANSIMC','CUAMOVFACANSIMV','CUAMOVFACANTPRT','CUAMOVFACANVACN','CUAMOVFACCOMACN','CUAMOVFACCOMACX','CUAMOVFACCOMETN'
		,'CUAMOVFACCOMETX','CUAMOVFACCOMIIF','CUAMOVFACCOMMIL','CUAMOVFACCOMOPA','CUAMOVFACCOMOTC','CUAMOVFACCOMRF','CUAMOVFACCOMSIM','CUAMOVFACCOMSPT','CUAMOVFACDIVACC','CUAMOVFACEMILIB','CUAMOVFACPRESTA','CUAMOVFACSUSOSA','CUAMOVFACVENACN','CUAMOVFACVENEMP','CUAMOVFACVENIIF','CUAMOVFACVENMIL'
		,'CUAMOVFACVENOTC','CUAMOVFACVENPRT','CUAMOVFACVENRF','CUAMOVFACVENSIM','CUAMOVFACVENSPT','CUAMOVFACVENACX','CUAMOVFACVENRV','CUAMOVFACCOMRV','CUAMOVFACCOMRE','CUAMOVFACVENRE','CUAMOVFACCOMOPC','CUAMOVFACCOMOPP','CUAMOVFACVENOPC','CUAMOVFACVENOPP','CUAMOVFACVEPRBO','CUAMOVFACTRANPEN','CUAMOVFACDTENPEN'];
					
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
									IdModuloCuadratura						: {type: "string"},
									IdEmpresa								: {type: "number"},
									IdCuadraturaDiariaOperaciones			: {type: "string"},
									OperacionCuadraturaDiariaOperaciones	: {type: "code"},
									FechaEjecucion							: {type: "sysdate"},
									TotalOperaciones 						: {type: "number"},
									OperacionesConProblemas					: {type: "number"},
									TipoOperacion 							: {type: "string"},
									Resultado 								: {type: "code"}
								}
							}
						}
					}
		}),
		gridCreated: function(grid) {
			$scope.gridApiCuadraturas = grid;
		},
		columns: [
		
					{field : "IdModuloCuadratura",	                    title : "UI_OPERACION", width:"300px", hidden:true, groupable: true},
		          	{field : "OperacionCuadraturaDiariaOperaciones",	title : "UI_OPERACION", width:"300px", groupable: true},
		          	{field : "FechaEjecucion", 							title : 'UI_FECHA', 	width: "90px" , groupable: true},
					{field : "TotalOperaciones",						title : "UI_CANTIDAD_OPERACIONES", width : "100px", groupable: true},
					{field : "OperacionesConProblemas",					title : "UI_DIFERENCIA_OPERACIONES", width : "120px", groupable: true},
					{field : "TipoOperacion",							title : "UI_TIPO_OPERACION", width : "0px", hidden:true,},
					{field : "Resultado", 								title : "UI_RESULTADO",	width : "200px", groupable: true}
				],
		rowColorBackground : function(row) {
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
	
	//CONFIGURACION GRILLA DE MOV COMPROBANTE
	$scope.confBasica = {
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
										NombreLargoCliente				: {type: "string"},
										Descripcion						: {type: "string"},
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.gridApiMovCom = grid;
			},
		columns: [
			{field: "NombreEstadoFacturacion",		title: "UI_ESTADO_FACTURACION",					width: "140px"},
			{field: "IdMovimientoComprobante",		title: "UI_MOVIMIENTO_COMPROBANTE",				width: "140px"},
			{field: "NombreTipoComprobante",		title: "UI_TIPO_COMPROBANTE" ,					width: "240px"},
			{field: "IdConceptoMovimiento",			title: "UI_CONCEPTO", 							width: "100px"},
			{field: "NombreConceptoMovimiento",		title: "UI_CONCEPTO", 							width: "250px"},
			{field: "DocumentoIdentidad",			title: "UI_DOCUMENTO_IDENTIDAD",	 			width: "130px"},
			{field: "TipoDocumentoIdentidad",		title: "UI_TIPO_DOCUMENTO_IDENTIDAD",			width: "150px"},
			{field: "NombreLargoCliente",			title: "UI_NOMBRE_CLIENTE",						width: "250px"},
			{field: "Descripcion",						title: "UI_DESCRIPCION", 			width: "100px", align: "left"}
		],
		allowCopy			: { delimeter: "\t" },
		sortable: true,
		pageable: true,
		filterable: true,
		scrollable: true,
		height: 250,
		selectable 			: "multiple row",
		userSettings:true,
		navigatable: true,
		excel : {allPages : true},
		autoBind : false
	};
	
	//CONFIGURACION GRILLA DE transacciones
	$scope.confBasicaMovimientoTransaccion = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										Descripcion						: {type: "string"},
										IdTransaccion	:{type: "string"},
										FolioTransaccion	:{type: "string"},
										FechaTransaccion	:{type: "string"},
										FechaHoraTransaccion :{type: "datetime"},
										IdClasificacionTipoInstrumento	:{type: "string"},
										IdTipoInstrumento	:{type: "string"},
										NombreBolsa	:{type: "string"},
										TipoOperacion	:{type: "string"},
										IdCondicionLiquidacion	:{type: "string"},
										IdMoneda	:{type: "string"},
										NombreTipoInstrumento	:{type: "string"},
										NemotecnicoInstrumento	:{type: "string"},
										Cantidad	:{type: "string"},
										Precio	:{type: "string"},
										Monto	:{type: "string"},
										CantidadAsignada	:{type: "string"},
										CantidadPorAsignar	:{type: "string"},
										CantidadEnviada	:{type: "string"},
										CantidadPorEnviar	:{type: "string"},
										CantidadFacturada	:{type: "string"},
										CantidadPorFacturar	:{type: "string"},
										MontoFacturado	:{type: "string"},
										MontoPorFacturar	:{type: "string"}
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.gridApiMovimientoTransaccion = grid;
			},
		columns: [
			{field: "Descripcion",						title: "UI_ESTADO",							width: "140px",		template : "<span translate>#: Descripcion # </span>"},
			{field: "IdTransaccion",					title: "UI_TRANSACCION",					width: "140px"},
			{field: "FolioTransaccion",					title: "UI_FOLIO_TRANSACCION",				width: "140px"},
			{field: "FechaHoraTransaccion",				title: "UI_FECHA_TRANSACCION",				width: "140px", 	template : "<span>#: util.fechaHoraSistema(FechaHoraTransaccion,'DD-MM-YYYY HH:mm:ss') # </span>"},
			{field: "IdClasificacionTipoInstrumento",	title: "UI_IDCLASIFICACIONTIPOINSTRUMENTO",	width: "140px", 	hidden: true},
			{field: "IdTipoInstrumento",				title: "UI_IDTIPOINSTRUMENTO",				width: "140px", 	hidden: true},
			{field: "NombreBolsa",						title: "UI_BOLSA",							width: "140px"},
			{field: "TipoOperacion",					title: "UI_TIPO_OPERACION",					width: "140px"},
			{field: "IdCondicionLiquidacion",			title: "UI_CONDICION_LIQUIDACION",			width: "140px", 	hidden: true},
			{field: "IdMoneda",							title: "UI_MONEDA",							width: "140px"},
			{field: "NombreTipoInstrumento",			title: "UI_NOMBRE_TIPO_INSTRUMENTO",		width: "140px", 	hidden: true},
			{field: "NemotecnicoInstrumento",			title: "UI_NEMO",							width: "140px"},
			{field: "Cantidad",							title: "UI_CANTIDAD",						width: "140px",		template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field: "Precio",							title: "UI_PRECIO",							width: "140px",		template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			{field: "Monto",							title: "UI_MONTO",							width: "140px",		template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"},
			{field: "CantidadAsignada",					title: "UI_CANTIDAD_ASIGNADA",				width: "140px",		template : " <label> #:  (util.bcs_formato(CantidadAsignada,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field: "CantidadPorAsignar",				title: "UI_CANTIDAD_POR_ASIGNAR",			width: "140px",		template : " <label> #:  (util.bcs_formato(CantidadPorAsignar,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field: "CantidadEnviada",					title: "UI_CANTIDAD_ENVIADA",				width: "140px",		template : " <label> #:  (util.bcs_formato(CantidadEnviada,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field: "CantidadPorEnviar",				title: "UI_CANTIDAD_POR_ENVIAR",			width: "140px",		template : " <label> #:  (util.bcs_formato(CantidadPorEnviar,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field: "CantidadFacturada",				title: "UI_CANTIDAD_FACTURADA",				width: "140px",		template : " <label> #:  (util.bcs_formato(CantidadFacturada,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field: "CantidadPorFacturar",				title: "UI_CANTIDAD_POR_FACTURARr",			width: "140px",		template : " <label> #:  (util.bcs_formato(CantidadPorFacturar,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field: "MontoFacturado",					title: "UI_MONTO_FACTURADO",				width: "140px",		template : " <label> #:  (util.bcs_formato(MontoFacturado,'GEN','Monto',IdMoneda,'','')).valor #</label>"},
			{field: "MontoPorFacturar",					title: "UI_MONTO_POR_FACTURAR",				width: "140px",		template : " <label> #:  (util.bcs_formato(MontoPorFacturar,'GEN','Monto',IdMoneda,'','')).valor #</label>"},
		],
		allowCopy			: { delimeter: "\t" },
		sortable: true,
		pageable: true,
		filterable: true,
		scrollable: true,
		height: 250,
		selectable 			: "multiple row",
		userSettings:true,
		navigatable: true,
		excel : {allPages : true},
		autoBind : false
	};
		
	
	$scope.consultarResumen = function(){
		try{
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);

			$scope.limpiarPantalla();
			
			var urlConsultarResumen = "/BSCL/Plataforma/GWJAVA/PLA/call?t=28";
			var r = $resource("", {},
		    {
				'ConsultarResumen': { method: "POST", url: urlConsultarResumen}
			});
			
			var datos= {				
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModuloCuadratura: 'FACTURACION',
				p_FechaEjecucion: util.dateToFormat($scope.SesionFechaSistema ,"YYYY-MM-DD")
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
		$scope.gridApiMovCom.dataSource.removeAll();
		$scope.gridApiCuadraturas.dataSource.removeAll();
	};
	
	
	$scope.$watch('seleccionCuadraturas',function(newval,oldval){
	   if ( _.isUndefined(newval) ) return;
	   
	   $scope.HabilitarBotonCuadratura = true;   //Si uno de los movimiento es válido, se activan los botones
	});	


	$scope.Cuadratura = function(){
		try{
			
			$scope.data.cierre = false;
			$scope.data.cuadratura = false;
			bcsComunes.ventanaOverlay(idVentana, true);
			
			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4254";
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
			
			var datos= {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'FMM',
				p_FechaInicio: util.dateToFormat($scope.SesionFechaSistema ,"YYYY-MM-DD"),
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
			$scope.HabilitarBotonCuadratura = true;
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);
			var ProcesosAEjecutar = [];

			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4254";
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
				p_IdModulo: 'FMM',
				p_FechaInicio: $scope.seleccionCuadraturas[0].FechaEjecucion == '0001-01-01'?util.dateToFormat($scope.SesionFechaSistema ,"YYYY-MM-DD"): util.dateToFormat($scope.seleccionCuadraturas[0].FechaEjecucion, "YYYY-MM-DD"),
				p_IdProceso: ProcesosAEjecutar,
				p_TipoLlamado: 'Consulta'
			};
			
			var post = r.Cuadratura(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					
					var tra = JSON.parse(result.P_OUT_DETALLE_CUADRATURA);
					
					if ( $scope.tiposCuadraturaOtrosConceptos.includes($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones) ){
						buildDetalleCuadratura($scope.gridApiMovCom, tra);
					};

					if ( $scope.tiposCuadraturaTransacciones.includes($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones) ){
						buildDetalleCuadratura($scope.gridApiMovimientoTransaccion, tra);
					};
					
					if(result.P_OUT_MENSAJES){
						bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant(result.P_OUT_MENSAJES[0].codigo));
					};
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
		var descuadre = 0;
		var textoDescuadre = "";
		var descuadres =[];

		$.each($scope.gridApiCuadraturas.dataSource.data(),function(key,value){
			if (value.OperacionesConProblemas !== 0 || value.FechaEjecucion != util.dateInicioDia($scope.SesionFechaSistema)) {
				errores++;
			}
			if (value.OperacionesConProblemas !== 0) {
				descuadre++;
				descuadres.push(value.OperacionCuadraturaDiariaOperaciones);
			}
		});
		if (descuadre > 0){
			textoDescuadre = "UI_DESCUADRE";
		}else{
			textoDescuadre = "OK";
		}
		var texto = 'APROBAR_CUADRATURA_PAUTA'
		if(errores > 0 || $scope.gridApiCuadraturas.dataSource.data().length == 0){
			texto = 'APROBAR_CUADRATURA_ERRORES_PAUTA'
		}
		if(descuadres.length > 0 ){
			var alertaDescuadre = bcsAlerta.crear('PREGUNTA',$translate.instant("UI_APROBACION_DESCUADRE_DIARIO"));
			alertaDescuadre.result.then(function(result){
				if(result != OPCIONES_BCSALERTA.ACEPTAR){
					return;
				}else{
					var t = bcsAlerta.crear('PREGUNTA',$translate.instant(texto));
					t.result.then(function(result){
						if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
							bcsComunes.ventanaOverlay($scope.winID,true);
							var datos = {
								IdEmpresa : $scope.SesionIdEmpresa,
								CodigoModulo : 'FAC',
								CodigoProceso : 'CUADRATURA_FACTURACION',
								FechaSistema : fechaSistema,
								CodigoUsuario : $scope.SesionUsuario,
								ResultadoProceso : textoDescuadre,
								p_UsaReglasNegocio: 'S'
							}
							bcsServicioPlataforma.AgregarControlProceso(datos, function(result){
								if(result.P_OUT_ERRORESCONTADOR == 0){
									if(result.P_OUT_MENSAJES.length > 0){
										bcsComunes.ventanaMsg({mensaje:$translate.instant("FACTURAS_DESCUADRATURAS"),conflictos:util.parseServerResponse(result)},idVentana);
									}else{
										bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_EXITOSA"),conflictos:util.parseServerResponse(result)},idVentana);	
									}
								}else{
									bcsComunes.ventanaMsg({mensaje:result.P_OUT_MENSAJE,conflictos:util.parseServerResponse(result)},idVentana);
								}
							},function(result){
								console.log(result);
							});
						}
						
					});
				}
			})
		}else{
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant(texto));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					bcsComunes.ventanaOverlay($scope.winID,true);
					var datos = {
						IdEmpresa : $scope.SesionIdEmpresa,
						CodigoModulo : 'FAC',
						CodigoProceso : 'CUADRATURA_FACTURACION',
						FechaSistema : fechaSistema,
						CodigoUsuario : $scope.SesionUsuario,
						ResultadoProceso : textoDescuadre
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
	}
	
});