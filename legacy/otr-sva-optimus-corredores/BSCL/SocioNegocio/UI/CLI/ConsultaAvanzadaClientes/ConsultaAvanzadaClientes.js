angular.module('ModuloSocioNegocio').controller('ConsultaAvanzadaClientesCtrl',function($scope,$translate,$resource,bcsComunes,$timeout, bcsServicioPlataforma){

	$scope.formData = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.formData.masOpciones = false;

	var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
	var filters = {};

	if (parametros.parametros !== undefined && parametros.parametros.modo === "SELECCION") {
		$scope.modoSeleccion = true;
		if (parametros.parametros.tipo === "CLIENTE") {
			$scope.tipoSeleccion = "CLIENTE";
		}else {
			$scope.tipoSeleccion = "PORTAFOLIO";
		}
			
	}
		
	
	//caja
	$scope.$watch('formData.busqueda.MenorQue',function(newval,oldval){
		if (!oldval && newval) {
			$scope.formData.busqueda.MayorQue = false;
		}
		else if (oldval && !newval && !$scope.formData.busqueda.MayorQue) {
			$scope.formData.busqueda.MayorQue = true;
		}
		
		if (!$scope.formData.busqueda.MayorQue && !$scope.formData.busqueda.MenorQue) {
			$scope.formData.busqueda.MayorQue = true;
		}
		
	});
	
	$scope.$watch('formData.busqueda.MayorQue',function(newval,oldval){
		if (!oldval && newval) {
			$scope.formData.busqueda.MenorQue = false;
		}
		else if (oldval && !newval && !$scope.formData.busqueda.MenorQue) {
			$scope.formData.busqueda.MenorQue = true;
		}
	});
	
	//custodia
	$scope.$watch('formData.busqueda.CustodiaMenorQue',function(newval,oldval){
		if (!oldval && newval) {
			$scope.formData.busqueda.CustodiaMayorQue = false;
		}
		else if (oldval && !newval && !$scope.formData.busqueda.CustodiaMayorQue) {
			$scope.formData.busqueda.CustodiaMayorQue = true;
		}
		
		if (!$scope.formData.busqueda.CustodiaMayorQue && !$scope.formData.busqueda.CustodiaMenorQue) {
			$scope.formData.busqueda.CustodiaMayorQue = true;
		}
		
	});
	
	$scope.$watch('formData.busqueda.CustodiaMayorQue',function(newval,oldval){
		if (!oldval && newval) {
			$scope.formData.busqueda.CustodiaMenorQue = false;
		}
		else if (oldval && !newval && !$scope.formData.busqueda.CustodiaMenorQue) {
			$scope.formData.busqueda.CustodiaMenorQue = true;
		}
	});
	
	
	//formData para configuraciones generales 
	$scope.formData.seleccionar = function(){
		if ($scope.tipoSeleccion === "CLIENTE") {
			bcsComunes.ventanaData($scope.idVentana,$scope.formData.seleccionado[0].IdCliente);
		}
		else {
			if ($scope.formData.seleccionado[0] !== undefined) {
				bcsComunes.ventanaData($scope.idVentana,$scope.formData.seleccionado[0].IdPortafolio);
			}
		}
		bcsComunes.ventanaCerrar($scope.idVentana);
	};
	
	
	$scope.tipoBloqueoEnabled = false;
	$scope.fechaDefuncionDesdeDisabled = true;
	$scope.fechaDefuncionHastaDisabled = true;
	
	$scope.$watch('formData.busqueda.IdEstadoCliente',function(newval,oldval){
		if($scope.formData.busqueda.IdEstadoCliente == 'BLO'){
			$scope.tipoBloqueoEnabled = true;
		}
		else{
			$scope.tipoBloqueoEnabled = false;
		}
	})
	
	$scope.$watch('formData.busqueda.IdTipoBloqueoCliente',function(newval,oldval){
		if($scope.formData.busqueda.IdTipoBloqueoCliente == 'BMUERT'){
			$scope.fechaDefuncionDesdeDisabled = false;
			$scope.fechaDefuncionHastaDisabled = false;
		}
		else{
			$scope.fechaDefuncionDesdeDisabled = true;
			$scope.fechaDefuncionHastaDisabled = true;
		}
	})
	
	//formData para configuraciones generales 
	$scope.formData.busqueda = {
			
	};
	$scope.formData.filtros = {
			tipoBloqueoCliente : {
				IdTipoEntidad : { field : "IdTipoEntidad", operator : "eq" , value : "CLIENTE" },
				IdAccion : { field : "IdAccion", operator : "eq" , value : "BLO" },
				IdEmpresa : { field : "IdEmpresa", operator : "eq" , value : $scope.SesionIdEmpresa }
			},
			tipoSegmento : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq" , value : $scope.SesionIdEmpresa }
			},
			tipoCliente : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq" , value : $scope.SesionIdEmpresa },
				IdEstado: { field : "IdEstado", operator : "eq" , value : "ACT" }
			},
			propositoPortafolio : {
				IdEstado : { field : "IdEstado", operator : "eq" , value : 'ACT' }
			},
			ejecutivoPortafolio : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq" , value : $scope.SesionIdEmpresa }
			},
			tipoBloqueoPortafolio : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq" , value : $scope.SesionIdEmpresa },
				IdTipoEntidad : { field : "IdTipoEntidad", operator : "eq" , value : "PORTAFOLIO" },
				IdAccion : { field : "IdAccion", operator : "eq" , value : "BLO" }
			},
			usoFlujoCaja : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq" , value : $scope.SesionIdEmpresa },
				DesplegarEnUI : { field : "DesplegarEnUI", operator : "eq" , value : 'S'}
			},
			//Se añaden filtros para evitar que se traigan demasiados datos innecesarios de los servicios 1631 y 1632
			FiltroPerfilProductos : {
				DescripcionPerfilProductos : { field: "DescripcionPerfilProductos", operator: "doesnotcontain", value: "EXT_"}		
			},
			FiltroPerfilCobros : {
				DescripcionPerfilCobros : { field: "DescripcionPerfilCobros", operator: "doesnotcontain", value: "EXT_"}
			}							
	}
	
	$scope.formData.limpiar = function() {
		$scope.formData.busqueda = {
				
		};
	}
	
	$scope.formData.dobleClick = function(portafolio) {
		if (!portafolio)
			return;
		if ($scope.modoSeleccion) {
			$scope.formData.seleccionar();
		}
		else {
			bcsComunes.ventanaAbrir("administrarPortafolio",{ IdCliente: portafolio.IdCliente, IdPersona : $scope.formData.seleccionado[0].IdPersona , IdPortafolio: portafolio.IdPortafolio },false,false);
		}
	}
	
	$scope.formData.verFichaCliente = function(){
		bcsComunes.ventanaAbrir("administrarSocioNegocio", {IdCliente : $scope.formData.seleccionado[0].IdCliente, IdPersona : $scope.formData.seleccionado[0].IdPersona, Entidad: "CLI"},false);
	}
	
	var r = $resource("", {}, {  'ConsultaAvanzadaClientes' :  { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1479&c=1"  }});
	
	var parsearFiltros = function(){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo	: 'SOC'
		}
		
		datos.p_IdEmpleadoConsulta = $scope.SesionIdEmpleado;
		if ($scope.formData.busqueda.IdTipoBloqueoPortafolio) {
			datos.p_IdTipoBloqueoPortafolio = $scope.formData.busqueda.IdTipoBloqueoPortafolio;
		}
		if ($scope.formData.busqueda.IdEmpleadoFiltro) {
			datos.p_IdEmpleadoFiltro = $scope.formData.busqueda.IdEmpleadoFiltro;
		}		
		if ($scope.formData.busqueda.IdEstadoPortafolio) {
			datos.p_IdEstadoPortafolio = $scope.formData.busqueda.IdEstadoPortafolio;
		}
		if ($scope.formData.busqueda.IdPropositoPortafolio) {
			datos.p_IdPropositoPortafolio = $scope.formData.busqueda.IdPropositoPortafolio;
		}
		if ($scope.formData.busqueda.IdPerfilCobros) {
			datos.p_IdPerfilCobros = $scope.formData.busqueda.IdPerfilCobros;
		}
		if ($scope.formData.busqueda.IdSegmentoCliente) {
			datos.p_IdSegmentoCliente = $scope.formData.busqueda.IdSegmentoCliente;
		}
		if ($scope.formData.busqueda.IdEstadoCliente) {
			datos.p_IdEstadoCliente = $scope.formData.busqueda.IdEstadoCliente;
		}
		if ($scope.formData.busqueda.IdPersonaBancoCustodio) {
			datos.p_IdPersonaBancoCustodio = $scope.formData.busqueda.IdPersonaBancoCustodio;
		}
		if ($scope.formData.busqueda.IdPersonaBrokerExtranjero) {
			datos.p_IdPersonaBrokerExtranjero = $scope.formData.busqueda.IdPersonaBrokerExtranjero;
		}
		if ($scope.formData.busqueda.IdPerfilProductos) {
			datos.p_IdPerfilProductos = $scope.formData.busqueda.IdPerfilProductos;
		}
		if ($scope.formData.busqueda.IdPerfilComercial) {
			datos.p_IdPerfilComercial = $scope.formData.busqueda.IdPerfilComercial;
		}
		if ($scope.formData.busqueda.aliasPortarfolio) {
			datos.p_AliasPortafolio = $scope.formData.busqueda.aliasPortarfolio;
		}
		if ($scope.formData.busqueda.IdTipoBloqueoCliente) {
			datos.p_IdTipoBloqueoCliente = $scope.formData.busqueda.IdTipoBloqueoCliente;
		}
		if (!!$scope.formData.busqueda.fechaNacimientoDesde) {
			datos.p_FechaNacimientoDesde = util.dateSPdate(util.dateInicioDia($scope.formData.busqueda.fechaNacimientoDesde));
		}
		if (!!$scope.formData.busqueda.fechaNacimientoHasta) {
			datos.p_FechaNacimientoHasta = util.dateSPdate(util.dateTerminoDia($scope.formData.busqueda.fechaNacimientoHasta));
		}
		if (!!$scope.formData.busqueda.fechaCreacionDesde) {
			datos.p_FechaCreacionDesde = util.dateSPseconddate(util.dateInicioDia($scope.formData.busqueda.fechaCreacionDesde));
		}
		if (!!$scope.formData.busqueda.fechaCreacionHasta) {
			datos.p_FechaCreacionHasta = util.dateSPseconddate(util.dateTerminoDia($scope.formData.busqueda.fechaCreacionHasta));
		}
		if (!!$scope.formData.busqueda.fechaDefuncionDesde) {
			datos.p_FechaDefuncionDesde = util.dateSPseconddate(util.dateInicioDia($scope.formData.busqueda.fechaDefuncionDesde));
		}
		if (!!$scope.formData.busqueda.fechaDefuncionHasta) {
			datos.p_FechaDefuncionHasta = util.dateSPseconddate(util.dateTerminoDia($scope.formData.busqueda.fechaDefuncionHasta));
		}
		if ($scope.formData.busqueda.IdTipoCliente) {
			datos.p_IdTipoCliente = $scope.formData.busqueda.IdTipoCliente;
		}
		if ($scope.formData.busqueda.IdTipoPersona) {
			datos.p_IdTipoPersona = $scope.formData.busqueda.IdTipoPersona;
		}
		if ($scope.formData.busqueda.documentoIdentidadAsociado) {
			datos.p_DocumentoIdentidad = $scope.formData.busqueda.documentoIdentidadAsociado;
		}
		if ($scope.formData.busqueda.tipoDocumentoIdentidadAsociado) {
			datos.p_TipoDocumentoIdentidad = $scope.formData.busqueda.tipoDocumentoIdentidadAsociado;
		}
		if ($scope.formData.busqueda.nombreCompleto) {
			datos.p_Nombre = $scope.formData.busqueda.nombreCompleto;
		}
		
		if ($scope.formData.busqueda.clienteFinal) {
			datos.p_ClienteFinal = $scope.formData.busqueda.clienteFinal? 'S': 'N';
		}
		
		if ($scope.formData.busqueda.IdTipoEnrolamiento) {
			datos.p_TipoEnrolamiento = $scope.formData.busqueda.IdTipoEnrolamiento || '';
		}
		
		
		if ($scope.formData.busqueda.buscaPorFlujoCaja && ($scope.formData.busqueda.MayorQue === true || $scope.formData.busqueda.MenorQue === true)) {
			datos.p_BuscaPorFlujoCaja = 'S';
			datos.p_UsoFlujoCaja = $scope.formData.busqueda.UsoFlujoCaja;
			datos.p_SaldoFlujoCaja = $scope.formData.busqueda.saldoCuentaInversion;
			datos.p_IdMonedaFlujoCaja = $scope.formData.busqueda.IdMonedaTesoreria;
			datos.p_CriterioFlujoCaja = $scope.formData.busqueda.MayorQue?'MAYOR_QUE':'MENOR_QUE';
		}
		
		if ($scope.formData.busqueda.buscaPorFlujoCustodia && ($scope.formData.busqueda.CustodiaMayorQue === true || $scope.formData.busqueda.CustodiaMenorQue === true)) {
			datos.p_BuscaPorFlujoCustodia = 'S';
			datos.p_TipoOperacionCustodia = $scope.formData.busqueda.IdUsoFlujoCustodia;
			datos.p_SaldoFlujoCustodia = $scope.formData.busqueda.cantidadInstrumento;
			datos.p_IdInstrumento = $scope.formData.busqueda.instrumento.IdInstrumento;
			datos.p_CriterioFlujoCustodia = $scope.formData.busqueda.CustodiaMayorQue?'MAYOR_QUE':'MENOR_QUE';
		}
		
		return datos;
	}
	
	$scope.formData.obtenerDiferenciaEnDias = function(fechaInicio, fechaTermino) {
		var diferenciaEnDias = 0;
		if (!!fechaInicio) {
            // si omiten fecha hasta busca la fecha del sistema		
			if (fechaTermino===null || fechaTermino===undefined) {
				fechaTermino = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}	
			var diferenciaEnMilisegundos = Math.abs(fechaTermino.getTime() - fechaInicio.getTime());
		    diferenciaEnDias = Math.ceil(diferenciaEnMilisegundos/(1000 * 3600 * 24));
		}
		return diferenciaEnDias;
	}	

	$scope.formData.buscar = function() {
        var diferenciaEnDias = $scope.formData.obtenerDiferenciaEnDias($scope.formData.busqueda.fechaCreacionDesde, $scope.formData.busqueda.fechaCreacionHasta)
		if (diferenciaEnDias>31) {
			bcsComunes.ventanaMsgErr($translate.instant("UI_FECHA_CREACION_RANGO_EXCEDE_31_DIAS"), $scope.idVentana);
			return;			
		} else {
			var datos = parsearFiltros();
			leDs.setCallParameters(datos);
		}
	};
	
	var fieldsChile = {
						TipoDocumentoIdentidad	: { type : "string" },
						DocumentoIdentidad 		: { type : "string" },
						NombreCompleto 			: { type : "string" },
						FechaNacimiento			: { type : "sysdate" },
						FechaCreacion			: { type : "sysdate" },
						FechaDefuncion			: { type : "sysdate" },
						FechaUltimaModificacion	: { type : "sysdate" },
						FechaUltimaOperacion	: { type : "sysdate" },
						NombreTipoPersona 		: { type : "string" },
						DescripcionSegmentoCliente 		: { type : "string" },
						CodigoEmpleadoEjecutivo 		: { type : "string" },
						NombreEmpleadoEjecutivo 		: { type : "string" },
						NombreTipoCliente 			: { type : "string" },
						NombreCategoriaInversionista 	: { type : "string" },
						NombreEstadoCliente 	: { type : "string" },
						NombreTipoBloqueoCliente 	: { type : "string" },
						NombreTipoBloqueoPortafolio 	: { type : "string" },
						NombreEstadoPortafolio 	: { type : "string" },
						CodigoPortafolio 					: { type : "string" },
						ClienteFinal    					: { type : "string" },
						NombreCompletoBroker                : { type : "string" },
						IdPortafolio						: { type : "number" },
						IdCliente							: { type : "number" },
						CodigoEmpleadoEjecutivoAnterior 	: { type : "string" },
						NombreEmpleadoEjecutivoAnterior 	: { type : "string" },
						PropositoPortafolio 	: { type : "string" },
						DescripcionPortafolio 		: { type : "string" },
						EstadoPortafolio 		: { type : "string" },
						IdPerfilComercial 		: { type : "string" },
						IdPerfilProductos 		: { type : "string" },
						IdPerfilCobros 		: { type : "string" },
						IdPerfilLimites 		: { type : "string" },

						IdMonedaFlujoCaja 		: { type : "string" },
						
						IdMonedaInstrumento 		: { type : "string" },
						IdMonedaFlujoCaja: { type : "string" },
						TipoEnrolamiento 		: { type : "string" },
						ContratosFirmados : { type : "string"},
						AreaCliente : {type: "string"},
						IdTipoCanalIngresoCorredor : {type: "string"}
					};

	var fieldsPeru = {
						TipoDocumentoIdentidad	: { type : "string" },
						DocumentoIdentidad 		: { type : "string" },
						DocumentoIdentidadRutCavali : { type : "string" },
						NombreCompleto 			: { type : "string" },
						FechaNacimiento			: { type : "sysdate" },
						FechaCreacion			: { type : "sysdate" },
						FechaDefuncion			: { type : "sysdate" },
						NombreTipoPersona 		: { type : "string" },
						DescripcionSegmentoCliente 		: { type : "string" },
						CodigoEmpleadoEjecutivo 		: { type : "string" },
						NombreEmpleadoEjecutivo 		: { type : "string" },
						NombreTipoCliente 			: { type : "string" },
						NombreCategoriaInversionista 	: { type : "string" },
						NombreEstadoCliente 	: { type : "string" },
						NombreTipoBloqueoCliente 	: { type : "string" },
						NombreTipoBloqueoPortafolio 	: { type : "string" },
						NombreEstadoPortafolio 	: { type : "string" },
						CodigoPortafolio 					: { type : "string" },
						ClienteFinal    					: { type : "string" },
						NombreCompletoBroker                : { type : "string" },
						IdPortafolio						: { type : "number" },
						IdCliente							: { type : "number" },
						CodigoEmpleadoEjecutivoAnterior 	: { type : "string" },
						NombreEmpleadoEjecutivoAnterior 	: { type : "string" },
						PropositoPortafolio 	: { type : "string" },
						DescripcionPortafolio 		: { type : "string" },
						EstadoPortafolio 		: { type : "string" },
						IdPerfilComercial 		: { type : "string" },
						IdPerfilProductos 		: { type : "string" },
						IdPerfilCobros 		: { type : "string" },
						IdPerfilLimites 		: { type : "string" },

						IdMonedaFlujoCaja 		: { type : "string" },
						
						IdMonedaInstrumento 		: { type : "string" },
						IdMonedaFlujoCaja: { type : "string" },
						TipoEnrolamiento 		: { type : "string" },
						ContratosFirmados : { type : "string"},
						IdTipoCanalIngresoCorredor : {type: "string"}
					};

	var columnsChile = [
  			          { field: "TipoDocumentoIdentidad",	title: $translate.instant('UI_TIPO_DOCUMENTO_IDENTIDAD'),width: "100px", locked:true, hidden:($scope.SesionLocale==="CL")},
  			          { field: "DocumentoIdentidad",	title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),width: "100px", locked:true},
  			          { field: "NombreCompleto",		title: $translate.instant('UI_NOMBRE_COMPLETO'),width: "200px", locked:true},
  			          { field: "CodigoPortafolio",		title: $translate.instant('UI_CODIGO_PORTAFOLIO'),width: "100px", locked:true},
  			          { field: "ClienteFinal",   		title: $translate.instant('UI_CLIENTE_FINAL'),width: "70px", hidden:($scope.SesionLocale==="CL")},
  			          { field: "NombreCompletoBroker",	title: $translate.instant('UI_BROKER'),width: "180px", hidden:($scope.SesionLocale==="CL")},
  			          { field: "FechaNacimiento",	    title: $translate.instant('UI_FECHA_NACIMIENTO'),width: "100px"},
			          { field: "FechaCreacion",	        title: $translate.instant('UI_FECHA_CREACION'),width: "100px"},
			          { field: "FechaDefuncion",	        title: $translate.instant('UI_FECHA_DEFUNCION'),width: "100px"},
  			          { field: "NombreTipoPersona",			title: $translate.instant('UI_TIPO_PERSONA'),width: "100px"},
  			          { field: "DescripcionSegmentoCliente",		title: $translate.instant('UI_SEGMENTO'),width: "150px"},
  			          { field: "CodigoEmpleadoEjecutivo",	    title: $translate.instant('UI_CODIGO_EJECUTIVO'),width: "100px"},
  			          { field: "NombreEmpleadoEjecutivo",	    title: $translate.instant('UI_NOMBRE_EJECUTIVO'),width: "100px"},
  			          { field: "NombreTipoCliente",			title: $translate.instant('UI_TIPO_CLIENTE'),width: "100px"},
  			          { field: "NombreCategoriaInversionista",title: $translate.instant('UI_CATEGORIA_INVERSIONISTA'),width: "100px"},
  			          { field: "NombreEstadoCliente",	    title: $translate.instant('UI_ESTADO_CLIENTE'),width: "100px"},
			          { field: "NombreTipoBloqueoCliente",	        title: $translate.instant('UI_TIPO_BLOQUEO_CLIENTE'),width: "150px"},
			          { field: "NombreTipoBloqueoPortafolio",	        title: $translate.instant('UI_TIPO_BLOQUEO_PORTAFOLIO'),width: "150px"},
  			          { field: "PropositoPortafolio",	title: $translate.instant('UI_PROPOSITO_PORTAFOLIO'),width: "100px"},
  			          { field: "DescripcionPortafolio",	title: $translate.instant('UI_DESCRIPCION_PORTAFOLIO'),width: "100px"},
  			          { field: "NombreEstadoPortafolio",		title: $translate.instant('UI_ESTADO_PORTAFOLIO'),width: "100px"},
					  { field: "TipoEnrolamiento",	    title: $translate.instant('UI_TIPO_ENROLAMIENTO'),width: "150px"},
  			          { field: "IdPerfilComercial",		title: $translate.instant('UI_MODELO_COMERCIAL'),width: "200px"},
  			          { field: "IdPerfilProductos",		title: $translate.instant('UI_PERFIL_PRODUCTOS'),width: "200px"},
  			          { field: "IdPerfilCobros",		title: $translate.instant('UI_PERFIL_COBROS'),width: "200px"},
  			          { field: "IdPerfilLimites",		title: $translate.instant('UI_PERFIL_LIMITES'),width: "200px"},
	  			  	  { field: "IdMonedaFlujoCaja",	    title: $translate.instant('UI_MONEDA_SALDO_FLUJO_CAJA'),width: "150px"},
	  			 	  { field: "IdTipoCanalIngresoCorredor",		title: $translate.instant('UI_CANAL_INGRESO'),width: "200px"},
			];

	var columnsPeru = [
  			          { field: "TipoDocumentoIdentidad",	title: $translate.instant('UI_TIPO_DOCUMENTO_IDENTIDAD'),width: "100px", locked:true, hidden:($scope.SesionLocale==="CL")},
  			          { field: "DocumentoIdentidad",	title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),width: "100px", locked:true},
  			          { field: "DocumentoIdentidadRutCavali",	title: $translate.instant('UI_RUT_CAVALI'),width: "100px", locked:true},
  			          { field: "NombreCompleto",		title: $translate.instant('UI_NOMBRE_COMPLETO'),width: "200px", locked:true},
  			          { field: "CodigoPortafolio",		title: $translate.instant('UI_CODIGO_PORTAFOLIO'),width: "100px", locked:true},
  			          { field: "ClienteFinal",   		title: $translate.instant('UI_CLIENTE_FINAL'),width: "70px", hidden:($scope.SesionLocale==="CL")},
  			          { field: "NombreCompletoBroker",	title: $translate.instant('UI_BROKER'),width: "180px", hidden:($scope.SesionLocale==="CL")},
  			          { field: "FechaNacimiento",	    title: $translate.instant('UI_FECHA_NACIMIENTO'),width: "100px"},
			          { field: "FechaCreacion",	        title: $translate.instant('UI_FECHA_CREACION'),width: "100px"},
			          { field: "FechaDefuncion",	        title: $translate.instant('UI_FECHA_DEFUNCION'),width: "100px"},
  			          { field: "NombreTipoPersona",			title: $translate.instant('UI_TIPO_PERSONA'),width: "100px"},
  			          { field: "DescripcionSegmentoCliente",		title: $translate.instant('UI_SEGMENTO'),width: "150px"},
  			          { field: "CodigoEmpleadoEjecutivo",	    title: $translate.instant('UI_CODIGO_EJECUTIVO'),width: "100px"},
  			          { field: "NombreEmpleadoEjecutivo",	    title: $translate.instant('UI_NOMBRE_EJECUTIVO'),width: "100px"},
  			          { field: "NombreTipoCliente",			title: $translate.instant('UI_TIPO_CLIENTE'),width: "100px"},
  			          { field: "NombreCategoriaInversionista",title: $translate.instant('UI_CATEGORIA_INVERSIONISTA'),width: "100px"},
  			          { field: "NombreEstadoCliente",	    title: $translate.instant('UI_ESTADO_CLIENTE'),width: "100px"},
			          { field: "NombreTipoBloqueoCliente",	        title: $translate.instant('UI_TIPO_BLOQUEO_CLIENTE'),width: "150px"},
			          { field: "NombreTipoBloqueoPortafolio",	        title: $translate.instant('UI_TIPO_BLOQUEO_PORTAFOLIO'),width: "150px"},
  			          { field: "PropositoPortafolio",	title: $translate.instant('UI_PROPOSITO_PORTAFOLIO'),width: "100px"},
  			          { field: "DescripcionPortafolio",	title: $translate.instant('UI_DESCRIPCION_PORTAFOLIO'),width: "100px"},
  			          { field: "NombreEstadoPortafolio",		title: $translate.instant('UI_ESTADO_PORTAFOLIO'),width: "100px"},
					  { field: "TipoEnrolamiento",	    title: $translate.instant('UI_TIPO_ENROLAMIENTO'),width: "150px"},
  			          { field: "IdPerfilComercial",		title: $translate.instant('UI_MODELO_COMERCIAL'),width: "200px"},
  			          { field: "IdPerfilProductos",		title: $translate.instant('UI_PERFIL_PRODUCTOS'),width: "200px"},
  			          { field: "IdPerfilCobros",		title: $translate.instant('UI_PERFIL_COBROS'),width: "200px"},
  			          { field: "IdPerfilLimites",		title: $translate.instant('UI_PERFIL_LIMITES'),width: "200px"},
	  			  	  { field: "IdMonedaFlujoCaja",	    title: $translate.instant('UI_MONEDA_SALDO_FLUJO_CAJA'),width: "150px"},
	  			  	 { field: "IdTipoCanalIngresoCorredor",		title: $translate.instant('UI_CANAL_INGRESO'),width: "200px"},
			];

	leDs = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize 			: 200,	
			callParameters      : filters,
			callTransaction 	: "1479",  // PLUGIN_ORD.jar@cl.bcs.ordenes.ConsultarAsignaciones
			callResultSet 		: "P_OUT_CLIENTES",
			schema : {
				model : {
					fields : $scope.SesionLocale === "CL" ? fieldsChile : fieldsPeru
				}
			}
		},
	});
	
	$scope.confClientes = {
			dataSource 	: leDs,
			columns 	:  [
			          		{ field: "TipoDocumentoIdentidad",	title: $translate.instant('UI_TIPO_DOCUMENTO_IDENTIDAD'),width: "100px", locked:true, hidden:($scope.SesionLocale==="CL")},
			          		{ field: "DocumentoIdentidad",	title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),width: "100px", locked:true},
							{ field: "NombreCompleto",		title: $translate.instant('UI_NOMBRE_COMPLETO'),width: "200px", locked:true},
							{ field: "CodigoPortafolio",		title: $translate.instant('UI_CODIGO_PORTAFOLIO'),width: "100px", locked:true},
							{ field: "ClienteFinal",   		title: $translate.instant('UI_CLIENTE_FINAL'),width: "70px", hidden:($scope.SesionLocale==="CL")},
							{ field: "NombreCompletoBroker",	title: $translate.instant('UI_BROKER'),width: "180px", hidden:($scope.SesionLocale==="CL")},
							{ field: "FechaNacimiento",	    title: $translate.instant('UI_FECHA_NACIMIENTO'),width: "100px"},
					        { field: "FechaCreacion",	        title: $translate.instant('UI_FECHA_CREACION'),width: "100px"},
					        { field: "FechaDefuncion",	        title: $translate.instant('UI_FECHA_DEFUNCION'),width: "100px"},
					        { field: "FechaUltimaModificacion",	title: $translate.instant('UI_FECHA_ULTIMA_MODIFICACION'),width: "100px"},
					        { field: "FechaUltimaOperacion",	title: $translate.instant('UI_FECHA_ULTIMA_OPERACION'),width: "100px"},
					        { field: "NombreTipoPersona",			title: $translate.instant('UI_TIPO_PERSONA'),width: "100px"},
					        { field: "DescripcionSegmentoCliente",		title: $translate.instant('UI_SEGMENTO'),width: "150px"},
					        { field: "CodigoEmpleadoEjecutivo",	    title: $translate.instant('UI_CODIGO_EJECUTIVO'),width: "100px"},
					        { field: "NombreEmpleadoEjecutivo",	    title: $translate.instant('UI_NOMBRE_EJECUTIVO'),width: "100px"},
					        { field: "NombreTipoCliente",			title: $translate.instant('UI_TIPO_CLIENTE'),width: "100px"},
					        { field: "NombreCategoriaInversionista",title: $translate.instant('UI_CATEGORIA_INVERSIONISTA'),width: "100px"},
					        { field: "NombreEstadoCliente",	    title: $translate.instant('UI_ESTADO_CLIENTE'),width: "100px"},
					        { field: "NombreTipoBloqueoCliente",	        title: $translate.instant('UI_TIPO_BLOQUEO_CLIENTE'),width: "150px"},
					        { field: "NombreTipoBloqueoPortafolio",	        title: $translate.instant('UI_TIPO_BLOQUEO_PORTAFOLIO'),width: "150px"},
							{ field: "ContratosFirmados",	    title: $translate.instant('UI_CONTRATOS_FIRMADOS'),width: "100px"},
					        { field: "PropositoPortafolio",	title: $translate.instant('UI_PROPOSITO_PORTAFOLIO'),width: "100px"},
					        { field: "DescripcionPortafolio",	title: $translate.instant('UI_DESCRIPCION_PORTAFOLIO'),width: "100px"},
					        { field: "NombreEstadoPortafolio",		title: $translate.instant('UI_ESTADO_PORTAFOLIO'),width: "100px"},
							{ field: "TipoEnrolamiento",	    title: $translate.instant('UI_TIPO_ENROLAMIENTO'),width: "150px"},
					        { field: "IdPerfilComercial",		title: $translate.instant('UI_MODELO_COMERCIAL'),width: "200px"},
					        { field: "IdPerfilProductos",		title: $translate.instant('UI_PERFIL_PRODUCTOS'),width: "200px"},
							{ field: "AreaCliente", 		title: $translate.instant('UI_AREA_CLIENTE' ), width: "100px"},
					        { field: "IdPerfilCobros",		title: $translate.instant('UI_PERFIL_COBROS'),width: "200px"},
					        { field: "IdPerfilLimites",		title: $translate.instant('UI_PERFIL_LIMITES'),width: "200px"},
					        { field: "IdMonedaFlujoCaja",	    title: $translate.instant('UI_MONEDA_SALDO_FLUJO_CAJA'),width: "150px"},
					        { field: "IdTipoCanalIngresoCorredor",		title: $translate.instant('UI_CANAL_INGRESO'),width: "200px"},
					        ],
		    allowCopy	: true,					        
			sortable 	: true,
			pageable 	: true,
			filterable	: true,
			navigatable : true,
			userSettings: true,
			height		: 420,
			excel		: {allPages: true},
			scrollable 	: true,
			selectable	: "multiple, row",
			autoBind	: false
	}

