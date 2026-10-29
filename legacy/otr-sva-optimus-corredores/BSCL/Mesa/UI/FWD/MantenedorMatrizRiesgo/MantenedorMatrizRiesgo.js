(function() {
	angular.module('ModuloMesa').controller('MantenedorMatrizRiesgo',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.oDesplegar = {};
		$scope.ObjSeleccionGrilla = {};
		$scope.HabilitarGrabar = false;
		$scope.HabilitarEliminar = false;
		
		/*Valores por Defecto para el par de Monedas*/
		$scope.data.MonedaPrincipalDefecto = 'USD';
		$scope.data.MonedaSecundariaDefecto = 'CLP';
		
		//Clasificacion
		$scope.data.ConfiguracionClasificacion={
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				Codigo : { "field"   : "Codigo","operator": "eq", "value"   : "DV" },
			}
		};
		
		//Tipo Instrumento
		$scope.data.ConfiguracionInstrumento={
			filtros : {
				//IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "" },
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" },
				logic : "and",
		           IdMonedaPrincipal : { "field"   : "IdMonedaPrincipal","operator": "eq", "value" : "" },
		           IdMonedaSecundaria : { "field"   : "IdMonedaSecundaria","operator": "eq", "value" : "" }				
			}
		};
		
		//Operación
		$scope.data.ConfiguracionOperacion={
			filtros : {
				IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW" }
			}
		};
		
		//Moneda Principal
		$scope.data.ConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		//Moneda Secundaria
		$scope.data.ConfiguracionMonedaSecundaria={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		var dsDefMatrizRiesgo = {
			type : "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "Plazo", dir: "desc" },
                pageSize        : 9,
	            transport: {
	            	read: {
	            		url: bcsConsultasMesa('ConsultarMatrizRiesgo'),
	            		dataType: "json"
	            	}
				},
                schema: {
                	model: {
                		fields :{
                			 IdEmpresa			: {type: "number"}
							,IdTipoProducto	    : {type: "string"}
							,IdInstrumento		: {type: "string"}
							,IdMatrizRiesgo		: {type: "number"}
							,IdMonedaPrincipal	: {type: "string"}
							,IdMonedaSecundaria : {type: "string"}
							,IdTipoOperacion 	: {type: "string"}
							,Accion		 	    : {type: "string"}
							,Plazo				: {type: "number", editable: true ,validation: { min: 0, required: true }}
							,Porcentaje			: {type: "number", editable: true ,validation: { min: 0, required: true }}
						}
                	}
                }
			 }
		};
		
		//GRILLA
		var dsMatrizRiesgo = new bcs.DataSource(dsDefMatrizRiesgo);
		$scope.confBasicaMatrizRiesgo = {
			dataSource			: dsMatrizRiesgo,
			columns				: [ 
			       				   	 {field: "IdEmpresa", title: "UI_EMPRESA", width: "50px", align: "left", sortable : false , hidden: true}
			       				    ,{field: "IdTipoProducto", title: "UI_PRODUCTO", width: "50px", align: "left", sortable : false , hidden: true}
			       				    ,{field: "IdInstrumento", title: "UI_INSTRUMENTO", width: "50px", align: "left", sortable : false , hidden: true}
			       				    ,{field: "IdMatrizRiesgo", title: "UI_MATRIZ_RIESGO", width: "50px", align: "left", sortable : false , hidden: true}
			       				   	,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "50px", align: "left", sortable : false }
									,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "50px", align: "left", sortable : false } 		
			       				   	,{field: "IdTipoOperacion", title: "UI_TIPO_OPERACION", width: "50px", align: "left", sortable : false, hidden: true}
			       				    ,{field: "Accion", title: "UI_TIPO_OPERACION", width: "50px", align: "left", sortable : false }
			       				    ,{field: "Plazo", title: "UI_PLAZO_VENCIMIENTO_HASTA", width: "50px", align: "left", sortable : false, editable : true }
			       				    ,{field: "Porcentaje", title: "UI_PORCENTAJE", width: "50px", align: "left", sortable : false, editable : true }
								  ],		
			sortable			: true,
			pageable			: true,
			filterable			: true,
			height				: 240,
			selectable 			: "row",
			autoBind		    : false,
		};
		
		$scope.BuscarMatrizRiesgo = function(){
			if(typeof $scope.data.OperacionSeleccionada === 'undefined') return;
			if(typeof $scope.data.MonedaPrincipalSeleccionada === 'undefined') return;
			if(typeof $scope.data.MonedaSecundariaSeleccionada === 'undefined') return;
			
			var filter = Array();
			filter.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
			filter.push({ field: "IdTipoProducto", operator: "eq", value: $scope.data.OperacionSeleccionada.IdTipoProducto });
			filter.push({ field: "IdInstrumento", operator: "eq", value: $scope.data.InstrumentoSeleccionado.IdInstrumento });
			filter.push({ field: "IdTipoOperacion", operator: "eq", value: $scope.data.OperacionSeleccionada.IdTipoOperacion });
			filter.push({ field: "IdMonedaPrincipal", operator: "eq", value: $scope.data.MonedaPrincipalSeleccionada.IdMonedaPrincipal });
			filter.push({ field: "IdMonedaSecundaria", operator: "eq", value: $scope.data.MonedaSecundariaSeleccionada.IdMonedaSecundaria });
			
			dsMatrizRiesgo.filter(filter);
			
			$scope.HabilitarEliminar = false;
			$scope.HabilitarGrabar = false;
		};

		$scope.GrabarMatrizRiesgo = function(){
			
			var registrosValidacion = dsMatrizRiesgo.data() || [];
			
			for (var indiceFila = 0; indiceFila < registrosValidacion.length; indiceFila++) {
				if (registrosValidacion[indiceFila].Plazo == 0) {
					bcsComunes.msg("error", "info", $translate.instant('UI_PLAZO_0_MATRIZ_RIESGO'));
					return;
				}
			}
			
			$scope.HabilitarEliminar = false;
			$scope.HabilitarGrabar = false;
			var tablaMatrizRiesgo = [];
			var registrosMatriz1 = dsMatrizRiesgo.data() || [];
			var registrosMatriz2 = dsMatrizRiesgo.data() || [];
			var cantRepetidos = 0;
			
			//Validacion mismo Plazo mas de una vez
			$.each(registrosMatriz1,function(key1,value1){
				$.each(registrosMatriz2,function(key2,value2){
					if(value1.IdMonedaPrincipal === value2.IdMonedaPrincipal && value1.IdMonedaSecundaria === value2.IdMonedaSecundaria && value1.IdTipoOperacion === value2.IdTipoOperacion && value1.Plazo === value2.Plazo){
						cantRepetidos++;
					}
				});
				if(cantRepetidos > 1){
					return false;
				}
				else{
					var ObjMatrizRiesgo = {
						 "IdMatrizRiesgo"	   : value1.IdMatrizRiesgo || 0
						,"IdTipoProducto"	   : value1.IdTipoProducto
						,"IdInstrumento"	   : value1.IdInstrumento
						,"IdMonedaPrincipal"   : value1.IdMonedaPrincipal
						,"IdMonedaSecundaria"  : value1.IdMonedaSecundaria
						,"IdTipoOperacion"	   : value1.IdTipoOperacion
						,"Plazo"			   : value1.Plazo
						,"Porcentaje"		   : value1.Porcentaje
					};
					tablaMatrizRiesgo.push(ObjMatrizRiesgo);
					
					cantRepetidos = 0;
				}
            });
            
            if(cantRepetidos > 1){
				bcsComunes.msg("error","Error","No se puede grabar el mismo Plazo más de una vez.");
				return;
			}

			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'MFW',
				p_UsaReglasNegocio : 'N',
				p_tablaMatrizRiesgo	: tablaMatrizRiesgo
			};
			bcsServiciosMesa.CrearMatrizRiesgo(datos).$promise.then(successIngreso).catch(error);
		};

		var error = function(respuesta){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_GRABAR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		};
		
		var successIngreso = function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {	
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			else{			
				bcsComunes.msg("error","error",$translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			$scope.BuscarMatrizRiesgo();
		};

		$scope.$watchGroup(['data.MonedaPrincipalSeleccionada','data.MonedaSecundariaSeleccionada'],function(newval,oldval) {   
            if(typeof $scope.data.MonedaPrincipalSeleccionada === 'undefined') return;
            if(typeof $scope.data.MonedaSecundariaSeleccionada === 'undefined') return;
            if(typeof $scope.data.ClasificacionSeleccionado === 'undefined') return;
			if(newval === oldval) return;
			if(typeof $scope.data.OperacionSeleccionada === 'undefined') return;
			
			//var IdMonedaPrincipalSel = ($scope.data.MonedaPrincipalIdActual === false ? '' : $scope.data.MonedaPrincipalIdActual);
			//var IdMonedaSecundariaSel = ($scope.data.MonedaSecundariaIdActual === false ? '' : $scope.data.MonedaSecundariaIdActual);
			//var IdTipoInstrumento = ($scope.data.ClasificacionSeleccionado === false ? '' : $scope.data.ClasificacionSeleccionado.Codigo);
			
			//$scope.data.ConfiguracionInstrumento.filtros.IdTipoInstrumento.value = IdTipoInstrumento;
			$scope.data.ConfiguracionInstrumento.filtros.IdMonedaPrincipal.value = $scope.data.MonedaPrincipalIdActual ? $scope.data.MonedaPrincipalIdActual : '';
			$scope.data.ConfiguracionInstrumento.filtros.IdMonedaSecundaria.value = $scope.data.MonedaSecundariaIdActual? $scope.data.MonedaSecundariaIdActual : '';
		});
		
		$scope.IngresarMatrizRiesgo = function(){
			
			var registros = dsMatrizRiesgo.data();
			
			for (var indiceFila = 0; indiceFila < registros.length; ++indiceFila) {
				if (registros[indiceFila].Plazo == 0) {
					bcsComunes.msg("error", "info", $translate.instant('UI_PLAZO_0_MATRIZ_RIESGO'));
					return;
				}
			}
			
			if(typeof $scope.data.MonedaPrincipalSeleccionada === 'undefined') return;
            if(typeof $scope.data.MonedaSecundariaSeleccionada === 'undefined') return;
            if(typeof $scope.data.ClasificacionSeleccionado === 'undefined') return;
            if(typeof $scope.data.MonedaSecundariaSeleccionada === 'undefined') return;
            
			$scope.HabilitarGrabar = true;
			dsMatrizRiesgo.add({
				 IdEmpresa			: $scope.SesionIdEmpresa
				,IdTipoProducto		: $scope.data.OperacionSeleccionada.IdTipoProducto
				,IdInstrumento		: $scope.data.InstrumentoSeleccionado.IdInstrumento
				,IdMatrizRiesgo		: undefined
				,IdMonedaPrincipal	: $scope.data.MonedaPrincipalSeleccionada.IdMonedaPrincipal
				,IdMonedaSecundaria : $scope.data.MonedaSecundariaSeleccionada.IdMonedaSecundaria
				,IdTipoOperacion 	: $scope.data.OperacionSeleccionada.IdTipoOperacion
				,Accion 	        : $scope.data.OperacionSeleccionada.Accion
				,Plazo				: 0
				,Porcentaje			: 0
			});
		};
		
		$scope.$watch('seleccionMatrizRiesgo',function(newval,oldval){
			if(typeof $scope.seleccionMatrizRiesgo === 'undefined') return;
			if(newval === oldval) return;
			
			$scope.HabilitarEliminar = true;
			$scope.HabilitarGrabar = true;
		});
		
		$scope.EliminarMatrizRiesgo = function(){
			if(typeof $scope.seleccionMatrizRiesgo === 'undefined') return;
			
			dsMatrizRiesgo.remove($scope.seleccionMatrizRiesgo);
			
			$scope.HabilitarGrabar = true;
			$scope.HabilitarEliminar = false;
		};
	});
}());