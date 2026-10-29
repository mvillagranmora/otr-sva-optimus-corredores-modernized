/***************************************************************************
* Purpose	: Controlador ingresar Evento Capital Cliente.
* Author	: Juan Pablo Zapata - BCS
* Date		:18/02/2016
**************************************************************************/

angular.module('ModuloCustodia').controller('CrearEventoCapitalClienteModalCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosEventoCapital,bcsAlerta,OPCIONES_BCSALERTA
				  ) {

$scope.data = {};
$scope.formData = {};
$scope.data.Monto   = {min:-99999999};

var overlay = bcsComunes.overlay;
var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
var parametros = bcsComunes.ventanaParams(idVentana);
$scope.data.oDatosEvento = parametros;

	if($scope.SesionLocale=='PE'){
		$scope.data.MuestraVacali = true;
		$scope.data.MuestraArchivo = true;
	}else{
		$scope.data.MuestraVacali = false;
		$scope.data.MuestraArchivo = false;
	};

	//$scope.data.formatoMonto = util.bcs_formato(parametros.EventoCapitalCliente[0].Total,'CUS','Monto',parametros.EventoCapitalCliente[0].IdMoneda,'','');
	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};

	$scope.data.FormatoMoneda = util.bcs_formato(parametros.EventoCapitalCliente[0].Total,'GEN','Monto',parametros.EventoCapitalCliente[0].IdMoneda,'','')
	if(parametros.EventoCapitalCliente[0].IdTipoInstrumento == 'FFM'){
		$scope.data.FormatoMoneda.cantidad = util.bcs_formato(parametros.EventoCapitalCliente[0].Total,'GEN','Cantidad',parametros.EventoCapitalCliente[0].IdMoneda,parametros.EventoCapitalCliente[0].IdTipoInstrumento,'');	
	}
	else{
		$scope.data.FormatoMoneda.cantidad = util.bcs_formato(parametros.EventoCapitalCliente[0].Total,'GEN','Monto',parametros.EventoCapitalCliente[0].IdMoneda,'','');	
	}
	$scope.data.configuracionCantidad =		{format: 'n' + $scope.data.FormatoMoneda.cantidad.cantidadDecimales	,"min": -999999999999999	,spinners: false};
	$scope.data.configuracionMontos = 		{format: 'n' + $scope.data.FormatoMoneda.cantidadDecimales	,"min": -999999999999999	,spinners: false};
	$scope.data.configuracionMontoCapital = {format: 'n' + $scope.data.FormatoMoneda.cantidadDecimales	,"min": -999999999999999	};
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	
	//-----------------------------------------------------
	// CONFIGURACION DE COMBOBOX
	//-----------------------------------------------------

	$scope.data.oConfiguracionTipoSocioNegocio={
		tipo 			: "combobox-tipo-socio-negocio",
		configuracion 	: {},
		filtros 		: {},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: (parametros.EventoCapitalCliente[0].TipoSocioNegocio || ''),
		idPostFiltro  	:  'CLI',
		idActualTraspaso: false,
		seleccionadoTraspaso:false,
		idPostFiltroTraspaso:false
};

	$scope.data.oConfiguracionTipoSocioNegocioSinContraparte={
			tipo 			: "combobox-tipo-socio-negocio",
			configuracion 	: {},
			filtros : {
				IdTipoSocioDeNegocio      : { "field"   : "IdTipoSocioDeNegocio",     "operator": "ne", "value" : "CNT" }
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: (parametros.EventoCapitalCliente[0].TipoSocioNegocio || ''),
			idPostFiltro  	:  'CLI',
			idActualTraspaso: false,
			seleccionadoTraspaso:false,
			idPostFiltroTraspaso:false
	};

	$scope.persona = {};

	$scope.data.oConfiguracionEntidad={
		tipo : "combobox-contraparte",
		configuracion : {},
		filtros : {
			IdEstado      : { "field"   : "IdEstado",     "operator": "eq", "value" : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '0'
};
	$scope.data.oConfiguracionSocioNegocio={
			tipo 			: "combobox-socio-negocio",
			configuracion 	: {},
			filtros			: {
								IdEmpresa     		: { "field"   : "IdEmpresa",         "operator": "eq", "value"   : $scope.SesionIdEmpresa },
								EstadoPersona 		: { "field"   : "EstadoPersona",     "operator": "eq", "value"   : "ACT"},
								EsCliente     		: { "field"   : "EsCliente",         "operator": "eq", "value"   : "" },
								EstadoCliente 		: { "field"   : "EstadoCliente",     "operator": "eq", "value"   : ""},			
								EsProveedor   		: { "field"   : "EsProveedor",       "operator": "eq", "value"   : "" },
								EstadoProveedor 	: { "field"   : "EstadoProveedor",   "operator": "eq", "value"   : ""},
								EsEmpleado    		: { "field"   : "EsEmpleado",        "operator": "eq", "value"   : "" },
								EstadoEmpleado 		: { "field"   : "EstadoEmpleado",    "operator": "eq", "value"   : ""},
								EsContraparte 		: { "field"   : "EsContraparte",     "operator": "eq", "value"   : "" },
								EstadoContraparte 	: { "field"   : "EstadoContraparte", "operator": "eq", "value"   : ""},
								IdPersona 	        : { "field"   : "IdPersona",         "operator": "eq", "value"   : 0 },
							  },
		  filtrosTraspaso	: {
				IdEmpresa     		: { "field"   : "IdEmpresa",         "operator": "eq", "value"   : $scope.SesionIdEmpresa },
				EstadoPersona 		: { "field"   : "EstadoPersona",     "operator": "eq", "value"   : "ACT"},
				EsCliente     		: { "field"   : "EsCliente",         "operator": "eq", "value"   : "" },
				EstadoCliente 		: { "field"   : "EstadoCliente",     "operator": "eq", "value"   : ""},			
				EsProveedor   		: { "field"   : "EsProveedor",       "operator": "eq", "value"   : "" },
				EstadoProveedor 	: { "field"   : "EstadoProveedor",   "operator": "eq", "value"   : ""},
				EsEmpleado    		: { "field"   : "EsEmpleado",        "operator": "eq", "value"   : "" },
				EstadoEmpleado 		: { "field"   : "EstadoEmpleado",    "operator": "eq", "value"   : ""},
				EsContraparte 		: { "field"   : "EsContraparte",     "operator": "eq", "value"   : "" },
				EstadoContraparte 	: { "field"   : "EstadoContraparte", "operator": "eq", "value"   : ""},
				IdPersona 	        : { "field"   : "IdPersona",         "operator": "eq", "value"   : 0 },
			  },
			idActual 		: false,
			seleccionado 	: false,		
			idDefecto 		: (parametros.EventoCapitalCliente[0].IdUsuario || 0),
			idPostFiltro  	: false,	
			itemPostFiltro	: false,
			idActualTraspaso: false,
			seleccionadoTraspaso: false,		
			idDefectoTraspaso: false,
			idPostFiltroTraspaso: false,	
			itemPostFiltroTraspaso: false
		};
	
	$scope.data.oConfiguracionPortafolio={
			tipo 			: "combobox-portafolio",
			configuracion 	: {},
			filtros	: {
								IdCliente 		: { "field"	: "IdCliente","operator": "eq", "value"   	:  0 },
								IdEstado 		: { "field"	: "IdEstado","operator": "eq", "value"   	:  'ACT'}
		    				},
			filtrosAPV			: {
								IdCliente 		: { "field"	: "IdCliente","operator": "eq", "value"   	:  0 },
								IdEstado 		: { "field"	: "IdEstado","operator": "eq", "value"   	:  'ACT'},
								IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
							  },
		    filtrosTraspaso	: {
				IdCliente 		: { "field"	: "IdCliente","operator": "eq", "value"   	:  0 },
				IdEstado 		: { "field"	: "IdEstado","operator": "eq", "value"   	:  'ACT'}
		    				},
			idActual 		: false,
			seleccionado 	: false,
			autoCargar		: (parametros.EventoCapitalCliente[0].IdPortafolio ? true : false),
			idDefecto 		: (parametros.EventoCapitalCliente[0].IdPortafolio || '1'),
			idPostFiltro  	: false,
			idActualTraspaso: false,
			seleccionadoTraspaso: false,
			autoCargarTraspaso: false,
			idDefectoTraspaso: '1',
			idPostFiltroTraspaso: false	
		};
	
	$scope.data.oConfiguracionSocioNegocioRelacionado={
			tipo 			: "combobox-relacionado",
			configuracion 	: {},
			filtros			: {
								IdEmpresa		: { "field"   : "IdEmpresa",		"operator": "eq", "value"   : $scope.SesionIdEmpresa },
								IdEstado 		: { "field"   : "IdEstado",			"operator": "eq", "value"   : "ACT"},
								IdPersonaOrigen	: { "field"   : "IdPersonaOrigen",	"operator": "eq", "value"   : 0},
							  },
			filtrosTraspaso	: {
					IdEmpresa		: { "field"   : "IdEmpresa",		"operator": "eq", "value"   : $scope.SesionIdEmpresa },
					IdEstado 		: { "field"   : "IdEstado",			"operator": "eq", "value"   : "ACT"},
					IdPersonaOrigen	: { "field"   : "IdPersonaOrigen",	"operator": "eq", "value"   : 0},
				  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ("" || 0),
			idPostFiltro  	: false,	
			itemPostFiltro	: false,
			idActualTraspaso: false,
			seleccionadoTraspaso: false,
			idDefectoTraspaso : 0,
			idPostFiltroTraspaso : false,	
			itemPostFiltroTraspaso: false
		};

		$scope.data.oConfiguracionSocioNegocioContraparteRelacionado={
			tipo 			: "combobox-relacionado",
			configuracion 	: {},
			filtros			: {
								IdEmpresa		: { "field"   : "IdEmpresa",		"operator": "eq", "value"   : $scope.SesionIdEmpresa },
								IdEstado 		: { "field"   : "IdEstado",			"operator": "eq", "value"   : "ACT"},
								IdPersonaOrigen	: { "field"   : "IdPersonaOrigen",	"operator": "eq", "value"   : 0},
							  },
			filtrosTraspaso	: {
					IdEmpresa		: { "field"   : "IdEmpresa",		"operator": "eq", "value"   : $scope.SesionIdEmpresa },
					IdEstado 		: { "field"   : "IdEstado",			"operator": "eq", "value"   : "ACT"},
					IdPersonaOrigen	: { "field"   : "IdPersonaOrigen",	"operator": "eq", "value"   : 0},
				  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ("" || 0),
			idPostFiltro  	: false,	
			itemPostFiltro	: false,
			idActualTraspaso: false,
			seleccionadoTraspaso: false,
			idDefectoTraspaso : 0,
			idPostFiltroTraspaso : false,	
			itemPostFiltroTraspaso: false
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
			idDefecto : 'CUS'
	};
	
	$scope.data.oConfiguracionMoneda={
		tipo : "combobox-detalle-tipo-atributo",
		configuracion : {},
		filtros: {
			CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: "DIV_DOM" }
		},
		idActual 		: false,
		seleccionado 	: false,		
		idDefecto 		: (parametros.EventoCapitalCliente[0].IdMoneda || 0),
		idPostFiltro  	: false,	
		itemPostFiltro	: false,
		idActualTraspaso: false,
		seleccionadoTraspaso: false,		
		idDefectoTraspaso: false,
		idPostFiltroTraspaso: false,	
		itemPostFiltroTraspaso: false
	};

	$scope.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
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
		idDefecto : 'DCV'
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
//			IdBolsa : { "field"   : "IdBolsa","operator": "eq", "value"   : $scope.SesionBolsaDefectoSistema },
//			IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	var admEveCap = this;
  		admEveCap.winId 		= bcsComunes.ventanaWinId($scope);

  		
  	$scope.formData.resetFoco =  function() {
		//$timeout(function() {
			$scope.formData.TipoEvento = true;
		//},1000);

	};
/*	
	$scope.$watch('data.kngDerecho', function(newval,oldval){
		$scope.data.kngMonto = ($scope.data.kngDerecho * $scope.data.kngFactor);
	},true);

	
	$scope.$watch('data.kngDerechoTXT', function(newval,oldval){
		$scope.data.kngMontoTXT = Math.round(($scope.data.kngDerechoTXT * $scope.data.kngFactorTXT));
	},true);
*/
	// ----------------------------------------------
	// BLOQUE WATCH (INICIO)
	// ----------------------------------------------  
	//Al cambiar "Tipo Socio Negocio" actualiza listado de socios de negocio
	$scope.$watch('data.oConfiguracionTipoSocioNegocio.seleccionado',function(newval,oldval){
		if($scope.data.oConfiguracionTipoSocioNegocio != null && $scope.data.oConfiguracionSocioNegocio != null &&
			$scope.data.oConfiguracionTipoSocioNegocio != undefined && $scope.data.oConfiguracionSocioNegocio != undefined){
			if (typeof $scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== 'undefined' && 
				$scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== false &&
				$scope.accion !== 'MODIFICAR')
			{
				if($scope.data.oConfiguracionSocioNegocio != null && $scope.data.oConfiguracionSocioNegocio != undefined &&
					$scope.data.oConfiguracionSocioNegocio.filtros != null && $scope.data.oConfiguracionSocioNegocio.filtros != undefined &&
					$scope.data.oConfiguracionTipoSocioNegocio != null && $scope.data.oConfiguracionTipoSocioNegocio != undefined){
						$scope.setarSocioNegocio();
						$scope.data.oConfiguracionSocioNegocio.idActual = false;
						$scope.data.oConfiguracionPortafolio.idActual 	= false;

						/////$scope.data.generarComprobante = true;
						if($scope.data.oConfiguracionTipoSocioNegocio.seleccionado != null){
							var IdTipoSocioDeNegocio = $scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio
							switch (IdTipoSocioDeNegocio)
							{				
								case "CLI":   //Cliente
									$scope.data.oConfiguracionSocioNegocio.filtros.EsCliente.value      = 'UI_SI';
									$scope.data.oConfiguracionSocioNegocio.filtros.EstadoCliente.value  = 'ACT';
									$scope.data.oConfiguracionPortafolio.activado = true;
									$scope.data.showPortafolio=true;
									/////$scope.data.generarComprobante = false;
									break;
				
								case "EMP":   //Empleado
									$scope.data.oConfiguracionSocioNegocio.filtros.EsEmpleado.value     = 'UI_SI';
									$scope.data.oConfiguracionSocioNegocio.filtros.EstadoEmpleado.value = 'ACT';
									$scope.data.showPortafolio= false;
									$scope.data.oConfiguracionPortafolio.activado = false;
									
									break;					
				
								case "PRO":   //Proveedor					
									$scope.data.oConfiguracionSocioNegocio.filtros.EsProveedor.value     = 'UI_SI';
									$scope.data.oConfiguracionSocioNegocio.filtros.EstadoProveedor.value = 'ACT';
									$scope.data.showPortafolio= false;
									$scope.data.oConfiguracionPortafolio.activado = false;			
									break;
				
								case "CNT":   //Contraparte									
									$scope.data.oConfiguracionSocioNegocio.filtros.EsContraparte.value     = 'UI_SI';
									$scope.data.oConfiguracionSocioNegocio.filtros.EstadoContraparte.value = 'ACT';
									$scope.data.showPortafolio= false;
									$scope.data.oConfiguracionPortafolio.activado = false;
									break;
							}
						}
				}
			}
		}

	});	


	$scope.$watch('data.oConfiguracionTipoSocioNegocioSinContraparte.seleccionado',function(newval,oldval){
		if($scope.data.oConfiguracionTipoSocioNegocioSinContraparte != null && $scope.data.oConfiguracionSocioNegocio != null &&
			$scope.data.oConfiguracionTipoSocioNegocio != undefined && $scope.data.oConfiguracionSocioNegocio != undefined){
			if (typeof $scope.data.oConfiguracionTipoSocioNegocioSinContraparte.seleccionado !== 'undefined' && 
				$scope.data.oConfiguracionTipoSocioNegocioSinContraparte.seleccionado !== false &&
				$scope.accion !== 'MODIFICAR')
			{
				if($scope.data.oConfiguracionSocioNegocio != null && $scope.data.oConfiguracionSocioNegocio != undefined &&
					$scope.data.oConfiguracionSocioNegocio.filtros != null && $scope.data.oConfiguracionSocioNegocio.filtros != undefined &&
					$scope.data.oConfiguracionTipoSocioNegocioSinContraparte != null && $scope.data.oConfiguracionTipoSocioNegocioSinContraparte != undefined){
						$scope.setarSocioNegocio();
						$scope.data.oConfiguracionSocioNegocio.idActual = false;
						$scope.data.oConfiguracionPortafolio.idActual 	= false;

						/////$scope.data.generarComprobante = true;
						if($scope.data.oConfiguracionTipoSocioNegocioSinContraparte.seleccionado != null){
							var IdTipoSocioDeNegocio = $scope.data.oConfiguracionTipoSocioNegocioSinContraparte.seleccionado.IdTipoSocioDeNegocio
							switch (IdTipoSocioDeNegocio)
							{				
								case "CLI":   //Cliente
									$scope.data.oConfiguracionSocioNegocio.filtros.EsCliente.value      = 'UI_SI';
									$scope.data.oConfiguracionSocioNegocio.filtros.EstadoCliente.value  = 'ACT';
									$scope.data.oConfiguracionPortafolio.activado = true;
									$scope.data.showPortafolio=true;
									break;
				
								case "EMP":   //Empleado
									$scope.data.oConfiguracionSocioNegocio.filtros.EsEmpleado.value     = 'UI_SI';
									$scope.data.oConfiguracionSocioNegocio.filtros.EstadoEmpleado.value = 'ACT';
									$scope.data.showPortafolio= false;
									$scope.data.oConfiguracionPortafolio.activado = false;
									break;					
				
								case "PRO":   //Proveedor					
									$scope.data.oConfiguracionSocioNegocio.filtros.EsProveedor.value     = 'UI_SI';
									$scope.data.oConfiguracionSocioNegocio.filtros.EstadoProveedor.value = 'ACT';
									$scope.data.showPortafolio= false;
									$scope.data.oConfiguracionPortafolio.activado = false;			
									break;
							}
						}
				}
			}
		}

	});
	
	$scope.$watch('data.oConfiguracionSocioNegocio.seleccionado',function(newval,oldval){
		
		if ($scope.accion === 'MODIFICAR'){
			return;
		}
		if($scope.data.oConfiguracionSocioNegocio != null && $scope.data.oConfiguracionTipoSocioNegocio != null){
			if (typeof $scope.data.oConfiguracionSocioNegocio.seleccionado !== 'undefined' && $scope.data.oConfiguracionSocioNegocio.seleccionado !== false &&
					typeof $scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== 'undefined' && $scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== false)
					{
						if($scope.data.oConfiguracionTipoSocioNegocio.seleccionado != null && $scope.data.oConfiguracionSocioNegocio.seleccionado != null){
							if ($scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio === 'CLI' && 
									$scope.data.oConfiguracionSocioNegocio.seleccionado.EsCliente === 'UI_SI'){
									
									$scope.data.oConfiguracionPortafolio.filtros.IdCliente.value = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
									$scope.data.oConfiguracionPortafolio.filtrosAPV.IdCliente.value = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;	
									if(typeof IdProductoAgrupador != 'undefined'){
										if($scope.MostrarProducto){
											$scope.data.oConfiguracionPortafolio.filtrosAPV.IdProductoAgrupador.value = Number(IdProductoAgrupador);
										}	
									}									
							}
							if ($scope.data.oConfiguracionSocioNegocio.seleccionado.BloqueadoPorCumplimiento === 'S' && $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVING')
							{			
								bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO")},$scope.IdVentana);				
							}
						}
					}
					else
					{
						if($scope.data.oConfiguracionPortafolio.filtros != null && $scope.data.oConfiguracionPortafolio.filtrosAPV != null){
							$scope.data.oConfiguracionPortafolio.filtros.IdCliente.value = -1; //limpia portafolio
							$scope.data.oConfiguracionPortafolio.filtrosAPV.IdCliente.value = -1;
						}
					}
		}
		
	});
	
	$scope.$watch('data.kngDerecho', function(newval, oldval){
		if(newval != oldval && $scope.data.oDatosEvento.IdEvento.Codigo == "DA"){
			$scope.data.kngNueva = ($scope.data.kngDerecho * Number($scope.data.kngFactorTXT));
		}
		else if(newval != oldval && ($scope.data.oDatosEvento.IdEvento.Codigo == "DV" || $scope.data.oDatosEvento.IdEvento.Codigo == "RC")){
			$scope.data.kngMonto = ($scope.data.kngDerecho * Number($scope.data.kngFactorTXT));
			$scope.data.kngMontoTXT = $scope.data.kngMonto;
		}
	},true);
	
	$scope.$watch('data.kngDerechoTXT', function(newval, oldval){
		if(newval != oldval && $scope.data.oDatosEvento.IdEvento.Codigo == "DA"){
			$scope.data.kngNuevaTXT = ($scope.data.kngDerechoTXT * Number($scope.data.kngFactorTXT));
		}
		else if(newval != oldval && ($scope.data.oDatosEvento.IdEvento.Codigo == "DV" || $scope.data.oDatosEvento.IdEvento.Codigo == "RC")){
			$scope.data.kngMontoTXT = ($scope.data.kngDerechoTXT * Number($scope.data.kngFactorTXT));
		}
	},true);
	
	$scope.$watchGroup(['data.Accion','data.TipoEvento'], function(newval,oldval){
		if ($scope.data.Accion==='Modificar' && $scope.data.TipoEvento==='DV'){
			$scope.data.MuestraMontoGastoTransferencia = true;
			$scope.data.MuestraMontoImpuestoExtranjero = true;
		}else{
			$scope.data.MuestraMontoGastoTransferencia = false;
			$scope.data.MuestraMontoImpuestoExtranjero = false;
		}
	},true);

	$scope.$watchGroup(['data.Accion','data.idInstrumentoTXT'], function(newval,oldval){
		if($scope.data.Accion!='Modificar' || $scope.data.TipoEvento!='DV'){
			$scope.data.kngMontoGastoTransferenciaDisabled = true;
			$scope.data.kngMontoImpuestoExtranjeroDisabled = true;
		}else{
			$scope.data.ConsultarGastoEventoCapitalDV();
		}
	},true);

//  	$scope.$watch('filtro.TipoEvento', function(newval,oldval){
	var initUI = function(codigo){
		//se deben ocultar o mostrar ciertos campos dependiendo del tipo de evento que se trate.
  		
	//  		if ($scope.filtro.TipoEvento.Codigo === 'DV'){ // DIVIDENDO EN DINERO
			if (codigo === 'DV' || codigo === 'RC'){
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.MuestraCantidadNueva = false;
				$scope.data.MuestraMontoCapital = false;
				$scope.data.MuestraMontoInteres = false;
				$scope.data.MuestraMoneda = true;
				$scope.data.MuestraTotal = true;
				$scope.data.MuestraMontoRetenido = true;
				$scope.data.ngDisabledSwitchRetencionISFUT = true;
			};
	//  		if ($scope.filtro.TipoEvento.Codigo === 'DA'){// DIVIDENDO EN ACCIONES
			if (codigo === 'DA'){// DIVIDENDO EN ACCIONES  			
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.MuestraCantidadNueva = true;
				$scope.data.MuestraMontoCapital = false;
				$scope.data.MuestraMontoInteres = false;
				$scope.data.MuestraMoneda = false;
				$scope.data.MuestraTotal = false;
			};

	//  		if ($scope.filtro.TipoEvento.Codigo === 'SL'){// SORTEO DE LETRAS
			if (codigo === 'SL'){// SORTEO DE LETRAS
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.MuestraCantidadNueva = true;
				$scope.data.MuestraMontoCapital = false;
				$scope.data.MuestraMontoInteres = false;
				$scope.data.MuestraMoneda = true;
				$scope.data.MuestraTotal = true;
			};

			// POR EL MOMENTO SOLO ESTA PROGRAMADO PARA DA Y DV, PARA TODO EL RESTO SE DEBE HABILITAR.
	//  		if (($scope.filtro.TipoEvento.Codigo != 'DA') && ($scope.filtro.TipoEvento.Codigo != 'DV') && ($scope.filtro.TipoEvento.Codigo != 'SL')){
			if ((codigo != 'DA') && (codigo != 'DV') && (codigo != 'SL')  && (codigo != 'RC')){  			
				$scope.data.MuestraInstrumentoNuevo = true;
				$scope.data.MuestraCantidadNueva = true;
				$scope.data.MuestraMontoCapital = true;
				$scope.data.MuestraMontoInteres = true;
				$scope.data.MuestraMoneda = true;
				$scope.data.MuestraTotal = true;
				$scope.data.MuestraMontoRetenido = true;
			};

			// ESTE CONTROL ES PARA EL CASO DE LAS MODIFICACIONES. 
	//  		if (typeof $scope.filtro.TipoEvento.Codigo == 'undefined') {
			if (typeof codigo == 'undefined') {  			
				if ($scope.data.IdTipoEventoCapitalTXT === 'DV'){ // DIVIDENDO EN DINERO
					$scope.data.MuestraInstrumentoNuevo = false;
					$scope.data.MuestraCantidadNueva = false;
					$scope.data.MuestraMontoCapital = false;
					$scope.data.MuestraMontoInteres = false;
					$scope.data.MuestraMoneda = true;
					$scope.data.MuestraTotal = true;
				};
				if ($scope.data.IdTipoEventoCapitalTXT === 'DA'){// DIVIDENDO EN ACCIONES
					$scope.data.MuestraInstrumentoNuevo = false;
					$scope.data.MuestraCantidadNueva = true;
					$scope.data.MuestraMontoCapital = false;
					$scope.data.MuestraMontoInteres = false;
					$scope.data.MuestraMoneda = false;
					$scope.data.MuestraTotal = false;
				};
				if ($scope.data.IdTipoEventoCapitalTXT === 'CA'){// CANJES
					$scope.data.MuestraMontoCapital = false;
					$scope.data.MuestraTotal = false;
					$scope.data.MuestraMontoInteres = false;
				};	
	//	  		if ($scope.filtro.TipoEvento.Codigo === 'SL'){// SORTEO DE LETRAS
				if (codigo === 'SL'){// SORTEO DE LETRAS
					$scope.data.MuestraInstrumentoNuevo = false;
					$scope.data.MuestraCantidadNueva = true;
					$scope.data.MuestraMontoCapital = false;
					$scope.data.MuestraMontoInteres = false;
					$scope.data.MuestraMoneda = true;
					$scope.data.MuestraTotal = true;
				};	  		
			}
			bcsComunes.ventanaTamanoAutomatico(idVentana);
  	};

  	// EN CASO DE CAMBIAR EL TIPO DE EVENTO O EL NÚMERO DEL EVENTO, SE DEBE BUSCAR Y TRAER LA INFORMACIÓN.
  	// EN CASO DE NO ENCONTRAR INFORMACIÓN ARROJA MENSAJE DE ERROR.
//	$scope.$watchGroup(['data.kngDividendo','filtro.TipoEvento'], function(newval,oldval){
	$scope.$watchGroup(['data.kngDividendo','data.oConfiguracionTipoEvento.seleccionado'], function(newval,oldval){		
		if 	(typeof $scope.data.kngDividendo !== 'undefined' && 
				    $scope.data.kngDividendo !== 0 &&
				    $scope.data.kngDividendo !== '' )
				    {
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario, 
				p_IdModulo : 'CUS', 
				p_UsaReglasNegocio : 'S',
				
	       		p_IdEventoCapital : $scope.data.kngDividendo || 0,
//	       		p_IdTipoEventoCapital : $scope.filtro.TipoEvento.Codigo || '' 
	       		p_IdTipoEventoCapital : $scope.data.oConfiguracionTipoEvento.seleccionado.Codigo|| ''
			};
			
			bcsServiciosEventoCapital.ConsultaEventoCapital(datos,function(result)
			{
				if (result.P_OUT_ERRORESCONTADOR === 0){
					$scope.data.IdTipoEventoCapital				= result.P_OUT_IDTIPOEVENTOCAPITAL;
					$scope.data.kngSecuencia					= result.P_OUT_SECUENCIA;
					$scope.data.TipoEventoW						= result.P_OUT_TIPOEVENTO;
					$scope.data.IdTipoInstrumento				= result.P_OUT_IDTIPOINSTRUMENTO;
					$scope.data.IdClasificacionTipoInstrumento	= result.P_OUT_IDCLASIFICACIONTIPOINSTRUMENTO;
					$scope.data.ClasificacionInstrumento		= result.P_OUT_TIPOINSTRUMENTO;
					$scope.data.IdInstrumento 					= result.P_OUT_IDINSTRUMENTO;
					$scope.data.Instrumento 					= result.P_OUT_NEMO;
					$scope.data.KngIdFechaPago					= result.P_OUT_FECHAPAGO;
					$scope.data.kngValorUnitario				= result.P_OUT_VALOR;
					$scope.data.kngFactor						= Number(result.P_OUT_FACTOR);
					$scope.data.Descripcion						= result.P_OUT_DESCRIPCIONEVENTO;
					$scope.data.ISFUTEventoCapital 				= result.P_OUT_ISFUT;//ISFUT EVENTO CAPITAL
					$scope.data.TasaEfectiva 					= result.P_OUT_TASAEFECTIVA;//TASA EFECTIVA EVENTO CAPITAL
				}
				else
			    {
					$scope.data.kngFechaCondicionLiquidacion = '1900-01-01';
				
					var i = 0;
		            for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
		            {
		            	//ERROR
		            	bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_BUSCA_EVENTO_CAPITAL_CLIENTE_ERROR"));
		            }
			    }
			},function(result)
			{
				console.log(JSON.stringify(result));
			});
		};
	},true);
	
	//CheckBox isfut(cliente) y retencion isfut para calcular retencion en base a tasa efectiva del evento capital
	
	$scope.$watch('data.switchISFUT', function(newval, oldval){
		if(newval === false) {
			$scope.data.kngMontoRetencionISFUTTXT = 0;
			$scope.data.ngDisabledSwitchRetencionISFUT = true;
			$scope.data.switchRetencionISFUT = false;
			$scope.data.kngMontoRetencionISFUTTXT = 0;
		}else{
			if($scope.data.ISFUTEventoCapital == "N" && $scope.data.TasaEfectiva <= 0 ){
				$scope.data.kngMontoRetencionISFUTTXT = 0;
				$scope.data.ngDisabledSwitchRetencionISFUT = true;
				$scope.data.switchRetencionISFUT = false;
			}else{
				$scope.data.ngDisabledSwitchRetencionISFUT = false;
				$scope.data.kngMontoRetencionISFUTTXT = Math.round(($scope.data.TasaEfectiva * $scope.data.kngMontoTXT) / 100);
			}			
			
		}
	});
	
	$scope.$watch('data.kngMontoTXT', function(newval, oldval){
		if($scope.data.switchISFUT !== undefined){
			if($scope.data.switchISFUT != false) {
				if($scope.data.ISFUTEventoCapital == "N" && $scope.data.TasaEfectiva <= 0){
					$scope.data.kngMontoRetencionISFUTTXT = 0;				
				}else{
					$scope.data.ngDisabledSwitchRetencionISFUT = false;
					$scope.data.kngMontoRetencionISFUTTXT = Math.round(($scope.data.TasaEfectiva * $scope.data.kngMontoTXT) / 100);
				}	
			}
		}
	});

	$scope.$watch('data.switchRetencionISFUT', function(newval, oldval){
		if(newval === false && !$scope.data.switchISFUT) {
			$scope.data.kngMontoRetencionISFUTTXT = 0;
		}else{
			if($scope.data.ISFUTEventoCapital == "N" && $scope.data.TasaEfectiva <= 0){
				$scope.data.kngMontoRetencionISFUTTXT = 0;
			}else{
				$scope.data.kngMontoRetencionISFUTTXT = Math.round(($scope.data.TasaEfectiva * $scope.data.kngMontoTXT) / 100);
			}
			
		}
	});

	$scope.$watch('data.kngMonto', function(newval, oldval){			$scope.FormatearMontos(newval, oldval ,'Monto' 	, false);	});		
	$scope.$watch('data.kngMontoTXT', function(newval, oldval){			$scope.FormatearMontos(newval, oldval ,'Monto'	, true);	});	
	
	$scope.$watch('data.kngMontoCapital', function(newval, oldval){		$scope.FormatearMontos(newval, oldval ,'MontoCap', false);	});
	$scope.$watch('data.kngMontoCapitalTXT', function(newval, oldval){	$scope.FormatearMontos(newval, oldval ,'MontoCap', true);	});

	$scope.$watch('data.oConfiguracionPortafolio.seleccionado', function(newval, oldval){

		if( newval != oldval && 
			($scope.data.oDatosEvento.IdEvento.Codigo == "DV" || $scope.data.oDatosEvento.IdEvento.Codigo == "RC") &&
			$scope.instrumentoUsaPagoDividendosOtrasMonedas && 
			$scope.data.oConfiguracionPortafolio.seleccionado ) {
			$scope.consultaConfiguracionMonedaPagoDividendosCliente($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio);
		}

	},true);

	$scope.$watch('data.oConfiguracionMoneda.seleccionado', function(newval, oldval){

		if(newval != oldval && ($scope.data.oDatosEvento.IdEvento.Codigo == "DV" || $scope.data.oDatosEvento.IdEvento.Codigo == "RC")) {
				$scope.consultarFactorPorMoneda($scope.data.oConfiguracionMoneda.seleccionado.DetalleIndicador,$scope.data.oConfiguracionMoneda.seleccionado.IdDetalleIndicador);
		}

	},true);

	$scope.$watch('data.kngFactorTXT', function(newval, oldval){
		if(newval != oldval && ($scope.data.oDatosEvento.IdEvento.Codigo == "DV" || $scope.data.oDatosEvento.IdEvento.Codigo == "RC")){
			
		}
	},true);

	$scope.FormatearMontos = function (newval, oldval, Campo, TXT){ // Formateo de los montos forzado. para los TXT no estaba funcionando
		if (newval == oldval){
			return;
		}
		Monto = newval;
		Potencia =	Math.pow(10,$scope.data.FormatoMoneda.cantidadDecimales);
		if(newval === false) {
			Monto = 0;
		}else{
			Monto =  Monto * Potencia;
			if ($scope.data.FormatoMoneda.trucaRedondea == 'R'){
				Monto = Math.round(Monto);
				Monto = Monto / Potencia;
			}else{
				Monto = Math.trunc(Monto);
				Monto = Monto / Potencia;
				}
			}
		if (Campo == 'Monto'){
			if (!TXT){
				$scope.data.kngMonto 	= Monto;
			}else{
				$scope.data.kngMontoTXT = Monto;			}
		}else if (Campo == 'MontoCap'){
			if (!TXT){
			$scope.data.kngMontoCapital 	= Monto;
		}else{
			$scope.data.kngMontoCapitalTXT 	= Monto;		}
		}
	}

	$scope.consultarConfiguracionInstrumentoPagoDividendosOtrasMonedas = function() {

		if( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'DV' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'RC'  ) {

			let configuracionInstrumentoDividendos = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1505",
							dataType: "json"
						}
					},
					filter: [
						{ field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'RV' },
						{ field: "Nemotecnico ", operator: "eq", value: $scope.data.oDatosEvento.EventoCapitalCliente[0].Nemo }					    
					],
					schema: {
						model: {
							fields: {
								IdDireccionFisica: { type: "string" },
								Predeterminado: { type: "string" }
							}
						}
					}
				}
			});
	
			configuracionInstrumentoDividendos.fetch().then(function() {
				$.each(configuracionInstrumentoDividendos.data(), function(key, value) {
					$scope.instrumentoUsaPagoDividendosOtrasMonedas = value.DividendoOtraMoneda === 'S' ? true : false;
					if( $scope.data.oDatosEvento.Accion == 'Modificar' && $scope.instrumentoUsaPagoDividendosOtrasMonedas ) {
						$scope.consultaConfiguracionMonedaPagoDividendosCliente($scope.data.oDatosEvento.EventoCapitalCliente[0].IdPortafolio);
					}
				});
			});

		}
	}

	$scope.consultaConfiguracionMonedaPagoDividendosCliente = function( IdPortafolio ) {

		if( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'DV' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'RC' ) {

				let configuracionDividendosCliente = new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering: true,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1343",
								dataType: "json"
							}
						},
						filter: [
							{ field: "IdPortafolio", operator: "eq", value: IdPortafolio},
							{ field: "CodigoTipoIndicador ", operator: "eq", value: 'DIV_DOM' }
						],
						schema: {
							model: {}
						}
					}
				});
	
				configuracionDividendosCliente.fetch().then(function() {
					$.each(configuracionDividendosCliente.data(), function(key, value) {
						$scope.configuracionDividendosClienteAmbasMonedas = value.NombreValorIndicador === 'USD' ? true : false;
						if ( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdMoneda == 'USD' ) {
							$scope.consultarFactorPorMoneda(value.NombreValorIndicador, value.ValorIndicador);
						} else {
							$scope.consultarIdDetalleIndicador();						
						}
					});
				});
			
		}
	}

	$scope.consultarFactorPorMoneda = function(IdMoneda, idValorIndicador) {

		if( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'DV' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'RC'  ) {

			let filtroMoneda = {};
			let consultaFactorPorMoneda = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700728",
							dataType: "json"
						}
					},
					filter: [
						{ field: "Secuencia", operator: "eq", value: $scope.data.oDatosEvento.EventoCapitalCliente[0].Secuencia },
						{ field: "Nemotecnico", operator: "eq", value: $scope.data.oDatosEvento.EventoCapitalCliente[0].Nemo }
					],
					schema: {
						model: {}
					}
				}
			});

			filtroMoneda = { field: "IdMoneda", operator: "eq", value: IdMoneda };		
			consultaFactorPorMoneda.definition.filter.push(filtroMoneda);
			consultaFactorPorMoneda.fetch().then(function() {
				if ( consultaFactorPorMoneda.data().length > 0) {
					$.each(consultaFactorPorMoneda.data(), function(key, value) {
						$scope.asignarNuevoFactor(value.Factor, idValorIndicador);
					});
				} else {
					var respuestaSimulada = {"P_OUT_MENSAJES":[{"id":0,"tipo":"ADVERTENCIA","codigo":"UI_NO_EXISTE_FACTOR_TABLA_VARIACIONCAPIALLOG","descripcion":""}]};
					bcsComunes.ventanaMsg({mensaje : $translate.instant("UI_NO_EXISTE_FACTOR_TABLA_VARIACIONCAPIALLOG"), conflictos: util.parseServerResponse(respuestaSimulada)}, $scope.idVentana);
					$scope.consultarIdDetalleIndicador();
				}
				
			});
			
		}
	}

	$scope.asignarNuevoFactor = function(nuevoFactor, idValorIndicador) {
		$scope.data.kngFactorTXT = Number(nuevoFactor);
		$scope.data.kngMonto = ($scope.data.kngDerecho * Number(nuevoFactor));
		$scope.data.kngMontoTXT = ($scope.data.kngDerechoTXT * Number(nuevoFactor));
		$scope.data.oConfiguracionMoneda.idDefecto = Number(idValorIndicador);
		$scope.data.oConfiguracionMoneda.idActual = Number(idValorIndicador);
	}

	$scope.consultarIdDetalleIndicador = function() {
		if( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'DV' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'RC'  ) {
		
			let consultaIdDetalleIndicador = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1549",
							dataType: "json"
						}
					},
					filter: [
						{ field: "CodigoTipoIndicador", operator: "eq", value: "DIV_DOM" }
					],
					schema: {
						model: {}
					}
				}
			});
			consultaIdDetalleIndicador.fetch().then(function() {
				$.each(consultaIdDetalleIndicador.data(), function(key, value) {
					if( value.DetalleIndicador === $scope.data.oDatosEvento.EventoCapitalCliente[0].IdMoneda ){
						$scope.asignarNuevoFactor($scope.data.oDatosEvento.EventoCapitalCliente[0].Factor, value.IdDetalleIndicador);
					}
				});
				
			});

		}
	}

	// ----------------------------------------------
	// BLOQUE WATCH (FIN)
	// ----------------------------------------------  
	
	$scope.setarSocioNegocio = function(){
				$scope.data.oConfiguracionSocioNegocio.filtros.EsCliente.value     		= '';		
				$scope.data.oConfiguracionSocioNegocio.filtros.EsProveedor.value   		= '';
				$scope.data.oConfiguracionSocioNegocio.filtros.EsEmpleado.value    		= '';
				$scope.data.oConfiguracionSocioNegocio.filtros.EsContraparte.value 		= '';
				$scope.data.oConfiguracionSocioNegocio.filtros.EstadoCliente.value     	= '';		
				$scope.data.oConfiguracionSocioNegocio.filtros.EstadoProveedor.value   	= '';
				$scope.data.oConfiguracionSocioNegocio.filtros.EstadoEmpleado.value    	= '';
				$scope.data.oConfiguracionSocioNegocio.filtros.EstadoContraparte.value 	= '';
	};
  		
