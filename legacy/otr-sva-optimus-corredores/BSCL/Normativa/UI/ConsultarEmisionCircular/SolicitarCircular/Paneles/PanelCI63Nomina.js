(function() {
	angular.module('ModuloNormativa').controller('PanelCI63NominaCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pcn = this;
		pcn.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		//pcn.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		pcn.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		pcn.TipoArchivo			= 'pdf';
	    pcn.IdVersionEsquema 	= '';	
		pcn.IdTipoDocumento = 'CI63Nomina';
		$scope.dataTemplate	= {};	
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		pcn.opcionFiltro='AM';
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
		// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
		{	Tipo			: 'BATCH',
			NombreParametro : 'idUsuario',
			Valor : $scope.SesionIdUsuario
		},{ Tipo			: 'BATCH',
			NombreParametro : 'schema',
			Valor :  Model.getSchema()
		}, {Tipo			: 'BATCH',
			NombreParametro : 'grabar',
			Valor : $scope.solCir.grabar
		}, {Tipo			: 'BATCH',
			NombreParametro : 'reemplazar',
			Valor : $scope.solCir.reemplazar
		},{ Tipo			: 'BATCH',
			NombreParametro : 'separadorListas',
			Valor : $scope.solCir.separadorListas
		},{ Tipo			: 'BATCH',
			NombreParametro : 'simboloDecimal',
			Valor : $scope.solCir.simboloDecimal
		},{	Tipo			: 'BATCH',
			NombreParametro : 'simboloFecha',
			Valor : $scope.solCir.simboloFecha
		},{	Tipo			: 'BATCH',
			NombreParametro : 'aceptarFechasNull',
			Valor : $scope.solCir.aceptarFechasNull
		},]};
		
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pcn.clear = function(){
			//$scope.formData = {};
			//	$scope.formData.tipoSocioNegocio = 0;
			//	$scope.formData.tipoDocumentoIdentidad = 0;
			pcn.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pcn.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			
		};
		
		var successCrearSolicitud = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("info","info",'Solicitud creada con éxito');
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
			}
			else
			{	
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",$translate.instant('Solicitud no fue creada'));
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			bcsComunes.ventanaOverlay(pcn.idVentana, false);
			bcsComunes.ventanaData(pcn.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pcn.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pcn.idVentana, false);
		};

		//Funcion Obtener version del Esquema
		pcn.fnVersionEsquema = function() {
			var paramServicio = {};
			paramServicio.p_TipoDocumento = pcn.IdTipoDocumento;
			paramServicio.p_Fecha = $scope.SesionFechaSistema;
			
			bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
				if (respuesta.P_OUT_VERSIONESQUEMA)
				{
					pcn.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
				}
				else
				{	
					bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
				}		
				
			}, function(error){
				bcsComunes.msg('error', $scope.TITLE, error.message);
			});	
			
		};		
		
		$scope.imprimirCircular = function() {
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pcn.IdTipoDocumento },
					                       { field: "TipoArchivo",  operator: "eq", value: pcn.TipoArchivo },
					                       { field: "Version",  	 operator: "eq", value: pcn.IdVersionEsquema },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					
					$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							
						//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_TipoInformacion"			: pcn.opcionFiltro,
												
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'Carta1Art171BCS.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;									
							
							
							
							
							//$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+",p_TipoInformacion =>'" + pcn.opcionFiltro +  "',p_out_TablaNomina => ?)";
							$scope.solCir.getReport($scope.dataTemplate);
						}
					});
				
			};		
		
		 pcn.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			//parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
	
			//Especificos

			//parametrosSP.p_TipoInformacion 			=  pcn.opcionFiltro;
			
			//Tipo Listas 
			//parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			//parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			//parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			//parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pcn.IdTipoDocumento;
			//parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			
			//parametrosSP.p_CreaParam 				= 'S';

			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TIPINF' , Valor : pcn.opcionFiltro });
						
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		
		pcn.visorCircular = function() {
			
			var paramServicio = {};
			paramServicio.p_TipoDocumento = pcn.IdTipoDocumento;
			paramServicio.p_Fecha = $scope.SesionFechaSistema;
			
			bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
				if (respuesta.P_OUT_VERSIONESQUEMA)
				{
					bcsVisorCircularConfig.getDataSource(respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_ESQUEMA , respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION, pcn.IdTipoDocumento, 
							function (){
								var tablaParametros = [];
								
								tablaParametros.push({TipoParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
								tablaParametros.push({TipoParametro : 'TIPINF',Valor :  pcn.opcionFiltro});
								
								bcsComunes.ventanaAbrir("VisorCircular", {
								tablaParametros : tablaParametros,
								IdTipoDocumento : pcn.IdTipoDocumento
							});
						}, function(error){
							bcsComunes.msg('error', $scope.TITLE, error.message);
						});
				}
				else
				{	
					bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
				}		
				
			}, function(error){
				bcsComunes.msg('error', $scope.TITLE, error.message);
			});
		};
		
		pcn.fnVersionEsquema();
		$scope.pcn.clear();
		
	});
}());