angular.module('ModuloPlataforma').controller('ConsultarDetalleEvento',function($rootScope,$scope,$http,bcsServicioPlataforma,bcsMantenedorEventosGrid,bcsComunes,$translate,bcsAlerta,$timeout){
	
	$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	$scope.conf = {}; 
					
 	$scope.idVentana = bcsComunes.ventanaWinId($scope);
    

	var dataSourceNotificados = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "IdEvento", dir: "asc" },
			pageSize : 20,   
		    transport: {
				read: {					
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290622", //  4290622|BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDestinatarios
					dataType: "json"
				}
			},
			schema : 
				{
					model: {
						fields : {				
							 IdOrigenEvento					: {type: "number"}
							,IdEvento						: {type: "number"}
							,Direccion	 					: {type: "string"}
							,TipoDestinatario				: {type: "string"}
			
						}
					}
			}
		}
	});
	
	
	$scope.Notificados = {
			dataSource			: dataSourceNotificados,
			columns				: bcsMantenedorEventosGrid.columnasConsultarNotificados(),			
			filterable			: true,
			autoBind			: false,  
			height				: 400,
			pageable			: true,
		    selectable 			: "row",
		    gridCreated			: function(grid) {
										mainGridOptions101 = grid;
										$scope.gridOptions101 = grid;
										$scope.buscarNotificados($scope.parametros.IdOrigenEvento);
										return grid;
									},
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			}
		}; 


	$scope.buscarNotificados = function(id){     
		
		var filters = new Array();
		filters.push({ field: "IdOrigenEvento", operator: "eq", value: id});
		
 		$scope.Notificados.dataSource.filter(filters);		
	};
	

	
	

	
	function htmlDecode(input){
		var e = document.createElement('div');
		e.innerHTML = input;
		return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
	}; 
	
	$scope.verDetalleNotificacion2 = function(data){
		console.log(data);
		var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
		$timeout(function () {
			OpenWindow.init(htmlDecode(data.MensajeBody))
		}, 500);
	}


	
});
