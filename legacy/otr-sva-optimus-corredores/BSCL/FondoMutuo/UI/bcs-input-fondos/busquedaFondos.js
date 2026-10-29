angular.module('ModuloFFM').controller('BusquedaFondosCtrl',
		function($scope,$resource,$rootScope,bcsComunes,$translate,bcsConfiguracionFFM)
{

	$scope.data	= {};	
	var overlay = bcsComunes.overlay;
	$scope.formData = {};
	$scope.formData.Todos = 'S';
	
	$scope.formData.PerfilParametro = '0';
	$scope.formData.filtrosEmisor = {
		IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	$scope.formData.filtroPerfil = {
		Jerarquia : { "field"	: "Jerarquia", operator: "gte", "value": undefined}	
	}
	var filtrosFondos = [];
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FFM';
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.formData.UsaEnCustodia = true;
	
	
	console.log(parametros.filtros)
	if (parametros.filtros)
	{
		if(parametros.filtros.IdEstado && parametros.filtros.IdEstado.value){
			filtrosFondos.push({ field: "IdEstado", operator: "eq", value:parametros.filtros.IdEstado.value } );
		}
		if(parametros.filtros.BloqueoAporte && parametros.filtros.BloqueoAporte.value){
			filtrosFondos.push({ field: "BloqueoAporte", operator: "eq", value:parametros.filtros.BloqueoAporte.value } );
		}
		if(parametros.filtros.BloqueoRescate && parametros.filtros.BloqueoRescate.value){
			filtrosFondos.push({ field: "BloqueoRescate", operator: "eq", value:parametros.filtros.BloqueoRescate.value } );
		}
		if(parametros.filtros.Jerarquia && parametros.filtros.Jerarquia.value){
			$scope.formData.JerarquiaParametro = parametros.filtros.Jerarquia.value;
			$scope.formData.Todos = 'N';
			$scope.formData.filtroPerfil.Jerarquia.value =  $scope.formData.JerarquiaParametro;
			filtrosFondos.push({ field: "Jerarquia", operator: "gte", value: $scope.formData.JerarquiaParametro });
		}
		if(parametros.filtros.IdMoneda && parametros.filtros.IdMoneda.value){
			$scope.formData.MonedaParametro = parametros.filtros.IdMoneda.value;
			filtrosFondos.push({ field: "IdMoneda", operator:"eq", value: $scope.formData.MonedaParametro});
		}
		if(parametros.filtros.IdFondo && parametros.filtros.IdFondo.value){
			$scope.formData.IdFondoParametro = parametros.filtros.IdFondo.value;
			filtrosFondos.push({ field: "IdFondo", operator:"eq", value: $scope.formData.IdFondoParametro});
		}
		if(parametros.filtros.IdCliente){
			$scope.formData.IdClienteParametro = parametros.filtros.IdCliente.value;
			filtrosFondos.push({ field: "IdCliente", operator:"eq", value: $scope.formData.IdClienteParametro});
		}
		if(parametros.filtros.IdPortafolio){
			$scope.formData.IdPortafolioParametro = parametros.filtros.IdPortafolio.value;
			filtrosFondos.push({ field: "IdPortafolio", operator:"eq", value: $scope.formData.IdPortafolioParametro});
			if(parametros.soloCustodia === 'S'){
				$scope.formData.EnCustodia = true;
			}else{
				$scope.formData.UsaEnCustodia = false;
			}
		}
		if(parametros.filtros.MoneyMarket){
			$scope.formData.MoneyMarketParametro = parametros.filtros.MoneyMarket.value;
			filtrosFondos.push({field: "MoneyMarket",operator:"eq",value:$scope.formData.MoneyMarketParametro});
		}
		if(parametros.filtros.IdMoneda){
			$scope.formData.IdMonedaParametro = parametros.filtros.IdMoneda.value;
			filtrosFondos.push({field: "IdMoneda",operator:"eq",value:$scope.formData.IdMonedaParametro});
		}
		if(parametros.filtros.IdEmisor){
			$scope.formData.IdEmisor = parametros.filtros.IdEmisor.value;
			filtrosFondos.push({field: "IdEmisor",operator:"eq",value:$scope.formData.IdEmisor});
			$scope.formData.IdEmisorDefecto=$scope.formData.IdEmisor;
		}else{$scope.formData.IdEmisorDefecto='0';};
		
		if(parametros.filtros.EsFondoNacional){
			$scope.formData.UsaEsFondoNacional = true;
			$scope.formData.EsFondoNacional = false;
		}else
			{
				$scope.formData.UsaEsFondoNacional = false;
			}
		
		
	}
	
	
	$scope.buscarFondo = function(){
		filtrosFondos = [];
		if(parametros.filtros){
			if(parametros.filtros.IdEstado && parametros.filtros.IdEstado.value){
				filtrosFondos.push({ field: "IdEstado", operator: "eq", value:parametros.filtros.IdEstado.value } );
			}
			if(parametros.filtros.CambioFondo && parametros.filtros.CambioFondo.value){
				filtrosFondos.push({ field: "CambioFondo", operator: "eq", value:parametros.filtros.CambioFondo.value } );
			}
			if(parametros.filtros.EsFondoNacional && parametros.filtros.EsFondoNacional.value){
				filtrosFondos.push({ field: "EsFondoNacional", operator: "eq", value:parametros.filtros.EsFondoNacional.value } );
			}
			if(parametros.filtros.BloqueoAporte && parametros.filtros.BloqueoAporte.value){
				filtrosFondos.push({ field: "BloqueoAporte", operator: "eq", value:parametros.filtros.BloqueoAporte.value } );
			}
			if(parametros.filtros.BloqueoRescate && parametros.filtros.BloqueoRescate.value){
				filtrosFondos.push({ field: "BloqueoRescate", operator: "eq", value:parametros.filtros.BloqueoRescate.value } );
			}
			if(parametros.filtros.IdFondo && parametros.filtros.IdFondo.value){
				$scope.formData.IdFondoParametro = parametros.filtros.IdFondo.value;
				filtrosFondos.push({ field: "IdFondo", operator:"eq", value: $scope.formData.IdFondoParametro});
			}
		}
		
		if($scope.formData.IdTipoFondo)
			filtrosFondos.push({ field: "IdTipoFondo", operator: "eq", value: $scope.formData.IdTipoFondo } );
		if($scope.formData.Perfil) 
			filtrosFondos.push({ field: "Jerarquia", operator: "gte", value: ($scope.formData.PerfilSeleccionado.Jerarquia) ? $scope.formData.PerfilSeleccionado.Jerarquia : 1} );
		if($scope.formData.ComisionSalida)
			filtrosFondos.push({ field: "EsComisionSalida", operator: "eq", value: $scope.formData.ComisionSalida } );
		if($scope.formData.IdEmisor)
			filtrosFondos.push({ field: "IdEmisor", operator: "eq", value: $scope.formData.IdEmisor } );
		if($scope.formData.IdMoneda)
			filtrosFondos.push({ field: "IdMoneda", operator: "eq", value: $scope.formData.IdMoneda } );
		if($scope.formData.MoneyMarket)
			filtrosFondos.push({ field: "MoneyMarket", operator: "eq", value: $scope.formData.MoneyMarket } );
		if($scope.formData.Diferido)
			filtrosFondos.push({ field: "Diferido", operator: "eq", value: $scope.formData.Diferido } );
		if($scope.formData.CambioFondo)
			filtrosFondos.push({ field: "CambioFondo", operator: "eq", value: $scope.formData.CambioFondo } );
		if($scope.formData.IdFamiliaFondo)
			filtrosFondos.push({ field: "IdFamiliaFondo", operator: "eq", value: $scope.formData.IdFamiliaFondo } );
		if($scope.formData.EsFondoNacional && !parametros.filtros.CambioFondo)
			filtrosFondos.push({ field: "EsFondoNacional", operator: "eq", value:'S'} );
		
		if($scope.formData.EnCustodia){
			if ($scope.formData.IdClienteParametro)
				filtrosFondos.push({ field: "IdCliente", operator:"eq", value: $scope.formData.IdClienteParametro});
			if ($scope.formData.IdPortafolioParametro)
				filtrosFondos.push({ field: "IdPortafolio", operator:"eq", value: $scope.formData.IdPortafolioParametro});
				
			$scope.gridApiFondosCustodia.dataSource.filter(filtrosFondos);
			$scope.gridApiFondosCustodia.refresh();
		} else {
			$scope.gridApi.dataSource.filter(filtrosFondos);
			$scope.gridApi.refresh();
		}
	};
	

	var testFiltro = {}; //{ field: "IdCliente", operator: "eq", value: Number(pcdg.IdCliente ) };
	
	
	var dsCustodia = bcsConfiguracionFFM.getDataSourceFondosCustodia();
	dsCustodia.definition.filter = filtrosFondos;
	
	var ds = bcsConfiguracionFFM.getDataSourceFondo();
	ds.definition.filter = filtrosFondos;

	var gridDefault = {
			columns :
				[	
				 	 bcsConfiguracionFFM.getConfiguracionColumna("IdSerieFondo")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("NombreSerieFondo")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("Emisor")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("IdMoneda")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("NombreTipoFondo")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("Familia")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("NombreEstado")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("Diferido")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("ValorCuota")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("MontoMinimo")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("MontoMaximo")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("EstadoAporteComercial")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("EstadoAporteBackOffice")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("EstadoRescateComercial")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("EstadoRescateBackOffice")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("BloqueoAporte")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("BloqueoRescate")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("NombreCategoriaPerfilFondoMutuo")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("ValorDolarAporte")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("ValorDolarRescate")
				 	,bcsConfiguracionFFM.getConfiguracionColumna("TipoInstrumento")
				 	
				],
		  		selectable : "row",
		  		filterable : {
		  			mode : "row"
		  		},	
		  		autoBind: false,
		  		allowCopy: true,
				resizable: true,
				height:250,
		  		dataSource: ds
			};
	
	$scope.gridConfigBusquedaFondos = angular.copy(gridDefault);
	$scope.gridConfigBusquedaFondosCustodia = gridDefault;
	$scope.gridConfigBusquedaFondosCustodia.dataSource = dsCustodia;
	
	$scope.seleccionarFondo = function(){
		if($scope.formData.EnCustodia){
			bcsComunes.ventanaData(idVentana,$scope.seleccionadoFondoCustodia);
			bcsComunes.ventanaCerrar(idVentana);
		} else {
			bcsComunes.ventanaData(idVentana,$scope.seleccionadoFondo);
			bcsComunes.ventanaCerrar(idVentana);
		}
	}	
	
	$scope.limpiarFiltros = function()
	{
		$scope.formData.Perfil = 0;
		$scope.formData.IdTipoFondo = 0;
		$scope.formData.ComisionEntrada = 0;
		$scope.formData.IdEmisor = 0;
		$scope.formData.IdMoneda = 0;
		$scope.formData.MoneyMarket = 0;
		$scope.formData.Diferido = 0;
		$scope.formData.IdFamiliaFondo = 0;
		$scope.formData.EsFondoNacional = false;
	};

	$('div#Win_busquedaFondos').css("height", "443px")
	
});	