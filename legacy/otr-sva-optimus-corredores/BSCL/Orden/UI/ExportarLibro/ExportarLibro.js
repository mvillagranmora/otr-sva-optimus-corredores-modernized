angular.module('ModuloOrdenes').controller('ExportarLibroCtrl',function($scope,bcsComunes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes,$translate)
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	var columnasLibro = parametros.columnas;
	if (parametros && parametros.fecha) {
		$scope.fecha = parametros.fecha;
	}
	
	if (parametros && parametros.libroDefecto) {
		$scope.libroDefecto = parametros.libroDefecto;
	}

	$scope.exportar = function() {
		var data = [];
		var title = "";
		var tipo = "";
		var libroDatasource = {};
		if (parametros.tipoLibro === 'ORDENES') {
			data = _.map(columnasLibro,function(element, index){
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
		else if(parametros.tipoLibro === 'ASIGNACIONES') {
			data = _.map(columnasLibro,function(element, index){
				return { field: element.field, title: element.title};
			});
			
			title = $translate.instant("UI_LIBRO_ASIGNACIONES");
			tipo = "LIBRO_ASIGNACIONES";
			
			var libroDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: bcsConsultasOrdenes('ConsultarAsignacionesUI'),
						dataType: "json"
					}
				},
				filter: [
				    { field: "IdLibroOrdenes", operator: "eq", value: $scope.filtros.LibroOrdenes.IdLibroOrdenes},
					{ field: "FechaAsignacion", operator: "gte", value: $scope.fecha},
					{ field: "FechaAsignacion", operator: "lte", value: util.date($scope.fecha).add(1,'day').toDate() }

				],
				schema: bcsConfiguracionOrdenesV2.getSchemaAsignacion()

			}});
			
			
			
		}
		else {
			return;
		}

		$.each(data,function(key,value){
			value.title = $translate.instant(value.title);
		})
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


