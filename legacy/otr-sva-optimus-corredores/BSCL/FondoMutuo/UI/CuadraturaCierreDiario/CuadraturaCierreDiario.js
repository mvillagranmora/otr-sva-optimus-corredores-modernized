angular.module('ModuloFFM').controller('CuadraturaDiariaFFMMCtrl',
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
	$scope.data.fechaCuadratura = $scope.SesionFechaSistema;
	$scope.kOptFechaCuadratura = {};
	$scope.kOptFechaCuadratura.max = $scope.data.fechaCuadratura;
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	//CONFIGURACION GRILLA DE RESUMEN
	$scope.confBasicaCuadraturas = {
		
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging     : true, // Paginado de los datos en el servidor.
				serverSorting    : true, // Ordenamiento de los datos en el servidor.
						transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4240",
								dataType: "json"
							}
						},
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
	
	//CONFIGURACION GRILLA DE SOLLICITUDES
	$scope.confBasica = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										IdSolicitudAporteRescate		: {type: "number"},
										FechaAceptacion					: {type: "sysdate"},
										FechaCertificacion				: {type: "sysdate"},
										FechaLiquidacion				: {type: "sysdate"},
										IdSerieFondo					: {type: "string"},
										NumeroCuotas					: {type: "number"},
										Monto							: {type: "number"},
										MontoComision					: {type: "number"},
										IdMonedaFondo					: {type: "string"},
										DescripcionTipoSolicitud		: {type: "string"},
										DecimalesCuota					: {type: "number"},
										Descripcion						: {type: "string"},
									}
								}
							}
						}
			}),
			gridCreated: function(grid) {
				$scope.gridApiCert = grid;
			},
		columns: [
		    {field: "IdSolicitudAporteRescate",			title: "UI_SECUENCIA", 				width: "100px"},
		    {field: "FechaAceptacion",					title: "UI_FECHA_CURSE", 			width: "125px"},
		    {field: "FechaCertificacion",				title: "UI_FECHA_CERTIFICACION", 	width: "125px"},
	      	{field: "FechaLiquidacion",					title: "UI_FECHA_LIQUIDACION", 		width: "125px"},
	      	{field: "IdSerieFondo",						title: "UI_INSTRUMENTO", 			width: "100px"},
	      	{field: "NumeroCuotas",						title: "UI_CANTIDAD", 				width: "75px",align: "left" , template : " <label> #:   util.formateaNumero(NumeroCuotas, DecimalesCuota) #</label>"},
	      	{field: "Monto",							title: "UI_MONTO", 					width: "75px",align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
	      	{field: "MontoComision",					title: "UI_MONTO_COMISION", 		width: "125px",align:"left" , template : " <label> #:  (util.bcs_formato(MontoComision,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
	      	{field: "IdMonedaFondo",					title: "UI_MONEDA", 				width: "75px"},
	      	{field: "DescripcionTipoSolicitud",			title: "UI_TIPO_SOLICITUD", 		width: "100px", align: "left"},
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
	
	$scope.consultarResumen = function(){
		try{

			var filtros = new Array();
			var filtroOperaciones = new Array();
			bcsComunes.ventanaOverlay(idVentana, true);
			$scope.limpiarPantalla();
			// inicializa los filtros
			filtroOperaciones.push({ field: "FechaEjecucion", operator: "eq", value: $scope.data.fechaCuadratura });

			filtros = {
				logic: "and",
				filters: filtroOperaciones
			}
			
			//$scope.gridApiCuadraturas.dataSource.filter(filtros);
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
		$scope.gridApiCert.dataSource.removeAll();
		$scope.gridApiCuadraturas.dataSource.removeAll();
	};
	
	
	$scope.Cuadratura = function(){
		try{
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);

			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4241";
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
				p_FechaInicio: $scope.SesionFechaSistema,				
				p_IdProceso: ProcesosAEjecutar,
				p_TipoLlamado: 'Proceso'
			};
			
			var post = r.Cuadratura(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					if(result.P_OUT_MENSAJES){
						bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant(result.P_OUT_MENSAJES[0].codigo));
					};
					$scope.gridApiCuadraturas.dataSource.read();
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
			var ProcesosAEjecutar = [];

			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4241";
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
				p_FechaInicio: $scope.SesionFechaSistema,
				p_IdProceso: ProcesosAEjecutar,
				p_TipoLlamado: 'Consulta'
			};
			
			var post = r.Cuadratura(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					
					var tra = JSON.parse(result.P_OUT_DETALLE_CUADRATURA);
					
					if ($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAMOVCERT'){
						buildDetalleCuadratura($scope.gridApiCert, tra);
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
								CodigoModulo : 'FMM',
								CodigoProceso : 'CUADRATURA_FFMM',
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
			})
		}else{
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant(texto));
					t.result.then(function(result){
						if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
							bcsComunes.ventanaOverlay($scope.winID,true);
							var datos = {
								IdEmpresa : $scope.SesionIdEmpresa,
								CodigoModulo : 'FMM',
								CodigoProceso : 'CUADRATURA_FFMM',
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
	}
	
});