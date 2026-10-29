(function() {
	angular.module('ModuloNormativa').controller('PanelUAF57BFPEJCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pUAF57 = this;
		pUAF57.idVentana 	= bcsComunes.ventanaWinId($scope);
		pUAF57.domVentana	= $("[ventanaid='"+$scope.idVentana+"']");
		pUAF57.TipoArchivo	= 'pdf';
	    pUAF57.IdVersionEsquema	= '';
		pUAF57.IdTipoDocumento	= 'UAF57BFPEJ';
		
		$scope.dataTemplate = {};	
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
 		// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
		{
				Tipo			: 'BATCH',
	 			NombreParametro : 'IdEmpresa',
	 			Valor :  $scope.SesionIdEmpresa
	 	},{	Tipo			: 'BATCH',
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
		$scope.pUAF57.clear = function(){
			pUAF57.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pUAF57.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		};
		

		//Funcion Obtener version del Esquema
		pUAF57.fnVersionEsquema = function(){
			var paramServicio = {};
			paramServicio.p_TipoDocumento = pUAF57.IdTipoDocumento;
			paramServicio.p_Fecha = $scope.SesionFechaSistema;
			
			bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta){
				if (respuesta.P_OUT_VERSIONESQUEMA){
					pUAF57.IdVersionEsquema = respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
				}
				else{	
					bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
				}
			}, function(error){
				bcsComunes.msg('error', $scope.TITLE, error.message);
			});
		};		
		
		$scope.imprimirCircular = function(){
			var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pUAF57.IdTipoDocumento },
			                       { field: "TipoArchivo",  operator: "eq", value: pUAF57.TipoArchivo },
			                       { field: "Version",  	 operator: "eq", value: pUAF57.IdVersionEsquema },
			                       //{ field: "Version",  	 operator: "eq", value: 'V1' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			
			$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
				if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;

					//PARAMETROS
					var parametros = {	"p_IdEmpresa" : $scope.$root.SesionIdEmpresa,
										"p_IdPersona" : pUAF57.oConfiguracionSocioNegocio.seleccionado.IdPersona || 0,
										"$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
					};
					
					//DATA 
					$scope.dataTemplate.data= {  "serviceId"   :	$scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					
					//$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+ ",p_out_TablaPEP => ?)";
					$scope.solCir.getReport($scope.dataTemplate);
				}
			});
		};
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pUAF57.idVentana, false);
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
			bcsComunes.ventanaOverlay(pUAF57.idVentana, false);
			bcsComunes.ventanaData(pUAF57.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pUAF57.idVentana);
			
		};
		
		/*
		 { field: "IdTipoDocumento",operator: "eq", value: pUAF57.IdTipoDocumento },
			                       { field: "TipoArchivo",  operator: "eq", value: pUAF57.TipoArchivo },
			                       { field: "Version",  	 operator: "eq", value: pUAF57.IdVersionEsquema },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } */
		
		
		 $scope.solicitar = function() {
		    	var parametrosSP = {};
		    	//Comunes
		    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
		    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
		    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
				parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
			
				//Especificos
				parametrosSP.p_IdPersona 					=  pUAF57.oConfiguracionSocioNegocio.seleccionado.IdPersona;
				
				//Creacion Documento
				parametrosSP.p_IdTipoDocumento 			= $scope.solCir.cmbbx.IdTipoDocumento;
				//parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;*/

				//Tarea Programada			
				//parametrosSP.p_CreaParam 				= 'S';
								
				parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
				
				if(pUAF57.oConfiguracionSocioNegocio.seleccionado){
					parametrosSP.p_TablaParametros.push({Tipo: 'BATCH',NombreParametro : 'IdPersona',Valor :pUAF57.oConfiguracionSocioNegocio.seleccionado.IdPersona});
					//parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro : 'IdPersona',Valor :pUAF57.oConfiguracionSocioNegocio.seleccionado.IdPersona});
					
				}else{
					parametrosSP.p_TablaParametros.push({Tipo: 'BATCH',NombreParametro : 'IdPersona',Valor :0});
				}
				
				
		 		
				bcsComunes.msg('info', 'Carga', 'En proceso...');
				bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
				
		      
			};
		
		 		
		pUAF57.visorCircular = function(){
			var paramServicio = {};
			paramServicio.p_TipoDocumento = pUAF57.IdTipoDocumento;
			paramServicio.p_Fecha = $scope.SesionFechaSistema;
			
			bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
				if (respuesta.P_OUT_VERSIONESQUEMA){
					bcsVisorCircularConfig.getDataSource(respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_ESQUEMA , respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION, pUAF57.IdTipoDocumento, 
						function (){
							var tablaParametros = [];
							
							tablaParametros.push({TipoParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
							
							bcsComunes.ventanaAbrir("VisorCircular", {
							tablaParametros : tablaParametros,
							IdTipoDocumento : pUAF57.IdTipoDocumento
						});
					}, function(error){
						bcsComunes.msg('error', $scope.TITLE, error.message);
					});
				}
				else{	
					bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
				}
			}, function(error){
				bcsComunes.msg('error', $scope.TITLE, error.message);
			});
		};
		
		pUAF57.fnVersionEsquema();
		$scope.pUAF57.clear();
		
	});
}());