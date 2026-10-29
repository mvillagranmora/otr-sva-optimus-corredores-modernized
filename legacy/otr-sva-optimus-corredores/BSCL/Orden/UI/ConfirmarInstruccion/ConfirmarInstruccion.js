angular.module('ModuloOrdenes').controller('ConfirmarInstruccionesCtrl',
    function ($rootScope,$scope, $translate, bcsComunes, bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes, bcsServicioOrden,Model,Utils,$compile,$resource,$timeout) {
       
		var self = this;
        
        const parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
        const idVentana = bcsComunes.ventanaWinId($scope);
		const libresPago = ['ENTIRV','RECIRV','ENTIRF','RECIRF','ENTIFI','RECIFI'];
		
        $scope.comportamiento = {};
        $scope.instrucciones = parametros.Instrucciones;
        $scope.data={};
        $scope.esCancelar = false;
        $scope.filtrosIdMotivo = {
			IdEstado 	: { "field"   : "IdEstado","operator": "eq", "value"   : "SCN" },
			Activo 		: { "field"   : "Activo","operator": "eq", "value"   : "S" }
		};
        
        console.log("PARAMETROS DE ENTRADA ConfirmarInstruccion", parametros.Instrucciones);
        
		(function init() {
			const comportamientoPorFlujo = {
				CONFIRMAR: {
					title: 'UI_CONFIRMAR_INSTRUCCION',
					cabeceraEsOperable: 'UI_ES_CONFIRMABLE',
					discriminadorOperabilidad: function (ins) {
						return (ins.IdEstadoInstruccion === 'ING' || ins.IdEstadoInstruccion === 'MOD') && !libresPago.includes(ins.IdTipoOperacionInstruccion);
					},
					ejecutarOperacion: confirmarInstruccionMasivo
				},
				RECHAZAR: {
					title: 'UI_RECHAZAR_INSTRUCCION',
					cabeceraEsOperable: 'UI_ES_RECHAZABLE',
					discriminadorOperabilidad: function (ins) {
						return ins.IdEstadoInstruccion === 'ING' || ins.IdEstadoInstruccion === 'CNF' || ins.IdEstadoInstruccion === 'MOD';
					},
					ejecutarOperacion: rechazarInstruccionMasivo
				},
				LIQUIDAR: {
					title: 'UI_LIQUIDAR_INSTRUCCION',
					cabeceraEsOperable: 'UI_ES_LIQUIDABLE',
					discriminadorOperabilidad: function (ins) {
						return ins.IdEstadoInstruccion === 'CNF' || libresPago.includes(ins.IdTipoOperacionInstruccion);
					},
					ejecutarOperacion: liquidarInstruccion
				},
				CANCELAR: {
					title: 'UI_CANCELAR_INSTRUCCION',
					cabeceraEsOperable: 'UI_ES_CANCELABLE',
					discriminadorOperabilidad: function (ins) {
						let fechaSistema = new Date($scope.SesionFechaSistema);
						let fechaIngresoInstruccion = new Date(ins.FechaIngresoString);

						let dias = Math.round((fechaSistema.getTime() - fechaIngresoInstruccion.getTime())/(1000*60*60*24));
						
						let cumpleRestriccionEstado = ins.IdEstadoInstruccion === 'ING' || ins.IdEstadoInstruccion === 'RZD' || ins.IdEstadoInstruccion === 'NLQ'
										|| ins.IdEstadoInstruccion === 'MOD';
						
						let cumpleRestriccionCanalDias = ins.IdTipoCanalIngreso === 'AGE'
						   				  || (ins.IdTipoCanalIngreso === 'CLI' && dias > 15);

						
						return cumpleRestriccionEstado && cumpleRestriccionCanalDias;
					},
					ejecutarOperacion: cancelarInstruccion
				},
				ACEPTA_SOL_CANCELACION: {
					title: 'UI_ACEPTAR_SOL_CANCELACION',
					cabeceraEsOperable: 'UI_ES_CANCELABLE',
					discriminadorOperabilidad: function (ins) {
						return ins.IdEstadoInstruccion === 'SCN';
					},
					ejecutarOperacion: aceptarCancelacionInstruccion
				},
				RECHAZA_SOL_CANCELACION: {
					title: 'UI_RECHAZAR_SOL_CANCELACION',
					cabeceraEsOperable: 'UI_ES_CANCELABLE',
					discriminadorOperabilidad: function (ins) {
						return ins.IdEstadoInstruccion === 'SCN';
					},
					ejecutarOperacion: rechazaCancelacionInstruccion
				}
			};
			
			if (parametros.flujo === 'CANCELAR'){
				$scope.esCancelar = true;
			}
			
			$scope.comportamiento = comportamientoPorFlujo[parametros.flujo];
			if (!$scope.comportamiento) {
				throw new Error('Flujo ' + parametros.flujo + ' no implementado');
			}

			bcsComunes.ventanaTitulo(idVentana, $translate.instant($scope.comportamiento.title));
			for (const instruccion of $scope.instrucciones) {
				if ($scope.comportamiento.discriminadorOperabilidad(instruccion)) {
					instruccion.EsOperable = 'S';
					instruccion.EsOperableLozalizado = $translate.instant('UI_SI');
				} else {
					instruccion.EsOperable = 'N';
					instruccion.EsOperableLozalizado = $translate.instant('UI_NO');
				}
			}

            const dataSource = new bcs.DataSource({
                type: "array",
                definition: {
                	sort: { field: "IdInstruccion", dir: "asc" },
                    schema: {
                        model: {
                            fields: {
                            	IdInstruccion 							: { type : "number" },
								DocumentoIdentidad 						: { type : "string" },
								IdTipoInstrumentoInstruccion			: { type : "string" },
								IdEmisor 								: { type : "string" },
								NombreEstado 							: { type : "string" },
								EsOperable								: { type : "string" },
								EsOperableLozalizado					: { type : "string" }
                            }
                        }
                    }
                }
            });


            self.configuracionGrilla = {
                gridCreated: function () {
                    self.configuracionGrilla.dataSource.data(parametros.Instrucciones);
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
                height: 300,
                columns: [
                	{field: "IdInstruccion",				title: 'UI_INSTRUCCION',							width: "100px"},
					{field: "DocumentoIdentidad",			title: 'UI_RUT_CLIENTE',							width: "100px"},
					{field: "IdTipoInstrumentoInstruccion", title: 'UI_INSTRUMENTO',							width: "100px"},
					{field: "IdEmisor",						title: 'UI_EMISOR'},
					{field: "NombreEstado",					title: 'UI_ESTADO'},
					{field: "EsOperableLozalizado",		title: $scope.comportamiento.cabeceraEsOperable,	width: '100px'}
                ]
            };
        })();
        	

        $scope.submit = function submit() {
        	$scope.comportamiento.ejecutarOperacion();
		}
        
        function cancelarInstruccion(){
        	bcsComunes.ventanaOverlay(idVentana, true);
        	const requestBody = {
        		p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'ORD',
				p_UsaReglasNegocio: 'S',
				p_IdTipoCanalIngreso: $scope.SesionTipoCanalIngreso,
				p_ComentarioAnulacion: $scope.data.comentario ? $scope.data.comentario:'',
				p_MotivoAnulacion: $scope.data.IdMotivoCancelar ? $scope.data.IdMotivoCancelar:'',
				p_IdsInstrucciones: $scope.instrucciones
					.filter(function (ins) {
						return ins.EsOperable === 'S';
					})
					.map(function (ins) {
						return ins.IdInstruccion;
					})
			};
        	
        	if (requestBody.p_IdsInstrucciones.length > 0){
        		bcsServicioOrden.AnularInstruccionMasivo(requestBody).$promise.then(
        				function onSuccess(response) {
        					
        					const res = util.parseServerResponse(response);
        					const sinErrores = res.erroresContador === 0;
        					const titulo = sinErrores ? 'UI_OPERACION_EXITOSA' : 'UI_OPERACION_ERROR';

        					bcsComunes.ventanaOverlay(idVentana, false);
        					bcsComunes.ventanaMsg({ mensaje: $translate.instant(titulo), conflictos: res }, idVentana);

        					sinErrores && bcsComunes.ventanaCerrar(idVentana);
        				},
        				function onError(response) {
        					const res = util.parseServerResponse(response);

        					bcsComunes.ventanaOverlay(idVentana, false);
        					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, idVentana);
        				}
        			); 
        	}
        	else{
        		bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_NO_HAY_INSTRUCCION_CANCELAR') }, idVentana);
        	}
        	
        }
        
        function liquidarInstruccion(){
        	bcsComunes.ventanaOverlay(idVentana, true);
        	const requestBody = {
        		p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'ORD',
				p_UsaReglasNegocio: 'S',
				p_IdTipoCanalIngreso: 'OPT',
				p_ComentarioLiquidacion: $scope.data.comentario ? $scope.data.comentario:'',
				p_IdsInstrucciones: $scope.instrucciones
					.filter(function (ins) {
						return ins.EsOperable === 'S';
					})
					.map(function (ins) {
						return ins.IdInstruccion;
					})
			};
        	
        	bcsServicioOrden.LiquidarInstruccion(requestBody).$promise.then(
    				function onSuccess(response) {
    					
    					const res = util.parseServerResponse(response);
    					const sinErrores = res.erroresContador === 0;
    					const titulo = sinErrores ? 'UI_OPERACION_EXITOSA' : 'UI_OPERACION_ERROR';

    					bcsComunes.ventanaOverlay(idVentana, false);
    					bcsComunes.ventanaMsg({ mensaje: $translate.instant(titulo), conflictos: res }, idVentana);

    					sinErrores && bcsComunes.ventanaCerrar(idVentana);
    				},
    				function onError(response) {
    					const res = util.parseServerResponse(response);

    					bcsComunes.ventanaOverlay(idVentana, false);
    					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, idVentana);
    				}
    			);
        }
        
		function confirmarInstruccionMasivo() {
			bcsComunes.ventanaOverlay(idVentana, true);
        	const requestBody = {
        		p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'ORD',
				p_UsaReglasNegocio: 'S',
				p_IdTipoCanalIngreso: 'OPT',
				p_ComentarioConfirmacion: $scope.data.comentario ? $scope.data.comentario:'',
				p_IdsInstrucciones: $scope.instrucciones
					.filter(function (ins) {
						return ins.EsOperable === 'S';
					})
					.map(function (ins) {
						return ins.IdInstruccion;
					})
			};

			bcsServicioOrden.ConfirmarInstruccionMasivo(requestBody).$promise.then(
				function onSuccess(response) {
					const res = util.parseServerResponse(response);
					const sinErrores = res.erroresContador === 0;
					const titulo = sinErrores ? 'UI_OPERACION_EXITOSA' : 'UI_OPERACION_ERROR';

					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaMsg({ mensaje: $translate.instant(titulo), conflictos: res }, idVentana);

					sinErrores && bcsComunes.ventanaCerrar(idVentana);
				},
				function onError(response) {
					const res = util.parseServerResponse(response);

					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, idVentana);
				}
			);
		};

		function rechazarInstruccionMasivo() {
			bcsComunes.ventanaOverlay(idVentana, true);
			const requestBody = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'ORD',
				p_UsaReglasNegocio: 'S',
				p_IdTipoCanalIngreso: 'OPT',
				p_ComentarioRechazo: $scope.data.comentario ? $scope.data.comentario:'',
				p_IdsInstrucciones: $scope.instrucciones
					.filter(function (ins) {
						return ins.EsOperable === 'S';
					})
					.map(function (ins) {
						return ins.IdInstruccion;
					})
			};

			bcsServicioOrden.RechazarInstruccionMasivo(requestBody).$promise.then(
				function onSuccess(response) {
					const res = util.parseServerResponse(response);
					const sinErrores = res.erroresContador === 0;
					const codigoMensaje = sinErrores ? 'UI_OPERACION_EXITOSA' : 'UI_OPERACION_ERROR';

					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaMsg({ mensaje: $translate.instant(codigoMensaje), conflictos: res }, idVentana);

					sinErrores && bcsComunes.ventanaCerrar(idVentana);
				},
				function onError(response) {
					const res = util.parseServerResponse(response);

					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, idVentana);
				}
			);
		};
		
		function aceptarCancelacionInstruccion(){
        	bcsComunes.ventanaOverlay(idVentana, true);
        	const requestBody = {
        		p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'ORD',
				p_UsaReglasNegocio: 'S',
				p_IdTipoCanalIngreso: $scope.SesionTipoCanalIngreso,
				p_ComentarioAceptacion: $scope.data.comentario ? $scope.data.comentario:'',
				p_IdsInstrucciones: $scope.instrucciones
					.filter(function (ins) {
						return ins.EsOperable === 'S';
					})
					.map(function (ins) {
						return ins.IdInstruccion;
					})
			};
        	
        	if (requestBody.p_IdsInstrucciones.length > 0){
        		bcsServicioOrden.AceptaCancelacionInstruccionMasivo(requestBody).$promise.then(
        				function onSuccess(response) {
        					
        					const res = util.parseServerResponse(response);
        					const sinErrores = res.erroresContador === 0;
        					const titulo = sinErrores ? 'UI_OPERACION_EXITOSA' : 'UI_OPERACION_ERROR';

        					bcsComunes.ventanaOverlay(idVentana, false);
        					bcsComunes.ventanaMsg({ mensaje: $translate.instant(titulo), conflictos: res }, idVentana);

        					sinErrores && bcsComunes.ventanaCerrar(idVentana);
        				},
        				function onError(response) {
        					const res = util.parseServerResponse(response);

        					bcsComunes.ventanaOverlay(idVentana, false);
        					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, idVentana);
        				}
        			); 
        	}
        	else{
        		bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_NO_HAY_INSTRUCCIONES_POR_ACEPTAR') }, idVentana);
        	}
        	
        };
		
		function rechazaCancelacionInstruccion(){
        	bcsComunes.ventanaOverlay(idVentana, true);
        	const requestBody = {
        		p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'ORD',
				p_UsaReglasNegocio: 'S',
				p_IdTipoCanalIngreso: $scope.SesionTipoCanalIngreso,
				p_ComentarioAceptacion: $scope.data.comentario ? $scope.data.comentario:'',
				p_IdsInstrucciones: $scope.instrucciones
					.filter(function (ins) {
						return ins.EsOperable === 'S';
					})
					.map(function (ins) {
						return ins.IdInstruccion;
					})
			};
        	
        	if (requestBody.p_IdsInstrucciones.length > 0){
        		bcsServicioOrden.RechazaCancelacionInstruccionMasivo(requestBody).$promise.then(
        				function onSuccess(response) {
        					
        					const res = util.parseServerResponse(response);
        					const sinErrores = res.erroresContador === 0;
        					const titulo = sinErrores ? 'UI_OPERACION_EXITOSA' : 'UI_OPERACION_ERROR';

        					bcsComunes.ventanaOverlay(idVentana, false);
        					bcsComunes.ventanaMsg({ mensaje: $translate.instant(titulo), conflictos: res }, idVentana);

        					sinErrores && bcsComunes.ventanaCerrar(idVentana);
        				},
        				function onError(response) {
        					const res = util.parseServerResponse(response);

        					bcsComunes.ventanaOverlay(idVentana, false);
        					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, idVentana);
        				}
        			); 
        	}
        	else{
        		bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_NO_HAY_INSTRUCCIONES_POR_ACEPTAR') }, idVentana);
        	}
        	
        };
		
		$scope.cerrar = function() {
			bcsComunes.ventanaCerrar(idVentana);
		}

    });