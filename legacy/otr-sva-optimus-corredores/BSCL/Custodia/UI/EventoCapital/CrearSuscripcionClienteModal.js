/***************************************************************************
* Purpose	: Controlador ingresar Suscripciones para un Cliente.
* Author	: Cristian Guzmán O - BCS
* Date		: 19/05/2016
**************************************************************************/

angular.module('ModuloCustodia').controller('CrearSuscripcionClienteModalCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosEventoCapital,bcsServicioPlataforma
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
	

  	$scope.data.TipoEvento = $scope.data.oDatosEvento.IdEvento.Codigo;
  	//$scope.data.idInstrumento = $scope.data.oDatosEvento.IdInstrumento.IdEmisor;
//  	$scope.data.idInstrumento = $scope.data.oDatosEvento.EventoCapitalCliente.seleccionEventoCapital.IdEmisor
  	$scope.data.idFechaPago = $scope.data.oDatosEvento.IdFechaPago;
  	$scope.data.kngSuscripcionTXT = 0;
  	

	$scope.data.kngDisponibleSuscripcionTXT = 0;
	
    $scope.data.kngDisponibleSuscripcionTXT = $scope.data.oDatosEvento.EventoCapitalCliente.CantidadNueva;
	$scope.data.SuscripcionComplementaria = $scope.data.oDatosEvento.EventoCapitalCliente.SuscripcionComplementaria;
    
    if($scope.data.oDatosEvento.EventoCapitalCliente.SuscripcionComplementaria == 'S') {
        $scope.data.kngCantidadSuscritaTXT = $scope.data.oDatosEvento.EventoCapitalCliente.CantidadDerecho
    } else {
		$scope.data.kngCantidadSuscritaTXT = 0
    }
  	

  	if (parametros.Accion == 'Crear') {
  		$scope.data.TipoEventoTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.TipoEventoTXT;
		$scope.data.idInstrumentoTXT	= $scope.data.oDatosEvento.EventoCapitalCliente.idInstrumentoTXT;	
		$scope.data.kngSecuenciaTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.kngSecuenciaTXT;
		$scope.data.DescripcionTXT 		= $scope.data.oDatosEvento.EventoCapitalCliente.DescripcionTXT;
  	}
  	else
  	{
  		$scope.data.TipoEventoTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.TipoEvento;
		$scope.data.idInstrumentoTXT	= $scope.data.oDatosEvento.EventoCapitalCliente.Nemo;	
		$scope.data.kngSecuenciaTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.SecuenciaCupon;
		$scope.data.DescripcionTXT 		= $scope.data.oDatosEvento.EventoCapitalCliente.DescripcionEvento;
  	}

	$scope.data.accionDisabled = true;
	$scope.data.disabledTipoEvento = true;	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);	

	};
	
