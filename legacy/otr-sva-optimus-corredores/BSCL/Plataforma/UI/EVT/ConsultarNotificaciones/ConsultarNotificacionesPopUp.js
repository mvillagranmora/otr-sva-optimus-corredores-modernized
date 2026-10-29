angular.module('ModuloPlataforma').controller('ConsultarNotificacionesPopUpCtrl',function($scope,bcsServicioPlataforma,bcsMantenedorEventosGrid,bcsComunes,$translate,bcsAlerta,$http,$rootScope,$timeout){

 	$scope.conf = {};
	$scope.datos = {};     
	$scope.template = {};
	$scope.campos = {};
	var objFather = {};
	var objTagDefault = {};
	var obj = {};
	var obj = {};
	var keyValue = [];
	var contextoArray = [];
	var contextoObj = {};
	
	$scope.dataSourceNotificaciones = new bcs.DataSource({
		
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "IdNotificacion", dir: "desc" },
			pageSize : 50,
		    transport: {
				read: { 
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1381",
					dataType: "json"
				}
			},
			schema :
			{
				model: {
					fields : {

							 IdEmpresa						: {type: "number"}
							,IdNotificacion 				: {type: "number"}
							,IdConfiguracionEvento			: {type: "number"}
							,FechaCreacion 					: {type: "date"}
							,FormaDespacho					: {type: "code"}
							,TiempoDespacho					: {type: "code"}
							,Asunto							: {type: "string"}
							,FechaDespachoOrginal			: {type: "sysdatetime"}
							,FechaDespachoRecordatorio		: {type: "sysdatetime"}
							,Enviado						: {type: "code"}
							,FechaEnviado					: {type: "sysdatetime"}
							,IdUsuarioEnviado				: {type: "number"}
							,ReEnviar						: {type: "code"}
							,ReEnviado						: {type: "code"}
							,FechaReEnviado					: {type: "sysdatetime"}
							,IdUsuarioReEnviado				: {type: "number"}
							,ConfirmacionDespacho			: {type: "code"}
							,TokenConfirmacion				: {type: "string"}
							,Confirmado						: {type: "code"}
							,FechaConfirmado				: {type: "date"}
							,AccionConfirmado				: {type: "code"}
							,IdUsuarioConfimado				: {type: "number"}
							,FechaVencimiento				: {type: "date"}
							,FechaRecordatorio				: {type: "date"}
							,Observacion					: {type: "string"}
							,Descartado						: {type: "code"}
							,IdUsuarioDescarte				: {type: "number"}
							,FechaDescarte					: {type: "sysdatetime"}
							,Enviar							: {type: "code"}
							,DireccionEvento				: {type: "string"}
							,IdUsuarioRecibe				: {type: "number"}
							,Leida							: {type: "code"}
							,FormaDespachoSrv 				: {type: "string"}
							,DescripcionConfiguracion		: {type: "string"}
						 

							
						
					}
				} 
			},
			filter :
				 [
					{field : "Leida",operator: "eq", value : 'N'},
		         	{field : "Enviado",operator:"eq",value: 'UI_SI'},
		         	{field : "FormaDespachoSrv ", operator:"neq", value : 'C'},
		         	{field : "IdUsuarioRecibe",operator:"eq",value: $scope.SesionIdUsuario}
				]
				           
			        
			 
			 
		}  
	});
	
	
	
	$scope.notificacionesrecibidas = {
			dataSource			:  $scope.dataSourceNotificaciones,
			columns				:  [
 				                       { field: "DescripcionConfiguracion"		,title: 'UI_NOTIFICACION'					, width: "100px" },
				                       { field: "FormaDespacho"					,title: 'UI_FORMA_DESPACHO'					, width: "100px" },
				                       { field: "Asunto"						,title: 'UI_ASUNTO'		        			, width: "145px" },
				                       { field: "TiempoDespacho"				,title: 'UI_TIEMPO_DESPACHO'				, width: "100px" },
				                       { field: "FechaDespachoOrginal"			,title: 'UI_FECHA_DESPACHO_ORIGINAL'		, width: "145px", template: kendo.template("#if (util.dateisdefault(FechaDespachoOrginal))  {##} else {##=util.formatoSecondate(FechaDespachoOrginal)##}#")   }
			                     ],			
			sortable			: false,
			filterable			: false,    
			pageable			: true, 
			autoBind			: true,  //comentar
			selectable 			: "row",
			height				: 290
		    
		};
    
	$scope.limpiarFiltros = function (){
		$scope.conf.kngFechaDesde = util.date($scope.SesionFechaSistema).hour(0).minute(0).second(0).toDate();
		$scope.conf.kngFechaHasta = util.date($scope.SesionFechaSistema).add(23,'hours').minute(0).second(0).toDate();
		$scope.gridApi.dataSource.removeAll();
	};
	
	angular.element(document).ready(function () {
		var filters = new Array();	
		filters.push({ field: "IdUsuarioRecibe", operator: "eq", value: $scope.SesionIdUsuario });
	});
	
	function agregarPredeterminados(ds){
  		obj = {};
 		var i = 0;
 		var z = 0;
		var total = ds.length;
 		var codigoevento = '';
 		var name = '';
	
		$.each(ds, function(key,codigos){
			obj[codigos.Llave] = codigos.ValorDefault;
			i++;
			if(i===total){
				objTagDefault[codigos.CodigoTipoEvento] = obj;
				obj = {};
			}
        });
		
		objFather["data"] = objTagDefault;
  		$scope.camposAsociados = JSON.stringify(objFather);
		camposAsociados = $scope.camposAsociados;  
		
 	 
	};  
	
	var tagsDefaultDataSource_ = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: { 
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+1393,
				dataType: "json"
		    }
		},
		filter : {	field: "IdPlantilla", operator: "eq", value:  0}
	});
 
	tagsDefaultDataSource_.fetch(function(){
		$scope.view = tagsDefaultDataSource_.view();
	});
	
	
	$scope.evaluar = function(){
		objFather = {};
		objTagDefault = {};

		var datos = {	
		    			p_IdEmpresa : $scope.SesionIdEmpresa,
			  			p_IdUsuario : $scope.SesionIdUsuario,
			  			p_IdModulo : 'EVT',
			  			p_in_IdNotificacion : [],
			  			p_Accion : 6
    				};
    	datos.p_in_IdNotificacion.push({IdNotificacion : $scope.conf.notificacionesrecibidas.IdNotificacion });
		var dataSourceTemplate = new bcs.DataSource({
			
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdPlantilla", dir: "desc" },
				pageSize : 50,
			    transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1405",
						dataType: "json"
					}
				},
				schema : 
				{
					model: {
						fields : {
							
							
							 IdPlantilla					: {type: "number"}
							,Template 						: {type: "string"}
							,IdUsuarioRecibe				: {type: "number"}
							,Agrupa							: {type: "string"} 
							,IdConfiguracionEvento			: {type: "number"}
							,IdNotificacion					: {type: "number"}
						 
							
						}
					} 
				},
				filter : [
							{field : "IdEmpresa", operator: "eq", value : $scope.SesionIdEmpresa},
							{field : "IdNotificacion",operator: "eq", value : $scope.conf.notificacionesrecibidas.IdNotificacion}
						]
			}
		});
		dataSourceTemplate.fetch(function(){
			  
 				$scope.datos = $.extend({},dataSourceTemplate.data()[0]);
 				var dataSourceCamposNoAgrupados = new bcs.DataSource({
					
					type : "odata",
					definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: { field: "IdEventoDetalle", dir: "desc" },
						pageSize : 50,   
					    transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1406",
								dataType: "json"
							}
						},
						schema : 
						{
							model: {
								fields : {
									
									 Llave						: {type: "string"}
									,Valor 						: {type: "string"}
									,CodigoTipoEvento			: {type: "string"}
									,IdEvento					: {type: "number"}
									,IdConfiguracionEvento		: {type: "number"}
									,Agrupa						: {type: "string"}
								}
							} 
						},
						filter :{ logic : "and",
								  filters : [ 
											  { field : "IdNotificacion", operator: "eq", value : $scope.conf.notificacionesrecibidas.IdNotificacion},
											  {field : "Agrupa", operator: "eq", value : 'N'}
										 ]
							
								}
					}
				});
				var dataSourceCamposAgrupados = new bcs.DataSource({
					
					type : "odata",
					definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: { field: "IdEventoDetalle", dir: "desc" },
						pageSize : 50,   
					    transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1407",
								dataType: "json"
							}
						},
						schema : 
						{
							model: {
								fields : {
									
									 Llave						: {type: "string"}
									,Valor 						: {type: "string"}
									,CodigoTipoEvento			: {type: "string"}
									,IdEvento					: {type: "number"}
									,IdConfiguracionEvento		: {type: "number"}
									,Agrupa						: {type: "string"}
								}
							} 
						},
						filter :{ logic   : "and",
							  filters : [
										  {	field : "Agrupa", operator: "eq", value : 'S'},
										  { field : "IdConfiguracionEvento", operator: "eq", value : $scope.conf.notificacionesrecibidas.IdConfiguracionEvento},
										  { field : "IdNotificacion", operator: "eq", value : $scope.conf.notificacionesrecibidas.IdNotificacion},
 										] 
							}
								
								
						
					}   
				});
				keyValue = [];
				if($scope.datos.Agrupa==='S'){
				dataSourceCamposAgrupados.fetch(function(){
 						
				 	var i = 0;
					var total = dataSourceCamposAgrupados.total();
 					var codigoevento = '';
				 	var llave = '';
					$.each(dataSourceCamposAgrupados.data(), function(key,codigos){
						if(parseInt(codigos.UsuarioEnvia)===$scope.SesionIdUsuario || parseInt(codigos.UsuarioRecibe)===$scope.SesionIdUsuario){
					    	if(codigos.Agrupa=='S'){	
							
							if(i===0){
								codigoevento = codigos.CodigoTipoEvento;
								llave = codigos.Llave;
							}
							if(codigoevento===codigos.CodigoTipoEvento){
								if(i>0){
									if(llave===codigos.Llave){
											keyValue.push(obj);
											objTagDefault[codigoevento] = keyValue;
											obj = {};
 											obj[codigos.Llave] = codigos.Valor;
									}else{
											obj[codigos.Llave] = codigos.Valor;
									}
											
								}else{
											obj[codigos.Llave] = codigos.Valor;
									 }
										
							}else{
									keyValue.push(obj);
									objTagDefault[codigoevento] = keyValue;
									obj = {};
				 					keyValue = [];
									codigoevento = codigos.CodigoTipoEvento;
									obj[codigos.Llave] = codigos.Valor;
								}
									i++;
									if(i===total){
										keyValue.push(obj);
										objTagDefault[codigoevento] = keyValue;
										obj = {};
									}
						  } 
						}
 						});
					agregarPredeterminados($scope.view);
					if(total>0){
						var contexto = JSON.stringify(objFather);
						var json = JSON.parse(contexto);
						var template =  json.data.TagGenerales.Confirmar.replace('::URL::','#')+$scope.datos.Template;
						console.log("contexto "+contexto)
						console.log("TOTAL AGRUPADOS ==== " + total );
					    var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
					    var data = {
					      template: template,
					      context: contexto
					    };      
					    $http({ 
						        method: 'POST',
						        url: url,
						        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
						        transformRequest: function(obj) {
						            var str = [];
						            for(var p in obj)
						            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
						            return str.join("&");
						        },
						        data: data
						    }).success(function (dataTpt) {
						    	bcsServicioPlataforma.EnviarNotificaciones(datos,function(result){
				  		 	       	
					  		  	    if (result.P_OUT_ERRORESCONTADOR == "0"){
					  		  	    		console.log(dataTpt);
					  		  	    		var templt = dataTpt.replace(/<a (.*?)>/g,"<a href='#' >");
	 				  		 		    	var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
					  		   				$timeout(function(){OpenWindow.init(templt)},500);
					  		   		    	bcsComunes.msg('info', 'Procesar', 'OK');
					  		   		    	$scope.notificacionesrecibidas.dataSource.read(); 

					  		  				
					  		  	    	}else{
					  		   				bcsComunes.msg("error","error",$translate.instant(result.P_OUT_ERRORESDETALLE));
					  		  	    	}

					  		  			},
					  		  			function(result){
					  		  				console.log(result);
					  		  				bcsComunes.msg("error","error",$translate.instant('Error al intentar actualizar notificación'));
					  		   			}); 
								
						    }).error(function (err){
					 	    	bcsComunes.msg('error', 'Procesar', err);
						    });
					}
				});  
				}
				if($scope.datos.Agrupa==='N'){
				dataSourceCamposNoAgrupados.fetch(function(){
					var z = 0;
					var total = dataSourceCamposNoAgrupados.total();
					console.log(total);
					$.each(dataSourceCamposNoAgrupados.data(), function(key,codigos){
						if(parseInt(codigos.UsuarioEnvia)===$scope.SesionIdUsuario || parseInt(codigos.UsuarioRecibe)===$scope.SesionIdUsuario){

							if(codigos.Agrupa=='N'){
								
								if(z===0){
									codigoevento = codigos.CodigoTipoEvento;
									obj[codigos.Llave] = codigos.Valor;
 								}
								if(z>0){
									if(codigoevento===codigos.CodigoTipoEvento){
										obj[codigos.Llave] = codigos.Valor;
									}else{
										objTagDefault[codigoevento] = obj;
										obj = {};
										obj[codigos.Llave] = codigos.Valor;
										codigoevento = codigos.CodigoTipoEvento;
									}
									
								}
							z++;
							if(z===total){
								objTagDefault[codigoevento] = obj;
								obj = {};
							}
							}
							}
							
						});
					
					agregarPredeterminados($scope.view);
					
					if(total>0){
						var contexto = JSON.stringify(objFather);
						var json = JSON.parse(contexto);
						var template =  (json.data.TagGenerales.Confirmar).replace('::URL::','#')+$scope.datos.Template.replace(/<!--@(.*?)-->/g, '</@$1>');						 
					    var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
					    var datostemplate = {
					      template: template,
					      context: contexto
					    };
					    $http({ 
						        method: 'POST',
						        url: url,
						        headers: {
 						        		'Content-Type': 'application/x-www-form-urlencoded'
						        			},
						        transformRequest: function(obj) {
						            var str = [];
						            for(var p in obj)
						            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
						            return str.join("&");
						        },
						        data: datostemplate,
 						    }).success(function (dataTpt) {

 						    	var templt = dataTpt.replace(/<a (.*?)>/g,"<a href='#' >");
								var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
								$timeout(function(){OpenWindow.init(templt)},500);
								bcsComunes.msg('info', 'Procesar', 'OK');
								$scope.notificacionesrecibidas.dataSource.read();

 						    	bcsServicioPlataforma.EnviarNotificaciones(datos,function(result){
				  		 	       	
 					  		  	    if (result.P_OUT_ERRORESCONTADOR == "0"){
 					  		  	    	bcsComunes.msg("info","info",$translate.instant("LEIDA"));		
									}else{
										bcsComunes.msg("error","error",$translate.instant(result.P_OUT_ERRORESDETALLE));
									}

								},
								function(result){
									console.log(result);
									bcsComunes.msg("error","error",$translate.instant('Error al intentar actualizar notificación'));
								}); 
								
							}).error(function (err){
					 	    	bcsComunes.msg('error', 'Procesar', err);
						    });
					}
				});
				}	
		});
	};

	var firstWidthCrear = $('#Win_MisNotificaciones');
    firstWidthCrear.css('width', '810px');

});
   