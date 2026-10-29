/*************************************************************
 * Purpose	: Controlador para creación/edición de Operador.
 * Author	: JGarces - BCS
 * Date		: 05/02/2016
 *************************************************************/

 (function() {
	angular.module('ModuloPlataforma').controller('administrarmodOperCtrl',
		function($scope,$resource,$translate,$element,bcsComunes,$timeout,bcsAlerta,servicioTransacciones){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var modOper = this;
		modOper.filtrosCorredora = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		}
		modOper.datosAntes 	= {};
		modOper.datosDespues= {};
		modOper.fn 			= {};
		modOper.ds			= {};
		modOper.cbbx		= {};
		modOper.win			= {}; 
		modOper.winId 		= bcsComunes.ventanaWinId($scope);
		modOper.param 		= bcsComunes.ventanaParams(modOper.winId);
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
		
		modOper.win.guardar = function() {
			var datosGuardar = modOper.fn.genDatosGuardar();
			if(modOper.win.modoCrear) { //crear
				servicioTransacciones.saveConfiguracionOperador(datosGuardar,function (result){
					var datos = {};
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
						bcsComunes.msg('info','OPERADOR','Operador Creado');
					}
					else {
						//[CELCONDOR-688]--Campo libro de ordenes en "operadores en rueda"
						//datos.message = result.CODIGO + ' : ' + result.PILA;
						datos.message = result.P_OUT_ERRORESDETALLE[0].Descripcion;
						//[CELCONDOR-688]--Campo libro de ordenes en "operadores en rueda"
						bcsComunes.msg('error','OPERADOR','Error al crear operador: ' + datos.message);
						console.log(result);
					}	
					bcsComunes.ventanaCerrar(modOper.winId);
				});	
			}
			else { //editar
				servicioTransacciones.updateConfiguracionOperador(datosGuardar,function (result){
					var datos = {};
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
						bcsComunes.msg('info','OPERADOR','Operador Actualizado');
					}
					else {
						datos.message = result.CODIGO + ' : ' + result.PILA;
						bcsComunes.msg('error','OPERADOR','Error al actualizar operador' + datos.message);
						console.log(result);
					}	
					bcsComunes.ventanaCerrar(modOper.winId);
				});	
			}
			
		};
		
		modOper.win.cancelar = function() {
			bcsComunes.ventanaCerrar(modOper.winId);
		};
		
		
/*********************************************
 * DATASOURCES
 *********************************************/
				
		modOper.dsDetalleConfiguracionOperador = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1565",
					dataType: "json"
				}
			},
			schema: {
				model: {
					fields: {
						CodigoOperador 			: { type: "string"},
						CodigoCorredora			: { type: "string"},
						AsignacionAuto			: { type: "string"},	
						ComprobanteAuto			: { type: "string"},	
						MovimientoFacturarAuto 	: { type: "string"},	
					}
				}
			}
		});	
		
		modOper.dsTipoInstrumentoTransaccion = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1566",
					dataType: "json"
				}
			},
			schema: {
				model: {
					fields: {
						IdTipoInstrumento 	: { type: "string"},
						Nombre				: { type: "string"},
						IdEstado			: { type: "string"},	
					}
				}
			}
		});	
		
/*********************************************
 * COMBOBOX
 *********************************************/
		modOper.cbbx.socio={
			idActual : false,
			seleccionado : false,
			configuracion :{},
			filtros: {
				IdEmpresa : { "field"   : "IdEmpresa","operator": "eq", "value"   :  $scope.$root.SesionIdEmpresa },
			},
		};

		modOper.cbbx.portafolio={
			configuracion :{},
			filtros: {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
			},
		};
	
