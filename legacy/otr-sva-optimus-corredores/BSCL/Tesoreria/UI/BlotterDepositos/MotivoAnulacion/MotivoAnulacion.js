angular.module('ModuloTesoreria').controller('MotivoAnulacionCtrl',
			function($scope,$translate,$timeout,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosTesoreria, bcsBlotterDepositosGrid)
{
	
	$scope.winID 		= bcsComunes.ventanaWinId($scope);
	$scope.parametros 	= bcsComunes.ventanaParams($scope.winID);	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	
	$scope.oConfiguracionMotivoAnulacion={
		tipo : "combobox-concepto-tesoreria",
		configuracion : {},
		filtros : {
			NombreEntidad : 		{ "field": "NombreEntidad", "operator": "eq", "value": $scope.parametros.NombreEntidad },			
			TipoConceptoTesoreria : { "field": "TipoConceptoTesoreria", "operator": "eq", "value": $scope.parametros.TipoConceptoTesoreria },
			IdEstado :      		{ "field": "IdEstado", "operator": "eq", "value": "VIG" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};

	$scope.confirmar = function()
	{	
		if ($scope.oConfiguracionMotivoAnulacion.seleccionado.IdConcepto === undefined){
			bcsComunes.msg("error","error",$translate.instant('UI_DEBE_SELECCIONAR_MOTIVO_ANULACION'));
			return;
		}
		
		var MotivoAnulacion = [];
		MotivoAnulacion.push({	IdConceptoAnulacion: $scope.oConfiguracionMotivoAnulacion.seleccionado.IdConcepto, 
								Descripcion: $scope.descripcion || ''
					  		});
		
		bcsComunes.ventanaData($scope.winID,MotivoAnulacion);
		bcsComunes.ventanaCerrar($scope.winID);
	};

	$scope.cancelar = function()
	{					
		bcsComunes.ventanaData($scope.winID,[]);
		bcsComunes.ventanaCerrar($scope.winID);
	};

});