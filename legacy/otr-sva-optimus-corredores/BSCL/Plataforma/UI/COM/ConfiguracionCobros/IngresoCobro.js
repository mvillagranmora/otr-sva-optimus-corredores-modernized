angular.module('ModuloOrdenes').controller('IngresoCobroCtrl',function($scope,bcsComunes,$translate,$resource) {

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.refresh = function(){
		if ($("#grillaRangos").find("[kendo-grid]").data("kendo-grid"))
			$("#grillaRangos").find("[kendo-grid]").data("kendo-grid").refresh();
	}
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	var params = bcsComunes.ventanaParams(idVentana);
	
	$scope.formData = {};
	
	$scope.definicion = {};
	$scope.defecto = {};
	
	$scope.definicion.PorcentualFijo = {};
	$scope.definicion.PorcentualRangos = {};
	$scope.definicion.PorcentualTasaPlazo = {};
	$scope.definicion.OtrosDatos = {};
	
	$scope.pestanaRangos = {};
	$scope.pestanaInEx = {};
	
	$scope.pestanaInEx.filtrosEmisor = {
			logic : "or",
			EmiteIRF : { field : "EmiteIRF", operator : "eq", value : 'S' },
			EmiteIIF : { field : "EmiteIIF", operator : "eq", value : 'S' },
	}
	
	$scope.pestanaInEx.filtrosTipoCliente = {
			IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa }
	}
	
	
	$scope.pestanaInEx.etiquetasEmisor = {
		incluye : $translate.instant("UI_INCLUYE"),
		excluye : $translate.instant("UI_EXCLUYE")
	}
	
	$scope.pestanaInEx.valoresEmisor = {
		incluye : true,
		excluye : false
	}
	
	
	$scope.pestanaInEx.etiquetasInstrumento = {
		incluye : $translate.instant("UI_INCLUYE"),
		excluye : $translate.instant("UI_EXCLUYE")
	}
	
	$scope.pestanaInEx.valoresInstrumento = {
		incluye : true,
		excluye : false
	}
	
	
	$scope.pestanaInEx.etiquetasTipoDeCliente = {
		incluye : $translate.instant("UI_INCLUYE"),
		excluye : $translate.instant("UI_EXCLUYE")
	}
	
	$scope.pestanaInEx.valoresTipoDeCliente = {
		incluye : true,
		excluye : false
	}
	
	
	$scope.pestanaInEx.etiquetasCliente = {
		incluye : $translate.instant("UI_INCLUYE"),
		excluye : $translate.instant("UI_EXCLUYE")
	}
	
	$scope.pestanaInEx.valoresCliente = {
		incluye : true,
		excluye : false
	}
	
	
	$scope.pestanaInEx.etiquetasCorresponsal = {
		incluye : $translate.instant("UI_INCLUYE"),
		excluye : $translate.instant("UI_EXCLUYE")
	}
	
	$scope.pestanaInEx.valoresCorresponsal = {
		incluye : true,
		excluye : false
	}
	
	var resources = $resource("",{},{
				CrearDefinicionCobro		: { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20011"},
				ActualizarDefinicionCobro	: { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20014"}
			}
			
	);
	

	if (params.DefinicionCobro) {
		$scope.editando = true;
		
		//editando
		$scope.definicion.Codigo 		= params.DefinicionCobro.CodigoDefinicionCobro;
		$scope.definicion.Nombre	 	= params.DefinicionCobro.Descripcion;
		$scope.defecto.IdClaseCobro 	= params.DefinicionCobro.IdClaseCobro;
		$scope.defecto.IdTipoCobro 		= params.DefinicionCobro.IdTipoCobro;
		$scope.defecto.IdEstadoCobro 	= params.DefinicionCobro.IdEstado;
		$scope.defecto.IdTipoProducto 	= params.DefinicionCobro.IdTipoProducto === null?0:params.DefinicionCobro.IdTipoProducto;

		//valores tipo cobro monto fijo
		$scope.definicion.EsCobroMonedaOperacion 	= params.DefinicionCobro.CheckCobroMonedaOperacion === 'S'?true:false;
		$scope.defecto.IdMonedaCobro 				= params.DefinicionCobro.IdMonedaCobroOtraMoneda;
		$scope.definicion.MontoFijo 				= params.DefinicionCobro.MontoFijo_MontoCobro;
		$scope.definicion.MontoFijoMenorActivado 	= params.DefinicionCobro.MontoFijo_CheckMontoOperacionMenor==='S'?true:false;
		$scope.definicion.MontoFijoOperacionMenor 	= params.DefinicionCobro.MontoFijo_ValorMontoOperacionMenor;
		$scope.definicion.MontoFijoSiNoActivado 	= params.DefinicionCobro.MontoFijo_CheckSiNo==='S'?true:false;
		$scope.definicion.MontoFijoSiNo 			= params.DefinicionCobro.MontoFijo_ValorMontoSiNo;
		
		//valores tipo monto fijo por unidad
		
		$scope.definicion.MontoFijoPorUnidadValor		= params.DefinicionCobro.MontoFijoPorUnidad_MontoCobro || 0;
		$scope.definicion.AllInCheckValorMontoPorUnidad	= params.DefinicionCobro.MontoFijoPorUnidad_AllInCheckValor ==='S'?true:false;
		
		//valores tipo cobro porcentaje fijo
		$scope.definicion.PorcentualFijo.PorcentualFijoInputValor 	= params.DefinicionCobro.PorcentualFijo_ValorCobro;
		$scope.definicion.PorcentualFijo.SobreMontoCheckValor		= params.DefinicionCobro.PorcentualFijo_CheckSobreMonto==='S'?true:false;
		$scope.definicion.PorcentualFijo.SobreCantidadCheckValor	= params.DefinicionCobro.PorcentualFijo_CheckSobreCantidad==='S'?true:false;
		$scope.definicion.PorcentualFijo.MayorEntreDosCheckValor	= params.DefinicionCobro.PorcentualFijo_CheckMayorEntreDos==='S'?true:false;
		$scope.definicion.PorcentualFijo.MenorEntreDosCheckValor	= params.DefinicionCobro.PorcentualFijo_CheckMenorEntreDos==='S'?true:false;
		$scope.definicion.PorcentualFijo.MontoMinimoCheckValor		= params.DefinicionCobro.PorcentualFijo_CheckMontoMinimo==='S'?true:false;
		$scope.definicion.PorcentualFijo.MontoMaximoCheckValor		= params.DefinicionCobro.PorcentualFijo_CheckMontoMaximo==='S'?true:false;
		$scope.definicion.PorcentualFijo.MontoMinimoInputValor		= params.DefinicionCobro.PorcentualFijo_ValorMontoMinimo;
		$scope.definicion.PorcentualFijo.MontoMaximoInputValor		= params.DefinicionCobro.PorcentualFijo_ValorMontoMaximo;
		$scope.defecto.IdMonedaCobroMinMax							= params.DefinicionCobro.PorcentualFijo_IdMonedaMinimoMaximo;
		
		//valores tipo cobro porcentaje tasa/plazo
		$scope.definicion.PorcentualTasaPlazo.PorcentualFijoInputValor = params.DefinicionCobro.PorcentualTasaPlazo_ValorCobro;
		
		//valores otros
		$scope.definicion.OtrosDatos.OperacionContadoCheckValor		= params.DefinicionCobro.Otros_CheckOperacionContado==='S'?true:false;
		$scope.definicion.OtrosDatos.OperacionPlazoCheckValor		= params.DefinicionCobro.Otros_CheckOperacionPlazo==='S'?true:false;
		$scope.definicion.OtrosDatos.BaseTasaCheckValor				= params.DefinicionCobro.Otros_CheckBaseTasa==='S'?true:false;
		$scope.defecto.IdBaseTasaCobro								= params.DefinicionCobro.Otros_ValorBaseTasa;
		$scope.definicion.OtrosDatos.InteresSimpleCheckValor		= params.DefinicionCobro.Otros_CheckInteresSimple==='S'?true:false;
		$scope.definicion.OtrosDatos.InteresCompuestoCheckValor		= params.DefinicionCobro.Otros_CheckInteresCompuesto==='S'?true:false;
		$scope.definicion.OtrosDatos.AllInCheckValor				= params.DefinicionCobro.Otros_CheckAllIn==='S'?true:false;
		$scope.definicion.OtrosDatos.AllInInputValor				= params.DefinicionCobro.Otros_ValorAllInMontoOperacionBase;

		$scope.definicion.OtrosDatos.AfectoImpuestoCheckValor			= params.DefinicionCobro.Otros_CheckAfectoImpuesto==='S'?true:false;
		$scope.definicion.OtrosDatos.PermiteDisminuirValorCheckValor	= params.DefinicionCobro.Otros_CheckPermiteDisminuirValor==='S'?true:false;
		$scope.definicion.OtrosDatos.AplicaPortafolioCheckValor			= params.DefinicionCobro.Otros_CheckAplicaPortafolio==='S'?true:false;
		$scope.definicion.OtrosDatos.PermiteAumentarValorCheckValor		= params.DefinicionCobro.Otros_CheckPermiteAumentarValor==='S'?true:false;
		$scope.definicion.OtrosDatos.AplicaCorredoraCorresponsalValor	= params.DefinicionCobro.Otros_CheckAplicaCorresponsal==='S'?true:false;
		

		$scope.definicion.PorcentualTasaPlazo.PorcentualTasaPlazoInputValor = params.DefinicionCobro.PorcentualTasaPlazo_ValorCobro;
		
		$scope.defecto.IdTipoRangoCobro		= params.DefinicionCobro.PorcentualRango_IdTipoRangoCobro;
		$scope.defecto.IdMonedaRangoMonto	= params.DefinicionCobro.PorcentualRango_IdMonedaRangoMonto;
		
		$scope.pestanaInEx.emisorCheck 			= params.DefinicionCobro.Otros_IncluyeExcluyeEmisoresCheck==='S'?true:false;
		$scope.pestanaInEx.instrumentoCheck 	= params.DefinicionCobro.Otros_IncluyeExcluyeInstrumentosCheck==='S'?true:false;
		$scope.pestanaInEx.tipoDeClienteCheck 	= params.DefinicionCobro.Otros_IncluyeExcluyeTiposDeClienteCheck==='S'?true:false;
		$scope.pestanaInEx.clienteCheck 		= params.DefinicionCobro.Otros_IncluyeExcluyeClientesCheck==='S'?true:false;
		$scope.pestanaInEx.corresponsalCheck 	= params.DefinicionCobro.Otros_IncluyeExcluyeCorresponsalesCheck==='S'?true:false;
		
		$scope.pestanaInEx.valoresEmisor.incluye = params.DefinicionCobro.Otros_IncluyeExcluyeEmisoresValor === 'I'?true:false;
		$scope.pestanaInEx.valoresEmisor.excluye = params.DefinicionCobro.Otros_IncluyeExcluyeEmisoresValor === 'E'?true:false;
		
		$scope.pestanaInEx.valoresInstrumento.incluye = params.DefinicionCobro.Otros_IncluyeExcluyeInstrumentosValor === 'I'?true:false;
		$scope.pestanaInEx.valoresInstrumento.excluye = params.DefinicionCobro.Otros_IncluyeExcluyeInstrumentosValor === 'E'?true:false;
		
		$scope.pestanaInEx.valoresTipoDeCliente.incluye = params.DefinicionCobro.Otros_IncluyeExcluyeTiposDeClienteValor === 'I'?true:false;
		$scope.pestanaInEx.valoresTipoDeCliente.excluye = params.DefinicionCobro.Otros_IncluyeExcluyeTiposDeClienteValor === 'E'?true:false;
		
		$scope.pestanaInEx.valoresCliente.incluye = params.DefinicionCobro.Otros_IncluyeExcluyeClientesValor === 'I'?true:false;
		$scope.pestanaInEx.valoresCliente.excluye = params.DefinicionCobro.Otros_IncluyeExcluyeClientesValor === 'E'?true:false;
		
		$scope.pestanaInEx.valoresCorresponsal.incluye = params.DefinicionCobro.Otros_IncluyeExcluyeCorresponsalesValor === 'I'?true:false;
		$scope.pestanaInEx.valoresCorresponsal.excluye = params.DefinicionCobro.Otros_IncluyeExcluyeCorresponsalesValor === 'E'?true:false;
		
		
	}else {
		//nuevo
		$scope.defecto.IdMonedaCobro = undefined;
		$scope.defecto.IdClaseCobro = 'COMISION';
		$scope.defecto.IdTipoCobro = 'MONTOFIJO';
		$scope.defecto.IdEstadoCobro = 'ACT';
		$scope.definicion.MontoFijo = undefined;
		$scope.defecto.IdTipoProducto 	= 0;

		$scope.definicion.EsCobroMonedaOperacion = true;

		$scope.pestanaInEx.emisorCheck 							= false;
		$scope.pestanaInEx.instrumentoCheck 					= false;
		$scope.pestanaInEx.tipoDeClienteCheck 					= false;
		$scope.pestanaInEx.clienteCheck 						= false;
		$scope.pestanaInEx.corresponsalCheck 					= false;
		$scope.definicion.OtrosDatos.AplicaPortafolioCheckValor	= true;
		$scope.definicion.OtrosDatos.OperacionContadoCheckValor = true;
		//$scope.defecto.IdBaseTasaCobro = 3;
		
	}






	$scope.crearDefinicionCobros = function() {
		
		var rangos = [];
		$.each($scope.pestanaRangos.apiGrillaRangos.dataSource.data(),function(key,value){
			rangos.push({Hasta: value.Hasta, Valor: value.Valor});
		});
		
		if ($scope.pestanaInEx.emisorCheck && !$scope.pestanaInEx.valoresEmisor.incluye && !$scope.pestanaInEx.valoresEmisor.excluye) {
			bcsComunes.msg("error","error","UI_DEBE_INDICAR_EXCLUYE_O_INCLUYE_EMISOR");
			return;
		}
		if ($scope.pestanaInEx.instrumentoCheck && !$scope.pestanaInEx.valoresInstrumento.incluye && !$scope.pestanaInEx.valoresInstrumento.excluye) {
			bcsComunes.msg("error","error","UI_DEBE_INDICAR_EXCLUYE_O_INCLUYE_INSTRUMENTO");
			return;
		}
		/*if ($scope.pestanaInEx.tipoDeClienteCheck && !$scope.pestanaInEx.valoresTipoDeCliente.incluye && !$scope.pestanaInEx.valoresTipoDeCliente.excluye) {
			bcsComunes.msg("error","error","UI_DEBE_INDICAR_EXCLUYE_O_INCLUYE_TIPO_CLIENTE");
			return;
		}
		if ($scope.pestanaInEx.clienteCheck && !$scope.pestanaInEx.valoresCliente.incluye && !$scope.pestanaInEx.valoresCliente.excluye) {
			bcsComunes.msg("error","error","UI_DEBE_INDICAR_EXCLUYE_O_INCLUYE_CLIENTE");
			return;
		}
		if ($scope.pestanaInEx.corresponsalCheck && !$scope.pestanaInEx.valoresCorresponsal.incluye && !$scope.pestanaInEx.valoresCorresponsal.excluye) {
			bcsComunes.msg("error","error","UI_DEBE_INDICAR_EXCLUYE_O_INCLUYE_CORRESPONSAL");
			return;
		}
		*/		
		var incluyeExcluyeEmisor = undefined;
		var emisores = []
		if ($scope.pestanaInEx.emisorCheck) {
			incluyeExcluyeEmisor = $scope.pestanaInEx.valoresEmisor.incluye?"I":"E";
			$.each($scope.pestanaInEx.apiGrillaEmisor.dataSource.data(),function(key,value){
				emisores.push({IdEmisor: value.IdEmisor});
			})
		}
		
		
		
		var incluyeExcluyeInstrumento = undefined;
		var instrumentos = []
		if ($scope.pestanaInEx.instrumentoCheck) {
			incluyeExcluyeInstrumento = $scope.pestanaInEx.valoresInstrumento.incluye?"I":"E";
			$.each($scope.pestanaInEx.apiGrillaInstrumentos.dataSource.data(),function(key,value){
				instrumentos.push({IdInstrumento: value.IdInstrumento});
			})
		}
		
		/*var incluyeExcluyeTipoDeCliente = undefined;
		var tiposCliente = []
		if ($scope.pestanaInEx.tipoDeClienteCheck) {
			incluyeExcluyeTipoDeCliente = $scope.pestanaInEx.valoresTipoDeCliente.incluye?"I":"E";
			$.each($scope.pestanaInEx.apiGrillaTiposCliente.dataSource.data(),function(key,value){
				tiposCliente.push({IdTipoCliente: value.IdTipoCliente});
			})
		}
		
		var incluyeExcluyeCliente = undefined;
		var clientes = []
		if ($scope.pestanaInEx.clienteCheck) {
			incluyeExcluyeCliente = $scope.pestanaInEx.valoresCliente.incluye?"I":"E";
			$.each($scope.pestanaInEx.apiGrillaClientes.dataSource.data(),function(key,value){
				clientes.push({IdPortafolio: value.IdPortafolio});
			})
		}*/
		
		var incluyeExcluyeCorresponsal = undefined;
		var corresponsales = []
		if ($scope.pestanaInEx.corresponsalCheck) {
			incluyeExcluyeCorresponsal = $scope.pestanaInEx.valoresCorresponsal.incluye?"I":"E";
			$.each($scope.pestanaInEx.apiGrillaCorredoraCorresponsal.dataSource.data(),function(key,value){
				corresponsales.push({IdCorredoraCorresponsal: value.IdCorredoraCorresponsal});
			})
		}
		
		
		if ($scope.definicion.tipoCobroSeleccionado.IdTipoCobro === 'PORCENTAJEFIJO') {
			if ($scope.definicion.PorcentualFijo.SobreMontoCheckValor === false && 
					$scope.definicion.PorcentualFijo.SobreCantidadCheckValor === false &&
					$scope.definicion.PorcentualFijo.MayorEntreDosCheckValor === false &&
					$scope.definicion.PorcentualFijo.MenorEntreDosCheckValor === false) {
				bcsComunes.msg("error","UI_ERROR","UI_DEBE_SELECCIONAR_OPCION_CALCULO_PORCENTAJE_FIJO");
				return;
			}
		}
			
		
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : "PLA",
			p_DefinicionCobro : {
				IdClaseCobro						: $scope.definicion.claseCobroSeleccionado.IdClaseCobro,
				IdTipoCobro							: $scope.definicion.tipoCobroSeleccionado.IdTipoCobro,
				CodigoDefinicionCobro				: $scope.definicion.Codigo,
				IdEstado							: $scope.definicion.estadoCobroSeleccionado.IdEstado,
				Descripcion							: $scope.definicion.Nombre || "",
				CheckCobroMonedaOperacion			: $scope.definicion.EsCobroMonedaOperacion?'S':'N',
				IdMonedaCobroOtraMoneda				: $scope.definicion.idMonedaCobroSeleccionada?$scope.definicion.idMonedaCobroSeleccionada:"",
				IdTipoProducto						: $scope.definicion.OtrosDatos.IdTipoProductoSeleccionado === 0?null:$scope.definicion.OtrosDatos.IdTipoProductoSeleccionado,
				MontoFijo_MontoCobro				: $scope.definicion.MontoFijo || 0,
				MontoFijo_CheckMontoOperacionMenor	: $scope.definicion.MontoFijoMenorActivado?'S':'N',
				MontoFijo_ValorMontoOperacionMenor	: $scope.definicion.MontoFijoOperacionMenor || 0,
				MontoFijo_CheckSiNo					: $scope.definicion.MontoFijoSiNoActivado?'S':'N',
				MontoFijo_ValorMontoSiNo			: $scope.definicion.MontoFijoSiNo || 0,
				
				MontoFijoPorUnidad_MontoCobro       : $scope.definicion.MontoFijoPorUnidadValor || 0,
				MontoFijoPorUnidad_AllInCheckValor  : $scope.definicion.AllInCheckValorMontoPorUnidad?"S":"N",
				
				PorcentualFijo_ValorCobro			: $scope.definicion.PorcentualFijo.PorcentualFijoInputValor || 0,
				PorcentualFijo_CheckSobreMonto		: $scope.definicion.PorcentualFijo.SobreMontoCheckValor?'S':'N',
				PorcentualFijo_CheckSobreCantidad	: $scope.definicion.PorcentualFijo.SobreCantidadCheckValor?'S':'N',
				PorcentualFijo_CheckMayorEntreDos	: $scope.definicion.PorcentualFijo.MayorEntreDosCheckValor?'S':'N',
				PorcentualFijo_CheckMenorEntreDos	: $scope.definicion.PorcentualFijo.MenorEntreDosCheckValor?'S':'N',
				PorcentualFijo_CheckMontoMinimo		: $scope.definicion.PorcentualFijo.MontoMinimoCheckValor?'S':'N',
				PorcentualFijo_ValorMontoMinimo		: $scope.definicion.PorcentualFijo.MontoMinimoInputValor || 0,
				PorcentualFijo_CheckMontoMaximo		: $scope.definicion.PorcentualFijo.MontoMaximoCheckValor?'S':'N',
				PorcentualFijo_ValorMontoMaximo		: $scope.definicion.PorcentualFijo.MontoMaximoInputValor || 0,
				PorcentualFijo_IdMonedaMinimoMaximo	: $scope.definicion.PorcentualFijo.idMonedaMinMaxCobroSeleccionada?$scope.definicion.PorcentualFijo.idMonedaMinMaxCobroSeleccionada:'',
						
				PorcentualTasaPlazo_ValorCobro		: $scope.definicion.PorcentualTasaPlazo.PorcentualTasaPlazoInputValor || 0,		
						
				Otros_CheckOperacionContado			: $scope.definicion.OtrosDatos.OperacionContadoCheckValor?'S':'N',
				Otros_CheckOperacionPlazo			: $scope.definicion.OtrosDatos.OperacionPlazoCheckValor?'S':'N',
				Otros_CheckBaseTasa					: $scope.definicion.OtrosDatos.BaseTasaCheckValor?'S':'N',
				Otros_ValorBaseTasa					: $scope.definicion.OtrosDatos.IdBaseTasaCobroSeleccionada?$scope.definicion.OtrosDatos.IdBaseTasaCobroSeleccionada:0,
				Otros_CheckInteresSimple			: $scope.definicion.OtrosDatos.InteresSimpleCheckValor?'S':'N',
				Otros_CheckInteresCompuesto			: $scope.definicion.OtrosDatos.InteresCompuestoCheckValor?'S':'N',
				Otros_CheckAllIn					: $scope.definicion.OtrosDatos.AllInCheckValor?'S':'N',
				Otros_ValorAllInMontoOperacionBase	: $scope.definicion.OtrosDatos.AllInInputValor || 0,
				Otros_CheckAfectoImpuesto			: $scope.definicion.OtrosDatos.AfectoImpuestoCheckValor?'S':'N',
				Otros_CheckAplicaPortafolio			: $scope.definicion.OtrosDatos.AplicaPortafolioCheckValor?'S':'N',
				Otros_CheckAplicaCorresponsal		: $scope.definicion.OtrosDatos.AplicaCorredoraCorresponsalValor?'S':'N',
				Otros_CheckPermiteAumentarValor		: $scope.definicion.OtrosDatos.PermiteAumentarValorCheckValor?'S':'N',
				Otros_CheckPermiteDisminuirValor	: $scope.definicion.OtrosDatos.PermiteDisminuirValorCheckValor?'S':'N',
						
				PorcentualRango_IdTipoRangoCobro	: $scope.pestanaRangos.IdTipoRangoCobroSeleccionado || "",
				PorcentualRango_IdMonedaRangoMonto	: $scope.pestanaRangos.IdMonedaRangoCobroSeleccionado || "",
			
				Otros_IncluyeExcluyeEmisoresCheck		: $scope.pestanaInEx.emisorCheck?'S':'N',
				Otros_IncluyeExcluyeInstrumentosCheck	: $scope.pestanaInEx.instrumentoCheck?'S':'N',
				Otros_IncluyeExcluyeTiposDeClienteCheck	: 'N',
				Otros_IncluyeExcluyeClientesCheck		: 'N',
				Otros_IncluyeExcluyeCorresponsalesCheck	: $scope.pestanaInEx.corresponsalCheck?'S':'N',
								
				Otros_IncluyeExcluyeEmisoresValor		: incluyeExcluyeEmisor || 'N',
				Otros_IncluyeExcluyeInstrumentosValor	: incluyeExcluyeInstrumento || 'N',
				Otros_IncluyeExcluyeTiposDeClienteValor	: 'N',
				Otros_IncluyeExcluyeClientesValor		: 'N',
				Otros_IncluyeExcluyeCorresponsalesValor	: incluyeExcluyeCorresponsal || 'N',

				Rangos 							: rangos,
				Emisores 						: emisores,
				Instrumentos 					: instrumentos,
				TiposCliente 					: [],
				Clientes 						: [],
				CorredorasCorresponsales 		: corresponsales,
						
				
			}
			
		};
		bcsComunes.ventanaOverlay(idVentana,true);
		if (!params.DefinicionCobro) {
			var p = resources.CrearDefinicionCobro(datos).$promise;
		}
		else {
			var p = resources.ActualizarDefinicionCobro(datos).$promise;
		}
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			var p = bcsComunes.ventanaMsg({
				conflictos : util.parseServerResponse(result)
			},idVentana);
			
			bcsComunes.ventanaPromesa(p).then(function(){
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					bcsComunes.ventanaCerrar(idVentana);
				}
			})
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
				conflictos : util.parseServerResponse(result)
			},idVentana);
		})
	}
	
	
	$scope.$watch('definicion.EsCobroMonedaOperacion',function(newval,oldval){
		if (newval === true) {
			$scope.definicion.idMonedaCobroSeleccionada = undefined;
		}
			
	});
	
	$scope.$watch('definicion.MontoFijoSiNoActivado',function(newval,oldval){
		if (newval === false) {
			$scope.definicion.MontoFijoSiNo = undefined;
		}
	})
	
	$scope.$watch('definicion.MontoFijoMenorActivado',function(newval,oldval){
		if (newval === false) {
			$scope.definicion.MontoFijoOperacionMenor = undefined;
			$scope.definicion.MontoFijoSiNoActivado = false;
		}
	})
	
	$scope.$watch('definicion.MontoFijoActivado',function(newval,oldval){
		if (newval === false) {
			$scope.definicion.MontoFijo = undefined;
			$scope.definicion.MontoFijoMenorActivado = false;
		}
	})
	
	
	$scope.$watch('definicion.tipoCobroSeleccionado',function(newval,oldval){
		if (newval) {
			$scope.definicion.MontoFijoActivado = false;
			$scope.definicion.PorcentualFijo.PorcentualFijoCheckValor = false;
			$scope.definicion.PorcentualTasaPlazo.PorcentualTasaPlazoCheckValor = false;
			$scope.pestanaRangos.PorcentualPorRangoCheckValor = false;
			$scope.definicion.ComisionIndirecta = false;
			$scope.definicion.MontoFijoPorUnidadActivado = false;
			
			if (newval.IdTipoCobro === 'COMISIONINDIRECTA') {
				$scope.definicion.ComisionIndirecta = true;
			}
			if (newval.IdTipoCobro === 'MONTOFIJO') {
				$scope.definicion.MontoFijoActivado = true;
				$scope.definicion.PorcentualFijo.PorcentualFijoCheckValor = false;
				$scope.definicion.PorcentualTasaPlazo.PorcentualTasaPlazoCheckValor = false;
				$scope.pestanaRangos.PorcentualPorRangoCheckValor = false;
			}
			if (newval.IdTipoCobro === 'MONTOFIJOPORUNIDAD') {
				$scope.definicion.MontoFijoPorUnidadActivado = true;
			}
			if (newval.IdTipoCobro === 'PORCENTAJEFIJO') {
				$scope.definicion.MontoFijoActivado = false;
				$scope.definicion.PorcentualFijo.PorcentualFijoCheckValor = true;
				$scope.definicion.PorcentualTasaPlazo.PorcentualTasaPlazoCheckValor = false;
				$scope.pestanaRangos.PorcentualPorRangoCheckValor = false;
			}
			if (newval.IdTipoCobro === 'PORCENTAJERANGOS') {
				$scope.definicion.MontoFijoActivado = false;
				$scope.definicion.PorcentualFijo.PorcentualFijoCheckValor = false;
				$scope.definicion.PorcentualTasaPlazo.PorcentualTasaPlazoCheckValor = false;
				$scope.pestanaRangos.PorcentualPorRangoCheckValor = true;
			}
			if (newval.IdTipoCobro === 'PORCENTAJETASAPLAZO') {
				$scope.definicion.MontoFijoActivado = false;
				$scope.definicion.PorcentualFijo.PorcentualFijoCheckValor = false;
				$scope.definicion.PorcentualTasaPlazo.PorcentualTasaPlazoCheckValor = true;
				$scope.pestanaRangos.PorcentualPorRangoCheckValor = true;
			}
		}
	});
	
	
	$scope.$watch('definicion.PorcentualFijo.PorcentualFijoCheckValor',function(newval,oldval){
		if (!$scope.definicion.PorcentualFijo.PorcentualFijoCheckValor) {
			$scope.definicion.PorcentualFijo.idMonedaMinMaxCobroSeleccionada = undefined;
		}
		else {

		}
	});
	
	$scope.$watch('definicion.PorcentualFijo.MayorEntreDosCheckValor',function(newval,oldval){
		if ($scope.definicion.PorcentualFijo.MayorEntreDosCheckValor)
			if ($scope.definicion.PorcentualFijo.MenorEntreDosCheckValor) {
				$scope.definicion.PorcentualFijo.MenorEntreDosCheckValor = false;
				return;
			}
	})
	
	$scope.$watch('definicion.PorcentualFijo.MenorEntreDosCheckValor',function(newval,oldval){
		if ($scope.definicion.PorcentualFijo.MayorEntreDosCheckValor)
			if ($scope.definicion.PorcentualFijo.MenorEntreDosCheckValor) {
				$scope.definicion.PorcentualFijo.MayorEntreDosCheckValor = false;
				return;
			}
	})
	
	//-----------------------------------------------------------------------------------------
	//tipo cobro porcentaje tasa/plazo
	/*$scope.$watch('definicion.PorcentualTasaPlazo.PorcentualTasaPlazoCheckValor',function(newval,oldval){
		if (!$scope.definicion.OtrosDatos.OperacionPlazoCheckValor) {
			$scope.definicion.OtrosDatos.InteresSimpleCheckValor = true;
			$scope.definicion.OtrosDatos.InteresCompuestoCheckValor = false;
		}
		else {
			
		}
			
	})*/
	
	//----------------------------------------------------------------------------------
	//interes simple, interes compuesto
	$scope.$watch('definicion.OtrosDatos.InteresSimpleCheckValor',function(newval,oldval){
		if ($scope.definicion.OtrosDatos.InteresSimpleCheckValor)
			if ($scope.definicion.OtrosDatos.InteresCompuestoCheckValor) {
				$scope.definicion.OtrosDatos.InteresCompuestoCheckValor = false;
				return;
			}
	})
	
	$scope.$watch('definicion.OtrosDatos.InteresCompuestoCheckValor',function(newval,oldval){
		if ($scope.definicion.OtrosDatos.InteresCompuestoCheckValor)
			if ($scope.definicion.OtrosDatos.InteresSimpleCheckValor) {
				$scope.definicion.OtrosDatos.InteresSimpleCheckValor = false;
				return;
			}
	})
	//------------------------------------------------------------------------------------
	
	
	//-------------------------------------------------------------------------------------
	//All In Valor
	$scope.$watch('definicion.OtrosDatos.AllInCheckValor',function(newval,oldval){
		if (!$scope.definicion.OtrosDatos.AllInCheckValor)
			$scope.definicion.OtrosDatos.AllInInputValor = undefined;
	})
	
	//-------------------------------------------------------------------------------------
	
	$scope.$watch('definicion.PorcentualFijo.MontoMinimoCheckValor',function(newval,oldval){
		if (!$scope.definicion.PorcentualFijo.MontoMinimoCheckValor) {
			$scope.definicion.PorcentualFijo.MontoMinimoInputActivado = false;
			$scope.definicion.PorcentualFijo.MontoMinimoInputValor = undefined;
		}else {
			$scope.definicion.PorcentualFijo.MontoMinimoInputActivado = true;
		}
	})
	
	$scope.$watch('definicion.PorcentualFijo.MontoMaximoCheckValor',function(newval,oldval){
		if (!$scope.definicion.PorcentualFijo.MontoMaximoCheckValor) {
			$scope.definicion.PorcentualFijo.MontoMaximoInputActivado = false;
			$scope.definicion.PorcentualFijo.MontoMaximoInputValor = undefined;
		}else {
			$scope.definicion.PorcentualFijo.MontoMaximoInputActivado = true;
		}
	})
	
	var dsRangos = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			pageSize : 200,
			serverSorting : true, 
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3561",
					dataType: "json"
				}                    
			},
			schema : {
				model: {
					fields : {
						IdEmpresa 				: { type: "number"},
						CodigoDefinicionCobro 	: { type: "string"},
						Hasta					: { type: "number"},
						Valor					: { type: "number"}
					}
				}
			}
		},
	});
	
	//----------------------------------------------------------------------------------------------------------------------------
	//pestana rangos
	$scope.pestanaRangos.configuracionGrillaRangos = {
			autoBind: true,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			height:300,
			resizable: true,
      		selectable: "row",
      		scrollable: true,
      		navigatable: true,
      		gridCreated : function(grid){
      			$scope.pestanaRangos.apiGrillaRangos = grid;
      			if (params.DefinicionCobro) {
      				var p = dsRangos.query({filter :[ 
      					{field : "IdEmpresa",operator : "eq" , value : params.DefinicionCobro.IdEmpresa},
      					{field : "CodigoDefinicionCobro",operator : "eq" , value : params.DefinicionCobro.CodigoDefinicionCobro}]});
      				p.then(function(result){
      					var data = dsRangos.data(false);
      					$scope.pestanaRangos.apiGrillaRangos.dataSource.data(data);
      				})
      			}
      			
      		},
			columns : [
				{field: "Hasta", title: 'UI_HASTA',width: "100px"},
				{field: "Valor", title: 'UI_VALOR'}
				
			],
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					serverSorting:false,
					sort: { field: "Hasta", dir: "asc" },
					schema : {
						model : {
							fields : {
								IdEmpresa 				: { type: "number"},
								CodigoDefinicionCobro 	: { type: "string"},
								Hasta					: { type: "number"},
								Valor					: { type: "number"}
							}
						} 
							
					}
				},
	  		}),
	};
	
	$scope.pestanaRangos.agregarRango = function() {
		if ($scope.pestanaRangos.ValorHastaInputValor === undefined || $scope.pestanaRangos.ValorPorcentajeInputValor === undefined) {
			bcsComunes.msg("error","UI_ERROR","UI_DEBE_INGRESAR_AMBOS_VALORES");
			return;
		}
		$scope.pestanaRangos.apiGrillaRangos.dataSource.add({Hasta : $scope.pestanaRangos.ValorHastaInputValor , Valor : $scope.pestanaRangos.ValorPorcentajeInputValor});
		$scope.pestanaRangos.ValorHastaInputValor = undefined;
		$scope.pestanaRangos.ValorPorcentajeInputValor = undefined;
	} 
	
	$scope.pestanaRangos.quitarRango = function() {
		$scope.pestanaRangos.apiGrillaRangos.dataSource.remove($scope.pestanaRangos.rangoSeleccionado);
		$scope.pestanaRangos.rangoSeleccionado = undefined;
	} 
	
	
	$scope.$watch('pestanaRangos.PorcentualPorRangoCheckValor',function(newval,oldval){
		if (!$scope.pestanaRangos.PorcentualPorRangoCheckValor) {
			$scope.pestanaRangos.IdTipoRangoCobroSeleccionado = undefined;
			$scope.pestanaRangos.IdMonedaRangoCobroSeleccionado  = undefined;
			$scope.pestanaRangos.ValorHastaInputValor = undefined;
			$scope.pestanaRangos.ValorPorcentajeInputValor = undefined;
			
		}
		else {
			
		}
	})
	
	//----------------------------------------------------------------------------------------------------------------------------
	var dsEmisores = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			pageSize : 200,
			serverSorting : true, 
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3556",
					dataType: "json"
				}                    
			},
			schema : {
				model: {
					fields : {
						IdEmpresa 				: { type: "number"},
						CodigoDefinicionCobro 	: { type: "string"},
						IdEmisor				: { type: "string"}
					}
				}
			}
		},
	});

	
	$scope.pestanaInEx.configuracionGrillaEmisores = {
			autoBind: true,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			height:250,
			resizable: true,
      		selectable: "row",
      		scrollable: true,
      		sortable: true,
      		navigatable: true,
      		gridCreated : function (grid){
      			$scope.pestanaInEx.apiGrillaEmisor = grid;
      			if (params.DefinicionCobro) {
      				var p = dsEmisores.query({filter :[ 
      					{field : "IdEmpresa",operator : "eq" , value : params.DefinicionCobro.IdEmpresa},
      					{field : "CodigoDefinicionCobro",operator : "eq" , value : params.DefinicionCobro.CodigoDefinicionCobro}]});
      				p.then(function(result){
      					var data = dsEmisores.data(false);
      					$scope.pestanaInEx.apiGrillaEmisor.dataSource.data(data);
      				})
      			}
      		},
			columns : [
				{field: "IdEmisor", title: $translate.instant('UI_EMISOR')}
				
			],
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : {
						model : {
							fields : {
								IdEmisor : {type : "string"}
							}
						} 
							
					}
				},
	  		}),
	};
	
	$scope.pestanaInEx.agregarEmisor = function() {
		var emisores = $scope.pestanaInEx.apiGrillaEmisor.dataSource.data();
		if (_.find(emisores,function(emi){return emi.IdEmisor === $scope.pestanaInEx.EmisorSeleccionadoCombo.IdEmisor})) {
			bcsComunes.msg("error","UI_ERROR","UI_EMISOR_YA_AGREGADO");
			return;
		}
		$scope.pestanaInEx.apiGrillaEmisor.dataSource.add({IdEmisor : $scope.pestanaInEx.IdEmisorSeleccionadoCombo});
		$scope.pestanaInEx.EmisorSeleccionadoCombo = undefined;
	} 
	
	$scope.pestanaInEx.quitarEmisor = function() {
		$scope.pestanaInEx.apiGrillaEmisor.dataSource.remove($scope.pestanaInEx.EmisorSeleccionado);
		$scope.pestanaInEx.EmisorSeleccionado = undefined;
	} 
	
	var dsInstrumentos = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			pageSize : 200,
			serverSorting : true, 
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3557",
					dataType: "json"
				}                    
			},
			schema : {
				model: {
					fields : {
						IdEmpresa 				: { type: "number"},
						CodigoDefinicionCobro 	: { type: "string"},
						IdInstrumento			: { type: "string"},
						Nemotecnico				: { type: "string"},
						RazonSocial				: { type: "string"}
					}
				}
			}
		},
	});
	$scope.pestanaInEx.configuracionGrillaInstrumentos = {
			autoBind: true,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			height:250,
			resizable: true,
      		selectable: "row",
      		scrollable: true,
      		sortable: true,
      		navigatable: true,
      		gridCreated : function (grid){
      			$scope.pestanaInEx.apiGrillaInstrumentos = grid;
      			if (params.DefinicionCobro) {
      				var p = dsInstrumentos.query({filter :[ 
      					{field : "IdEmpresa",operator : "eq" , value : params.DefinicionCobro.IdEmpresa},
      					{field : "CodigoDefinicionCobro",operator : "eq" , value : params.DefinicionCobro.CodigoDefinicionCobro}]});
      				p.then(function(result){
      					var data = dsInstrumentos.data(false);
      					$scope.pestanaInEx.apiGrillaInstrumentos.dataSource.data(data);
      				})
      			}
      		},
			columns : [
				{field: "Nemotecnico", title: $translate.instant('UI_INSTRUMENTO')},
				{field: "RazonSocial", title: $translate.instant('UI_EMPRESA')}
				
			],
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [
						
						],
					schema : {
						model : {
							fields : {
								IdInstrumento : {type : "number"},
								Nemotecnico : {type : "string"},
								RazonSocial : {type : "string"}
							}
						} 
							
					}
				},
	  		}),
	};
	
	$scope.pestanaInEx.agregarInstrumento = function() {
		var instrumentos = $scope.pestanaInEx.apiGrillaInstrumentos.dataSource.data();
		if (_.find(instrumentos,function(ins){return ins.IdInstrumento === $scope.pestanaInEx.InstrumentoSeleccionadoCombo.IdInstrumento})) {
			bcsComunes.msg("error","UI_ERROR","UI_INSTRUMENTO_YA_AGREGADO");
			return;
		}
		
		$scope.pestanaInEx.apiGrillaInstrumentos.dataSource.add({
			IdInstrumento : $scope.pestanaInEx.InstrumentoSeleccionadoCombo.IdInstrumento,
			Nemotecnico : $scope.pestanaInEx.InstrumentoSeleccionadoCombo.Nemotecnico,
			RazonSocial : $scope.pestanaInEx.InstrumentoSeleccionadoCombo.RazonSocial
			
		});
		$scope.pestanaInEx.setInstrumento = "-1";
	} 
	
	$scope.pestanaInEx.quitarInstrumento = function() {
		$scope.pestanaInEx.apiGrillaInstrumentos.dataSource.remove($scope.pestanaInEx.InstrumentoSeleccionado);
		$scope.pestanaInEx.EmisorSeleccionado = undefined;
	} 
	
	var dsTiposCliente = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			pageSize : 200,
			serverSorting : true, 
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3558",
					dataType: "json"
				}                    
			},
			schema : {
				model: {
					fields : {
						IdEmpresa 				: { type: "number"},
						CodigoDefinicionCobro 	: { type: "string"},
						IdTipoCliente 			: { type: "string"},
						NombreTipoCliente	 	: { type: "string"},
					}
				}
			}
		},
	});
	$scope.pestanaInEx.configuracionGrillaTiposClientes = {
			autoBind: true,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			height:250,
			resizable: true,
      		selectable: "row",
      		scrollable: true,
      		sortable: true,
      		navigatable: true,
      		gridCreated : function (grid){
      			$scope.pestanaInEx.apiGrillaTiposCliente = grid;
      			if (params.DefinicionCobro) {
      				var p = dsTiposCliente.query({filter :[ 
      					{field : "IdEmpresa",operator : "eq" , value : params.DefinicionCobro.IdEmpresa},
      					{field : "CodigoDefinicionCobro",operator : "eq" , value : params.DefinicionCobro.CodigoDefinicionCobro}]});
      				p.then(function(result){
      					var data = dsTiposCliente.data(false);
      					$scope.pestanaInEx.apiGrillaTiposCliente.dataSource.data(data);
      				})
      			}
      		},
			columns : [
				{field: "NombreTipoCliente", title: $translate.instant('UI_TIPO_CLIENTE')}
				
			],
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : {
						model : {
							fields : {
								IdTipoCliente : {type : "string"},
								NombreTipoCliente : {type : "string"}
							}
						} 
							
					}
				},
	  		}),
	};
	
	$scope.pestanaInEx.agregarTipoCliente = function() {
		var tiposCliente = $scope.pestanaInEx.apiGrillaTiposCliente.dataSource.data();
		if (_.find(tiposCliente,function(tip){return tip.IdTipoCliente === $scope.pestanaInEx.TipoClienteSeleccionadoCombo.IdTipoCliente})) {
			bcsComunes.msg("error","UI_ERROR","UI_INSTRUMENTO_YA_AGREGADO");
			return;
		}
		
		$scope.pestanaInEx.apiGrillaTiposCliente.dataSource.add({
			IdTipoCliente : $scope.pestanaInEx.TipoClienteSeleccionadoCombo.IdTipoCliente,
			NombreTipoCliente : $scope.pestanaInEx.TipoClienteSeleccionadoCombo.Nombre,
			
		});
		$scope.pestanaInEx.IdTipoClienteSeleccionadoCombo = undefined;
	} 
	
	$scope.pestanaInEx.quitarTipoCliente = function() {
		$scope.pestanaInEx.apiGrillaTiposCliente.dataSource.remove($scope.pestanaInEx.TipoClienteSeleccionado);
		$scope.pestanaInEx.TipoClienteSeleccionado = undefined;
	} 
	
	
	var dsClientes = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			pageSize : 200,
			serverSorting : true, 
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3559",
					dataType: "json"
				}                    
			},
			schema : {
				model: {
					fields : {
						IdEmpresa 				: { type: "number"},
						CodigoDefinicionCobro 	: { type: "string"},
						IdCliente : {type : "number"},
						IdPortafolio: {type : "number"},
						DocumentoIdentidad : {type : "string"},
						NombreCompleto : {type : "string"},
						CodigoPortafolio : {type : "string"},
					}
				}
			}
		},
	});
	$scope.pestanaInEx.configuracionGrillaClientes = {
			autoBind: true,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			height:250,
			resizable: true,
      		selectable: "row",
      		scrollable: true,
      		sortable: true,
      		navigatable: true,
      		gridCreated : function (grid){
      			$scope.pestanaInEx.apiGrillaClientes = grid;
      			if (params.DefinicionCobro) {
      				var p = dsClientes.query({filter :[ 
      					{field : "IdEmpresa",operator : "eq" , value : params.DefinicionCobro.IdEmpresa},
      					{field : "CodigoDefinicionCobro",operator : "eq" , value : params.DefinicionCobro.CodigoDefinicionCobro}]});
      				p.then(function(result){
      					var data = dsClientes.data(false);
      					$scope.pestanaInEx.apiGrillaClientes.dataSource.data(data);
      				})
      			}
      		},
			columns : [
				{field: "DocumentoIdentidad", title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')},
				{field: "NombreCompleto", title: $translate.instant('UI_CLIENTE')},
				{field: "CodigoPortafolio", title: $translate.instant('UI_PORTAFOLIO')}
				
			],
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : {
						model : {
							fields : {
								IdCliente : {type : "number"},
								IdPortafolio: {type : "number"},
								DocumentoIdentidad : {type : "string"},
								NombreCompleto : {type : "string"},
								CodigoPortafolio : {type : "string"},
							}
						} 
							
					}
				},
	  		}),
	};
	
	$scope.pestanaInEx.agregarPortafolio = function() {
		var portafolios = $scope.pestanaInEx.apiGrillaClientes.dataSource.data();
		if (_.find(portafolios,function(por){return por.IdPortafolio === $scope.pestanaInEx.PortafolioSeleccionadoCombo.IdPortafolio})) {
			bcsComunes.msg("error","UI_ERROR","UI_PORTAFOLIO_YA_AGREGADO");
			return;
		}
		
		$scope.pestanaInEx.apiGrillaClientes.dataSource.add({
			IdCliente 			: $scope.pestanaInEx.PortafolioSeleccionadoCombo.IdCliente,
			IdPortafolio 		: $scope.pestanaInEx.PortafolioSeleccionadoCombo.IdPortafolio,
			DocumentoIdentidad 	: $scope.pestanaInEx.PortafolioSeleccionadoCombo.DocumentoIdentidad,
			NombreCompleto 		: $scope.pestanaInEx.PortafolioSeleccionadoCombo.NombreCompleto,
			CodigoPortafolio 	: $scope.pestanaInEx.PortafolioSeleccionadoCombo.CodigoPortafolio,
			
			
		});
		$scope.pestanaInEx.IdPortafolioSeleccionadoCombo = false;
	} 
	
	$scope.pestanaInEx.quitarPortafolio = function() {
		$scope.pestanaInEx.apiGrillaClientes.dataSource.remove($scope.pestanaInEx.PortafolioSeleccionado);
		$scope.pestanaInEx.PortafolioSeleccionado = undefined;
	} 
	
	var dsCorresponsales = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			pageSize : 200,
			serverSorting : true, 
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3560",
					dataType: "json"
				}                    
			},
			schema : {
				model: {
					fields : {
						IdEmpresa 				: { type: "number"},
						CodigoDefinicionCobro 	: { type: "string"},
						IdCorredoraCorresponsal	: {type : "number"},
						CodigoCorredor			: {type : "string"},
						IdPais 					: {type : "number"},
						NombrePais				: {type : "string"},
					}
				}
			}
		},
	});
	$scope.pestanaInEx.configuracionGrillaCorredoraCorresponsal = {
			autoBind: true,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			height:250,
			resizable: true,
      		selectable: "row",
      		scrollable: true,
      		sortable: true,
      		navigatable: true,
      		gridCreated : function (grid){
      			$scope.pestanaInEx.apiGrillaCorredoraCorresponsal = grid;
      			if (params.DefinicionCobro) {
      				var p = dsCorresponsales.query({filter :[ 
      					{field : "IdEmpresa",operator : "eq" , value : params.DefinicionCobro.IdEmpresa},
      					{field : "CodigoDefinicionCobro",operator : "eq" , value : params.DefinicionCobro.CodigoDefinicionCobro}]});
      				p.then(function(result){
      					var data = dsCorresponsales.data(false);
      					$scope.pestanaInEx.apiGrillaCorredoraCorresponsal.dataSource.data(data);
      				})
      			}
      		},
			columns : [
				{field: "CodigoCorredor", title: $translate.instant('UI_CORREDORA_CORRESPONSAL')},
				{field: "NombrePais", title: $translate.instant('UI_PAIS')}
				
				
			],
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : {
						model : {
							fields : {
								IdCorredoraCorresponsal	: {type : "number"},
								CodigoCorredor			: {type : "string"},
								IdPais 					: {type : "number"},
								NombrePais				: {type : "string"},
							}
						} 
							
					}
				},
	  		}),
	};
	
	$scope.pestanaInEx.agregarCorredoraCorresponsal = function() {
		var corredoras = $scope.pestanaInEx.apiGrillaCorredoraCorresponsal.dataSource.data();
		if (_.find(corredoras,function(cor){return cor.IdCorredoraCorresponsal === $scope.pestanaInEx.CorredoraCorresponsalSeleccionadaCombo.IdCorredorCorresponsal})) {
			bcsComunes.msg("error","UI_ERROR","UI_CORREDORA_YA_AGREGADA");
			return;
		}
		
		$scope.pestanaInEx.apiGrillaCorredoraCorresponsal.dataSource.add({
			IdCorredoraCorresponsal : $scope.pestanaInEx.CorredoraCorresponsalSeleccionadaCombo.IdCorredorCorresponsal,
			CodigoCorredor 			: $scope.pestanaInEx.CorredoraCorresponsalSeleccionadaCombo.CodigoCorredor,
			IdPais 					: $scope.pestanaInEx.CorredoraCorresponsalSeleccionadaCombo.IdPais,
			NombrePais 				: $scope.pestanaInEx.CorredoraCorresponsalSeleccionadaCombo.NombrePais
			
			
		});
		$scope.pestanaInEx.IdCorredoraCorresponsalSeleccionadaCombo = undefined;
	} 
	
	$scope.pestanaInEx.quitarCorredoraCorresponsal = function() {
		$scope.pestanaInEx.apiGrillaCorredoraCorresponsal.dataSource.remove($scope.pestanaInEx.CorredoraCorresponsalSeleccionada);
		$scope.pestanaInEx.CorredoraCorresponsalSeleccionada = undefined;
	} 
	
	
});