/*************************************************************************
* CARGA INICIAL
**************************************************************************/

	$scope.data.Accion = $scope.data.oDatosEvento.Accion;
	//seteamos cabecera al crear
	$scope.data.TipoEventoTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente[0].TipoEvento;
	$scope.data.ISINTXT						= $scope.data.oDatosEvento.EventoCapitalCliente[0].ISIN;
	$scope.data.kngDividendoTXT				= $scope.data.oDatosEvento.EventoCapitalCliente[0].NumeroDividendo.toString();
	$scope.data.kngSecuenciaTXT				= $scope.data.oDatosEvento.EventoCapitalCliente[0].Secuencia.toString();
	$scope.data.KngIdFechaPago 				= $scope.data.oDatosEvento.EventoCapitalCliente[0].FechaPago;
	$scope.data.kngFactorTXT				= Number($scope.data.oDatosEvento.EventoCapitalCliente[0].Factor);
	$scope.data.DescripcionTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente[0].Descripcion;
	$scope.data.idInstrumentoTXT			= $scope.data.oDatosEvento.EventoCapitalCliente[0].Nemo;
	$scope.data.MonedaTXT					= $scope.data.oDatosEvento.EventoCapitalCliente[0].NombreMoneda;
	$scope.data.EventoCerradoValido			= false;
	$scope.instrumentoUsaPagoDividendosOtrasMonedas = false;
	$scope.configuracionDividendosClienteAmbasMonedas = false;
	
	$scope.data.MuestraMontoGastoTransferencia = false;
	$scope.data.kngMontoGastoTransferenciaDisabled = true;
	$scope.data.MuestraMontoImpuestoExtranjero = false;
	$scope.data.kngMontoImpuestoExtranjeroDisabled = true;
	
	if($scope.data.oDatosEvento.Accion === 'Crear' && ($scope.data.oDatosEvento.EventoCapitalCliente[0].IdEstado === 'CER' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdEstado === 'REA')){
		switch ($scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital){
			case 'DV':
			    $scope.data.EventoCerradoValido	= true;
			    break;
			case 'RC':
			    $scope.data.EventoCerradoValido	= true;
			    break;
			default:
				$scope.data.EventoCerradoValido	= false;
				}
				
	};
	
	$scope.consultarConfiguracionInstrumentoPagoDividendosOtrasMonedas();

	// SETEAMOS TODOS LOS VISIBLES EN TRUE
	$scope.data.MuestraInstrumentoNuevo = true;
	$scope.data.MuestraCantidadNueva = true;
	$scope.data.MuestraMontoCapital = true;
	$scope.data.MuestraMontoRetenido = true;
  	$scope.data.MuestraMontoInteres = true;
  	$scope.data.MuestraMoneda = true;
  	$scope.data.MuestraTotal = true;
	
