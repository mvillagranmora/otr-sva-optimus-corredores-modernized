	angular.module('ModuloAPV').controller('DetalleCargaRecaudacionCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';
	
		$scope.data = {};
		$scope.btnProcedar = false;
			
		var dci = this;
		
		//	Configurar al principio
		
		dci.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		dci.data = {};
		
		//Asigna valores iniciales
		var datos = {};	
		
		dci.inicio = function (){
			
			console.log("Inicio");
			
			$scope.data.datagrilla = dsDetalleCarga.data();
			
			
		}
		
		
		
		dci.procesarDetallesCarga = function(accion){		
				
			let bodyServicio= {
				p_IdEmpresa:$scope.SesionIdEmpresa,
				p_IdUsuario:$scope.SesionIdUsuario,
				p_IdModulo:'APV',
				p_accion:accion,
				p_IdCargaMasiva:dci.parametrosEntrada.IdCargaMasiva
			}
			
			bcsComunes.ventanaOverlay($scope.ventanaId,true);	
			
			bcsServiciosAPV.ProcesarCargaMasivaRecaudacion(bodyServicio).$promise.then(function(respuesta){
				for(var key in respuesta.P_OUT_MENSAJES){
		   			if(respuesta.P_OUT_MENSAJES[key].codigo ===  "ERROR")
		   				bcsComunes.msg("warning","warning", respuesta.P_OUT_MENSAJES[key].descripcion);
		   			else
		   				bcsComunes.msg("success","success", respuesta.P_OUT_MENSAJES[key].descripcion);
	
					bcsComunes.ventanaOverlay(idVentana,false);
					$scope.habilitarBoton = true;
				}			     	
	
				if(respuesta.P_OUT_MENSAJES.length > 0){
					var mensajeCarga = new Array();
					$.each(respuesta.P_OUT_MENSAJES,function(key,value){
						 var objCarga = {
								IdInterno : value.id,
								tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
								codigo : value.codigo,
								descripcion : value.descripcion
						 };
						 mensajeCarga.push(objCarga);
					});
				}
				respuesta.P_OUT_RESULTADO = mensajeCarga;
				bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);
				bcsComunes.ventanaOverlay(idVentana,false);	
			});
			
			var filter = Array(); 
			filter.push({ field: "IdCargaMasiva", operator: "eq", value: dci.parametrosEntrada.IdCargaMasiva });
			dsDetalleCarga.read();
			dsDetalleCarga.filter(filter);
			
					
		};
		
			
		
		
		
				
		var defDetalleCarga = {
				type : "odata",
				definition : {
	                serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 30,
	                sort 			: false,
	                transport		: {
						read: {
							url 		: bcsConsultasAPV('ConsultarCargaDetalleRecaudacion'),
							dataType	: "json"
					    }
	                },
	                schema 			: {
	                	model: {
	                		fields: {
	                			 IdCargaMasiva		: {type: "number"}
								,Fecha				: {type: "string"}                      
								,TipoDeposito		: {type: "string"}                  
								,FolioCartola		: {type: "number"}              
								,FolioLineaCartola	: {type: "number"}       
								,Banco				: {type: "string"}                          
								,Cuenta				: {type: "string"}                         
								,Monto				: {type: "number"}                  
								,PeriodoPago		: {type: "string"}             
								,TipoPago			: {type: "string"}               
								,RUTEmpleador		: {type: "string"}               
								,RUTCliente			: {type: "string"}                
								,TipoDeAfiliado		: {type: "string"}             
								,Regimen			: {type: "string"}                  
								,MensajeValidacion	: {type: "string"}        
								,EstadoRendicion	: {type: "string"}              
								,EstadoProceso		: {type: "string"}
	                		}
	                	}
	                }
				}
		};
		
		var dsDetalleCarga = new bcs.DataSource(defDetalleCarga);
		dci.cargaDetalle = {
			dataSource: dsDetalleCarga,
			columns		: [  {field: "IdCargaMasiva"				, title: "UI_ID_CARGA"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true}
							,{field: "Fecha"						, title: "UI_FECHA"				, width: "110px", align: "left"		, sortable : true, editable: false }
							,{field: "TipoDeposito"					, title: "UI_ID_PLAN"			, width: "110px", align: "left"		, sortable : true, editable: false}
							,{field: "FolioCartola"					, title: "UI_FOLIO_CARTOLA"		, width: "180px", align: "left"		, sortable : true, editable: false }
							,{field: "FolioLineaCartola"			, title: "UI_LINEA_CARTOLA"		, width: "110px", align: "right"	, sortable : true, editable: false }
							,{field: "Banco"						, title: "UI_BANCO"				, width: "110px", align: "left"		, sortable : true, editable: false}
							,{field: "Cuenta"						, title: "UI_CUENTA"			, width: "110px", align: "left"		, sortable : true, editable: false }
							,{field: "PeriodoPago"					, title: "UI_PERIODO_PAGO"		, width: "300px", align: "left"		, sortable : true, editable: false }
							,{field: "Monto"						, title: "UI_MONTO"				, width: "80px"	, align: "center"	, sortable : true, editable: false }
														
							,{field: "RUTEmpleador"					, title: "UI_RUT_EMPLEADOR"		, width: "80px"	, align: "center"	, sortable : true, editable: false }
							,{field: "RUTCliente"					, title: "UI_RUT_CLIENTE"		, width: "80px"	, align: "center"	, sortable : true, editable: false }
							,{field: "TipoDeAfiliado"				, title: "UI_TIPO_AFILIADO"		, width: "200px", align: "center"	, sortable : true, editable: false }
							,{field: "Regimen"						, title: "UI_REGIMEN"			, width: "100px", align: "center"	, sortable : true, editable: false }
							,{field: "MensajeValidacion"			, title: "UI_MENSAJE_VALIDACION", width: "300px", align: "center"	, sortable : true, editable: false }
							,{field: "EstadoRendicion"				, title: "UI_ESTADO_RENDICION"	, width: "100px", align: "center"	, sortable : true, editable: false }
							,{field: "EstadoProceso"				, title: "UI_ESTADO_PROCESO"	, width: "200px", align: "center"	, sortable : true, editable: false }
			],
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 280,
			autoBind	: true,
			gridCreated: function(grid){
	    			var filter = Array();
    				filter.push({ field: "IdCargaMasiva", operator: "eq", value: dci.parametrosEntrada.IdCargaMasiva });
	    			dsDetalleCarga.filter(filter);		    			
	    		}
			//filter: [{ field: "IdCargaMasiva", operator: "eq", value: dci.parametrosEntrada.IdCargaMasiva }]
		};
		
		dci.inicio();
	});