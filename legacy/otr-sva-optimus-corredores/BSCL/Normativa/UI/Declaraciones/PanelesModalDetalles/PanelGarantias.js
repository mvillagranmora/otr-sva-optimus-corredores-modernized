 angular.module('ModuloNormativa').controller('PanelGarantiasCtrl', 
	function($scope,bcsComunes,bcsServiciosNormativa, bcsConfigGridModalDetalles, bcsAlerta){	
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		var pgc = this;
		var keyCabecera = $scope.mddj.keyCabecera;
		var version = $scope.mddj.version;
		var params = {tipo : "GAR", keyCab: keyCabecera };
		
		// GRILLA PRINCIPAL
		pgc.confBasicaGarantias = {
			dataSource : bcsConfigGridModalDetalles.dataSourceGarantias(keyCabecera),
			columns : bcsConfigGridModalDetalles.columnasGarantias(),
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
		
		pgc.abrirModal = function(objGarantia){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarGarantias",{ objGarantia: objGarantia, version: version, keyCab: keyCabecera},false,false, idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					pgc.seleccionadoGarantias = undefined;
					pgc.confBasicaGarantias.dataSource.read();
				}
	    	}); 
		};
		
		pgc.modificar = function(objGarantia) {
			pgc.abrirModal(objGarantia);
		};
		
		pgc.agregar = function() {
			pgc.abrirModal(undefined);
		};
		
			
		pgc.eliminar = function() {
			if (pgc.seleccionadoGarantias.length === 0) {
				bcsComunes.msg("error","Informacion","Debe seleccionar al menos una Garantía.");
				return;
			}
			var msgAlerta = pgc.seleccionadoGarantias.length > 1 ? "las Garantías seleccionadas?" : "la Garantía seleccionada?";
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar " + msgAlerta , idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){				
					var lstGarantias = [];
					
					$.each(pgc.seleccionadoGarantias,function(key,value){
						lstGarantias.push({ 
							KRutCtraparte1 : value.KRutCtraparte1,
							KIdContrato : value.KIdContrato,
							KFechaContrato : util.dateSPdate(value.KFechaContrato),
							KIdOpeEstruc : value.KIdOpeEstruc,
							kProducto : value.kProducto,
							kPeriodoComercial : util.dateSPdate(value.kPeriodoComercial),
							KS6_IdGar : value.KS6_IdGar
						});
					});

					datos = {
						p_IdEmpresa 	  : $scope.SesionIdEmpresa,
						p_IdUsuario 	  : $scope.SesionIdUsuario,
						p_IdModulo		  : 'NOR',
						p_Garantias    	  : lstGarantias
					};
					
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServiciosNormativa.EliminarGarantia(datos).$promise.then(
					function(result) {
						var msgSalida = lstGarantias.length > 1 ? "Garantías eliminadas." : "Garantía eliminada." ;
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : msgSalida, conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function () {
                            bcsComunes.ventanaOverlay(idVentana, false);
							pgc.seleccionadoGarantias = undefined;
							pgc.confBasicaGarantias.dataSource.read();
                        });
						
					},
					function(result){
						bcsComunes.msg("error","Error","Error al procesar la eliminación.");
						bcsComunes.ventanaOverlay(idVentana, false);
					});
				}
			});
		};
		
		pgc.cargarDataBolson = function() {
			var id1 = bcsComunes.ventanaAbrir("CargadorDatosBolsones", params, false, false, idVentana);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(){ 
				pgc.seleccionadoGarantias = undefined;
				pgc.confBasicaGarantias.dataSource.read();
	    	}); 
		}
});
