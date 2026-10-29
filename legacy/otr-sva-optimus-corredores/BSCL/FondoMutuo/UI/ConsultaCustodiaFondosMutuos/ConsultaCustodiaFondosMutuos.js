angular.module('ModuloFFM').controller('ConsultaCustodiaFFMCtrl',
function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaCustodiaFondosMutuos();
	var apiGridCustodiaFondosMutuos = conf.options.dataSource;
	$scope.decimalesMaxCustodia = 0;
	
	var setTemplateCantidadCustodia = function(dataItem){
		var decimales = $scope.decimalesMaxCustodia > 0 ? $scope.decimalesMaxCustodia : dataItem.DecimalesCuota;
		return util.formateaNumero(dataItem.CantidadCustodia, decimales);
	};
	
	if(conf.options){
		if(conf.options.columns){
			conf.options.columns.forEach(function(col) {
				if (col.field === 'CantidadCustodia') {
					col.template = function(dataItem) {
						return "<label>" + setTemplateCantidadCustodia(dataItem) + "</label>";
					};
				}
			});
		}
	}
	
	$scope.defaultOptions = conf.options;
	$scope.buscarCustodia = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'FMM';
	datos.p_UsaReglasNegocio = 'S';
	
	$scope.data = {};
	$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaValor = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.oClientePortafolio = {
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
   	
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	/*
	var dataSourceConfiguracion = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
					dataType: "json"
				}		
			},
			schema: {
		     	model: {
		     		fields: { Valor : { type: "string"}	}
		     	}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'APV.Habilitado' },
			         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});
	
	dataSourceConfiguracion.fetch(function(){
		if(dataSourceConfiguracion.data().length > 0){
			if(dataSourceConfiguracion.data()[0].Valor === 'S'){
				$scope.MostrarProducto = true;
			}
		}
 	});
 	*/
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.ingresarAporteNormal = function(datosGrilla)
	{
		if(!datosGrilla || datosGrilla.length === 0) return null;
		bcsComunes.ventanaAbrir("IngresarFFM", {IdSerieFondo : datosGrilla.Nemotecnico,
												IdSocioDeNegocio : datosGrilla.IdSocioDeNegocio,
												IdCliente : datosGrilla.IdCliente,
												IdPortafolio : datosGrilla.IdPortafolio,
												IdTipoMovimientoFondoMutuo : 'APONORM'} );
	};
	
	$scope.ingresarRescateNormal = function(datosGrilla)
	{
		if(!datosGrilla || datosGrilla.length === 0) return null;
		bcsComunes.ventanaAbrir("IngresarFFM", {IdSerieFondo : datosGrilla.Nemotecnico,
												IdCliente : datosGrilla.IdCliente,
												IdSocioDeNegocio : datosGrilla.IdSocioDeNegocio,
												IdPortafolio : datosGrilla.IdPortafolio,
												IdTipoMovimientoFondoMutuo : 'RESNORM'} );
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.formData.setFondo		= "-1";
		$scope.data.kngFecha			= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.IdPortafolio		= false;
		
		$scope.oSocioNegocio.idActual	= false;
		$scope.oProducto.idActual		= false;
		$scope.oPortafolio.idActual		= false;
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid)
	{
		$scope.ajustaVentana();
	}

	$scope.buscar = function ()
	{	
		apiGridCustodiaFondosMutuos.setCallParameters($scope.preparaParametros($scope.data));
		apiGridCustodiaFondosMutuos.read();			
	};

	$scope.$watch('apiGridCustodiaFondosMutuos.dataSource.ds._data', function(newval, oldval){
        if(newval !== undefined && newval.length > 0 && (newval !== oldval)) {
            $scope.data.kngFechaValor = newval[0].FechaValorCuota;
            
            $scope.decimalesMaxCustodia = 0;
            
            for(var i = 0; i < newval.length; i++){
				var dec = newval[i].DecimalesCuota || 0;
				if(dec > $scope.decimalesMaxCustodia) $scope.decimalesMaxCustodia = dec;
			}           
        }

        if(newval !== undefined && oldval !== undefined){
        	if ((apiGridCustodiaFondosMutuos.data().length) === 0)  
		     {
			   bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_NO_EXISTE_INFORMACION_HISTORICA"));
			   return;
		     };
        }
        
    });
	
	$scope.preparaParametros = function(filtros)
	{
		parametrosConsulta = {};
		parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
		parametrosConsulta.p_IdModulo = "FMM";
		parametrosConsulta.p_AgrupaPorBoveda = "S";
		parametrosConsulta.p_IdCliente = 0;
		parametrosConsulta.p_IdPortafolio = 0;
		parametrosConsulta.p_IdProductoAgrupador = null;
		parametrosConsulta.p_IdInstrumento = ($scope.filtros.FondoMutuo) ? $scope.filtros.FondoMutuo.IdInstrumento : 0;
		parametrosConsulta.p_FechaConsulta = util.dateSPdate($scope.data.kngFecha);
		
		if($scope.MostrarProducto)
		{
			if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
				parametrosConsulta.p_IdCliente = Number($scope.oSocioNegocio.seleccionado.IdCliente);
			
			if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				parametrosConsulta.p_IdPortafolio = Number($scope.oPortafolio.seleccionado.IdPortafolio);
			}
			else if($scope.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				parametrosConsulta.p_IdPortafolio = Number($scope.oPortafolio.seleccionado.IdPortafolio);
			}
			else if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio === undefined)
			{
				if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) !== 0)
				{
					if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) === 9999)
					{
						parametrosConsulta.p_IdProductoAgrupador = 0;
					}
					else
					{
						parametrosConsulta.p_IdProductoAgrupador = Number($scope.oProducto.seleccionado.IdProductoAgrupador);
					}
				}
			}
		}
		else
		{
			parametrosConsulta.p_IdCliente = ($scope.data.oClientePortafolio.seleccionado) ? $scope.data.oClientePortafolio.seleccionado.IdCliente : 0;
			parametrosConsulta.p_IdPortafolio = ($scope.data.oClientePortafolio.seleccionado) ? $scope.data.oClientePortafolio.seleccionado.IdPortafolio : 0;
		}
		
		return parametrosConsulta;
	};
	
	$scope.limpiarFiltros();
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});