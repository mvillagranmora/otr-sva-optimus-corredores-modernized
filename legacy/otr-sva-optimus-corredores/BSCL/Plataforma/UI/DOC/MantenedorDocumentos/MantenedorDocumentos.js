angular.module('ModuloPlataforma').controller('MantenedorDocumentosCtrl',function($scope,bcsComunes,$resource,$translate){
	
	$scope.data = {};
	$scope.ventanaId = bcsComunes.ventanaWinId($scope);

	dataSourceTipoDocumento = function() {
	return new bcs.DataSource({
			type : "odata", 
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				pageSize : 50,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1544", //asignar ODATA
						dataType : "json"
					}
				},
				schema : ModeloTipoDocumento()
			}
		});
	};
	
	columnasTipoDocumento = function() {
		return [ 
    	   	    {field: "IdTipoDocumento", title: $translate.instant('UI_NOMBRE'), width: "220px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "NombreDocumento", title: $translate.instant('UI_NOMBRE'), width: "240px", attributes:{style:"text-align:center;"}  }
    	   	    ,{field: "SolicitarFechaFirma", title: $translate.instant('UI_SOLICITAR_FECHA_FIRMA'), width: "60px", attributes:{style:"text-align:left;"}  }
    	   	    ,{field: "Glosa", title: $translate.instant('UI_GLOSA'),  attributes:{style:"text-align:left;"} }
    	   	    ,{field: "ValidaFechaVigencia", title: $translate.instant('UI_VALIDAR_FECHA'),  attributes:{style:"text-align:left;"} }
    	   	    ,{field: "Descripcion", title: $translate.instant('UI_CLASE_DOCUMENTO'),  attributes:{style:"text-align:left;"} }
    	   	    ,{field: "FechaFirma", title: $translate.instant('UI_FECHA_FIRMA'),  attributes:{style:"text-align:left;"} }
				,{field: "RequiereFirmaCliente", title: $translate.instant('UI_REQUIERE_FIRMA_CLIENTE'),  attributes:{style:"text-align:left;"} }
    	   	    ,{field: "FirmaAutomaticaDocumento", title: $translate.instant('UI_FIRMA_AUTOMATICA'),  attributes:{style:"text-align:left;"} }
			];
	}; 
	
	ModeloTipoDocumento = function() {
		return {
			model: {
    	        	fields:{
    	        		IdTipoDocumento 		: { type : "string"},
    	        		IdEmpresa 				: { type : "number"},
    	        		NombreDocumento 		: { type : "string"},
    	        		SolicitarFechaFirma		: { type : "string"},
    	        		NumeroFirmasMinimas 	: { type : "number"},
    	        		NumeroFirmasMaximas  	: { type : "number"},
    	        		Glosa 					: { type : "string"},
    	        		ValidaFechaVigencia		: { type : "string"},
    	        		Descripcion				: { type : "string"},
    	        		FechaFirma				: { type : "string"},
					    CodigoArchivo           : { type : "string"},
						ValidaClienteFull       : { type : "string"},
						RequiereFirmaCliente    : { type : "string"},
						FirmaAutomaticaDocumento: { type : "string"}
    	        	} 
			}
		};
	}; 
	
	$scope.data.TipoDocumentos = {
			dataSource			: dataSourceTipoDocumento(),
			columns				: columnasTipoDocumento(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
			height				: 280,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row"
		};
	
	$scope.Agregar = function()
	{
		bcsComunes.ventanaOverlay($scope.ventanaId,true);
		var ventanaId = bcsComunes.ventanaAbrir("ModalDocumentos",{},true,false);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);   
		
		$.when(promesa).then(function(){
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			$scope.gridApiTipoDocumentos.dataSource.read();
         }); 
	};
	
	$scope.Modificar = function()
	{
		bcsComunes.ventanaOverlay($scope.ventanaId,true);
		var ventanaId = bcsComunes.ventanaAbrir("ModalDocumentos",$scope.data.seleccionTipoDocumentos,true,false);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);   
		
		$.when(promesa).then(function(){
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			$scope.gridApiTipoDocumentos.dataSource.read();
         }); 
	};
	
	$scope.Cerrar = function () {
		bcsComunes.ventanaCerrar(ventanaId); 
	};
	
	
});