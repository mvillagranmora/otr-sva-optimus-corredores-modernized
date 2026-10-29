(function() {
	angular.module('ModuloOrdenes').controller('administrarmodSNCtrl',
		function($scope,$resource,$translate,bcsComunes,$timeout,bcsAlerta,bcsServicioOrden,bcsServicioPlataforma){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		$scope.data = {};	
		$scope.data.LargoShortName=0;
		
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('LargoShortname', $scope.SesionIdEmpresa, 'ORDENES')
		.then(function ( parametroLargoShortName ) {
			if (parametroLargoShortName.Valor){
				$scope.data.LargoShortName= parametroLargoShortName.Valor;
			}else{
				$scope.data.LargoShortName= 15;
			}
		})
		
		var modSN = this;
		modSN.filtrosCorredora = {
			Habilitado : { "field"   : "Habilitado","operator": "eq", "value"   : "S" }
		}
		modSN.datosAntes 	= {};
		modSN.datosDespues  = {};
		modSN.fn 			= {};
		modSN.ds			= {};
		modSN.cbbx		    = {};
		modSN.win			= {}; 
		modSN.winId 		= bcsComunes.ventanaWinId($scope);
		modSN.param 		= bcsComunes.ventanaParams(modSN.winId);
		var filterConf = {
			logic: "and",
			filters: [
				{ field: "IdEstado", operator: "eq", value: 'VIG' },
				{ field: "IdClasificacionTipoInstrumento", operator: "eq", value: "RV" },
				{
					logic: "and",
					filters: [
						{ field: "IdTipoInstrumento", operator: "ne", value: "ORF" },
						{ field: "IdTipoInstrumento", operator: "ne", value: "SIM" },
						{ field: "IdTipoInstrumento", operator: "ne", value: "PAC" },
						{ field: "IdTipoInstrumento", operator: "ne", value: "PRT" },
						{ field: "IdTipoInstrumento", operator: "ne", value: "EMP" },
						{ field: "IdTipoInstrumento", operator: "ne", value: "FRW" },
						{ field: "IdTipoInstrumento", operator: "ne", value: "DOL" }
					]
				}
			]
		};
        
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
		
		modSN.win.guardar = function() {
			var datosGuardar = modSN.fn.genDatosGuardar();
			if(modSN.win.modoCrear) { //crear
				if (datosGuardar.p_NewShortName.length > $scope.data.LargoShortName) {
					bcsComunes.ventanaMsgErr('Shortname no puede superar los '+ $scope.data.LargoShortName + ' caracteres.' ,idVentana);
					return;
				}			
				bcsServicioOrden.saveConfiguracionShortName(datosGuardar,function (result){
					var datos = {};
					if (result.P_OUT_ERRORESCONTADOR < 1){
						bcsComunes.msg('info','SHORTNAME','ShortName Creado');
					}
					else {

						datos.message = result.P_OUT_MENSAJES[0].descripcion;
						bcsComunes.msg('error','SHORTNAME','Error al crear ShortName: ' + datos.message);
						console.log(result);
					}	
					bcsComunes.ventanaCerrar(modSN.winId);
				});	
			}
			else { //editar
				bcsServicioOrden.updateConfiguracionShortName(datosGuardar,function (result){
					var datos = {};
					if (result.P_OUT_ERRORESCONTADOR < 1){
						bcsComunes.msg('info','SHORTNAME','ShortName Actualizado');
					}
					else {
						datos.message = result.P_OUT_MENSAJES[0].descripcion;
						bcsComunes.msg('error','SHORTNAME','Error al crear ShortName: ' + datos.message);
						console.log(result);
					}	
					bcsComunes.ventanaCerrar(modSN.winId);
				});	
			}
			
		};
		
		modSN.win.cancelar = function() {
			bcsComunes.ventanaCerrar(modSN.winId);
		};
		
		
/*********************************************
 * DATASOURCES
 *********************************************/
				
		modSN.dsDetalleConfiguracionShortName = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1902",
					dataType: "json"
				}
			},
			schema: {
				model: {
					fields: {
						IdTipoInstrumento 		: { type: "string"},
						AsignacionAuto			: { type: "string"},	
						ComprobanteAuto			: { type: "string"},	
						MovimientoFacturarAuto 	: { type: "string"},	
					}
				}
			}
		});	
		
		modSN.dsTipoInstrumentos = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1570",
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
			},
			change: function(e) {
				var datosUnicos = {};
				var resultadosFinales = [];
		

				e.items.forEach(function(item) {
					if (!datosUnicos[item.IdTipoInstrumento]) {
						datosUnicos[item.IdTipoInstrumento] = true;
						resultadosFinales.push(item);
					}
				});
		
				modSN.resultadosTipoInstrumentosUnicos = resultadosFinales;
			}
		});	
		
/*********************************************
 * COMBOBOX
 *********************************************/
		modSN.cbbx.socio={
			idActual : false,
			seleccionado : false,
			configuracion :{},
			filtros: {
				IdEmpresa : { "field"   : "IdEmpresa","operator": "eq", "value"   :  $scope.$root.SesionIdEmpresa },
			},
		};

		modSN.cbbx.portafolio={
			configuracion :{},
			filtros: {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
			},
		};
	
/*********************************************
 * GRILLAS
 *********************************************/

		
