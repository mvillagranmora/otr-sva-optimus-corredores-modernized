/*************************************************************
 * Purpose	: Mantenedor de Centro Costo
 * Author	: BCS
 * Date		: 02/04/2018
 *************************************************************/

(function() {
	angular.module('ModuloContabilidad').controller('AdministrarCentroCostoCtrl',function($scope,$translate,bcsComunes,$timeout,$state,bcsAlerta){
	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/

			var admCentroCosto = this;
			admCentroCosto.idVentana = bcsComunes.ventanaWinId($scope);
			admCentroCosto.datosEntrada = bcsComunes.ventanaParams(admCentroCosto.idVentana)
			
			admCentroCosto.win = {};

	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/	
		
	admCentroCosto.win.administrarCentroCosto = function(registro) {
		bcsComunes.ventanaOverlay(admCentroCosto.idVentana,true);
		var winCentroCosto = bcsComunes.ventanaAbrir("ModalAdministrarCentroCosto", {
			CentroCosto:registro}, false,false);
		var promesa = bcsComunes.ventanaPromesa(winCentroCosto);                               
        promesa.then(function(res){
        	bcsComunes.ventanaOverlay(admCentroCosto.idVentana,false);
        	admCentroCosto.apiGrillaCentroCosto.dataSource.read();
        });
	};
	admCentroCosto.win.editarCentroCosto = function(registro) {
		if(registro) admCentroCosto.win.administrarCentroCosto(registro);
	};

/*********************************************
 * MODELO
 *********************************************/
	
	ModeloCentroCosto = function() {
		return {
			model: {
    	        	fields:{
    	        		IdProductoConcepto	: { type: "string"},
    	        		ProductoConcepto	: { type: "string"},
    	        		IdRelacionado   	: { type: "string"},		
    	        		Relacionado   		: { type: "string"},
    	        		IdBolsa        		: { type: "string"},
    	        		Bolsa        		: { type: "string"},
    	        		TipoCentro  		: { type: "string"},
    	        		Business        	: { type: "string"},
    	        		Vehiculo        	: { type: "string"},
    	        		CentroBeneficio 	: { type: "string"},
    	        		Tipo   				: { type: "string"},
    	        	} 
			}
		};
	};	
/**********************************************
* DATASOURCES
*********************************************/	
					
		admCentroCosto.grillaCentroCosto =  new bcs.DataSource({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					serverPaging 	: true, 
					serverSorting 	: true, 
					pageSize 		: 50,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1816",
							dataType : "json"
						}
					},
					schema 			: ModeloCentroCosto(),
				}
			}
		);	

/*********************************************
 * COLUMNAS
 *********************************************/
	
columnasCentroCosto = function() {
	return [ 
		{field: "ProductoConcepto",	width: "250px",title: $translate.instant("UI_PRODUCTO_CONCEPTO"),	align:'left'},
		{field: "Relacionado",		width: "100px",title: $translate.instant("UI_RELACIONADO"),			align:'left'},
		{field: "Bolsa",			width: "200px",title: $translate.instant("UI_BOLSA"),				align:'left'},
		{field: "TipoCentro",		width: "100px",title: $translate.instant("UI_TIPO_CENTRO"),			align:'left'},
		{field: "Business",			width: "100px",title: $translate.instant("UI_BUSINESS"),			align:'left'},
		{field: "Vehiculo",			width: "100px",title: $translate.instant("UI_VEHICULO"),			align:'left'},		
		{field: "CentroBeneficio",	width: "300px",title: $translate.instant("UI_BENEFICIO"),			align:'left'},
		{field: "Tipo",				width: "100px",title: $translate.instant("UI_TIPO"),				align:'left'},
		];
};
			

/*********************************************
 * GRILLAS
 *********************************************/	
				
	$scope.grillaAdministraCentroCosto =  {
		dataSource			: admCentroCosto.grillaCentroCosto,
		columns				: columnasCentroCosto(),
		gridCreated: function(grid) {
			$scope.admCentroCosto.apiGrillaCentroCosto = grid;
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
	angular.module('ModuloContabilidad').factory('servicioCentroCosto',['$resource',function($resource) {
		var urlSaveCentroCosto = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1820";
		var urlUpdateCentroCosto = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1819";
		return $resource("", {},
		{
			'saveCentroCosto' : {method: "POST",url:urlSaveCentroCosto},
			'updateCentroCosto' : {method: "POST",url:urlUpdateCentroCosto},
		});
	}]);
}()); 