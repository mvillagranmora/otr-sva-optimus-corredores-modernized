angular.module('ModuloNormativa').controller('MantenedorReporteILSCtrl',
	function($scope,$uibModal,$translate,bcsComunes,bcsServiciosNormativa, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	var parametrosVentana = bcsComunes.ventanaParams($scope.winID);
	var itemsAccTransBur = [51,81,82];
	var itemsDolar = [44,78,95];
	var itemsDerechME = [46,63,84];
	var itemsSaldoNetAcreME = [44,48,68];
	
	
	$scope.resultadoIndices ={};
	$scope.datosCorredora={};
	$scope.detalleresultadoIndices = [];
	$scope.acumuladoresultadoIndices = [];	
	$scope.detalleConceptos = [];
	$scope.resultadoItems = [];
	$scope.detalleCobertura = [];
	$scope.detalleItems = [];
	
	$scope.resultadoItems.PatrimonioLiquido = 0;
	$scope.detalleCobertura.SaldoTotalB = 0;
	//$scope.dataDetalleItems.SaldoTotalA = 0;
	$scope.detalleItems.AccionesConTransBur = 0;
	$scope.detalleItems.AccionesSinTransBur = 0;
	$scope.detalleItems.Oro = 0;
	$scope.detalleItems.Dolar = 0;
	$scope.detalleItems.CFMMenor90 = 0;
	$scope.detalleItems.CFMInvOtros = 0;
	$scope.detalleItems.CFMOtros = 0;
	$scope.detalleItems.CxC = 0;
	$scope.detalleItems.DctoCtaxCob = 0;
	$scope.detalleItems.DeuVarios = 0;
	$scope.detalleItems.ActCirculantes = 0;
	$scope.detalleItems.DerecME = 0;
	$scope.detalleItems.SalNetAcrME = 0;
	$scope.detalleItems.TAccVC = 0;
	
	
	//Grilla Resultado Indices
	var dataSourceResultadoIndices = parametrosVentana.items.confResultados.dataSource.ds._data;
	var dataSourceConceptos = parametrosVentana.items.confConceptos.dataSource.ds._data;
	var dataSourcesItems = parametrosVentana.items.confItems.dataSource.ds._data;
	var dataSourcesDetalleCobertura = parametrosVentana.items.confDetalleCobertura.dataSource.ds._data;
	var dataSourcesDetalleItems = parametrosVentana.items.confDetalleItem.dataSource.ds._data;
	
    //CELCONDOR-518	
	$scope.datosCorredora.DocumentoIdentidad        = parametrosVentana.items.empresa.seleccionado.DocumentoIdentidad;
	$scope.datosCorredora.RazonSocial				= parametrosVentana.items.empresa.seleccionado.RazonSocial
	$scope.datosCorredora.Direccion					= parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalCalle + " " +  parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalNumero + " - " + parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalNombreCiudad + " - " + parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalNombrePais;

    
    var cargarReporteILS = function(){
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		var data = dataSourceResultadoIndices;
		var dataConceptos = dataSourceConceptos;
		var dataItems = dataSourcesItems;
		var dataDetalleCobertura = dataSourcesDetalleCobertura;
		var dataDetalleItems = dataSourcesDetalleItems;
		
		if (data.length > 0){
			//Datos fijos de indices
			$scope.resultadoIndices.Fecha = util.dateSPdate(parametrosVentana.items.kngFechaContable);
			$scope.resultadoIndices.Intermediario = parametrosVentana.items.empresa.seleccionado.Nombre;			
			
			//Datos variables indices
			for (var i = 0; i < data.length; i++) {
				$scope.detalleresultadoIndices.push(data[i]);				

				if (dataConceptos.length > 0){				
					$scope.acumuladoresultadoIndices.push(data[i]);
					for (var j = 0; j < dataConceptos.length; j++) {
						if(dataConceptos[j].IdIndice === data[i].IdIndice){
							$scope.detalleConceptos.push(dataConceptos[j]);							
							$scope.acumuladoresultadoIndices.push(dataConceptos[j]);
						}
				    }
			     }			
		    }
		    bcsComunes.ventanaOverlay($scope.winID, false);
	    }
		
		if(dataItems.length > 0){
			for(var i=0; i< dataItems.length; i++){
				$scope.resultadoItems.push(dataItems[i]);
				
				if (dataItems[i].Operacion === '+'){
					$scope.resultadoItems.PatrimonioLiquido = $scope.resultadoItems.PatrimonioLiquido + dataItems[i].Monto;
				}else if (dataItems[i].Operacion === '-'){
					        $scope.resultadoItems.PatrimonioLiquido = $scope.resultadoItems.PatrimonioLiquido - dataItems[i].Monto;
				      }
			}
			bcsComunes.ventanaOverlay($scope.winID, false);
		}	
		
		if(dataDetalleCobertura.length > 0){
			for(var i=0; i< dataDetalleCobertura.length; i++){
				$scope.detalleCobertura.push(dataDetalleCobertura[i]);
				
				$scope.detalleCobertura.SaldoTotalB = $scope.detalleCobertura.SaldoTotalB + dataDetalleCobertura[i].MontoCobertura;
			}
			
			bcsComunes.ventanaOverlay($scope.winID, false);
		}
		
		if(dataDetalleItems.length > 0){
			for(var i=0; i< dataDetalleItems.length; i++){
				//Acciones de Transaccion Bursátil
				if(itemsAccTransBur.indexOf(dataDetalleItems[i].IdItem) >= 0){
					$scope.detalleItems.AccionesConTransBur = $scope.detalleItems.AccionesConTransBur + dataDetalleItems[i].Monto;	
				}
				
				//Acciones sin Transaccion Bursátil
				if(dataDetalleItems[i].IdItem === 93){
					$scope.detalleItems.AccionesSinTransBur = $scope.detalleItems.AccionesSinTransBur + dataDetalleItems[i].Monto;	
				}
				
				//Oro
				if(dataDetalleItems[i].IdItem === 94){
					$scope.detalleItems.Oro = $scope.detalleItems.Oro + dataDetalleItems[i].Monto;	
				}
				
				//Dólar
				if(itemsDolar.indexOf(dataDetalleItems[i].IdItem) >= 0){
					$scope.detalleItems.Dolar = $scope.detalleItems.Dolar + dataDetalleItems[i].Monto;	
				}
				
				//CFM inst. menor 90
				if(dataDetalleItems[i].IdItem === 50){
					$scope.detalleItems.CFMMenor90 = $scope.detalleItems.CFMMenor90 + dataDetalleItems[i].Monto;	
				}
				
				//CFM otros
				if(dataDetalleItems[i].IdItem === 98){
					$scope.detalleItems.CFMInvOtros = $scope.detalleItems.CFMInvOtros + dataDetalleItems[i].Monto;	
				}
				
				//CFM Otros
				if(dataDetalleItems[i].IdItem === 58){
					$scope.detalleItems.CFMOtros = $scope.detalleItems.CFMOtros + dataDetalleItems[i].Monto;	
				}
												
				//CxC
				if(dataDetalleItems[i].IdItem === 96){
					$scope.detalleItems.CxC = $scope.detalleItems.CxC + dataDetalleItems[i].Monto;	
				}
				
				//Documentos y cuentas x cobrar
				if(dataDetalleItems[i].IdItem === 52){
					$scope.detalleItems.DctoCtaxCob = $scope.detalleItems.DctoCtaxCob + dataDetalleItems[i].Monto;	
				}
				
				//Deudores varios
				if(dataDetalleItems[i].IdItem === 8){
					$scope.detalleItems.DeuVarios = $scope.detalleItems.DeuVarios + dataDetalleItems[i].Monto;	
				}
				
				//Otros activos circulantes
				if(dataDetalleItems[i].IdItem === 73){
					$scope.detalleItems.ActCirculantes = $scope.detalleItems.ActCirculantes + dataDetalleItems[i].Monto;	
				}
				
				//Derechos ME
				if(itemsDerechME.indexOf(dataDetalleItems[i].IdItem) >= 0){					
					$scope.detalleItems.DerecME = $scope.detalleItems.DerecME + dataDetalleItems[i].Monto;	
				}
				
				//Saldo neto acreedor ME
				if(itemsSaldoNetAcreME.indexOf(dataDetalleItems[i].IdItem) >= 0){					
					$scope.detalleItems.SalNetAcrME = $scope.detalleItems.SalNetAcrME + dataDetalleItems[i].Monto;	
				}
				
				//Titulos accionarios
				if(dataDetalleItems[i].IdItem === 85){					
					$scope.detalleItems.TAccVC = $scope.detalleItems.TAccVC + dataDetalleItems[i].Monto;	
				}
				
				//$scope.dataDetalleItems.SaldoTotalA = $scope.dataDetalleItems.SaldoTotalA + dataDetalleCobertura[i].Monto;
			}			
			bcsComunes.ventanaOverlay($scope.winID, false);
		}
		
		
    };	
    
    cargarReporteILS();
    
    $(".export-pdf").click(function() {
		
		  kendo.drawing.drawDOM($("#test-drawing"), {
	          forcePageBreak:".break",
	          paperSize: "A2",
	          margin: "0.5cm",
	          //multipage:true
	        }).then(function(group){
	          kendo.drawing.pdf.saveAs(group, "Reporte ILS.pdf");
	        });
    });
    
});
