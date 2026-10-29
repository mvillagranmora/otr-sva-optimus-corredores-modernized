angular.module('ModuloPlataforma').controller('ConfiguracionPuntajeEncuestaCtrl', function($scope,$resource,$translate,bcsComunes,bcsServicioPlataforma,bcsAlerta){
	
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;

	$scope.edicion = {
			modificando : false,
			creando		: false,
			seleccionado : undefined,
			idCategoria : undefined,
			puntajeHasta : undefined,
			categoriaSeleccionada : undefined
	};
	
	$scope.quitarPuntaje = function() {
		if (!$scope.seleccionado) return;
		$scope.api.dataSource.remove($scope.seleccionado);
		$scope.seleccionado = undefined;
	}

	$scope.agregarPuntaje = function() {
		$scope.edicion.creando = true;
		$scope.edicion.modificando = false;
		$scope.edicion.idCategoria = undefined;
		$scope.edicion.puntajeHasta = undefined;
		$scope.edicion.categoriaSeleccionada = undefined;
	}
	
	$scope.modificarPuntaje = function() {
		$scope.edicion.modificando = true;
		$scope.edicion.seleccionado = $scope.seleccionado;
		$scope.edicion.creando = false;
		$scope.edicion.idCategoria = $scope.seleccionado.IdCategoriaInversionista;
		$scope.edicion.puntajeHasta = $scope.seleccionado.PuntajeHasta;
	}
	
	$scope.cancelarPuntaje = function() {
		$scope.edicion.creando = false;
		$scope.edicion.modificando = false;
		$scope.edicion.idCategoria = undefined;
		$scope.edicion.puntajeHasta = undefined;
	}
	
	$scope.guardarPuntaje = function() {

		//todo validar que categoria no este agregada
		//todo validar que puntaje no este repetido

		var data = $scope.api.dataSource.data();
		for (var i = 0; i< data.length ; i++) {
			if (data[i].PuntajeHasta === $scope.edicion.puntajeHasta) {
				bcsComunes.msg("error","error","UI_PUNTAJE_YA_CONFIGURADO");
				return;
			}
			if (data[i].IdCategoriaInversionista === $scope.edicion.IdCategoriaInversionista) {
				bcsComunes.msg("error","error","UI_CATEGORIA_YA_CONFIGURADO");
				return;
			}
			
		}


		$scope.edicion.creando = false;
		var obj = {
				IdEmpresa : $scope.SesionIdEmpresa,
				PuntajeHasta : $scope.edicion.puntajeHasta,
				IdCategoriaInversionista : $scope.edicion.idCategoria,
				NombreCategoriaInversionista : $scope.edicion.categoriaSeleccionada.NombreCategoriaInversionista
		}
		$scope.api.dataSource.add(obj);
		$scope.api.dataSource.sort();
		$scope.edicion.idCategoria = undefined;
		$scope.edicion.puntajeHasta = undefined;
	}
	
	$scope.actualizarPuntaje = function() {
		
		$scope.edicion.seleccionado.PuntajeHasta = $scope.edicion.puntajeHasta;
		$scope.edicion.seleccionado.IdCategoriaInversionista = $scope.edicion.idCategoria;
		$scope.edicion.seleccionado.NombreCategoriaInversionista = $scope.edicion.categoriaSeleccionada.NombreCategoriaInversionista;
		
		$scope.edicion.modificando = false;
		$scope.edicion.idCategoria = undefined;
		$scope.edicion.puntajeHasta = undefined;
		$scope.edicion.seleccionado = undefined;
	}
	
	$scope.cancelarCambios = function() {
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.guardarCambios = function() {
		
		var urlActualizarPuntajes = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=100059";
		var r = $resource("", {}, {'ActualizarDefinicionPuntajes': {method:"POST", url:urlActualizarPuntajes}});
		
		var puntajes = [];
		var data = $scope.api.dataSource.data();
		
		for (var i = 0; i<data.length ; i++) {
			var obj = {
					p_PuntajeHasta 			: data[i].PuntajeHasta,
					p_IdCategoriaInversionista : data[i].IdCategoriaInversionista,
			}
			
			puntajes.push(obj);
			
		}
		
		var p_datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo	: "PLA",
				p_simularOperacion : "N",
				p_UsaReglasNegocio : "S",
				
				p_Puntajes : puntajes
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = r.ActualizarDefinicionPuntajes(p_datos);
		post.$promise.then(function(result) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var id = bcsComunes.ventanaMsg({
				conflictos : util.parseServerResponse(result)
			},$scope.idVentana);
			
			if (result.P_OUT_ERRORESCONTADOR === 0) {
				bcsComunes.ventanaPromesa(id).then(function() {
					bcsComunes.ventanaCerrar($scope.idVentana);	
				});
			}

			
			
		},
		function(result) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
				conflictos : util.parseServerResponse(result)
			},$scope.idVentana);
		});
		
		
	}
	
	
	$scope.configuracion = {
			dataSource			: new bcs.DataSource({ 
				type 		: "odata",
				definition 	: {
					serverFiltering 	: true, 
					serverPaging 		: true,
					serverSorting 		: true,
					pageSize 			: 50,
					transport 			: {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=100058",
							dataType : "json"
						}
					},
					schema	:{ 
						model: {
							fields : {
								IdEmpresa						: {type: "number"},
								PuntajeHasta					: {type: "number"},
								IdCategoriaInversionista		: {type: "string"},
								NombreCategoriaInversionista	: {type: "string"}
									
						}
					} 
					}
			  }}),
			  columns				: [
				  { field: "PuntajeHasta"					,title: 'UI_PUNTAJE_HASTA'		, width: "100px" },
				  { field: "NombreCategoriaInversionista"	,title: 'UI_CATEGORIA_INVERSIONISTA'	 }
                 ],		
			sortable			: true,
			filterable			: false,
			pageable			: false,
			autoBind			: true,
			height				: 200,
		    selectable 			: "row"
		};	
});