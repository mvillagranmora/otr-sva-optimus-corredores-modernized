(function() {
	angular.module('ModuloMesa').controller('VencimientoForward',
	function($scope,$translate,$resource,$timeout,$http,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.ObjSeleccionGrilla = {};
		$scope.habilitoBtnProcesar = false;
		$scope.habilitoBtnEnviarTesoreria = false;
		$scope.habilitoBtnReversa = false;
		//$scope.habilitoBtnEnviarTesoreriaMonLiq = false; //esta validacion no aplica
		$scope.data.kngFechaOperacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaOperacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.dataTemplate= {};
		
		//COMBOBOX
		//ClientePortafolio
		$scope.data.oClientePortafolio={
			filtros: {
				IdEstado  : {"field": "IdEmpresa","operator": "eq", "value"  :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"  :  'UI_SI'},
				IdCliente : {"field": "EsCliente","operator": "eq", "value"  :  0},
			},
		};
		
		//Instrumento
		$scope.data.ConfiguracionInstrumento={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }				
			}
		};
		
		//Moneda Principal
		$scope.data.ConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};

		//Operación
		$scope.data.oConfiguracionOperacion={
			filtros : {
				IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW" }
			}
		};
		
		//Fecha Vencimiento
		$scope.data.oConfiguracionFechaVencimientoDesde = {
			max: util.dateInicioDia($scope.SesionFechaSistema)
		};

		$scope.data.oConfiguracionFechaVencimientoHasta = {
			max: util.dateTerminoDia($scope.SesionFechaSistema)
		};
		
		
		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering: true,
				transport: {
					read: {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=450",
						dataType : "json",
					}
				},
				schema: {
					model: {
						fields: {
							CompanyId	: {type: "string"},
							Id			: {type: "string"},
							ReportName	: {type: "string"}					
						}
					}
				}
			},
		});
		
		var gridOperaciones = null;

		var dsDefConsultaMovimientosFWD = {
			type : "odata",
			definition: {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdOperacion", dir: "ASC" },
                sort 			: [
   	                //{ field: "IdTipoInstrumento",	dir: "desc" },
   	                { field: "IdOperacion",			dir: "asc" },
   	                { field: "IdCartera",			dir: "desc" },
   	                //{ field: "FechaOperacionOPE",	dir: "desc" },
   	            ],
                pageSize        : 15,
                transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=484",
						dataType:"json"
            		}
            	},
                schema: {
                	model: {
                		fields: {
                			IdOperacion					: {type: "number"},
							FolioOperacion				: {type: "number"},
							FechaOperacion				: {type: "sysdate"},
							NombreInstrumento			: {type: "string"},
							FechaLiquidacion			: {type: "sysdate"},
							FechaOperacionOPE			: {type: "systime"},
							NombreInstrumento       	: {type: "string"},
							Nemotecnico       			: {type: "string"},
							IdTipoOperacion				: {type: "string"},
							TipoOperacion				: {type: "string"},
							RutCliente					: {type: "string"},
							NombreCliente				: {type: "string"},
							SocioNegocio				: {type: "string"},
							CodigoPropositoPortafolio	: {type: "string"},
							IdMonedaPrincipal			: {type: "string"},
							IdMonedaSecundaria			: {type: "string"},
							IdMonedaMontoRecibimos   	: {type: "string"},
							IdMonedaMontoPagamos		: {type: "string"},
							MontoMonedaPrincipal		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
							MontoMonedaSecundaria		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
							TipoCambioCierre			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
							ParidadCierre				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
							MontoRecibimos				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaMontoRecibimos,'',item.Nemotecnico).excelFormat;}},
							MontoPagamos				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaMontoPagamos,'',item.Nemotecnico).excelFormat;}},
							Modalidad					: {type: "number"},
							ModalidadLiquidacionValor	: {type: "string"},
							ModalidadLiquidacion		: {type: "string"},
							Plazo						: {type: "number"},
							PlazoResidual				: {type: "number"},
							PrecioVencimiento			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
							ParidadVencimiento			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
							IdMonedaLiquidacion			: {type: "string"},
							FechaVencimiento			: {type: "sysdate"},
							FechaEfectiva				: {type: "sysdate"},
							FechaAnticipo				: {type: "sysdate"},
							Agente						: {type: "number"},
							NombreAgente				: {type: "string"},
							CodigoUsuario				: {type: "string"},
							FixingNombre				: {type: "string"},
							FechaFixing					: {type: "sysdate"},
							FechaFixingGrillaVcto		: {type: "string"},
							IdEstado					: {type: "string"},
							EsAnticipoParcial			: {type: "string"},
							TipoVencimiento				: {type: "string"},
							IdTipoComprobante			: {type: "string"}, //TODO Modificacion campo nuevo
							FolioComprobante			: {type: "number"},
							Estado						: {type: "string"},
							FechaFiltroGrillaVcto       : {type: "sysdate"},
							Estado					    : {type: "string"},
							MontoLiquidacion	        : {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',$scope.SesionIdMonedaSistema,'',item.Nemotecnico).excelFormat;}},
							IdMonedaCompensacion		: {type: "string"},
							ValorLiquidacion	        : {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaLiquidacion,'',item.Nemotecnico).excelFormat;}},
                		}
					}
            	}
			}
		};	
		
		var dsOperacionForward = new bcs.DataSource(dsDefConsultaMovimientosFWD);
		$scope.confBasicaOperaciones = {
			dataSource: dsOperacionForward,
			columns: [
			     {field: "IdOperacion",				title: "UI_FOLIO",				width: "70px",	groupable: true }
			    ,{field: "FolioOperacion",			title: "UI_FOLIO",				width: "70px",	groupable: true, hidden: true }
			    ,{field: "NombreInstrumento",		title: "UI_INSTRUMENTO",		width: "150px",	groupable: true }
			    ,{field: "TipoOperacion",			title: "UI_TIPO_OPERACION",		width: "100px",	groupable: true }
			    ,{field: "NombreCliente",			title: "UI_NOMBRE_CLIENTE",		width: "150px",	groupable: true }
			    ,{field: "SocioNegocio",			title: "UI_SOCIO_NEGOCIO",		width: "150px",	groupable: true, hidden: true }
				,{field: "RutCliente", title: "UI_RUT",	width: "150px",	groupable: true }
			    ,{field: "CodigoPropositoPortafolio", title: "UI_ID_PORTAFOLIO",	width: "150px",	groupable: true }
			    ,{field: "FechaOperacion",			title: "UI_FECHA_OPERACION",	width: "90px",	groupable: true }
			    ,{field: "Plazo",					title: "UI_PLAZO",				width: "100px",	groupable: true }
			 	,{field: "FechaVencimiento",		title: "UI_FECHA_VENCIMIENTO",	width: "90px",	groupable: true }
			    ,{field: "FechaLiquidacion",		title: "UI_FECHA_LIQUIDACION",	width: "90px",	groupable: true }
				,{field: "FechaAnticipo",			title: "UI_FECHA_ANTICIPO",	width: "90px",	groupable: true }	
			    ,{field: "ModalidadLiquidacionValor", title: "UI_MODALIDAD",		width: "130px",	groupable: true, hidden: true }
			    ,{field: "ModalidadLiquidacion",	title: "UI_MODALIDAD",			width: "130px",	groupable: true }
			    ,{field: "IdMonedaPrincipal",		title: "UI_MONEDA_PRINCIPAL",	width: "100px",	groupable: true }
			    ,{field: "MontoMonedaPrincipal",	title: "UI_MONTO_MONEDA_PRINCIPAL", width: "120px", groupable: true, 		    	
			    	format: function (value, item) {
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico);
			    	}
			    }
			    ,{field: "IdMonedaSecundaria",		title: "UI_MONEDA_SECUNDARIA",	width: "100px", groupable: true }
			    ,{field: "MontoMonedaSecundaria",	title: "UI_MONTO_MONEDA_SECUNDARIA", width: "120px", groupable: true,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico);
			    	}
			    }
			    ,{field: "TipoCambioCierre",		title: "UI_TC_CIERRE",			width: "60px", groupable: true,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico);
			    	}
			    }
			    ,{field: "ParidadCierre",			title: "UI_PARIDAD_CIERRE",		width: "90px", groupable: true,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico);
			    	}
			    }
			    
			    ,{field: "MontoRecibimos",	title: "UI_MONTO_RECIBIMOS",width: "120px", groupable: true, align: "right",
			    	format: function(value, item){
			    		//return util.bcs_formato(value,'FWD','Monto',item.ModalidadLiquidacionValor === 'ENTREGA_FISICA' ? item.IdMonedaPrincipal : item.IdMonedaLiquidacion,'','');
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaMontoRecibimos,'',item.Nemotecnico);
			    	}
		    	}
			    ,{field: "MontoPagamos",	title: "UI_MONTO_PAGAMOS",	width: "120px", groupable: true, align: "right",
			    	format: function(value, item){
			    		//return util.bcs_formato(value,'FWD','Monto',item.ModalidadLiquidacionValor === 'ENTREGA_FISICA' ? item.IdMonedaSecundaria : item.IdMonedaLiquidacion,'','');
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaMontoPagamos,'',item.Nemotecnico);
			    	}
		    	}
			    ,{field: "PrecioVencimiento",	title: "UI_PRECIO_LIQUIDACION",	width: "80px",	groupable: true, editable: true,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico);
			    	}
			    }
			    ,{field: "ParidadVencimiento",	title: "UI_PARIDAD_LIQUIDACION",width: "80px",	groupable: true, editable: true,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico);
			    	}
			    }
			    ,{field: "IdMonedaLiquidacion",	title: "UI_MONEDA_LIQUIDACION",	width: "100px",	groupable: true, editable: true }
			    ,{field: "FixingNombre",		title: "UI_FIXING",				width: "100px",	groupable: true }
			    ,{field: "FechaFixingGrillaVcto", title: "UI_FIXING_FECHA",		width: "90px",	groupable: true }
			    ,{field: "Estado",			title: "UI_ESTADO",				width: "100px",	groupable: true }
			    ,{field: "TipoVencimiento",		title: "UI_TIPO_VENCIMIENTO",	width: "80px",	groupable: true }
			    ,{field: "IdTipoComprobante",	title: "UI_TIPO_COMPROBANTE",	width: "80px",	groupable: true } //TODO Modificacion campo nuevo reversa
			    ,{field: "FolioComprobante",	title: "UI_FOLIO_COMPROBANTE",	width: "70px",	groupable: true } //TODO Modificacion campo nuevo reversa
				,{field: "MontoLiquidacion",	title: "UI_MONTO_LIQUIDACION",width: "120px", groupable: true, align: "right",
			    	format: function(value, item){
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',$scope.SesionIdMonedaSistema,'',item.Nemotecnico);
			    	}
		    	}
		    	,{field: "IdMonedaCompensacion",	title: "UI_MONEDA_COMPENSACION",	width: "100px",	groupable: true}
		    	,{field: "ValorLiquidacion",	title: "UI_VALOR_LIQUIDACION",width: "120px", groupable: true, align: "right",
			    	format: function(value, item){
			    		return util.bcs_formato(value,item.IdTipoInstrumento,'Monto',item.IdMonedaLiquidacion,'',item.Nemotecnico);
			    	}
		    	}
			],		
			save: function(e) {				
				if(!!e.values.IdMonedaLiquidacion) {
					e.preventDefault(); //Para redefinir el comportamiento del evento 'save'
					
					var seleccionado = e.model;
					e.values.IdMonedaLiquidacion = e.values.IdMonedaLiquidacion.toUpperCase();
					seleccionado.IdMonedaLiquidacion = e.values.IdMonedaLiquidacion;
					
					if(e.values.IdMonedaLiquidacion === seleccionado.IdMonedaPrincipal){ // MX
						var factorLiquidacion = seleccionado.Nemotecnico === 'ARBITR' ? seleccionado.TCMercadoMonedaPrincipal : seleccionado.PrecioVencimiento;
						seleccionado.MontoRecibimos = (seleccionado.MontoRecibimos && factorLiquidacion) ? seleccionado.MontoRecibimosAux / factorLiquidacion : 0;
						seleccionado.MontoPagamos = (seleccionado.MontoPagamos && factorLiquidacion) ? seleccionado.MontoPagamosAux / factorLiquidacion : 0;
					}
					else if(e.values.IdMonedaLiquidacion === 'CLP' && seleccionado.Nemotecnico === 'SECANO'){
						seleccionado.MontoRecibimos = seleccionado.MontoRecibimos ? seleccionado.MontoRecibimosAux * seleccionado.PrecioVencimiento : 0;
						seleccionado.MontoPagamos = seleccionado.MontoPagamos ? seleccionado.MontoPagamosAux * seleccionado.PrecioVencimiento : 0;
					}
					else if(e.values.IdMonedaLiquidacion === seleccionado.IdMonedaSecundaria && seleccionado.Nemotecnico === 'ARBITR'){ // ARBITR
						seleccionado.MontoRecibimos = seleccionado.MontoRecibimos ? seleccionado.MontoRecibimosAux / seleccionado.TCMercadoMonedaSecundaria : 0;
						seleccionado.MontoPagamos = seleccionado.MontoPagamos ? seleccionado.MontoPagamosAux / seleccionado.TCMercadoMonedaSecundaria : 0;
					}
					else{ // CLP y cualquier otro valor
						seleccionado.MontoRecibimos = seleccionado.MontoRecibimos ? seleccionado.MontoRecibimosAux : 0;
						seleccionado.MontoPagamos = seleccionado.MontoPagamos ? seleccionado.MontoPagamosAux : 0;
					}
					
					seleccionado.IdMonedaMontoRecibimos = seleccionado.IdMonedaLiquidacion;
					seleccionado.IdMonedaMontoPagamos = seleccionado.IdMonedaLiquidacion;
					seleccionado.ValorLiquidacion = seleccionado.MontoRecibimos - seleccionado.MontoPagamos;
					seleccionado.MontoRecibimosAux = seleccionado.MontoRecibimos;
					seleccionado.MontoPagamosAux = seleccionado.MontoPagamos;
					gridOperaciones.refresh();
				}
			},
			sortable			: true,
			pageable			: true,
			filterable			: true,
			navigatable 		: true,
			userSettings 		: true,
			excel 				: { allPages: true },
			height				: 430,
			selectable 			: "multiple row",
			autoBind 			: false,
			gridCreated			: function(grid) {
				gridOperaciones = grid;
			},
			rowColorBackground  :
				function(row) {
					if (row.IdEstado === 'VCD' || row.IdEstado === 'ANT') return "yellow";
					else if (row.IdEstado === 'LIQ') return "green";
					return "";
				},
			notEditableColumns	:
				function(row) {
					let difFecha = row.FechaAnticipo > 0 ? row.FechaAnticipo : row.FechaVencimiento > 0 ? row.FechaVencimiento : row.FechaLiquidacion;
					var difFechasGrid = Math.round(util.datediff(difFecha, util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL")).asDays());
					if(difFechasGrid < 0 || row.Estado === 'LIQ' || row.ModalidadLiquidacionValor === 'ENTREGA_FISICA' ||
					   ((row.Estado === 'VCD' || row.Estado === 'ANT') && row.Nemotecnico === 'SEINFL')){
						return ['PrecioVencimiento','ParidadVencimiento','IdMonedaLiquidacion'];
					}
					else if(row.Estado === 'VIG'){
						if(row.Nemotecnico === 'ARBITR') return ['PrecioVencimiento','IdMonedaLiquidacion'];
						else if(row.Nemotecnico !== 'ARBITR') return ['ParidadVencimiento','IdMonedaLiquidacion'];
					}
					else if(row.Estado === 'ANT' || row.EGRIdTipoOperacion === 'ANTFWD'){
						return ['PrecioVencimiento','ParidadVencimiento'];
					}
					else if(row.Estado === 'VCD'){
						if(row.Nemotecnico === 'ARBITR') return ['PrecioVencimiento'];
						else if(row.Nemotecnico !== 'ARBITR') return ['ParidadVencimiento'];
					};
				}
		};
		
		
		$scope.BuscoCarteraForward = function(nroPag, filtrosAnteriores){		
			var filter = new Array();
			
			if(filtrosAnteriores === undefined){
				filter.push({ field: "IdEstado", operator: "Ne", value: 'ANU'});
				filter.push({ field: "IdEstado", operator: "Ne", value: 'MOD'});
				if($scope.data.OperacionSeleccionada && $scope.data.OperacionSeleccionada.Accion && $scope.data.OperacionSeleccionada.Accion.trim() !== 'TODOS' ){
					filter.push({ field: "IdTipoOperacion"	, operator: "eq", value: $scope.data.OperacionSeleccionada.IdTipoOperacion});	
				}			
				if($scope.data.oClientePortafolio.seleccionado.IdCliente !== undefined){
					filter.push({ field: "IdCliente", operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdCliente});	
				}
				if($scope.data.oClientePortafolio.seleccionado.IdPortafolio !== undefined){
					filter.push({ field: "IdPortafolio", operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdPortafolio});	
				}
				filter.push({ field: "FechaFiltroGrillaVcto", operator: "gte", value: util.dateInicioDia($scope.data.kngFechaOperacionDesde)});
				filter.push({ field: "FechaFiltroGrillaVcto", operator: "lte", value: util.dateTerminoDia($scope.data.kngFechaOperacionHasta)});
			}
			else{
				filter = filtrosAnteriores;
			}
			
			$scope.ObjSeleccionGrilla.seleccionoperaciones = undefined;
			dsOperacionForward.filter(filter);
			if(nroPag !== 1){
				$scope.confBasicaOperaciones.dataSource = dsOperacionForward.fetchPage(nroPag);
			}
			
			$scope.habilitoBtnReversa = false;
			$scope.habilitoBtnProcesar = false;
			$scope.habilitoBtnEnviarTesoreria = false;
		};
		
		
		$scope.$watch('ObjSeleccionGrilla.seleccionoperaciones',function(newval,oldval){
			if(newval === undefined || newval === oldval || newval.length === 0){
				$scope.habilitoBtnProcesar			= false;
				$scope.habilitoBtnEnviarTesoreria	= false;
				$scope.habilitoBtnReversa			= false;
				return;
			}
			
			// si FechaVencimiento es menor que FechaProceso no se puede procesar
			var difFechas = Math.round(util.datediff(newval[0].FechaVencimiento ,util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL")).asDays());
			if(difFechas < 0){//TODO
				$scope.habilitoBtnProcesar			= false;
				$scope.habilitoBtnEnviarTesoreria	= false;
				$scope.habilitoBtnReversa			= false;
				return;
			}
			else{
				var habilitoProcesar= true;
				var habilitoEnvTes	= true;
				var habilitoReversa	= true;
				$.each($scope.ObjSeleccionGrilla.seleccionoperaciones,function(key,value){
					if(value.Estado === 'VIG'){
						if(habilitoProcesar)habilitoProcesar= true;
						if(habilitoEnvTes)	habilitoEnvTes	= false;
						if(habilitoReversa)	habilitoReversa	= false;
					}
					else if(value.Estado === 'VCD'){
						if(habilitoProcesar)habilitoProcesar= value.ModalidadLiquidacionValor === 'ENTREGA_FISICA' ? false : true;
						if(habilitoEnvTes)	habilitoEnvTes	= value.ModalidadLiquidacionValor === 'ENTREGA_FISICA' ? false : true;
						if(habilitoReversa)	habilitoReversa	= false;
					}
					else if(value.Estado === 'ANT'){
						if(habilitoProcesar)habilitoProcesar= false;
						if(habilitoEnvTes)	habilitoEnvTes	= value.ModalidadLiquidacionValor === 'ENTREGA_FISICA' ? false : true;
						if(habilitoReversa)	habilitoReversa	= value.TipoVencimiento === 'ANTICIPADA' ? true : false;
					}
					else if(value.Estado === 'LIQ'){
						if(habilitoProcesar)habilitoProcesar= false;
						if(habilitoEnvTes)	habilitoEnvTes	= false;
						if(habilitoReversa)	habilitoReversa	= value.TipoVencimiento === 'ANTICIPADA' ? true : false;
					}
					else{
						habilitoProcesar= false;
						habilitoEnvTes	= false;
						habilitoReversa	= false;
					}
				});
				
				$scope.habilitoBtnProcesar			= habilitoProcesar;
				$scope.habilitoBtnEnviarTesoreria	= habilitoEnvTes;
				$scope.habilitoBtnReversa			= habilitoReversa;
			}
		});

		$scope.$watch('data.kngFechaOperacionDesde',function(newval,oldval){
			if(newval && newval !== oldval){
				$scope.habilitoBtnProcesar			= false;
				$scope.habilitoBtnEnviarTesoreria	= false;
				$scope.habilitoBtnReversa			= false;
				return;
			}
		});

		$scope.$watch('data.kngFechaOperacionHasta',function(newval,oldval){
			if(newval && newval !== oldval){
				$scope.habilitoBtnProcesar			= false;
				$scope.habilitoBtnEnviarTesoreria	= false;
				$scope.habilitoBtnReversa			= false;
				return;
			}
		});
		
		
		$scope.ProcesarOperaciones = function(){
			var tablaOperaciones	= [];
			$.each($scope.ObjSeleccionGrilla.seleccionoperaciones,function(key,value){	
				if((value.Estado === 'VIG' || value.Estado === 'ANT' || value.Estado === 'VCD') && 
				   ((value.ModalidadLiquidacionValor === 'ENTREGA_FISICA') || 
					(value.Nemotecnico !== 'ARBITR' && value.PrecioVencimiento > 0) || 
					(value.Nemotecnico === 'ARBITR' && value.ParidadVencimiento > 0))
				   ){
					var ObjOper = {
						"IdCartera"				: value.IdCartera,
						"FolioCartera" 			: value.IdOperacion,
						"IdOperacion"			: value.FolioOperacion,
						"IdEstado"				: value.IdEstado,
						"Precio"				: value.ModalidadLiquidacionValor === 'ENTREGA_FISICA' ? (value.TipoCambioCierre || value.ParidadCierre) : value.Nemotecnico === 'ARBITR' ? value.ParidadVencimiento : value.PrecioVencimiento,
						"IdMonedaLiquidacion" 	: value.IdMonedaLiquidacion,
					};
					tablaOperaciones.push(ObjOper);
				}
			});
			
			var datosProcesarOperacionesVencidas = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdPais			: $scope.SesionIdPaisSistema,
				p_IdUsuario 		: $scope.SesionIdUsuario,
				p_IdBolsa			: 0,
				p_IdSucursal		: 0,
				p_IdModulo			: 'MFW',
				p_UsaReglasNegocio 	: 'N',
				p_tablaOperVencidas	: tablaOperaciones
			};
			
			if(tablaOperaciones.length === 0){
				bcsComunes.msg("warning","Informacion","Debe Ingresar al menos un Precio para poder vencer la(s) operacion(es).");
				return;
			}		
			
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.VencimientoForward(datosProcesarOperacionesVencidas).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		   		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("Operacion(es) procesada(s) correctamente."),
						conflictos:util.parseServerResponse(respuesta)
					  },idVentana);
				}
				else{
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("Existen problemas al procesar operaciones"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
			    }
				$scope.habilitoBtnProcesar = false;
				$scope.habilitoBtnEnviarTesoreria = false;
				bcsComunes.ventanaOverlay(idVentana,false);
				$scope.BuscoCarteraForward(dsOperacionForward.page(), dsOperacionForward.filter());	   			
	   		},function(respuesta){
	   			$scope.habilitoBtnProcesar = false;
				$scope.habilitoBtnEnviarTesoreria = false;
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("error","Error","Existen problemas al procesar operaciones");
	   		});
		};
		
		
		$scope.EnviarATesoreria = function(){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_PROCESO_LIQUIDACION_FWD")+' '+$translate.instant("UI_CONTINUAR"));
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					var tablaOperaciones = [];
					$.each($scope.ObjSeleccionGrilla.seleccionoperaciones,function(key,value){	
						if (value.Estado === 'VCD' || value.Estado === 'ANT'){
							var ObjOper = {
								"IdCartera"				: value.IdCartera,
								"FolioCartera"			: value.IdOperacion,
								"IdOperacion"			: value.FolioOperacion,
								"IdEstado"				: value.IdEstado,
								"MontoOperacion"		: value.MontoRecibimos || value.MontoPagamos,
								"IdMonedaLiquidacion"	: value.IdMonedaLiquidacion,
							};
							tablaOperaciones.push(ObjOper);
						}
					});
					
					var datosEnviarATesoreria = {
						p_IdEmpresa 			: $scope.SesionIdEmpresa,
						p_IdUsuario 			: $scope.SesionIdUsuario,
						p_IdModulo				: 'MFW',
						p_IdPais				: $scope.SesionIdPaisSistema,
						p_IngresoEgresoGenerar	: tablaOperaciones
					};
			   		
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServiciosMesa.MovimientoTesoreríaForward(datosEnviarATesoreria).$promise.then(function(respuesta){
				   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				   		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.msg("info","Informacion","Operaciones Liquidadas correctamente.");
						}
						else{
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("Existen errores o advertencias al liquidar las operaciones"),
								conflictos:util.parseServerResponse(respuesta)
							},idVentana);
					    }
						$scope.habilitoBtnProcesar = false;
						$scope.habilitoBtnEnviarTesoreria = false;
						bcsComunes.ventanaOverlay(idVentana,false);
						$scope.BuscoCarteraForward(dsOperacionForward.page(), dsOperacionForward.filter());	   			
			   		},function(respuesta){
			   			$scope.habilitoBtnProcesar = false;
						$scope.habilitoBtnEnviarTesoreria = false;
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						bcsComunes.msg("error","Error","Existen problemas al enviar operaciones");
			   		});
				}
				else{
					bcsComunes.ventanaOverlay(idVentana, false);
					return;
				}
			});
		};
		
		$scope.ReversaAnticipoOperacion = function(){
			if($scope.ObjSeleccionGrilla.seleccionoperaciones.length > 1){
				bcsComunes.msg("error","Informacion","Sólo puede Reversar una operación.");
				return;
			}

			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_REVERSA_ANTICIPO_OPERACION"));
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					/*if($scope.ObjSeleccionGrilla.seleccionoperaciones[0].Estado !== 'ANT'){
						bcsComunes.msg("info","Informacion","Para Reversar una operación el estado de esta debe ser 'ANT' (ANTICIPADO).");
						return;
					}*/
					/*if($scope.ObjSeleccionGrilla.seleccionoperaciones[0].EsAnticipoParcial === 'SI'){
						bcsComunes.msg("error","Informacion","Los Anticipos Parciales no se pueden Reversar.");
						return;
					}*/
					
					var tablaOperaciones = [];
					var ObjOper = {
						"IdCartera" 	: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdCartera,
						"FolioCartera" 	: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdOperacion,
						"IdOperacion"	: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].FolioOperacion,
						"IdEstado"		: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdEstado,
					};
					tablaOperaciones.push(ObjOper);
					
					var datosReversaOperaciones = {
						p_IdEmpresa 		: $scope.SesionIdEmpresa,
						p_IdPais			: $scope.SesionIdPaisSistema,
						p_IdUsuario 		: $scope.SesionIdUsuario,
						p_IdBolsa			: 1,	
						p_IdSucursal    	: 1,
						p_IdModulo			: 'MFW',
						p_UsaReglasNegocio	: 'N',
						p_tablaOperReversa	: tablaOperaciones
					};
					
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServiciosMesa.ReversaAnticipoForward(datosReversaOperaciones).$promise.then(function(respuesta){
				   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				   		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.ventanaMsg({
								mensaje:'Anticipo reversado correctamente: ',
								conflictos:util.parseServerResponse(respuesta)
							});
						}
						else{
							var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
							bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							$.each(ErrorDetalle,function(key,value){
								
								bcsComunes.ventanaMsg({
									mensaje:'Error al reversar las operaciones:',
									conflictos: { errores: [{ Codigo : value.codigo, Descripcion : value.descripcion}] }
								});
								
							});
						}
						$scope.habilitoBtnProcesar = false;
						$scope.habilitoBtnEnviarTesoreria = false;
						bcsComunes.ventanaOverlay(idVentana,false);
						$scope.BuscoCarteraForward(dsOperacionForward.page(), dsOperacionForward.filter());	   			
			   		},function(respuesta){
			   			$scope.habilitoBtnProcesar = false;
						$scope.habilitoBtnEnviarTesoreria = false;
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						bcsComunes.msg("error","Error","Existen problemas al reversar las operaciones");
						
					});
				} else{
					bcsComunes.ventanaOverlay(idVentana, false);
					return;
				}
			});
		};
		
		
		//REPORTE CARTA LIQUIDACION
		$scope.generarCartaLiquidacion = function(){
			if(!$scope.ObjSeleccionGrilla.seleccionoperaciones){
				bcsComunes.msg("error","error","Debe seleccionar una o más operaciones.");
				return;
			}
			if($scope.ObjSeleccionGrilla.seleccionoperaciones.length > 1){
				bcsComunes.msg("error","error","Sólo puede generar Carta Liquidación de una operación.");
				return;
			}
			
			
			var filterTemplate = [ { field: "NombreReporte",operator: "eq", value: 'CARTALIQUIDACIONFWD' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= ConsultaTemplate.view()[0].TipoReporte;
					$scope.dataTemplate.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
					$scope.dataTemplate.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte;
					
					var parametros = {
						"p_IdEmpresa"		: $scope.$root.SesionIdEmpresa,
						"p_IdPais"			: $scope.$root.SesionIdPaisSistema,
						"p_Fecha"			: $scope.SesionFechaSistema,
						"p_IdCartera"		: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdCartera,
						"p_IdOperacion"		: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdOperacion,
						"p_FolioOperacion"	: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].FolioOperacion,
						"p_FechaOperacion"	: util.dateSPdate($scope.ObjSeleccionGrilla.seleccionoperaciones[0].FechaOperacion),
						"p_IdEstado"		: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdEstado,
						"$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]
					};
					$scope.dataTemplate.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros };
					$scope.getReport($scope.dataTemplate);
				}
			});

			

		};
		
		$scope.getReport = function(dataTemplate){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.IdReporte,
				"parameters"	: "",
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.NombreReporte,
						"data"		: dataTemplate.data,
						"parameters": {}
					},
					"subReports": [] 
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
					$scope.data.activarReporte = false;
					$scope.notificarCartasLiquidacion();
				})
				.error(function(response) {
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
						conflictos: { errores: [{ Codigo : 'COD', Descripcion : 'Un error inesperado ha ocurrido' }] }
					},idVentana);
				});
		};

		$scope.notificarCartasLiquidacion = function(){
			
			let parametros = {

				"p_IdEmpresa"		: $scope.$root.SesionIdEmpresa,
				"p_IdUsuario" 		: $scope.SesionIdUsuario,
				"p_Fecha"			: $scope.SesionFechaSistema,
				"p_CartaANotificar" : [
					{
						"p_IdCartera"		: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdCartera,
						"p_IdOperacion"		: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdOperacion,
						"p_FolioOperacion"	: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].FolioOperacion,
						"p_FechaOperacion"	: util.dateSPdate($scope.ObjSeleccionGrilla.seleccionoperaciones[0].FechaOperacion),
						"p_IdEstado"		: $scope.ObjSeleccionGrilla.seleccionoperaciones[0].IdEstado
					}
				]
				
			};

			bcsServiciosMesa.NotificarCartaLiquidacion(parametros).$promise.then(function(respuesta) {

				if ( respuesta.P_OUT_ERRORESCONTADOR === 0) {

					bcsComunes.ventanaMsg({ 
						mensaje:$translate.instant('UI_NOTIFICACION_CARTA_LIQUIDACION')
					},idVentana);

				} else {

					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_NOTIFICACION_CARTA_LIQUIDACION"));

				}

			},function(respuesta){

				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_NOTIFICACION_CARTA_LIQUIDACION"));

			});

		}
		
	});
}());
