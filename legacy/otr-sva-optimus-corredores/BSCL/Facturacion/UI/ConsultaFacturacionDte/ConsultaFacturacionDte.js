angular.module('ModuloFacturacion').controller('ConsultaFacturacionDteCtrl',
	function($scope, $resource, bcsComunes) {
	//shechenleitner 24-09-2018 - IMPMBIOPT-287 - Opcion de menu Dte Connect
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.dataVentana = {};
		//Lectura de parametros configuracion facturacion "DTE_URL" - BSCL.Facturacion.BD.Vistas::ConsultarConfiguracionFacturacion
		var dataSourceConfiguracion = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true,
				serverPaging : true,
				serverSorting : true,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=5002",
						dataType : "json"
					}
				},
				schema : {
					model : {
						fields : {
							Valor : {type : "string"}
						}
					}
				},
				filter : [ {
					field : "CodigoParametro",
					operator : "eq",
					value : 'DTE_URL'
				}, {
					field : "IdEmpresa",
					operator : "eq",
					value : $scope.SesionIdEmpresa
				} ]
			}
		});
		
		dataSourceConfiguracion.fetch(function() {
			if (dataSourceConfiguracion.data().length > 0) {
				$scope.valor = dataSourceConfiguracion.data()[0].Valor;
				//console.log($scope.valor);
			}
		});
		
		$scope.dataVentana.redireccionar = function() {
			window.open($scope.valor, '_blank');
			bcsComunes.ventanaCerrar(idVentana);
		};
		
	});