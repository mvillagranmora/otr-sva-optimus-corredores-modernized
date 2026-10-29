(function () {
    angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoPersonaDireccionesFisicasEnrolCtrl',
        function ($scope, $translate, servicioSociodeNegocio, bcsComunes,bcsAlerta) {
            /******************************************************
             * INICIO DE VARIABLES
             * ***************************************************/
            const ppdf = this;
            ppdf.idVentana = bcsComunes.ventanaWinId($scope);
			ppdf.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
            ppdf.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['DIRECCION_FISICA'];

            ppdf.abrirModal = function(xIdEnrolamiento, xDireccionFisica,xCodigoFormulario) {
                let id1 = bcsComunes.ventanaAbrir("agregarModificarEnrolamientoDireccionFisica", {
                    DireccionFisica: xDireccionFisica,
                    IdEnrolamiento : xIdEnrolamiento, 
					CodigoFormulario: xCodigoFormulario
                }, false, false, ppdf.idVentana);
                let promesa = bcsComunes.ventanaPromesa(id1);
                promesa.then(function (data) {
                    if (data === true) {
                        ppdf.direccionFisicaSeleccionada = undefined;
                        ppdf.direccionFisicaOptions.dataSource.read();
                    }
                });
            };

            ppdf.agregar = function agregar() {
                ppdf.abrirModal(ppdf.IdEnrolamiento, undefined, ppdf.CodigoFormulario);
            };

			ppdf.eliminar = function eliminar(dfi) {
				var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar la dirección seleccionada?", ppdf.idVentana);
				confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
					if(result==1){
						const datos = {
							p_IdEmpresa 			: $scope.SesionIdEmpresa,
							p_IdUsuario 			: $scope.SesionIdUsuario,
							p_IdModulo 				: 'SOC',
							p_UsaReglasNegocio 		: 'S',
							p_CodigoFormulario		: ppdf.CodigoFormulario,
							p_IdTipoCanal			: 'OPT',
							p_DireccionesFisicas:[{
								IdInterno : 1,
								IdDireccionFisica: dfi.IdDireccionFisica,
								IdEnrolamiento: dfi.IdEnrolamiento,
								Calle: dfi.Calle || '',
								Numero: dfi.Numero || '',
								Sector: dfi.Sector || ''
							}]
						}
	
						bcsComunes.ventanaOverlay(ppdf.idVentana,true)
						servicioSociodeNegocio.eliminarBorradorDireccionFisica(datos).$promise.then(
							function(result) {
								bcsComunes.ventanaOverlay(ppdf.idVentana,false);
								if (result.P_OUT_ERRORESCONTADOR > 0){
									bcsComunes.msg("error","error","Error al borrar dirección física");
									return;
								}
								
								bcsComunes.msg("info","info","Direccion Física borrada");
								
								bcsComunes.ventanaMsg({ mensaje : "Direccion Física borrada",conflictos : util.parseServerResponse(result)}, ppdf.idVentana);
								ppdf.direccionFisicaOptions.dataSource.read();
							},
							function(result){
								bcsComunes.msg("error","error","Error al borrar Dirección Física");
							});
					}
				});
			};

			ppdf.predeterminar = function(dfi) {
				const cambiaPredeterminado = dfi.Predeterminado === 'UI_SI' ? 'N' : 'S';
				const datos = {
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario,
					p_IdModulo 				: 'SOC',
					p_UsaReglasNegocio 		: 'S',
					p_CodigoFormulario		: ppdf.CodigoFormulario,
					p_IdTipoCanal			: 'OPT',
					p_DireccionesFisicas:[{
						IdInterno : 1,
						IdDireccionFisica: dfi.IdDireccionFisica || 0,
						IdEnrolamiento: dfi.IdEnrolamiento,
						CodigoProposito: dfi.CodigoProposito || '',
						Calle: dfi.Calle || '',
						Numero: dfi.Numero || '',
						Sector: dfi.Sector || '',
						Departamento: dfi.Departamento || '',
						CodigoPostal    : dfi.CodigoPostal || '',
                        IdCiudad        : dfi.IdCiudad,
                        IdDivisionPais  : dfi.IdDivisionPais || 0,
 						Comentario      : dfi.Comentario || '',
						CodigoCompuesto : dfi.CodigoCompuesto || '',
						Predeterminado: cambiaPredeterminado
					}]
				}
		  
		        bcsComunes.ventanaOverlay(ppdf.idVentana, true);    	    	
		        servicioSociodeNegocio.modificarBorradorDireccionFisica(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_DIRECCION_FISICA_PREDETERMINADO"),
							conflictos : util.parseServerResponse(result)}, ppdf.idVentana);
						ppdf.direccionFisicaOptions.dataSource.read();
						bcsComunes.ventanaOverlay(ppdf.idVentana, false);
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
						bcsComunes.ventanaOverlay(ppdf.idVentana, false);
					});
			};
			
            ppdf.modificar = function modificar(dfi) {
                ppdf.abrirModal(ppdf.IdEnrolamiento, dfi, ppdf.CodigoFormulario);
            };

            $scope.admSoc.socioNegocioPromise && $scope.admSoc.socioNegocioPromise.then(function () {
                
                ppdf.direccionFisicaOptions = {
                    dataSource : new bcs.DataSource({
                        type      : "odata",
                        definition: {
                            serverFiltering: true,
                            transport      : {
                                read: {
                                    url     : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700204",
                                    dataType: "json"
                                }
                            },
                            filter         : { field: "IdEnrolamiento", operator: "eq", value: Number(ppdf.IdEnrolamiento) },
                            schema         : {
                                model: {
                                    fields: {
                                        IdDireccionFisica     : { type: "number" },
                                        Calle                 : { type: "string" },                                        
                                        Numero                : { type: "string" },
                                        Departamento          : { type: "string" },
										Sector                : { type: "string" },                         
                                        CodigoPostal          : { type: "string" },
										IdCiudad              : { type: "number" },
										NombreCiudad          : { type: "string" },
       									CodigoProposito       : { type: "string" },
										DescripcionCodigoProposito: { type: "string" },
										Comentario            : { type: "string" }, 
                                 		IdEnrolamiento	      : { type: "number" },
                                        IdDivisionPais        : { type: "number" },
										NombreDivisionPais    : { type: "string" },
                                        Predeterminado        : { type: "string" },
                                        CodigoCompuesto       : { type: "string" },
                                    }
                                }
                            }
                        }
                    }),
                    reorderable: true,
                    filterable : false,
                    groupable  : false,
                    pageable   : false,
                    selectable : "row",
                    columnMenu : false,
                    resizable  : true,
                    autoBind   : true,
                    scrollable : true,
                    height     : 200,
                    columns    : [ {
                        field     : "Calle",
                        title     : $translate.instant('UI_CALLE'),
                        attributes: { style: "text-align:left;" },
                        width     : "200px"
                    }, {
                        field     : "Numero",
                        title     : $translate.instant('UI_NUMERO'),
                        attributes: { style: "text-align:left;" },
                        width     : "100px"
                    }, {
                        field     : "Departamento",
                        title     : ($scope.SesionLocale == 'PE')
                                    ? $translate.instant('UI_REFERENCIA')
                                    : $translate.instant('UI_COMPLEMENTO'),
                        attributes: { style: "text-align:left;" },
                        width     : "100px"
                    }, {
                        field     : "Sector",
                        title     : $translate.instant('UI_SECTOR'),
                        attributes: { style: "text-align:left;" },
                        width     : "150px"
                    }, {
                        field     : "CodigoPostal",
                        title     : $translate.instant('UI_CODIGO_POSTAL'),
                        attributes: { style: "text-align:left;" },
                        width     : "100px"
                    },{
                        field     : "NombreCiudad",
                        title     : $translate.instant('UI_CIUDAD'),
                        attributes: { style: "text-align:left;" },
                        width     : "200px"
                    }, {
                        field     : "DescripcionCodigoProposito",
                        title     : $translate.instant('UI_CODIGO_PROPOSITO'),
                        attributes: { style: "text-align:left;" },
                        width     : "100px"
                    }, {
                        field     : "Comentario",
                        title     : $translate.instant('UI_COMENTARIO'),
                        attributes: { style: "text-align:left;" },
                        width     : "200px"
                    }, {
                        field     : "NombreDivisionPais",
                        title     : ($scope.SesionLocale == 'PE')
                                    ? $translate.instant('UI_UBIGEO')
                                    : $translate.instant('UI_CODIGO_UNICO_TERRITORIAL'),
                        attributes: { style: "text-align:left;" },
                        width     : "150px"
                    }, {
                        field     : "Predeterminado",
                        title     : $translate.instant("UI_PREDETERMINADO"),
                        template  : "<span translate> #: Predeterminado # </span>",
                        attributes: { style: "text-align:left;" },
                        width     : "80px"
                    }, {
                        field     : "CodigoCompuesto",
                        title     : $translate.instant('UI_CODIGO_COMPUESTO'),
                        attributes: { style: "text-align:left;" },
                        width     : "150px"
                    }  ]
                };
            });
        });
}());
//------------------------- FIN CONTROLLER --------------------------