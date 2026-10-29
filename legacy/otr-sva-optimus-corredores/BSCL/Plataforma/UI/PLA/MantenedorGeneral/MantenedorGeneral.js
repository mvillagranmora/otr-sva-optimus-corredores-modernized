angular.module('ModuloPlataforma').controller('MantenedorGeneralCtrl',
		function($scope,$translate,$resource,bcsComunes, $uibModal,bcsServicioPlataforma, 
				$timeout){
	
	/*****************************************************************
    * DECLARACION DE TIPOS                                          *
    *****************************************************************/	
	
	$scope.data	= {};
	$scope.data.accionesMantenedor = new Array();
	$scope.data.mostrar = false;
	$scope.data.mostrarGrid = false;
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.fnConsultaAtributoMantenedor = function() {
		return $resource("", {},{
			'call': { method: "POST", url: "/BSCL/Plataforma/"}
		});
	};   	

	
	/******************************************************************
	 * ACCION AGREGAR                                                 * 
	 * Ingresa a la tabla correspondiente al mantenedor dinamico      *
	 * seleccionado en la opcion del menu del arbol,un nuevo registro * 
     ******************************************************************/
	//Lista campos de mantenedor
	$scope.data.fnBotonAgregar = function () {
		//Lista atributos(columnas) funcion agregar
		var listaAtributosFnAgregarDataSource = new kendo.data.DataSource({
			type:"odata",
			serverFiltering: true,
			serverSorting: true,  //ordena al lado del servidor
			sort: { field: "IdOrden", dir: "asc" }, //campo y criterio de ordenamiento
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1440",
					dataType: "json"
				},
			}
		});
		
		
		listaAtributosFnAgregarDataSource.query({
			filter: [ { field: "IdMantenedor", operator: "eq", value: ($scope.data.currentMenu).codigo }, //dataItem.codigo
			          { field: "VisibleAgregar", operator: "eq", value: "S" }
			]
		}).then(function() {
			var viewAtriFnAgregar = listaAtributosFnAgregarDataSource.view();
			var columnsAtriFnAgregar = [];
			
			for (var i = 0; i < viewAtriFnAgregar.length;i++){
				columnsAtriFnAgregar.push({field: viewAtriFnAgregar[i].NombreCampo,width: "50px",title: $translate.instant(viewAtriFnAgregar[i].NombreDicionario)})				
			}
			
					
			var camposAtr = [];

			for (var i = 0; i < viewAtriFnAgregar.length; i++){
				var campo = {};
			    campo.NombreCampo = viewAtriFnAgregar[i].NombreCampo;
			    campo.NombreDiccionario = viewAtriFnAgregar[i].NombreDiccionario;			    
			    campo.ComponenteUI = viewAtriFnAgregar[i].ComponenteUI;
			    campo.Formato = viewAtriFnAgregar[i].Formato;			    
			    campo.TipoDatoCampo = viewAtriFnAgregar[i].TipoDatoCampo;
			    campo.RequeridoCampo = viewAtriFnAgregar[i].RequeridoCampo === 'S' ? true : false;
			    campo.maxLength = viewAtriFnAgregar[i].NombreCampo === 'IdMercado' ? 4 : undefined;
				camposAtr.push(campo);
			}

			if(camposAtr.length>0){
				var parametros = {
					dato1:camposAtr,
					Mantenedor : ($scope.data.currentMenu).codigo,
				};
				bcsComunes.ventanaOverlay($scope.idVentana, true);
				var id1 = bcsComunes.ventanaAbrir("AgregarAtributoMantenedorGeneral",parametros,false,false);
				var promesa=bcsComunes.ventanaPromesa(id1);
				promesa.then(function(respuesta){
					if(respuesta){
						refrescarGrilla($scope.data.currentMenu);
					}
					bcsComunes.ventanaOverlay($scope.idVentana, false);
				});
			}else{
				bcsComunes.msg("info","Mantenedor General","Ingreso inhabilitado.");
			}
		});		
	};
	//End Lista campos de mantenedor	
	/****************************************************************
	 * END ACCION AGREGAR                                           *
     ****************************************************************/
	
	
	/*****************************************************************
	 * ACCION MODIFICAR                                              *
	 * Modifica en la tabla correspondiente al mantenedor dinamico   *
	 * seleccionado en la opcion del menu del arbol, el registro     *
     *****************************************************************/	
	//Lista campos de mantenedor
	$scope.data.fnBotonModificar = function () {
		$scope.data.habilitado = true;
		$scope.data.mostrar = true;
		
		if($scope.data.seleccion != undefined){
			$scope.data.LlavePrimaria = $scope.data.seleccion[$scope.data.CampoPrimario.NombreCampo];
		}
		else{return;}
		
		//shechenleitner 30-05-2017
		//Cambio de fecha se debe realizar por medio del mantenedor "Ajuste Fecha Sistema"
		if ($scope.data.LlavePrimaria == "PLA.FechaSistema") {
			bcsComunes.msg("error","error",$translate.instant('ERROR_MANTENEDOR_FECHA_SISTEMA'));
			return;
		}
		
		$scope.data.CampoPK = [];
		$scope.data.modificar = [];
		
		//Lista atributos(columnas) funcion modificar
		var listaAtributosFnModificarDataSource = new kendo.data.DataSource({
			type:"odata",
			serverFiltering: true,
			serverSorting: true,  //ordena al lado del servidor
			sort: { field: "Orden", dir: "asc" }, //campo y criterio de ordenamiento
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1440",
					dataType: "json"
				},
			},
		});
		
		

		listaAtributosFnModificarDataSource.query({
			filter: [ { field: "IdMantenedor", operator: "eq", value: ($scope.data.currentMenu).codigo }, //dataItem.codigo
			          { field: "VisibleModificar", operator: "eq", value: "S" }
			]
		}).then(function(){
			

			var viewAtriFnModificar = listaAtributosFnModificarDataSource.view();
			var columnsAtriFnModificar = [];
			var camposAtr = [];

			for (var i = 0; i < viewAtriFnModificar.length; i++){
				var campo = {};
			    campo.NombreCampo = viewAtriFnModificar[i].NombreCampo;
			    campo.NombreDiccionario = viewAtriFnModificar[i].NombreDiccionario;			    
			    campo.ComponenteUI = viewAtriFnModificar[i].ComponenteUI;
			    campo.Formato = viewAtriFnModificar[i].Formato;			    
			    campo.TipoDatoCampo = viewAtriFnModificar[i].TipoDatoCampo;
			    campo.RequeridoCampo = viewAtriFnModificar[i].RequeridoCampo === 'S' ? true : false;
				camposAtr.push(campo);
			    
			    $scope.data.modificar[campo.NombreCampo] = $scope.data.seleccion[campo.NombreCampo];
			}
			

			for (var i = 0; i < $scope.atributos.length; i++){
				if ($scope.atributos[i].TipoCampo == "PK"){
					//$scope.data.CampoPrimario = viewAtri[i];
					//[  { nombre: pk1 , valor: valor1 },{ nombre: pk2 , valor: valor2 } ]
					$scope.data.CampoPK.push(  { nombre: $scope.atributos[i].NombreCampo , valor: $scope.data.seleccion[$scope.atributos[i].NombreCampo] } );
				}
				//columnsAtri.push({field: viewAtri[i].NombreCampo,width: "50px",title: $translate.instant(viewAtri[i].NombreDicionario)})
			}

			var parametros = {
				dato1:camposAtr,
				dato2: $scope.data.modificar,
				LlavePrimaria :  $scope.data.LlavePrimaria,
				CampoPrimario : $scope.data.CampoPrimario.NombreCampo,
				Mantenedor : ($scope.data.currentMenu).codigo,
				CampoPK : $scope.data.CampoPK,
				TituloMantenedor : viewAtriFnModificar[0].TituloMantenedor
			};
			bcsComunes.ventanaOverlay($scope.idVentana, true);
			var id1 = bcsComunes.ventanaAbrir("ModificarAtributoMantenedorGeneral",parametros,false,false);
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        promesa.then(function(respuesta){
	        	if(respuesta){
	        		refrescarGrilla($scope.data.currentMenu);
	        		$scope.data.seleccion = undefined;
				}
				bcsComunes.ventanaOverlay($scope.idVentana, false);
	    	});

		});

		
	};
	//End Lista campos de mantenedor	
	          
	/*****************************************************************
	 * END ACCION MODIFICAR                                          *
     *****************************************************************/ 
	          	          
	/*****************************************************************
	 * ACCION ELIMINAR                                               *
	 * Eliminar(cambiar estado) a registro seleccionado de la grilla *
     *****************************************************************/
	$scope.data.fnBotonEliminar = function () {
		if($scope.data.seleccion != undefined){
			$scope.data.LlavePrimaria = $scope.data.seleccion[$scope.data.CampoPrimario.NombreCampo];
		}
		else{return;}
		
		$scope.data.eliminar = [];
		
		var urlEnvio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=43";
	    var resourceAccionRegistro = $resource("", {}, { 'eliminar': { method: "POST", url: urlEnvio} });
	    
	    var datos = {};
		datos.p_Mantenedor = "";
		datos.p_Dato = []; 
	       
	    var parametrosSP = {};
	    var numFila = 1;
        var p_Dato = [];
	    
	    /**** Recupera Id desde seleccion de grilla ***/
	    p_Dato.push({NumFila: numFila, Campo: $scope.data.CampoPrimario.NombreCampo, Valor: $scope.data.LlavePrimaria});
	    
	    /**** End Recupera Id desde seleccion de grilla ****/
	    for (var key in $scope.data.eliminar){
	    	p_Dato.push({ NumFila: numFila, Campo: key, Valor: $scope.data.modificar[key] });
	    };
	    
	    /**para Derivados**/
	    if ($scope.data.CampoPrimario.IdMantenedor == 'DERIVA'){
	    	p_Dato = [];
		    for (var key in $scope.data.seleccion){
		    	if (key == 'ModalidadEjercicio' || key == 'TipoLiquidacion' || key == 'ParametroEjercicio' || key == 'ParametroEjercicioValor'){
		    		p_Dato.push({ NumFila: numFila++, Campo: key, Valor: $scope.data.seleccion[key] });
		    	}
		    };
	    };	   
	    parametrosSP.p_IdEmpresa 		= $scope.SesionIdEmpresa;
	  	parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario;
	    parametrosSP.p_Mantenedor = ($scope.data.currentMenu).codigo;
	    parametrosSP.p_Dato = p_Dato;
	    
	    resourceAccionRegistro.eliminar(parametrosSP).$promise.then(function(result){
	    	var datos = {};
	    	bcsComunes.msg("info","Mantenedor General","Registro eliminado correctamente");
	    	datos.REF_ID  = 0;
	    	
	    	refrescarGrilla($scope.data.currentMenu);
	    	
	    	$scope.data.atributosMantenedor = [];
	    	$scope.data.mostrar = false;
	    },function(result){
	    	console.log("error");
	    	console.log(result);
	    });
	    $scope.data.seleccion = undefined;
	};
	/*****************************************************************
	 * END ACCION FUNCION ELIMINAR                                   *
     *****************************************************************/
	
	
	$scope.fnBotonEspecial = function (e) {
		window.alert('especial');
	};
	
	
	/*****************************************************************
	 * CARGA DATOS EN LA GRILLA Y BOTONES DE ACCION                  *
	 * Muestro en la grilla las columnas y los datos correspondiente,*
	 * estos se obtienen del servicio                                *
	 * Plataforma::ProcesarAtributoMantenedorDatoAsociado            *
	 * basado en la tabla AtributoMantenedor                         *
     *****************************************************************/
	$scope.ListaMantenedoresClic = function(dataItem){
		$scope.data.currentMenu = dataItem;
		if(!dataItem.codigo)
			return false;//evitar la llamada con la seleccion del menu principal
			
		refrescarGrilla(dataItem);
		
		//Lista Acciones(Botones) Mantenedor
		var listaAccionMantenedorDataSource = new kendo.data.DataSource({
			type:"odata",
			serverFiltering: true,
			serverSorting: true,  //ordena al lado del servidor
			sort: { field: "Orden", dir: "asc" }, //campo y criterio de ordenamiento
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1441",
					dataType: "json"
				},
			},
		});
		
		listaAccionMantenedorDataSource.query({
			filter: [ 
			          { field: "Visible", operator: "eq", value: "S" },
			          { field: "IdMantenedor", operator: "eq", value: dataItem.codigo },
			]
		}).then(function(){
			//Recorrer Accion (Botones)
			var viewAcc = listaAccionMantenedorDataSource.view();
			var botonesAcc = [];
			for (var i = 0; i < viewAcc.length;i++){
				var btn = {};
				btn.NombreDiccionario = viewAcc[i].NombreDiccionario;
				btn.habilitado = true;
				btn.tipoclass = '';
				if (viewAcc[i].Tipo == 'AGREGAR') { 
					btn.fnclic = $scope.data.fnBotonAgregar; btn.tipoclass = "principal"; btn.id="MantenedorGeneral_agregar";
					if (viewAcc[i].IdMantenedor == 'TIPINS') { btn.habilitado = false;}
				}
				if (viewAcc[i].Tipo == 'MODIFICAR') { btn.fnclic = $scope.data.fnBotonModificar; btn.tipoclass = "principal"; btn.id="MantenedorGeneral_modificar";}
				if (viewAcc[i].Tipo == 'ELIMINAR') { btn.fnclic = $scope.data.fnBotonEliminar; btn.tipoclass = "principal"; btn.id="MantenedorGeneral_eliminar"; if (viewAcc[i].IdMantenedor == 'BOLSA') { btn.habilitado = false;}}
				if (viewAcc[i].Tipo == 'ESPECIAL') { btn.fnclic = $scope.fnBotonEspecial; btn.tipoclass = "principal"; btn.id="MantenedorGeneral_especial";}
				botonesAcc.push(btn);
			}
			botonesAcc.push({NombreDiccionario: "UI_EXPORTAR", habilitado:true, fnclic: $scope.exportarExcel, tipoclass : "principal"});//btn exportar excel
			$scope.data.accionesMantenedor = botonesAcc;
		});
	};
	/****************************************************************
	* END CARGA DATOS EN LA GRILLA Y BOTONES DE ACCION              *	
    *****************************************************************/
	
	
	/*****************************************************************
	 * LISTA EL MENU - SUB MENU                                      *  
	 * Crea el arbol del menu dinamico, dependiendo de los registros *
	 * que se encuentran ingresados en la tabla PLA::MAN.Mantenedor  *
     *****************************************************************/
	var listaMantenedorDataSource = new kendo.data.DataSource({
		type:"odata",
        serverFiltering: true,
	    serverSorting: true,  //ordena al lado del servidor
	    sort: [ 
	            { field: "NombreGrupoMantenedor", dir: "asc" }, //campo y criterio de ordenamiento
	            { field: "NombreMantenedor", dir: "asc" }
	    ],
	    transport: {
	    	read: {
	    		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1439",
                dataType: "json"
	    	},
	    },
	    filter: [ 
	              { field: "IdEstadoGrupoMantenedor", operator: "eq", value: "ACT" },
	              { field: "IdEstadoMantenedor", operator: "eq", value: "ACT" },
	    ]
	});
	
	listaMantenedorDataSource.read().then(function(){
		var view = listaMantenedorDataSource.view();
		var objDS = {};
	    objDS.data= new Array();	  
	    var nombreGrupo = "";
	    
	    for (var i=0; i<view.length; i++){
	    	if (nombreGrupo != view[i].NombreGrupoMantenedor){
	    		var menuN = {};  
				menuN.items = new Array();
				menuN.text=view[i].NombreGrupoMantenedor;  
				   
				for (var j=0; j<view.length; j++){
					if( view[i].NombreGrupoMantenedor == view[j].NombreGrupoMantenedor){
						var subMenuN = {};
						subMenuN.text=view[j].NombreMantenedor;
						subMenuN.codigo=view[j].CodigoMantenedor;
						menuN.items.push(subMenuN);
					}
				}
				objDS.data.push(menuN);
	    	}
	    	nombreGrupo = view[i].NombreGrupoMantenedor;
	    }
	    $scope.uiTVListaMantenedoresDatos.setDataSource(new kendo.data.HierarchicalDataSource(objDS));
	});
	/****************************************************************
	* END LISTA EL MENU - SUB MENU                                  *  
    *****************************************************************/
	$scope.data.optionsGrillaAtributos = {
			dataSource : new bcs.DataSource({
				definition : {},
				type : "array"
			}),
			columns : [],
			gridCreated: function(grid) {
				$scope.apiMantenedorCodigoGeneral = grid;
			},
			sortable: false,
			reorderable: false,
			editable: false,
			allowCopy: true,
			filterable: true,
			groupable: false,
			pageable: false,
			selectable: "row",
			resizable: true,
	  		autoBind: true,
	  		scrollable: true,
	 		height:300,
			//userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			}
	};
	
	function refrescarGrilla(dataItem){
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		//Lista Atributos(Columnas Grilla) Mantenedor
		var listaAtributoMantenedorDataSource = new kendo.data.DataSource({
			type:"odata",
			serverFiltering: true,
			serverSorting: true,  //ordena al lado del servidor
			sort: { field: "IdMantenedor", dir: "asc" }, //campo y criterio de ordenamiento
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1440",
					dataType: "json"
				},
			},
			error: function(e){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		});
		
		//Imprime variable con columna
		listaAtributoMantenedorDataSource.query({
			filter: [ 
			          //{ field: "VisibleGrilla", operator: "eq", value: "S" },
			          { field: "IdMantenedor", operator: "eq", value: dataItem.codigo },
			]
		}).then(function(){
			//Recorrer Atributos (Columnas)
			var viewAtri = listaAtributoMantenedorDataSource.view();
			$scope.atributos = viewAtri;
			var columnsAtri = [];
			$scope.data.atributosMantenedor = []; 
			$scope.data.mostrar = false;			
			$scope.newSchema = {};
			//Generar las columnas
			for (var i = 0; i < viewAtri.length; i++){
				if (viewAtri[i].TipoCampo == "PK"){
					$scope.data.CampoPrimario = viewAtri[i];
				}
				columnsAtri.push({field: viewAtri[i].NombreCampo,width: viewAtri[i].AnchoColumna + "px",title: $translate.instant(viewAtri[i].NombreDiccionario),hidden: (viewAtri[i].VisibleGrilla === 'N' ? true : false) })
				$scope.newSchema[viewAtri[i].NombreCampo] = {type: (viewAtri[i].TipoDatoCampo==="BIGINT")? "number" : "string"};
			} 
			
			//Pintar la grilla
	        var consultaDatosMantenedor = function(v_Dato){
	        	var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=32";
	        	var r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
	        	var post = r.urlConsulta(v_Dato);
	        	
	        	return post.$promise;
	        };
	        
	        var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
			parametrosSP.p_Mantenedor = dataItem.codigo;
			
			consultaDatosMantenedor(parametrosSP).then(function(result){
				var r = [];
				//Recorrer resultado (datos)
				var data = {};
				var cont = 1;
				
				$.each(result.V_DATO,function(key,value){
					if (data[value.NumFila] === undefined){
						data[value.NumFila] = {};
					}
					data[value.NumFila][value.Campo] = value.Valor;
				});
				
				$.each(data,function(key,value){
					r.push(value);
				});
				$scope.data.mostrarGrid = true;
				var options = $scope.apiMantenedorCodigoGeneral.dataSource.getOptions();
				options.definition.schema.model.fields = $scope.newSchema;
				
				$scope.apiMantenedorCodigoGeneral.dataSource.setOptions(options);
				$scope.apiMantenedorCodigoGeneral.setOptions({columns: columnsAtri});

				$scope.apiMantenedorCodigoGeneral.dataSource.data(r);
				
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				
				$timeout(function() {//fallback para ajustar tamaño de la grilla.
					$scope.apiMantenedorCodigoGeneral.refresh();
				},1);
				
				$scope.atributosValor = r;
			});
		});
	}
	
	$scope.generarInformeParametrosSistema = function(){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'PLA',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'GenerarInformeParametrosSistema',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'PLA'
				}]
		}
		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then(
		function(result) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
			bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
				conflictos : util.parseServerResponse(result)}, $scope.IdVentana);

		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});
	}
	
	$scope.bloqueoDesbloqueoMasivo= function(){
		
	};
	
});

// ------------------------------------------
// CONTROL MODIFICAR
//------------------------------------------
//migrado a modal nuevo

//------------------------------------------
// CONTROL AGREGAR
//------------------------------------------
