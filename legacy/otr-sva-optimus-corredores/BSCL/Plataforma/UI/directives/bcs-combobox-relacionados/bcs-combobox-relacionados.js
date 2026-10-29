angular.module('ModuloPlataforma').directive('bcsComboboxRelacionados', function($modal,$translate,$rootScope,$timeout) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			datosRelacionados : '=?',
			kOptions: '=?',
			ngModel : '=?',
			kNgModel : '=?',
			kendoWidget : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?',
			todos : '=?',
			filtroIdSocioDeNegocio: '=?',
			filtroIdCliente: '=?',
			filtroIdPersona: '=?',
			filtroIdTipoDocumentoIdentidad: '=?',
			filtroDocumentoIdentidad: '=?',
			filtroIdTipoRelacion: '=?'
		},
		template:'<span class="k-textbox k-space-right" style="width: 100%;" >'+
			     '   <input class="bcs-autocomplete" kendo-auto-complete="kendoWidget"'+  
			     '                                k-ng-readonly="kNgReadonly" '+
			     '                                k-ng-disabled="kNgDisabled"'+ 
			     '                                k-options="kOptions" '+
			     '                                ng-model="ngModel" '+
			     '                                k-ng-model="kNgModel"'+
			     '                                k-ng-delay="kOptions"'+
			     '                                />                    '+
			     '   <a href="#" ng-click="fnBusquedaRelacionado()" class="k-select k-icon k-i-search">&nbsp;</a>'+
				 '</span>',
		controller : function($scope){
			this.getSeleccionado = function(){
				return ($scope.datosRelacionados && $scope.datosRelacionados.IdPersona);
			}
		},		
		link: function(scope, element, attrs, fn) {
			$timeout(function(){			
			//Valores iniciales
			scope.datosRelacionados={};
			scope.busquedaMinima = 2;				
			scope.postData = {};
			scope.ngModel='';
			if (scope.todos===true) scope.titulo=$translate.instant('UI_TODOS');
			else scope.titulo=$translate.instant('UI_RELACIONADOS');	
			if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
			if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;			
			//Evento de creacion del widget
			scope.$on("kendoWidgetCreated", function (event, widget) {
				if (widget.$angular_scope === scope) {
					//widget
					scope.kendoWidget = widget;
					scope.kendoWidget.popupCerrar = false;		
					//perdida del foco
					scope.kendoWidget.element.on("blur", function() {
						scope.kendoWidget.popupCerrar = true;				
						scope.kendoWidget.close();
						scope.kendoWidget.popupCerrar = false;
					});	
				}					
			});			
			
			//Busqueda de Instrumento 
			scope.fnBusquedaRelacionado = function(e) {
				//TODO: siguiente sprint
				
			};
			
			scope.$watch("datosRelacionados",function(newval,oldval) {
				if (newval===false) { //clean datos
					scope.datosRelacionados = { };
					scope.ngModel = "";
					//scope.kendoWidget.value(scope.fnTextoInput());
				}
			});
			
			scope.$watch('buscarInstrumento', function(newValue, oldValue) {
				if (scope.buscarInstrumento !== false) {
					scope.ngModel = scope.buscarInstrumento;
					if (scope.kendoWidget !== undefined) scope.kendoWidget.value(scope.buscarInstrumento);
					scope.kOptions.dataSource.read();	
					scope.buscarInstrumento=false;
				}
			});	
						
			var defaultOptions =
			{
					placeholder: scope.titulo,
					headerTemplate: '<div class="noDataMessage">No se encontrarón resultados.</div>',
		            dataTextField: "NombreCompleto",
		            dataValueField: "IdPersona",
		            filter: "startswith",
		            autoBind: false,
		            height: 300,
		            template: $.proxy(kendo.template("#= util.fnFormatHighlight(NombreCompleto+' -'+DocumentoIdentidadDefecto, KInputVal ) #"), this.dataSource),
					dataBound: function(e) {
					  console.log("bcs-combobox-relacionados event :: dataBound",e);
					  var noItems = this.list.find(".noDataMessage");
					  if (!this.dataSource.view()[0] && scope.kendoWidget.element.is(":focus")) {
						noItems.show();
						this.popup.open();						
					  } else {
						noItems.hide();
					  }
					},		
					open: function(e) {
						console.log("bcs-combobox-relacionados event :: open",e);
					},						
					close: function(e) {
						console.log("bcs-combobox-relacionados event :: close",e);
						var widget = e.sender;
						if (scope.ngModel.length>0 && !widget.popupCerrar && !this.dataSource.view()[0]) {
							e.preventDefault();
						}
					},		
					filtering: function(e) {
						console.log("bcs-combobox-relacionados event :: filtering",e);
						if (typeof scope.ngModel === 'undefined' || scope.ngModel.length<scope.busquedaMinima) {
							this.popup.close();	
							e.preventDefault();
							scope.datosRelacionados={};
						}
					},					
					select: function(e) {
						console.log("bcs-combobox-relacionados event :: select",e);
						var dataItem = this.dataItem(e.item.index());
						scope.datosRelacionados=dataItem;
					},    
					change: function(e) {
						console.log("bcs-combobox-relacionados event :: change",e);
					},    					
		            dataSource: new kendo.data.DataSource({		            			            
		            	type: "json",
		            	serverFiltering: true,		            	
		            	serverSorting: false, 
		            	serverPaging : false,
                        transport : {                                                                          
                                     read: function(options) {
                                          $.ajax({
                                          url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=176&r=P_OUT_PERSONASRELACIONADAS",
                                          dataType: "json", 
                                          type: "POST",
                                          contentType:"application/json; charset=utf-8",
                                          data: JSON.stringify(scope.postData),
                                          success: function(result) { 
                                        	  var temp=scope.ngModel.toUpperCase();
                                              options.success(result);   
                                              scope.ngModel=temp;
                                              scope.kendoWidget.value(temp);
                                          },
                                          error: function(result) {
                                            options.error(result);
                                          }});
                                    }                                                                                
                        },  
		            	requestStart: function(e) {
		            		console.log("bcs-combobox-relacionados event :: requestStart",e);
							//filtros Iniciales antes de llamar		  
		            		var texto=(scope.ngModel + '').replace(/%/g, '~');
							if (typeof scope.ngModel === 'undefined' || scope.ngModel.length<scope.busquedaMinima) {
								e.preventDefault();	
								scope.datosRelacionados={};
							}else {
								//Parametros de la llamada
								scope.postData.p_IdEmpresa = $rootScope.SesionIdEmpresa;
								//scope.postData.p_FiltroInstrumento =  (scope.ngModel||'').toUpperCase();								
								scope.postData.p_IdSocioDeNegocio =  scope.filtroIdSocioDeNegocio||0;
								scope.postData.p_IdPersona =  scope.filtroIdPersona||0;
								scope.postData.p_IdCliente  = scope.filtroIdCliente||0;
								scope.postData.p_IdTipoDocumentoIdentidad =  scope.filtroIdTipoDocumentoIdentidad||'';
								scope.postData.p_DocumentoIdentidad  = scope.filtroDocumentoIdentidad||'';
								scope.postData.p_IdTipoRelacion =  scope.filtroIdTipoRelacion||'';
							}									
						},                         
		            	requestEnd: function(e) {
		            		console.log("bcs-combobox-relacionados event :: requestEnd",e);
		            		/*if (typeof e.response !== 'undefined') {
			            		//Procesa datos
								var result = [];  
								var autosel = false;
								for (i=0;i<e.response.length;i++)
								{
									e.response[i].KInputVal=scope.ngModel;
									result.push(e.response[i]);
									//Auto selecciona
									if (scope.ngModel.toUpperCase()===e.response[i].NombreCompleto.toUpperCase()) {
										scope.datosRelacionados=e.response[i];
										autosel=true;
									}
								}
								if (!autosel) {
									scope.datosRelacionados={};
								}
								e.response = result;	
		            		}*/
		            		if (typeof e.response !== 'undefined' && e.response.P_OUT_PERSONASRELACIONADAS  !== 'undefined' ) {
			            		//Procesa datos
								var result = [];  
								var autosel = false;
								for (i=0;i<e.response.P_OUT_PERSONASRELACIONADAS.length;i++)
								{
									e.response.P_OUT_PERSONASRELACIONADAS[i].KInputVal=scope.ngModel;
									result.push(e.response.P_OUT_PERSONASRELACIONADAS[i]);
									//Auto selecciona
									if (scope.ngModel.toUpperCase()===e.response.P_OUT_PERSONASRELACIONADAS[i].NombreCompleto.toUpperCase()) {
										scope.datosRelacionados=e.response.P_OUT_PERSONASRELACIONADAS[i];
										autosel=true;
									}
								}
								if (!autosel) {
									scope.datosRelacionados={};
								}
								e.response = result;	
		            		}
		            	}            
		            })
			};						
			scope.kOptions = scope.kOptions || {};
			scope.kOptions = $.extend( {}, defaultOptions, scope.kOptions );	
		});
		}
	};
});
