/*************************************************************
 * Purpose	: Panel Persona Documentos Identidad
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaDocumentosIdentidadCtrl',
	function($scope,$translate,$resource,bcsComunes,$uibModal,servicioSociodeNegocio){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppdi = this;
		ppdi.showBloqueo = false;
		ppdi.idVentana = bcsComunes.ventanaWinId($scope);
		
		$scope.$watch("ppdi.documentoIdentidadSeleccionado",
	        function handleDocumentoIdentidad( newValue, oldValue) {
				if(newValue !== undefined){
					if(newValue.IdEstado === 'ACT')
					{
						ppdi.showBloqueo = true;
					}
					else
					{
						ppdi.showBloqueo = false;
					}
				}
	        }
	    );
		
		ppdi.abrirModal = function(xIdPersona,xDocumentoIdentidad)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarDocumentoIdentidad",{ 
				DocumentoIdentidad: xDocumentoIdentidad , 
				IdPersona: xIdPersona, 
				CodigoFormulario: ppdi.CodigoFormulario}
				,false,false,ppdi.idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true)
				{
					ppdi.documentoIdentidadSeleccionado = undefined;
					ppdi.documentoIdentidadOptions.dataSource.read();
				}
	    	}); 
		};	
		
		ppdi.agregar = function() {
			ppdi.abrirModal(ppdi.IdPersona,undefined);
		};
		ppdi.eliminar = function(doc) {
			
			if(doc.IdDocumentoIdentidad){
				var registroCambio = [];
				registroCambio.push({
					CampoModificado: "Borrado",
					ValorNuevo: "S",
					IdEntidad: ppdi.documentoIdentidadSeleccionado.IdDocumentoIdentidad
				});
				
				var parametros = {
					p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
	        		p_IdUsuario 		:  $scope.$root.SesionIdUsuario,
	        		p_IdModulo 			: 'SOC',
	        		p_UsaReglasNegocio	: "N",
	        		p_IdTipoCanal 		: "OPT",
					p_RegistroCambio	: registroCambio
				};
				
				bcsComunes.ventanaOverlay(ppdi.ventanaId,true);
			
				servicioSociodeNegocio.eliminarPersonaDocumentoIdentidadMasivo(parametros).$promise.then(
					function(result){
						bcsComunes.ventanaOverlay(ppdi.idVentana,false);
						var srv=util.parseServerResponse(result);
						
						var subtitulo='';
						
						if (srv.erroresContador > 0){
							subtitulo='UI_OPERACION_CON_ERRORES';
							bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
						} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
							subtitulo='UI_DOCUMENTO_IDENTIDAD_BORRADO';
						} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
							 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
						}
						
						bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, ppdi.idVentana);
						ppdi.documentoIdentidadOptions.dataSource.read();
						
					},
					function(result){		
						bcsComunes.ventanaOverlay(ppdi.idVentana,false);						
						var srv=util.parseServerResponse(result);
						var p=bcsComunes.ventanaMsg({ 
							mensaje : '', 
							conflictos : srv
						},ppdi.idVentana);
					}
				);
			}
			/*
			var eliminarDocumentoIdentidadSP = function(v_Dato)
		    {
			       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1736";
			       var r = $resource
			         ("", {}, {
				                 'urlConsulta': { method: "POST", url: urlConsulta}
			                  }
			         );
			       var post = r.urlConsulta(v_Dato);
			       return post.$promise;
		    };
		    
		    var parametrosSP = {};
	        parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo 		= 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdDocumentoIdentidad = ppdi.documentoIdentidadSeleccionado.IdDocumentoIdentidad;
	        
	            	    	
	        eliminarDocumentoIdentidadSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : "Documento Identidad Borrado",
						conflictos : util.parseServerResponse(result)}, ppdi.idVentana);
					ppdi.documentoIdentidadOptions.dataSource.read();
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});*/
		};
		
		ppdi.modificar = function(doc) {
			ppdi.abrirModal(ppdi.IdPersona,doc);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppdi.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppdi.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['DOCUMENTO_IDENTIDAD'];
			ppdi.documentoIdentidadOptions = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1314",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppdi.IdPersona) },
						schema: {
							model: {
								fields: {
									IdDocumentoIdentidad			: { type: "number"},
									IdPersona						: { type: "number"},
									IdTipoDocumentoIdentidad		: { type: "string"},
									NombreTipoDocumentoIdentidad	: { type: "string"},
									DocumentoIdentidad				: { type: "string"},
									NumeroSerie						: { type: "string"},
									FechaEmision					: { type: "date"},
									FechaVencimiento				: { type: "date"},
									IdEstado						: { type: "string"},
									Estado							: { type: "string"},
									FechaIngreso					: { type: "date"},
									FechaCreacion					: { type: "date"},
									FechaModificacion				: { type: "date"},
									IdCarga							: { type: "number"},
									IdEstadoAutorizacion			: { type: "string"},
									IdOrigen						: { type: "number"},
									Predeterminado					: { type: "string"},
									IdEmpresa						: { type: "number"},
								}
							}
						}
					}
				}),
				reorderable: true,
				filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				columnMenu: false,
				resizable: true,
				autoBind: true,
				scrollable: true,
				height:200,
	
				columns: [{
					field: "NombreTipoDocumentoIdentidad",
					title: $translate.instant('UI_TIPO'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},{
					field: "DocumentoIdentidad",
					title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},{
					field: "NumeroSerie",
					title: $translate.instant('UI_NUMERO_SERIE'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},{
					field: "FechaEmision",
					title: $translate.instant('UI_FECHA_EMISION'),
					attributes: {style:"text-align:left;"},
					format: "{0:d}",
					width: "100px"
				},{
					field: "FechaVencimiento",
					title: $translate.instant('UI_FECHA_VENCIMIENTO'),
					attributes: {style:"text-align:left;"},
					format: "{0:d}",
					template: kendo.template("#if (kendo.toString(FechaVencimiento, 'd-MM-yyyy')=='31-12-0000') {##} else {##=kendo.toString(FechaVencimiento, 'dd-MM-yyyy')##}#"),
					width: "100px"
				},{
					field: "Predeterminado",
					title: $translate.instant('UI_PREDETERMINADO'),
					template : "<span translate>#: Predeterminado # </span>",
					attributes: {style:"text-align:left;"},
				},{
					field: "Estado",
					title: $translate.instant('UI_ESTADO'),
					attributes: {style:"text-align:left;"},
				}]
			};
		});	
		
		var cambioEstadoDocumentoIdentidadSP = function(v_Dato)
	    {
	       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100";
	       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
	       var post = r.urlConsulta(v_Dato);
	       return post.$promise;
	    };
	    
	    
	    ppdi.vencerDocumento = function () {
	    	
	    	var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
		parametrosSP.p_IdPersona = ppdi.IdPersona,
	        parametrosSP.p_IdModulo = 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "S";
	        parametrosSP.p_IdDocumentoIdentidad = ppdi.documentoIdentidadSeleccionado.IdDocumentoIdentidad;
	        parametrosSP.p_IdEstado = 'VCD';
	        parametrosSP.p_Comentario = 'Vencimiento por interfaz';
	        bcsComunes.ventanaOverlay(ppdi.idVentana, true);
	        cambioEstadoDocumentoIdentidadSP(parametrosSP).then(
				function(result) {
					var titulo = "";
					if(result.P_OUT_ERRORESDETALLE.length>0){
						titulo = "UI_ERROR";
					}else{
						titulo = "UI_DOCUMENTO_IDENTIDAD_VENCIDO";
					}
					var idVentanaModal = bcsComunes.ventanaMsg({ mensaje : titulo,
						conflictos : util.parseServerResponse(result)}, ppdi.idVentana);
					bcsComunes.ventanaPromesa(idVentanaModal).then(function(){
						ppdi.documentoIdentidadOptions.dataSource.read();
						bcsComunes.ventanaOverlay(ppdi.idVentana, false);
					});
					
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppdi.idVentana, false);
				});
	    };
	    

	    
	    /*
	    ppdi.bloqueoDocumento = function () {
	    	var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'PER';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdDocumentoIdentidad = ppdi.documentoIdentidadSeleccionado.IdDocumentoIdentidad;
	        parametrosSP.p_IdEstado = 'VCD';
	        parametrosSP.p_Comentario = 'Bloqueo por interfaz';	    	
	        cambioEstadoDocumentoIdentidadSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : "Documento de Identidad Bloqueado",
						conflictos : util.parseServerResponse(result)}, ppdi.idVentana);
					ppdi.documentoIdentidadOptions.dataSource.read();
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	    };
	    
	    ppdi.desbloqueoDocumento = function () {
	    	var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'PER';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdDocumentoIdentidad = ppdi.documentoIdentidadSeleccionado.IdDocumentoIdentidad;
	        parametrosSP.p_IdEstado = 'ACT';
	        parametrosSP.p_Comentario = 'Desbloqueo por interfaz';    	
	        cambioEstadoDocumentoIdentidadSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : "Documento de Identidad Bloqueado",
						conflictos : util.parseServerResponse(result)}, ppdi.idVentana);
					ppdi.documentoIdentidadOptions.dataSource.read();
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	    };*/
	    

	});
}());
//------------------------- FIN CONTROLLER --------------------------