//	console.log("Primera : " + $scope.data.Accion);
//	console.log("Segunda : " + $scope.data.MuestraInstrumentoNuevo);
	
  	$scope.data.TipoEvento = $scope.data.oDatosEvento.IdEvento.Codigo;
  	$scope.data.idInstrumento = $scope.data.oDatosEvento.IdInstrumento;
  	$scope.data.idFechaPago = $scope.data.oDatosEvento.IdFechaPago;
	
	$scope.data.kngDividendo = 0;

	$scope.data.idPostFiltroTXT				= 'CUS';
	$scope.data.IdTipoSocioNegocio			= 'CLI';
	$scope.data.oConfiguracionBoveda.idActual = 'EXT';

	$scope.data.disabledkngSecuencia = true;
	
	$scope.data.DividendoOptions = {format: 'n0'};	
	$scope.data.SecuenciaOptions = {format: 'n0'};
	$scope.data.kngNumDividendo = 0;
	$scope.data.NumDividendoOptions = {format: 'n0'};
	$scope.data.kngSecuencia = 0;	
	//$scope.data.porcentajeFactorOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.porcentajeFactorOptions = {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.kngFactor = 0;
	$scope.data.porcentajeValorFactorOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.kngValorFactor = 0;
	$scope.data.accionDisabled = true;
	$scope.data.kngFechaPago = $scope.SesionFechaSistema;
	//$scope.data.KngIdFechaPago = $scope.SesionFechaSistema;

	$scope.data.kngDerechoArchivo			= 0;
	$scope.data.kngDifDerecho				= 0;

	$scope.data.kngCantidadNuevaArchivo		= 0;
	$scope.data.kngDifCantidadNuevaArchivo	= 0;

	$scope.data.kngMontoBeneficioArchivo	= 0;
	$scope.data.kngDifMontoBeneficioArchivo	= 0;
	
	$scope.data.kngMontoInteresArchivo		= 0;
	$scope.data.kngDifMontoInteres			= 0;
	
	$scope.data.kngMontoCapitalArchivo		= 0;
	$scope.data.kngDifMontoCapital			= 0;
