angular.module('ModuloPlataforma').controller('BloqueoMasivoClientesCtrl',
function($scope,$translate,$modal,$log,$resource, bcsAlerta,OPCIONES_BCSALERTA,bcsComunes, bcsServicioPlataforma, servicioSociodeNegocio)
{
	$scope.data		= {};
	$scope.config 	= {bloquear:false};	
	var overlay 	= bcsComunes.overlay;
	var idVentana 	= bcsComunes.ventanaWinId($scope);
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.nombreArchivo 	= null;
	$scope.data.selectedTipoProceso = null;
	$scope.nombreProceso = "";

	$scope.data.tipoProceso = {
		placeholder		: "...",
		dataTextField	: "Nombre",
		dataValueField	: "IdAccion",
		filter			: "contains",
		autoBind		: false,
		minLength		: 1,
		dataSource		: [
			{ Nombre : "BLOQUEO", IdAccion : "BLO" },
			{ Nombre : "DESBLOQUEO", IdAccion : "ACT" }
		],
		clearButton		: false,
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
   };

	$scope.data.oConfiguracionTipoBloqueoDesbloqueoCliente={
		tipo : 'combobox-tipo-bloqueo-socio-negocio',
		configuracion : {},
		filtros : [ 
			{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa},
			{ field: "IdTipoEntidad", operator: "eq", value: "CLIENTE"},
			{ field: "IdAccion", operator: "eq", value: "BLO"}
		],
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoBloqueoDesbloqueoPortafolio={
		tipo : 'combobox-tipo-bloqueo-socio-negocio',
		configuracion : {},
		filtros : [ 
			{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa},
			{ field: "IdTipoEntidad", operator: "eq", value: "PORTAFOLIO"},
			{ field: "IdAccion", operator: "eq", value: "BLO"}
		],
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
		
	$scope.cargarArchivo = function(){
		var codigoArchivo = 'ARCHIVO_BLOQUEO_DESBLOQUEO_CLIENTES_MASIVO';
		if($scope.SesionLocale=='PE') codigoArchivo = 'ARCHIVO_BLOQUEO_CLIENTES_MASIVO_PE';
		var datosArchivo = {codigoArchivo: codigoArchivo};
	 	var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);
		var promesa=bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){
			if(data.fileData && data.fileData.length > 0){
			 	$scope.data.clientes = data.fileData;
				$scope.nombreArchivo = data.fileName;
			}else{
				$scope.config.bloquear = true;
			}
		});
	};
	
	$scope.$watch('data.selectedTipoProceso', function(newval, oldval){

		if(newval) {

			$scope.data.oConfiguracionTipoBloqueoDesbloqueoPortafolio.idActual = false,
			$scope.data.oConfiguracionTipoBloqueoDesbloqueoCliente.idActual = false;
			
			$scope.nombreProceso = ($scope.data.selectedTipoProceso == 'BLO') ? "bloquear" : "desbloquear";

			$scope.data.oConfiguracionTipoBloqueoDesbloqueoCliente.filtros.find(function(v) {
				return v.field == 'IdAccion';
			  }).value = $scope.data.selectedTipoProceso;
			
			$scope.data.oConfiguracionTipoBloqueoDesbloqueoPortafolio.filtros.find(function(v) {
				return v.field == 'IdAccion';
			}).value = $scope.data.selectedTipoProceso;
			
		}

	},true);

	var resourceBloqueoPE = $resource("",{},{'CallBloqueoClientesMasivos':{method:"POST",url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=20020"}});
	
	var doPostCL = function(){
		var parametros= {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_Accion: $scope.data.selectedTipoProceso,
			p_Registros: []
		};

		$.each($scope.data.clientes,function(key,value) {
			var aux = {
				TipoDocumentoIdentidad : value.TIPODOCUMENTO || "",
				DocumentoIdentidad : value.DOCUMENTOIDENTIDAD || "",
				CodigoPortafolio : value.CODIGOPORTAFOLIO || "",
				TipoCambioEstadoCliente : value.MOTIVOCLI || "",
				TipoCambioEstadoPortafolio : value.MOTIVOPOR || "",
			};
			parametros.p_Registros.push(aux);
		});

		servicioSociodeNegocio.BloqueoDesbloqueoMasivoClientesPortafolios(parametros,
			function(respuesta) {

				var operaciones = [];

				$.each(respuesta.P_OUT_RESULTADO,function(key,value) {
					operaciones.push({
						id: value.Id,
						resultado: $translate.instant(value.Resultado),
						mensaje: value.Mensaje
					});
				});

				if( respuesta.P_OUT_ERRORESCONTADOR > 0 ) {
					$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
						operaciones.push({
							id: "",
							resultado: value.Codigo,
							mensaje: value.Descripcion
						});	
					});
				}
				 
				var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
					tituloVentana : $translate.instant("UI_BLOQUEO_DESBLOQUEO_MASIVO_CLIENTES"), 
					nombreEntidad : $translate.instant("UI_ENTIDAD"), 
					operaciones : operaciones   
				  },false,false,$scope.idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);

			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.IdVentana);
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
			}
		);
		
	};
	var doPostPE = function(){
		var datosBloqueo = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_SimularOperacion:'N',
			p_Accion:'BLO',
			p_Registros: [],
		};
		$.each($scope.data.clientes,function(key,value) {
			var aux = {
				TipoPersona:value.TIPOPERSONA || "",
				TipoDocumentoIdentidad:value.TIPODOCUMENTO || "",
				DocumentoIdentidad:value.DOCUMENTOIDENTIDAD || "",
				CodigoPortafolio: value.CODIGOPORTAFOLIO || 0,
				TipoCambioEstadoCliente:value.MOTIVOBLOCLI || "",
				TipoCambioEstadoPortafolio:value.MOTIVOBLOPOR || "",
			};
			datosBloqueo.p_Registros.push(aux);
		});
		var lepost = resourceBloqueoPE.CallBloqueoClientesMasivos(datosBloqueo);
		lepost.$promise.then(function(respuesta){
			var operaciones = [];
			$.each(respuesta.P_OUT_OPERACIONES_REALIZADAS,function(key,value) {
				operaciones.push({
					id: value.Entidad, 
					resultado: (value.Resultado==="EXITO")?$translate.instant('UI_EXITO'):$translate.instant('UI_ERROR'), 
					mensaje: value.Entidad+":"+value.Id+". "+value.Descripcion}
				);
			});
			var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
	            tituloVentana : $translate.instant("UI_BLOQUEO_MASIVO_CLIENTES"), 
	            nombreEntidad : $translate.instant("UI_ENTIDAD"), 
	            operaciones : operaciones   
	          },false,false,$scope.idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(respuesta){
			console.log(respuesta)
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.bloquearDesbloquear = function(){
		var count = 0;
		$.each($scope.data.clientes, function(key, val){
			if(val.MensajeValidacion==="OK"||val.MENSAJEVALIDACION==="OK"){
				if(val.MOTIVOCLI==undefined || val.MOTIVOCLI=="") val.MOTIVOCLI = $scope.data.oConfiguracionTipoBloqueoDesbloqueoCliente.idActual;
				if(val.MOTIVOPOR==undefined || val.MOTIVOPOR=="") val.MOTIVOPOR = $scope.data.oConfiguracionTipoBloqueoDesbloqueoPortafolio.idActual;	
			}
			count++;
			delete val.MensajeValidacion;
		});
		var t = bcsAlerta.crear("PREGUNTA","¿Está Seguro que desea "+ $scope.nombreProceso + ": " + count+" cliente(s) y su(s) portafolio(s)?");
		t.result.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,true);
			if (result===OPCIONES_BCSALERTA.ACEPTAR){			
				if($scope.SesionLocale=='PE'){
					doPostPE();
				}else{
					doPostCL();	
				} 
			}else{
				bcsComunes.ventanaOverlay(idVentana,false);
			}
		});		
	};

	$scope.limpiarDatos = function(){
		$scope.data.oConfiguracionTipoBloqueoDesbloqueoPortafolio.idActual = false,
		$scope.data.oConfiguracionTipoBloqueoDesbloqueoCliente.idActual = false;
		$scope.data.clientes="";
		$scope.nombreArchivo = null;
		$scope.data.selectedTipoProceso = null;
	};

	function changeHeight(height) {
        var firstHeightCrear = $('#Win_BloqueoMasivoClientes');
        firstHeightCrear.css('height', height);
      }

	$scope.$watch('nombreArchivo', function(newValue, oldValue) {
	if ( $scope.nombreArchivo != null ) {
		changeHeight('251px'); 
	} else {
		changeHeight('233px');
	}
	});

});	
