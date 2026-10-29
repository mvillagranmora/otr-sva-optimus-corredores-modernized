/*************************************************************
 * Purpose	: Panel Persona Tarjetas Bancarias
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaTarjetasBancariasCtrl',
	function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$uibModal,$resource,servicioSociodeNegocio){
		
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppca = this;
		ppca.showBloqueo = false;
		ppca.idVentana = bcsComunes.ventanaWinId($scope);
		
		$scope.$watch("ppca.TarjetaBancariaSeleccionada",
	        function handleDocumentoIdentidad( newValue, oldValue) {
				if(newValue !== undefined){
					if(newValue.IdEstado === 'ACT')
					{
						ppca.showBloqueo = true;
					}
					else
					{
						ppca.showBloqueo = false;
					}
				}
	        }
	    );
		
		ppca.abrirModal = function(xIdPersona,xTarjetaBancaria)
		{
			const id1 = bcsComunes.ventanaAbrir("agregarModificarTarjetaBancaria", {
				TarjetaBancaria: xTarjetaBancaria,
				IdPersona: xIdPersona,
				CodigoFormulario: ppca.CodigoFormulario
			}, false, false, ppca.idVentana);

			const promesa = bcsComunes.ventanaPromesa(id1);
			promesa.then(function (data) {

				if (data === true) {
					ppca.TarjetaBancariaSeleccionada = undefined;
					ppca.TarjetaBancariaOptions.dataSource.read();
				}
			});
		};
		
		
		ppca.agregar = function() {
			ppca.abrirModal(ppca.IdPersona,undefined);
		};
		
		ppca.eliminar = function(cta) {
			
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_MENSAJE_ELIMINAR_TARJETA"), ppca.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){
			
			
					if (cta.IdTarjetaBancaria){
						var registroCambio = [];
						registroCambio.push({
							CampoModificado: "Borrado",
							ValorNuevo: "S",
							IdEntidad: cta.IdTarjetaBancaria
						});
						
						var parametros = {
							p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
			        		p_IdUsuario 		:  $scope.$root.SesionIdUsuario,
							p_IdPersona         : ppca.IdPersona,
			        		p_IdModulo 			: 'SOC',
			        		p_UsaReglasNegocio	: "S",
			        		p_IdTipoCanal 		: "OPT",
							p_RegistroCambio	: registroCambio
						};
						
						bcsComunes.ventanaOverlay(ppca.ventanaId,true);
						
						servicioSociodeNegocio.eliminarPersonaTarjetaBancariaMasivo(parametros).$promise.then(
							function(result){
								bcsComunes.ventanaOverlay(ppca.ventanaId,false);
								var srv=util.parseServerResponse(result);
								
								if (srv.erroresContador > 0){
									subtitulo='UI_OPERACION_CON_ERRORES';
									bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
								} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
									subtitulo='UI_TARJETA_BANCARIA_BORRADA';
								} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
									 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
								}
								
								bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, ppca.ventanaId);
								
								ppca.TarjetaBancariaOptions.dataSource.read();
							},
							function(result){		
								bcsComunes.ventanaOverlay(ppca.ventanaId,false);						
								var srv=util.parseServerResponse(result);
								var p=bcsComunes.ventanaMsg({ 
									mensaje : '', 
									conflictos : srv
								},ppca.ventanaId);
							}
						);
						
					}	
				} else {
					bcsComunes.ventanaOverlay(ppca.ventanaId, false);
					return;
				}
			});	
		};
		ppca.modificar = function(cta) {
			ppca.abrirModal(ppca.IdPersona,cta);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppca.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['TARJETA_BANCARIA'];
			ppca.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppca.TarjetaBancariaOptions = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1318",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppca.IdPersona) },
						schema: {
							model: {
								fields: {
									IdTarjetaBancaria : { type: "number"},
									IdBanco : { type: "number"},
									NombreBanco : { type: "string"},
									IdTipoTarjetaBancaria: { type: "string"},
									NombreTipoTarjetaBancaria: { type: "string"},
									NumeroTarjetaBancariaEnmasc: { type: "string"},
									IdPersona: { type: "number"},
									FechaIngreso: { type: "date"},
									FechaCreacion: { type: "date"},
									FechaModificacion: { type: "date"},
									FechaVencimiento: { type: "date"},							
									IdEstado: { type: "string"},
									NombreEstado: { type: "string"}
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
					field: "NombreBanco",
					title: $translate.instant('UI_BANCO'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},
				{
					field: "NombreTipoTarjetaBancaria",
					title: $translate.instant('UI_TIPO'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},
				{
					field: "NumeroTarjetaBancariaEnmasc",
					title: $translate.instant('UI_NUMERO_TARJETA'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},		
				{
					field: "FechaVencimiento",
					title: $translate.instant('UI_FECHA_VENCIMIENTO'),
					attributes: {style:"text-align:left;"},
					format: "{0:d}",
					width: "150px"
				},
				
				{
					field: "NombreEstado",
					title: $translate.instant('UI_ESTADO'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				}]		
			};
		});	
		
		
		var cambioEstadoTarjetaBancariaSP = function(v_Dato)
	    {
	       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=115";
	       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
	       var post = r.urlConsulta(v_Dato);
	       return post.$promise;
	    };
		
	    
	    ppca.bloqueoTarjeta = function () {
	    	var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdPersona = ppca.IdPersona;
	        parametrosSP.p_IdModulo = 'PER';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdTarjetaBancaria = ppca.TarjetaBancariaSeleccionada.IdTarjetaBancaria;
	        parametrosSP.p_IdEstado = 'VCD';
	        parametrosSP.p_Comentario = 'Bloqueo por interfaz';  	    	
	        cambioEstadoTarjetaBancariaSP(parametrosSP).then(
				function(result) {

					bcsComunes.ventanaMsg({ mensaje : "Tarjeta Bancaria Bloqueada",
						conflictos : util.parseServerResponse(result)}, ppca.idVentana);
					ppca.TarjetaBancariaOptions.dataSource.read();
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	    };
	    
	    ppca.desbloqueoTarjeta = function () {
	    	var parametrosSP = {};
	    	parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdPersona = ppca.IdPersona;
	        parametrosSP.p_IdModulo = 'PER';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdTarjetaBancaria = ppca.TarjetaBancariaSeleccionada.IdTarjetaBancaria;
	        parametrosSP.p_IdEstado = 'ACT';
	        parametrosSP.p_Comentario = 'Desbloqueo por interfaz';    	
	        cambioEstadoTarjetaBancariaSP(parametrosSP).then(
				function(result) {

					bcsComunes.ventanaMsg({ mensaje : "Tarjeta Bancaria Desbloqueada",
						conflictos : util.parseServerResponse(result)}, ppca.idVentana);
					ppca.TarjetaBancariaOptions.dataSource.read();
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	    };
	});
}());
//------------------------- FIN CONTROLLER --------------------------