/*********************************************
 * FUNCIONES
 *********************************************/
		

		
		modSN.fn.cargaDatos = function(datosOrigen) { 
			if(datosOrigen) {
                bcsComunes.ventanaTitulo(modSN.winId, $translate.instant("UI_EDITAR_SHORTNAME"));
				var datos = _.clone(datosOrigen);
				var filterShortName = [{ field: "ShortName", operator: "eq", value: datosOrigen.ShortName }];
				modSN.dsDetalleConfiguracionShortName.query({filter:filterShortName}).then(function() {
					var detalleConf = modSN.dsDetalleConfiguracionShortName.view();
					if (detalleConf.length > 0){
						console.log(detalleConf);
						$.each(detalleConf,function(key,value){
							detalleConf[key].AsignacionAuto 		= (value.AsignacionAuto==='S')?true:false;
							detalleConf[key].ComprobanteAuto 		= (value.ComprobanteAuto==='S')?true:false;
							detalleConf[key].MovimientoFacturarAuto	= (value.MovimientoFacturarAuto==='S')?true:false;
						});
						datos.listaTipoInstrumentos = detalleConf;
						modSN.inicializarValores(datos);
					}
				});
			}
			else {
				modSN.dsTipoInstrumentos.filter(filterConf);
				modSN.dsTipoInstrumentos.read().then(function() {
					// Directamente usar modSN.resultadosTipoInstrumentosUnicos sin llamar a .view()
					var detalleConf = modSN.resultadosTipoInstrumentosUnicos;
					if (detalleConf && detalleConf.length > 0) {
						modSN.datosDespues.listaTipoInstrumentos = _.clone(detalleConf);
						modSN.datosDespues.Habilitado = true;
			
						$.each(modSN.datosDespues.listaTipoInstrumentos, function(key, value) {
							modSN.datosDespues[value.IdTipoInstrumento] = {
								AsignacionAuto: false,
								ComprobanteAuto: false,
								MovimientoFacturarAuto: false
							};
						});
					}
				});
			}
		};
		
		modSN.inicializarValores = function(datos) {
			modSN.datosAntes 		= _.clone(datos);
			modSN.datosDespues 	= _.clone(datos);
			modSN.datosDespues.Habilitado = (datos.Habilitado==='S')?true:false;
			$.each(modSN.datosDespues.listaTipoInstrumentos, function(key,value){
				modSN.datosDespues[value.IdTipoInstrumento] = {};
				modSN.datosDespues[value.IdTipoInstrumento].AsignacionAuto 			= value.AsignacionAuto;
				modSN.datosDespues[value.IdTipoInstrumento].ComprobanteAuto 			= value.ComprobanteAuto;
				modSN.datosDespues[value.IdTipoInstrumento].MovimientoFacturarAuto 	= value.MovimientoFacturarAuto;
			}); 
			if (datos.IdSocioDeNegocio>0) { 
				$timeout(function(){ 
					modSN.datosDespues.IdSocioDeNegocio = datos.IdSocioDeNegocio; 
					modSN.datosDespues.IdSocioDeNegocioPost = datos.IdSocioDeNegocio;
				}, 400);
			}
		}
		
		modSN.fn.genDatosGuardar = function() {
			var dg = {};
			dg.p_IdUsuario = $scope.$root.SesionIdUsuario;
			dg.p_NewShortName = modSN.datosDespues.ShortName;
            dg.p_OldShortName = modSN.datosAntes.ShortName;
            dg.p_IdSocioDeNegocio = modSN.datosDespues.IdSocioDeNegocio;
            dg.p_IdPortafolio = modSN.datosDespues.IdPortafolio;
            dg.p_FechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			dg.p_Habilitado = (modSN.datosDespues.Habilitado) ? 'S':'N';
			dg.p_DetalleConfiguracion = [];
			$.each(modSN.datosDespues.listaTipoInstrumentos,function(key,value){
				var aux = {};
				aux.IdTipoInstrumento 		= value.IdTipoInstrumento;
				aux.AsignacionAuto 			= value.AsignacionAuto?'S':'N';
				aux.ComprobanteAuto 		= value.ComprobanteAuto?'S':'N';
				aux.MovimientoFacturarAuto 	= value.MovimientoFacturarAuto?'S':'N';
				dg.p_DetalleConfiguracion.push(aux);
			});
			return dg;
		};	

        modSN.cambiarAsignacionAuto = function(item) {
            if (!item.AsignacionAuto) {
                item.MovimientoFacturarAuto = false;
                item.ComprobanteAuto = false;
            }
        };
        modSN.cambiarMovimientoFacturarAuto = function(item) {
            if (item.MovimientoFacturarAuto) {
                item.AsignacionAuto = true;
            }else{
                item.ComprobanteAuto = false;
            }
        };
        modSN.cambiarComprobanteAuto = function(item) {
            if (item.ComprobanteAuto) {
                item.AsignacionAuto = true;
                item.MovimientoFacturarAuto = true;
            }
        };
/*********************************************
 * FUNCIONES DE POPUP
 *********************************************/	
		

		
/*********************************************
 * FUNCIONES WATCH 
 *********************************************/	
		//Caso edición: cargar el portafolio una vez cargado el socio de negocio
		$scope.$watch('modSN.datosDespues.IdSocioDeNegocio',function(nval,oval) {
			if(nval>0){
				if(modSN.param && modSN.param.ShortName) {
					modSN.datosDespues.IdPortafolio = modSN.param.ShortName.IdPortafolio; 
					modSN.datosDespues.IdPortafolioPost = modSN.param.ShortName.IdPortafolio;
				}
			} 
			bcsComunes.ventanaAutoHeight(idVentana);
		});
		var idVentana = bcsComunes.ventanaWinId ($scope);
    	bcsComunes.ventanaAutoHeight(idVentana);
		

/*********************************************
 * GOOOOO
 *********************************************/	
		if(modSN.param && modSN.param.ShortName) {
			modSN.win.modoCrear = false;
			modSN.fn.cargaDatos(modSN.param.ShortName);
		}
		else {
			modSN.win.modoCrear = true;
			modSN.fn.cargaDatos();	
		}
	
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------


