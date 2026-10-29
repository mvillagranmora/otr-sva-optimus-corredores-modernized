angular.module('ModuloPlataforma').controller('ActivacionMasivaClientesCtrl',
function($scope,$translate,$modal,$log,$resource, bcsAlerta,OPCIONES_BCSALERTA,bcsComunes, bcsServicioPlataforma)
{
	$scope.data		= {};
	$scope.config 	= {bloquear:true};	
	var overlay 	= bcsComunes.overlay;
	var idVentana 	= bcsComunes.ventanaWinId($scope);
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	$scope.data.oConfiguracionTipoBloqueoCliente={
		tipo : 'combobox-tipo-bloqueo-socio-negocio',
		configuracion : {},
		filtros : [ 
			{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa},
			{ field: "IdTipoEntidad", operator: "eq", value: "CLIENTE"},
			{ field: "IdAccion", operator: "eq", value: "ACT" }],
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoBloqueoPortafolio={
		tipo : 'combobox-tipo-bloqueo-socio-negocio',
		configuracion : {},
		filtros : [ 
			{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa},
			{ field: "IdTipoEntidad", operator: "eq", value: "PORTAFOLIO"},
			{ field: "IdAccion", operator: "eq", value: "ACT" }],
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
		
	$scope.cargarArchivo = function(){
		var codigoArchivo = '';
		if($scope.SesionLocale=='PE'){
			codigoArchivo = 'ARCHIVO_DESBLOQUEO_CLIENTES_MASIVO_PE';
		}else{
			console.log("DESARROLLADOR: Funcionalidad implementada para proyecto Perú");
			return;
		}
		var datosArchivo = {codigoArchivo: codigoArchivo};
	 	var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);
		var promesa=bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){
			if(data.fileData && data.fileData.length > 0){
			 	$scope.data.clientes = data.fileData;
			 	$scope.validarCampos();
			}else{
			 $scope.config.bloquear = true;
			}
		});
	};
	
	$scope.$watch('data.oConfiguracionTipoBloqueoCliente.idActual', function(nval, oval){$scope.validarCampos();});
	$scope.$watch('data.oConfiguracionTipoBloqueoPortafolio.idActual', function(nval, oval){$scope.validarCampos();});
	
	$scope.validarCampos = function(){
		if($scope.data.clientes!="" && $scope.data.oConfiguracionTipoBloqueoCliente.idActual && $scope.data.oConfiguracionTipoBloqueoPortafolio.idActual)$scope.config.bloquear = false;
		else $scope.config.bloquear = true;
	};

	var resourceBloqueoCL = $resource("",{},{'CallBloqueoClientesMasivos':{method:"POST",url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1513"}});
	var resourceBloqueoPE = $resource("",{},{'CallBloqueoClientesMasivos':{method:"POST",url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=20020"}});
	
	var doPostCL = function(){
		var datosBloqueo= {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_BloqueoClientes: $scope.data.clientes
		};
		var lepost = resourceBloqueoCL.CallBloqueoClientesMasivos(datosBloqueo);
		lepost.$promise.then(function(respuesta){
			var operaciones = [];
			$.each(respuesta.P_OUT_OPERACIONES_REALIZADAS,function(key,value) {
				operaciones.push({
					id: value.Entidad,
					resultado: (value.IdEstado==="BLO")?$translate.instant('UI_EXITO') : $translate.instant('UI_ERROR'),
					mensaje: value.Entidad+":"+value.Id+" "+value.Operacion}
				);
			});
			var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
	            tituloVentana : $translate.instant("UI_BLOQUEO_MASIVO_CLIENTES"), 
	            nombreEntidad : $translate.instant("UI_ENTIDAD"), 
	            operaciones : operaciones   
	          },false,false,$scope.idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(respuesta){
			console.log(respuesta);
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	var doPostPE = function(){
		var datosBloqueo = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_SimularOperacion:'N',
			p_Accion:'ACT',
			p_Registros: [],
		};
		$.each($scope.data.clientes,function(key,value) {
			var aux = {
				TipoPersona:value.TIPOPERSONA,
				TipoDocumentoIdentidad:value.TIPODOCUMENTO,
				DocumentoIdentidad:value.DOCUMENTOIDENTIDAD,
				CodigoPortafolio:value.CODIGOPORTAFOLIO,
				TipoCambioEstadoCliente:value.MOTIVOBLOCLI,
				TipoCambioEstadoPortafolio:value.MOTIVOBLOPOR,
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
	            tituloVentana : $translate.instant("UI_ACTIVACION_MASIVA_CLIENTES"), 
	            nombreEntidad : $translate.instant("UI_ENTIDAD"), 
	            operaciones : operaciones   
	          },false,false,$scope.idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(respuesta){
			console.log(respuesta)
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.activar = function(){
		if($scope.SesionLocale!='PE'){
			alert("DESARROLLADOR: Funcionalidad implementada para proyecto Perú");
			return;
		}
		var count = 0;
		$.each($scope.data.clientes, function(key, val){
			if(val.MensajeValidacion==="OK"||val.MENSAJEVALIDACION==="OK"){
				if(val.MOTIVOBLOCLI==undefined||val.MOTIVOBLOCLI.length==0) val.MOTIVOBLOCLI = $scope.data.oConfiguracionTipoBloqueoCliente.idActual;
				if(val.MOTIVOBLOPOR==undefined||val.MOTIVOBLOPOR.length==0) val.MOTIVOBLOPOR = $scope.data.oConfiguracionTipoBloqueoPortafolio.idActual;	
			}
			count++;
			delete val.MensajeValidacion;
		});
		var t = bcsAlerta.crear("PREGUNTA","¿Está Seguro que desea bloquear "+count+" cliente(s) y su(s) portafolio(s)?");
		t.result.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,true);
			if (result===OPCIONES_BCSALERTA.ACEPTAR){			
				if($scope.SesionLocale=='PE') doPostPE();
				else doPostCL();
			}else{
				bcsComunes.ventanaOverlay(idVentana,false);
			}
		});		
	};

	$scope.limpiarDatos = function(){
		$scope.data.oConfiguracionTipoBloqueoPortafolio.idActual = false,
		$scope.data.oConfiguracionTipoBloqueoCliente.idActual = false;
		$scope.data.clientes="";
	};
});	
