angular.module('ModuloPlataforma').controller('CrearDefinicionEncuestaCtrl', function($scope,$resource,$translate,bcsComunes,bcsServicioPlataforma,bcsAlerta){
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;
	
	$scope.form = {};
	$scope.form.correlativoPreguntas = 1;
	$scope.form.correlativoRespuestas = 1;
	
	$scope.form.configuracionPreguntas = {
			dataSource			: new bcs.DataSource({
				type 		: "array",
				definition : {
					schema	:	{
						model: 	{
							fields : {
								IdDefinicionPregunta	: {type: "number"},
								TextoPregunta			: {type: "string"}
							}
						} 
					}
				}
				
			}),
			
			columns	: [
				{ field: "IdDefinicionPregunta"	,title: 'UI_ID_PREGUNTA', width : "50px"},
				{ field: "TextoPregunta"		,title: 'UI_PREGUNTA' , width : "auto" }
				],		
			sortable			: true,
			groupable			: false,
			filterable			: false,
			pageable			: false,
			autoBind			: true,
			height				: 100,
		    selectable 			: "row"
		};
	
	$scope.form.configuracionRespuestas = {
			dataSource			: new bcs.DataSource({
				type 		: "array",
				definition	: {
					schema	:{ 
						model: {
							fields : {
								IdDefinicionPregunta	: {type: "number"},
								IdDefinicionRespuesta	: {type: "number"},
								TextoRespuesta			: {type: "string"},
								Puntaje					: {type: "number"},
							}
						} 
					}
				}
				
			}),
			
			columns	: [
				{ field: "IdDefinicionPregunta"	,title: 'UI_ID_PREGUNTA', width : "70px"},
				{ field: "TextoRespuesta"		,title: 'UI_RESPUESTA', width : "auto"},
				{ field: "Puntaje"				,title: 'UI_PUNTAJE'	, width: "70px" }
				],		
			sortable			: true,
			groupable			: false,
			filterable			: false,
			pageable			: false,
			autoBind			: true,
			height				: 150,
		    selectable 			: "row"
		};
	
	$scope.form.filtrosEstado = {
		Entidad : {"field" : "NombreEntidad", "operator" : "eq", "value" : 'ENCUESTA'}
	}
	
	$scope.form.crearPregunta = function() {
		var id = bcsComunes.ventanaAbrir("AgregarPregunta",{},false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			if (data && data.TextoPregunta) {
				$scope.form.apiPreguntas.dataSource.add({ IdDefinicionPregunta : $scope.form.correlativoPreguntas++, TextoPregunta : data.TextoPregunta })
			}
		});
	}

	$scope.form.modificarPregunta = function() {
		var id = bcsComunes.ventanaAbrir("AgregarPregunta",{ data : $scope.form.seleccionadoPreguntas },false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			if (data) {
				
				$scope.form.seleccionadoPreguntas.TextoPregunta = data.TextoPregunta;
				
				
			}
		});
	}

	$scope.form.eliminarPregunta = function() {
		if ($scope.form.seleccionadoPreguntas) {
			
			var respuestas = $scope.form.apiRespuestas.dataSource.data();
			var r = Array();
			for (var i = 0; i < respuestas.length ; i ++) {
				if (respuestas[i].IdDefinicionPregunta == $scope.form.seleccionadoPreguntas.IdDefinicionPregunta) {
					r.push(respuestas[i]);
				}
			}
			
			for (var i = 0; i < r.length ; i++) {
				$scope.form.apiRespuestas.dataSource.remove(r[i]);
			}
			
			$scope.form.apiPreguntas.dataSource.remove($scope.form.seleccionadoPreguntas);
			$scope.form.seleccionadoPreguntas = null;
			$scope.form.seleccionadoRespuestas = null;
		}
	}

	$scope.form.crearRespuesta = function() {
		var id = bcsComunes.ventanaAbrir("AgregarRespuesta",{},false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			if (data && data.TextoRespuesta) {
				$scope.form.apiRespuestas.dataSource.add({ 
					IdDefinicionPregunta : $scope.form.seleccionadoPreguntas.IdDefinicionPregunta, 
					IdDefinicionRespuesta : $scope.form.correlativoRespuestas++, 
					TextoRespuesta : data.TextoRespuesta, 
					Puntaje : data.Puntaje })	
			}
			
			
		});
	}

	$scope.form.modificarRespuesta = function() {
		var id = bcsComunes.ventanaAbrir("AgregarRespuesta",{ data : $scope.form.seleccionadoRespuestas },false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			if (data) {
				
				$scope.form.seleccionadoRespuestas.TextoRespuesta = data.TextoRespuesta;
				$scope.form.seleccionadoRespuestas.Puntaje = data.Puntaje;
				
				
			}
		});
	}

	$scope.form.eliminarRespuesta = function() {
		if ($scope.form.seleccionadoRespuestas) {
			$scope.form.apiRespuestas.dataSource.remove($scope.form.seleccionadoRespuestas);
			$scope.form.seleccionadoRespuestas = null;
		}
	}

	$scope.form.aceptar = function() {
		
		var preguntas = [];
		for (var i = 0; i<$scope.form.configuracionPreguntas.dataSource.data().length;i++) {
			var p = {
				IdPregunta 		: $scope.form.configuracionPreguntas.dataSource.data()[i].IdDefinicionPregunta,
				TextoPregunta 	: $scope.form.configuracionPreguntas.dataSource.data()[i].TextoPregunta
		
			};
			 preguntas.push(p);
		}
		
		var respuestas = [];
		for (var i = 0; i<$scope.form.configuracionRespuestas.dataSource.data().length;i++) {
			var r = {
				IdPregunta 		: $scope.form.configuracionRespuestas.dataSource.data()[i].IdDefinicionPregunta,
				IdRespuesta		: $scope.form.configuracionRespuestas.dataSource.data()[i].IdDefinicionRespuesta,
				TextoRespuesta	: $scope.form.configuracionRespuestas.dataSource.data()[i].TextoRespuesta,
				Puntaje			: $scope.form.configuracionRespuestas.dataSource.data()[i].Puntaje
			}
			respuestas.push(r);
	
		}
		
		var p_datos = {
				p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
				p_IdUsuario			: $scope.$root.SesionIdUsuario,
				p_IdModulo			: 'PLA',
				p_UsaReglasNegocio	: 'S',
				p_simularOperacion	: 'N',
				p_Nombre			: $scope.form.nombre,
				p_IdEstado			: $scope.form.estado,
				p_Preguntas			: preguntas,
				p_Respuestas		: respuestas
		}
		
		var urlCrearDefinicionEncuesta = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=100051";
		var r = $resource("", {}, {'CrearDefinicion': {method:"POST", url:urlCrearDefinicionEncuesta}});
		
		var post = r.CrearDefinicion(p_datos);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		post.$promise.then(function(result) {
		
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var id = bcsComunes.ventanaMsg({
				mensaje: '',
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);
			
			bcsComunes.ventanaPromesa(id).then(function(){
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					bcsComunes.ventanaCerrar($scope.idVentana);
				}
			})
		},
		function(err){
			bcsComunes.ventanaMsg({
				mensaje: '',
				conflictos:util.parseServerResponse(err)
			},$scope.idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		});
		
		
		
	}

	$scope.form.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	

	
});