angular.module('ModuloSocioNegocio').controller('ModalParametrosContratosCtrl',
		function($scope,$translate,$modal,$log,$sce,$http,
				bcsComunes, bcsServicioPlataforma, bcsConfiguracionSociodeNegocio)
{

	$scope.data	= {};	
	$scope.parametros = [];
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parame = bcsComunes.ventanaParams (idVentana); //datos del contrato seleccionado
	$scope.parametros  = parame.parametros;
	
	console.log($scope.parametros);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.btnActivos  = true;   //Activa botones
	$scope.data.tipoPersona = parame.IdTipoPersona;
	$scope.data.resultadoValidacion = "OK";
		
	$scope.imprimirArchivo = function(){
		//en $scope.parametros[].Valor se encuentra el valor de cada parametro.
		var data = {};
		var rut = "";
		var tipoPer = "";
		
		
				
		
		$.each($scope.parametros, function(key,val){
			data[val.Nombre] = val.Valor;
			if (val.IdentificaPersona == "R"){
				rut = val.Valor;
			}
			console.log(data[val.Nombre]);
		});
		
		//valida tipo de persona correcto *******
		if ($scope.data.tipoPersona != "undefined"){
			if ($scope.data.tipoPersona == "N" || $scope.data.tipoPersona == "PERSONA NATURAL"){
				tipoPer = "N";
			}else{
				tipoPer = "J";
			}
		}else{
			tipoPer = "";
		}
		var filtro1 = {
			filters : [],
			logic : "and"
		};
		filtro1.filters.push({ field: "TipoDocumentoIdentidad", operator: "eq", value: "R" });
		filtro1.filters.push({ field: "DocumentoIdentidad", operator: "eq", value: rut });
		filtro1.filters.push({ field: "Tipo", operator: "eq", value: tipoPer });
		
		console.log("Rut:" + rut);
		console.log("Tipo de persona:" + tipoPer);
		
		var personaDatosGeneralesDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1309",
						dataType: "json"
					}
				},
				filter: filtro1,
				schema: bcsConfiguracionSociodeNegocio.getSchemaPersona()
			}
		});
		
		personaDatosGeneralesDatasource.query().then(function(result){
			console.log(personaDatosGeneralesDatasource.data().length);
			if (personaDatosGeneralesDatasource.data().length == 0){
				bcsComunes.msg("error","Error", "El Rut ingresado no corresponde al Tipo de persona del contrato.");
				$scope.data.resultadoValidacion = "El Rut ingresado no corresponde al Tipo de persona del contrato.";
			}else{
				console.log(data); 
				bcsComunes.ventanaData(idVentana, data);
				bcsComunes.ventanaCerrar(idVentana);
			}
			});
					
		//valida tipo de persona correcto *******
		
		

	};
});	
