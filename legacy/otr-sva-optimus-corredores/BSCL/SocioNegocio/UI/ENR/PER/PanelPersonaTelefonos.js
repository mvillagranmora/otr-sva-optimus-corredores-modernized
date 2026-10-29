(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaTelefonosEnrolCtrl',
	function($scope,$translate,bcsComunes,servicioSociodeNegocio){
		const ppt = this;
		ppt.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
		ppt.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['TELEFONO'];

		ppt.ventanaId = bcsComunes.ventanaWinId($scope);
		
		ppt.abrirModal = function(xIdEnrolamiento,xIdTelefono){
			let id1 = bcsComunes.ventanaAbrir("agregarModificarEnrolamientoTelefono",{ Telefono: xIdTelefono , IdEnrolamiento: xIdEnrolamiento, CodigoFormulario: ppt.CodigoFormulario },false,false,ppt.ventanaId); 
	        let promesa = bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data){
					ppt.telefonoSeleccionado = undefined;
					ppt.telefonoOptions.dataSource.read();
				}
	    	}); 
		};
		
		ppt.agregar = function agregar() {
			ppt.abrirModal(ppt.IdEnrolamiento,undefined,ppt.CodigoFormulario);
		};

		ppt.eliminar = function eliminar(tel) {
			const datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'SOC',
				p_UsaReglasNegocio 		: 'S',
				p_CodigoFormulario		: ppt.CodigoFormulario,
				p_IdTipoCanal			: 'OPT',
				p_Telefonos		: [{
					IdInterno : 1,
					IdTelefono : tel.IdTelefono || 0,
					IdEnrolamiento : ppt.IdEnrolamiento
				}]	
			};
			
			bcsComunes.ventanaOverlay(ppt.idVentana,true);
			servicioSociodeNegocio.eliminarBorradorTelefono(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(ppt.idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.msg("error","error","Error al borrar teléfono");
						return;
					}
					
					bcsComunes.msg("info","info","Teléfono borrado");
					
					bcsComunes.ventanaMsg({ mensaje : "Teléfono borrado",conflictos : util.parseServerResponse(result)}, ppt.idVentana);
					ppt.telefonoOptions.dataSource.read();
				},
				function(result){
					bcsComunes.msg("error","error","Error al borrar teléfono");
				}
			);		    
		};

		ppt.predeterminar = function predeterminar(tel) {
			const cambiarPredeterminar = tel.Predeterminado==='UI_SI' ? 'N' : 'S';
			const datos = {
				p_Telefonos:[{
					  IdInterno : 1,
					  IdEnrolamiento: tel.IdEnrolamiento,
					  IdTelefono: tel.IdTelefono,
					  IdPais: tel.IdPais,
					  IdTipoTelefono: tel.IdTipoTelefono,
					  Numero: tel.Numero,
					  Anexo: tel.Anexo || '',
					  CodigoProposito: tel.CodigoProposito,
					  Comentario: tel.Comentario || '',
					  Predeterminado: cambiarPredeterminar
				   }
				],
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'SOC',
				p_UsaReglasNegocio 		: 'S',
				p_CodigoFormulario		: ppt.CodigoFormulario,
				p_IdTipoCanal			: 'OPT',
			 }
		

		servicioSociodeNegocio.modificarBorradorTelefono(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay(ppt.ventanaId, false);
				bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_TELEFONO_PREDETERMINADO"),
					conflictos : util.parseServerResponse(result)}, ppt.ventanaId);
				ppt.telefonoOptions.dataSource.read();
			},
			function(result){
				bcsComunes.ventanaOverlay(ppt.ventanaId, false);
				bcsComunes.msg("error","Error","La operacion tiene errores");
			});
	};

		ppt.modificar = function modificar(tel) {
			ppt.abrirModal(ppt.IdEnrolamiento,tel);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppt.telefonoOptions = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700206",
								dataType: "json"
							}
						},
						filter: { field: "IdEnrolamiento", operator: "eq", value: Number(ppt.IdEnrolamiento) },
						schema: {
							model: {
								fields: {
									
									IdTelefono : { type: "number"},
									IdEnrolamiento : { type: "number"},
									IdPais : { type: "number"},
									NombrePais : { type: "string"},
									NombreTipoTelefono : { type: "string"},
									Comentario : { type: "string"},
									CodigoProposito : { type: "string"},
									DescripcionProposito : { type: "string"},
									Numero : { type: "string"},
									Anexo : { type: "string"},
									Predeterminado : { type: "string"}
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
					field: "NombreTipoTelefono",
					title: $translate.instant('UI_TIPO_TELEFONO'),
					attributes: {style:"text-align:left;"}
				},
				{
					field: "Numero",
					title: $translate.instant('UI_NUMERO'),
					attributes: {style:"text-align:left;"}
				},
				{
					field: "Comentario",
					title: $translate.instant('UI_COMENTARIO'),
					attributes: {style:"text-align:left;"}
				},
				{
					field: "DescripcionProposito",
					title: $translate.instant('UI_PROPOSITO'),
					attributes: {style:"text-align:left;"}
				},
				{
					field: "NombrePais",
					title: $translate.instant('UI_PAIS'),
					attributes: {style:"text-align:left;"}
				},	
				{
					field: "Anexo",
					title: $translate.instant('UI_ANEXO'),
					attributes: {style:"text-align:left;"}
				},
				{
					field: "Predeterminado",
					title: $translate.instant('UI_PREDETERMINADO'),
					template: "<span translate> #: Predeterminado #</span>",
					attributes: {style:"text-align:left;"}
				}]		
			};
		});	
	});
}());