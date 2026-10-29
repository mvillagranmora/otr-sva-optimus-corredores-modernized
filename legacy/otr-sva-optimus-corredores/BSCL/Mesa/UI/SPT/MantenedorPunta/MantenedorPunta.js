+(function() {
	angular.module('ModuloMesa').controller('MantenedorSpotCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa,bcsDinamicosMesa)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var row = '';
		var varEstado = 'VIG';
			
		$scope.data = {};
		$scope.data.Puntas = bcsDinamicosMesa.DynPuntas;		
		$scope.data.kngLimiteSuperior = 10;
		$scope.data.kngLimiteInferior = 10;
		var ingSpot = this;
		
		ingSpot.datosAntes = {};
		ingSpot.datosDespues = {};
		ingSpot.datosAntes = angular.copy(ingSpot.datosDespues);
		
		//Configurar al principio
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.MargenPunta = $scope.data.kngMargenPunta || 0;
		ingSpot.data = {};
		//BLOQUEO BOTONES
		$scope.data.bloqueoGrabarPuntas = false;	
		$scope.data.bloqueoGrabarPuntasTramo = false;
		$scope.data.bloqueoGrabarPuntasAgente = false;
		$scope.data.bloqueoGrabarPuntasCliente = false;
		//$scope.ObjSeleccionGrilla = {};
		$scope.data.kngIgualarMargenes = false;	
		$scope.data.kngFechaHora = $scope.SesionFechaSistema + " " + $scope.SesionHoraSistema;
		
		$scope.msgWebsocket = function(message) {
			datos.IdMoneda = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda || "USD";
			datos.IdEmpresa = $scope.SesionIdEmpresa || 1;
			if (message.Evento === 'ACTPTA') {
				ObtenerValores(datos, successIngreso,error);   
				ConsultarPuntasTramo(datos);
			}
		}
		
		
		//Asigna valores iniciales
		var datos = {};
		//----COMBOBOX MONEDA---------------------
		$scope.$watch('data.oConfiguracionMoneda.seleccionado',function(newval,oldval)
		{ 	
			
			if (typeof $scope.data.oConfiguracionMoneda === 'undefined')
				return
				
			if (newval === oldval)
				return	
			
		 //-- Valores para el SP Consultar Puntas --
			
			datos.IdMoneda = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
			datos.IdEmpresa = $scope.SesionIdEmpresa;
			
			ObtenerValores(datos, successIngreso,error); 
			ConsultarPuntasTramo(datos);
		});

		var params = function (indexCodigoParametro) {
			var codigoParametro = ['LIMITE_SUPERIOR_SPOT_MESA','LIMITE_INFERIOR_SPOT_MESA'];
			return {
				p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	
				p_IdTipoProducto 	: 'MEME',
				p_CodigoParametro 	: codigoParametro[indexCodigoParametro]
			};
		}


		bcsServiciosMesa.ConsultarConfiguracionMesa(params(0)).$promise.then(function(respuesta) {
			$.each(respuesta.P_OUT_DATOSMESA,function(_,value){
				$scope.data.kngLimiteSuperior= value.Valor;
			})
		})

		bcsServiciosMesa.ConsultarConfiguracionMesa(params(1)).$promise.then(function(respuesta) {
			$.each(respuesta.P_OUT_DATOSMESA,function(_,value){
				$scope.data.kngLimiteInferior= value.Valor;
			})
		})

		$scope.data.habilitado = function () {
			if ($scope.data.LimiteInferior !== undefined || $scope.data.LimiteSuperior !== undefined  ) {
				$scope.data.bloqueoGrabarPuntasAgente = true;
			}
		}

		var ConsultarPuntasTramo = function (datos) {
			var monedaConsulta = datos.IdMoneda !== 'undefined' ? datos.IdMoneda : 'USD';
			//-- Grilla Puntas --
			var filtroPunta = new Array();
           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Punta" });
           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:monedaConsulta});
           	dsPuntas.filter(filtroPunta); 
           	//-- Grilla Agente  --
    		var filtroAgente = new Array();
    		filtroAgente.push({ field: "Canal", operator: "eq", value: "Agente" });
    		filtroAgente.push({ field: "IdMonedaPunta", operator: "eq", value:monedaConsulta});
            dsAgente.filter(filtroAgente);  
            //-- Grilla Cliente --	
           	var filtroCliente = new Array();
    		filtroCliente.push({ field: "Canal", operator: "eq", value: "Cliente" });
    		filtroCliente.push({ field: "IdMonedaPunta", operator: "eq", value:monedaConsulta});
           	dsCliente.filter(filtroCliente); 
		}
		//---------------------------------------------
		//
		var ObtenerValores = function(datos, success_fn,error_fn){
			if (typeof datos.IdMoneda !== 'undefined')
			{
				var urlObtenerValores = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3595";
				var r = $resource("", {},{
					'ConsultarPuntasMantenedor': { method: "POST", url: urlObtenerValores}
				});
				var post = r.ConsultarPuntasMantenedor(datos);
				post.$promise.then(success_fn).catch(error_fn);	
			}
		};
		
		var varIdPunta;
		var estado;
		var successIngreso = function(respuesta)
		{
			if (respuesta.P_OUT_PUNTAS.length === 0)
            {
				$scope.data.kngAperturaPrecio = 0;
				$scope.data.kngPuntaCompra = 0;
				$scope.data.kngPuntaVenta = 0;
				asignarcheckbox(false);
				//checkboxIgualarMargenes(false);
				$scope.data.decimalesPunta = 0;
				
				bcsComunes.msg("warning","warning",$translate.instant('UI_NO_HAY_PUNTAS'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaOverlay($scope.winID, false);
				
				
            } else{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaOverlay($scope.winID, false);

            	varIdPunta						= respuesta.P_OUT_PUNTAS[0].IdPuntas;
				$scope.data.kngAperturaPrecio   = respuesta.P_OUT_PUNTAS[0].AperturaPrecio;
				$scope.data.kngPuntaCompra 		= respuesta.P_OUT_PUNTAS[0].PuntaCompra;
				$scope.data.kngPuntaVenta  		= respuesta.P_OUT_PUNTAS[0].PuntaVenta;
				asignarcheckbox(respuesta.P_OUT_PUNTAS[0].CheckPrecioPorTramo);
				varEstado						= varEstado;
				$scope.data.decimalesPunta = respuesta.P_OUT_PUNTAS[0].PuntosDecimales;
				$scope.data.Editable = respuesta.P_OUT_PUNTAS[0].Editable === 'S';
				$scope.data.SistemaMod = respuesta.P_OUT_PUNTAS[0].SistemaModificador || 'MANUAL_SIN_INFORMACION';
				$scope.data.Fecha = respuesta.P_OUT_PUNTAS[0].Fecha ? new Date(respuesta.P_OUT_PUNTAS[0].Fecha) : '';
				$scope.data.bloqueoGrabarPuntas = false;
				console.log($scope.SesionHoraSistema);
            }
          };	
		
		/*Actualzar valores Puntas*/

		$scope.$watch('data.kngAperturaPrecio',function(newval,oldval)
		{   
			if ((oldval === undefined) || (newval === oldval)){
				$scope.data.bloqueoGrabarPuntas = false;

				return
			}
			if ((newval != oldval) && (oldval != undefined) && $scope.data.Editable)
			{
				console.log((newval != oldval) && (oldval != undefined) && $scope.data.Editable, 'data.kngAperturaPrecio')
				$scope.data.bloqueoGrabarPuntas = true;
				$scope.data.kngPuntaVenta = $scope.data.kngPuntaCompra + ($scope.data.kngAperturaPrecio/100);
			}
		});

		$scope.$watch('data.kngPuntaCompra',function(newval,oldval)
		{   
			$scope.data.kngPuntaVenta = $scope.data.kngPuntaCompra + ($scope.data.kngAperturaPrecio/100);
			if ((newval != oldval) && (oldval != undefined) && (isNaN(oldval) === false) && $scope.data.Editable)
			{
				console.log((newval != oldval) && (oldval != undefined) && $scope.data.Editable, 'data.kngPuntaCompra')
				$scope.data.bloqueoGrabarPuntas = true;
			}
		});

		$scope.$watch('data.kngPuntaVenta',function(newval,oldval)
		{   
			$scope.data.kngPuntaCompra = $scope.data.kngPuntaVenta - ($scope.data.kngAperturaPrecio/100);
			if ((newval != oldval) && (oldval != undefined) && (isNaN(oldval) === false) && $scope.data.Editable)
			{
				console.log((newval != oldval) && (oldval != undefined) && $scope.data.Editable, 'data.kngPuntaVenta')
				$scope.data.bloqueoGrabarPuntas = true;
			}
		});
		/*CheckBox*/
		var varCheckPrecio;
		$scope.$watch('data.PrecioTramos',function(newval,oldval){
			
			if ( newval !== oldval && oldval !== undefined ) {
				$scope.data.bloqueoGrabarPuntasTramo = true;	
			}
			
			if($scope.data.PrecioTramos && oldval != undefined)
			{
				varCheckPrecio = 'SI';
			}
			else
			{
				varCheckPrecio = 'NO';
			}	
		});	
		/*------------------*/
		var varCheckIgualarMargenes;
		$scope.$watch('data.IgualarMargenes',function(newval,oldval){
		if($scope.data.IgualarMargenes)
		{
			varCheckIgualarMargenes = 'SI';
			$scope.data.bloqueoGrabarPuntasCliente = true;
		}
		else
		{
			varCheckIgualarMargenes = 'NO';
		}
		});	
		
		/*------------------*/

		var error = function(respuesta)
		{
			//if (respuesta.P_OUT_PUNTAS === undefined)
				bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaOverlay($scope.winID, false);
		
		};
		//
		//---------------------------------------------
		var dsDefPuntasPorTramo = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "MontoPorTramo", dir: "desc" },
                    pageSize        : 100,
                    transport		: {
		        	            		read: 
		        	            		{
		        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=406",
		        							dataType:"json"
		        	            		}
		        	            	},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													IdPuntasPorTramo		: {type: "number"},
													IdEmpresa				: {type: "number"},
													IdPuntas				: {type: "number"},
													MontoPorTramo			: {type: "number", editable: true ,validation: { min: 0, required: true }, excelFormat: "#,##0.00"},
													PorcentajeTramo       	: {type: "number", editable: true ,validation: { min: 0, required: true }},
													MontoCentavos			: {type: "number", excelFormat: "#,##0.00"},
													ValorCompra				: {type: "number", excelFormat: "#,##0.00"},
													ValorVenta				: {type: "number", excelFormat: "#,##0.00"},
													Estado					: {typer:"string"},
													IdUsuario				: {type: "number"},
													Usuario				    : {type: "string", editable: true },
													FechaModificacion		: {type: "sysdate", editable: true },
													Canal					: {type: "string"},
													IdMonedaPunta			: {type: "string"}
												}
											}
									  }

    			 }
		};
       	
		//---------------------------------------------
		//GRILLA PUNTAS
		var dsPuntas = new bcs.DataSource(dsDefPuntasPorTramo);
		$scope.confBasicaPunta = {
			dataSource	: dsPuntas,
			columns	: [	{field: "MontoPorTramo", title: "UI_MONTO_EN_USD", width: "70px", align: "right", sortable : false, editable: true,
							format : function(value, dataItem) {
								return util.bcs_formato(value,'SPT','Monto','USD','','');
							}}
						,{field: "MontoCentavos", title: "UI_APERTURA_PESOS", width: "70px", align: "right", sortable : false, editable: true,
							format : function(value, dataItem) {
								return util.bcs_formato(value,'SPT','Monto','USD','','');
							}}
						,{field: "ValorCompra", title: "UI_COMPRA", width: "70px", align: "right", sortable : false,
							format : function(value, dataItem) {
								return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
							}}
       				    ,{field: "ValorVenta", title: "UI_VENTA", width: "70px", align: "right", sortable : false,
							format : function(value, dataItem) {
								return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
							}}
       				    ,{field: "IdUsuario", title: "UI_ID_USUARIO_MODIFICA", width: "70px", align: "left", sortable : false, hidden: true }
       				    ,{field: "Usuario", title: "UI_USUARIO_MODIFICA", width: "70px", align: "left", sortable : false }
       				    ,{field: "FechaModificacion", title: "UI_FECHA_MODIFICACION", width: "70px", align: "left", sortable : false }
       				],
			sortable	: true,
			pageable	: true,
			filterable	: false,
			navigatable: true,
			excel : {
				allPages: true,
			},
			height		: 200,
			selectable 	: "row",
		    autoBind 	 : false,
		    gridCreated: function(grid){
				var filtroPunta = new Array();
	           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Punta" });
	           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda||''});
	           	dsPuntas.filter(filtroPunta); 
	    		$scope.confBasicaPunta.dataSource = dsPuntas;
		    }
       	};
		
		// GRILLA AGENTE
       	var dsAgente = new bcs.DataSource(dsDefPuntasPorTramo);
		$scope.confBasicaAgente = 
		{
				dataSource		: dsAgente,		
				columns			: [ {field: "MontoPorTramo", title: "UI_MONTO", width: "70px", align: "right", sortable : false, editable: true,
										format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
										}}
								   ,{field: "MontoCentavos", title: "UI_APERTURA_PESOS", width: "70px", align: "right", sortable : false, editable: true,
										format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto','USD','','');
										}}
				       			   ,{field: "ValorCompra", title: "UI_MESA_COMPRA", width: "70px", align: "right", sortable : false, editable: false,
					       				format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
										}}
				       			   ,{field: "ValorVenta", title: "UI_MESA_VENDE", width: "70px", align: "right", sortable : false, editable: false,
					       				format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
										}}
				       			   ,{field: "Usuario", title: "UI_USUARIO_MODIFICA", width: "70px", align: "left", sortable : false }
				       			   ,{field: "FechaModificacion", title: "UI_FECHA_MODIFICACION", width: "70px", align: "left", sortable : false }
				       			   ],		
				sortable		: true,
				pageable		: true,
				filterable		: false,
				navigatable		: true,
				excel : {
					allPages: true,
				},
				height			: 200,
				selectable 		: "row",
				autoBind 		: true,
				gridCreated: function(grid){
					var filtroPunta = new Array();
		           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Agente" });
		           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda||''});
		           	dsAgente.filter(filtroPunta); 
		    		$scope.confBasicaPunta.dataSource = dsAgente;
			    }
		};
		
		// GRILLA CLIENTE
       	var dsCliente = new bcs.DataSource(dsDefPuntasPorTramo);
		$scope.confBasicaCliente = 
		{
				dataSource		: dsCliente,
					columns		:[	{field : "MontoPorTramo", title: "UI_MONTO", width: "70px", align: "right", sortable : false, editable: true,
						       			format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
										}}
									,{field: "MontoCentavos", title: "UI_APERTURA_PESOS", width: "70px", align: "right", sortable : false, editable: true,
										format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto','USD','','');
										}}
									,{field : "ValorCompra",title:'UI_MESA_COMPRA',width : "70px",	sortable : false, editable: false,
					             		format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
										}}
									,{field : "ValorVenta", title:'UI_MESA_VENDE', width : "70px",	sortable : false, editable: false,
										format : function(value, dataItem) {
											return util.bcs_formato(value,'SPT','Monto',$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda,'','');
										}}
									,{field: "Usuario", title: "UI_USUARIO_MODIFICA", width: "70px", align: "left", sortable : false }
									,{field: "FechaModificacion", title: "UI_FECHA_MODIFICACION", width: "70px", align: "left", sortable : false }
								 ],
					
				sortable		: true,
				pageable		: true,
				filterable		: false,
				navigatable		: true,
				excel : {
					allPages: true,
				},
				height			: 200,
				selectable 		: "row",
				autoBind 		: true,
				gridCreated: function(grid){
					var filtroPunta = new Array();
		           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Cliente" });
		           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda||''});
		           	dsCliente.filter(filtroPunta); 
		    		$scope.confBasicaPunta.dataSource = dsCliente;
			    }
		};
