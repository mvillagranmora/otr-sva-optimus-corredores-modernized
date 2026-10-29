(function() {
	angular.module('ModuloMesa').controller('posicionPorMonedasCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		
		$scope.data = {};
		
		var posicionPorMonedas = this;
		//Configurar al principio
		
		posicionPorMonedas.data = {};
		
		$scope.fechaOperacion = {};
		//$scope.fechaOperacion.max = new Date();
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.monedaPrincipal; 
		$scope.data.kngMonedaPrincipal;
		
		$scope.data.oConfiguracionTipoMesa={
			filtros : {
				NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "TipoMesa" },
				ValorParametro 		: { "field"   : "ValorParametro" , "operator" : "eq" , "value" : "" }
			}
		};
		
		// actualizacion Web Socket
		$scope.msgWebsocket = function(message) {
			if (message.Evento === 'ACTSPOT') {
				$scope.data.consultarPosicion();
			}
		}
		
		
		$scope.data.consultarPosicion = function()
		{	
			//overlay.encendido = true;
			
			if($scope.data.kngFecha === undefined)
			{
				bcsComunes.msg("error","error",$translate.instant('UI_FECHA_VACIA'));
				//overlay.encendido = false;
			}
			else{
				
				var parametros = {};

				parametros.p_IdMoneda          = parametrosVentana.idMoneda;
				parametros.p_IdEmpresa         = $scope.SesionIdEmpresa;
				parametros.p_IdUsuario         = $scope.SesionIdUsuario;
				parametros.p_FechaOperacion    = $scope.data.kngFecha;
				
				var seleccionTipoMesa = "";
				
				if($scope.data.oConfiguracionTipoMesa.seleccionado === undefined)
					seleccionTipoMesa = 'TODOS';
				else
					seleccionTipoMesa = $scope.data.oConfiguracionTipoMesa.seleccionado.ValorParametro;
			
				switch(seleccionTipoMesa)
				{
				case "INTERBANCARIO":
					parametros.p_TipoMesa	   = 'INTERB';
					break;
				case "DISTRIBUCION":
					parametros.p_TipoMesa	   = 'DISTRI';
					break;
				default:
					parametros.p_TipoMesa	   = 'TODOS';
				}
				
				bcsServiciosMesa.ConsultarPosicionPorMoneda(parametros, successConsultarPosicion,errorConsultarPosicion);
			}
		};
		
		var successConsultarPosicion = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){				
				dsPosicionPorMoneda.data(respuesta.P_OUT_POSICIONPORMONEDAS);
			}
			else
			{		
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaMsg({
				      mensaje:$translate.instant("UI_ERROR"),
				      conflictos:{
				       errores : [ {
				           Codigo : 'COD',
				           Descripcion : 'Un error inesperado ha ocurrido'
				          } ],
				      }
				},idVentana);
			}
			//overlay.encendido = false;
          };	
		
		var errorConsultarPosicion = function(respuesta)
		{
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));

			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : 'Un error inesperado ha ocurrido'
			          } ],
			      }
			},idVentana);
			
			//overlay.encendido = false;
		};
		
			var dsDefPosicionPorMoneda = 
			{
					type : "array",
					definition : 
					{	
					   pageSize : 20,
					   data : [],
	                   schema 			: { 
	                    				   model: 
	                    						{
													fields : 
													{
														IdMoneda			: {type: "string"},
														Compras				: {type: "decimal", excelFormat:function (item){return util.bcs_formato(0,'SPT','Monto',item.IdMoneda,'','').excelFormat;}},
														Ventas				: {type: "decimal", excelFormat:function (item){return util.bcs_formato(0,'SPT','Monto',item.IdMoneda,'','').excelFormat;}},
														PosicionInicial		: {type: "decimal", excelFormat:function (item){return util.bcs_formato(0,'SPT','Monto',item.IdMoneda,'','').excelFormat;}},
														Posicion			: {type: "decimal", excelFormat:function (item){return util.bcs_formato(0,'SPT','Monto',item.IdMoneda,'','').excelFormat;}},
														Paridad				: {type: "decimal", excelFormat:function (item){return util.bcs_formato(0,'SPT','Monto',item.IdMoneda,'','').excelFormat;}},
														EquivalenteUSD		: {type: "decimal", excelFormat:function (item){return util.bcs_formato(0,'SPT','Monto',item.IdMoneda,'','').excelFormat;}},
													}
												}
										  }
	    			 }
			};	
		       	
		//---------------------------------------------
		//GRILLA PUNTAS
 
		var dsPosicionPorMoneda = new bcs.DataSource(dsDefPosicionPorMoneda);
		$scope.confPosicionPorMoneda = 
		{
    				dataSource		: dsPosicionPorMoneda,
    					columns		: [  {field: "IdMoneda", title: "UI_MONEDA", width: "70px", align: "left", sortable : false }
				       				    ,{field: "Compras", title: "UI_COMPRAS", width: "70px", align: "left", sortable : false, 
				       				    	template_ : " <label> #if(Compras === 0){# 0 #}else { # #: (util.bcs_formato(Compras,'SPT','Monto',IdMoneda,'','')).valor # #} #</label>",
			       				    		format : function(value, dataItem) {
			       								return util.bcs_formato(value,'SPT','Monto',dataItem.IdMoneda,'','');
			       							}
				       				     }
				       				    ,{field: "Ventas", title: "UI_VENTAS", width: "70px", align: "left", sortable : false, 
				       				    	template_ : " <label> #if(Ventas === 0){# 0 #}else { # #: (util.bcs_formato(Ventas,'SPT','Monto',IdMoneda,'','')).valor # #} #</label>",
				       				    	format : function(value, dataItem) {
			       								return util.bcs_formato(value,'SPT','Monto',dataItem.IdMoneda,'','');
			       							}
				       				    }
				       				    ,{field: "PosicionInicial", title: "UI_POSICION_INICIAL", width: "70px", align: "left", sortable : false, 
				       				    	template_ : " <label> #if(PosicionInicial === 0){# 0 #}else { # #: (util.bcs_formato(PosicionInicial,'SPT','Monto',IdMoneda,'','')).valor # #} #</label>",
				       				    	format : function(value, dataItem) {
			       								return util.bcs_formato(value,'SPT','Monto',dataItem.IdMoneda,'','');
			       							}
				       				    }
				       				    ,{field: "Posicion", title: "UI_POSICION", width: "70px", align: "left", sortable : false, 
				       				    	template_ : " <label> #if(Posicion === 0){# 0 #}else { # #: (util.bcs_formato(Posicion,'SPT','Monto',IdMoneda,'','')).valor # #} #</label>",
				       				    	format : function(value, dataItem) {
			       								return util.bcs_formato(value,'SPT','Monto',dataItem.IdMoneda,'','');
			       							}
				       				    }
				       				    ,{field: "Paridad", title: "UI_PARIDAD", width: "70px", align: "left", sortable : false, 
				       				    	template_ : " <label> #if(Paridad === 0){# 0 #}else { # #: (util.bcs_formato(Paridad,'SPT','Monto',IdMoneda,'','')).valor # #} #</label>",
				       				    	format : function(value, dataItem) {
			       								return util.bcs_formato(value,'SPT','Monto',dataItem.IdMoneda,'','');
			       							}
				       				    }
				       				    ,{field: "EquivalenteUSD", title: "UI_EQUIVALENTE_USD", width: "70px", align: "left", sortable : false,
				       				    	template_ : " <label> #if(EquivalenteUSD === 0){# 0 #}else { # #: (util.bcs_formato(EquivalenteUSD,'SPT','Monto',IdMoneda,'','')).valor # #} #</label>",
				       				    	format : function(value, dataItem) {
			       								return util.bcs_formato(value,'SPT','Monto',dataItem.IdMoneda,'','');
			       							}
				       				    }
									  ],		
    					sortable	: true,
    					pageable	: true,
    					filterable	: false,
    					navigatable: true,
    					height		:235,
    					selectable 	: "row",
    					userSettings 	: true,
//    					excel 			: { allPages: true },
    				    autoBind 	 : false,
       	};

		$scope.data.consultarPosicion();
//------------		
	});
}());