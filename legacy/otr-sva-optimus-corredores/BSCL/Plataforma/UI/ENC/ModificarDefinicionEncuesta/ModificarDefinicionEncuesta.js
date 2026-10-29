angular.module('ModuloPlataforma').controller('ModificarDefinicionEncuestaCtrl', function($scope,$resource,$translate,bcsComunes,bcsServicioPlataforma,bcsAlerta){
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;
	var params = bcsComunes.ventanaParams($scope.idVentana);
	$scope.form = {};
	$scope.IdDefinicionEncuesta = params.IdDefinicionEncuesta;
	$scope.form.correlativoPreguntas = 1;
	
	
	
	$scope.form.configuracionPreguntas = {
			dataSource			: new bcs.DataSource({
				type 		: "array",
				definition : {
					schema	:	{
						model: 	{
							fields : {
								IdDefinicionPregunta		: {type: "number"},
								IdDefinicionPreguntaIndex	: {type: "number"},
								TextoPregunta				: {type: "string"}
							}
						} 
					}
				}
				
			}),
			
			columns	: [
				{ field: "IdDefinicionPreguntaIndex"	,title: 'UI_ID_PREGUNTA', width : "50px"},
				{ field: "TextoPregunta"				,title: 'UI_PREGUNTA' , width : "auto" }
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
								IdDefinicionPregunta		: {type: "number"},
								IdDefinicionPreguntaIndex	: {type: "number"},
								IdDefinicionRespuesta		: {type: "number"},
								TextoRespuesta				: {type: "string"},
								Puntaje						: {type: "number"},
							}
						} 
					}
				}
				
			}),
			
			columns	: [
				{ field: "IdDefinicionPreguntaIndex"	,title: 'UI_ID_PREGUNTA', width : "70px"},
				{ field: "TextoRespuesta"				,title: 'UI_RESPUESTA', width : "auto"},
				{ field: "Puntaje"						,title: 'UI_PUNTAJE'	, width: "70px" }
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
				$scope.form.apiPreguntas.dataSource.add(
						{ 
							IdDefinicionPreguntaIndex : $scope.form.correlativoPreguntas++, 
							IdDefinicionPregunta : 0,
							TextoPregunta : data.TextoPregunta })
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
				if (respuestas[i].IdDefinicionPreguntaIndex == $scope.form.seleccionadoPreguntas.IdDefinicionPreguntaIndex) {
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
					IdDefinicionPreguntaIndex : $scope.form.seleccionadoPreguntas.IdDefinicionPreguntaIndex,
					IdDefinicionRespuesta : 0,
					TextoRespuesta : data.TextoRespuesta, 
					Puntaje : data.Puntaje })	
			}
			
			
		});
	}

	$scope.form.modificarRespuesta = function() {
		var id = bcsComunes.ventanaAbrir("AgregarRespuesta",{ data : $scope.form.seleccionadoRespuestas },false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			if (data && data.TextoRespuesta) {
				
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

	var dataSource = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering : true,
			serverPaging 	: false,
			serverSorting 	: true,
			transport: {
				read		: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=100057",
								dataType: "json"
							  }
				},						
			filter			: { field: "IdDefinicionEncuesta", operator: "eq", value: params.IdDefinicionEncuesta},
			schema 			: {
				model	: {
					fields	: { 
						IdDefinicionEncuesta			: { type: "number"},
						Nombre							: { type: "string"},
						IdEstado						: { type: "string"}
						}
					}
				
					
				}
		}
	});
	
	var dataSourcePreguntas = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering : true,
			serverPaging 	: false,
			serverSorting 	: true,
			transport: {
				read		: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=100062",
								dataType: "json"
							  }
				},						
			filter			: { field: "IdDefinicionEncuesta", operator: "eq", value: params.IdDefinicionEncuesta},
			schema 			: {
				model	: {
					fields	: { 
						IdDefinicionEncuesta		: { type: "number"},
						IdDefinicionPregunta		: { type: "number"},
						IdDefinicionPreguntaIndex	: { type: "number"},
						TextoPregunta				: { type: "string"},
						IdDefinicionRespuesta		: { type: "number"},
						TextoRespuesta				: { type: "string"},
						Puntaje						: { type: "number"}
						}
					}
			}
		}
	});
	
	dataSource.fetch().then(function(){
		var data = dataSource.data();
		
		if (data.length !== 1) {
			console.error("No se recuperó informacion");
			return;
		}
		
		$scope.form.nombre = data[0].Nombre;
		$scope.form.estado = data[0].IdEstado;
		$scope.form.estadoP = data[0].IdEstado;
		
	});
	
	dataSourcePreguntas.fetch().then(function() {
		
		var data = dataSourcePreguntas.data();

		var preguntas = [];
		var respuestas = [];
		var hashPreguntas = {};

		for (var i = 0; i<data.length ; i++) {
			var elem = data[i];
			if (elem.IdDefinicionPregunta == 0) {
				continue;
			}
			var pregunta = hashPreguntas[elem.IdDefinicionPregunta];
			if (pregunta === undefined) {
				//nueva pregunta 
				pregunta = {
					IdDefinicionPreguntaIndex : $scope.form.correlativoPreguntas,
					IdDefinicionPregunta : elem.IdDefinicionPregunta,
					TextoPregunta : elem.TextoPregunta
				}
				$scope.form.correlativoPreguntas++;
				preguntas.push(pregunta);
				hashPreguntas[elem.IdDefinicionPregunta] = pregunta;
			}

			if (elem.IdDefinicionRespuesta === 0) {
				continue;
			}
			var respuesta = {
				TextoRespuesta : elem.TextoRespuesta,
				Puntaje : elem.Puntaje,
				IdDefinicionRespuesta  : elem.IdDefinicionRespuesta,
				IdDefinicionPregunta  : elem.IdDefinicionPregunta,
				IdDefinicionPreguntaIndex  : pregunta.IdDefinicionPreguntaIndex
			}
			respuestas.push(respuesta);

		}
		$scope.form.configuracionPreguntas.dataSource.data(preguntas);
		$scope.form.configuracionRespuestas.dataSource.data(respuestas);

		$scope.form.preguntasOriginal = JSON.parse(JSON.stringify(preguntas));
		$scope.form.respuestasOriginal = JSON.parse(JSON.stringify(respuestas));
		
	});
	
		
	
	$scope.form.aceptar = function() {
		
		var preguntas = [];
		var respuestas = [];
		//preguntas nuevas
		//preguntas eliminadas
		//preguntas modificadas

		var preguntasActuales = $scope.form.configuracionPreguntas.dataSource.data();
		var preguntasOriginales = $scope.form.preguntasOriginal;


		var respuestasActuales = $scope.form.configuracionRespuestas.dataSource.data();
		var respuestasOriginales = $scope.form.respuestasOriginal;

		for (var i = 0;i < preguntasActuales.length ; i++) {
			if (preguntasActuales[i].IdDefinicionPregunta == 0) {
				var preguntaNueva = {
					Accion : "C",
					IdDefinicionPregunta : preguntasActuales[i].IdDefinicionPreguntaIndex * -1,
					TextoPregunta : preguntasActuales[i].TextoPregunta
				}

				preguntas.push(preguntaNueva);
			}
		}

		for (var i = 0;i < preguntasOriginales.length ; i++) {
			var found = false;
			for (var j = 0 ; j < preguntasActuales.length ; j++) {
				if (preguntasOriginales[i].IdDefinicionPregunta === preguntasActuales[j].IdDefinicionPregunta) {
					found = true;

					if (preguntasOriginales[i].TextoPregunta !== preguntasActuales[j].TextoPregunta) {
						var preguntaModificada = {
						IdDefinicionPregunta : preguntasOriginales[i].IdDefinicionPregunta,
							TextoPregunta : preguntasActuales[j].TextoPregunta,
							Accion : "M"
						}
						preguntas.push(preguntaModificada);
					}

					break;
				}
			}
			if (!found) {
				var preguntaEliminada = {
					IdDefinicionPregunta : preguntasOriginales[i].IdDefinicionPregunta,
					Accion : "E"
				}
				preguntas.push(preguntaEliminada);
			}
		}

		//respuestas nuevas
		//respuestas modificadas
		//respuestas eliminadas

		for (var i = 0;i < respuestasActuales.length ; i++) {
			if (respuestasActuales[i].IdDefinicionRespuesta == 0) {
				var respuestaNueva = {
					Accion : "C",
					IdDefinicionPregunta : respuestasActuales[i].IdDefinicionPregunta > 0? respuestasActuales[i].IdDefinicionPregunta : respuestasActuales[i].IdDefinicionPreguntaIndex * -1,
					IdDefinicionRespuesta : 0,
					TextoRespuesta : respuestasActuales[i].TextoRespuesta,
					Puntaje : respuestasActuales[i].Puntaje,
				}

				respuestas.push(respuestaNueva);
			}
		}


		for (var i = 0;i < respuestasOriginales.length ; i++) {
			var found = false;
			for (var j = 0 ; j < respuestasActuales.length ; j++) {
				if (respuestasOriginales[i].IdDefinicionRespuesta === respuestasActuales[j].IdDefinicionRespuesta) {
					found = true;

					if (respuestasOriginales[i].TextoRespuesta !== respuestasActuales[j].TextoRespuesta ||
					respuestasOriginales[i].Puntaje !== respuestasActuales[j].Puntaje) {
						var respuestaModificada = {
							IdDefinicionPregunta : respuestasOriginales[i].IdDefinicionPregunta,
							IdDefinicionRespuesta : respuestasOriginales[i].IdDefinicionRespuesta,
							TextoRespuesta : respuestasActuales[j].TextoRespuesta,
							Puntaje : respuestasActuales[j].Puntaje,
							Accion : "M"
						}
						respuestas.push(respuestaModificada);
					}

					break;
				}
			}
			
			if (!found) {
				var respuestaEliminada = {
					IdDefinicionRespuesta : respuestasOriginales[i].IdDefinicionRespuesta,
					Accion : "E"
				}
				respuestas.push(respuestaEliminada);
			}
		}

		
		
		var p_datos = {
				p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
				p_IdUsuario			: $scope.$root.SesionIdUsuario,
				p_IdModulo			: 'PLA',
				p_UsaReglasNegocio	: 'S',
				p_simularOperacion	: 'N',
				p_IdDefinicionEncuesta : $scope.IdDefinicionEncuesta,
				p_Nombre			: $scope.form.nombre,
				p_IdEstado			: $scope.form.estado,
				p_Preguntas			: preguntas,
				p_Respuestas		: respuestas
		}
		
		var urlModificarDefinicionEncuesta = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=100054";
		var r = $resource("", {}, {'ModificarDefinicion': {method:"POST", url:urlModificarDefinicionEncuesta}});
		
		var post = r.ModificarDefinicion(p_datos);
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