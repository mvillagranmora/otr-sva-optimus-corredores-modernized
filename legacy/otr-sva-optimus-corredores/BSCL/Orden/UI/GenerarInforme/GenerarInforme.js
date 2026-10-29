angular.module('ModuloOrdenes').controller('GenerarInformeCtrl',function($scope,bcsComunes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes,$translate)
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.nombreInforme = $translate.instant("UI_INFORME_TRANSACCIONES_ANULADAS_MODIFICADAS");
	
	$scope.generar = function() {
		var data = [];
		var title = "";
		var tipo = "";
		var libroDatasource = {};
		
		
		var options = bcsConfiguracionOrdenesV2.getConfiguracionConsultarTransaccionesAnuladasModificadas().options;
		var schema = bcsConfiguracionOrdenesV2.getSchemaTransaccion().model.fields;
		
		data = _.map(options.columns,function(element, index){
			return { field: element.field, title: element.title, format : schema[element.field]?schema[element.field].excelFormat:null};
		});

		title = $translate.instant("UI_INFORME_TRANSACCIONES_ANULADAS_MODIFICADAS");
		var dataSource = new bcs.DataSource({
			type: "call",
			definition : {
				callParameters      : {
					p_IdEmpresa		:$scope.SesionIdEmpresa,
					p_IdUsuario		:$scope.SesionIdUsuario,
					p_IdModulo		:"ORD",
					p_FechaConsulta	:util.dateSPdate($scope.fecha),
				},
				callTransaction 	: "3238",
				callResultSet 		: "p_out_Transacciones",
				schema 				: bcsConfiguracionOrdenesV2.getSchemaTransaccion(),

			}});
		bcsComunes.ventanaOverlay(idVentana,true);
		dataSource.fetch().then(function(result) {
			bcsComunes.ventanaOverlay(idVentana,false);

			if (dataSource.data().length === 0) {
				bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_DATOS");
				return;
			}
			var nombre = title + " " +util.dateSPdate($scope.fecha);
			dataSource.exportToExcel(data,title,nombre);
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
		});
		
		
	}
	
	$scope.generar2 = function() {
		var data = [];
		var title = "";
		var tipo = "";
		var libroDatasource = {};
		if (parametros.tipoLibro === 'ORDENES') {
			data = _.map(bcsConfiguracionOrdenesV2.getConfiguracionConsultarOrdenes().options.columns,function(element, index){
				return { field: element.field, title: element.title};
			});
			
			title = $translate.instant("UI_LIBRO_ORDENES");
			tipo = "LIBRO_ORDENES";
			var libroDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: bcsConsultasOrdenes('ConsultarOrden'),
						dataType: "json"
					}
				},
				filter: [
				    { field: "IdLibroOrdenes", operator: "eq", value: $scope.filtros.LibroOrdenes.IdLibroOrdenes},
					{ field: "FechaOperacionOrden", operator: "gte", value: $scope.fecha},
					{ field: "FechaOperacionOrden", operator: "lte", value: util.date($scope.fecha).add(1,'day').toDate() }

				],
				schema: bcsConfiguracionOrdenesV2.getSchemaOrden()

			}});
			
		}

		bcsComunes.ventanaOverlay(idVentana,true);
		libroDatasource.fetch().then(function(result) {
			bcsComunes.ventanaOverlay(idVentana,false);
			
			if (libroDatasource.data().length === 0) {
				bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_DATOS");
				return;
			}
			var nombre = tipo + "_" + util.dateSPdate($scope.fecha) + "_" +$scope.filtros.LibroOrdenes.NombreLibroOrdenes;
			libroDatasource.exportToExcel(data,title,nombre);
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
		});

	}
	
	$scope.cerrar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}

});


