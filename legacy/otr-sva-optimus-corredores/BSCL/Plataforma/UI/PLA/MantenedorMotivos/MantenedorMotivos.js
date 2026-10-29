/*************************************************************
 * Purpose	: Mantenedor de Motivos
 * Author	: GCuevas - BCS
 * Date		: 01/12/2021
 *************************************************************/

(function() {
	angular.module('ModuloPlataforma').controller('MantenedorMotivosCtrl',function($scope,$translate,bcsComunes,$timeout,$state,bcsAlerta){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/

		var manMot = this;
		manMot.idVentana = bcsComunes.ventanaWinId($scope);
		manMot.datosEntrada = bcsComunes.ventanaParams(manMot.idVentana)
		
		manMot.win = {};

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
	
		manMot.win.crearMotivo = function(registro) {
			bcsComunes.ventanaOverlay(manMot.idVentana,true);
			var winMotivo = bcsComunes.ventanaAbrir("ModalMantenedorMotivos", {
				motivo:registro}, false,false);
			var promesa = bcsComunes.ventanaPromesa(winMotivo);                               
            promesa.then(function(res){
            	bcsComunes.ventanaOverlay(manMot.idVentana,false);
            	manMot.apiGrillaMotivo.dataSource.read();
            });
		};
		
		manMot.win.editarMotivo = function(registro) {
			if(registro) {
				if(registro.Tipo=="M")
					manMot.win.crearMotivo(registro);
				else
					bcsComunes.msg("error","MOTIVO",'No se pueden modificar motivos de tipo Automatico');
			}
		};

/*********************************************
 * COMBOBOX
 *********************************************/

		
/*********************************************
 * MODELO
 *********************************************/
	
	ModeloMotivos = function() {
		return {
			model: {
    	        	fields:{
    	        		IdEstado				: { type: "string"},
	    				Estado					: { type: "string"},
	    				Tipo					: { type: "string"},
	    				Motivo					: { type: "string"},
	    				Activo					: { type: "string"}
    	        	} 
			}
		};
	};
		
/*********************************************
 * DATASOURCES
 *********************************************/	
				
	manMot.grillaMotivo =  new bcs.DataSource({
			type : "call", 
			definition : {
				callTransaction : "700379",
				serverFiltering : false,
				serverPaging 	: false,
				serverSorting 	: false,
				callParameters  : {},
				callResultSet 	: "P_OUT_MOTIVOS",
				sort			: [{ field: "Estado", dir: "asc"},
									{ field: "Tipo", dir: "asc"},
									{ field: "Motivo", dir: "asc"}],
				pageSize 		: 200,	
				schema 			: ModeloMotivos()
			}
		}
	);

/*********************************************
 * COLUMNAS
 *********************************************/
		
	columnasMotivos = function() {
		return [
			 	{field: "Estado",					width: "60px",title: $translate.instant("UI_ESTADO"),				align:'center'},
			 	{field: "Tipo",						width: "40px",title: $translate.instant("UI_TIPO"),				align:'center'},
			 	{field: "Motivo",					width: "200px",title: $translate.instant("UI_MOTIVO"),				align:'left'},
			 	{field: "Activo",					width: "40px",title: $translate.instant("UI_ACTIVO"),				align:'center'}			 	
				];
	};
		
/*********************************************
 * GRILLAS
 *********************************************/	
				
	$scope.grillaMantenedorMotivos =  {
		dataSource			: manMot.grillaMotivo,
		columns				: columnasMotivos(),
		gridCreated: function(grid) {
			$scope.manMot.apiGrillaMotivo = grid;
		},
		sortable			: true,
		pageable			: true,
		autoBind			: true,
		filterable			: true,
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
		
	});
}());

//---------------------------------------------- FIN CONTROLADOR PRINCIPAL ----------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioMotivo',['$resource',function($resource) {
		var urlSaveMotivo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700380";
		var urlUpdateMotivo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700381";
		return $resource("", {},
		{
			'saveMotivo' : {method: "POST",url:urlSaveMotivo},
			'updateMotivo' : {method: "POST",url:urlUpdateMotivo},
		});
	}]);
}()); 