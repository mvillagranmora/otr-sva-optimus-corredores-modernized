angular.module('ModuloNormativa').controller('PanelPagosPeriodoCtrl', 
	function($scope,bcsComunes,bcsServiciosNormativa, bcsConfigGridModalDetalles, bcsAlerta){	
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		var ppp = this;
		var keyCabecera = $scope.mddj.keyCabecera;
		var version = $scope.mddj.version;
		var params = {tipo : "PAGPER", keyCab: keyCabecera };
		
		// GRILLA PRINCIPAL
		ppp.confBasicaPagosPeriodo = {
			dataSource : bcsConfigGridModalDetalles.dataSourcePagosPeriodo(keyCabecera),
			columns : bcsConfigGridModalDetalles.columnasPagosPeriodo(),
			pageable : {
		    	refresh: true,
		       	buttonCount: 5
		    },
			height : 470,
			selectable : "multiple row",
			userSettings : true,
			sortable : true,
			filterable : false,
			groupable : false,
			autoBind: true
		};
		
		ppp.abrirModal = function(objPagoPeriodo){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarPagosPeriodo",{ objPagoPeriodo: objPagoPeriodo, version: version, keyCab: keyCabecera},false,false, idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					ppp.seleccionadoPagosPeriodo = undefined;
					ppp.confBasicaPagosPeriodo.dataSource.read();
				}
	    	}); 
		};
		
		ppp.modificar = function(objPagoPeriodo) {
			ppp.abrirModal(objPagoPeriodo);
		};
		
		ppp.agregar = function() {
			ppp.abrirModal(undefined);
		};
		
			
		ppp.eliminar = function() {
			if (ppp.seleccionadoPagosPeriodo.length === 0) {
				bcsComunes.msg("error","Informacion","Debe seleccionar al menos un Periodo de Pago.");
				return;
			}
			var msgAlerta = ppp.seleccionadoPagosPeriodo.length > 1 ? "los Pagos de Periodo seleccionados?" : "el Pago de Periodo seleccionado?";
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar " + msgAlerta , idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){				
					var lstPagosPeriodo = [];
					
					$.each(ppp.seleccionadoPagosPeriodo,function(key,value){
						lstPagosPeriodo.push({ 
							KRutCtraparte1 : value.KRutCtraparte1,
							KIdContrato : value.KIdContrato,
							KFechaContrato : util.dateSPdate(value.KFechaContrato),
							KIdOpeEstruc : value.KIdOpeEstruc,
							kProducto : value.kProducto,
							kPeriodoComercial : util.dateSPdate(value.kPeriodoComercial),
							KS5_CorrPago : value.KS5_CorrPago, 
							KS5_IdPago: value.KS5_IdPago });
					});

					datos = {
						p_IdEmpresa 	  : $scope.SesionIdEmpresa,
						p_IdUsuario 	  : $scope.SesionIdUsuario,
						p_IdModulo		  : 'NOR',
						p_PagosPeriodo    : lstPagosPeriodo
					};
					
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServiciosNormativa.EliminarPagosPeriodo(datos).$promise.then(
					function(result) {
						var msgSalida = lstPagosPeriodo.length > 1 ? "Periodos de Pago eliminados." : "Periodo de Pago eliminado." ;
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : msgSalida, conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function () {
                            bcsComunes.ventanaOverlay(idVentana, false);
							ppp.seleccionadoPagosPeriodo = undefined;
							ppp.confBasicaPagosPeriodo.dataSource.read();
                        });
						
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","Error","Error al procesar la eliminación.");
						bcsComunes.ventanaOverlay(idVentana, false);
					});
				}
			});
		};
		
		ppp.cargarDataBolson = function() {
			var id1 = bcsComunes.ventanaAbrir("CargadorDatosBolsones", params, false, false, idVentana);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(){ 
				ppp.seleccionadoPagosPeriodo = undefined;
				ppp.confBasicaPagosPeriodo.dataSource.read();
	    	}); 
		}
});
