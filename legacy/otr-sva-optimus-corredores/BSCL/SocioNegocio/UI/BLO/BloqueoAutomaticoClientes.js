/******************************************
 * Purpose	: Mantenedor bloqueo auto clientes
 * Author	: JG
 * Date		: 30/01/2017
 *****************************************/
(function() {
	angular.module('ModuloPlataforma').controller('BloqueoAutomaticoClientesCtrl',
	function($scope,bcsComunes,$translate,bcsAlerta,servicioSociodeNegocio){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var idVentana 	= bcsComunes.ventanaWinId($scope); 
		var parametros 	= bcsComunes.ventanaParams(idVentana);
		var bloautocli 		= this;
		bloautocli.conf 	= {};
		bloautocli.filtros	= {};
		bloautocli.datos	= {};
		bloautocli.datosAntes = {};
		bloautocli.cmbbx 	= {};
		bloautocli.win 		= {}; 
		bloautocli.grid 	= {};
		
		
		/*********************************************
		 * DATASOURCE
		 *********************************************/
		var schemaDocumentos = function schemaDocumentos(){
			return {
				model: {
					fields : {
						IdEmpresa						: {type: "number"},
						IdEstado						: {type: "string"},
						BloqueoCumplirMayoriaEdad		: {type: "number"},
						BloqueoFichaDesactualizada		: {type: "number"},
						BloqueoPortafolioSinOperaciones	: {type: "number"},
						FechaCreacion					: {type: "datetime"},
						FechaModificacion				: {type: "datetime"},
						IdTipoDocumento					: {type: "string"},
						IdEstadoTipoDocumento			: {type: "string"},
						NombreEstadoTipoDocumento		: {type: "string"},
						NombreDocumento					: {type: "string"} 	
					}
				}
			}
		}
		
		var dsConfDocumentos = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3500",
						dataType: "json"
				    }
				},
				schema : schemaDocumentos(),
				filter:{field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa}
			}
		});		
		
		var loadDsConfDocumentos = function loadDsConfDocumentos(){
			bcsComunes.ventanaOverlay(idVentana,true);
			dsConfDocumentos.query().then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				var datos=dsConfDocumentos.data();
				if(datos.length>0){
					bloautocli.grid.documentos.dataSource.data(datos);
					bloautocli.anosAlCumplirMayoriaEdad=datos[0].BloqueoCumplirMayoriaEdad;
					bloautocli.mesesFichaDesactualizada=datos[0].BloqueoFichaDesactualizada;
					bloautocli.mesesPortafolioSinOperaciones=datos[0].BloqueoPortafolioSinOperaciones;
				}else console.log("no se han obtenido datos");
			});
		};

		/*********************************************
		 * GRILLAS
		 *********************************************/
		bloautocli.grid.documentos = {
			dataSource	: new bcs.DataSource({
				type 		: "array",
				definition 	: {
					pageSize : 50,
					schema 	 : schemaDocumentos()
				}
			}),
			columns	: [
	            { field:"NombreDocumento",title:'UI_NOMBRE_DOCUMENTO',width:"300px"},
	            { field:"NombreEstadoTipoDocumento",title:'UI_ESTADO',width:"160px"},
            ],		
			sortable	: true,
			filterable	: false,
			pageable	: false,
			autoBind	: false,
			height		: 280,
		    selectable 	: "row",
		    gridCreated: function(grid) {
		    	loadDsConfDocumentos();
			}
		};	
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		bloautocli.win.buscar = function(){
	    	var filters = new Array();	
			filters.push({field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa});
	    	bloautocli.grid.documentos.dataSource.query({ filter: filters }).then(function(){
	    		if(bloautocli.grid.documentos.dataSource.data().length>0)
				bloautocli.datosAntes = bloautocli.grid.documentos.dataSource.data()[0];
			});			
		};
		
		bloautocli.win.activar = function(seleccionado){
			if(seleccionado){
				seleccionado.NombreEstadoTipoDocumento='ACTIVO';
				seleccionado.IdEstadoTipoDocumento='ACT';
			}
		};
		 
		bloautocli.win.desactivar = function(seleccionado){
			if(seleccionado){
				seleccionado.NombreEstadoTipoDocumento='INACTIVO';
				seleccionado.IdEstadoTipoDocumento='INA';
			}
		};
		
		bloautocli.win.guardar = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			var datos = {};
			datos.p_IdEmpresa=$scope.$root.SesionIdEmpresa;
			datos.p_IdEstado="ACT";
			datos.p_BloqueoCumplirMayoriaEdad=bloautocli.anosAlCumplirMayoriaEdad;
			datos.p_BloqueoFichaDesactualizada=bloautocli.mesesFichaDesactualizada;
			datos.p_BloqueoPortafolioSinOperaciones=bloautocli.mesesPortafolioSinOperaciones;
			datos.p_Registros=[];
			var datosgrilla=bloautocli.grid.documentos.dataSource.data();
			for(var vm=0;vm<datosgrilla.length;vm++){
				var item = {};
				item.IdEstado=datosgrilla[vm].IdEstadoTipoDocumento;
				item.IdTipoDocumento=datosgrilla[vm].IdTipoDocumento;
				datos.p_Registros.push(item);
			}
			servicioSociodeNegocio.modificarConfiguracionBloqueoAutomaticoClientes(datos,
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result.P_OUT_ERRORESCONTADOR==0){
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant('UI_CONFIGURACION_GUARDADA'),
							conflictos:util.parseServerResponse(result)
						},idVentana);	
					}else{
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant('UI_ERROR_GUARDAR_CONFIGURACION'),
							conflictos:util.parseServerResponse(result)
						},idVentana);
					}
					loadDsConfDocumentos();
				},function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					console.log(result);
					var p=bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_GUARDAR_CONFIGURACION'),idVentana);
					bcsComunes.ventanaPromesa(p).then(function(){
						bcsComunes.ventanaCerrar(idVentana);
					});                                       
				}
			);
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
	 	
		/*********************************************
		 * GO!!!
		 *********************************************/
	});
}());
//------------------------- FIN CONTROLLER --------------------------
