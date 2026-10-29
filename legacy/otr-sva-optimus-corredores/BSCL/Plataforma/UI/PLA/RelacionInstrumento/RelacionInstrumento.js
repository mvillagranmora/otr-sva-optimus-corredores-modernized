/*************************************************************
 * Purpose	: Mantenedor de Relación de Instrumentos
 * Author	: JPZapata - BCS
 * Date		: Mayo-2016
 *************************************************************/

(function() {
	angular.module('ModuloPlataforma').controller('RelacionInstrumentoCtrl',function($scope,$translate,bcsComunes,$timeout,$state,bcsAlerta,$resource){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/

		var RelInst = this;
		RelInst.idVentana = bcsComunes.ventanaWinId($scope);
		RelInst.datosEntrada = bcsComunes.ventanaParams(RelInst.idVentana)
		
		RelInst.win = {};

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
	
		RelInst.win.administrarRelacionInstrumento = function(registro) {
			bcsComunes.ventanaOverlay(RelInst.idVentana,true);
			var winRelacionInstrumento = bcsComunes.ventanaAbrir("ModalRelacionInstrumento", {
				RegRelacionInstrumento:registro}, false,false);
			var promesa = bcsComunes.ventanaPromesa(winRelacionInstrumento);                               
            promesa.then(function(res){
            	$scope.apiGrillaRelacionInstrumento.dataSource.read();
            	bcsComunes.ventanaOverlay(RelInst.idVentana,false);
            });
		};
		
		RelInst.win.editarRelacionInstrumento = function(registro) {
			if(registro) RelInst.win.administrarRelacionInstrumento(registro);
		};

		
/*********************************************
 * COMBOBOX
 *********************************************/
			

/*********************************************
 * MODELO
 *********************************************/
	
	ModeloRelacionInstrumento = function() {
		return {
			model: {
    	        	fields:{
		                    	IdRelacionInstrumento			: { type: "number"},
		                    	IdTipoRelacionInstrumento		: { type: "string"},
		                    	TipoRelacionInstrumento			: { type: "string"},
		                    	IdInstrumento					: { type: "number"},
		                    	Nemo							: { type: "string"},
		                    	IdInstrumentoRelacionado		: { type: "number"},
		                    	NemoRelacionado					: { type: "string"},
		                    	FechaInicioRelacion				: { type: "date"},
		                    	FechaFinRelacion				: { type: "date"},
		                    	Descripcion						: { type: "string"},
		                    	IdEstado						: { type: "string"}
    	        	} 
			}
		};
	};

/*********************************************
 * DATASOURCES
 *********************************************/	
	dataSourceRelacionInstrumento =  new bcs.DataSource({
			type : "odata", 
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				pageSize : 50,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1042", //asignar ODATA
						dataType : "json"
					}
				},
				schema : ModeloRelacionInstrumento()
			}
		});
	//};
	
/*********************************************
 * COLUMNAS
 *********************************************/
	
	columnasRelacionInstrumento = function() {
		return [ 
			 	{field: "IdRelacionInstrumento",			width: "30px",	title: $translate.instant("UI_ID"),							align:'center'},
			 	{field: "TipoRelacionInstrumento",			width: "100px",	title: $translate.instant("UI_TIPO_RELACION_INSTRUMENTO"),	align:'left'},
			 	{field: "Nemo",								width: "70px",	title: $translate.instant("UI_NEMO"),						align:'left'},
			 	{field: "NemoRelacionado",					width: "70px",	title: $translate.instant("UI_NEMO_RELACIONADO"),			align:'left'},
			 	{field: "FechaInicioRelacion",				width: "70px",	title: $translate.instant("UI_FECHA_INICIO_RELACION"),		align:'left'},
			 	{field: "FechaFinRelacion",					width: "70px",	title: $translate.instant("UI_FECHA_FIN_RELACION"),			align:'left'},
			 	{field: "Descripcion",						width: "100px",	title: $translate.instant("UI_DESCRIPCION"),				align:'left'},
			 	{field: "IdEstado",							width: "30px",	title: $translate.instant("UI_ESTADO"),						align:'center'}			 	
				];
	};

 

	
/*********************************************
 * GRILLAS
 *********************************************/	
				
		$scope.grillaRelacionInstrumento =  {
			dataSource			: dataSourceRelacionInstrumento,
			columns				: columnasRelacionInstrumento(),
			gridCreated: function(grid) {
				$scope.apiGrillaRelacionInstrumento= grid;
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
	angular.module('ModuloPlataforma').factory('servicioRelacionInstrumento',['$resource',function($resource) {
		var urlSaveRelacionInstrumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=321";
		var urlUpdateRelacionInstrumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=322";
		return $resource("", {},
		{
			'saveRelacionInstrumento' : {method: "POST",url:urlSaveRelacionInstrumento},
			'updateRelacionInstrumento' : {method: "POST",url:urlUpdateRelacionInstrumento},
		});
	}]);
}()); 