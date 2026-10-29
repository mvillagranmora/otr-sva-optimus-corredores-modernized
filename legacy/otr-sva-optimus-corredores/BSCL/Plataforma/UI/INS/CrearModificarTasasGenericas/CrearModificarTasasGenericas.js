angular.module('ModuloPlataforma').controller('CrearModificarTasasGenericasCtrl',function($scope,bcsComunes){
	
	var CMTG 	= this;
	CMTG.grilla = {};
	$scope.filtros = {};
	$scope.formData = {};
	$scope.data ={};

	$scope.idVentana = bcsComunes.ventanaWinId($scope);

	$scope.formData.oConfiguracionTipoInstrumento = {
			filtros : { 	
				IdClasificacionTipoInstrumento : {
			        "field"   : "IdClasificacionTipoInstrumento",
			        "operator": "eq",
			        "value"   : "" 
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	
	
	
	 $scope.IdEstado        = { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" };
	 $scope.Clasificacion    = [ { field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'RF' },
	                             { field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'IF' }]
	
	//CONFIGURACION DE COMBOBOX
	  $scope.formData.oConfiguracionClasificacion={
	      tipo       : "combobox-clasificacion-tipo-instrumento",
	      configuracion   : {},
	      filtros : {
		        complejo : [$scope.IdEstado,$scope.Clasificacion],
		          logic: "and",
		          filters: [
		        {
		            logic: "or",
		            filters: $scope.Clasificacion
		        },
		        $scope.IdEstado        
		        ]}, 
	      idActual     : false,
	      seleccionado   : false,
	      idDefecto     : ''      
	  };
	
	  
	
	
	$scope.formData.defaultOptions = {
			allowCopy: true,
			autoBind: false,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
	      	filterable: true,
	      	height:300,
	      	pageable : true,
	      	reorderable: true,
			resizable: true,
			scrollable: true,
	      	selectable: "multiple, row",
			sortable: true,
			
			columns : [
			           {field: "IdPrecioTasaTipoInstrumento",	title : "IdPrecioTasaTipoInstrumento",	width: "50px",hidden: true},
			           {field: "IdTipoInstrumento",				title : "UI_TIPO_INSTRUMENTO",			width: "50px",hidden: true},
			           {field: "NombreInstrumento",				title : "UI_INSTRUMENTO",				width: "50px",hidden: true},
			           {field: "IdTipoReajuste",				title : "UI_TIPO_REAJUSTE",				width: "50px"},
			           {field: "IdClasificacionTipoInstrumento",title : "UI_CLASIFICACION",				width: "50px",hidden: true},
			           {field: "NombreClasificacion",			title : "UI_NOMBRE_CLASIFICACION",		width: "50px",hidden: true},			           
			           {field: "PlazoDesde",					title : "UI_PLAZO_DESDE",				width: "50px"},
			           {field: "PlazoHasta",					title : "UI_PLAZO_HASTA",				width: "50px"},			           
			           {field: "Tasa",							title : "UI_TASA",						width: "50px"},
			           {field: "Precio",						title : "UI_Precio",					width: "50px"},
			           {field: "FechaUltimaModificacion",		title : "FechaUltimaModificacion",		width: "50px"},
			           
			           ],
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: { field: "PlazoDesde", dir: "asc" },
					pageSize:50,
					schema: {
						model : {
							fields : {
								IdPrecioTasaTipoInstrumento		: { type: "number"},
								IdClasificacionTipoInstrumento	: { type: "string"},
								NombreClasificacion				: { type: "string"},
								IdTipoInstrumento  				: { type: "string"},
								NombreInstrumento  				: { type: "string"},
								IdTipoReajuste  				: { type: "string"},
								PlazoDesde						: { type: "number"},
								PlazoHasta  					: { type: "number"},
								Tasa		  					: { type: "number"},
								Precio							: { type: "number"},
								FechaUltimaModificacion  		: { type: "sysdate"}
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600055",
							dataType: "json"
						}
					}
				}
	      	}),

	};
	
	$scope.formData.ModificarTasasGenericas = function(seleccion) {
		
		if(seleccion)
		{
			var prom = bcsComunes.ventanaAbrir("ModificarTasasGenericas", seleccion ,false,false,$scope.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(prom);
	        promesa.then(function(data){ 
	        		$scope.formData.apiGrid.dataSource.read();	

	    	});  

		}else{
			
			bcsComunes.msg("error","error",'Debe seleccionar al menos un registro');

		}
		 
	};
	
	$scope.formData.CrearTasa = function() {
		
			var prom = bcsComunes.ventanaAbrir(
					"CrearTasasGenericas", 
					{ "IdPrecioTasaTipoInstrumento"   : 0,
					  "IdClasificacionTipoInstrumento": $scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento,
					  "IdTipoInstrumento"			  : $scope.filtros.tipoInstrumento.IdTipoInstrumento,
					  "IdTipoReajuste"				  : $scope.filtros.MonedaObj.IdMoneda
					}, false, false, $scope.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(prom);
	        promesa.then(function(data){ 
	        		//$scope.formData.apiGrid.dataSource.read();	

	    	});  

		
		 
	};
	
	$scope.formData.BuscarTasasGenericas = function()
	{	
	
		var filters = new Array();
		
		filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento });
		filters.push( { "field": "IdTipoInstrumento","operator": "eq", "value" : $scope.filtros.tipoInstrumento.IdTipoInstrumento });
		filters.push( { "field": "IdTipoReajuste","operator": "eq", "value" : $scope.filtros.MonedaObj.IdMoneda });

		$scope.formData.apiGrid.dataSource.filter(filters);

	};
	

});
