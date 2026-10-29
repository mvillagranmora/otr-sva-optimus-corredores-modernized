angular.module('ModuloTesoreria').controller('EnvioDocumentosDetalleCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,
				 $translate,bcsServiciosTesoreria, bcsBlotterComprobantesGrid)
{
	var bInfoEnvioDocumentos	= this;	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.btnActivo = true;

	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.parametrosVentana = bcsComunes.ventanaParams($scope.winID);
	$scope.bInfoEnvioDocumentos	= $scope.parametrosVentana;		
	
	
	//--------------------------------------
	// CONFIGURACION DE GRILLA
	//--------------------------------------	
	
	var dsDef = bcsBlotterComprobantesGrid.dsDefEnvioDocumentosDetalle;
	dsDef.definition.filter = { field: "IdEnvioDocumento", operator: "eq", value: $scope.parametrosVentana.IdEnvioDocumento };
	
	$scope.confBasicaEnvioDocumentosDetalle = {
		dataSource			: new bcs.DataSource(dsDef),
		columns				: bcsBlotterComprobantesGrid.columnasEnvioDocumentosDetalle(),		
		sortable			: true,
		pageable			: false,
		autoBind			: true,
		filterable			: true,
		height				: 430,
		selectable 			: "multiple row",
		userSettings		: true,
		gridCreated			: function(e){ $scope.gridApi = e;},		
		rowColorBackground	: function(row) {								
								if (row.IdEstadoEnvioDocumentoDetalle == 'DEV')
									return "red";
								return "";
							  },
	};
	
	
	
	//--------------------------------------
	// FUNCIONES
	//--------------------------------------	

	
	// Permite cambiar el estado(aceptado/devuelto) del detalle del envio(documentos)
	$scope.aceptarDevolverDocumentos = function(IdEstado) 
	{
		if ($scope.seleccionEnvioDocumentosDetalle === undefined)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
		}
		else	
		{
			bcsComunes.ventanaOverlay($scope.winID, true);				
			var dataEnvioDocumento        = [];
			var dataEnvioDocumentoDetalle = [];
			
			dataEnvioDocumento.push({				
				IdEnvioDocumento			:  $scope.parametrosVentana.IdEnvioDocumento,
				IdCaja						:  $scope.parametrosVentana.IdCaja,
				IdMedioDePago 				:  $scope.parametrosVentana.IdMedioDePago,
				IdMoneda  					:  $scope.parametrosVentana.IdMoneda,
				IdEstado  					:  $scope.parametrosVentana.IdEstado				
			});			
			
			 
			$.each($scope.seleccionEnvioDocumentosDetalle, function(index, value){					
				dataEnvioDocumentoDetalle.push({IdEnvioDocumento         : value.IdEnvioDocumento, 
												IdEnvioDocumentoDetalle  : value.IdEnvioDocumentoDetalle,
												IdComprobanteFormaDePago : value.IdComprobanteFormaDePago,
												IdEstado                 : IdEstado,									 
											  });
			});
			
			
			var datos = {};
			datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
			datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
			datos.p_IdModulo                	= 'TES';
			datos.p_simularOperacion			= 'N';
			datos.p_UsaReglasNegocio			= 'S';				
			datos.p_TipoEjecucion			    = 'EST';   //EST: cambio de estado
			datos.p_EnvioDocumento 		        = dataEnvioDocumento;
			datos.p_EnvioDocumentoDetalle 		= dataEnvioDocumentoDetalle;
			
			bcsComunes.log().info(JSON.stringify(datos));
			
			bcsServiciosTesoreria.GestionEnvioDocumentos(
				datos,
				function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					 		
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						
						//Actualiza el detalle de "Envio Documentos"
						$scope.gridApi.dataSource.read();						
					} 
					else 
					{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
					}
				},
				function(respuesta){						
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			);			
		} //if ($scope.seleccionEnvioDocumentosDetalle !== undefined)
		
		$scope.seleccionEnvioDocumentosDetalle = undefined;
		
		return;	
	};	

	
});