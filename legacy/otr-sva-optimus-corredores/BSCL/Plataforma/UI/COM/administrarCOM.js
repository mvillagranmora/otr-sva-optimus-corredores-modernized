/*************************************************************
 * Purpose	: Contenedor principal de Modelo Comercial.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('AdministrarCOMCtrl',function($scope,bcsComunes,$compile){
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		$scope.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		$scope.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		$scope.domVentana 	= $("[ventanaid='"+$scope.idVentana+"']");
			
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/

		$scope.fnTabPlantillaComercial = function () {
            if ($scope.TabPlantillaComercialCargado !== true) {
            	$scope.TabPlantillaComercialCargado = true;
            	$scope.domVentana.find('#PanelPlantillaComercial').append($compile('<div ng-include="\'../../Plataforma/UI/COM/PlantillaComercial/PlantillaComercial.html\'" ></div>')($scope));
            }
     	};
     	
     	$scope.fnTabPlantillaProductos = function () {
            if ($scope.TabPlantillaProductosCargado !== true) {
            	$scope.TabPlantillaProductosCargado = true;
            	$scope.domVentana.find('#PanelPlantillaProductos').append($compile('<div ng-include="\'../../Plataforma/UI/COM/PlantillaProductos/PlantillaProductos.html\'" ></div>')($scope));
            }
     	};

     	$scope.fnTabPlantillaCobros = function () {
            if ($scope.TabPlantillaCobrosCargado !== true) {
            	$scope.TabPlantillaCobrosCargado = true;
            	$scope.domVentana.find('#PanelPlantillaCobros').append($compile('<div ng-include="\'../../Plataforma/UI/COM/PlantillaCobros/PlantillaCobros.html\'" ></div>')($scope));
            }
     	};
     	
     	$scope.fnTabPlantillaCobrosPeru = function () {
            if ($scope.TabPlantillaCobrosPeruCargado !== true) {
            	$scope.TabPlantillaCobrosPeruCargado = true;
            	$scope.domVentana.find('#PanelPlantillaCobrosPeru').append($compile('<div ng-include="\'../../Plataforma/UI/COM/PlantillaCobrosPeru/PlantillaCobrosPeru.html\'" ></div>')($scope));
            }
     	};
     	
     	$scope.fnTabGrupoActivos = function () {
            if ($scope.TabGrupoActivosCargado !== true) {
            	$scope.TabGrupoActivosCargado = true;
            	$scope.domVentana.find('#PanelGrupoActivos').append($compile('<div ng-include="\'../../Plataforma/UI/COM/GrupoActivos/GrupoActivos.html\'" ></div>')($scope));
            }
     	};	
     	
     	$scope.fnTabPlantillaLimites = function () {
            if ($scope.TabPlantillaLimitesCargado !== true) {
            	$scope.TabPlantillaLimitesCargado = true;
            	$scope.domVentana.find('#PanelPlantillaLimites').append($compile('<div ng-include="\'../../Plataforma/UI/COM/PlantillaLimites/PlantillaLimites.html\'" ></div>')($scope));
            }
     	};
	});
}());