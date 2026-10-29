(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaTarjetasBancariasEnrolCtrl',
	function($scope,$translate,bcsComunes,servicioSociodeNegocio){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppca = this;
		ppca.idVentana = bcsComunes.ventanaWinId($scope);
				
		ppca.abrirModal = function(xIdEnrolamiento,xTarjetaBancaria, xCodigoFormulario)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarEnrolamientoTarjetaBancaria",{ TarjetaBancaria: xTarjetaBancaria , IdEnrolamiento: xIdEnrolamiento, CodigoFormulario: xCodigoFormulario },false,false,ppca.idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true)
				{
					ppca.TarjetaBancariaSeleccionada = undefined;
					ppca.TarjetaBancariaOptions.dataSource.read();
				}
	    	}); 
		};
		
		
		ppca.agregar = function() {
			ppca.abrirModal(ppca.IdEnrolamiento,undefined,ppca.CodigoFormulario);
		};
		
		ppca.eliminar = function(cta) {
			var datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'SOC',
				p_UsaReglasNegocio 		: 'S',
				p_CodigoFormulario		: ppca.CodigoFormulario,
				p_IdTipoCanal			: 'OPT',
				p_TarjetasBancarias		: [{
					IdInterno : 1,
					IdTarjetaBancaria : cta.IdTarjetaBancaria || 0,
					IdEnrolamiento : ppca.IdEnrolamiento,
					NumeroTarjetaBancaria : cta.NumeroTarjetaBancaria || ""
				}]	
			};
			
			bcsComunes.ventanaOverlay(ppca.idVentana,true);
			
			servicioSociodeNegocio.eliminarBorradorTarjetaBancaria(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(ppca.idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.msg("error","error","Error al borrar tarjeta bancaria");
						return;
					}
					
					bcsComunes.msg("info","info","Tarjeta bancaria borrada");
					
					bcsComunes.ventanaMsg({ mensaje : "Tarjeta Bancaria borrada",conflictos : util.parseServerResponse(result)}, ppca.idVentana);
					ppca.TarjetaBancariaSeleccionada = undefined;
					ppca.TarjetaBancariaOptions.dataSource.read();
				},
				function(){
					bcsComunes.msg("error","error","Error al borrar tarjeta bancaria");
				}
			);		    
		};
		
		ppca.modificar = function(cta) {
			ppca.abrirModal(ppca.IdEnrolamiento,cta,ppca.CodigoFormulario);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppca.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
			ppca.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['TARJETA_BANCARIA'];
			ppca.TarjetaBancariaOptions = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700210",
								dataType: "json"
							}
						},
						filter: { field: "IdEnrolamiento", operator: "eq", value: Number(ppca.IdEnrolamiento) },
						schema: {
							model: {
								fields: {
									IdTarjetaBancaria : { type: "number"},
									IdBanco : { type: "number"},
									IdTipoTarjetaBancaria: { type: "string"},
									NumeroTarjetaBancaria : { type: "string"},
									IdEnrolamiento: { type: "number"},
									FechaVencimiento: { type: "date"},							
									CodigoVerificacion: { type: "number"},
									NombreBanco : { type : "string"},
									NombreTipoTarjetaBancaria : { type : "string"},
								}
							}
						}
					}
				}),
				reorderable: true,
				filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				columnMenu: false,
				resizable: true,
				autoBind: true,
				scrollable: true,
				height:200,
				columns: [{
					field: "IdTarjetaBancaria",
					title: $translate.instant('UI_TARJETA_BANCARIA'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},
				{
					field: "NombreBanco",
					title: $translate.instant('UI_ID_BANCO'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},
				{
					field: "NombreTipoTarjetaBancaria",
					title: $translate.instant('UI_TIPO_TARJETA_BANCARIA'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},
				{
					field: "NumeroTarjetaBancaria",
					title: $translate.instant('UI_NUMERO_TARJETA'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},		
				{
					field: "FechaVencimiento",
					title: $translate.instant('UI_FECHA_VENCIMIENTO'),
					attributes: {style:"text-align:left;"},
					format: "{0:d}",
					width: "150px"
				},
				
				{
					field: "CodigoVerificacion",
					title: $translate.instant('UI_CODIGO_VERIFICACION'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				}]		
			};
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------