//	$scope.data.Monto = {format: 'n0'};

	$scope.data.disabledTipoEvento = true;
	//ISFUT
	//$scope.data.ngDisabledSwitchISFUT = true;
	$scope.data.ngDisabledSwitchRetencionISFUT = true;
	$scope.data.ISFUTEventoCapital = $scope.data.oDatosEvento.EventoCapitalCliente[0].ISFUT;
	if($scope.data.ISFUTEventoCapital=='S'){
		$scope.data.ngDisabledSwitchISFUT = false;
	}else{
		$scope.data.ngDisabledSwitchISFUT = true;	
	};
	$scope.data.TasaEfectiva=$scope.data.oDatosEvento.EventoCapitalCliente[0].TasaEfectiva;
	$scope.data.kngMontoRetencionISFUTTXT = 0;
	$scope.data.kngMontoTXT				  = $scope.data.oDatosEvento.EventoCapitalCliente[0].Total;
	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.data.refrescar = function() {
	copyPOJO();
	 
	};
/*************************************************************************
* CARGA INICIAL MODIFICACIÓN
**************************************************************************/		
if ($scope.data.oDatosEvento.EventoCapitalCliente && $scope.data.Accion=='Modificar')
{
	//Deshabilitamos todos-------------------------------
	$scope.data.disablekngDerechoTXT = false;
	$scope.data.MuestraCantidadNueva = false;
	$scope.data.MuestraTotal = false;
	$scope.data.MuestraInstrumentoNuevo = false;
	$scope.data.MuestraMontoCapital = false;
	$scope.data.MuestraMontoRetenido = false;
	$scope.data.MuestraMontoInteres = false;

	$scope.data.MuestraMontoGastoTransferencia = false;
	$scope.data.kngMontoGastoTransferenciaDisabled = true;
	$scope.data.MuestraMontoImpuestoExtranjero = false;
	$scope.data.kngMontoImpuestoExtranjeroDisabled = true;
	//---------------------------------------------------
	
	$scope.data.IdEventoCapitalCliente		= $scope.data.oDatosEvento.EventoCapitalCliente[0].IdEventoCapitalCliente;
	$scope.data.TipoEventoTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente[0].TipoEvento;
	$scope.data.IdTipoEventoCapitalTXT		= $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital;
	
	$scope.data.disabledTipoEvento = false;
	
	$scope.data.kngDividendoTXT				= $scope.data.oDatosEvento.EventoCapitalCliente[0].NumeroDividendo.toString();
	//$scope.data.kngSecuenciaTXT				= $scope.data.oDatosEvento.EventoCapitalCliente[0].SecuenciaCupon;
	$scope.data.ClasificacionInstrumentoTXT	= $scope.data.oDatosEvento.EventoCapitalCliente[0].TipoInstrumento;
  	$scope.data.idInstrumentoTXT			= $scope.data.oDatosEvento.EventoCapitalCliente[0].Nemo;
  	$scope.data.ISINTXT						= $scope.data.oDatosEvento.EventoCapitalCliente[0].ISIN;
  	$scope.data.idFechaPagoM 				= $scope.data.oDatosEvento.EventoCapitalCliente[0].FechaPago;
	$scope.data.kngValorUnitarioTXT			= $scope.data.oDatosEvento.EventoCapitalCliente[0].Valor;
	$scope.data.kngFactorTXT				= $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital == "SL" ? 1 : Number($scope.data.oDatosEvento.EventoCapitalCliente[0].Factor);;
	$scope.data.BovedaTXT					= Number($scope.data.oDatosEvento.EventoCapitalCliente[0].Factor);
	$scope.data.DescripcionTXT 				= $scope.data.oDatosEvento.EventoCapitalCliente[0].DescripcionEvento;
	$scope.data.ClienteTXT 					= $scope.data.oDatosEvento.EventoCapitalCliente[0].DocumentoIdentidad  + ' - ' + $scope.data.oDatosEvento.EventoCapitalCliente[0].NombreCliente;
	$scope.data.PortafolioTXT 				= '(' + $scope.data.oDatosEvento.EventoCapitalCliente[0].CodigoPortafolio + ') - ' + $scope.data.oDatosEvento.EventoCapitalCliente[0].Portafolio;
	$scope.data.RUTCAVALITXT 				= $scope.data.oDatosEvento.EventoCapitalCliente[0].RUTCavali;
	$scope.data.MonedaTXT 					= $scope.data.oDatosEvento.EventoCapitalCliente[0].NombreMoneda;
	$scope.data.kngDerechoTXT				= $scope.data.oDatosEvento.EventoCapitalCliente[0].CantidadDerecho;
	$scope.data.v_CantidadDerecho			= $scope.data.oDatosEvento.EventoCapitalCliente[0].CantidadDerecho;
	$scope.data.kngMontoInteres				= $scope.data.oDatosEvento.EventoCapitalCliente[0].MontoInteres;
	
	$scope.data.oConfiguracionTipoCartera.idActual	= $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoCartera;
	$scope.data.oConfiguracionBoveda.idActual		= $scope.data.oDatosEvento.EventoCapitalCliente[0].IdBoveda;

	$scope.data.kngDerechoArchivo			= $scope.data.oDatosEvento.EventoCapitalCliente[0].CantidadDerechoArchivo;
	$scope.data.kngDifDerecho				= $scope.data.oDatosEvento.EventoCapitalCliente[0].DiferenciaCantidadDerecho;

	$scope.data.kngCantidadNuevaArchivo		= $scope.data.oDatosEvento.EventoCapitalCliente[0].AccionesDisponiblesArchivo;
	$scope.data.kngDifCantidadNuevaArchivo	= $scope.data.oDatosEvento.EventoCapitalCliente[0].DiferenciaAccionesCantidadNueva;

	$scope.data.kngMontoBeneficioArchivo	= $scope.data.oDatosEvento.EventoCapitalCliente[0].MontoBeneficioArchivo;
	$scope.data.kngDifMontoBeneficioArchivo	= $scope.data.oDatosEvento.EventoCapitalCliente[0].DiferenciaMontoBeneficio;
	
	$scope.data.kngMontoInteresArchivo		= $scope.data.oDatosEvento.EventoCapitalCliente[0].MontoInteresArchivo;
	$scope.data.kngDifMontoInteres			= $scope.data.oDatosEvento.EventoCapitalCliente[0].DiferenciaMontoInteres;
	
	$scope.data.kngMontoCapitalArchivo		= $scope.data.oDatosEvento.EventoCapitalCliente[0].MontoCapitalArchivo;
	$scope.data.kngDifMontoCapital			= $scope.data.oDatosEvento.EventoCapitalCliente[0].DiferenciaMontoCapital;

	$scope.data.kngMontoRetenidoArchivo		= $scope.data.oDatosEvento.EventoCapitalCliente[0].MontoRetenidoArchivo;

	$scope.data.kngMontoImpuestoExtranjero	= String($scope.data.oDatosEvento.EventoCapitalCliente[0].MontoImpuestoExtranjero).replace(".",",");
	$scope.data.kngMontoGastoTransferencia	= String($scope.data.oDatosEvento.EventoCapitalCliente[0].GastoTransferencia).replace(".",",");
	
	if ($scope.data.IdTipoEventoCapitalTXT == "DV")
	{
		$scope.data.disablekngDerechoTXT = false;
	};
	
	$scope.data.kngNuevaTXT					= $scope.data.oDatosEvento.EventoCapitalCliente[0].CantidadNueva;
	
	$scope.data.kngMontoTXT					= $scope.data.oDatosEvento.EventoCapitalCliente[0].Total;
	$scope.data.v_MontoOriginal				= $scope.data.oDatosEvento.EventoCapitalCliente[0].Total;
	$scope.data.disabledMontoTXT = false; // //true = disabled
	
	
	$scope.data.kngMontoCapitalTXT          = $scope.data.oDatosEvento.EventoCapitalCliente[0].MontoCapital;
	
	
	$scope.data.disabledMontoCapital = false; // //true = disabled
	
	//$scope.data.kngMontoInteresTXT		= $scope.data.oDatosEvento.EventoCapitalCliente.ALGO;
	$scope.data.disabledMontoInteres = false // //true = disabled
	
	$scope.data.idPostFiltroTXT				= $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoCartera;
	
	$scope.data.disabledTipoEvento = false;
	$scope.data.IdTipoSocioNegocio = $scope.data.oDatosEvento.EventoCapitalCliente[0].TipoSocioNegocio;
	//ISFUT Cliente
	$scope.data.switchISFUT = $scope.data.oDatosEvento.EventoCapitalCliente[0].ISFUT == "S" ? true : false;
	$scope.data.switchRetencionISFUT = $scope.data.oDatosEvento.EventoCapitalCliente[0].RetencionISFUT == "S" ? true : false;
	$scope.data.kngMontoRetencionISFUTTXT = $scope.data.oDatosEvento.EventoCapitalCliente[0].MontoRetencionISFUT;	
	$scope.data.ISFUTEventoCapital = $scope.data.oDatosEvento.EventoCapitalCliente[0].ISFUTEventoCapital;
	$scope.data.TasaEfectiva = $scope.data.oDatosEvento.EventoCapitalCliente[0].TasaEfectiva;
	$scope.data.ngDisabledSwitchRetencionISFUT = true;
	if($scope.data.ISFUTEventoCapital=='S'){
		$scope.data.ngDisabledSwitchISFUT = false;
	}else{
		$scope.data.ngDisabledSwitchISFUT = true;	
	};
	
};

