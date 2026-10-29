angular.module('ModuloMesa').controller('ConsultarVencimientosOperacionesMesaRFCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa,$http,bcsServiciosGrillasMesaRF,bcsAlerta,OPCIONES_BCSALERTA)
{	
	var conVencimiento = this;
	$scope.show = false;
	$scope.dataVencimiento = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.dataVencimiento.confBasicaOperacionesMesaRF = {};
	
	// --------------------------------------------------
	// CONFIGURACION DE GRILLAS
	// --------------------------------------------------
	var promise = Promise.resolve(bcsServiciosGrillasMesaRF.getConfiguracionOperacionesMesaRF_Vencimiento());
	promise.then(function(confGridOperaciones){
		$scope.dataVencimiento.confBasicaOperacionesMesaRF = confGridOperaciones.options;	
		$scope.dataVencimiento.buscarOperacionesMesaRF = confGridOperaciones.buscar;
		$scope.show = true;
	});
	
	
	var fechaVencimientos = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	var dataSourceConsultarEstadoMesa = bcsServiciosGrillasMesaRF.getDataSourceConsultarEstadoMesa();
	$scope.NombreArchivo = "ConsultaVencimientosOperacionesMesaRF_"+$scope.SesionFechaSistema;
	$scope.dataVencimiento.SesionIdEmpresa = $scope.SesionIdEmpresa;
	//var fechaVencimientos = util.date($scope.SesionFechaSistema).startOf('day').add(1,'d').toDate();
	
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.dataVencimiento.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};
	
	$scope.$watch('dataVencimiento.oConfiguracionMesas.seleccionado', function(newval,oldval ){
		if ($scope.dataVencimiento.oConfiguracionMesas.seleccionado) {
			$scope.blotterOpMesa.IdMesa = $scope.dataVencimiento.oConfiguracionMesas.seleccionado.IdMesa;
			if ($scope.dataVencimiento.gridApiOperacionesMesaRF != undefined) $scope.blotterOpMesa.VENBUSCA();
		}
		
	});
	$scope.$watch('blotterOpMesa.IdMesa', function(newval,oldval ){
		if ($scope.blotterOpMesa.IdMesa) {
			$scope.dataVencimiento.oConfiguracionMesas.idActual = $scope.blotterOpMesa.IdMesa;
		}
		
	});
	
	$scope.dataVencimiento.kngFechaOperacion = fechaVencimientos;
	$scope.dataVencimiento.kngFechaOperacionFin = fechaVencimientos;
	
	$scope.dataVencimiento.Modo = 'VEN';
	$scope.blotterOpMesa.VENBUSCA = function(){
		$scope.dataVencimiento.buscarOperacionesMesaRF($scope.dataVencimiento.gridApiOperacionesMesaRF,$scope.dataVencimiento);
	};
	$scope.dataVencimiento.cargaGrillas = function(){
		$scope.dataVencimiento.buscarOperacionesMesaRF($scope.dataVencimiento.gridApiOperacionesMesaRF,$scope.dataVencimiento);
		$scope.blotterOpMesa.OPEBUSCA();
		$scope.blotterOpMesa.ANTBUSCA();
	};
	$scope.dataVencimiento.detalleOperacion = function(datos){
		bcsComunes.ventanaAbrir("DetalleOperacionMesaRF", {
					IdOperacion: $scope.dataVencimiento.seleccionOperacionesMesaRF[0].IdOperacion,
					IdTipoProducto : $scope.dataVencimiento.seleccionOperacionesMesaRF[0].IdTipoProducto,
				},false);
		/*$scope.Estado = "CONSULTAR";
		if ($scope.data.seleccionOperacionesMesaRF[0].IdEstado == 'PEN') 
			$scope.Estado = "MODIFICAR";
		bcsComunes.ventanaAbrir("IngresoOperacionMesaRF", 
				{IdMesa: $scope.data.oConfiguracionMesas.seleccionado.IdMesa, 
			     IdClienteCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdCliente,
				 IdPortafolioCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio,
				 FechaOperacion: $scope.data.kngFechaOperacion,
				 IdOperacion: $scope.data.seleccionOperacionesMesaRF[0].IdOperacion,
				 Accion: $scope.Estado
				});*/
	};
	
	var success = function(respuesta)
	{
		var operaciones = [];
		var id;
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{		
			$.each(respuesta.P_OUT_OPERACIONES,function(key,value) {
				operaciones.push({id: value.IdOperacion , resultado: $translate.instant('UI_EXITO'), mensaje: $translate.instant('UI_OPERACION_REVERSA_ANTICIPO_EXITO')});
			});
	
			id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
				tituloVentana : $translate.instant("UI_MESA_RENTA_FIJA"), 
				nombreEntidad : $translate.instant("UI_SECUENCIA"), 
				operaciones : operaciones   
			},false,false,$scope.winID);
			
		}
		else
		{			
			id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		}
		
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.dataVencimiento.cargaGrillas();
		});
		
	};
	var error = function(respuesta)
	{		
		//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_REVERSA_ANTICIPO_ERROR'));
		//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		//bcsComunes.ventanaOverlay($scope.winID, false);
		var id = bcsComunes.ventanaMsg({
						//mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
	};
	// --------------------------------------------------
	// INGRESO DE REVERSA ANTICIPO OPERACION
	// --------------------------------------------------
	$scope.crearReversaAnticipoOperacion = function() {
		var datos = {};
		if ($scope.dataVencimiento.seleccionOperacionesMesaRF === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_OPERACION'));		
		}
		else
		{	
			var Estado = $scope.dataVencimiento.seleccionOperacionesMesaRF[0].IdEstado;
			if ( Estado !== "ANT" ) {
				bcsComunes.msg("warning","info",$translate.instant('UI_OPERACION_INVALIDA'));
			}
			else
			{
				if ($scope.dataVencimiento.seleccionOperacionesMesaRF.length === 1 ) {
					dataSourceConsultarEstadoMesa.query({filter : {field:"IdMesa", operator: "eq", value: Number($scope.dataVencimiento.oConfiguracionMesas.idActual)}}).then(function(){
						var data = dataSourceConsultarEstadoMesa.data();
						if (data.length > 0){
							if (data[0].TipoEstadoModulo === 'C'){
								bcsComunes.msg("warning","info",$translate.instant('UI_MODULO_CERRADO'));
								return;
							}
							if (data[0].IdEstadoMesa === 'C'){
								bcsComunes.msg("warning","info",$translate.instant('MESA_RF_CERRADA'));
								return;
							}
							if (data[0].FechaActualSistema !== $scope.SesionFechaSistema){
								//bcsComunes.msg("warning","info",$translate.instant('FECHA_PROCESO_INVALIDA'));
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse({P_OUT_ERRORESDETALLE : [{Codigo:'FECHA_PROCESO_INVALIDA', Descripcion:$translate.instant('FECHA_PROCESO_INVALIDA')}]})
								},$scope.winID);
								return;
							}
							var msg = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_MENSAJE_REVERSA_ANTICIPO_OPERACION')+' . '+$translate.instant('UI_CONTINUAR'));
							msg.result.then(function(result){
								bcsComunes.ventanaOverlay($scope.winID, true);
								if (result===OPCIONES_BCSALERTA.ACEPTAR) {
									datos.p_IdEmpresa = $scope.SesionIdEmpresa;
									datos.p_IdUsuario = $scope.SesionIdUsuario;
									datos.p_IdModulo = 'MRF';
									datos.p_UsaReglasNegocio = 'S';
									datos.p_IdPais = $scope.$root.SesionIdPaisSistema;
									datos.p_Operaciones = [];
									$.each($scope.dataVencimiento.seleccionOperacionesMesaRF,function(key,value) {
										var ope = {};
										ope.IdOperacion = value.IdOperacion;
										ope.IdEstadoSiguiente = 'VIG';
										datos.p_Operaciones.push(ope);
									});
									
									
									bcsServiciosMesa.ReverzarAnticipoOperacionesMesaRF(datos,success,error);
								}
								else {
									bcsComunes.ventanaOverlay($scope.winID, false);
								}
							});
						}
					});
				}
				else {
					bcsComunes.msg("error","error",$translate.instant('UI_SELECCIONAR_UN_ANTICIPO'));
				}
			};
		};
		
	};
	// --------------------------------------------------
	// INGRESO DE RENOVACIO OPERACION
	// --------------------------------------------------
	$scope.crearRenovacionOperacion = function() {
		if ($scope.dataVencimiento.seleccionOperacionesMesaRF === undefined || $scope.dataVencimiento.seleccionOperacionesMesaRF.length != 1 )
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_OPERACION'));		
		}
		else
		{	
			var Estado = $scope.dataVencimiento.seleccionOperacionesMesaRF[0].IdEstado;
			var FechaLiquidacion = $scope.dataVencimiento.seleccionOperacionesMesaRF[0].FechaLiquidacion;
			if ( Estado !== "LIQ" ) {
				bcsComunes.msg("warning","info",$translate.instant('UI_OPERACION_INVALIDA'));
			}
			else
			{	
				dataSourceConsultarEstadoMesa.query({filter : {field:"IdMesa", operator: "eq", value: Number($scope.dataVencimiento.oConfiguracionMesas.idActual)}}).then(function(){
					var data = dataSourceConsultarEstadoMesa.data();
					if (data.length > 0){
						if (data[0].TipoEstadoModulo === 'C'){
							bcsComunes.msg("warning","info",$translate.instant('UI_MODULO_CERRADO'));
							return;
						}
						if (data[0].IdEstadoMesa === 'C'){
							bcsComunes.msg("warning","info",$translate.instant('MESA_RF_CERRADA'));
							return;
						}
						if (data[0].FechaActualSistema !== $scope.SesionFechaSistema){
							//bcsComunes.msg("warning","info",$translate.instant('FECHA_PROCESO_INVALIDA'));
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse({P_OUT_ERRORESDETALLE : [{Codigo:'FECHA_PROCESO_INVALIDA', Descripcion:$translate.instant('FECHA_PROCESO_INVALIDA')}]})
							},$scope.winID);
							return;
						}
						var id1=bcsComunes.ventanaAbrir("ModificaOperacionMesaRF", 
								{Operacion : $scope.dataVencimiento.seleccionOperacionesMesaRF[0],
								 Accion: "RENOVAR"
								},false,false,$scope.winID,false, {title : $translate.instant('UI_RENOVAR_OPERACION')})
						var promesa=bcsComunes.ventanaPromesa(id1);
						//bcsComunes.ventanaAbrir("IngresoOperacion", {} )
								
				   		$.when(promesa).then(function(data){
				   			$scope.dataVencimiento.cargaGrillas();
				      	});
					}
				});
			}
		}
	};
	
	/**********************************************************
	 * 					PAUTA
	 * ********************************************************/
	
	$scope.EjecutarPauta = function (){
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var fecha = $scope.SesionFechaSistema;
		var operaciones=[];
		
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'PLA',	
				p_Fecha		: fecha,
				p_Pais		: 44,
				p_SimularOperacion :'N',
				p_IdTipoVencimiento:'PAC'
		}

		//Vencimiento PACTO
		bcsServiciosMesa.EjecutarVencimientosPautaMesa(datos).$promise.then(
		function(result) {
			
			if(result.P_OUT_ERRORESCONTADOR===0){
				operaciones.push({id:"Vencimiento Pactos" , resultado:"Ok" ,mensaje:result.P_OUT_DESCRIPCION});			
			}else{
				operaciones.push({id:"Vencimiento Pactos"	  , resultado:"Error" ,mensaje:"Vencimiento con errores"});
			}
			
			//Vencimiento IF
			datos.p_IdTipoVencimiento='IF';
			bcsServiciosMesa.EjecutarVencimientosPautaMesa(datos).$promise.then(
			function(result) {
				if(result.P_OUT_ERRORESCONTADOR===0){
					operaciones.push({id:"Vencimiento IF" , resultado:"Ok" ,mensaje:result.P_OUT_DESCRIPCION});			
				}else{
					operaciones.push({id:"Vencimiento IF"	  , resultado:"Error" ,mensaje:"Vencimiento con errores"});
				}
				
				//Vencimient RF
				datos.p_IdTipoVencimiento='RF';
				
				bcsServiciosMesa.EjecutarVencimientosPautaMesa(datos).$promise.then(
						function(result) {
							if(result.P_OUT_ERRORESCONTADOR===0){
								operaciones.push({id:"Vencimiento RF" , resultado:"Ok" ,mensaje:result.P_OUT_DESCRIPCION});			
							}else{
								operaciones.push({id:"Vencimiento RF"	  , resultado:"Error" ,mensaje:"Vencimiento con errores"});
							}
							
							bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
								tituloVentana : "Vencimientos Pauta", 
								nombreEntidad : "Vencimiento", 
								operaciones : operaciones   
							},false,false,$scope.winID);
						},
						function(result){
							bcsComunes.ventanaOverlay($scope.winID,false);
							operaciones.push({id:"Vencimiento RF"	  , resultado:"Error" ,mensaje:"Vencimiento con errores"});
						});
						
						
				
			},
			function(result){
				bcsComunes.ventanaOverlay($scope.winID,false);
				operaciones.push({id:"Vencimiento IF"	  , resultado:"Error" ,mensaje:"Vencimiento con errores"});
			});

		},
		function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			operaciones.push({id:"Vencimiento Pactos"	  , resultado:"Error" ,mensaje:"Vencimiento con errores"});
		});
		
		
		
		
		
	};
	
	
var array = [
		{ Estado: 'Vigente', Color: 'black' },
		{ Estado: 'Anticipado', Color: 'light-green-font' },
	    { Estado: 'Liquidado', Color: 'green' }
	    

	];
	
		bcsComunes.tootipGlosa('toolConsultarVencimientosOperacionesMesaRF', array)		
	
	
	
	
	
});