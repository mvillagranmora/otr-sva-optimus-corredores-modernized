/******************************************
 * Purpose	: Mantenedor de agenda job
 * Author	: JG
 * Date		: 30/11/2016
 *****************************************/
(function() {
	angular.module('ModuloPlataforma').controller('MantenedorPautaCtrl',
	function($scope,bcsComunes,$translate,bcsAlerta){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var idVentana 	= bcsComunes.ventanaWinId($scope); 
		var parametros 	= bcsComunes.ventanaParams(idVentana);
		var mpauta 		= this;
		mpauta.conf 	= {};
		mpauta.filtros	= {};
		mpauta.datos	= {};
		mpauta.cmbbx 	= {};
		mpauta.win 		= {}; 
		mpauta.grid 	= {};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		mpauta.cmbbx.tipoPrograma ={
			idActual : false,
			seleccionado : false
		};
		mpauta.cmbbx.tipoPrograma.filtros = {
			EsPauta : {field : "EsPauta",	operator : "eq", value : 'S' },	
		}
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		mpauta.grid.pauta = {
			dataSource	: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering 	: true,
					serverPaging 		: true,
					serverSorting 		: true,
					sort 				: [
					     				  	{ field: "FechaEjecucionPrograma", dir: "desc" },
					     				  	{ field: "IdPrograma", dir: "asc" },
					     				  ],
					pageSize 			: 50,
					transport: {
	                	read: {
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3141",
	                		dataType: "json"
	                	}
	            	},
					schema 				: {
						model: {
							fields : {
								IdTipoPrograma				: {type: "string"},
								Descripcion					: {type: "string"},
								MaximoIntentos				: {type: "number"},
								Modulo						: {type: "string"},
								NombreClasePrograma			: {type: "string"},
								TipoClasePrograma			: {type: "string"},
								EsPauta						: {type: "string"},
								Ejecucion					: {type: "string"},
								RequiereTipoPrograma		: {type: "string"},
								SiguienteTipoPrograma		: {type: "string"},
								IdControlPauta				: {type: "number"},
								FechaSistema				: {type: "datetime"},
								FechaCreacion				: {type: "datetime"},
								IdEstado					: {type: "string"},
								Mensaje						: {type: "string"},
								IdPrograma					: {type: "number"},
								IdGrupoParametro			: {type: "number"},
								IdAgenda					: {type: "number"},
								Intento						: {type: "number"},
								IdUsuario					: {type: "number"},
								IdEstadoPrograma			: {type: "string"},
								ResultadoPrograma			: {type: "string"},
								FechaCreacionPrograma		: {type: "datetime"},
								FechaEjecucionPrograma		: {type: "datetime"},
								FechaFinPrograma			: {type: "datetime"},
								FechaSistemaPrograma		: {type: "datetime"},
								IdProgramaReprocesado		: {type: "number"},
							}
						}
					}
				}
			}),
			columns		: [
	            { field: "IdTipoPrograma"			,title: 'UI_TIPO_PROGRAMA'				, width: "150px"},
	            { field: "Descripcion"				,title: 'UI_DESCRIPCION'				, width: "220px"},
	            { field: "FechaEjecucionPrograma"	,title: 'UI_FECHA_EJECUCION'				, width: "120px"},
	            { field: "IdEstado"					,title: 'UI_ESTADO'						, width: "50px"},
	            { field: "Mensaje"					,title: 'UI_MENSAJE'					, width: "280px"},
	            { field: "IdPrograma"				,title: 'UI_IDPROGRAMA'					, width: "80px"},
	            { field: "RequiereTipoPrograma"		,title: 'UI_REQUIERE_TIPO_PROGRAMA'		, width: "140px"},
	            { field: "SiguienteTipoPrograma"	,title: 'UI_SIGUIENTE_TIPO_PROGRAMA'	, width: "140px"},
	            { field: "NombreClasePrograma"		,title: 'UI_CLASE_PROGRAMA'				, width: "350px"},
            ],		
			sortable	: true,
			filterable	: true,
			pageable	: true,
			autoBind	: false,
			height		: 500,
		    selectable 	: "row",
		    gridCreated: function(grid) {
		    	mpauta.datos.Fecha = util.dateInicioDia($scope.$root.SesionFechaSistema);
		    	mpauta.win.buscar();
			},
			rowColorBackground: function(row) {
				var color = "";
				switch(row.get("IdEstado")) {
					case 'TER':
						color = "green";
						break;
					/*case 'PEN':
						color = "yellow";
						break;*/
					case 'ERR':
						color = "red";
						break;
				}
				return color;
			},
		};	
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		mpauta.win.buscar = function(){
			var filters = new Array();	
			if (mpauta.datos.Fecha){
				filters.push({field: "FechaSistemaPrograma", operator: "gte", value: util.dateInicioDia(mpauta.datos.Fecha)});
				filters.push({field: "FechaSistemaPrograma", operator: "lte", value: util.dateTerminoDia(mpauta.datos.Fecha)});
			}
			mpauta.grid.pauta.dataSource.filters(filters);
		};
		 
		mpauta.win.limpiar = function (){
			mpauta.datos.Fecha = undefined;
			mpauta.grid.pauta.dataSource.filters([{field: "IdTipoPrograma", operator: "eq", value: ''}]);
		};
		
		mpauta.win.pautaDetalle = function(IdPrograma){
			var id1 = bcsComunes.ventanaAbrir("MantenedorPautaDetalle",{ IdPrograma: IdPrograma},false, false, idVentana); 
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(data){ 
            	mpauta.win.buscar();
             }); 
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
	 	
		/*********************************************
		 * GO!!!
		 *********************************************/
		mpauta.win.limpiar();
	});
}());
//------------------------- FIN CONTROLLER --------------------------
