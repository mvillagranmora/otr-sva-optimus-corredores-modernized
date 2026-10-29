(function() {
	angular.module('ModuloMesa').controller('LimitesSpotCorredora',
	function(
		$scope,$translate,$resource,$timeout,$http,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,
		bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA
		)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoHeight(idVentana);
		$scope.data = {};
		$scope.filtros = {};

		$scope.filtros.kngFechaInicio = util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.oConfiguracionCliente = {
		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
			idActual : undefined,
			seleccionado : -1,
			idDefecto : -1
		};
		
		$scope.$watch('oConfiguracionCliente.seleccionado',function(newval,oldval){
			if (!$scope.oConfiguracionCliente.seleccionado) return;
			try{
				$scope.socioGral.idActual = $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
				$scope.socioGral.idDefecto = $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
			}catch(e){
			}
		});
		
		$scope.buscarRegistros = function (){
			var datos = {};	
			
			datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
			datos.p_IdUsuario			= $scope.SesionIdUsuario;
			datos.p_IdModulo			= "MES";
			datos.p_IdCliente		 	= $scope.oConfiguracionCliente.seleccionado.IdCliente || 0;
			console.log(datos.p_IdCliente);
			datos.p_FechaOperacion 		= util.dateSPdate($scope.filtros.kngFechaInicio);
			console.log(datos.p_FechaOperacion)
	
			dsInformacionLimitesCorredora.setCallParameters(datos);
			dsInformacionLimitesCorredora.read();	
		}
		
		// GRILLA
		var dsInformacionLimitesCorredora = new bcs.DataSource({
			type: "call",
			definition 	: {
				callTransaction	: "60069",
				callResultSet	: "P_OUT_TABLA",
				schema: {
					model: {
						fields: {
							IdPersona					: { type: "number"},		
							DocumentoIdentidadCliente  	: { type: "string"},
							NombreCliente				: { type: "string"},
							TotalMontoMonedaSecundaria	: { type: "number"},							
						}
					}
				},
				aggregate: [
					{ field:"TotalMontoMonedaSecundaria", aggregate: "sum" } ,       
				]
			}
		});	
		
		
		$scope.confBasicaInfLimites = {
				dataSource: dsInformacionLimitesCorredora,								
				columns: [				
				    {field : "DocumentoIdentidadCliente", 	title:'UI_RUT_GRILLA', 				width : "80px", sortable : false }
				   ,{field : "NombreCliente", 				title:'UI_NOMBRE_GRILLA', 				width : "200px", sortable : false }	
				   ,{field: "TotalMontoMonedaSecundaria"				,title:	$translate.instant('UI_OCUPADO')		, width: "120px", align: "left" , format: "{0:n0}", sortable : true, editable: false, footerTemplate: "#: 'Total = ' # #=kendo.toString(sum,'n0')#",footerAttributes: {style:'text-align:right'}},
				],			
				sortable			: true,
				pageable			: true,
				autoBind			: false,
		    	filterable			: true,
				height				: 450,
			    selectable 			: "multiple row",
			    userSettings		: true,
				navigatable			: true,
				excel : {
			        allPages: true,
			      },
			};						

		$scope.limpiarFiltros = function(){	
			$scope.oConfiguracionCliente.idActual			= false;
			$scope.socioGral.idActual = false;
			$scope.filtros.kngFechaInicio 	= util.dateInicioDia($scope.SesionFechaSistema);
			dsInformacionLimitesCorredora.removeAll();		
		};
	});
}());
