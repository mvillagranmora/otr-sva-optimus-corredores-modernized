angular.module('ModuloPlataforma').controller('ModalAgregarValorCobroDetallePeruCtrl',function ($scope,bcsComunes,$translate,bcsServiciosPlataformaComercialPeru,$q) {
		
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var params = bcsComunes.ventanaParams($scope.idVentana);
	$scope.IdPerfilCobros = params.IdPerfilCobros;
	$scope.modo = params.modo;
	
	var promiseGridOperaciones = $q.defer();
	var promiseGridCanales = $q.defer();
	var promiseGridBolsas = $q.defer();
	var promiseGridCobros = $q.defer();

	$scope.editarCobro = function() {
		if ($scope.cobrosSeleccionados && $scope.cobrosSeleccionados.length === 1) {
			var p = bcsComunes.ventanaAbrir("IngresoCobro",{ DefinicionCobro : $scope.cobrosSeleccionados[0] },false,false,$scope.idVentana);
			bcsComunes.ventanaPromesa(p).then(function(result){
				$scope.apiGridCobros.dataSource.read();
			});
		}
	}
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData($scope.idVentana,false);
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.aceptar = function() {
		
		var operaciones = $scope.operacionesSeleccionadas;
		var canales = $scope.canalesSeleccionados;
		var bolsas = $scope.bolsasSeleccionadas;
		var cobros = $scope.cobrosSeleccionados;
		
		var detalles = [];

		$.each(operaciones,function(key1,operacion){
			$.each(canales,function(key2,canal){
				$.each(bolsas,function(key3,bolsa){
					$.each(cobros,function(key4,cobro){
						detalles.push({
							IdTipoProducto 			: operacion.IdTipoProducto,
							IdTipoOperacion 			: operacion.IdTipoOperacion,
							IdTipoCanalIngreso		: canal.IdTipoCanalIngreso,
							IdBolsa					: bolsa.IdBolsa,
							CodigoDefinicionCobro	: cobro.CodigoDefinicionCobro
						});
					})
				})
			})
		})
		
		if (detalles.length === 0) {
			bcsComunes.msg("error","error","UI_DEBE_SELECCIONAR_DATOS");
			return;
		}

		var datos = {
			p_IdEmpresa 			: $scope.SesionIdEmpresa,
			p_IdUsuario 			: $scope.SesionIdUsuario,
			p_IdModulo				: "PLA",
			p_IdPerfilCobros		: $scope.IdPerfilCobros,
			p_DetallesPerfilCobro	: detalles
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		if ($scope.modo === 'PE'){ //edicion portafolio extra
			var p = bcsComunes.ventanaAbrir("modalEditarCobrosPeru",{IdPerfilCobros: $scope.IdPerfilCobros, DefinicionOperacion: operaciones, DefinicionCanal: canales, DefinicionBolsa: bolsas, DefinicionCobro : cobros},false,false,$scope.idVentana);
			bcsComunes.ventanaPromesa(p).then(function(result){
				$scope.apiGridCobros.dataSource.read();
				bcsComunes.ventanaCerrar($scope.idVentana);
			});
		}
		else {
		
			bcsServiciosPlataformaComercialPeru.crearDetallePerfilCobros(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				var id = bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(result)
				},$scope.idVentana)
				bcsComunes.ventanaPromesa(id).then(function(){
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.ventanaCerrar($scope.idVentana);
					}
				})
			},function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(result)
				},$scope.idVentana)
			});
		}
	}
	
	$scope.gridOperaciones = {
			dataSource		: new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering : true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3551",
							dataType: "json"
						}
					},
					filter : [{ field : "AccionOrden", operator : "neq", value : null },{ field : "AccionOrden", operator : "neq", value : "" }],
					schema: {
						model: { 
							fields: {		 	
								IdTipoOperacion 		: { type: "string"},
								NombreTipoProducto 		: { type: "string"},
								NombreTipoOperacion		: { type: "string"},
								IdTipoProducto 		: { type: "string"},
							} 
						}
					},
					group: [
				        {field: "NombreTipoProducto"}
				        ]
					
				}
			}),
			columns			: [
				{field: "NombreTipoProducto",		width: "120px",title: "UI_TIPO_PRODUCTO", hidden : true},
				{field: "NombreTipoOperacion",		title: "UI_TIPO_OPERACION"},
			],		
			sortable			: false,
			groupable			: false,
			autoBind			: true, 
			height				: 200,
			userSettings		: false,
			navigatable			: true,
			gridCreated			: function(grid) {
				$scope.apiGridOperaciones = grid;
				promiseGridOperaciones.resolve();			
			},
			excel : {
				allPages: true,
			},
		    selectable 			: "multiple,row"
		 };
	
	
	$scope.gridCanales = {
			dataSource		: new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering : true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3552",
							dataType: "json"
						}
					},
					schema: {
						model: { 
							fields: {		 	
								IdTipoCanalIngreso	: { type: "string"},
								NombreTipoCanalIngreso 	: { type: "string"},
							} 
						}
					}
				}
			}),
			columns			: [
				{field: "NombreTipoCanalIngreso", title: $translate.instant("UI_CANAL")},
			],		
			sortable			: false,
			groupable			: false,
			autoBind			: true, 
			height				: 200,
			userSettings		: false,
			navigatable			: true,
			gridCreated			: function(grid) {
				$scope.apiGridCanales = grid;
				promiseGridCanales.resolve();
			},
			excel : {
				allPages: true,
			},
		    selectable 			: "multiple,row"
		 };
	
	$scope.gridBolsas = {
			dataSource		: new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering : true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3553",
							dataType: "json"
						}
					},
					schema: {
						model: { 
							fields: {		 	
								IdBolsa		: { type: "string"},
								NombreBolsa : { type: "string"},
								NombrePais : { type: "string"}
							} 
						}
					}
				}
			}),
			columns			: [
				{field: "NombreBolsa",		width: "200px",title: "UI_BOLSA"},
				{field: "NombrePais",		width: "175px",title: "UI_PAIS"},
			],		
			sortable			: false,
			groupable			: false,
			autoBind			: true, 
			height				: 200,
			userSettings		: false,
			navigatable			: true,
			gridCreated			: function(grid) {
				$scope.apiGridBolsas = grid;
				promiseGridBolsas.resolve();
			},
			excel : {
				allPages: true,
			},
		    selectable 			: "multiple,row"
		 };
	
	var filtroBaseCobros = {
		logic : "and",
		filters : [
			{ field : "Otros_CheckAplicaPortafolio" , operator : "eq", value : "S"},
			{ field : "IdEmpresa" , operator : "eq", value : $scope.SesionIdEmpresa},
			{ field : "IdEstado" , operator : "eq", value : "ACT"},
		]
	};

	$scope.gridCobros = {
			dataSource		: new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering : true,
					filter : filtroBaseCobros,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3530",
							dataType: "json"
						}
					},
					schema: {
						model: { 
							fields: {		 	
								CodigoDefinicionCobro		: {type : "string"},
								IdClaseCobro	 			: {type : "string"},
								NombreClaseCobro 			: {type : "string"},
								IdTipoCobro 				: {type : "string"},
								NombreTipoCobro 			: {type : "string"},
								MontoFijo_MontoCobro		: {type : "number"},
								PorcentualFijo_ValorCobro	: {type : "number"},
								ValorCobroCombinado 	: {type : "number"},
								IdMonedaCobroUI		 		: {type : "string"},
								ValorCobroMonedaPorcentaje 	: {type : "string"},
								UsoMonedaFormato 	: {type : "string"}
							} 
						}
					}
				}
			}),
			columns			: [
				{field: "NombreClaseCobro", 			title: 'UI_CLASE_COBRO',width: "100px"},
				{field: "CodigoDefinicionCobro", 		title: 'UI_CODIGO',		width: "100px"},
				{field: "NombreTipoCobro", 				title: 'UI_TIPO_COBRO',	width: "200px"},
				{field: "ValorCobroCombinado",			width: "100px",title: $translate.instant("UI_VALOR"),template : " <label> #:  (util.bcs_formato(ValorCobroCombinado,'GEN',UsoMonedaFormato,IdMonedaCobroUI, '','')).valor #</label>"},
				{field: "ValorCobroMonedaPorcentaje",	width: "100px",title: $translate.instant("UI_MONEDA_PORCENTAJE")},

			],		
			sortable			: false,
			groupable			: false,
			filterable 			: true,
			autoBind			: false, 
			height				: 200,
			userSettings		: false,
			navigatable			: true,
			gridCreated			: function(grid) {
				$scope.apiGridCobros = grid;
				promiseGridCobros.resolve();
			},
			excel : {
				allPages: true,
			},
		    selectable 			: "multiple,row"
		 };
		
	$q.all([promiseGridCobros.promise,promiseGridOperaciones.promise,promiseGridCanales.promise,promiseGridBolsas.promise]).then(function(result){
	 	
		$scope.$watch('operacionesSeleccionadas',function(newval,oldval){
			if (newval !== undefined && oldval !== undefined && JSON.stringify(newval) === JSON.stringify(oldval).toString()) {
				return;
			}
			else {
				
				var productos = _.groupBy(newval, function(ope){ return ope.IdTipoProducto });
				if (Object.keys(productos).length > 1) {
					var filtrosCobros1 = filtroBaseCobros;
					var filtrosCobros2 = {};
					filtrosCobros2.logic = "or";
					filtrosCobros2.filters = [ 
						{field : "IdTipoProducto",operator : "eq", value : null}
					];
					var filtrosCobros = {
						logic	: "and",
						filters	: [filtrosCobros1,filtrosCobros2]
					};
					$scope.apiGridCobros.dataSource.filter(filtrosCobros);
				}
				else {
					var filtrosCobros1 = filtroBaseCobros;
					var filtrosCobros2 = {};
					filtrosCobros2.logic = "or";
					filtrosCobros2.filters = [ 
						{field : "IdTipoProducto",operator : "eq", value : null}
					];
					
					if (Object.keys(productos)[0] !== undefined) {
						filtrosCobros2.filters.push({field : "IdTipoProducto",operator : "eq", value : Object.keys(productos)[0]});
					}
					else {
						filtrosCobros2.filters.push({field : "IdTipoProducto",operator : "neq", value : null});
					}
					
					var filtrosCobros = {
						logic	: "and",
						filters	: [filtrosCobros1,filtrosCobros2]
					};
					$scope.apiGridCobros.dataSource.filter(filtrosCobros);
				}


			}
		})

	

	 	
	 });
		
});