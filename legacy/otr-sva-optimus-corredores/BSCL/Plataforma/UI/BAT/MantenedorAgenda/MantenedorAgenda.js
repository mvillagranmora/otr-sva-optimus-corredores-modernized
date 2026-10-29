/******************************************
 * Purpose	: Mantenedor de agenda job
 * Author	: JG
 * Date		: 30/11/2016
 *****************************************/
(function() {
	angular.module('ModuloPlataforma').controller('MantenedorAgendaCtrl',
	function($scope,bcsComunes,$translate,bcsAlerta){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var idVentana 	= bcsComunes.ventanaWinId($scope); 
		var parametros 	= bcsComunes.ventanaParams(idVentana);
		var magenda 	= this;
		magenda.conf 	= {};
		magenda.filtros	= {};
		magenda.datos	= {};
		magenda.cmbbx 	= {};
		magenda.win 	= {};
		magenda.grid 	= {};
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		magenda.cmbbx.tipoPrograma ={
			idActual : false,
			seleccionado : false
		};
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		magenda.grid.agenda = {
			dataSource	: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering 	: true,
					serverPaging 		: true,
					serverSorting 		: true,
					//sort 				: { field: "fechaEjecucion", dir: "desc" },
					pageSize 			: 50,
					transport: {
	                	read: {
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3139",
	                		dataType: "json"
	                	}
	            	},
					schema 				: {
						model: {
							fields : {
								IdAgenda 					: {type: "number"},
								IdTipoPrograma				: {type: "number"},
								EsDemonio					: {type: "string"},
								DiaDeMes					: {type: "string"},
								DiaDeSemana					: {type: "string"},
								Mes							: {type: "string"},
								Hora						: {type: "number"},
								Minuto						: {type: "number"},
								SoloDiasHabiles				: {type: "string"},
								SoloPrincipioMes			: {type: "string"},
								SoloFinDeMes				: {type: "string"},
								habilitado					: {type: "string"},
								NombreParametro				: {type: "string"},
								Valor						: {type: "string"},
								SiguienteFechaDeEjecucion	: {type: "datetime"},
								UltimaFechaDeEjecucion		: {type: "datetime"},//sysdatetime //datetime
								DescripcionTipoPrograma		: {type: "string"},
								EsPauta						: {type: "string"},
								//---
							 	/*IdPrograma 			: {type: "number"},
								IdGrupoParametro 	: {type: "number"},
								IdAgenda 			: {type: "number"},
								IdTipoTarea 		: {type: "string"},
								Intento 			: {type: "number"},
								IdUsuario 			: {type: "string"},
								idEstado			: {type: "string"},
								Resultado			: {type: "string"},
								fechaCreacion		: {type: "datetime"},
								fechaEjecucion		: {type: "sysdatetime"},
								fechaFin			: {type: "sysdatetime"},
								Nombre				: {type: "string"}, //sysdatetime //datetime
								Descripcion			: {type: "string"},
								CodigoUsuario		: {type: "string"},*/
							}
						} 
					}
				}
			}),
			columns		: [
	            { field: "IdAgenda"						,title: 'UI_AGENDA'						, width: "80px"},
	            { field: "DescripcionTipoPrograma"		,title: 'UI_TIPO_PROGRAMA'				, width: "250px"},
	            { field: "EsPauta"						,title: 'UI_ES_PAUTA'					, width: "100px"},
	            { field: "EsDemonio"					,title: 'UI_DEMONIO'					, width: "100px"},
	            { field: "habilitado"					,title: 'UI_HABILITADO'					, width: "100px"},
	            { field: "SiguienteFechaDeEjecucion"	,title: 'UI_SIGUIENTE_FECHA_EJECUCION'	, width: "120px"},
	            { field: "UltimaFechaDeEjecucion"		,title: 'UI_ULTIMA_FECHA_EJECUCION'		, width: "120px"},
	            { field: "DiaDeMes"						,title: 'UI_DIA_DE_MES'					, width: "100px"},
	            { field: "DiaDeSemana"					,title: 'UI_DIA_DE_SEMANA'				, width: "100px"},
	            { field: "Mes"							,title: 'UI_MES'						, width: "100px"},
	            { field: "Hora"							,title: 'UI_HORA'						, width: "100px"},
	            { field: "Minuto"						,title: 'UI_MINUTO'						, width: "100px"},
	            { field: "SoloDiasHabiles"				,title: 'UI_SOLO_DIAS_HABILES'			, width: "100px"},
	            { field: "SoloPrincipioMes"				,title: 'UI_SOLO_PRINCIPIO_MES'			, width: "100px"},
	            { field: "SoloFinDeMes"					,title: 'UI_SOLO_FIN_MES'				, width: "100px"},
	            /*//----------------------------------------------
	            { field: "Descripcion"			,title: 'UI_TIPO_PROGRAMA'		, width: "80px"},
	            { field: "CodigoUsuario"		,title: 'UI_USUARIO'			, width: "44px"},
	            { field: "fechaCreacion"		,title: 'UI_FECHA_CREACION'		, width: "64px"},
	            { field: "fechaEjecucion"		,title: 'UI_FECHA_INICIO'		, width: "64px"},
	            { field: "fechaFin"				,title: 'UI_FECHA_TERMINO'		, width: "64px"},
	            { field: "Nombre"				,title: 'UI_ESTADO'				, width: "50px"},
	            { field: "Resultado"			,title: 'UI_DESCRIPCION'		, width: "50px"},*/
            ],		
			sortable	: true,
			filterable	: true,
			pageable	: true,
			autoBind	: false,
			height		: 450,
		    selectable 	: "row",
		    gridCreated: function(grid) {
				grid.dataSource.filters([{field: "IdTipoPrograma", operator: "eq", value: ''}]);
			},
		};	
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		magenda.win.buscar = function(){
			var filters = new Array();	
			if (magenda.datos.seleccionadoTipoPrograma && magenda.datos.seleccionadoTipoPrograma.IdTipoPrograma){
				filters.push({field: "IdTipoPrograma", operator: "eq", value: magenda.datos.seleccionadoTipoPrograma.IdTipoPrograma});
			}
			if (magenda.datos.seleccionadoEstadoPrograma && magenda.datos.seleccionadoEstadoPrograma.IdEstadoPrograma) {
				filters.push({field: "IdEstado", operator: "eq", value: magenda.datos.seleccionadoEstadoPrograma.IdEstadoPrograma});
			}
			if (magenda.conf.kngFechaDesde){
				var resp = util.validarRangoFecha(magenda.datos.FechaDesde, magenda.datos.FechaHasta);
				if(resp.estado){
					bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
					return;
				}
				filters.push({field: "fechaCreacion", operator: "gte", value: util.date(magenda.datos.FechaDesde).toDate()});
			}
			if (magenda.conf.kngFechaHasta) {
				filters.push({field: "fechaCreacion", operator: "lte", value: util.date(magenda.datos.FechaHasta).toDate()});
			}
			
			magenda.grid.agenda.dataSource.filters(filters);
		};
		 
		magenda.win.limpiar = function (){
			magenda.datos.IdTipoPrograma 	= 0;
			magenda.datos.IdEstadoPrograma 	= 0;
			magenda.datos.FechaDesde = undefined;
			magenda.datos.FechaHasta = undefined;
			magenda.grid.agenda.dataSource.filters([{field: "IdTipoPrograma", operator: "eq", value: ''}]);
			//magenda.datos.FechaDesde 		= util.dateInicioDia($scope.$root.SesionFechaSistema);
			//magenda.datos.FechaHasta 		= util.dateTerminoDia($scope.$root.SesionFechaSistema);
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
	 	
		/*********************************************
		 * GO!!!
		 *********************************************/
		magenda.win.limpiar();
	});
}());
//------------------------- FIN CONTROLLER --------------------------
