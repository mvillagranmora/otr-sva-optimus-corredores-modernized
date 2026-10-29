angular.module('ModuloPlataforma').controller('ModalEditarCobroPeruCtrl',function ($scope,bcsComunes,$translate,bcsServiciosPlataformaComercialPeru,$q) {
		
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.data = {};
	var params = bcsComunes.ventanaParams($scope.idVentana);
	$scope.data.IdPerfilCobros = params.IdPerfilCobros;
	$scope.data.OperacionEditar = params.DefinicionOperacion;
	$scope.data.CanalEditar = params.DefinicionCanal;
	$scope.data.BolsaEditar = params.DefinicionBolsa;
	$scope.data.CobrosEditar = params.DefinicionCobro;
	
	var promiseGridCobros = $q.defer();

	
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData($scope.idVentana,false);
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.aceptar = function() {
		var detalles = [];
		var editaCobro = false;
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		/************************************/
		
		$.each($scope.data.OperacionEditar,function(key1,operacion){
			$.each($scope.data.CanalEditar,function(key2,canal){
				$.each($scope.data.BolsaEditar,function(key3,bolsa){
					$.each($scope.data.apiGridCobros.dataSource.data(),function(key4,cobro){
						detalles.push({
							IdTipoProducto 			: operacion.IdTipoProducto,
							IdTipoOperacion 		: operacion.IdTipoOperacion,
							IdTipoCanalIngreso		: canal.IdTipoCanalIngreso,
							IdBolsa					: bolsa.IdBolsa,
							CodigoDefinicionCobro	: cobro.CodigoDefinicionCobro,
							ValorCobro				: cobro.ValorCobroCombinado,
						});
					})
				})
			})
		})
		if (detalles.length === 0){
			editaCobro = true;
			$.each($scope.data.apiGridCobros.dataSource.data(),function(key4,cobro){
				detalles.push({
					/*IdTipoProducto 			: cobro.IdTipoProducto,
					IdTipoOperacion 		: cobro.IdTipoOperacion,
					IdTipoCanalIngreso		: cobro.IdTipoCanalIngreso,
					IdBolsa					: cobro.IdBolsa,
					CodigoDefinicionCobro	: cobro.CodigoDefinicionCobro,
					ValorCobro				: cobro.ValorCobroCombinado,*/
					IdDetallePerfilCobros	:cobro.IdDetallePerfilCobros,
					CodigoDefinicionCobro	:cobro.CodigoDefinicionCobro + '-' + $scope.data.IdPerfilCobros + '_' + cobro.IdDetallePerfilCobros,//  CAVALI-EXT_20262_580
					IdTipoCobro				:cobro.IdTipoCobro,
					ValorCobro				:cobro.ValorCobroCombinado,
				});
			})
		}
		var datos = {
			p_IdEmpresa 			: $scope.SesionIdEmpresa,
			p_IdUsuario 			: $scope.SesionIdUsuario,
			p_IdModulo				: "PLA",
			p_IdPerfilCobros		: $scope.data.IdPerfilCobros,
			p_DetallesPerfilCobro	: detalles
		};
		/**********************************/
		if (editaCobro){
			//datos.p_DetallesPerfilCobro = detalles;
			bcsServiciosPlataformaComercialPeru.editarDefinicionCobros(datos).$promise.then(function(result){
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
		else {
			//datos.p_DetallesPerfilCobro = detalles;
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
	
	$scope.cargaInicial = function() {
		if ($scope.data.CobrosEditar.length > 0){
			$scope.data.apiGridCobros.dataSource.data($scope.data.CobrosEditar,false);
			$scope.data.apiGridCobros.dataSource.sync();
		}
	};
	$scope.data.gridCobros = {
		dataSource		: new bcs.DataSource({
			type: "array",
			definition : {
				data : [],
				schema: {
					model: { 
						fields: {		 	
							Descripcion					: {type : "string"},
							CheckCobroMonedaOperacion	: {type : "string"},
							CodigoDefinicionCobro		: {type : "string"},
							Descripcion					: {type : "string"},
							IdClaseCobro				: {type : "string"},
							IdEmpresa					: {type : "number"},
							IdEstado					: {type : "string"},
							IdMonedaCobroOtraMoneda		: {type : "string"},
							IdMonedaCobroUI				: {type : "string"},
							IdTipoCobro					: {type : "string"},
							IdTipoProducto				: {type : "string"},
							MontoFijo_CheckMontoOperacionMenor	: {type : "string"},
							MontoFijo_CheckSiNo			: {type : "string"},
							MontoFijo_MontoCobro		: {type : "number"},
							MontoFijo_ValorMontoOperacionMenor	: {type : "number"},
							MontoFijo_ValorMontoSiNo	: {type : "number"},
							NombreClaseCobro			: {type : "string"},
							NombreTipoCobro				: {type : "string"},
							NombreTipoProducto			: {type : "string"},
							Otros_CheckAfectoImpuesto	: {type : "string"},
							Otros_CheckAllIn			: {type : "string"},
							Otros_CheckAplicaCorresponsal	: {type : "string"},
							Otros_CheckAplicaPortafolio	: {type : "string"},
							Otros_CheckBaseTasa			: {type : "string"},
							Otros_CheckInteresCompuesto	: {type : "string"},
							Otros_CheckInteresSimple	: {type : "string"},
							Otros_CheckOperacionContado	: {type : "string"},
							Otros_CheckOperacionPlazo	: {type : "string"},
							Otros_CheckPermiteAumentarValor	: {type : "string"},
							Otros_CheckPermiteDisminuirValor: {type : "string"},
							Otros_IncluyeExcluyeClientesCheck	: {type : "string"},
							Otros_IncluyeExcluyeClientesValor	: {type : "string"},
							Otros_IncluyeExcluyeCorresponsalesCheck	: {type : "string"},
							Otros_IncluyeExcluyeCorresponsalesValor	: {type : "string"},
							Otros_IncluyeExcluyeEmisoresCheck	: {type : "string"},
							Otros_IncluyeExcluyeEmisoresValor	: {type : "string"},
							Otros_IncluyeExcluyeInstrumentosCheck	: {type : "string"},
							Otros_IncluyeExcluyeInstrumentosValor	: {type : "string"},
							Otros_IncluyeExcluyeTiposDeClienteCheck	: {type : "string"},
							Otros_IncluyeExcluyeTiposDeClienteValor	: {type : "string"},
							Otros_ValorAllInMontoOperacionBase	: {type : "number"},
							Otros_ValorBaseTasa			: {type : "number"},
							PorcentualFijo_CheckMayorEntreDos	: {type : "string"},
							PorcentualFijo_CheckMenorEntreDos	: {type : "string"},
							PorcentualFijo_CheckMontoMaximo		: {type : "string"},
							PorcentualFijo_CheckMontoMinimo		: {type : "string"},
							PorcentualFijo_CheckSobreCantidad	: {type : "string"},
							PorcentualFijo_CheckSobreMonto		: {type : "string"},
							PorcentualFijo_IdMonedaMinimoMaximo	: {type : "string"},
							PorcentualFijo_ValorCobro			: {type : "number"},
							PorcentualFijo_ValorMontoMaximo		: {type : "number"},
							PorcentualFijo_ValorMontoMinimo		: {type : "number"},
							PorcentualRango_IdMonedaRangoMonto	: {type : "string"},
							PorcentualRango_IdTipoRangoCobro	: {type : "string"},
							PorcentualTasaPlazo_ValorCobro		: {type : "number"},
							UsoMonedaFormato			: {type : "string"},
							ValorCobroCombinado			: {type : "number"},
							ValorCobroMonedaPorcentaje	: {type : "string"},

							IdBolsa							: {type : "string"},
							IdTipoOperacion					: {type : "string"},
							IdTipoCanalIngreso			: {type : "string"},
							IdDetallePerfilCobros		: {type:"number"},
						} 
					}
				}
			}
		}),
		columns			: [
			{field: "NombreClaseCobro", 			title: 'UI_CLASE_COBRO',width: "100px"},
			{field: "CodigoDefinicionCobro", 		title: 'UI_CODIGO',		width: "100px"},
			{field: "NombreTipoCobro", 				title: 'UI_TIPO_COBRO',	width: "200px"},
			{field: "ValorCobroCombinado",			width: "100px",title: $translate.instant("UI_VALOR"),template : " <label> #:  (util.bcs_formato(ValorCobroCombinado,'GEN',UsoMonedaFormato,IdMonedaCobroUI, '','')).valor #</label>", editable: true},
			{field: "ValorCobroMonedaPorcentaje",	width: "100px",title: $translate.instant("UI_MONEDA_PORCENTAJE")},
		],		
		sortable			: false,
		groupable			: false,
		filterable 			: false,
		autoBind			: false, 
		height				: 150,
		userSettings		: false,
		navigatable			: true,
		notEditableColumns  : function(row){
			var arrayColum = [];
			//deshabilito la edicion del campo
			var permitido = ['PORCENTAJETASAPLAZO','PORCENTAJEFIJO','MONTOFIJO'];
			if (permitido.indexOf(row.IdTipoCobro) === -1){
				arrayColum.push('ValorCobroCombinado');
			}				
			return arrayColum;
		},
		gridCreated			: function(grid) {
			$scope.data.apiGridCobros = grid;
			//promiseGridCobros.resolve();
			$scope.cargaInicial();
		},
	 };
		
	
});