angular.module('ModuloPlataforma').controller('VerDetalleEncuestaCtrl', function($scope,OPCIONES_BCSALERTA,$translate,bcsComunes,bcsServicioPlataforma,bcsAlerta){
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;
	var params = bcsComunes.ventanaParams($scope.idVentana);
	$scope.form = {};
	$scope.data = {};
	var dataSource = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering : true,
			serverPaging 	: false,
			serverSorting 	: true,
			transport: {
				read		: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=100061",
								dataType: "json"
							  }
				},						
			filter			: { field: "IdRegistroEncuesta", operator: "eq", value: params.IdRegistroEncuesta},
			sort 			: [{ field: "IdRegistroPregunta", dir: "asc" },{ field: "IdRegistroRespuesta", dir: "desc" },],
			schema 			: {
				model	: {
					fields	: { 
						FechaEncuesta					: { type: "datetime"},
						IdRegistroEncuesta				: { type: "number"},
						CodigoUsuarioResponde			: { type: "string"},
						NombreCategoriaInversionista	: { type: "string"}
						}
					}
				
					
				}
			}
	});
	
	dataSource.fetch().then(function(){
		
		var ddd = dataSource.data();
		var lista = {};
		var index = 1;

		
		if(ddd.length > 0){
			$scope.datos = {};
			$scope.datos.NombreCliente = ddd[0].NombreCompleto
			$scope.datos.FechaEncuesta = util.formatoTimeStamp(ddd[0].FechaEncuesta)
			$scope.datos.NombreUsuario = ddd[0].CodigoUsuarioResponde
			$scope.datos.CategoriaInversionista = ddd[0].NombreCategoriaInversionista
			$scope.datos.Canal = params.Canal
		}

		for (var i = 0; i<ddd.length; i++) {

			var elem = ddd[i];

			var respuesta = {
				TextoRespuesta : elem.TextoRespuesta,
				PuntajeRespuesta : elem.PuntajeRespuesta,
				Seleccionado : elem.Seleccionado
			}

			if (!lista[elem.IdRegistroPregunta]) {
				lista[elem.IdRegistroPregunta] = {
					respuestas : [],
					TextoPregunta : elem.TextoPregunta,
					Indice : index++
				}
			}

			lista[elem.IdRegistroPregunta].respuestas.unshift(respuesta)	


		}
		
		$scope.data = lista;


	});
	
	$scope.form.salir = function() {
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.form.descargar = function() {
		kendo.drawing.drawDOM($("#encuestaR"), {
	          forcePageBreak:".break",
	          paperSize: "A4",
	          margin: "1cm",
	          multipage:true
	        }).then(function(group){
	          kendo.drawing.pdf.saveAs(group, "EncuestaInversionista.pdf");
	        });
	}

		
});