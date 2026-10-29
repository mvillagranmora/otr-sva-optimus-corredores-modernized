angular.module('ModuloFFM').controller('ExportarBatchFFMCtrl',
function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsServicioPlataforma, bcsAlerta,OPCIONES_BCSALERTA)
{	


	$scope.filtros  = {};
	$scope.winID    = bcsComunes.ventanaWinId($scope);
	$scope.paramHoraEjecucionProceso = "";
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	

	
	//---------------------------------------------------
	// Obtiene valor del parametro que especifica hora de ejecucion del proceso
	//---------------------------------------------------
	if ($scope.SesionIdEmpresa)
	{
		
			var dsParametros = new bcs.DataSource({
				type: "odata",
				
				definition 	: {			
					serverFiltering 	: true,
					transport 			: {
											read : {
												 url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3088", // 3088: BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarParametroFondoMutuo
												 dataType : "json",
											  }
										  },
					
					filter: [
					           { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
					           { field: "NombreTipoParametro", operator: "eq", value: 'HORA_EJECUCION_BATCH_EXPORTAR_SOLAPORES' }
					        ],
										  
					schema 				: {
							model: {
								fields : {
									IdEmpresa          	: {type: "number"},
									DocumentoIdentidad 	: {type: "string"},									
									IdEmpresa          	: {type: "number"},
									IdParametro         : {type: "number"},
									NombreTipoParametro : {type: "string"},
									ValorParametro      : {type: "string"},
									NombreParametro     : {type: "string"},
									IdEstado          	: {type: "string"},
									NombreEstado        : {type: "string"}
								}
							}
						}
										  
				},
			});
			
			dsParametros.fetch().then(function(){
				 if (dsParametros.data().length <= 0 ) 
				 {
					 console.log("NO EXISTE PARAMETRO CON HORA DE EJECUCION DEL PROCESO. POR LO TANTO SE EJECUTARA AHORA.");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						 $scope.paramHoraEjecucionProceso = dsParametros.data()[0].ValorParametro;
						return true;
					 });
				 }
		    });
	};
	
	
	
	$scope.cargaInicial = function()
	{
		$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
	
	};
	
	$scope.cargaInicial();
	
	
	// --------------------------------------------------------------------------
	//Genera en forma Batch un archivo csv con las solicitudes de Aporte Rescate
	// --------------------------------------------------------------------------
	$scope.exportar = function()
	{			

		// -----------------------------------------------------------------------------
		// 1. Se calcula FechaHora (segundos de retardo) para la ejecucion del proceso
		
        var segundosRetardo = 0;     // Segundos de retardo en la ejecucion del proceso

        if ($scope.paramHoraEjecucionProceso != "" && $scope.paramHoraEjecucionProceso != null)
        {
            // Se arma horario de ejecucion
			var fechaActual = new Date();
			var anno = fechaActual.getFullYear();
			var mes  = fechaActual.getMonth()
			var dia  = fechaActual.getDate()
		
        	// Se desglosa parametro (HH:MM) para obtener hora y minuto de ejecucion
			var arrHoraEjecucion  =  $scope.paramHoraEjecucionProceso.split(":");
			var hora   = arrHoraEjecucion[0];
			var minuto = arrHoraEjecucion[1];
				
			var fechaNueva = new Date(anno, mes,dia, hora, minuto);

			//Obtiene diferencia en segundos entre fechas
			segundosRetardo = Math.trunc((fechaNueva-fechaActual)/1000);//La diferencia se da en milisegundos por eso se divide en 1000

			//Si la FechaHora de ejecucion es menor a la FechaHora actual, se incrementa un dia la fecha de ejecucion
			if (segundosRetardo < 0)
			{
				fechaNueva.setDate(fechaNueva.getDate() + 1);
				segundosRetardo = Math.trunc((fechaNueva-fechaActual)/1000);
			}
        }

        
        // -----------------------------------------------------------------------------
		// 2. Se inscribe proceso Batch (segun FechaHora calculada anteriormente)

		var entradaPrograma 		= {};					
		entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
		entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
		entradaPrograma.p_IdModulo    		= 'FMM';
		entradaPrograma.p_IdAgenda    		= 0;    
		entradaPrograma.p_IdTipoTarea   	= 'GENERAR_ARCHIVO_SOLICITUD_APORTE_RESCATE';
		entradaPrograma.p_Segundos      	= segundosRetardo;			
		entradaPrograma.p_CreaParam     	= 'S';
	    

		entradaPrograma.p_TablaParametros =
		[
		   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
		   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
		   {NombreParametro	: 'IdModulo', 	  		Valor: 'FMM'},	
		   {NombreParametro	: 'FechaDesde', 	  	Valor: util.dateSPdate($scope.filtros.fechaDesde) || '0001-01-01'},
		   {NombreParametro	: 'FechaHasta', 	  	Valor: util.dateSPdate($scope.filtros.fechaHasta) || '0001-01-01'},  
		   
		   {NombreParametro	: 'IdTipoMovimientoFondoMutuo', Valor: $scope.parametros.p_IdTipoMovimientoFondoMutuo},
		   {NombreParametro	: 'Estado', 	  				Valor: $scope.parametros.p_Estado},		   
		   {NombreParametro	: 'IdEmisor', 	  				Valor: $scope.parametros.p_IdEmisor},		   
		   {NombreParametro	: 'IdSerieFondo', 	  			Valor: $scope.parametros.p_IdSerieFondo	},
		   {NombreParametro	: 'IdCliente', 	  				Valor: $scope.parametros.p_IdCliente},
		   {NombreParametro	: 'IdProductoAgrupador', 	  	Valor: $scope.parametros.p_IdProductoAgrupador},
		   {NombreParametro	: 'IdPortafolio', 	  			Valor: $scope.parametros.p_IdPortafolio},		   
		   {NombreParametro	: 'TipoSolicitud', 	  			Valor: $scope.parametros.p_TipoSolicitud},
		   {NombreParametro	: 'IdMoneda', 	  				Valor: $scope.parametros.p_IdMoneda}	   
		];

		

		console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
					
		bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
			function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
					Programa = respuesta.P_OUT_IDPROGRAMA;					
					if (Programa.length > 0)
					    idPrograma = Programa[0].IdPrograma;								
							
					bcsComunes.ventanaMsg({
						mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas"							
					},$scope.winID);						
				}
				else
				{						
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")							
					},$scope.winID);
				}
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);					
			}
		);		

	};

	$scope.cerrar = function()
	{
		bcsComunes.ventanaCerrar($scope.winID);
	};
	



});