angular.module('ModuloPlataforma').controller('ResumenOperacionesMasivasCtrl',function($scope,$translate,bcsComunes){
	
	//DEBE RECIBIR
	//TITULO, NOMBREENTIDAD, DATA (IDENTIDAD, ESTADO, MENSAJE)
		
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	bcsComunes.ventanaTitulo(idVentana, parametros.tituloVentana);
	
	$scope.salir = function() {
		
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.configuracion = {
			height:200,
			autoBind: true,
			dataSource: new bcs.DataSource({
				type : "array",
	  			definition : {
					data : parametros.operaciones,
					schema:{
						model:{
							fields:{
								id			: {type: "string"},
								resultado 	: {type: "string"},
								mensaje		: {type: "string"},
							}
						}
					}
				},
	  		}),
  			columns: [
  			          {field: "id",title: parametros.nombreEntidad},
  			          {field: "resultado",title: $translate.instant('UI_RESULTADO')},
  			          {field: "mensaje",title: $translate.instant('UI_MENSAJE')},
			
  			        
			],
				
		};
	
});