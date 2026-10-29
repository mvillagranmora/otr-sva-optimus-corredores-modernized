angular.module('ModuloPlataforma').controller('CrearIndicesCtrl',
		function($scope,$translate, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA,bcsServicioPlataforma,bcsServiciosCuentaInversion,$resource){

	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/

	$scope.filtros = {};
	$scope.data = {};

	//NUEVO
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.modo = 'INGRESAR';
	var parametros = bcsComunes.ventanaParams($scope.idVentana);	
	$scope.formData = {};
	const TRANSACCION_INSERT = "C";
	//NUEVO
	
	$scope.data.getConfigComboboxMonedaParametro={
			tipo : "combobox-moneda-parametro",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.data.getConfigComboboxMonedaValor={
			tipo : "combobox-moneda-valor",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	
	$scope.CrearIndice = function(){				
		
		 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_CREARA_UN_NUEVO_INDICE') );		
		 pregunta.result.then(
				 function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){					
					bcsComunes.ventanaOverlay($scope.IdVentana, true);
					var datos = {
							p_IdEmpresa 		: $scope.SesionIdEmpresa,
				    		p_IdUsuario 		: $scope.SesionIdUsuario,
				    		p_IdModulo 			: 'PLA',
				    		p_simularOperacion 	: 'N',
							p_UsaReglasNegocio 	: 'S',
							p_Tarea				: TRANSACCION_INSERT, 
							p_Registros : []
						};
					//$.each(IdIndices,function(key,IdIndices){
			  			datos.p_Registros.push({
			  				p_IdIndiceFinanciero : $scope.formData.Indice,
			  				p_Nombre 			 : $scope.formData.NombreIndice,
			  				p_Descripcion		 : $scope.formData.DescripcionIndice,
			  				p_IdMonedaParametro  : $scope.filtros.MonedaParametroObj.Codigo||"",
			  				p_IdMonedaValor		 : $scope.filtros.MonedaValorObj.Codigo||"",
			  				p_Periodo			 : 'DI',
			  				p_Tarea				 : TRANSACCION_INSERT			  			
			  			});
			  		//});					
					
					bcsComunes.log().info(JSON.stringify(datos));
					//LLamada a servicio carga masiva
					bcsServiciosCuentaInversion.ProcesaIndicesAtributtionAnalisys(datos,
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.IdVentana, false);							 		
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
								
								/*
								$scope.limpiarcampos();  //limpio los campos
								$scope.formData.buscar($scope.filtros);
								*/
								//NUEVO
								bcsComunes.ventanaData($scope.idVentana,{guardar : true});
								bcsComunes.ventanaCerrar($scope.idVentana);
								//NUEVO
							} else {	
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
							}
						},
						function(respuesta){
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
							bcsComunes.ventanaOverlay($scope.IdVentana, false);
						}
					);
				}
		 });		
	
			
		};	
		
		$scope.limpiarcampos = function(){
			$scope.filtros.monedaparametro = false;
			$scope.filtros.monedavalor = false;			
			$scope.formData.Indice = "";
			$scope.formData.NombreIndice = "";
			$scope.formData.DescripcionIndice = "";
		};
		let firstHeight = $('#Win_CrearIndice');
		firstHeight.css('height', '192px');
		firstHeight.css('width', '380px');
});