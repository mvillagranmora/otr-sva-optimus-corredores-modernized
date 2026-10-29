angular.module('ModuloNormativa').controller('MantenedorReporteDetalladoILSCtrl',
	function($scope,$uibModal,$translate,bcsComunes,bcsServiciosNormativa, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	var parametrosVentana = bcsComunes.ventanaParams($scope.winID);
	/*var itemsAccTransBur = [51,81,82];
	var itemsDolar = [44,78,95];
	var itemsDerechME = [46,63,84];
	var itemsSaldoNetAcreME = [44,48,68];*/
	
	
	$scope.resultadoIndices ={};
	$scope.datosCorredora={};
	$scope.detalleresultadoIndices = [];
	$scope.acumuladoresultadoIndices = [];	
	$scope.detalleConceptos = [];
	$scope.resultadoItems = [];
	$scope.detalleCobertura = [];
	$scope.detalleItems = [];
	$scope.resultadoDetalleCobertura = [];
	$scope.resultadoDetallePatrimonio = [];
		
	$scope.resultadoDetallePatrimonio.PatrimonioLiquido = 0;
	$scope.detalleCobertura.SaldoTotalB = 0;
	$scope.resultadoDetalleCobertura.SaldoTotalA = 0;
	/*$scope.detalleItems.AccionesConTransBur = 0;
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
	$scope.detalleItems.TAccVC = 0;*/
	
	
	//Grilla Resultado Indices
	var dataSourceResultadoIndices = angular.copy(parametrosVentana.items.confResultados.dataSource._data);
	var dataSourceConceptos = angular.copy(parametrosVentana.items.confConceptos.dataSource._data);
	var dataSourcesItems = parametrosVentana.items.confItems.dataSource._data;
	var dataSourcesDetalleCobertura = angular.copy(parametrosVentana.items.confDetalleCobertura.dataSource._data);
	var dataSourcesDetalleItems = angular.copy(parametrosVentana.items.confDetalleItem.dataSource._data);
	
    //CELCONDOR-518	
	$scope.datosCorredora.DocumentoIdentidad        = parametrosVentana.items.empresa.seleccionado.DocumentoIdentidad;
	$scope.datosCorredora.RazonSocial				= parametrosVentana.items.empresa.seleccionado.RazonSocial
	$scope.datosCorredora.Direccion					= parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalCalle + " " +  parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalNumero + " - " + parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalNombreCiudad + " - " + parametrosVentana.items.empresa.seleccionado.DireccionFisicaPrincipalNombrePais;

    
    var cargarReporteILS = function(){
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		console.log(parametrosVentana.items.confResultados.dataSource);
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
				data[i].MontoIndice = (util.bcs_formato(data[i].MontoIndice,'GEN','Tasa','CLP','','').valor) + ' ' + data[i].Tipo;
				$scope.detalleresultadoIndices.push(data[i]);				

				if (dataConceptos.length > 0){				
					$scope.acumuladoresultadoIndices.push(data[i]);
					for (var j = 0; j < dataConceptos.length; j++) {
						if(dataConceptos[j].IdIndice === data[i].IdIndice){
							$scope.acumuladoresultadoIndices.push(dataConceptos[j]);
						}
				    }
			     }			
		    }
			for (var i = 0; i<dataConceptos.length; i++){

				if (!($scope.detalleConceptos.some(c => c.IdConcepto === dataConceptos[i].IdConcepto))){
				    dataConceptos[i].MontoCalculado = util.bcs_formato(dataConceptos[i].MontoCalculado,'GEN','Monto','CLP','','INDICE').valor;
					$scope.detalleConceptos.push(dataConceptos[i]);
				}
				else{
                    dataConceptos[i].MontoCalculado = util.bcs_formato(dataConceptos[i].MontoCalculado,'GEN','Monto','CLP','','INDICE').valor;
                }
			}
		    bcsComunes.ventanaOverlay($scope.winID, false);
	    }
		
		/*if(dataItems.length > 0){
			for(var i=0; i< dataItems.length; i++){
				$scope.resultadoItems.push(dataItems[i]);
				
				if (dataItems[i].Operacion === '+'){
					$scope.resultadoItems.PatrimonioLiquido = $scope.resultadoItems.PatrimonioLiquido + dataItems[i].Monto;
				}else if (dataItems[i].Operacion === '-'){
					        $scope.resultadoItems.PatrimonioLiquido = $scope.resultadoItems.PatrimonioLiquido - dataItems[i].Monto;
				      }
			}
			bcsComunes.ventanaOverlay($scope.winID, false);
		}	*/
		
		if(dataDetalleCobertura.length > 0){
			for(var i=0; i< dataDetalleCobertura.length; i++){
				if(dataDetalleCobertura[i].Categoria !== 'TOTAL GENERAL'){
					$scope.detalleCobertura.SaldoTotalB = $scope.detalleCobertura.SaldoTotalB + dataDetalleCobertura[i].MontoCobertura;
				}
				dataDetalleCobertura[i].MontoGarantia = util.bcs_formato(dataDetalleCobertura[i].MontoGarantia,'GEN','Monto','CLP','','').valor;
                dataDetalleCobertura[i].MontoBancosyEmpresas = util.bcs_formato(dataDetalleCobertura[i].MontoBancosyEmpresas,'GEN','Monto','CLP','','').valor;
                dataDetalleCobertura[i].FactorA = dataDetalleCobertura[i].FactorA + '%';
                dataDetalleCobertura[i].FactorB = dataDetalleCobertura[i].FactorB + '%';
                dataDetalleCobertura[i].MontoCobertura = util.bcs_formato(dataDetalleCobertura[i].MontoCobertura,'GEN','Monto','CLP','','').valor;
                $scope.detalleCobertura.push(dataDetalleCobertura[i]);
			}
			$scope.detalleCobertura.SaldoTotalB = util.bcs_formato($scope.detalleCobertura.SaldoTotalB,'GEN','Monto','CLP','','').valor;
			bcsComunes.ventanaOverlay($scope.winID, false);
		}
		
		if(dataDetalleItems.length > 0){
			for(var i=0; i< dataDetalleItems.length; i++){

				if ((dataDetalleItems[i].IdConcepto === 2) && (dataDetalleItems[i].IdIndice === 4)){
					dataDetalleItems[i].MontoCalculado = dataDetalleItems[i].MontoCalculado;
                    $scope.resultadoDetallePatrimonio.push(dataDetalleItems[i]);
				}
				
				if ((dataDetalleItems[i].IdConcepto === 3) && (dataDetalleItems[i].IdIndice === 4)){
					$scope.resultadoDetalleCobertura.SaldoTotalA = $scope.resultadoDetalleCobertura.SaldoTotalA + dataDetalleItems[i].MontoCalculado;
					dataDetalleItems[i].MontoCalculado = util.bcs_formato(dataDetalleItems[i].MontoCalculado,'GEN','Monto','CLP','','').valor;
					$scope.resultadoDetalleCobertura.push(dataDetalleItems[i]);
				}
			}
			$scope.resultadoDetallePatrimonio.PatrimonioLiquido = dataConceptos.filter(c => c.IdIndice === 4 && c.IdConcepto === 2)[0].MontoCalculado;
			$scope.resultadoDetalleCobertura.SaldoTotalA = dataConceptos.filter(c => c.IdIndice === 4 && c.IdConcepto === 3)[0].MontoCalculado;
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
