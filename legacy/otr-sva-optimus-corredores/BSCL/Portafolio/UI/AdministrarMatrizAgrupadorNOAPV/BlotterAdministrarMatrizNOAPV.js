angular.module('ModuloPortafolio').controller('BlotterAdministrarMatrizNOAPVCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource,servicioPortafolio) {
	
	$scope.datos = {};
	
	var idVentana=bcsComunes.ventanaWinId($scope);
	var columnas = function() {
		return  {
			IdMatrizAgrupadorNOAPV : { field: "IdMatrizAgrupadorNOAPV",title: 'UI_ID', width: "117px"},
			CodigoPortafolio : { field: "CodigoPortafolio",title: 'UI_CODIGO_PORTAFOLIO' , width: "117px"},
			EsADC : { field: "EsADC" ,title: 'UI_ES_ADC', width: "50px"}    
		};
	};
	
	var getConfiguracionColumna = function(campo,extras) {
		if (!columnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, columnas()[campo], extras);
	};
	
	$scope.defaultOptions = {
		userSettings:true,
		sortable: true,
  		reorderable: true,
  		filterable: true,
		groupable: true,
		pageable: true,
		selectable: "multiple, row",
		reorderable: true,
		allowCopy: true,
		resizable: true,
  		autoBind: true,
  		scrollable: true,
  		height:450,

		dataSource: new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdMatrizAgrupadorNOAPV", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=601201",
						dataType: "json"
					}
				},
				schema : {
					model: {
						fields : {
							IdMatrizAgrupadorNOAPV		: {type: "number"}
							,CodigoPortafolio			: {type: "string"}
							,EsADC						: {type: "string"}
						}
					} 
				},
			},
			
		}),
	
		columns: [
		           getConfiguracionColumna("IdMatrizAgrupadorNOAPV")
		          ,getConfiguracionColumna("CodigoPortafolio")
		          ,getConfiguracionColumna("EsADC")
		]
	};
	
	$scope.ingresar = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var id = bcsComunes.ventanaAbrir("CrearMatrizAgrupadorNOAPV",{},false); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){
        	bcsComunes.ventanaOverlay(idVentana,false);
        	$scope.defaultOptions.dataSource.read();
        	$scope.datos.kNgModel = undefined;
        });
	}
	
	$scope.modificar = function(matriz){
		if(!matriz || matriz.length === 0) return
		var id = bcsComunes.ventanaAbrir("ModificarMatrizAgrupadorNOAPV",{Matriz:matriz[0]},false);
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){
        	bcsComunes.ventanaOverlay(idVentana,false);
        	$scope.defaultOptions.dataSource.read();
        	$scope.datos.kNgModel = undefined;
        });
	}
		
	$scope.eliminar = function(matriz){
		if(!matriz || matriz.length === 0) return
		bcsComunes.ventanaOverlay(idVentana,true);
		var mt = matriz.map(function(item){
			return {IdMatrizAgrupadorNOAPV : item.IdMatrizAgrupadorNOAPV}
		})
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'SOC',
				p_SimularOperacion : 'N',
				p_UsaReglasNegocio : 'S',
				p_MatrizAgrupadorNOAPV: mt
		}
		servicioPortafolio.EliminarMatrizAgrupadorNOAPV(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result ){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, idVentana);
					}
					$scope.defaultOptions.dataSource.read();
					$scope.datos.kNgModel = undefined;
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
				});
	}
	
	$scope.buscar = function(){
		var filtro = [{ field: "EsADC", operator: "eq", value: $scope.EsADC ? 'S' : 'N'}];
		$scope.defaultOptions.dataSource.filter(filtro);
	}
	
	
})