(function() {
	angular.module('ModuloMesa').controller('MantenedorCanastaFactores',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.oDesplegar = {};
		$scope.ObjSeleccionGrilla = {};
		$scope.botonEliminarCanastaHabilitado = false;
		//Instrumento
		$scope.data.ConfiguracionInstrumento={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }				
			}
		};
		
		//Moneda Principal
		$scope.data.ConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		//Canasta
		$scope.data.ConfiguracionCanasta={
			filtros : {
				IdEmpresa 	: { "field"   : "IdEmpresa","operator": "eq", "value" : "1" }
			}
		};
		
		var dsDefCanasta = {
				type : "odata",
				definition : {
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdMoneda", dir: "asc" },
                    pageSize        : 8,
		            transport       : {
									   read: {
											url: bcsConsultasMesa('ConsultarCanasta'),
											dataType: "json"
									   }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													 IdEmpresa				: {type: "number"}
													,IdInstrumentoCanasta	: {type: "number"}
													,IdInstrumento			: {type: "number"}
													,IdMoneda     			: {type: "string"}
													,IdCanasta 				: {type: "number"}
													,NombreCanasta			: {type: "string"}
												}
											}
									  }

    			 }
		};
		
		//GRILLA
		var dsCanasta  = new bcs.DataSource(dsDefCanasta);
		$scope.confBasicaCanasta = {
			dataSource			: dsCanasta,
			columns				: [ 
			       				   	 {field: "IdEmpresa", title: "UI_EMPRESA", width: "50px", align: "left", sortable : false , hidden: true} 		
									,{field: "IdCanasta", title: "UI_CANASTA", width: "50px", align: "left", sortable : false , hidden: true} 		
			       				   	,{field: "IdMoneda", title: "UI_MONEDA_PRINCIPAL", width: "50px", align: "left", sortable : false}
			       				    ,{field: "NombreCanasta", title: "UI_CANASTA", width: "50px", align: "left", sortable : false } 		
								  ],		
			sortable			: true,
			pageable			: true,
			filterable			: false,
			height				: 200,
			selectable 			: "multiple row",
			autoBind		    : false,
		};
		
		var dsDefFactores = {
				type : "odata",
				definition : {
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdFactor", dir: "asc" },
                    pageSize        : 8,
                    transport       : {
									   read: {
											url: bcsConsultasMesa('ConsultarFactoresCanasta'),
											dataType: "json"
									   }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													 IdEmpresa	: {type: "number"}
													,IdFactor   : {type: "number"}
													,IdCanasta	: {type: "number"}								
													,Hasta 		: {type: "number"}
													,Factor		: {type: "number"}
														
												}
											}
									  }

    			 }
		};
		
		//GRILLA
		var dsFactores = new bcs.DataSource(dsDefFactores)
		$scope.confBasicaFactores = {
			dataSource			: dsFactores,
			columns				: [ 
			       				   	 {field: "IdEmpresa", title: "UI_EMPRESA", width: "50px", align: "left", sortable : false , hidden: true}
			       				    ,{field: "IdCanasta", title: "UI_CANASTA", width: "50px", align: "left", sortable : false}
			       				   	,{field: "IdFactor", title: "UI_IDFACTOR", width: "50px", align: "left", sortable : false, hidden: true}
			       				    ,{field: "Hasta", title: "UI_HASTA", width: "50px", align: "right", sortable : false }
			       				    ,{field: "Factor", title: "UI_FACTOR", width: "50px", align: "right", sortable : false}
			       				    
								  ],		
			sortable			: true,
			pageable			: true,
			filterable			: false,
			height				: 200,
			selectable 			: "multiple row",
			autoBind		    : false
		};
		
		$scope.BuscarCanastas = function(){
			var filter = Array();
			filter.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
			dsCanasta.filters(filter);
			$scope.botonEliminarCanastaHabilitado = false;
		};
		
		$scope.BuscarFactores = function(){
			if(typeof $scope.data.CanastaSeleccionado === 'undefined')
				return;
			
			var filter = Array();
			filter.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
			if($scope.data.CanastaSeleccionado.Nombre.trim() !== 'TODOS')
			{
				filter.push({ field: "IdCanasta", operator: "eq", value: $scope.data.CanastaSeleccionado.IdCanasta });
			}
			dsFactores.filters(filter);
		};
		
		$scope.IngresarCanasta = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("MantenedorCanastaModal", {} , true,true); 
	        var promesa=bcsComunes.ventanaPromesa(IdVentanamodal);
	        promesa.then(function(data){
	        	if(typeof data !== 'undefined')
	        		if(data.Resultado === 'OK')
	        			bcsComunes.msg("info","info",$translate.instant('UI_CANASTA_INGRESADA'));
	        		else if (data.Resultado === 'ERROR')
	        			bcsComunes.msg("error","error",$translate.instant('UI_CANASTA_ERROR'));
	        			
	        		$scope.BuscarCanastas();
	        }); 
		};
		
		$scope.IngresarFactores = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("MantenedorFactoresModal", {} , true,true); 
	        var promesa=bcsComunes.ventanaPromesa(IdVentanamodal);
	        promesa.then(function(data){
	        	if(typeof data !== 'undefined')
	        		if(data.Resultado === 'OK')
	        			bcsComunes.msg("info","info",$translate.instant('UI_FACTOR_INGRESADA'));
	        		else if (data.Resultado === 'ERROR')
	        			bcsComunes.msg("error","error",$translate.instant('UI_CANASTA_ERROR'));
	        			
	        		$scope.BuscarFactores();
	        }); 	
		};
		
		$scope.$watch('ObjSeleccionGrilla.seleccionCanasta',function(newval,oldval){
			if($scope.ObjSeleccionGrilla.seleccionCanasta === undefined){
				$scope.botonEliminarCanastaHabilitado = false;
				return;
			}else
			{
				$scope.botonEliminarCanastaHabilitado = true;
			}
		});
		
		$scope.EliminarCanasta = function(){
			var tablaCanastaMoneda = [];
			$.each($scope.ObjSeleccionGrilla.seleccionCanasta,function(key,value)
            {       		     
				var ObjCanastaMoneda = {
						"IdCanasta" : $scope.ObjSeleccionGrilla.seleccionCanasta[key].IdCanasta,
						"IdMoneda" : $scope.ObjSeleccionGrilla.seleccionCanasta[key].IdMoneda
					};
				tablaCanastaMoneda.push(ObjCanastaMoneda);
            });
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo : 'MFW',
					p_UsaReglasNegocio : 'N',
					p_tablaCanastaMoneda	: tablaCanastaMoneda
			};
			bcsServiciosMesa.EliminarCanasta(datos).$promise.then(successIngreso).catch(error);
		};
		
		$scope.EliminarFactorCanasta = function(){
			var tablaFactorCanasta = [];
			$.each($scope.ObjSeleccionGrilla.seleccionFactores,function(key,value)
            {       		     
				var ObjFactorCanasta = {
						"IdFactor" : $scope.ObjSeleccionGrilla.seleccionFactores[key].IdFactor
					};
				tablaFactorCanasta.push(ObjFactorCanasta);
            });
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo : 'MFW',
					p_UsaReglasNegocio : 'N',
					p_tablaFactorCanasta	: tablaFactorCanasta
			};
			bcsServiciosMesa.EliminarFactorCanasta(datos).$promise.then(successIngreso).catch(error);
		};
		
		var error = function(respuesta){
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_ELIMINADA_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		};
		
		var successIngreso = function(respuesta){
			//si vuelve ref id, se creo la asignacion
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{	
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_ELIMINADA_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				//$scope.buscarMovimientos();
			}
			else
			{			
				bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_ELIMINADA_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			$scope.BuscarCanastas();
			$scope.BuscarFactores();
		};

		
	});
}());