/*********************************************
 * GRILLAS
 *********************************************/

		modOper.tablaCarteraInversion = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					pageSize 		: 50,
					transport: {
						read		: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=21002",
										dataType: "json"
									  }
						},						
					filter			: [{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
					      			 { field: "CodigoOperador", operator: "eq", value: (modOper.param&&modOper.param.operador)?modOper.param.operador.CodigoOperador?modOper.param.operador.CodigoOperador:'':''}],
					sort 			: { field: "NombreCarteraInversion", dir: "asc" },
					schema 			: {
						model	: { fields	: { 
							CodigoOperador: {type:"string"},
							NombreCarteraInversion: { type: "string"},
							IdEmpresa: { type: "number"},
							IdEstado   : { type: "string"}}
						}
					},
				}
			}),
			columns	: [{
					field: "NombreCarteraInversion",
					title: $translate.instant('UI_NOMBRE'),
					attributes: {style:"text-align:left;"},
					width: "100%"}
				],		
			sortable			: false,
			groupable			: false,
			pageable			: false,
			autoBind			: true, 
			height				: 300,
		    selectable 			: "row"
		 };
		
		modOper.tablaCarteraEjecutivo = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					pageSize 		: 50,
					transport: {
						read		: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=21040",
										dataType: "json"
									  }
						},						
					filter			: [{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
					      			 { field: "CodigoOperador", operator: "eq", value: (modOper.param&&modOper.param.operador)?modOper.param.operador.CodigoOperador?modOper.param.operador.CodigoOperador:'':''}],
					sort 			: { field: "NombreCarteraEjecutivo", dir: "asc" },
					schema 			: {
						model	: { fields	: { 
							CodigoOperador: {type:"string"},
							NombreCarteraEjecutivo: { type: "string"},
							IdEmpresa: { type: "number"},
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
			height				: 300,
		    selectable 			: "row"
		 };
		
/*********************************************
 * FUNCIONES
 *********************************************/
		var r = $resource("", {}, {
			'crearOCI': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21008"},
			'eliminarOCI': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21009"},
			'crearOCA': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21041"},
			'eliminarOCA': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21042"},
		});
		
		modOper.agregarCarteraInversion = function(){
			var _datos=modOper.tablaCarteraInversion.dataSource.data();
			var existe = false;
			for(var vm=0;_datos.length;_datos++){
				if (_datos[vm].NombreCarteraInversion==modOper.inputCarteraInversion){
					existe=true;
					break;
				}
			}
			if(existe){
				console.log("item existe");
				return;
			}
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'TRA',
				p_CodigoOperador: modOper.datosDespues.CodigoOperador,
				p_NombreCarteraInversion: modOper.inputCarteraInversion
			};
			bcsComunes.ventanaOverlay(modOper.winId,true);
			r.crearOCI(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
					modOper.tablaCarteraInversion.dataSource.read();
					modOper.inputCarteraInversion=undefined;
				}
			},function(error){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),modOper.winId);
			});
		};
		modOper.agregarCarteraEjecutivo = function(){
			var _datos=modOper.tablaCarteraEjecutivo.dataSource.data();
			var existe = false;
			for(var vm=0;_datos.length;_datos++){
				if (_datos[vm].NombreCarteraEjecutivo==modOper.inputCarteraEjecutivo){
					existe=true;
					break;
				}
			}
			if(existe){
				console.log("item existe");
				return;
			}
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'TRA',
				p_CodigoOperador: modOper.datosDespues.CodigoOperador,
				p_NombreCarteraEjecutivo: modOper.inputCarteraEjecutivo
			};
			bcsComunes.ventanaOverlay(modOper.winId,true);
			r.crearOCA(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
					modOper.tablaCarteraEjecutivo.dataSource.read();
					modOper.inputCarteraEjecutivo=undefined;
				}
			},function(error){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),modOper.winId);
			});
		};
		
		modOper.eliminarCarteraInversion = function(item){
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'TRA',
				p_CodigoOperador:modOper.datosDespues.CodigoOperador,
				p_NombreCarteraInversion: item.NombreCarteraInversion
			};
			bcsComunes.ventanaOverlay(modOper.winId,true);
			r.eliminarOCI(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
					modOper.tablaCarteraInversion.dataSource.read();
					modOper.inputCarteraInversion=undefined;
				}
			},function(error){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),modOper.winId);
			});
		};
		
		modOper.eliminarCarteraEjecutivo = function(item){
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'TRA',
				p_CodigoOperador:modOper.datosDespues.CodigoOperador,
				p_NombreCarteraEjecutivo: item.NombreCarteraEjecutivo
			};
			bcsComunes.ventanaOverlay(modOper.winId,true);
			r.eliminarOCA(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},modOper.winId);
					modOper.tablaCarteraEjecutivo.dataSource.read();
					modOper.inputCarteraEjecutivo=undefined;
				}
			},function(error){
				bcsComunes.ventanaOverlay(modOper.winId,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),modOper.winId);
			});
		};
		
		modOper.fn.cargaDatos = function(datosOrigen) { 
			if(datosOrigen) {
				var datos = _.clone(datosOrigen);
				var filterOperador = [{ field: "CodigoOperador", operator: "eq", value: datosOrigen.CodigoOperador }];
				modOper.dsDetalleConfiguracionOperador.query({filter:filterOperador}).then(function() {
					var detalleConf = modOper.dsDetalleConfiguracionOperador.view();
					if (detalleConf.length > 0){
						console.log(detalleConf);
						$.each(detalleConf,function(key,value){
							detalleConf[key].ReservaTransaccion 	= (value.ReservaTransaccion==='S')?true:false;
							detalleConf[key].AsignacionAuto 		= (value.AsignacionAuto==='S')?true:false;
							detalleConf[key].ComprobanteAuto 		= (value.ComprobanteAuto==='S')?true:false;
							detalleConf[key].MovimientoFacturarAuto	= (value.MovimientoFacturarAuto==='S')?true:false;
						});
						datos.listaTipoInstrumentos = detalleConf;
						modOper.inicializarValores(datos);
					}
				});
			}
			else {
				var filterConf = [{ field: "IdEstado", operator: "eq", value: 'ACT' }];
				modOper.dsTipoInstrumentoTransaccion.query({filter:filterConf}).then(function() {
					var detalleConf = modOper.dsTipoInstrumentoTransaccion.view();
					if (detalleConf.length > 0){
						modOper.datosDespues.listaTipoInstrumentos = _.clone(detalleConf);
						modOper.datosDespues.IdEstado=true;
					}
					$.each(modOper.datosDespues.listaTipoInstrumentos, function(key,value){
						modOper.datosDespues[value.IdTipoInstrumento] = {};
						modOper.datosDespues[value.IdTipoInstrumento].AsignacionAuto 			= false;
						modOper.datosDespues[value.IdTipoInstrumento].ComprobanteAuto 			= false;
						modOper.datosDespues[value.IdTipoInstrumento].MovimientoFacturarAuto 	= false;
					}); 
				});
			}
		};
		
		modOper.inicializarValores = function(datos) {
			modOper.datosAntes 		= _.clone(datos);
			modOper.datosDespues 	= _.clone(datos);
			modOper.datosDespues.IdEstado = (datos.IdEstado==='ACT')?true:false;
			modOper.datosDespues.UtilizaPortafolio = (datos.UtilizaPortafolio==='S')?true:false;
			modOper.datosDespues.ReservaTransaccion = (datos.ReservaTransaccion==='S')?true:false;
			$.each(modOper.datosDespues.listaTipoInstrumentos, function(key,value){
				modOper.datosDespues[value.IdTipoInstrumento] = {};
				modOper.datosDespues[value.IdTipoInstrumento].AsignacionAuto 			= value.AsignacionAuto;
				modOper.datosDespues[value.IdTipoInstrumento].ComprobanteAuto 			= value.ComprobanteAuto;
				modOper.datosDespues[value.IdTipoInstrumento].MovimientoFacturarAuto 	= value.MovimientoFacturarAuto;
			}); 
			if (datos.IdSocioDeNegocio>0) { 
				$timeout(function(){ 
					modOper.datosDespues.IdSocioDeNegocio = datos.IdSocioDeNegocio; 
					modOper.datosDespues.IdSocioDeNegocioPost = datos.IdSocioDeNegocio;
				}, 400);
			}
		}
		
		modOper.fn.genDatosGuardar = function() {
			var dg = {};
			dg.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			dg.p_IdUsuario = $scope.$root.SesionIdUsuario;
			dg.p_IdModulo = 'PLA';
			dg.p_simularOperacion = 'N';
			dg.p_UsaReglasNegocio = 'N';
			dg.p_IdLibroOrden = modOper.datosDespues.IdLibroOrdenes; 	//[CELCONDOR-688]--Campo libro de ordenes en "operadores en rueda"
			dg.p_CodigoOperador = modOper.datosDespues.CodigoOperador;
			dg.p_IdCorredora = modOper.datosDespues.IdCorredora;
			dg.p_ReservaTransaccion = (modOper.datosDespues.ReservaTransaccion) ? 'S':'N';
			dg.p_UtilizaPortafolio = (modOper.datosDespues.UtilizaPortafolio) ? 'S':'N';
			if(modOper.datosDespues.UtilizaPortafolio) dg.p_IdPortafolio = (modOper.datosDespues.IdPortafolio) ? modOper.datosDespues.IdPortafolio:0;
			else dg.p_IdPortafolio = 0;
			dg.p_IdEstado = (modOper.datosDespues.IdEstado) ? 'ACT':'INA';
			dg.p_detalle = [];
			$.each(modOper.datosDespues.listaTipoInstrumentos,function(key,value){
				var aux = {};
				aux.IdTipoInstrumento 		= value.IdTipoInstrumento;
				aux.AsignacionAuto 			= value.AsignacionAuto?'S':'N';
				aux.ComprobanteAuto 		= value.ComprobanteAuto?'S':'N';
				aux.MovimientoFacturarAuto 	= value.MovimientoFacturarAuto?'S':'N';
				aux.DetalleExiste			= value.DetalleExiste||'N';
				dg.p_detalle.push(aux);
			});
			return dg;
		};	

