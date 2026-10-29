(function() {
	angular.module('ModuloCuentaInversion').controller('ModalCobroComisionConceptosCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope, bcsServiciosCuentaInversion ){
	
		var mccc = this;
		mccc.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		$scope.parametros = bcsComunes.ventanaParams(mccc.idVentana);
		
		mccc.fechaOptions = {};
		mccc.fechaOptions.min = new Date(util.dateInicioDia($scope.SesionFechaSistema));
		mccc.fechaVigenciaDesde = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		//SETEO INICIAL
	 
		//COMBOBOX
		//schema
		
		var obj = {};
		
		mccc.getSchemaConceptosCobroComision = function() {
			return {
				model: {
					fields : {
						IdConcepto 				: { type : "string"},
						NombreConcepto			: { type : "string"}
					}
				}
			};
		};
		
		mccc.configuracionColumnas = function() {
			return {
				IdConcepto				:{field: "IdConcepto",				title: $translate.instant('UI_CONCEPTO'),				width: "30px",	align: "left"},
				NombreConcepto			:{field: "NombreConcepto",			title: $translate.instant('UI_NOMBRE_CONCEPTO'),		width: "110px",	align: "left"}
			};
		};
		
		mccc.getDataSourceConceptosDisponiblesCobroComision = function(){
			return new bcs.DataSource({ 
				type : "odata",
				definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					sort: { field: "IdConcepto", dir: "asc" },
					pageSize : 200,
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=701161",
							dataType: "json"
						}
					},
					schema : mccc.getSchemaConceptosCobroComision()
				}
				
			});
		};
		let defDataSourceConceptosAExcluirCobroComision = { 
			type : "array",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdConcepto", dir: "asc" },
				pageSize : 200,
				schema : mccc.getSchemaConceptosCobroComision()
			}
		};
		
		mccc.dataSourceConceptosAExcluirCobroComision = new bcs.DataSource(defDataSourceConceptosAExcluirCobroComision);
		
		mccc.getConfiguracionColumna = function(campo,extras) {
			if (!mccc.configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
			return _.extend( {}, mccc.configuracionColumnas()[campo], extras);
		};
		
		mccc.conceptosDisponiblesOptions = {
			userSettings:true,
			sortable: true,
	  		reorderable: false,
	  		filterable: true,
			groupable: false,
			pageable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
	  		autoBind: true,
	  		scrollable: true,
	  		height:450,
	  		navigatable	: true,
			dataSource: mccc.getDataSourceConceptosDisponiblesCobroComision(),
			columns: [
			           mccc.getConfiguracionColumna("IdConcepto",{ })
			          ,mccc.getConfiguracionColumna("NombreConcepto",{ })							  
			]
		};
		
		mccc.conceptosAExcluirOptions = {
			userSettings:true,
			sortable: true,
	  		reorderable: false,
	  		filterable: true,
			groupable: false,
			pageable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
	  		autoBind: true,
	  		scrollable: true,
	  		height:450,
	  		navigatable	: true,
			dataSource: mccc.dataSourceConceptosAExcluirCobroComision,
			columns: [
			           mccc.getConfiguracionColumna("IdConcepto",{ })
			          ,mccc.getConfiguracionColumna("NombreConcepto",{ })							  
			]
		};
		//////////////////// FIN GRILLA
		
		
		mccc.excluirConceptos = function() {
			if(mccc.dataSourceConceptosAExcluirCobroComision.data().length === 0){
				bcsComunes.msg("error","Error","Debe Seleccionar al menos 1 Concepto.");
				return;
			}
			console.log("CONCEPTOS EN LA LISTA DE EXCLUSION: ");
			console.log(mccc.dataSourceConceptosAExcluirCobroComision.data());
			
			let conceptos = [];
				
			$.each( mccc.dataSourceConceptosAExcluirCobroComision.data(), function(index, value){
				let concepto = {IdConcepto: value.IdConcepto};
				conceptos.push(concepto);
			});
			
			let parametros = {
				p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
        		p_IdUsuario 		: $scope.$root.SesionIdUsuario,
        		p_IdModulo 			: 'CTA',
        		p_UsaReglasNegocio	: "N",
        		p_Conceptos			: conceptos,
        		p_FechaVigenciaDesde: util.dateSPdate(mccc.fechaVigenciaDesde)
			};
			console.log(parametros);
			bcsComunes.ventanaOverlay(mccc.idVentana,true);
			bcsServiciosCuentaInversion.CrearCobroComisionExclusionConcepto(parametros).$promise.then(
				function(result){
					bcsComunes.ventanaOverlay(mccc.idVentana,false);
					var srv=util.parseServerResponse(result);
					
					if (srv.erroresContador > 0){
						subtitulo='UI_OPERACION_CON_ERRORES';
						bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
					} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
						subtitulo='UI_EXCLUSION_CONCEPTO_OK';
					} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
						 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
					}
					
					bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, mccc.idVentana);
					bcsComunes.ventanaCerrar(mccc.idVentana);
				}, function(result){		
					bcsComunes.ventanaOverlay(mccc.idVentana,false);
					bcsComunes.ventanaCerrar(mccc.idVentana);						
					var srv=util.parseServerResponse(result);
					var p=bcsComunes.ventanaMsg({ 
						mensaje : '', 
						conflictos : srv
					},mccc.idVentana);
				}
				
			);
		};
		
		mccc.agregarListado = function () {
			if (mccc.seleccionConceptosDisponibles && mccc.seleccionConceptosDisponibles.length > 0) {
		        console.log("Agregar los conceptos " + mccc.seleccionConceptosDisponibles);
		        $.each(mccc.seleccionConceptosDisponibles, function(index, value) {
		            if (!mccc.dataSourceConceptosAExcluirCobroComision.data().find(concepto => concepto.IdConcepto === value.IdConcepto)) {
		                let concepto = { IdConcepto: value.IdConcepto, NombreConcepto: value.NombreConcepto };
		                mccc.dataSourceConceptosAExcluirCobroComision.add(concepto);
		            }
		        });
		        console.log(mccc.dataSourceConceptosAExcluirCobroComision);
		        
			}
		};
		
		mccc.quitarListado =  function () {
			console.log("CONCEPTOS EN LA LISTA DE EXCLUSION: ");
			console.log(mccc.dataSourceConceptosAExcluirCobroComision.data());
			console.log("CONCEPTOS A QUITAR DE LA LISTA DE EXCLUSION: ");
			console.log(mccc.seleccionConceptosAExcluir);
			
			// Obtener los IDs de los conceptos a excluir
		    let idsAExcluir = mccc.seleccionConceptosAExcluir.map(function(concepto) {
		        return concepto.IdConcepto;
		    });
		    
		    // Filtrar el DataSource para excluir los conceptos seleccionados
		    let nuevosDatos = mccc.dataSourceConceptosAExcluirCobroComision.data().filter(function(concepto) {
		        return !idsAExcluir.includes(concepto.IdConcepto);
		    });
		    
		    // Establecer los nuevos datos en el DataSource
		    mccc.dataSourceConceptosAExcluirCobroComision.data(nuevosDatos);
			console.log("CONCEPTOS EN LA LISTA DE EXCLUSION AL FINAL DE LA ELIMINACION: ");
			console.log(mccc.dataSourceConceptosAExcluirCobroComision.data());
		};
		
	});
}());