angular.module('ModuloMesa').controller('ContratoCondicionesGeneralesFWDCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,$resource, bcsServiciosMesa,$http,bcsAlerta,OPCIONES_BCSALERTA)
{	
	var conOperacion = this;
	$scope.data = {};
	$scope.Estado = '';
	$scope.dataTemplate	= {};
	$scope.dataTemplate2= {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var fechaOperacion = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.NombreArchivo = "ContratoCondicionesGenerales_"+$scope.SesionFechaSistema;
	$scope.data.HabilitaBotones = true;
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.AllPortafolio= false;
	$scope.data.FechaOperacionOpt = {
			max:fechaOperacion
	};
	$scope.data.esFormaConjunta = true;
	$scope.apoCorredora = {};
	$scope.datosAntes ={};
	$scope.datosDespues ={};
	$scope.datosAntesCorredora ={};
	$scope.datosDespuesCorredora ={};
	$scope.definicionIndicadores = {};
	$scope.definicionIndicadoresCorredora = {};
	$scope.apoCorredora.IdCorredora = $scope.SesionCorredoraIdSocioDeNegocio;
	$scope.data.kngFechaOperacion = fechaOperacion;
	$scope.IdportafolioFWD;
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionClientePortafolioCorredora={
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
	

	$scope.data.oConfiguracionApoderadoCorredora={
			filtros: {
				IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
				IdPersonaOrigen : {"field"   : "IdPersonaOrigen","operator": "eq", "value" :$scope.SesionIdPersonaCorredora},
				Administracion : {"field"   : "IdTipoRelacion","operator": "eq", "value" : "APO"},
				IdEstado : {"field" : "IdEstado", "operator": "eq", "value": "ACT"}
			},
		};
	
	$scope.data.oConfiguracionApoderadoCliente={
			filtros: {
				IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
				IdPersonaOrigen : {"field"   : "IdPersonaOrigen","operator": "eq", "value" :'0'},
				Administracion : {"field"   : "IdTipoRelacion","operator": "eq", "value" : "APO"},
				IdEstado : {"field" : "IdEstado", "operator": "eq", "value": "ACT"}
			},
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
						IdEstado		: {type: "string"},
						
						
					}
				}
			}
		},
	});

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
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
					conflictos:util.parseServerResponse(response)
				},$scope.winID);
			});
	};

	$scope.validarContratoMarcoFWDFirmado = function(IdCliente, IdTipoDocumento){
		var dataSource = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700343",
						dataType: "json"
					}
				},
				filter: [
					{ field: 'IdEmpresa', operator: 'eq', value: Number($scope.$root.SesionIdEmpresa) },
					{ field: 'IdCliente', operator: 'eq', value: Number(IdCliente) },
					{ field: 'IdTipoDocumento', operator: 'eq', value: String(IdTipoDocumento) }
				],
				schema: {
					model: {
						fields: {
							FirmaEstado: { type: 'string' }
						}
					}
				}
			}
		}
	);
	
	const filter = [
		{ field: 'IdEmpresa', operator: 'eq', value: Number($scope.$root.SesionIdEmpresa) },
		{ field: 'IdCliente', operator: 'eq', value: Number(IdCliente) },
		{ field: 'IdTipoDocumento', operator: 'eq', value: String(IdTipoDocumento) }
	]
	
	var conflictos = {};
	conflictos.P_OUT_MENSAJES= [{id: 0, tipo: "ADVERTENCIA", codigo: "", descripcion: $translate.instant('UI_ADVERTENCIA_FWD_NO_FIRMADO')}];
	conflictos = util.parseServerResponse(conflictos);
	
	dataSource.query({filter: filter}).then(function(){
		if (dataSource.view().length>0){
			if (dataSource.view()[0].FirmaEstado !== "ACT"){
				bcsComunes.ventanaMsg({
					mensaje: "UI_ADVERTENCIA",
					conflictos: conflictos
				},$scope.winID);
			}
			return;
		}else{
			bcsComunes.ventanaMsg({
				mensaje: "UI_ADVERTENCIA",
				conflictos: conflictos
			},$scope.winID);
			return;
		}
	});
}