//	$scope.formData.gridConfiguration = {
//			allowCopy	: true,
//    		autoBind	: false,
//    		excel 		: {allPages: true,},
//      		filterable	: true,
//    		groupable	: true,
//    		height		: 420,
//    		pageable	: true,
//    		reorderable	: true,
//    		resizable	: true,
//      		selectable	: "multiple, row",
//    		sortable	: true,
//    		userSettings: true,
//    		navigatable	: true,
//    		dataSource: leDs,
//    		columns: $scope.SesionLocale === "CL" ? columnsChile : columnsPeru,
//
//    		gridCreated : function(grid) {
//    			$scope.formData.mainGrid = grid;
//				$scope.toggleColumns("CLIENTE");
//    		}  
//
//	}
	
	var call = $resource("", {}, {  'generarReporteConsultaAvanzadaClientes' :  { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"  }});

	
	
	
	
/*	
	$scope.formData.toExcel = function(){
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var datasFiltros = parsearFiltros();
		
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'SOC',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'GenerarReporteConsultaAvanzadaCliente',
					SegundosDesfase : 0
				}],
				p_Parametro : []
		}
		$.each(datasFiltros,function(key,value){
			datos.p_Parametro.push({
				IdInterno : 0,
				NombreParametro : key,
				Valor : value
			})
		});
		call.generarReporteConsultaAvanzadaClientes(datos).$promise.then(
		function(result) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
			bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
				conflictos : util.parseServerResponse(result)}, $scope.idVentana);

		},
		function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});
		bcsComunes.ventanaOverlay($scope.idVentana,false);
	}
*/
	$scope.fnTabConsultaAvanzadaClientesCliente = function(){
		$scope.toggleColumns("CLIENTE");
	}
	$scope.fnTabConsultaAvanzadaClientesPortafolio = function(){
		$scope.toggleColumns("PORTAFOLIO");
	}
	$scope.fnTabConsultaAvanzadaClientesInversiones = function(){
		$scope.toggleColumns("INVERSION");
	}

	$scope.toggleColumns = function(tab){
		if(!!$scope.formData.mainGrid){
			if(tab==="INVERSION"){
				$scope.formData.mainGrid.showColumn("IdMonedaFlujoCaja");
				
			}else{
				$scope.formData.mainGrid.hideColumn("IdMonedaFlujoCaja");
				
			}
			$scope.formData.mainGrid.refresh();
		}
	}
	
	
  	
});
	
	
	 
	
	
	
	
	
	

