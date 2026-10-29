angular.module('ModuloPlataforma').controller('RequisitosCierreCtrl',
		function($scope,bcsComunes,$translate,bcsCierreModulosGrid, $resource){
	
	var rcrr = this;
	parametroBuscar = false;
	RequisitosBuscar = false;
	
	//Obtiene datos de la empresa
	if ($scope.SesionIdEmpresa)
	{
			var empresaDatasource = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    			read: {
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1557",
	    				dataType: "json"
			    	}
				},
				filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
			});
	
	        empresaDatasource.fetch().then(function(){
				 if (empresaDatasource.data().length != 1 ) 
				 {
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = empresaDatasource.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });		
	};
	
	rcrr.oConfiguracionModulos={
			tipo          : "combobox-modulos-cierre",
			configuracion : {},
			filtros       : {
				//IdPaisBanco    : { "field"   : "IdPaisBanco","operator": "eq", "value"   : $scope.SesionIdPaisSistema },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : ''			
	};
	
	rcrr.buscar = function() {
		rcrr.ProcesosBuscar = true;
		rcrr.RequisitosBuscar = true
	};
	
	$scope.$watch('rcrr.oConfiguracionModulos.idActual', function(newValue, oldValue){
		if (newValue === oldValue) return;
		if (newValue !== false && newValue !== 0 && newValue !== null) {
			rcrr.apertura = (rcrr.oConfiguracionModulos.seleccionado.TipoApretura === 'M')? true:false;
			rcrr.cierre = (rcrr.oConfiguracionModulos.seleccionado.TipoCierre === 'M')? true:false;
			
			}
	});
	
});