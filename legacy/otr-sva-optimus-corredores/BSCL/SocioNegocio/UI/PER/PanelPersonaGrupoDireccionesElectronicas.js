/*************************************************************
 * Purpose	: Panel Persona Grupo Direcciones Electronicas
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaGrupoDireccionesElectronicasCtrl',function($scope,
			$translate,bcsComunes,bcsAlerta, $uibModal,$resource,servicioSociodeNegocio){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppde = this;
		ppde.idVentana = bcsComunes.ventanaWinId($scope);
		ppde.abrirModal = function(xIdPersona)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarGrupoDireccionElectronica", {
				IdPersona: xIdPersona,
				CodigoFormulario: ppde.CodigoFormulario
			}, false, false, ppde.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(){ 
	        	ppde.direccionElectronicaOptions.dataSource.read();
	    	}); 
		};

		ppde.agregar = function() {
			ppde.abrirModal(ppde.IdPersona);
		};
		ppde.eliminar = function(grupoSeleccionado) {
			console.log('grupoSeleccionado',grupoSeleccionado)
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar el grupo seleccionado?", ppde.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){
					
					var registroCambio = [];
					registroCambio.push({
						CampoModificado: "Borrado",
						ValorNuevo: "S",
						IdEntidad: grupoSeleccionado.IdGrupoDireccion
					});
					
					var parametros = {
						p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
		        		p_IdUsuario 		:  $scope.$root.SesionIdUsuario,
		        		p_IdModulo 			: 'SOC',
		        		p_IdPersona 		: ppde.IdPersona,
		        		p_UsaReglasNegocio	: "N",
		        		p_IdTipoCanal 		: "OPT",
						p_RegistroCambio	: registroCambio
					};
					
					bcsComunes.ventanaOverlay(ppde.ventanaId,true);
				
					servicioSociodeNegocio.eliminarPersonaGrupoDireccionElectronicaMasivo(parametros).$promise.then(
						function(result){
							bcsComunes.ventanaOverlay(ppde.ventanaId,false);
							var srv=util.parseServerResponse(result);
							
							var subtitulo='';
							if (srv.erroresContador > 0){
							subtitulo='UI_OPERACION_CON_ERRORES';
							bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
							} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
								subtitulo='UI_GRUPO_DIRECCION_ELECTRONICA_BORRADA';
							} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
								 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
							}
							
							bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, ppde.ventanaId);
							ppde.direccionElectronicaOptions.dataSource.read();	
						},
						function(result){		
							bcsComunes.ventanaOverlay(ppde.ventanaId,false);						
							var srv=util.parseServerResponse(result);
							var p=bcsComunes.ventanaMsg({ 
								mensaje : '', 
								conflictos : srv
							},ppde.ventanaId);
						}
					);
				
					/*var eliminarDireccionFisicaSP = function(v_Dato)
					{
						var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13031";
						var r = $resource
						("", {}, {
							'urlConsulta': { method: "POST", url: urlConsulta}
						}
					);
					var post = r.urlConsulta(v_Dato);
						return post.$promise;
					};

					var parametrosSP = {};
					parametrosSP.p_IdEmpresa 		= $scope.SesionIdEmpresa;
					parametrosSP.p_IdUsuario 		=  $scope.SesionIdUsuario;
					parametrosSP.p_IdModulo 		= 'SOC';
					parametrosSP.p_UsaReglasNegocio = "N";
					parametrosSP.p_IdGrupoDireccion = grupoSeleccionado.IdGrupoDireccion;

					bcsComunes.ventanaOverlay(ppde.idVentana, true);
					eliminarDireccionFisicaSP(parametrosSP).then(
					function(result) {
						bcsComunes.ventanaMsg({ 
							mensaje : "Grupo Direccion Borrado",
							conflictos : util.parseServerResponse(result)}, ppde.idVentana);
						ppde.direccionElectronicaOptions.dataSource.read();
						bcsComunes.ventanaOverlay(ppde.idVentana, false);
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
						bcsComunes.ventanaOverlay(ppde.idVentana, false);
					});*/
				}
			});
		};
		ppde.modificar = function(grupoSeleccionado) {
			console.log('grupoSeleccionado',grupoSeleccionado)
			var id1 = bcsComunes.ventanaAbrir("agregarGrupoDireccionElectronicaDetalle",
				{
					IdPersona: ppde.IdPersona,
					Grupo: grupoSeleccionado,
					CodigoFormulario: ppde.CodigoFormulario,
					CodigoFormularioDetalle: ppde.CodigoFormularioDetalle
				}, false, false, ppde.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(){ 
	        	ppde.direccionElectronicaOptions.dataSource.read();
	    	});
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppde.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['GRUPO_DIRECCION_ELECTRONICA'];
			ppde.CodigoFormularioDetalle = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['GRUPO_DIRECCION_ELECTRONICA_DETALLE'];
			ppde.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppde.direccionElectronicaOptions = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1316",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppde.IdPersona) },
						schema: {
							model: {
								fields: {
									IdGrupoDireccion : { type: "number"},
									Grupo : { type : "string"},
									Direccion : { type: "string"},
									IdPersona : { type: "number"},
									IdEstado : { type: "string"},
									Comentario: { type: "string"},
									NombreEstado : { type: "string"},
									FechaIngreso : { type: "date"},
									FechaCreacion : { type: "date"},
									FechaModificacion : { type: "date"}
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
					field: "Grupo",
					title: $translate.instant('UI_GRUPO'),
					attributes: {style:"text-align:left;"},
					width: "370px"
				},
				{
					field: "Comentario",
					title: $translate.instant('UI_COMENTARIO'),
					attributes: {style:"text-align:left;"},
					width: "500px"
				},
				{
					field: "NombreEstado",
					title: $translate.instant('UI_ESTADO'),
					attributes: {style:"text-align:left;"},
					width: "220px"
				}]	
			};
		});	
	});
}());
//------------------------- FIN CONTROLLER --------------------------