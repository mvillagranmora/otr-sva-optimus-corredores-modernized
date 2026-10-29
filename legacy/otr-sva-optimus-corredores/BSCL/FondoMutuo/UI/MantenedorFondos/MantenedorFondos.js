angular.module('ModuloFFM').controller('MantenedorFondosCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	var conf = bcsConfiguracionFFM.getConsultaInstrumentosFondosMutuos();
	$scope.defaultOptions = conf.options;
	$scope.buscarInstrumentosFMM = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	var datasourceComisionMaxima = bcsConfiguracionFFM.getDataSourceComisionesFondoMutuoMaxima();
	var datasourceComisionActiva = bcsConfiguracionFFM.getDataSourceComisionesFondoMutuoActiva();
	   
    
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.formData.filtrosTipoInstrumento = {
		IdClasificacionTipoInstrumento: {"field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value":"FM"}
	}
	
	$scope.ingresarInstrumentoFMM = function()
	{
		$scope.formData.abrirVentanaFFMM({});
	};
	
	$scope.formData.abrirVentanaFFMM = function(params){
		var id = bcsComunes.ventanaAbrir("IngresarInstrumentoFMM",params,false,false,$scope.winID); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){
        	conf.options.dataSource.read();
        	$scope.datos.kNgModel = undefined;
        }); 
	}
	
	$scope.modificarInstrumentoFMM = function (items,edicion) {
		if(items && items.length > 0){
			$scope.formData.abrirVentanaFFMM({IdSerieFondo : items[0].IdSerieFondo,Edicion:edicion});
		}
  	};
  	
  	$scope.mantenedorRegimen = function(){
  		var id = bcsComunes.ventanaAbrir("MantenedorRegimenFondoMutuo",{},false); 
  	}
  	
  	$scope.indicadoresFMM = function(){
  		bcsComunes.ventanaAbrir("MantenedorValorCuota",{}); 
  	}
  	
  	$scope.mantenedorFamiliasFFM = function(){
  		bcsComunes.ventanaAbrir("MantenedorFamiliasFFM",{}); 
  	}
  	
  	$scope.tipoCambioFMM = function(){
  		bcsComunes.ventanaAbrir("MantenedorValorTipoCambio", {});
  	}
	
  	$scope.administracionLiquidezFMM = function(){
  		bcsComunes.ventanaAbrir("MantenedorAdministracionLiquidez",{}); 
  	}
  	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdInstrumento = 0;
		$scope.filtros.IdEstado = 0;
		$scope.filtros.IdEmisor = 0;
		$scope.formData.setFondo = "-1";
	};
	
	$scope.comisionesFMM = function(items){
		if(items.length <= 0){
			bcsComunes.msg("warning","warning","Debe seleccionar un fondo.");
			return false;
		}
		//Obtiene IdComisión Maxima
		var filterComisionMax = new Array();
	        filterComisionMax.push({field: "IdSerieFondo", operator: "eq", value: items[0].IdSerieFondo});
			filterComisionMax.push({ field: "Estado", operator: "eq", value: 'ACT' });


		datasourceComisionActiva.query({filter:filterComisionMax}).then(function(){
			$scope.formData.IdComision = datasourceComisionActiva.data().length > 0 ? datasourceComisionActiva.data()[0].IdComision : 0;
			bcsComunes.ventanaAbrir("ModificarComisionesFFM",{IdSerieFondo : items[0].IdSerieFondo, NombreFamilia: items[0].NombreFamilia, IdComision: $scope.formData.IdComision});
			return true;
		});
			
  	}
	
	$scope.limpiarFiltros();
});