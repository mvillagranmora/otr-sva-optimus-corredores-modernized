angular.module('ModuloSocioNegocio').controller('BusquedaSocioNegocioCtrl',
		function($scope,$translate,$modal,$log,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};	
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'SOC';
	$scope.data.btnActivos  = true;   //Activa botones
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	
	$scope.gridConfigBusquedaSocioNegocio = {
			columns :
			[
			 	{field: "NombreCompleto",title: $translate.instant('UI_NOMBRE'),align: "left",width: "200px"}
				,{field: "NombreTipoDocumentoIdentidadDefecto",title: $translate.instant('UI_TIPO_DOCUMENTO_IDENTIDAD_ABR'),align: "left",width: "80px"}
				,{field: "DocumentoIdentidadDefecto",template:"<div class='text-right'> #:  IdTipoDocumentoIdentidadDefecto==='R'?util.formatRut(DocumentoIdentidadDefecto):DocumentoIdentidadDefecto #</div>",title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_ABR'),align: "left",width: "100px"}
				,{field: "TipoPersona",title: $translate.instant('UI_TIPO_PERSONA_ABR'),align: "left",width: "100px"}
				,{field: "NombrePaisResidencia",title: $translate.instant('UI_PAIS_RESIDENCIA_ABR'),align: "left",width: "100px"}
				,{field: "FechaIngreso",title: $translate.instant('UI_FECHA_INGRESO_ABR'), format: "{0:d}",align: "left",width: "70px"}
				,{field: "EsCliente",title: $translate.instant('UI_CLIENTE_ABR'),template : "<span translate>#: EsCliente # </span>",align: "left",width: "70px", hidden:true}
				,{field: "EsEmpleado",title: $translate.instant('UI_EMPLEADO_ABR'),template : "<span translate>#: EsEmpleado # </span>",align: "left",width: "70px", hidden:true}
				,{field: "EsProveedor",title: $translate.instant('UI_PROVEEDOR_ABR'),template : "<span translate>#: EsProveedor # </span>",align: "left",width: "70px", hidden:true}
				,{field: "EsContraparte",title: $translate.instant('UI_CONTRAPARTE_ABR'),template : "<span translate>#: EsContraparte # </span>",align: "left",width: "70px", hidden:true}

			],
	  		selectable : "row",
	  		dataSource : new bcs.DataSource({
	  			type: 'odata',
	  			definition : {
	  				serverFiltering: true,
	            	serverPaging: true,
	            	serverSorting: true,
	           	 	pageSize:20,
	        		sort: [{ field: "NombreTipoDocumentoIdentidadDefecto", dir: "asc" },
	              	  	{ field: "DocumentoIdentidadDefecto", dir: "asc" },
	                   	{ field: "NombreCompleto", dir: "asc" }
	                ],
	            	transport: {
	                	read: {
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1050", //UIConsultarSocioDeNegocio
	                		dataType: "json"
	                	}
	            	},
	            	schema: {
		        		model: {
	    	        		fields: {
	    	        			IdSocioDeNegocio : { type: "number"}
	    	        			,FechaIngreso : { type: "date"}
	    	        			,FechaCreacion : { type: "date"}
	    	        			,FechaModificacion : { type: "date"}
	    	        			,IdTipoDocumentoIdentidadDefecto : { type: "string"}
	    	        			,NombreTipoDocumentoIdentidadDefecto : { type: "string"}
	    	        			,NombreCompleto : { type: "string"}
	    	        			,DocumentoIdentidadDefecto : { type: "string"}
	    	        			,TipoPersona : { type: "string"}
	    	        			,RazonSocial : { type: "string"}
	    	        			,NombreCompleto : { type: "string"}
	    	        			,Nombres : { type: "string"}
	    	        			,ApellidoPaterno : { type: "string"}
	    	        			,ApellidoMaterno : { type: "string"}
	    	        			,Alias : { type: "string"}
	    	        			,FechaNacimiento : { type: "date"}
	    	        			,IdPaisResidencia : { type: "string"}
	    	        			,CodigoPaisResidencia : { type: "string"}
	    	        			,NombrePaisResidencia : { type: "string"}
	    	        			,FechaBloqueo: { type: "date"}
	    	        			,EsCliente : { type: "string"}
	    	        			,EsEmpleado : { type: "string"}
	    	        			,EsProveedor : { type: "string"}
	    	        			,EsContraparte : { type: "string"}
		        			}
		        		}
		        	}
	  			},
	  		}),
			pageable: true,
			pageSize: 20,
			autoBind:false,
			height:200,
		};
	
	$scope.read = function(){
		var objfilters = { };
		var niv1filters = new Array();
		for (var parametro in parametros.filters) {
			niv1filters.push(parametros.filters[parametro]);//filtros de tipo socio negocio.
		}
		objfilters.logic = 'and';
		objfilters.filters = niv1filters;
		
		
    	if ($scope.formData.tipoDocumentoIdentidad){
    		niv1filters.push({ field: "IdTipoDocumentoIdentidadDefecto", operator: "eq", value: $scope.formData.tipoDocumentoIdentidad });
    	}
    	if ($scope.formData.NumeroDocumentoIdentidad){
    		niv1filters.push({ field: "DocumentoIdentidadDefecto", operator: "contains", value: $scope.formData.NumeroDocumentoIdentidad });
    	}
    	if ($scope.formData.NombreCompleto){
			var obj2filters = { };
			var niv2filters = new Array();
			obj2filters.logic = 'or';
			obj2filters.filters = niv2filters;
			//Busqueda RazonSocial
			niv2filters.push({ field: "RazonSocial", operator: "contains", value: $scope.formData.NombreCompleto });
			//Busqueda Nombres
			niv2filters.push({ field: "Nombres", operator: "contains", value: $scope.formData.NombreCompleto });
			//Busqueda ApellidoPaterno
			niv2filters.push({ field: "ApellidoPaterno", operator: "contains", value: $scope.formData.NombreCompleto });
			//Busqueda ApellidoMaterno
			niv2filters.push({ field: "ApellidoMaterno", operator: "contains", value: $scope.formData.NombreCompleto });
			//Busqueda Alias
			niv2filters.push({ field: "Alias", operator: "contains", value: $scope.formData.NombreCompleto });
			//Busqueda NombreCompleto
			niv2filters.push({ field: "NombreCompleto", operator: "contains", value: $scope.formData.NombreCompleto });
			//Busqueda DocumentoIdentidadDefecto
			niv2filters.push({ field: "DocumentoIdentidadDefecto", operator: "startswith", value: $scope.formData.NombreCompleto });
			//Adjunta Filtros a listas
			niv1filters.push(obj2filters);
    	}
    	if (niv1filters.length>0) $scope.gridConfigBusquedaSocioNegocio.dataSource.filters(objfilters);
    	else  $scope.gridConfigBusquedaSocioNegocio.dataSource.filters(new Array());
	};
	
	$scope.seleccionarSocioNegocio = function(){
		bcsComunes.ventanaData(idVentana,$scope.seleccionGrillaBusquedaSocioNegocio);
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	
});	