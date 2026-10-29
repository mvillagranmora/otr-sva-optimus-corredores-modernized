angular.module('ModuloTesoreria').controller('ConsultarRendicionCtrl',function($scope,$translate,bcsComunes,bcsServiciosTesoreria,bcsAlerta,OPCIONES_BCSALERTA){

	var obj = {};
	
	obj.ingresarCerrarVentana = function(id, idPadre){
		 if(id){
			 var promesa=bcsComunes.ventanaPromesa(id);                               
		     $.when(promesa).then(function(data){
		    	 $timeout(function() {
		    		 bcsComunes.ventanaCerrar(idPadre);
		    	 },20);
		     }); 
		 }else{
			 bcsComunes.ventanaCerrar(idPadre); 
		 }
	 }
	
	var winID=bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(winID);
	var rendicion = {};
	var parametrosRendicionDetalle = [];
	var parametrosRendicionCabecera = [];
	$scope.defaultOptions = {
			userSettings:true,
			sortable: true,
	  		reorderable: true,
	  		filterable: true,
			groupable: true,
			pageable: true,
			selectable: "none",
			reorderable: true,
			allowCopy: true,
			resizable: true,
	  		autoBind: false,
	  		scrollable: true,
	  		height:450,	
			dataSource: new bcs.DataSource({
				type : "array",
				definition : {
					schema : { 
						model: {
							fields : {
								Codigo 	   				: { type : "string"},
								EstadoProceso 	  	  	: { type : "string"},
								FolioAPV      			: { type : "number"},
								FolioCSC   	   			: { type : "string"},
								IdCliente      	  	   	: { type : "number"},				
								IdEstado   				: { type : "number"},			
								SumaTotal  				: { type : "number"},
								NombreCompleto			: { type : "string"},
								FolioPAC				: { type : "number"},
								DocumentoIdentidad		: { type : "string"},
								EstadoCliente			: { type : "string"},
								EstadoPortafolio		: { type : "string"},
								IdentServicio			: { type : "string"},
								EstadoPAC				: { type : "string"},
								DescripcionSolicitud	: { type : "string"},
								EstadoCargo				: { type : "string"},
								DescripcionEstadoCargo	: { type : "string"},
								EstadoRendicion			: { type : "string"},
								EstadoCSC				: { type : "string"},
								EstadoSAAPV				: { type : "string"},
								NombreBanco				: { type : "string"},
								CuentaBancaria			: { type : "string"},
								MontoDeposito  			: { type : "number"},
								CodigoPortafolio		: { type : "string"},
								FechaSuscripcion		: { type : "sysdate"},
								DiaDescuento  			: { type : "number"},
								IdMandatoPACUI  		: { type : "number"},
							}
						}
					}			
				}
			}),
			columns: [
					{field: "IdentServicio",title: $translate.instant('UI_MANDATO_AGRUPADOR'),width: "200px",align: "left"},
					{field: "NombreCompleto",title: $translate.instant('UI_CLIENTE'),width: "150px",align: "left"},
					{field: "DocumentoIdentidad",title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),width: "100px",align: "left"},
					{field: "EstadoCliente",title: $translate.instant('UI_ESTADO_CLIENTE'),width: "100px",align: "left"},
					{field: "EstadoPortafolio",title: $translate.instant('UI_ESTADO_PORTAFOLIO'),width: "100px",align: "left"},
					{field: "FolioAPV",title: $translate.instant('UI_FOLIO_APV'),width: "100px",align: "left"},
					{field: "FolioCSC",title: $translate.instant('UI_ID_CONTRATO'),width: "75px",align: "left"},
					{field: "FolioPAC",title: $translate.instant('UI_FOLIO_PAC'),width: "100px",align: "left"},
					{field: "EstadoPAC",title: $translate.instant('UI_ESTADO_PAC'),width: "100px",align: "left"},
					{field: "SumaTotal",title: $translate.instant('UI_SUMA_TOTAL'),width: "100px",align: "left"},
					{field: "EstadoCargo",title: $translate.instant('UI_ESTADO_CARGO'),width: "100px",align: "left"},
					{field: "EstadoRendicion",title: $translate.instant('UI_ESTADO_RENDICION'),width: "250px",align: "left"},
					{field: "EstadoSAAPV",title: $translate.instant('UI_ESTADO_SAAPV'),width: "100px",align: "left"},
					{field: "EstadoCSC",title: $translate.instant('UI_ESTADO_CSC'),width: "100px",align: "left"},
					{field: "EstadoProceso",title: $translate.instant('UI_ESTADO_PROCESO'),width: "100px", align: "left"},
					{field: "NombreBanco",title: $translate.instant('UI_BANCO'),width: "200px",align: "left"},
					{field: "CuentaBancaria",title: $translate.instant('UI_CUENTA_BANCARIA'),width: "200px",align: "left"},
					{field: "MontoDeposito",title: $translate.instant('UI_MONTO_DEPOSITO'),width: "100px",align: "left"},
					{field: "CodigoPortafolio",title: $translate.instant('UI_CODIGO_PORTAFOLIO'),width: "100px",align: "left"},
					{field: "IdMandatoPACUI",title: $translate.instant('UI_ID_MANDATO_PAC'),width: "100px",align: "left"},
					{field: "FechaSuscripcion",title: $translate.instant('UI_FECHA_SUSCRIPCION'),width: "100px",align: "left"},
					{field: "DiaDescuento",title: $translate.instant('UI_DIA_DESCUENTO'),width: "100px",align: "left"},
					
			],
			gridCreated : function(grid) {
				rendicion.grid = grid;
				rendicion.grid.dataSource.data(parametros.Solicitudes);
				rendicion.grid.refresh();
			}
	};
	
	console.log("solicitudes: ", parametros.Solicitudes);
	
	parametros.Parametros.p_simular = 'N';
	$scope.isVisible = true;
	
	$scope.procesarArchivo = function(){
		var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );
		
		
		pregunta.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				parametrosRendicionCabecera = parametros.Parametros.p_CargaMasiva;
				
				for(i = 0; i < parametros.Solicitudes.length; i++){
					if (parametros.Solicitudes[i].EstadoProcesoRendicion != '') continue;
					for(j=0; j < parametros.Parametros.p_CargaMasivaDetalle.length; j++){
						if (parametros.Parametros.p_CargaMasivaDetalle[j].TipoRegistro == 'D' && parametros.Solicitudes[i].IdentServicio.trim() == parametros.Parametros.p_CargaMasivaDetalle[j].IdentServicio.trim()){
							parametrosRendicionDetalle.push({  CodigoBanco: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoBanco.toString(),	
							    CodigoEmpresa: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoEmpresa.toString(),
							    CodigoConvenio: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoConvenio.toString(),		
							    TipoRegistro: parametros.Parametros.p_CargaMasivaDetalle[j].TipoRegistro.toString(),
							    IdentServicio: parametros.Parametros.p_CargaMasivaDetalle[j].IdentServicio.toString(), 
							    Filler1: "",  			
							    MontoCargo: parametros.Parametros.p_CargaMasivaDetalle[j].MontoCargo.toString(),
							    FechaVencimiento: parametros.Parametros.p_CargaMasivaDetalle[j].FechaVencimiento.toString(),
							    FechaCargo: parametros.Parametros.p_CargaMasivaDetalle[j].FechaCargo.toString(),
							    EstadoCargo: parametros.Parametros.p_CargaMasivaDetalle[j].EstadoCargo.toString(),
							    CodigoRechazo: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoRechazo.toString(),
							    InfAdicional: parametros.Parametros.p_CargaMasivaDetalle[j].InfAdicional.toString(),
							    Filler:""})
						}
					}
				}
				
				var params = {				
						p_IdEmpresa  : $scope.SesionIdEmpresa,
						p_ValorNuevo  : "S",
				};
				
				
				bcsServiciosTesoreria.ValidaEjecucionProcesoRecPAC(params).$promise.then(function(resp){
					bcsComunes.setConflictos(util.parseServerResponse(resp));
					
					if (resp.P_OUT_ERRORESCONTADOR === 0){
							parametros.Parametros.p_CargaMasiva = [];
							parametros.Parametros.p_CargaMasivaDetalle = [];
							parametros.Parametros.p_CargaMasiva = parametrosRendicionCabecera;
							parametros.Parametros.p_CargaMasivaDetalle = parametrosRendicionDetalle;
							
							if(parametrosRendicionDetalle.length > 0){
								bcsComunes.ventanaOverlay(winID, true);
								bcsComunes.log().info(JSON.stringify(parametros.Parametros));
								bcsServiciosTesoreria.CrearCargaMasivaPacRen(parametros.Parametros,
										function(respuesta){
										bcsComunes.ventanaOverlay(winID, false);
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("UI_PROCESO_AGENDADO"),
											conflictos:util.parseServerResponse(respuesta)
										},winID);
										bcsComunes.ventanaOverlay(winID, false);},
										function(respuesta){
											bcsComunes.ventanaOverlay(winID, false);
											bcsComunes.ventanaMsg({
												mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
												conflictos:util.parseServerResponse(respuesta)
											},winID);
											bcsComunes.ventanaOverlay(winID, false);
											
											var params2 = {				
												p_IdEmpresa  : $scope.SesionIdEmpresa,
												p_ValorNuevo  : "N",
											};
									
											bcsServiciosTesoreria.ValidaEjecucionProcesoRecPAC(params2);						
										}	
								)
							}
							else{
								bcsComunes.ventanaOverlay(winID, false);
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_SIN_REGISTROS")
								},winID);
								bcsComunes.ventanaOverlay(winID, false);

					   			var params2 = {				
												p_IdEmpresa  : $scope.SesionIdEmpresa,
												p_ValorNuevo  : "N",
												};
									
								bcsServiciosTesoreria.ValidaEjecucionProcesoRecPAC(params2);
							}
					}else{
						var ErrorDetalle = resp.P_OUT_MENSAJES || [];
						$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas :");
						});
						bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas :" ),conflictos:util.parseServerResponse(resp)},winID);
						bcsComunes.ventanaOverlay(winID,false);
				    }
				});	
					
				
				
			}
		})
	}
	

	
});