/*************************************************************************
* CARGA INICIAL MODIFICACIÓN
**************************************************************************/		
if ($scope.data.oDatosEvento.EventoCapitalCliente  && parametros.Accion == 'Modificar')
{
	//Deshabilitamos todos-------------------------------
	$scope.data.disablekngDerechoTXT = false;
	$scope.data.MuestraCantidadNueva = false;
	$scope.data.MuestraTotal = false;
	$scope.data.MuestraInstrumentoNuevo = false;
	$scope.data.MuestraMontoCapital = false;
	$scope.data.MuestraMontoInteres = false;
	//---------------------------------------------------
	
	$scope.data.IdEventoCapitalCliente		= $scope.data.oDatosEvento.EventoCapitalCliente.IdEventoCapitalCliente;
	$scope.data.TipoEventoTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente.TipoEvento;
	$scope.data.IdTipoEventoCapitalTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoEventoCapital;
	$scope.data.ClienteTXT 					= $scope.data.oDatosEvento.EventoCapitalCliente.DocumentoIdentidad  + ' - ' + $scope.data.oDatosEvento.EventoCapitalCliente.NombreCliente;
	$scope.data.PortafolioTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente.Portafolio;
};
	
 	
/*************************************************************************
* INGRESO 
**************************************************************************/		
	$scope.data.CrearSuscripcionCliente = function()
    {
		var datos = {};	
		var datosPrecio = {};
		var datosSuscripcion = {};
		bcsComunes.ventanaOverlay(idVentana,true);
		$scope.data.accionDisabled = undefined;

		if ($scope.data.kngDisponibleTXT == undefined)
		{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_CONSULTAR_CARTERA"));
			bcsComunes.ventanaOverlay(idVentana,false);
			$scope.data.accionDisabled = true;
    	 	return;
		}
		

		
		//VALIDAMOS QUE LA CANTIDAD DISPONIBLE+GARANTIA SEA MAYOR O IGUAL A LA CANTIDAD A SUSCRIBIR.
		if (Number($scope.data.kngSuscripcionTXT) > (Number($scope.data.kngDisponibleTXT)+Number($scope.data.kngGarantiaTXT)))
		{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_SUSCRIPCION_ERROR"));
			bcsComunes.ventanaOverlay(idVentana,false);
			$scope.data.accionDisabled = true;
    	 	return;
    	 };

 		//VALIDAMOS QUE LA CANTIDAD A SUSCRIBIR NO SEA MAYOR AL DISPOBIBLE PARA SUSCRIBIR(Cantidad disponible-Cantidad en Tramite)
 		if (Number($scope.data.kngSuscripcionTXT) > Number($scope.data.kngDisponibleSuscripcionTXT))
 		{
 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_SUSCRIPCION_EXCEDIDA"));
 			bcsComunes.ventanaOverlay(idVentana,false);
 			$scope.data.accionDisabled = true;
     	 	return;
     	 };
     	 
     	 
    	 if (Number($scope.data.kngSuscripcionTXT)== 0)
		{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_SUSCRIPCION_CERO"));
			bcsComunes.ventanaOverlay(idVentana,false);
			$scope.data.accionDisabled = true;
    	 	return;
    	 };
    	 
		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo = 'CUS';
		datos.p_UsaReglasNegocio = 'N';
		
//		//OBTENER PRECIO *************************************************************************

		$scope.data.kngPrecio = 0;
		
		datosPrecio.p_IdEmpresa = $scope.SesionIdEmpresa;
		datosPrecio.p_IdUsuario = $scope.SesionIdUsuario;
		datosPrecio.p_IdModulo = 'CUS';
		datosPrecio.p_IdPais = $scope.SesionIdPaisSistema;
		
		datosPrecio.p_DatosInstrumentos		= [{
			IdRegistro: 0,
			IdTrabajo : 0,
			TipoCartera : 0,
			IdUso : 'CARTERA',
			IdTipoCriterioTasa : 0,
			IdClasificacionTipoInstrumento	: parametros.Accion == "Crear" ? $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdClasificacionTipoInstrumento || '' : $scope.data.oDatosEvento.EventoCapitalCliente.IdClasificacionTipoInstrumento || '',
			IdTipoInstrumento 				: parametros.Accion == "Crear" ? $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdTipoInstrumento || '' : $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoInstrumento || '',
			IdInstrumento 					: parametros.Accion == "Crear" ? $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdInstrumento : $scope.data.oDatosEvento.EventoCapitalCliente.IdInstrumento || 0,
			Nemotecnico						: parametros.Accion == "Crear" ? $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].Nemo : $scope.data.oDatosEvento.EventoCapitalCliente.Nemo || '',
			IdMoneda 						: parametros.Accion == "Crear" ? $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdMoneda : $scope.data.oDatosEvento.EventoCapitalCliente.IdMoneda || '',
			FechaConsulta 					: $scope.SesionFechaSistema,
			PlazoResidual					: 0
		}];
					

		bcsServicioPlataforma.EntregarPreciosTasas(datosPrecio,function(respuesta){
			const datosEvento = $scope.data.oDatosEvento.EventoCapitalCliente;
			const precioInstrumentoOSA = parametros.Accion == "Crear" ? datosEvento.oEventoCapital.seleccionEventoCapital[0].FactorNuevo : datosEvento.FactorNuevo;
			if(precioInstrumentoOSA){
				datos.p_Precio = precioInstrumentoOSA;
			}else{
				if (respuesta.P_OUT_PRECIOSTASAS && respuesta.P_OUT_PRECIOSTASAS[0]){
					datos.p_Precio = Number(respuesta.P_OUT_PRECIOSTASAS[0].Precio);
				}else{
					datos.p_Precio = 0;
				}
			}

			datosSuscripcion.p_IdEmpresa = $scope.SesionIdEmpresa;
			datosSuscripcion.p_IdUsuario = $scope.SesionIdUsuario;
			datosSuscripcion.p_IdModulo = 'CUS';
			
			
	   		if (parametros.Accion == "Crear"){
	   			//ENCABEZADO
				datos.p_IdEventoCapital 				= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdEventoCapital;
				datos.p_IdTipoEventoCapital				= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdTipoEventoCapital;
				datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].Secuencia;
				datos.p_IdCliente						= $scope.oSocioNegocio.seleccionado.IdCliente;
				datos.p_DocumentoIdentidad				= $scope.oSocioNegocio.seleccionado.DocumentoIdentidad;
				datos.p_IdSocioNegocio					= $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
				datos.p_TipoSocioNegocio				= 'CLI';
				datos.p_IdPortafolio					= $scope.oPortafolio.seleccionado.IdPortafolio;
				datos.p_CodigoPortafolio				= $scope.oPortafolio.seleccionado.CodigoPortafolio;
				datos.p_IdTipoDocumentoIdentidad		= $scope.oSocioNegocio.seleccionado.IdTipoDocumentoIdentidadDefecto;
				datos.p_DocumentoIdentidad      		= $scope.oSocioNegocio.seleccionado.DocumentoIdentidadDefecto;
				datos.p_NombreCliente					= $scope.oSocioNegocio.seleccionado.NombreCompleto;
				datos.p_IdClasificacionTipoInstrumento	= $scope.data.oDatosEvento.IdInstrumento.IdClasificacionTipoInstrumento;
				datos.p_IdTipoInstrumento				= $scope.data.oDatosEvento.IdInstrumento.IdTipoInstrumento;
				datos.p_IdInstrumento					= $scope.data.oDatosEvento.IdInstrumento.IdInstrumento;
				datos.p_Nemo							= $scope.data.oDatosEvento.IdInstrumento.Nemotecnico;
				datos.p_IdInstrumentoNuevo				= 0;
				datos.p_NemoNuevo						= '';
				
				
				datos.p_FechaCierre						= util.dateSPdate($scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].FechaLimite);				
				datos.p_FechaPago						= util.dateSPdate($scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].FechaPago);
				datos.p_CantidadDerecho					= 0;
				datos.p_TipoCartera						= 'CUS';
				datos.p_CantidadNueva					= $scope.data.kngSuscripcionTXT;;
				datos.p_MontoCapital					= 0;
				datos.p_MontoInteres					= 0;
				datos.p_Total							= 0;
				datos.p_IdMoneda						= $scope.data.oDatosEvento.IdInstrumento.IdMoneda;
				datos.p_SecuenciaCupon					= 0;
				datos.p_IdEmpleadoEjecutivoPortafolio	= $scope.oPortafolio.seleccionado.IdEmpleadoEjecutivo;;
				datos.p_CodigoUsuarioAgente 			= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].CodigoUsuarioAgente;
				datos.p_NombreAgente					= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].NombreAgente;
				datos.p_CodigoSucursal					= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].CodigoSucursal;
				if ( $scope.SesionLocale === 'PE' ){
					datos.p_IdBoveda						= 'CAV';
					datos.p_IdBolsa							= 'BVL';					
				}else{
					datos.p_IdBoveda						= 'DCV';
					datos.p_IdBolsa							= 'BCS';	
				}
				datos.p_NumeroDividendo 				= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].NumeroDividendo;
				arrSuscripcion = new Array()
				arrSuscripcion.push(datos);
				datosSuscripcion.p_Suscripcion = arrSuscripcion;
				bcsServiciosEventoCapital.SuscripcionEmisionPago(datosSuscripcion,successSuscripcionEmisionPago,errorSuscripcionEmisionPago);
	   		}
	   		else
	   		{	
		   		datos.p_IdEventoCapital 				= $scope.data.oDatosEvento.EventoCapitalCliente.IdEventoCapital;
				datos.p_IdTipoEventoCapital				= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoEventoCapital;
				datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.Secuencia;
				datos.p_IdCliente						= $scope.data.oDatosEvento.EventoCapitalCliente.IdCliente;
				datos.p_DocumentoIdentidad				= $scope.data.oDatosEvento.EventoCapitalCliente.DocumentoIdentidad;
				datos.p_IdSocioNegocio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdSocioNegocio;
				datos.p_TipoSocioNegocio				= $scope.data.oDatosEvento.EventoCapitalCliente.TipoSocioNegocio;
				datos.p_IdPortafolio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdPortafolio;
				datos.p_CodigoPortafolio				= $scope.data.oDatosEvento.EventoCapitalCliente.CodigoPortafolio;
				datos.p_IdTipoDocumentoIdentidad		= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoDocumentoIdentidad;
				datos.p_DocumentoIdentidad      		= $scope.data.oDatosEvento.EventoCapitalCliente.DocumentoIdentidad;
				datos.p_NombreCliente					= $scope.data.oDatosEvento.EventoCapitalCliente.NombreCliente;
				datos.p_IdClasificacionTipoInstrumento	= $scope.data.oDatosEvento.EventoCapitalCliente.IdClasificacionTipoInstrumento;
				datos.p_IdTipoInstrumento				= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoInstrumento;
				datos.p_IdInstrumento					= $scope.data.oDatosEvento.EventoCapitalCliente.IdInstrumento;
				datos.p_Nemo							= $scope.data.oDatosEvento.EventoCapitalCliente.Nemo;
				datos.p_IdInstrumentoNuevo				= 0;
				datos.p_NemoNuevo						= '';
				


				datos.p_FechaCierre						= $scope.data.oDatosEvento.EventoCapitalCliente.FechaCierre;
				datos.p_FechaPago						= util.dateSPdate($scope.data.oDatosEvento.EventoCapitalCliente.FechaPago);
				datos.p_CantidadDerecho					= 0;
				datos.p_TipoCartera						= 'CUS';
				datos.p_CantidadNueva					= $scope.data.kngSuscripcionTXT;
				datos.p_MontoCapital					= 0;
				datos.p_MontoInteres					= 0;
				datos.p_Total							= 0;
				datos.p_IdMoneda						= $scope.data.oDatosEvento.EventoCapitalCliente.IdMoneda;
				datos.p_SecuenciaCupon					= 0;
				datos.p_IdEmpleadoEjecutivoPortafolio	= $scope.data.oDatosEvento.EventoCapitalCliente.IdEmpleadoEjecutivoPortafolio;
				datos.p_CodigoUsuarioAgente 			= $scope.data.oDatosEvento.EventoCapitalCliente.CodigoUsuarioAgente;
				datos.p_NombreAgente					= $scope.data.oDatosEvento.EventoCapitalCliente.NombreAgente;
				datos.p_CodigoSucursal					= $scope.data.oDatosEvento.EventoCapitalCliente.CodigoSucursal;
				if ( $scope.SesionLocale === 'PE' ){
					datos.p_IdBoveda						= 'CAV';
					datos.p_IdBolsa							= 'BVL';					
				}else{
					datos.p_IdBoveda						= 'DCV';
					datos.p_IdBolsa							= 'BCS';	
				}
				
				datos.p_NumeroDividendo 				= $scope.data.oDatosEvento.EventoCapitalCliente.NumeroDividendo;
				arrSuscripcion = new Array()
				arrSuscripcion.push(datos);
				datosSuscripcion.p_Suscripcion = arrSuscripcion;
				bcsServiciosEventoCapital.SuscripcionEmisionPago(datosSuscripcion,successSuscripcionEmisionPago,errorSuscripcionEmisionPago);
	   		}
	   	},function(respuesta){
	   		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
	   		bcsComunes.ventanaOverlay(idVentana,false);
			$scope.data.kngPrecio = 0;
		});
   	};
   	
