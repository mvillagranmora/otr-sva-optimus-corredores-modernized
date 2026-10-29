(function() {
	angular.module('ModuloPlataforma').controller('AdministrarModelosTipoClienteCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var aModTC = this;
		
		$scope.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		$scope.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		$scope.domVentana 	= $("[ventanaid='"+$scope.idVentana+"']");
		
		
		//aModTC.IdTipoClienteDef = 'RETAIL';
		aModTC.IdTipoClienteDef = $scope.$root.SesionValoresDefectoSistema.IdTipoCliente;
		aModTC.filtrosTipoCliente = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
			read:true
		};

		$scope.fnTabInformacionCliente = function () {
			aModTC.visible = 'INFCLI';
            if ($scope.TabInformacionClienteCargado !== true) {
            	$scope.TabInformacionClienteCargado = true;
            	$scope.domVentana.find('#PanelInformacionCliente').append($compile('<div ng-include="\'../../Plataforma/UI/IND/PanelInformacionCliente.html\'" ></div>')($scope));
            }
     	};
		
		$scope.fnTabIndicadoresCliente = function () {
			aModTC.visible = 'INDCLI';
            if ($scope.TabIndicadoresClienteCargado !== true) {
            	$scope.TabIndicadoresClienteCargado = true;
            	$scope.domVentana.find('#PanelIndicadoresCliente').append($compile('<div ng-include="\'../../Plataforma/UI/IND/PanelIndicadoresCliente.html\'" ></div>')($scope));
            }
     	};
     	
     	$scope.fnTabIndicadoresPortafolio = function () {
			aModTC.visible = 'INDPOR';
            if ($scope.TabIndicadoresPortafolioCargado !== true) {
            	$scope.TabIndicadoresPortafolioCargado = true;
            	$scope.domVentana.find('#PanelIndicadoresPortafolio').append($compile('<div ng-include="\'../../Plataforma/UI/IND/PanelIndicadoresPortafolio.html\'" ></div>')($scope));
            }
     	};
     	
     	$scope.fnTabRequisitosCliente = function () {
     		aModTC.visible = 'REQCLI';
            if ($scope.TabRequisitosClienteCargado !== true) {
            	$scope.TabRequisitosClienteCargado = true;
            	$scope.domVentana.find('#PanelRequisitosCliente').append($compile('<div ng-include="\'../../Plataforma/UI/IND/PanelRequisitosCliente.html\'" ></div>')($scope));
            }
     	};
     	
     	$scope.fnTabRequisitosPortafolio = function () {
     		aModTC.visible = 'REQPOR';
            if ($scope.TabRequisitosPortafolioCargado !== true) {
            	$scope.TabRequisitosPortafolioCargado = true;
            	$scope.domVentana.find('#PanelRequisitosPortafolio').append($compile('<div ng-include="\'../../Plataforma/UI/IND/PanelRequisitosPortafolio.html\'" ></div>')($scope));
            }
     	};
     	
     	$scope.fnTabPreferencias = function () {
     		aModTC.visible = 'PREF';
            if ($scope.TabPreferenciasCargado !== true) {
            	$scope.TabPreferenciasCargado = true;
            	$scope.domVentana.find('#PanelPreferencias').append($compile('<div ng-include="\'../../Plataforma/UI/IND/PanelPreferencias.html\'" ></div>')($scope));
            }
     	};
		
     	$scope.cargarDatosTabs = function(){
     		aModTC.IdSeleccionado = aModTC.IdTipoCliente;
			aModTC.TipoClienteObj.PorDefectoCheck = aModTC.TipoClienteObj.PorDefecto === "S" ? true:false;
     	};

     	$scope.crearModelo = function(){
			var id1 = bcsComunes.ventanaAbrir("ModalTipoCliente",{},false,false,$scope.idVentana);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
			promesa.then(function(data){ 
				//Refrescar grilla para visualizar cambios.
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				aModTC.filtrosTipoCliente.read = !aModTC.filtrosTipoCliente.read;
			});
     	};

     	$scope.modificarModelo = function(){
			var id1 = bcsComunes.ventanaAbrir("ModalTipoCliente",{modelo: aModTC.TipoClienteObj},false,false,$scope.idVentana);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
			promesa.then(function(data){ 
				//Refrescar grilla para visualizar cambios.
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				aModTC.filtrosTipoCliente.read = !aModTC.filtrosTipoCliente.read;
			});
     	};
	});
}());