//------------ GUARDAR PUNTAS -----
		
		//LLAMADA A SERVICIOS SP

		$scope.data.grabarPuntas = function()
	    { 
			//Crear tablas de datos de las grillas				
			var tablaDatosPuntas	= [];		
			var tablaDatosPuntasTramo = [];
			var ObjPuntasTramo = {};
			var ObjPuntas = {
							  "IdPuntas"			: varIdPunta || 0
							, "AperturaPrecio"		: $scope.data.kngAperturaPrecio
							, "IdUsuario"			: $scope.SesionIdUsuario
							, "PuntaCompra"			: $scope.data.kngPuntaCompra
							, "PuntaVenta"			: $scope.data.kngPuntaVenta
							, "IdMonedaPunta"		: $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda
							, "CheckPrecioPorTramo"	: varCheckPrecio
							, "Estado"				: varEstado	
			};
			tablaDatosPuntas.push(ObjPuntas);
		
			var RegistrosPuntaXtramo = dsPuntas.data()|| [];
			$.each(RegistrosPuntaXtramo,function(key,value){	
				var ObjPuntasTramo = {

						"IdPuntasPorTramo"		:	value.IdPuntasPorTramo || 0
						,"IdPuntas"				: 	value.IdPuntas || 0
						,"MontoPorTramo" 		:	value.MontoPorTramo	|| 0
						,"PorcentajeTramo"		:	value.PorcentajeTramo || 0
						,"MontoCentavos"  		:	value.MontoCentavos || 0
						,"ValorCompra"  		: 	0	
						,"ValorVenta"  			: 	0
						,"Estado"				:	value.Estado || 'VIG'
						,"IdUsuario"			:	$scope.SesionIdUsuario	
						,"FechaModificacion" 	:	$scope.SesionFechaSistema
						,"Canal"				:	value.Canal
			
					
					};
					tablaDatosPuntasTramo.push(ObjPuntasTramo);
			});
			
			var parametros = {};
						parametros.p_IdEmpresa          = $scope.SesionIdEmpresa;
						parametros.p_fecha				= $scope.data.kngFechaHora;
						parametros.p_IdMoneda           = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
						parametros.p_DatosPuntas      	= tablaDatosPuntas;
						parametros.p_DatosPuntasTramo   = tablaDatosPuntasTramo;
						//nuevo parametro igualar puntas

			//Se actualizan puntas en Puntas
			
						
						
			bcsComunes.log().info(JSON.stringify(parametros));	
	       	bcsServiciosMesa.CrearPuntasSpot(parametros,successGrabarPuntas,error);
	        //Se actualizan puntas en Agente
	       	var RegistrosPuntaXtramo = dsAgente.data()|| [];
			$.each(RegistrosPuntaXtramo,function(key,value){	
				var ObjPuntasTramo = {

						"IdPuntasPorTramo"		:	value.IdPuntasPorTramo || 0
						,"IdPuntas"				: 	value.IdPuntas || 0
						,"MontoPorTramo" 		:	value.MontoPorTramo	|| 0
						,"PorcentajeTramo"		:	value.PorcentajeTramo || 0
						,"MontoCentavos"  		:	value.MontoCentavos || 0
						,"ValorCompra"  		: 	0	
						,"ValorVenta"  			: 	0
						,"Estado"				:	value.Estado || 'VIG'
						,"IdUsuario"			:	$scope.SesionIdUsuario	
						,"FechaModificacion" 	:	$scope.SesionFechaSistema
						,"Canal"				:	value.Canal
			
					
					};
					tablaDatosPuntasTramo.push(ObjPuntasTramo);
			});
			
			var parametros = {};
						parametros.p_IdEmpresa          = $scope.SesionIdEmpresa;
						parametros.p_fecha				= $scope.data.kngFechaHora;
						parametros.p_IdMoneda           = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
						parametros.p_DatosPuntas      	= tablaDatosPuntas;
						parametros.p_DatosPuntasTramo   = tablaDatosPuntasTramo;
						//nuevo parametro igualar puntas
	       	bcsServiciosMesa.CrearPuntasSpot(parametros,successGrabarPuntas,error);
	       	
	        //Se actualizan puntas en Clientes
	       	var RegistrosPuntaXtramo = dsCliente.data()|| [];
			$.each(RegistrosPuntaXtramo,function(key,value){	
				var ObjPuntasTramo = {

						"IdPuntasPorTramo"		:	value.IdPuntasPorTramo || 0
						,"IdPuntas"				: 	value.IdPuntas || 0
						,"MontoPorTramo" 		:	value.MontoPorTramo	|| 0
						,"PorcentajeTramo"		:	value.PorcentajeTramo || 0
						,"MontoCentavos"  		:	value.MontoCentavos || 0
						,"ValorCompra"  		: 	0	
						,"ValorVenta"  			: 	0
						,"Estado"				:	value.Estado || 'VIG'
						,"IdUsuario"			:	$scope.SesionIdUsuario	
						,"FechaModificacion" 	:	$scope.SesionFechaSistema
						,"Canal"				:	value.Canal
					};
					tablaDatosPuntasTramo.push(ObjPuntasTramo);
			});
			
			var parametros = {};
						parametros.p_IdEmpresa          = $scope.SesionIdEmpresa;
						parametros.p_fecha				= $scope.data.kngFechaHora;
						parametros.p_IdMoneda           = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
						parametros.p_DatosPuntas      	= tablaDatosPuntas;
						parametros.p_DatosPuntasTramo   = tablaDatosPuntasTramo;
						
	       	bcsServiciosMesa.CrearPuntasSpot(parametros,successGrabarPuntas,error);
	       	
	       	
	       	$scope.data.Puntas.UpdatePuntas = true;
		};
		
		var asignarcheckbox = function(chkPrecioTramos)
		{
			if (chkPrecioTramos==='SI') 
			{
				$scope.data.PrecioTramos=true;
			}else
			{
				$scope.data.PrecioTramos=false;
			}

		};

		