/*************************************************************************
* INGRESO SUSCRIPCION EXITOSO
**************************************************************************/
	var successSuscripcionEmisionPago = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
		bcsComunes.ventanaOverlay(idVentana,false);
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_SECUENCIA !== undefined && respuesta.P_OUT_SECUENCIA.length > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_INGRESO_EXITO"));
			$scope.data.btnCerrar(true);
		}
		else
	    {

			bcsComunes.ventanaMsg({
				mensaje:'UI_ERROR_AL_CREAR_ASIGNACIONES',
				conflictos:util.parseServerResponse(respuesta)
			},$scope.idVentana);
			$scope.data.btnCerrar(true);
	    }
	};
	

	
/*************************************************************************
* INGRESO SUSCRIPCION CON ERRORES
**************************************************************************/	
	var errorSuscripcionEmisionPago = function(respuesta)
	{
		bcsComunes.ventanaOverlay(idVentana,false);
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
		$scope.data.accionDisabled = true;
	};
	

/*************************************************************************
* CONSULTA CARTERAS
**************************************************************************/

	$scope.data.btnConsultarCarteras = function(){
	bcsComunes.ventanaOverlay(idVentana,true);
	var datos = {};
	datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
	datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
	datos.p_IdModulo = 'CUS';

	if ($scope.data.Accion == 'Crear') {
	
		if ($scope.oSocioNegocio.seleccionado == false){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_CLIENTE"));
			$scope.data.accionDisabled = true;
			bcsComunes.ventanaOverlay(idVentana,false);
    	 	return;
		}
			
		if ($scope.oPortafolio.seleccionado === false){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_PORTAFOLIO"));
			$scope.data.accionDisabled = true;
			bcsComunes.ventanaOverlay(idVentana,false);
    	 	return;
		}
		datos.p_IdClasificacionTipoInstrumento 	= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdClasificacionTipoInstrumento;
		datos.p_IdTipoInstrumento 				= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdTipoInstrumento;
		datos.p_IdInstrumento					= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].IdInstrumento;
		datos.p_IdCliente						= $scope.oSocioNegocio.seleccionado.IdCliente;
		datos.p_IdPortafolio					= $scope.oPortafolio.seleccionado.IdPortafolio;
		datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.oEventoCapital.seleccionEventoCapital[0].Secuencia;
		datos.p_IdSocioNegocio					= $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
		datos.p_IdPais							= $scope.SesionIdPaisSistema;
	}
	else
	{	
		datos.p_IdClasificacionTipoInstrumento 	= $scope.data.oDatosEvento.EventoCapitalCliente.IdClasificacionTipoInstrumento;
		datos.p_IdTipoInstrumento 				= $scope.data.oDatosEvento.EventoCapitalCliente.IdTipoInstrumento;
		datos.p_IdInstrumento					= $scope.data.oDatosEvento.EventoCapitalCliente.IdInstrumento;
		datos.p_IdCliente						= $scope.data.oDatosEvento.EventoCapitalCliente.IdCliente;
		datos.p_IdPortafolio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdPortafolio;
		datos.p_Secuencia						= $scope.data.oDatosEvento.EventoCapitalCliente.Secuencia;
		datos.p_IdSocioNegocio					= $scope.data.oDatosEvento.EventoCapitalCliente.IdSocioNegocio;
		datos.p_IdPais							= $scope.SesionIdPaisSistema;
	}		
	bcsServiciosEventoCapital.ConsultaCarterasSuscripcionCliente(datos,successConsultaCarterasSuscripcionCliente,errorConsultaCarterasSuscripcionCliente);
		
   	
   	};

   	
