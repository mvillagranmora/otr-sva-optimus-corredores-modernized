(function() {
	angular.module('ModuloMesa').controller('ConsultarInformacionCliente',
	function($scope,$translate,$resource,$timeout,$http,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA, bcsConsultarInformacionClienteGrid)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};

		//ComboBox		
		$scope.data.oConfiguracionAgente={
				tipo : "combobox-ejecutivo",
				configuracion : {},
				filtros : { 	
					IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};	
		
		
		$scope.data.oConfiguracionTipPersona={
				tipo : "combobox-tipo-persona",
				configuracion : {},
				filtros : { 	
					IdTipoPersona : { "field"   : "IdTipoPersona","operator": "eq", "value"   :  '' },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''				
		};
		
		
		$scope.buscarRegistros = function (){
		var datos = {};	
		
		datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
		datos.p_IdUsuario			= $scope.SesionIdUsuario;
		datos.p_IdModulo			= "SOC";
		datos.p_IdPersonaEjecutivo 	= $scope.data.oConfiguracionAgente.seleccionado.IdPersonaEjecutivo || 0;
		datos.p_IdTipoPersona 		= $scope.data.oConfiguracionTipPersona.seleccionado.IdTipoPersona || '';		

		dsInformacionCliente.setCallParameters(datos);
		dsInformacionCliente.read();
		
			
		}
		
		// GRILLA
		var dsInformacionCliente = new bcs.DataSource({
			type: "call",
			definition 	: {
//				pageSize 		: 15,
				callTransaction	: "601209",
				callResultSet	: "P_OUT_TABLA",
				schema: {
					model: {
						fields: {
							IdPersona					: { type: "number"},		
							IdPersonaEjecutivo			: { type: "number"},
							DocumentoIdentidadCliente  	: { type: "string"},
							NombreCliente				: { type: "string"},
							IdTipoPersona				: { type: "string"},
							NombreTipoPersona			: { type: "string"},
							FechaNacimiento				: { type: "string"}, 
							Cargo						: { type: "string"},
							NombreEjecutivo				: { type: "string"},
							DireccionElectronica		: { type: "string"},
							DocumentoIdentidadRepLegal	: { type: "string"},
							NombreRepLegal				: { type: "string"},
							Patrimonio					: { type: "number"},							
						}
					}
				}
			}
		});	
		
		
		$scope.confBasicaInfCliente = {
				dataSource: dsInformacionCliente,								
				columns: [
//					{field : "IdPersona", 					title:'UI_PERSONA', 					width : "80px", sortable : false }
//					{field : "IdPersonaEjecutivo", 			title:'IdPersonaEjecutivo', 			width : "80px", sortable : false }					
				    {field : "DocumentoIdentidadCliente", 	title:'UI_RUT_CLIENTE', 				width : "80px", sortable : false }
				   ,{field : "NombreCliente", 				title:'UI_NOMBRE_CLIENTE', 				width : "200px", sortable : false }	
				   ,{field : "NombreTipoPersona", 			title:'UI_TIPO_PERSONA', 				width : "110px", sortable : false }
				   ,{field : "NombreEjecutivo", 			title:'UI_AGENTE', 						width : "200px", sortable : false }
				   ,{field : "DocumentoIdentidadRepLegal", 	title:'UI_RUT_REPRESENTANTE_LEGAL', 	width : "80px", sortable : false}
				   ,{field : "NombreRepLegal", 				title:'UI_NOMBRE_REPRESENTANTE_LEGAL', 	width : "200px", sortable : false}
				   ,{field : "Cargo", 						title:'UI_CARGO', 						width : "200px", sortable : false}
				   ,{field : "FechaNacimiento", 			title:'UI_FECHA_NACIMIENTO', 			width : "80px", sortable : false }
				   ,{field : "DireccionElectronica", 		title:'UI_EMAIL', 						width : "200px", sortable : false }
				   ,{field : "Patrimonio", 					title:"UI_PESOS", 						width : "120px", template : kendo.template("#if (( (util.bcs_formato(Patrimonio,'GEN','Monto','CLP','','')).valor == -1  || ((util.bcs_formato(Patrimonio,'GEN','Monto','CLP','','')).valor == null)))  {##} else {##=(util.bcs_formato(Patrimonio,'GEN','Monto','CLP','','')).valor##}#") }
			   

					
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
			$scope.data.oConfiguracionAgente.idActual = -1;
			$scope.data.oConfiguracionTipPersona.idActual = 0;
			$scope.seleccionInfCliente = {};
			
		};
	});
}());
