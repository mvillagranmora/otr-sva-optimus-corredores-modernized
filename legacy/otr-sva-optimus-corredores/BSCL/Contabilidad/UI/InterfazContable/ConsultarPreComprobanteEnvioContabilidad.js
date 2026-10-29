angular.module('ModuloContabilidad').controller('ConsultarPreComprobanteEnvioContabilidadCtrl',
		function ($rootScope,$scope,$translate,bcsComunes,$resource) {

	$scope.data = {};
	$scope.envioContabilidad = {};

	var filters = [];
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.oDatosDetalle = parametros;
	
	var dsGrillaCabecera = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			excel 				: { allPages : true },
			sort 				: [ { field: "JdtNum", dir: "asc" } ],
			filter				: {field: "JdtNum", operator: "eq", value: $scope.data.oDatosDetalle.IdPreComprobante},
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=600057",dataType : "json"}},
			schema : {
				model : {
					fields : {
						JdtNum										: {type  : "number"},
						ReferenceDate								: {type  : "string"},
						Memo										: {type  : "string"},
						DueDate										: {type  : "string"},
						TaxDate										: {type  : "string"},
						U_BCS_IDPreComp								: {type  : "number"},
						U_BCS_TipoCen								: {type  : "number"}
					}
				}
			}
		}
	});
	
	var dsGrillaDetalle = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			excel 				: { allPages : true },
			sort 				: [ { field: "IdMovimiento", dir: "asc" } ],
			filter				: {field: "ParentKey", operator: "eq", value: $scope.data.oDatosDetalle.IdPreComprobante},
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=600058",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdTipoContabilidad							: {type  : "number"},
						ParentKey									: {type  : "number"},
						Debit										: {type  : "number"},
						Credit										: {type  : "number"},
						U_glosa										: {type  : "string"},
						LineMemo									: {type  : "string"},
						ProfitCode									: {type  : "string"},
						BPLId										: {type  : "number"},
						AccountCode									: {type  : "string"},
						DueDate										: {type  : "string"},
						Formato										: {type  : "string"},
						TieneProject								: {type  : "string"},
						Project										: {type  : "string"},
						ProductoMovimiento							: {type  : "string"},
						RelacionCorredora							: {type  : "string"},
						CodSecuencia								: {type  : "string"},
						IdMovimiento								: {type  : "number"},
						Reference1									: {type  : "string"},
						Reference2									: {type  : "string"},
						AdditionalReference							: {type  : "string"},
						FCDebit										: {type  : "number"},
						FCCredit									: {type  : "number"},
						FCCurrency									: {type  : "string"},
						ShortName									: {type  : "string"},
						TipoSocioNegocio							: {type  : "string"},
						ReferenceDate1								: {type  : "string"},
						TaxDate										: {type  : "string"},
						CostingCode									: {type  : "string"},
						CostingCode2								: {type  : "string"},
						CostingCode3								: {type  : "string"},
						CostingCode4								: {type  : "string"},
						CostingCode5								: {type  : "string"},
						U_BCS_TipoPersona							: {type  : "string"},
						U_BCS_Rut									: {type  : "string"},
						U_BCS_Portafolio							: {type  : "string"},
						U_BCS_Nombre								: {type  : "string"},
						U_BCS_CtaDCV								: {type  : "string"},
						U_BCS_EmitidoA								: {type  : "string"},
						U_BCS_Comprobante							: {type  : "string"},
						U_BCS_Folio									: {type  : "string"},
						U_BCS_NDcto									: {type  : "string"},
						U_BCS_Concepto								: {type  : "string"},
						U_BCS_CV									: {type  : "string"},
						U_BCS_Nemo									: {type  : "string"},
						U_BCS_IFRSCtaPropia							: {type  : "string"},
						U_BCS_CodigoProducto						: {type  : "string"},
						U_BCS_Bolsa									: {type  : "string"},
						U_BCS_Relacion								: {type  : "string"},
						U_RelacionCodigoSap							: {type  : "string"},
						U_BCS_FolioDTE								: {type  : "string"}
					}
				}
			}
		}
	});
	
	columnasCabecera = function() {
  		return [
		 {field: "JdtNum"										,title: 'UI_PRECOMPROBANTE'						,width: "100px"}
		,{field: "ReferenceDate"								,title: 'UI_REFERENCE_DATE'						,width: "100px"}
		,{field: "Memo"											,title: 'UI_GLOSA'								,width: "60px"}
		,{field: "DueDate"										,title: 'UI_DUE_DATE'							,width: "120px"}
		,{field: "TaxDate"										,title: 'UI_TAX_DATE'							,width: "120px"}
		,{field: "U_BCS_IDPreComp"								,title: 'UI_U_BCS_IDPRECOMP'					,width: "120px"}
		,{field: "U_BCS_TipoCen"								,title: 'UI_U_BCS_TIPOCEN'						,width: "120px"}
		];
    };
    
    columnasDetalle = function() {
  		return [
  		 {field: "IdTipoContabilidad"							,title: 'UI_ID_TIPO_CONTABILIDAD'				,width: "100px"}
  		,{field: "AccountCode"									,title: 'UI_CUENTA_CONTABLE'					,width: "60px"}
		,{field: "Debit"										,title: 'UI_DEBIT'								,width: "60px"}
		,{field: "Credit"										,title: 'UI_CREDIT'								,width: "120px"}
		,{field: "U_glosa"										,title: 'UI_GLOSA'								,width: "120px"}
		,{field: "LineMemo"										,title: 'UI_LINE_MEMO'							,width: "100px"}
		,{field: "ProfitCode"									,title: 'UI_PROFIT_CODE'						,width: "100px"}
		,{field: "BPLId"										,title: 'UI_BPLID'   							,width: "60px"}
		,{field: "DueDate"										,title: 'UI_FECHA_LIQUIDACION'   				,width: "120px"}
		,{field: "Formato"										,title: 'UI_FORMATO'   							,width: "160px"}
		,{field: "TieneProject"									,title: 'UI_TIENE_PROJECT'   					,width: "60px"}
		,{field: "Project"										,title: 'UI_PROJECT'   							,width: "60px"}
		,{field: "ProductoMovimiento"							,title: 'UI_PRODUCTO_MOVIMIENTO'				,width: "100px"}
		,{field: "RelacionCorredora"							,title: 'UI_RELACION_CORREDORA'					,width: "60px"}
		,{field: "CodSecuencia"									,title: 'UI_COD_SECUENCIA'						,width: "120px"}
		,{field: "IdMovimiento"									,title: 'UI_ID_MOVIMIENTO'						,width: "120px"}
		,{field: "Reference1"									,title: 'UI_REFERENCE_1'						,width: "100px"}
		,{field: "Reference2"									,title: 'UI_REFERENCE_2'						,width: "100px"}
		,{field: "AdditionalReference"							,title: 'UI_ADDITIONAL_REFERENCE'   			,width: "60px"}
		,{field: "FCDebit"										,title: 'UI_FC_DEBIT'   						,width: "120px"}
		,{field: "FCCredit"										,title: 'UI_FC_CREDIT'   						,width: "160px"}
		,{field: "FCCurrency"									,title: 'UI_FC_CURRENCY'   						,width: "60px"}
		,{field: "ShortName"									,title: 'UI_SHORTNAME'   						,width: "60px"}
		,{field: "TipoSocioNegocio"								,title: 'UI_TIPO_SOCIO_NEGOCIO'					,width: "100px"}
		,{field: "ReferenceDate1"								,title: 'UI_DUE_DATE'							,width: "60px"}
		,{field: "TaxDate"										,title: 'UI_TAX_DATE'							,width: "120px"}
		,{field: "CostingCode"									,title: 'UI_COSTING_CODE'						,width: "120px"}
		,{field: "CostingCode2"									,title: 'UI_COSTING_CODE2'						,width: "100px"}
		,{field: "CostingCode3"									,title: 'UI_COSTING_CODE3'						,width: "100px"}
		,{field: "CostingCode4"									,title: 'UI_COSTING_CODE4'   					,width: "60px"}
		,{field: "CostingCode5"									,title: 'UI_COSTING_CODE5'   					,width: "120px"}
		,{field: "U_BCS_TipoPersona"							,title: 'UI_BCS_TIPO_PERSONA'   				,width: "160px"}
		,{field: "U_BCS_Rut"									,title: 'UI_BCS_RUT'   							,width: "60px"}
		,{field: "U_BCS_Portafolio"								,title: 'UI_BCS_PORTAFOLIO'   					,width: "60px"}
		,{field: "U_BCS_Nombre"									,title: 'UI_BCS_NOMBRE'							,width: "100px"}
		,{field: "U_BCS_CtaDCV"									,title: 'UI_BCS_CTA_DCV'						,width: "60px"}
		,{field: "U_BCS_EmitidoA"								,title: 'UI_BCS_EMITIDO_A'						,width: "120px"}
		,{field: "U_BCS_Comprobante"							,title: 'UI_BCS_COMPROBANTE'					,width: "120px"}
		,{field: "U_BCS_Folio"									,title: 'UI_BCS_FOLIO'							,width: "100px"}
		,{field: "U_BCS_NDcto"									,title: 'UI_BCS_NDCTO'							,width: "100px"}
		,{field: "U_BCS_Concepto"								,title: 'UI_BCS_CONCEPTO'   					,width: "60px"}
		,{field: "U_BCS_CV"										,title: 'UI_BCS_CV'   							,width: "120px"}
		,{field: "U_BCS_Nemo"									,title: 'UI_BCS_NEMO'   						,width: "160px"}
		,{field: "U_BCS_IFRSCtaPropia"							,title: 'UI_BCS_IFRSCTA_PROPIA'   				,width: "60px"}
		,{field: "U_BCS_CodigoProducto"							,title: 'UI_BCS_CODIGO_PRODUCTO'   				,width: "60px"}
		,{field: "U_BCS_Bolsa"									,title: 'UI_BCS_BOLSA'   						,width: "160px"}
		,{field: "U_BCS_Relacion"								,title: 'UI_BCS_RELACION'   					,width: "60px"}
		,{field: "U_RelacionCodigoSap"							,title: 'UI_RELACION_CODIGO_SAP'   				,width: "60px"}
		,{field: "U_BCS_FolioDTE"								,title: 'UI_FOLIO_DTE'							,width: "100px"}
		];
    };
		

	$scope.envioContabilidad.confBasicaCabecera= {
		dataSource			: dsGrillaCabecera,
		columns				: columnasCabecera(),
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		filterable			: false,
		height				: 70,
		selectable 			: "single row",
		autoBind			: true,
		gridCreated 		: function(grid) {		
			$scope.grillaCabecera = grid;
		}		
	};
	
	$scope.envioContabilidad.confBasicaDetalle= {
		dataSource			: dsGrillaDetalle,
		columns				: columnasDetalle(),
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		filterable			: true,
		height				: 350,
		selectable 			: "single row",
		autoBind			: true,
		gridCreated 		: function(grid) {		
			$scope.grillaDetalle = grid;
		}		
	};
	
	$scope.btnEnviarPreComprobantes	= function() {
		var filterComprobantes = {};
		var precmbteEnviar = $scope.data.oDatosDetalle.IdPreComprobante;
		
		filterComprobantes.p_IdEmpresa				= 	$scope.SesionIdEmpresa;
		filterComprobantes.p_IdUsuario				=   $scope.SesionIdUsuario;
		filterComprobantes.p_IdModulo				=   'CON';
		filterComprobantes.p_UsaReglasNegocio		=   'N';
		filterComprobantes.p_SecCmbte				=	precmbteEnviar;
		filterComprobantes.p_Destino				=	'B';
		
		enviarPrecomprobante(filterComprobantes);
	}
	
	var enviarPrecomprobante = function(parameters) {
		
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4145";
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		
		var post = r.Filtros(parameters);
		
		bcsComunes.ventanaOverlay(idVentana, true);
		post.$promise.then(function(respuesta){
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaMsg({	
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
					
		}).catch(function(respuesta) {
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaMsg({	
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
    	});
		
	}

});