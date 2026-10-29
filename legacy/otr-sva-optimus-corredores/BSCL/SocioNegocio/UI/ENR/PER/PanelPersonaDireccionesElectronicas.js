(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaDireccionesElectronicasEnrolCtrl',
	function($scope,$translate,bcsComunes,$resource,bcsAlerta,servicioSociodeNegocio){
		
		const ppde = this;
		ppde.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz["DIRECCION_ELECTRONICA"];
		ppde.idVentana = bcsComunes.ventanaWinId($scope);
		ppde.abrirModal = function(xIdEnrolamiento, xDireccionElectronica){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarDireccionElectronicaEnrolamiento",{ DireccionElectronica: xDireccionElectronica , IdEnrolamiento: xIdEnrolamiento, CodigoFormulario:ppde.CodigoFormulario  },false,false,ppde.idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data){
					ppde.direccionElectronicaSeleccionada = undefined;
					ppde.direccionElectronicaOptions.dataSource.read();
				}
	    	}); 
		};	
		
		ppde.agregar = function() {
			ppde.abrirModal(ppde.IdEnrolamiento,undefined,ppde.CodigoFormulario);
		};
		ppde.eliminar = function(die) {
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar la dirección seleccionada?", ppde.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){
					const datos = {
						p_IdEmpresa 			: $scope.SesionIdEmpresa,
						p_IdUsuario 			: $scope.SesionIdUsuario,
						p_IdModulo 				: 'SOC',
						p_UsaReglasNegocio 		: 'S',
						p_CodigoFormulario		: ppde.CodigoFormulario,
						p_IdTipoCanal			: 'OPT',
						p_DireccionesElectronicas:[{
							IdInterno : 1,
							IdDireccionElectronica: die.IdDireccionElectronica,
							IdEnrolamiento: die.IdEnrolamiento
						 }
					  ]
					}

					bcsComunes.ventanaOverlay(ppde.idVentana,true)
					servicioSociodeNegocio.eliminarBorradorDireccionElectronica(datos).$promise.then(
						function(result) {
							bcsComunes.ventanaOverlay(ppde.idVentana,false);
							if (result.P_OUT_ERRORESCONTADOR > 0){
								bcsComunes.msg("error","error","Error al borrar dirección electrónica");
								return;
							}
							
							bcsComunes.msg("info","info","Direccion Electrónica borrada");
							
							bcsComunes.ventanaMsg({ mensaje : "Direccion Electrónica borrada",conflictos : util.parseServerResponse(result)}, ppde.idVentana);
							ppde.direccionElectronicaOptions.dataSource.read();
						},
						function(result){
							bcsComunes.msg("error","error","Error al borrar Dirección Electrónica");
						});
				}
			});
		};
		ppde.modificar = function(die) {
			ppde.abrirModal(ppde.IdEnrolamiento,die);
		};
		ppde.predeterminar = function(die) {
			const cambiaPredeterminado = die.Predeterminado === 'UI_SI' ? 'N' : 'S';
			const datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'SOC',
				p_UsaReglasNegocio 		: 'S',
				p_CodigoFormulario		: ppde.CodigoFormulario,
				p_IdTipoCanal			: 'OPT',
				p_DireccionesElectronicas:[{
					IdInterno : 1,
					IdDireccionElectronica: die.IdDireccionElectronica || 0,
					IdEnrolamiento: die.IdEnrolamiento,
					Direccion: die.Direccion,
					CodigoProposito: die.CodigoProposito,
					Comentario: die.Comentario || '',
					Predeterminado: cambiaPredeterminado
				 }
			  ]
			}
	  
	        bcsComunes.ventanaOverlay(ppde.idVentana, true);    	    	
	        servicioSociodeNegocio.modificarBorradorDireccionElectronica(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_DIRECCION_ELECTRONICA_PREDETERMINADO"),
						conflictos : util.parseServerResponse(result)}, ppde.idVentana);
					ppde.direccionElectronicaOptions.dataSource.read();
					bcsComunes.ventanaOverlay(ppde.idVentana, false);
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppde.idVentana, false);
				});
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppde.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
			ppde.direccionElectronicaOptions = {
				dataSource: new bcs.DataSource({
					type : "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700205",
								dataType: "json"
							}
						},
						filter: { field: "IdEnrolamiento", operator: "eq", value: Number(ppde.IdEnrolamiento) },
						schema: {
							model: {
								fields: {
									IdDireccionElectronica : { type: "number"},
									IdEnrolamiento	 : { type: "number" },
									Direccion : { type: "string"},
									CodigoProposito : { type: "string"},
									DescripcionProposito : { type: "string"},
									Comentario : { type: "string"},
									Predeterminado : {type: "string"}
								}
							}
						}
					}
				}),
				reorderable: true,
				filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				columnMenu: false,
				resizable: true,
				autoBind: true,
				scrollable: true,
				height:200,
				columns: [{
					field: "Direccion",
					title: $translate.instant('UI_DIRECCION'),
					attributes: {style:"text-align:left;"},
					width: "350px"
				},
				{
					field: "DescripcionProposito",
					title: $translate.instant('UI_PROPOSITO'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},
				{
					field: "Comentario",
					title: $translate.instant('UI_COMENTARIO'),
					attributes: {style:"text-align:left;"},
					width: "350px"
				},
				{
					field: "Predeterminado",
					title: $translate.instant("UI_PREDETERMINADO"),
					template: "<span translate> #: Predeterminado # </span>",
					attributes: {style:"text-align:left;"},
					width: "80px"
				}]	
			};
		});	
	});
}());