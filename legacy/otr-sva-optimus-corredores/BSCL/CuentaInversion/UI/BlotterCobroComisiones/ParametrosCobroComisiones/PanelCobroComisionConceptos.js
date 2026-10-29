(function() {
	angular.module('ModuloCuentaInversion').controller('PanelCobroComisionConceptosCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope, bcsAlerta, bcsServiciosCuentaInversion ){
	
	var pccc = this;
	pccc.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams(pccc.idVentana);

	
	//SETEO INICIAL
 
	//COMBOBOX
	//schema
	
	var obj = {};
	
	pccc.getSchemaConceptosCobroComision = function() {
		return {
			model: {
				fields : {
					IdExclusionConcepto		: { type : "number"},
					IdConcepto 				: { type : "string"},
					NombreConcepto			: { type : "string"},
					FechaVigenciaDesde		: { type : "date"}
				}
			}
		};
	};
	
	pccc.configuracionColumnas = function() {
		return {
			IdExclusionConcepto		:{field: "IdExclusionConcepto",		title: $translate.instant('UI_CANAL'),					width: "75px",	align: "left", hidden: true},
			IdConcepto				:{field: "IdConcepto",				title: $translate.instant('UI_CONCEPTO'),				width: "20px",	align: "left"},
			NombreConcepto			:{field: "NombreConcepto",			title: $translate.instant('UI_NOMBRE_CONCEPTO'),		width: "110px",	align: "left"},
			FechaVigenciaDesde		:{field: "FechaVigenciaDesde",		title: $translate.instant('UI_FECHA_VIGENCIA_DESDE'),	width: "25px",	align: "left"},
		};
	};
	
	pccc.getDataSourceConceptosCobroComision = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdExclusionConcepto", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=701160",
						dataType: "json"
					}
				},
				schema : pccc.getSchemaConceptosCobroComision()
			},
			
		});
	};
	
	pccc.getConfiguracionColumna = function(campo,extras) {
		if (!pccc.configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, pccc.configuracionColumnas()[campo], extras);
	};
	
	pccc.conceptosCobroComisionOptions = {
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
  		navigatable	: true,
		dataSource: pccc.getDataSourceConceptosCobroComision(),
		columns: [
		           pccc.getConfiguracionColumna("IdExclusionConcepto",{ groupable: true})
		          ,pccc.getConfiguracionColumna("IdConcepto",{ groupable: true})
		          ,pccc.getConfiguracionColumna("NombreConcepto",{ groupable: true})
		          ,pccc.getConfiguracionColumna("FechaVigenciaDesde",{ groupable: true})							  
		]
	};
	//////////////////// FIN GRILLA
	
	
	pccc.ingresarConcepto = function() {
		let id1 = bcsComunes.ventanaAbrir("AgregarCobroComisionExclusionConcepto", {}, false,false,pccc.idVentana); 
		
		let promesa=bcsComunes.ventanaPromesa(id1);                               
        promesa.then(function(data){
			pccc.conceptosCobroComisionOptions.dataSource.read();
    	});
	};
	
	pccc.eliminar = function(){
		var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_MENSAJE_ELIMINAR_CONCEPTO_EXCLUIDO"), pccc.idVentana);
		confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
			if(result==1){
				let conceptos = [];
				
				$.each( pccc.conceptosSeleccionados, function(index, value){
					let concepto = {IdExclusionConcepto: value.IdExclusionConcepto };
					conceptos.push(concepto);
				});
				
				let parametros = {
					p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
	        		p_IdUsuario 		: $scope.$root.SesionIdUsuario,
	        		p_IdModulo 			: 'CTA',
	        		p_UsaReglasNegocio	: "N",
	        		p_Conceptos			: conceptos
				};
				console.log(parametros);
				bcsComunes.ventanaOverlay(pccc.idVentana,true);
				bcsServiciosCuentaInversion.EliminarCobroComisionExclusionConcepto(parametros).$promise.then(
					function(result){
						bcsComunes.ventanaOverlay(pccc.idVentana,false);
						var srv=util.parseServerResponse(result);
						
						if (srv.erroresContador > 0){
							subtitulo='UI_OPERACION_CON_ERRORES';
							bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
						} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
							subtitulo='UI_EXCLUSION_CONCEPTO_BORRADA';
						} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
							 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
						}
						
						bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, pccc.idVentana);
							
						pccc.conceptosCobroComisionOptions.dataSource.read();
					}, function(result){		
						bcsComunes.ventanaOverlay(pccc.idVentana,false);						
						var srv=util.parseServerResponse(result);
						var p=bcsComunes.ventanaMsg({ 
							mensaje : '', 
							conflictos : srv
						},pccc.idVentana);
					}
				);
				pccc.conceptosCobroComisionOptions.dataSource.read();
			}
		});
	};
	
});
}());