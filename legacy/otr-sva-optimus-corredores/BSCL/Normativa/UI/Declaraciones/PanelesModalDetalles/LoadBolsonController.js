angular.module('ModuloNormativa').directive('fileReader', function() {
  return {
    scope: {
      fileReader:"="
    },
    link: function(scope, element) {
      $(element).on('change', function(changeEvent) {
        var files = changeEvent.target.files;
        if (files.length) {
          var r = new FileReader();
          r.onload = function(e) {
              var contents = e.target.result;
            
			scope.$apply(function () {
            	scope.fileReader = contents;
            });
          };
          
          r.readAsText(files[0],'iso-8859-1');
        }
      });
    }
  };
});


angular.module('ModuloNormativa').controller('UploadBolsonController',function ($scope,  $resource, bcsComunes) {
    $scope.data = {};
	$scope.params = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	$scope.data.ignorarPrimeraLinea = true;
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	var csvToArray = function (csvString,skipFirstLine){
		// The array we're going to build
		var csvArray   = [];
		// Break it into rows to start
		var csvRows    = csvString.split(/\n/);
		// Take off the first line to get the headers, then split that into an array
		var csvHeaders = [];
		
		if($scope.params.tipo === "CALPAG"){
			csvHeaders = ["KRutCtraparte1","KIdContrato","KFechaContrato","KIdOpeEstruc","KProducto","KPeriodoComercial","KS4_XR_NroFlu","S4_XR_TipoFlujo","S4_XR_FacTasa","S4_XR_SpreadTasa","S4_XR_TasaFija",
					"S4_XR_FecFixTasa","S4_XR_FecIni","S4_XR_FecTer","S4_XR_MontoNoc","S4_XR_MontoInt","S4_X_MonMonto"];
		}
		
		if($scope.params.tipo === "PAGPER"){
			csvHeaders = ["KRutCtraparte1","KIdContrato","KFechaContrato","KIdOpeEstruc","KProducto","KPeriodoComercial","KS5_CorrPago","KS5_IdPago","S5_FecPago","S5_MontoPago","S5_TipoPago"];
		}
		
		if($scope.params.tipo === "GAR"){
			csvHeaders = ["KRutCtraparte1","KIdContrato","KFechaContrato","KIdOpeEstruc","KProducto","KPeriodoComercial","S6_TipoGar","S6_MonVal","KS6_IdGar","S6_MontoUmb","S6_ValGtia"];
		}
		
		if($scope.params.tipo === "TIPACTGAR"){
			csvHeaders = ["KRutCtraparte1","KIdContrato","KFechaContrato","KIdOpeEstruc","KProducto","KPeriodoComercial","S7_TipoGar","KS7_IdGar","S7_TipoActGar","S7_PorcGar"];
		}
		
		// Loop through remaining rows
		for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
			
			var rowArray  = csvRows[rowIndex].split(';');		
			
			if ((rowIndex === 0 && skipFirstLine === true) || (rowArray.length < 4)) {
				console.log("IGNORADO : " +  JSON.stringify(rowArray));
				continue;
			}
			
		    // Create a new row object to store our data.
		    var rowObject = {};
		    // Then iterate through the remaining properties and use the headers as keys
		    for(var propIndex = 0; propIndex < csvHeaders.length; ++propIndex){
		      	//Grab the value from the row array we're looping through...
		    	var propValue =   rowArray[propIndex].replace(/^"|"$/g,'') || '';
		      //...also grab the relevant header (the RegExp in both of these removes quotes)
		      	var propLabel = csvHeaders[propIndex].replace(/^"|"$/g,'');
				
		      	rowObject[propLabel] = propValue.trim();
		    }
		    csvArray.push(rowObject);
		  }
  		return csvArray;
	};
		
	$scope.data.guardar =  function() {
		if($scope.params.tipo === "CALPAG"){
			cargarCalendarioPago();
		}
		
		if($scope.params.tipo === "PAGPER"){
			cargarPagoPeriodo();
		}
		
		if($scope.params.tipo === "GAR"){
			 cargarGarantia();
		}		
		
		if($scope.params.tipo === "TIPACTGAR"){
			 cargarTipoActivoGarantia();
		}
	};
	
	function cargarCalendarioPago(){
		var datosCalendarioPagoArchivo = csvToArray($scope.data.fileContent,true);

		var datosCalendarioPago = [];
		
		var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700208";
		var r = $resource("", {}, {'cargarBolson': { method: "POST", url: url}});
		
		datosCalendarioPagoArchivo.forEach(function(item, index){
			var calendarioPago = {
				p_RutContraparte : item.KRutCtraparte1,
				p_IdContrato : item.KIdContrato,
				p_FechaContrato : item.KFechaContrato,
				p_IdOperacion : item.KIdOpeEstruc,
				p_Producto : item.KProducto,
				p_PeriodoComercial : item.KPeriodoComercial,
				p_NumeroFlujo : item.KS4_XR_NroFlu,
				p_TipoFlujo : item.S4_XR_TipoFlujo,
				p_FactorTasa : item.S4_XR_FacTasa,
				p_SpreadTasa: item.S4_XR_SpreadTasa,
				p_TasaFija: item.S4_XR_TasaFija,
				p_FechaFixTasa: item.S4_XR_FecFixTasa,
				p_FechaInicio: item.S4_XR_FecIni,
				p_FechaTermino: item.S4_XR_FecTer,
				p_MontoNocional: item.S4_XR_MontoNoc,
				p_MontoIntercambio: item.S4_XR_MontoInt,
				p_MonedaMonto: item.S4_X_MonMonto
			}
			datosCalendarioPago.push(calendarioPago);
		});
			
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'NOR',
			p_TipoCarga : 'ARCHIVO',
			p_CalendarioPagos : datosCalendarioPago
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = r.cargarBolson(datos);
		post.$promise.then(
			function(result) {				
				bcsComunes.ventanaMsg({
					mensaje:"Resumen de carga",
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);	
																
				bcsComunes.ventanaCerrar($scope.idVentana);								
			},
			function(result) {
				bcsComunes.msg("error","info","Error al cargar Calendarios de Pago");
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		);
	}
	
	function cargarPagoPeriodo(){
		var datosPagoPeriodoArchivo = csvToArray($scope.data.fileContent,true);

		var datosPagoPeriodo = [];

		var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700216";
		var r = $resource("", {}, {'cargarBolson': { method: "POST", url: url}});
		
		datosPagoPeriodoArchivo.forEach(function(item, index){
			var pagoPeriodo = {
				p_RutContraparte : item.KRutCtraparte1,
				p_IdContrato : item.KIdContrato,
				p_FechaContrato : item.KFechaContrato,
				p_IdOperacion : item.KIdOpeEstruc,
				p_Producto : item.KProducto,
				p_PeriodoComercial : item.KPeriodoComercial,
				p_CorrelativoPago : item.KS5_CorrPago,
				p_IdPago : item.KS5_IdPago,
				p_FechaPago : item.S5_FecPago,
				p_MontoPago: item.S5_MontoPago,
				p_TipoPago: item.S5_TipoPago,
			}
			datosPagoPeriodo.push(pagoPeriodo);
		});
			
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'NOR',
			p_TipoCarga : 'ARCHIVO',
			p_PagosPeriodo : datosPagoPeriodo
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = r.cargarBolson(datos);
		post.$promise.then(
			function(result) {
				bcsComunes.ventanaMsg({
					mensaje:"Resumen de carga",
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
				
				bcsComunes.ventanaCerrar($scope.idVentana);			
			},
			function(result) {
				bcsComunes.msg("error","info","Error al cargar Pagos Periodo");
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		);
	}
	
	function cargarGarantia(){
		var datosGarantiaArchivo = csvToArray($scope.data.fileContent,true);

		var datosGarantia = [];
		
		var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700219";
		var r = $resource("", {}, {'cargarBolson': { method: "POST", url: url}});
		
		datosGarantiaArchivo.forEach(function(item, index){
			var garantia = {
				p_RutContraparte : item.KRutCtraparte1,
				p_IdContrato : item.KIdContrato,
				p_FechaContrato : item.KFechaContrato,
				p_IdOperacion : item.KIdOpeEstruc,
				p_Producto : item.KProducto,
				p_PeriodoComercial : item.KPeriodoComercial,
				p_TipoGarantia : item.S6_TipoGar,
				p_MonedaValorizacion : item.S6_MonVal,
				p_IdGarantia : item.KS6_IdGar,
				p_MontoUmbral: item.S6_MontoUmb,
				p_ValorGarantia: item.S6_ValGtia
			}
			datosGarantia.push(garantia);
		});
			
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'NOR',
			p_TipoCarga : 'ARCHIVO',
			p_Garantias : datosGarantia
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = r.cargarBolson(datos);
		post.$promise.then(
			function(result) {
				bcsComunes.ventanaMsg({
					mensaje:"Resumen de carga",
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
				
				bcsComunes.ventanaCerrar($scope.idVentana);			
			},
			function(result) {
				bcsComunes.msg("error","info","Error al cargar Garantias");
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		);
	}
	
	function cargarTipoActivoGarantia(){
		var datosTipoActivoGarantiaArchivo = csvToArray($scope.data.fileContent,true);

		var datosTipoActivoGarantia = [];

		var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700222";
		var r = $resource("", {}, {'cargarBolson': { method: "POST", url: url}});
		
		datosTipoActivoGarantiaArchivo.forEach(function(item, index){
			var tipoActivoGarantia = {
				p_RutContraparte : item.KRutCtraparte1,
				p_IdContrato : item.KIdContrato,
				p_FechaContrato : item.KFechaContrato,
				p_IdOperacion : item.KIdOpeEstruc,
				p_Producto : item.KProducto,
				p_PeriodoComercial : item.KPeriodoComercial,
				p_TipoGarantia : item.S7_TipoGar,
				p_IdGarantia : item.KS7_IdGar,
				p_TipoActivoGarantia : item.S7_TipoActGar,
				p_PorcentajeGarantia: item.S7_PorcGar,
			}
			datosTipoActivoGarantia.push(tipoActivoGarantia);
		});
			
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'NOR',
			p_TipoCarga : 'ARCHIVO',
			p_TipoActivoGarantias : datosTipoActivoGarantia
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = r.cargarBolson(datos);
		post.$promise.then(
			function(result) {
				bcsComunes.ventanaMsg({
					mensaje:"Resumen de carga",
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
				
				bcsComunes.ventanaCerrar($scope.idVentana);			
			},
			function(result) {
				bcsComunes.msg("error","info","Error al cargar Tipo Activo de Garantias");
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		);
	}
	
	$scope.fileChanged = function(input) {
        $scope.nombreArchivo = '';
        if(input.files[0]){
            $scope.fileName = input.files[0].name;
            if($scope.fileName){
                    $scope.nombreArchivo = $scope.fileName;
            }
        }
    };
});
