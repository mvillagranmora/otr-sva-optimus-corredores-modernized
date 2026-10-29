angular.module('ModuloMesa').controller('CuadraturaDiariaFWDCtrl',
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
	var fechaSistema = $scope.SesionFechaSistema;
	$scope.kOptFechaCuadratura = {};
	$scope.data.fechaCuadratura = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	$scope.HabilitarBotonCuadratura = false;
	$scope.HabilitarBotonAprobar = false;
	
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
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4263",
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
		          	{field : "OperacionCuadraturaDiariaOperaciones",	title : "UI_OPERACION", 				width:"300px", groupable: true,   template: function(value){ return $translate.instant(value.OperacionCuadraturaDiariaOperaciones);}},
		          	{field : "FechaEjecucion", 							title : 'UI_FECHA', 					width: "90px" , groupable: true},
					{field : "TotalOperaciones",						title : "UI_CANTIDAD_OPERACIONES",		width : "100px", groupable: true},
					{field : "OperacionesConProblemas",					title : "UI_DIFERENCIA_OPERACIONES",	width : "120px", groupable: true},
					{field : "TipoOperacion",							title : "UI_TIPO_OPERACION",			width : "0px", hidden:true},
					{field : "Resultado", 								title : "UI_RESULTADO",					width : "200px", groupable: true, template: function(value){return $translate.instant(value.Resultado);}}
				],
		rowColorBackground : function(row) {
			console.log(row);
			if(row.get("TotalOperaciones")==0 && row.get("OperacionesConProblemas")==0){
				return "black";
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
	
	//CONFIGURACION GRILLA
	$scope.confBasica = {
			dataSource: new bcs.DataSource({
				type : "array", //Los tipos posibles son: odata, call, array y object
				definition : {
							pageSize: 200, // Número de registros por página.
							schema : {
								model: {
									fields : {
										IdOperacion						: {type: "number"},
										FolioCartera					: {type: "number"},
										RazonSocial						: {type: "string"},
										MontoMonedaPrincipal			: {type: "number"},
										MontoMonedaSecundaria			: {type: "number"},
										RazonableActivo					: {type: "number"},
										RazonablePasivo					: {type: "number"},
										IdMonedaPrincipal				: {type: "string"},
										IdMonedaSecundaria				: {type: "string"},
										IdMesa							: {type: "string"},
										IdEstado						: {type: "string"},
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
		    {field: "IdOperacion",						title: "UI_SECUENCIA", 				width: "100px"},
		    {field: "FolioCartera",						title: "UI_FOLIO", 					width: "125px"},
		    {field: "RazonSocial",						title: "UI_NOMBRE", 				width: "125px"},
		    {field: "MontoMonedaPrincipal",				title: "UI_MONTO_PRINCIPAL", 		width: "75px",align: "left" , template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,'MES','Monto',IdMonedaPrincipal, '','')).valor #</label>"},
	      	{field: "MontoMonedaSecundaria",			title: "UI_MONTO_SECUNDARIO", 		width: "125px",align:"left" , template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,'MES','Monto',IdMonedaSecundaria, '','')).valor #</label>"},
	      	{field: "RazonableActivo",					title: "UI_VALOR_RAZONABLE_ACTIVO", width: "75px",align: "left" , template : " <label> #:  (util.bcs_formato(RazonableActivo,'MES','Monto','CLP', '','')).valor #</label>"},
	      	{field: "RazonablePasivo",					title: "UI_VALOR_RAZONABLE_PASIVO", width: "125px",align:"left" , template : " <label> #:  (util.bcs_formato(RazonablePasivo,'MES','Monto','CLP', '','')).valor #</label>"},
	      	{field: "IdMonedaPrincipal",				title: "UI_MONEDA_PRINCIPAL", 		width: "75px"},
	      	{field: "IdMonedaSecundaria",				title: "UI_MONEDA_SECUNDARIA", 		width: "75px"},
	      	{field: "IdMesa",							title: "UI_MESA", 					width: "100px", align: "left"},
	      	{field: "Descripcion",						title: "UI_DESCRIPCION", 			width: "100px", align: "left", template: function(value){ return $translate.instant(value.Descripcion);}}
			
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
			$scope.HabilitarBotonCuadratura = false;
			$scope.HabilitarBotonAprobar = true;
			bcsComunes.ventanaOverlay(idVentana, true);
			$scope.limpiarPantalla();
			var filtros = new Array();
			var filtroOperaciones = new Array();
			// inicializa los filtros
			filtroOperaciones.push({ field: "FechaEjecucion", operator: "eq", value: $scope.data.fechaCuadratura });

			filtros = {
				logic: "and",
				filters: filtroOperaciones
			}
			
			$scope.gridApiCuadraturas.dataSource.read();
			//$scope.gridApiCuadraturas.dataSource.filter(filtros);
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

			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4266";
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
				p_IdModulo: 'MES',
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
				$scope.HabilitarBotonCuadratura = true;
			});
			
		}catch(e){
			console.log(e);
			bcsComunes.ventanaOverlay(idVentana, false);
		}
		$scope.HabilitarBotonCuadratura = false;
	};
	
	$scope.ConsultaUltimoDetalle = function(vars){
		try{
			$scope.HabilitarBotonCuadratura = true;
			$scope.data.cuadratura = false;
			$scope.data.cierre = false;
			bcsComunes.ventanaOverlay(idVentana, true);
			var ProcesosAEjecutar = [];

			var urlCuadratura = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4266";
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
				p_IdModulo: 'MES',
				p_FechaInicio: $scope.SesionFechaSistema,
				p_IdProceso: ProcesosAEjecutar,
				p_TipoLlamado: 'Consulta'
			};
			
			var post = r.Cuadratura(datos);
			post.$promise.then(function(result){
				try{
					$scope.data.operacionesConError = 0;
					
					var tra = JSON.parse(result.P_OUT_DETALLE_CUADRATURA);
					
					if ($scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAMOVPEN' 
						|| $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAMOVMODCON'
						|| $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAMOVMODFISSPT'
						|| $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAVERVALRAZ'
						|| $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAMOVMODFIS'
						|| $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAENVTESCTA'
						|| $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUAENVTESSINCTA'
						|| $scope.seleccionCuadraturas[0].IdCuadraturaDiariaOperaciones == 'CUADATOSCEN'){
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
								CodigoModulo : 'MFW',
								CodigoProceso : 'CUADRATURA_MFW',
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
						CodigoModulo : 'MFW',
						CodigoProceso : 'CUADRATURA_MFW',
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