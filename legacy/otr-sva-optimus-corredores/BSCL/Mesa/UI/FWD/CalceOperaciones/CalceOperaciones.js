(function() {
	angular.module('ModuloMesa').controller('CalceOperaciones',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.oDesplegar = {};
		$scope.ObjSeleccionGrilla = {};
		var totalCalce = 0;
		$scope.HabilitoBotonDescalce = false;
		$scope.HabilitoBotonCalce = false;
		//Instrumento
		$scope.data.ConfiguracionInstrumento={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }				
			}
		};
		
		//Moneda Principal
		$scope.data.ConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};

		//Operación
		$scope.data.oConfiguracionOperacion={
			filtros : {
				IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW" }
			}
		};
		
		var dsDefConsultaMovimientosFWD = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "FolioOperacion", dir: "desc" },
                pageSize        : 15,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=515",
                		dataType:"json"
                	}
                },
                schema: {
                	model: {
                		fields: {
                			IdOperacion				: {type: "number"},
							FolioOperacion			: {type: "number"},
							FechaOperacion			: {type: "sysdate"},
							FechaLiquidación		: {type: "sysdate"},
							FechaOperacionOPE		: {type: "systime"},
							NombreInstrumento       : {type: "string"},
							Nemotecnico       		: {type: "string"},
							IdTipoOperacion			: {type: "string"},
							TipoOperacion			: {type: "string"},
							RutCliente				: {type: "string"},
							NombreCliente			: {type: "string"},
							SocioNegocio			: {type: "string"},
							IdMonedaPrincipal		: {typer:"string"},
							IdMonedaSecundaria		: {type: "string"},
							MontoMonedaPrincipal	: {type: "number"},
							MontoMonedaSecundaria	: {type: "number"},
							TipoCambioCierre		: {type: "number"},
							TipoCambioCosto			: {type: "number"},
							ParidadCierre			: {type: "number"},
							ParidadCosto			: {type: "number"},
							MontoEquivalente		: {type: "number"},
							Margen					: {type: "number"},
							Utilidad				: {type: "number"},
							MontoDiferido			: {type: "number"},
							MontoCalce				: {type: "number"},
							MontoDisponible			: {type: "number"},
							Modalidad				: {type: "number"},
							Plazo					: {type: "number"},
							PlazoResidual			: {type: "number"},
							FechaVencimiento		: {type: "sysdate"},
							FechaEfectiva			: {type: "sysdate"},
							Agente					: {type: "number"},
							NombreAgente			: {type: "string"},
							CodigoUsuario			: {type: "string"},
							Estado					: {type: "string"}
						}
					}
                }
			}
		};	
		
		var dsCarteraOperCalce = new bcs.DataSource(dsDefConsultaMovimientosFWD);
		$scope.confBasicaOperaciones = {
			dataSource: dsCarteraOperCalce,
			columns: [
			     {field: "IdOperacion", title: "UI_FOLIO", width: "60px", align: "left"}
			    ,{field: "FolioOperacion", title: "UI_FOLIO", width: "60px", align: "left", sortable : false, hidden: true }
			    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", align: "left"}
			    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "150px", align: "left"}
			    ,{field: "SocioNegocio", title: "UI_SOCIO_NEGOCIO", width: "150px", align: "left"}
			    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA", width: "60px", align: "center"}
			    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_OPERACION", width: "70px", align: "left", template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    ,{field: "MontoDisponible", title: "UI_MONTO_DISPONIBLE_CALCE", width: "70px", align: "right", template : " <label> #:  (util.bcs_formato(MontoDisponible,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    ,{field: "PlazoResidual", title: "UI_PLAZO_RESIDUAL", width: "70px", align: "right"}
			],		
			sortable			: true,
			userSettings		: true,
			reorderable			: true,
			pageable			: true,
			filterable			: true,
			height				: 250,
			selectable 			: "row",
			autoBind 			: false,
			rowColorBackground  :
				function(row) {
					if(row.MontoDisponible === 0) return "red";
					return "";
				},
		};
		
		
		var dsDefConsultaOperacionesCalce = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 15,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=520",
                		dataType:"json"
                	}
                },
                schema: {
                	model: {
                		fields: {
							IdOperacionOri			: {type: "number"},
							IdOperacion				: {type: "number"},
							FolioCartera			: {type: "number"},
							IdMonedaPrincipal		: {type: "string"},
							IdMonedaSecundaria		: {type: "string"},
							MontoOperacion			: {type: "number"},
							MontoCalce				: {type: "number", validation : {min : 0}},
							MontoDisponible			: {type: "number"},
							PlazoResidual			: {type: "number"},
							IdTipoInstrumento		: {type: "string"},
							IdInstrumento			: {type: "number"},
							IdTipoOperacion			: {type: "string"},
							TipoOperacion			: {type: "string"},
							FechaVencimiento		: {type: "sysdate"},
							Modalidad				: {type: "number"},
							IdCliente				: {type: "number"},
							RutCliente				: {type: "string"},
							NombreCliente			: {type: "string"},
							IdAgente				: {type: "number"},
							NombreAgente			: {type: "string"},
							IdEstado				: {type: "string"}
						}
                	}
                }
			}
		};
		
		var dsCarteraCalce = new bcs.DataSource(dsDefConsultaOperacionesCalce);
		$scope.confBasicaCalce = {
			dataSource: dsCarteraCalce,
			columns: [ 
			     {field: "IdOperacion", 	title: "UI_FOLIO", width: "60px", align: "left", sortable : false, hidden: true }
			    ,{field: "IdOperacionOri", 	title: "UI_FOLIO", width: "60px", align: "left", sortable : false, hidden: true }
			    ,{field: "FolioCartera", 	title: "UI_FOLIO", width: "60px", align: "left"}
			    ,{field: "TipoOperacion", 	title: "UI_TIPO_OPERACION", width: "90px", align: "left"}
			    ,{field: "NombreCliente", 	title: "UI_NOMBRE_CLIENTE", width: "150px", align: "left"}
			    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA", width: "60px", align: "center"}
			    ,{field: "MontoOperacion", 	title: "UI_MONTO_OPERACION", width: "70px", align: "left", template : " <label> #:  (util.bcs_formato(MontoOperacion,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>"}
				,{field: "MontoDisponible", title: "UI_MONTO_DISPONIBLE_CALCE", width: "70px", align: "right", template : " <label> #:  (util.bcs_formato(MontoDisponible,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
				,{field: "MontoCalce", 		title: "UI_MONTO_CALZAR", width: "70px", align: "right", editable: true,format:"{0:d}", template : " <label> #:  (util.bcs_formato(MontoCalce,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" } 		
			],
			sortable			: true,
			userSettings		: true,
			reorderable			: true,
			pageable			: true,
			filterable			: true,
			height				: 250,
			selectable 			: "row",
			autoBind 			: false,
			rowColorBackground  :
				function(row) {
					if (row.MontoDisponible === 0)
						return "red";				                 
					if (row.IdOperacionOri === $scope.ObjSeleccionGrilla.seleccionoperaciones.FolioOperacion)
						return "green";
					return "";
	            },
            notEditableColumns	: 
            	function(row) {
	            	if (row.MontoDisponible === 0 || $scope.ObjSeleccionGrilla.seleccionoperaciones.MontoDisponible === 0)
	            		return ['MontoCalce'];
	            }
		};		
		
		
		var BuscoCarteraForward = function(){
			if(dsCarteraCalce !== undefined)
				dsCarteraCalce.removeAll();
			
			var filter = Array();
			filter.push({ field: "Estado", operator: "eq", value: 'VIG'});
			if($scope.data.OperacionSeleccionada !== undefined){
				filter.push({ field: "IdTipoOperacion"	, operator: "eq", value: $scope.data.OperacionSeleccionada.IdTipoOperacion});	
			}
			if($scope.data.InstrumentoSeleccionado !== undefined){
				if ($scope.data.InstrumentoSeleccionado.Nemotecnico !== 0){
					filter.push({ field: "IdInstrumento", operator: "eq", value: $scope.data.InstrumentoSeleccionado.IdInstrumento});
				}
			}
			filter.push({ field: "PlazoResidual", operator: "gt", value: 0});
			$scope.ObjSeleccionGrilla.seleccionoperaciones = undefined;
			dsCarteraOperCalce.filter(filter);
		};
		
		$scope.BuscoOperacionesFWD = function(){		
			BuscoCarteraForward();
		};
		
		$scope.BuscoOperacionesParaCalzar = function(){
			if($scope.ObjSeleccionGrilla.seleccionoperaciones !== undefined){
				if(dsCarteraCalce !== undefined)
					dsCarteraCalce.removeAll();
				
				$scope.IdEstado = { field: "IdEstado", operator: "eq", value: 'VIG'};
				$scope.IdOperacionOriA = { field: "IdOperacionOri", operator: "eq", value: $scope.ObjSeleccionGrilla.seleccionoperaciones.FolioOperacion};
				$scope.IdOperacionOriB = { field: "IdOperacionOri", operator: "eq", value: 0};
				$scope.PlazoResidual = { field: "PlazoResidual", operator: "eq", value: $scope.ObjSeleccionGrilla.seleccionoperaciones.PlazoResidual};
				
				if($scope.ObjSeleccionGrilla.seleccionoperaciones.IdTipoOperacion === 'COMFWD'){
					$scope.IdTipoOperacion = { field: "IdTipoOperacion", operator: "eq", value: 'VENFWD'};
				}else if($scope.ObjSeleccionGrilla.seleccionoperaciones.IdTipoOperacion === 'VENFWD'){
					$scope.IdTipoOperacion = { field: "IdTipoOperacion", operator: "eq", value: 'COMFWD'};
				}
				$scope.Modalidad = { field: "Modalidad", operator: "eq", value: $scope.ObjSeleccionGrilla.seleccionoperaciones.Modalidad};
				$scope.IdMonedaPrincipal = { field: "IdMonedaPrincipal", operator: "eq", value: $scope.ObjSeleccionGrilla.seleccionoperaciones.IdMonedaPrincipal};
				$scope.IdMonedaSecundaria = { field: "IdMonedaSecundaria", operator: "eq", value: $scope.ObjSeleccionGrilla.seleccionoperaciones.IdMonedaSecundaria};

				
				
				var filters = {}
				//Si esta calzada totalmente
				if($scope.ObjSeleccionGrilla.seleccionoperaciones.MontoDisponible === 0){
						filters  =  {
						logic: "and",
						filters : [$scope.IdOperacionOriA]
					};
					//Si no esta calzada en su totalidad
				} else if($scope.ObjSeleccionGrilla.seleccionoperaciones.MontoDisponible === $scope.ObjSeleccionGrilla.seleccionoperaciones.INGCantidad){
					filters  =  {
					logic: "and",
					filters : [{
								logic : "or",
								filters : [$scope.IdOperacionOriA,$scope.IdOperacionOriB]
							  }
							 ,$scope.IdEstado
							 ,$scope.PlazoResidual
				             ,$scope.IdTipoOperacion
				             ,$scope.Modalidad 
				             ,$scope.IdMonedaPrincipal
				             ,$scope.IdMonedaSecundaria
						]};
				}else {
					//Si esta calzada parcialmente
					filters  =  {
					logic: "or",
					filters : [$scope.IdOperacionOriA,
							{
								logic : "and",
								filters : [
									  $scope.IdOperacionOriB
									 ,$scope.IdEstado
									 ,$scope.PlazoResidual	
						             ,$scope.IdTipoOperacion
						             ,$scope.Modalidad 
						             ,$scope.IdMonedaPrincipal
						             ,$scope.IdMonedaSecundaria
									]}
							 
						]};
				}

			
				
				dsCarteraCalce.filter(filters);
				
				$scope.data.kngMontoCalce = $scope.ObjSeleccionGrilla.seleccionoperaciones.MontoDisponible;
				$scope.data.kngMontoCalzado = undefined;
				$scope.data.kngMontoDiferencia = undefined;
				totalCalce = 0;
			}
		};
		/*
		$scope.OperacionesCalzadas = function(dataItem){
			return {
				dataSource			: AsignoDataSourceOperCalzadas(dataItem),
				columns				: [ 
				       				     {field: "FolioCartera", title: "UI_FOLIO", width: "60px", align: "left", sortable : false }
				       				    ,{field: "IdTipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", align: "left", sortable : false }
				       				    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "150px", align: "left", sortable : false }
				       				    ,{field: "MontoOperacion", title: "UI_MONTO_OPERACION", width: "70px", align: "left", sortable : false }
										,{field: "MontoDisponible", title: "UI_MONTO_DISPONIBLE_CALCE", width: "70px", align: "right", sortable : false }
									  ],		
				sortable			: true,
				pageable			: true,
				autoBind			: true,  //comentar
			    selectable 			: "row",
			    detailExpandold: function(e) {
			        console.log(e);
			        e.sender.select(e.masterRow);
			    }
			};
		};
		
		AsignoDataSourceOperCalzadas = function(dataItem){
			return new bcs.DataSource ({
				type : "odata",
				definition : 
				{
	                serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 15,
	                transport		: {
		        	            		read: 
		        	            		{
		        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=520",
		        							dataType:"json"
		        	            		}
		        	            	},
	                schema 			: { 
	                				   model: 
	                						{
												fields : 
												{
													IdOperacionOri			: {type: "number"},
													IdOperacion				: {type: "number"},
													FolioCartera			: {type: "number"},
													MontoOperacion			: {type: "number"},
													MontoCalce				: {type: "number"},
													MontoDisponible			: {type: "number"},
													PlazoResidual			: {type: "number"},
													IdInstrumento			: {type: "number"},
													IdTipoOperacion			: {type: "string"},
													DocumentoIdentidad		: {type: "string"},
													NombreCliente			: {type: "string"},
													IdAgente				: {type: "number"},
													NombreAgente			: {type: "string"}
												}
	                						}
		        	            	},
		        	 filter			:[{ field: "IdOperacionOri", 	operator: "eq", 	value: Number(dataItem.FolioOperacion)}]
				 }
			});
		};
		*/
		
		$scope.$watch('ObjSeleccionGrilla.seleccioncalce',function(newval, oldval){
			if($scope.ObjSeleccionGrilla.seleccioncalce !== undefined && $scope.ObjSeleccionGrilla.seleccioncalce.IdEstado === 'VIG'){
				if($scope.ObjSeleccionGrilla.seleccioncalce.MontoDisponible < $scope.ObjSeleccionGrilla.seleccioncalce.MontoOperacion){
					$scope.HabilitoBotonDescalce = true;	
				}
				else if($scope.ObjSeleccionGrilla.seleccioncalce.MontoCalce > 0){
					$scope.HabilitoBotonCalce = true;
				}
				else{
					$scope.HabilitoBotonDescalce = false;
					$scope.HabilitoBotonCalce = false;
				}
			}
			else{
				$scope.HabilitoBotonDescalce = false;
				$scope.HabilitoBotonCalce = false;
			}
		});
		
		$scope.DescalzarOperacion = function(){
			DescalceOperacion();
		};
		
		var DescalceOperacion = function(){
			if($scope.ObjSeleccionGrilla.seleccioncalce !== undefined){
				
				var tablaOperacionesDescalce = [];
				var ObjDescalce = {
				 	"FolioInterno" 	: $scope.ObjSeleccionGrilla.seleccioncalce.IdOperacion,
				 	"FolioCartera"	: $scope.ObjSeleccionGrilla.seleccioncalce.IdOperacionOri,			
				};
				tablaOperacionesDescalce.push(ObjDescalce);
				
				var datosDescalce = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdPais			: $scope.SesionIdPaisSistema,
					p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo			: 'MFW',
					p_UsaReglasNegocio 	: 'N',
					p_tablaDescalce 	: tablaOperacionesDescalce	
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosMesa.DescalceOperacionForward(datosDescalce).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			   		
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.msg("info","Informacion","Operación Descalzada correctamente.");
					}
					else{
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Error al Descalzar - " + value.Descripcion);
						});
				    }
					bcsComunes.ventanaOverlay(idVentana,false);
					$scope.HabilitoBotonDescalce = false;
					BuscoCarteraForward();
		   		},function(respuesta){
		   			$scope.HabilitoBotonDescalce = false;
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.msg("error","Error","Existen problemas al ingresar la operacion");
		   		});
			}
		};
		
		
		var valMontosCalces = $scope.confBasicaCalce;
		valMontosCalces.save = function(e){
			var dataCalces = dsCarteraCalce.data();
			var FolioCartera = e.model.IdOperacion;
			var montoCalce = 0;
			
			for(var i=0; i < dataCalces.length; i++){
				if(dataCalces[i].IdOperacion === FolioCartera){
					if(dataCalces[i].MontoDisponible >= e.values.MontoCalce){
						if(dataCalces[i].MontoDisponible > 0){
							montoCalce += e.values.MontoCalce;
							e.model.MontoCalce = e.values.MontoCalce;
						}
						else{
							e.values.MontoCalce = 0;
							e.model.MontoCalce = 0;
							e.preventDefault();
							bcsComunes.msg("error","Error","No se puede asignar Monto Calzar si no hay Monto Disponible.");
							break;
						}
					}
					else{
						e.values.MontoCalce = 0;
						e.model.MontoCalce = 0;
						e.preventDefault();
						bcsComunes.msg("error","Error","No se puede asignar un Monto Calzar mayor al Monto Disponible Calce.");
						break;
					}
				}
				else {
					montoCalce += (dataCalces[i].MontoCalce || 0);
				}
				
			}
			if(montoCalce > $scope.data.kngMontoCalce)
			{
				montoCalce -= e.values.MontoCalce;
				e.values.MontoCalce = 0;
				e.model.MontoCalce = 0;
				e.preventDefault();
				bcsComunes.msg("error","Error","El monto total de operaciones a calzar no debe supererar el monto de la operación " + ($scope.data.kngMontoCalce) + ".");
			}
			$scope.HabilitoBotonCalce = true;
			$scope.data.kngMontoCalzado = montoCalce;
			$scope.data.kngMontoDiferencia = (($scope.data.kngMontoCalce || 0) - ($scope.data.kngMontoCalzado || 0));
		};
		
		
		$scope.GuardarCalces = function(){
			var tablaOperacionesCalce	= [];
			var CalceOperacionesForward = dsCarteraCalce.data()|| [];
			$.each(CalceOperacionesForward,function(key,value){	
				if (value.MontoCalce > 0){
					//Operación que voy a calzar
					var ObjCalce = {
					 	"FolioInterno" 			: $scope.ObjSeleccionGrilla.seleccionoperaciones.FolioOperacion,
					 	"IdOperacion"			: 0,
					 	"FolioCartera"			: value.FolioCartera,
       				    "IdTipoOperacion" 		: value.IdTipoOperacion,
       				    "IdMonedaPrincipal"		: value.IdMonedaPrincipal,
       				    "IdTipoInstrumento"		: value.IdTipoInstrumento,
       				    "IdInstrumento"			: value.IdInstrumento,
       				    "FechaVencimiento"		: value.FechaVencimiento,
       				    "MontoOperacion"		: value.MontoOperacion,
						"MontoDisponible"		: value.MontoDisponible,
						"MontoCalce"			: value.MontoCalce
					};
					tablaOperacionesCalce.push(ObjCalce);
				}
			});
			
			var datosGraboCalce = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdPais			: $scope.SesionIdPaisSistema,
				p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo			: 'MFW',
				p_UsaReglasNegocio 	: 'N',
				p_tablaCalce 		: tablaOperacionesCalce
			};
			
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.GrabarCalces(datosGraboCalce).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		   		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.msg("info","Informacion","Operaciones calzadas correctamente.");
				}
				else{
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Error al calzar operaciones : " + value.Codigo + " - " + value.Descripcion);
					});
			    }
				$scope.HabilitoBotonCalce = false;
				bcsComunes.ventanaOverlay(idVentana,false);
				BuscoCarteraForward();	   			
	   		},function(respuesta){
	   			$scope.HabilitoBotonCalce = false;
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("error","Error","Existen problemas al ingresar la operacion");
	   		});
		};
	});
}());