/*************************************************************************
* CONSULTAR APLICACIÓN DE GASTO SEGÚN INSTRUMENTO
**************************************************************************/		
	$scope.data.ConsultarGastoEventoCapitalDV = function()
	{
		if($scope.data.oDatosEvento.EventoCapitalCliente.length == 0) return;
		var idInstrumento = $scope.data.oDatosEvento.EventoCapitalCliente[0].IdInstrumento;
		var idCliente = $scope.data.oDatosEvento.EventoCapitalCliente[0].IdCliente;
		var datos = {};
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo = 'CUS';
		datos.p_IdInstrumento = idInstrumento;
		datos.p_IdCliente = idCliente;
		bcsServiciosEventoCapital.ConsultarGastoEventoCapitalDV(datos,
		function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_CONSULTA.length > 0){
				$scope.data.kngMontoGastoTransferenciaDisabled = (respuesta.P_OUT_CONSULTA[0].AplicaGasto == 'S') ? false : true;
				$scope.data.kngMontoImpuestoExtranjeroDisabled = (respuesta.P_OUT_CONSULTA[0].AplicaImpuesto == 'S') ? false : true;
			}else{
				$scope.data.kngMontoGastoTransferenciaDisabled = true;
				$scope.data.kngMontoImpuestoExtranjeroDisabled = true;
			}
		});
	}

