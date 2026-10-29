angular.module('ModuloNormativa').controller('MantenedorReporteResumidoILSCtrl',
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
	$scope.resultadoResumidoPatrimonio = [];
	$scope.resultadoResumidoCobertura = [];	
	$scope.detalleCobertura = [];

	
	$scope.resultadoResumidoPatrimonio.PatrimonioLiquido = 0;
	$scope.detalleCobertura.SaldoTotalB = 0;
	$scope.resultadoResumidoCobertura.SaldoTotalA = 0;
	
	
	//Grilla Resultado Indices
	var dataSourceResultadoIndices = angular.copy(parametrosVentana.items.confResultados.dataSource._data);
	var dataSourceConceptos = angular.copy(parametrosVentana.items.confConceptos.dataSource._data);
	var dataSourcesItems = angular.copy(parametrosVentana.items.confItems.dataSource._data);
	var dataSourcesDetalleCobertura = angular.copy(parametrosVentana.items.confDetalleCobertura.dataSource._data);
	var dataSourcesGrupoConcepto = angular.copy(parametrosVentana.items.confGC.dataSource._data);
	
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
		var dataGC = dataSourcesGrupoConcepto;
		
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
		
		if(dataGC.length > 0){
			for(var i=0; i< dataGC.length; i++){
				if ((dataGC[i].IdConcepto === 2) && (dataGC[i].IdIndice === 4)){
                    dataGC[i].MontoCalculado = util.bcs_formato(dataGC[i].MontoCalculado,'GEN','Monto','CLP','','INDICE').valor;
					$scope.resultadoResumidoPatrimonio.push(dataGC[i]);
				}
				if (dataItems[i].IdConcepto === 3){
					//$scope.resultadoResumidoCobertura.SaldoTotalA = $scope.resultadoResumidoCobertura.SaldoTotalA + dataItems[i].MontoCalculado;
                    dataItems[i].MontoCalculado = util.bcs_formato(dataItems[i].MontoCalculado,'GEN','Monto','CLP','','INDICE').valor;
					$scope.resultadoResumidoCobertura.push(dataItems[i]);
				}
			}
			$scope.resultadoResumidoPatrimonio.PatrimonioLiquido = dataConceptos.filter(c => c.IdIndice === 4 && c.IdConcepto === 2)[0].MontoCalculado;
			bcsComunes.ventanaOverlay($scope.winID, false);
		}
		
		if(dataGC.length > 0){
			for(var i=0; i< dataGC.length; i++){
				if ((dataGC[i].IdConcepto === 3) && (dataGC[i].IdIndice === 4)){
				    dataGC[i].MontoCalculado = util.bcs_formato(dataGC[i].MontoCalculado,'GEN','Monto','CLP','','INDICE').valor;
					$scope.resultadoResumidoCobertura.push(dataGC[i]);
					//$scope.resultadoResumidoCobertura.SaldoTotalA = $scope.resultadoResumidoCobertura.SaldoTotalA + dataGC[i].MontoCalculado;
				}
			}
			$scope.resultadoResumidoCobertura.SaldoTotalA = dataConceptos.filter(c => c.IdIndice === 4 && c.IdConcepto === 3)[0].MontoCalculado;
			bcsComunes.ventanaOverlay($scope.winID, false);
		}
		
		if(dataDetalleCobertura.length > 0){
			for(var i=0; i< dataDetalleCobertura.length; i++){
				if(dataDetalleCobertura[i].Categoria !== 'TOTAL GENERAL'){
					$scope.detalleCobertura.SaldoTotalB = $scope.detalleCobertura.SaldoTotalB + dataDetalleCobertura[i].MontoCobertura;
				}
				dataDetalleCobertura[i].MontoCobertura = util.bcs_formato(dataDetalleCobertura[i].MontoCobertura,'GEN','Monto','CLP','','INDICE').valor;
				$scope.detalleCobertura.push(dataDetalleCobertura[i]);
			}
            $scope.detalleCobertura.SaldoTotalB = util.bcs_formato($scope.detalleCobertura.SaldoTotalB,'GEN','Monto','CLP','','INDICE').valor;
			bcsComunes.ventanaOverlay($scope.winID, false);
		}
				
    };
    
    //Para diferenciar formato segn sea razon (%) o monto con separador de miles.
    $scope.valueFormatting = function(idindice){
    	//return 6 > 5 ? {color: 'green', 'background-color': '#f4f4f4'} : {color: 'red'}
    	return {'background-color': '#f4f4f4'}
    }
    
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
