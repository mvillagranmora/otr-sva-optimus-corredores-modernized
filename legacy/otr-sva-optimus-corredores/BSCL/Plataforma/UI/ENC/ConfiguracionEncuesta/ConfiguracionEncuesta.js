angular.module('ModuloPlataforma').controller('ConfiguracionEncuestaCtrl', function($scope,OPCIONES_BCSALERTA,$translate,bcsComunes,bcsServicioPlataforma,bcsAlerta){
	
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;
	$scope.form = {};
	
	
	
	$scope.form.configuracion = {
			dataSource			: new bcs.DataSource({ 
				type 		: "odata",
				definition 	: {
					serverFiltering 	: true, 
					serverPaging 		: true,
					serverSorting 		: true,
					pageSize 			: 50,
					transport 			: {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=100057",
							dataType : "json"
						}
					},
					schema	:{ 
						model: {
							fields : {
								IdDefinicionEncuesta		: {type: "number"},
								Nombre						: {type: "string"},
								Estado						: {type: "string"},
								CodigoUsuarioCreacion		: {type: "string"}	
						}
					} 
					}
			  }}),
			  columns				: [
				  { field: "Nombre"					,title: 'UI_NOMBRE_ENCUESTA'	, width: "50px", },
				  { field: "Estado"					,title: 'UI_ESTADO'				, width: "50px", },
				  { field: "CodigoUsuarioCreacion"	,title: 'UI_USUARIO_CREADOR'	, width: "50px", }
                 ],		
			sortable			: true,
			filterable			: true,
			pageable			: true,
			autoBind			: true,
			height				: 300,
		    selectable 			: "row"
		};
	
	$scope.form.crearEncuesta = function() {
		var id = bcsComunes.ventanaAbrir("CrearDefinicionEncuesta",{},false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			$scope.form.api.dataSource.read();
		});
		
	}
	
	$scope.form.modificarEncuesta = function() {
		var id = bcsComunes.ventanaAbrir("ModificarDefinicionEncuesta",{ IdDefinicionEncuesta : $scope.form.seleccionado.IdDefinicionEncuesta },false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			$scope.form.api.dataSource.read();
		});
	}
	
	$scope.form.verPuntajes = function() {
		var id = bcsComunes.ventanaAbrir("ConfiguracionPuntajeEncuesta",{},false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			
		});
	}
});