/*************************************************************************
* INGRESO 
**************************************************************************/		
	$scope.data.CrearEventoCapitalCliente = function()
    {
		var datos = {};	
//		overlay.encendido = true; 
		overlay.encendido = false;
		//$scope.data.accionDisabled = undefined;
		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo = 'CUS';
		datos.p_UsaReglasNegocio = 'S';
		
		var eventosCapital = [];
				
   		if (parametros.Accion == "Modificar"){
   			
   			var CantidadNueva = 0;
   			var TotalNuevo = 0;
   			var MontoCap = 0;

			const fixMontoTXT = Math.round($scope.data.kngMontoTXT * 10000);
			const fixMontoCapitalTXT = Math.round($scope.data.kngMontoCapitalTXT * 10000);
			const fixMontoInteres = Math.round($scope.data.kngMontoInteres * 10000);

			const valmontoSuma = fixMontoCapitalTXT + fixMontoInteres;
   			
   			if (!(fixMontoTXT == valmontoSuma) && $scope.data.oDatosEvento.IdEvento.Codigo == "CC"){

				bcsComunes.ventanaMsgErr($translate.instant("UI_CORTE_CUPON_ERROR_MODIFICAR"), $scope.idVentana);
   				return;
   			}
   			
   			if($scope.data.oDatosEvento.IdEvento.Codigo == "DV"){
   				CantidadNueva = Number($scope.data.kngNuevaTXT);
   				TotalNuevo = Number($scope.data.kngMontoTXT);
   			}else{
   				if($scope.data.oDatosEvento.IdEvento.Codigo == "DA"){
   					CantidadNueva = Number($scope.data.kngNuevaTXT);
   					TotalNuevo = Number($scope.data.kngMontoTXT);
   				}else{
   					if($scope.data.oDatosEvento.IdEvento.Codigo == "SL"){
   						CantidadNueva = Number($scope.data.kngNuevaTXT);
   						TotalNuevo = Number($scope.data.kngMontoTXT);
   					}else {
   						CantidadNueva = Number($scope.data.kngNuevaTXT);
   						TotalNuevo = Number($scope.data.kngMontoTXT);
   						MontoCap = Number($scope.data.kngMontoCapitalTXT);
   					}
   				}
   			} 

			if($scope.data.oDatosEvento.IdEvento.Codigo == "SU" && (CantidadNueva != null ? (TotalNuevo / CantidadNueva) : 0) == 0){
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_PRECIO_SUSCRIPCION"));
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.SI){
						eventosCapital.push({
							"p_IdTipoEventoCapital" 				: $scope.data.oDatosEvento.IdEvento.Codigo || '',
							"p_IdEventoCapitalCliente"				: $scope.data.IdEventoCapitalCliente,
							"p_CantidadDerecho" 					: Number($scope.data.kngDerechoTXT) || 0,
							"p_CantidadNueva"						: CantidadNueva,
							
						
							"p_MontoCapital" 						: MontoCap,
							"p_Precio" 						        : CantidadNueva != null ? (TotalNuevo / CantidadNueva) : 0,
							
							
							"p_MontoInteres" 						: Number($scope.data.kngMontoInteres) || 0,
							"p_TipoCartera" 						: $scope.data.oConfiguracionTipoCartera.seleccionado.IdTipoCartera || '',
							"p_IdBoveda" 							: $scope.data.oConfiguracionBoveda.idActual,
							"p_Total" 								: TotalNuevo, //0, // ARREGLAR TARAO
							"p_CantidadDerechoArchivo" 				: 0,
							"p_DiferenciaCantidadDerechoArchivo" 	: 0,
							"p_CantidadNuevaArchivo" 				: 0,
							"p_DiferenciaCantidadNuevaArchivo" 		: 0,
							"p_MontoBeneficioArchivo" 				: 0,
							"p_DiferenciaMontoBeneficioArchivo" 	: 0,
							"p_MontoInteresArchivo" 				: 0,
							"p_DiferenciaMontoInteresArchivo" 		: 0,
							"p_MontoCapitalArchivo" 				: $scope.data.kngMontoCapitalTXT,
							"p_DiferenciaMontoCapitalArchivo" 		: 0,
							"p_FechaHoraUltimaActualizacion" 		: util.dateSPseconddate(util.date($scope.SesionFechaSistema).hour(util.date(new Date()).hour()).minute(util.date(new Date()).minute()).second(util.date(new Date()).second()).toDate()),
							"p_TipoSocioNegocio"					: $scope.data.oDatosEvento.EventoCapitalCliente[0].TipoSocioNegocio,
							"p_ISFUT" 								: $scope.data.switchISFUT ? 'S' : 'N',
							"p_RetencionISFUT" 						: $scope.data.switchRetencionISFUT ? 'S' : 'N',
							"p_MontoRetencionISFUT"					: Number($scope.data.kngMontoRetencionISFUTTXT) || 0,
						 "p_MontoGastoTransferencia"				: Number($scope.data.kngMontoGastoTransferencia) || (Number($scope.data.kngMontoGastoTransferencia.replace(',','.')) || 0),
						 "p_MontoImpuestoExtranjero"				: Number($scope.data.kngMontoImpuestoExtranjero) || (Number($scope.data.kngMontoImpuestoExtranjero.replace(',','.')) || 0)
		 
						});
					 
					 if( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'DV' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'RC'  || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'CA') {
						 eventosCapital[0].p_IdMoneda = $scope.data.oConfiguracionMoneda.seleccionado.DetalleIndicador || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdMoneda;
					 }
		 
					 datos.p_EventoCapital = eventosCapital;
					 
					 bcsServiciosEventoCapital.ModificarEventoCapitalCliente(datos,
					 function(respuesta){	  	
						 if (respuesta.P_OUT_ERRORESCONTADOR == "0"){
								 var vMsg = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
								 bcsComunes.ventanaPromesa(vMsg).then(function() {$scope.data.btnCerrar(true);});
						 }else{
							 if (respuesta.P_OUT_ERRORESCONTADOR > 0)
								 bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
						 }
					 });
					}
					else{
						bcsComunes.msg("info","info",$translate.instant("UI_CANCELAR_MODIFICACION"));
						return
					}
				});
			}else{
				eventosCapital.push({
					"p_IdTipoEventoCapital" 				: $scope.data.oDatosEvento.IdEvento.Codigo || '',
					"p_IdEventoCapitalCliente"				: $scope.data.IdEventoCapitalCliente,
					"p_CantidadDerecho" 					: Number($scope.data.kngDerechoTXT) || 0,
					"p_CantidadNueva"						: CantidadNueva,
					
				
					"p_MontoCapital" 						: MontoCap,
					"p_Precio" 						        : CantidadNueva != null ? (TotalNuevo / CantidadNueva) : 0,
					
					
					"p_MontoInteres" 						: Number($scope.data.kngMontoInteres) || 0,
					"p_TipoCartera" 						: $scope.data.oConfiguracionTipoCartera.seleccionado.IdTipoCartera || '',
					"p_IdBoveda" 							: $scope.data.oConfiguracionBoveda.idActual,
					"p_Total" 								: TotalNuevo, //0, // ARREGLAR TARAO
					"p_CantidadDerechoArchivo" 				: 0,
					"p_DiferenciaCantidadDerechoArchivo" 	: 0,
					"p_CantidadNuevaArchivo" 				: 0,
					"p_DiferenciaCantidadNuevaArchivo" 		: 0,
					"p_MontoBeneficioArchivo" 				: 0,
					"p_DiferenciaMontoBeneficioArchivo" 	: 0,
					"p_MontoInteresArchivo" 				: 0,
					"p_DiferenciaMontoInteresArchivo" 		: 0,
					"p_MontoCapitalArchivo" 				: $scope.data.kngMontoCapitalTXT,
					"p_DiferenciaMontoCapitalArchivo" 		: 0,
					"p_FechaHoraUltimaActualizacion" 		: util.dateSPseconddate(util.date($scope.SesionFechaSistema).hour(util.date(new Date()).hour()).minute(util.date(new Date()).minute()).second(util.date(new Date()).second()).toDate()),
					"p_TipoSocioNegocio"					: $scope.data.oDatosEvento.EventoCapitalCliente[0].TipoSocioNegocio,
					"p_ISFUT" 								: $scope.data.switchISFUT ? 'S' : 'N',
					"p_RetencionISFUT" 						: $scope.data.switchRetencionISFUT ? 'S' : 'N',
					"p_MontoRetencionISFUT"					: Number($scope.data.kngMontoRetencionISFUTTXT) || 0,
				 "p_MontoGastoTransferencia"				: Number($scope.data.kngMontoGastoTransferencia) || (Number($scope.data.kngMontoGastoTransferencia.replace(',','.')) || 0),
				 "p_MontoImpuestoExtranjero"				: Number($scope.data.kngMontoImpuestoExtranjero) || (Number($scope.data.kngMontoImpuestoExtranjero.replace(',','.')) || 0)
 
				});
			 
			 if( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'DV' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'RC'  || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'CA') {
				 eventosCapital[0].p_IdMoneda = $scope.data.oConfiguracionMoneda.seleccionado.DetalleIndicador || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdMoneda;
			 }
 
			 datos.p_EventoCapital = eventosCapital;
			 
			 bcsServiciosEventoCapital.ModificarEventoCapitalCliente(datos,
			 function(respuesta){	  	
				 if (respuesta.P_OUT_ERRORESCONTADOR == "0"){
						 var vMsg = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
						 bcsComunes.ventanaPromesa(vMsg).then(function() {$scope.data.btnCerrar(true);});
				 }else{
					 if (respuesta.P_OUT_ERRORESCONTADOR > 0)
						 bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
				 }
			 });
			}	
   		}
   		else
   		{
   			var CantidadNueva = 0;
   			var Total = 0;
   			
   			if (!($scope.data.kngMonto == $scope.data.kngMontoCapital + $scope.data.kngMontoInteres) && $scope.data.oDatosEvento.IdEvento.Codigo == "CC"){

				bcsComunes.ventanaMsgErr($translate.instant("UI_CORTE_CUPON_ERROR_MODIFICAR"), $scope.idVentana);
   				return;
   			}
   			
   			if($scope.data.oDatosEvento.IdEvento.Codigo == "DV" || $scope.data.oDatosEvento.IdEvento.Codigo == "RC"){
   				CantidadNueva = Number($scope.data.kngMonto);
   				Total = Number($scope.data.kngMonto);
   			}else{
   				if($scope.data.oDatosEvento.IdEvento.Codigo == "DA"){
   					CantidadNueva = Number($scope.data.kngNueva);
   					Total = Number($scope.data.kngMonto);
   				}else{
   					if($scope.data.oDatosEvento.IdEvento.Codigo == "SL"){
   						CantidadNueva = Number($scope.data.kngNueva);
   						Total = Number($scope.data.kngMonto);
   					}else {
   						CantidadNueva = Number($scope.data.kngNueva);
   						Total = Number($scope.data.kngMonto);
   					}
   				}
   			} 

   			var IdCliente = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
   			var IdProveedor = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdProveedor;
   			var IdEmpleado = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdEmpleado;
   			var IdContraparte =$scope.data.oConfiguracionSocioNegocio.seleccionado.IdContraparte;
   			var TipoSocioNegocio = $scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio;
   			if($scope.data.EventoCerradoValido){
   				IdContraparte		=	$scope.data.oConfiguracionEntidad.seleccionado.IdContraparte;
   				TipoSocioNegocio	=	$scope.data.oConfiguracionTipoSocioNegocioSinContraparte.seleccionado.IdTipoSocioDeNegocio
   			}
   			
   			var IdPortafolio;
   			if($scope.data.oConfiguracionPortafolio.seleccionado != null && $scope.data.oConfiguracionPortafolio.seleccionado != false){
   				IdPortafolio = Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)
   			}else{
   				IdPortafolio = -1;
   			}
   			
				eventosCapital.push({
	   				"p_IdTipoEventoCapital" : $scope.data.oDatosEvento.IdEvento.Codigo || '',
	   				"p_IdClasificacionTipoInstrumento" : $scope.data.oDatosEvento.IdEvento.IdClasificacionTipoInstrumento || '',
	   				"p_IdTipoInstrumento" : $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoInstrumento || '',
	   				"p_IdInstrumento" : $scope.data.oDatosEvento.EventoCapitalCliente[0].IdInstrumento || 0,
	   				"p_Nemo" : $scope.data.oDatosEvento.EventoCapitalCliente[0].Nemo || 0,
	   				"p_FechaPago" : util.dateSPdate($scope.data.KngIdFechaPago) ||'1900-01-01',
	   				"p_NumeroDividendo" : Number($scope.data.kngDividendoTXT) || 0,
	   				"p_Secuencia" : Number($scope.data.kngSecuenciaTXT) || 0,
	   				"p_ValorUnitario" : Number($scope.data.kngValorUnitario) || 0,
	   				"p_Factor" : Number($scope.data.kngFactorTXT) || 0,
	   				"p_Descripcion" : $scope.data.DescripcionTXT || '',
	   				"p_IdCliente" : Number(IdCliente) || -1,
	   				"p_IdEmpleado" : Number(IdEmpleado) || -1,
	   				"p_IdProveedor" : Number(IdProveedor) || -1,
	   				"p_IdContraparte" : Number(IdContraparte) || -1,
	   				"p_IdPortafolio" : Number(IdPortafolio),
	   				"p_IdMoneda" : $scope.data.oDatosEvento.EventoCapitalCliente[0].IdMoneda|| '',
	   				"p_CantidadDerecho" : Number($scope.data.kngDerecho) || 0,
	   				"p_CantidadNueva" : CantidadNueva,
	   				"p_Total" : Total,
	   				"p_MontoCapital" : Number($scope.data.kngMontoCapital) || 0,
	   				"p_MontoInteres" : Number($scope.data.kngMontoInteres) || 0,
	   				"p_TipoCartera" : $scope.data.oConfiguracionTipoCartera.seleccionado.IdTipoCartera || '',
	   				"p_IdBoveda" : $scope.data.oConfiguracionBoveda.idActual,
	   				"p_IdOrigen" : "MAN",
	   				"p_TipoSocioNegocio"	: TipoSocioNegocio,
	   				"p_ISFUT" 							: $scope.data.switchISFUT ? 'S' : 'N',
	   		   		"p_RetencionISFUT" 					: $scope.data.switchRetencionISFUT ? 'S' : 'N',
	   		   		"p_MontoRetencionISFUT"				: $scope.data.kngMontoRetencionISFUTTXT,
	   		   		"p_DocumentoIdentidad" : $scope.data.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto,
	   		   		"p_NombreCompleto" : $scope.data.oConfiguracionSocioNegocio.seleccionado.NombreCompleto,
	   		   		"p_IdSocioNegocio" : $scope.data.oConfiguracionSocioNegocio.seleccionado.IdSocioDeNegocio
	   			});
			
			if( $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'DV' || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdTipoEventoCapital === 'RC'  ) {
				eventosCapital[0].p_IdMoneda = $scope.data.oConfiguracionMoneda.seleccionado.DetalleIndicador || $scope.data.oDatosEvento.EventoCapitalCliente[0].IdMoneda;
			}
			datos.p_EventoCapital = eventosCapital;
			bcsComunes.ventanaOverlay(idVentana,true);
			$scope.data.accionDisabled = false;
			bcsServiciosEventoCapital.CrearEventoCapitalCliente(datos,
				function(respuesta){	  		
					if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
						var vMsg = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
							conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
							$scope.data.refrescar();
					}
					else{
						if (respuesta.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
								conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
							$scope.data.accionDisabled = true;
						}
					}
					bcsComunes.ventanaOverlay(idVentana,false);

			});
			
   		}
   	};
   	
