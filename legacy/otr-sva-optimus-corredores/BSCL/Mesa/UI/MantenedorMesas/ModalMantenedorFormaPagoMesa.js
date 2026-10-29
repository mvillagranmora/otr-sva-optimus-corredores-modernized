(function() {
	angular.module('ModuloMesa').controller('ModalMantenedorFormaPagoMesaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsConfiguracionSociodeNegocio,bcsServiciosGrillasMesaRF){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		var mMFPM 		= this;
		mMFPM.idVentana	= bcsComunes.ventanaWinId($scope);
		mMFPM.parametros	= bcsComunes.ventanaParams(mMFPM.idVentana);
		mMFPM.domVentana	= $("[ventanaid='"+mMFPM.idVentana+"']");
		mMFPM.win			= {};
		mMFPM.fn			= {};
		mMFPM.data	= {};
		var dataSourceMedioPago = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPago();
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		/*
		$scope.IdEmpresa 			= {"field": "IdEmpresa"		,	"operator": "eq", "value": $scope.$root.SesionIdEmpresa };
		$scope.IdTipoProductoMeme 	= {"field": "IdTipoProducto"	,	"operator": "eq", "value": 'MEME' };
		$scope.IdTipoProductoMerf 	= {"field": "IdTipoProducto"	,	"operator": "eq", "value": 'MERF' };
		$scope.IdTipoProductoMefw 	= {"field": "IdTipoProducto"	,	"operator": "eq", "value": 'MEFW' };
		$scope.IdEstado				= {"field": "IdEstado"		,	"operator": "eq", "value": 'ACT'}
		mMFPM.data.oConfiguracionTipoOperacion={
			tipo : "combobox-preferencia-pago-tipo-operacion",
			configuracion : {},
			filtros: {
				complejo : [ $scope.IdEmpresa, $scope.IdTipoProductoMeme, $scope.IdTipoProductoMerf, $scope.IdTipoProductoMefw, $scope.IdEstado]  ,
			  	logic: "and",
				filters: [
					{
				      logic: "or",
				      filters: [
				    	  $scope.IdTipoProductoMeme, $scope.IdTipoProductoMerf, $scope.IdTipoProductoMefw
				      ]
				    }
				    ,$scope.IdEmpresa
					,$scope.IdEstado
				]
			},
			idActual: false
		};*/
		
	
		mMFPM.data.oConfiguracionTipoProducto={
			tipo : "combobox-tipo-producto-mesa-modulo",
			configuracion : {dataValueField : "IdTipoProductoMesa"},
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			},
			idActual: false,
			idPostFiltro:false
		};
		
		mMFPM.data.oConfiguracionTipoOperacion={
			tipo : "combobox-preferencia-pago-tipo-operacion",
			configuracion : {},
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdTipoProducto 	: {field: "IdTipoProducto"	,	operator: "eq", value: "" },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			},
			idActual: false,
			idDefecto: false,
			idPostFiltro:false
		};
		
		mMFPM.data.oConfiguracionMoneda={
			tipo : "combobox-preferencia-pago-moneda",
			configuracion : {},
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdTipoProducto 	: {field: "IdTipoProducto"	,	operator: "eq", value: "" },
				IdTipoOperacion : {field: "IdTipoOperacion"	,	operator: "eq", value: "" },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			},
			idActual: false,
			idPostFiltro:false
		};
		mMFPM.data.oConfiguracionMedioDePago={
			tipo : "combobox-preferencia-pago-medio-pago",
			configuracion : {},
			filtros: {
				IdEmpresa		: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdTipoProducto 	: {field: "IdTipoProducto"	,	operator: "eq", value: "" },
				IdTipoOperacion : {field: "IdTipoOperacion"	,	operator: "eq", value: "" },
				IdMoneda 		: {field: "IdMoneda"	,	operator: "eq", value: "" },
				IdEstado		: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			},
			idActual: false,
			idPostFiltro:false
		};
		
		mMFPM.data.oConfiguracionEstado={
			tipo : "combobox-estado-mesas",
			configuracion : {dataValueField : "IdEstado"},
			filtros: {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MESA" }
			},
			idActual: false,
			idPostFiltro:false
		};
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		mMFPM.fn.aceptar = function (){
			console.log("Aceptar");
			var datos = {};
			var detalle = [];
			bcsComunes.ventanaOverlay(mMFPM.idVentana, true);
			if (mMFPM.data.oConfiguracionTipoProducto.idActual  && mMFPM.data.oConfiguracionMedioDePago.idActual &&
				mMFPM.data.Dias >= 0 && mMFPM.data.Nombre && mMFPM.data.oConfiguracionEstado.idActual){
				dataSourceMedioPago.query({filter : {field:"IdTipoProducto", operator: "eq", value: mMFPM.data.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa}}).then(function(){
					var data = dataSourceMedioPago.data(); 
					for (var i=0; i < data.length; i++){
						if (mMFPM.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion === 0 || mMFPM.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion === data[i].IdTipoOperacion){
							if (mMFPM.data.oConfiguracionMoneda.seleccionado.IdMoneda === 0 || mMFPM.data.oConfiguracionMoneda.seleccionado.IdMoneda === data[i].IdMoneda){
								if (mMFPM.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === 0 || mMFPM.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === data[i].IdMedioDePago){
									var datosFP = {};
									datosFP.IdFormaDePago			= data[i].IdPreferenciaPago + "_" + mMFPM.data.Dias;
									datosFP.IdPreferenciaPago		= data[i].IdPreferenciaPago;
									datosFP.Nombre					= mMFPM.data.Nombre;
									datosFP.Dias					= mMFPM.data.Dias
									datosFP.FechaCreacion			= util.dateSPdate(fechaSistema);
									datosFP.FechaModificacion		= util.dateSPdate(fechaSistema);
									datosFP.PorDefecto				= mMFPM.data.PorDefecto? "S":"N";
									datosFP.IdUsuarioCreador		= $scope.$root.SesionIdUsuario
									datosFP.IdEstado				= mMFPM.data.oConfiguracionEstado.idActual;
									//datosFP.IdEmpresa				= $scope.$root.SesionIdEmpresa;
									detalle.push(datosFP);
								}
							}
						}
					}
					console.log(detalle);
					datos.idEmpresa = $scope.$root.SesionIdEmpresa;
					datos.formaPago = detalle;
					datos.accion = mMFPM.parametros.Accion;
					//llamar el servicio de ingreso
					bcsServiciosMesa.CrearModificarFormaPagoMesa(datos,function(respuesta){
						console.log(respuesta);
						var id = bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},mMFPM.idVentana);
						bcsComunes.ventanaPromesa(id).then(function(result){
							bcsComunes.ventanaOverlay(mMFPM.idVentana, false);
							bcsComunes.ventanaCerrar(mMFPM.idVentana);
						});
						
					},function(value){
						console.log(respuesta);
						var id = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},mMFPM.idVentana);
						bcsComunes.ventanaPromesa(id).then(function(result){
							bcsComunes.ventanaOverlay(mMFPM.idVentana, false);
							bcsComunes.ventanaCerrar(mMFPM.idVentana);
						});
					});
				});
			}
		};
		mMFPM.fn.cancelar = function (){
			console.log("Cancelar");
			bcsComunes.ventanaOverlay(mMFPM.idVentana, false);
			bcsComunes.ventanaCerrar(mMFPM.idVentana);
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		mMFPM.fn.CargarFormaPago = function (){
			if (mMFPM.parametros.Accion === 'MODIFICAR'){
				mMFPM.data.oConfiguracionTipoProducto.idActual 		= mMFPM.parametros.dataGrilla[0].IdTipoProducto === 'MEME'?'SPOT':mMFPM.parametros.dataGrilla[0].IdTipoProducto==='MEFW'?'FWD':mMFPM.parametros.dataGrilla[0].IdTipoProducto;
				mMFPM.data.oConfiguracionTipoProducto.idPostFiltro 	= mMFPM.parametros.dataGrilla[0].IdTipoProducto === 'MEME'?'SPOT':mMFPM.parametros.dataGrilla[0].IdTipoProducto==='MEFW'?'FWD':mMFPM.parametros.dataGrilla[0].IdTipoProducto;
				mMFPM.data.oConfiguracionTipoProducto.idDefecto 	= mMFPM.parametros.dataGrilla[0].IdTipoProducto === 'MEME'?'SPOT':mMFPM.parametros.dataGrilla[0].IdTipoProducto==='MEFW'?'FWD':mMFPM.parametros.dataGrilla[0].IdTipoProducto;
				mMFPM.data.oConfiguracionTipoOperacion.idActual 	= mMFPM.parametros.dataGrilla[0].IdTipoOperacion;
				mMFPM.data.oConfiguracionTipoOperacion.idPostFiltro = mMFPM.parametros.dataGrilla[0].IdTipoOperacion;
				mMFPM.data.oConfiguracionTipoOperacion.idDefecto	= mMFPM.parametros.dataGrilla[0].IdTipoOperacion;
				mMFPM.data.oConfiguracionMoneda.idActual 			= mMFPM.parametros.dataGrilla[0].IdMoneda;
				mMFPM.data.oConfiguracionMoneda.idPostFiltro 		= mMFPM.parametros.dataGrilla[0].IdMoneda;
				mMFPM.data.oConfiguracionMoneda.idDefecto	 		= mMFPM.parametros.dataGrilla[0].IdMoneda;
				mMFPM.data.oConfiguracionMedioDePago.idActual 		= mMFPM.parametros.dataGrilla[0].IdMedioDePago;
				mMFPM.data.oConfiguracionMedioDePago.idPostFiltro 	= mMFPM.parametros.dataGrilla[0].IdMedioDePago;
				mMFPM.data.oConfiguracionMedioDePago.idDefecto	 	= mMFPM.parametros.dataGrilla[0].IdMedioDePago;
				mMFPM.data.oConfiguracionEstado.idActual 			= mMFPM.parametros.dataGrilla[0].IdEstado;
				mMFPM.data.oConfiguracionEstado.idPostFiltro 		= mMFPM.parametros.dataGrilla[0].IdEstado;
				mMFPM.data.oConfiguracionEstado.idDefecto			= mMFPM.parametros.dataGrilla[0].IdEstado;
				mMFPM.data.Dias 									= mMFPM.parametros.dataGrilla[0].Dias;
				mMFPM.data.Nombre 									= mMFPM.parametros.dataGrilla[0].NombreFormaDePago;
				mMFPM.data.PorDefecto								= mMFPM.parametros.dataGrilla[0].PorDefecto === 'S'?true:false;
				mMFPM.data.Habilita = false;
			}
			else {
				mMFPM.data.oConfiguracionTipoProducto.idActual 		= mMFPM.parametros.IdTipoProducto;
				mMFPM.data.oConfiguracionTipoProducto.idPostFiltro 	= mMFPM.parametros.IdTipoProducto;
				mMFPM.data.Habilita = true;
			}
		};
		
		mMFPM.fn.CargarFormaPago();
	});
}());