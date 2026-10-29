(function() {
angular.module('ModuloCustodia').controller('PanelCartasCertifCtrl',function($scope,$translate,Model,bcsComunes,bcsServiciosCustodia,bcsConfiguracionCertificado){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pcCus = this;
		pcCus.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pcCus.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pcCus.IdTipoDocumento = 'CIRCNomina';
		pcCus.salidaMensaje=''; 
		var parametrosSP = {};
		$scope.dataTemplate	= {};
		
		pcCus.control	= {};

		pcCus.pantalla = {};
		pcCus.pantalla.modoEdicion = false;
		pcCus.botonGuardarActivado = false;



		/*********************************************
		 * DATA SOURCE
		 ********************************************/
		pcCus.DatosDataSource = bcsConfiguracionCertificado.dataSourceParametros();
		var filterDatos = [];
			filterDatos.push({field: "IdEstado", operator: "eq", value: "ACT"});
			filterDatos.push({field: "Periodo", operator: "eq", value: ""});
		pcCus.DatosDataSource.query({filter: filterDatos}).then(function(e){
				cargarDatosParametros(pcCus.DatosDataSource.data());
				
		});

		
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
			Valor : $scope.cerCif.grabar
		}, {
			NombreParametro : 'reemplazar',
			Valor : $scope.cerCif.reemplazar
		},{
			NombreParametro : 'separadorListas',
			Valor : $scope.cerCif.separadorListas
		},{
			NombreParametro : 'simboloDecimal',
			Valor : $scope.cerCif.simboloDecimal
		},{
			NombreParametro : 'simboloFecha',
			Valor : $scope.cerCif.simboloFecha
		},{
			NombreParametro : 'aceptarFechasNull',
			Valor : $scope.cerCif.aceptarFechasNull
		},]};
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		function cargarDatosParametros(arrayDatosParametros) {
	
				if (arrayDatosParametros.length != 0){
										
							for (i = 0; i<arrayDatosParametros.length; i++) {
								switch (arrayDatosParametros[i].Codigo){
									case "MERCADO":
										switch (arrayDatosParametros[i].Valor){
											case "registroRV":
												pcCus.control.registroRV = (arrayDatosParametros[i].Valor == 'registroRV') ? true: false;
												break;
											case "registroRF":	
												pcCus.control.registroRF = (arrayDatosParametros[i].Valor == 'registroRF') ? true: false;
												break;
											case "registroIIF":	
												pcCus.control.registroIIF = (arrayDatosParametros[i].Valor == 'registroIIF') ? true: false;
												break;
											case "registroFFMM":	
												pcCus.control.registroFFMM = (arrayDatosParametros[i].Valor == 'registroFFMM') ? true: false;
												break;												
										};
									break;
									case "TIPOOPER":
										switch (arrayDatosParametros[i].Valor){
											case "registroPACTOS":
												pcCus.control.registroPACTOS = (arrayDatosParametros[i].Valor == 'registroPACTOS') ? true: false;
											break;												
											case "registroSIMULTANEA":
												pcCus.control.registroSIMULTANEA = (arrayDatosParametros[i].Valor == 'registroSIMULTANEA') ? true: false;												
											break;
											case "registroVENTACORTA":
												pcCus.control.registroVENTACORTA = (arrayDatosParametros[i].Valor == 'registroVENTACORTA') ? true: false;												
											break;
											case "registroFORWARD":
												pcCus.control.registroFORWARD = (arrayDatosParametros[i].Valor == 'registroFORWARD') ? true: false;
											break;
											case "registroPRESTAMOS":
												pcCus.control.registroPRESTAMOS = (arrayDatosParametros[i].Valor == 'registroPRESTAMOS') ? true: false;
											break;
										};
								    break;
								   	case "MONTO":
										switch (arrayDatosParametros[i].Valor){
											case "registroMONTOCOMPROM":
												pcCus.control.registroMONTOCOMPROM = (arrayDatosParametros[i].Valor == 'registroMONTOCOMPROM') ? true: false;
											break;
										};
								    break;
									case "CLIENTE":
										switch (arrayDatosParametros[i].Valor){
											case "filtraContratoCustodia":
												pcCus.control.filtraContratoCustodia = (arrayDatosParametros[i].Valor == 'filtraContratoCustodia') ? true: false;
											break;
											case "filtraIncluirClientesSinCustodia":
												pcCus.control.filtraIncluirClientesSinCustodia = (arrayDatosParametros[i].Valor == 'filtraIncluirClientesSinCustodia') ? true: false;
											break;
										};
								    break;
							   };					
							};
							
					};
					pcCus.controlOriginal = $.extend({},pcCus.control);
		};

		
		var successCrearSolicitud = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				pcCus.salidaMensaje=$translate.instant("UI_MODIFICACION_INGRESO_EXITO");
				
				bcsComunes.msg("info","info", pcCus.salidaMensaje);
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
			}
			else
			{	
				
				pcCus.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");

				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",pcCus.salidaMensaje);
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}
			
			bcsComunes.ventanaData(pcCus.idVentana, respuesta);
			bcsComunes.ventanaOverlay(pcCus.idVentana,false);
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	

			pcCus.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pcCus.salidaMensaje, 
								 conflictos : util.parseServerResponse(respuesta)}, pcCus.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",pcCus.salidaMensaje);
			bcsComunes.ventanaOverlay(pcCus.idVentana, false);
		};
		

		pcCus.guardarParametros = function() {
			parametrosSP= {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.cerCif.modulo;
	    	
			parametrosSP.p_UsaReglasNegocio 		=  'N';
			parametrosSP.p_Periodo					=	'';
			//Especificos
			fnOntenerParametros();
					
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsComunes.ventanaOverlay(pcCus.idVentana,true);
			
			bcsServiciosCustodia.CircularizacionGuardarParametros(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			fnEditarParametros();
		};
		
		function fnOntenerParametros() {
			var parCodigo ="";
			var campos = [{ nombre: "registroRV" 		 ,tipo : "bool",codigo: "MERCADO"}
						 ,{ nombre: "registroRF",tipo : "bool",codigo: "MERCADO"}
						 ,{ nombre: "registroIIF",tipo : "bool",codigo: "MERCADO"}
						 ,{ nombre: "registroFFMM",tipo : "bool",codigo: "MERCADO"}
						 ,{ nombre: "registroPACTOS",tipo : "bool",codigo: "TIPOOPER"}
						 ,{ nombre: "registroSIMULTANEA",tipo : "bool",codigo: "TIPOOPER"}
						 ,{ nombre: "registroVENTACORTA",tipo : "bool",codigo: "TIPOOPER"}
						 ,{ nombre: "registroFORWARD", tipo : "bool",codigo: "TIPOOPER"}
						 ,{ nombre: "registroPRESTAMOS", tipo : "bool",codigo: "TIPOOPER"}
						 ,{ nombre: "registroMONTOCOMPROM",tipo : "bool",codigo: "MONTO"}
						 ,{ nombre: "filtraContratoCustodia",tipo: "bool",codigo: "CLIENTE"}
						 ,{nombre: "filtraIncluirClientesSinCustodia",tipo: "bool",codigo: "CLIENTE"}
						 ];
			var registroCambio = [];
			$.each(campos,function(key,value) {
				if (value.tipo === 'bool') {
				//	if (pcCus.control[value.nombre] !== pcCus.controlOriginal[value.nombre])
						if (pcCus.control[value.nombre]===true)
						{
							parCodigo = value.codigo;
							registroCambio.push({Codigo :parCodigo , Valor :value.nombre });
						};
				}			
			});
			
			parametrosSP.p_RegistroParametro = registroCambio;			
		
		};
		
		pcCus.editarPCCus = function() {
			fnEditarParametros();
		};

		function fnEditarParametros() {
			pcCus.pantalla.modoEdicion = !pcCus.pantalla.modoEdicion;
			pcCus.botonGuardarActivado = !pcCus.botonGuardarActivado;
		};

	});
}());
