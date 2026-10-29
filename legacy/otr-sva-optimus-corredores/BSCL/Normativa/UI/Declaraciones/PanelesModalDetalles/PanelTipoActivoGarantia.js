angular.module('ModuloNormativa').controller('PanelTipoActivoGarantiaCtrl', 
	function($scope, bcsServiciosNormativa, bcsAlerta, bcsComunes, bcsConfigGridModalDetalles){	
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		var pdg = this;
		var keyCabecera = $scope.mddj.keyCabecera;
		var version = $scope.mddj.version;
		var params = {tipo : "TIPACTGAR", keyCab: keyCabecera };
		
		// GRILLA PRINCIPAL
		pdg.confBasicaTipoActivoGarantia = {
			dataSource : bcsConfigGridModalDetalles.dataSourceTipoActivoGarantias(keyCabecera),
			columns : bcsConfigGridModalDetalles.columnasTipoActivoGarantias(),
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
		
		pdg.abrirModal = function(objTipoActivoGarantia)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarTipoActivoGarantia",{ objTipoActivoGarantia: objTipoActivoGarantia, version: version, keyCab: keyCabecera},false,false, idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					pdg.seleccionadoTipoActivoGarantia = undefined;
					pdg.confBasicaTipoActivoGarantia.dataSource.read();
				}
	    	}); 
		};
		
		pdg.modificar = function(objTipoActivoGarantia) {
			pdg.abrirModal(objTipoActivoGarantia);
		};
		
		pdg.agregar = function() {
			pdg.abrirModal(undefined);
		};
		
			
		pdg.eliminar = function() {
			if (pdg.seleccionadoTipoActivoGarantia.length === 0) {
				bcsComunes.msg("error","Informacion","Debe seleccionar al menos un Tipo Activo de Garantía.");
				return;
			}
			var msgAlerta = pdg.seleccionadoTipoActivoGarantia.length > 1 ? "los Tipos Activo de Grantías seleccionados?" : "el Tipo Activo de Garantía seleccionado?";
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar " + msgAlerta , idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){				
					var lstTipoActivoGarantia = [];
					
					$.each(pdg.seleccionadoTipoActivoGarantia,function(key,value){
						lstTipoActivoGarantia.push({ 
							KRutCtraparte1 : value.KRutCtraparte1,
							KIdContrato : value.KIdContrato,
							KFechaContrato : util.dateSPdate(value.KFechaContrato),
							KIdOpeEstruc : value.KIdOpeEstruc,
							kProducto : value.kProducto,
							kPeriodoComercial : util.dateSPdate(value.kPeriodoComercial),
							KS7_IdGar : value.KS7_IdGar
						});
					});

					datos = {
						p_IdEmpresa 	  			 : $scope.SesionIdEmpresa,
						p_IdUsuario 	  			 : $scope.SesionIdUsuario,
						p_IdModulo		  			 : 'NOR',
						p_TipoActivoGarantia  		 : lstTipoActivoGarantia
					};
					
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServiciosNormativa.EliminarTipoActivoGarantia(datos).$promise.then(
					function(result) {
						var msgSalida = lstTipoActivoGarantia.length > 1 ? "Tipos Activo de Garantías eliminados." : "Tipo Activo de Garantía eliminado." ;
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : msgSalida, conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function () {
                            bcsComunes.ventanaOverlay(idVentana, false);
							pdg.seleccionadoTipoActivoGarantia = undefined;
							pdg.confBasicaTipoActivoGarantia.dataSource.read();
                        });
						
					},
					function(result){
						bcsComunes.msg("error","Error","Error al procesar la eliminación.");
						bcsComunes.ventanaOverlay(idVentana, false);
					});
				}
			});
		};
		
		pdg.cargarDataBolson = function() {
			var id1 = bcsComunes.ventanaAbrir("CargadorDatosBolsones", params, false, false, idVentana);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(){ 
				pdg.seleccionadoTipoActivoGarantia = undefined;
				pdg.confBasicaTipoActivoGarantia.dataSource.read();
	    	}); 
		}
});
