angular.module('ModuloPlataforma').controller('ConsultarInstrumentosCtrl',function($scope,bcsComunes,OPCIONES_BCSALERTA,bcsAlerta,bcsServicioPlataforma,$translate){
	
	$scope.filtros = {};
	$scope.formData = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.data	= {};
	$scope.data.btnAutorizar = false;
	$scope.data.btnDesAutorizar = false;
	let gridInstrumentos;

	$scope.formData.buscar = function(filtros) {
		let filters = new Array();
		$scope.data.btnAutorizar = false;
		$scope.data.btnDesAutorizar = false
		
		if (filtros.clasificacionTipoInstrumento && filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento){
			let clasificacionInstrumento = filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;
			
			if(clasificacionInstrumento === 'RV'){
				gridInstrumentos.showColumn("EsInstrumentoCalificado");
			}else{
				gridInstrumentos.hideColumn("EsInstrumentoCalificado");
			}
			
			filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: clasificacionInstrumento });
		}
		
		if (filtros.tipoInstrumento && filtros.tipoInstrumento.IdTipoInstrumento)
			filters.push({ field: "IdTipoInstrumento", operator: "eq", value: filtros.tipoInstrumento.IdTipoInstrumento });
			
		if (filtros.IdEstado)
			filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado });
			
		$scope.formData.apiGrid.dataSource.filter(filters);
	};
	
	$scope.formData.ingresar = function() {
		var prom = bcsComunes.ventanaAbrir("MantenedorInstrumento", { } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(prom).then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}
		});
	};
	
	$scope.formData.modificar = function(instrumentoSeleccionado) {
		if (!instrumentoSeleccionado) return;
		var prom = bcsComunes.ventanaAbrir("MantenedorInstrumento", { instrumento : instrumentoSeleccionado[0] } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(prom).then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}
		});
	};
	
	
	$scope.formData.CrearModificarTasasGenericas = function() {
		var prom = bcsComunes.ventanaAbrir("CrearModificarTasasGenericas", { } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(prom).then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}
		});
	};
	
	
	$scope.formData.defaultOptions = {
			allowCopy: true,
			autoBind: false,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
	      	filterable: true,
	      	height:500,
	      	pageable : true,
	      	reorderable: true,
			resizable: true,
			scrollable: true,
	      	selectable: "multiple, row",
			sortable: true,
			
			columns : [
			           {field: "IdInstrumento",							title : "UI_SECUENCIA",						width: "75px",groupable: true},
			           {field: "Nemotecnico",							title : "UI_NEMOTECNICO",					width: "100px",groupable: true},
			           {field: "ISIN",									title : "UI_ISIN",							width: "100px"},
			           {field: "CorteMinimo",							title : "UI_CORTE_MINIMO",					width: "100px",groupable: true},
			           {field: "EstaInscrito",							title : "UI_INSCRITO",						width: "75px",groupable: true},
			           {field: "RazonSocial",							title : "UI_RAZON_SOCIAL",					width: "300px",groupable: true},
			           {field: "FechaEmision",							title : "UI_FECHA_EMISION",					width: "100px",groupable: true},
			           {field: "FechaVencimiento",						title : "UI_FECHA_VENCIMIENTO",				width: "100px",groupable: true},
			           {field: "IdTipoReajuste",						title : "UI_TIPO_REAJUSTE",					width: "75px",groupable: true},
			           {field: "IdMoneda",								title : "UI_MONEDA",						width: "75px",groupable: true},
			           {field: "IdEstado",								title : "UI_ESTADO",						width: "75px",groupable: true},
			           {field: "NombreTipoInstrumento",					title : "UI_TIPO_INSTRUMENTO",				width: "200px",groupable: true},
			           {field: "NombreClasificacionTipoInstrumento",	title : "UI_CLASIFICACION",					width: "100px",groupable: true},
			           {field: "IdEmisor",								title : "UI_EMISOR",						width: "100px",groupable: true},
			           {field: "NombreCompletoEmisor",					title : "UI_NOMBRE_EMISOR",					width: "200px",groupable: true},
			           {field: "DocumentoIdentidadEmisor",				title : "UI_DOCUMENTO_IDENTIDAD_EMISOR",	width: "100px",groupable: true},
			           {field: "Tamano",								title : "UI_TAMANO_CONTRATO",				width: "100px",groupable: true},
			           {field: "IdEntidadClasificadoraRiesgo",			title : "UI_ENTIDAD_CLASIFICADORA",			width: "100px",groupable: true},
			           {field: "ValorClasificacionRiesgo",				title : "UI_VALOR_CLASIFICACION_RIESGO",	width: "100px",groupable: true},
			           {field: "PorcentajeCastigo",						title : "UI_PORCENTAJE_DESCUENTO",			width: "100px",groupable: true, hidden: true},
					   {field: "AfectoArt107",							title : "UI_AFECTO_ART_107",				width: "75px",groupable: true},
					   {field: "AfectoArt104",							title : "UI_AFECTO_ART_104",				width: "75px",groupable: true},															
					   {field: "DividendoOtraMoneda",					title : "UI_DIVIDENDO_OTRAS_MONEDAS",		width: "75px",groupable: true},	
					   {field: "EsAutorizado",							title : "UI_ES_AUTORIZADO",				    width: "75px",groupable: true},
					   {field: "CobroComisionesCustodia",				title : "UI_COBRO_COMISIONES_CUSTODIA",		width: "145px",groupable: true},
					   {field: "NemotecnicoRaiz",						title : "UI_NEMOTECNICO_RAIZ",    			width: "140px",groupable: true},
					   {field: "IdCondicionDeLiquidacion",				title : "UI_CONDICION_LIQUIDACION",		    width: "140px",groupable: true},		           					   						   
					   {field: "EsInstrumentoCalificado",				title : "UI_INSTRUMENTO_CALIFICADO",		width: "145px",groupable: true, hidden: true}				   
			           ], 
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: { field: "Nemotecnico", dir: "asc" },
					pageSize:250,
					schema: {
						model : {
							fields : {
								IdInstrumento 						: { type: "number"},
				           		Nemotecnico  						: { type: "string"},
				           		ISIN  								: { type: "string"},
				           		CorteMinimo  						: { type: "number"},
				           		EstaInscrito  						: { type: "string"},
				          		RazonSocial  						: { type: "string"},
				           		FechaEmision  						: { type: "sysdate"},
				           		FechaVencimiento  					: { type: "sysdate"},
				           		IdTipoReajuste 	 					: { type: "string"},
				           		IdMoneda  							: { type: "string"},
				           		IdEstado  							: { type: "string"},
				           		NombreCompletoEmisor  				: { type: "string"},
				           		DocumentoIdentidadEmisor  			: { type: "string"},
				           		
				           		NombreTipoInstrumento  				: { type: "string"},
				           		NombreClasificacionTipoInstrumento  : { type: "string"},
				           		IdEmisor  							: { type: "string"},
				           		RazonSocialEmisor  					: { type: "string"},
				           		RutEmisor		  					: { type: "string"},
				           		Tamano		  						: { type: "number"},
				           		IdEntidadClasificadoraRiesgo 		: { type: "string"}	,	
				           		ValorClasificacionRiesgo 			: { type: "string"},
				           		Precio 								: { type: "number"},
				           		Tasa 								: { type: "number"},
				           		PrecioReferencia					: { type: "number"},
				           		FechaReferencia						: { type: "sysdate"},
				           		TipoBonoIRF							: { type: "string"}	,
				           		OrigenInstrumento					: { type: "string"}	,
				           		TipoRenta							: { type: "string"}	,
				           		RelacionadoGrupo					: { type: "string"}	,
				           		FormadorMercado						: { type: "string"}	,
				           		PactoAndino							: { type: "string"}	,
				           		FactorExposicion					: { type: "number"} ,
				           		LargoPlazo							: { type: "string"}	,
				           		NemotecnicoRelacionado              : { type: "string"} ,
				           		PorcentajeCastigo					: { type: "number"},
								AfectoArt107						: { type: "string"},
								AfectoArt104						: { type: "string"},
								DividendoOtraMoneda					: { type: "string"},
								EsAutorizado						: { type: "string"},
								CobroComisionesCustodia				: { type: "string"},
								NemotecnicoRaiz						: { type: "string"},
								IdCondicionDeLiquidacion			: { type: "string"},							
								EsInstrumentoCalificado				: { type: "string"}
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1505",
							dataType: "json"
						}
					}
				}
	      	}),
	      	gridCreated : function(grid) {
		    	gridInstrumentos = grid;
		    	if ($scope.SesionLocale === 'PE'){
					grid.showColumn("PorcentajeCastigo");
				}
		    },
	};
		
	$scope.formData.buscarSubyacentes = function(filtros) {
		var filters = new Array();
		if (filtros.clasificacionTipoInstrumento && filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento)
			filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento });
		if (filtros.tipoInstrumento && filtros.tipoInstrumento.IdTipoInstrumento)
			filters.push({ field: "IdTipoInstrumento", operator: "eq", value: filtros.tipoInstrumento.IdTipoInstrumento });
		$scope.formData.apiGridSubyacentes.dataSource.filters(filters);
	};
	$scope.formData.configuracionSubyacentes = {
			
			allowCopy: true,
			autoBind: false,
	      	filterable: true,
			groupable: true,
			height:500,
	      	reorderable: true,
			resizable: true,
			scrollable: true,
	      	selectable: "multiple, row",
			sortable: true,

			columns : [		           
			           {field: "NombreTipoInstrumento",		title : "UI_TIPO_INSTRUMENTO",				width: "200px",groupable: true} ,
			           {field: "NombreBolsa",				title : "UI_BOLSA",							width: "100px",groupable: true} ,
			           {field: "Nombre",					title : "UI_TIPO_INSTRUMENTO_SUBYACENTE",	width: "200px",groupable: true} ,
			           {field: "Nemotecnico",				title : "UI_NEMOTENICO",					width: "100px",groupable: true} ,
			          
			           ],
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					pageSize:200,
					schema: {
						model : {
							fields : {
										IdInstrumentoSubyacente  	: { type: "string"},
			           					IdTipoInstrumento   		: { type: "string"},
			          					NombreTipoInstrumento   	: { type: "string"},
							            IdBolsa   					: { type: "string"},
							            NombreBolsa   				: { type: "string"},
							            IdTipoInstrumentoSubyacente : { type: "string"},
							            Nombre   					: { type: "string"},
							            IdInstrumento  				: { type: "string"},
							            Nemotecnico  				: { type: "string"},	
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1550",
							dataType: "json"
						}
					}
				}
	      	}),

	};	
  
	$scope.formData.oConfiguracionTipoInstrumento = {
			filtros : { 	
				IdClasificacionTipoInstrumento : {
			        "field"   : "IdClasificacionTipoInstrumento",
			        "operator": "eq",
			        "value"   : ""
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.$watch("formData.instrumentoSeleccionado", function handleAutorizado(newValue, oldValue) {

		if(newValue !== undefined){
			$scope.data.btnAutorizar = true;
			$scope.data.btnDesAutorizar = true; 
			$.each($scope.formData.instrumentoSeleccionado,function(key,value)
			{
				if(($scope.formData.instrumentoSeleccionado[key].CodigoTipoInstrumento != 'CFI' && $scope.formData.instrumentoSeleccionado[key].CodigoTipoInstrumento != 'CFX') || $scope.formData.instrumentoSeleccionado[key].EsAutorizado != 'N'){
					$scope.data.btnAutorizar = false;
				}
				if(($scope.formData.instrumentoSeleccionado[key].CodigoTipoInstrumento != 'CFI' && $scope.formData.instrumentoSeleccionado[key].CodigoTipoInstrumento != 'CFX') || $scope.formData.instrumentoSeleccionado[key].EsAutorizado != 'S'){
					$scope.data.btnDesAutorizar = false;
				}				
			});
		}
	});

	$scope.formData.autorizar = function(iii) {

		var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_AUTORIZAR_INSTRUMENTO"), $scope.idVentana);
		t.then(function(result)
		  {
			if (result===OPCIONES_BCSALERTA.SI)
			{
			var InstrumentosAutorizar = [];
				bcsComunes.ventanaOverlay($scope.idVentana, true);
				//Bloquea botones
				$scope.data.btnAutorizar = false;
				$scope.data.btnDesAutorizar = false;
				
				 //Recopila Id de movimientos seleccionados
				 $.each($scope.formData.instrumentoSeleccionado,function(key,value)
				   {      		     
					 InstrumentosAutorizar.push(
					   {   
						IdInstrumento: $scope.formData.instrumentoSeleccionado[key].IdInstrumento,
						ISIN : $scope.formData.instrumentoSeleccionado[key].ISIN || null,
						CorteMinimo : $scope.formData.instrumentoSeleccionado[key].CorteMinimo || null,
						EstaInscrito : $scope.formData.instrumentoSeleccionado[key].EstaInscrito || null,
						InstrumentoAcreditado : $scope.formData.instrumentoSeleccionado[key].InstrumentoAcreditado || null,
						RazonSocial : $scope.formData.instrumentoSeleccionado[key].RazonSocial || null,
						FechaEmision : $scope.formData.instrumentoSeleccionado[key].FechaEmision ? util.dateSPdate($scope.formData.instrumentoSeleccionado[key].FechaEmision): null,
						FechaVencimiento : $scope.formData.instrumentoSeleccionado[key].FechaVencimiento ? util.dateSPdate($scope.formData.instrumentoSeleccionado[key].FechaVencimiento): null,
						IdEmisor : $scope.formData.instrumentoSeleccionado[key].IdEmisor || null,
						IdTipoReajuste : $scope.formData.instrumentoSeleccionado[key].IdTipoReajuste || null,
						IdMoneda : $scope.formData.instrumentoSeleccionado[key].IdMoneda || null,
						IdEstado : $scope.formData.instrumentoSeleccionado[key].IdEstado || null,
						Tamano : $scope.formData.instrumentoSeleccionado[key].Tamano || null,
						PrecioBase : ($scope.formData.instrumentoSeleccionado[key].Precio!==undefined) ? $scope.formData.instrumentoSeleccionado[key].Precio : null,
						PrecioReferencia : 0,				//deprecated
						FechaReferencia : "0001-01-01",		//deprecated
						FrecuenciaCupon : $scope.formData.instrumentoSeleccionado[key].FrecuenciaCupon || null,
						NumeroCupones : ($scope.formData.instrumentoSeleccionado[key].NumeroCupones !==undefined && $scope.formData.instrumentoSeleccionado[key].NumeroCupones >=0) ? $scope.formData.instrumentoSeleccionado[key].NumeroCupones : null,
						TipoBonoIRF : $scope.formData.instrumentoSeleccionado[key].TipoBonoIRF || null,
						OrigenInstrumento : $scope.formData.instrumentoSeleccionado[key].OrigenInstrumento || null,
						TipoRenta : $scope.formData.instrumentoSeleccionado[key].TipoRenta || null,
						RelacionadoGrupo : $scope.formData.instrumentoSeleccionado[key].RelacionadoGrupo || null,
						FormadorMercado : $scope.formData.instrumentoSeleccionado[key].FormadorMercado || null,
						PactoAndino: $scope.formData.instrumentoSeleccionado[key].PactoAndino || null,
						FactorExposicion : $scope.formData.instrumentoSeleccionado[key].FactorExposicion || null,
						LargoPlazo : $scope.formData.instrumentoSeleccionado[key].LargoPlazo || null,
						PorcentajeCastigo : $scope.formData.instrumentoSeleccionado[key].PorcentajeCastigo || null,
						FechaHastaAcreditado : $scope.formData.instrumentoSeleccionado[key].FechaHastaAcreditado || null,
						NemotecnicoCambio : $scope.formData.instrumentoSeleccionado[key].NemotecnicoCambio || null,
						EsPutEsCall : $scope.formData.instrumentoSeleccionado[key].TipoOpcion.ValorParametro || null,
						PrecioStrike : $scope.formData.instrumentoSeleccionado[key].PrecioStrike || null,
						IdTipoInstrumento : $scope.formData.instrumentoSeleccionado[key].IdTipoInstrumento || null,
						Tasa : $scope.formData.instrumentoSeleccionado[key].Tasa || null,						
						AfectoArt107 : $scope.formData.instrumentoSeleccionado[key].AfectoArt107 || null,
						AfectoArt104 : $scope.formData.instrumentoSeleccionado[key].AfectoArt104 || null,
						DividendoOtraMoneda	: $scope.formData.instrumentoSeleccionado[key].DividendoOtraMoneda || null,
						EsAutorizado:  'S',
						CobroComisionesCustodia: $scope.formData.instrumentoSeleccionado[key].CobroComisionesCustodia || 'S',
						IdInstrumentoRaiz: $scope.formData.instrumentoSeleccionado[key].IdInstrumentoRaiz || 0,
						IdCondicionLiquidacion: $scope.formData.instrumentoSeleccionado[key].IdCondicionDeLiquidacion || '',
						EsInstrumentoCalificado: $scope.formData.instrumentoSeleccionado[key].EsInstrumentoCalificado || null
					   });
				   });
				 
				 
				var datos = {
							p_IdEmpresa : $scope.SesionIdEmpresa,
							p_IdUsuario : $scope.SesionIdUsuario,
							p_IdModulo : 'PLA',
							p_simularOperacion : 'N',
							p_UsaReglasNegocio : 'N',
							p_Autorizar : 'S',
							p_Instrumentos : InstrumentosAutorizar,
							p_ValoresRiesgo : [],
							p_Bolsas : []
						};				 

					bcsComunes.ventanaOverlay($scope.idVentana, true);
					bcsServicioPlataforma.Servicios().ModificarInstrumento(datos).$promise.then(
						function(result) {
							bcsComunes.ventanaOverlay($scope.idVentana,false);
							if (result.P_OUT_ERRORESCONTADOR === 0) {
									ventanaResultadoOK("UI_INSTRUMENTO_AUTORIZADO",result);
									$scope.formData.buscar($scope.filtros);
							} else {
								ventanaResultadoError("UI_ERROR_AUTORIZADO_INSTRUMENTO",result);
							}
						},
						function(result) {
							bcsComunes.ventanaOverlay($scope.idVentana,false);
							bcsComunes.msg("error","UI_ERROR","UI_ERROR_AUTORIZADO_INSTRUMENTO");
						});				
					
			   }
			 else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		  });		
	}
	
	$scope.formData.desautorizar = function(iii) {
		var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_DESAUTORIZAR_INSTRUMENTO"), $scope.idVentana);
		t.then(function(result)
				  {
					if (result===OPCIONES_BCSALERTA.SI)
					{
					var InstrumentosAutorizar = [];
						bcsComunes.ventanaOverlay($scope.idVentana, true);
						//Bloquea botones						
						$scope.data.btnAutorizar = false;
						$scope.data.btnDesAutorizar = false;
						
						
						 //Recopila Id de movimientos seleccionados
						 $.each($scope.formData.instrumentoSeleccionado,function(key,value)
						   {      		     
							 InstrumentosAutorizar.push(
							   {   
								IdInstrumento: $scope.formData.instrumentoSeleccionado[key].IdInstrumento,
								ISIN : $scope.formData.instrumentoSeleccionado[key].ISIN || null,
								CorteMinimo : $scope.formData.instrumentoSeleccionado[key].CorteMinimo || null,
								EstaInscrito : $scope.formData.instrumentoSeleccionado[key].EstaInscrito || null,
								InstrumentoAcreditado : $scope.formData.instrumentoSeleccionado[key].InstrumentoAcreditado || null,
								RazonSocial : $scope.formData.instrumentoSeleccionado[key].RazonSocial || null,
								FechaEmision : $scope.formData.instrumentoSeleccionado[key].FechaEmision ? util.dateSPdate($scope.formData.instrumentoSeleccionado[key].FechaEmision): null,
								FechaVencimiento : $scope.formData.instrumentoSeleccionado[key].FechaVencimiento ? util.dateSPdate($scope.formData.instrumentoSeleccionado[key].FechaVencimiento): null,
								IdEmisor : $scope.formData.instrumentoSeleccionado[key].IdEmisor || null,
								IdTipoReajuste : $scope.formData.instrumentoSeleccionado[key].IdTipoReajuste || null,
								IdMoneda : $scope.formData.instrumentoSeleccionado[key].IdMoneda || null,
								IdEstado : $scope.formData.instrumentoSeleccionado[key].IdEstado || null,
								Tamano : $scope.formData.instrumentoSeleccionado[key].Tamano || null,
								PrecioBase : ($scope.formData.instrumentoSeleccionado[key].Precio!==undefined) ? $scope.formData.instrumentoSeleccionado[key].Precio : null,
								PrecioReferencia : 0,				//deprecated
								FechaReferencia : "0001-01-01",		//deprecated
								FrecuenciaCupon : $scope.formData.instrumentoSeleccionado[key].FrecuenciaCupon || null,
								NumeroCupones : ($scope.formData.instrumentoSeleccionado[key].NumeroCupones !==undefined && $scope.formData.instrumentoSeleccionado[key].NumeroCupones >=0) ? $scope.formData.instrumentoSeleccionado[key].NumeroCupones : null,
								TipoBonoIRF : $scope.formData.instrumentoSeleccionado[key].TipoBonoIRF || null,
								OrigenInstrumento : $scope.formData.instrumentoSeleccionado[key].OrigenInstrumento || null,
								TipoRenta : $scope.formData.instrumentoSeleccionado[key].TipoRenta || null,
								RelacionadoGrupo : $scope.formData.instrumentoSeleccionado[key].RelacionadoGrupo || null,
								FormadorMercado : $scope.formData.instrumentoSeleccionado[key].FormadorMercado || null,
								PactoAndino: $scope.formData.instrumentoSeleccionado[key].PactoAndino || null,
								FactorExposicion : $scope.formData.instrumentoSeleccionado[key].FactorExposicion || null,
								LargoPlazo : $scope.formData.instrumentoSeleccionado[key].LargoPlazo || null,
								PorcentajeCastigo : $scope.formData.instrumentoSeleccionado[key].PorcentajeCastigo || null,
								FechaHastaAcreditado : $scope.formData.instrumentoSeleccionado[key].FechaHastaAcreditado || null,
								NemotecnicoCambio : $scope.formData.instrumentoSeleccionado[key].NemotecnicoCambio || null,
								EsPutEsCall : $scope.formData.instrumentoSeleccionado[key].TipoOpcion.ValorParametro || null,
								PrecioStrike : $scope.formData.instrumentoSeleccionado[key].PrecioStrike || null,
								IdTipoInstrumento : $scope.formData.instrumentoSeleccionado[key].IdTipoInstrumento || null,
								Tasa : $scope.formData.instrumentoSeleccionado[key].Tasa || null,
								AfectoArt107 : $scope.formData.instrumentoSeleccionado[key].AfectoArt107 || null,
								AfectoArt104 : $scope.formData.instrumentoSeleccionado[key].AfectoArt104 || null,
								DividendoOtraMoneda	: $scope.formData.instrumentoSeleccionado[key].DividendoOtraMoneda || null,
								EsAutorizado:  'N',
								CobroComisionesCustodia: $scope.formData.instrumentoSeleccionado[key].CobroComisionesCustodia || 'S',
								IdInstrumentoRaiz: $scope.formData.instrumentoSeleccionado[key].IdInstrumentoRaiz || 0,
								IdCondicionLiquidacion: $scope.formData.instrumentoSeleccionado[key].IdCondicionDeLiquidacion || '',
								EsInstrumentoCalificado: $scope.formData.instrumentoSeleccionado[key].EsInstrumentoCalificado || null	
							   });							 
						   });
						 
						 
						var datos = {
									p_IdEmpresa : $scope.SesionIdEmpresa,
									p_IdUsuario : $scope.SesionIdUsuario,
									p_IdModulo : 'PLA',
									p_simularOperacion : 'N',
									p_UsaReglasNegocio : 'N',
									p_Autorizar : 'N',
									p_Instrumentos : InstrumentosAutorizar,
									p_ValoresRiesgo : [],
									p_Bolsas : []
								};				 

							bcsComunes.ventanaOverlay($scope.idVentana, true);
							bcsServicioPlataforma.Servicios().ModificarInstrumento(datos).$promise.then(
								function(result) {
									bcsComunes.ventanaOverlay($scope.idVentana,false);
									if (result.P_OUT_ERRORESCONTADOR === 0) {
											ventanaResultadoOK("UI_INSTRUMENTO_DESAUTORIZADO",result);
											$scope.formData.buscar($scope.filtros);
									} else {
										ventanaResultadoError("UI_ERROR_DESAUTORIZADO_INSTRUMENTO",result);
									}
								},
								function(result) {
									bcsComunes.ventanaOverlay($scope.idVentana,false);
									bcsComunes.msg("error","UI_ERROR","UI_ERROR_DESAUTORIZADO_INSTRUMENTO");
								});				
							
					   }
					 else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				  });						
		
	}
	
	
	function ventanaResultadoOK(codigoMensaje, result) {
		var p = bcsComunes.ventanaMsg({
			mensaje : $translate.instant(codigoMensaje),
			conflictos : util.parseServerResponse(result)
		}, $scope.IdVentana);
		bcsComunes.ventanaPromesa(p).then(function() {
			bcsComunes.ventanaData($scope.IdVentana, {
				guardar : true
			});
			bcsComunes.ventanaCerrar($scope.IdVentana);
		});
	}

	function ventanaResultadoError(codigoMensaje, result) {
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
		bcsComunes.ventanaMsg({
			mensaje : codigoMensaje,
			conflictos : util.parseServerResponse(result)
		}, $scope.IdVentana);
	}
	
});
