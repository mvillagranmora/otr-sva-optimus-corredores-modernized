/*************************************************************
 * Purpose	: Panel Persona Telefonos
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaTelefonosCtrl', 
	function($scope,$translate,bcsComunes,$uibModal,$resource,bcsAlerta,servicioSociodeNegocio){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppt = this;		
		ppt.idVentana = bcsComunes.ventanaWinId($scope);
		ppt.abrirModal = function(xIdPersona,xIdTelefono)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarTelefono",
			{ 	Telefono: xIdTelefono , 
				IdPersona: xIdPersona,
				CodigoFormulario: ppt.CodigoFormulario },
				false,false,ppt.idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true)
				{
					ppt.telefonoSeleccionado = undefined;
					ppt.telefonoOptions.dataSource.read();
				}
	    	}); 
		};
		
		ppt.agregar = function() {
			ppt.abrirModal(ppt.IdPersona,undefined);
		};
		
		ppt.eliminar = function(tel) {
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_MENSAJE_ELIMINAR_TELEFONO"), ppt.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){
					if(tel.IdTelefono){
						var registroCambio = [];
						registroCambio.push({
							CampoModificado: "Borrado",
							ValorNuevo: "S",
							IdEntidad: tel.IdTelefono
						});
						
						var parametros = {
							p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
			        		p_IdUsuario 		:  $scope.$root.SesionIdUsuario,
							p_IdPersona         : ppt.idVentana,
			        		p_IdModulo 			: 'SOC',
			        		p_UsaReglasNegocio	: "N",
			        		p_IdTipoCanal 		: "OPT",
							p_RegistroCambio	: registroCambio
						};
						
						bcsComunes.ventanaOverlay(ppt.idVentana,true);
						
						servicioSociodeNegocio.eliminarPersonaTelefonoMasivo(parametros).$promise.then(
							function(result){
								bcsComunes.ventanaOverlay(ppt.idVentana,false);
								var srv=util.parseServerResponse(result);
								
								var subtitulo='';
								
								if (srv.erroresContador > 0){
									subtitulo='UI_OPERACION_CON_ERRORES';
									bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
								} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
									subtitulo='UI_TELEFONO_BORRADO';
								} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
									 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
								}
								
								let idMensaje = bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, ppt.idVentana);
								
								ppt.telefonoOptions.dataSource.read();
								
							},
							function(result){		
								bcsComunes.ventanaOverlay(ppt.idVentana,false);						
								var srv=util.parseServerResponse(result);
								var p=bcsComunes.ventanaMsg({ 
									mensaje : '', 
									conflictos : srv
								},ppt.idVentana);
							}
						);
					}
					
				} else {
					bcsComunes.ventanaOverlay(ppt.idVentana, false);
					return;
				}
			});	
		};
		
		ppt.predeterminar = function(tel) {
			bcsComunes.ventanaOverlay(ppt.idVentana, true);
			var predeterminarTelefonoSP = function(v_Dato)
		    {
			       var urlServicio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13024";
			       var r = $resource
			         ("", {}, {
				                 'urlServicio': { method: "POST", url: urlServicio}
			                  }
			         );
			       var post = r.urlServicio(v_Dato);
			       return post.$promise;
		    };
		    
		    var parametrosSP = {};
	        parametrosSP.p_IdEmpresa 		= $scope.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.SesionIdUsuario;
	        parametrosSP.p_IdModulo 		= 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "S";
	        parametrosSP.p_IdTelefono = tel.IdTelefono;
	            	    	
	        predeterminarTelefonoSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaOverlay(ppt.idVentana, false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_TELEFONO_PREDETERMINADO"),
						conflictos : util.parseServerResponse(result)}, ppt.idVentana);
					ppt.telefonoOptions.dataSource.read();
				},
				function(result){
					bcsComunes.ventanaOverlay(ppt.idVentana, false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
		};
		
		ppt.modificar = function(tel) {
			ppt.abrirModal(ppt.IdPersona,tel);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			
			ppt.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppt.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['TELEFONO'];

			ppt.telefonoOptions = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1319",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppt.IdPersona) },
						schema: {
							model: {
								fields: {
									
									IdTelefono : { type: "number"},
									IdPersona : { type: "number"},
									IdPais : { type: "number"},
									NombrePais : { type: "string"},
									NombreTipoTelefono : { type: "string"},
									Comentario : { type: "string"},
									CodigoProposito : { type: "string"},
									DescripcionProposito : { type: "string"},
									Numero : { type: "string"},
									Anexo : { type: "string"},
									IdEstado : { type: "string"},
									NombreEstado : { type: "string"},
									FechaIngreso : { type: "date"},
									FechaCreacion : { type: "date"},
									FechaModificacion : { type: "date"},
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
					field: "NombreEstado",
					title: $translate.instant('UI_ESTADO'),
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
//------------------------- FIN CONTROLLER --------------------------