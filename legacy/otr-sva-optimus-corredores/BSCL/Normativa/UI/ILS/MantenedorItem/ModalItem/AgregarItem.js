angular.module('ModuloNormativa').controller('AgregarItemCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosNormativa){
	
	//Parametros
	$scope.winID = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var habilitaBotonGrabar = false;
	$scope.data = {};	
	$scope.Item = $.extend({}, parametros.Item);
	$scope.habilitadoAceptar = false;
	
	// Configuracion de Combobox Tipo Item
	$scope.oConfiguracionItemTipo={
			tipo : "combobox-tipo-item",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "NombreEntidad", "operator": "eq", "value": 'TES.Caja'},
				IdEstado : {"field": "IdEstado", "operator": "eq", "value":  (parametros.Item ? '':'CER') } 
			},
			idActual : false,
			seleccionado : false,
			idDefecto : 0			
	};
	
	$scope.oConfiguracionItemTipo.idDefecto = 'CTB';
	
	if (parametros.Item) //MODIFICAR
	{	
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_MODIFICAR_ITEM'));
		$scope.data.IdItem = parametros.Item.IdItem;
		$scope.data.NombreItem = parametros.Item.NombreItem;
		$scope.data.NombreCortoItem = parametros.Item.NombreCortoItem;
		$scope.oConfiguracionItemTipo.idDefecto = parametros.Item.IdTipoItem;

	}
	else //AGREGAR
	{	
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_AGREGAR_ITEM'));
	}
	
	// -----------------------------------------------
	// ----------------- CREAR ITEM ------------------
	// -----------------------------------------------
	var successCrearItem = function (respuesta){
		//AGREGAR
		if($scope.Item && respuesta.P_OUT_IDITEM !== undefined && respuesta.P_OUT_IDITEM > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_ITEM_INGRESAR_EXITO'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			bcsComunes.ventanaCerrar($scope.winID);
		}
		//MODIFICAR
		else if ($scope.Item && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_ITEM_MODIFICADO_EXITO'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			bcsComunes.ventanaCerrar($scope.winID);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		
			bcsComunes.ventanaMsg({
				mensaje: $translate.instant('UI_OPERACION_ERROR'),
				conflictos: util.parseServerResponse(respuesta),
				errores : [ {Codigo : 'COD', Descripcion : $translate.instant('UI_ITEM_INGRESAR_ERROR')} ]
			},$scope.winID);
			
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	var errorCrearItem = function(respuesta)
	{	
		console.log("Entro por el error del item");
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			

		bcsComunes.ventanaMsg({
			mensaje: $translate.instant('UI_ITEM_INGRESAR_ERROR'),
			conflictos: util.parseServerResponse(respuesta),
			errores : [ {Codigo : 'COD',Descripcion : $translate.instant('UI_OPERACION_CON_ERRORES')} ]
		},$scope.winID);
		
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	$scope.AgregarItem = function(){
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		if(parametros.Item){
			
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdItem : $scope.data.IdItem,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdTipoItem : $scope.oConfiguracionItemTipo.idActual,	
					p_IdModulo : 'ILS',
					p_Estado : 'VIG',	
					p_Mantenedor : 'ITEM',
					p_Nombre : $scope.data.NombreItem,
					p_NombreCorto : $scope.data.NombreCortoItem,					
					p_TipoOperacion : 'M',					
					p_UsaReglasNegocio : 'S',
					p_FechaModificacion : util.dateSPdate(new Date()),
					p_Dato :
						[{NumFila: 1, Campo: 'IdItem', Valor: parametros.Item.IdItem, ValorAnterior: parametros.Item.IdItem},
						 {NumFila: 2, Campo: 'NombreItem', Valor: $scope.data.NombreItem, ValorAnterior: parametros.Item.NombreItem},
						 {NumFila: 3, Campo: 'NombreCortoItem', Valor: $scope.data.NombreCortoItem, ValorAnterior: parametros.Item.NombreCortoItem},
						 {NumFila: 4, Campo: 'IdTipoItem', Valor: $scope.TipoItem.IdItem, ValorAnterior: parametros.Item.IdTipoItem}]
					
			};
			bcsComunes.log().info(JSON.stringify(parametrosSP));
			//FIXME bcsServiciosNormativa 
			bcsServiciosNormativa.MantenedorItem(parametrosSP, successCrearItem, errorCrearItem);
		}
		else {
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdItem : 0,
					p_IdModulo : 'ILS',
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdTipoItem : $scope.TipoItem.seleccionado.IdTipoItem,
					p_Estado : 'VIG',
					p_Nombre : $scope.data.NombreItem,
					p_NombreCorto : $scope.data.NombreCortoItem,
					p_TipoOperacion : 'I',
					p_UsaReglasNegocio : 'S'					
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				//FIXME bcsServiciosNormativa
				bcsServiciosNormativa.MantenedorItem(parametrosSP, successCrearItem, errorCrearItem);
		}
		
	};
	
	// -----------------------------------------------
	// ----------------- LIMPIAR ---------------------
	// -----------------------------------------------	
	
	$scope.limpiarFormulario = function(){
		$scope.data.IdItem = '';
		$scope.data.NombreItem = '';
		$scope.data.NombreCortoItem = '';
		$scope.TipoItem.IdTipoItem = 0;
	};
	
	// -----------------------------------------------
	// ----------------- CANCELAR ---------------------
	// -----------------------------------------------	
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.winID);
	};
	
	//-----------------------------------------
	//---------------watch--------------------	
	//-----------------------------------------
	function verificaCampos(){
		let cont =0;
		
		if($scope.data.NombreItem == "" || $scope.data.NombreItem ==null || $scope.data.NombreItem == undefined){
			cont= cont+1;
		}
		if($scope.TipoItem.seleccionado == "" || $scope.TipoItem.seleccionado ==null || $scope.TipoItem.seleccionado == undefined){
			cont= cont+1;
		}
		if($scope.data.NombreCortoItem == "" || $scope.data.NombreCortoItem ==null || $scope.data.NombreCortoItem == undefined){
			cont= cont+1;
		}
		if(cont>0){
			return false;
		}else{
			return true;
		}
	}
	
	$scope.$watchGroup(['data.NombreCortoItem','TipoItem.seleccionado','data.NombreItem'],function(newval,oldval){ 
		if (angular.equals(newval,oldval)) return;
		if (newval!==false) {
	
			$scope.habilitaBotonGrabar = verificaCampos();
		}
	});
	
	
	
});