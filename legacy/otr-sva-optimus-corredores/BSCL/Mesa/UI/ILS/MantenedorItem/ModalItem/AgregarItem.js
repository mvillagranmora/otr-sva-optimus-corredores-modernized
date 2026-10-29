angular.module('ModuloMesa').controller('AgregarItemCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosTesoreria, bcsServiciosMesa){
	
	//Parametros
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	$scope.data = {};	
	$scope.Item = $.extend({}, parametros.Item);
	
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
	
	
	if (parametros.Item) //MODIFICAR
	{	
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_ILS_MODIFICAR_ITEM'));
		$scope.data.IdItem = parametros.Item.IdItem;
		$scope.data.NombreItem = parametros.Item.NombreItem;
		$scope.data.NombreCortoItem = parametros.Item.NombreCortoItem;
		$scope.oConfiguracionItemTipo.idDefecto = parametros.Item.IdTipoItem;

	}
	else //AGREGAR
	{	
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_ILS_AGREGAR_ITEM'));
	}
	
	// -----------------------------------------------
	// ----------------- CREAR ITEM ------------------
	// -----------------------------------------------
	var successCrearItem = function (respuesta){
		//AGREGAR
		if($scope.Item && respuesta.P_OUT_IDITEM !== undefined && respuesta.P_OUT_IDITEM > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO') +' '+  respuesta.P_OUT_IDCAJA);
			
			$scope.limpiarFormulario();
			bcsComunes.ventanaCerrar(idVentana);
		}
		//MODIFICAR
		else if ($scope.Item && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			bcsComunes.ventanaCerrar(idVentana);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	var errorCrearItem = function(respuesta)
	{	
		console.log("Entro por el errror del item");
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	$scope.AgregarItem = function(){
		bcsComunes.ventanaOverlay(idVentana, true);
		
		if(parametros.Item){
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdItem : $scope.data.IdItem,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdTipoItem : $scope.oConfiguracionItemTipo.idActual,	
					p_IdModulo : 'ILS',
					p_Estado : 'V',	
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
			//FIXME bcsServiciosMesa 
			bcsServiciosMesa.MantenedorItem(parametrosSP, successCrearItem, errorCrearItem);
		}
		else {
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdItem : 0,
					p_IdModulo : 'ILS',
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdTipoItem : $scope.TipoItem.seleccionado.IdTipoItem,
					p_Estado : 'V',
					p_Nombre : $scope.data.NombreItem,
					p_NombreCorto : $scope.data.NombreCortoItem,
					p_TipoOperacion : 'I',
					p_UsaReglasNegocio : 'S'					
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				//FIXME bcsServiciosMesa
				bcsServiciosMesa.MantenedorItem(parametrosSP, successCrearItem, errorCrearItem);
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
		bcsComunes.ventanaCerrar(idVentana);
	};
	
});