/*************************************************************
 * Purpose	: Controlador consulta pref.calce libro inventario
 * Author	: JGarces - BCS
 * Date		: 06-03-2018
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('PreferenciaCalceLibroInventarioCtrl',
	function($scope,$translate,bcsComunes,bcsAlerta,prefCalceLibroInventario){
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var pclinv = this;
		pclinv.win = {};
		pclinv.win.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
		
		pclinv.win.tipoCriterioNeteo = function() {
			var winModal = bcsComunes.ventanaAbrir("TipoCriterioNeteo", {
				dato:undefined}, false,false,pclinv.win.idVentana);
			var promesa = bcsComunes.ventanaPromesa(winModal);                               
            promesa.then(function(res){
            	pclinv.grid.dataSource.read();
            });	
		};
		
		pclinv.win.recalcular = function() {
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA'
			};
			bcsComunes.ventanaOverlay(pclinv.win.idVentana,true);
			prefCalceLibroInventario.calculaPreferenciaCalceLibroInventario(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(pclinv.win.idVentana,false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_OPERACION_REALIZADA'),
						conflictos : util.parseServerResponse(result)}, pclinv.win.idVentana);
					pclinv.grid.dataSource.read();
				},
				function(result){
					bcsComunes.ventanaOverlay(pclinv.win.idVentana,false);
					pclinv.grid.dataSource.read();
					bcsComunes.msg("error","Error",$translate.instant('UI_OPERACION_TIENE_ERRORES'));
				}
			);
		};
		
		
/*********************************************
 * DATASOURCES
 *********************************************/
			
/*********************************************
 * COMBOBOX
 *********************************************/
		
/*********************************************
 * GRILLAS
 *********************************************/	
		
		pclinv.gridSet = function(dataItem){
			return {
				dataSource		: new bcs.DataSource({
					type 		: "odata",
					definition 	: {
						serverFiltering : true,
						serverPaging 	: true,
						serverSorting 	: true,
						sortable 		: true,
						pageSize 		: 50,
						schema 			: {
							model	: {
			                    fields	: {
			                    	IdPreferenciaCalceLibroInventario : { type: "number"},
			                    	IdConcepto		: { type: "string"},
			                    	EsPagadoCon		: { type: "string"},
			                    	Preferencia		: { type: "number"},
			                    	IdEstado		: { type: "string"},	              	
		                        }
	                   		}
						},
						transport : {
							read : {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=100035",
								dataType : "json"
							}
						}
					}
				}),
				columns	: [
				    //{field: "IdPreferenciaCalceLibroInventario",width: "100px",title: $translate.instant("UI_ID"),attributes : {style : "text-align:left;"}},	
				    {field: "IdConcepto",width: "100px",title: $translate.instant("UI_CONCEPTO"),attributes : {style : "text-align:left;"}},
				    {field: "EsPagadoCon",width: "100px",title: $translate.instant("UI_PAGADO_CON"),attributes : {style : "text-align:left;"}},
				    {field: "Preferencia",width: "100px",title: $translate.instant("UI_PREFERENCIA"),attributes : {style : "text-align:left;"}},
				    //{field: "IdEstado",width: "100px",title: $translate.instant("UI_ID"),attributes : {style : "text-align:left;"}},
				 	
				],		
				sortable			: true,
				pageable			: true,
				autoBind			: true, 
				filterable			: true,
				height				: 350,
			    selectable 			: "row"
		    	/*excel : {
					allPages: true,
				},
				detailInit : function(e) {
			    	//
				}*/
			};
		};
		
		pclinv.grid = pclinv.gridSet();
		
		
	
/*********************************************
 * DATASOURCES
 *********************************************/	
		
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
//-------------------------- FIN CONTROLLER ----------------------

(function() {
	angular.module('ModuloPlataforma').factory('prefCalceLibroInventario',['$resource',function($resource) {
		var urlCalculaPreferenciaCalceLibroInventario = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100030";
		var urlCrearTipoCriterioNeteo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100032";
		var urlModificarTipoCriterioNeteo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100033";
		var urlEliminarTipoCriterioNeteo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100034";
		return $resource("", {},
		{
			'calculaPreferenciaCalceLibroInventario' : {method: "POST",url:urlCalculaPreferenciaCalceLibroInventario},
			'crearTipoCriterioNeteo' : {method: "POST",url:urlCrearTipoCriterioNeteo},
			'modificarTipoCriterioNeteo' : {method: "POST",url:urlModificarTipoCriterioNeteo},
			'eliminarTipoCriterioNeteo' : {method: "POST",url:urlEliminarTipoCriterioNeteo}
		});
	}]);
}()); 