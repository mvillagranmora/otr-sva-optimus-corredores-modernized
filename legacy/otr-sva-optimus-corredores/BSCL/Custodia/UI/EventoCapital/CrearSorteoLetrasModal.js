angular.module('ModuloPlataforma').directive('fileReader', function() {
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
/***************************************************************************
* Purpose	: Controlador ingresar Suscripciones para un Cliente.
* Author	: Cristian Guzmán O - BCS
* Date		: 19/05/2016
**************************************************************************/

angular.module('ModuloCustodia').controller('CrearSuscripcionClienteModalCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosEventoCapital
				  ) {

$scope.data = {};
	
var overlay = bcsComunes.overlay;
var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
var parametros = bcsComunes.ventanaParams(idVentana);
$scope.data.oDatosEvento = parametros

	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
	$scope.data.oConfiguracionTipoEvento={
			tipo : "combobox-tipo-evento-capital",
			configuracion : {},
			filtros : { 	
				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoCartera={
			tipo : "combobox-tipo-cartera",
			configuracion : {},
			 filtros : {
                        IdEstado : { "field" : "EstadoTipoCartera" , "operator": "eq", "value"   : "ACT" },
                },
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionMoneda={
		tipo : "combobox-moneda",
		configuracion : {},
		filtros : { 	
			IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.oPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	//BOVEDA
	$scope.data.oConfiguracionBoveda={
		tipo : "combobox-boveda",
		configuracion : {},
		filtros : { 	
			
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	//Clasificacion
	$scope.data.oConfiguracionClasificacion={
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	var admEveCap = this;
  		admEveCap.winId 		= bcsComunes.ventanaWinId($scope);

  		
/*************************************************************************
* CARGA INICIAL
**************************************************************************/

	$scope.data.Accion = $scope.data.oDatosEvento.Accion;
	
	
	// SETEAMOS TODOS LOS VISIBLES EN TRUE
	$scope.data.MuestraInstrumentoNuevo = true;
	$scope.data.MuestraCantidadNueva = true;
	$scope.data.MuestraMontoCapital = true;
  	$scope.data.MuestraMontoInteres = true;
  	$scope.data.MuestraMoneda = true;
  	$scope.data.MuestraTotal = true;
	

//  	$scope.data.TipoEvento = $scope.data.oDatosEvento.IdEvento.Codigo;
//  	$scope.data.idInstrumento = $scope.data.oDatosEvento.IdInstrumento.IdEmisor;
//  	$scope.data.idFechaPago = $scope.data.oDatosEvento.IdFechaPago;
//
//  	if (parametros.Accion == 'Crear') {
//  		$scope.data.TipoEventoTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.TipoEvento;
//		$scope.data.idInstrumentoTXT	= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.Nemo;	
//		$scope.data.kngSecuenciaTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.Secuencia;
//		$scope.data.DescripcionTXT 		= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.Descripcion;
//  	}
//  	else
//  	{
//  		$scope.data.TipoEventoTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.TipoEvento;
//		$scope.data.idInstrumentoTXT	= $scope.data.oDatosEvento.EventoCapitalCliente.Nemo;	
//		$scope.data.kngSecuenciaTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.SecuenciaCupon;
//		$scope.data.DescripcionTXT 		= $scope.data.oDatosEvento.EventoCapitalCliente.DescripcionEvento;
////		$scope.data.btnConsultarCarteras();//QUITAR	
//  	}

	$scope.data.accionDisabled = true;
	$scope.data.disabledTipoEvento = true;	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);	

	};
	
/*************************************************************************
* CARGA INICIAL MODIFICACIÓN
**************************************************************************/		
//if ($scope.data.oDatosEvento.EventoCapitalCliente  && parametros.Accion == 'Modificar')
//{
//	//Deshabilitamos todos-------------------------------
//	$scope.data.disablekngDerechoTXT = false;
//	$scope.data.MuestraCantidadNueva = false;
//	$scope.data.MuestraTotal = false;
//	$scope.data.MuestraInstrumentoNuevo = false;
//	$scope.data.MuestraMontoCapital = false;
//	$scope.data.MuestraMontoInteres = false;
//	//---------------------------------------------------
//	
//	$scope.data.IdEventoCapitalCliente		= $scope.data.oDatosEvento.EventoCapitalCliente.IdEventoCapitalCliente;
//	$scope.data.TipoEventoTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente.TipoEvento;
//	$scope.data.IdTipoEventoCapitalTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoEventoCapital;
//	$scope.data.ClienteTXT 					= $scope.data.oDatosEvento.EventoCapitalCliente.DocumentoIdentidad  + ' - ' + $scope.data.oDatosEvento.EventoCapitalCliente.NombreCliente;
//	$scope.data.PortafolioTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente.Portafolio;
//};
	
 	
/*************************************************************************
* INGRESO 
**************************************************************************/		
//	$scope.data.CrearSuscripcionCliente = function()
//    {
//		var datos = {};	
//		overlay.encendido = true; 
//		$scope.data.accionDisabled = undefined;
//
//		//VALIDAMOS QUE LA CANTIDAD DISPONIBLE SEA MAYOR O IGUAL A LA CANTIDAD A SUSCRIBIR.
//		
//		if (Number($scope.data.kngSuscripcionTXT) > Number($scope.data.kngDisponibleTXT))
//		{
//			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_SUSCRIPCION_ERROR"));
//    		overlay.encendido = false; 
//			$scope.data.accionDisabled = true;
//    	 	return;
//    	 };
//
//    	 if (Number($scope.data.kngSuscripcionTXT)== 0)
//		{
//			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_SUSCRIPCION_CERO"));
//    		overlay.encendido = false; 
//			$scope.data.accionDisabled = true;
//    	 	return;
//    	 };
//    	 
//		
//		datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
//		datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
//		datos.p_IdModulo = 'CUS';
//		datos.p_UsaReglasNegocio = 'N';
//				
//   		if (parametros.Accion == "Crear"){
//   			//ENCABEZADO
//			datos.p_IdEventoCapital 				= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.IdEventoCapital;
//			datos.p_IdTipoEventoCapital				= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.IdTipoEventoCapital;
//			datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.Secuencia;
//			datos.p_IdSocioNegocio					= $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
//			datos.p_TipoSocioNegocio				= '';
//			datos.p_IdPortafolio					= $scope.oPortafolio.seleccionado.IdPortafolio;
//			datos.p_IdTipoDocumentoIdentidad		= $scope.oSocioNegocio.seleccionado.IdTipoDocumentoIdentidadDefecto;
//			datos.p_DocumentoIdentidad      		= $scope.oSocioNegocio.seleccionado.DocumentoIdentidadDefecto;
//			datos.p_NombreCliente					= $scope.oSocioNegocio.seleccionado.NombreCompleto;
//			datos.p_IdClasificacionTipoInstrumento	= $scope.data.oDatosEvento.IdInstrumento.IdClasificacionTipoInstrumento;
//			datos.p_IdTipoInstrumento				= $scope.data.oDatosEvento.IdInstrumento.IdTipoInstrumento;
//			datos.p_IdInstrumento					= $scope.data.oDatosEvento.IdInstrumento.IdInstrumento;
//			datos.p_Nemo							= $scope.data.oDatosEvento.IdInstrumento.Nemotecnico;
//			datos.p_IdInstrumentoNuevo				= 0;
//			datos.p_NemoNuevo						= '';
//			datos.p_FechaPago						= util.dateSPdate($scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.FechaPago);
//			datos.p_CantidadDerecho					= 0;
//			datos.p_TipoCartera						= 'CUS';
//			datos.p_CantidadNueva					= $scope.data.kngSuscripcionTXT;;
//			datos.p_MontoCapital					= 0;
//			datos.p_MontoInteres					= 0;
//			datos.p_Total							= 0;
//			datos.p_IdMoneda						= $scope.data.oDatosEvento.IdInstrumento.IdTipoReajuste;
//			datos.p_SecuenciaCupon					= 0;
//			datos.p_CodigoUsuarioAgente 			= '';
//			datos.p_NombreAgente					= '';
//			datos.p_CodigoSucursal					= '';
//			datos.p_IdBoveda						= 'DCV';
//			datos.p_IdBolsa							= 'BCS';
//			bcsServiciosEventoCapital.SuscripcionEmisionPago(datos,successSuscripcionEmisionPago,errorSuscripcionEmisionPago);
//   		}
//   		else
//   		{	
//	   		datos.p_IdEventoCapital 				= $scope.data.oDatosEvento.EventoCapitalCliente.IdEventoCapital;
//			datos.p_IdTipoEventoCapital				= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoEventoCapital;
//			datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.Secuencia;
//			datos.p_IdSocioNegocio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdSocioNegocio;
//			datos.p_TipoSocioNegocio				= '';
//			datos.p_IdPortafolio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdPortafolio;
//			datos.p_IdTipoDocumentoIdentidad		= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoDocumentoIdentidad;
//			datos.p_DocumentoIdentidad      		= $scope.data.oDatosEvento.EventoCapitalCliente.DocumentoIdentidad;
//			datos.p_NombreCliente					= $scope.data.oDatosEvento.EventoCapitalCliente.NombreCliente;
//			datos.p_IdClasificacionTipoInstrumento	= $scope.data.oDatosEvento.EventoCapitalCliente.IdClasificacionTipoInstrumento;
//			datos.p_IdTipoInstrumento				= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoInstrumento;
//			datos.p_IdInstrumento					= $scope.data.oDatosEvento.EventoCapitalCliente.IdInstrumento;
//			datos.p_Nemo							= $scope.data.oDatosEvento.EventoCapitalCliente.Nemo;
//			datos.p_IdInstrumentoNuevo				= 0;
//			datos.p_NemoNuevo						= '';
//			datos.p_FechaPago						= util.dateSPdate($scope.data.oDatosEvento.EventoCapitalCliente.FechaPago);
//			datos.p_CantidadDerecho					= 0;
//			datos.p_TipoCartera						= 'CUS';
//			datos.p_CantidadNueva					= $scope.data.kngSuscripcionTXT;
//			datos.p_MontoCapital					= 0;
//			datos.p_MontoInteres					= 0;
//			datos.p_Total							= 0;
//			datos.p_IdMoneda						= $scope.data.oDatosEvento.EventoCapitalCliente.IdMoneda;
//			datos.p_SecuenciaCupon					= 0;
//			datos.p_CodigoUsuarioAgente 			= '';
//			datos.p_NombreAgente					= '';
//			datos.p_CodigoSucursal					= '';
//			datos.p_IdBoveda						= 'DCV';
//			datos.p_IdBolsa							= 'BCS';
//	   		bcsServiciosEventoCapital.SuscripcionEmisionPago(datos,successSuscripcionEmisionPago,errorSuscripcionEmisionPago);
//   		}
//   	};
//   	
///*************************************************************************
//* INGRESO SUSCRIPCION EXITOSO
//**************************************************************************/
//	var successSuscripcionEmisionPago = function(respuesta)
//	{
//		//console.log(JSON.stringify(respuesta));
//   		overlay.encendido = false;
//		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
//		$scope.data.accionDisabled = true;
//			
//		if (respuesta.P_OUT_SECUENCIA !== undefined && respuesta.P_OUT_SECUENCIA  > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
//		{
//			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_INGRESO_EXITO"));
//			$scope.data.btnCerrar(true);
//		}
//		else
//	    {
//	    	if (respuesta.P_OUT_ERRORESCONTADOR == 1){
//				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
//			}
//			else
//			{
//				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
//			}			
//	    }
//	};
//	
//
//	
///*************************************************************************
//* INGRESO SUSCRIPCION CON ERRORES
//**************************************************************************/	
//	var errorSuscripcionEmisionPago = function(respuesta)
//	{
//		overlay.encendido = false;
//		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
//		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
//		$scope.data.accionDisabled = true;
//	};
//	
//
///*************************************************************************
//* CONSULTA CARTERAS
//**************************************************************************/
//
//	$scope.data.btnConsultarCarteras = function(){
//	
//	var datos = {};
//	datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
//	datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
//	datos.p_IdModulo = 'CUS';
//
//	if ($scope.data.Accion == 'Crear') {
//	
//		if ($scope.oSocioNegocio.seleccionado == false){
//			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_CLIENTE"));
//    	    overlay.encendido = false; 
//			$scope.data.accionDisabled = true;			
//    	 	return;
//		}
//			
//		if ($scope.oPortafolio.seleccionado === false){
//			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_PORTAFOLIO"));
//    		overlay.encendido = false; 
//			$scope.data.accionDisabled = true;
//    	 	return;
//		}
//		datos.p_IdClasificacionTipoInstrumento 	= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.IdClasificacionTipoInstrumento;
//		datos.p_IdTipoInstrumento 				= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.IdTipoInstrumento;
//		datos.p_IdInstrumento					= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.IdInstrumento;
//		datos.p_IdCliente						= $scope.oSocioNegocio.seleccionado.IdCliente;
//		datos.p_IdPortafolio					= $scope.oPortafolio.seleccionado.IdPortafolio;
//		datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.Secuencia;
//		datos.p_IdSocioNegocio					= $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
//	}
//	else
//	{	
//		datos.p_IdClasificacionTipoInstrumento 	= $scope.data.oDatosEvento.EventoCapitalCliente.IdClasificacionTipoInstrumento;
//		datos.p_IdTipoInstrumento 				= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoInstrumento;
//		datos.p_IdInstrumento					= $scope.data.oDatosEvento.EventoCapitalCliente.IdInstrumento;
//		datos.p_IdCliente						= $scope.data.oDatosEvento.EventoCapitalCliente.IdCliente;
//		datos.p_IdPortafolio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdPortafolio;
//		datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.Secuencia;
//		datos.p_IdSocioNegocio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdSocioNegocio;
//	}		
//   	bcsServiciosEventoCapital.ConsultaCarterasSuscripcionCliente(datos,successConsultaCarterasSuscripcionCliente,errorConsultaCarterasSuscripcionCliente);
//   	};
//
//   	
///*************************************************************************
//* CONSULTA SUSCRIPCION EXITOSO
//**************************************************************************/
//	var successConsultaCarterasSuscripcionCliente = function(respuesta)
//	{
//		//console.log(JSON.stringify(respuesta));
//   		overlay.encendido = false;
//		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
//		$scope.data.accionDisabled = true;
//			
//		if (respuesta.P_OUT_CANTIDADDISPONIBLE !== undefined ) 
//		{
//			$scope.data.kngDisponibleTXT 			= Number(respuesta.P_OUT_CANTIDADDISPONIBLE);
//			$scope.data.kngGarantiaTXT 				= Number(respuesta.P_OUT_CANTIDADGARANTIAS);
//			$scope.data.kngSuscripcionTXT 			= Number(respuesta.P_OUT_CANTIDADDISPONIBLE);
//			$scope.data.kngTramiteSuscripcionTXT 	= Number(respuesta.P_OUT_CANTIDADTRAMITE);
//		}
//		else
//	    {
//	    	$scope.data.kngDisponibleTXT 			= 0;
//			$scope.data.kngGarantiaTXT 				= 0;
//			$scope.data.kngSuscripcionTXT 			= 0;
//			$scope.data.kngTramiteSuscripcionTXT  	= 0;
//			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_VALIDA_CANTIDADDISPONIBLE"));
//	    }
//	};
//	
//
///*************************************************************************
//* CONSULTA SUSCRIPCION  CON ERRORES
//**************************************************************************/	
//	var errorConsultaCarterasSuscripcionCliente = function(respuesta)
//	{
//		overlay.encendido = false;
//		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
//		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
//		$scope.data.accionDisabled = true;
//	};

 	//Primer evento - filtro inicial
	$timeout(function(){
		$scope.data.oConfiguracionTipoEvento.filtros.read = true;
		$scope.data.oConfiguracionMoneda.filtros.read = true; 
	},1000);
	
});


