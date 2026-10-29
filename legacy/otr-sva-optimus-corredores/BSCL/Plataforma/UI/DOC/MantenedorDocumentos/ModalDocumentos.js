angular.module('ModuloPlataforma').controller('ModalDocumentosCtrl',
	function ($scope, bcsComunes, $translate, $resource, $timeout) {


		var ventanaId = bcsComunes.ventanaWinId($scope);
		parametros = bcsComunes.ventanaParams(ventanaId);
		var noIngresado = true;
		var plantillaIngresada = "";
      	var pModal = this;
	
		var ingresadoGrilla = 	false;
		
		pModal.idVentana = bcsComunes.ventanaWinId($scope);
	
		$scope.canalesIngreso = [];
		$scope.CanalEnrolamiento = [];
		
		$scope.data = {
			NumFirmasMinimas: 	0,
			NumFirmasMaximas: 	0,
			ValidaFecha: 		false,
			FirmaAutomatica: false,
		};
		$scope.data.Habilitado = true;
		$scope.data.oNumFirmasMaximasOptions = { min: 0 };
		$scope.CambioCanales	=	false;
		$scope.CambioCanalesEnrolamiento = false;
		$scope.indicadoresRequisitos = [];
		$scope.data.FirmaAutomatica = parametros.FirmaAutomaticaDocumento === 'S' ? true : false
		
		$scope.plantillasContrato = [];

		$scope.filtrosFormularioIniciales = {
			consultaIndicadoresRequisitos: {
				IdUso: { field: 'IdUso', operator: 'eq', value: 'REQ' },
				IdEstado: { field: 'IdEstado', operator: 'eq', value: 'ACT' },
				IdTipoEntidad: { field: 'IdTipoEntidad', operator: 'eq', value: 'CLIENTE' }
			},
			consultaPlantillasContrato: {
				CodigoArchivo: { field: 'CodigoArchivo' }
			},
			consultaCanalesIngreso: {
				PermiteFirmaDocumento: { field: 'PermiteFirmaDocumento', operator: 'eq', value: 'S' }
			},
			consultaCanalesEnrolamiento: {
				PermiteFirmaDocumento: { field: 'PermiteEnrolamiento', operator: 'eq', value: 'S' }
			}
		};
		$scope.filtrosFormulario = Object.assign({}, $scope.filtrosFormularioIniciales);

		(function init() {
			const dataSource = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700322",
							dataType: "json"
						}
					},
					filter: { field: 'IdTipoDocumento', operator: 'eq', value: parametros.IdTipoDocumento || '' },
					schema: {
						model: {
							fields: {
								IdTipoIndicador: { type: 'number' },
								IdTipoEntidad: { type: 'string' },
								IdEmpresa: { type: 'number' },
								NombreTipoIndicador: { type: 'string' },
								GrupoIndicador: { type: 'string' },
								IdUso: { type: 'string' },
								IdEstado: { type: 'string' },
								IdTipoPersona: { type: 'string' },
								RequiereFirmaCliente: { type: 'string' },

							}
						}
					}
				}
			});


			$scope.grillaIndicadoresRequisitosOptions = {
				gridCreated: function (grid) {
					grid.dataSource.bind('change', function () {
						$scope.indicadoresRequisitos = grid.dataSource.data();

						$scope.idsPersonasFirmantes = [];
						let filtrosNuevosCombo = Object.assign({}, $scope.filtrosFormularioIniciales.consultaIndicadoresRequisitos);

						for (let indicadorRequisito of $scope.indicadoresRequisitos) {
							const key = 'IdTipoIndicador' + indicadorRequisito.IdTipoIndicador;
							filtrosNuevosCombo[key] = {
								field: 'IdTipoIndicador',
								operator: 'neq',
								value: indicadorRequisito.IdTipoIndicador
							};

							$scope.idsPersonasFirmantes.push(indicadorRequisito.IdTipoIndicador);
						}
						$scope.filtrosFormulario.consultaIndicadoresRequisitos = filtrosNuevosCombo;

					});
				},
				dataSource: dataSource,
				reorderable: true,
				filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				columnMenu: false,
				resizable: true,
				autoBind: true,
				scrollable: true,
				height: 100,
				columns: [
					{
						field: "NombreTipoIndicador",
						title: $translate.instant('UI_REQUISITOS'),
						attributes: { style: "text-align: left;" },
						width: "300px"
					}
				]
			};


		})();

		var dataSourcePlantilla = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				serverSorting: true,
				sort: { field: "IdPlantillaContratoDocumento", dir: "desc" },
				filter: { field: "IdTipoDocumento", operator: "eq", value: parametros.IdTipoDocumento || '' },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290681",
						dataType: "json"
					},
				},
				schema: {
					model: {
						fields: {
							PlantillaContrato: { type: "string" },
							Estado: { type: "string" },
							FechaVigencia: { type: "date" },
							FechaVencimiento: { type: "date" },
						}
					}

				}
			},
		});

		$scope.grillaVersionPlantillaContrato = {
     
			gridCreated: function (grid) {
				grid.dataSource.bind('change', function () {
					$scope.plantillasContrato = grid.dataSource.data();
				});
			},
           
			dataSource: dataSourcePlantilla,
			reorderable: true,
			filterable: false,
			groupable: false,
			pageable: false,
			selectable: "row",
			columnMenu: false,
			resizable: true,
			autoBind: true,
			scrollable: true,
			height: 125,
			columns: [
				{
					field: "PlantillaContrato",
					title: $translate.instant('UI_PLANTILLA_CONTRATO'),
					attributes: { style: "text-align: left;" },
					width: "100px"
				},
				{
					field: "Estado",
					title: $translate.instant('UI_ESTADO'),
					attributes: { style: "text-align: left;" },
					width: "100px"
				},
				{
					field: "FechaVigencia",
					title: $translate.instant('UI_FECHA_VIGENCIA'),
					attributes: { style: "text-align: left;" },
					width: "100px"
				},
				{
					field: "FechaVencimiento",
					title: $translate.instant('UI_FECHA_VENCIMIENTO'),
					attributes: { style: "text-align: left;" },
					width: "100px"
				}
			]
		};


		$scope.GrillaCanalesDataSource = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				filter: { field: "IdTipoDocumento", operator: "eq", value: parametros.IdTipoDocumento || '' },
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=80000",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdTipoCanalIngreso: { type: 'string' },
							IdTipoDocumento: 	{ type: 'string' },
							Nombre: 			{ type: 'string' },
							
						}
					}
				}
			}
		});

		$scope.GrillaCanalesOptions = {
			gridCreated: function (grid) {
				grid.dataSource.bind('change', function () {
					$scope.canalesIngreso = grid.dataSource.data();
					
				});
			},
			dataSource: $scope.GrillaCanalesDataSource,
			height: 100,
			selectable: "row",
			autoBind: true,
			columns: [
				{
					field: "Nombre",
					title: $translate.instant('UI_CANALES_FIRMA'),
					attributes: { style: "text-align: left;" },
					width: "300px"
				}
			]
		};

		$scope.agregarIndicadorRequisito = function agregarIndicadorRequisito(indicadorRequisito) {
			const indicadorRequisitoGrilla = $scope.indicadoresRequisitos.find(function (ir) {
				return ir.IdTipoIndicador === indicadorRequisito.IdTipoIndicador;
			});

			if (indicadorRequisitoGrilla === undefined) {
				$scope.indicadoresRequisitos.push(indicadorRequisito)
				$scope.grillaIndicadoresRequisitosOptions.dataSource.data($scope.indicadoresRequisitos);
			}
		};

		$scope.quitarIndicadorRequisito = function quitarIndicadorRequisito(indicadorRequisito) {
			const indicadorRequisitoGrilla = $scope.indicadoresRequisitos.find(function (p) {
				return p.IdPersona === indicadorRequisito.IdPersona;
			});

			$scope.indicadoresRequisitos.remove(indicadorRequisitoGrilla);
			$scope.grillaIndicadoresRequisitosOptions.dataSource.data($scope.indicadoresRequisitos);
			delete $scope.data.IndicadorRequisitoGrilla;
		};


		$scope.agregarVersionPlantilla = function agregarVersionPlantilla(Plantilla) {
			
		//new *******************
			const RequisitoGrilla = $scope.plantillasContrato.find(function (ir) {
				return ir.PlantillaContrato === Plantilla.CodigoArchivo;
			});
			
			var datos= {}; ;

			datos.PlantillaContrato =Plantilla.CodigoArchivo;
			datos.Estado='';
			datos.FechaVigencia = util.dateInicioDia($scope.SesionFechaSistema);
			datos.FechaVencimiento= util.dateInicioDia($scope.SesionFechaSistema);
			
			
			if (RequisitoGrilla === undefined) {
				if (ingresadoGrilla){
				$scope.plantillasContrato.shift();
			   }
				$scope.plantillasContrato.unshift(datos)
				$scope.grillaVersionPlantillaContrato.dataSource.data($scope.plantillasContrato);
				ingresadoGrilla= true;
			}
		//new *******************

			if (parametros.IdTipoDocumento) {
				noIngresado = false;
				plantillaIngresada = Plantilla.PlantillaContrato;
			}

		}


		$scope.Cerrar = function () {
			bcsComunes.ventanaCerrar(ventanaId);
		
		};
	
	
		if (parametros.IdTipoDocumento) {
			$scope.data.Habilitado = false;
			$scope.data.CodigoDocumento = parametros.IdTipoDocumento;
			$scope.data.NombreDocumento = parametros.NombreDocumento;
			$scope.data.NumFirmasMinimas = parametros.NumeroFirmasMinimas;
			$scope.data.NumFirmasMaximas = parametros.NumeroFirmasMaximas;
			$scope.data.Glosa = parametros.Glosa;
			$scope.data.ValidaFecha = parametros.ValidaFechaVigencia === 'S';
			$scope.data.IdTipoPersona = parametros.IdTipoPersona === 'A' ? 0 : parametros.IdTipoPersona;
			$scope.data.IdTipoPersonaDef = $scope.data.IdTipoPersona;
			$scope.data.SolicitarFechaFirma = parametros.SolicitarFechaFirma === 'S';
			$scope.data.IdClaseDef = parametros.IdClase;
			$scope.data.CodigoNormativa = parametros.TipoDocumentoNormativa;
			$scope.data.FechaFirma = parametros.FechaFirma === 'S';
			$scope.data.CodigoArchivo = parametros.CodigoArchivo;
			$scope.data.CodigoArchivoDef = parametros.CodigoArchivo;
			$scope.data.ValidaClienteFull = parametros.ValidaClienteFull === 'S';
			$scope.data.RequiereFirmaCliente = parametros.RequiereFirmaCliente === 'S';
			$scope.data.BloquearAlVencer = parametros.BloquearAlVencer === 'S';
		}



		$scope.AbreVentanaConfirmacion = function () {
			const request = {};
			request.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			request.p_IdUsuario = $scope.$root.SesionIdUsuario;
			request.p_IdModulo = 'CLI';
			request.p_UsaReglasNegocio = "N";
			request.p_TipoDocumentos = [
				{
					IdTipoDocumento: $scope.data.CodigoDocumento,
					NombreDocumento: $scope.data.NombreDocumento,
					NumeroFirmantesMinimo: $scope.data.NumFirmasMinimas,
					NumeroFirmantesMaximo: $scope.data.NumFirmasMaximas,
					Glosa: $scope.data.Glosa,
					ValidaFechaVigencia: $scope.data.ValidaFecha ? 'S' : 'N',
					IdTipoPersona: $scope.data.IdTipoPersona === 0 ? 'A' : $scope.data.IdTipoPersona,
					SolicitarFechaFirma: $scope.data.SolicitarFechaFirma ? 'S' : 'N',
					IdClase: $scope.data.IdClase,
					TipoDocumentoNormativa: $scope.data.CodigoNormativa,
					FechaFirma: $scope.data.FechaFirma ? 'S' : 'N',
					CodigoArchivo: $scope.data.CodigoArchivo,
					ValidaClienteFull: $scope.data.ValidaClienteFull ? 'S' : 'N',
					RequiereFirmaCliente: $scope.data.RequiereFirmaCliente ? 'S' : 'N',
					BloquearAlVencer: $scope.data.BloquearAlVencer ? 'S' : 'N',
					FirmaAutomaticaDocumento: $scope.data.FirmaAutomatica ? "S":"N",
					Requisitos: $scope.indicadoresRequisitos.map(function (ir) {
						return {
							IdTipoIndicador: ir.IdTipoIndicador
						}
					})
				}
			];
			request.CodigoDocumento 				= $scope.data.CodigoDocumento
			request.IdClaseDef 						= parametros.IdClase;
			request.IdTipoDocumento 				= parametros.IdTipoDocumento;
			request.IdTipoPersona 					= $scope.data.IdTipoPersona === 0 ? 'A' : $scope.data.IdTipoPersona;
			request.SolicitarFechaFirma				= parametros.SolicitarFechaFirma ? 'S' : 'N';
			request.RequiereFirmaCliente 			= $scope.data.RequiereFirmaCliente ? 'S' : 'N';
			request.p_CodigoArchivo 				= $scope.data.CodigoArchivo;
			request.actualizaPlantilla 				= ($scope.data.CodigoArchivoDef !== $scope.data.CodigoArchivo )? true:false;
			request.actualizarCanales				= $scope.CambioCanales;
			request.UpdateCanalesEnrolamiento 		= $scope.CambioCanalesEnrolamiento;
			request.CanalesDeIngreso 				= $scope.canalesIngreso;
			request.CanalesDeEnrolamiento 			= $scope.CanalEnrolamiento;
	
			var id1 = bcsComunes.ventanaAbrir("ModalConfirmacion", { mDoc: request }, false, false, ventanaId);
			var promesa = bcsComunes.ventanaPromesa(id1);
			promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		$scope.grillaVersionPlantillaContrato.dataSource.read();
					bcsComunes.ventanaCerrar(ventanaId);
				}
				
	    	});
		

		};

	
		$scope.$watchGroup(['data.NumFirmasMinimas'], function (newValues, oldValues) {
			if (newValues.length > 0) {
				const numFirmasMinimas = parseInt(newValues[0], 10);
				if (typeof numFirmasMinimas === 'number') {
					$scope.data.oNumFirmasMaximasOptions = { min: numFirmasMinimas }
				}
			}
		});
		
		
		$scope.agregarCanal = function agregarCanal(canalIngreso) {
			$scope.CambioCanales = true;
			const grillaCanales = $scope.canalesIngreso.find(function (ir) {
				return ir.Nombre === canalIngreso.Nombre;
			});
			if (grillaCanales === undefined) {
				$scope.canalesIngreso.push(canalIngreso);
				$scope.GrillaCanalesOptions.dataSource.data($scope.canalesIngreso);
			}
		
			
		};
		
		$scope.quitarCanal = function quitarCanal(canalIngreso) {
			$scope.CambioCanales = true;
			const grillaCanales = $scope.canalesIngreso.find(function (p) {
				return p.Nombre === canalIngreso.Nombre;
			});

			$scope.canalesIngreso.remove(grillaCanales);
			$scope.GrillaCanalesOptions.dataSource.data($scope.canalesIngreso);
			delete $scope.data.grillaCanales;
		};
		
		
		
		
		$scope.GrillaCanalesEnrolamientoDataSource = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				filter: { field: "IdTipoDocumento", operator: "eq", value: parametros.IdTipoDocumento || '' },
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=80002",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdTipoCanal: 	{ type: 'string' },
							Nombre: 		{ type: 'string' },
							
						}
					}
				}
			}
		});

		$scope.GrillaCanalesEnrolamientoOptions = {
			gridCreated: function (grid) {
				grid.dataSource.bind('change', function () {
					$scope.CanalEnrolamiento = grid.dataSource.data();
					
				});
			},
			dataSource: $scope.GrillaCanalesEnrolamientoDataSource,
			height: 100,
			selectable: "row",
			autoBind: true,
			columns: [
				{
					field: "Nombre",
					title: $translate.instant('UI_CANALES_ENROLAMIENTO'),		// AGREGAR DICCIONARIO
					attributes: { style: "text-align: left;" },
					width: "300px"
				}
			]
		};

		$scope.agregarCanalEnrolamiento = function agregarCanalEnrolamiento(Canal) {
			$scope.CambioCanalesEnrolamiento = true;
			const grillaCanalesEnrolamiento = $scope.CanalEnrolamiento.find(function (ir) {
				return ir.Nombre === Canal.Nombre;
			});
			if (grillaCanalesEnrolamiento === undefined) {
				var arreglado =  { 
					IdTipoCanal: Canal.IdTipoCanalIngreso,
					Nombre: Canal.Nombre,
				};
				$scope.CanalEnrolamiento.push(arreglado);
				$scope.GrillaCanalesEnrolamientoOptions.dataSource.data($scope.CanalEnrolamiento);
			}
		
			
		};
		
		$scope.quitarCanalEnrolamiento = function quitarCanalEnrolamiento(Canal) {
			$scope.CambioCanalesEnrolamiento = true;
			const grillaCanalesEnrolamiento = $scope.CanalEnrolamiento.find(function (p) {
				return p.Nombre === Canal.Nombre;
			});

			$scope.CanalEnrolamiento.remove(grillaCanalesEnrolamiento);
			$scope.GrillaCanalesEnrolamientoOptions.dataSource.data($scope.CanalEnrolamiento);
			delete $scope.data.grillaCanalesEnrolamiento;
		};
		
		
		

	});