$scope.data.contratoCondcionesGeneralesFWD = function(){
		
		var subReports = [];
		
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'FWDCONDICIONESGENERALES' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		
		

		var ServiceDataReporte= {
				"p_IdEmpresa"					   : $scope.SesionIdEmpresa,
				"p_IdModulo"					   : 'FWD',
				"p_Fecha"						   : $scope.data.kngFechaOperacion,	
				"p_IdCliente"                      : $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente === undefined?null:$scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente,
				"p_IdPortafolio"				   : $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio||null,
				"p_IdPersonaCorredora"             : $scope.SesionIdPersonaCorredora,
				"p_IdApoderadoCorredoraPrimario"   : $scope.data.ApoderadoPrimarioCorredora.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoPrimarioCorredora.seleccionado.IdPersonaDestino,
				"p_IdApoderadoCorredoraSecundario" : $scope.data.ApoderadoSecundarioCorredora.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoSecundarioCorredora.seleccionado.IdPersonaDestino,
				"p_IdApoderadoClientePrimario"     : $scope.data.ApoderadoPrimarioCliente.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoPrimarioCliente.seleccionado.IdPersonaDestino,
				"p_IdApoderadoClienteSecundario"   : $scope.data.ApoderadoSecundarioCliente.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoSecundarioCliente.seleccionado.IdPersonaDestino,
				"p_IdPais"						   : $scope.SesionIdPaisSistema,
				"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
				//"p_EsFormaIndividual"              : $scope.data.esFormaIndividual===true?'S':'N',
				//"p_EsFormaConjunta"				   : $scope.data.esFormaConjunta===true?'S':'N'	
				
	       };	
		
		$scope.validarContratoMarcoFWDFirmado($scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente, "CONTMARFWD");
		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
			for(i=0;i<data.length;i++) {
				var dataTem = data[i];
				if (data[i].IdTemplateMesa === 'FWDCONDICIONESGENERALES'){
					
						template = {
									TipoReporte		: data[i].TipoReporte//'pdf' 
									,IdReporte		: data[i].IdReporte
									,NombreReporte	: $translate.instant(data[i].NombreReporte)
									,data			: { "serviceId" 	: data[i].ServicioReporte,
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
												                  "serviceData" :ServiceDataReporte
												              }
										});
				}			
			}
			
			
		template.subReports = subReports;
		console.log(template);
		$scope.getReport(template);
		
		
			
		});
	};	

	// FUNCIONES 

	const IdProtafolioMesa =async function getIdPortafolioMesa(){
		
			var dataPortafolio =await new bcs.DataSource({
				type: "odata",
				
				definition : {
					serverFiltering:true,
					serverSorting: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=454",
							dataType:"json"
						}
					},
					filter:[{field:"TipoProducto", operator:"eq", value: 'FORWARD'}],
					sort:[
						{ field: "PorDefectoUI", dir: "desc" },
						{ field: "Nombre", dir:"asc" }
					],
					schema: {
						model : {
							fields : {
							}
						}
					}
				  }
			});
			
			dataPortafolio.fetch().then(function(){
				$scope.IdportafolioFWD=dataPortafolio.data()[0].IdPortafolio;	
			});
		
	}

var cargaDatosPreferencias = function(PortafolioCliente){
			var parametrosSP = {
				p_IdEmpresa				: $scope.SesionIdEmpresa || 1,
				p_IdEntidad				: PortafolioCliente || 0,
				p_IdTipoEntidad			: 'PORTAFOLIO',
				p_CodigoTipoIndicador	: '',
				p_GrupoIndicador		: '',
				p_ModeloCliente			: '',
				p_IdIndicador			: 0
			};



			
			consultaIndicadoresSP(parametrosSP).then(function(result) {
				if(result && result.P_OUT_INDICADORES){
					$.each(result.P_OUT_INDICADORES,function(key,value){
						$scope.definicionIndicadores[value.GrupoIndicador + "//" +value.CodigoTipoIndicador] = value === undefined?'':value;
						
					});

					$scope.datosAntes['GRUPOFORWARD//FWDAPO1']	= $scope.definicionIndicadores['GRUPOFORWARD//FWDAPO1'].ValorIndicador;
					//Segundo Apoderado Firma Contrato
					$scope.datosAntes['GRUPOFORWARD//FWDAPO2']	= $scope.definicionIndicadores['GRUPOFORWARD//FWDAPO2'].ValorIndicador;
				}
				$scope.datosDespues = _.clone($scope.datosAntes);
			},function(result){
				bcsComunes.ventanaMsg({
					mensaje:'',
					conflictos:util.parseServerResponse(result)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
			});	
	};
	
	var cargaDatosPreferenciasCorredora = function(){
		
		var parametrosSP = {
			p_IdEmpresa				: $scope.SesionIdEmpresa || 1,
			p_IdEntidad				: $scope.IdportafolioFWD || 0,
			p_IdTipoEntidad			: 'PORTAFOLIO',
			p_CodigoTipoIndicador	: '',
			p_GrupoIndicador		: '',
			p_ModeloCliente			: '',
			p_IdIndicador			: 0
		};
		consultaIndicadoresSP(parametrosSP).then(function(result) {
			if(result && result.P_OUT_INDICADORES){
				$.each(result.P_OUT_INDICADORES,function(key,value){
					$scope.definicionIndicadoresCorredora[value.GrupoIndicador + "//" +value.CodigoTipoIndicador] = value === undefined?'':value;
					
				});

				$scope.datosAntesCorredora['GRUPOFORWARD//FWDAPO1']	= $scope.definicionIndicadoresCorredora['GRUPOFORWARD//FWDAPO1'].ValorIndicador;
				//Segundo Apoderado Firma Contrato
				$scope.datosAntesCorredora['GRUPOFORWARD//FWDAPO2']	= $scope.definicionIndicadoresCorredora['GRUPOFORWARD//FWDAPO2'].ValorIndicador;
			}
			$scope.datosDespuesCorredora = _.clone($scope.datosAntesCorredora);
		},function(result){
			bcsComunes.ventanaMsg({
				mensaje:'',
				conflictos:util.parseServerResponse(result)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID,false);
		});	
};
	var consultaIndicadoresSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=275";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
	};



	//FIN FUNCIONES

	$scope.$watch('data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio',function(newval,oldval) {
		
		if(newval){
			cargaDatosPreferencias(newval);
		}
   	});

	$scope.$watch('IdportafolioFWD',function(newval,oldval) {
		
		if(newval){
			cargaDatosPreferenciasCorredora();
		}
	});

	$scope.data.oConfiguracionClientePortafolioCorredora.idActual	=$scope.SesionCorredoraIdSocioDeNegocio;
	$scope.data.oConfiguracionClientePortafolioCorredora.idDefecto	= $scope.SesionCorredoraIdSocioDeNegocio;
	IdProtafolioMesa();
});