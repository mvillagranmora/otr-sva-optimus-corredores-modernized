angular.module('ModuloNormativa').controller('MantenedorDigitadoCtrl',
	function($scope,$uibModal,$translate,bcsComunes,bcsServiciosNormativa, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var overlay = bcsComunes.overlay;
	
	//Inicializacion de variables
	$scope.data = {};
	$scope.Item = "";	
	$scope.filtro = {item: '', tipo: '', nombreCorto:''};	
	$scope.habilitado = false;
	$scope.habilitadoDigitada = false;
	$scope.inputDisabled = true;
	var IdItemConfiguracion = 0;
	$scope.data.ChkMayorCero = false;
	$scope.data.ChkMenorCero = false;
	$scope.data.ChkPermiteCero = false;
	var idItem = 0;
	
	$scope.data.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	
	if($scope.data.datosEntrada.items != undefined){
		$scope.data.FechaContable = $scope.data.datosEntrada.items.FechaContable;
		$scope.data.IdEmpresa = $scope.data.datosEntrada.items.IdEmpresa;
		$scope.data.Reporte = $scope.data.datosEntrada.items.Reporte;

	
		$scope.data.kngReporte = $scope.data.datosEntrada.items.Reporte;
		$scope.kngFechaContable = $scope.data.FechaContable;
	};
	
    //---------------------------------------------
    //----------------- SERVICIOS -----------------
    //---------------------------------------------
	$scope.BuscarItemDig = function() {
 		var parametros = {};
		parametros.p_IdTipoInforme		= $scope.data.Reporte;
		parametros.p_IdIndice			= 0;
		parametros.p_IdConcepto		= 0;
		parametros.p_IdItem			= 0;
		parametros.p_FechaContable		= $scope.data.FechaContable;
		parametros.p_IdEmpresa 		= $scope.data.IdEmpresa; 
		parametros.p_IdUsuario 		= $scope.SesionIdUsuario;
    	bcsServiciosNormativa.ItemDigBuscar(parametros,success,error);
    }; 

    var success = function(respuesta)
    {
    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
			if(respuesta.P_OUT_CAMPOSDIGITADOSSUPERIOR.length > 0)
	    		dataSourceItemDigitado.data(respuesta.P_OUT_CAMPOSDIGITADOSSUPERIOR); 
  
			if(respuesta.P_OUT_CAMPOSDIGITADOSINFERIOR.length > 0)	    		
	    		dataSourceItemConfiguracion.data(respuesta.P_OUT_CAMPOSDIGITADOSINFERIOR);
 
			$scope.limpiar();
    	}
    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
    	{
    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
    		{
    			bcsComunes.ventanaMsg({
				      mensaje:$translate.instant("UI_ERROR"),
				      conflictos:{
				       errores : [ {
				           Codigo : 'COD',
				           Descripcion : value.Descripcion
				          } ],
				      }
  				},$scope.winID);
    		});
    	}

    };
    var error = function(respuesta)
    {
    	bcsComunes.ventanaMsg({
		      mensaje:$translate.instant("UI_ERROR"),
		      conflictos:{
		       errores : [ {
		           Codigo : 'COD',
		           Descripcion : ''
		          } ],
		      }
		},$scope.winID);    	
    };
    
	$scope.grabar = function() {
		
		var tabItemDigitado = [];
		
		if(dataSourceItemDigitado.data().length > 0){
			$.each(dataSourceItemDigitado.data(),function(key,value){
					tabItemDigitado.push({IdTipoInforme: $scope.data.Reporte, IdItem: value.IdItem, Monto: value.Monto });
			});
			
			var parametros = {};
	 		parametros.p_IdEmpresa			= $scope.data.IdEmpresa;
	 		parametros.p_IdUsuario			= $scope.SesionIdUsuario;
	 		parametros.p_IdModulo			= 'ILS'; 
	 		parametros.p_UsaReglasNegocio 	= 'N';
	 		parametros.p_FechaContable		= util.formatoDate($scope.data.FechaContable);
	 		parametros.p_TabItemDigitado 	= tabItemDigitado; 
	    	bcsServiciosNormativa.ItemGrabarDig(parametros,succesGrabar,errorGrabar);
		}else{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_NO_HAY_DATOS"),
				conflictos:{
				       errores : [ {
				           Codigo : 'COD',
				           Descripcion : ''
				          } ],
				      }
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
		}
    }; 
     
    var succesGrabar = function(respuesta)
    {
    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
    		$scope.BuscarItemDig();
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_GRABAR_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
    	}  
    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
    	{
    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
    		{
    			bcsComunes.ventanaMsg({
    			      mensaje:$translate.instant("UI_ERROR"),
    			      conflictos:{
    			       errores : [ {
    			           Codigo : 'COD',
    			           Descripcion : value.Descripcion
    			          } ],
    			      }
    			},$scope.winID);
    		});
    	}
    };
    var errorGrabar = function(respuesta)
    {		
		bcsComunes.ventanaMsg({
		      mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
		      conflictos:{
		       errores : [ {
		           Codigo : 'COD',
		           Descripcion : ''
		          } ],
		      }
		},$scope.winID);
		
		overlay.encendido = false;
		bcsComunes.ventanaOverlay($scope.winID, false);
    	
    };
	
    //---------------------------------------------
    //------------------ MODELOS ------------------
    //---------------------------------------------
	
	// Definir modelo Vista Item.
   //Se agrega Saldo CELCONDOR-598    
	ModeloItem = function() {
		return {
			model: {
				fields : {
					IdItem 		 	: { type: "number"},
					IdTipoItem 	 	: { type: "number"},
					NombreItem 	 	: { type: "string"},
					NombreCortoItem	: { type: "string"},
					Monto 			: { type: "number"},
					MayorACeroItem 	: { type: "string"},
					MenorACeroItem	: { type: "string"},
					PermiteCeroItem : { type: "string"},					
					Saldo			: { type: "number"}
	        	}
			}
		};
	}; 
	
	// Definir modelo Vista Item Configuracion.
	ModeloItemConfiguracion = function() {
		return {
			model: {
    	        	fields:{
    	        		IdItem 				: { type: "number"},
    	        		NombreInforme 		: { type: "string"},    	        		
    	        		NombreIndice 		: { type: "string"},
    	        		OperadorIndice 		: { type: "string"},
    	        		FactorIndice 		: { type: "number"},
    	        		NombreConcepto 		: { type: "string"},
    	        		OperadorConcepto 	: { type: "string"},
    	        		FactorConcepto 		: { type: "number"},
    	        		NombreItem 			: { type: "string"},
    	        		Monto 				: { type: "number"},
    	        	} 
			}
		};
	};
	
    //---------------------------------------------
    //---------------- DATASOURCES ----------------
    //---------------------------------------------
	
	dataSourceItemDigitado =  new bcs.DataSource({
		type 		: "array",
		definition 	: {
						//serverFiltering 	: true, // Filtrado de datos en el servidor.
						//serverPaging 		: true, // Paginado de los datos en el servidor.
						//serverSorting 		: false, // Ordenamiento de los datos en el servidor.
						sortable 			: true,
						pageSize 			: 50,
						sort 			: { field: "IdItem", dir: "ASC" },
						schema 				: ModeloItem(),
				  	}
	});			

	
	var defItemConfiguracion = {
			type 		: "array",
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							pageSize 			: 50,
							sort 			: { field: "IdItem", dir: "ASC" },
							schema 				: ModeloItemConfiguracion(),
						  }
	};
	
	var dataSourceItemConfiguracion = new bcs.newDataSource(defItemConfiguracion);
	
    //---------------------------------------------
    //------------------ COLUMNAS -----------------
    //---------------------------------------------

	// Columnas de la grilla superior, Items 
	// Se agrega Saldo CELCONDOR-598
	columnasItem = function() {
		return [ 
			    {field: "IdItem"	, title: "UI_ITEM"		 	, width: "10px", hidden: true}
		       ,{field: "NombreItem", title: "UI_NOMBRE_ITEM"	, width: "170px",	editable: false}
//		       ,{field: "Monto"		, title: "UI_MONTO"		 	, width: "50px",	editable: true, template : "#: (util.bcs_formato(Monto,'SPT','Cantidad','USD','','')).valor #" }
//		       ,{field: "Saldo"	    , title: "UI_MONTO_CONTABLE", width: "50px",	template : "#: (util.bcs_formato(Saldo,'SPT','Cantidad','CLP','','')).valor #" }
		       ,{field: "Monto"		, title: "UI_MONTO"		 	, width: "50px", 	editable: true, template : "#: (util.bcs_formato(Monto,'SPT','Cantidad','CLP','','')).valor #" }
		       ,{field: "Saldo"	    , title: "UI_MONTO_CONTABLE", width: "50px", 	template :"#: (util.bcs_formato(Saldo,'GEN','Monto','CLP','','INDICE')).valor #"}
		       
		       
		       
				];
	};
	 
	// Columnas de la grilla Inferior, Items Configuracion 
	columnasItemConfiguracion = function() {
		return  [
			    {field: "IdItem"			, title: "UI_ITEM"				, width: "10px", hidden: true}
		       ,{field: "NombreInforme"		, title: "UI_NOMBRE_INFORME"	, width: "200px"}
			   ,{field: "NombreIndice"		, title: "UI_NOMBRE_INDICE"		, width: "200px"}
			   ,{field: "OperadorIndice"	, title: "UI_OPERACION"			, width: "50px", align: "right"}
		       ,{field: "FactorIndice"		, title: "UI_FACTOR"			, width: "50px", template : "#: (util.bcs_formato(FactorIndice,'ILS','Cantidad','USD','','')).valor #"}
		       ,{field: "NombreConcepto"	, title: "UI_NOMBRE_CONCEPTO"	, width: "500px"}  
		       ,{field: "OperadorConcepto"	, title: "UI_OPERACION"			, width: "50px", align: "right"}
		       ,{field: "FactorConcepto"	, title: "UI_FACTOR"			, width: "50px", template : "#: (util.bcs_formato(FactorConcepto,'ILS','Cantidad','USD','','')).valor #"}
		       ,{field: "NombreItem"		, title: "UI_NOMBRE_ITEM"		, width: "200px"}
		       ,{field: "Monto"				, title: "UI_MONTO"				, width: "100px", template : "#: (util.bcs_formato(Monto,'SPT','Cantidad','USD','','')).valor #" }
	            ];
	};    
    
    // Configuración Items
	$scope.Item = {
			autoBind		: true,
			dataSource		: dataSourceItemDigitado,
			columns			: columnasItem(),		
			filterable		: false,
			sortable		: true,
			pageable		: true,
			height			: 380,
		    selectable 		: "row",
		    save : function(e)  {
		    	if($scope.data.ChkMayorCero == true && $scope.data.ChkMenorCero == false && $scope.data.ChkPermiteCero == false && e.values.Monto <= 0 ){
					bcsComunes.msg("error","error",$translate.instant('UI_ERROR_MAYOR_0'));
					overlay.encendido = false;
				}else if($scope.data.ChkMayorCero == true && $scope.data.ChkMenorCero == false && $scope.data.ChkPermiteCero == true && e.values.Monto < 0){
					bcsComunes.msg("error","error",$translate.instant('UI_ERROR_MAYOR_IGUAL_0'));
					overlay.encendido = false;
				}		
				else if($scope.data.ChkMayorCero == false && $scope.data.ChkMenorCero == true && $scope.data.ChkPermiteCero == false && e.values.Monto >= 0){
					bcsComunes.msg("error","error",$translate.instant('UI_ERROR_MENOR_0'));
					overlay.encendido = false
				}	
				else if($scope.data.ChkMayorCero == false && $scope.data.ChkMenorCero == true && $scope.data.ChkPermiteCero == true && e.values.Monto > 0){
					bcsComunes.msg("error","error",$translate.instant('UI_ERROR_MENOR_IGUAL_0'));
					overlay.encendido = false;
				}else{
					$scope.data.kngMonto = e.values.Monto;
					return;
				}
		    	e.preventDefault();
		    },
	};
	
	// Configuración ItemConfiguracion
	$scope.ItemConfiguracion = {
			dataSource			: dataSourceItemConfiguracion,
			columns				: columnasItemConfiguracion(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,
			height				: 230,
		    selectable 			: "multiple row"
	};
		
    //---------------------------------------------
    //------------------ EVENTOS ------------------
    //---------------------------------------------
	
	$scope.$watch('seleccionItemProgramado', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			$scope.habilitadoProgramado = true;
		}
	},true);

	//---------------------------------------------
	//----------------- LIMPIAR ------------------
	//---------------------------------------------
	
	// Función para limpiar los campos de la búsqueda 
	$scope.limpiar = function(){
		
		$scope.data.kngMonto = undefined;
		$scope.data.ChkMayorCero = false;
		$scope.data.ChkMenorCero =  false;
		$scope.data.ChkPermiteCero =  false;
	}
	var idItem = 0;
    //---------------------------------------------
    //------------------ EVENTOS ------------------
    //---------------------------------------------
	$scope.$watch('seleccionItem',function(newval,oldval){
		if($scope.seleccionItem === undefined) return;
		
		$scope.data.kngMonto = newval.Monto;
		$scope.data.ChkMayorCero = newval.MayorACeroItem == 'S' ? true : false;
		$scope.data.ChkMenorCero = newval.MenorACeroItem == 'S' ? true : false;
		$scope.data.ChkPermiteCero = newval.PermiteCeroItem == 'S' ? true : false;
		idItem = newval.IdItem;

	});
	
	
	$scope.BuscarItemDig();
	
});