//---AGREGAR NUEVA FILA A LA GRILLA PUNTAS
		//cambiar nombre Agregar Fila a las grilla //data.IngresoNuevoTramo
		$scope.data.AgregarPuntasPorTramo = function(varCanal)
	    { 
			//overlay.encendido = true;
			//Crear tablas de datos de las grillas	
			var tablaDatosPuntas	= [];		
			var tablaDatosPuntasTramo = [];
			var ObjPuntasTramo = {};
			var ObjPuntas = {
							  "IdPuntas"			: varIdPunta
							, "AperturaPrecio"		: $scope.data.kngAperturaPrecio
							, "IdUsuario"			: $scope.SesionIdUsuario
							, "PuntaCompra"			: $scope.data.kngPuntaCompra
							, "PuntaVenta"			: $scope.data.kngPuntaVenta
							, "IdMonedaPunta"		: $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda
							, "CheckPrecioPorTramo"	: varCheckPrecio
							, "Estado"				: varEstado	
			};
			tablaDatosPuntas.push(ObjPuntas);
			var dsAux;
			switch (varCanal) {
			  case 'Punta':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Punta
				  dsAux = dsPuntas;
				  break;
			  case 'Agente':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Agente
				  dsAux = dsAgente;
				  break;
			  case 'Cliente':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Cliente
				  if (varCheckIgualarMargenes === 'SI')
				  {				  dsAux = dsAgente;			}
				  else 
				  {				  dsAux = dsCliente;		}
				 break;
			}
			var RegistrosPuntaXtramo = dsAux.data()|| [];
			$.each(RegistrosPuntaXtramo,function(key,value){	
				var ObjPuntasTramo = {

						"IdPuntasPorTramo"		:	value.IdPuntasPorTramo 	|| 0
						,"IdPuntas"				: 	value.IdPuntas 			|| 0
						,"MontoPorTramo" 		:	value.MontoPorTramo		|| 0
						,"PorcentajeTramo"		:	value.PorcentajeTramo 	|| 0
						,"MontoCentavos"  		:	value.MontoCentavos 	|| 0
						,"ValorCompra"  		: 	value.ValorCompra 		|| 0	
						,"ValorVenta"  			: 	value.ValorVenta 		|| 0
						,"Estado"				:	value.Estado 			|| 0
						,"IdUsuario"			:	value.IdUsuario//$scope.SesionIdUsuario	
						,"FechaModificacion" 	:	value.FechaModificacion//$scope.SesionFechaSistema
						,"Canal"				:	varCanal
			
					
					};
					tablaDatosPuntasTramo.push(ObjPuntasTramo);
			});

			var parametros = {};
						parametros.p_IdEmpresa          = $scope.SesionIdEmpresa;
						parametros.p_fecha				= $scope.data.kngFechaHora;
						parametros.p_LimiteSuperior    	= $scope.data.kngLimiteSuperior;
						parametros.p_LimiteInferior		= $scope.data.kngLimiteInferior;
						parametros.p_IdMoneda           = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
						parametros.p_Canal				= varCanal;
						parametros.p_DatosPuntas      	= tablaDatosPuntas;
						parametros.p_DatosPuntasTramo   = tablaDatosPuntasTramo;

			bcsComunes.log().info(JSON.stringify(parametros));	
	       	bcsServiciosMesa.CrearPuntasTramo(parametros,successGrabarPuntas,error);     	
		};
		
		//------Agrega nuevo registro a la grilla--------------------
		
		var dsIngresoNuevoTramo = function(varCanal)
		{
			switch (varCanal) {
			  case 'Punta':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Punta
					dsPuntas.add
					(
						 { 
						   IdPuntas 	: varIdPunta
						 , ValorCompra	: $scope.data.kngPuntaCompra
						 , ValorVenta	: $scope.data.kngPuntaVenta
						 , IdUsuario	: $scope.SesionIdUsuario
						 , Usuario		: $scope.SesionUsuario
						 , FechaModificacion: $scope.data.kngFecha 
						 }	
					);
					//DESBLOQUEAR
					$scope.data.bloqueoGrabarPuntasTramo = true;
			  break;

			  case 'Agente':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Agente
					dsAgente.add
					(
						 { 
						   IdPuntas 	: varIdPunta
						 , ValorCompra	: $scope.data.kngPuntaCompra
						 , ValorVenta	: $scope.data.kngPuntaVenta
						 , IdUsuario	: $scope.SesionIdUsuario 
						 , FechaModificacion: $scope.data.kngFecha 
						 }	
					);
					//DESBLOQUEAR
					$scope.data.bloqueoGrabarPuntasAgente = true;
			  break;
			   
			  case 'Cliente':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Cliente
					dsCliente.add
					(
						 { 
						   IdPuntas 	: varIdPunta
						 , ValorCompra	: $scope.data.kngPuntaCompra
						 , ValorVenta	: $scope.data.kngPuntaVenta
						 , IdUsuario	: $scope.SesionIdUsuario 
						 , FechaModificacion: $scope.data.kngFecha 
						 }	
					);
					$scope.data.bloqueoGrabarPuntasCliente = true;
			  break;  
			}
		};
		
		$scope.data.IngresoNuevoTramo = function(varCanal)
		{
			dsIngresoNuevoTramo(varCanal);

		};
		
	/* Eliminar registro en grilla*/	
		$scope.data.EliminaTramo = function(row)
		{
			var dsAux;
			switch (row.Canal) {
			  case 'Punta':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Punta
				  dsAux = dsPuntas;
				  break;
			  case 'Agente':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Agente
				  dsAux = dsAgente;
					break;
			  case 'Cliente':
			    //Sentencias ejecutadas cuando el resultado de expresion coincide con Cliente
				  dsAux = dsCliente;
						break;
			}
			var tramosPunta = dsAux.remove(row);
			$scope.data.AgregarPuntasPorTramo(row.Canal);
			
		};

