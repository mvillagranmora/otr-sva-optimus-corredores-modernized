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

(function() {
	angular.module('ModuloNormativa').controller('PanelINFTRASEILCtrl', 
			function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pse = this;
		pse.idVentana = bcsComunes.ventanaWinId($scope); //ID del Formulario
		pse.domVentana = $("[ventanaid='" + $scope.idVentana + "']");
		pse.habilitado = true;
		pse.IdTipoDocumento = 'INFTRASEIL';
		
		pse.control	= {};
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var TipoRegistro = [];
		pse.Filtros = {};
		
		var p_TablaParametrosAux; //parametros generales que puede ver el jobmanager
		var params = {
				p_TablaParametrosAux : [
				    {	Tipo			: 'BATCH',
				    	NombreParametro : 'idUsuario',
				    	Valor : $scope.SesionIdUsuario
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'schema',
				    	Valor : Model.getSchema()
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'grabar',
				    	Valor : $scope.solCir.grabar
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'reemplazar',
				    	Valor : $scope.solCir.reemplazar
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'separadorListas',
				    	Valor : $scope.solCir.separadorListas
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'simboloDecimal',
				    	Valor : $scope.solCir.simboloDecimal
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'simboloFecha',
				    	Valor : $scope.solCir.simboloFecha
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'aceptarFechasNull',
				    	Valor : $scope.solCir.aceptarFechasNull
				    },]};
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		var parametrosSP = {};
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		function SplitTxt (csvString, skipFirstLine) {
			var csvArray = [];
			csvString = csvString + '';
			var csvRows = csvString.split(/\n/);
			for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
				var rowArray = csvRows[rowIndex].split(';');
				
				for (var propIndex = 0; propIndex < rowArray.length; ++propIndex){
					var propValue =  rowArray[propIndex].replace(/^"|"$/g,'') || '';
					csvArray.push(propValue);
				}
			}
			return csvArray;
		};
		
		$scope.pse.clear =  function(){
			pse.FecInicio 		= util.dateInicioDia($scope.SesionFechaSistema);
			pse.FecTermino 		= util.dateInicioDia($scope.SesionFechaSistema);
			
			pse.Filtros.Nemo = '';
			//pse.oConfiguracionSocioNegocio.seleccionado = 0;
			
			pse.control.registroRV = true;
			pse.control.registroRF = true;
			pse.control.registroIIF = true;
			pse.control.registroFFMM = true;
			pse.control.registroSPOT= true;
			
		};
		
		var successCrearSolicitud = function(respuesta)
		{	
			const respuestaParseada = util.parseServerResponse(respuesta);
			const { erroresContador = 0, errores = [] } = respuestaParseada;
			bcsComunes.setConflictos(respuestaParseada);

			if (erroresContador === 0) {
				bcsComunes.msg("info","info",'Solicitud creada con éxito');
				IdSolicitudCircular = respuesta.P_OUT_RESULTADO;
			}
			else
			{
				const textoError =  'Solicitud no fue creada';
				const llaveError = errores[0]?.Descripcion || errores[0]?.Codigo || '';
				const mensajeError = llaveError ? `${textoError} - ${$translate.instant(llaveError)}` : textoError;
				bcsComunes.msg("error","error", mensajeError);
				errores.forEach((error, index) => {
					const detalleError = error.Descripcion || error.Codigo || 'Sin descripción';
					bcsComunes.log().info(`Error ${index + 1}: ${detalleError}`);
				});
			}	
			bcsComunes.ventanaOverlay(pse.idVentana, false);
			bcsComunes.ventanaData(pse.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pse.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pse.idVentana, false);
		};
		
		function pad (n, length) {
		    var  n = n.toString();
		    while(n.length < length)
		         n = '0' + n;
		return n;
		};
		
		pse.solicitar = function() {
			//$scope.pse.envioFecha();
			pse.habilitado = false;
			parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
			parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
	    	//parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
	    	
	    	//Especificos
	    	parametrosSP.p_FechaInicio 				=  util.dateSPdate(pse.FecInicio);
			parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pse.FecTermino);
			
			//Tipo Listas 
			//parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			//parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			//parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			//parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pse.IdTipoDocumento;
			//parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;
			
			//Tarea Programada	
			
			
			
			
			var fd = util.dateSPdate(pse.FecInicio);
			params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'fechaDesde',Valor : fd});
			
			var fh = util.dateSPdate(pse.FecTermino);
			params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'fechaHasta',Valor : fh});
			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
			
			if (fh < fd){
	 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
				pse.habilitado = true;
	 			return;
	 		};
			
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : fd });
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : fh });			
				
			
			
			if (pse.oConfiguracionSocioNegocio.seleccionado){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : pse.oConfiguracionSocioNegocio.seleccionado.IdCliente });					
			};
			
			if (pse.Filtros.Nemo){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor : pse.Filtros.Nemo });					
			};
			
			
			if (pse.Filtros.Oficio){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'OFI' , Valor : pse.Filtros.Oficio });
				var codigo =pad(pse.Filtros.Oficio,3);
				var nomarch=  'DIN_' +  pse.FecTermino.getFullYear() + '_' + codigo + '_TRA';
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMSAL' , Valor : nomarch});
				//parametrosSP.p_TablaParametros.push({Tipo: 'BATCH',NombreParametro :'NOMSAL' , Valor : nomarch});
			};			
			
			fnObtenerParametrosEspecificos()
			
			//parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TIPINF' , Valor : pcn.opcionFiltro });
			
			
			
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
		};
		
		function fnObtenerParametrosEspecificos() {
			
			//OBTENCION DE LISTADO DE RUT
			if(pse.FiltrosRut!=undefined)
			{
				var csvToArray;
				var RutsCli = SplitTxt(pse.FiltrosRut.fileContent,true);
					 
				//Recorremos las filas seleccionadas
				var v_iter ;
				v_iter=1;
				 
				 if(RutsCli.length>0){
					 $.each(RutsCli,function(key,value){
						 
						 parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT_MAS' , Valor : RutsCli[key] }); 
		
						 v_iter=v_iter+1;
					 });
				 }
			
			};
			
			//Obtiene datos de archivo con listado de Nemos
			var ListadoNemos = [];
			if(pse.FiltrosNemo!=undefined)
			{
				var Nemos = SplitTxt(pse.FiltrosNemo.fileContent,true);
					 
				//Recorremos las filas seleccionadas
				var v_iter ;
				v_iter=1;
				 
				if(Nemos.length>0){
					$.each(Nemos,function(key,value){

						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO_MAS' , Valor : Nemos[key] });
						
						v_iter=v_iter+1;
					}); 
				};
		
			};
			//OPCION TODOS
			if(pse.control.TodosCli===true){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_CLI' , Valor : 'S' });
			
			}
			
			if(pse.control.TodosNemo===true){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_NEMO' , Valor : 'S' });
			}
			
			//CLASIFICACION DE INSTRUMENTO
			if(	pse.control.registroRV && pse.control.registroRF &&	pse.control.registroIIF &&	pse.control.registroFFMM && pse.control.registroSPOT)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_CLAINS' , Valor : 'S' });
				
			}else{
				if (pse.control.registroRV){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'RV' });};
				if (pse.control.registroRF){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'RF' });};
				if (pse.control.registroIIF){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'IF' });};
				//if (pse.control.registroFFMM){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'FM' });};
				if (pse.control.registroSPOT){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'SP' });};
			};
			
			// Duplicados y vacios en parametrosSP.p_TablaParametros
			const setDuplicados = new Set();
			parametrosSP.p_TablaParametros = parametrosSP.p_TablaParametros.filter(({ Tipo, NombreParametro, Valor }) => {
				if (Tipo !== 'SOLICITUD') return true;
				if (Valor == null || !String(Valor).trim()) return false;
				const identificador = `${NombreParametro}-${Valor}`;
				return !setDuplicados.has(identificador) && setDuplicados.add(identificador);
			});
		}
		
		$scope.$watch('pse.oConfiguracionSocioNegocio.seleccionado',function(newval,oldval){
		if (pse.oConfiguracionSocioNegocio)
			{			
				if (pse.oConfiguracionSocioNegocio.seleccionado.IdCliente)
				{
					pse.control.TodosCli=false;
				}else{
					pse.control.TodosCli=true;
				};
			};					
		});
							
		$scope.$watch('pse.FiltrosRut',function(newval,oldval){
			if (pse.FiltrosRut!=undefined)
			{
				pse.control.TodosCli=false;
			}else{
				pse.control.TodosCli=true;
			};
				
		});
		
		
		$scope.$watch('pse.FiltrosNemo',function(newval,oldval){
			if (pse.FiltrosNemo!=undefined)
			{
				pse.control.TodosNemo=false;
			}else{
				pse.control.TodosNemo=true;
			};
				
		});
		
		$scope.pse.clear();
		
	});
}());