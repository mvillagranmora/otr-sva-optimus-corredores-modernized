angular.module('ModuloFacturacion').controller('FacturacionCobroMensualCtrl',
		function($scope, $translate, $modal, $rootScope, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA,
				bcsServiciosFacturacion, bcsFacturacionCobroMensualGrid, bcsServicioPlataforma)
{
	$scope.data	= {};
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	//Seteo de variables de sistema
	$scope.data.kngFechaProceso =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.IdEmpresa		= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario		= $scope.SesionIdUsuario;
	$scope.data.IdModulo		= 'FAC';
	$scope.data.btnActivos  	= true;   //Activa botones
	$scope.data.oIVA			= 0;
	
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	var comprobantes = [];
	$.each(parametros,function(key, value) {
		var comp = {
			IdComprobante	: value.IdComprobante,
			ModoFacturacion	: value.CodigoModoFacturacionComprobante
		}
		comprobantes.push(comp);
	})
	
	var filters = {};	
	filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;;		
	filters.p_IdUsuario         	= $scope.SesionIdUsuario;
	filters.p_IdModulo				= 'FAC';
	filters.p_simularOperacion		= 'N';
	filters.p_UsaReglasNegocio		= 'S';
	filters.p_ComprobanteCobro		= comprobantes;

	$scope.dataSourceFacturacionCobroMensual = new bcs.DataSource({
		type 		: "call", //Los tipos posibles son: odata, call, array y object
		definition 	: {
						callTransaction 	: "700301",
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            callParameters  	: filters,
			            callResultSet 		: "P_OUT_FACTURACION_COBRO",
			            sort 				: { field: "NombreCliente", dir: "asc" }, // Criterios de Ordenamiento.
			            pageSize 			: 50, // Número de registros por página.
			            schema 				:  bcsFacturacionCobroMensualGrid.modeloFacturacionCobroMensual(),			
	                    aggregate			: [ //Para resumen de totales de cobro
	                 	  		                { field: "MontoDerecho", aggregate: "sum" },
	                 	  		                { field: "MontoComision", aggregate: "sum" },
	                 	  		                { field: "Monto", aggregate: "sum" },
                                              ]
		              }
	});

	//GRILLA 
	$scope.confBasicaClienteFacturacionCobroMensual = { 
		dataSource			: $scope.dataSourceFacturacionCobroMensual,
		columns				: bcsFacturacionCobroMensualGrid.columnasFacturacionCobroMensual(),
		autoBind			: true,
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		navigatable         : true,
		allowCopy 			: true,
		height				: 350,
		excel 				: { allPages : true },		
	};

	// obtenemos valor del IVA
	var datosLlamadaA = {
		p_IdEmpresa 	: $scope.SesionIdEmpresa,
		p_IdUsuario 	: $scope.SesionIdUsuario,
		p_IdModulo 		: 'CUS',
		p_IdCobro		: 'IVA'
	};

	bcsServiciosFacturacion.ConsultarIVA(datosLlamadaA,function(result) {
		if (result.P_OUT_VALORCOBRO > 0){
			$scope.data.oIVA = result.P_OUT_VALORCOBRO;
		} 
	});


	
	$scope.data.enviarFacturar = function () {

		// validacion de registros a facturar
		var facturar = true;
    	var data = $scope.gridApiClienteFacturacionCobroMensual.dataSource.data();
		$.each(data,function(key, value) {
			if (value.Monto===0) {
				bcsComunes.msg("warning","warning",$translate.instant('UI_FACTURACION_COMISION_CERO'));
				facturar = false;
				return;
			}
		});
    	
		if (facturar) {
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ESTA_SEGURO_ENVIAR_FACTURAR_MOVIMIENTOS_SELECCIONADOS'),idVentana);
			t.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					
					var datos = {
						p_IdEmpresa 		: $scope.SesionIdEmpresa,
						p_IdUsuario 		: $scope.SesionIdUsuario,
						p_IdModulo			: 'FAC',
						p_EsCargaMasiva 	: 'N',
						p_simularOperacion  : 'N',
						p_UsaReglasNegocio	: 'S',
						p_EsIngresoAutomatico : 'S',
						p_CrearComprobanteAutomatico : 'N',
						p_PorcentajeIva		: $scope.data.oIVA,
						p_CobroComision	    : []
					};
					
					$.each(data,function(key,cobros){
						
						datos.p_CobroComision.push({
													IdCliente : cobros.IdCliente,
													NombreCliente : cobros.NombreCliente,
													DocumentoIdentidad : cobros.DocumentoIdentidad,
													IdPortafolio : cobros.IdPortafolio,
													IdMoneda : cobros.IdMoneda,
													ModPrefFact : cobros.ModPrefFact,
													ModoFacturacion : cobros.ModoFacturacion,
													IdContratoComision : cobros.IdContratoComision,
													IdClasificacionTipoInstrumento : cobros.IdClasificacionTipoInstrumento,
													NombreConvenioContrato : cobros.NombreConvenioContrato,
													Monto : cobros.Monto,
													MontoDerecho : cobros.MontoDerecho,
													MontoComision : cobros.MontoComision,
													IdConcepto : cobros.IdConceptoFacturacion,
													NombreConcepto : cobros.NombreConcepto,
													IdInstrumento : cobros.IdInstrumento,
													IdComprobanteRel: cobros.IdComprobanteRel,
						                            });
					});

					bcsServiciosFacturacion.EnviarCobrosMensualesFacturar(datos,function(result){
						bcsComunes.ventanaOverlay($scope.idVentana,false);

						var id = bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos: util.parseServerResponse(result)
						},$scope.idVentana)
						bcsComunes.ventanaCerrar(idVentana);
						
					},function(result){
						bcsComunes.ventanaOverlay($scope.idVentana,false);

						var id = bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos: util.parseServerResponse(result)
						},$scope.idVentana)
						bcsComunes.ventanaCerrar(idVentana);
					});
				}
			});
		}

    };
    
    $scope.data.cancelar = function () {
    	bcsComunes.ventanaCerrar(idVentana);
    }

    $scope.data.seleccionarTodosMovimientos = function() {	
		$scope.gridApiClienteFacturacionCobroMensual.select($scope.gridApiClienteFacturacionCobroMensual.tbody.find(">tr"));		
	};
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		$scope.gridApiClienteFacturacionCobroMensual.clearSelection();
	};
	
});