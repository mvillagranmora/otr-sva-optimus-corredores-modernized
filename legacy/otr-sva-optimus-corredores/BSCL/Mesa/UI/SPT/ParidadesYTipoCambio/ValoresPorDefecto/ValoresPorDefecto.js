
    angular.module('ModuloMesa').controller('ValoresPorDefectoCtrl',
    function($scope,$translate,bcsComunes,$http,bcsServiciosMesa){
        /*********************************************         * VARIABLES Y CONTEXTOS         ********************************************/        
        
    	/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var vPDC 		= this;
		vPDC.idVentana	= bcsComunes.ventanaWinId($scope);
		vPDC.win		= {};
		vPDC.grilla		= {};
		vPDC.fn			= {};
		vPDC.filtro		= {};
		
		vPDC.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		vPDC.win.botonActivo = true;
		
		var tcDO = 0;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		var gridParidadesValorPorDefecto = null;
		
		var dsDefParidadesYTipoCambioValorPorDefecto = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering : false, // Filtrado de datos en el servidor.
				serverPaging 	: false, // Paginado de los datos en el servidor.
				serverSorting 	: false, // Ordenamiento de los datos en el servidor.
				schema: {
					model: {
						fields: {
							IdParidadYTipoCambioValorPorDefecto	: {type: "number"},
							IdMoneda				: {type: "string"},
							NombreMoneda			: {type: "string"},
							ParidadMercado			: {type: "number", excelFormat: "#,##0.00"},
							ParidadBID				: {type: "number", excelFormat: "#,##0.00"},
							ParidadASK				: {type: "number", excelFormat: "#,##0.00"},
							LimiteSuperiorParidad	: {type: "number", excelFormat: "#,##0.00"},
							LimiteInferiorParidad	: {type: "number", excelFormat: "#,##0.00"},
							ParidadSBIF				: {type: "number", excelFormat: "#,##0.00"},
							TipoCambioMercado		: {type: "number", excelFormat: "#,##0.00"},
							LimiteSuperiorTipoCambio: {type: "number", excelFormat: "#,##0.00"},
							LimiteInferiorTipoCambio: {type: "number", excelFormat: "#,##0.00"},
							PrecioSpotCompra		: {type: "number", excelFormat: "#,##0.00"},
							PrecioSpotVenta			: {type: "number", excelFormat: "#,##0.00"},
							PrecioSpotPromedio		: {type: "number", excelFormat: "#,##0.00"},
							MultiplicaODivide		: {type: "string"},
							UsuarioModificador		: {type: "string"}
						}
					}
				}
			},
		});
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		var dsParidadesYTipoCambioValorPorDefecto = new bcs.DataSource(dsDefParidadesYTipoCambioValorPorDefecto);
		
		vPDC.grilla.ParidadesYTipoCambioValorPorDefecto = {
			dataSource: dsParidadesYTipoCambioValorPorDefecto,
			columns: [
			    {field: "IdParidadYTipoCambioValorPorDefecto", title: "UI_PARIDAD_Y_TIPO_CAMBIO",	width: "80px",	hidden: true },
			    {field: "IdMoneda",				title: "UI_MONEDA",					width: "80px"	},
			    {field: "NombreMoneda",			title: "UI_NOMBRE_MONEDA",			width: "120px"	},
			    {title: "UI_PARIDADES", align: "center",
			    	columns: [
			    	    {field: "ParidadMercado",	title: "UI_PARIDAD_DE_MERCADO",	width: "120px",	template: "<label> #: (util.bcs_formato(ParidadMercado,	'SPT','PARIDAD','USD','','')).valor #</label>",	editable: true	},
			    	    {field: "ParidadBID",		title: "UI_PARIDAD_BID",		width: "100px",	template: "<label> #: (util.bcs_formato(ParidadBID,		'SPT','PARIDAD','USD','','')).valor #</label>",	editable: true	},
			    	    {field: "ParidadASK",		title: "UI_PARIDAD_ASK",		width: "100px",	template: "<label> #: (util.bcs_formato(ParidadASK,		'SPT','PARIDAD','USD','','')).valor #</label>",	editable: true	},
			    	    {field: "LimiteSuperiorParidad", title: "UI_LIMITE_SUPERIOR_PARIDAD", width: "120px", template: "<label> #: (util.bcs_formato(LimiteSuperiorParidad,'SPT','PARIDAD','USD','','')).valor #</label>",	editable: true	},
			    	    {field: "LimiteInferiorParidad", title: "UI_LIMITE_INFERIOR_PARIDAD", width: "120px", template: "<label> #: (util.bcs_formato(LimiteInferiorParidad,'SPT','PARIDAD','USD','','')).valor #</label>",	editable: true	},
			    	    {field: "ParidadSBIF",		title: "UI_PARIDAD_SBIF",		width: "100px", template: "<label> #: (util.bcs_formato(ParidadSBIF,	'SPT','PARIDAD','USD','','')).valor #</label>",	editable: true	},
			    ]},
			    {title: "UI_TIPO_CAMBIO", align: "center",
			    	columns: [
			    	    {field: "TipoCambioMercado",		title: "UI_TIPO_DE_CAMBIO",				width: "100px", template: "<label> #: (util.bcs_formato(TipoCambioMercado,			'SPT','TIPOCAMBIO','USD','','')).valor #</label>",	editable: true	},
			    	    {field: "LimiteSuperiorTipoCambio", title: "UI_LIMITE_SUPERIOR_TIPO_CAMBIO",width: "160px", template: "<label> #: (util.bcs_formato(LimiteSuperiorTipoCambio,	'SPT','TIPOCAMBIO','USD','','')).valor #</label>",	editable: true	},
			    	    {field: "LimiteInferiorTipoCambio", title: "UI_LIMITE_INFERIOR_TIPO_CAMBIO",width: "160px", template: "<label> #: (util.bcs_formato(LimiteInferiorTipoCambio,	'SPT','TIPOCAMBIO','USD','','')).valor #</label>",	editable: true	}
				]},
				{title: "UI_PRECIO_SPOT", align: "center",
   				    columns: [
   				        {field: "PrecioSpotCompra",		title: "UI_PRECIO_SPOT_COMPRA",		width: "120px", template: "<label> #: (util.bcs_formato(PrecioSpotCompra,	'SPT','PRECIO','USD','','')).valor #</label>",	editable: true	},
   				        {field: "PrecioSpotVenta",		title: "UI_PRECIO_SPOT_VENTA",		width: "120px", template: "<label> #: (util.bcs_formato(PrecioSpotVenta,	'SPT','PRECIO','USD','','')).valor #</label>",	editable: true	},
   				        {field: "PrecioSpotPromedio",	title: "UI_PRECIO_SPOT_PROMEDIO",	width: "120px", template: "<label> #: (util.bcs_formato(PrecioSpotPromedio,	'SPT','PRECIO','USD','','')).valor #</label>",	editable: true	},
				]},
				{field: "UsuarioModificador",			title: "Usuario Modificador",			width: "120px"	},
			],
			
			save: function(e) {
				var seleccionado = e.model;
				
				if(!tcDO || tcDO === '' || tcDO === '0'){
					$.each(e.model.parent(),function(key,value){
						if(value.IdMoneda === 'DO'){
							tcDO = value.TipoCambioMercado;
						}
					});
				}

				//Calcula el TipoCambioMercado cuando cambia la ParidadMercado de cualquier moneda (menos USD)
				if(e.values.ParidadMercado && seleccionado.IdMoneda !== 'DO' && seleccionado.IdMoneda !== 'USD'){
					e.preventDefault(); //Para redefinir el comportamiento del evento 'save'
					
					seleccionado.ParidadMercado = e.values.ParidadMercado;
					if(seleccionado.MultiplicaODivide === 'D'){
						seleccionado.TipoCambioMercado = e.values.ParidadMercado ? tcDO / e.values.ParidadMercado : 0;
					}
					else{
						seleccionado.TipoCambioMercado = tcDO * e.values.ParidadMercado;
					}
					gridParidadesValorPorDefecto.refresh();
				}
				//Calcula la ParidadMercado cuando cambia el TipoCambioMercado de cualquier moneda (menos USD)
				else if(e.values.TipoCambioMercado && seleccionado.IdMoneda !== 'DO' && seleccionado.IdMoneda !== 'USD'){
					e.preventDefault();
					
					seleccionado.TipoCambioMercado = e.values.TipoCambioMercado;
					if(seleccionado.MultiplicaODivide === 'D'){
						seleccionado.ParidadMercado = e.values.TipoCambioMercado ? tcDO / seleccionado.TipoCambioMercado : 0;
					}
					else{
						seleccionado.ParidadMercado = tcDO ? e.values.TipoCambioMercado / tcDO : 0;
					}
					gridParidadesValorPorDefecto.refresh();
				}
				//Calcula la ParidadMercado y el TipoCambioMercado de todas las monedas (menos USD) cuando cambia el TipoCambioMercado del DO
				else if(e.values.TipoCambioMercado && seleccionado.IdMoneda === 'DO'){
					e.preventDefault();
					
					seleccionado.TipoCambioMercado = e.values.TipoCambioMercado;
					$.each(e.model.parent(),function(key,value){
						if(value.IdMoneda === 'UF'){
							value.ParidadMercado = 0;
						}
						else if(value.IdMoneda !== 'DO' && value.IdMoneda !== 'USD'){
	    					if(value.MultiplicaODivide === 'D'){
	    						if(value.TipoCambioMercado > 0){
	    							value.ParidadMercado = e.values.TipoCambioMercado / value.TipoCambioMercado;
	    						}
	    						value.TipoCambioMercado = value.ParidadMercado ? e.values.TipoCambioMercado / value.ParidadMercado : 0;
							}
							else{
								if(value.TipoCambioMercado > 0){
									value.ParidadMercado = value.TipoCambioMercado / e.values.TipoCambioMercado; 
								}
								value.TipoCambioMercado = e.values.TipoCambioMercado * value.ParidadMercado;
							}
	    				}
	    			});
					gridParidadesValorPorDefecto.refresh();
				}
			},
			
			sortable	: true,
			autoBind	: true,
			height		: 420,
		    selectable 	: "row",
		    excel: {
				allPages: true,
			},
		    filterable	: false,
		    navigatable : true,
		    scrollable	: true,
		    userSettings: true,
		    gridCreated	: function(grid) {
		    	gridParidadesValorPorDefecto = grid;
			},
			notEditableColumns	:
				function(row) {
					if(row.IdMoneda === 'UF' || row.IdMoneda === 'DO'){
						return ['ParidadMercado','ParidadBID','ParidadASK','LimiteSuperiorParidad','LimiteInferiorParidad','ParidadSBIF','TipoCambioMercado','LimiteSuperiorTipoCambio','LimiteInferiorTipoCambio','PrecioSpotCompra','PrecioSpotVenta','PrecioSpotPromedio'];
						//return ['ParidadMercado','ParidadBID','ParidadASK'];
					}
				}
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		$scope.$watch('vPDC.filtro.Fecha',function(newval,oldval) {
			if(!newval || newval === ''){
				vPDC.win.botonActivo = false;
			}
			else{
				vPDC.win.botonActivo = true;
			}
		});

		vPDC.fn.buscar = function(){
			if(!vPDC.filtro.Fecha || vPDC.filtro.Fecha === ''){
				bcsComunes.msg("error",$translate.instant("UI_PARIDAD_Y_TIPOCAMBIO"),"Debe ingresar valor en filtro Fecha.");
				return;
			}
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario= $scope.$root.SesionIdUsuario;
			parametrosSP.p_Fecha	= util.dateSPdate(vPDC.filtro.Fecha);
			
			bcsComunes.ventanaOverlay(vPDC.idVentana,true);
						
			bcsServiciosMesa.ConsultaParidadYTipoCambioValorPorDefecto(parametrosSP,function (result){
				
				
				if(result.P_OUT_ERRORESCONTADOR === 0){
					console.log(result);
					dsParidadesYTipoCambioValorPorDefecto.data(result.P_OUT_PARIDADESYTIPOCAMBIOVALORPORDEFECTO);
	    			
					$.each(result.P_OUT_PARIDADESYTIPOCAMBIOVALORPORDEFECTO,function(key,value){
	    				if(value.IdMoneda === 'DO'){
	    					tcDO = value.TipoCambioMercado;
	    				}
	    			});
				}
				bcsComunes.ventanaOverlay(vPDC.idVentana,false);
				
			}, function(resError){
				bcsComunes.msg("error",$translate.instant("UI_PARIDAD_Y_TIPOCAMBIO"),$translate.instant("UI_REVISE_LOG"));
				bcsComunes.ventanaOverlay(vPDC.idVentana,false);
			});
			//pYTC.win.MontosModificados = false;
		};
		
		
		vPDC.fn.grabar = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa				= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario				= $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdModulo					= 'MSP';
			parametrosSP.p_Fecha					= vPDC.filtro.Fecha;
			parametrosSP.p_DatosParidadYTipoCambioValorPorDefecto		= dsParidadesYTipoCambioValorPorDefecto.data();
			
			if(parametrosSP.p_DatosParidadYTipoCambioValorPorDefecto.length /*&& pYTC.win.MontosModificados*/){
				bcsComunes.ventanaOverlay(vPDC.idVentana,true);
				
				bcsServiciosMesa.GuardaParidadYTipoCambioValorPorDefecto(parametrosSP,function (result){
					if (result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
						bcsComunes.msg("info",$translate.instant("UI_PARIDAD_Y_TIPOCAMBIO"),$translate.instant("UI_GRABADO"));
					}
					else if(result.P_OUT_ADVERTENCIASCONTADOR && result.P_OUT_ADVERTENCIASCONTADOR > 0){
						bcsComunes.msg("warning",$translate.instant("UI_PARIDAD_Y_TIPOCAMBIO"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
					}
					else if(result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.msg("error",$translate.instant("UI_PARIDAD_Y_TIPOCAMBIO"),result.P_OUT_ERRORESDETALLE[0].Codigo);
					}
					else bcsComunes.msg("error",$translate.instant("UI_PARIDAD_Y_TIPOCAMBIO"),$translate.instant("UI_REVISE_LOG"));
					
					vPDC.fn.buscar();
					bcsComunes.ventanaOverlay(vPDC.idVentana,false);
					
				}, function(resError){
					bcsComunes.msg("error",$translate.instant("UI_PARIDAD_Y_TIPOCAMBIO"),$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(vPDC.idVentana,false);
				});
	        }
			else{
				bcsComunes.msg("warning","UI_PARIDAD_Y_TIPOCAMBIO",$translate.instant("UI_SIN_MODIFICACIONES"));
			}
		};
		
		vPDC.fn.buscar();
	});
