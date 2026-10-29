/*************************************************************
 * Purpose	: Mantenedor de Clase Emisor
 * Author	: BCS
 * Date		: 02/04/2018
 *************************************************************/

(function() {
	angular.module('ModuloContabilidad').controller('AdministrarClaseEmisorCtrl',function($scope,$translate,bcsComunes,$timeout,$state,bcsAlerta){
	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/

			var admClaseEmisor = this;
			admClaseEmisor.idVentana = bcsComunes.ventanaWinId($scope);
			admClaseEmisor.datosEntrada = bcsComunes.ventanaParams(admClaseEmisor.idVentana)
			
			admClaseEmisor.win = {};

	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/	
		
	admClaseEmisor.win.administrarClaseEmisor = function(registro) {
		bcsComunes.ventanaOverlay(admClaseEmisor.idVentana,true);
		var winClaseEmisor = bcsComunes.ventanaAbrir("ModalAdministrarClaseEmisor", {
			ClaseEmisor:registro}, false,false);
		var promesa = bcsComunes.ventanaPromesa(winClaseEmisor);                               
        promesa.then(function(res){
        	bcsComunes.ventanaOverlay(admClaseEmisor.idVentana,false);
        	admClaseEmisor.apiGrillaClaseEmisor.dataSource.read();
        });
	};
	admClaseEmisor.win.editarClaseEmisor = function(registro) {
		if(registro) admClaseEmisor.win.administrarClaseEmisor(registro);
	};

/*********************************************
 * MODELO
 *********************************************/
	
	ModeloClaseEmisor = function() {
		return {
			model: {
    	        	fields:{
                    	TipoOperacion				: { type: "string"},
                    	IdClasificacionInstrumento	: { type: "string"},		
                    	ClasificacionInstrumento	: { type: "string"},

                    	IdTipoBonoIRF				: { type: "string"},
                    	TipoBonoIRF        			: { type: "string"},

                    	IdOrigenInstrumento  		: { type: "string"},
                    	OrigenInstrumento  			: { type: "string"},

                    	IdPlaza              		: { type: "string"},
                    	Plaza              			: { type: "string"},

                    	IdCodigoIFRS         		: { type: "string"},
                    	CodigoIFRS         			: { type: "string"},

                    	RelacionadoGrupo   			: { type: "string"},

                    	IdPresenciaBursatil  		: { type: "string"},
                    	PresenciaBursatil  			: { type: "string"},

                    	FormadorMercado    			: { type: "string"},

                    	IdPactoAndino        		: { type: "string"},
                    	PactoAndino        			: { type: "string"},

                    	ClaseEmisor        			: { type: "string"},

                    	LargoPlazo         			: { type: "string"},        	        	

                    } 
			}
		};
	};
	
/**********************************************
* DATASOURCES
*********************************************/	
					
		admClaseEmisor.grillaClaseEmisor =  new bcs.DataSource({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					serverPaging 	: true, 
					serverSorting 	: true, 
					pageSize 		: 50,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1815",
							dataType : "json"
						}
					},
					schema 			: ModeloClaseEmisor(),
				}
			}
		);	

/*********************************************
 * COLUMNAS
 *********************************************/
	
columnasClaseEmisor = function() {
	return [ 
		{field: "TipoOperacion",				width: "100px",title: $translate.instant("UI_TIPO_OPERACION"),			align:'left'},
		{field: "ClasificacionInstrumento",		width: "120px",title: $translate.instant("UI_CLASE_INSTRUMENTO"),		align:'left'},
		{field: "TipoBonoIRF",					width: "120px",title: $translate.instant("UI_TIPO_BONO_IRF"),			align:'left'},
		{field: "OrigenInstrumento",			width: "100px",title: $translate.instant("UI_TIPO_INSTRUMENTO"),		align:'left'},
		{field: "Plaza",						width: "100px",title: $translate.instant("UI_PLAZA"),					align:'left'},
		{field: "CodigoIFRS",					width: "160px",title: $translate.instant("UI_CODIGO_IFRS"),				align:'left'},
		{field: "RelacionadoGrupo",				width: "100px",title: $translate.instant("UI_RELACIONADO_GRUPO"),		align:'left'},
		{field: "PresenciaBursatil",			width: "100px",title: $translate.instant("UI_PRESENCIA_BURSATIL"),		align:'left'},
		{field: "FormadorMercado",				width: "100px",title: $translate.instant("UI_FORMADOR_MERCADO"),		align:'left'},
		{field: "PactoAndino",					width: "100px",title: $translate.instant("UI_PACTO_ANDINO"),			align:'left'},
		{field: "ClaseEmisor",					width: "100px",title: $translate.instant("UI_CLASE_EMISOR"),			align:'left'},
		{field: "LargoPlazo",					width: "100px",title: $translate.instant("UI_LARGO_PLAZO"),				align:'left'},				
		];
};
			

/*********************************************
 * GRILLAS
 *********************************************/	
				
	$scope.grillaAdministraClaseEmisor =  {
		dataSource			: admClaseEmisor.grillaClaseEmisor,
		columns				: columnasClaseEmisor(),
		gridCreated: function(grid) {
			$scope.admClaseEmisor.apiGrillaClaseEmisor = grid;
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
	angular.module('ModuloContabilidad').factory('servicioClaseEmisor',['$resource',function($resource) {
		var urlSaveClaseEmisor = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1817";
		var urlUpdateClaseEmisor = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1818";
		return $resource("", {},
		{
			'saveClaseEmisor' : {method: "POST",url:urlSaveClaseEmisor},
			'updateClaseEmisor' : {method: "POST",url:urlUpdateClaseEmisor},
		});
	}]);
}()); 