/*********************************************
 * FUNCIONES DE POPUP
 *********************************************/	
		

		
/*********************************************
 * FUNCIONES WATCH 
 *********************************************/	
		
		$scope.$watch('modOper.datosDespues.UtilizaPortafolio',function(nval,oval) {
			if(nval===false) {
				$.each(modOper.datosDespues.listaTipoInstrumentos, function(key,value){
					modOper.datosDespues[value.IdTipoInstrumento] = {};
					modOper.datosDespues[value.IdTipoInstrumento].AsignacionAuto = false;
					modOper.datosDespues[value.IdTipoInstrumento].ComprobanteAuto = false;
					modOper.datosDespues[value.IdTipoInstrumento].MovimientoFacturarAuto = false;
				}); 
				modOper.datosDespues.IdSocioDeNegocio = undefined;
				modOper.datosDespues.IdPortafolio = undefined;
			}
			bcsComunes.ventanaAutoHeight(idVentana);
		});
			
		
		//Caso edición: cargar el portafolio una vez cargado el socio de negocio
		$scope.$watch('modOper.datosDespues.IdSocioDeNegocio',function(nval,oval) {
			if(nval>0){
				if(modOper.param && modOper.param.operador) {
					modOper.datosDespues.IdPortafolio = modOper.param.operador.IdPortafolio; 
					modOper.datosDespues.IdPortafolioPost = modOper.param.operador.IdPortafolio;
				}
			} 
			bcsComunes.ventanaAutoHeight(idVentana);
		});
		var idVentana = bcsComunes.ventanaWinId ($scope);
    	bcsComunes.ventanaAutoHeight(idVentana);
		

/*********************************************
 * GOOOOO
 *********************************************/	
		if(modOper.param && modOper.param.operador) {
			modOper.win.modoCrear = false;
			modOper.fn.cargaDatos(modOper.param.operador);
		}
		else {
			modOper.win.modoCrear = true;
			modOper.fn.cargaDatos();	
		}
	
	});

}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------


