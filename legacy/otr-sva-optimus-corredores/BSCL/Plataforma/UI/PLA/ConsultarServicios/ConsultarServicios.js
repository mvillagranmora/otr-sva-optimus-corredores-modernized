angular.module('ModuloPlataforma').controller('ConsultarServiciosCtrl',function($scope,bcsComunes,$timeout){

	$scope.configuracionServicios = {
			columns : [
			           {field: "IdServicio",		title : "IdServicio", 		width : "70px"},
			           {field: "RutaServicio",		title : "RutaServicio", 	width : "180px"},
			           {field: "TipoServicio",		title : "TipoServicio", 	width : "70px"},
			           {field: "Descripcion",		title : "Descripcion"},
			           ],
	      	height:300,
	      	autoBind: true,
	      	userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
	      	filterable : 'row',
	      	dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					schema: {
						model : {
							fields : {
								IdServicio 		: { type: "string"},
				           		RutaServicio  	: { type: "string"},
				           		TipoServicio  	: { type: "string"},
				           		Descripcion  	: { type: "string"},
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1229",
							dataType: "json"
						}
					}
				}
	      	}),

	};
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaCambiaCss(idVentana,'overflow','hidden');
	$timeout(function() {bcsComunes.ventanaTamanoAutomatico(idVentana)},500	);
	
});
