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
	angular.module('ModuloNormativa').controller('PanelInformeMensualRVCtrl',function($scope,$timeout,Model,Utils,$translate,FileUpload,bcsAlerta,OPCIONES_BCSALERTA,bcsComunes,$compile,bcsServiciosNormativa,bcsBlotterSolicitudCircular){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pcn = this;
		
		
		pcn.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(pcn.idVentana);
		$scope.SolicitudCircular = $.extend({}, parametros.SolicitudCircular);
		$scope.ParametrosSolicitud = $.extend({}, parametros.ParametrosSolicitud);
		pcn.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
	
		var vTipoRegistro	= [];
		var parametrosSP 	= {};
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		$scope.data ={
		oCircularConfiguracion : {},
		Filtros: {},
		modoLectura:false
		};
		
			
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

		$scope.pcn.clear = function(){
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

		function pad (n, length) {
		    var  n = n.toString();
		    while(n.length < length)
		         n = '0' + n;
		return n;
		};
		 
		 pcn.solicitar = function() {
	    	
			//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
	    	parametrosSP.p_FechaInicio              =  util.dateSPdate(pcn.FecInicio);
			fnOntenerTipoRegistro();
			if (pcn.FecInicio==null || pcn.FecInicio==undefined)
				{
					pcn.FecInicio=util.dateInicioDia('0001-01-01');
				};
			if (pcn.FecTermino==null || pcn.FecTermino==undefined)
				{
					pcn.FecTermino=util.dateInicioDia($scope.SesionFechaSistema);
				};
			
			var fd = util.dateSPdate(pcn.FecInicio);	
			var fh = util.dateSPdate(pcn.FecTermino);
			if (fh < fd){
    	 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
    	 			return;
    	 	};	      
			
      	    $.each(vTipoRegistro,function(key,value){
      	    	bcsComunes.msg('info', 'Carga', 'En proceso...');
				
			//	fnOntenerParametrosComunes();
				
			//Creacion Documento
  			parametrosSP.p_IdTipoDocumento =  vTipoRegistro[key].IdTipoDocumento;
  			//parametrosSP.p_Esquema =  vTipoRegistro[key].Esquema;
  			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;

			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : util.dateSPdate(pcn.FecInicio) });
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : util.dateSPdate(pcn.FecTermino) });
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
      	    fnLimpiarParametros();
      	    });
			
			
	      
		};
		
		//Funcion que permite llenar los parametros comunes del servicio
		function fnOntenerParametrosComunes() {
			
		  params = {
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
					},]};
			
					parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;
		
					//Tarea Programada			
					//parametrosSP.p_IdTipoTarea 				= pcn.IdTipoTarea;
					parametrosSP.p_CreaParam 				= 'S';
					
					
					parametrosSP.p_FechaCreacion 			= $scope.SesionFechaSistema;
					parametrosSP.p_FechaPresentacion 		= $scope.SesionFechaSistema;
					parametrosSP.p_IdAgenda 				= 0;
					parametrosSP.p_Segundos 				= 0;
					
					parametrosSP.p_Oficio 					= '';
			
		};
		//Limpiar variables de parametros
		function fnLimpiarParametros() {
			parametrosSP			=	{};
			p_TablaParametrosaux	=	[];
			params 					= 	{};
		};
		//Funcion que permite llenar los parametros espcificos del servicio segun Tipo Documento
				
		//Funcion que permite llenar el arreglo de seleccion
		function fnOntenerTipoRegistro() {
			vTipoRegistro.push({IdTipoDocumento: 'INFINVRV',Esquema:'INFINVRV'});	
		};	
				
		if (parametros.SolicitudCircular){
			$scope.modoVisualizacion =  true;
			var filterDatos = [];
			filterDatos.push({field: "IdSolicitudCircular", operator: "eq", value: $scope.SolicitudCircular.IdSolicitudCircular});
			filterDatos.push({field: "TipoParametro", operator: "neq", value: "RUT"});
			filterDatos.push({field: "TipoParametro", operator: "neq", value: "NEMO"});
		
			var DatosDataSource = bcsBlotterSolicitudCircular.dataSourceParametrosSolicitudCircular();
			
			DatosDataSource.query({filter: filterDatos}).then(function(e){
				console.log(DatosDataSource.data());
				cargarDatosParametros(DatosDataSource.data(), parametros.SolicitudCircular);
				$scope.data.modoLectura = true;
			});
		}
		
		$scope.$watchGroup(['pcn.FecInicio','pcn.FecTermino'],function(newval,oldval){
			if( $scope.pcn.FecInicio && $scope.pcn.FecTermino){
				$scope.seleccionGrabar = true;
			} else {
				$scope.seleccionGrabar = false;
			}
			
			});
	});
	
	
	
}());