//------------Mensaje--------------------
		var successGrabarPuntas = function(respuesta)
		
		{
			//si vuelve ref id, se creo la asignacion
			if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
			{		
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			else
			{			
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_OPERACION_INGRESO_ERRO"));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			overlay.encendido = false;
			bcsComunes.ventanaOverlay($scope.winID, false);
		//actualiza grilla puntas con valores nuevos
			var dsPuntas2 = new bcs.DataSource(dsDefPuntasPorTramo);
			dsPuntas2.read().then(function()
			{
					var filtroPunta = new Array();
		           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Punta" });
		           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda});
		           	dsPuntas.filters(filtroPunta); 
			       	$scope.confBasicaPunta.dataSource = dsPuntas2;
			});
			
			//actualiza grilla Agente con valores nuevos
			var dsAgente2 = new bcs.DataSource(dsDefPuntasPorTramo);
			dsAgente2.read().then(function()
			{
					var filtroPunta = new Array();
		           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Agente" });
		           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda});
		           	dsAgente.filters(filtroPunta); 
			       	$scope.confBasicaAgente.dataSource = dsAgente2;
			});
			
			
			//actualiza grilla Cliente con valores nuevos
			var dsCliente2 = new bcs.DataSource(dsDefPuntasPorTramo);
			dsCliente2.read().then(function()
			{
					var filtroPunta = new Array();
		           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Cliente" });
		           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda});
		           	dsCliente.filters(filtroPunta); 
			       	$scope.confBasicaCliente.dataSource = dsCliente2;
			});
			
			/*------------------*/
			$scope.data.IgualarMargenes=false;
			$scope.data.bloqueoGrabarPuntas = false;
			$scope.data.bloqueoGrabarPuntasTramo = false;
			$scope.data.bloqueoGrabarPuntasAgente = false;
			$scope.data.bloqueoGrabarPuntasCliente = false;
		};
		/*-----Editar---------*/
		var valGrillaPunta = $scope.confBasicaPunta;
		valGrillaPunta.save = function(e)
		{
			var dataPuntas = dsPuntas.data();
			var Valores = e.valores;
			var filaActual = e.model;
			
				$scope.data.bloqueoGrabarPuntasTramo = true;
			
		};		
		
		var valGrillaAgente = $scope.confBasicaAgente;
		valGrillaAgente.save = function(e)
		{
			var dataAgente = dsAgente.data();
			var Valores = e.valores;
			var filaActual = e.model;
			
				$scope.data.bloqueoGrabarPuntasAgente = true;
			
		};		
		
		
		var valGrillaCliente = $scope.confBasicaCliente;
		valGrillaCliente.save = function(e)
		{
			var dataCliente = dsCliente.data();
			var Valores = e.valores;
			var filaActual = e.model;
			
				$scope.data.bloqueoGrabarPuntasCliente = true;
			
		};		
		
//------------		
	});
}());

