angular.module('ModuloNormativa').controller('PanelCalendarioPagosCtrl', 
	function($scope, bcsServiciosNormativa, bcsAlerta, bcsComunes, Utils, bcsConfigGridModalDetalles){	
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		var pcp = this;
		var keyCabecera = $scope.mddj.keyCabecera;
		var version = $scope.mddj.version;
		var params = {tipo : "CALPAG", keyCab: keyCabecera };

		// GRILLA PRINCIPAL
		pcp.confBasicaCalendarioPagos = {
			dataSource : bcsConfigGridModalDetalles.dataSourceCalendarioPagos(keyCabecera),
			columns : bcsConfigGridModalDetalles.columnasCalendarioPagos(),
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
		
		pcp.abrirModal = function(objCalPag){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarCalendarioPagos",{ objCalPag: objCalPag, version: version, keyCab: keyCabecera},false,false, idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					pcp.seleccionadoCalendarioPagos = undefined;
					pcp.confBasicaCalendarioPagos.dataSource.read();
				}
	    	}); 
		};
		
		pcp.modificar = function(objCalPag) {
			pcp.abrirModal(objCalPag);
		};
		
		pcp.agregar = function() {
			pcp.abrirModal(undefined);
		};
		
			
		pcp.eliminar = function() {
			if (pcp.seleccionadoCalendarioPagos.length === 0) {
				bcsComunes.msg("error","Informacion","Debe seleccionar al menos un Calendario de Pago.");
				return;
			}
			var msgAlerta = pcp.seleccionadoCalendarioPagos.length > 1 ? "los Calendarios de Pago seleccionados?" : "el Calendario de Pago seleccionado?";
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar " + msgAlerta , idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){				
					var lstCalendarioPago = [];
					
					$.each(pcp.seleccionadoCalendarioPagos,function(key,value){
						lstCalendarioPago.push({ 
							KRutCtraparte1 : value.KRutCtraparte1,
							KIdContrato : value.KIdContrato,
							KFechaContrato : util.dateSPdate(value.KFechaContrato),
							KIdOpeEstruc : value.KIdOpeEstruc,
							kProducto : value.kProducto,
							kPeriodoComercial : util.dateSPdate(value.kPeriodoComercial),
							KS4_XR_NroFlu : value.KS4_XR_NroFlu					
						});
					});

					datos = {
						p_IdEmpresa 	  : $scope.SesionIdEmpresa,
						p_IdUsuario 	  : $scope.SesionIdUsuario,
						p_IdModulo		  : 'NOR',
						p_CalendarioPago  : lstCalendarioPago
					};
					
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServiciosNormativa.EliminarCalendarioPago(datos).$promise.then(
					function(result) {
						var msgSalida = lstCalendarioPago.length > 1 ? "Calendarios de Pago eliminados." : "Calendario de Pago eliminado." ;
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : msgSalida, conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function () {
                            bcsComunes.ventanaOverlay(idVentana, false);
							pcp.seleccionadoCalendarioPagos = undefined;
							pcp.confBasicaCalendarioPagos.dataSource.read();
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
		
		pcp.cargarDataBolson = function() {
			var id1 = bcsComunes.ventanaAbrir("CargadorDatosBolsones", params, false, false, idVentana);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(){ 
				pcp.seleccionadoCalendarioPagos = undefined;
				pcp.confBasicaCalendarioPagos.dataSource.read();
	    	}); 
		}
});
