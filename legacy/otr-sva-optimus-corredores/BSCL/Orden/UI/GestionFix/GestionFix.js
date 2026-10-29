angular.module('ModuloOrdenes').controller('GestionFixCtrl',function ($scope,bcsComunes,bcsServicioOrden,bcsConsultasOrdenes ) {

	$scope.apiSolicitudes;
	$scope.apiRespuestas;
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.refrescar = function() {
		var data = {};
		var filters1 = [];
		var filters2 = [];
		if ($scope.IdOrden) {
			filters1.push({ field : "SEC_ORDEN" , operator : "eq", value : $scope.IdOrden  } ); 
			filters2.push({ field : "SEC_ORDEN" , operator : "eq", value : $scope.IdOrden  } ); 
		}
		if ($scope.Fecha) {
			filters1.push({ field : "IN_RUTEO" , operator : "eq", value : $scope.Fecha  } ); 
			filters2.push({ field : "FECHA" , operator : "eq", value : $scope.Fecha  } ); 
		}
		data1 = { filter : filters1 }; 
		data2 = { filter : filters2 }; 
		$scope.apiSolicitudes.dataSource.query(data1);
		$scope.apiRespuestas.dataSource.query(data2);
	};
	
	$scope.procesar = function(seleccionado) {
		
		if (!seleccionado) return;
		var datos = {
				p_NroEntrada   	:seleccionado.NRO_ENTRADA,
				p_IdOrden   	:seleccionado.SEC_ORDEN,
				p_Mensaje		:seleccionado.MENSAJE,
				p_IdEstadoFix	:seleccionado.ESTADO,
				p_Cantidad		:seleccionado.CANTIDAD,
				p_FolioTrans	:seleccionado.FOLIO_TRANS,
				//p_Fecha			:seleccionado.FECHA,
				p_Fecha			:$scope.SesionFechaSistema,
				p_TipoOperacion	:seleccionado.TIPO_OPERAC			
		}
		bcsComunes.ventanaOverlay(idVentana,true);
		bcsServicioOrden.ProcesarMensajesNegociacion(datos).$promise.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
		})
		
	};
	
	$scope.confSolicitudes = {
			gridCreated : function(e) {
				$scope.apiSolicitudes = e;
			},
			autoBind: true,
			height : 280,
			filterable : true,
			sortable : true,
			pageable : true,
			dataSource : new bcs.DataSource({
				type : "odata",
				definition : {
	  				serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true,
					sort: { field: "SECUENCIA", dir: "desc" },
					pageSize : 100,
				    transport: {
						read: {
							url:bcsConsultasOrdenes('ConsultarTBSOLICITUDES'),
							dataType: "json"
						},
					},
					schema : {
						model : {
							fields : {

							}
						}
					}
				}
				
			}),
			columns : [
			           {field: "SECUENCIA",				title: 'SECUENCIA',				width: "75px"},
			           {field: "IN_RUTEO",				title: 'IN_RUTEO',				width: "100px"},
			           {field: "OUT_RUTEO",				title: 'OUT_RUTEO',				width: "150px"},
			           {field: "PROCESADO",				title: 'PROCESADO',				width: "75px"},
			           {field: "SEC_ORDEN",				title: 'SEC_ORDEN',				width: "100px"},
			           {field: "TIPO_OPERAC",			title: 'TIPO_OPERAC',			width: "75px"},
			           {field: "ESTADO",				title: 'ESTADO',				width: "100px"},
			           {field: "NEMO",					title: 'NEMO',					width: "100px"},
			           {field: "CANTIDAD",				title: 'CANTIDAD',				width: "75px"},
			           {field: "PRECIO",				title: 'PRECIO',				width: "75px"},
			           {field: "RUT_CLI",				title: 'RUT_CLI',				width: "100px"},
			           {field: "FEC_ING_ORDEN",			title: 'FEC_ING_ORDEN',			width: "100px"},
			           {field: "IND_VALIDEZ",			title: 'IND_VALIDEZ',			width: "100px"},
			           {field: "FEC_VCTO_VALIDEZ",		title: 'FEC_VCTO_VALIDEZ',		width: "100px"},
			           {field: "CAN_ASIG_ACUM",			title: 'CAN_ASIG_ACUM',			width: "100px"},
			           {field: "BOLSA",					title: 'BOLSA',					width: "100px"},
			           {field: "CONDICION_LIQUIDACION",	title: 'CONDICION_LIQUIDACION',	width: "100px"},
			           {field: "SPONSORING_FIRM",		title: 'SPONSORING_FIRM',		width: "100px"},
			           {field: "MERCADO",				title: 'MERCADO',				width: "100px"},
			           {field: "SUB_MERCADO",			title: 'SUB_MERCADO',			width: "100px"},
			           {field: "PREPAGO",				title: 'PREPAGO',				width: "100px"},
			           {field: "TASA_MAXIMA",			title: 'TASA_MAXIMA',			width: "100px"},
			           {field: "TASA_PROPIA",			title: 'TASA_PROPIA',			width: "100px"},
			           {field: "ID_REFERENCIA",			title: 'ID_REFERENCIA',			width: "100px"},
			           {field: "FECHA_VENCIMIENTO",		title: 'FECHA_VENCIMIENTO',		width: "100px"},
			           {field: "DIVISIBLE",				title: 'DIVISIBLE',				width: "100px"},
			           {field: "OD",					title: 'OD',					width: "100px"},
			           {field: "SEC_ORDEN_OD_COMPRA",	title: 'SEC_ORDEN_OD_COMPRA',	width: "100px"},
			           {field: "SEC_ORDEN_OD_VENTA",	title: 'SEC_ORDEN_OD_VENTA',	width: "100px"},
			           {field: "OP_INTERNO",			title: 'OP_INTERNO',			width: "100px"},
			           {field: "SEC_ORDEN_2",			title: 'SEC_ORDEN_2',			width: "100px"},
			           {field: "ID_CLIENTE",			title: 'ID_CLIENTE'},
			]
	};
	
	$scope.confRespuestas = {
			gridCreated : function(e) {
				$scope.apiRespuestas = e;
			},
			autoBind: true,
			height : 280,
			filterable : true,
			sortable : true,
			pageable : true,
			selectable : "row",
			dataSource : new bcs.DataSource({
				type : "odata",
				definition : {
	  				serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true,
					sort: { field: "IN_SGC", dir: "desc" },
					pageSize : 100,
				    transport: {
						read: {
							url:bcsConsultasOrdenes('ConsultarTBRESPUESTAS'),
							dataType: "json"
						},
					},
					schema : {
						model : {
							fields : {

							}
						}
					}
				}
				
			}),
			columns : [
			           {field: "NRO_ENTRADA",			title: 'NRO_ENTRADA',			width: "75px"},
			           {field: "FOLIO_TRANS",			title: 'FOLIO_TRANS',			width: "100px"},
			           {field: "FECHA",					title: 'FECHA',					width: "100px"},
			           {field: "NEMO",					title: 'NEMO',					width: "75px"},
			           {field: "SEC_ORDEN",				title: 'SEC_ORDEN',				width: "100px"},
			           {field: "ESTADO",				title: 'ESTADO',				width: "75px"},
			           {field: "CANTIDAD",				title: 'CANTIDAD',				width: "75px"},
			           {field: "PRECIO",				title: 'PRECIO',				width: "75px"},
			           {field: "CAN_ASIG_ACUM",			title: 'CAN_ASIG_ACUM',			width: "75px"},
			           {field: "PRECIO_PROM",			title: 'PRECIO_PROM',			width: "75px"},
			           {field: "MENSAJE",				title: 'MENSAJE',				width: "300px"},
			           {field: "PROCESADO",				title: 'PROCESADO',				width: "100px"},
			           {field: "IN_SGC",				title: 'IN_SGC',				width: "125px"},
			           {field: "MONEDA",				title: 'MONEDA',				width: "100px"},
			           {field: "BOLSA",					title: 'BOLSA',					width: "100px"},
			           {field: "CONDICION_LIQUIDACION",	title: 'CONDICION_LIQUIDACION',	width: "100px"},
			           {field: "SPONSORING_FIRM",		title: 'SPONSORING_FIRM',		width: "100px"},
			           {field: "EXEC_ID",				title: 'EXEC_ID',				width: "100px"},
			           {field: "ORDER_ID",				title: 'ORDER_ID',				width: "100px"},
			           {field: "MERCADO",				title: 'MERCADO',				width: "100px"},
			           {field: "SUB_MERCADO",			title: 'SUB_MERCADO',			width: "100px"},
			           {field: "PREPAGO",				title: 'PREPAGO',				width: "100px"},
			           {field: "TASA_MAXIMA",			title: 'TASA_MAXIMA',			width: "100px"},
			           {field: "TASA_PROPIA",			title: 'TASA_PROPIA',			width: "100px"},
			           {field: "TASA_ADJUDICACION",		title: 'TASA_ADJUDICACION',		width: "100px"},
			           {field: "MEJOR_POSTURA",			title: 'MEJOR_POSTURA',			width: "100px"},
			           {field: "ID_REFERENCIA",			title: 'ID_REFERENCIA',			width: "100px"},
			           {field: "FECHA_VENCIMIENTO",		title: 'FECHA_VENCIMIENTO',		width: "100px"},
			           {field: "DIVISIBLE",				title: 'DIVISIBLE',				width: "100px"},
			           
			           {field: "OD",					title: 'OD',					width: "100px"},
			           {field: "HORA_ADJUDICACION",		title: 'HORA_ADJUDICACION',		width: "100px"},
			           {field: "SEC_ORDER_OD",			title: 'SEC_ORDER_OD',			width: "100px"},
			           {field: "TIPO_OPERAC",			title: 'TIPO_OPERAC',			width: "100px"},
			           {field: "OP_INTERNO",			title: 'OP_INTERNO',			width: "100px"},
			           {field: "SEC_ORDEN_2",			title: 'SEC_ORDEN_2',			width: "100px"},
			           {field: "ID_CLIENTE",			title: 'ID_CLIENTE',			width: "100px"},
			           

			]
	}		
});
