angular.module('ModuloCustodia').controller('InformeMayorYMenorValorCuentaPropiaCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosCustodia,$http,bcsAlerta,OPCIONES_BCSALERTA, bcsServicioPlataforma)
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
	$scope.data.HabilitaBotones = true;
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.AllPortafolio= true;
	$scope.data.esCuentaPropia= true;
	$scope.data.IdPortafolioTemporal = 0;
	$scope.data.esTasaMercado= true;
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);

	$scope.filtros = {};
	$scope.filtros.kngFechaConsulta 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaValorizacion = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.fechaValorizacionMin = {
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

	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionClientePortafolio={
		tipo : "combobox-cliente-portafolio-sf",
		configuracion :{},
		idActual : true,
		seleccionado : true,
		filtros: {
			IdEmpresa : {field: "IdEmpresa",operator: "eq", value   :  $scope.SesionIdEmpresa },
			IdSocioDeNegocio: { field: "IdSocioDeNegocio",	operator: "eq", value : $scope.SesionCorredoraIdSocioDeNegocio},
			NombreCompletoPortafolio :{ field: "NombreCompletoPortafolio",	operator: "eq", value : ''},
			//IdPortafolio 	: { field: "IdPortafolio",operator: "eq",value   :  0 }
		},
		idDefecto : true,
		//idPostFiltro: false,
		//itemPostFiltro: false
	};

	$scope.data.oConfiguracionClasificacionTipoInstrumento = {
		tipo: "combobox-clasificacion-tipo-instrumento",
		configuracion: {},
		filtros: {
			Rv: { "field": "IdClasificacionTipoInstrumento", "operator": "eq", "value": 'RV' },
			Rf: { "field": "IdClasificacionTipoInstrumento", "operator": "eq", "value": 'RF' },    // Se habilitará con VC RF
			logic: "or"
		},
		idActual: false,
		seleccionado: false,
		idDefecto: '',
		idPostFiltro: false
	};

	$scope.data.oConfiguracionContactos = {
		tipo: "combobox-contactos",
		filtros: {
			IdCliente: { "field": "IdCliente", "operator": "eq", "value": "" }
		},
		idActual: false,
		idDefecto: false,
		seleccionado: false,
	};

	const setFiltrosClientePortafolio = function() {
		if( $scope.data.esCuentaPropia ) {
			$scope.data.oConfiguracionClientePortafolio.filtros = {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value   :  $scope.SesionIdEmpresa },
				IdSocioDeNegocio: { field: "IdSocioDeNegocio",	operator: "eq", value : $scope.SesionCorredoraIdSocioDeNegocio},
				NombreCompletoPortafolio :{ field: "NombreCompletoPortafolio",	operator: "eq", value : ''},
			}
		}

		if( !$scope.data.esCuentaPropia ){
			$scope.data.oConfiguracionClientePortafolio.filtros = {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value   :  $scope.SesionIdEmpresa },
			}
		}
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
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4171",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdTemplateCustodia: { type: "string" },
						IdReporte: { type: "string" },
						NombreReporte: { type: "string" },
						TipoReporte: { type: "string" },
						ServicioReporte: { type: "string" },
						Descripcion: { type: "string" },
						IdEmpresa: { type: "number" },
						IdEstado: { type: "string" },
						NombreArchivoReporte: { type: "string" }, 
						RutaArchivoReporte: { type: "string" }, 
						VersionReporte: { type: "string" },
						IdServicio: { type: "number" },
					}
				}
			}
		},
	});

	/*************** Validaciones ********************/


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

	$scope.data.validarValorizacionAnterior = function() {		
		
		if($scope.filtros.kngFechaConsulta > $scope.filtros.kngFechaValorizacion ) {
			bcsComunes.msg("error", "INFORME MAYOR Y MENOR CUENTA PROPIA", $translate.instant('UI_ERROR_FECHA_VALORIZACION'));
			bcsComunes.ventanaOverlay(conOperacion.idVentana, false);
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			return;
		}
		
		let fechaConsulta = util.dateSPdate($scope.filtros.kngFechaConsulta);
		let fechaValorizacion = util.dateSPdate($scope.filtros.kngFechaValorizacion);

		const partialKey = `MAYOR.MENOR.VALOR.${$scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento}.MERC${fechaConsulta}${fechaValorizacion}`;

		let valorizacionFilter =  [ { field: "Key",	operator: "eq", value: partialKey }];
		
		if($scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente){
			valorizacionFilter.push({field: "IdCliente", operator: "eq", value: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente});
		}else {
			valorizacionFilter.push({field: "IdSocioDeNegocio", operator: "eq", value: $scope.SesionCorredoraIdSocioDeNegocio});
		}

		let llaveValorizacionAnterior;

		consultarValorizacionAnterior.query({ filter: valorizacionFilter }).then(function(){
			let valorizacionAnterior = consultarValorizacionAnterior.data();
			llaveValorizacionAnterior = valorizacionAnterior[0]?.IdInforme

			if(!llaveValorizacionAnterior) {
				//Si no encuentra llave va a la generar informe por defecto
				$scope.data.informeMayorMenorValorCuentaPropia("", true);
				return;
			}
			let fechaUltimaValorizacion = valorizacionAnterior[0]?.FechaHora;
			var modalConfirmacion = bcsAlerta.crear("PREGUNTA_SI_NO",
									`La ultima valorización fue realizada el ${fechaUltimaValorizacion} ¿Desea realizar una nueva valorización?`,
									$scope.winID);
			modalConfirmacion.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI) {
					$scope.data.informeMayorMenorValorCuentaPropia("", true);
				} else {
					$scope.data.informeMayorMenorValorCuentaPropia(llaveValorizacionAnterior, false);
				}
			})
		})

		return;		
	}

	$scope.data.validarFechas = function(){ 
		if (util.datediff($scope.filtros.kngFechaValorizacion, $scope.filtros.kngFechaConsulta)._data.days < 0) {
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('FECHA_CONSULTA_MAYOR_FECHA_VALORACIÓN'));
			return false;
		}
		return true
	}

	$scope.data.cambiarFechaMin = function(){ 
		var datos = {
			p_fechas  : [
							{ fechaInicial : util.dateSPdate($scope.filtros.kngFechaConsulta), numeroDias : 1, pais : $scope.SesionIdPaisSistema }
						],
			p_diaHabil 			: 'S',
			p_soloCaeDiaHabil 	: 'S',
			p_idPais 			: $scope.SesionIdPaisSistema
		};
		$scope.filtros.kngFechaValorizacion = util.dateInicioDia($scope.filtros.kngFechaConsulta);
		$scope.filtros.fechaValorizacionMin = {
			min: util.dateInicioDia($scope.filtros.kngFechaConsulta)
		};

	}

	/*************** FIN Validaciones ********************/

	$scope.data.obtenerInformeMayorYMenorValor = function(data) {
		return new Promise((resolve, reject) => {
			bcsServiciosCustodia.ConsultarInformeMayorYMenorValor(data).$promise
				.then(result => {
					console.log(result);
					resolve(result);
				})
				.catch(error => {
					bcsComunes.setConflictos(util.parseServerResponse(error));
					bcsComunes.msg("error", "Error", "Existen problemas al realizar la búsqueda del informe mayor y menor valor");
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					reject(error);
				});
		});
	}
	
	$scope.data.informeMayorMenorValorCuentaPropia = async function(idInforme = "" ,ejecutaValorizacion = true){

		bcsComunes.ventanaOverlay($scope.IdVentana,true);

		var diffFechaValidas = $scope.data.validarFechas()
		if (!diffFechaValidas) return

		if(!ejecutaValorizacion && !idInforme) {
			bcsComunes.msg("error", "INFORME MAYOR Y MENOR CUENTA PROPIA", $translate.instant('UI_LLAVE_INVALIDA'));
			bcsComunes.ventanaOverlay(conOperacion.idVentana, false);
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			return;
		}
		
		var ServiceDataSubReporte= {
	       	 	"p_IdEmpresa"			: $scope.SesionIdEmpresa
			    ,"p_IdUsuario"			: $scope.SesionIdUsuario
			    ,"p_IdModulo"			: 'CUS'
				,"p_Fecha"				: $scope.filtros.kngFechaConsulta
				,"p_FechaConsulta"		: $scope.filtros.kngFechaConsulta
				,"p_FechaValorizacion"	: $scope.filtros.kngFechaValorizacion
				,"p_TipoValorizacion"	: 'M'
				,"p_IdCliente"			: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente || 0
				,"p_IdPortafolio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio || 0
				,"p_IdSocioNegocio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0
				,"p_TipoReporte"		: 'INF_MAME'
				,"$schema"				: [{ "field" : "LogoCorredora", "type" : "blob" }]
				,"p_IdPais"				: $scope.SesionIdPaisSistema
				,"p_IdInforme"			: idInforme
				,"p_IdTipoValorizacion" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || ''
	       };
		var ServiceDataReporte= {
	       	 	"p_IdEmpresa"			: $scope.SesionIdEmpresa
			    ,"p_IdUsuario"			: $scope.SesionIdUsuario
			    ,"p_IdModulo"			: 'CUS'
				,"p_Fecha"				: util.dateSPdate($scope.filtros.kngFechaConsulta)
				,"p_FechaConsulta"		: util.dateSPdate($scope.filtros.kngFechaConsulta)
				,"p_FechaValorizacion"	: util.dateSPdate($scope.filtros.kngFechaValorizacion)
				,"p_TipoValorizacion"	: 'MERC'
				,"p_IdCliente"			: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente  || 0
				,"p_IdPortafolio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio || 0
				,"p_IdSocioNegocio"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0
				,"p_TipoReporte"		: 'INF_MAME'
				,"p_IdPais"				: $scope.SesionIdPaisSistema
				,"p_IdInforme"			: idInforme
				,"p_EjecutaValorizacion": true
				,"p_IdTipoValorizacion" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || ''
	    };

		if (ejecutaValorizacion) {
			let valorizacionResponse;
			try {
				valorizacionResponse = await $scope.data.obtenerInformeMayorYMenorValor(ServiceDataReporte);
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
	
			ServiceDataReporte.p_IdInforme = valorizacionResponse.IdInforme;
			ServiceDataSubReporte.p_IdInforme = valorizacionResponse.IdInforme;
		} else {
			ServiceDataReporte.p_IdInforme = idInforme;
			ServiceDataSubReporte.p_IdInforme = idInforme;
		}
		
		//Cambio TipoValorizacion para procedimiento
		ServiceDataReporte.p_TipoValorizacion = ServiceDataReporte.p_TipoValorizacion === 'MERC' ? 'M': '';

		var subReports = [];
		
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'INFORME_MAYOR_MENOR_VALOR_CUENTA_PROPIA' },
								{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
								{ field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		

		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
			for(i=0;i<data.length;i++) {
				var dataTem = data[i];
				if (data[i].IdTemplateCustodia === 'CpMaMe'){					
					template = {
								TipoReporte		: data[i].FormatoSalida
								,IdReporte		: data[i].IdReporte
								,NombreReporte	: $translate.instant(data[i].NombreReporte)
								,data			: { "serviceId" 	: data[i].IdServicio,
													"serviceData" 	:ServiceDataReporte
												}
								,idReporte		: 'INF_MAME'
					};
				}
				else {
					//
					subReports.push({ "reportName" 	: data[i].NombreSubReporte
									,"parameters" 	: {} 
									,"data" 		: {
											"serviceId" : data[i].IdServicio,
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
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				//var fileURL = URL.createObjectURL(file);
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'')
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
				bcsServiciosCustodia.GenerarArchivoInformeCuentaPropia(dataTemplate.data.serviceData).$promise.then(function(result) {
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
	
	//$scope.SesionCorredoraIdSocioDeNegocio;
	//$scope.data.oConfiguracionClientePortafolio.filtros.read = true;
	$scope.data.oConfiguracionClientePortafolio.idActual	= $scope.SesionCorredoraIdSocioDeNegocio;
	$scope.data.oConfiguracionClientePortafolio.idDefecto	= $scope.SesionCorredoraIdSocioDeNegocio;

	$scope.$watch('data.esCuentaPropia',function(newval,oldval){
		if(newval){
			$scope.data.oConfiguracionClientePortafolio.idActual = $scope.SesionCorredoraIdSocioDeNegocio;
			$scope.data.oConfiguracionClientePortafolio.idActual = false;
			setFiltrosClientePortafolio();
		}else{
			$scope.data.oConfiguracionClientePortafolio.idActual = false;
			$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual = "RF";
			setFiltrosClientePortafolio();
		}
	});

	$scope.$watch('data.AllPortafolio',function(newval,oldval){
		if(newval){
			$scope.data.IdPortafolioTemporal = $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio
			$scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio = undefined;
		}else{
			$scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio = $scope.data.IdPortafolioTemporal;
		}
	});

	$scope.$watch('data.oConfiguracionClientePortafolio.seleccionado',function(newval,oldval){
		if($scope.data.AllPortafolio){
			$scope.data.IdPortafolioTemporal = $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio
			$scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio = undefined;
		} 
	});
});
