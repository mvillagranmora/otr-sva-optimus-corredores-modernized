angular.module('ModuloTesoreria').controller('DetalleNominaDepositoCtrl',
			function($scope,$translate,$timeout,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosTesoreria, bcsBlotterDepositosGrid)
{
	
	$scope.winID 		= bcsComunes.ventanaWinId($scope);
	$scope.parametros 	= bcsComunes.ventanaParams($scope.winID);	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	var bInfoDeposito	= this;
	var dsDefDeposito 	= bcsBlotterDepositosGrid.dsDepositos;
	dsDefDeposito.definition.filter = { field : "IdDeposito", operator: "eq", value: $scope.parametros[0].IdDeposito};
	dsDefDeposito = new bcs.DataSource(dsDefDeposito);

	dsDefDeposito.fetch().then(function(){
		if (dsDefDeposito.data().length >= 1 ){							
			bInfoDeposito.Detalle	= dsDefDeposito.data()[0];
		}
	});


	var dsDef = bcsBlotterDepositosGrid.dsDepositoDetalle;
	dsDef.definition.filter = { field : "IdDeposito", operator: "eq", value: $scope.parametros[0].IdDeposito};
	$scope.confBasicaDepositosDetalle = {
		dataSource			: new bcs.DataSource(dsDef),
		columns				: bcsBlotterDepositosGrid.columnasDepositoDetalle(),
		sortable			: true,
		pageable			: false,
		autoBind			: true,
		filterable			: true,
		height				: 300,
		selectable 			: "multiple row",
		gridCreated			: function(e){ $scope.gridApi = e;},
		rowColorBackground	: function(row) {								
								if (row.IdEstado == 'ANU')
									return "red";
								return "";
							  },
		userSettings		: true
	};

	
	$scope.GestionDeDeposito = function(TipoEjecucion, MotivoAnulacion){
		var parametros = {};
		var documentos = [];
		$.each($scope.seleccionDepositosDetalle, function(index, value){
			var ObjCus = {
				"Id"			: value.IdDepositoDetalle || 0,
				"Entidad"		: 'TES.DepositoDetalle',
				"IdConcepto"	: MotivoAnulacion.length > 0 ? MotivoAnulacion[0].IdConceptoAnulacion : '',
				"Descripcion"	: MotivoAnulacion.length > 0 ? MotivoAnulacion[0].Descripcion : ''
			};
			documentos.push(ObjCus);
		});


		parametros.p_IdEmpresa   		= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario   		= $scope.SesionIdUsuario;
		parametros.p_IdModulo 			= 'TES';
		parametros.p_simularOperacion 	= 'N';
		parametros.p_UsaReglasNegocio   = 'S';
		parametros.p_FechaMovimiento	= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		parametros.p_TipoEjecucion		= TipoEjecucion;
		parametros.p_Registros			= documentos;

		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosTesoreria.GestionDeDeposito(
			parametros,
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);

				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);


				//Actualizo Cabecera
				dsDefDeposito.fetch().then(function(){
					if (dsDefDeposito.data().length >= 1 ){							
						bInfoDeposito.Detalle	= dsDefDeposito.data()[0];
					}
				});
				
				//Actualizo detalle
				$scope.gridApi.dataSource.read();
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	};

	$scope.LiquidarDocumentos = function(){
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_CERTIFICAR_Y_LIQUIDAR_DOCUMENTO'),$scope.winID );
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				$scope.GestionDeDeposito('LIQ',[]);
			}
		});
		return;
	};


	$scope.AnularDocumentos = function(){
		var Parametros = {
			"NombreEntidad"			: 'TES.Deposito',
			"TipoConceptoTesoreria"	: 'ANU'
		};
		
		var ventanaMotivoAnulacion = bcsComunes.ventanaAbrir("MotivoAnulacion", Parametros, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaMotivoAnulacion);
		promesa.then(function(data){

			if (data.length > 0){
				$scope.GestionDeDeposito('ANU', data);
			}
		});
	};

	var firstWindow = $('#Win_DetalleNominaDeposito');
			firstWindow.css("height","508px")
});