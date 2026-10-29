angular.module('ModuloMesa').controller('PosicionInicialMRFCtl',
		function($scope
				,$state
				,$timeout
				,$modal
				,$translate
				,bcsComunes 
				,bcsServiciosMesa
				) {

	
	$scope.data = {};
	$scope.grid = {};
	$scope.data.frmTipoReajuste = ' CLP ';
	$scope.data.frmTipoPosicion = 'DET';
	$scope.NombreArchivo = "Posicion MRF "+ $scope.SesionFechaSistema;
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	
	$scope.data.ConfMetodo={
			tipo : "combobox-metodo-posicion-mrf",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						IdTipoPosicion : { "field"   : "IdTipoPosicion", "operator": "eq", "value" : "INICIAL" }
					  },
			idActual : false,
			seleccionado : false,
			autoCargar: true 
	};	
	$scope.data.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};

	var dsPosicionMRF = new bcs.DataSource({
		type : "array",
		definition : {
			/*serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize 			: 25,								
		    transport: 			{
								read: {
										url:bcsConsultasMesa('ConsultarPosicionMRF'),
										dataType: "json"
									  }
								},
							
				*/
			data : [],
			schema : {
				model : {
					fields : {
								IdMetodo: {type : "number"},
								NombreMetodo : {type : "string"},
								IdTipoReajuste : {type : "string"},
								IdMesa : {type : "number"},
								NomMesa : {type : "string"},
								IdEvento : {type : "number"},
								IdTipoProducto : {type : "string"},
								IdTipoOperacion : {type : "string"},
								IdTipoEvento : {type : "string"},
								NombreEvento : {type : "string"},
								NombreAgrupacion : {type : "string"},
								IndAccion : {type : "string"},
								Debe : {type : "number"},
								Haber : {type : "number"}
					}
				}
			}
		}
	});	 
	

    $scope.grid.confPosicionMRF = {
		dataSource  : dsPosicionMRF,
		columns     : [ {field : "NombreAgrupacion",  title:"UI_ITEM", width: "50px"}
		                ,{field : "NombreEvento",  title:"UI_NOMBRE_EVENTO",  width: "100px"}
				        ,{field : "Debe", title: $translate.instant('UI_MONTO_COMPRA'), width: "80px",template : " <label> #: (util.bcs_formato(Debe,'GEN','Monto','CLP','','')).valor #</label>"}
				        ,{field : "Haber", title: $translate.instant('UI_MONTO_VENTA'), width: "80px",template : " <label> #: (util.bcs_formato(Haber,'GEN','Monto','CLP','','')).valor #</label>"}
        ],
        contextMenu : true,
        filterable  : true,     
        sortable    : true,
        pageable    : true,
        height      : 400,
        userSettings: true,
        selectable  : "row",
        autoBind    : false,
        dataBound: function(e) {
                    var gridElement = e.sender.element[0];
                    var rows = gridElement.querySelectorAll('tbody tr');
                    rows.forEach(function(row) {
                        row.addEventListener('dblclick', function() {    
                            var dataItem = e.sender.dataItem(row);							
							$scope.mostrarVentanaDetalleItem(dataItem,$scope.data.ConfMetodo.seleccionado.IdMetodo,$scope.data.ConfMetodo.seleccionado.NombreMetodo,$scope.data.frmTipoPosicion,$scope.data.fecha);
                            
                        });
                    });
        }
    }; 		


	$scope.mostrarVentanaDetalleItem = function(item,IdMetodo,NombreMetodo,TipoPosicion,Fecha){		
		bcsComunes.ventanaAbrir("DetallePosicionInicialMRF",{item,IdMetodo,NombreMetodo,TipoPosicion,Fecha},false,false,$scope.winID);		
	}



	$scope.msgWebsocket = function(message) { 
    	if(message.Evento==='ACTMRF'|| message.Evento==='ANUMRF'|| message.Evento==='MODMRF'|| message.Evento==='SIMFACT' || message.Evento==='ACTPOSMRF'){
			$scope.buscarPosicionMRF();
    	}
		
    };
    
	$scope.buscarDetalleMRF = function() {
		$scope.data.frmTipoPosicion = 'DET';
		$scope.buscarPosicionMRF();
	};
    

	$scope.buscarResumenMRF = function() {
		$scope.data.frmTipoPosicion = 'RSM';
		$scope.buscarPosicionMRF();
	};

	$scope.buscarPosicionMRF = function() {
		
		if ( $scope.data.ConfMetodo.idActual === false ) {
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_INDICAR_METODO_POSICION_MRF'));
		} else {
			var datosPosicionMRF = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'MRF',
					p_IdPais : $scope.SesionIdPaisSistema,
					p_FechaPosicion : $scope.data.kngFecha,
					p_IdMetodo : $scope.data.ConfMetodo.idActual,
					p_NombreMetodo : $scope.data.ConfMetodo.seleccionado.NombreMetodo,
					p_TipoPosicion : $scope.data.frmTipoPosicion 
			};
	
			$scope.NombreArchivo = "Posicion MRF "+ util.dateSPdate($scope.data.kngFecha);
			$scope.data.fecha = util.dateSPdate($scope.data.kngFecha);
			$scope.data.frmTipoReajuste = $scope.data.ConfMetodo.seleccionado.IdTipoReajuste;
			var p1 = bcsServiciosMesa.ConsultarPosicionMRF2(datosPosicionMRF).$promise;
			bcsComunes.ventanaOverlay($scope.winID, true);
			p1.then(function(result) {
				var PosicionMRF = [];
				for (var posIndex in result.P_OUT_POSICION)
				{
				
					var CampoPosicion = {};
						CampoPosicion.IdMetodo= result.P_OUT_POSICION[posIndex].IdMetodo;
						CampoPosicion.NombreMetodo= result.P_OUT_POSICION[posIndex].NombreMetodo;
						CampoPosicion.IdTipoReajuste= result.P_OUT_POSICION[posIndex].IdTipoReajuste;
						CampoPosicion.IdMesa= result.P_OUT_POSICION[posIndex].IdMesa;
						CampoPosicion.NomMesa= result.P_OUT_POSICION[posIndex].NomMesa;
						CampoPosicion.IdEvento= result.P_OUT_POSICION[posIndex].IdEvento;
						CampoPosicion.IdTipoProducto= result.P_OUT_POSICION[posIndex].IdTipoProducto;
						CampoPosicion.IdTipoOperacion = result.P_OUT_POSICION[posIndex].IdTipoOperacion;
						CampoPosicion.IdTipoEvento = result.P_OUT_POSICION[posIndex].IdTipoEvento;
						CampoPosicion.NombreEvento = result.P_OUT_POSICION[posIndex].NombreEvento;					
						CampoPosicion.NombreAgrupacion = result.P_OUT_POSICION[posIndex].NombreAgrupacion;
						CampoPosicion.IndAccion = result.P_OUT_POSICION[posIndex].IndAccion;
						CampoPosicion.Debe = result.P_OUT_POSICION[posIndex].Debe;
						CampoPosicion.Haber = result.P_OUT_POSICION[posIndex].Haber;
	
						PosicionMRF.push(CampoPosicion);
				}
				
				//$scope.data.frmMesaPosicion = result.P_OUT_NOMBREMESA;
				$scope.data.oConfiguracionMesas.idActual = result.P_OUT_IDMESA;
				$scope.data.frmPosicionFinal = Number(result.P_OUT_POSICIONFINAL);
				$scope.grid.confPosicionMRF.dataSource.data(PosicionMRF);
	
				bcsComunes.ventanaOverlay($scope.winID, false);
			});	
		
		};

		//var filtersCartera 	= [];
		//
		//filtersCartera.push({ field: "IdMesa", operator: "eq", value: Number($scope.data.oMesasRF.idActual) });
		//if ($scope.data.oUsuario.seleccionado){
		//	filtersCartera.push({ field: "IdUsrReserva", operator: "eq", value: Number($scope.data.oUsuario.idActual) });	
		//}
		//								
		//dsCarteraReservada.filter(filtersCartera);
		//
		
	};    
    
	
	$scope.data.kngFecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.ConfMetodo.idActual = false;
	
	
});