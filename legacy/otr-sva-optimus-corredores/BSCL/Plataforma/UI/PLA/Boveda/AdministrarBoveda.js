/*************************************************************
 * Purpose	: Mantenedor de Maestro de Bovedas
 * Author	: JGarces - BCS
 * Date		: 02/05/2016
 *************************************************************/

(function() {
	angular.module('ModuloPlataforma').controller('AdministrarBovedaCtrl',function($scope,$translate,bcsComunes,$timeout,$state,bcsAlerta){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/

		var admBvd = this;
		admBvd.idVentana = bcsComunes.ventanaWinId($scope);
		admBvd.datosEntrada = bcsComunes.ventanaParams(admBvd.idVentana)
		
		admBvd.win = {};

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
	
		admBvd.win.administrarBoveda = function(registro) {
			bcsComunes.ventanaOverlay(admBvd.idVentana,true);
			var winBoveda = bcsComunes.ventanaAbrir("ModalAdministrarBoveda", {
				boveda:registro}, false,false);
			var promesa = bcsComunes.ventanaPromesa(winBoveda);                               
            promesa.then(function(res){
            	bcsComunes.ventanaOverlay(admBvd.idVentana,false);
            	admBvd.apiGrillaBoveda.dataSource.read();
            });
		};
		
		admBvd.win.editarBoveda = function(registro) {
			if(registro) admBvd.win.administrarBoveda(registro);
		};

/*********************************************
 * COMBOBOX
 *********************************************/

		
/*********************************************
 * MODELO
 *********************************************/
	
	ModeloBovedas = function() {
		return {
			model: {
    	        	fields:{
		                    	IdBoveda				: { type: "string"},
		                    	Codigo					: { type: "string"},
		                    	Nombre					: { type: "string"},
		                    	IdEstado				: { type: "string"},
		                    	NombreEstado			: { type: "string"},
		                    	IdPersona				: { type: "number"},
		                    	NombreCompletoPersona	: { type: "string"},
		                    	DocumentoIdentidad		: { type: "string"},
		                    	IdSocioDeNegocio		: { type: "number"},
    	        	} 
			}
		};
	};
		
/*********************************************
 * DATASOURCES
 *********************************************/	
				
	admBvd.grillaBoveda =  new bcs.DataSource({
			type : "odata", 
			definition : {
				serverFiltering : true, 
				serverPaging 	: true, 
				serverSorting 	: true, 
				pageSize 		: 50,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1475",
						dataType : "json"
					}
				},
				schema 			: ModeloBovedas(),
			}
		}
	);

/*********************************************
 * COLUMNAS
 *********************************************/
		
	columnasBovedas = function() {
		return [ 
			 	{field: "IdBoveda",					width: "100px",title: $translate.instant("UI_ID"),					align:'center'},
			 	{field: "Codigo",					width: "100px",title: $translate.instant("UI_CODIGO"),				align:'center'},
			 	{field: "Nombre",					width: "100px",title: $translate.instant("UI_NOMBRE"),				align:'left'},
			 	{field: "NombreEstado",				width: "100px",title: $translate.instant("UI_ESTADO"),				align:'center'},
			 	{field: "NombreCompletoPersona",	width: "100px",title: $translate.instant("UI_PERSONA"),				align:'left'},
			 	{field: "DocumentoIdentidad",		width: "100px",title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),	align:'left'},			 	
				];
	};
		
/*********************************************
 * GRILLAS
 *********************************************/	
				
	$scope.grillaAdministraBovedas =  {
		dataSource			: admBvd.grillaBoveda,
		columns				: columnasBovedas(),
		gridCreated: function(grid) {
			$scope.admBvd.apiGrillaBoveda = grid;
		},
		sortable			: true,
		pageable			: true,
		autoBind			: true,
		height				: 280,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
	    selectable 			: "row"
	};

/*********************************************
 * FUNCIONES
 *********************************************/
		
		
/*********************************************
 * WATCHERS
 ********************************************/

		
/*********************************************
 * GOOOOOOOOO!
 *********************************************/

		
	});
}());

//---------------------------------------------- FIN CONTROLADOR PRINCIPAL ----------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioBoveda',['$resource',function($resource) {
		var urlSaveBoveda = "/BSCL/Plataforma/GWJAVA/PLA/call?t=310";
		var urlUpdateBoveda = "/BSCL/Plataforma/GWJAVA/PLA/call?t=311";
		return $resource("", {},
		{
			'saveBoveda' : {method: "POST",url:urlSaveBoveda},
			'updateBoveda' : {method: "POST",url:urlUpdateBoveda},
		});
	}]);
}()); 