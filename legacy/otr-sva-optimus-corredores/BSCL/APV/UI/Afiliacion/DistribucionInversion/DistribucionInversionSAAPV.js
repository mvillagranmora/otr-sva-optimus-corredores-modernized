+(function() {
	angular.module('ModuloAPV').controller('InversionCtrl',
	function($scope,$rootScope,$translate,$resource,$timeout,bcsComunes)
	{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		//Obtiene datos desde la ventana principal
		var distrib = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';
		var gridDistribucion = null;
		var parametrosDevueltos = {};
		var contadorHabilita = 0;
		var varIdPosicionInicial;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var ventana = parametrosVentana.modoVentana;
		//$scope.Instrumento_habilitado = true;
		
		var IdTipoInstrumento = parametrosVentana.IdTipoInstrumento;

		
		distrib.IdTipoInstrumento = IdTipoInstrumento;
		
		distrib.bloqueoGuardar 	= false;	
		distrib.bloqueoEliminar = false;
		distrib.IdPerfilProductos = parametrosVentana.IdPerfilProductos;
		 		
		var FolioSAAPV 	= parametrosVentana.IdContratoSAAPV;
		var MontoTotal 	= parametrosVentana.MontoTotal;
		var TipoMonto	= parametrosVentana.TipoMonto
		var ParamDistri = parametrosVentana.paramVentanaDistribuir;
		var folioSelec	= parametrosVentana.FolioContratoSAAPV;
		parametrosDevueltos = parametrosVentana.paramVentanaDistribuir;

/******************************************************/
/*** FILTROS										***/
/******************************************************/

		distrib.instrumento = {};
		
		
		distrib.FiltrosBusquedaFondo =
		{
				IdCategoriaPerfilFondoMutuo : { "field" : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
				IdCliente 					: { "field" : "IdCliente","operator": "eq", "value"   :  undefined},
				IdPortafolio 				: { "field" : "IdPortafolio","operator": "eq", "value"   :  undefined},
				IdEstado 					: { "field" : "IdEstado", "operator": "eq", value:'ACT' },
				BloqueoAporte 				: { "field" : "BloqueoAporte", "operator" : "eq", "value":'N' },
				BloqueoRescate 				: { "field" : "BloqueoRescate", "operator" : "eq", "value":'N' }
		};	
		
		distrib.oConfiguracionClasificacionTipoInstrumento=
		{
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};

	   	distrib.oConfiguracionTipoInstrumento=
	   	{   	
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
				filtros : {
					IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : ""},
					IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
					NoPactos 						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PAC" },
					NoPrestamos						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PRT" },
					NoEmprestitos					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "EMP" },
					NoSimultaneas					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SIM" },
					NoSpot 							: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SPT" }
					
				},
				idActual : false,
				seleccionado : false,
				idDefecto : '',
				idPostFiltro : false
		};
	   	
	   	distrib.oConfiguracionInstrumento=
	   	{
			buscar          : "-1"    
		};

/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
		
		var dsDefDistrib = 
		{
			type : "array",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 100,
                sort 			: false,
                schema 			: {
                	model: {
                		fields: {
                			FolioContratoSAAPV  : {type: "strng"},
                			IdInstrumento     	: {type: "number"},
							RazonSocial			: {type: "string"},
							Nemotecnico			: {type: "string"},
							MontoPesos			: {type: "number"},
							Porcentaje			: {type: "float"},
							IdEstado			: {type: "string"},
							IdMoneda			: {type: "string"},
                		}
                	}
                }
			}
		};
		
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
		
		var DistribInversion = new bcs.DataSource(dsDefDistrib);
		$scope.Distribucion =
		{
			dataSource: DistribInversion,
			columns: [
			     {field: "IdInstrumento"	, title: "UI_ID_INSTRUMENTO",	width: "20px", align: "right", hidden: true },
			     {field: "RazonSocial"		, title: "UI_RAZON_SOCIAL",		width: "50px", align: "left"},
			     {field: "Nemotecnico"		, title: "UI_SERIE",			width: "15px", align: "left"},
			     {field: "MontoPesos"		, title: "UI_MONTO",			width: "15px", align: "right", hidden: (parametrosVentana.TipoSolicitud === 'TRA' ? true : false), editable: (parametrosVentana.TipoSolicitud === 'TRA' ? false : true), format: function(value, item){ return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema,'',''); }},
			     {field: "Porcentaje"		, title: "UI_PORCENTAJE",		width: "15px", align: "right", editable: true},
			     {field: "IdMoneda"			, title: "UI_MONEDA",			width: "15px", align: "right"}
			],
			sortable	: false,
			pageable	: true,
			filterable	: false,
			height		: 210,
			selectable 	: "row",
			autoBind	: true,
			navigatable : true,
			gridCreated : function(Distribucion){
				gridDistribucion = Distribucion;
				if (ParamDistri.length != undefined){
					if(parametrosVentana.limpiaDistribucion === true){
						DistribInversion.removeAll();
						DistribInversion.read();
						parametrosDevueltos = {};
					}else{
						$.each(ParamDistri,function(key,value){
							DistribInversion.add({ 
							   FolioContratoSAAPV : FolioSAAPV,
							   IdInstrumento 	  : value.IdInstrumento,
							   RazonSocial   	  : value.RazonSocial,
							   Nemotecnico		  : value.Nemotecnico,
							   MontoPesos		  : value.MontoPesos,
							   Porcentaje		  : value.Porcentaje,
							   IdEstado			  : value.IdEstado,
							   IdMoneda			  : value.IdMoneda,
							});
						});
						bcsComunes.ventanaData(idVentana,DistribInversion.data());
					}
				}	
			}
		};
		
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
		distrib.AgregarRegistro = function()
		{
			var dataDitrib = DistribInversion.data();
			var contador = 0;
			
			if(distrib.instrumento !== undefined)
			{
				//if(IdTipoInstrumento !== 'FFMM')
				//{
					if(distrib.instrumento.IdInstrumento !== undefined)
					{
						for(var i=0; i < dataDitrib.length; i++)
						{
							if(distrib.instrumento.IdInstrumento !== dataDitrib[i].IdInstrumento)
							{
								contador = 1 + contador;
							}
						}
						
						if(contador == dataDitrib.length)
						{
							DistribInversion.add({ 
								FolioContratoSAAPV	: FolioSAAPV,
								IdInstrumento 	  	: distrib.instrumento.IdInstrumento,
								RazonSocial   	  	: distrib.instrumento.RazonSocial,
								Nemotecnico		  	: distrib.instrumento.Nemotecnico,
								MontoPesos			: 0,
								IdEstado			: 'VIG',
								IdMoneda			: distrib.instrumento.IdMoneda,
							});	
							distrib.bloqueoGuardar = true;
							
						}
						else
						{
							bcsComunes.msg("warning",'Warning','Este Instrumento ya fue agregado.');
							return;
						}
					}	
				//}
				/*else
				{
					if(distrib.instrumentoSeleccionado.IdInstrumento !== undefined)
					{
						for(var i=0; i < dataDitrib.length; i++)
						{
							if(distrib.instrumentoSeleccionado.IdInstrumento !== dataDitrib[i].IdInstrumento)
							{
								contador = 1 + contador;
							}
						}
						
						if(contador == dataDitrib.length)
						{
							DistribInversion.add({ 
								FolioContratoSAAPV	: FolioSAAPV,
								IdInstrumento 	  	: distrib.instrumentoSeleccionado.IdInstrumento,
								RazonSocial   	  	: distrib.instrumentoSeleccionado.Nemotecnico,
								Nemotecnico		  	: distrib.instrumentoSeleccionado.IdSerieFondo,
								MontoPesos			: 0,
								IdEstado			: 'VIG'
							});	
							distrib.bloqueoGuardar = true;
						
						}
						else
						{
							bcsComunes.msg("warning",'Warning','Este Instrumento ya fue agregado.');
							return;
						}
					}	
				}*/
			}
		};
		
		distrib.fnBusquedaFondo = function()
		{
			var id1 = bcsComunes.ventanaAbrir("busquedaFondos",{
				filtros     			: distrib.FiltrosBusquedaFondo,
				soloCustodia			: undefined,
			},false,false,idVentana);
			
			bcsComunes.ventanaPromesa(id1).then(function(data)
			{
				if (data && data.IdFondo)
				{
					$scope.datosFondo = $.extend({},data);
					$scope.nemotecnico = undefined;
					$scope.datosFondo.ValorFormato = util.truncarNumero(scope.datosFondo.ValorCuota, scope.datosFondo.DecimalesValorCuota);
				}
			});
		};
		
		distrib.GuardarDistribucion = function()
		{
			var sumamonto = 0;
			var sumaporcentaje =0;
			var sumaPorcentajeTraspaso = 0;
			if(parametrosVentana.TipoSolicitud === 'TRA'){
				$.each(DistribInversion.data(),function(key,value){
					sumaPorcentajeTraspaso = sumaPorcentajeTraspaso + Number(value.Porcentaje);
				});
				if(sumaPorcentajeTraspaso < 100){
					bcsComunes.msg("warning","warning","El o los Instrumentos a Distribuir deben sumar el 100%.");
					return;
				}
			}
			
			var inversion = DistribInversion.data() || [];
			var tablaDistribucion = [];
			$.each(inversion,function(key,value){
				sumamonto += value.MontoPesos;
				sumaporcentaje += Number(value.Porcentaje);
				
				var tabla = {
						"FolioContratoSAAPV" : value.FolioContratoSAAPV,
						"IdInstrumento" : value.IdInstrumento,
						"RazonSocial"  : value.RazonSocial,
						"Nemotecnico" : value.Nemotecnico,
						"MontoPesos" : value.MontoPesos,
						"Porcentaje" : value.Porcentaje,
						"IdEstado" : value.IdEstado,
						"IdMoneda" : value.IdMoneda,
				}
				tablaDistribucion.push(tabla);
				
			});
			
			// EL PORCENTAJE DEBE SER 100% PARA GUARDAR
			if(sumamonto === MontoTotal && sumaporcentaje === 100)
			{
				parametrosDevueltos = tablaDistribucion;
				bcsComunes.ventanaData(idVentana,parametrosDevueltos);
				bcsComunes.ventanaCerrar(idVentana);
			}
			else
			{
				bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_PORCENTAJE_DEBE_CUMPLIR_UN_100'));
				return;
			}
		};
		
		/*Al cerrar ventana para no perder datos*/
		if(parametrosVentana.limpiaDistribucion === true)
		{
			bcsComunes.ventanaData(idVentana,{});
		}
		else
		{
			bcsComunes.ventanaData(idVentana,parametrosDevueltos);
		}
		
		/* Eliminar registro en grilla*/	
		distrib.EliminaDistribucion = function(row)
		{
			distrib.bloqueoEliminar = false;
			DistribInversion.remove(row);	
			distrib.bloqueoGuardar 	= true;
		};
		
		var valDistrib = $scope.Distribucion
		valDistrib.save = function(e)
		{
			var dataDitrib = DistribInversion.data();
			var montotTotalDisponible = parametrosVentana.MontoTotal;
			
			var montoT = 0;
			var porcentaje = 0;
			var calculoPorcentaje = 0; 
			var calculoMonto = 0; 
			
			if(e.values.MontoPesos !== undefined)
			{
				if(e.values.MontoPesos > montotTotalDisponible)
				{
					e.values.MontoPesos = 0;
					e.model.MontoPesos = 0;
					e.values.Porcentaje = 0;
					e.model.Porcentaje = 0;
					e.preventDefault();

					bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_MONTO_TOTAL_NO_DEBE_SUPERAR_EL_MONTO_DEL_CONTRATO_MONTO_DISPONIBLE') + " " + montotTotalDisponible);
					return;
				}
				else
				{
					calculoPorcentaje = (e.values.MontoPesos * 100) / montotTotalDisponible;
				}
			}
			
			if(e.values.Porcentaje !== undefined)
			{
				if(e.values.Porcentaje > 100)
				{
					e.values.Porcentaje = 0;
					e.model.Porcentaje = 0;
					e.values.MontoPesos = 0;
					e.model.MontoPesos = 0;
					e.preventDefault();
					
					bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_PORCENTAJE_NO_DEBE_SUPERAR_EL_100'));
					return;
				}
				else
				{
					calculoMonto = (e.values.Porcentaje * montotTotalDisponible) / 100;
				}
			}
			
			for(var i=0; i < dataDitrib.length; i++)
			{
				if(calculoPorcentaje <= 100 && calculoPorcentaje > 0){
					e.model.Porcentaje = calculoPorcentaje;
					porcentaje += Number(dataDitrib[i].Porcentaje);
				}
				if(calculoMonto <= montotTotalDisponible && calculoMonto > 0){
					e.model.MontoPesos = calculoMonto;
					montoT += dataDitrib[i].MontoPesos;
				}
			}
			
			if(montoT > MontoTotal)
			{
				montotTotalDisponible += e.values.Monto; 
				e.values.MontoPesos = 0;
				e.model.MontoPesos = 0;
				e.values.Porcentaje = 0;
				e.model.Porcentaje = 0;
				e.preventDefault();
				bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_MONTO_TOTAL_NO_DEBE_SUPERAR_EL_MONTO_DEL_CONTRATO_MONTO_DISPONIBLE') + " " + montotTotalDisponible);
			}
			if(porcentaje > 100){
				porcentaje -= Number(e.values.Porcentaje);
				var porDisponible = (100 - Number(porcentaje));
				e.values.MontoPesos = 0;
				e.model.MontoPesos = 0;
				e.values.Porcentaje = 0;
				e.model.Porcentaje = 0;
				e.preventDefault();
				bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_PORCENTAJE_NO_DEBE_SUPERAR_EL_100_PORCENTAJE_DISPONIBLE') + " " + porDisponible);
			}
			if(porcentaje === 100 || montoT === MontoTotal)
			{
				distrib.bloqueoGuardar = true;
			}
			else{
				distrib.bloqueoGuardar = false;
				bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_PORCENTAJE_DEBE_CUMPLIR_UN_100'));
			}
			
			gridDistribucion.refresh();
		};
		
/******************************************************/
/*** WATCH											***/
/******************************************************/		

		$scope.$watch('distrib.seleccionGrilla',function(newval,oldval)
		{
			if ((newval != oldval))
			{
				distrib.bloqueoEliminar = true;
			}
		});
		
		/*$scope.$watch('distrib.oConfiguracionTipoInstrumento.idActual',function(newval,oldval)
		{
			if(distrib.instrumento !== undefined)
			{
				distrib.oConfiguracionInstrumento.buscar = "-1"
			}
		});	*/
		

		$scope.$watch('distrib.IdTipoInstrumento', function(newval,oldval){
			
			//if (newval == 0){ $scope.Instrumento_habilitado = false; return; }
			/*if (newval != undefined & newval != oldval  ){
				$scope.Instrumento_habilitado = false;
				
				

			}*/

		});

/******************************************************/
/*** FINAL											***/
/******************************************************/
	});
}());