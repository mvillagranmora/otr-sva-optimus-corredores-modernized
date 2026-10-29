(function() {
	angular.module('ModuloNormativa').controller('PanelCartasCtrl',function($scope,$timeout,FileUpload,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa,bcsConfiguracionCircularizacion){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
	    pc1.TipoArchivo			= 'pdf';
	    pc1.IdVersionEsquema 	= $scope.solCir.IdVersionEsquema;
		
		pc1.IdTipoDocumento = 'CIRCNomina';
		pc1.salidaMensaje=''; 
		var parametrosSP = {};
		$scope.dataTemplate	= {};
		
		//pc1.dataParametro	= {};
		pc1.control	= {};
		pc1.control.TodosCli=true;
		var ListadoRuts = [];
		$scope.pc1.envioFecha = function(){
			var date = '';
			date= util.dateSPdate($scope.solCir.Periodo);
			pc1.Periodo= date.substring(0,4)  + date.substring(5,7);
		};
		/*********************************************
		 * DATA SOURCE
		 ********************************************/
		pc1.DatosDataSource = bcsConfiguracionCircularizacion.dataSourceParametros();
		$scope.pc1.envioFecha();
		var filterDatos = [];
			filterDatos.push({field: "IdEstado", operator: "eq", value: "ACT"});
			//filterDatos.push({field: "Periodo", operator: "eq", value: pc1.Periodo});
			filterDatos.push({field: "Periodo", operator: "eq", value: ""});
		pc1.DatosDataSource.query({filter: filterDatos}).then(function(e){
				console.log(pc1.DatosDataSource.data());
				cargarDatosParametros(pc1.DatosDataSource.data());
				
		});

		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		pc1.oConfiguracionSocioNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
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
		function cargarDatosParametros(arrayDatosParametros) {
	
				if (arrayDatosParametros.length != 0){
										
							for (i = 0; i<arrayDatosParametros.length; i++) {
								switch (arrayDatosParametros[i].Codigo){
									case "MERCADO":
										switch (arrayDatosParametros[i].Valor){
											case "registroRV":
												pc1.control.registroRV = (arrayDatosParametros[i].Valor == 'registroRV') ? true: false;
												break;
											case "registroRF":	
												pc1.control.registroRF = (arrayDatosParametros[i].Valor == 'registroRF') ? true: false;
												break;
											case "registroIIF":	
												pc1.control.registroIIF = (arrayDatosParametros[i].Valor == 'registroIIF') ? true: false;
												break;
											case "registroFFMM":	
												pc1.control.registroFFMM = (arrayDatosParametros[i].Valor == 'registroFFMM') ? true: false;
												break;												
										};
									break;
									case "TIPOOPER":
										switch (arrayDatosParametros[i].Valor){
											case "registroPACTOS":
												pc1.control.registroPACTOS = (arrayDatosParametros[i].Valor == 'registroPACTOS') ? true: false;
											break;												
											case "registroSIMULTANEA":
												pc1.control.registroSIMULTANEA = (arrayDatosParametros[i].Valor == 'registroSIMULTANEA') ? true: false;												
											break;
											case "registroVENTACORTA":
												pc1.control.registroVENTACORTA = (arrayDatosParametros[i].Valor == 'registroVENTACORTA') ? true: false;												
											break;
											case "registroFORWARD":
												pc1.control.registroFORWARD = (arrayDatosParametros[i].Valor == 'registroFORWARD') ? true: false;
											break;
											case "registroPRESTAMOS":
												pc1.control.registroPRESTAMOS = (arrayDatosParametros[i].Valor == 'registroPRESTAMOS') ? true: false;
											break;
										};
								    break;
								   	case "MONTO":
										switch (arrayDatosParametros[i].Valor){
											case "registroMONTOCOMPROM":
												pc1.control.registroMONTOCOMPROM = (arrayDatosParametros[i].Valor == 'registroMONTOCOMPROM') ? true: false;
											break;
										};
								    break;
									case "CLIENTE":
										switch (arrayDatosParametros[i].Valor){
											case "filtraContratoCustodia":
												pc1.control.filtraContratoCustodia = (arrayDatosParametros[i].Valor == 'filtraContratoCustodia') ? true: false;
											break;
											case "filtraIncluirClientesSinCustodia":
												pc1.control.filtraIncluirClientesSinCustodia = (arrayDatosParametros[i].Valor == 'filtraIncluirClientesSinCustodia') ? true: false;
											break;
										};
								    break;
							   };					
							};
							
					};
					pc1.controlOriginal = $.extend({},pc1.control);
		};
				
		$scope.pc1.clear = function(){

			pc1.UIModo=$scope.solCir.params.PrevUIModo;
		};
		

		
		var successCrearSolicitud = function(respuesta)
		{	
			//bcsComunes.ventanaOverlay(pc1.idVentana,true);
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				pc1.salidaMensaje=$translate.instant("UI_MODIFICACION_INGRESO_EXITO");
				
				bcsComunes.msg("info","info", pc1.salidaMensaje);
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
			}
			else
			{	
				
				pc1.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
			/*	bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
										conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana);
			*/
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",pc1.salidaMensaje);
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			//bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
			//	conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana),true,true;
			
			bcsComunes.ventanaData(pc1.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pc1.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	

			pc1.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
								 conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",pc1.salidaMensaje);
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
		
		 pc1.guardarParametros = function() {
			parametrosSP= {}; 
			$scope.pc1.envioFecha();
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
	    	
			parametrosSP.p_UsaReglasNegocio 		=  'N';
			//parametrosSP.p_Periodo					=	pc1.Periodo;
			parametrosSP.p_Periodo					=	'';
			//Especificos
			fnOntenerParametros();
					
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsComunes.ventanaOverlay(pc1.idVentana,true);
			/*
			bcsServiciosNormativa.CircularizacionGuardarParametros(parametrosSP,successCrearSolicitud,errorCrearSolicitud).$promise.then(function(respuesta){
				bcsComunes.ventanaOverlay(pc1.idVentana, false);
				var msg = new bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
				conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana,true);
				promesa.then(function(msg){
							//bcsComunes.ventanaCerrar(ventanaSolicitarCircularizacion.idVentana);
				var r =msg;
				        	
						 });
				
			});
			*/
			
			bcsServiciosNormativa.CircularizacionGuardarParametros(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	       
			 
			 
		 };
			
		 pc1.solicitarCircularizacion = function() {
			 
			if ($scope.solCir.Periodo!==undefined) { 
		    	parametrosSP= {};
		    	//Comunes
		    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
		    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
		    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
		    	$scope.pc1.envioFecha();
		    	
		    	
				//parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol;
				
				parametrosSP.p_Periodo				 	=  pc1.Periodo;
				
				
				//Especificos
				if (pc1.oConfiguracionSocioNegocio.seleccionado){
					parametrosSP.p_IdCliente				=	pc1.oConfiguracionSocioNegocio.seleccionado.IdCliente;
				};
				
				if (pc1.control.TodosCli==true)
				{
					parametrosSP.p_TodosRut=	1;
				};
				if (pc1.control.FiltrosRut!=undefined)
				{
					fnOntenerRutMasivo();
				};
					
				
				//Tipo Listas 
				parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
	
				//Creacion Documento
				parametrosSP.p_IdTipoDocumento 			= pc1.IdTipoDocumento;
	
				//Tarea Programada		
				/*
				var fd = util.dateSPdate(pc1.FecInicio);
				params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
				var fh = util.dateSPdate(pc1.FecTermino);
				params.p_TablaParametrosaux.push({NombreParametro : 'fechaHasta',Valor : fh});
				*/
				parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
				
				bcsComunes.msg('info', 'Carga', 'En proceso...');
				bcsServiciosNormativa.CircularizacionCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			};
	      
		};
		
		function fnOntenerRutMasivo() {

			if(pc1.control.FiltrosRut!=undefined)
			{
				
				var RutsCli = SplitTxt(pc1.control.FiltrosRut.fileContent,true);
				//Recorremos las filas seleccionadas
				var v_iter ;
				 v_iter=1;
				 
				 if(RutsCli.length>0){
					$.each(RutsCli,function(key,value){

						ListadoRuts.push(
						 {
							  Rut: RutsCli[key]
															 
						 });
						 v_iter=v_iter+1;
				   });
				 };
			};
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
				//	if (pc1.control[value.nombre] !== pc1.controlOriginal[value.nombre])
						if (pc1.control[value.nombre]===true)
						{
							parCodigo = value.codigo;
							registroCambio.push({Codigo :parCodigo , Valor :value.nombre });
						};
				}			
			});
			
			parametrosSP.p_RegistroParametro = registroCambio;
			//parametrosSP.push(registroCambio);
			
		};	

		function SplitTxt(csvString,skipFirstLine) {
		
			var csvArray   = [];
			csvString =	csvString + '';
			var csvRows    = csvString.split(/\n/);

			for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
		
				var rowArray  = csvRows[rowIndex].split(';');		

			for(var propIndex = 0; propIndex < rowArray.length; ++propIndex){
				
					var propValue =   rowArray[propIndex].replace(/^"|"$/g,'') || '';

				csvArray.push(propValue);
				}
		
			}
		return csvArray;
					
		};

		
		$scope.$watch('pc1.oConfiguracionSocioNegocio.seleccionado',function(newval,oldval){
		if (pc1.oConfiguracionSocioNegocio.seleccionado)
			{	
				if (pc1.oConfiguracionSocioNegocio.seleccionado.IdCliente)
				{
					pc1.control.TodosCli=false;
				}else{
					pc1.control.TodosCli=true;
				};
			};
				
		});

		$scope.$watch('pc1.control.FiltrosRut',function(newval,oldval){
			if (pc1.control.FiltrosRut!=undefined)
			{
				pc1.control.TodosCli=false;
			}else{
				pc1.control.TodosCli=true;
			};
				
		});
		
		
	
		$scope.pc1.clear();

	});
}());

angular.module('ModuloNormativa').directive('fileReader', function() {
  return {
    scope: {
      fileReader:"="
    },
    link: function(scope, element) {
      $(element).on('change', function(changeEvent) {
        var files = changeEvent.target.files;
        if (files.length) {
          var r = new FileReader();
          r.onload = function(e) {
              var contents = e.target.result;
              scope.$apply(function () {
                scope.fileReader = contents;
              });
          };
          
          r.readAsText(files[0],'iso-8859-1');
        }
      });
    }
  };
});