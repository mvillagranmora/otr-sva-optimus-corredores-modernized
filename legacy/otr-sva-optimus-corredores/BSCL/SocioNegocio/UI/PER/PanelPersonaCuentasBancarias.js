/*************************************************************
 * Purpose	: Panel Persona Cuentas Bancarias.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaCuentasBancariasCtrl',
	function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$uibModal, $resource,servicioSociodeNegocio){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppca = this;
		ppca.showBloqueo = false;
		ppca.ventanaId = bcsComunes.ventanaWinId($scope);
		
		$scope.$watch("ppca.cuentaBancariaSeleccionada",
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
		
		ppca.abrirModal = function(xIdPersona,xCuentaBancaria, xCodigoTipoPersona, xIdCliente, xCuentasBancarias, xEsPac)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarCuentaBancaria",{ 
				CuentaBancaria: xCuentaBancaria, 
				IdPersona: xIdPersona,
				CodigoTipoPersona: xCodigoTipoPersona, 
				IdCliente : xIdCliente, 
				CuentasBancarias: xCuentasBancarias,
				EsPac: xEsPac,
				CodigoFormulario: ppca.CodigoFormulario },
				false,false,ppca.ventanaId); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true)
				{
					ppca.cuentaBancariaSeleccionada = undefined;
					ppca.cuentaBancariaOptions.dataSource.read();
				}
	    	}); 
		};
		
		ppca.agregar = function() {
			ppca.abrirModal(ppca.IdPersona,undefined, $scope.admSoc.dataSocioNegocio.CodigoTipoPersona, $scope.admSoc.dataSocioNegocio.IdCliente, ppca.cuentaBancariaOptions.dataSource.data());
		};
		
		ppca.eliminar = function(cta) {
            var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_CUENTA_BANCARIA"));
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					
					if (cta.IdCuentaBancaria){
						var registroCambio = [];
						registroCambio.push({
							CampoModificado: "Borrado",
							ValorNuevo: "S",
							IdEntidad: ppca.cuentaBancariaSeleccionada.IdCuentaBancaria
						});
						
						var parametros = {
							p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
			        		p_IdUsuario 		:  $scope.$root.SesionIdUsuario,
						p_IdPersona 		:  ppca.IdPersona,
			        		p_IdModulo 			: 'SOC',
			        		p_UsaReglasNegocio	: "S",
			        		p_IdTipoCanal 		: "OPT",
							p_RegistroCambio	: registroCambio
						};
						
						bcsComunes.ventanaOverlay(ppca.ventanaId,true);
						
						servicioSociodeNegocio.eliminarPersonaCuentaBancariaMasivo(parametros).$promise.then(
							function(result){
								bcsComunes.ventanaOverlay(ppca.ventanaId,false);
								var srv=util.parseServerResponse(result);
								
								var subtitulo='';
								if (srv.erroresContador > 0){
									subtitulo='UI_OPERACION_CON_ERRORES';
									bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
								} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
									subtitulo='UI_CUENTA_BANCARIA_BORRADA';
								} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
									 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
								}
								
								bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, ppca.ventanaId);
								ppca.cuentaBancariaOptions.dataSource.read();	
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
					/*var eliminarCuentaBancariaSP = function(v_Dato)
				    {
					       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1687";
					       var r = $resource
					         ("", {}, {
						                 'urlConsulta': { method: "POST", url: urlConsulta}
					                  }
					         );
					       var post = r.urlConsulta(v_Dato);
					       return post.$promise;
				    };
				    
				    var parametrosSP = {};
			        parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
			        parametrosSP.p_IdUsuario 		=  $scope.$root.SesionIdUsuario;
			        parametrosSP.p_IdModulo 		= 'SOC';
			        parametrosSP.p_UsaReglasNegocio = "N";
			        parametrosSP.p_IdCuentaBancaria = ppca.cuentaBancariaSeleccionada.IdCuentaBancaria;
			        parametrosSP.p_Comentario 		= 'Borrado por interfaz';
			            	    	
			        eliminarCuentaBancariaSP(parametrosSP).then(
						function(result) {
							bcsComunes.ventanaMsg({ mensaje : "Cuenta Bancaria Borrada",
								conflictos : util.parseServerResponse(result)}, ppca.ventanaId);
							ppca.cuentaBancariaOptions.dataSource.read();
						},
						function(result){
							bcsComunes.msg("error","Error","La operacion tiene errores");
						});*/
			        
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
			ppca.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppca.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['CUENTA_BANCARIA'];
			ppca.cuentaBancariaOptions = {
				dataSource: new bcs.DataSource({
					type : "odata",
					definition : {
						serverFiltering : true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1310",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppca.IdPersona) },
						schema: {
							model: {
								fields: {
									IdCuentaBancaria 			: { type: "number"},
									IdBanco 					: { type: "number"},
									NombreBanco 				: { type: "string"},
									IdPais 						: { type: "number"},
									NombrePais 					: { type: "string"},
									IdTipoCuentaBancaria		: { type: "string"},
									NombreTipoCuentaBancaria	: { type: "string"},
									IdMoneda					: { type: "string"},
									NumeroCuentaBancaria		: { type: "string"},
									IdPersona					: { type: "number"},
									FechaIngreso				: { type: "date"},
									FechaCreacion				: { type: "date"},
									FechaModificacion			: { type: "date"},
									IdEstado					: { type: "string"},
									NombreEstado				: { type: "string"},
									IdTipoIdentificadorCuenta	: { type: "string"},
									IdentificadorCuenta			: { type: "string"},
									IdPersonaRelacionada		: { type: "number"},
									NombrePersonaRelacionada	: { type: "string"},
									CodigoCuentaInterbancario	: { type: "string"},
									CodigoAba					: { type: "string"},
									OtroCodigoBancoExtranjero	: { type: "string"},
									CodigoIban					: { type: "string"},
									OtroCodigoCuentaExtranjero	: { type: "string"},
									IdPlaza						: { type: "number"},
									NombrePlaza					: { type: "string"},
									EsPac					    : { type: "string"},
									Predeterminado			    : { type: "string"},
									IdCliente					: { type: "number"},
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
					field: "NombrePais",
					title: $translate.instant('UI_PAIS'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},	
				{
					field: "IdMoneda",
					title: $translate.instant('UI_MONEDA'),
					attributes: {style:"text-align:left;"},
					width: "50px"
				},
				{
					field: "NombreTipoCuentaBancaria",
					title: $translate.instant('UI_TIPO'),
					attributes: {style:"text-align:left;"},
					width: "150px"
				},
				{
					field: "NumeroCuentaBancaria",
					title: $translate.instant('UI_NUMERO_CUENTA'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},
				{
					field: "IdTipoIdentificadorCuenta",
					title: $translate.instant('UI_TIPO_IDENTIFICADOR_CUENTA'),
					attributes: {style:"text-align:left;"},
					width: "100px",
					hidden: !($scope.SesionLocale=='PE')
				},
				{
					field: "IdentificadorCuenta",
					title: $translate.instant('UI_IDENTIFICADOR_CUENTA'),
					attributes: {style:"text-align:left;"},
					width: "100px",
					hidden: !($scope.SesionLocale=='PE')
				},
				{
					field: "NombrePersonaRelacionada",
					title: $translate.instant('UI_RAZON_SOCIAL_O_NOMBRE'),
					attributes: {style:"text-align:left;"},
					width: "100px",
					hidden: !($scope.SesionLocale=='PE')
				},
				{
					field: "CodigoCuentaInterbancario",
					title: $translate.instant('UI_CODIGO_CUENTA_INTERBANCARIO'),
					attributes: {style:"text-align:left;"},
					width: "100px",
					hidden: !($scope.SesionLocale=='PE')
				},
				{
					field: "NombrePlaza",
					title: $translate.instant('UI_PLAZA'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},				
				{
					field: "NombreEstado",
					title: $translate.instant('UI_ESTADO'),
					attributes: {style:"text-align:left;"},
					width: "100px"
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
		
		var cambioEstadoCuentaBancariaSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=116";
		       var r = $resource
		         ("", {}, {
			                 'urlConsulta': { method: "POST", url: urlConsulta}
		                  }
		         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
		
	    ppca.bloqueoCuenta = function () {
	    	var parametrosSP = {};
	        parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.$root.SesionIdUsuario;
		parametrosSP.p_IdPersona 		=  ppca.IdPersona;
	        parametrosSP.p_IdModulo 		= 'PER';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdCuentaBancaria = ppca.cuentaBancariaSeleccionada.IdCuentaBancaria;
	        parametrosSP.p_IdEstado 		= 'INA';
	        parametrosSP.p_Comentario 		= 'Bloqueo por interfaz';
	            	    	
	        cambioEstadoCuentaBancariaSP(parametrosSP).then(
				function(result) {
					if (result.P_OUT_ERRORESCONTADOR>0) {
						bcsComunes.ventanaMsg({ mensaje : result.P_OUT_ERRORESDETALLE[0].Codigo,
						conflictos : util.parseServerResponse(result),
					}, ppca.ventanaId);
						bcsComunes.msg("error","Error",$translate.instant('UI_ERROR_BLOQUEAR_CUENTA_PREDETERMINADA'));	
					}else{
					bcsComunes.ventanaMsg({ mensaje : "Cuenta Bancaria Bloqueada",
						conflictos : util.parseServerResponse(result)}, ppca.ventanaId);
					ppca.cuentaBancariaOptions.dataSource.read();
					}
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	    };
	    
	    ppca.desbloqueoCuenta = function () {
	    	var parametrosSP = {};
	    	parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.$root.SesionIdUsuario;
		parametrosSP.p_IdPersona 		=  ppca.IdPersona;
	        parametrosSP.p_IdModulo 		= 'PER';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdCuentaBancaria = ppca.cuentaBancariaSeleccionada.IdCuentaBancaria;
	        parametrosSP.p_IdEstado 		= 'ACT';
	        parametrosSP.p_Comentario 		= 'Desbloqueo por interfaz';
	            	    	
	        cambioEstadoCuentaBancariaSP(parametrosSP).then(
				function(result) {
					
					bcsComunes.ventanaMsg({ mensaje : "Cuenta Bancaria Desbloqueada",
						conflictos : util.parseServerResponse(result)}, ppca.ventanaId);
					ppca.cuentaBancariaOptions.dataSource.read();
					
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	    };
	    ppca.predeterminarCuenta = function (cuentabancaria) {
			var predeterminarCuentaBancariaSP = function(v_Dato)
	    	{
			       var urlServicio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=5003";
		       var r = $resource
		         ("", {}, {
				                 'urlServicio': { method: "POST", url: urlServicio}
		                  }
		         );
			       var post = r.urlServicio(v_Dato);
		       return post.$promise;
	    	};
	    	var parametrosSP = {};
	    	parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdPersona 		=  ppca.IdPersona;
	        parametrosSP.p_IdModulo 		= 'PER';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdCuentaBancaria = ppca.cuentaBancariaSeleccionada.IdCuentaBancaria;
	        parametrosSP.p_Predeterminado   = 'S';
	        parametrosSP.p_Comentario 	= 'Predeterminado por interfaz';
	        
			if(cuentabancaria.IdCliente === 0){
				parametrosSP.p_CrearPreferenciaPago = 'N';
				predeterminarCuentaBancariaSP(parametrosSP).then(
					function(result) {
						if (result.P_OUT_ERRORESCONTADOR > 0) {
							bcsComunes.ventanaMsg({ 
								mensaje: result.P_OUT_ERRORESDETALLE[0].Codigo,
								conflictos: util.parseServerResponse(result),
							}, ppca.ventanaId);
							bcsComunes.msg("error", "Error", $translate.instant('UI_ERROR_PREDETERMINAR_CUENTA_BLOQUEADA'));	
						} else {
							bcsComunes.ventanaMsg({ 
								mensaje: "Esta Cuenta Bancaria se ha predeterminado como principal",
								conflictos: util.parseServerResponse(result),
							}, ppca.ventanaId);
							ppca.cuentaBancariaOptions.dataSource.read();
						}
					},
					function(result) {
						bcsComunes.msg("error", "Error", "La operación no se pudo completar");
					}
				);
			}else{
				var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_CREAR_PREFERENCIA_AUTOMATICA_PREDETERMINAR'), ppca.ventanaId);

				t.then(function(result){
					parametrosSP.p_CrearPreferenciaPago = result === OPCIONES_BCSALERTA.SI ? 'S' : 'N';
					predeterminarCuentaBancariaSP(parametrosSP).then(
						function(result) {
							if (result.P_OUT_ERRORESCONTADOR>0) {
								bcsComunes.ventanaMsg({ mensaje : result.P_OUT_ERRORESDETALLE[0].Codigo,
								conflictos : util.parseServerResponse(result),
								}, ppca.ventanaId);
								bcsComunes.msg("error","Error",$translate.instant('UI_ERROR_PREDETERMINAR_CUENTA_BLOQUEADA'));	
							}else{		
							bcsComunes.ventanaMsg({ mensaje : "Esta Cuenta Bancaria se ha predeterminado como principal",
								conflictos : util.parseServerResponse(result)}, ppca.ventanaId);
							ppca.cuentaBancariaOptions.dataSource.read();
							}
							
						},
						function(result){
							bcsComunes.msg("error","Error","La operacion no se pudo completar");
						});
				});
			}
	    };
	});
}());
//------------------------- FIN CONTROLLER --------------------------