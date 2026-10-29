(function() {
	angular.module('ModuloNormativa').controller('PanelCir1985Ctrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pcpcir1985 = this;
		pcpcir1985.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pcpcir1985.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pcpcir1985.TipoArchivo			= 'pdf';
		pcpcir1985.IdVersionEsquema 	= '';
	
		pcpcir1985.IdTipoDocumento = 'CIR1985';
		

		$scope.dataTemplate	= {};
		$scope.data	= {};
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
		$scope.pcpcir1985.clear = function(){
			pcpcir1985.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);						
		};
		
		$scope.data.oClientePortafolio={
				tipo : "combobox-cliente-portafolio",
				configuracion :{},
				idActual : false,
				seleccionado : false,
				filtros: {
					IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.$root.SesionIdEmpresa },
					EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
				},
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
			bcsComunes.ventanaOverlay(pcpcir1985.idVentana, false);
			bcsComunes.ventanaData(pcpcir1985.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pcpcir1985.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pcpcir1985.idVentana, false);
		};
		//Funcion Obtener version del Esquema
		pcpcir1985.fnVersionEsquema = function() {
			if (pcpcir1985.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pcpcir1985.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pcpcir1985.IdTipoDocumento;
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
								pcpcir1985.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		
		pcpcir1985.imprimirCircular = function() {			
			// Obtener  version del tipo documento					
			pcpcir1985.fnVersionEsquema();					
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pcpcir1985.IdTipoDocumento },
					                       { field: "TipoArchivo",  operator: "eq", value: pcpcir1985.TipoArchivo },
					                       { field: "Version",operator: "eq", value: pcpcir1985.IdVersionEsquema },
					                       { field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",operator: "eq", value: 'ACT' } ];
					$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,												
												"p_IdCliente"				: $scope.data.oClientePortafolio.seleccionado.IdCliente,
												"p_IdPortafolio"            : $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
												"p_FechaIngresoSolicitud"	: util.dateSPdate(pcpcir1985.FecInicio),
												 "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
												
											  };
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo
														,"serviceData" :	parametros
													  };
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;							
							$scope.solCir.getReport($scope.dataTemplate);
						}
					});
			};
				
		
		
		
			
		pcpcir1985.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol

			
			//Especificos

			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pcpcir1985.FecInicio);

			//Tipo Listas 
			parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pcpcir1985.IdTipoDocumento;
			parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			

			parametrosSP.p_CreaParam 				= 'S';
			
			parametrosSP.p_EsPauta					= 'N'
			
			var fd = util.dateSPdate(pcpcir1985.FecInicio);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
			

			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CrearSolicitudCircular(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		

		$scope.$watch('pcpcir1985.FecInicio', function(newval,oldval){
			pcpcir1985.fnVersionEsquema();
		},true);
		
		$scope.pcpcir1985.clear();

	});
}());