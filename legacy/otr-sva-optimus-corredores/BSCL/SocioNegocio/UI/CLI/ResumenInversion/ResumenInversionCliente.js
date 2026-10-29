angular.module('ModuloSocioNegocio').controller('ResumenInversionClienteCtrl',
function($scope,bcsComunes,$translate,$resource)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.data = {};	
	$scope.config = {};
	$scope.config.certificados = false;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.config.params = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	$scope.config.callCount = 0;
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.oSocioNegocio = {
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		seleccionado : false,
		filtros: {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }
		},
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		},
	};
	
	//nuevo
	$scope.oClientePortafolio ={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
	
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	/*
	var dataSourceConfiguracion = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
					dataType: "json"
				}		
			},
			schema: {
		     	model: {
		     		fields: { Valor : { type: "string"}	}
		     	}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'APV.Habilitado' },
			         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});
	
	dataSourceConfiguracion.fetch(function(){
		if(dataSourceConfiguracion.data().length > 0){
			if(dataSourceConfiguracion.data()[0].Valor === 'S'){
				$scope.MostrarProducto = true;
			}
		}
 	});
 	*/
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.buscar = function()
	{
		$scope.config.callCount =0;
		bcsComunes.ventanaOverlay(idVentana,true);
		
		$("#treeInversiones").remove();
		$("#treeInversionesPasivos").remove();
		$("#treeMovimientos").remove();
		$("#chartInversiones").remove();
		
		var IdCliente			= null;
		var IdPortafolio		= null;
		var IdProductoAgrupador	= null;
		
		//si params existe, quiere decir que no fue llamado desde el menu
		if($scope.config.params.IdCliente)
		{
			IdCliente = $scope.config.params.IdCliente;
			IdPortafolio = $scope.config.params.IdPortafolio;
		}
		else
		{
			if($scope.MostrarProducto)
			{
				IdCliente = Number($scope.oClientePortafolio.seleccionado.IdCliente);
				
				if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oClientePortafolio.seleccionado.IdPortafolio !== undefined)
				{
					IdPortafolio = Number($scope.oClientePortafolio.seleccionado.IdPortafolio);
				}
				else if($scope.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.oClientePortafolio.seleccionado.IdPortafolio !== undefined)
				{
					IdPortafolio = Number($scope.oClientePortafolio.seleccionado.IdPortafolio);
				}
				else if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oClientePortafolio.seleccionado.IdPortafolio === undefined)
				{
					if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) !== 0)
					{
						if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) === 9999)
						{
							IdProductoAgrupador = 0;
						}
						else
						{
							IdProductoAgrupador = Number($scope.oProducto.seleccionado.IdProductoAgrupador);
						}
					}
				}
			}
			else
			{
				IdCliente = $scope.oClientePortafolio.seleccionado.IdCliente;
				IdPortafolio = $scope.oClientePortafolio.seleccionado.IdPortafolio;
			}
		}
		
		var dataResumenInversion =
		{
			p_IdEmpresa				: $scope.SesionIdEmpresa,
			p_IdUsuario				: $scope.SesionIdUsuario,
			p_IdModulo				: 'SOC',
			p_FechaDesde			: util.date($scope.SesionFechaSistema).startOf('month').format("YYYY-MM-DD"),
			p_FechaHasta			: $scope.SesionFechaSistema,
			p_Datos					: [{"IdCliente": IdCliente, "IdPortafolio": IdPortafolio}],
			p_IdProductoAgrupador	: IdProductoAgrupador,
			p_IdTipoCanal           : 'OPT',
			p_ObtenerPactos         : "S"
		};
		
		var urlObtenerResumenInversion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12099";
		var urlObtenerResumenMovimientos = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12100";
		
		/*******************************/
		/*** ResumenInversionCliente ***/
		/*******************************/
		var r = $resource("", {},
		{
			'ResumenInversionCliente': { method: "POST", url: urlObtenerResumenInversion}
		});
		
		var postInversiones = r.ResumenInversionCliente(dataResumenInversion);
		postInversiones.$promise.then(function(result)
		{
			
			var gruposPasivos = [];
			var treePasivos = [];
			var elementosPasivo = [];
			
			var tree = [];
			var grupos = [];			
			var elementos = [];
			var id = 0;
			var total = 0;
			
			var res = result.P_OUT_SALIDA.resumenInversion;
			
			for(var i in res){

				var nombreGrupo = (res[i].Grupo === "PACTOS_SUB") ? "PACTOS" : res[i].Grupo;

				if(grupos.indexOf(nombreGrupo)<0){

					grupos.push(nombreGrupo);	
					tree.push({id: id, "grupo":res[i].Grupo, "valor":"undefined", parentId: null});
					
					id++;
				}
				elementos.push({"grupo":nombreGrupo,"texto": res[i].Item, "valor": res[i].MontoMonedaSistema});
				total+=Math.abs(res[i].MontoMonedaSistema);
			}
			
			
			var elementosTmp =[];
			var idp=0;
			
			for (var i in elementos){
				var esPasivo = (
					elementos[i].texto === "Prestamos" || 
					elementos[i].texto === "Simultanea" || 
					res[i].Grupo === "PACTOS_SUB" 
				);

				var grRep=false;
				if(esPasivo){
					elementosPasivo.push(elementos[i]);
					
					for(var j in gruposPasivos){
						if(gruposPasivos[j]===elementos[i].grupo){
							grRep=true;
							break;
						}
					}
					if(grRep===false){
						gruposPasivos.push(elementos[i].grupo);
						treePasivos.push({id: idp, "grupo":elementos[i].grupo, "valor":"undefined", parentId: null});
						idp++;
					}
				} else { 
					elementosTmp.push(elementos[i]);
				}
				
			}
			elementos=elementosTmp;
			
			
			for(var i in result.P_OUT_SALIDA.resumenInversion){
				result.P_OUT_SALIDA.resumenInversion[i].porcentaje = (Math.abs(result.P_OUT_SALIDA.resumenInversion[i].MontoMonedaSistema) / total);
				console.log(result.P_OUT_SALIDA.resumenInversion[i].porcentaje);
			}

			for(var e in elementos){
				var padre = grupos.indexOf(elementos[e].grupo);
				tree.push({id:id, "grupo": elementos[e].texto, "valor": elementos[e].valor, parentId: padre});
				id++;
				total+=Math.abs(elementos[e].valor || 0);
			}
			
			elementosTmp =[];			
			for(var e in elementosPasivo){
				elementosTmp.push({"grupo":elementosPasivo[e].grupo,"texto":elementosPasivo[e].texto , "valor":elementosPasivo[e].valor });
			}

			var sum =0;
			elementosPasivo=[];
			var anterior = null;

			for (var e in elementosTmp) {
				var actual = elementosTmp[e];

				if (anterior !== null &&
					actual.grupo === anterior.grupo &&
					actual.texto === anterior.texto) {

					sum += actual.valor;

				} else {

					if (anterior !== null) {
						elementosPasivo.push({
							grupo: anterior.grupo,
							texto: anterior.texto,
							valor: sum
						});
					}

					sum = actual.valor;
				}

				anterior = actual;

			}

			if (anterior !== null) {
				elementosPasivo.push({
					grupo: anterior.grupo,
					texto: anterior.texto,
					valor: sum
				});
			}
			
			for(var e in elementosPasivo){
				var padre = gruposPasivos.indexOf(elementosPasivo[e].grupo);
				treePasivos.push({id:idp, "grupo": elementosPasivo[e].texto, "valor": elementosPasivo[e].valor, parentId: padre});
				idp++;
				total+=Math.abs(elementosPasivo[e].valor || 0);
			}
			



			//Crea tabla resumen de inversiones
			$scope.data.treeInversiones = new kendo.data.TreeListDataSource({
				data : tree,
 				schema: {
 					model:{
 						id: "id",
 						expanded: true
 					}
 				}
			});
			$scope.data.treeInversionesPasivos = new kendo.data.TreeListDataSource({
				data : treePasivos,
 				schema: {
 					model:{
 						id: "id",
 						expanded: true
 					}
 				}
			});
			
			//Tabla Activos
			$("#containerInversiones").append("<div id=\"treeInversiones\"></div>");			
			$("#treeInversiones").kendoTreeList({
				dataSource: $scope.data.treeInversiones,
				//height: 540,
				columns: [
					{ field: "grupo", title: "Activos por producto", width: "150px" },
					{ field: "valor", title: "Monto CLP", width: "150px", template : "<label> # if(valor!='undefined'){##=(util.bcs_formato(valor,'ORD','MONTO', 'CLP', '','')).valor##} else {} #</label>" }
				],
				messages:{
					noRows: $translate.instant("UI_SIN_RESULTADOS")
				}
			});
			
			//Tabla Pasivos
			$("#containerInversionesPasivos").append("<div id=\"treeInversionesPasivos\"></div>");			
			$("#treeInversionesPasivos").kendoTreeList({
				dataSource: $scope.data.treeInversionesPasivos,
				//height: 540,
				columns: [
					{ field: "grupo", title: "Pasivos por producto", width: "150px", attributes:{style:"text-align:left;"}},
					{ field: "valor", title: "Monto CLP", width: "150px", attributes:{style:"text-align:left;"}, template : "<label> # if(valor!='undefined'){##=(util.bcs_formato(valor,'ORD','MONTO', 'CLP', '','')).valor##} else {} #</label>" }
				],
				messages:{
					noRows: $translate.instant("UI_SIN_RESULTADOS")
				}
			});
			
			
			//RESUELVE ERROR NOMBRE REPETIDO DE PACTOS
			for(var i in result.P_OUT_SALIDA.resumenInversion){
				if(result.P_OUT_SALIDA.resumenInversion[i].Item === 'Pactos'){
					result.P_OUT_SALIDA.resumenInversion[i].Item = result.P_OUT_SALIDA.resumenInversion[i].Item + " " + result.P_OUT_SALIDA.resumenInversion[i].Grupo;
				}
			}
			
			
			if(tree.length>0){
				$("#contenedorChartInversiones").append("<div id=\"chartInversiones\" class=\"col-xs-12\"></div>");
				$("#chartInversiones").kendoChart({
					legend: {
						position: "bottom"
					},
					dataSource: {
						data: result.P_OUT_SALIDA.resumenInversion
					},
					series: [{
						type: "pie",
						field: "porcentaje",
						categoryField: "Item",
						//explodeField: "explode"
					}],
					seriesColors: ["#03a9f4", "#ff9800", "#fad84a", "#4caf50","#673AB7","#f44336","#e91e63"],
					tooltip: { visible: true,  
						format: "{0:P}", 
						//template: '${category}: #=(util.bcs_formato(value,"ORD","Monto","CLP","","")).valor#'
						//template: "#= category # - #= kendo.format('{0:P}', value) #"
						template: "#=category#: #= kendo.format('{0:P}', value) #"
					}
				});
            }
		}).finally(function(){
			$scope.config.callCount++;
		});
		
		/*********************************/
		/*** ResumenMovimientosCliente ***/
		/*********************************/
		var r = $resource("", {},
		{
			'ResumenMovimientosCliente': { method: "POST", url: urlObtenerResumenMovimientos}
		});
		
		var postMovimientos = r.ResumenMovimientosCliente(dataResumenInversion);
		postMovimientos.$promise.then(function(result)
		{
			var grupos = [];
			var tree = [];
			var elementos = [];
			var id = 0;
			for(var i in result.ResultSet_0){
				if(grupos.indexOf(result.ResultSet_0[i].Moneda)<0){
					//identificar los grupos del arbol
					grupos.push(result.ResultSet_0[i].Moneda);
					tree.push({id: id, texto:result.ResultSet_0[i].Moneda, "moneda":result.ResultSet_0[i].Moneda, "cargo":"undefined", "abono":"undefined", parentId: null, css:""});
					id++;
				}
				elementos.push({"moneda":result.ResultSet_0[i].Moneda,"texto": result.ResultSet_0[i].TipoMovimiento, "cargo": result.ResultSet_0[i].Cargo, "abono": result.ResultSet_0[i].Abono, "saldoInicial": result.ResultSet_0[i].SaldoInicial, "saldoFinal": result.ResultSet_0[i].SaldoFinal, css:""});
			}
			
			for(var e in elementos){
				var padre = grupos.indexOf(elementos[e].moneda);
				tree.push({id:id++, texto: "Saldo inicial", "moneda": elementos[e].moneda, "cargo":"undefined", "abono": elementos[e].saldoInicial, parentId: padre, css:""});
				tree.push({id:id++, texto: elementos[e].texto, "moneda": elementos[e].moneda, "cargo": elementos[e].cargo, "abono": elementos[e].abono, parentId: padre, css:""});
				tree.push({id:id++, texto: "Saldo Final", "moneda": elementos[e].moneda, "cargo": "undefined" , "abono": elementos[e].saldoFinal, parentId: padre, css:""});
				tree[padre].cargo = elementos[e].cargo;
				tree[padre].abono = elementos[e].saldoFinal;
				tree[padre].css = elementos[e].moneda + " hidden";
			}

			$scope.data.treeMovimientos = new kendo.data.TreeListDataSource({
				data : tree,
				schema: {
					model:{
						id: "id",
						expanded: true
					}
				}
			});


			$("#containerMovimientos").append("<div id=\"treeMovimientos\"></div>");			
			 $("#treeMovimientos").kendoTreeList({
				dataSource: $scope.data.treeMovimientos,
				//height: 540,
				columns: [
					{ field: "texto", title: "Moneda", width: "150px"},
					{ field: "cargo", title: "Cargos",template : "<label class='# ##=css#'> # if(cargo!='undefined'){##=(util.bcs_formato(cargo,'ORD','MONTO', moneda, '','')).valor##} else {} #</label>"},
					{ field: "abono", title: "Abonos",template : "<label class='# ##=css#'> # if(abono!='undefined'){##=(util.bcs_formato(abono,'ORD','MONTO', moneda, '','')).valor##} else {} #</label>" }
				],
				messages:{
					noRows: $translate.instant("UI_SIN_RESULTADOS")
				},
				collapse: function(e) {
					$("."+e.model.moneda).removeClass("hidden");
				},
				expand: function(e) {
					$("."+e.model.moneda).addClass("hidden");
				}
			});
			 
		}).finally(function(res){
			$scope.config.callCount++;
		});
	};
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	$scope.$watch('config.callCount', function(newValue, oldValue) {
		if(newValue==2){
			bcsComunes.ventanaOverlay(idVentana,false);
		}
	});
	
	(function(){
		if($scope.config.params.IdCliente)
			$scope.buscar();
	})();
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});