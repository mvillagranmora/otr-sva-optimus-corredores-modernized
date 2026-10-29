/*************************************************************
 * Purpose	: Controlador tipo criterio neteo
 * Author	: JGarces - BCS
 * Date		: 06-03-2018
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('TipoCriterioNeteoCtrl',
	function($scope,$translate,bcsComunes,bcsAlerta,servicioTipoCriterioNeteo){
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var tCriNet = this;
		tCriNet.win = {};
		tCriNet.win.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
		tCriNet.win.crear = function() {
			var winModal = bcsComunes.ventanaAbrir("ModalTipoCriterioNeteo", {
				dato:undefined}, false,false,tCriNet.win.idVentana);
			var promesa = bcsComunes.ventanaPromesa(winModal);                               
            promesa.then(function(res){
            	tCriNet.grid.dataSource.read();
            });
		};
		
		tCriNet.win.editar = function(registro) {
			if(registro&&registro.Editable!="S"){
				bcsComunes.msg("warning","Editar","El registro no es editable");
				return;
			}
			if(registro){
				var winModal = bcsComunes.ventanaAbrir("ModalTipoCriterioNeteo", {
					dato:registro}, false,false,tCriNet.win.idVentana);
				var promesa = bcsComunes.ventanaPromesa(winModal);                               
	            promesa.then(function(res){
	            	tCriNet.grid.dataSource.read();
	            });	
			}
		};
		
		tCriNet.win.eliminar = function(registro) {
			if(registro&&registro.Editable!="S"){
				bcsComunes.msg("warning","Eliminar","El registro no es editable");
				return;
			}
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA',
				p_IdTipoCriterioNeteo : registro.IdTipoCriterioNeteo
			};
			bcsComunes.ventanaOverlay(tCriNet.win.idVentana,true);
			servicioTipoCriterioNeteo.eliminarTipoCriterioNeteo(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(tCriNet.win.idVentana,false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_DATOS_GUARDADOS'),
						conflictos : util.parseServerResponse(result)}, tCriNet.win.idVentana);
					tCriNet.grid.dataSource.read();
				},
				function(result){
					bcsComunes.ventanaOverlay(tCriNet.win.idVentana,false);
					tCriNet.grid.dataSource.read();
					bcsComunes.msg("error","Error",$translate.instant('UI_OPERACION_TIENE_ERRORES'));
				}
			);
		};
		
		tCriNet.win.recalcular = function() {
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA'
			};
			bcsComunes.ventanaOverlay(tCriNet.win.idVentana,true);
			servicioTipoCriterioNeteo.calculaPreferenciaCalceLibroInventario(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(tCriNet.win.idVentana,false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_OPERACION_REALIZADA'),
						conflictos : util.parseServerResponse(result)}, tCriNet.win.idVentana);
					tCriNet.grid.dataSource.read();
				},
				function(result){
					bcsComunes.ventanaOverlay(tCriNet.win.idVentana,false);
					tCriNet.grid.dataSource.read();
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
		
		tCriNet.gridDef = function(dataItem){
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
			                    	IdTipoCriterioNeteo			: { type: "number",editable: false 	},
			                    	NombreTipoCriterioNeteo		: { type: "string",editable: false 	},
			                    	SecuenciaNeteo				: { type: "number",editable: false	},
			                    	Editable					: { type: "string",editable: false	}
		                        }
	                   		}
						},
						transport : {
							read : {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=100031",
								dataType : "json"
							}
						}
					}
				}),
				columns	: [
				    {field: "NombreTipoCriterioNeteo",width: "80%",title: $translate.instant("UI_NOMBRE"),attributes : {style : "text-align:left;"}},	
				 	{field: "SecuenciaNeteo",width: "20%",title: $translate.instant("UI_SECUENCIA"),attributes : {style : "text-align:left;"}},
				],		
				sortable			: true,
				pageable			: true,
				autoBind			: true, 
				height				: 350,
			    selectable 			: "row",
		    	excel : {
					allPages: true,
				},
				detailInit : function(e) {
			    	//
				}
			};
		};
		
		tCriNet.grid = tCriNet.gridDef();
		
		
	
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
	angular.module('ModuloPlataforma').factory('servicioTipoCriterioNeteo',['$resource',function($resource) {
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