/*************************************************************************
* INGRESO EXITOSO
**************************************************************************/
	var successCrearEventoCapital = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_SECUENCIA !== undefined && respuesta.P_OUT_SECUENCIA  > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_INGRESO_EXITO"));
			//bcsComunes.ventanaCerrar(admEveCap.winId);
			$scope.data.refrescar();
		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
	    }
	};
	

/*************************************************************************
* INGRESO CON ERRORES
**************************************************************************/	
	var errorCrearEventoCapital = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
		$scope.data.accionDisabled = true;
	};


	
/*************************************************************************
* MODIFICACIÓN EXITOSA
**************************************************************************/
	var successModificaEventoCapitalCliente = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		
		if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
           {	
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_INGRESO_EXITO"));
			$scope.data.btnCerrar(true);
			
				
           }
        else
           {
   			 if (respuesta.P_OUT_ERRORESCONTADOR > 0)  
               {
	              bcsComunes.ventanaMsg({
		          	                       mensaje:$translate.instant(""),
		                                   conflictos:util.parseServerResponse(respuesta)
		                                },$scope.IdVentana);
               }
             else
               if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0)
                 {
	                bcsComunes.ventanaMsg({
		            	                     mensaje:$translate.instant(""),
		                                     conflictos:util.parseServerResponse(respuesta)
		                                  },$scope.IdVentana); 
                 }
           }

	};
	

