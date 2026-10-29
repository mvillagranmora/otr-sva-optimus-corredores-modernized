(function() {
	angular.module('ModuloNormativa').controller('PanelCarta1BCSCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		//pc1.IdTipoCircular 	= 'ART171CartaUno';
		//pc1.IdTipoTarea		= 'EXPORTACIONCARTA1ART171';
	    pc1.TipoArchivo			= 'pdf';
	    pc1.IdVersionEsquema 	= '';
		
		pc1.IdTipoDocumento = 'ART171Car1';
		

		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
		// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
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
		},]};
		
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pc1.clear = function(){
			//$scope.formData = {};
			//	$scope.formData.tipoSocioNegocio = 0;
			//	$scope.formData.tipoDocumentoIdentidad = 0;
			pc1.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pc1.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			
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
			bcsComunes.ventanaOverlay(pc1.idVentana, false);
			bcsComunes.ventanaData(pc1.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pc1.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pc1.idVentana, false);
		};
		//Funcion Obtener version del Esquema
		pc1.fnVersionEsquema = function() {
			if (pc1.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pc1.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pc1.IdTipoDocumento;
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
								pc1.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		
		$scope.imprimirCircular = function() {
					if(pc1.HabFecTermino===false){
						pc1.FecTermino=pc1.FecInicio;
					};
			// Obtener  version del tipo documento
					
					 
					
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pc1.IdTipoDocumento },
					                       { field: "TipoArchivo",  operator: "eq", value: pc1.TipoArchivo },
					                       { field: "Version",  	  operator: "eq", value: pc1.IdVersionEsquema },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_FechaOperacionDesde"		: util.dateSPdate(pc1.FecInicio),
												"p_FechaOperacionHasta"		: util.dateSPdate(pc1.FecTermino),												
												
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'Carta1Art171BCS.rpt'
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
			
		 pc1.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
			//parametrosSP.p_IdTipoCircular         	=  pc1.IdTipoCircular;
			
			//Especificos
			if(pc1.HabFecTermino===false){
				pc1.FecTermino=pc1.FecInicio;
			};
			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pc1.FecInicio);
			parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pc1.FecTermino);
			
			
			//Tipo Listas 
			parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pc1.IdTipoDocumento;
			//parametrosSP.p_Esquema 					= pc1.Esquema;
			//parametrosSP.p_Version 					= util.dateInicioDia($scope.SesionFechaSistema);//evaluar en el SP
			//parametrosSP.p_Version 					= parametrosSP.p_Version.getFullYear();
			parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			
			//parametrosSP.p_IdTipoTarea 				= pc1.IdTipoTarea;
			parametrosSP.p_CreaParam 				= 'S';
			
			var fd = util.dateSPdate(pc1.FecInicio);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
			
			var fh = util.dateSPdate(pc1.FecTermino);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaHasta',Valor : fh});
			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CrearSolicitudCircular(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		
		

		
		pc1.visorCircular = function() {
			if(pc1.HabFecTermino===false){
				pc1.FecTermino=pc1.FecInicio;
			};
			
			var fd = util.dateSPdate(pc1.FecInicio);
			var fh = util.dateSPdate(pc1.FecTermino);
			
			var paramServicio = {};
			paramServicio.p_TipoDocumento = pc1.IdTipoDocumento;
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
					bcsVisorCircularConfig.getDataSource(respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_ESQUEMA , respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION, pc1.IdTipoDocumento, 
							function (){
								var tablaParametros = [];
								
								tablaParametros.push({TipoParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
								
								
								tablaParametros.push({TipoParametro : 'fechaDesde',Valor : fd});
								
								
								tablaParametros.push({TipoParametro : 'fechaHasta',Valor : fh});
								
								bcsComunes.ventanaAbrir("VisorCircular", {
								tablaParametros : tablaParametros,
								IdTipoDocumento : pc1.IdTipoDocumento
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
		
		$scope.$watch('pc1.FecInicio', function(newval,oldval){
			pc1.fnVersionEsquema();
		},true);
		
		$scope.pc1.clear();

	});
}());