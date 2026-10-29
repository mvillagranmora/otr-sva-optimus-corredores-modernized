(function() {
	angular.module('ModuloMesa').controller('RutaDatatec',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
				
		var ingSpot = this;
		
		ingSpot.datosAntes = {};
		ingSpot.datosDespues = {};
		ingSpot.datosAntes = angular.copy(ingSpot.datosDespues);
		
		//Configurar al principio
		
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.MargenPunta = $scope.data.kngMargenPunta || 0;
		$scope.data.nombreParametro = parametrosVentana.nombreParametro;
		
		ingSpot.data = {}; 

		var dsDefParametro = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "", dir: "DESC" },
                    pageSize        : 100,
                    transport		: {
		        	            		read: {
		        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=432",
		        							dataType:"json"
		        	            		}
		        	            	},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													IdEmpresa				: {type: "string"},
													IdTipoProducto			: {type: "string"},
													CodigoParametro			: {type: "string"},
													Descripcion				: {type: "string"},
													Valor					: {type: "string"},
												}
											}
									  }
    			 }
		};
		
		dsParametro = new bcs.DataSource(dsDefParametro)
		dsParametro.read().then(function(){
			
			var filtroParametro = new Array();
			filtroParametro.push({ field: "CodigoParametro", operator: "eq", value: parametrosVentana.codigoParametro });
			dsParametro.filters(filtroParametro)
			dsParametro.read().then(function(){
				$scope.data.valor = dsParametro.view()[0].Valor;
			});
		});

		EditarParametro = function(datos,success_fn,error_fn){
			var urlEditarParametro = "/BSCL/Plataforma/GWJAVA/PLA/call?t=434";
			var r = $resource("", {},{
				'EditarParametro': { method: "POST", url: urlEditarParametro}
			});
			var post = r.EditarParametro(datos);
			post.$promise.then(success_fn).catch(error_fn);
		};
		
		var error = function(respuesta)
		{
			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay($scope.winID, false);
		};
		
		var successEditarParametro = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
			{		
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			else
			{		
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
				});
			}
			overlay.encendido = false;
			bcsComunes.ventanaOverlay($scope.winID, false);			
		};	
		
		$scope.data.grabarParametro = function()
	    {
			var tablaDatosParametro = [];
			
			var ObjDatosParametro = {
					"CodigoParametro"   : parametrosVentana.codigoParametro
					,"Valor"			: $scope.data.valor	
				};
			tablaDatosParametro.push(ObjDatosParametro);
			var parametros = {};
			parametros.p_datosParametro      = tablaDatosParametro;
			parametros.p_IdEmpresa           = $scope.SesionIdEmpresa;
			parametros.p_IdUsuario           = $scope.SesionIdUsuario;
			EditarParametro(parametros,successEditarParametro,error);
	    };
	});
}());