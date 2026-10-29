(function() {
	angular.module('ModuloMesa').controller('Devengo',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.data.LimiteFechaProceso = {};
		$scope.data.LimiteFechaProceso.min = new Date(util.dateInicioDia($scope.SesionFechaSistema));
		$scope.data.kngFechaProceso = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		
		$scope.DevengoForward = function(){
			
			var datosDevengo = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdPais			: $scope.SesionIdPaisSistema,
					p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo			: 'MFW',
					p_IdBolsa			: 'BCS',
					p_IdSucursal		: 1,
					p_UsaReglasNegocio 	: 'N',
					p_FechaProceso		: util.dateSPdate($scope.data.kngFechaProceso)
			};
			
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.DevengoForward(datosDevengo).$promise.then(function(respuesta){
	   			
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								
				//se valido la orden
				if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
				{
					bcsComunes.msg("info","Informacion","Operaciones calzadas correctamente.");
				}
				else
			    {
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
//					var ObjErrores = Array();
					$.each(ErrorDetalle,function(key,value){	
//						ObjErrores.push({field : "Código" , value  : value.Codigo}
//									   ,{field : "Descripción" , value   : value.Descripcion});
						bcsComunes.msg("error","Error","Existen problemas al realizar el proceso de Devengo de Forward. : " + value.Codigo + " - " + value.Descripcion);
					});
			    }
				$scope.HabilitoBotonCalce = false;
				bcsComunes.ventanaOverlay(idVentana,false);   			
	   		},function(respuesta){
	   			//ERROR AL CREAR LA ORDEN
	   			$scope.HabilitoBotonCalce = false;
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("error","Error","Existen problemas al realizar el proceso de Devengo de Forward.");
	   		});

		};
		
	});
}());

