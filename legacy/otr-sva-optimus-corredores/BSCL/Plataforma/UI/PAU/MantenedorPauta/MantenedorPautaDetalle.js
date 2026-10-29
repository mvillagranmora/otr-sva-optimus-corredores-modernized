/**********************************************
 * Purpose	: Mantenedor de agenda job detalle
 * Author	: JG
 * Date		: 30/11/2016
 *********************************************/
(function() {
	angular.module('ModuloPlataforma').controller('MantenedorPautaDetalleCtrl',
	function($scope,bcsComunes,$translate,bcsAlerta){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var idVentana 	= bcsComunes.ventanaWinId($scope); 
		var parametros 	= bcsComunes.ventanaParams(idVentana);
		var mpautadet 	= this;
		mpautadet.conf 	= {};
		mpautadet.filtros	= {};
		mpautadet.datos		= {};
		mpautadet.cmbbx 	= {};
		mpautadet.win 		= {};
		mpautadet.fn 		= {};
		mpautadet.grid 		= {};
		mpautadet.datos		= {};
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/

		/*********************************************
		 * GRILLAS
		 *********************************************/
		var gridControlPauta = {};
		mpautadet.grid.controlPauta = {
			dataSource	: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering 	: true,
					serverPaging 		: true,
					serverSorting 		: true,
					sort 				: [{ field: "IdControlPauta", dir: "asc" }],
					pageSize 			: 50,
					transport: {
	                	read: {
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3142",
	                		dataType: "json"
	                	}
	            	},
	            	filter : [{field: "IdPrograma", operator: "eq", value: parametros.IdPrograma}],
					schema	: {
						model	: {
							fields	: {
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
								NombreEstado				: {type: "string"},
								Mensaje						: {type: "string"},
								IdPrograma					: {type: "number"},
								IdGrupoParametro			: {type: "number"},
								IdAgenda					: {type: "number"},
								Intento						: {type: "number"},
								IdUsuario					: {type: "number"},
								IdEstadoPrograma			: {type: "string"},
								NombreEstadoPrograma		: {type: "string"},
								ResultadoPrograma			: {type: "string"},
								FechaCreacionPrograma		: {type: "datetime"},
								FechaEjecucionPrograma		: {type: "sysdatetime"},
								FechaFinPrograma			: {type: "datetime"},
								FechaSistemaPrograma		: {type: "sysdatetime"},
								IdProgramaReprocesado		: {type: "number"},
							}
						}
					}
				}
			}),
			columns		: [
				{ field: "IdControlPauta"			, title: 'UI_ID'					, width: "40px"},
				{ field: "NombreEstado"				, title: 'UI_ESTADO'				, width: "120px"},
				{ field: "Mensaje"					, title: 'UI_MENSAJE'				, width: "300px"},
				{ field: "FechaCreacion"			, title: 'UI_FECHA_CREACION'		, width: "100px"},
			 ],		
			sortable	: true,
			filterable	: true,
			pageable	: true,
			autoBind	: false,
			height		: 250,
		    selectable 	: "row",
		    gridCreated: function(grid) {
		    	gridControlPauta = grid;
		    	mpautadet.win.actualizar();
			},
		};	
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		mpautadet.win.actualizar = function(){
			gridControlPauta.dataSource.query().then(function(){
				var aux  = util.dateDS(gridControlPauta.dataSource.data()[0].FechaEjecucionPrograma,"FechaHoraSistema","ODATA");
				mpautadet.datos.FechaEjecucionPrograma =util.formatoSecondate(aux);
			});
		}
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		/*********************************************
		 * GO!!!
		 *********************************************/

	});
}());
//------------------------- FIN CONTROLLER --------------------------
