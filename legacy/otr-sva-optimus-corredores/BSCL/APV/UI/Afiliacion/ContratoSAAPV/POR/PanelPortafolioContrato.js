/*************************************************************
 * Purpose	: Panel Portafolio Contrato
 * Author	: TEAM PRONET IT SPA - BCS
 * Date		: 15/01/2018
 **************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelPortafolioContratoCtrl',
	function($scope,$translate,$rootScope,bcsComunes,$timeout,$state,$modal,$q,bcsConsultasAPV,bcsServiciosAPV,bcsAlerta,OPCIONES_BCSALERTA)
	{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var ppcs = this;
		
		ppcs.habilitarAgregar = true;
		ppcs.habilitoMesInicio = true;
		ppcs.habilitoMesTermino = true;
		
		ppcs.MontoPesoOrigenDeshabilitado	= true;
		ppcs.MontoUFOrigenDeshabilitado		= true;
		ppcs.PorcentajeOrigenDeshabilitado	= true;
		ppcs.ProductoAgrupadorDeshabilitado = false;
		
		ppcs.MontoPesoOrigenRequerido = true;
		ppcs.MontoUFOrigenRequerido = true;
		ppcs.PorcentajeOrigenRequerido = true;
		ppcs.PerInicioRequerido = false;
		ppcs.PerTerminoRequerido = false;
		
		ppcs.PerInicioOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year', min: new Date()};
		ppcs.PerTerminoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year', min: new Date()};
		ppcs.datosForm ={};
		$scope.csaapv.ppcs = ppcs;
		
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
		
		var dsConsultaContratoPortafolio = 
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdPortafolio", dir: "DESC" },
                pageSize        : 10,
                transport		: {
								read: {
									url 		: bcsConsultasAPV('ConsultarContratoPortafolio'),
									dataType	: "json"
							    }
				},
                schema 			: { 
                				   model: 
                						{
											fields : 
											{													
												"IdPortafolio"				: {type: "number"},
											    "IdPlanTipoInversion" 		: {type: "number"},
											    "IdProductoAgrupador" 		: {type: "number"},
												"NombrePlanTipoInversion" 	: {type: "string"},
												"Codigo" 					: {type: "string"}
											    
											}
										}
								  }
			 }
		};
		
		var defOrigenAhorro = {
				type: "array",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 100,
	                sort 			: false,
					schema: {
						model: {
							fields: {
								IdPortafolio				: { type: "number"},
								IdProducto					: { type: "string"},
								Producto					: { type: "string"},
								OrigenAhorro				: { type: "string"},
								MontoPesos					: { type: "number"},
								MontoUF						: { type: "number"},
								PorcentajeRegimenImponible	: { type: "number"},
								Pago	    				: { type: "string"},
								PagoNombre    				: { type: "string"},
								MesInicio					: { type: "string"},	
								MesTermino					: { type: "string"},
								Regimen						: { type: "string"},
								IdMatrizAgrupadorAPV		: { type: "number"},
								CodigoAgrupador				: { type: "string"},
								IdPropositoPortafolio		: { type: "string"},
							}
						}
					}		
				}	
			};
			
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
		
		ppcs.dsOrigenAhorro = new bcs.DataSource(defOrigenAhorro);
		ppcs.origenAhorro = 
		{
			dataSource: ppcs.dsOrigenAhorro,
			columns: 
			[
				{field: "OrigenAhorro"					,title: $translate.instant('UI_ORIGEN_AHORRO'),	width: "80px", align: "rigth" , hidden   : true, sortable: true, editable: false },
				{field: "Producto"						,title: $translate.instant('UI_PRODUCTO'),		width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "MontoPesos"					,title: $translate.instant('UI_MONTO_PESOS'),	width: "80px", align: "rigth" , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoPesos,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "MontoUF"						,title: $translate.instant('UI_MONTO_UF'),		width: "80px", align: "rigth" , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoUF,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "PorcentajeRegimenImponible"	,title: $translate.instant('UI_PORCENTAJE'),	width: "80px", align: "rigth" , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(PorcentajeRegimenImponible,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "Pago"							,title: $translate.instant('UI_PAGO'),			width: "80px", align: "left"  , sortable : true, editable: false, hidden: true },
				{field: "PagoNombre"					,title: $translate.instant('UI_PAGO'),			width: "80px", align: "left"  , sortable : true, editable: false },
				{field: "MesInicio"						,title: $translate.instant('UI_MES_INICIO'),	width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "MesTermino"					,title: $translate.instant('UI_MES_TERMINO'),	width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "Regimen"						,title: $translate.instant('UI_REGIMEN'),		width: "80px", align: "rigth" , sortable : true, editable: false }
			],	
			sortable		: false,
			pageable		: true,
			autoBind		: true,
			height			: 100,
			selectable 		: "row",
			userSettings	: true,
			navigatable		: true,
			gridCreated		: function(grid){
				if(parametrosVentana.modoVentana === 'M' && $scope.csaapv.validaPortafolio === false)
				{ 
					$scope.csaapv.validaPortafolio = true;
					$scope.csaapv.ConsultaContratoSAAPV($scope.csaapv.parametrosEntrada.IdContratoSAAPV);
				}
			}
		}

/******************************************************/
/*** FILTROS										***/
/******************************************************/
		
		ppcs.FiltroProductoAgrupador =
		{
			filtros:
			{
				IdPlanTipoInversion : {"field" : "IdPlanTipoInversion", "operator" : "eq", "value" : 0}
			}	
		};
		
		ppcs.FiltroRegimenAgrupador =
		{
			filtros:
			{
				IdProductoAgrupador : {"field" : "IdProductoAgrupador", "operator" : "eq", "value" : 0}
			}	
		};
			
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/

		ppcs.fnAgregoOrigenAhorro = function()
		{
			var idVentana = bcsComunes.ventanaWinId($scope);
			bcsComunes.ventanaOverlay(idVentana,true);
			
			if($scope.csaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === undefined)
			{
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCIONE_PLAN"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			if(ppcs.ProductoAgrupadorSeleccionado.NombreProductoAgrupador === undefined)
			{
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCIONE_PRODUCTO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			if(ppcs.RegimenAgrupadorSeleccionado.IdMatrizAgrupadorAPV === undefined)
			{
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCIONE_REGIMEN"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}	
			if(ppcs.tipoPagoSeleccion.Codigo === undefined)
			{
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCIONE_PAGO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			if(ppcs.kngMontoPesoOrigen === undefined && ppcs.kngMontoUFOrigen === undefined && ppcs.kngPorcentajeOrigen === undefined)
			{
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_INGRESE_MONTO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			if(ppcs.tipoPagoSeleccion.Codigo === 'TEM')
			{
				if(ppcs.kngPeriodoInicio === undefined)
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_INGRESE_PERIODO_INICIO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
				
				if(ppcs.kngPeriodoTermino === undefined)
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_INGRESE_PERIODO_TERMINO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
				
				if(ppcs.kngPeriodoInicio >= ppcs.kngPeriodoTermino)
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_PERIODO_INICIO_DEBE_SER_MENOR_A_PERIODO_TERMINO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					
					ppcs.PerInicioOptions.max = new Date("9999-12-31");
					ppcs.PerTerminoOptions.min = new Date();
					
					ppcs.kngPeriodoInicio	= undefined;
					ppcs.kngPeriodoTermino 	= undefined;
					
					return;
				}
			}
			else
			{
				if(ppcs.kngPeriodoInicio === undefined)
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_INGRESE_PERIODO_INICIO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			}

			var filtro = Array();
			filtro.push({ field: "IdPlanTipoInversion", operator: "eq", value: Number($scope.csaapv.NombrePlanAhorroSeleccionado.IdPlanTipoInversion)});
			filtro.push({ field: "IdProductoAgrupador", operator: "eq", value: Number(ppcs.ProductoAgrupadorSeleccionado.IdProductoAgrupador)});
			filtro.push({ field: "Codigo"			  , operator: "eq", value: ppcs.RegimenAgrupadorSeleccionado.Codigo});
			filtro.push({ field: "IdPersona"		  , operator: "eq", value: Number($scope.csaapv.IdPersona)});
			
			var ConsultaContratoPortafolio = new bcs.DataSource(dsConsultaContratoPortafolio);
			ConsultaContratoPortafolio.query({ filter : filtro}).then(function()
			{
				if(ConsultaContratoPortafolio.view().length > 0)
				{
					$scope.csaapv.IdPortafolioExiste = ConsultaContratoPortafolio.view()[0].IdPortafolio;
					var t = bcsAlerta.crear("PREGUNTA_SI_NO","Ya existe un Contrato asignado a un portafolio, ¿Desea asignarlo?" ,idVentana);
					t .then(function(result)
					{
						if (result === OPCIONES_BCSALERTA.NO)
						{
							bcsComunes.ventanaOverlay(idVentana,false);
							return;
						}
						fnAgregoOrigenAhorro();
					});
				}
				else
				{
					fnAgregoOrigenAhorro();
				}	
				$scope.csaapv.habilitoPlanAhorro 	= true;
			});
			
			bcsComunes.ventanaOverlay(idVentana,false);
		};
			
		fnAgregoOrigenAhorro = function ()
		{
			var agregar = 'S';
			var valor =''
			var busco = false;
			if(ppcs.dsOrigenAhorro.view().length > 0)
			{			
				$.each(ppcs.dsOrigenAhorro.view(),function(key,value){
					if((ppcs.RegimenAgrupadorSeleccionado.IdMatrizAgrupadorAPV === value.IdMatrizAgrupadorAPV) 
						|| (ppcs.RegimenAgrupadorSeleccionado.Codigo.substring(0,3) === value.Regimen.substring(0,3))
							||(ppcs.RegimenAgrupadorSeleccionado.Codigo.substring(0,2) === value.Regimen.substring(0,2)))
						busco = true;
				});
			}
			
			if(busco === false)
			{
				var v_OrigenAhorro = "";
				if(ppcs.RegimenAgrupadorSeleccionado.Codigo.substring(0, 3) === "APV") v_OrigenAhorro = "APV";
				if(ppcs.RegimenAgrupadorSeleccionado.Codigo.substring(0, 2) === "DC") v_OrigenAhorro = "DC";
				/*EOR*/
				if(ppcs.dsOrigenAhorro.view().length > 0)
				 {
					agregar = 'N';
			  		if(ppcs.dsOrigenAhorro.view()[0].MontoPesos !== undefined)
					{valor = 'PESOS';	}	
					if(ppcs.dsOrigenAhorro.view()[0].MontoUF !== undefined)
					{valor = 'U.F.';}				
					if(ppcs.dsOrigenAhorro.view()[0].PorcentajeOrigen !== undefined)
					{valor = '% R.I.';}
					if(valor === 'PESOS' && ppcs.kngMontoPesoOrigen === undefined)
					{
						bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_INGRESAR_UN_MONTO_AHORRO_")+valor);
						return;
					}else if (valor === 'U.F.' && ppcs.kngMontoUFOrigen === undefined)
							{
							bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_INGRESAR_UN_MONTO_AHORRO_")+valor);
							return;
							}else if (valor === '% R.I.' && ppcs.kngPorcentajeOrigen === undefined)
									{
										bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_INGRESAR_UN_MONTO_AHORRO_")+valor);
										return;
									}else{
										agregar = 'S';
									}
						
				}
				/*EOR*/ 
				if(agregar === 'S'){
					ppcs.dsOrigenAhorro.add({
						IdPortafolio		  		: $scope.csaapv.IdPortafolioExiste,
						IdProducto 			  		: ppcs.ProductoAgrupadorIdActual,
						Producto 			  		: ppcs.ProductoAgrupadorSeleccionado.NombreProductoAgrupador,
						OrigenAhorro		  		: v_OrigenAhorro,
						MontoPesos			  		: ppcs.kngMontoPesoOrigen,
						MontoUF				  		: ppcs.kngMontoUFOrigen,
						PorcentajeRegimenImponible	: ppcs.kngPorcentajeOrigen,
						Pago				  		: ppcs.tipoPagoSeleccion.Codigo,
						PagoNombre			  		: ppcs.tipoPagoSeleccion.Nombre,
						MesInicio			  		: ppcs.kngPeriodoInicio === undefined ? "0001-01" : ppcs.kngPeriodoInicio.toISOString().slice(0,7),
						MesTermino			  		: ppcs.kngPeriodoTermino === undefined ? "9999-01" : ppcs.kngPeriodoTermino.toISOString().slice(0,7),
						Regimen				  		: ppcs.RegimenAgrupadorSeleccionado.Codigo,
						IdMatrizAgrupadorAPV  		: ppcs.RegimenAgrupadorSeleccionado.IdMatrizAgrupadorAPV,
						CodigoAgrupador		  		: ppcs.RegimenAgrupadorSeleccionado.CodigoAgrupador,
						IdPropositoPortafolio 		: ppcs.RegimenAgrupadorSeleccionado.IdPropositoPortafolio
					});	
					ppcs.LimpioVariablesOrigen();
					$scope.csaapv.paramModificaDistribucion = 1;
					$scope.csaapv.IdProducto=ppcs.ProductoAgrupadorIdActual;
					$scope.csaapv.limpiaDistribucion = true;
					
					ppcs.MontoPesoOrigenRequerido = true;
					ppcs.MontoUFOrigenRequerido = true;
					ppcs.PorcentajeOrigenRequerido = true;
					
					ppcs.PerInicioRequerido = false;
					ppcs.PerTerminoRequerido = false;
					
					ppcs.habilitoMesInicio = true;
					ppcs.habilitoMesTermino = true;
					
					ppcs.MontoPesoOrigenDeshabilitado	= true;
					ppcs.MontoUFOrigenDeshabilitado		= true;
					ppcs.PorcentajeOrigenDeshabilitado	= true;
				}
			}
			else
			{
				bcsComunes.msg("warning","warning","No puede tener dos regimenes APV en un mismo contrato.");
			}
		};
			
		ppcs.fnEliminoOrigenAhorro = function(){
			if(ppcs.origenAhorroSeleccionado === undefined)
				return;
			if($scope.csaapv.paramVentanaDistribuir.length > 0){
				var t = bcsAlerta.crear("PREGUNTA_SI_NO","Existen datos en Distribución si elimina un Ahorro se borrará la distribución ¿Desea continuar?" ,idVentana);
				t .then(function(result)
				{
					if (result === OPCIONES_BCSALERTA.NO)
					{
						return;
					}
					$scope.csaapv.habilitoPlanAhorro = false;
					ppcs.dsOrigenAhorro.remove(ppcs.origenAhorroSeleccionado);
					//$scope.csaapv.paramVentanaDistribuir.length = 0;
					$scope.csaapv.paramVentanaDistribuir = {};
					$scope.csaapv.paramModificaDistribucion = 1;
					$scope.csaapv.limpiaDistribucion = true;
				});	
			}else{

				ppcs.dsOrigenAhorro.remove(ppcs.origenAhorroSeleccionado);
				$scope.csaapv.paramVentanaDistribuir = {};
				$scope.csaapv.habilitoPlanAhorro = true;
				
			}
			
			if(ppcs.dsOrigenAhorro.view().length === 0){
				
				$scope.csaapv.habilitoPlanAhorro = false;
			}
		};
			
		ppcs.LimpioVariablesOrigen = function()
		{	
			ppcs.kngMontoPesoOrigen 		= undefined;
			ppcs.kngMontoUFOrigen 			= undefined;
			ppcs.kngPorcentajeOrigen 		= undefined;
			ppcs.kngPeriodoInicio 			= undefined;
			ppcs.kngPeriodoTermino 			= undefined;
			ppcs.ProductoAgrupadorIdActual 	= false;
			ppcs.RegimenAgrupadorIdActual	= false;
			ppcs.TipoPagoIdActual 			= false;
		};
			
		ppcs.validaPeriodo = function()
		{
			if(ppcs.tipoPagoSeleccion === undefined || ppcs.tipoPagoSeleccion === false)
			{
				ppcs.kngPeriodoInicio = tipo === 'I' ? '' : ppcs.kngPeriodoInicio;
				ppcs.kngPeriodoTermino = tipo === 'T' ? '' : ppcs.kngPeriodoTermino;
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_UN_TIPO_DE_PAGO"));
				return;
			}
		};
		
		ppcs.Requerido = function(tipo)
		{
			if(tipo === 'MONTO')
			{
				if(ppcs.MontoPesoOrigen !== null)
				{
					ppcs.MontoUFOrigenRequerido 		= false;
					ppcs.MontoUFOrigenDeshabilitado		= false;
					ppcs.PorcentajeOrigenRequerido 		= false;
					ppcs.PorcentajeOrigenDeshabilitado	= false;
				}
			}
			else if(tipo === 'UF')
			{
				if(ppcs.MontoUFOrigen !== null)
				{
					ppcs.MontoPesoOrigenRequerido		= false;
					ppcs.MontoPesoOrigenDeshabilitado	= false;
					ppcs.PorcentajeOrigenRequerido 		= false;
					ppcs.PorcentajeOrigenDeshabilitado	= false;
				}
			}
			
			if(ppcs.MontoPesoOrigen === null || ppcs.MontoPesoOrigen === undefined)
			{
				if(ppcs.MontoUFOrigen === null || ppcs.MontoUFOrigen == undefined)
				{
					if(ppcs.PorcentajeOrigen === null || ppcs.PorcentajeOrigen === undefined)
					{
						ppcs.MontoPesoOrigenRequerido 		= true;
						ppcs.MontoPesoOrigenDeshabilitado	= true;
						ppcs.MontoUFOrigenRequerido 		= true;
						ppcs.MontoUFOrigenDeshabilitado		= true;
						ppcs.PorcentajeOrigenRequerido 		= true;
						ppcs.PorcentajeOrigenDeshabilitado	= true;
					}
				}
			}
		};
		
/******************************************************/
/*** WATCH											***/
/******************************************************/

		$scope.$watch('ppcs.tipoPagoSeleccion',function(newval,oldval)
		{
			if(ppcs.tipoPagoSeleccion !== undefined && ppcs.tipoPagoSeleccion !== false)
			{
				if(ppcs.tipoPagoSeleccion.Codigo === 'IND' || ppcs.tipoPagoSeleccion.Codigo === 'UNI')
				{
					ppcs.habilitoMesInicio = false;
					ppcs.habilitoMesTermino = true;
					
					ppcs.PerInicioRequerido = true;
					ppcs.PerTerminoRequerido = false;
				}
				else
				{
					ppcs.habilitoMesInicio = false;
					ppcs.habilitoMesTermino = false;
					
					ppcs.PerInicioRequerido = true;
					ppcs.PerTerminoRequerido = true;
				}
				ppcs.kngPeriodoTermino = undefined;
			}
		});

		$scope.$watch('csaapv.PlanAhorroIdActual ',function(newval,oldval)
		{

			if ($scope.csaapv.PlanAhorroIdActual !== false ){
				ppcs.ProductoAgrupadorDeshabilitado =true;
			}else{
				ppcs.ProductoAgrupadorDeshabilitado =false;
			}
		
		});
		
		$scope.$watch('ppcs.PorcentajeOrigen',function(newval,oldval)
		{
			if(ppcs.PorcentajeOrigen !== null)
			{
				ppcs.MontoPesoOrigenRequerido 		= false;
				ppcs.MontoUFOrigenRequerido 		= false;
				ppcs.MontoPesoOrigenDeshabilitado	= false;
				ppcs.MontoUFOrigenDeshabilitado		= false;
			}
			
			if(ppcs.MontoPesoOrigen === null || ppcs.MontoPesoOrigen === undefined)
			{
				if(ppcs.MontoUFOrigen === null || ppcs.MontoUFOrigen == undefined)
				{
					if(ppcs.PorcentajeOrigen === null || ppcs.PorcentajeOrigen === undefined)
					{
						ppcs.MontoPesoOrigenRequerido 		= true;
						ppcs.MontoPesoOrigenDeshabilitado	= true;
						ppcs.MontoUFOrigenRequerido 		= true;
						ppcs.MontoUFOrigenDeshabilitado		= true;
						ppcs.PorcentajeOrigenRequerido 		= true;
						ppcs.PorcentajeOrigenDeshabilitado	= true;
					}
				}
			}
		});
		
		$scope.$watch('ppcs.TipoPagoIdActual',function(newval,oldval)
		{
			if(ppcs.TipoPagoIdActual !== undefined && ppcs.TipoPagoIdActual !== false)
			{
				if(ppcs.TipoPagoIdActual === 'UNI' || ppcs.TipoPagoIdActual === 'IND')
				{
					ppcs.PerInicioOptions.max = new Date("9999-12-31");
					ppcs.PerTerminoOptions.min = new Date();
				}
				
				ppcs.kngPeriodoInicio	= undefined;
				ppcs.kngPeriodoTermino 	= undefined;
			}
		});
		
		$scope.$watch('ppcs.kngPeriodoInicio',function(newval,oldval)
		{
			if(ppcs.kngPeriodoInicio !== undefined && ppcs.kngPeriodoInicio !== null)
			{
				if(ppcs.tipoPagoSeleccion.Codigo === 'TEM')
				{
					var dateInicio	= new Date();
					dateInicio		= ppcs.kngPeriodoInicio;
					
					ppcs.PerTerminoOptions.min = dateInicio;
				}
				
				if(ppcs.tipoPagoSeleccion.Codigo === 'UNI')
				{
					ppcs.PerTerminoOptions.min = new Date();
					ppcs.kngPeriodoTermino = ppcs.kngPeriodoInicio;
				}
			}
		});
		
		$scope.$watch('ppcs.kngPeriodoTermino',function(newval,oldval)
		{
			if(ppcs.kngPeriodoTermino !== undefined && ppcs.kngPeriodoTermino !== null)
			{
				if(ppcs.tipoPagoSeleccion.Codigo === 'TEM')
				{
					var dateFin		= new Date();
					dateFin			= ppcs.kngPeriodoTermino;
					
					ppcs.PerInicioOptions.max = dateFin;
				}
			}
		});
		
		
		
/******************************************************/
/*** FINAL											***/
/******************************************************/
	});
}());