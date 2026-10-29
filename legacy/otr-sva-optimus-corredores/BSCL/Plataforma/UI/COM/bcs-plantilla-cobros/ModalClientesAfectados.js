(function() {
	angular.module('ModuloPlataforma').controller('ModalClientesAfectadosCtrl',
		function ($scope,bcsComunes,$translate,bcsConfiguracionMatrizPerfilComercial) {
			
			var modCliAf = this;
			modCliAf.idVentana 				= bcsComunes.ventanaWinId($scope);
			modCliAf.parametros 			= bcsComunes.ventanaParams(modCliAf.idVentana);
			modCliAf.domVentana 			= $("[ventanaid='"+modCliAf.idVentana+"']");
			modCliAf.gridClientesAfectados 	= {};
			modCliAf.win					= {};
			modCliAf.respuesta 				= {};
			
			modCliAf.win.cancelar = function () {
				modCliAf.respuesta.aceptar = false;
				bcsComunes.ventanaData(modCliAf.idVentana, modCliAf.respuesta);
				bcsComunes.ventanaCerrar(modCliAf.idVentana);
			};

			modCliAf.win.aceptar = function () {
				modCliAf.respuesta.aceptar = true;
				bcsComunes.ventanaData(modCliAf.idVentana, modCliAf.respuesta);
				bcsComunes.ventanaCerrar(modCliAf.idVentana);
			};
			
			modCliAf.gridClientesAfectados = {
					dataSource		: {
						type 		: "odata",
						definition 	: {
							serverFiltering : true, 
							serverPaging 	: true,
							serverSorting 	: true, 
							sortable 		: true,
							pageSize 		: 50,
							schema 			: {
								model	: {
									fields: {
										IdCliente 			: { type: "number"},
										RazonSocial 		: { type: "string"},
										NombreCompleto 		: { type: "string"},
										Rut 				: { type: "string"},
										IdPortafolio 		: { type: "number"},
										CodigoPortafolio	: { type: "string"}
									}
								}
							}
						}
					},
					columns :[
       					{field: "NombreCompleto",width: "50%",title: $translate.instant("UI_NOMBRE_COMPLETO"),attributes : {style : "text-align:left;"}},
						{field: "DocumentoIdentidad",width: "30%",title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),attributes : {style : "text-align:left;"}},
						{field: "CodigoPortafolio",width: "20%",title: $translate.instant("UI_CODIGO_PORTAFOLIO"),format:"{0:n0}",attributes : {style : "text-align:left;"}}
					],		
					sortable			: false,
					groupable			: false,
					pageable			: false,
					autoBind			: true, 
					height				: 250,
				    selectable 			: "row"
				 };
			
			$scope.$watch('modCliAf.apiGridClientesAfectados',function(newVal){
				if(newVal) modCliAf.apiGridClientesAfectados.dataSource.data(modCliAf.parametros.dato1);
			});			
			
	});
}());