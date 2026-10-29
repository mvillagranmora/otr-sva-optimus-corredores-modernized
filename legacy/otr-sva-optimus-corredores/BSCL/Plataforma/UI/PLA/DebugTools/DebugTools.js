angular.module('ModuloPlataforma').controller('DebugToolsCtrl',function($scope,bcsComunes,$resource){
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.tab1 = {};
	$scope.tab2 = {};
	
	$scope.$watch('tab1.aaa',function(newval,oldval) {
		if (!newval) return;
		try {
			var t = JSON.parse(newval);
		}
		catch(e) {
			console.log(e);
			return;
		}
		var sqlInsert = "";
		var sqlSelect = "";
		var sqlFinal = "";
		if (!angular.isArray(t)) {
			console.log('no es arreglo')
			return;
		}
		//INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion") 
		//SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+1,'PAG','USD','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL

		var primero = true;
		$.each(t,function(key,val){
			sqlInsert = "INSERT INTO QWERTY (";
			sqlSelect = "SELECT ";
			$.each(val,function(key2,val2){
				sqlInsert+='"' + key2 +'",'
				var vall = "";
				if (angular.isNumber(val2))
					vall = val2;
				else if (angular.isString(val2))
					vall =  "'" + val2 + "'";
				else 
					vall =  "'" + val2 + "'";
				sqlSelect+= vall + ' as ' + '"' + key2 + '",';

			});
			sqlSelect = sqlSelect.substring(0,sqlSelect.length-1) + " from dummy;\n";
			sqlFinal += sqlInsert.substring(0,sqlInsert.length-1) + ")\n" + sqlSelect  + "\n";
			
		});
		$scope.tab1.bbb = sqlFinal;
		
	});

	$scope.$watch('tab2.aaa',function(newval,oldval) {
		if (!newval) return;
		
		var lineas = newval.split('\n');
		var campos = lineas[0].split(';');
		var valores = lineas[1].split(';');
		
		var sqlInsert = "INSERT INTO QWERTY (";
		for (var i = 0; i<campos.length; i++) {
			sqlInsert+= '"' + campos[i] + '",';
		}
		sqlInsert = sqlInsert.substring(0,sqlInsert.length-1) + ")";

		var sqlSelect = "SELECT ";
		for (var i = 0; i<valores.length; i++) {
			sqlSelect+= "'" + valores[i] + "',";
		}
		sqlSelect = sqlSelect.substring(0,sqlSelect.length-1) + " from dummy;\n";
		var sqlFinal = sqlInsert + "\n" + sqlSelect  + "\n";
		$scope.tab2.bbb = sqlFinal;
	});
	
});