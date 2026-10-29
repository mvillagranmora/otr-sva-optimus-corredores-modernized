angular.module('ModuloContabilidad').controller('CuadraturaDiariaCONCtrl',
		function($scope,$translate,$modal,$log,$resource,$timeout,
				bcsComunes,bcsAlerta,OPCIONES_BCSALERTA, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.data.resumenVencimiento = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
		
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.cuadratura = false;
	$scope.data.cierre = false;
	$scope.data.showCentralizaciones = true;
	$scope.data.showComprobantes = false;
	$scope.data.fechaCuadratura = $scope.SesionFechaSistema;
	$scope.kOptFechaCuadratura = {};
	$scope.kOptFechaCuadratura.max = $scope.data.fechaCuadratura;
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	//CONFIGURACION GRILLA DE RESUMEN
	$scope.confBasicaCuadraturas = {
		
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
						transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4251",
								dataType: "json"
							}
						},
						serverSorting : true, 
						sort: { field: "Orden", dir: "asc" },
						pageSize: 200, // Número de registros por página.
						schema : {
							model: {
								fields : {
									IdCuadraturaDiariaOperaciones			: {type: "string"},
									OperacionCuadraturaDiariaOperaciones	: {type: "code"},//{type: "string"},
									FechaEjecucion							: {type: "sysdate"},
									TotalOperaciones 						: {type: "number"},
									OperacionesConProblemas					: {type: "number"},
									TipoOperacion 							: {type: "string"},
									Resultado 								: {type: "code"},//{type: "string"},
									Orden									: {type: "number"},
								}
							}
						}
					}
		}),
		gridCreated: function(grid) {
			$scope.gridApiCuadraturas = grid;
		},
		columns: [
		          	{field : "OperacionCuadraturaDiariaOperaciones",	title : "UI_OPERACION", 				width:"300px",   template: function(value){return $translate.instant(value.OperacionCuadraturaDiariaOperaciones);}},
		          	{field : "FechaEjecucion", 							title : 'UI_FECHA', 					width: "90px" },
					{field : "TotalOperaciones",						title : "UI_CANTIDAD_OPERACIONES",		width : "100px"},
					{field : "OperacionesConProblemas",					title : "UI_DIFERENCIA_OPERACIONES",	width : "120px"},
					{field : "TipoOperacion",							title : "UI_TIPO_OPERACION",			width : "0px", hidden:true},
					{field : "Resultado", 								title : "UI_RESULTADO",					width : "200px", template: function(value){return $translate.instant(value.Resultado);}}
				],
		rowColorBackground : function(row) {
			console.log(row);
			if(row.get("TotalOperaciones")==0 && row.get("OperacionesConProblemas")==0){
				return "yellow";
			}else if(row.get("OperacionesConProblemas")==0){
				return "green";
			}else{
				return "red";
			}
	  	},
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
	
	//CONFIGURACION GRILLA DE CENTRALIZACIONES
	$scope.confBasicaCentralizaciones = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										IdTipoCentralizacion			: {type: "number"},
										IdMovimiento					: {type: "number"},
										IdTipoMovimiento				: {type: "string"},
										IdEmpresa						: {type: "number"},
										Numero							: {type: "string"},
										Linea							: {type: "string"},
										Fecha							: {type: "string"},
										DocumentoIdentidad				: {type: "string"},
										Ejercicio						: {type: "string"},
										Periodo							: {type: "string"},
										Monto							: {type: "number"},
										IdMoneda						: {type: "string"},
										Estado							: {type: "string"},
										Descripcion						: {type: "string"},//{type: "code"},
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.gridApiCen = grid;
			},
		columns: [
		    {field: "IdMovimiento",						title: "UI_ID_MOVIMIENTO", 			width: "100px"},
		    {field: "Fecha",							title: "UI_FECHA", 					width: "125px"},
		    {field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD", 	width: "125px"},
	      	{field: "Ejercicio",						title: "UI_EJERCICIO", 				width: "125px"},
	      	{field: "Periodo",							title: "UI_PERIODO", 				width: "100px"},
	      	{field: "Monto",							title: "UI_MONTO", 					width: "75px",align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda, '','')).valor #</label>"},
	      	{field: "IdMoneda",							title: "UI_MONEDA", 				width: "75px"},
	      	{field: "Descripcion",						title: "UI_DESCRIPCION", 			width: "100px", align: "left", template: function(value){return $translate.instant(value.Descripcion);}}
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
	
	//CONFIGURACION GRILLA DE COMPROBANTES
	$scope.confBasicaComprobantes = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										IdPreComprobante				: {type: "number"},
										NombreEmpresa					: {type: "string"},
										Ejercicio						: {type: "number"},
										FechaContable					: {type: "string"},
										FechaProceso					: {type: "string"},
										HoraProceso						: {type: "string"},
										FolioContabilidad				: {type: "number"},
										CantidadAsientos				: {type: "number"},
										Estado							: {type: "string"},
										NombreEstado					: {type: "string"},
										Glosa							: {type: "string"},
										IdReversa						: {type: "number"},
										Tipo							: {type: "string"},
										CodigoUsuario					: {type: "string"},
										IdTipoContabilidad				: {type: "number"},
										NombreTipoContabilidad			: {type: "string"},
										IdEmpresa						: {type: "number"},
										Debe							: {type: "number"},
										Haber							: {type: "number"},
										IdMoneda						: {type: "string"},
										IdEstadoPrograma				: {type: "string"},
										Descripcion						: {type: "string"},
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.gridApiCom = grid;
			},
		columns: [
		    {field: "IdPreComprobante",					title: "UI_ID_PRECOMPROBANTE", 		width: "100px"},
		    {field: "FechaContable",					title: "UI_FECHA", 					width: "125px"},
		    {field: "FolioContabilidad",				title: "UI_FOLIO", 					width: "125px"},
	      	{field: "Ejercicio",						title: "UI_EJERCICIO", 				width: "125px"},
	      	{field: "CantidadAsientos",					title: "UI_CANTIDAD_ASIENTOS", 		width: "100px"},
	      	{field: "NombreEstado",						title: "UI_ESTADO", 				width: "100px"},
	      	{field: "Debe",								title: "UI_DEBE", 					width: "75px",align: "left" },
	      	{field: "Haber",							title: "UI_HABER", 					width: "75px",align: "left" },
	      	{field: "IdMoneda",							title: "UI_MONEDA", 				width: "75px"},
	      	{field: "Descripcion",						title: "UI_DESCRIPCION", 			width: "100px", align: "left"}
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
	
	$scope.consultarResumen = function(){
		try{
			bcsComunes.ventanaOverlay(idVentana, true);
			$scope.limpiarPantalla();
			$scope.gridApiCuadraturas.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana, false);
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
		$scope.gridApiCen.dataSource.removeAll();
		$scope.gridApiCom.dataSource.removeAll();
		$scope.gridApiCuadraturas.dataSource.removeAll();
	};
	
	$scope.Cuadratura = function () {
		try {
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);
	
			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4252";
			var r = $resource("", {}, {
				'Cuadratura': { method: "POST", url: urlCuadratura }
			});
	
			var ProcesosAEjecutar = [];
			if ($scope.seleccionCuadraturas) {
				ProcesosAEjecutar = $scope.seleccionCuadraturas.map(function (proc) {
					return { IdCuadraturaDiariaOperaciones: proc.IdCuadraturaDiariaOperaciones };
				});
			} else {
				ProcesosAEjecutar = $scope.gridApiCuadraturas.dataSource.data().map(function (proc) {
					return { IdCuadraturaDiariaOperaciones: proc.IdCuadraturaDiariaOperaciones };
				});
			}
	
			$scope.seleccionCuadraturas = undefined;
	
			if (!ProcesosAEjecutar.length) {
				bcsComunes.msg('warning', "Atención", "No hay procesos para ejecutar.");
				bcsComunes.ventanaOverlay(idVentana, false);
				return;
			}
	
			var batchSize = 5;
	
			function ejecutarLote(idx) {
				if (idx >= ProcesosAEjecutar.length) {
					$scope.gridApiCuadraturas.dataSource.read();
	
					$timeout(function () {
						try {
							var datosGrilla = $scope.gridApiCuadraturas.dataSource.data() || [];
							var descuadre = datosGrilla.some(function (d) {
								return d.OperacionesConProblemas > 0;
							});
	
							if (descuadre) {
								bcsComunes.msg('warning', "Atención", "El proceso terminó, pero existen operaciones descuadradas. Revise el detalle.");
							} else {
								bcsComunes.msg('success', "OK", "La cuadratura quedó sin diferencias.");
							}
						} catch (e) {
							bcsComunes.msg('error', "Error", "Ocurrió un error al validar el resultado de la cuadratura.");
						} finally {
							bcsComunes.ventanaOverlay(idVentana, false);
						}
					}, 500);
	
					return;
				}
	
				var batch = ProcesosAEjecutar.slice(idx, idx + batchSize);
				var datos = {
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdModulo: 'CON',
					p_FechaInicio: $scope.SesionFechaSistema,
					p_IdProceso: batch,
					p_TipoLlamado: 'Proceso'
				};
	
				r.Cuadratura(datos).$promise.then(function (result) {
					try {
						if (result && result.P_OUT_MENSAJES) {
							delete result.P_OUT_MENSAJES;
						}
	
						ejecutarLote(idx + batchSize);
	
					} catch (e) {
						bcsComunes.ventanaOverlay(idVentana, false);
					}
				}, function (err) {
					try {
						var detalleStr = result.P_OUT_DETALLE_CUADRATURA;
						if (typeof detalleStr === "string") {
							detalleStr = detalleStr.trim();
							if (detalleStr.startsWith("{") || detalleStr.startsWith("[")) {
								var tra = JSON.parse(detalleStr);
								buildDetalleCuadratura($scope.gridApiCen, tra);
							} else {
								throw new Error("Respuesta no es JSON válido: " + detalleStr);
							}
						}
					} catch (parseError) {
						console.error("Error parseando JSON del backend:", parseError);
						bcsComunes.msg("error", "Error", "El servidor devolvió datos inválidos. Contactar a soporte.");
						bcsComunes.ventanaOverlay(idVentana, false);
					}
				});
			}
	
			ejecutarLote(0);
	
		} catch (e) {
			bcsComunes.ventanaOverlay(idVentana, false);
		}
	};			
		
	
	$scope.ConsultaUltimoDetalle = function(vars){
		try{
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);
			var ProcesosAEjecutar = [];
			if ($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones !== 'CUACOMCON'){
				$scope.data.showComprobantes = false;
				$scope.data.showCentralizaciones = true;
				$scope.gridApiCen.dataSource.removeAll();
			}
			if($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones === 'CUACOMCON'){
				$scope.data.showCentralizaciones = false;
				$scope.data.showComprobantes = true;
				$scope.gridApiCom.dataSource.removeAll();
			}
			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4252";
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
				p_IdModulo: 'CON',
				p_FechaInicio: $scope.SesionFechaSistema,
				p_IdProceso: ProcesosAEjecutar,
				p_TipoLlamado: 'Consulta'
			};
			
			var post = r.Cuadratura(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					
					var tra = JSON.parse(result.P_OUT_DETALLE_CUADRATURA);
					
					if ($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones !== 'CUACOMCON'){
						buildDetalleCuadratura($scope.gridApiCen, tra);
					}
					
					if($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones === 'CUACOMCON'){
						buildDetalleCuadratura($scope.gridApiCom, tra);
					}
					
					if(result.P_OUT_MENSAJES && result.P_OUT_MENSAJES.length > 0){
						  var countErrores = result.P_OUT_MENSAJES.filter((mensaje) => mensaje.tipo == "ERROR").length;
						  if(countErrores == 0){
							bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant(result.P_OUT_MENSAJES[0].codigo));
							}
						};
					bcsComunes.ventanaOverlay(idVentana, false);
				}catch(e){
					bcsComunes.ventanaOverlay(idVentana, false);
				}
					
					
					
			}, function(result){//en caso de falla del servicio.
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), 'ERROR: No se pudo completar la cuadratura.');
				bcsComunes.ventanaOverlay(idVentana, false);
			});
			
		}catch(e){
			console.log(e);
			bcsComunes.ventanaOverlay(idVentana, false);
		}
	};
	
	$scope.exportarExcel = function(){
		if($scope.data.showCentralizaciones) $scope.exportarExcelCentralizaciones();
		if($scope.data.showComprobantes) $scope.exportarExcelComprobantes();
	}
	
	$scope.Aprobar = function(){
		var errores = 0;
		var descuadre = 0;
		var textoDescuadre = "";

		$.each($scope.gridApiCuadraturas.dataSource.data(),function(key,value){
			if (value.OperacionesConProblemas !== 0 || value.FechaEjecucion != util.dateInicioDia($scope.SesionFechaSistema)) {
				errores++;
			}
			if (value.OperacionesConProblemas !== 0) {
				descuadre++;
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


		if(descuadre > 0 ){
			var alertaDescuadre = bcsAlerta.crear('PREGUNTA',$translate.instant("UI_APROBACION_DESCUADRE_DIARIO"));
			alertaDescuadre.result.then(function(result){
				if(result != OPCIONES_BCSALERTA.ACEPTAR){
					return;
				}else{
					$scope.AgregarControlProceso(texto);
				}
			})
		}else{
			$scope.AgregarControlProceso(texto);
		}
	}

	$scope.AgregarControlProceso = function(texto, textoDescuadre){
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant(texto));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				bcsComunes.ventanaOverlay($scope.winID,true);
				var datos = {
					IdEmpresa : $scope.SesionIdEmpresa,
					CodigoModulo : 'CON',
					CodigoProceso : 'CUADRATURA_CONTABILIDAD',
					FechaSistema : $scope.SesionFechaSistema,
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
	
});