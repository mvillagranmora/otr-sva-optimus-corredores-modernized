angular.module('ModuloMesa').controller('ContratoCondicionesGeneralesCtrl',
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
	$scope.datosAntesCorredora ={};
	$scope.datosDespues ={};
	$scope.datosDespuesCorredora ={};
	$scope.definicionIndicadores = {};
	$scope.definicionIndicadoresCorredora = {};
	$scope.IdPortafolioMesaRF;
	$scope.apoCorredora.IdCorredora = $scope.SesionCorredoraIdSocioDeNegocio;
	$scope.data.kngFechaOperacion = fechaOperacion;
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
/*	var ConsultaTemplate = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						CompanyId	: {type: "string"},
						Id			: {type: "string"},
						ReportName	: {type: "string"}
					}
				}
			}
		},
	});*/
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

$scope.data.contratoCondcionesGenerales = function(){
		
		var subReports = [];
		
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'MRFCONDICIONESGENERALES' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		
		
		/*var ServiceDataSubReporte= {
				"p_IdEmpresa"		: $scope.SesionIdEmpresa,
				"p_IdModulo"		: 'MRF',
				"P_IdOperacion"	    : 1917,
				"p_IdPais" 			: 44
			//	,"$schema"				: [{ "field" : "LogoCorredora", "type" : "blob" }]
	       };*/
		var ServiceDataReporte= {
				"p_IdEmpresa"					   : $scope.SesionIdEmpresa,
				"p_IdModulo"					   : 'MRF',
				"p_Fecha"						   : $scope.data.kngFechaOperacion,	
				"p_IdCliente"                      : $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente === undefined?null:$scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente,
				"p_IdPersonaCorredora"             : $scope.SesionIdPersonaCorredora,
				"p_IdApoderadoCorredoraPrimario"   : $scope.data.ApoderadoPrimarioCorredora.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoPrimarioCorredora.seleccionado.IdPersonaDestino,
				"p_IdApoderadoCorredoraSecundario" : $scope.data.ApoderadoSecundarioCorredora.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoSecundarioCorredora.seleccionado.IdPersonaDestino,
				"p_IdApoderadoClientePrimario"     : $scope.data.ApoderadoPrimarioCliente.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoPrimarioCliente.seleccionado.IdPersonaDestino,
				"p_IdApoderadoClienteSecundario"   : $scope.data.ApoderadoSecundarioCliente.seleccionado.IdPersonaDestino === undefined?null:$scope.data.ApoderadoSecundarioCliente.seleccionado.IdPersonaDestino,
				"p_IdPais"						   : $scope.SesionIdPaisSistema,
				"p_EsFormaIndividual"              : $scope.data.esFormaIndividual===true?'S':'N',
				"p_EsFormaConjunta"				   : $scope.data.esFormaConjunta===true?'S':'N',
				"$schema"						   : [{ "field" : "LogoCorredora", "type" : "blob" }]
				
	       };	
		
		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
			for(i=0;i<data.length;i++) {
				var dataTem = data[i];
				if (data[i].IdTemplateMesa === 'MRFCONDICIONESGENERALES'){
					
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
				filter:[{field:"TipoProducto", operator:"eq", value: 'MESA RF'}],
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
			console.log(dataPortafolio.data()[0]);
			$scope.IdPortafolioMesaRF = dataPortafolio.data()[0].IdPortafolio; // trae primer valor del filtro ( si hay predeterminado lo trae , si no trae el primer id para la mesa )	
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
			//Primer Apoderado Firma Adendum
			$scope.datosAntes['GRUPOMESA//MES_APO1']		= $scope.definicionIndicadores['GRUPOMESA//MES_APO1'].ValorIndicador;
			//Primer Apoderado Firma Adendum
			$scope.datosAntes['GRUPOMESA//MES_APO2']		= $scope.definicionIndicadores['GRUPOMESA//MES_APO2'].ValorIndicador;
			//Firma Conjunta Adendum
			$scope.datosAntes['GRUPOMESA//MES_FIRMA_CONJUNTA']		= ($scope.definicionIndicadores['GRUPOMESA//MES_FIRMA_CONJUNTA'].ValorIndicador	=== 'S') ? true:false;
		}
		$scope.datosDespues = _.clone($scope.datosAntes);
		$scope.data.esFormaConjunta = $scope.datosAntes['GRUPOMESA//MES_FIRMA_CONJUNTA'];
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
		p_IdEntidad				: $scope.IdPortafolioMesaRF || 0,
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
			//Primer Apoderado Firma Adendum
			$scope.datosAntesCorredora['GRUPOMESA//MES_APO1']		= $scope.definicionIndicadoresCorredora['GRUPOMESA//MES_APO1'].ValorIndicador;
			//Primer Apoderado Firma Adendum
			$scope.datosAntesCorredora['GRUPOMESA//MES_APO2']		= $scope.definicionIndicadoresCorredora['GRUPOMESA//MES_APO2'].ValorIndicador;
			
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

$scope.$watch('IdPortafolioMesaRF',function(newval,oldval) {
		
	if(newval){
		cargaDatosPreferenciasCorredora();
	}
});

$scope.$watch('data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio',function(newval,oldval) {
	
	if(newval){
		cargaDatosPreferencias(newval);
	}
   });

	//WATCH
	$scope.$watch('data.esFormaIndividual',function(newval,oldval) {
		
		if(newval){
			if($scope.data.esFormaConjunta===true){
				$scope.data.esFormaConjunta=false;
			}
			
		}
   	});
	$scope.$watch('data.esFormaIndividual',function(newval,oldval) {
		
		if(oldval){
			if($scope.data.esFormaIndividual===false){
				$scope.data.esFormaConjunta=true;
			}
			
		}
   	});
	
	$scope.$watch('data.esFormaConjunta',function(newval,oldval) {
		
		if(newval){
			if($scope.data.esFormaIndividual===true){
				$scope.data.esFormaIndividual=false;
			}
			
		}
   	});
	$scope.$watch('data.esFormaConjunta',function(newval,oldval) {
		
		if(oldval){
			if($scope.data.esFormaConjunta===false){
				$scope.data.esFormaIndividual=true;
			}
		}
   	});
	
	
	
/*	$scope.data.generarReporte = function()		
    { 
		$scope.dataTemplate= {};
		$scope.subReports = [];
		//--------------------
		var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'MRFCONDICIONESGENERALES' } ];
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			if(ConsultaTemplate.view().length > 0) {
				$scope.dataTemplate.TipoReporte		= 'pdf';
				$scope.dataTemplate.Id	= ConsultaTemplate.view()[0].Id;
				$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;
			
				var parametros = { 	"p_IdEmpresa"		: $scope.SesionIdEmpresa,
									"p_IdModulo"		: 'MRF',
									"P_IdOperacion"	    : 1917,
									"p_IdPais" 			: 44
									/*"P_OPCION"		: opcionCobertura,
//									"P_TIPOVALOR"		: tipoCobertura, 
//									"P_IDCLIENTE"		: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente === undefined ? null : $scope.data.oConfiguracionClientePortafolio.seleccionado.IdCliente,
//									"P_IDPORTAFOLIO"	: $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio === undefined ? null : $scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio
								};
			 
				$scope.dataTemplate.data = { "serviceId" : Number(461), "serviceData" : parametros };
			
				$scope.getReport($scope.dataTemplate);
			}
		});
    };*/

	
	//$scope.SesionCorredoraIdSocioDeNegocio;
	//$scope.data.oConfiguracionClientePortafolio.filtros.read = true;
	$scope.data.oConfiguracionClientePortafolioCorredora.idActual	=$scope.SesionCorredoraIdSocioDeNegocio;
	$scope.data.oConfiguracionClientePortafolioCorredora.idDefecto	= $scope.SesionCorredoraIdSocioDeNegocio;
	IdProtafolioMesa();
});