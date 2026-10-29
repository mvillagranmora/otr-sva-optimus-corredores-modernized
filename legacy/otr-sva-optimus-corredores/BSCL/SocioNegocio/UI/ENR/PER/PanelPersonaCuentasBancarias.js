/*************************************************************
 * Purpose	: Panel Persona Cuentas Bancarias.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaCuentasBancariasEnrolCtrl',
	function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$resource,servicioSociodeNegocio){
		var ppca = this;
		ppca.showBloqueo = false;
		ppca.ventanaId = bcsComunes.ventanaWinId($scope);

		ppca.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['CUENTA_BANCARIA'];
		
		ppca.abrirModal = function(xIdEnrolamiento,xCuentaBancaria, xCodigoTipoPersona, xCuentasBancarias, xEsPac){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarCuentaBancariaEnrolamiento",{ 
				IdEnrolamiento: xIdEnrolamiento, 
				CuentaBancaria: xCuentaBancaria,
				CodigoTipoPersona: xCodigoTipoPersona,
				CuentasBancarias: xCuentasBancarias, 
				EsPac: xEsPac, 
				CodigoFormulario: ppca.CodigoFormulario },false,false,ppca.ventanaId); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					ppca.cuentaBancariaSeleccionada = undefined;
					ppca.cuentaBancariaOptions.dataSource.read();
				}
	    	}); 
		};
		
		ppca.agregar = function() {
			ppca.abrirModal(ppca.IdEnrolamiento,undefined, $scope.admSoc.dataSocioNegocio.CodigoTipoPersona, ppca.cuentaBancariaOptions.dataSource.data());
		};

		ppca.eliminar = function(cta) {
			const datos = {
				p_CuentasBancarias:[{
					IdInterno : 1,
					IdEnrolamiento : cta.IdEnrolamiento,
					IdCuentaBancaria: cta.IdCuentaBancaria
				}],
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo:"SOC",
				p_UsaReglasNegocio:"S",
				p_CodigoFormulario: ppca.CodigoFormulario,
				p_IdTipoCanal: 'OPT'
			 }
			
			bcsComunes.ventanaOverlay(ppca.idVentana,true);
			
			servicioSociodeNegocio.eliminarBorradorCuentaBancaria(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(ppca.idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.msg("error","error","Error al borrar cuenta bancaria");
						return;
					}
					
					bcsComunes.msg("info","info","Cuenta bancaria borrada");
					
					bcsComunes.ventanaMsg({ mensaje : "Cuenta Bancaria borrada",conflictos : util.parseServerResponse(result)}, ppca.idVentana);
					ppca.cuentaBancariaOptions.dataSource.read();
				},
				function(result){
					bcsComunes.msg("error","error","Error al borrar cuenta bancaria");
				}
			);		    
		};
		

		ppca.modificar = function(cta) {
			ppca.abrirModal(ppca.IdEnrolamiento,cta);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppca.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
			ppca.cuentaBancariaOptions = {
				dataSource: new bcs.DataSource({
					type : "odata",
					definition : {
						serverFiltering : true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700212",
								dataType: "json"
							}
						},
						filter: { field: "IdEnrolamiento", operator: "eq", value: Number(ppca.IdEnrolamiento) },
						schema: {
							model: {
								fields: {
									IdCuentaBancaria 			: { type: "number"},
									IdBanco 					: { type: "number"},
									NombreBanco 				: { type: "string"},
									IdPaisOperacion 			: { type: "number"},
									NombrePais 					: { type: "string"},
									IdTipoCuentaBancaria		: { type: "string"},
									NombreTipoCuentaBancaria	: { type: "string"},
									IdMoneda					: { type: "string"},
									NumeroCuentaBancaria		: { type: "string"},
									IdEnrolamiento				: { type: "number"},
									IdTipoIdentificadorCuenta	: { type: "string"},
									IdentificadorCuenta			: { type: "string"},
									CodigoCuentaInterbancario	: { type: "string"},
									CodigoAba					: { type: "string"},
									OtroCodigoBancoExtranjero	: { type: "string"},
									CodigoIban					: { type: "string"},
									OtroCodigoCuentaExtranjero	: { type: "string"},
									IdPlaza						: { type: "number"},
									NombrePlaza					: { type: "string"},
									EsPac					    : { type: "string"},
									Predeterminado			    : { type: "string"},
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
					field: "Predeterminado",
					title: $translate.instant("UI_PREDETERMINADO"),
					template: "<span translate> #: Predeterminado # </span>",
					attributes: {style:"text-align:left;"},
					width: "80px"
				}]		
			};
		});	
	    ppca.predeterminarCuenta = function (cuentabancaria) {
			var predeterminarCuentaBancariaSP = function(v_Dato)
	    	{
			       var urlServicio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=5004";
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
	        parametrosSP.p_IdModulo 		= 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdCuentaBancaria = ppca.cuentaBancariaSeleccionada.IdCuentaBancaria;
	        parametrosSP.p_Predeterminado   = 'S';
	        parametrosSP.p_Comentario 	= 'Predeterminado por interfaz';
			var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_CREAR_PREFERENCIA_AUTOMATICA_PREDETERMINAR'), ppca.ventanaId);
			t.then(function(result){
				parametrosSP.p_CrearPreferenciaPago = result === OPCIONES_BCSALERTA.SI ? 'S' : 'N';
				predeterminarCuentaBancariaSP(parametrosSP).then(
					function(result) {		
						bcsComunes.ventanaMsg({ mensaje : "Esta Cuenta Bancaria se ha predeterminado como principal",
							conflictos : util.parseServerResponse(result)}, ppca.ventanaId);
						ppca.cuentaBancariaOptions.dataSource.read();
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion no se pudo completar");
					});
			});
	    };
	});
}());