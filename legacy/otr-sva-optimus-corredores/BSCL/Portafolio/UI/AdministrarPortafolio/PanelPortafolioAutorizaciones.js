/*************************************************************
 * Purpose	: Autorizaciones del Portafolio
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioAutorizacionesCtrl',
		function($scope, $translate, $resource, bcsComunes, bcsServicioPlataforma){
		
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
			var pPorAut = this;
			pPorAut.ventanaId = bcsComunes.ventanaWinId($scope);
			var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
			
			pPorAut.IdCliente = parametros.IdCliente;
			pPorAut.IdPortafolio = parametros.IdPortafolio;

			// Configuración de campos requeridos para formularios
			$scope.admPort.socioNegocioPromise && $scope.admPort.socioNegocioPromise.then(function () {
				pPorAut.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_AUTORIZACIONES'];
			});
			
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
			
			pPorAut.showBloqueo = false;
			$scope.$watch("pPorAut.autorizadoSeleccionado",
		        function handleAutorizado(newValue, oldValue) {
					if(newValue !== undefined){
						pPorAut.showBloqueo = newValue.IdEstado === 'ACT' ? true : false;
					}
		        }
		    );
			
			pPorAut.agregar = function() {
				pPorAut.abrirModal(pPorAut.IdCliente, pPorAut.IdPortafolio, undefined, pPorAut.IdPersona);
			};
			pPorAut.modificar = function(aut) {
				//pPorAut.abrirModal(pPorAut.IdCliente, pPorAut.IdPortafolio, aut);
			};
			pPorAut.eliminar = function(aut) {
				alert(aut);
			};
			
			pPorAut.bloquearAutorizado = function(){
		        var datos = {
	    			p_IdEmpresa			: $scope.SesionIdEmpresa,
	    			p_IdUsuario			: $scope.SesionIdUsuario,
	    			p_IdModulo			: 'POR',
	    			p_UsaReglasNegocio	: 'S',
	    			
	    			p_IdAutorizado	: pPorAut.autorizadoSeleccionado.IdAutorizado,
	    			p_IdEstado		: 'BLO',
	    			p_Comentario	: 'Bloqueo por interfaz'
	    		};
		        
		        bcsComunes.ventanaOverlay(pPorAut.ventanaId,true);
		        cambioEstadoAutorizadoSP(datos).then(
		        	function(result){
		        		var titulo = "";
			        	if(result.P_OUT_ADVERTENCIASCONTADOR){
			        		titulo = "La operacion tiene advertencias";
			        	}else if(result.P_OUT_ERRORESCONTADOR){
			        		titulo = "La operacion tiene errores";
			        	}else{
			        		titulo = "Autorizado a dar órdenes :: Bloqueado ::";
			        		pPorAut.gridAutorizados.dataSource.read();
			        	}
			        	bcsComunes.ventanaMsg({
							mensaje: titulo,
							conflictos:util.parseServerResponse(result)
						},pPorAut.ventanaId);
			        	
			        	bcsComunes.ventanaOverlay(pPorAut.ventanaId,false);
			        },
			        function(result){
			        	bcsComunes.ventanaMsg({
							mensaje: "La operacion tiene errores",
							conflictos:util.parseServerResponse(result)
						},pPorAut.ventanaId);
			        	bcsComunes.ventanaOverlay(pPorAut.ventanaId,false);
			        });
	        };
	        
	        pPorAut.desbloquearAutorizado = function(){
	        	var datos = {
	    			p_IdEmpresa			: $scope.SesionIdEmpresa,
	    			p_IdUsuario			: $scope.SesionIdUsuario,
	    			p_IdModulo			: 'POR',
	    			p_UsaReglasNegocio	: 'S',
	    			
	    			p_IdAutorizado	: pPorAut.autorizadoSeleccionado.IdAutorizado,
	    			p_IdEstado		: 'ACT',
	    			p_Comentario	: 'Desbloqueo por interfaz'
	    		};
	        	
	        	bcsComunes.ventanaOverlay(pPorAut.ventanaId,true);
	        	cambioEstadoAutorizadoSP(datos).then(
		        	function(result){
		        		var titulo = "";
			        	if(result.P_OUT_ADVERTENCIASCONTADOR){
			        		titulo = "La operacion tiene advertencias";
			        	}else if(result.P_OUT_ERRORESCONTADOR){
			        		titulo = "La operacion tiene errores";
			        	}else{
			        		titulo = "Autorizado a dar órdenes :: Desbloqueado ::";
			        		pPorAut.gridAutorizados.dataSource.read();
			        	}
			        	bcsComunes.ventanaMsg({
							mensaje: titulo,
							conflictos:util.parseServerResponse(result)
						},pPorAut.ventanaId);
			        	bcsComunes.ventanaOverlay(pPorAut.ventanaId,false);
			        },
			        function(result){
			        	bcsComunes.ventanaMsg({
							mensaje: "La operacion tiene errores",
							conflictos:util.parseServerResponse(result)
						},pPorAut.ventanaId);
			        	bcsComunes.ventanaOverlay(pPorAut.ventanaId,false);
			        });
	        };
			
		/*********************************************
		 * DATASOURCES
		 *********************************************/
			
	        if ($scope.SesionLocale === 'CL') {
	        	var columnass = [
	        		{field: "DocumentoIdentidad", template:"<label> #:  util.formatRut(DocumentoIdentidad) #</label>", title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "110px"},
			    	{field: "NombreTipoAutorizado", title: $translate.instant('UI_TIPO_AUTORIZACION'), width: "100px"},
			    	{field: "NombreCompleto", title: $translate.instant('UI_NOMBRE_COMPLETO'), width: "150px"},
			    ];
	        }
	        
	        if ($scope.SesionLocale === 'PE') {
		        var columnass = [
				    {field: "DocumentoIdentidad", width: "110px"},
				    {field: "NombreTipoAutorizado", title: $translate.instant('UI_TIPO_AUTORIZACION'), width: "100px"},
				    {field: "NombreCompleto", title: $translate.instant('UI_NOMBRE_COMPLETO'), width: "150px"},
				];
	        }
	        
	        if ($scope.SesionLocale === 'PE') {
	        	columnass.push({field: "NumeroRegistroPublico", title: $translate.instant('UI_NUMERO_REGISTRO_PUBLICO'), width: "110px"});
	        	columnass.push({field: "Asiento", title: $translate.instant('UI_ASIENTO'), width: "100px"});
	        	columnass.push({field: "FichaPartida", title: $translate.instant('UI_FICHA_O_PARTIDA'), width: "100px"});
	        	columnass.push({field: "TipoFirma", title: $translate.instant('UI_TIPO_FIRMA'), width: "100px"});
	        }
	        
	        columnass.push({field: "Estado", title: $translate.instant('UI_ESTADO'), width: "100px"});
	        
			pPorAut.gridAutorizados = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, 
						serverPaging 	: true,
						serverSorting 	: true,
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1262",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdPortafolio", operator: "eq", value: Number(pPorAut.IdPortafolio) }
						],
						schema: {
							model: {
								fields: {
									IdAutorizado		: { type: "number"},
									IdPortafolio		: { type: "number"},
									IdPersona			: { type: "number"},
									IdTipoAutorizado	: { type: "string"},
									NombreTipoAutorizado: { type: "string"},
									IdEstado			: { type: "string"},
									Estado				: { type: "string"},
									IdEstadoAutorizacion: { type: "string"},
									FechaIngreso		: { type: "date"},
									FechaCreacion		: { type: "date"},
									FechaModificacion	: { type: "date"},
									IdUsuarioCreador	: { type: "number"},
									IdDocumentoIdentidad: { type: "number"},
									Tipo				: { type: "string"},
									NombreCompleto		: { type: "string"},
									Nombres				: { type: "string"},
									ApellidoPaterno		: { type: "string"},
									ApellidoMaterno		: { type: "string"},
									RazonSocial			: { type: "string"},
									DocumentoIdentidad	: { type: "string"},
									
									NumeroRegistroPublico	: { type: "string"},
									Asiento					: { type: "string"},
									FichaPartida			: { type: "string"},
									TipoFirma				: { type: "string"}
								}
							}
						}
					}
				}),
				columns: columnass,
				sortable			: false,
				pageable			: true,
				autoBind			: true,
				height				: 200,
			    selectable 			: "row"
			};
			
			pPorAut.PortafolioDataSource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3518",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdPortafolio 	: { type: "number"},
								IdCliente		: { type: "number"},
								IdPersona		: { type: "number"},	
							}
						}
					}
				}
			});
			
			var cambioEstadoAutorizadoSP = function(p_datos){
				var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=162";
				var r = $resource("", {}, {'urlConsulta': {method:"POST", url:urlConsulta}});
				var post = r.urlConsulta(p_datos);
				return post.$promise;
		    };
		    
		    var filtroConsultaPortafolio = [ { field: "IdPortafolio", operator: "eq", value: Number(pPorAut.IdPortafolio) } ];
		    pPorAut.portafolioPromise = pPorAut.PortafolioDataSource.query({filter:filtroConsultaPortafolio}).then(function(){
				if(pPorAut.PortafolioDataSource.view().length>0){
					pPorAut.IdPersona = pPorAut.PortafolioDataSource.view()[0].IdPersona;
				}
			});
		    
			pPorAut.abrirModal = function(p_IdCliente, p_IdPortafolio, p_IdAutorizado, p_IdPersona){
				var id1 = bcsComunes.ventanaAbrir("agregarModificarPortafolioAutorizacion", {
					IdCliente: p_IdCliente,
					IdPortafolio: p_IdPortafolio,
					IdAutorizado: p_IdAutorizado,
					IdPersona: p_IdPersona,
					CodigoFormulario: pPorAut.CodigoFormulario
				}, false, false, pPorAut.ventanaId);
				var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(data){
		        	if (data === true){
						pPorAut.autorizadoSeleccionado = undefined;
						pPorAut.gridAutorizados.dataSource.read();
					}
		    	}); 
			};
		});
}());
//------------------------- FIN CONTROLLER --------------------------