angular.module('ModuloFacturacion').controller('GenerarInformeComprobanteComisionesMensualesCtrl',
		function($scope,bcsComunes,$translate,$resource){
	
	const iccm 				= this;
	iccm.data				= {};								
	iccm.combobox			= {};
	iccm.data.FechaDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
	iccm.data.FechaHasta 	= util.dateInicioDia($scope.SesionFechaSistema);
	iccm.data.FechaSistema 	= util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");

	iccm.data.FechaConsultaOpt = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana, false, 3000);
	
	/******************************************************/
	/*** BOTON GENERAR									***/
	/******************************************************/
				
	iccm.generar = function() {
		
		let programa = 'GENERAR_INFORME_MATRIZ_COMISIONES';
		if(iccm.data.InformeSeleccionado.Codigo === "CCDET"){
			programa = 'GENERAR_INFORME_COMISION_CLIENTE_DET';
		}
		if (iccm.data.InformeSeleccionado.Codigo === "CCRES"){
			programa = 'GENERAR_INFORME_COMISION_CLIENTE_RES';
		}

		auxDatePerCont =  util.dateOdata(util.date(iccm.data.FechaDesde).hours(0).minutes(0).seconds(0).toDate());
		iccm.data.FechaDesde = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth(), 1);
	    iccm.data.FechaHasta = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth() + 1, 0);
	
		const datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'FAC',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : programa,
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'COR'
				},{
					IdInterno : 0,
					NombreParametro : 'Archivo',
					ValorBlob : 'PruebaGenerarInformeCobroComisionesMensuales'
				},{
					IdInterno : 0,
					NombreParametro : 'TipoInforme',
					ValorBlob :	iccm.data.InformeSeleccionado.Codigo
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdCliente',
					ValorBlob :	iccm.data.portafolioSeleccionado.IdCliente || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'RutCliente',
					ValorBlob :	iccm.data.portafolioSeleccionado.DocumentoIdentidad || ''
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdPortafolio',
					ValorBlob :	iccm.data.portafolioSeleccionado.IdPortafolio || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'CodigoPortafolio',
					ValorBlob :	iccm.data.portafolioSeleccionado.CodigoPortafolio || ''
				},{
					IdInterno : 0,
					NombreParametro : 'IdSegmentoCliente',
					ValorBlob :	iccm.data.SegmentoClienteSeleccionado.IdSegmentoCliente
				},{
					IdInterno : 0,
					NombreParametro : 'DescripcionSegmento',
					ValorBlob :	iccm.data.SegmentoClienteSeleccionado.DescripcionSegmento
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdClasificacionTipoInstrumento',
					ValorBlob :	iccm.data.ClasificacionTipoInstrumentoSeleccionado.IdClasificacionTipoInstrumento || ''
				},
				{
					IdInterno : 0,
					NombreParametro : 'NombreTipoInstrumento',
					ValorBlob :	iccm.data.ClasificacionTipoInstrumentoSeleccionado.Nombre
				},
				{
					IdInterno : 0,
					NombreParametro : 'FechaSistema',
					ValorBlob :	iccm.data.FechaSistema || null
				},
				{
					IdInterno : 0,
					NombreParametro : 'FechaDesde',
					ValorBlob :	iccm.data.FechaDesde || null
				},
				{
					IdInterno : 0,
					NombreParametro : 'FechaHasta',
					ValorBlob :	iccm.data.FechaHasta || null
				}]
		}
    		
		const r = $resource("",{} , {
				'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"}
			});
			
		r.Consultar(datos).$promise.then(
		function(result) {
			if(result.P_OUT_ERRORESCONTADOR>0){
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}else{
				const fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
			}
		},
		function(result){
			alert(result);
		});
		bcsComunes.msg("info", "info", 'Archivo generado correctamente');
		
	};
	
	iccm.limpiar = function() {
		iccm.data.IdPortafolio 						= null;
		iccm.data.portafolioSeleccionado 			= null;
		iccm.data.CodigoInforme 					= null;
		iccm.data.InformeSeleccionado 				= null;
		iccm.data.IdSegmentoCliente 				= null;
		iccm.data.IdClasificacionTipoInstrumento 	= null;
		iccm.data.FechaDesde 						= util.dateInicioDia($scope.SesionFechaSistema);
		iccm.data.FechaHasta 						= util.dateInicioDia($scope.SesionFechaSistema);
	
	}

	$scope.$watch('iccm.data.portafolioSeleccionado', function(newval, oldval) {
				
		if(!newval){
			
			iccm.data.IdSegmentoCliente = (iccm.data.portafolioSeleccionado ? (iccm.data.portafolioSeleccionado.IdCliente == undefined ? "" : iccm.data.IdSegmentoCliente) : "");
			
			return;
		} 
		
		if(newval && iccm.data.portafolioSeleccionado.IdCliente != undefined){
			iccm.ConsultarSegmentoCliente(iccm.data.portafolioSeleccionado.IdCliente);
		}
	});
	
	iccm.ConsultarSegmentoCliente = function(IdCliente) {
		 var ConsultarSegmento = new bcs.DataSource(ConsultarSegmentoCliente);
		var filterSegmento = [
			{ field: "IdCliente", operator: "eq", value: IdCliente }
		];
		
		ConsultarSegmento.query({ filter: filterSegmento }).then(function() {
			if (ConsultarSegmento.data().length > 0 && ConsultarSegmento.data()[0] != undefined) {
				iccm.data.IdSegmentoCliente = ConsultarSegmento.data()[0].IdSegmentoCliente;
			}
		});
	};
	
	var ConsultarSegmentoCliente = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700353",
					dataType: "json"
				}
			},
			schema 	: {
				model	: {
					fields	: {
						IdSegmentoCliente : { type: "number"}
					}
				}
			}
		}
	});
	
});