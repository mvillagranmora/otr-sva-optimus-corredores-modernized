angular.module('ModuloCustodia').controller('EjercicioOpcionesCtrl',function($scope,bcsComunes,$timeout,$compile, $translate, bcsServiciosCustodia){
	
	$scope.datos = {}
	
	$scope.datos.portafolioSeleccionado = null;
	$scope.datos.fecha = $scope.SesionFechaSistema;	

	$scope.ejercicios = [];

	$scope.envioDisponible = false;
	$scope.envioDisponibleHoraInicio = '';
	$scope.envioDisponibleHoraFin = '';

	$scope.habilitarGuardarBtn = false;

	$scope.parametrosEjercicio = null;

	$scope.datos.disableBtn = true;
	
	$scope.$watch('datos.fecha', function(valorFecha){

		if(valorFecha != null){
			$scope.datos.disableBtn = true;
		}
		else{
			$scope.datos.disableBtn = false;
		}
	});
	
	let getColumns = function(){
		return [
			{field: 'FolioCartera', title: 'UI_FOLIO_CARTERA', width: '70px'},
			{field: 'DocumentoIdentidad', title: 'UI_DOCUMENTO_IDENTIDAD', width: '70px'},
			{field: 'SocioNegocio', title: 'UI_SOCIO_NEGOCIO', width: '70px'},
			{field: 'CodigoPortafolio', title: 'UI_CODIGO_PORTAFOLIO', width: '70px'},
			{field: 'Serie', title: 'UI_NEMOTECNICO', width: '70px'},
			{field: 'Subyacente', title: 'UI_SUBYACENTE', width: '70px'},
			{field: 'TipoOpcion', title: 'UI_TIPO_OPCION', width: '70px'},
			{field: 'Estilo', title: 'UI_ESTILO_OPCION', width: '70px'},
			{field: 'ModalidadLiquidacion', title: 'UI_MODALIDAD', width: '70px'},
			{field: 'ModalidadEjercicio', title: 'UI_MODALIDAD_EJERCICIO', width: '70px', groupable: true},
			{field: 'FechaVencimiento', title: 'UI_FECHA_VENCIMIENTO', width: '70px', template: "#= kendo.toString(FechaVencimiento, 'dd-MM-yyyy') #"},
			{field: 'PrecioEjercicio', title: 'UI_PRECIO_EJERCICIO', width: '70px'},
			{field: 'PrecioSubyacente', title: 'UI_PRECIO_SUBYACENTE', width: '70px'},
			{field: 'Moneyness', title: 'UI_MONEYNESS', width: '70px', groupable: true, template: function(dataItem){
				if(dataItem.Moneyness === 'IN THE MONEY')
					return '<div style="color:green;">'+dataItem.Moneyness+'</div>';
				if(dataItem.Moneyness === 'OUT OF THE MONEY')
					return '<div style="color:red;">'+dataItem.Moneyness+'</div>';
				if(dataItem.Moneyness === 'AT THE MONEY')
					return '<div style="color:red;">'+dataItem.Moneyness+'</div>'
			}},
			{field: 'SaldoFinanciero', title: 'UI_SALDO_FINANCIERO', width: '70px'},
			{field: 'ContratosEjercibles', title: 'UI_CONTRATO_EJERCIBLES', width: '70px'},
			{field: 'ContratosEjercidos', title: 'UI_CONTRATO_EJERCIDOS', width: '70px', editable: true},
			{field: 'Bloqueado', title: 'UI_BLOQUEAR', width: '70px', editable: true, groupable: true},
			{field: 'Editado', title: 'UI_EDITADO', width: '70px', template: function(dataItem){
				return 'No Editado';
			}},
			{field: 'EstadoIntencion', title: 'UI_ESTADO_INTENCION', width: '70px'},
			/*{field: 'FechaInver', title: 'UI_FECHA_INVER', width: '70px'},
			{field: 'ContratoEjercidos', title: 'UI_CONTRATO_EJERCIDOS', width: '70px'},
			{field: 'ContratoSorteados', title: 'UI_CONTRATO_SORTEADOS', width: '70px'},*/
		];
	}
	
	let getSechma = function(){
		return {
			model : {
				fields : {
					DocumentoIdentidad: { type: 'string'},
					SocioNegocio: { type: 'string'},
					CodigoPortafolio: { type: 'string'},
					Serie: { type: 'string'},
					Subyacente: {type: 'string'},
					TipoOpcion: { type: 'string'},					
					Estilo: { type: 'string'},
					ModalidadLiquidacion: { type: 'string'},
					ModalidadEjercicio: { type: 'string'},
					FechaVencimiento: {type: 'date'},
					PrecioEjercicio: {type: 'number'},
					PrecioSubyacente: {type: 'number'},
					Moneyness: {type: 'string'},
					SaldoFinanciero: {type: 'string'},
					ContratosEjercibles: { type: 'number'},
					ContratosEjercidos: { type: 'number'},
					Bloqueado: {type: 'string'},
					Editado: {type: 'string'},
					EstadoIntencion: {type: 'string'},
					IdCliente: { type: 'number'},
					IdPortafolio: { type: 'number'},
					FolioCartera: { type: 'number'}
					/*FechaInver: {type: 'string'},
					ContratoEjercidos: { type: 'number'},
					ContratoSorteados: { type: 'number'},*/
				}
			}
		}
	}

	let datasourceEjercicioAutomatico = new bcs.DataSource({
		type : "call",
		definition : {
			callResultSet	: "P_OUT_EJERCICIOS",
			callTransaction	: "700149",
			serverFiltering : true,			
			schema : getSechma(),
			group: [
				{ field: 'ModalidadEjercicio' },
				{ field: 'Moneyness' }
			]
		},
		
	});

	$scope.consultarParametrosEjercicio = function(){
		let ds = new bcs.DataSource({
			type: 'odata',
			definition: {
				serverFiltering: true,			
				transport: {
					read: {
						url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=700154',
						dataType: 'json'
					}
				},
				schema : {
					model : {
						fields : {
							IdEmpresa 		: { type : "number"},
							
						}
					}
				}
			}		
		});

		ds.fetch().then(function(){	
			//console.log( ds.data());		
			$scope.parametrosEjercicio = ds.data();
			//console.log($scope.parametrosEjercicio);							
		},function() {
			console.error("ERROR AL CARGAR PARAMETROS DE EJERCICIO");
		})
	};

	$scope.buscar = function(){			
		parametrosSP = {};		
		parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;		
		parametrosSP.p_IdModulo = 'CUS';
		
		parametrosSP.p_FechaConsulta =  moment(util.date($scope.datos.fecha)).format('YYYY-MM-DD');
		
		parametrosSP.p_IdClasificacionTipoInstrumento = "";
		parametrosSP.p_IdTipoInstrumento = "";
		parametrosSP.p_IdInstrumento = 0;		
	
		parametrosSP.p_IdCliente = $scope.datos.portafolioSeleccionado.IdCliente;
		parametrosSP.p_IdPortafolio =  $scope.datos.portafolioSeleccionado.IdPortafolio;
		parametrosSP.p_TipoDerivado = 'OP';	
		datasourceEjercicioAutomatico.setCallParameters(parametrosSP);		
		
	};

	$scope.guardar = function(){
		let ejercicios = $scope.confEjercicioAutomatico.dataSource.data();		
		let ejerciciosEditados = [];
		for(ejercicio of ejercicios){		
			if(ejercicio.Editado == 'Editado'){				
				ejerciciosEditados.push({
					IdCartera: ejercicio.IdCartera,
					Bloqueado: ejercicio.Bloqueado,
					ContratosEjercidos: ejercicio.ContratosEjercidos,
					ContratosSorteados: 0,
					IdEstado: 'GUA',
					IdEstadoCCLV: '',
					DescripcionEstadoCCLV: '',
					IdCliente: $scope.datos.portafolioSeleccionado.IdCliente,
					IdPortafolio: $scope.datos.portafolioSeleccionado.IdPortafolio,
					IdEmpresa: $scope.$root.SesionIdEmpresa,
				});
			}
		}
		let params = {};
		params.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		params.p_IdUsuario = $scope.$root.SesionIdUsuario;
		params.p_IdPais = 44;
		params.p_Ejercicios = ejerciciosEditados;			
		
		bcsServiciosCustodia.IngresarEjercicio(params, function(){
			$scope.confEjercicioAutomatico.dataSource.read();
			$scope.habilitarGuardarBtn = false;
		}, function(){
			/* error */
		});
	};

	$scope.consultarHorarioDisponible = function(){
		let params = {};
		params.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		params.p_IdUsuario = $scope.$root.SesionIdUsuario;
		params.p_Opcion = 'C';
		params.p_IdModulo = 'CUS';
		params.p_Terminal = '';
		params.p_ConsultarEjercicioBloqueo = [];	
		
		bcsServiciosCustodia.ConsultarEjercicioBloqueo(params, function(response){
			if(response.P_OUT_HORARIO.Estado === 'NO'){
				$scope.envioDisponible = false;
				/*$scope.envioDisponibleHoraInicio = moment(response.P_OUT_HORARIO.HoraInicio).format('HH:mm');
				$scope.envioDisponibleHoraFin = moment(response.P_OUT_HORARIO.HoraFin).format('HH:mm');*/
			}else {
				$scope.envioDisponible = true;
				/*$scope.envioDisponibleHoraInicio = moment(response.P_OUT_HORARIO.HoraInicio).format('HH:mm');
				$scope.envioDisponibleHoraFin = moment(response.P_OUT_HORARIO.HoraFin).format('HH:mm');*/
			}			
		}, function(response){
			bcsComunes.ventanaMsg({				
				mensaje:$translate.instant('UI_ERROR'),
				conflictos:util.parseServerResponse(response)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
	}

	$scope.enviar = function(){
		let params = {};
		let ejercicios = $scope.confEjercicioAutomatico.dataSource.data();
		
		params.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		params.p_IdUsuario = $scope.$root.SesionIdUsuario;
		params.p_Opcion = 'A';
		params.p_IdModulo = 'CUS';
		params.p_Terminal = '';
		params.p_ConsultarEjercicioBloqueo = [];
		for(ejercicio of ejercicios){
			if(ejercicio.IdEstado === 'GUA'){	
				if(ejercicio.Bloqueado === 'SI' && ejercicio.ModalidadEjercicio === 'AUTOMATICA'){
					params.p_ConsultarEjercicioBloqueo.push({
						p_IdCartera: ejercicio.IdCartera,
						p_FolioCartera: ejercicio.FolioCartera,
						p_ContratosSorteados: 0,			
						p_IdCliente: ejercicio.IdCliente,//$scope.datos.portafolioSeleccionado.IdCliente,
						p_IdPortafolio: ejercicio.IdPortafolio,//$scope.datos.portafolioSeleccionado.IdPortafolio,			
						p_Bloqueo: 'S',
						p_CantEjercicio: 0,
						p_Comitente: ejercicio.DocumentoIdentidad,//$scope.datos.portafolioSeleccionado.DocumentoIdentidad,
						p_FondoAgente: '',
						p_FondoComitente:  '',
						p_Serie: ejercicio.Serie
					});
				}
				else if(ejercicio.Bloqueado === 'NO'){
					params.p_ConsultarEjercicioBloqueo.push({
						p_IdCartera: ejercicio.IdCartera,
						p_FolioCartera: ejercicio.FolioCartera,
						p_ContratosSorteados: 0,			
						p_IdCliente: ejercicio.IdCliente,//$scope.datos.portafolioSeleccionado.IdCliente,
						p_IdPortafolio: ejercicio.IdPortafolio,//$scope.datos.portafolioSeleccionado.IdPortafolio,						
						p_Bloqueo: 'N',
						p_CantEjercicio: ejercicio.ContratosEjercidos,
						p_Comitente: ejercicio.DocumentoIdentidad,//$scope.datos.portafolioSeleccionado.DocumentoIdentidad,
						p_FondoAgente: '',
						p_FondoComitente:  '',
						p_Serie: ejercicio.Serie
					});
				}

			}
		}
		
		bcsServiciosCustodia.ConsultarEjercicioBloqueo(params, function(response){
			console.log(response);
			bcsComunes.ventanaMsg({	mensaje:$translate.instant('UI_ERROR'),
				conflictos:util.parseServerResponse(response)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);			
		}, function(response){
			bcsComunes.ventanaMsg({				
				mensaje:$translate.instant('UI_ERROR'),
				conflictos:util.parseServerResponse(response)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
	};

	 /* ConsultarEjercicioBloqueo */
	$scope.confEjercicioAutomatico = {
		dataSource: datasourceEjercicioAutomatico,
		columns: getColumns(),
		userSettings: true,
		allowCopy: { delimeter: "\t" },
		pageable: true,
		sortable: true,
		groupable: true,
		filterable: true,
		height: 567,
		excel: {allPages: true,},
		selectable: "row",
		save: function(e) {
			if(e.values.ContratosEjercidos){				
				if(e.values.ContratosEjercidos > e.model.ContratosEjercibles){						
					e.values.ContratosEjercidos = e.model.ContratosEjercibles;
					e.preventDefault();
				}
				e.model.ContratosEjercidos = e.values.ContratosEjercidos;
				e.model.SaldoFinanciero = (e.model.PrecioEjercicio - e.model.PrecioSubyacente)*e.model.ContratosEjercidos;
			}
			if(e.values.Bloqueado){
				if(e.values.Bloqueado === 'SI' || e.values.Bloqueado === 'NO'){
					e.model.Bloqueado = e.values.Bloqueado;
				}
				else{
					e.preventDefault();
				}

			
			}	
			e.model.Editado = 'Editado';
			$scope.habilitarGuardarBtn = true;
		},
		notEditableColumns:	function(row) {
			row.EstadoIntencion = $scope.EstadoIntencion(row);
			
			if(row.EstadoIntencion === 'MontoUtilidadBloqueoEjercicio'){
				row.Bloqueado = 'SI';
				row.EstadoIntencion = 'Bloquear Ejercicio si la Utilidad es Menor a';
			}
			if(row.EstadoIntencion === 'MontoUtilidadPermitirEnvio'){
				row.Bloqueado = 'NO';
				row.EstadoIntencion = 'Permitir Env�o Solo si la Utilidad es Mayor o Igual a';
			}

			if(row.ModalidadEjercicio === 'AUTOMATICA' && row.Moneyness != 'IN THE MONEY'){
				row.Bloqueado = 'SI';
				return ['ContratosEjercidos', 'Bloqueado'];			
			}
			if(row.ModalidadEjercicio === 'AUTOMATICA' && row.Moneyness === 'IN THE MONEY'){
				row.ContratosEjercidos = row.ContratosEjercibles;
				return ['ContratosEjercidos'];			
			}
			if(row.ModalidadEjercicio === 'MANUAL'){
				// row.ContratosEjercidos = row.ContratosEjercibles;
				return ['Bloqueado'];			
			}
						
		},		
	};

	$scope.EstadoIntencion = function(ejercicio){
		//console.log(ejercicio);
		if(ejercicio.ModalidadEjercicio === 'AUTOMATICA' && ejercicio.ModalidadLiquidacion === 'FINANCIERA'){
			let parametro = $scope.parametrosEjercicio.filter(parametro => 	parametro.ParametroEjercicio === 'MontoUtilidadBloqueoEjercicio');
			if(parametro.ParametroEjercicioValor < ejercicio.SaldoFinanciero){				
				return 'MontoUtilidadBloqueoEjercicio';
			} 
		}
		if(ejercicio.ModalidadEjercicio === 'MANUAL' && ejercicio.ModalidadLiquidacion === 'FINANCIERA'){
			let parametro = $scope.parametrosEjercicio.filter(parametro => 	parametro.ParametroEjercicio === 'MontoUtilidadPermitirEnvio');
			if(parametro.ParametroEjercicioValor < ejercicio.SaldoFinanciero){				
				return '"MontoUtilidadPermitirEnvio"';
			} 
		}
		if($scope.envioDisponible)
			return 'Envio disponible';
		return 'Fuera de horario';
	}

	$scope.consultarHorarioDisponible();
	$scope.consultarParametrosEjercicio();
	
});