angular.module('ModuloTesoreria').controller('ArqueoCajaCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosTesoreria,bcsBlotterComprobantesGrid,bcsAlerta,OPCIONES_BCSALERTA)
{
	var bInfoCaja = this;
	$scope.winID = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	bInfoCaja.Caja			= 	bcsComunes.ventanaParams($scope.winID).Caja.CodigoCaja;
	bInfoCaja.IdCaja 		= 	bcsComunes.ventanaParams($scope.winID).Caja.IdCaja;
	bInfoCaja.IdEstado 		= 	bcsComunes.ventanaParams($scope.winID).Caja.IdEstadoCaja;
	bInfoCaja.NombreEstado 	= 	bcsComunes.ventanaParams($scope.winID).Caja.NombreEstado;

	var dsArqueoCaja = new bcs.DataSource({
        type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: [{ field: "NombreMedioDePago", dir: "asc" },
								   { field: "IdMoneda", dir: "asc" }
								  ],
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1734",dataType : "json"}},
			filter				: [{ field: "IdCaja", operator: "eq", value: bInfoCaja.IdCaja},
								   { field: "IdModalidadDePago", operator: "eq", value: 'MODCAJ'},
								  ],
			schema : {
				model : {
					fields : {
						NombreMedioDePago	: {type: "string"},
						IdMoneda			: {type: "string"},
						Cantidad			: {type: "number"},
						Monto				: {type: "number"},
					}
				}
			}
		}
	});	
	$scope.confArqueoCaja = {
		dataSource  : dsArqueoCaja,
		columns 	: [   	
						 {field : "NombreMedioDePago",	title:"UI_MEDIO_DE_PAGO",	width: "100px"}
						,{field : "IdMoneda",			title:"UI_MONEDA",			width: "50px"}
						,{field : "Cantidad",			title:"UI_CANTIDAD",		width: "50px"}
						,{field : "Monto",				title:"UI_MONTO",			template : "#if ( IdMoneda === null) {#0#} else {#<label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>#}#" } //
                      ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		height 		: 360,
		selectable 	: "row",
		autoBind 	: true,
		userSettings: true
	};
	

	var dsDetalleArqueoCaja = new bcs.DataSource(bcsBlotterComprobantesGrid.dsDefDocumentos);
	$scope.confDetalleArqueoCaja = {
		dataSource  : dsDetalleArqueoCaja,
		columns 	: bcsBlotterComprobantesGrid.columnasDocumentosArqueo(),
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		height 		: 360,
		selectable 	: "multiple row",		
		userSettings: true
	};


	$scope.$watch('seleccionArqueoCaja', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) { //&& newValue.Cantidad >0
			dsDetalleArqueoCaja.removeAll();
			$scope.seleccionDetalleArqueoCaja = null;
			var filtros = [];

			filtros.push({ field: "IdCaja",			operator: "eq", value: $scope.seleccionArqueoCaja.IdCaja });
			filtros.push({ field: "IdMedioDePago",	operator: "eq", value: $scope.seleccionArqueoCaja.IdMedioDePago });
            filtros.push({ field: "IdMoneda",   	operator: "eq", value: $scope.seleccionArqueoCaja.IdMoneda });
            
            if ($scope.seleccionArqueoCaja.IdMedioDePago !== 'EFE'){
            	filtroOr = 	{
							logic: "or",
							filters: [
							  { field: "IdEstado", operator: "eq", value: "ING" },
							  { field: "IdEstado", operator: "eq", value: "CRT" },
							]
						};	
            } else {
            	filtros.push({ field: "Depositado",   operator: "eq", value: 'N' });
            	filtroOr = 	{
								logic: "or",
								filters: [
								  { field: "IdEstado", operator: "eq", value: "ING" },
								  { field: "IdEstado", operator: "eq", value: "LIQ" },
								]
							};            
            }

			
			filtros.push(filtroOr);
			dsDetalleArqueoCaja.filters(filtros);
		}
	},true);

	//OPCIONES
	$scope.mantenedorCaja = function(Estado){
		var dato = [];		
		dato.push(	{NumFila: 1, Campo: 'IdCaja', Valor: bInfoCaja.IdCaja, ValorAnterior: bInfoCaja.IdEstado},
					{NumFila: 1, Campo: 'IdEstado', Valor: Estado, ValorAnterior: bInfoCaja.IdEstado})


		var parametros = {};
		parametros.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario 			= $scope.SesionIdUsuario;
		parametros.p_IdModulo 			= 'TES';
		parametros.p_SimularOperacion 	= 'N';
		parametros.p_UsaReglasNegocio 	= 'S';
		parametros.p_Mantenedor 		= 'CAJA';
		parametros.p_FechaModificacion 	= $scope.SesionFechaSistema,
		parametros.p_Dato 				= dato
		
		bcsComunes.ventanaOverlay($scope.winID, true);
	 	bcsServiciosTesoreria.ModificarMantenedorCaja(
			parametros,
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);

				if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0){	
					
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);					

					bcsComunes.ventanaData($scope.winID);
					bcsComunes.ventanaCerrar($scope.winID);

				} else {	
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
			},
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);

				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_MODIFICAR_ESTADO_CAJA"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);				
			}

		);
	};
	$scope.envioADeposito = function(){
		var documentosADepositar = [];
		$.each($scope.seleccionDetalleArqueoCaja, function(key,value){
			documentosADepositar.push({"IdComprobanteFormaDePago": $scope.seleccionDetalleArqueoCaja[key].IdComprobanteFormaDePago});
		});
		
		var ventanaDocumentosDepositar = bcsComunes.ventanaAbrir("IngresoDeposito", documentosADepositar, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaDocumentosDepositar);
		promesa.then(function(data){
			dsArqueoCaja.read();
			dsDetalleArqueoCaja.read();
		});
	};
	

	
	
	//--------------------------------------------------------
	//-- ENVIO A BODEDA Y ENVIO DE DOCUMENTOS A TESORERIA
	//--------------------------------------------------------
	$scope.envioABoveda = function()
	{	
		if ($scope.seleccionArqueoCaja !== undefined)
		{	
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ENVIARAN_A_BOVEDA_LOS_DOCUMENTOS') );
			t.result.then(function(result)
			{
				if (result===OPCIONES_BCSALERTA.ACEPTAR)
				{	
					var arrDataFormaPago = new Array();			
					arrDataFormaPago = dsDetalleArqueoCaja.dataColumns(['IdComprobante','IdComprobanteFormaDePago','IdTipoMovimiento','IdMedioDePago','IdModalidadDePago',
													   'IdMoneda','Monto','IdCuentaBancariaCorredora','IdTarjetaBancariaCorredora','IdChequera',
													   'NumeroDocumentoCorredora','FechaDocumentoPagoCorredora','IdCuentaBancariaSocio','IdBancoSocio',
													   'NombreCuentaBancariaSocio','NumeroCuentaBancariaSocio','IdTipoCuentaBancariaSocio',
													   'NombreTipoCuentaBancariaSocio','IdTarjetaBancariaSocio','IdTipoTarjetaBancariaSocio',
													   'NombreTipoTarjetaBancariaSocio','NumeroTarjetaBancariaSocio','NumeroSerieSocio','NumeroDocumentoSocio',
													   'FechaDocumentoPagoSocio','CertificadoAutomatico']);
					if (arrDataFormaPago !== undefined)
					{
						for(i=0;i<arrDataFormaPago.length;i++){
							arrDataFormaPago[i].CertificadoAutomatico = 'N';
						};
					}

					var parametros = {};
					parametros.p_IdEmpresa 			= $scope.SesionIdEmpresa;
					parametros.p_IdUsuario 			= $scope.SesionIdUsuario;
					parametros.p_IdModulo 			= 'TES';
					parametros.p_SimularOperacion 	= 'N';
					parametros.p_UsaReglasNegocio 	= 'S';
					parametros.p_FechaMovimiento 	= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));					                                  
					parametros.p_TipoEjecucion 		= 'EBV';
					parametros.p_FormasDePago		= arrDataFormaPago;
					
					bcsComunes.ventanaOverlay($scope.winID, true);
				 	bcsServiciosTesoreria.GestionFormaDePago(
						parametros,
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.winID, false);

							if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0){	
								
								//Si medio de pago es Cheque o Vale Vista se envían los documentos al tesorero para su revisión
								//Los documentos enviados se revisan en la pestaña "Envio Documentos" del blotter "Comprobantes Tesorería"					
								if ($scope.seleccionArqueoCaja.IdMedioDePago === 'CHE' || $scope.seleccionArqueoCaja.IdMedioDePago === 'VVI')
								{
									$scope.enviarDocumentos();
								}
								else
								{
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.winID);							
								}
								
								dsArqueoCaja.read();
								dsDetalleArqueoCaja.read();						

							} else {	
								bcsComunes.ventanaMsg({						
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
							}
						},
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.winID, false);

							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_ERROR_MODIFICAR_ESTADO_DOCUMENTO"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);				
						}
					);				 	
				}
			});
		}
		else
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_DEBE_SELECCIONAR_MEDIO_PAGO"),
			},$scope.winID);
		}
	};
	
	

	

	//Enviar documentos a tesoreria
	$scope.enviarDocumentos  = function() 
	{	
		
			bcsComunes.ventanaOverlay($scope.winID, true);
			
			var arrEnvioDocumento = [];
			arrEnvioDocumento.push({IdEnvioDocumento : 1,  //Para ING(Crear ED) este número es sólo referencial
									IdCaja  		 : $scope.seleccionArqueoCaja.IdCaja,
									IdMedioDePago    : $scope.seleccionArqueoCaja.IdMedioDePago,
									IdMoneda         : $scope.seleccionArqueoCaja.IdMoneda,									 
									IdEstado         : 'ING'             //ING: ingresado
									});		

			var arrEnvioDocumentoDetalle = new Array();
			arrEnvioDocumentoDetalle = dsDetalleArqueoCaja.dataColumns(['IdComprobanteFormaDePago']);
			
			if (arrEnvioDocumentoDetalle !== undefined)
			{
				for(i=0;i<arrEnvioDocumentoDetalle.length;i++)
				{
					arrEnvioDocumentoDetalle[i].IdEnvioDocumento = 1;         //Para ING(crear ED) este número es sólo referencial
					arrEnvioDocumentoDetalle[i].IdEnvioDocumentoDetalle = 1;  //Para ING(crear ED) este número es sólo referencial
					arrEnvioDocumentoDetalle[i].IdEstado = 'ING'              //ING: ingresado
				};
			}

									
			var datos = {};
			datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
			datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
			datos.p_IdModulo                	= 'TES';
			datos.p_simularOperacion			= 'N';
			datos.p_UsaReglasNegocio			= 'S';				
			datos.p_TipoEjecucion			    = 'ING';   //ING: crear nuevo envio de documentos
			datos.p_EnvioDocumento 		        = arrEnvioDocumento;
			datos.p_EnvioDocumentoDetalle 		= arrEnvioDocumentoDetalle;			
			bcsComunes.log().info(JSON.stringify(datos));
			
			bcsServiciosTesoreria.GestionEnvioDocumentos(
				datos,
				function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					 		
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
												
					} 
					else 
					{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
					}
				},
				function(respuesta){						
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			);			
		};
		

	
});