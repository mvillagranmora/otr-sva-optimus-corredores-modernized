angular.module('ModuloPlataforma', ["kendo.directives","ngSanitize"]).controller('MantenedorPlantilla',function($rootScope,$scope,bcsMantenedorEventosGrid,bcsServicioPlataforma,bcsComunes,$translate,bcsAlerta,$http){

	$scope.conf = {};
	$scope.filtros = {};
	$scope.data = {};
 	var idVentana = bcsComunes.ventanaWinId($scope);
 	
 	$rootScope.$on("CallParentTemplateModify", function(){
		 $scope.Refrescar(); 
      });

	console.log('Codigo Tipo Evento ' + $scope.filtros.codigoTipoEvento);

	 //$scope.html = '<p style="color:#d3d3d3;">  Escriba plantilla. Para utilizar campo -> ${NombreDeCampo}</p>';
	 
	
    $scope.mostrarEjemploPlantilla = function () {  

    	console.log('Data - texto ' + $scope.data.texto)
		console.log('Codigo Tipo Evento ' + $scope.filtros.codigoTipoEvento.CodigoTipoEvento);
    	
		
		
    };
    
    //PlantillaNotificacion
    
	var dataSourcePlantilla = new bcs.DataSource({
		
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
 			pageSize : 50,
 			sort 				: { field: "FechaModificacion", dir: "asc" }, // Criterios de Ordenamiento.
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t="+1386,
					dataType: "json"
				}
			},
			schema : 
			{
				model: {
					fields : {
						
						
						 IdPlantilla			 	: {type: "number"}
						,Nombre						: {type: "string"}
					 	,Template					: {type: "string"}
	 					,FechaCreacion				: {type: "sysdatetime"} 
 						,Asunto						: {type: "string"} 
 						,Usuario					: {type: "string"}
 						,UsuarioModifica			: {type: "string"}
 						,FechaModificacion			: {type: "sysdatetime"}
 						 
					}
				} 
			}
		}
	});
	
	var dataSourceConfiguracion = new bcs.DataSource({
		
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "IdConfiguracionEvento", dir: "desc" },
			pageSize : 50,
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1376",
					dataType: "json"
				}
			},
			schema : 
			{
				model: {
					fields : {
						
						
						IdConfiguracionEvento 		: {type: "number"}
					 	,Descripcion				: {type: "string"}
	 					,IdUsuario					: {type: "number"} 
						,FechaCreacion				: {type: "date"}
						,IdEmpresa					: {type: "number"} 
						,IdEstado					: {type: "string"}
						,IdEstadoUI					: {type: "code"}
						,NombreFormaDespachoUI		: {type: "code"}
						,NombreTiempoDespachoUI		: {type: "code"}
	 					,PermitePersonalizar		: {type: "string"} 
						,FormaDespacho				: {type: "string"} 
						,TiempoDespacho				: {type: "string"} 
						,ConfirmacionDespacho		: {type: "string"} 
						,ConfirmacionDespachoUI		: {type: "code"} 
						,FechaVencimiento			: {type: "date"} 
						,FechaRecordatorio			: {type: "date"} 
						,IdPlantilla				: {type: "number"} 
						,IdPlantillaRecordatorio	: {type: "number"}
						,CodigoTipoEvento			: {type: "string"}
						
					}
				} 
			}
		}
	});
 
	
	$scope.plantillasconfiguracion = {
			
			dataSource			: dataSourcePlantilla,
			columns				: [
				                    { field: "Nombre"					    ,title: 'UI_NOMBRE_PLANTILLA'		, width: "50px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
				                    { field: "Asunto"						,title: 'UI_ASUNTO'			, width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"} },
				                    { field: "FechaCreacion"				,title: 'UI_FECHA_CREACION'		, width: "50px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
				                    { field: "Usuario"						,title: 'UI_USUARIO_CREA'		, width: "50px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
				                    { field: "UsuarioModifica"				,title: 'UI_USUARIO_MODIFICA'		, width: "50px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
				                    { field: "FechaModificacion"			,title: 'UI_FECHA_MODIFICACION'		, width: "50px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },

				                    ],
			gridCreated: function(grid) {
				$scope.data.mainGrid = grid;
			},	    	
			sortable			: true,
			filterable			: true,
 			autoBind			: true,  //comentar
			height				: 440,
		    selectable 			: "row"
			
	};
	
	$scope.verDetalle = function(){
		/*
		var filters = new Array();
 
		if ($scope.conf.plantillasseleccion.IdPlantilla)
			filters.push({ field: "IdPlantilla", operator: "eq", value: $scope.conf.plantillasseleccion.IdPlantilla }); 
 		$scope.data.mainGrid.dataSource.filters(filters);
		*/
		$scope.ModificarPlantilla();
	};
	
	
	$scope.agregarPlantilla = function(){
		
		var id1 = bcsComunes.ventanaAbrir("BlotterAgregarPlantilla",{},false,false,idVentana);
		console.log("IDVENTANADNASD " + idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ });  
		
	};
	
	$scope.ModificarPlantilla = function(){
		
		var id1 = bcsComunes.ventanaAbrir("BlotterModificarPlantilla",{Plantilla : $scope.conf.plantillasseleccion},false,false,idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);
 		promesa.then(function(data){ });
		
	};
	$scope.Refrescar = function(){
		
		$scope.gridApiPlantillas.dataSource.read();
	};
 
 
});