/*************************************************************************
* MODIFICACIÓN CON ERRORES
**************************************************************************/	
	var errorModificaEventoCapitalCliente = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
		$scope.data.accionDisabled = true;
	};
	
	var copyPOJO= function(){
		$scope.data2 = $scope.data;
		$scope.data = {};
		
		$scope.data.FormatoMoneda = $scope.data2.FormatoMoneda;
		$scope.data.Accion=$scope.data2.Accion;			
		$scope.data.CrearEventoCapitalCliente=$scope.data2.CrearEventoCapitalCliente;
		$scope.data.DescripcionTXT=$scope.data2.DescripcionTXT;
		$scope.data.DividendoOptions=$scope.data2.DividendoOptions;
		$scope.data.ISINTXT=$scope.data2.ISINTXT;
		$scope.data.KngIdFechaPago=$scope.data2.KngIdFechaPago;
		$scope.data.MonedaTXT=$scope.data2.MonedaTXT;
		$scope.data.MuestraArchivo=$scope.data2.MuestraArchivo;
		$scope.data.MuestraCantidadNueva=$scope.data2.MuestraCantidadNueva;
		$scope.data.MuestraInstrumentoNuevo=$scope.data2.MuestraInstrumentoNuevo;
		$scope.data.MuestraMoneda=$scope.data2.MuestraMoneda;
		$scope.data.MuestraMontoCapital=$scope.data2.MuestraMontoCapital;
		$scope.data.MuestraMontoInteres=$scope.data2.MuestraMontoInteres;
		$scope.data.MuestraMontoRetenido=$scope.data2.MuestraMontoRetenido;
		$scope.data.MuestraTotal=$scope.data2.MuestraTotal;
		$scope.data.MuestraVacali=$scope.data2.MuestraVacali;
		$scope.data.NumDividendoOptions=$scope.data2.NumDividendoOptions;
		$scope.data.SecuenciaOptions=$scope.data2.SecuenciaOptions;
		$scope.data.TipoEvento=$scope.data2.TipoEvento;
		$scope.data.TipoEventoTXT=$scope.data2.TipoEventoTXT;
		$scope.data.TipoSocioNegocio=$scope.data2.TipoSocioNegocio;
		$scope.data.accionDisabled=true;
		$scope.data.btnCerrar=$scope.data2.btnCerrar;
		$scope.data.configuracionMontos=$scope.data2.configuracionMontos;
		$scope.data.disabledMontoCapital=$scope.data2.disabledMontoCapital
		$scope.data.disabledMontoInteres=$scope.data2.disabledMontoInteres;
		$scope.data.disabledMontoTXT=$scope.data2.disabledMontoTXT;
		$scope.data.disabledTipoEvento=$scope.data2.disabledTipoEvento;
		$scope.data.disabledkngSecuencia=$scope.data2.disabledkngSecuencia;
		$scope.data.disablekngDerechoTXT=$scope.data2.disablekngDerechoTXT;
		$scope.data.formatoMonto=$scope.data2.formatoMonto;
		$scope.data.idInstrumento=$scope.data2.idInstrumento;
		$scope.data.idInstrumentoTXT=$scope.data2.idInstrumentoTXT;
		$scope.data.idPostFiltroTXT=$scope.data2.idPostFiltroTXT;
		$scope.data.kngCantidadNuevaArchivo=$scope.data2.kngCantidadNuevaArchivo;
		$scope.data.kngDerechoArchivo=$scope.data2.kngDerechoArchivo;
		$scope.data.kngDifCantidadNuevaArchivodata2=$scope.data2.kngDifCantidadNuevaArchivo;
		$scope.data.kngDifDerecho=$scope.data2.kngDifDerecho;
		$scope.data.kngDifMontoBeneficioArchivo=$scope.data2.kngDifMontoBeneficioArchivo;
		$scope.data.kngDifMontoCapital=$scope.data2.kngDifMontoCapital;
		$scope.data.kngDifMontoInteres=$scope.data2.kngDifMontoInteres;
		$scope.data.kngDividendo=$scope.data2.kngDividendo;
		$scope.data.kngDividendoTXT=$scope.data2.kngDividendoTXT;
		$scope.data.kngFactor=$scope.data2.kngFactor;
		$scope.data.kngFactorTXT=$scope.data2.kngFactorTXT;
		$scope.data.kngFechaPago=$scope.data2.kngFechaPago;
		$scope.data.kngMontoBeneficioArchivo=$scope.data2.kngMontoBeneficioArchivo;
		$scope.data.kngMontoCapitalArchivo=$scope.data2.kngMontoCapitalArchivo;
		$scope.data.kngMontoInteresArchivo=$scope.data2.kngMontoInteresArchivo;
		$scope.data.kngNumDividendo=$scope.data2.kngNumDividendo;
		$scope.data.kngSecuencia=$scope.data2.kngSecuencia;
		$scope.data.kngSecuenciaTXT=$scope.data2.kngSecuenciaTXT;
		$scope.data.kngValorFactor=$scope.data2.kngValorFactor;
		$scope.data.oConfiguracionBoveda=$scope.data2.oConfiguracionBoveda;
		$scope.data.oConfiguracionClasificacion=$scope.data2.oConfiguracionClasificacion;
		$scope.data.oConfiguracionMoneda=$scope.data2.oConfiguracionMoneda;
		$scope.data.oConfiguracionTipoCartera=$scope.data2.oConfiguracionTipoCartera;
		$scope.data.oConfiguracionTipoEvento=$scope.data2.oConfiguracionTipoEvento;
		$scope.data.oConfiguracionTipoInstrumento=$scope.data2.oConfiguracionTipoInstrumento;
		$scope.data.oDatosEvento=$scope.data2.oDatosEvento;
		$scope.data.porcentajeFactorOptions=$scope.data2.porcentajeFactorOptions;
		$scope.data.porcentajeValorFactorOptions=$scope.data2.porcentajeValorFactorOptions;
		$scope.data.refrescar=$scope.data2.refrescar;
		$scope.data.oConfiguracionTipoSocioNegocio= $scope.data2.oConfiguracionTipoSocioNegocio;
		$scope.data.oConfiguracionTipoSocioNegocio.idActual = 'CLI';
		$scope.data.oConfiguracionTipoSocioNegocio.activado = true;
		$scope.data.oConfiguracionPortafolio= $scope.data2.oConfiguracionPortafolio;
		$scope.data.oConfiguracionPortafolio.seleccionado = null;
		$scope.data.oConfiguracionPortafolio.activado = true;
		$scope.data.oConfiguracionSocioNegocio= $scope.data2.oConfiguracionSocioNegocio;
		$scope.data.oConfiguracionSocioNegocio.idActual = null;
		$scope.data.oConfiguracionSocioNegocio.activado = true;
		$scope.data.EventoCerradoValido	= $scope.data2.EventoCerradoValido;
		$scope.data.showPortafolio= true;
		
		$scope.data.oConfiguracionTipoCartera.idActual= 'CUS';

		$scope.data.idFechaPago = $scope.data2.idFechaPago;
	}
		
 	//Primer evento - filtro inicial
	$timeout(function(){
		$scope.data.oConfiguracionTipoEvento.filtros.read = true;
		$scope.data.oConfiguracionMoneda.filtros.read = true;
		initUI($scope.data.oDatosEvento.IdEvento.Codigo);
	},1000);
	
});


