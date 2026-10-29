(function() {
	angular.module('ModuloOrdenes').controller('asignacionDirectaCtrl',
		function($scope,$resource,$translate,bcsComunes,$timeout,bcsAlerta,bcsServicioOrden){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var asDir = this;
		asDir.filtrosCorredora = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		}
		asDir.datosAntes 	= {};
		asDir.datosDespues= {};
		asDir.fn 			= {};
		asDir.ds			= {};
		asDir.cbbx		= {};
		asDir.win			= {}; 
		asDir.winId 		= bcsComunes.ventanaWinId($scope);
		asDir.param 		= bcsComunes.ventanaParams(asDir.winId);
        var idVentana = bcsComunes.ventanaWinId ($scope);
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
        asDir.win.guardar = function() {
            var datosGuardar = asDir.fn.genDatosGuardar();
            
            bcsServicioOrden.GuardarConfiguracionAsignacionDirecta(datosGuardar,function (result){
                    if (result.P_OUT_ERRORESCONTADOR < 1){
                        bcsComunes.msg('info','OPERADOR','Configuracion Actualizada');
                    }
                    else {
                        bcsComunes.msg('error','OPERADOR','Error al actualizar configuracion');
                    }	
                    bcsComunes.ventanaCerrar(asDir.winId);
                    
            });	
            
        };
		
        asDir.win.cancelar = function() {
			bcsComunes.ventanaCerrar(asDir.winId);
		};
		
       
		
/*********************************************
 * DATASOURCES
 *********************************************/
    /*servicio que llama a vista con Instrumentos*/
    asDir.dsDetalleConfiguracionAsignacionDirecta = new kendo.data.DataSource({
        type: "odata",
        serverFiltering:true,
        transport: {
            read: {
                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1899",
                dataType: "json"
            }
        },
        schema: {
            model: {
                fields: {
                    IdEmpresa 			    : { type: "string"},
                    CodigoCorredora			: { type: "string"},
                    AsignacionAuto			: { type: "string"},	
                    ComprobanteAuto			: { type: "string"},	
                    MovimientoFacturarAuto 	: { type: "string"},	
                }
            }
        }
    });	
    /*servicio que llama a vista con Instrumentos*/
    asDir.dsTipoInstrumentos = new kendo.data.DataSource({
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
    
            asDir.resultadosTipoInstrumentosUnicos = resultadosFinales;
        }
    });		
		

/*********************************************
 * GRILLAS
 *********************************************/

		
/*********************************************
 * FUNCIONES
 *********************************************/
    asDir.fn.cargaDatos = function() { 
        var NoDatos = false;
        var filterOperador = [{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa }];
        asDir.dsDetalleConfiguracionAsignacionDirecta.query({filter:filterOperador}).then(function() {
            var detalleConf = asDir.dsDetalleConfiguracionAsignacionDirecta.view();
            if (detalleConf.length > 0){
                $.each(detalleConf,function(key,value){
                    detalleConf[key].AsignacionAuto 		= (value.AsignacionAuto==='S')?true:false;
                    detalleConf[key].ComprobanteAuto 		= (value.ComprobanteAuto==='S')?true:false;
                    detalleConf[key].MovimientoFacturarAuto	= (value.MovimientoFacturarAuto==='S')?true:false;
                });
                var datos = detalleConf;
                asDir.inicializarValores(datos);
            }else{
                asDir.dsTipoInstrumentos.filter(filterConf);
				asDir.dsTipoInstrumentos.read().then(function() {
					var detalleConf = asDir.resultadosTipoInstrumentosUnicos;
					if (detalleConf && detalleConf.length > 0) {
						asDir.datosDespues = _.clone(detalleConf);
						asDir.datosDespues.IdEstado = true;
			
						$.each(asDir.datosDespues, function(key, value) {
							asDir.datosDespues[value.IdTipoInstrumento] = {};
                        asDir.datosDespues[value.IdTipoInstrumento].AsignacionAuto 			= false;
                        asDir.datosDespues[value.IdTipoInstrumento].ComprobanteAuto 			= false;
                        asDir.datosDespues[value.IdTipoInstrumento].MovimientoFacturarAuto 	= false;
						});
					}
				});
            }
        });
        

        


    };
    asDir.inicializarValores = function(datos) {
        asDir.datosAntes 		= _.clone(datos);
        asDir.datosDespues 	= _.clone(datos);
        asDir.datosDespues.IdEstado = (datos.IdEstado==='ACT')?true:false;
        $.each(asDir.datosDespues, function(key,value){
            asDir.datosDespues[value.IdTipoInstrumento] = {};
            asDir.datosDespues[value.IdTipoInstrumento].AsignacionAuto 			= value.AsignacionAuto;
            asDir.datosDespues[value.IdTipoInstrumento].ComprobanteAuto 			= value.ComprobanteAuto;
            asDir.datosDespues[value.IdTipoInstrumento].MovimientoFacturarAuto 	= value.MovimientoFacturarAuto;
        }); 
        if (datos.IdSocioDeNegocio>0) { 
            $timeout(function(){ 
                asDir.datosDespues.IdSocioDeNegocio = datos.IdSocioDeNegocio; 
                asDir.datosDespues.IdSocioDeNegocioPost = datos.IdSocioDeNegocio;
            }, 400);
        }
        bcsComunes.ventanaAutoHeight(idVentana);
    }

    asDir.fn.genDatosGuardar = function() {
        var dg = {};
        dg.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        dg.p_IdUsuario = $scope.$root.SesionIdUsuario;
        dg.p_IdModulo = 'PLA';
        dg.p_FechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
        dg.p_DetalleConfiguracion = [];
        $.each(asDir.datosDespues,function(key,value){
            var aux = {};
            aux.IdTipoInstrumento 		= value.IdTipoInstrumento;
            aux.AsignacionAuto 			= value.AsignacionAuto?'S':'N';
            aux.ComprobanteAuto 		= value.ComprobanteAuto?'S':'N';
            aux.MovimientoFacturarAuto 	= value.MovimientoFacturarAuto?'S':'N';
            dg.p_DetalleConfiguracion.push(aux);
        });
        return dg;
    };	
    bcsComunes.ventanaAutoHeight(idVentana);  
    asDir.cambiarAsignacionAuto = function(item) {
        if (!item.AsignacionAuto) {
            item.MovimientoFacturarAuto = false;
            item.ComprobanteAuto = false;
        }
    };
     asDir.cambiarMovimientoFacturarAuto = function(item) {
        if (item.MovimientoFacturarAuto) {
            item.AsignacionAuto = true;
        }else{
            item.ComprobanteAuto = false;
        }
    };
    
    asDir.cambiarComprobanteAuto = function(item) {
        if (item.ComprobanteAuto) {
            item.AsignacionAuto = true;
            item.MovimientoFacturarAuto = true;
        }
    };  
		
/*********************************************
 * FUNCIONES WATCH 
 *********************************************/	
 
   
/*********************************************
 * GOOOOO
 *********************************************/	
     if(asDir.param && asDir.param.operador) {
        asDir.win.modoCrear = false;
        asDir.fn.cargaDatos(asDir.param.operador);
    }
    else {
        asDir.win.modoCrear = true;
        asDir.fn.cargaDatos();	
    }
	});
    
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------