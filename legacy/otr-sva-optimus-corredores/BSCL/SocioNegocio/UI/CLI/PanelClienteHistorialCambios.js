/*************************************************************
 * Purpose	: Agregar panel de historial de cambios en detalle de Clientes en Socio Negocio
 * Author	: GCuevas
 * Date		: 10/03/2021
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteHistorialCambiosCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsConfiguracionSociodeNegocio,
			bcsComunes,
			$stateParams)
	{
			/******************************************************
			 * INICIO DE VARIABLES
			 * ***************************************************/	
			var pchc = this;

		    pchc.IdCliente = $scope.admSoc.dataSocioNegocio.IdCliente;
		    pchc.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;

		    $scope.admSoc.socioNegocioPromise.then(function () {
		    	pchc.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['HISTORIAL_CAMBIOS_CLIENTE'];
			});

		    /*********************************************
			 * FUNCIONES
			 *********************************************/
			pchc.revisarencuesta = function() {
				
				if (pchc.RegistroCategoriaInversionistaSeleccionado && pchc.RegistroCategoriaInversionistaSeleccionado.TipoIngreso === 'Encuesta') {
					bcsComunes.ventanaAbrir("VerDetalleEncuesta",{  IdRegistroEncuesta : pchc.RegistroCategoriaInversionistaSeleccionado.IdRegistroEncuesta, Canal : pchc.RegistroCategoriaInversionistaSeleccionado.IdCanal},false,false,pchc.ventanaId);
				}
				
			}
		    /*********************************************
			 * GRILLAS
			 *********************************************/
			pchc.RegistroCategoriaInversionistaOptions = {
					dataSource		: new bcs.DataSource({
						type 		: "odata",
						definition 	: {
							serverFiltering : true,
							serverPaging 	: true,
							serverSorting 	: true,
							pageSize 		: 50,
							transport: {
								read		: {
												url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=100060",
												dataType: "json"
											  }
								},						
							filter			: { field: "IdCliente", operator: "eq", value: Number(pchc.IdCliente)},
							sort 			: { field: "FechaIngresoRegistro", dir: "desc" },
							schema 			: {
								model	: { fields	: { 
									FechaRegistroCategoria			: { type: "datetime"},
									IdRegistroEncuesta				: { type: "number"},
									CodigoUsuarioResponde			: { type: "string"},
									NombreCategoriaInversionista	: { type: "string"},
									IdCanal							: { type: "string"},
									TipoIngreso						: { type: "string"},
									CodigoPortafolio				: { type: "string"},
									IdPerfilProductos				: { type: "string"}
									}
								}
							},
						}
					}),
					columns	: [
						{
							field: "FechaRegistroCategoria",
							title: $translate.instant('UI_FECHA_REGISTRO'),
							attributes: {style:"text-align:left;"},
							format: "{0:d}",
							width: "100px"
						},						
						{
							field: "CodigoUsuarioResponde",
							title: $translate.instant('UI_USUARIO'),
							width: "50px"
						},{
							field: "NombreCategoriaInversionista",
							title: $translate.instant('UI_CATEGORIA_INVERSIONISTA'),
							width: "80px"
						},{
							field: "IdCanal",
							title: $translate.instant('UI_CANAL'),
							width: "50px"
						},{
							field: "TipoIngreso",
							title: $translate.instant('UI_TIPO_MODIFICACION'),
							width: "70px"
						},{
							field: "CodigoPortafolio",
							title: $translate.instant('UI_CODIGO_PORTAFOLIO'),
							width: "70px"
						},{
							field: "IdPerfilProductos",
							title: $translate.instant('UI_PANTILLA_PRODUCTO'),
							width: "70px"
						}],
					sortable			: false,
					groupable			: false,
					pageable			: false,
					autoBind			: true,
					height				: 120,
				    selectable 			: "row"
			};
			
			pchc.LogHistorialCambiosOptions = {
					dataSource		: new bcs.DataSource({
						type 		: "odata",
						definition 	: {
							serverFiltering : true,
							serverPaging 	: true,
							serverSorting 	: true,
							pageSize 		: 5,
							transport: {
								read		: {
												url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700288",
												dataType: "json"
											  }
								},
							filter: [
								{ field: "IdPersona", operator: "eq", value: Number(pchc.IdPersona) }
							],					
							sort 			: { field: "FechaRegistro", dir: "desc" },
							schema 			: {
								model	: { fields	: { 
									FechaRegistro			: { type: "datetime"},
									CodigoUsuario			: { type: "string"},
									ValorAntiguo 			: { type: "string"},
									ValorNuevo   			: { type: "string"},
									CampoRegistro 			: { type: "string"},
									IdPersona				: { type: "string"}
									}
								}
							},
						}
					}),
					columns	: [{
							field: "CodigoUsuario",
							title: $translate.instant('UI_USUARIO'),
							attributes: {style:"text-align:left;"},
							width: "60px"
						},{
							field: "CampoRegistro",
							title: $translate.instant('UI_CAMPO_MODIFICADO'),
							attributes: {style:"text-align:left;"},
							width: "80px"
						},{
							field: "ValorAntiguo",
							title: $translate.instant('UI_VALOR_ANTIGUO'),
							attributes: {style:"text-align:left;"},
							width: "80px"
						},{
							field: "ValorNuevo",
							title: $translate.instant('UI_VALOR_NUEVO'),
							attributes: {style:"text-align:left;"},
							width: "80px"
						},{
							field: "FechaRegistro",
							title: $translate.instant('UI_FECHA_REGISTRO'),
							attributes: {style:"text-align:left;"},
							format: "{0:d}",
							width: "100px"
						}],
						sortable			: false,
						groupable			: false,
						pageable			: false,
						autoBind			: true,
						height				: 120,
					    selectable 			: "row"
				};
	});
 }());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
