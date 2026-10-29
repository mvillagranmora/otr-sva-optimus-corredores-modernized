angular.module('ModuloPlataforma').controller('MantenedorContratosCtrl',function($scope,$translate, bcsComunes,bcsServicioPlataforma,OPCIONES_BCSALERTA,bcsAlerta,$resource,$interval){
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.data = {};
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	$scope.configuracion = {
		FiltrosApoderado : {
			IdPersonaOrigen : {field: "IdPersonaOrigen", operator: "eq", value: $scope.SesionIdPersonaCorredora},
			IdTipoRelacion : {field: "IdTipoRelacion", operator: "eq", value: 'APOFIR'}
		}
	}
	
	
	var modificarDatosContratosSP = function(v_Datos)
    {
		var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1420";
		var r = $resource("", {}, {
				'url': { method: "POST", url: url}
              }
		);
		var post = r.url(v_Datos);
		return post.$promise;
    };
	
	$scope.guardar = function()
	{
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'PLA';
        
        parametrosSP.p_RazonSocial = $scope.data.RazonSocial;
        parametrosSP.p_RUT = $scope.data.RUT;
        parametrosSP.p_Direccion = $scope.data.Direccion;
        parametrosSP.p_Telefono = $scope.data.Telefono;
        parametrosSP.p_Email = $scope.data.Email;
        parametrosSP.p_Fax = $scope.data.Fax;
        parametrosSP.p_Apoderado1 = $scope.data.PrimerApoderado ? $scope.data.PrimerApoderado.NombreCompleto : '';
        parametrosSP.p_DocumentoApoderado1 = $scope.data.PrimerApoderado ? $scope.data.PrimerApoderado.DocumentoIdentidadDefecto : '';
        parametrosSP.p_Apoderado2 = $scope.data.SegundoApoderado ? $scope.data.SegundoApoderado.NombreCompleto : '';
        parametrosSP.p_DocumentoApoderado2 = $scope.data.SegundoApoderado ? $scope.data.SegundoApoderado.DocumentoIdentidadDefecto : '';
		parametrosSP.p_IdPersonaApoderado1 = $scope.data.PrimerApoderado ? $scope.data.PrimerApoderado.IdPersonaDestino : 0;
        parametrosSP.p_IdPersonaApoderado2 = $scope.data.SegundoApoderado ? $scope.data.SegundoApoderado.IdPersonaDestino : 0;
        
        
        bcsComunes.ventanaOverlay(idVentana,true);   	
        modificarDatosContratosSP(parametrosSP).then(
			function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
				}else if (result.P_OUT_ERRORESCONTADOR){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				}else
					{
						bcsComunes.msg("info","info","Datos Guardados");
						var filterDatos = [];
						filterDatos.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
						DatosDatasource.query({filter:filterDatos}).then(function(e){
							console.log(DatosDatasource.data());
							$scope.dataOriginal = angular.copy(DatosDatasource.data()[0]);
							$scope.data = angular.copy(DatosDatasource.data()[0]);		
						});	
					}
			},
			function(result){
				bcsComunes.ventanaOverlay(idVentana,false);	
				bcsComunes.msg("error","Error","La operacion tiene errores");
			});
	};
	
	
	
	/////////// INICIO PANTALLA /////////////
	var DatosDatasource = new bcs.DataSource({
			type: "odata",
			definition:{
				serverFiltering:true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1414",
						dataType: "json"
					}
				},
				schema: {
					model:
					{
						fields : {
								Direccion : {type:"string"},
								Email: {type:"string"},
								Fax: {type:"string"},
								IdEmpresa: {type:"number"},
								RUT: {type:"string"},
								RazonSocial: {type:"string"},
								Apoderado1 : {type:"string"},
								DocumentoApoderado1: {type:"string"},
								Apoderado2: {type:"string"},
								DocumentoApoderado2: {type:"string"},
								IdPersonaApoderado1: {type:"number"},
								IdPersonaApoderado2: {type:"number"},
						}
					}
				},
//				filters : [
//							{ field: "IdEmpresa", operator: "eq", value:  $scope.$root.SesionIdEmpresa },
//						],
			},
		
		});

	$scope.start = function(){
	
		var filterDatos = [];
		filterDatos.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
		bcsComunes.ventanaOverlay(idVentana,true);
		DatosDatasource.query({filter:filterDatos}).then(function(e){
			console.log(DatosDatasource.data());
			$scope.dataOriginal = angular.copy(DatosDatasource.data()[0]);
			$scope.data = angular.copy(DatosDatasource.data()[0]);
			$scope.data.stop = $interval(function(){
				if($scope.data.IdEmpresa){
					bcsComunes.ventanaOverlay(idVentana,false);
					$interval.cancel($scope.data.stop);
				}
			},100);
		});	
	
	};
	
	$scope.start();
	
});