/*************************************************************************
* CONSULTA SUSCRIPCION EXITOSO
**************************************************************************/
	var successConsultaCarterasSuscripcionCliente = function(respuesta)
	{
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_CANTIDADDISPONIBLE !== undefined )
		{
			$scope.data.Disponible 					= {min:-999999999999999999999};
			$scope.data.DisponibleSuscripcion		= {min:-999999999999999999999};
			$scope.data.Garantia					= {min:-999999999999999999999};
			$scope.data.TramiteSuscripcion			= {min:-999999999999999999999};

			$scope.data.kngDisponibleTXT 			= Number(respuesta.P_OUT_CANTIDADDISPONIBLE);
			$scope.data.kngGarantiaTXT 				= Number(respuesta.P_OUT_CANTIDADGARANTIAS);
			$scope.data.kngTramiteSuscripcionTXT 	= Number(respuesta.P_OUT_CANTIDADTRAMITE);
			$scope.data.kngSuscripcionTXT 			= ((Number(respuesta.P_OUT_CANTIDADDISPONIBLE)+Number(respuesta.P_OUT_CANTIDADGARANTIAS))-Number(respuesta.P_OUT_CANTIDADTRAMITE));
			
		
			
			$scope.data.kngDisponibleSuscripcionTXT = ((Number(respuesta.P_OUT_CANTIDADDISPONIBLE)+Number(respuesta.P_OUT_CANTIDADGARANTIAS))-Number(respuesta.P_OUT_CANTIDADTRAMITE));
			
			if($scope.data.kngDisponibleTXT < 0){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_SUSCRIPCION_NEGATIVO"));
			}

		}
		else
	    {
	    	$scope.data.kngDisponibleTXT 			= 0;
			$scope.data.kngGarantiaTXT 				= 0;
			$scope.data.kngSuscripcionTXT 			= 0;
			$scope.data.kngTramiteSuscripcionTXT  	= 0;

			$scope.data.kngDisponibleSuscripcionTXT = 0;
			
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_VALIDA_CANTIDADDISPONIBLE"));
	    }
		bcsComunes.ventanaOverlay(idVentana,false);
	};
	

/*************************************************************************
* CONSULTA SUSCRIPCION  CON ERRORES
**************************************************************************/	
	var errorConsultaCarterasSuscripcionCliente = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},idVentana);
		
		$scope.data.accionDisabled = true;
		bcsComunes.ventanaOverlay(idVentana,false);
	};

 	//Primer evento - filtro inicial
	$timeout(function(){
		$scope.data.oConfiguracionTipoEvento.filtros.read = true;
		$scope.data.oConfiguracionMoneda.filtros.read = true;
		if (parametros.Accion == 'Modificar')
		{
			$scope.data.btnConsultarCarteras();
		}
	},100);
	
	
	
});



