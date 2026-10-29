(function() {
	angular.module('ModuloAPV').controller('IngresoDetalleCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV,bcsAlerta,OPCIONES_BCSALERTA,$q)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var IdEmpleadorSeleccionado = 0;
		var IdEstadoEmpleador = "";
		var IdClienteSeleccionado = 0;
		var IdEstadoClienteSeleccionado = "";
		var IdRecaudacionEmpleador = 0;
		var primeraCarga = 'SI';
		
		var ingDet = this;
		ingDet.EsRegimenAPV = false;
		ingDet.EsRegimenDC = false;
		ingDet.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		ingDet.recaudacionDefecto= ingDet.parametrosEntrada.idRecaudacion;
		ingDet.folioDeposito = ingDet.parametrosEntrada.idRecaudacion;
		ingDet.parametrosEntrada.modoVentana = "I";
		ingDet.accionEmpleador = "I";
		ingDet.accionCliente = "I";
		$scope.data = {};
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		ingDet.PerPagoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		$scope.fechaActual = new Date();
		ingDet.ConvenidoAcumuladoCli = 0;
		ingDet.APVAcumuladoCli = 0;
		ingDet.NumeroTrabajadores = 0;
		ingDet.nHojas = 0;
		ingDet.depositosConvenidosEmp = 0;
		ingDet.depositoAPVEmp = 0;
		ingDet.reajuste = 0;
		ingDet.intereces = 0;
		ingDet.kngDepositosConvenidosCli = 0;
		ingDet.kngDepositosAPVCli = 0;

		ingDet.oConfiguracionTipoDeposito={
				tipo : "combobox-medio-pago-tesoreria",
				configuracion : {},
				filtros : {
					IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : "CLP" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};	
		
		ingDet.oConfiguracionSucursalBancaria={
				tipo : "combobox-sucursal-bancaria",
				configuracion : {},
				filtros : {
					IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : "" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		ingDet.oConfiguracionCuentaCorriente={
				tipo : "combobox-numero-cuentas-bancarias-corredora",
				configuracion : {},
				filtros : {
					IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : "" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		ingDet.filtroRegimen = {
				IdEstado : {field: "IdEstado", operator: "eq", value: "VIG"},
				IdEstado : {field: "EstadoRecaudacion", operator: "eq", value: "VIG"}
		}
		
		ingDet.oConfiguracionfolioDeposito= {
			filtros : {
				IdRecaudacion : { field   : "IdRecaudacion",operator: "eq", value : ingDet.folioDeposito }
			},
			idDefecto: ingDet.recaudacionDefecto,
			idActual:ingDet.folioDeposito
		};	
		
		ingDet.fnLimpiarEmpleador = function(quitarSeleccion, filtrarCliente)
		{ 
			ingDet.kngPeriodoPago					= undefined;
			ingDet.idPersona						= false;
			ingDet.idPersonaPostFiltro				= undefined;
			ingDet.nHojas 							= 0;
			ingDet.kngNHojas 						= 0;
			ingDet.tipoPago							= false;
			ingDet.tipoPagoPostFiltro				= undefined;
			ingDet.nTrabajadores					= undefined;
			ingDet.kngNTrabajadores					= undefined;
			ingDet.depositosConvenidosEmp			= 0;
			ingDet.kngDepositosConvenidosEmp		= 0;
			ingDet.reajuste 						= 0;
			ingDet.kngReajuste 						= 0;
			ingDet.depositoAPVEmp					= 0;
			ingDet.kngDepositoAPVEmp				= 0;
			ingDet.intereces						= 0;
			ingDet.kngIntereces						= 0;
			ingDet.subtotalEmpleador				= undefined;
			ingDet.kngSubtotalEmpleador				= undefined;
			ingDet.totalEmpleador					= undefined;
			ingDet.kngTotalEmpleador				= undefined;
			ingDet.kngDiferenciaSubtotalEmpleador	= undefined;
			ingDet.kngDiferenciaTotalEmpleador 		= undefined;
			//ingDet.parametrosEntrada.modoVentana = "I";
			ingDet.accionEmpleador = "I";
			
			if(filtrarCliente === true){
				var filter = Array(); 
				filter.push({ field: "IdRecaudacionEmpleador", operator: "eq", value: 0 });
				dsDatosCliente.filter(filter);
			}
			
			if(quitarSeleccion === 'SI'){
				$('#gridEmpleador tr').removeClass("k-state-selected");
				ingDet.seleccionEmpleador = undefined;
			}
		};
		
		ingDet.fnLimpiarCliente = function(quitarSeleccion)
		{
			
			//Limpia sector EMPLEADOR 
			ingDet.kngPeriodoPago					= undefined;
			ingDet.idPersona						= false;
			ingDet.idPersonaPostFiltro				= undefined;
			ingDet.nHojas 							= 0;
			ingDet.kngNHojas 						= 0;
			ingDet.tipoPago							= false;
			ingDet.tipoPagoPostFiltro				= undefined;
			ingDet.nTrabajadores					= undefined;
			ingDet.kngNTrabajadores					= undefined;
			ingDet.depositosConvenidosEmp			= 0;
			ingDet.kngDepositosConvenidosEmp		= 0;
			ingDet.reajuste 						= 0;
			ingDet.kngReajuste 						= 0;
			ingDet.depositoAPVEmp					= 0;
			ingDet.kngDepositoAPVEmp				= 0;
			ingDet.intereces						= 0;
			ingDet.kngIntereces						= 0;
			ingDet.subtotalEmpleador				= undefined;
			ingDet.kngSubtotalEmpleador				= undefined;
			ingDet.totalEmpleador					= undefined;
			ingDet.kngTotalEmpleador				= undefined;
			ingDet.kngDiferenciaSubtotalEmpleador	= undefined;
			ingDet.kngDiferenciaTotalEmpleador 		= undefined;
			//ingDet.parametrosEntrada.modoVentana = "I";
			ingDet.accionEmpleador = "I";
			//Limpia sector CLIENTE
			ingDet.idCliente							= false;
			ingDet.idClientePostFiltro					= undefined;
			ingDet.tipoAfiliado							= false;
			ingDet.tipoAfiliadoPostFiltro				= undefined;
			ingDet.IdRegimen							= false;
			ingDet.IdRegimenPostFiltro					= undefined;
			ingDet.depositosConvenidosCli				= undefined;
			ingDet.kngDepositosConvenidosCli			= undefined;
			ingDet.depositosConvenidosCalculadoCli 		= undefined;
			ingDet.kngDepositosConvenidosCalculadoCli	= undefined;
			ingDet.depositosAPVCli						= undefined;
			ingDet.kngDepositosAPVCli					= undefined;
			ingDet.depositosAPVCalculadoCli				= undefined;
			ingDet.kngDepositosAPVCalculadoCli			= undefined;
			ingDet.totalCli								= undefined;
			ingDet.kngTotalCli							= undefined;
			ingDet.totalCalculadoCli					= undefined;
			ingDet.kngTotalCalculadoCli					= undefined;
			ingDet.kngDepositosConvenidosCli 			= 0;
			ingDet.kngDepositosAPVCli 					= 0;
			//ingDet.parametrosEntrada.modoVentana 		= "I";
			ingDet.accionCliente = "I";
			
			if(quitarSeleccion === 'SI'){
				$('#gridCliente tr').removeClass("k-state-selected");
				ingDet.seleccionCliente = undefined;

				ingDet.folioDeposito						= undefined;

				ingDet.resumen								= 0;
				ingDet.calculado							= 0;
				ingDet.digitado								= 0;
				ingDet.diferenciaEmpleador					= 0;
				ingDet.diferenciaCliente					= 0;

				dsDatosEmpleador.removeAll();
				dsDatosCliente.removeAll();
			}
		};
				
		ingDet.oConfiguracionRegimenTributario={
				tipo : "combobox-regimen-tributario",
				configuracion : {},
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		//Calculos Empleador.
		
		ingDet.fnCalculosEmpleador = function()
		{
			ingDet.fnSubTotalDiferencia();
			ingDet.fnTotalDiferenciaEmpleador();
			ingDet.fnSubTotalEmpleador();
			ingDet.fnTotalEmpleador();
		}
		
		ingDet.fnSubTotalEmpleador = function()
		{
			var subTotal = ((ingDet.kngDepositosConvenidosEmp === undefined ? 0 : ingDet.kngDepositosConvenidosEmp) + (ingDet.kngDepositoAPVEmp === undefined ? 0 : ingDet.kngDepositoAPVEmp));
			
			ingDet.subtotalEmpleador = subTotal === 0 ? undefined : subTotal;
		};
		
		ingDet.fnTotalEmpleador = function()
		{
			var subTotal = ((ingDet.kngDepositosConvenidosEmp === undefined ? 0 : ingDet.kngDepositosConvenidosEmp) + (ingDet.kngDepositoAPVEmp === undefined ? 0 : ingDet.kngDepositoAPVEmp));
			
			var total = (ingDet.kngReajuste === undefined ? 0 : ingDet.kngReajuste) + (ingDet.kngIntereces === undefined ? 0 : ingDet.kngIntereces) + subTotal; 

			ingDet.totalEmpleador =  total === 0 ? undefined : total;
		};
		
		ingDet.fnSubTotalDiferencia = function()
		{
			var subTotal = ((ingDet.kngDepositosConvenidosEmp === undefined ? 0 : ingDet.kngDepositosConvenidosEmp) + (ingDet.kngDepositoAPVEmp === undefined ? 0 : ingDet.kngDepositoAPVEmp));

			var diferencia = (ingDet.depositoSeleccionado === undefined ? 0 : ingDet.depositoSeleccionado.MontoDeposito - (ingDet.calculado === undefined ? 0 : ingDet.calculado)) - subTotal;
							
			//return diferencia === 0 ? undefined : (diferencia > 0 ? diferencia : 0);

			ingDet.diferenciaSubTotalEmpleador =  ingDet.depositoSeleccionado === undefined ? undefined : diferencia;
		};
		
		ingDet.fnTotalDiferenciaEmpleador = function()
		{
			var subTotal = ((ingDet.kngDepositosConvenidosEmp === undefined ? 0 : ingDet.kngDepositosConvenidosEmp) + (ingDet.kngDepositoAPVEmp === undefined ? 0 : ingDet.kngDepositoAPVEmp));

			var total = (ingDet.kngReajuste === undefined ? 0 : ingDet.kngReajuste) + (ingDet.kngIntereces === undefined ? 0 : ingDet.kngIntereces) + subTotal; 

			var diferencia = (ingDet.depositoSeleccionado === undefined ? 0 : ingDet.depositoSeleccionado.MontoDeposito - (ingDet.calculado === undefined ? 0 : ingDet.calculado)) - total;
							
			ingDet.diferenciaTotalEmpleador = ingDet.depositoSeleccionado === undefined ? undefined : diferencia;
		};
		

		//Calculos Cliente.
		
		ingDet.fnSubTotalCliente = function()
		{
			var valor = ((ingDet.kngDepositosConvenidosCli === undefined ? 0 : ingDet.kngDepositosConvenidosCli) + (ingDet.kngDepositosAPVCli === undefined ? 0 : ingDet.kngDepositosAPVCli));
			
			return valor === 0 ? undefined : valor;
		};
		
		ingDet.fnConvenidoCalculadoCli = function()
		{
			var valor = ingDet.ConvenidoAcumuladoCli + (ingDet.kngDepositosConvenidosCli === undefined ? 0 : ingDet.kngDepositosConvenidosCli);
			
			return valor === 0 ? undefined : valor;
		};
		
		ingDet.fnAPVCalculadoCli = function()
		{
			var valor = ingDet.APVAcumuladoCli + (ingDet.kngDepositosAPVCli === undefined ? 0 : ingDet.kngDepositosAPVCli);
			
			return valor === 0 ? undefined : valor;
		};
		
		ingDet.fnTotalCalculadoCli = function()
		{
			var valor = (ingDet.fnConvenidoCalculadoCli() === undefined ? 0 : ingDet.fnConvenidoCalculadoCli()) + (ingDet.fnAPVCalculadoCli() === undefined ? 0 : ingDet.fnAPVCalculadoCli());
			
			return valor === 0 ? undefined : valor;
		};
		
		
		//Calculos Resumen.
		
		ingDet.fnTotalDiferenciaResumenEmpleador = function()
		{
			var subTotal = ((ingDet.kngDepositosConvenidosEmp === undefined ? 0 : ingDet.kngDepositosConvenidosEmp) + (ingDet.kngDepositoAPVEmp === undefined ? 0 : ingDet.kngDepositoAPVEmp));
			
			var total = (ingDet.kngReajuste === undefined ? 0 : ingDet.kngReajuste) + (ingDet.kngIntereces === undefined ? 0 : ingDet.kngIntereces) + subTotal; 
				
			var diferencia = (ingDet.depositoSeleccionado === undefined ? 0 : ingDet.depositoSeleccionado.MontoDeposito) - total;

			return diferencia === 0 ? undefined : diferencia;
		};
		
		ingDet.fnTotalesResumen = function(){

			var totalEmpleador = 0;
			var totalCliente = 0;
			
			const promises = [];
			promises.push(dsDatosEmpleador.read());
			promises.push(dsDatosCliente.read());
			$q.all(promises).then(function () {
				for (i = 0; i < dsDatosEmpleador.data().length; i++) { 
					totalEmpleador = totalEmpleador + dsDatosEmpleador.data()[i].TotalResumen;
				}
				
				for (i = 0; i < dsDatosCliente.data().length; i++) { 
					totalCliente = totalCliente + dsDatosCliente.data()[i].TotalDigitado;
				}
				
				ingDet.resumen 				= ingDet.depositoSeleccionado.MontoDeposito;
				ingDet.calculado 			= totalEmpleador;
				ingDet.digitado 			= totalCliente;
				ingDet.diferenciaEmpleador	= ingDet.resumen - totalEmpleador;
				ingDet.diferenciaCliente	= ingDet.resumen - totalCliente;
			})

			
		};
		//********
		
		ingDet.fnValidaEmpleador = function()
		{ 
			var valido = false;
		
			if(
				ingDet.folioDeposito					=== false ||
				ingDet.folioDeposito					=== undefined ||
				ingDet.kngPeriodoPago					=== undefined ||
				ingDet.idPersona						=== false ||
				ingDet.idPersona						=== undefined ||
				ingDet.tipoPago							=== false ||
				ingDet.tipoPago							=== undefined 
			){
				valido = false;
			}else{
				if(ingDet.depositosConvenidosEmp !== undefined && ingDet.depositosConvenidosEmp !== null && ingDet.depositosConvenidosEmp > 0)
					valido = true;
				else if(ingDet.depositoAPVEmp !== undefined && ingDet.depositoAPVEmp !== null && ingDet.depositoAPVEmp > 0)
					valido = true;
				else
					valido = false;
			}
			return valido;
		};
		
		ingDet.fnValidaCliente = function()
		{ 
			var valido = false;
		
			if(
				ingDet.folioDeposito					=== false ||
				ingDet.folioDeposito					=== undefined ||
				ingDet.idCliente						=== false 		||
				ingDet.idCliente						=== undefined 	||
				ingDet.tipoAfiliado						=== false 		||
				ingDet.tipoAfiliado						=== undefined 	||
				ingDet.IdRegimen						=== false 		||
				ingDet.IdRegimen						=== undefined
			){
				valido = false;
			}else{
				if(ingDet.depositosConvenidosCli !== undefined && ingDet.depositosConvenidosCli !== null && ingDet.depositosConvenidosCli !== "" && ingDet.depositosConvenidosCli !== '')
					valido = true;
				else if(ingDet.depositosAPVCli !== undefined && ingDet.depositosAPVCli !== null && ingDet.depositosAPVCli !== "" && ingDet.depositosAPVCli !== '')
					valido = true;
				else
					valido = false;
			}
			
			return valido;
		};		
			
		ingDet.fnGrabarEmpleador = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
		
			if(ingDet.accionEmpleador === "M"){
				if(IdEstadoEmpleador === "DIS" || IdEstadoEmpleador === "DEV"){
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_DETALLE_EMPLEADOR_TIENE_REGISTROS_DIS_DEV')},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			}
			
			if(dsDatosCliente.data().length > 0 && dsDatosEmpleador.data().length === 0){
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_DEPOSITO_SOLO_PARA_CLIENTES')},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			
			var reajuste = 0;
			var intereses = 0;
			var DCEmp = 0;
			var APVEmp = 0;
			var nHojas = 0;
			
			if(ingDet.depositosConvenidosEmp !== undefined && ingDet.depositosConvenidosEmp !== null && ingDet.depositosConvenidosEmp !== "" && ingDet.depositosConvenidosEmp !== '')
				DCEmp = ingDet.depositosConvenidosEmp;
			
			if(ingDet.depositoAPVEmp !== undefined && ingDet.depositoAPVEmp !== null && ingDet.depositoAPVEmp !== "" && ingDet.depositoAPVEmp !== '')
				APVEmp = ingDet.depositoAPVEmp;
			
			if(ingDet.reajuste !== undefined && ingDet.reajuste !== null && ingDet.reajuste !== "" && ingDet.reajuste !== '')
				reajuste = ingDet.reajuste;
			
			if(ingDet.intereces !== undefined && ingDet.intereces !== null && ingDet.intereces !== "" && ingDet.intereces !== '')
				intereses = ingDet.intereces;
				if(ingDet.nHojas !== undefined && ingDet.nHojas != null && ingDet.nHojas !== "" && ingDet.nHojas !== '')
				nHojas = ingDet.nHojas;
			
			bcsComunes.ventanaOverlay(idVentana,true);
			var parametros = {				
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : "APV",
				p_Tipo: ingDet.accionEmpleador === "M" ? "M" : "I",  ///Individual Manual o Por Servicio
				p_Origen: "MANUAL",
				p_Parametros : [{
					IdRecaudacionEmpleador	: ingDet.accionEmpleador === "M"? IdEmpleadorSeleccionado : 0,
					IdRecaudacion 			: ingDet.depositoSeleccionado.IdRecaudacion,
					IdEmpresa				: $scope.SesionIdEmpresa,
					IdPersona				: ingDet.empleadorSeleccionado.IdPersona,
					FechaIngreso			: $scope.SesionFechaSistema,
					PeriodoPago				: ingDet.kngPeriodoPago === undefined ? "1900-01" : ingDet.kngPeriodoPago.toISOString().slice(0,7),
					NumeroHojas				: nHojas,
					TipoAfiliado			: "",
					TipoPago				: ingDet.tipoPagoSeleccionado.Codigo,
					Insistencias			: "",
					MontoDepositoConvenido 	: DCEmp,
					MontoDepositoAPV		: APVEmp,
					Reajustes				: reajuste,
					Intereses				: intereses,
					Origen					: "Manual",
					Observaciones			: "",
					IdEstado				: "ING"
				}],
			};
			
			
			bcsServiciosAPV.CrearRecaudacionEmpleador(parametros).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					//bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
					
					dsDatosEmpleador.read().then(function(){
						ingDet.fnTotalesResumen();	
					});
					if(ingDet.accionEmpleador === "I"){
						ingDet.fnLimpiarEmpleador("NO",false);
						ingDet.fnLimpiarCliente();
					}
					
					var filter = Array(); 
					
					filter.push({ field: "IdRecaudacion"	, operator: "eq", value: ingDet.depositoSeleccionado.IdRecaudacion });
					dsDatosEmpleador.filter(filter);
					dsDatosEmpleador.read().then(function(){
						dsDatosCliente.filter(filter);
						dsDatosCliente.read().then(function(){
							ingDet.fnTotalesResumen();
							ingDet.fnSubTotalDiferencia();
							ingDet.fnTotalDiferenciaEmpleador();
							ingDet.fnLimpiarEmpleador("SI",false);
							ingDet.fnLimpiarCliente();
						});
					});
				}
				else{
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
					bcsComunes.ventanaOverlay(idVentana,false);
			    }
				bcsComunes.ventanaOverlay(idVentana,false);
		  		},function(respuesta){
				var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
		  			$.each(ErrorDetalle,function(key,value){	
					bcsComunes.msg("error","Error","Existen problemas:" + value );
				});
		  			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
		  		});
		};
	
		ingDet.fnConsultaRecaudacion = function(){

			var filter = Array(); 
			filter.push({ field: "IdRecaudacion", operator: "eq", value: ingDet.folioDeposito });
			dsDatosEmpleador.filter(filter);
			dsDatosEmpleador.read().then(function(){
				dsDatosCliente.filter(filter);
				dsDatosCliente.read().then(function(){
					ingDet.fnTotalesResumen();
					ingDet.fnSubTotalDiferencia();
					ingDet.fnTotalDiferenciaEmpleador();
					//ingDet.fnLimpiarEmpleador("NO",false);
					//ingDet.fnLimpiarCliente();
				});
			});
		}
		
		ingDet.fnGrabarCliente = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			if(ingDet.accionCliente === "M"){
				if(IdEstadoClienteSeleccionado === "DIS" || IdEstadoClienteSeleccionado === "DEV"){
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_DETALLE_CIENTE_TIENE_REGISTROS_DIS_DEV')},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			}
			
			if(dsDatosEmpleador.data().length > 0 && ingDet.seleccionEmpleador === undefined){
				if(IdRecaudacionEmpleador === undefined || IdRecaudacionEmpleador === "" || IdRecaudacionEmpleador === 0 || IdRecaudacionEmpleador === null){
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_SELECCIONE_EMPLEADOR')},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			}
			
			var fecha = new Date();

			var DCCli = 0;
			var APVCli = 0;
			
			if(ingDet.depositosConvenidosCli !== undefined && ingDet.depositosConvenidosCli !== null && ingDet.depositosConvenidosCli !== "" && ingDet.depositosConvenidosCli !== '')
				DCCli = ingDet.depositosConvenidosCli;
			
			if(ingDet.depositosAPVCli !== undefined && ingDet.depositosAPVCli !== null && ingDet.depositosAPVCli !== "" && ingDet.depositosAPVCli !== '')
				APVCli = ingDet.depositosAPVCli;
			
			var IdRecEmpleador = 0;
			
			if(ingDet.accionCliente === "M"){
				if(	ingDet.seleccionCliente[0].IdRecaudacionEmpleador === null ||
					ingDet.seleccionCliente[0].IdRecaudacionEmpleador === undefined || 
					ingDet.seleccionCliente[0].IdRecaudacionEmpleador === "")
					IdRecEmpleador = 0;
				else{
					IdRecEmpleador = ingDet.seleccionCliente[0].IdRecaudacionEmpleador;
				}
			}
			
			var parametros = {				
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : "APV",
					p_Tipo: ingDet.accionCliente === "M" ? "M" : "I",  ///Individual Manual o Por Servicio
					p_Origen: "MANUAL",
					p_Parametros : [{
						IdRecaudacionCliente	: ingDet.accionCliente === "M"? IdClienteSeleccionado : 0,
						IdRecaudacion			: ingDet.depositoSeleccionado.IdRecaudacion,
						IdRecaudacionEmpleador 	: ingDet.accionCliente === "M" ? IdRecEmpleador : IdEmpleadorSeleccionado,
						IdEmpresa				: $scope.SesionIdEmpresa,
						IdCliente				: ingDet.clienteSeleccionado.IdCliente,
						FechaIngreso			:  $scope.SesionFechaSistema,
						TipoAfiliado			: ingDet.tipoAfiliadoSeleccionado.Codigo,
						IdRegimenTriburario		: ingDet.regimenSeleccionado.Codigo,
						MontoDepositoConvenido 	: DCCli,
						MontoDepositoAPV		: APVCli,
						Origen					: "Manual",
						IdEstado				: "ING"
					}],
				};
				
				bcsComunes.ventanaOverlay($scope.ventanaId,true);
				
				bcsServiciosAPV.CrearRecaudacionCliente(parametros).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						//bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
						
						var filter = Array(); 
						
						filter.push({ field: "IdRecaudacion"	, operator: "eq", value: ingDet.depositoSeleccionado.IdRecaudacion });
						dsDatosEmpleador.filter(filter);
						dsDatosEmpleador.read().then(function(){
							dsDatosCliente.filter(filter);
							dsDatosCliente.read().then(function(){
								ingDet.fnTotalesResumen();
								ingDet.fnSubTotalDiferencia();
								ingDet.fnTotalDiferenciaEmpleador();
								ingDet.fnLimpiarEmpleador("SI",false);
								ingDet.fnLimpiarCliente();
							});
						});
						
						ingDet.ConvenidoAcumuladoCli = ingDet.ConvenidoAcumuladoCli + ingDet.kngDepositosConvenidosCli;
						ingDet.APVAcumuladoCli = ingDet.APVAcumuladoCli + ingDet.kngDepositosAPVCli;
						
						
						if(ingDet.accionCliente === "I")
							ingDet.fnLimpiarCliente();
					}
					else{
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
						$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						});
						bcsComunes.ventanaOverlay(idVentana,false);
				    }
					bcsComunes.ventanaOverlay(idVentana,false);
			  		},function(respuesta){
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
			  			$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
			  			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
			  		});
			};
			
		var datosEmpleador = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "FechaIngreso", dir: "DESC" },
					filter: [
						{ field: "IdRecaudacion", operator: "eq", value: ingDet.folioDeposito }
					],  
                    pageSize        : 7,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarEmpleadorRecaudacion'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{		
													"IdRecaudacionEmpleador"	: {type: "number"},
													"IdRecaudacion"				: {type: "number"},
													"IdEmpresa"					: {type: "number"},
													"IdPersona"					: {type: "number"},
													"DocumentoIdentidad"		: {type: "string"},
													"NombreCompleto"			: {type: "string"},
													"FechaIngreso"				: {type: "sysdate"},
													"PeriodoPago"				: {type: "string"},
													"NumeroHojas"				: {type: "number"},
													"TipoAfiliado"				: {type: "string"},
													"TipoPago"					: {type: "string"},
													"TotalResumen"				: {type: "number"},
													"Insistencias"				: {type: "string"},
													"MontoDepositoConvenido"	: {type: "number"},
													"MontoDepositoAPV"			: {type: "number"},
													"Reajustes"					: {type: "number"},
													"Intereses"					: {type: "number"},
													"Origen"					: {type: "string"},
													"Observaciones"				: {type: "string"},
													"IdEstado"					: {type: "string"},
													"Estado"					: {type: "string"},
												}
                    						}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosEmpleador = new bcs.DataSource(datosEmpleador);
		ingDet.confGridEmpleador = 
		{
    		dataSource		: dsDatosEmpleador,
    			columns		: [  {field: "IdRecaudacionEmpleador"	, title: "UI_ID_EMPLEADOR"		, width: "110px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "IdRecaudacion"			, title: "UI_FOLIO_RECAUDACION"	, width: "110px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "DocumentoIdentidad"		, title: "UI_RUT"				, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreCompleto"			, title: "UI_NOMBRE"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaIngreso"				, title: "UI_FECHA_INGRESO"		, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "PeriodoPago"				, title: "UI_PERIODO_PAGO"		, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "TipoPago"					, title: "UI_TIPO_PAGO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "Estado"					, title: "UI_ESTADO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "TotalResumen"				, title: "UI_TOTAL_RESUMEN"		, width: "110px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(TotalResumen,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "Observaciones"			, title: "UI_OBSERVACIONES"		, width: "180px", align: "left"		, sortable : true, editable: false }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 150,
	    		selectable 	: "multiple row",
	    		autoBind 	: false
		};	
		
		
		var datosCliente = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
					filter: [
						{ field: "IdRecaudacion", operator: "eq", value: ingDet.folioDeposito }
					], 
                    sort 			: { field: "FechaIngreso", dir: "DESC" },
                    pageSize        : 7,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarClienteRecaudacion'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{		
													"IdRecaudacionCliente"		: {type: "number"},
													"IdRecaudacionEmpleador"	: {type: "number"},
													"IdEmpresa"					: {type: "number"},
													"IdCliente"					: {type: "number"},
													"IdPersona"					: {type: "number"},
													"DocumentoIdentidad"		: {type: "string"},
													"NombreCompleto"			: {type: "string"},
													"FechaIngreso"				: {type: "sysdate"},
													"TipoAfiliado"				: {type: "string"},
													"IdRegimenTriburario"		: {type: "string"},
													"MontoDepositoConvenido"	: {type: "number"},
													"MontoDepositoAPV"			: {type: "number"},
													"TotalDigitado"				: {type: "number"},
													"Origen"					: {type: "string"},
													"IdEstado"					: {type: "string"},
													"Estado"					: {type: "string"},
												}
                    						}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosCliente = new bcs.DataSource(datosCliente);
		ingDet.confGridCliente = 
		{
    		dataSource		: dsDatosCliente,
    			columns		: [	
    							 {field: "IdRecaudacionCliente"		, title: "UI_ID_CLIENTE_REC"	, width: "110px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "IdRecaudacionEmpleador"	, title: "UI_ID_EMPLEADOR"		, width: "110px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "IdCliente"				, title: "UI_ID_CLIENTE"		, width: "110px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "DocumentoIdentidad"		, title: "UI_RUT"				, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreCompleto"			, title: "UI_NOMBRE"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaIngreso"				, title: "UI_FECHA_INGRESO"		, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "TipoAfiliado"				, title: "UI_TIPO_AFILIADO"		, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "Estado"					, title: "UI_ESTADO"			, width: "110px", align: "left"		, sortable : true, editable: false}
								,{field: "MontoDepositoConvenido"	, title: "UI_DIGITADO_DEP"		, width: "110px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDepositoConvenido,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "MontoDepositoAPV"			, title: "UI_DIGITADO_APV"		, width: "110px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDepositoAPV,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "TotalDigitado"			, title: "UI_TOTAL_DIGITADO"	, width: "110px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(TotalDigitado,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}

							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 150,
	    		selectable 	: "multiple row",
	    		autoBind 	: false,
				excel 		: { allPages: true }
		};	
		
		ingDet.asignarCliente = function(Cliente){
			var filter = Array(); 
			
			if(Cliente !== 0 && Cliente !== undefined){
				
				IdClienteSeleccionado 				= Cliente.IdRecaudacionCliente;
				IdEstadoClienteSeleccionado 		= Cliente.IdEstado;
				IdRecaudacionEmpleador				= Cliente.IdRecaudacionEmpleador;
				
				ingDet.idCliente					= Cliente.IdPersona;
				ingDet.idClientePostFiltro			= Cliente.IdPersona;
				ingDet.tipoAfiliado					= Cliente.TipoAfiliado;
				ingDet.tipoAfiliadoPostFiltro		= Cliente.TipoAfiliado;
				ingDet.IdRegimen					= Cliente.IdRegimenTriburario;
				ingDet.IdRegimenPostFiltro			= Cliente.IdRegimenTriburario;
				ingDet.depositosConvenidosCli		= Cliente.MontoDepositoConvenido;
				ingDet.kngDepositosConvenidosCli	= Cliente.MontoDepositoConvenido;
				ingDet.depositosAPVCli				= Cliente.MontoDepositoAPV;
				ingDet.kngDepositosAPVCli			= Cliente.MontoDepositoAPV;
				
				ingDet.accionCliente = "M";	
			}
		};
		
		ingDet.buscarClientesPorEmpleador = function(Empleador){
			var filter = Array(); 
			
			if(Empleador !== 0 && Empleador !== undefined){
				ingDet.nTrabajadores = undefined;
				ingDet.kngNTrabajadores = undefined;
				
				IdEmpleadorSeleccionado = Empleador.IdRecaudacionEmpleador;
				IdEstadoEmpleador = Empleador.IdEstado;
				
				ingDet.folioDeposito					= Empleador.IdRecaudacion;
				ingDet.folioDepositoPostFiltro			= Empleador.IdRecaudacion;
				ingDet.kngPeriodoPago					= new Date(Empleador.PeriodoPago.substr(0,4),(parseInt(Empleador.PeriodoPago.substr(5,2)) - 1),1);
				ingDet.idPersona						= Empleador.IdPersona;
				ingDet.idPersonaPostFiltro				= Empleador.IdPersona;
				ingDet.nHojas 							= Empleador.NumeroHojas;
				ingDet.kngNHojas 						= Empleador.NumeroHojas;
				ingDet.tipoPago							= Empleador.TipoPago;
				ingDet.tipoPagoPostFiltro				= Empleador.TipoPago;
				ingDet.depositosConvenidosEmp			= Empleador.MontoDepositoConvenido;
				ingDet.kngDepositosConvenidosEmp		= Empleador.MontoDepositoConvenido;
				ingDet.reajuste 						= Empleador.Reajustes;
				ingDet.kngReajuste 						= Empleador.Reajustes;
				ingDet.depositoAPVEmp					= Empleador.MontoDepositoAPV;
				ingDet.kngDepositoAPVEmp				= Empleador.MontoDepositoAPV;
				ingDet.intereces						= Empleador.Intereses;
				ingDet.kngIntereces						= Empleador.Intereses;
				
				filter.push({ field: "IdRecaudacionEmpleador"	, operator: "eq", value: Empleador.IdRecaudacionEmpleador });
				dsDatosCliente.filter(filter)
				dsDatosCliente.read().then(function(){
					ingDet.nTrabajadores = dsDatosCliente.data().length;
					ingDet.kngNTrabajadores = dsDatosCliente.data().length;
					ingDet.fnCalculosEmpleador();
				});
				
				ingDet.accionEmpleador = "M";
			}
		};
		
		ingDet.fnVentanaIngresarEmpleador = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("CrearPersona", {}, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){

	        });
		};
		
		ingDet.fnVentanaIngresarCliente = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("CrearCliente", {}, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				
	        });
		};
		ingDet.fnBuscarEmpleadorClienteInicial = function(){		
			var filter = Array(); 
			 
			filter.push({ field: "IdRecaudacion", operator: "eq", value: ingDet.recaudacionDefecto });
			const promises = [];
			promises.push(dsDatosEmpleador.query({filter:filter}));
			promises.push(dsDatosCliente.query({filter:filter}));
			$q.all(promises).then(function () {
				ingDet.fnTotalesResumen();
				ingDet.fnSubTotalDiferencia();
				ingDet.fnTotalDiferenciaEmpleador();
			})
			
		}
		
 		$scope.$watch('$viewContentLoaded', function(){
			ingDet.fnBuscarEmpleadorClienteInicial();
		 });
		 
		 $scope.$watch('ingDet.regimenSeleccionado', function(newval,oldval){
			if(newval == null){
				return;
			}
			if (newval.CodigoRegimenTipoAhorro == "APV") {
				ingDet.EsRegimenAPV = true;
				ingDet.EsRegimenDC = false;
				ingDet.depositosConvenidosCli = 0;
				ingDet.kngDepositosConvenidosCli = 0;
			}else if (newval.CodigoRegimenTipoAhorro == "DC") {
				ingDet.EsRegimenDC = true;
				ingDet.EsRegimenAPV = false;
				ingDet.depositosAPVCli = 0;
				ingDet.kngDepositosAPVCli = 0;
			}else return;
		 });
		 
		//ELIMINAR
		ingDet.fnEliminarDetalleGrilla = function(grilla){
			if(grilla === 'E')
			{
				if(ingDet.seleccionEmpleador !== undefined && ingDet.seleccionEmpleador[0].IdEstado != "DIS" && ingDet.seleccionEmpleador[0].IdEstado != "DEV")
				{
					for (i = 0; i < dsDatosCliente.data().length; i++){
						if(dsDatosCliente.data()[i].IdEstado === "DIS" || dsDatosCliente.data()[i].IdEstado === "DEV"){
							bcsComunes.ventanaMsg({ mensaje:$translate.instant("UI_ERROR_DETALLE_EMPLEADOR_TIENE_DETALLE_CLIENTE_DIS_DEV")}, idVentana);
							return;
						}
					}
										
					var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se Eliminara Registro. ¿Desea continuar?" ,idVentana);
					t .then(function(result)
					{
						if (result === OPCIONES_BCSALERTA.NO)
						{
							return;
						}else{
							//servicio
							var parametrosEliminar = {
								IdEmpresa				: $scope.SesionIdEmpresa,
								IdRecaudacion			: ingDet.seleccionEmpleador[0].IdRecaudacion,
								IdRecaudacionCliente	: 0,
								IdRecaudacionEmpleador 	: ingDet.seleccionEmpleador[0].IdRecaudacionEmpleador,
								TipoPersonaEliminar		: 'E'
							}	
							bcsServiciosAPV.EliminarRegistrosRecaudacion(parametrosEliminar).$promise.then(function(respuesta){
								bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								if (respuesta.P_OUT_ERRORESCONTADOR === 0){
									//bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_PROCESO_COMPLETADO')},idVentana);
									bcsComunes.ventanaMsg({ mensaje:$translate.instant(respuesta.P_OUT_MENSAJES[0].descripcion)}, idVentana);
									bcsComunes.ventanaOverlay(idVentana,false);
									ingDet.fnConsultaRecaudacion();
								}
								else{
									var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
									$.each(ErrorDetalle,function(key,value){	
										bcsComunes.msg("error","Error","Existen problemas:" + value );
									});
									bcsComunes.ventanaOverlay(idVentana,false);
								}
							},function(respuesta){
								var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
								$.each(ErrorDetalle,function(key,value){	
									bcsComunes.msg("error","Error","Existen problemas:" + value );
								});
								bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
							});						 
						}
					});
				}else{
					if(ingDet.seleccionEmpleador === undefined)
						bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_DEBE_SELECCIONAR_REGISTRO_A_ELIMINAR')},idVentana);
					else
						bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_DETALLE_EMPLEADOR_TIENE_REGISTROS_DIS_DEV')},idVentana);
				}/// hasta aca
			}else{		
				if(ingDet.seleccionCliente !== undefined  && ingDet.seleccionCliente[0].IdEstado != "DIS" && ingDet.seleccionCliente[0].IdEstado != "DEV"){
					var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se Eliminara Registro. ¿Desea continuar?" ,idVentana);
					t .then(function(result){
						if (result === OPCIONES_BCSALERTA.NO){
							return;
						}else{
							//servicio
							var parametrosEliminar = {
								IdEmpresa				: $scope.SesionIdEmpresa,
								IdRecaudacion			: ingDet.seleccionCliente[0].IdRecaudacion,
								IdRecaudacionCliente	: ingDet.seleccionCliente[0].IdRecaudacionCliente,
								IdRecaudacionEmpleador 	: ingDet.seleccionCliente[0].IdRecaudacionEmpleador,
								TipoPersonaEliminar		: 'C'
							}	
							bcsServiciosAPV.EliminarRegistrosRecaudacion(parametrosEliminar).$promise.then(function(respuesta){
								bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								if (respuesta.P_OUT_ERRORESCONTADOR === 0){
									//bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_PROCESO_COMPLETADO')},idVentana);
									bcsComunes.ventanaMsg({ mensaje:$translate.instant(respuesta.P_OUT_MENSAJES[0].descripcion)}, idVentana);
									bcsComunes.ventanaOverlay(idVentana,false);
									ingDet.fnConsultaRecaudacion();
								}
								else{
									var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
									$.each(ErrorDetalle,function(key,value){	
										bcsComunes.msg("error","Error","Existen problemas:" + value );
									});
									bcsComunes.ventanaOverlay(idVentana,false);
							    }
					   		},function(respuesta){
								var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					   			$.each(ErrorDetalle,function(key,value){	
									bcsComunes.msg("error","Error","Existen problemas:" + value );
								});
					   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
					   		});
						}
					});
				}else{
					if(ingDet.seleccionCliente === undefined)
						bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_DEBE_SELECCIONAR_REGISTRO_A_ELIMINAR')},idVentana);
					else
						bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_DETALLE_CLIENTE_TIENE_REGISTROS_DIS_DEV')},idVentana);
					return;
				}/// hasta aca
			}
		}
/*-------------------------------------------------------------------------------*/
	});
}());