angular.module('ModuloFFM').controller('ModificarComisionesCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	
	//var conf = bcsConfiguracionFFM.getTramoComisionesFondoMutuo();
	var dataSourceComision = bcsConfiguracionFFM.getDataSourceComisionesFondoMutuo();
	var dataSourceTramoComision = bcsConfiguracionFFM.getDataSourceTramoComisiones();
	var dataSourceTramoComisionArray = bcsConfiguracionFFM.getTramoComisionesFondoMutuoArray();
		
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	
	$scope.formData = {};
	$scope.filtros = {};
	$scope.formData.ComisionDespues = {};
	$scope.defaultOptions = dataSourceTramoComisionArray.options;
	$scope.defaultOptions.selectable = 'row';
	
	var comisiones = [];		
	let tramosQuitados = [];
	
	$scope.formData.agregarTramo = function (){
		comisiones = [];
		var PerMaxima = 0;

        $.each($scope.defaultOptions.dataSource.data(),function(key,value) {
			if (PerMaxima < value.DiasHasta){
				PerMaxima = value.DiasHasta + 1;
			}
		});

		comisiones.push({IdTramo : 0, DiasDesde: PerMaxima, DiasHasta : 0, PorcentajeComision : 0, IdInterno : 0});
		$.each($scope.defaultOptions.dataSource.data(),function(key,value) {
			comisiones.push(value);
		});
		$scope.defaultOptions.dataSource.data(comisiones);
	}
	
	$scope.formData.quitarTramo = function (){
		if ($scope.formData.tramosSeleccionados === undefined || $scope.formData.tramosSeleccionados.length < 0){
			bcsComunes.msg("warning","warning","Debe seleccionar un tramo.");
			return false;
		}
		
		var PerMaximaTramo = 0
		$.each($scope.defaultOptions.dataSource.data(),function(key,value) {
			if (PerMaximaTramo < value.DiasHasta){
				PerMaximaTramo = value.DiasHasta;
			}
		});

		if(PerMaximaTramo !== $scope.formData.tramosSeleccionados.DiasHasta) {
			bcsComunes.ventanaMsgErr($translate.instant("UI_ERROR_QUITAR_TRAMO_COMISIONES"));
			return;
		};
		
		$scope.defaultOptions.dataSource.remove($scope.formData.tramosSeleccionados);
		
		if($scope.formData.tramosSeleccionados.IdTramo > 0){
			tramosQuitados.push({IdTramo: $scope.formData.tramosSeleccionados.IdTramo, Estado: "INA"});
		}
			
		comisiones = [];
		$.each($scope.defaultOptions.dataSource.data(),function(key,value) {
			comisiones.push(value);
			$scope.formData.tramosSeleccionados = undefined;
		});
	}	
    	
    //Obtiene Comisión dependiendo del IdComision obtenido como maximo		
	var filterComision = new Array();
	    filterComision.push({field: "IdSerieFondo", operator: "eq", value: $scope.parametros.IdSerieFondo});
		if($scope.parametros.IdComision > 0){
			filterComision.push({field: "IdComision", operator: "eq", value: $scope.parametros.IdComision});
		}
	    
	    	
    	
    var filterTramo = new Array();
        filterTramo.push({field: "IdSerieFondo", operator: "eq", value: $scope.parametros.IdSerieFondo});
        filterTramo.push({field: "EstadoTramo", operator: "eq", value: 'ACT'});
		if($scope.parametros.IdComision > 0){
       		filterTramo.push({field: "IdComision", operator: "eq", value: $scope.parametros.IdComision});
		}
        
        
	dataSourceComision.query({filter:filterComision}).then(function(){
		if (dataSourceComision.data().length<1)
		{
			console.log("Comision no existe!");
			$scope.formData.ComisionDespues.TipoComision = 'SALIDA';
			return false;
		}
		else
		{
			$scope.$apply(function() {
				$scope.formData.ComisionAntes = dataSourceComision.data()[0];
				$scope.formData.ComisionDespues = angular.copy($scope.formData.ComisionAntes);
			});
			return true;
		}
	});
		
	$scope.formData.modificarComisiones = function()
    {	 
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 comisiones = [];
		 
		 $.each($scope.defaultOptions.dataSource.data(),function(key,value) {
				comisiones.push(value);
		 });
		 
		 var datosModificarComisiones = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_UsaReglasNegocio : 'N',
				 
				 p_Comision : [{
					    IdInterno		    :	0,
					    IdComision			:	$scope.parametros.IdComision,
					    IdSerieFondo 	    :	$scope.parametros.IdSerieFondo,
					    Descripcion  		:	$scope.formData.ComisionDespues.Descripcion,
					    TipoComision		:	$scope.formData.ComisionDespues.TipoComision,
					    FechaVigenciaDesde  :	util.dateSPdate($scope.formData.ComisionDespues.FechaVigenciaDesde),
					    FechaVigenciaHasta  :	util.dateSPdate($scope.formData.ComisionDespues.FechaVigenciaHasta),
					    PorcentajeLiberado  :   $scope.formData.ComisionDespues.PorcentajeLiberado,
					    Estado              :   $scope.formData.ComisionDespues.Estado,
						TramosComision 		:	$scope.defaultOptions.dataSource.data(),
						TramosQuitados		:	tramosQuitados
				 }],
				 
		 }
		 
	     bcsServicioFondosMutuos.AdministrarComisiones(datosModificarComisiones).$promise.then(function(respuesta){
	      	 console.log(respuesta);
	    	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	      		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_COMISIONES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	      	 }else{
      		 	bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_COMISIONES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
      	     }
  		 },function(respuesta){
  			 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_COMISIONES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		dataSourceTramoComision.query({filter:filterTramo}).then(function(){
			if (dataSourceTramoComision.data().length<1)
			{
				console.log("Comision no existe!");
				$scope.formData.agregarTramo();
				return false;
			}
			else
			  {
				 var data = angular.copy(dataSourceTramoComision.data());
				 $.each(data,function(key,value) {
					var comision = {IdTramo: value.IdTramo, DiasDesde: value.DiasDesde, DiasHasta: value.DiasHasta, PorcentajeComision: value.PorcentajeComision, IdInterno: 0};
					comisiones.push(comision);
				 });
				 $scope.defaultOptions.dataSource.data(comisiones);
				 return true;
			  }
		});
	}
});