angular.module('ModuloTesoreria').controller('BlotterMonedasTesoreriaCtrl',
		function($scope,$modal,$state,bcsComunes,$translate,$rootScope,bcsServiciosTesoreria)
{
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);


	var dsMonedasSistema = new bcs.DataSource({
        type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: [{ field: "IdMoneda", dir: "asc" }],
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1727",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdMoneda		: {type : "string"},
						Nombre			: {type : "string"},
						Simbolo			: {type : "string"},
						NombreEstado	: {type : "string"},
					}
				}
			}
		}
	});	
	$scope.confMonedasSistema = {
		dataSource  : dsMonedasSistema,
		columns 	: [   {field : "IdMoneda",		title:"UI_CODIGO",	width: "50px"}
		                 ,{field : "Nombre",		title:"UI_NOMBRE",	width: "180px"}
		                 ,{field : "Simbolo",		title:"UI_SIMBOLO",	width: "50px"}
		                 ,{field : "NombreEstado",	title:"UI_ESTADO",	width: "50px"}
                      ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		height 		: 330,
		selectable 	: "multiple row",
		autoBind 	: true,
		userSettings: true
	};
	


	var dsMonedasTesoreria = new bcs.DataSource({
        type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: [{ field: "IdMoneda", dir: "asc" }],
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1038",dataType : "json"}},
			filter				: { field: "IdEstadoMonedaTesoreria", operator: "eq", value: 'ACT'},
			schema : {
				model : {
					fields : {								
						IdMoneda	: {type : "string"},
						Nombre		: {type : "string"}
					}
				}
			}
		}
	});	
	$scope.confMonedasTesoreria = {
		dataSource  : dsMonedasTesoreria,
		columns 	: [   {field : "IdMoneda",	title:"UI_CODIGO",	width: "50px"}
		                 ,{field : "Nombre",	title:"UI_NOMBRE" }
                      ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		height 		: 330,
		selectable 	: "multiple row",
		autoBind 	: true,
		userSettings: true
	};


	$scope.agregarMonedasTesoreria = function(){
		var Monedas = [];
		for(i=0; i< $scope.seleccionMonedasSistema.length;i++){
			Monedas.push({IdMoneda 		: $scope.seleccionMonedasSistema[i].IdMoneda,
						  TipoEjecucion	: 'ACT'
			});
		}

		var datos = {};
		datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
		datos.p_IdUsuario			= $scope.SesionIdUsuario;
		datos.p_IdModulo			= 'TES';		
		datos.p_UsaReglasNegocio	= 'S';
		datos.p_FechaMovimiento		= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));	
		datos.p_Monedas				= Monedas

		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosTesoreria.GestionDeMonedas(datos).$promise.then(
			function(respuesta){	  		
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
							
				//Limpia seleccionados de grilla
				$scope.seleccionMonedasSistema   = undefined;
				$scope.seleccionMonedasTesoreria = undefined;
				
				dsMonedasSistema.read();
				dsMonedasTesoreria.read();
				bcsComunes.ventanaOverlay($scope.winID, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AGREGAR_MONEDAS"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	};


	$scope.eliminarMonedasTesoreria = function(){
		var Monedas = [];
		for(i=0; i< $scope.seleccionMonedasTesoreria.length;i++){
			Monedas.push({IdMoneda 		: $scope.seleccionMonedasTesoreria[i].IdMoneda,
						  TipoEjecucion	: 'INA'
			});
		}

		var datos = {};
		datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
		datos.p_IdUsuario			= $scope.SesionIdUsuario;
		datos.p_IdModulo			= 'TES';		
		datos.p_UsaReglasNegocio	= 'S';
		datos.p_FechaMovimiento		= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));		
		datos.p_Monedas				= Monedas;


		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosTesoreria.GestionDeMonedas(datos).$promise.then(
			function(respuesta){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);

				//Limpia seleccionados de grilla
				$scope.seleccionMonedasSistema   = undefined;
				$scope.seleccionMonedasTesoreria = undefined;
				
				dsMonedasSistema.read();
				dsMonedasTesoreria.read();				
				bcsComunes.ventanaOverlay($scope.winID, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AGREGAR_MONEDAS"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	};

	var firstHeight = $('#Win_MonedasTesoreria');
        firstHeight.css('height', '442px');
});