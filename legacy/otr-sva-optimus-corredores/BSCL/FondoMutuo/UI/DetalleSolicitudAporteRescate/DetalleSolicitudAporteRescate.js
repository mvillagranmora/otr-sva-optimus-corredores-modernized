angular.module('ModuloFFM').controller('DetalleAporteRescateCtrl',function ($scope,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsServicioReportes) {
		$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		$scope.solicitudAporteRescate = {};
		$scope.formData = {};
		$scope.filtros = {};
		var conf = bcsConfiguracionFFM.getConsultaSolicitudAporteRescateFondosMutuosDetalle();
		var apiGrid = conf.options.dataSource;
		$scope.defaultOptions = conf.options;
		$scope.buscarSolicitudes = conf.buscar;
		$scope.winID=bcsComunes.ventanaWinId($scope);
		$scope.masInformacion = true;
		$scope.dataTemplate = {};
		if (!$scope.parametros.IdSolicitudAporteRescate)
			console.log("Solicitud de aporte rescate no existe!");
		else {
			
			bcsComunes.ventanaOverlay($scope.winID,true);
			
			var datosConsultaSolicitud = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'FMM',
					p_SolicitudesAporteRescate : $scope.parametros.IdSolicitudAporteRescate
			}
			
			bcsServicioFondosMutuos.ConsultarDetalleSolicitud(datosConsultaSolicitud).$promise.then(function(respuesta){
		     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		$scope.solicitudAporteRescate = respuesta.P_OUT_RESULTADO[0];
					$scope.solicitudAporteRescate.FormatoNumeroCuotas = util.formateaNumero($scope.solicitudAporteRescate.NumeroCuotas, $scope.solicitudAporteRescate.DecimalesCuota);
					$scope.solicitudAporteRescate.FormatoValorCuota = util.formateaNumero($scope.solicitudAporteRescate.ValorCuota, $scope.solicitudAporteRescate.DecimalesValorCuota);
					$scope.solicitudAporteRescate.FormatoNumeroCuotasInversion = util.formateaNumero($scope.solicitudAporteRescate.NumeroCuotasInversion, $scope.solicitudAporteRescate.DecimalesCuota);
					$scope.solicitudAporteRescate.FormatoValorCuotaInversion = util.formateaNumero($scope.solicitudAporteRescate.ValorCuotaInversion, $scope.solicitudAporteRescate.DecimalesValorCuota);
					$scope.solicitudAporteRescate.FechaAceptacionRegistro = util.dateDS($scope.solicitudAporteRescate.FechaAceptacionRegistro,'FechaHoraSistema','CALL');
					$scope.solicitudAporteRescate.FechaCertificacionRegistro = util.dateDS($scope.solicitudAporteRescate.FechaCertificacionRegistro,'FechaHoraSistema','CALL');
					$scope.solicitudAporteRescate.FechaLiquidacionRegistro = util.dateDS($scope.solicitudAporteRescate.FechaLiquidacionRegistro,'FechaHoraSistema','CALL');
					$scope.solicitudAporteRescate.FechaAnulacionRegistro = util.dateDS($scope.solicitudAporteRescate.FechaAnulacionRegistro,'FechaHoraSistema','CALL');
					$scope.solicitudAporteRescate.FechaIngresoRegistro = util.dateDS($scope.solicitudAporteRescate.FechaIngresoRegistro,'FechaHoraSistema','CALL');
					if($scope.solicitudAporteRescate.EstadoSolicitud === 'CRT' || $scope.parametros.Simular === 'S'){
						$scope.masInformacion = false;
					}
					bcsComunes.ventanaTamanoAutomatico($scope.winID);
					bcsComunes.ventanaOverlay($scope.winID,false);
					
					return true;
		     	 }else{
		     		console.log("Solicitud de aporte rescate no existe!");
					return false;
		     	 }
	  		 },function(respuesta){
	  			console.log("Error!");
				return false;
	  		});
		}
		
		$scope.$watch('solicitudAporteRescate.Glosa',function(newval,oldval){
			if(newval && newval.IdSolicitudAporteRescate){
				bcsComunes.ventanaTamanoAutomatico($scope.winID);
			}
		});
		
		$scope.$watch('masInformacion',function(newval,oldval){
			if(newval != oldval) bcsComunes.ventanaTamanoAutomatico($scope.winID);
		});
		
		$scope.ajustaVentana = function()
		{
			bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
		};
		
		$scope.defaultOptions.gridCreated = function(grid)
		{
			$scope.filtros.IdSolicitudRescate = $scope.parametros.IdSolicitudAporteRescate;
			$scope.buscarSolicitudes(grid,$scope.filtros);
			$scope.ajustaVentana();
		};
		
		$scope.verMandato = function() {
			var solicitud = $scope.solicitudAporteRescate;
			var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'CompMLT' },
					{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					{ field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			
			bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
			if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
				$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
				$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
				$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
			
				var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
									"p_IdUsuario"				: $scope.SesionIdUsuario,
									"p_IdModulo"		: 'FMM',
									"p_IdSolicitudAporteRescate" : solicitud.IdSolicitudAporteRescate,
									
			                        "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
									  };
			
				$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
										   , "serviceData" :	parametros
										  };
				
				$scope.dataTemplate.parameters= [];
				var subrep = new Array();
				$scope.dataTemplate.subReports=subrep;
				
				bcsConfiguracionFFM.getReport($scope.dataTemplate);
			}
			});
		}
		
		$scope.verComprobante = function() {
			var solicitud = $scope.solicitudAporteRescate;
			var tipoComprobante = '';
			var tipoMovimiento = solicitud.IdTipoMovimientoFondoMutuo;
			console.log("Solicitud", solicitud);
			if(tipoMovimiento == 'RESTRANSF' || tipoMovimiento == 'RESTRASP' || tipoMovimiento == 'APODIV' || tipoMovimiento == 'RESCAM' || tipoMovimiento == 'RESCOM') return;
			
			if(tipoMovimiento == 'RESNORM'||tipoMovimiento == 'APONORM'||tipoMovimiento == 'APOTRASP'||tipoMovimiento == 'APORESPRO'||tipoMovimiento == 'APOOVER'||tipoMovimiento == 'APOREINV'||tipoMovimiento == 'RESREINV'){
				if(tipoMovimiento == 'RESNORM') tipoComprobante = 'CompResc';
				if(tipoMovimiento == 'APONORM'  || tipoMovimiento == 'APOTRASP') tipoComprobante = 'CompApor';
				if(tipoMovimiento == 'APORESPRO') tipoComprobante = 'CompApor';
				if(tipoMovimiento == 'APOOVER') tipoComprobante = 'CompOver';
				if(tipoMovimiento == 'APOREINV') tipoComprobante = 'ComApRei';
				if(tipoMovimiento == 'RESREINV') tipoComprobante = 'ComReRei';
				
				if(typeof solicitud.NumeroCuotasMovimientoAsociado == 'undefined') solicitud.NumeroCuotasMovimientoAsociado = '';
				if(typeof solicitud.MontoMovimientoAsociado == 'undefined') solicitud.MontoMovimientoAsociado = '';
				
				var resultadoSolicitud = {
					    SecuenciaOperacion              : solicitud.IdSolicitudAporteRescate,
						IdOperacion						: solicitud.IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: encodeURIComponent(solicitud.IdSerieFondo),
						TipoFondo 						: solicitud.IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(solicitud.FechaAceptacion),
						MontoInicial	 				: solicitud.CostoMonedaLocal,
						NumeroCuotas			 		: solicitud.NumeroCuotas,
						CostoUF                         : solicitud.CostoUF,
						IdEmisor                        : solicitud.IdEmisorOrigen,
						MontoAporteOrigen               : solicitud.CostoMonedaLocal,
						esMLT                           : solicitud.IndicadorMLT === 'S' ? 'SI' : 'NO',
						FechaOrigenInversion            : util.formatoDate(solicitud.FechaOriginalInversion),
						DocumentoIdentidad              : solicitud.DocumentoIdentidad,
						CodigoPortafolio                : solicitud.CodigoPortafolio,
						NombreCliente                   : solicitud.NombreCompleto,
						FechaLiquidacion                : util.formatoDate(solicitud.FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(solicitud.FechaCertificacion),
						MontoTransferencia              : solicitud.MontoMovimientoAsociado,
						NumeroCuotasTransferencia        : solicitud.NumeroCuotasMovimientoAsociado,
						IdEmisorOrigen                  : solicitud.IdEmisorOrigen,
						IdSerieFondoDestino             : solicitud.IdSerieFondoDestino,
						IdEmisorDestino                 : solicitud.IdEmisorDestino,
						IndicadorMLT                    : solicitud.IndicadorMLT,
		 		};
				var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: tipoComprobante },
				                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
				bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
					if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
						$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
						$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
						$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;

						var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
											"p_IdUsuario"				: $scope.SesionIdUsuario,
											"p_IdModulo"		: 'FMM',
											"p_IdSolicitudAporteRescate" : tipoComprobante == 'ComApRei' ? solicitud.IdSolicitudAporteRescateRelacionado : solicitud.IdSolicitudAporteRescate,
											
				                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
											  };
			
						$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
												   , "serviceData" :	parametros
												  };
						
						$scope.dataTemplate.parameters= [{
															"type" : "pdf417",
															"name" : "pdf417",
															"data" :  bcsConfiguracionFFM.generarPdf417(resultadoSolicitud)
														}];
						var subrep = new Array();
						$scope.dataTemplate.subReports=subrep;
						bcsConfiguracionFFM.getReport($scope.dataTemplate);
					}
				});
				 
			}else{
				
				// Reporte Ingreso/Retiro de Titulo Custodi y Garantia
				
				var ConfiguracionTemplate = new bcs.DataSource({
					type: "odata",
					definition:{
						serverFiltering:true,
						sort: { field: "ReportVersion", dir: "desc" },
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015&c=1",
								dataType: "json",
								data:{
									$select: "\"CompanyId\",\"Id\",\"ReportName\",\"ReportVersion\""
								}
							}
						},
						schema: {
							model:
							{
								fields : {
									Id: {type: "string"},
									ReportName: {type: "string"},
									ReportVersion: {type: "string"}
								}
							}
						}
					}
				});
				
				var filtro = new Array();	
				
				
					filtro.push({ field: "CompanyId", operator: "eq", value: $scope.$root.SesionIdEmpresa, });
					filtro.push({ field: "ReportName", operator: "eq", value: "COMPROBANTEINGRESORETIROCUSTODIA" });				
			
				
				ConfiguracionTemplate.query({filter:filtro}).then(function() {
					
					if(solicitud){
						
						var pdf417 = solicitud.IdMovimientoCarteraEnTransito+" "+solicitud.IdCliente+" "+solicitud.DocumentoIdentidad+" "+solicitud.FechaLiquidacion;
						var data = {
								"P_IDEMPRESA" : $scope.$root.SesionIdEmpresa,
								"P_IDMODULO" : "CUS",
								"P_IDCLIENTE" : solicitud.IdCliente,
								"P_IDPORTAFOLIO" : solicitud.IdPortafolio,
								"P_IDMOVIMIENTO" : solicitud.IdMovimientoCarteraEnTransito,
								"$schema":[{"field":"LogoCorredora", "type":"blob"}]
							};	
						bcsServicioReportes.descargarReportePdf417(ConfiguracionTemplate.data()[0].Id, 13018, 'COMPROBANTEINGRESORETIROCUSTODIA', pdf417, "PDF", data, $scope.winID, false);
						
					}else{
						bcsComunes.msg("error","Error", $translate.instant("UI_ERROR_OBTENER_REPORTE"));
					}
					
				});
				
				
				
				
			}
			
			
			
		
		};
});
