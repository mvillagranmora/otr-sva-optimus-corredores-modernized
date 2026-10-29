angular.module('ModuloCuentaInversion').factory('bcsFormatoDocumentosCuentaInversion',
		function(bcsComunes,$rootScope,$translate,$http)  
{
	var obj = {};
	obj.infoComprobante = {};
	

	//Formato comprobante de movimientos
	var formatoComprobante = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			transport: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1773",dataType: "json"}},
			filter: [{ field: "IdTemplateCuentaInversion",	operator: "eq", value: 'RPT_CMPCTA'}, 
					 { field: "IdEstado",  					operator: "eq", value: 'ACT' },
					 { field: "IdEmpresa", 					operator: "eq", value: $rootScope.SesionIdEmpresa },
					],
			schema : {
				model : {
					fields : {								
						IdReporte		: { type: "string"},
						NombreReporte	: { type: "string"},
						IdServicio		: { type: "number"},
						Descripcion		: { type: "string"},
						IdEmpresa		: { type: "number"},
						IdEstado		: { type: "string"},
						ReportName		: { type: "string"},
						MasterReportId	: { type: "string"},
						ReportFile		: { type: "string"},
						ReportVersion	: { type: "number"},
						FormatoSalida	: { type: "string"},						
					}
				}
			}
		}
	});
	formatoComprobante.query().then(function(){
		if (typeof formatoComprobante.data()[0] !== 'undefined'){

			$.each(formatoComprobante.data(), function(key, registro){
				 obj.infoComprobante = {
					IdReporte		: registro.IdReporte,
					NombreReporte	: registro.NombreReporte,
					IdServicio		: registro.IdServicio,
					Descripcion		: registro.Descripcion,
					IdEmpresa		: registro.IdEmpresa,
					IdEstado		: registro.IdEstado,
					ReportName		: registro.ReportName,
					MasterReportId	: registro.MasterReportId,
					ReportFile		: registro.ReportFile,
					ReportVersion	: registro.ReportVersion,
					FormatoSalida	: registro.FormatoSalida,		
				 };					 
			 });
		}		
	});
	obj.CrearComprobanteCuentaInversion = function(configuracion,IdCargoAbonoMovimiento){
		var fecha = new Date();		
		var nombreArchivoDescarga = configuracion.NombreReporte  +"_Nro_"+ 
									String(IdCargoAbonoMovimiento)  +"_"+  
									String(fecha.getFullYear()) + 
									String(fecha.getMonth()+1) + 
									String(fecha.getDate()) + "_" + 
									String(fecha.getHours()) + 
									String(fecha.getMinutes()) + 
									String(fecha.getSeconds()) + "." + 
									configuracion.FormatoSalida;
		
		var parameters = {	"reportId"      		: configuracion.IdReporte,
							"debug"					: "true",
							"exportFormat"  		: configuracion.FormatoSalida,						
							"parameters"    		: "",						
							"configuration"			: {
								"mainReport"		: {	
									"data" 			: {
											"serviceId" 	: configuracion.IdServicio,
											"serviceData" 	: {
																"P_IDEMPRESA"      			: Number($rootScope.SesionIdEmpresa),										                      
																"P_IDCARGOABONOMOVIMIENTO"  : Number(IdCargoAbonoMovimiento),
																"$schema":[{"field":"LogoCorredora", "type":"blob"}]
								                              },
									},										                                  
									"parameters"	: {},
									"reportName"	: configuracion.NombreReporte
								},
								"subReports": []
							},
		};
	
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				saveAs(file, nombreArchivoDescarga);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
			});
	}

	
	return obj;
});	

