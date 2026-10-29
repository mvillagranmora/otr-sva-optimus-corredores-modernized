/***********************************************
 * Purpose	: MANTENEDOR CARTERA EJECUTIVO
 * Author	: 
 * Date		: 09/04/2019
 ***********************************************/
(function(){
	angular.module('ModuloPlataforma').controller('CarteraEjecutivoCtrl', 
		function($scope,$translate,$resource,bcsComunes,bcsServicioPlataforma,$timeout){
		/*********************
		 * VARIABLES
		 *********************/
		var controllerName 		= 'CarteraEjecutivoCtrl';
		var ci 					= this;
   		console.log('%cINICIA CONTROLLER: '+controllerName,'background:#222;color:#bada55;font-size:16px');
		var idVentana 			= bcsComunes.ventanaWinId($scope);
		ci.idVentana 			= bcsComunes.ventanaWinId($scope); 
		ci.maestro				= {};
		ci.detalle				= {
				configuracionEjecutivos : {
					filtros: {
						IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
					}
				}
		};
		
		var initDatos = function(){
			ci.maestro.nombre = undefined;
			ci.maestro.seleccionado = undefined;
			ci.detalle.seleccionado = undefined;
			ci.vista='MASTER';
			
		};
		
		var initDatosDetalle = function(){
			ci.detalle.configuracionEjecutivos.idActual = false;
			ci.detalle.seleccionado = undefined;
			ci.vista='DETAIL';
		}
		
		initDatos();
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		ci.maestro.configTabla = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					pageSize 		: 50,
					transport: {
						read		: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=21034",
										dataType: "json"
									  }
						},						
					filter			: [{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa}],
					sort 			: { field: "NombreCarteraEjecutivo", dir: "asc" },
					schema 			: {
						model	: { fields	: { 
							NombreCarteraEjecutivo: { type: "string"},
							IdEmpresa: { type: "number"},
							Descripcion : { type: "string"},
							IdEstado   : { type: "string"}}
						}
					},
				}
			}),
			columns	: [{
					field: "NombreCarteraEjecutivo",
					title: $translate.instant('UI_NOMBRE'),
					attributes: {style:"text-align:left;"},
					width: "100%"}
				],		
			sortable			: false,
			groupable			: false,
			pageable			: false,
			autoBind			: true, 
			height				: 250,
		    selectable 			: "row"
		 };
		
		ci.detalle.configTabla = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					pageSize 		: 50,
					transport: {
						read		: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=21035",
										dataType: "json"
									  }
						},						
					filter			: [{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
					      			 { field: "NombreCarteraEjecutivo", operator: "eq", value: null}],
					sort 			: { field: "NombreCompleto", dir: "asc" },
					schema 			: {
						model	: { fields	: { 
							NombreCarteraEjecutivo: { type: "string"},
							IdEmpresa: { type: "number"},
							Descripcion : { type: "string"},
							IdEstado   : { type: "string"},
							IdEmpleadoEjecutivo   : { type: "string"},
							NombreCompleto   : { type: "string"}}
						}
					},
				}
			}),
			columns	: [{
					field: "NombreCompleto",
					title: $translate.instant('UI_EJECUTIVO'),
					attributes: {style:"text-align:left;"},
					width: "100%"}
				],		
			sortable			: false,
			groupable			: false,
			pageable			: false,
			autoBind			: true, 
			height				: 210,
		    selectable 			: "row"
		 };
		
		var r = $resource("", {}, {
			'crearmaestro': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21036"},
			'eliminarmaestro': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21037"},
			'creardetalle': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21038"},
			'eliminardetalle': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21039"},
		});

		ci.maestro.crear = function(){
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'INS',
				p_NombreCarteraEjecutivo: ci.maestro.nombre,
				p_Descripcion: ci.maestro.nombre
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			r.crearmaestro(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},idVentana);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},idVentana);
					ci.maestro.configTabla.dataSource.read();
					initDatos();
				}
			},function(error){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),idVentana);
			});
		};
		
		ci.maestro.verDetalle = function(){
			ci.vista="DETAIL";
			var filtroDetalle = [
			                     { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
				      			 { field: "NombreCarteraEjecutivo", operator: "eq", value: ci.maestro.seleccionado.NombreCarteraEjecutivo}
			                     ];
			ci.detalle.configTabla.dataSource.filter(filtroDetalle);
		};
		
		ci.maestro.eliminar = function(){
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'INS',
				p_NombreCarteraEjecutivo: ci.maestro.seleccionado.NombreCarteraEjecutivo
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			r.eliminarmaestro(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},idVentana);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},idVentana);
					ci.maestro.configTabla.dataSource.read();
					initDatos();
				}
			},function(error){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),idVentana);
			});
		};
		
		ci.detalle.agregar = function(){
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'INS',
				p_NombreCarteraEjecutivo: ci.maestro.seleccionado.NombreCarteraEjecutivo,
				p_IdEmpleadoEjecutivo: ci.detalle.configuracionEjecutivos.idActual
			};
			console.log("AgregarDetalle", datos);
			bcsComunes.ventanaOverlay(idVentana,true);
			r.creardetalle(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},idVentana);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},idVentana);
					ci.detalle.configTabla.dataSource.read();
					initDatosDetalle();
				}
			},function(error){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),idVentana);
			});
		};
		
		ci.detalle.eliminar = function(){
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'INS',
				p_NombreCarteraEjecutivo: ci.detalle.seleccionado.NombreCarteraEjecutivo,
				p_IdEmpleadoEjecutivo: ci.detalle.seleccionado.IdEmpleadoEjecutivo
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			r.eliminardetalle(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},idVentana);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},idVentana);
					ci.detalle.configTabla.dataSource.read();
					initDatosDetalle();
				}
			},function(error){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),idVentana);
			});
		};
		
		ci.detalle.volver = function(){
			var filtroDetalle = [
			                     { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
				      			 { field: "NombreCarteraEjecutivo", operator: "eq", value: null}
			                     ];
			ci.detalle.configTabla.dataSource.filter(filtroDetalle);
			ci.maestro.configTabla.dataSource.read();
			initDatos();
		};
		
		
	});
}());
//------------------------- FIN CONTROLLER --------------------------	