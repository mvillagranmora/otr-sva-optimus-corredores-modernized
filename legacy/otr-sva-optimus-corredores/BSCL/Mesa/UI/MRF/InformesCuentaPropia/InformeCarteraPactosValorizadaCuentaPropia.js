angular.module('ModuloMesa').controller('InformeCarteraPactosValorizadaCuentaPropiaCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa
			,$http,bcsAlerta,OPCIONES_BCSALERTA, bcsServicioPlataforma,$rootScope,OPCIONES_BCSALERTA)
{	
	var conOperacion = this;
	$scope.data = {};
	$scope.data.informes = {};
    $scope.data.informes.fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.Estado = '';
	$scope.dataTemplate	= {};
	$scope.dataTemplate2= {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var fechaOperacion = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.NombreArchivo = "ConsultaOperacionesMesaRF_"+$scope.SesionFechaSistema;
	$scope.data.HabilitaBotones = true;
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.AllPortafolio= true;
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	$scope.data.kngFechaProceso = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
	$scope.data.fechaValorizacionMin = {
		min: util.dateInicioDia($scope.SesionFechaSistema)
	};
	var datos = {
		p_fechas  : [
						{ fechaInicial :$scope.SesionFechaSistema, numeroDias : 1, pais : $scope.SesionIdPaisSistema }
					],
		p_diaHabil 			: 'S',
		p_soloCaeDiaHabil 	: 'S',
		p_idPais 			: $scope.SesionIdPaisSistema
	};
	bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
			if (result.P_OUT_ERRORESCONTADOR == 0)
			{
				$scope.data.kngFechaValorizacion = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
			}				
		}
	,function(result){
		console.log(result);
		}
	);	

	
	
	$scope.data.kngFechaOperacion = fechaOperacion;
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionClientePortafolio={
		tipo : "combobox-cliente-mesas",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {field: "IdEmpresa",operator: "eq", value   :  $scope.SesionIdEmpresa },
			IdTipoProducto: { field: "IdTipoProducto",	operator: "eq", value : 'MERF'}
		},
		idDefecto : false,
	};
	
	
	// --------------------------------------------------
	// TEMPLATES REPORTES
	// --------------------------------------------------
	var ConsultaTemplate = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=450",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdTemplateMesa	: {type: "string"},
						IdReporte		: {type: "string"},
						NombreReporte	: {type: "string"},
						TipoReporte		: {type: "string"},
						ServicioReporte	: {type: "string"},
						IdEmpresa		: {type: "number"},
						IdEstado		: {type: "string"}
					}
				}
			}
		},
	});

	var consultarValorizacionAnterior = new bcs.DataSource({
		type		: "odata",
		definition	: {
			serverFiltering	: true,
			transport		: {
				read			: {
					url				: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010137",
					dataType		: "json"
				}
			},
			schema 			: {
				model			: {
					fields 			: {
						IdInforme		: {type: "string"},
						Key				: {type: "string"},
						FechaHora		: {type: "string"},
						Rank			: {type: "number"}
					}
				}
			}
		}
	});
	
	$scope.data.cambiarFechaMin = function(){ 
		var datos = {
			p_fechas  : [
							{ fechaInicial : util.dateSPdate($scope.data.kngFechaOperacion), numeroDias : 1, pais : $scope.SesionIdPaisSistema }
						],
			p_diaHabil 			: 'S',
			p_soloCaeDiaHabil 	: 'S',
			p_idPais 			: $scope.SesionIdPaisSistema
		};
		bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
				if (result.P_OUT_ERRORESCONTADOR == 0)
				{
					$scope.data.kngFechaValorizacion = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
				}				
			}
		,function(result){
			console.log(result);
			}
		);
		$scope.data.fechaValorizacionMin = {
			min: util.dateInicioDia($scope.data.kngFechaOperacion)
		};

	}

	$scope.data.validarValorizacionAnterior = function() {		
		
		if($scope.data.kngFechaOperacion > $scope.data.kngFechaValorizacion ) {
			bcsComunes.msg("error", "INFORME DE CARTERA DE PACTOS VALORIZADA", $translate.instant('UI_ERROR_FECHA_VALORIZACION'));
			bcsComunes.ventanaOverlay(conOperacion.idVentana, false);
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			return;
		}
		
		let tipoValorizacion = $scope.data.esTasaMercado===true?'MERC':$scope.data.esTasaCompra===true?'COMP':'PAC';
		let fechaConsulta = util.dateSPdate($scope.data.kngFechaOperacion);
		let fechaValorizacion = util.dateSPdate($scope.data.kngFechaValorizacion);

		const partialKey = "CARTERA.PACTO" + tipoValorizacion + fechaConsulta + fechaValorizacion;		

		let valorizacionFilter =  [ { field: "Key",	operator: "eq", value: partialKey }, {field: "IdSocioDeNegocio", operator: "eq", value: $scope.SesionCorredoraIdSocioDeNegocio}];

		let llaveValorizacionAnterior;

		consultarValorizacionAnterior.query({ filter: valorizacionFilter }).then(function(){
			let valorizacionAnterior = consultarValorizacionAnterior.data();
			llaveValorizacionAnterior = valorizacionAnterior[0]?.IdInforme

			if(!llaveValorizacionAnterior) {
				//Si no encuentra llave va a la generar informe por defecto
				$scope.data.informeCarteraPropia("", true);
				return;
			}
			let fechaUltimaValorizacion = valorizacionAnterior[0]?.FechaHora;
			var modalConfirmacion = bcsAlerta.crear("PREGUNTA_SI_NO",
									`La ultima valorización fue realizada el ${fechaUltimaValorizacion} ¿Desea realizar una nueva valorización?`,
									$scope.winID);
			modalConfirmacion.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI) {
					$scope.data.informeCarteraPropia("", true);
				} else {
					$scope.data.informeCarteraPropia(llaveValorizacionAnterior, false);
				}
			})
		})

		return;		
	}

	$scope.data.valorizacionAdicionales = function(){
		bcsComunes.ventanaOverlay($scope.IdVentana,true);

		var datosValorizacion = {
       	 	p_IdEmpresa								: $scope.SesionIdEmpresa
		    ,p_IdUsuario							: $scope.SesionIdUsuario
		    ,p_IdModulo								: 'MRF'
			,p_FechaSistema							: util.dateSPdate($scope.data.kngFechaProceso)
			,p_IdPais								: $scope.SesionIdPaisSistema
			,p_IdPortafolio							: 0
			,p_ValorizaPactosSubyacenteTasaCompra	:  "S"
		};
		
		bcsServiciosMesa.ValorizacionValoresAdicionales(datosValorizacion).$promise.then(function(respuesta){
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.msg("info","Informacion","Proceso de Valorización Adicional realizado correctamente.");
				$scope.data.informeCarteraPropia();
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
			}
			else{
				bcsComunes.ventanaMsg({
					mensaje:respuesta.P_OUT_MENSAJE,
					conflictos:util.parseServerResponse(respuesta)
				},$scope.IdVentana);
				bcsComunes.ventanaOverlay($scope.IdVentana,false);

		    }
   		},function(respuesta){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error","Error","Existen problemas al realizar el proceso de Valorización de Valores Adicionales.");
			bcsComunes.ventanaOverlay($scope.IdVentana,false);

   		});
	};

	$scope.data.obtenerCarteraValorizada = function(data) {
		return new Promise((resolve, reject) => {
			bcsServiciosMesa.ConsultarCarteraPactosValorizada(data).$promise
				.then(result => {
					console.log(result);
					resolve(result);
				})
				.catch(error => {
					bcsComunes.setConflictos(util.parseServerResponse(error));
					bcsComunes.msg("error", "Error", "Existen problemas al realizar la búsqueda de cartera valorizada");
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					reject(error);
				});
		});
	}

	$scope.data.informeCarteraPropia = async function(idInforme = "" ,ejecutaValorizacion = true){
		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);

		if(!ejecutaValorizacion && !idInforme) {
			bcsComunes.msg("error", "INFORME DE CARTERA DE PACTOS VALORIZADA", $translate.instant('UI_LLAVE_INVALIDA'));
			bcsComunes.ventanaOverlay(conOperacion.idVentana, false);
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			return;
		}
		
		var ServiceDataSubReporte= {
	       	 	"p_IdEmpresa"			: $scope.SesionIdEmpresa
			    ,"p_IdUsuario"			: $scope.SesionIdUsuario
			    ,"p_IdModulo"			: 'MRF'
				,"p_Fecha"				: $scope.data.kngFechaOperacion
				,"p_FechaConsulta"		: $scope.data.kngFechaOperacion
				,"p_FechaValorizacion"	: $scope.data.kngFechaValorizacion
				,"p_TipoValorizacion"	: $scope.data.esTasaMercado===true?'M':$scope.data.esTasaCompra===true?'C':'P'
				,"p_IdCliente"			: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente || 0
				,"p_IdPortafolio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio || 0
				,"p_IdSocioNegocio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0
				,"p_TipoReporte"		: 'CAR_PAC'
				,"p_EjecutaValorizacion": ejecutaValorizacion
				,"$schema"				: [{ "field" : "LogoCorredora", "type" : "blob" }]
				,"p_IdPais"				: $scope.SesionIdPaisSistema
	       };
		var ServiceDataReporte= {
	       	 	"p_IdEmpresa"			: $scope.SesionIdEmpresa
			    ,"p_IdUsuario"			: $scope.SesionIdUsuario
			    ,"p_IdModulo"			: 'MRF'
				,"p_Fecha"				: $scope.data.kngFechaOperacion
				,"p_FechaConsulta"		: util.dateSPdate($scope.data.kngFechaOperacion)
				,"p_FechaValorizacion"	: util.dateSPdate($scope.data.kngFechaValorizacion)
				,"p_TipoValorizacion"	: $scope.data.esTasaMercado===true?'MERC':$scope.data.esTasaCompra===true?'COMP':'PAC'
				,"p_IdCliente"			: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente || 0
				,"p_IdPortafolio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio || 0
				,"p_IdSocioNegocio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0
				,"p_TipoReporte"		: 'CAR_PAC'
				,"p_EjecutaValorizacion": ejecutaValorizacion
				,"p_IdPais"				: $scope.SesionIdPaisSistema
		};
		
		if (ejecutaValorizacion) {
			let valorizacionResponse;
			try {
				valorizacionResponse = await $scope.data.obtenerCarteraValorizada(ServiceDataReporte);
			} catch (error) {
				return;
			} finally {
				console.log(valorizacionResponse)
			}
			//Validacion errores
			if(valorizacionResponse.P_OUT_ERRORESCONTADOR > 0) {
				bcsComunes.ventanaMsg({
					mensaje:valorizacionResponse.P_OUT_MENSAJE,
					conflictos:util.parseServerResponse(valorizacionResponse)
				},$scope.IdVentana);
				bcsComunes.ventanaOverlay(conOperacion.idVentana, false);
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				return;
			}
	
			ServiceDataReporte.p_IdInforme = valorizacionResponse.P_OUT_LLAVE_REPORTE;
			ServiceDataSubReporte.p_IdInforme = valorizacionResponse.P_OUT_LLAVE_REPORTE;
		} else {
			ServiceDataReporte.p_IdInforme = idInforme;
			ServiceDataSubReporte.p_IdInforme = idInforme;
		}
		
		//Cambio TipoValorizacion para procedimiento
		ServiceDataReporte.p_TipoValorizacion = ServiceDataReporte.p_TipoValorizacion === 'MERC' ? 'M': ServiceDataReporte.p_TipoValorizacion === 'COMP' ? 'C' : 'P';
	
		var subReports = [];
		
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'CARTERA_PACTOS_VALORIZADA_CUENTA_PROPIA' },
								{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
								{ field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		

		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
			for(i=0;i<data.length;i++) {
				var dataTem = data[i];
				if (data[i].IdTemplateMesa === 'CUERPO_INFORME_PAC_VAL_CTA_PRO'){					
					template = {
								TipoReporte		: data[i].TipoReporte//'pdf' 
								,IdReporte		: data[i].IdReporte//'EBE13D58A5C74C1FE1000000A1EE22E7'
								,NombreReporte	: $translate.instant(data[i].NombreReporte)//("INFORME_CARTERA_PROPIA_MRF")
								,data			: { "serviceId" 	: data[i].ServicioReporte,//'1761',
													"serviceData" 	:ServiceDataReporte
												}
								,idReporte		: 'CAR_PRO'
					};
				}
				else {
					//
					subReports.push({ "reportName" 	: data[i].IdTemplateMesaSubreporte//'InformeCarteraPropiaGarantiaMesaRF_Detalle'
									,"parameters" 	: {} 
									,"data" 		: {
											"serviceId" : data[i].ServicioReporte,//'1759',
											"serviceData" :ServiceDataSubReporte
									}
					});
				}			
			}
			
		template.subReports = subReports;
		$scope.getReport(template);
		bcsComunes.ventanaOverlay(conOperacion.idVentana, false);
		bcsComunes.ventanaOverlay($scope.IdVentana,false);
			
		});
	};
	
	//WATCH
	$scope.$watch('data.esTasaMercado',function(newval,oldval) {
		
		if(newval){
			if($scope.data.esTasaCompra===true || $scope.data.esTasaPacto===true){
				$scope.data.esTasaCompra=false;
				$scope.data.esTasaPacto=false;
			}
			
		}
   	});
	
	$scope.$watch('data.esTasaCompra',function(newval,oldval) {
		
		if(newval){
			if($scope.data.esTasaMercado===true || $scope.data.esTasaPacto===true){
				$scope.data.esTasaMercado=false;
				$scope.data.esTasaPacto=false;
			}
			
		}
   	});
		
	$scope.$watch('data.esTasaPacto',function(newval,oldval) {
		
		if(newval){
			if($scope.data.esTasaMercado===true || $scope.data.esTasaCompra===true){
				$scope.data.esTasaMercado=false;
				$scope.data.esTasaCompra=false;
			}
			
		}
   	});
	
	//FIN WATCH
	
	$scope.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": {}
				},
				"subReports": dataTemplate.subReports || []
			}
		};
		
		let horaReporte = util.date(BCS_TIMESTAMP_LOCAL).add(util.duration({"seconds": $scope.segundos})).format("HH:mm");
				
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				//var fileURL = URL.createObjectURL(file);
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'')+horaReporte;
				nombreArchivo = !dataTemplate.idReporte ? nombreArchivo+'_'+$scope.data.seleccionOperacionesMesaRF[0].IdOperacion:nombreArchivo;
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				excelReport();
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				//bcsComunes.ventanaMsg({
				//                     conflictos:util.parseServerResponse(response)
				//                   },$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
			});

			var excelReport = function(){
				bcsServiciosMesa.GenerarArchivoInformeCarteraPacto(dataTemplate.data.serviceData).$promise.then(function(result) {
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						bcsComunes.msg("warning",$translate.instant("UI_GENERAR_REPORTES"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
						bcsComunes.ventanaOverlay($scope.winID,false);
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						bcsComunes.msg("error",$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),result.P_OUT_ERRORESDETALLE[0].Codigo);
						bcsComunes.ventanaOverlay($scope.winID,false);
					}
					else{
						var ruta = result.P_OUT_RESULTADOPROCESO.find((e)=> e.NombreParametro === "RutaInformeCuentaPropia").Valor
						var nombre = result.P_OUT_RESULTADOPROCESO.find((e)=> e.NombreParametro === "NombreArchivo").Valor
						$timeout(function(){
							$scope.data.downloadExcelReport(ruta, nombre)
						}, 2000);
					}
				},
				function(result){
					bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_AL_GENERAR_REPORTE'));
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
			}
	};

	$scope.data.downloadExcelReport = function(ruta, nombre, maximoIntentos = 10, intervaloMs = 4000){
		return new Promise((resolve, reject) => {
			let intentosRealizados = 0;
			function newTry() {
				var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
				var data = { file: ruta + nombre };
				$http({
					method: 'POST',
					url: url,
					params: data,
					responseType: "arraybuffer"
				}).then(function(response) {
					var file = new Blob([response.data], { type: 'application/vnd.ms-excel' });
					saveAs(file, nombre);
					bcsComunes.ventanaOverlay($scope.winIDa, false);
					resolve();
				}).catch(function(error) {
					intentosRealizados++;
					if (intentosRealizados >= maximoIntentos) {
						bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_AL_GENERAR_REPORTE'));
						bcsComunes.ventanaOverlay($scope.winID, false);
						reject(error);
					} else {
						setTimeout(newTry, intervaloMs);
					}
				});
			}
			newTry();
		})
	}

	
	$scope.data.oConfiguracionClientePortafolio.idActual	= $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0;
	$scope.data.oConfiguracionClientePortafolio.idDefecto	= $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0;
});