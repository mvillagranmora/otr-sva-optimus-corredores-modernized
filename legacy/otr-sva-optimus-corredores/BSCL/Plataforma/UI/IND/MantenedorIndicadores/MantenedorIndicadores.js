angular.module('ModuloPlataforma').controller('MantenedorIndicadoresCtrl',function($scope,bcsComunes,$resource,$translate){
	
	$scope.data = {};
	$scope.showDetalle = false;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.data.oConfiguracionEntidad = {
			tipo : "combobox-tipo-registro",
			configuracion : {},
			filtros : {
			
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	ModeloTiposAtributos = function() {
		return {
			model: {
    	        	fields:{
    	        		IdEmpresa 				: { type : "number"},
    	        		IdTipoIndicador 		: { type : "number"},
    	        		IdTipoEntidad 			: { type : "string"},
    	        		ClaseDato 				: { type : "string"},
    	        		NombreTipoIndicador		: { type : "string"},
    	        		ValorIndicadorDefecto   : { type : "string"},
    	        		GrupoIndicador 			: { type : "string"},
    	        		RequiereAutorizacion	: { type : "string"},
    	        		IdUso					: { type : "string"},
    	        		IdEstado				: { type : "string"},
    	        		CodigoTipoIndicador		: { type : "string"}, 
    	        		CantidadDecimales		: { type : "number"},
    	        		LongitudMinima			: { type : "number"},
    	        		LongitudMaxima			: { type : "number"},
    	        		IdTipoPersona			: { type : "string"},
    	        		RequiereFechaVencimiento: { type : "string"},
    	        	} 
			}
		};
	};
	
	dataSourceTiposAtributos = function() {
	return new bcs.DataSource({
			type : "odata", 
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				pageSize : 50,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1548", //asignar ODATA
						dataType : "json"
					}
				},
				schema : ModeloTiposAtributos(),
				
			}
		});
	};
	
	columnasTiposAtributos = function() {
		return [ 
    	   	    {field: "NombreTipoIndicador", title: $translate.instant('UI_NOMBRE'), width: "150px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "ClaseDato", title: $translate.instant('UI_TIPO'), width: "75px", attributes:{style:"text-align:center;"}  }
    	   	    ,{field: "GrupoIndicador", title: $translate.instant('UI_GRUPO'), width: "100px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "RequiereAutorizacion", title: $translate.instant('UI_REQUIERE_AUTORIZACION'),  attributes:{style:"text-align:left;"} }
				];
	};
	
	 
	
	
	$scope.TiposAtributos = {
			dataSource			: dataSourceTiposAtributos(),
			columns				: columnasTiposAtributos(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  //comentar
			height				: 280,
		    selectable 			: "row",
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable			: true,
		};
	
	dataSourceDetalleTiposIndicadores = function() {
		return new bcs.DataSource ({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					pageSize : 50,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1549", //asignar ODATA
							dataType : "json"
						}
					},
					schema : ModeloDetalleTiposIndicadores(),
					
				}
			});
		};
		
		columnasDetalleTiposIndicadores = function() {
			return [ 
			        {field: "Codigo", title: $translate.instant('UI_CODIGO'), width: "40px", attributes:{style:"text-align:left;"} } ,
	    	   	    {field: "DetalleIndicador", title: $translate.instant('UI_VALOR'), width: "150px", attributes:{style:"text-align:left;"} }
					];
		};
		
		ModeloDetalleTiposIndicadores = function() {
			return {
				model: {
	    	        	fields:{
	    	        		IdDetalleIndicador 		: { type : "number"},
	    	        		IdTipoIndicador 		: { type : "number"},
	    	        		DetalleIndicador 		: { type : "string"},
	    	        		Codigo					: { type : "string"}
	    	        	} 
				}
			};
		}; 
		
	
		$scope.DetalleTiposAtributos = {
				dataSource			: dataSourceDetalleTiposIndicadores(),
				columns				: columnasDetalleTiposIndicadores(),		
				sortable			: true,
				pageable			: true,
				autoBind			: false,  //comentar
				height				: 280,
				userSettings		: true,
				navigatable			: true,
				excel : {
					allPages: true,
				},
			    selectable 			: "row"
			};
	
	$scope.$watch("data.seleccionTiposAtributos", function handleChange( newValue, oldValue ) {
		if($scope.data.seleccionTiposAtributos)
		{
			if($scope.data.seleccionTiposAtributos.ClaseDato === 'COMBO')
			{
				$scope.showDetalle = true;
				
				var filtros = new Array();
				filtros.push({ field: "IdTipoIndicador", operator: "eq", value: $scope.data.seleccionTiposAtributos.IdTipoIndicador });		
				$scope.gridApiDetalleTiposAtributos.dataSource.filter(filtros);
			}
			else
			{
				$scope.showDetalle = false;
			}
		}
    });
	
	$scope.$watch("data.oConfiguracionEntidad.seleccionado", function handleChange( newValue, oldValue ) {
		if($scope.data.oConfiguracionEntidad.seleccionado)
		{
			var filtros = new Array();
			
			filtros.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			filtros.push({ field: "IdTipoEntidad", operator: "eq", value: newValue.IdTipoRegistro });
			filtros.push({ field: "IdUso", operator: "eq", value: 'IND' });
						
			$scope.gridApiAtributos.dataSource.filter(filtros);
		}
    });
	
	$scope.CrearTipoAtributo = function() {
		bcsComunes.ventanaOverlay(idVentana,true);
		var ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarTiposIndicadores",{ IdTipoEntidad : $scope.data.oConfiguracionEntidad.seleccionado.IdTipoRegistro, AccionBoton : 'Crear' },true,false);
		var promesa = bcsComunes.ventanaPromesa(ventanaModalId);   
		
		$.when(promesa).then(function(){
			bcsComunes.ventanaOverlay(idVentana,false);
			$scope.gridApiAtributos.dataSource.read();
         }); 
		
	};
	
	$scope.ModificarTipoAtributo = function() {
		bcsComunes.ventanaOverlay(idVentana,true);
		var ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarTiposIndicadores",
				{ IdTipoEntidad : $scope.data.oConfiguracionEntidad.seleccionado.IdTipoRegistro,
			TipoAtributo : $scope.data.seleccionTiposAtributos, AccionBoton : 'Modificar' },true,false);
		
		var promesa = bcsComunes.ventanaPromesa(ventanaModalId);   
		
		$.when(promesa).then(function(){
			bcsComunes.ventanaOverlay(idVentana,false);
			$scope.gridApiAtributos.dataSource.read();
         }); 
		
	};
	
	
	var agregarDetalleTipoAtributoSP = function(v_Dato)
    {
	       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=134";
	       var r = $resource
         ("", {}, {
	                 'urlConsulta': { method: "POST", url: urlConsulta}
                  }
         );
	       var post = r.urlConsulta(v_Dato);
	       return post.$promise;
    };
	
	
    $scope.AgregarDetalle = function() {
    	bcsComunes.ventanaOverlay(idVentana,true);
    	var ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarDetalleTiposIndicadores"
    			,{ tipoAtributo: $scope.data.seleccionTiposAtributos, AccionBoton : 'Crear' },true,false);
		var promesa = bcsComunes.ventanaPromesa(ventanaModalId);   
		
		$.when(promesa).then(function(){
			bcsComunes.ventanaOverlay(idVentana,false);
			$scope.gridApiDetalleTiposAtributos.dataSource.read();
         }); 
    };
    
    $scope.ModificarDetalle = function() {
    	bcsComunes.ventanaOverlay(idVentana,true);
    	var ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarDetalleTiposIndicadores"
    			,{ tipoAtributo: $scope.data.seleccionTiposAtributos , AccionBoton : 'Modificar', detalle: $scope.data.seleccionDetalleTiposAtributos},true,false);
		var promesa = bcsComunes.ventanaPromesa(ventanaModalId);   
		
		$.when(promesa).then(function(){
			bcsComunes.ventanaOverlay(idVentana,false);
			$scope.gridApiDetalleTiposAtributos.dataSource.read();
         }); 
    };
    
    
	
	$scope.AgregarDetalleTipoAtributo = function() {
		if($scope.data.seleccionTiposAtributos)
		{
			var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'ATR';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdTipoAtributo = $scope.data.seleccionTiposAtributos.IdTipoAtributo;
	        parametrosSP.p_DetalleAtributo = $scope.detalleTipoAtributo;
	        bcsComunes.ventanaOverlay(idVentana,true);
	        agregarDetalleTipoAtributoSP(parametrosSP).then(
				function(result) {
					
					
					var mensajeId = bcsComunes.ventanaMsg({ mensaje : "Detalle Tipo Atributo agregado",
						conflictos : util.parseServerResponse(result)}, idVentana);
					bcsComunes.ventanaPromesa(mensajeId).then(function(){
						$scope.gridApiDetalleTiposAtributos.dataSource.read();
						$scope.detalleTipoAtributo = '';
						bcsComunes.ventanaOverlay(idVentana,false);
					});
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
		}
	};
	
	
	
});