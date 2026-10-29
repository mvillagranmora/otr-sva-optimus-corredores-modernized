(function() {
	angular.module('ModuloNormativa').controller('PanelCuentaPropiaOtrasBolsasCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pcpob = this;
		pcpob.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pcpob.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pcpob.TipoArchivo			= 'pdf';
		pcpob.IdVersionEsquema 	= '';
	
		pcpob.IdTipoDocumento = 'ACPOB';
		

		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ 
		{
			NombreParametro : 'idUsuario',
			Valor : $scope.SesionIdUsuario
		},{
			NombreParametro : 'schema',
			Valor :  Model.getSchema()
		}, {
			NombreParametro : 'grabar',
			Valor : $scope.solCir.grabar
		}, {
			NombreParametro : 'reemplazar',
			Valor : $scope.solCir.reemplazar
		},{
			NombreParametro : 'separadorListas',
			Valor : $scope.solCir.separadorListas
		},{
			NombreParametro : 'simboloDecimal',
			Valor : $scope.solCir.simboloDecimal
		},{
			NombreParametro : 'simboloFecha',
			Valor : $scope.solCir.simboloFecha
		},{
			NombreParametro : 'aceptarFechasNull',
			Valor : $scope.solCir.aceptarFechasNull
		},{
			NombreParametro : 'EsPauta',
			Valor : 'N'
		},]};
		
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pcpob.clear = function(){
			pcpob.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
						
		};
		
		var successCrearSolicitud = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("info","info",'Solicitud creada con éxito');
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				
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
			bcsComunes.ventanaOverlay(pcpob.idVentana, false);
			bcsComunes.ventanaData(pcpob.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pcpob.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pcpob.idVentana, false);
		};
		//Funcion Obtener version del Esquema
		pcpob.fnVersionEsquema = function() {
			if (pcpob.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pcpob.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pcpob.IdTipoDocumento;
						if (fd !== null || fd !== undefined) {
							paramServicio.p_Fecha = fd;
						}
						else 
						{
							paramServicio.p_Fecha = $scope.SesionFechaSistema;
						}
						
						bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
							if (respuesta.P_OUT_VERSIONESQUEMA)
							{
								pcpob.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
							}
							else
							{	
								bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
							}		
							
						}, function(error){
							bcsComunes.msg('error', $scope.TITLE, error.message);
						});	
				};	
		};		
		
		pcpob.imprimirCircular = function() {
			
			// Obtener  version del tipo documento
					
			pcpob.fnVersionEsquema();
					
					  
					
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pcpob.IdTipoDocumento },
					                       { field: "TipoArchivo",  operator: "eq", value: pcpob.TipoArchivo },
					                       { field: "Version",operator: "eq", value: pcpob.IdVersionEsquema },
					                       { field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",operator: "eq", value: 'ACT' } ];
					$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_FechaOperacionDesde"		: util.dateSPdate(pcpob.FecInicio),
												"p_FechaOperacionHasta"		: util.dateSPdate(pcpob.FecInicio),
												"p_IdCliente"				:0,
												 "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
												
											  };
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo
														   , "serviceData" :	parametros
													  };
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;							
							
							
							//$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+",p_FechaOperacionDesde =>'" + util.dateSPdate(pc1.FecInicio) + "',p_FechaOperacionHasta =>'" +  util.dateSPdate(pc1.FecTermino) + "',p_out_TablaCarta1 => ?)";
							$scope.solCir.getReport($scope.dataTemplate);
							
										
						}
					});
				
			};
		
		
		
		 
			
		pcpob.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol

			
			//Especificos

			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pcpob.FecInicio);

			//Tipo Listas 
			parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pcpob.IdTipoDocumento;
			parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			

			parametrosSP.p_CreaParam 				= 'S';
			
			parametrosSP.p_EsPauta					= 'N'
			
			var fd = util.dateSPdate(pcpob.FecInicio);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
			

			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CrearSolicitudCircular(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		

		$scope.$watch('pcpob.FecInicio', function(newval,oldval){
			pcpob.fnVersionEsquema();
		},true);
		
		$scope.pcpob.clear();

	});
}());