angular.module('ModuloPlataforma', [ "kendo.directives" ]).controller('ModificarPerfilCtrl',
		function($scope,$translate,$modal,$log,$resource,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};	
	var overlay = bcsComunes.overlay;
	var accesoTree = [];
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	//Obtener datos desde la pantalla consulta usuarios
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.IdPerfil = parametros.IdPerfil;
	$scope.data.nombrePerfil = parametros.Nombre;
	$scope.data.idEstado = parametros.IdEstado;
	bcsComunes.ventanaAutoHeight(idVentana)
	

	$scope.filtrosFormularioIniciales = {
		consultaCanalesIngreso: {
			logic: "or",
			canalconfigurado: { field: 'IdTipoCanalIngreso', operator: 'eq', value: 'AGE' },
			canalconfigurado2: { field: 'IdTipoCanalIngreso', operator: 'eq', value: 'OPT' }
		}
	};
	$scope.filtrosFormulario = Object.assign({}, $scope.filtrosFormularioIniciales);

	var filtros = [
         { field: "IdPerfil", operator: "eq", value: $scope.data.IdPerfil},
         { field: "IdEmpresaPerfil", operator: "eq", value: $scope.data.IdEmpresa},
         { field: "EsVisibleMenu", operator: "eq", value: 'S'},
         { field: "CanalAcceso", operator: "eq", value: 'OPT'},
         ];

	var AccesoDataSource ; 

	$scope.$watch('data.CanalSeleccionado', CambiosCanalSelecionado);

	function GetData(){
		return new kendo.data.DataSource({
			type: "odata",
			serverFiltering: true,
			serverSorting : true,
			transport: {
				read: {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1526",
					dataType: "json"
			    }
			},
			schema: {
	        	model:
	    		{
	        		fields : {
	        			IdPerfil: {type: "number"},
						IdAcceso: {type: "number"},
						NombreAcceso: {type:"string"},
						TipoAcceso: {type:"string"},
						Padre: {type: "number"},
						Orden: {type: "number"},
						IdServicio: {type: "number"},
						IdElementoHtml: {type: "string"},
						EsVisibleMenu: {type: "string"},
						Existe: {type : "string"}
					}
	    		}
	    	},
	    	sort : [{ field: "Padre", dir: "asc"},{field: "Orden", dir:"asc" }],
	    	filter: filtros
		});
	}

    function checkBranch(parent, check){ 
    	var childrens = parent.items;
		for(var i= 0; i < childrens.length; i++){
			var element = $scope.tree.findByUid(childrens[i].uid)[0];
			$scope.tree.dataItem(element).set("checked", check);

			if (childrens[i].hasChildren && childrens[i].items.length>0) {
                checkBranch(childrens[i], check);
            }
		}
    }

    function onCheck(e) {
		var dataItem = $scope.tree.dataItem(e.node); //elemento checkeado
		var check = dataItem.checked;
		
		if(dataItem.checked && dataItem.items.length>0){
			//si se ha checkeado una rama y tiene hojas, checkear las hojas
			checkBranch(dataItem, check);
		}else{
			if(dataItem.items.length>0){
				//si se ha des-checkeado una rama y tiene hojas, des-checkear las hojas
				checkBranch(dataItem, check);
			}
		}
		//checker sus ramas padres
		var parent = dataItem.parent();
		while(parent && dataItem.checked){
			if(parent.uid){
				var element = $scope.tree.findByUid(parent.uid)[0];
				$scope.tree.dataItem(element).set("checked", check);
			}
			parent = parent.parent();
		}
    }
    
	$scope.options = {
      checkboxes: true,
      dataSource: accesoTree,
      check: onCheck
    };

	let Constructor_TreeList = function(){
		//Funcion anonima que construye el arbol de accesos, detecta y checkea que accesos tiene actualmente el perfil
		accesoTree = [];
		bcsComunes.ventanaOverlay(idVentana,true);
		//obtener todos los accesos del perfil
		AccesoDataSource = GetData();
		AccesoDataSource.query().then(function() {	
			$.each(AccesoDataSource.view(),function(key,value){
				if(value.TipoAcceso==="MENU1"){//es padre, insertar en el root
					var check = (value.Existe==='S')?true: false;
					accesoTree.push({idPerfil: value.IdPerfil, idAcceso: value.IdAcceso, text: $translate.instant(value.NombreAcceso), padre:value.Padre, expanded: false, items:[], checked: check});
				}else{
					//una vez insertados todos los nodos root en el arbol, se buscan los hijos y se insertan recursivamente en su nodo padre.
					appendLeaf(accesoTree, value);
				}	
			});
		}).then(function(){
			//Constructor del arbol
			$scope.tree.setDataSource(accesoTree);
			isTreeComplete();
		});
		 function appendLeaf(tree, leaf){
			if(tree.length>0){
				for(var actual in tree){
					if(tree[actual].idAcceso===leaf.Padre){
						var check = (leaf.Existe==='S')?true: false;
						tree[actual].items.push({idPerfil: leaf.IdPerfil, idAcceso: leaf.IdAcceso, text: $translate.instant(leaf.NombreAcceso), padre: leaf.Padre,  expanded: false, items:[], checked: check});
					}else{
						appendLeaf(tree[actual].items,leaf);
					}
				}
			}
		} 
	};

	Constructor_TreeList();

	function isTreeComplete(){
		//Si el arbol de permisos esta construido elimina el overlay
		if($scope.tree==undefined){
			setTimeout(isTreeComplete, 1000);
		}else{
			bcsComunes.ventanaOverlay(idVentana,false);
		}
	}

	function CambiosCanalSelecionado(){
        if ($scope.data.IdCanalActual != ""){
            filtros.pop();
            filtros.push({ field: "CanalAcceso", operator: "eq", value: $scope.data.IdCanalActual || 'OPT'});
            Constructor_TreeList();
        }
    };

	//COMBOBOX Estados
	$scope.data.oConfiguracionEstadoPerfiles={
		tipo : "combobox-estado-perfil",
		configuracion : {},
		filtros : {
			read : false
		},
		idActual : false,
		seleccionado : false,
		idDefecto : 'S'
	};
	
	function getDataAcceso(tree, leaf, dataAcceso){
		if(tree.length == 0){
			return tree;
		}else{
			for(var i =0; i < tree.length; i++){
				var leaf = tree[i];
				dataAcceso.push({
					p_IdAcceso: tree[i].idAcceso, 
					p_Existe: (tree[i].checked)?'S':'N' 
					});
				
				if(tree[i].items.length>0){
					getDataAcceso(tree[i].items, undefined, dataAcceso);
				}
			}
		}
	}
	
	$scope.actualizarPerfil = function(){
		if($scope.data.nombrePerfil.trim().length>0){
				 if($scope.data.oConfiguracionEstadoPerfiles.seleccionado){
					 bcsComunes.ventanaOverlay(idVentana,true);
					 //llamar procedimiento almacenado de actualizacion.
					 var accesos = [];
					 getDataAcceso($scope.tree.dataSource.view(), undefined, accesos);
					 
					 var dataAcceso = {
							 p_IdEmpresa: $scope.SesionIdEmpresa,
							 p_IdUsuario:$scope.SesionIdUsuario,
							 p_IdModulo: 'PLA',
							 p_Nombre: $scope.data.nombrePerfil,
							 p_IdEstado: $scope.data.oConfiguracionEstadoPerfiles.seleccionado.IdEstado,
							 p_IdPerfil: $scope.data.IdPerfil,
							 p_Accesos: accesos
					 };
					 
					 var urlUpdatePerfilAcceso = "/BSCL/Plataforma/GWJAVA/PLA/call?t=148";
						var r = $resource("", {},
					    {
							'ActualizarPerfilAcceso': { method: "POST", url: urlUpdatePerfilAcceso}
						});
												
						var post = r.ActualizarPerfilAcceso(dataAcceso);
						
						post.$promise.then(function(result){
							var titulo = "";
							if(result.P_OUT_UPDATE!=null && result.P_OUT_UPDATE==='ok++'){
								titulo = $translate.instant("UI_PERFIL_ACTUALIZADO_OK");
							}else{
								titulo = $translate.instant("UI_ERROR_ACTUALIZACION_PERFILES");
							}
							
							var idMensajeModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},idVentana);
							
							bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
								bcsComunes.ventanaOverlay(idVentana,false);
							});
						});
					 
				 }else{
						bcsComunes.msg("error",'Error',$translate.instant("ESTADO_PERFIL_VACIO"));
				 }
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("NOMBRE_PERFIL_VACIO"));
		}
		
	};
	
	$scope.comprimirArbol = function(){
		$scope.tree.collapse(".k-item");
	}
	
	$scope.expandirArbol = function(){
		$scope.tree.expand(".k-item");
	}
	
	$scope.sinAutorizacion = function(){
		var items = $scope.tree.dataItems();
		for (var i=0; i < items.length ; i++){
			$scope.tree.dataItem($scope.tree.findByUid(items[i].uid)[0]).set("checked", false);
			var e = {node: $scope.tree.findByUid(items[i].uid)[0]};
			onCheck(e);
		}
	}
	
	$scope.autorizacionTotal = function(){
		var items = $scope.tree.dataItems();
		for (var i=0; i < items.length ; i++){
			$scope.tree.dataItem($scope.tree.findByUid(items[i].uid)[0]).set("checked", true);
			var e = {node: $scope.tree.findByUid(items[i].uid)[0]};
			onCheck(e);
		}
	}
	
	$scope.cancelar = function(){
		$scope.data.IdPerfil = parametros.IdPerfil;
		$scope.data.nombrePerfil = parametros.Nombre;
		$scope.data.oConfiguracionEstadoPerfiles.idActual = parametros.IdEstado;
		$scope.tree.dataSource.read();	
	};
	
});