angular.module('ModuloPlataforma').controller('ConsultarFlujoCajaCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope,bcsServicioPlataforma)
{
	//Recupera parametros desde la ventana recibida
    $scope.winID 		= bcsComunes.ventanaWinId($scope);
    $scope.parametros 	= bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	
	var cliente = this;
	cliente.Info = [$scope.parametros];

	$scope.data = {};
	$scope.data.kngFechaEfectiva = util.dateInicioDia($scope.SesionFechaSistema);

	//LLAMAR A SP FLUJO CAJA
	var DataFlujo 		= [];
	var ColumnsFlujo 	= [];
	var AggregateFlujo	= [];
	var FieldsFlujo  	= {};
	

	
	function dynamicSort(property) {
		var sortOrder = 1;
		if(property[0] === "-") {
			sortOrder = -1;
			property = property.substr(1);
		}
		return function (a,b) {
			var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
			return result * sortOrder;
		}
	}


	var FlujoCaja = function(filters){
		var DataFlujo 		= [];
		var ColumnsFlujo 	= [];
		var AggregateFlujo	= [];
		var FieldsFlujo  	= {};
		
		bcsServicioPlataforma.Servicios().ConsultaFlujoCaja(filters).$promise.then(
			function(respuesta){			

				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					//Trasponer resultado para mostrar en array		
					var items = [];
					var NombreUso		= '';
					var NombreItem		= '';
					var NombreOperador 	= '';
					var NombreMonto 	= '';
					ColumnsFlujo.push( {field : "ItemCalculo",	title:"UI_ITEM",  width: "260px"}); //,	locked:true
					FieldsFlujo["ItemCalculo"] = {type : "string"};


					for(i=0; i<Object.keys(respuesta.P_OUT_FLUJOCAJADETALLADO).length; i++){
						if (NombreUso !== respuesta.P_OUT_FLUJOCAJADETALLADO[i].NombreUso){
							NombreUso		= respuesta.P_OUT_FLUJOCAJADETALLADO[i].NombreUso;
							NombreOperador 	= respuesta.P_OUT_FLUJOCAJADETALLADO[i].NombreUso + "_Operador";
							NombreMonto		= respuesta.P_OUT_FLUJOCAJADETALLADO[i].NombreUso + "_Monto";


							//Seteo de Columnas
							var columns = {};
							columns = {field : NombreMonto, title: NombreUso, width: "125px" , template: "<label> #:(util.bcs_formato("+NombreMonto+",'GEN','Monto','"+cliente.Info[0].IdMoneda+"','','')).valor #</label>", footerTemplate:" #:(util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #"};

							ColumnsFlujo.push(columns);

							//Seteo de los Fields
							FieldsFlujo[NombreOperador] = {type : "string"};
							FieldsFlujo[NombreMonto] 	= {type : "number"};

							//Seteo del Aggregate
							AggregateFlujo.push({ field: NombreMonto, aggregate: "sum" });
						};

						//Guardar data en array
						items.push(respuesta.P_OUT_FLUJOCAJADETALLADO[i]);
					};
					items.sort(dynamicSort("ItemCalculo"));

					//Seteo del data
					for(i=0; i<items.length; i++){
						if (NombreItem !== items[i].ItemCalculo){
							NombreItem = items[i].ItemCalculo;
							var item  = {};
							item["ItemCalculo"]		= $translate.instant(NombreItem);
							item[items[i].NombreUso + "_Operador"] = items[i].FuncionOperador;
							item[items[i].NombreUso + "_Monto"]	= items[i].Monto;
						} else if (NombreItem === items[i].ItemCalculo) {
							item[items[i].NombreUso + "_Operador"] = items[i].FuncionOperador;
							item[items[i].NombreUso + "_Monto"]	= items[i].Monto;
						}
						if ((i+1 <= items.length-1) && NombreItem !== items[i+1].ItemCalculo)
							DataFlujo.push(item);
						if (i === items.length-1)
							DataFlujo.push(item);				
					}


					//console.log($scope.confConsultarFlujoCaja);
					var tmp = confConsultarFlujoCaja();
					tmp.columns = ColumnsFlujo;
					tmp.defDataSource = {
						type : "array",
						definition : {
							data		: DataFlujo,
							schema  	: {
								model 	: { fields : FieldsFlujo }
							},
							aggregate	: AggregateFlujo
						}
					};
					$scope.gridCFC.setOptions({
						dataSource : new bcs.DataSource(tmp.defDataSource),
						columns : ColumnsFlujo
					});
				} else {				
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_CONSULTAR_FLUJO_CAJA"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_CONSULTAR_FLUJO_CAJA"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);			
			}
		);
	};


	//Grilla Flujo Caja
	$scope.gridCFC = {};
	var confConsultarFlujoCaja = function () {
		return {
			gridCreated : function(grid) {
				$scope.gridCFC = grid;
			},			
			dataSource : new bcs.DataSource({
				type : "array",
				definition : {
					schema : {
						model : {
							fields : { }
						}
					}
				}
			}),
			dataBound	: function(e) {
				bcsComunes.ventanaOverlay($scope.winID, false);
		    },
			columns 	: [],
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: false,
			height 		: 400,
			editable	: false,
			autoBind 	: true,
			selectable 	: "multiple row",
			excel 		: { allPages: true }
		};
	};
	$scope.confConsultarFlujoCaja = confConsultarFlujoCaja();


	$scope.$watch('data.kngFechaEfectiva',function(newval,oldval){
		bcsComunes.ventanaOverlay($scope.winID, true);

		var filters = {};
		filters.p_IdEmpresa				= $scope.SesionIdEmpresa;
		filters.p_IdUsuario				= $scope.SesionIdUsuario;
		filters.p_IdModulo				= 'CTA';
		filters.p_UsaReglasNegocio		= 'S';
		filters.p_FechaDesde			= util.dateSPdate($scope.data.kngFechaEfectiva);
		filters.p_FechaHasta			= util.dateSPdate($scope.data.kngFechaEfectiva);
		filters.p_Uso					= null;
		filters.p_DesplegarEnUI			= 'S';
		filters.p_AgruparPorOrigen		= 'N';
		filters.p_PortafolioAConsultar	= [{IdPortafolio: cliente.Info[0].IdPortafolio,IdMoneda: cliente.Info[0].IdMoneda}];
		FlujoCaja(filters);
	},true);

	var firstHeight = $('#Win_FlujoCaja');
    firstHeight.css('height', '555px');
});